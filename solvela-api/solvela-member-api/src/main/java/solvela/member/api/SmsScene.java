package solvela.member.api;

/**
 * 短信验证码的用途。
 *
 * <p>放在 {@code solvela-member-api} 而不是域内部：它出现在<b>跨进程契约</b>的入参上
 * （{@link MemberAuthApi#sendSmsCode}），网关要能引用到。判据同 {@link EmailCodeScene}。
 *
 * <h3>为什么与 {@link EmailCodeScene} 分开，而不是共用一个</h3>
 * 两条通道的场景集合<b>本来就不一样</b>，而且会越差越多：
 * <ul>
 *   <li>手机号这边的 {@link #REGISTER} 与邮箱那边的意义完全不同 ——
 *       邮箱注册是新开一条通道，手机号注册是把已有的那条补上验证；</li>
 *   <li>邮箱有 BIND（绑定邮箱），手机号将来会有「绑定手机号」，
 *       但那是两件事、两套业务规则。</li>
 * </ul>
 *
 * <p>共用一个枚举会让每一侧都面对一堆自己用不到的取值，而 switch 表达式的
 * 「新增取值编译不过」这个好处会退化成「另一条通道加场景，这边也得跟着改」。
 *
 * <p>🔴 场景必须进 Redis key：共用的话，一个为注册发的码就能拿去重置密码。
 *
 * @Date 2026-09-10
 */
public enum SmsScene {

    /**
     * 注册。
     *
     * <p>🔴 <b>这是手机号那条通道一直缺的东西</b>。在它之前，任何人都能拿别人的
     * 手机号建账号，而 {@code uk_mbr_phone_hash} 是唯一约束 ——
     * 号被占了，真机主就再也注册不了了。
     */
    REGISTER,

    /** 免密登录。 */
    LOGIN,

    /** 重置密码。 */
    RESET_PASSWORD,
}
