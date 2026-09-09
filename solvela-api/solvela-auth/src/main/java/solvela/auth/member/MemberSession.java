package solvela.auth.member;

/**
 * 一个<b>活着的</b>登录会话，用户在「登录设备」里看到的一行。
 *
 * <h3>为什么只有活着的</h3>
 * 这个列表要回答的问题是「现在有谁登着我的号」，不是「我这三个月登过几次」。
 * 后者是 {@code t_member_login_log}（后台看的），前者是 Redis 里此刻还没过期的令牌。
 * 混在一起的话，用户会看到一堆早就失效的记录，然后对着一个点不动的「下线」按钮发愁。
 *
 * <h3>🔴 对外用 sessionId，不是令牌摘要</h3>
 * 摘要是 Redis 的查找键。把它发给客户端，就等于把「按什么去删」的钥匙交出去了 ——
 * 虽然服务端会校验这条会话属不属于当前会员，但那是一道<b>可以写错</b>的检查，
 * 而不发出去的东西不需要检查。sessionId 是另发的随机串，和摘要没有推导关系。
 *
 * @param sessionId   这一行的对外标识，「下线」按钮回传的就是它
 * @param deviceType  APP / H5 / WECHAT / PC，客户端登录时自报
 * @param deviceId    服务端签发的设备号，可能为 null（老客户端还没带设备令牌）
 * @param ip          登录时的 IP
 * @param region      IP 归属地，展示用
 * @param loginTime   登录时间（毫秒）
 * @param current     <b>是不是用户此刻正在用的这一个</b>。列表里必须标出来 ——
 *                    不标的话，用户很容易把自己这台点下线，然后当场被踢出去
 */
public record MemberSession(
        String sessionId,
        String deviceType,
        String deviceId,
        String ip,
        String region,
        long loginTime,
        boolean current) {
}
