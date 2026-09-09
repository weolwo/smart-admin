package solvela.member.api;

/**
 * 发送短信验证码的入参。
 *
 * <p>与 {@link EmailCodeSendCmd} 少一个字段：<b>没有 currentMemberId</b>。
 * 邮箱那边要它，是因为 {@link EmailCodeScene#BIND} 得知道「这个邮箱是不是本人已有的」
 * 才能决定寄不寄；短信目前三个场景都是匿名的，多带一个当前会员进来
 * 只会让人以为它被用上了。等「绑定手机号」出现时再加，那时它才有意义。
 *
 * @param scene    用途。<b>必须由调用方显式指定</b> —— 场景进 Redis key，
 *                 共用的话一个为注册发的码就能拿去重置密码
 * @param phone    收件手机号，任意格式，域内会规范化
 * @param clientIp 客户端 IP，允许为 null（拿不到时放行并打警告）
 */
public record SmsCodeSendCmd(SmsScene scene, String phone, String clientIp) {
}
