package solvela.auth.device;

import java.time.Instant;

/**
 * 验签通过后从设备令牌里还原出来的身份。
 *
 * <p>它<b>不是</b>{@code t_device} 那一行 —— 那张表还有处置档、归属地、活跃时间。
 * 本记录只有「令牌里写了什么」，一次 HMAC 计算就能得到，不查任何存储。
 * 网关拿到的就是它，够不着也不需要 {@code t_device}。
 *
 * @param deviceId   32 位小写 hex，与 {@code t_device.device_id} 同一个值
 * @param deviceType 设备端 APP/H5/WECHAT/PC，与 {@code t_member_login_log.device_type} 同口径。
 *                   放进令牌是为了让网关不查库就能按端分流（H5 的限流阈值和 App 不是一套）
 * @param keyVersion 签发时用的密钥版本。留着是为了排查：
 *                   「这批令牌是哪一批密钥签的」在轮换期间是个会被问到的问题
 * @param issuedAt   签发时刻，秒精度。设备令牌<b>不过期</b>（它跟随安装，不是会话），
 *                   这个值只用于排查与将来可能的策略，验签不看它
 */
public record DeviceIdentity(String deviceId, String deviceType, int keyVersion, Instant issuedAt) {
}
