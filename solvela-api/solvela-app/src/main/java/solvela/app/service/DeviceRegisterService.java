package solvela.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import solvela.app.domain.DeviceRegisterRequest;
import solvela.app.domain.DeviceRegisterView;
import solvela.app.web.ApiErrors;
import solvela.app.web.ApiException;
import solvela.member.api.DeviceApi;
import solvela.member.api.DeviceRegisterCmd;
import solvela.member.api.DeviceRegisterResult;

/**
 * 设备注册的<b>接入层</b>：转发 + 措辞，没有别的。
 *
 * <p>与 {@code MemberLoginService} 同一个划法：「能不能给这台终端签一个身份」
 * 由 {@link DeviceApi} 回答（限频、落库、密钥都在那边），本类只把失败原因
 * 翻译成 HTTP 契约、把结果组装成客户端要的形状。
 *
 * <h3>这里的措辞可以全说真话</h3>
 * 与登录正好相反 —— 登录要含糊（区分「账号不存在」和「密码错」等于送出一个枚举接口），
 * 而设备注册<b>不涉及任何用户身份</b>：说清楚 deviceType 写错了、说清楚这个 IP
 * 领得太多了，都不泄露任何东西，反而能让客户端开发者一眼看出问题。
 *
 * @Date 2026-09-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceRegisterService {

    private static final String LIMITED_MSG = "设备注册过于频繁，请 %d 分钟后重试";

    private final DeviceApi deviceApi;

    public DeviceRegisterView register(DeviceRegisterRequest request, String ip) {
        DeviceRegisterResult result = deviceApi.register(new DeviceRegisterCmd(
                request.deviceType(), request.model(), request.osVersion(), request.appVersion(), ip));

        if (!result.success()) {
            throw translate(result);
        }
        return new DeviceRegisterView(result.deviceToken(), result.deviceId());
    }

    /**
     * 失败原因 → HTTP 契约。
     *
     * <p>用 switch 表达式而不是 if 链：新增一个 {@code DeviceRegisterFailReason} 时
     * <b>编译不过</b>，而不是悄悄落进某个兜底分支返回「服务开小差了」。
     * 判据同 {@code MemberLoginService.translate}。
     */
    private ApiException translate(DeviceRegisterResult result) {
        return switch (result.reason()) {
            // 400 而不是含糊其辞：这是纯粹的参数错误，客户端写错了就该知道自己写错了
            case BAD_DEVICE_TYPE -> new ApiException(ApiErrors.INVALID_ARGUMENT,
                    "设备端取值不支持，允许 APP / H5 / WECHAT / PC");
            case TOO_MANY_ATTEMPTS -> new ApiException(ApiErrors.OPERATION_LIMITED,
                    limitedMessage(result.retryAfterSeconds()));
        };
    }

    /**
     * 把限制剩余时间拼成人话。
     *
     * <p>向上取整到分钟：剩 10 秒时说「请 0 分钟后重试」比不说还糟。
     * 与 {@code MemberLoginService.lockedMessage} 同一处理。
     */
    private static String limitedMessage(long retryAfterSeconds) {
        return String.format(LIMITED_MSG, Math.max(1, (long) Math.ceil(retryAfterSeconds / 60.0)));
    }
}
