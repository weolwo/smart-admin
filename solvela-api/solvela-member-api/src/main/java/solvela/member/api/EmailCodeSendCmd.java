package solvela.member.api;

/**
 * 发送邮箱验证码的入参。
 *
 * @param scene           用途。<b>必须由调用方显式指定</b> —— 四个场景各自一套码、
 *                        一封信、一套限频，猜错一个就是把绑定邮箱的码发成了重置密码的
 * @param email           收件邮箱，任意格式，域内会规范化
 * @param clientIp        客户端 IP，允许为 null（拿不到时放行并打警告）
 * @param currentMemberId 当前登录会员，仅 {@link EmailCodeScene#BIND} 用得到，
 *                        其余场景为 null
 */
public record EmailCodeSendCmd(EmailCodeScene scene, String email, String clientIp, Long currentMemberId) {
}
