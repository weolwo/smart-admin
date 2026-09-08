package solvela.member.api;

/**
 * 设备注册入参。
 *
 * <h3>🔴 这里没有 deviceId —— 那正是整件事的关键</h3>
 * 设备号由<b>服务端</b>生成。客户端自报的话，脚本每次换一个 UUID，
 * {@code t_device} 就变成一张「攻击者想写多少行就写多少行」的表，设备维度的限流全部失效
 * （计数永远是 1）。签发之后，刷登录就必须先刷设备注册 ——
 * 而设备注册这一步可以让它很贵。
 *
 * <p>下面这几个字段仍然是客户端自报的，而且<b>没打算验</b>：它们只用于人工排查
 * （「这批设备都自称是 iOS 26 的 iPhone」本身就是个信号），不参与任何判断。
 * 唯一例外是 deviceType，它决定限流阈值走哪一套（H5 的可信度天然低于 App），
 * 所以它必须在一个封闭的取值集合里。
 *
 * @param deviceType APP/H5/WECHAT/PC，不在集合里直接拒绝
 * @param model      品牌型号，如 iPhone 15 Pro，允许为 null
 * @param osVersion  系统版本。区分 iOS/Android 靠它 —— deviceType 只到端
 * @param appVersion 应用版本，允许为 null
 * @param clientIp   客户端 IP，允许为 null（拿不到时放行并打警告，理由见 DeviceService）
 */
public record DeviceRegisterCmd(
        String deviceType,
        String model,
        String osVersion,
        String appVersion,
        String clientIp) {
}
