package solvela.member.api;

/**
 * 校验短信验证码的结果。取值与 {@link EmailCodeVerifyResult} 一一对应。
 *
 * <p>不共用一个枚举：两条通道的失败取值今天恰好一样，但它们没有理由永远一样
 * （短信将来可能有「该号码在运营商黑名单里」这类只属于它的取值）。
 * 而共用之后再想拆开，每一处 switch 都得回头改。
 */
public enum SmsCodeVerifyResult {

    /** 通过。<b>验证码已被消费</b>，同一个码不能再用第二次。 */
    OK,

    /** 没有待校验的验证码：从没发过，或者已经过期。 */
    NOT_FOUND,

    /** 验证码不对。 */
    MISMATCH,

    /** 连续输错次数用尽，验证码已作废，必须重新发送。 */
    TOO_MANY_ATTEMPTS,
}
