package solvela.member.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 会员邮箱验证码的参数。
 *
 * <pre>
 * solvela:
 *   member:
 *     email-code:
 *       length: 6
 *       ttl: 5m
 *       resend-cooldown: 60s
 *       max-verify-attempts: 5
 *       max-send-per-email-per-day: 10
 *       max-send-per-ip-per-day: 20
 * </pre>
 *
 * @Date 2026-09-09
 */
@Data
@Component
@ConfigurationProperties(prefix = "solvela.member.email-code")
public class MemberEmailCodeProperties {

    /**
     * 验证码位数。
     *
     * <p>🔴 <b>6 位，不是管理端那套的 4 位。</b>那边的注释写着「5 分钟有效期内穷举 1 万种也够呛」——
     * 这句话对<b>人</b>成立，对脚本不成立：1 万种组合、无限次尝试，几秒钟就跑完了。
     *
     * <p>位数与 {@link #maxVerifyAttempts} 是<b>一对</b>，要一起看：
     * 6 位 = 100 万种，配 5 次上限，猜中的概率是二十万分之一。
     * 单调其中一个都会让这个数失去意义。
     */
    private int length = 6;

    /**
     * 有效期。太短用户来不及切到邮箱，太长等于给暴力破解更多时间。
     *
     * <p>邮件比短信慢（要过对方服务器的反垃圾），5 分钟是能接受的下限。
     */
    private Duration ttl = Duration.ofMinutes(5);

    /**
     * 重发冷却。挡的是连点，也顺带挡住把这个接口当<b>免费邮件发射器</b>用。
     */
    private Duration resendCooldown = Duration.ofSeconds(60);

    /**
     * 同一个验证码最多能验错几次，用尽即作废、必须重发。
     *
     * <p>见 {@link #length}：这两个数是一对。
     * 「作废」而不是「锁一会儿」是有意的 —— 锁定会让攻击者拿到一个
     * 「这个邮箱刚才发过码」的信号，而重发的成本本来就很低。
     */
    private int maxVerifyAttempts = 5;

    /**
     * 同一邮箱一天最多收几封。
     *
     * <p>不限量的后果不只是骚扰：发件人显示的是我们的域名，被拿去发垃圾之后
     * <b>整个域名进黑名单</b>，此后所有系统邮件（含管理端的登录验证码）都进垃圾箱。
     * 那是个要几周才能洗白的坑。
     */
    private int maxSendPerEmailPerDay = 10;

    /**
     * 同一 IP 一天最多发几封。
     *
     * <p>与上一条是<b>两个维度</b>：按邮箱限挡的是「盯着一个人发」，
     * 按 IP 限挡的是「拿一堆邮箱地址群发」。只做前者的话，
     * 一个脚本换着邮箱发，每个都不超限，而总量已经足够让域名被拉黑。
     */
    private int maxSendPerIpPerDay = 20;

    /**
     * 验证码<b>怎么送到用户手上</b>。
     *
     * <p>🔴 与 {@code MailDelivery} 是<b>两个正交的东西</b>，别混：
     * <ul>
     *   <li>{@code MailDelivery} 回答「这封信该不该寄」—— 那是<b>安全</b>决定
     *       （寄给一个没有账号的邮箱等于泄露账号是否存在）；</li>
     *   <li>本项回答「寄的时候走什么通道」—— 那是<b>环境</b>决定。</li>
     * </ul>
     * 合成一个枚举的话，一个为了本地调试加的取值就会出现在安全判断的 switch 里。
     */
    private Transport transport = Transport.MAIL;

    /** 送达通道。 */
    public enum Transport {

        /** 真发信。 */
        MAIL,

        /**
         * <b>不发信，把验证码打进日志。</b>本地开发与联调用，省掉配 SMTP 这一步。
         *
         * <p>🔴 <b>生产环境用它会启动失败</b>，这是刻意的：日志里躺着每个人的验证码，
         * 拿到日志（或 ELK 权限）就能接管任意账号 —— 而日志的访问面通常比数据库宽得多，
         * 还会被采集、被转发、被长期保留。
         *
         * <p>做成「启动即失败」而不是「静默降级成 MAIL」：降级的话，
         * 有人在生产配了 LOG 却什么都没发生，他会以为这个开关不生效，
         * 转头去别处找原因 —— 而真正的问题（配置文件里躺着一个危险开关）没人知道。
         */
        LOG,
    }

    private Duration dailyWindow = Duration.ofDays(1);

    public Duration ttl() {
        return ttl == null ? Duration.ofMinutes(5) : ttl;
    }

    public Duration resendCooldown() {
        return resendCooldown == null ? Duration.ofSeconds(60) : resendCooldown;
    }

    public Duration dailyWindow() {
        return dailyWindow == null ? Duration.ofDays(1) : dailyWindow;
    }

    public int length() {
        return length < 4 ? 6 : length;
    }

    public int maxVerifyAttempts() {
        return maxVerifyAttempts <= 0 ? 5 : maxVerifyAttempts;
    }
}
