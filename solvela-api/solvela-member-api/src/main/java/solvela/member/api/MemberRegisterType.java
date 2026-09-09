package solvela.member.api;

/**
 * 注册方式。与 {@link MemberLoginType} 同一个判据：<b>扁平枚举 + 显式传入</b>。
 *
 * @Date 2026-09-09
 */
public enum MemberRegisterType {

    /**
     * 手机号 + 密码。
     *
     * <p>🔴 <b>这条通道至今没有任何验证</b>：全仓没有短信基础设施，
     * 所以任何人都能拿别人的手机号建号，而 {@code uk_mbr_phone_hash} 是唯一约束 ——
     * 号被占了，真机主就注册不了了。
     * 见 {@code MemberRegisterService} 的类注释，那段话从 2026-08 挂到现在。
     */
    PHONE_PASSWORD,

    /**
     * 邮箱 + 验证码，密码<b>可选</b>。
     *
     * <p>这是本项目<b>第一条真正被验证过</b>的注册通道：验码即证明这个邮箱归他，
     * 拿别人的邮箱注册不了。
     *
     * <p>密码可选是 DDL 早就留好的口子 —— {@code t_member.password} 允许 NULL，
     * 列注释写着「验证码登录可为空」。不设密码的会员之后走
     * {@link MemberLoginType#EMAIL_CODE} 登录。
     */
    EMAIL_CODE,
}
