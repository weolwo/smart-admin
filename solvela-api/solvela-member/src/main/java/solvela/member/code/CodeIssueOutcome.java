package solvela.member.code;

/**
 * 生成验证码的结果。<b>通道无关</b> —— 各通道自己把它翻译成自己的失败原因枚举。
 *
 * <p>不直接复用 {@code EmailCodeFailReason}：那是<b>契约</b>里的类型（会跨进程传），
 * 而本类是域内部的中间结果。让存储层依赖契约枚举，短信通道就得跟着用一个叫
 * {@code Email...} 的东西。
 *
 * @param code              生成的验证码；被限时为 null
 * @param status            结果
 * @param retryAfterSeconds 还要等多久，仅被限时有意义
 */
public record CodeIssueOutcome(String code, Status status, long retryAfterSeconds) {

    public enum Status {
        /** 生成成功，可以发了。 */
        OK,
        /** 上一条发出去还不到冷却时间。 */
        TOO_FREQUENT,
        /** 这个目标（或这个 IP）今天发得太多了。 */
        DAILY_LIMIT,
    }

    public boolean ok() {
        return status == Status.OK;
    }

    public static CodeIssueOutcome ok(String code) {
        return new CodeIssueOutcome(code, Status.OK, 0L);
    }

    public static CodeIssueOutcome tooFrequent(long retryAfterSeconds) {
        return new CodeIssueOutcome(null, Status.TOO_FREQUENT, retryAfterSeconds);
    }

    public static CodeIssueOutcome dailyLimit(long retryAfterSeconds) {
        return new CodeIssueOutcome(null, Status.DAILY_LIMIT, retryAfterSeconds);
    }
}
