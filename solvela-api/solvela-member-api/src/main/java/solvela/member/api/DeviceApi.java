package solvela.member.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 设备身份契约。
 *
 * <h3>为什么单独一个 Api，而不是挂到 MemberAuthApi 上</h3>
 * {@link MemberAuthApi} 的类注释说过「多一个接口就多一个『服务端薄壳建了没有』的失误面」，
 * 那条判据在这里不成立：设备<b>不属于会员</b> —— 注册设备时还没有任何会员身份，
 * 一台设备之后也可能登多个号。挂进认证契约会让「设备是会员的一部分」这个错误印象
 * 一路带进将来的服务拆分（设备域是要和会员域分开的）。
 *
 * <p>失误面用另一种方式堵：服务端薄壳 {@code DeviceInternalController implements DeviceApi}，
 * 少实现一个方法编译就不过。
 *
 * <h3>路径前缀 /internal 同样是有意的</h3>
 * 它<b>不需要任何身份</b>就能签发一个设备令牌 —— 与 {@code /internal/member/auth/register}
 * 同一个性质，永远不能对公网开放。真正面向客户端的那条路由在网关上（提交 4），
 * 由网关做限流之外的接入层校验，再转到这里。
 *
 * <h3>为什么签发在会员服务而不是网关</h3>
 * 网关的 classpath 上没有 mysql 驱动、也没有任何 {@code solvela-base-*} 模块
 * （{@code AppBoundaryTest} 四条断言守着），所以它够不着 {@code t_device}、
 * {@code RedisService} 和 {@code SolvelaIpUtil}。留在网关的只有<b>验签</b> ——
 * 纯 HMAC，零 IO，那正是当初选自包含令牌而不是 Redis 令牌的原因。
 */
@HttpExchange("/internal/device")
public interface DeviceApi {

    /**
     * 签发一台新设备。
     *
     * <p>每次调用都会<b>新建一行</b>并返回一个新令牌 —— 它不是幂等的，也不该是：
     * 「同一台设备重复注册」在服务端无从判断（能判断的前提是客户端能自证身份，
     * 而它此刻恰恰还没有身份）。防重复靠的是客户端把令牌持久化，
     * 以及这里的 IP 限频兜住异常量。
     */
    @PostExchange("/register")
    DeviceRegisterResult register(@RequestBody DeviceRegisterCmd cmd);
}
