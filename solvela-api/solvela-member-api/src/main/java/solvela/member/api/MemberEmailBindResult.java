package solvela.member.api;

/**
 * 绑定邮箱的结果。
 *
 * @param reason 失败原因；成功时为 null
 */
public record MemberEmailBindResult(EmailBindFailReason reason) {

    private static final MemberEmailBindResult OK = new MemberEmailBindResult(null);

    public boolean success() {
        return reason == null;
    }

    public static MemberEmailBindResult ok() {
        return OK;
    }

    public static MemberEmailBindResult fail(EmailBindFailReason reason) {
        return new MemberEmailBindResult(reason);
    }
}
