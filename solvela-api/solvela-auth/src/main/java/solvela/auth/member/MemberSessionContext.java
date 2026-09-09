package solvela.auth.member;

/**
 * 签发令牌时<b>顺手记下来的那点上下文</b>，供「登录设备」列表展示。
 *
 * <h3>为什么是入参，而不是在令牌库里现取</h3>
 * 令牌库住在 {@code solvela-auth}，它不认识 HTTP、也不认识 {@code CurrentDevice}。
 * 让它自己去取，就得把 Web 那一层拖进来 —— 而那正是这个模块刻意不依赖的东西
 *（{@code AppBoundaryTest} 钉着网关的 classpath）。
 *
 * <p>所以由调用方（网关的登录服务）在签发的那一刻把已知的事实传进来。
 * 全部允许为 null：拿不到设备号、拿不到归属地都是常态，不该让登录失败。
 *
 * <h3>⚠️ region 目前<b>永远是 null</b></h3>
 * 解析 IP 归属地的 {@code SolvelaIpUtil} 住在 {@code solvela-base-core}，
 * 而网关的 classpath 上<b>没有任何 solvela-base 模块</b>（{@code AppBoundaryTest}
 * 钉死的），令牌又只在网关签发 —— 两头凑不到一起。
 *
 * <p>所以「登录设备」列表现在显示 IP 而不显示归属地。要补的话有两条路：
 * 让域在认证结果里把解析好的归属地带回来（{@code t_member_login_log.ip_region}
 * 本来就是域里算的），或者在网关内联一个轻量解析。
 * 前者更省事，但会给认证契约加一个纯展示字段 —— 值不值得等有人真的要看再说。
 */
public record MemberSessionContext(String deviceType, String deviceId, String ip, String region) {

    /** 什么都不知道时用它 —— 比如内部工具签发的令牌。 */
    public static MemberSessionContext empty() {
        return new MemberSessionContext(null, null, null, null);
    }
}
