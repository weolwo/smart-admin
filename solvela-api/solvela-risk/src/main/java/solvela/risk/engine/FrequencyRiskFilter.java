package solvela.risk.engine;

import solvela.base.module.redis.RedisService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import solvela.risk.PromotionConfig;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 第二梯队：高频防刷规则（Order = 20）。拦截绝大部分黑产/羊毛党，走 Redis 不打库。
 *
 * <h3>两个维度，不是一个</h3>
 * <ul>
 *   <li>{@code identify_limit} —— 单会员限领。一直都有；</li>
 *   <li>{@code device_limit} —— 单设备限领。<b>2026-09-09 才真正生效</b>。</li>
 * </ul>
 *
 * <p>🔴 后者此前是一列<b>死配置</b>：DDL 有列、Model 有字段、后台表单能填、VO 能返回，
 * 而没有任何一行代码读它。运营填上「单设备每日限领 1 次」、保存成功、列表里也显示着，
 * 然后以为限住了 —— 真实行为是完全不限。根因不在本类，在于发奖链路上<b>拿不到设备号</b>
 * （{@code ProposalRecordAddCommand} 里只有 memberId）。补齐那条链路
 * （网关验签 → X-Device-Id 请求头 → MDC → {@link RiskContext#getDeviceId()}）之后，
 * 这里才有东西可读。
 *
 * <h3>为什么扩本类而不是新建一个 Filter</h3>
 * 周期换算那段（{@link #calculateExpireSeconds}）踩过两个坑并把理由写在了注释里：
 * LIFETIME/MONTHLY 曾静默落进 default 变成一天、CUSTOM 窗口过期时不能放行。
 * 复制一份出去，两边迟早漂移，而漂移的表现是「同一个活动，会员限领对、设备限领不对」。
 *
 * <h3>⚠️ 还有两列仍然是死配置</h3>
 * {@code phone_limit} 与 {@code fingerprint_limit} <b>本次没有实现</b>，
 * 因为它们的数据源在这里不存在：
 * <ul>
 *   <li>phone —— 本模块只有 memberId，取手机号要反查会员域，那是一条不该开的跨域依赖。
 *       而 member 与 phone 是 1:1，{@code identify_limit} 已覆盖绝大多数场景，
 *       两者只在「注销释放号码后被别人注册」时才有差别；</li>
 *   <li>fingerprint —— 没有接任何厂商指纹，这个值永远是 null。</li>
 * </ul>
 * 🔴 它们已在后台表单里<b>禁用并标注原因</b>。能填但不生效的配置项是运营事故的种子 ——
 * 那正是 device_limit 之前的处境。
 */
@Slf4j
@Service
public class FrequencyRiskFilter implements IRiskFilter {
    @Resource
    private RedisService redisService;

    @Override
    public int getOrder() {
        return 20; // 第二优先级
    }

    @Override
    public RiskResult doFilter(RiskContext context) {
        PromotionConfig config = context.getConfig();
        long expireSeconds = calculateExpireSeconds(config);

        // 🔴 会员维度的 key 必须用 member_id：账号可改，改名之后同一个人换一个 key 重新计数，
        //    「单人限领」当场归零 —— 这正是薅羊毛要的效果，而且计数看上去完全正常
        RiskResult memberResult = checkDimension(config, expireSeconds,
                String.valueOf(context.getRequest().getMemberId()), config.getIdentifyLimit(),
                RiskBlockCode.USER_FREQUENCY_LIMIT, "您的参与太频繁了，请稍后再试");
        if (!memberResult.isPassed()) {
            return memberResult;
        }

        // 设备维度。deviceId 为 null 是【正常情况】——老客户端还没带设备令牌，
        // 内部补发、定时任务也根本不在请求线程上。此时这一维直接跳过，
        // 绝不能当成「可疑」拦掉：那会在灰度完成之前把老版本用户全挡在外面
        return checkDimension(config, expireSeconds,
                context.getDeviceId() == null ? null : "d_" + context.getDeviceId(),
                config.getDeviceLimit(),
                RiskBlockCode.DEVICE_FREQUENCY_LIMIT, "当前设备参与太频繁了，请稍后再试");
    }

    /**
     * 一个维度的固定窗口计数。
     *
     * <p>{@code subject} 为 null（拿不到这个维度的值）或 {@code limit <= 0}（没配）时直接放行。
     * <b>两者都必须放行</b>，而且理由不同：没配是运营的选择，拿不到是我们自己的能力边界，
     * 把后者当成命中会把老客户端全拦下来。
     *
     * @param subject 计数主体。会员维度是<b>裸的 member_id</b>，设备维度带 {@code d_} 前缀。
     *                <p>🔴 会员那一维<b>绝不能加前缀</b>，哪怕看起来更整齐：
     *                键名一变，Redis 里所有在飞的计数就全部作废 —— 配「终身限领 1 次」的活动
     *                会给每个已经领过的人重新发一次额度，而且没有任何报错。
     *                设备维度是新加的，没有存量，所以可以带前缀。
     *                <p>两者不会撞：member_id 是纯数字，device_id 是 32 位 hex 且带 {@code d_}。
     */
    private RiskResult checkDimension(PromotionConfig config, long expireSeconds,
                                      String subject, Integer limit,
                                      RiskBlockCode blockCode, String reason) {
        if (subject == null || limit == null || limit <= 0) {
            return RiskResult.pass();
        }
        // 缓存 Key 示例: risk:freq:DAILY:promo_1001:m_5579345309
        String redisKey = String.format("risk:freq:%s:promo_%d:%s",
                config.getLimitPeriod(), config.getId(), subject);

        // INCR 与首次 EXPIRE 必须原子完成 —— 分成两条命令时，两者之间一旦中断
        // 就会留下一个没有 TTL 的计数键，这个主体对这个活动的限流【再也不会解除】。
        // 原因见 RedisService.increment 的注释。
        long currentCount = redisService.increment(redisKey, expireSeconds);
        return currentCount > limit ? RiskResult.reject(blockCode, reason) : RiskResult.pass();
    }

    private static final long ONE_DAY = 86400L;

    /**
     * 「终身」的 TTL。
     *
     * <p>不能真的不设过期时间：{@code RedisService.increment} 的 Lua 脚本发现 PTTL == -1
     * 会重新补一次 EXPIRE，无 TTL 的键在这里表达不出来；而且没有 TTL 的计数键
     * 会在 Redis 里无限堆积，一场大促下来就是几百万个永不回收的 key。
     * 十年对一个营销活动而言就是终身。
     */
    private static final long LIFETIME_SECONDS = ONE_DAY * 365 * 10;

    /**
     * 把限制周期换算成计数键的 TTL。
     *
     * <p>🔴 这里原先只认 DAILY / WEEKLY，<b>LIFETIME 和 MONTHLY 都掉进 default 变成了一天</b>：
     * 配「终身限领 1 次」的活动，用户第二天就能再领一次，而且计数看上去完全正常、
     * 没有任何报错 —— 与「策略工厂拿 String 查枚举键」是同一类静默失效。
     *
     * <p>注意这是<b>固定窗口</b>而不是自然周期：TTL 只在计数键首次创建时设置
     * （见 {@code RedisService.increment} 的 Lua），所以 DAILY 的真实语义是
     * 「首次参与后的 24 小时内」，不是「自然日内」。要改成自然日得换成
     * {@code RedisService.currentDaySecond()}，那会改变所有存量活动的行为，故此处不动。
     */
    private long calculateExpireSeconds(PromotionConfig config) {
        String period = config.getLimitPeriod();
        if (period == null) {
            return ONE_DAY;
        }
        return switch (period) {
            case "DAILY" -> ONE_DAY;
            case "WEEKLY" -> ONE_DAY * 7;
            case "MONTHLY" -> ONE_DAY * 31;
            case "LIFETIME" -> LIFETIME_SECONDS;
            case "CUSTOM" -> customWindowSeconds(config);
            // 库里出现了字典外的取值：宁可按最短的一天算，也不能放行
            default -> {
                log.warn("【风控配置异常】未知的限制周期 [{}]，按一天处理。配置ID: {}", period, config.getId());
                yield ONE_DAY;
            }
        };
    }

    /**
     * 自定义窗口：计数键活到 {@code limit_end_time} 为止，窗口一过计数自然清零。
     *
     * <p>窗口已经结束（或压根没配结束时间）时<b>回退成一天而不是放行</b>：
     * EXPIRE 传非正数会让 Redis 直接删键，于是每次请求都是「计数 1」，限领当场归零 ——
     * 那正是薅羊毛要的效果。窗口过期还在发奖本身就是配置问题，此时宁可多限也不能不限。
     */
    private long customWindowSeconds(PromotionConfig config) {
        LocalDateTime end = config.getLimitEndTime();
        if (end == null) {
            log.warn("【风控配置异常】限制周期为 CUSTOM 但没有结束时间，按一天处理。配置ID: {}", config.getId());
            return ONE_DAY;
        }
        long seconds = Duration.between(LocalDateTime.now(), end).getSeconds();
        if (seconds <= 0) {
            log.warn("【风控配置异常】限制周期窗口已于 {} 结束，仍在发奖，按一天处理。配置ID: {}", end, config.getId());
            return ONE_DAY;
        }
        return seconds;
    }
}
