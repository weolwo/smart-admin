package solvela.member.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import solvela.base.mail.MailService;
import solvela.base.mail.MailTemplateCodeEnum;
import solvela.base.domain.SystemEnvironment;
import solvela.base.module.redis.RedisService;
import solvela.base.util.SolvelaRandomUtil;
import solvela.base.util.SolvelaStringUtil;
import solvela.crypto.PiiHasher;
import solvela.member.api.EmailCodeFailReason;
import solvela.member.api.EmailCodeScene;
import solvela.member.api.EmailCodeSendResult;
import solvela.member.api.EmailCodeVerifyResult;
import solvela.member.api.MailDelivery;
import solvela.member.util.MemberEmailUtil;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员邮箱验证码：发送与校验。<b>只管码本身，不管业务</b>。
 *
 * <h3>职责边界</h3>
 * 本类回答的是「这个邮箱在这个场景下拿到过一个有效的码吗」。
 * <b>「这个邮箱能不能注册」「这个邮箱有没有对应的会员」不在这里</b> —— 那是各条链路自己的事。
 *
 * <p>🔴 而且那些判断的结果<b>绝不能反映在发码接口的返回值里</b>：
 * 「已注册 / 未注册」两种回答不同，发码接口就成了一个账号枚举器。
 * 正确做法是两种情况都静默成功（返回 ok 但不真的发信），
 * 与 {@code LoginService.requireSendableEmployee} 对「账号不存在」的处理同一个思路。
 *
 * <h3>Redis 里存什么</h3>
 * <pre>
 *   mbr:email:code:{场景}:{邮箱摘要}   -> 验证码|发送毫秒|已错次数
 *   mbr:email:send:mail:{场景}:{摘要}  -> 当天该邮箱的发送计数
 *   mbr:email:send:ip:{ip}             -> 当天该 IP 的发送计数（跨场景合计）
 * </pre>
 *
 * <p><b>键里放邮箱摘要而不是明文</b>：Redis 被 dump、被误导出、被运维 {@code KEYS *}
 * 打印到终端时，不该出现一串真实邮箱地址。理由与 {@code MemberRedisTokenStore}
 * 存令牌摘要一样 —— 这里额外的好处是，摘要用的是已有的 {@link PiiHasher}，
 * 与 {@code t_member.email_hash} 同一把密钥、同一个值，排查时能直接对上。
 *
 * <p><b>三样东西挤在一个值里</b>（码、发送时间、错误次数）而不是三个键：
 * 三个键会各自过期，出现「码还在、计数没了」这种半个状态 ——
 * 而那个状态恰好把失败次数上限清零了。这一条沿用管理端的做法并把它推到了极致。
 *
 * @Date 2026-09-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberEmailCodeService {

    private static final String KEY_CODE = "mbr:email:code:";

    private static final String KEY_SEND_MAIL = "mbr:email:send:mail:";

    private static final String KEY_SEND_IP = "mbr:email:send:ip:";

    /** 值的分隔符。{@code |} 不会出现在数字里，比下划线更不容易与内容混淆。 */
    private static final String SEP = "|";

    private final RedisService redisService;

    private final MailService mailService;

    private final PiiHasher piiHasher;

    private final MemberEmailCodeProperties properties;

    private final SystemEnvironment systemEnvironment;

    /**
     * 🔴 生产环境不许用 LOG 通道，<b>启动即失败</b>。
     *
     * <p>把验证码打进日志，等于把「接管任意账号」的能力交给每一个能看日志的人 ——
     * 而日志的访问面通常比数据库宽得多，还会被采集到 ELK、被转发、被长期保留。
     *
     * <p>为什么是失败而不是静默降级成 MAIL：降级的话，有人在生产配了 LOG
     * 却什么都没发生，他会以为这个开关不生效、转头去别处找原因 ——
     * 而真正的问题（生产配置文件里躺着一个危险开关）没有任何人知道。
     * 判据同 {@code PiiHasher} / {@code DeviceTokenCodec} 的「不给默认密钥」。
     */
    @jakarta.annotation.PostConstruct
    void checkTransport() {
        if (properties.getTransport() != MemberEmailCodeProperties.Transport.LOG) {
            return;
        }
        if (systemEnvironment.isProd()) {
            throw new IllegalStateException(
                    "solvela.member.email-code.transport=LOG 不允许在生产环境使用："
                            + "它会把每个人的验证码打进日志，而看得到日志的人就能接管任意账号。"
                            + "生产请配成 MAIL，并配好 spring.mail.*。");
        }
        log.warn("【邮箱验证码】当前是 LOG 通道：不发信，验证码直接打进日志。"
                + "搜关键字【邮箱验证码-LOG】。当前环境 {}。"
                + "🔴 这个开关只允许在非生产环境使用，配到生产会启动失败。",
                systemEnvironment.getCurrentEnvironment());
    }

    /**
     * 发一封验证码邮件。
     *
     * <p>分支顺序有讲究：<b>格式 → 冷却 → 日限 → 发信</b>。
     * 格式校验排最前面，因为它不查任何存储、不泄露任何信息 ——
     * 让一个手滑打错格式的用户去消耗当天配额没有道理（判据同
     * {@code MemberRegisterService.register}）。
     *
     * @param clientIp 允许为 null。拿不到时<b>放行并打警告</b>，不是拒绝 ——
     *                 一律拒绝会让任何一次取 IP 失败变成「全站收不到验证码」
     */
    public EmailCodeSendResult send(EmailCodeScene scene, String rawEmail, String clientIp) {
        return send(scene, rawEmail, clientIp, MailDelivery.DELIVER);
    }

    /**
     * 发一封验证码邮件，可以指定<b>不真的寄出去</b>。
     *
     * <p>{@link MailDelivery#SUPPRESS} 用于「这个邮箱没有对应会员」的情形：
     * 照常存码、照常计入限频，只是不寄信。这样有账号和没账号两条路径的<b>后续行为逐字相同</b>，
     * 否则校验那一步会把发送这一步藏住的东西漏出去。理由见 {@link MailDelivery} 的类注释。
     */
    public EmailCodeSendResult send(EmailCodeScene scene, String rawEmail, String clientIp,
                                    MailDelivery delivery) {
        String email = MemberEmailUtil.normalize(rawEmail);
        if (email == null) {
            return EmailCodeSendResult.fail(EmailCodeFailReason.BAD_EMAIL_FORMAT);
        }
        String emailHash = piiHasher.hash(email);
        String codeKey = codeKey(scene, emailHash);

        long cooldownLeft = cooldownRemaining(codeKey);
        if (cooldownLeft > 0) {
            return EmailCodeSendResult.tooFrequent(cooldownLeft);
        }

        EmailCodeSendResult quota = consumeDailyQuota(scene, emailHash, clientIp);
        if (quota != null) {
            return quota;
        }

        return generateAndSend(scene, email, codeKey, delivery);
    }

    /**
     * 校验并<b>消费</b>验证码。通过之后同一个码不能再用第二次。
     *
     * <p>🔴 验错也要写回 Redis（次数 +1），而且<b>必须保住剩余有效期</b>：
     * 重新 set 时如果把 TTL 设成一个完整的 ttl，攻击者只要一直猜，
     * 这个码就永远不过期 —— 有效期形同虚设。
     */
    public EmailCodeVerifyResult verify(EmailCodeScene scene, String rawEmail, String inputCode) {
        String email = MemberEmailUtil.normalize(rawEmail);
        if (email == null || SolvelaStringUtil.isBlank(inputCode)) {
            return EmailCodeVerifyResult.NOT_FOUND;
        }
        String codeKey = codeKey(scene, piiHasher.hash(email));
        String stored = redisService.get(codeKey);
        if (stored == null) {
            return EmailCodeVerifyResult.NOT_FOUND;
        }
        String[] parts = stored.split("\\" + SEP, -1);
        if (parts.length != 3) {
            // 只可能是有人手改了 Redis，或键撞了别的系统。当成没有，并清掉
            log.warn("【邮箱验证码】值的形状不对，已清理: {}", stored);
            redisService.delete(codeKey);
            return EmailCodeVerifyResult.NOT_FOUND;
        }

        // 🔴 常数时间比较。逐字符短路比较会让「猜对了前几位」慢一点点，
        //    而这是个 6 位数字码 —— 按位试的代价从 100 万降到 60 次
        if (MessageDigest.isEqual(parts[0].getBytes(StandardCharsets.UTF_8),
                inputCode.trim().getBytes(StandardCharsets.UTF_8))) {
            // 消费掉：一个码只能用一次。不删的话，同一个码在有效期内可以反复使用，
            // 而「重置密码」那条链路上这意味着攻击者拿到一次码就能改无数次密码
            redisService.delete(codeKey);
            return EmailCodeVerifyResult.OK;
        }

        return recordMismatch(codeKey, parts);
    }

    /**
     * 记一次验错。用尽次数即作废。
     *
     * <p>作废而不是「锁定一段时间」：锁定会给攻击者一个「这个邮箱刚才发过码」的信号，
     * 而重发的成本本来就很低，作废对真实用户只是多点一次「重新发送」。
     */
    private EmailCodeVerifyResult recordMismatch(String codeKey, String[] parts) {
        long ttlLeft = redisService.getExpire(codeKey);
        int attempts = parseInt(parts[2]) + 1;
        if (attempts >= properties.maxVerifyAttempts() || ttlLeft <= 0) {
            redisService.delete(codeKey);
            return attempts >= properties.maxVerifyAttempts()
                    ? EmailCodeVerifyResult.TOO_MANY_ATTEMPTS
                    // TTL 已经没了，说明这个码刚好在这一刻过期
                    : EmailCodeVerifyResult.NOT_FOUND;
        }
        // 用【剩余】有效期写回，不是完整的 ttl —— 见 verify 的方法注释
        redisService.set(codeKey, parts[0] + SEP + parts[1] + SEP + attempts, ttlLeft);
        return EmailCodeVerifyResult.MISMATCH;
    }

    /**
     * 还差多少秒才能重发；可以发返回 0。
     *
     * <p>发送时间<b>拼在值里</b>，不另存一个键 —— 两个键会各自过期，
     * 出现「码还在、时间戳没了」这种半个状态。沿用管理端的做法。
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
     * <p>两个维度都要过：按邮箱限挡「盯着一个人发」，按 IP 限挡「拿一堆邮箱群发」。
     * 只做前者的话，一个脚本换着邮箱发，每个都不超限，而总量已经足够让域名被拉黑。
     */
    private EmailCodeSendResult consumeDailyQuota(EmailCodeScene scene, String emailHash, String clientIp) {
        long window = properties.dailyWindow().toSeconds();

        String mailKey = redisService.generateRedisKey(KEY_SEND_MAIL, scene.name() + ":" + emailHash);
        if (redisService.increment(mailKey, window) > properties.getMaxSendPerEmailPerDay()) {
            return EmailCodeSendResult.dailyLimit(Math.max(1L, redisService.getExpire(mailKey)));
        }

        if (SolvelaStringUtil.isBlank(clientIp)) {
            log.warn("【邮箱验证码】拿不到客户端 IP，本次发送【未受 IP 限频保护】, scene: {}", scene);
            return null;
        }
        // IP 维度<b>跨场景合计</b>：分场景计的话，一个脚本轮着四个场景发，
        // 每个都不超限，而总量翻了四倍
        String ipKey = redisService.generateRedisKey(KEY_SEND_IP, clientIp);
        if (redisService.increment(ipKey, window) > properties.getMaxSendPerIpPerDay()) {
            return EmailCodeSendResult.dailyLimit(Math.max(1L, redisService.getExpire(ipKey)));
        }
        return null;
    }

    /**
     * 生成、存 Redis、发信。
     *
     * <p>🔴 <b>先存后发</b>。反过来（先发后存）在存储失败时会留下一封
     * 「用户收到了但服务端不认」的验证码，用户会反复重试一件必然失败的事。
     * 而先存后发失败的话，用户只是没收到信，点一次重发即可 —— 代价小得多。
     *
     * <p>发信失败时把刚存的码<b>删掉</b>：留着它会让 60 秒冷却生效，
     * 于是用户在收不到信的同时还被告知「请稍后再试」。
     */
    private EmailCodeSendResult generateAndSend(EmailCodeScene scene, String email, String codeKey,
                                                MailDelivery delivery) {
        String code = SolvelaRandomUtil.secureRandomNumbers(properties.length());
        redisService.set(codeKey, code + SEP + System.currentTimeMillis() + SEP + 0,
                properties.ttl().toSeconds());

        if (delivery == MailDelivery.SUPPRESS) {
            // 码存了、限频计了，就是不寄。调用方（登录/重置密码）已经确认这个邮箱没有会员，
            // 而它必须返回一个与「有会员」完全一致的结果
            log.info("【邮箱验证码】邮箱无对应会员，已静默计入但不发信, scene: {}, email: {}",
                    scene, MemberEmailUtil.mask(email));
            return EmailCodeSendResult.ok();
        }

        if (properties.getTransport() == MemberEmailCodeProperties.Transport.LOG) {
            // 🔴 这里打【完整的邮箱和完整的码】—— 那就是这个通道的全部用途，
            //    打码就没法用了。生产环境走不到这里（checkTransport 已经拦下）
            log.warn("【邮箱验证码-LOG】scene={}, email={}, code={}, 有效期 {} 分钟",
                    scene, email, code, properties.ttl().toMinutes());
            return EmailCodeSendResult.ok();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("code", code);
            params.put("minutes", properties.ttl().toMinutes());
            mailService.sendMail(templateOf(scene), params, Collections.singletonList(email));
        } catch (Exception e) {
            redisService.delete(codeKey);
            // 🔴 打 error 而不是吞掉：这是【我们自己】的问题（SMTP 挂了、账号密码不对、
            //    被对方限流），不是正常的业务失败。日志里只放打码后的邮箱
            log.error("【邮箱验证码】发送失败, scene: {}, email: {}",
                    scene, MemberEmailUtil.mask(email), e);
            return EmailCodeSendResult.fail(EmailCodeFailReason.SEND_FAILED);
        }

        log.info("【邮箱验证码】已发送, scene: {}, email: {}", scene, MemberEmailUtil.mask(email));
        return EmailCodeSendResult.ok();
    }

    /**
     * 场景 → 邮件模板。
     *
     * <p>用 switch 表达式而不是把模板编码塞进 {@link EmailCodeScene}：
     * 那个枚举在 {@code solvela-member-api} 里，而契约模块<b>不该依赖 base-mail</b> ——
     * 它是给网关也要用的，而网关一个 base 模块都不许有。
     *
     * <p>新增场景时这里<b>编译不过</b>，而不是悄悄落进某个默认模板。
     */
    private static MailTemplateCodeEnum templateOf(EmailCodeScene scene) {
        return switch (scene) {
            case REGISTER -> MailTemplateCodeEnum.MEMBER_REGISTER_CODE;
            case LOGIN -> MailTemplateCodeEnum.MEMBER_LOGIN_CODE;
            case BIND -> MailTemplateCodeEnum.MEMBER_BIND_EMAIL_CODE;
            case RESET_PASSWORD -> MailTemplateCodeEnum.MEMBER_RESET_PASSWORD_CODE;
        };
    }

    private String codeKey(EmailCodeScene scene, String emailHash) {
        return redisService.generateRedisKey(KEY_CODE, scene.name() + ":" + emailHash);
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
            // 与上面相反：这里放宽的代价只是多发一封信，收紧的代价是用户再也发不出来
            return 0L;
        }
    }
}
