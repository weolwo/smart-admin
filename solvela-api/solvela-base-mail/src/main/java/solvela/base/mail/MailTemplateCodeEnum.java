package solvela.base.mail;

/**
 * 模版编码
 *
 * @Author 1024创新实验室-创始人兼主任:卓大
 * @Date 2024/8/5
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a> ，Since 2012
 */
public enum MailTemplateCodeEnum {

    /**
     * 管理端员工登录的双因子验证码。
     */
    LOGIN_VERIFICATION_CODE,

    // ------------------------------------------------------------------ 会员端（2026-09-09）

    /**
     * 会员注册验证码。
     *
     * <p>🔴 四个会员场景<b>各自一封信</b>，不共用一个模板。邮件里必须写清楚
     * 「这个码是用来做什么的」—— 否则用户没有任何依据判断该不该把它念给别人听，
     * 而「诱导用户走一次无害的流程、拿到那 6 位数、转手去改他的密码」
     * 正是共用模板会打开的攻击面。
     */
    MEMBER_REGISTER_CODE,

    /** 会员邮箱免密登录验证码。 */
    MEMBER_LOGIN_CODE,

    /** 会员绑定/更换邮箱的验证码。 */
    MEMBER_BIND_EMAIL_CODE,

    /**
     * 会员重置密码验证码。
     *
     * <p>⚠️ 这封信的措辞要最重 —— 拿到这个码就能改密码，等于账号易主。
     * 模板里要明确写「如果不是你本人操作，请立即修改密码」。
     */
    MEMBER_RESET_PASSWORD_CODE,

}
