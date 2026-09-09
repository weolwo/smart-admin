package solvela.member.register;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 注册限频配置。
 *
 * <p>⚠️ 这是本项目<b>目前唯一</b>拦批量注册的东西 —— 还没有短信验证码。
 * 接入验证码之后这一层仍该留着：验证码防的是「不是真人」，限频防的是
 * 「真人也不该一小时注册 50 个号」，两件事。
 */
@Data
@Component
@ConfigurationProperties(prefix = "solvela.member.register")
public class MemberRegisterProperties {

    /**
     * 限频窗口。
     *
     * <p>不要设得太短：窗口越短，攻击者只要放慢速度就完全不受影响，
     * 而正常用户（同一个 NAT 出口后面的办公室、学校、咖啡厅）反而更容易被误伤。
     */
    private Duration window = Duration.ofHours(1);

    /**
     * 同一 IP 在一个窗口内允许的<b>注册尝试</b>次数（含失败）。
     *
     * <p>🔴 计的是尝试而不是成功，因为「手机号已注册」这个回答本身就是一个枚举口子
     * （见 {@code RegisterFailReason.PHONE_TAKEN} 的类注释，它藏不掉）。
     * 只计成功的话，枚举手机号完全不受限。
     *
     * <p>默认 10 是权衡：NAT 后面的正常用户几乎不可能一小时内摸到 10 次注册，
     * 而 10 次/小时对枚举来说慢到没有意义。
     */
    private int maxAttemptsPerIp = 10;

    /**
     * 手机号注册<b>是否必须验短信验证码</b>。
     *
     * <h3>🔴 默认 true，而全仓还没接短信服务商 —— 这是刻意的</h3>
     * 默认值应当描述<b>正确的世界</b>，而不是迁就当前的缺口。
     * 配成 false 是一个<b>需要有人明确按下去</b>的决定，而不是一个谁也没注意到的现状。
     *
     * <p>配 false 的后果要说清楚：<b>任何人都能拿别人的手机号建账号</b>，
     * 而 {@code uk_mbr_phone_hash} 是唯一约束 —— 号被占了，真机主就再也注册不了。
     * 唯一的缓解是 IP 与设备限频，它们只能压速率，<b>拦不住定向占号</b>
     * （占一个号只需要成功一次）。
     *
     * <p>过渡期建议：dev / test 配 true 并把 {@code sms-transport} 配成 LOG，
     * 链路照样跑得通；生产在接上厂商之前如果必须放行注册，就把这一项配成 false，
     * <b>并且知道自己在放行什么</b>。
     */
    private boolean phoneCodeRequired = true;
}
