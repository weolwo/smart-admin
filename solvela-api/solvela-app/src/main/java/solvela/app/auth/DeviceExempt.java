package solvela.app.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在接口方法上：<b>enforce 模式下也不要求设备令牌</b>。
 *
 * <p>目前只有一个合法用途：{@code POST /device/register} 本身 ——
 * 要求它带设备令牌就成了先有鸡还是先有蛋。
 *
 * <p>🔴 <b>不要拿它当「这个接口先放行一下」的开关。</b>
 * 每多标一个方法，就多一条不需要设备身份的入口，而攻击者只需要一条。
 * 与 {@link Anonymous} 同样的道理：注解是一个方法一个决定，加错了 code review 看得见；
 * 路径白名单则会连带放行未来同前缀的新接口，且没有人会收到通知。
 *
 * <p>它与 {@link Anonymous} 是<b>两个正交的维度</b>：
 * {@code @Anonymous} 说的是「不用登录」，本注解说的是「不用设备」。
 * {@code /device/register} 两个都要标 —— 它既没有会员身份，也没有设备身份。
 *
 * @Date 2026-09-08
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeviceExempt {
}
