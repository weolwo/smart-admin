package solvela.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solvela.app.auth.Anonymous;
import solvela.app.auth.DeviceExempt;
import solvela.app.domain.DeviceRegisterRequest;
import solvela.app.domain.DeviceRegisterView;
import solvela.app.service.DeviceRegisterService;
import solvela.app.web.ClientIp;

/**
 * 设备身份。客户端<b>首次启动时</b>调一次，不是首次登录。
 *
 * <h3>为什么是启动而不是登录</h3>
 * 匿名接口（注册、登录、活动页）也要有设备身份 —— 而防刷要防的恰恰是它们。
 * 等到登录才领，等于把最需要保护的那几条路留在外面。
 *
 * <h3>两个注解都要标，缺一不可</h3>
 * <ul>
 *   <li>{@link Anonymous}：这时候还没有会员；</li>
 *   <li>{@link DeviceExempt}：这时候还没有设备 —— 要求它带设备令牌就成了
 *       先有鸡还是先有蛋。</li>
 * </ul>
 * 两个注解是正交的两件事，见 {@code DeviceExempt} 的类注释。
 */
@Tag(name = "设备身份")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRegisterService deviceRegisterService;

    /**
     * 领一个设备身份。
     *
     * <p>不是幂等的：每次调用都会新建一台设备。防重复靠客户端把令牌持久化，
     * 以及服务端的 IP 限频兜住异常量 —— <b>服务端无从判断「这是不是同一台设备」</b>，
     * 能判断的前提是客户端能自证身份，而它此刻恰恰还没有身份。
     *
     * <p>IP 在端上取，不传进 service —— 理由同 {@code MemberLoginController.login}。
     */
    @Anonymous
    @DeviceExempt
    @PostMapping("/register")
    public DeviceRegisterView register(@RequestBody @Valid DeviceRegisterRequest request,
                                       HttpServletRequest servletRequest) {
        return deviceRegisterService.register(request, ClientIp.of(servletRequest));
    }
}
