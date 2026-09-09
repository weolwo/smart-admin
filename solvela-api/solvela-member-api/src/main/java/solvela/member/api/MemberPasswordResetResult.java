package solvela.member.api;

/**
 * 重置密码的结果。
 *
 * @param reason          失败原因；成功时为 null
 * @param revokedSessions 成功时吊销掉的会话数。<b>要返回给用户看</b> ——
 *                        「已在 3 台设备上退出登录」是他判断「刚才是不是别人在动我账号」的依据
 */
public record MemberPasswordResetResult(PasswordResetFailReason reason, int revokedSessions) {

    public boolean success() {
        return reason == null;
    }

    public static MemberPasswordResetResult ok(int revokedSessions) {
        return new MemberPasswordResetResult(null, revokedSessions);
    }

    public static MemberPasswordResetResult fail(PasswordResetFailReason reason) {
        return new MemberPasswordResetResult(reason, 0);
    }
}
