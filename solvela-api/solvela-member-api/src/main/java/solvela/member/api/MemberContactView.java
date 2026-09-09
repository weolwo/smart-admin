package solvela.member.api;

/**
 * 当前会员的联系方式，<b>全部脱敏</b>。给「账号安全」那一页用。
 *
 * <h3>🔴 为什么不放进 MemberPrincipal</h3>
 * {@code MemberPrincipal} 会进 Redis 缓存、会进日志。手机号和邮箱是 PII，
 * 整套 {@code PiiCipher} / {@code PiiHasher} 就是为了让它们不以明文形式散出去 ——
 * 塞进那个对象等于把加密白做了。
 *
 * <p>所以它是<b>单独一次调用</b>，而且下发的是打过码的值：
 * 页面上要显示的本来就只是 {@code 138****8000}，明文一次都不需要出域。
 * 这条路径在 {@code MemberProfile} 的注释里早就写着了。
 *
 * @param phone          脱敏手机号，未绑定为 null
 * @param email          脱敏邮箱，未绑定为 null
 * @param passwordSet    是否设过密码。<b>换绑邮箱的界面靠它决定给什么选项</b> ——
 *                       没设过密码的人只能走「旧邮箱验证码」那条路，
 *                       给他一个「输入当前密码」的框是让他对着一个填不了的东西发愁
 */
public record MemberContactView(String phone, String email, boolean passwordSet) {
}
