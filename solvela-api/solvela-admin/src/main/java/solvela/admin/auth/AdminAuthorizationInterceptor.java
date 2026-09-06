package solvela.admin.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import solvela.admin.module.system.login.domain.RequestEmployee;
import solvela.admin.module.system.login.manager.LoginManager;
import solvela.code.UserErrorCode;
import solvela.exception.BusinessException;
import solvela.web.AllowAnonymous;
import solvela.web.RequiresPermission;

/**
 * 授权：判断这次请求允不允许调这个方法。
 *
 * <h3>它取代了什么</h3>
 * 原先是 {@code AdminInterceptor}：一个方法里混着取 token、查身份、判匿名、续活跃、
 * 判权限，外加一段把 sa-token 内部异常码（11041 / 11016 / 11011~11015）翻译成业务错误码的
 * if-else —— 那段代码的正确性依赖着一张<b>我们没写过、也不会随版本通知我们的</b>码表。
 *
 * <p>现在只剩三条规则，可以一眼读完：
 * <ol>
 *   <li>{@link AllowAnonymous} —— 放行；</li>
 *   <li>没有身份 —— 按 {@link AuthFailure} 抛 401（活跃超时与登录失效是两种，前端行为不同）；</li>
 *   <li>有 {@link RequiresPermission} —— 比对该员工的权限点集合；超管直接过。</li>
 * </ol>
 *
 * <p>没有 {@code @RequiresPermission} 的接口 = 需要登录、不要求具体权限点。
 * 这正是原先 {@code @SaIgnore} 的意思，所以那个注解删掉了 —— 不写就是了。
 *
 * <h3>抛异常，不自己写响应体</h3>
 * 错误响应的格式只能有一个来源。原先这里用 {@code SolvelaResponseUtil.write} 手写 JSON，
 * 于是全局异常处理器改了格式，这三处不会跟着改 —— 而它们恰恰是最常被前端遇到的三种错误。
 */
@Component
@RequiredArgsConstructor
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final LoginManager loginManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求不带业务语义，也带不上令牌。正常情况下 CorsFilter 已经把它拦下了，
        // 这里是兜底：万一跨域配置被改坏，预检打到这里会拿到 401，
        // 而浏览器只会报一句含糊的 CORS 错误，排查成本极高
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return false;
        }

        if (!(handler instanceof HandlerMethod method)) {
            // 静态资源、错误转发等，交给后面的环节
            return true;
        }

        if (method.hasMethodAnnotation(AllowAnonymous.class)) {
            return true;
        }

        RequestEmployee employee = CurrentEmployee.orNull();
        if (employee == null) {
            // 🔴 两种 401 要分开：过期让前端弹「长时间未操作」并跳登录，
            // 无效令牌直接跳登录。合成一种的话用户被踢下线却不知道为什么
            throw new BusinessException(switch (AuthFailure.of(request)) {
                case INACTIVE -> UserErrorCode.LOGIN_ACTIVE_TIMEOUT;
                case INVALID -> UserErrorCode.LOGIN_STATE_INVALID;
            });
        }

        checkPermission(method, employee);
        return true;
    }

    /**
     * 权限点校验。没标 {@code @RequiresPermission} 的接口只要求登录，不要求权限。
     *
     * <p>超管不校验：它的权限来自<b>身份</b>而不是配置 —— 否则给超管配漏一个权限点，
     * 就等于把自己锁在系统外面，而修复动作本身也需要那个权限。
     */
    private void checkPermission(HandlerMethod method, RequestEmployee employee) {
        RequiresPermission required = method.getMethodAnnotation(RequiresPermission.class);
        if (required == null || Boolean.TRUE.equals(employee.getAdministratorFlag())) {
            return;
        }
        if (!loginManager.getUserPermission(employee.getUserId()).permissionList().contains(required.value())) {
            throw new BusinessException(UserErrorCode.NO_PERMISSION);
        }
    }
}
