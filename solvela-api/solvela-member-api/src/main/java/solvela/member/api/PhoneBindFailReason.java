package solvela.member.api;

/**
 * 绑定 / 更换手机号失败的原因。域只说原因，措辞由调用方定。
 *
 * <p>结构与 {@link EmailBindFailReason} 逐条对应 —— 两条通道的规则本来就该一样，
 * 差异只应该来自它们真正不同的地方（校验格式的工具、发码的通道），
 * 而不是「当时谁写的、写的时候想到没想到」。
 */
public enum PhoneBindFailReason {

    /** 手机号格式不对。可以明说：一个非法的串本来就不可能是任何人的号码。 */
    BAD_PHONE_FORMAT,

    /** 没有待校验的短信验证码：从没发过，或已过期。 */
    SMS_CODE_EXPIRED,

    /** 短信验证码错误。 */
    SMS_CODE_MISMATCH,

    /** 短信验证码连续输错次数用尽，已作废，必须重新发送。 */
    SMS_CODE_LOCKED,

    /**
     * 这个手机号已经被别的账号绑走了。
     *
     * <p>必须如实说 —— 藏起来的话用户不知道该换个号还是去找回账号。
     * 判据同 {@code RegisterFailReason.PHONE_TAKEN}：这个枚举口子是
     * 「唯一约束」自带的，藏不掉，只能靠限频压速率。
     */
    PHONE_TAKEN,

    /**
     * 换绑，但没给原主证明。
     *
     * <h3>🔴 拦的是这条链</h3>
     * <pre>会话被盗 → 换绑成攻击者的手机号 → 用短信验证码登录 → 永久接管</pre>
     * 每一步单看都合法。而 C 端令牌有 30 天有效期，token 泄露的机会比密码多得多。
     * 首次绑定不需要 —— 那时这条链的起点还不存在。
     */
    REBIND_VERIFICATION_REQUIRED,

    /**
     * 换绑时给了原主证明，但不对。
     *
     * <p>与 {@link #REBIND_VERIFICATION_REQUIRED} 分开，是因为客户端要据此决定
     * <b>弹输入框</b>还是<b>报错让他重填</b>。
     */
    REBIND_VERIFICATION_FAILED,
}
