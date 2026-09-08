package solvela.member.device;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备维度的拦截规则编码。
 *
 * <p>与 {@code RiskBlockCode} 同一个用意：<b>编码是给机器读的，文案是给人读的</b>。
 * dry-run 期间日志里出现的是这些编码，聚类统计靠它们 —— 提示文案会改、会带上具体数值，
 * 编码不会。改文案不该让统计图悄悄裂开。
 *
 * <p>🔴 新增规则一律进这个枚举，不要在 {@code DeviceGuard} 里随手写字符串：
 * 落进「未归类」的拦截，在漏斗上看起来就像没发生过。
 *
 * @Date 2026-09-09
 */
@Getter
@AllArgsConstructor
public enum DeviceGuardRule {

    /** 一天登录次数超限。最弱的一条，主要用来发现异常量级。 */
    LOGIN_TOO_MANY("LOGIN_TOO_MANY", "设备登录过于频繁"),

    /** 一小时失败次数超限。撞库的即时信号。 */
    FAIL_TOO_MANY("FAIL_TOO_MANY", "设备连续失败过多"),

    /** 一天碰过的不同会员数超限。<b>四条里信号最强的一条</b>：一机多号是养号的必然特征。 */
    MEMBER_FANOUT("MEMBER_FANOUT", "同一设备关联账号过多"),

    /** 一天注册数超限。压批量建号。 */
    REGISTER_TOO_MANY("REGISTER_TOO_MANY", "设备注册账号过于频繁"),
    ;

    private final String value;

    private final String desc;
}
