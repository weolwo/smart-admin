package solvela.member.api;

/**
 * 绑定邮箱失败的原因。域只说原因，措辞由调用方定。
 */
public enum EmailBindFailReason {

    /** 邮箱格式不对。可以明说。 */
    BAD_EMAIL_FORMAT,

    /** 新邮箱的验证码没有 / 已过期。 */
    EMAIL_CODE_EXPIRED,

    /** 新邮箱的验证码错误。 */
    EMAIL_CODE_MISMATCH,

    /** 新邮箱的验证码错太多次，已作废。 */
    EMAIL_CODE_LOCKED,

    /**
     * 这个邮箱已经被<b>别的账号</b>绑走了。
     *
     * <p>必须如实说 —— 否则用户不知道该换个邮箱还是该去找回那个账号，只会一直点。
     * 这确实让一个已登录用户能探测「某个邮箱是不是本站账号」，
     * 但他本来就能拿那个邮箱去注册一次得到同样的信息（见 {@code RegisterFailReason#EMAIL_TAKEN}）。
     */
    EMAIL_TAKEN,

    /**
     * 换绑时没有提供「你是原主」的证明。
     *
     * <p>见 {@link MemberEmailBindCmd} 的类注释：这一道拦的是
     * 「会话被盗 → 换绑 → 重置密码 → 永久接管」那条链。
     */
    REBIND_VERIFICATION_REQUIRED,

    /** 换绑时提供的当前密码不对，或旧邮箱验证码不对。 */
    REBIND_VERIFICATION_FAILED,
}
