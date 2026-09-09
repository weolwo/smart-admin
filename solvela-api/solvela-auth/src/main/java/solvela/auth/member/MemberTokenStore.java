package solvela.auth.member;

import java.util.List;

/**
 * 会员令牌的签发、解析与吊销。
 *
 * <p>Redis 是<b>权威</b>：一个令牌有没有效，只看 Redis 里那条记录还在不在。
 * MySQL 那边的登录日志是给人看的轨迹，不参与判定。
 */
public interface MemberTokenStore {

    /**
     * 签发一个令牌。
     *
     * @param context 登录时的设备与位置，只用于「登录设备」列表的展示。
     *                全部允许为 null —— 拿不到就少显示一项，不该让登录失败
     */
    MemberAccessToken issue(Long memberId, MemberSessionContext context);

    /** 令牌 → 会员号。无效返回 null。 */
    Long resolve(String tokenValue);

    /** 吊销这一个令牌（退出登录）。 */
    void revoke(String tokenValue);

    /**
     * 吊销这个会员的<b>全部</b>令牌。冻结账号、重置密码之后调。
     *
     * @return 实际吊销了几个
     */
    int revokeAll(Long memberId);

    /**
     * 这个会员当前<b>活着的</b>会话。
     *
     * @param currentTokenValue 用来标出「就是你现在用的这一个」。可以为 null
     */
    List<MemberSession> listSessions(Long memberId, String currentTokenValue);

    /**
     * 让指定的一个会话下线。
     *
     * <p>🔴 {@code memberId} <b>不是可选项</b>：没有它，任何人拿到一个 sessionId
     * 就能把别人的会话踢掉。实现必须确认这条会话确实属于这个会员。
     *
     * @return 是否真的踢掉了一个（false 表示这个 sessionId 不属于他，或已经不在了）
     */
    boolean revokeSession(Long memberId, String sessionId);

    /**
     * 下线<b>除当前这一个之外</b>的所有会话。
     *
     * <p>这是「我的号可能被别人登着」时最有用的那个按钮 —— 一次点掉所有其它设备，
     * 而自己不用重新登。全部下线（含自己）反而会让人犹豫要不要点。
     *
     * @return 踢掉了几个
     */
    int revokeOthers(Long memberId, String currentTokenValue);
}
