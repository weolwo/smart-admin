package solvela.member.api;

/** 绑定 / 更换手机号的结果。 */
public record MemberPhoneBindResult(PhoneBindFailReason reason) {

    private static final MemberPhoneBindResult OK = new MemberPhoneBindResult(null);

    public boolean success() {
        return reason == null;
    }

    public static MemberPhoneBindResult ok() {
        return OK;
    }

    public static MemberPhoneBindResult fail(PhoneBindFailReason reason) {
        return new MemberPhoneBindResult(reason);
    }
}
