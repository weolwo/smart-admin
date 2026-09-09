package solvela.member.api;

/**
 * 发送邮箱验证码的结果。形状与 {@link MemberRegisterResult} 一致。
 *
 * @param reason            失败原因；成功时为 null
 * @param retryAfterSeconds 还要等多久，仅 {@link EmailCodeFailReason#TOO_FREQUENT}
 *                          与 {@link EmailCodeFailReason#DAILY_LIMIT_REACHED} 时有意义
 */
public record EmailCodeSendResult(EmailCodeFailReason reason, long retryAfterSeconds) {

    private static final EmailCodeSendResult OK = new EmailCodeSendResult(null, 0L);

    public boolean success() {
        return reason == null;
    }

    /**
     * 成功。
     *
     * <p>🔴 「成功」<b>不等于「真的发了一封信」</b>：邮箱没注册（登录/重置场景）
     * 或已被注册（注册场景）时，域会静默返回成功而不发信 ——
     * 区分这两种情况等于送出一个账号枚举接口。见 {@link EmailCodeFailReason} 的类注释。
     */
    public static EmailCodeSendResult ok() {
        return OK;
    }

    public static EmailCodeSendResult fail(EmailCodeFailReason reason) {
        return new EmailCodeSendResult(reason, 0L);
    }

    public static EmailCodeSendResult tooFrequent(long retryAfterSeconds) {
        return new EmailCodeSendResult(EmailCodeFailReason.TOO_FREQUENT, retryAfterSeconds);
    }

    public static EmailCodeSendResult dailyLimit(long retryAfterSeconds) {
        return new EmailCodeSendResult(EmailCodeFailReason.DAILY_LIMIT_REACHED, retryAfterSeconds);
    }
}
