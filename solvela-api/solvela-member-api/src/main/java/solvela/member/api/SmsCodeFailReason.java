package solvela.member.api;

/**
 * 发送短信验证码失败的原因。域只说原因，措辞由调用方定。
 */
public enum SmsCodeFailReason {

    /** 手机号格式不对。可以明说：一个非法的串本来就不可能是任何人的号码。 */
    BAD_PHONE_FORMAT,

    /** 上一条发出去还不到冷却时间。 */
    TOO_FREQUENT,

    /**
     * 这个号码（或这个 IP）今天发得太多了。
     *
     * <p>🔴 短信这一档比邮件紧得多，因为<b>它真的花钱</b>：一条几分钱，
     * 被刷一天就是实打实的账单，而且量一大整个签名会被厂商限住 ——
     * 那时正常用户也收不到码了。
     */
    DAILY_LIMIT_REACHED,

    /**
     * 短信发不出去。
     *
     * <p>⚠️ 全仓还没有接任何短信服务商（见 {@code UnavailableSmsSender}）。
     * 但生产上<b>走不到这一档</b> —— 「配了 REAL 却没有厂商」在启动时就被拦下了
     * （{@code MemberSmsCodeService.checkTransport}）。真正会走到这里的，
     * 是接好厂商<b>之后</b>的运行期故障：余额不足、签名被限、厂商挂了。
     */
    SEND_FAILED,
}
