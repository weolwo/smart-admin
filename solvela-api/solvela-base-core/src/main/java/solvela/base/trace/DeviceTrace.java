package solvela.base.trace;

import org.slf4j.MDC;
import solvela.trace.DeviceContract;

/**
 * 本次请求的设备号，放在 MDC 上。
 *
 * <p>与 {@link Trace} 是同一套机制、同一个理由（见那个类的注释：日志框架读不了
 * {@code ScopedValue}，而异步 appender 在入队时就把 MDC 拷走了）。
 * 区别只有一个：<b>设备号可以没有</b>，所以 {@link #id()} 返回 null 是正常情况。
 *
 * <h3>它从哪来</h3>
 * 网关在设备令牌<b>验签通过</b>之后，把设备号写进 {@code X-Device-Id} 请求头；
 * 本进程的 {@code DeviceTraceFilter} 把它读回 MDC。头名与合法性规则来自
 * {@link DeviceContract}，两侧共用 —— 抄两份对不上时，设备维度的限流会静默失效。
 *
 * <p>⚠️ 与 traceId 一样<b>不会传播到线程池</b>。丢进 executor 的任务里取到的是 null，
 * 需要的话在请求线程上取出来当参数传进去。
 *
 * @Date 2026-09-09
 */
public final class DeviceTrace {

    private DeviceTrace() {
    }

    /** 绑定设备号，返回值必须放进 try-with-resources。 */
    public static MDC.MDCCloseable open(String deviceId) {
        return MDC.putCloseable(DeviceContract.MDC_KEY, deviceId);
    }

    /**
     * 取当前请求的设备号；<b>没有返回 null，而且这是常态</b>。
     *
     * <p>老客户端还没带设备令牌、内部定时任务根本不在请求线程上 —— 都会是 null。
     * 🔴 调用方不要把 null 当成「可疑」，那会在灰度完成之前就把老版本用户全挡在外面。
     */
    public static String id() {
        return MDC.get(DeviceContract.MDC_KEY);
    }
}
