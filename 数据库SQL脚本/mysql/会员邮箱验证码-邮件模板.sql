-- =====================================================================================
-- 会员邮箱验证码：四封邮件模板
-- 日期：2026-09-09
--
-- 【为什么四个场景各自一封信，而不是共用一个模板】
-- 共用的话，一个为「绑定邮箱」发出去的验证码就能拿去【重置密码】。攻击面很具体：
-- 诱导用户在某个页面走一次绑定邮箱（看起来人畜无害），拿到他念出来的那 6 位数，
-- 转手去改他的密码。
--
-- 所以每封信都必须写清楚【这个码是用来做什么的】—— 否则用户没有任何依据
-- 判断该不该把它念给别人听。四个场景的措辞刻意不同，重置密码那封最重。
--
-- 🔴 本文件同时补了 login_verification_code（管理端员工登录的双因子验证码）。
--    那一行此前【只存在于开发库】，data-baseline.sql 里一条 t_mail_template 都没有 ——
--    也就是说一个全新环境跑完两个基线之后，管理端一开双因子登录就报「模版不存在」。
--    这是早于本次的一个缺口，顺手补上。
-- =====================================================================================

-- 可重复执行：改文案时直接重跑本文件即可
DELETE FROM `t_mail_template` WHERE `template_code` IN (
    'login_verification_code',
    'member_register_code',
    'member_login_code',
    'member_bind_email_code',
    'member_reset_password_code'
);

-- -------------------------------------------------------------------------------------
-- ⚠️ template_code 必须是 MailTemplateCodeEnum 的枚举名【转小写】——
--    MailService 查的是 templateCode.name().toLowerCase()。
--    大小写不对的表现是「模版不存在」，而那句话看起来像是没插数据。
-- -------------------------------------------------------------------------------------

INSERT INTO `t_mail_template`
(`template_code`, `template_subject`, `template_content`, `template_type`, `disable_flag`)
VALUES
-- 管理端：员工登录双因子（补齐 data-baseline 的缺口，内容与开发库一致）
('login_verification_code', '登录验证码',
 '<!DOCTYPE HTML><html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body>
<div style="margin:0 auto;width:690px;font-family:Helvetica,Arial,sans-serif;line-height:28px;">
  <h2>登录验证码</h2>
  <p>请在登录页面输入此验证码：</p>
  <p style="font-size:28px;letter-spacing:6px;"><b>${code}</b></p>
  <p>验证码将于此邮件发出 5 分钟后过期。</p>
  <p>如果你未曾提出此请求，可以忽略这封邮件。</p>
</div></body></html>', 'freemarker', 0),

-- 会员：注册
('member_register_code', '注册验证码',
 '<!DOCTYPE HTML><html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body>
<div style="margin:0 auto;width:690px;font-family:Helvetica,Arial,sans-serif;line-height:28px;">
  <h2>注册验证码</h2>
  <p>你正在用这个邮箱<b>注册账号</b>。请在注册页面输入：</p>
  <p style="font-size:28px;letter-spacing:6px;"><b>${code}</b></p>
  <p>验证码 ${minutes} 分钟内有效。</p>
  <p>如果这不是你本人的操作，说明有人填错了邮箱地址，忽略这封邮件即可 —— 不会有账号被创建。</p>
</div></body></html>', 'freemarker', 0),

-- 会员：邮箱免密登录
('member_login_code', '登录验证码',
 '<!DOCTYPE HTML><html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body>
<div style="margin:0 auto;width:690px;font-family:Helvetica,Arial,sans-serif;line-height:28px;">
  <h2>登录验证码</h2>
  <p>你正在<b>登录</b>。请在登录页面输入：</p>
  <p style="font-size:28px;letter-spacing:6px;"><b>${code}</b></p>
  <p>验证码 ${minutes} 分钟内有效。</p>
  <p><b>不要把这串数字告诉任何人</b>，包括自称客服的人 —— 我们不会向你索要验证码。</p>
  <p>如果这不是你本人的操作，可能有人正在尝试登录你的账号，建议尽快修改密码。</p>
</div></body></html>', 'freemarker', 0),

-- 会员：绑定/更换邮箱
('member_bind_email_code', '邮箱绑定验证码',
 '<!DOCTYPE HTML><html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body>
<div style="margin:0 auto;width:690px;font-family:Helvetica,Arial,sans-serif;line-height:28px;">
  <h2>邮箱绑定验证码</h2>
  <p>你正在把这个邮箱<b>绑定到一个账号</b>上。请在绑定页面输入：</p>
  <p style="font-size:28px;letter-spacing:6px;"><b>${code}</b></p>
  <p>验证码 ${minutes} 分钟内有效。</p>
  <p><b>这串数字只用于绑定邮箱。</b>如果有人以别的理由（中奖、退款、客服核验）向你索要它，那是诈骗。</p>
  <p>如果这不是你本人的操作，忽略这封邮件即可，你的邮箱不会被绑定。</p>
</div></body></html>', 'freemarker', 0),

-- 会员：重置密码 —— 措辞最重，拿到这个码就能改密码
('member_reset_password_code', '重置密码验证码',
 '<!DOCTYPE HTML><html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body>
<div style="margin:0 auto;width:690px;font-family:Helvetica,Arial,sans-serif;line-height:28px;">
  <h2>重置密码验证码</h2>
  <p>有人正在用这个邮箱<b>重置账号密码</b>。请在重置页面输入：</p>
  <p style="font-size:28px;letter-spacing:6px;"><b>${code}</b></p>
  <p>验证码 ${minutes} 分钟内有效。</p>
  <p><b>拿到这串数字就能改掉你的密码。</b>不要告诉任何人，包括自称客服的人。</p>
  <p style="color:#b00;"><b>如果这不是你本人的操作，请立即登录并修改密码</b> ——
     有人知道你的邮箱地址，并且正在尝试接管你的账号。</p>
</div></body></html>', 'freemarker', 0);


-- =====================================================================================
-- 执行后
--
-- ① 上面五行也已同步进 mysql/data-baseline.sql，新环境建出来就有。
-- ② ${code} 与 ${minutes} 由 MemberEmailCodeService 传入；模板类型是 freemarker，
--    写错占位符不会报错，只会把 ${xxx} 原样发给用户。改文案后请自己发一封看看。
-- ③ ⚠️ spring.mail.* 目前还是上游 demo 的账号（lab1024@163.com），
--    真要把信发出去得先换成自己的 SMTP。test-connection: false，所以不换也不会启动失败，
--    只是发信时抛异常 —— 那条路径返回 SEND_FAILED 并打 error 日志。
-- =====================================================================================
