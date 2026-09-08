package solvela.app.auth;

import solvela.auth.device.DeviceIdentity;

import java.util.Optional;

/**
 * 当前请求的设备身份，绑定在 {@link ScopedValue} 的作用域上。
 *
 * <p>与 {@link CurrentMember} 是<b>两条独立的轴</b>，不要互相推导：
 * <ul>
 *   <li>匿名请求<b>也有设备</b>（注册、登录、活动页都是匿名的，而防刷要防的正是它们）；</li>
 *   <li>老版本客户端<b>有会员没设备</b>（灰度期间设备令牌还没铺开）。</li>
 * </ul>
 * 所以「已登录」推不出「有设备」，反过来也不成立。判断哪一个就问哪一个。
 *
 * <p>同样地，匿名/无设备时不绑定（ScopedValue 不接受 null 值），
 * {@link #isBound()} 就是「这个请求有没有可信设备身份」的准确答案。
 *
 * @Date 2026-09-08
 */
public final class CurrentDevice {

    static final ScopedValue<DeviceIdentity> DEVICE = ScopedValue.newInstance();

    private CurrentDevice() {
    }

    /**
     * 当前请求是否带了一个<b>验签通过</b>的设备令牌。
     *
     * <p>只在请求线程上有意义 —— 作用域跟着调用栈走，不跟着对象走，
     * 丢进线程池的任务不会继承它。理由同 {@link CurrentMember#isBound()}。
     */
    public static boolean isBound() {
        return DEVICE.isBound();
    }

    /** 取当前设备身份；没有返回空。 */
    public static Optional<DeviceIdentity> find() {
        return DEVICE.isBound() ? Optional.of(DEVICE.get()) : Optional.empty();
    }

    /**
     * 取当前设备号；没有返回 null。
     *
     * <p>这是最常用的一个 —— 往下游传的、写进日志的、拿去限流的都是它。
     * <b>刻意没有 require()</b>：设备身份在灰度期间本来就可能没有，
     * 提供一个「没有就抛」的入口会诱导调用方写出「老客户端一律 500」的代码。
     */
    public static String deviceIdOrNull() {
        return find().map(DeviceIdentity::deviceId).orElse(null);
    }
}
