package solvela.app.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关<b>怎么携带、以及要不要强制</b>设备令牌。
 *
 * <pre>
 * solvela:
 *   app:
 *     device:
 *       header: X-Device-Token
 *       mode: off        # off | observe | enforce
 * </pre>
 *
 * <p>与 {@link AuthProperties} 划的是同一条线：属于<b>端</b>的 HTTP 细节留在这里，
 * 属于<b>域</b>的规则（令牌活多久、密钥是哪把、一个 IP 一天能领几个）在别处。
 * 令牌本身怎么签怎么验在 {@code solvela.auth.device.*}，签发限频在
 * {@code solvela.member.device.*}。
 *
 * <h3>🔴 mode 存在的全部理由：服务端和客户端不可能同时上线</h3>
 * 直接强制会让所有存量客户端<b>当场 401</b> —— 这是整个设备身份方案里
 * 唯一可能造成全站事故的地方。所以必须三段走，而且每一段都要能一键退回上一段。
 *
 * @param header 携带设备令牌的请求头
 * @param mode   见 {@link Mode}
 */
@ConfigurationProperties(prefix = "solvela.app.device")
public record DeviceAuthProperties(String header, Mode mode) {

    public static final String DEFAULT_HEADER = "X-Device-Token";

    public DeviceAuthProperties {
        header = header == null || header.isBlank() ? DEFAULT_HEADER : header;
        // 🔴 默认必须是 OFF。漏配这一项的后果不对称：默认 OFF 只是「防刷还没生效」，
        //    默认 ENFORCE 是「所有老客户端立刻用不了」。
        mode = mode == null ? Mode.OFF : mode;
    }

    /**
     * 灰度档位。
     *
     * <p>取值刻意<b>不叫</b> {@code ON/OFF} 这种二元名字 —— 中间那档才是这套东西
     * 真正跑得起来的关键，用二元命名会让人以为它可以跳过。
     */
    public enum Mode {

        /**
         * 静默：验签照做、身份照绑，但<b>什么都不记、什么都不拦</b>。
         *
         * <p>服务端先上，客户端还没发版时用这一档。
         */
        OFF,

        /**
         * 打标：仍然放行，但周期性地把「带令牌的请求占比」打进日志。
         *
         * <p>那个比例就是<b>客户端版本覆盖率</b>：爬不上去说明客户端有 bug，
         * 而不是「用户升级得慢」。切 enforce 之前必须先在这一档看到它稳定。
         */
        OBSERVE,

        /**
         * 强制：没有有效设备令牌的请求一律 401（{@link DeviceExempt} 标注的除外）。
         *
         * <p>⚠️ 切到这一档之前请确认<b>回退路径是通的</b> ——
         * 改回 observe 要能在几分钟内生效，而不是等下一次发版。
         */
        ENFORCE,
    }
}
