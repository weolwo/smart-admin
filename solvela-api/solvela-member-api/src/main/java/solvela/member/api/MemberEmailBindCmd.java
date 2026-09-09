package solvela.member.api;

/**
 * 绑定/更换邮箱的入参。
 *
 * <h3>🔴 换绑为什么要多验一次「你是原主」</h3>
 * 只验新邮箱的话，有一条完整的<b>权限提升链</b>：
 * <pre>
 *   会话被盗 → 换绑成攻击者自己的邮箱 → 用「忘记密码」重置 → 永久接管账号
 * </pre>
 * 每一步单看都合法，合起来就是「偷到一个 token 等于拿走这个账号」。
 * 而 C 端令牌有 30 天有效期，token 泄露的机会比密码泄露多得多。
 *
 * <p>所以<b>换绑</b>（会员已经有邮箱）必须额外证明身份，二选一：
 * <ul>
 *   <li>{@link #currentPassword} —— 当前密码；</li>
 *   <li>{@link #oldEmailCode} —— 发到<b>旧邮箱</b>的验证码。给没设过密码的会员用
 *       （邮箱验证码注册出来的那批，{@code t_member.password} 是 NULL）。</li>
 * </ul>
 *
 * <p><b>首次绑定</b>不要求这一步：那时账号上还没有任何邮箱可言，
 * 多问一道只是提高门槛，堵不住上面那条链（链条的起点就是「已有邮箱可换」）。
 *
 * @param memberId        当前登录会员，由网关从令牌解析后填入，<b>不接受客户端传</b>
 * @param newEmail        要绑定的新邮箱
 * @param newEmailCode    发到新邮箱的验证码，<b>必填</b> —— 它证明这个邮箱归他
 * @param currentPassword 当前密码，换绑时二选一
 * @param oldEmailCode    发到旧邮箱的验证码，换绑时二选一
 * @param clientIp        客户端 IP
 * @param deviceId        验签通过的设备号，允许为 null
 */
public record MemberEmailBindCmd(
        Long memberId,
        String newEmail,
        String newEmailCode,
        String currentPassword,
        String oldEmailCode,
        String clientIp,
        String deviceId) {
}
