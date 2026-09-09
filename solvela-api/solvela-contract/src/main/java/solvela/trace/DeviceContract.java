package solvela.trace;

/**
 * 设备号跨进程传递的<b>线上约定</b>：头的名字、MDC 的键、什么样的值算合法。
 *
 * <h3>为什么走请求头而不是塞进每个 DTO</h3>
 * 与 {@link TraceContract} 同一个理由，而且更强：设备号要从网关一路带到<b>发奖风控</b>，
 * 中间经过活动、抽奖、奖品派发、提案四层。让每一层的 DTO 都加一个字段，
 * 等于给四个契约各开一个必须被正确填写的口子 —— 而其中任何一处忘了填，
 * 表现都是「设备维度的限流配了但从不命中」，没有任何报错。
 *
 * <p>{@code MemberAuthCmd} 的类注释把这条线画得很清楚：<b>要落库的业务数据进 Cmd，
 * 对每个接口都一样的上下文走 MDC</b>。设备号属于后者。
 *
 * <h3>🔴 下游信任这个头，前提是它只接受来自网关的流量</h3>
 * 头由网关在<b>验签通过之后</b>注入。如果 biz(1026) / member(1027) 的端口能被外网直接摸到，
 * 任何人都能自己编一个设备号绕过全部设备维度的限流 —— 而且绕得毫无痕迹。
 * 这与 {@code ClientIp} 的类注释里「只有入口网关覆盖 XFF 时第一段才可信」是同一类前提，
 * 靠的是网络策略，不是代码。
 *
 * <h3>与 TraceContract 的一处关键差别：不合法时返回 null，不生成</h3>
 * traceId 认不出来时自己生成一个是对的 —— 总得有个 id 串日志。
 * 设备号<b>绝不能这么做</b>：凭空造一个设备号出来，限流就会去数一个不存在的设备，
 * 而真正那台设备的计数永远是 0。<b>没有就是没有</b>，让下游按「老客户端」处理。
 *
 * <p>⚠️ 改这里等于改线上协议，三个进程必须同时发版。
 *
 * @Date 2026-09-09
 */
public final class DeviceContract {

    /**
     * 请求头名。
     *
     * <p>刻意<b>不叫</b> {@code X-Device-Token}：那个是客户端发给网关的<b>令牌</b>，
     * 这个是网关验签之后发给下游的<b>设备号</b>。两者一个是凭证一个是身份，
     * 用同一个名字迟早有人把令牌原样透传下去 —— 那等于把凭证散给了所有内部服务。
     */
    public static final String HEADER = "X-Device-Id";

    /**
     * MDC 的键。业务代码取值走 {@code DeviceTrace.id()}，不要直接 {@code MDC.get}。
     *
     * <p>理由同 {@link TraceContract#KEY}：这个字符串散落在几个文件里之后，
     * 改名就会漏掉一处，而漏掉的表现只是「这个字段莫名其妙变空了」。
     */
    public static final String MDC_KEY = "deviceId";

    /** 与 {@code t_device.device_id} 的 char(32) 一致：32 位小写 hex。 */
    private static final int LENGTH = 32;

    private DeviceContract() {
    }

    /**
     * 把请求头里的候选值收敛成一个合法设备号；<b>不合法返回 null</b>。
     *
     * <p>形状校验不是安全措施 —— 真正的防伪在网关的 HMAC 验签，这里只是不让脏值进 MDC
     * （它会被写进 {@code t_proposal_record.device_id}，而那一列是 char(32)）。
     */
    public static String sanitize(String candidate) {
        if (candidate == null || candidate.length() != LENGTH) {
            return null;
        }
        for (int i = 0; i < LENGTH; i++) {
            char c = candidate.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
                return null;
            }
        }
        return candidate;
    }
}
