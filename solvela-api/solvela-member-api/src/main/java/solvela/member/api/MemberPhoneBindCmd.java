package solvela.member.api;

/**
 * 绑定 / 更换手机号的入参。形状与 {@link MemberEmailBindCmd} 一致。
 *
 * @param memberId        🔴 由网关从令牌解析后填入，<b>不接受客户端传</b>。
 *                        收客户端的 memberId 等于「说自己是谁就是谁」
 * @param newPhone        要绑的新手机号
 * @param newPhoneCode    新手机号收到的短信验证码
 * @param currentPassword 当前密码。<b>换绑</b>时与 {@code oldPhoneCode} 二选一
 * @param oldPhoneCode    旧手机号收到的短信验证码。换绑时与 {@code currentPassword} 二选一。
 *                        没设过密码的会员只有这一条路
 * @param clientIp        客户端 IP
 * @param deviceId        设备号，可能为 null
 */
public record MemberPhoneBindCmd(
        Long memberId,
        String newPhone,
        String newPhoneCode,
        String currentPassword,
        String oldPhoneCode,
        String clientIp,
        String deviceId) {
}
