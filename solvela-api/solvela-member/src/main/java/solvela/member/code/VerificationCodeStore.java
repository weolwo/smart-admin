package solvela.member.code;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import solvela.base.module.redis.RedisService;
import solvela.base.util.SolvelaRandomUtil;
import solvela.base.util.SolvelaStringUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 验证码的<b>存储与校验</b>：生成、冷却、日限、验错计数、消费。<b>与通道无关</b>。
 *
 * <h3>为什么单独一个类</h3>
 * 邮箱和短信是两条通道，但下面这些<b>安全攸关</b>的性质对两者完全一样：
 * <ul>
 *   <li>失败次数上限（6 位码只有 100 万种，没有上限的话脚本几秒钟穷举完）；</li>
 *   <li>验错写回时<b>保住剩余有效期</b>（给完整 TTL 的话，一直猜就永不过期）；</li>
 *   <li>验过即消费（不然拿到一次码能反复用，重置密码那条链路上就是反复改密码）；</li>
 *   <li>常数时间比较（逐字符短路会把「按位试」的代价从 100 万降到 60 次）；</li>
 *   <li>发送时间拼在值里，不另存键（两个键会各自过期，出现「码还在、计数没了」）。</li>
 * </ul>
 *
 * <p>🔴 <b>照抄一份给短信，这五条迟早有一条在某一侧漂移</b>，
 * 而漂移的表现是「邮箱那边的上限生效、短信那边悄悄没有」—— 没有任何报错。
 * 所以它们只存在一份，通道由 {@code channel} 参数区分。
 *
 * <h3>本类不知道「邮箱」「手机号」是什么</h3>
 * 入参是<b>已经算好的摘要</b>。规范化（大小写、分隔符、国家码）与打码展示
 * 是各通道自己的规则，混进来只会让每种都做得半吊子 —— 判据同
 * {@code PiiHasher} 刻意不做规范化。
 *
 * @Date 2026-09-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeStore {

    private static final String KEY_CODE = "mbr:code:";

    private static final String KEY_SEND_TARGET = "mbr:code:send:target:";

    private static final String KEY_SEND_IP = "mbr:code:send:ip:";

    /** 值的分隔符。{@code |} 不会出现在数字里，比下划线更不容易与内容混淆。 */
    private static final String SEP = "|";

    private final RedisService redisService;

    private final VerificationCodeProperties properties;

    /**
     * 生成一个码并存下来；被冷却或日限挡住时返回对应结果。
     *
     * <p>分支顺序：<b>冷却 → 日限 → 生成</b>。冷却在前，因为它比日限便宜，
     * 而且连点是最常见的情形。
     *
     * @param channel      通道标识，进 key。两条通道的码<b>互不相干</b>
     * @param scene        用途，进 key。场景不隔离的话，一个为绑定发的码能拿去重置密码
     * @param identityHash 邮箱或手机号的摘要。<b>不放明文</b> —— Redis 被 dump、
     *                     被运维 {@code KEYS *} 打印到终端时，不该出现真实联系方式
     * @param clientIp     允许为 null（拿不到时只是少一层保护，不是拒绝）
     */
    public CodeIssueOutcome issue(String channel, String scene, String identityHash, String clientIp) {
        String codeKey = codeKey(channel, scene, identityHash);

        long cooldownLeft = cooldownRemaining(codeKey);
        if (cooldownLeft > 0) {
            return CodeIssueOutcome.tooFrequent(cooldownLeft);
        }

        CodeIssueOutcome quota = consumeDailyQuota(channel, scene, identityHash, clientIp);
        if (quota != null) {
            return quota;
        }

        String code = SolvelaRandomUtil.secureRandomNumbers(properties.length());
        redisService.set(codeKey, code + SEP + System.currentTimeMillis() + SEP + 0,
                properties.ttl().toSeconds());
        return CodeIssueOutcome.ok(code);
    }

    /**
     * 丢弃刚发的码。<b>发送失败时必须调</b>：留着它会让冷却生效，
     * 于是用户在收不到的同时还被告知「请稍后再试」。
     */
    public void discard(String channel, String scene, String identityHash) {
        redisService.delete(codeKey(channel, scene, identityHash));
    }

    /**
     * 校验并<b>消费</b>验证码。通过之后同一个码不能再用第二次。
     *
     * <p>🔴 验错也要写回 Redis（次数 +1），而且<b>必须保住剩余有效期</b>：
     * 重新 set 时如果给一个完整的 ttl，攻击者只要一直猜，这个码就永远不过期 ——
     * 有效期形同虚设。
     */
    public CodeVerifyOutcome verify(String channel, String scene, String identityHash, String input) {
        if (SolvelaStringUtil.isBlank(input)) {
            return CodeVerifyOutcome.NOT_FOUND;
        }
        String codeKey = codeKey(channel, scene, identityHash);
        String stored = redisService.get(codeKey);
        if (stored == null) {
            return CodeVerifyOutcome.NOT_FOUND;
        }
        String[] parts = stored.split("\\" + SEP, -1);
        if (parts.length != 3) {
            // 只可能是有人手改了 Redis，或键撞了别的系统。当成没有，并清掉
            log.warn("【验证码】值的形状不对，已清理: channel={}, scene={}", channel, scene);
            redisService.delete(codeKey);
            return CodeVerifyOutcome.NOT_FOUND;
        }

        // 🔴 常数时间比较。逐字符短路比较会让「猜对了前几位」慢一点点，
        //    而这是个 6 位数字码 —— 按位试的代价从 100 万降到 60 次
        if (MessageDigest.isEqual(parts[0].getBytes(StandardCharsets.UTF_8),
                input.trim().getBytes(StandardCharsets.UTF_8))) {
            // 消费掉：一个码只能用一次。不删的话，同一个码在有效期内可以反复使用，
            // 而「重置密码」那条链路上这意味着攻击者拿到一次码就能改无数次密码
            redisService.delete(codeKey);
            return CodeVerifyOutcome.OK;
        }

        return recordMismatch(codeKey, parts);
    }

    /**
     * 记一次验错。用尽次数即作废。
     *
     * <p>作废而不是「锁定一段时间」：锁定会给攻击者一个「这个目标刚才发过码」的信号，
     * 而重发的成本本来就很低，作废对真实用户只是多点一次「重新发送」。
     */
    private CodeVerifyOutcome recordMismatch(String codeKey, String[] parts) {
        long ttlLeft = redisService.getExpire(codeKey);
        int attempts = parseInt(parts[2]) + 1;
        if (attempts >= properties.maxVerifyAttempts() || ttlLeft <= 0) {
            redisService.delete(codeKey);
            return attempts >= properties.maxVerifyAttempts()
                    ? CodeVerifyOutcome.TOO_MANY_ATTEMPTS
                    // TTL 已经没了，说明这个码刚好在这一刻过期
                    : CodeVerifyOutcome.NOT_FOUND;
        }
        // 用【剩余】有效期写回，不是完整的 ttl —— 见 verify 的方法注释
        redisService.set(codeKey, parts[0] + SEP + parts[1] + SEP + attempts, ttlLeft);
        return CodeVerifyOutcome.MISMATCH;
    }

    /**
     * 还差多少秒才能重发；可以发返回 0。
     *
     * <p>发送时间<b>拼在值里</b>，不另存一个键 —— 两个键会各自过期，
     * 出现「码还在、时间戳没了」这种半个状态。
     */
    private long cooldownRemaining(String codeKey) {
        String stored = redisService.get(codeKey);
        if (stored == null) {
            return 0L;
        }
        String[] parts = stored.split("\\" + SEP, -1);
        if (parts.length != 3) {
            return 0L;
        }
        long elapsed = System.currentTimeMillis() - parseLong(parts[1]);
        long cooldownMillis = properties.resendCooldown().toMillis();
        return elapsed >= cooldownMillis ? 0L : (cooldownMillis - elapsed) / 1000 + 1;
    }

    /**
     * 消耗当天配额，超限返回失败结果、没超返回 null。
     *
     * <p>两个维度都要过：按目标限挡「盯着一个人发」，按 IP 限挡「拿一堆地址群发」。
     * 只做前者的话，一个脚本换着目标发，每个都不超限，而总量已经足够
     * 让域名进黑名单（邮箱）或把短信费烧光（短信）。
     */
    private CodeIssueOutcome consumeDailyQuota(String channel, String scene,
                                               String identityHash, String clientIp) {
        long window = properties.dailyWindow().toSeconds();

        String targetKey = redisService.generateRedisKey(
                KEY_SEND_TARGET, channel + ":" + scene + ":" + identityHash);
        if (redisService.increment(targetKey, window) > properties.getMaxSendPerTargetPerDay()) {
            return CodeIssueOutcome.dailyLimit(Math.max(1L, redisService.getExpire(targetKey)));
        }

        if (SolvelaStringUtil.isBlank(clientIp)) {
            log.warn("【验证码】拿不到客户端 IP，本次发送【未受 IP 限频保护】, channel={}, scene={}", channel, scene);
            return null;
        }
        // IP 维度<b>跨场景合计，但按通道分开</b>：跨场景是因为一个脚本可以轮着四个场景发；
        // 按通道分开是因为两条通道的成本完全不同 —— 短信一条几分钱，邮件不要钱
        String ipKey = redisService.generateRedisKey(KEY_SEND_IP, channel + ":" + clientIp);
        if (redisService.increment(ipKey, window) > properties.maxSendPerIpPerDay(channel)) {
            return CodeIssueOutcome.dailyLimit(Math.max(1L, redisService.getExpire(ipKey)));
        }
        return null;
    }

    private String codeKey(String channel, String scene, String identityHash) {
        return redisService.generateRedisKey(KEY_CODE, channel + ":" + scene + ":" + identityHash);
    }

    private static int parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            // 读不出来时当【已经错满】处理，而不是当 0：宁可让用户重发一次，
            // 也不能因为一个读不出来的计数把次数上限变成无限
            return Integer.MAX_VALUE - 1;
        }
    }

    private static long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            // 读不出来时当【很久以前发的】，也就是允许重发。
            // 与上面相反：这里放宽的代价只是多发一次，收紧的代价是用户再也发不出来
            return 0L;
        }
    }
}
