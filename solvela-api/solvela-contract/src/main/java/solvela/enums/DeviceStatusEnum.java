package solvela.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备处置档。取值与 {@code t_device.status} 的 DDL 注释一一对应。
 *
 * <h3>三档是「降级优先」的表达，不是三种严厉程度</h3>
 * 方案里那句「优先降级，不优先拒绝」落在这里：命中限流规则的第一反应是把设备
 * 推到 {@link #OBSERVE}（多要一道验证码），而不是 {@link #BANNED}（直接拒）。
 *
 * <p>理由是<b>误伤的代价不对称</b>：拦错一个正常用户，他不会来报障，只会不再打开；
 * 而多要一道验证码，正常用户只是多花十秒，刷子却要为每台设备付出一条短信的成本。
 *
 * <h3>🔴 谁能写哪一档</h3>
 * <ul>
 *   <li>{@link #NORMAL} ⇄ {@link #OBSERVE}：<b>自动</b>。命中规则降档，观察期满回档；</li>
 *   <li>{@link #BANNED}：<b>只能人工</b>，且 {@code operator} 必填。
 *       自动封禁一旦误判就是「这台机器再也用不了」，而受害者说不清自己遇到了什么。</li>
 * </ul>
 * 自动迁移必须带上原值做条件（见 {@code DeviceDao.transitStatus}），
 * 否则自动降档会把人工封禁悄悄覆盖回观察档。
 *
 * @Date 2026-09-10
 */
@Getter
@AllArgsConstructor
public enum DeviceStatusEnum implements BaseEnum {

    /** 正常。 */
    NORMAL(0, "正常"),

    /**
     * 观察：这台设备的登录要<b>多验一道短信验证码</b>。
     *
     * <p>不是拒绝 —— 真实用户照常进得来，只是多花十秒；
     * 而刷子每台设备都要付一条短信的钱，成本一下子上去了。
     */
    OBSERVE(1, "观察"),

    /** 封禁：注册与登录一律拒。<b>只能人工设置</b>。 */
    BANNED(2, "封禁"),
    ;

    private final Integer value;

    private final String desc;

    /** 库里读出来的整数 → 枚举。认不出的值当 {@link #NORMAL}，理由见下。 */
    public static DeviceStatusEnum of(Integer value) {
        if (value == null) {
            return NORMAL;
        }
        for (DeviceStatusEnum e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        /*
         * 🔴 认不出来时按 NORMAL 处理，不抛异常。
         * 这一列将来可能加档位，而滚动发布期间老实例会读到新值 ——
         * 那时抛异常等于「加了一个档位，老实例上全站登不进来」。
         * 放宽的代价只是少一次拦截，收紧的代价是一次事故。
         */
        return NORMAL;
    }
}
