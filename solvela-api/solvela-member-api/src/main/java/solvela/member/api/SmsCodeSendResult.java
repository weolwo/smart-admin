package solvela.member.api;

/**
 * 发送短信验证码的结果。形状与 {@link EmailCodeSendResult} 一致。
 *
 * @param reason            失败原因；成功时为 null
 * @param retryAfterSeconds 还要等多久，仅被限时有意义
 */
public record SmsCodeSendResult(SmsCodeFailReason reason, long retryAfterSeconds) {

    private static final SmsCodeSendResult OK = new SmsCodeSendResult(null, 0L);

    public boolean success() {
        return reason == null;
    }

    public static SmsCodeSendResult ok() {
        return OK;
    }

    public static SmsCodeSendResult fail(SmsCodeFailReason reason) {
        return new SmsCodeSendResult(reason, 0L);
    }

    public static SmsCodeSendResult tooFrequent(long retryAfterSeconds) {
        return new SmsCodeSendResult(SmsCodeFailReason.TOO_FREQUENT, retryAfterSeconds);
    }

    public static SmsCodeSendResult dailyLimit(long retryAfterSeconds) {
        return new SmsCodeSendResult(SmsCodeFailReason.DAILY_LIMIT_REACHED, retryAfterSeconds);
    }
}
