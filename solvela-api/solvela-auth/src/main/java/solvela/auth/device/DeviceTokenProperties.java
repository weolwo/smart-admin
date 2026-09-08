package solvela.auth.device;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 设备令牌的签名密钥。
 *
 * <pre>
 * solvela:
 *   auth:
 *     device:
 *       key-version: 2          # 【新令牌】用哪一把签
 *       keys:                   # 验签接受【全部】，这正是轮换的含义
 *         1: "旧密钥…"
 *         2: "新密钥…"
 * </pre>
 *
 * <h3>为什么密钥要带版本，而 {@link solvela.auth.member.MemberSessionProperties} 不用</h3>
 * 会员令牌是随机串，服务端存摘要 —— 想让全部令牌失效，删 Redis 就行，不涉及密钥。
 * 设备令牌是自包含的：<b>密钥就是唯一的信任根</b>。它泄露时必须能换，
 * 而换的那一刻已经发出去的令牌不能全部作废（那等于让所有用户重新注册设备）。
 * 所以要「新令牌用新密钥签，旧令牌仍按旧密钥验」——这需要令牌自报版本，
 * 也需要这里能同时持有多把。
 *
 * <p>轮换的完整动作是三步，缺一步都不对：
 * <ol>
 *   <li>加一把新密钥进 {@code keys}，<b>不动</b> {@code key-version} —— 此时新密钥只被接受，不被使用；</li>
 *   <li>确认所有实例都加载到了新配置，再把 {@code key-version} 指向它；</li>
 *   <li>等到旧令牌自然消亡（设备重装/清缓存）之后，才能从 {@code keys} 里删掉旧的。
 *       <b>这一步没有时间表</b> —— 设备令牌不过期，删早了就是让那批用户重新注册设备。</li>
 * </ol>
 *
 * <h3>🔴 与 PII 密钥必须是两把</h3>
 * {@code solvela.crypto.pii.hmac-key} <b>永远不能改</b>（改了老会员按新摘要查不到，
 * 登录不进来，而唯一索引又拦不住他用同一个号重新注册 —— 一个手机号变成两个账号，
 * 见 {@code PiiHasher} 的类注释）。设备密钥恰恰相反，必须能换。
 * 一个不能改、一个必须能改，共用一把等于两边都做不到。
 */
@Data
@Component
@ConfigurationProperties(prefix = "solvela.auth.device")
public class DeviceTokenProperties {

    /**
     * 签发新令牌用哪个版本的密钥。必须在 {@link #keys} 里存在，否则启动即失败。
     *
     * <p>默认 1 只是让「只配一把密钥」的最常见情形不用多写一行；
     * 它<b>不是</b>一个可以不配密钥就能用的默认值 —— keys 为空时照样启动不了。
     */
    private int keyVersion = 1;

    /**
     * 版本 → 密钥原文。验签时按令牌自报的版本查这里，<b>全部版本都接受</b>。
     *
     * <p>用 {@code LinkedHashMap} 而不是 HashMap：配置里的书写顺序即版本顺序，
     * 排查时 dump 出来是按序的。功能上没区别，读日志时有。
     */
    private Map<Integer, String> keys = new LinkedHashMap<>();
}
