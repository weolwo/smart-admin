package solvela.member.api;

/**
 * 设备注册结果。形状与 {@link MemberRegisterResult} 刻意保持一致。
 *
 * <h3>🔴 deviceToken 只在这一刻存在</h3>
 * 服务端<b>不存令牌</b>（它是自包含的，验签即可，见 {@code DeviceTokenCodec}）。
 * 客户端丢了就只能重新注册一台设备 —— 而那会在 {@code t_device} 里多出一行，
 * 也会消耗一次 IP 配额。所以客户端必须把它<b>持久化</b>，不是放内存。
 *
 * @param deviceToken       设备令牌原文，失败时为 null。<b>不要写进任何日志</b>
 * @param deviceId          设备号，失败时为 null。它不是秘密（令牌里就是明文），
 *                          回传给客户端是为了排查时能对上号
 * @param reason            失败原因；成功时为 null
 * @param retryAfterSeconds 还要等多久才能再试，仅 {@link DeviceRegisterFailReason#TOO_MANY_ATTEMPTS}
 *                          时有意义。给的是秒数，不是「请 10 分钟后重试」这句话
 */
public record DeviceRegisterResult(
        String deviceToken,
        String deviceId,
        DeviceRegisterFailReason reason,
        long retryAfterSeconds) {

    public boolean success() {
        return reason == null;
    }

    public static DeviceRegisterResult ok(String deviceToken, String deviceId) {
        return new DeviceRegisterResult(deviceToken, deviceId, null, 0L);
    }

    public static DeviceRegisterResult fail(DeviceRegisterFailReason reason) {
        return new DeviceRegisterResult(null, null, reason, 0L);
    }

    public static DeviceRegisterResult tooManyAttempts(long retryAfterSeconds) {
        return new DeviceRegisterResult(null, null, DeviceRegisterFailReason.TOO_MANY_ATTEMPTS, retryAfterSeconds);
    }
}
