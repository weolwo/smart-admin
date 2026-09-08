package solvela.app.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import solvela.app.web.ApiErrors;
import solvela.app.web.ApiException;

/**
 * enforce 档下：没有有效设备令牌的请求一律挡掉。
 *
 * <h3>为什么单独一个拦截器，不并进 {@link AuthorizationInterceptor}</h3>
 * 那个拦截器回答的是「这个人登录了吗」，本拦截器回答的是「这个请求来自一台我认得的设备吗」。
 * 两条<b>正交</b>的轴：匿名请求也要有设备（防刷要防的正是它们），
 * 而灰度期间老客户端有会员没设备。合在一个类里，两条判据会开始互相引用，
 * 而它们本来谁也不该知道谁。
 *
 * <p>顺序上排在授权之前：设备是比会员更外层的东西，先问外层。
 * 实际影响只有一个 —— 一个既没登录又没设备的请求，enforce 档下拿到的是
 * {@code DEVICE_REQUIRED} 而不是 {@code LOGIN_REQUIRED}。这是对的：
 * 让用户去登录解决不了他客户端太旧的问题。
 *
 * <h3>off / observe 档下本拦截器完全不做事</h3>
 * 不是「不生效」而是<b>确实什么都不判断</b>：模式的语义就是这样，
 * 前两档的价值全在放行 + 观察。见 {@link DeviceAuthProperties.Mode}。
 *
 * @Date 2026-09-08
 */
@Component
@RequiredArgsConstructor
public class DeviceRequirementInterceptor implements HandlerInterceptor {

    private final DeviceAuthProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (properties.mode() != DeviceAuthProperties.Mode.ENFORCE) {
            return true;
        }
        if (!(handler instanceof HandlerMethod method)) {
            // 静态资源、错误转发等，交给后面的环节。理由同 AuthorizationInterceptor
            return true;
        }
        if (method.hasMethodAnnotation(DeviceExempt.class)) {
            return true;
        }
        if (!CurrentDevice.isBound()) {
            // 抛出去交给 ApiExceptionHandler 统一成 401 —— 不在这里手写响应体，
            // 否则错误格式就有了两个来源，改一处忘一处
            throw new ApiException(ApiErrors.DEVICE_REQUIRED);
        }
        return true;
    }
}
