package solvela.member.api;

/**
 * 用邮箱验证码重置密码。
 *
 * <p>⚠️ 这条链路的验证码<b>威力最大</b>：拿到它就能改密码，等于账号易主。
 * 所以成功之后必须吊销该会员的<b>全部会话</b> —— 见 {@code MemberPasswordResetService}。
 *
 * @param email       账号邮箱
 * @param code        发到该邮箱的验证码（{@link EmailCodeScene#RESET_PASSWORD} 场景）
 * @param newPassword 新密码明文，强度校验在域内
 * @param clientIp    客户端 IP
 * @param deviceId    验签通过的设备号，允许为 null
 */
public record MemberPasswordResetCmd(
        String email,
        String code,
        String newPassword,
        String clientIp,
        String deviceId) {
}
