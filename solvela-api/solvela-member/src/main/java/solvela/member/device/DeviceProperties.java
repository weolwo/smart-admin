package solvela.member.device;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 设备签发的限频配置。
 *
 * <pre>
 * solvela:
 *   member:
 *     device:
 *       register-window: 1d
 *       max-register-per-ip: 10
 * </pre>
 *
 * <h3>为什么这一档比注册限频松，却更管用</h3>
 * {@code MemberRegisterProperties} 限的是「一小时能试几次注册」；这里限的是
 * 「一天能领几个设备身份」。后者是<b>整套设备防刷的地基</b>：
 * 刷登录必须先有设备令牌，刷设备令牌必须过这一关。
 *
 * <p>正常用户一天装十次 App 是不可能的，而对批量脚本来说，
 * 「每个 IP 一天只能拿 10 个身份」直接把成本从「换个 UDID 就行」抬到了「要有代理池」。
 * 战场从每天上百万次的登录接口，挪到了正常情况每次安装才发生一次的注册接口 ——
 * 异常流量在后者刺眼得多。
 *
 * <p>🔴 与密钥配置不同，这两个值<b>有可用的默认值</b>，不配也能起。
 * 限流参数配错的后果是「松了或紧了」，可以观察后调；密钥配错的后果是
 * 「整套设计的前提归零且无迹可循」——所以那个不配就启动失败，这个不。
 */
@Data
@Component
@ConfigurationProperties(prefix = "solvela.member.device")
public class DeviceProperties {

    /**
     * 限频窗口。
     *
     * <p>给一天而不是一小时：窗口越短，攻击者只要放慢速度就完全不受影响，
     * 而设备注册本来就是低频动作，用一天的窗口不会误伤任何真实用户。
     * 这条取舍与 {@code MemberRegisterProperties.window} 的注释同源。
     */
    private Duration registerWindow = Duration.ofDays(1);

    /**
     * 同一 IP 在一个窗口内最多能签发几个设备。
     *
     * <p>默认 10 是权衡：NAT 出口后面的办公室、学校、咖啡厅一天出现 10 台新设备是可能的，
     * 但不会更多；而 10 个/天对批量注册来说慢到没有意义。
     *
     * <p>⚠️ 上线初期建议先调大并观察真实分布再收紧 —— 这个值收得太紧，
     * 表现是「新用户装了 App 但进不去」，而他们不会来报障。
     */
    private int maxRegisterPerIp = 10;

    public Duration registerWindow() {
        return registerWindow == null ? Duration.ofDays(1) : registerWindow;
    }

    public int maxRegisterPerIp() {
        return maxRegisterPerIp <= 0 ? 10 : maxRegisterPerIp;
    }
}
