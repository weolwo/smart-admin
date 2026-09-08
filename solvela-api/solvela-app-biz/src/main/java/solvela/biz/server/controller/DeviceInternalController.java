package solvela.biz.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import solvela.member.api.DeviceApi;
import solvela.member.api.DeviceRegisterCmd;
import solvela.member.api.DeviceRegisterResult;
import solvela.member.device.DeviceService;

/**
 * {@link DeviceApi} 的 HTTP 薄壳。
 *
 * <p>与 {@link MemberAuthInternalController} 同一个做法：{@code implements} 契约接口而不是
 * 自己写 {@code @PostMapping} —— Spring MVC 认得接口上的 {@code @HttpExchange}，
 * 所以<b>路径与方法只在契约里定义一次</b>。自己写一遍映射的话，网关侧的客户端代理
 * 和这里的服务端映射就是两份，改一处忘另一处的表现是 404，而且要等到联调才发现。
 *
 * <p>⚠️ 本进程里有两个 {@link DeviceApi} 类型的 bean（本类与 {@link DeviceService}），
 * 所以<b>进程内不要按接口类型注入</b>，要注入就注入实现类。按接口注入的是网关，
 * 那边只有 HTTP 代理一个实现，不存在歧义。
 *
 * <h3>🔴 /internal/device/register 不需要任何身份就能签发设备令牌</h3>
 * 它和 {@code /internal/member/auth/register} 同一个性质：入口层必须把
 * {@code /internal/**} 整体挡在外面。这条路由一旦对公网开放，
 * 「一个 IP 一天 10 个设备」这道闸就等于不存在 —— 攻击者可以直接绕过网关来领身份，
 * 而整套设备防刷的地基就是它。
 */
@RestController
@RequiredArgsConstructor
public class DeviceInternalController implements DeviceApi {

    private final DeviceService deviceService;

    @Override
    public DeviceRegisterResult register(DeviceRegisterCmd cmd) {
        return deviceService.register(cmd);
    }
}
