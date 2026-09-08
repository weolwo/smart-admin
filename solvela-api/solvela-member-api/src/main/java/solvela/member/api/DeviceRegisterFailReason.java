package solvela.member.api;

/**
 * 设备注册的失败原因。
 *
 * <p>与 {@link RegisterFailReason} 同一个做法：域只说原因，<b>措辞由调用方定</b>。
 * 这里的取值刻意很少 —— 设备注册没有「查无此人」「密码错」那类需要含糊其辞的场景，
 * 它不涉及任何用户身份，说清楚不泄露任何东西。
 */
public enum DeviceRegisterFailReason {

    /**
     * deviceType 不在允许的取值里。
     *
     * <p>明说是安全的：这是一个纯粹的参数错误，客户端写错了就该知道自己写错了。
     */
    BAD_DEVICE_TYPE,

    /**
     * 同一 IP 签发设备过于频繁。
     *
     * <p>这是<b>整套设备防刷的第一道闸</b>：刷登录必须先刷设备注册，
     * 而正常用户一天不会装十次 App。见 {@code DeviceProperties}。
     */
    TOO_MANY_ATTEMPTS,
}
