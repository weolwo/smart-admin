-- =====================================================================================
-- 设备身份：新建 t_device，t_member_login_log 与 t_proposal_record 各加一列
-- 日期：2026-09-07
--
-- 【为什么需要它】
-- 此前系统里【没有「设备」这个概念】，所有防刷手段只能落在 IP 和手机号两个维度上，
-- 而这两个都便宜：IP 走代理池就换，手机号有黑卡市场。
--
-- 直接后果之一现在就摆在库里：t_promotion_config 的 device_limit / fingerprint_limit /
-- ip_limit / phone_limit 四列【是死配置】—— DDL 有列、Model 有字段、后台表单能填、
-- VO 能返回，但没有任何一行代码读它们。运营把「单设备每日限领 1 次」填上、保存成功、
-- 列表里也显示着，然后以为已经限住了，真实行为是完全不限。
-- 根因就是发奖链路上拿不到设备号（ProposalRecordAddCommand 里只有 memberId）。
--
-- 所以「防刷登录」和「让那四列活过来」不是两件事，是同一条链路的两端。
-- 本次只建结构，不改任何行为 —— 读写这些列的代码在后续提交里。
--
-- 【device_id 由服务端签发，不接受客户端自报】
-- 客户端自报的话，脚本每次换一个 UUID，本表就变成一张「攻击者想写多少行就写多少行」的表，
-- 设备维度的限流全部失效。签发之后，刷登录就必须先刷设备注册 ——
-- 而设备注册这一步可以让它很贵（限频、验证码、厂商证明）。
--
-- 【本表刻意没有 member_id】
-- 一台设备登多个号是【要发现的信号】，不是要建的约束。
-- 「这台设备下有哪些账号」从 t_member_login_log 聚合（下面给它加了 device_id），
-- 不单建关系表 —— 那张表已经按月分区、已经有 member_id，补一列就够了。
-- =====================================================================================


-- -------------------------------------------------------------------------------------
-- 一、设备注册表
-- -------------------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_device`;
CREATE TABLE `t_device`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT 'id',
    `device_id`        char(32)    NOT NULL COMMENT '服务端签发的设备号，不接受客户端自报',
    `device_type`      varchar(16) NOT NULL COMMENT '设备端：APP/H5/WECHAT/PC。与 t_member_login_log.device_type 同名同口径',
    `model`            varchar(64)          DEFAULT NULL COMMENT '品牌型号，客户端自报，仅供人工排查',
    `os_version`       varchar(32)          DEFAULT NULL COMMENT '系统版本：区分 iOS/Android 靠它，device_type 只到端',
    `app_version`      varchar(32)          DEFAULT NULL COMMENT '应用版本',
    `register_ip`      varchar(39)          DEFAULT NULL COMMENT '签发时IP（兼容IPv6，39位足够）',
    `register_region`  varchar(64)          DEFAULT NULL COMMENT 'IP归属地（ip2region 解析，SolvelaIpUtil 已有）',
    `key_version`      tinyint     NOT NULL DEFAULT '1' COMMENT '签发时用的HMAC密钥版本：密钥泄露要能轮换，而验签得知道该用哪一把',
    `attest_level`     tinyint     NOT NULL DEFAULT '0' COMMENT '可信度：0-仅自报, 1-验证码通过, 2-厂商证明通过（暂无厂商，先留档位）',
    `status`           tinyint     NOT NULL DEFAULT '0' COMMENT '处置档：0-正常, 1-观察（登录需验证码）, 2-封禁',
    `remark`           varchar(128)         DEFAULT NULL COMMENT '处置原因：给客服看的人话',
    `operator`         varchar(64)          DEFAULT NULL COMMENT '人工处置的操作人：status=2 时必填，用于追溯。自动降档时为空',
    `last_active_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间，节流写（>1h 才更新）',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（即签发时间）',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dev_did` (`device_id`),
    KEY                `idx_dev_status` (`status`, `last_active_time`),
    KEY                `idx_dev_ip` (`register_ip`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='设备注册表（服务端签发，不带 member_id）';

-- 字段上的几个取舍，写在这儿是因为它们看不出来：
--
--   · status 三档而不是布尔 —— 中间那档「观察」才是主力。自动封禁一旦规则写错就是
--     批量误伤且无人知晓：被封的用户不会打客服，他们只是登不进来然后走了。
--     所以只有观察档能自动降，封禁只能人工（operator 必填就是为此）。
--
--   · last_active_time 节流写 —— 每个请求都 UPDATE 一次会把这张表写成热点。
--     1 小时粒度对排查足够，而这一列的唯一用途就是排查。
--
--   · 不存 device_secret —— 令牌自包含可验签（HMAC），服务端不需要存任何
--     可用于伪造的东西。存了反而多一份泄露面。
--
--   · key_version 不能省 —— PiiHasher 的密钥是【不能改】的（改了老会员全部登不进来），
--     而设备密钥泄露时必须能换。所以两把密钥必须分开，且验签要知道用哪一把。


-- -------------------------------------------------------------------------------------
-- 二、登录日志加 device_id
--
-- 这一列是整件事里最便宜、回报最高的一笔：有了它，「一台设备碰过哪些账号」
-- 「这批号是不是同一批设备注册的」才查得出来 —— 而那正是将来判断
-- 「要不要花钱买厂商指纹」的唯一依据（现在这个问题答不了，因为等式两边的数都不知道）。
--
-- ⚠️ t_member_login_log 是按 create_time 分区的表，两条语句的代价不一样，
--    所以刻意【分开写】，别合成一条：
--      · ADD COLUMN 走 INSTANT（MySQL 8.0.29+ 支持在任意位置瞬时加列），毫秒级；
--      · ADD KEY 不支持 INSTANT，只能 INPLACE，且要逐个分区处理 —— 走低峰期。
--
-- 🔴 显式写 ALGORITHM=INSTANT 是有意的：服务端做不到时会【直接报错】，
--    而不是静默回落成 COPY 把整张分区表锁上几分钟。宁可执行失败重来。
-- -------------------------------------------------------------------------------------
ALTER TABLE `t_member_login_log`
    ADD COLUMN `device_id` char(32) DEFAULT NULL
        COMMENT '设备号，关联 t_device.device_id。为空表示该次登录发生在设备身份上线之前'
        AFTER `browser_name`, ALGORITHM = INSTANT;

ALTER TABLE `t_member_login_log`
    ADD KEY `idx_mbr_log_device` (`device_id`, `create_time`), ALGORITHM = INPLACE, LOCK = NONE;

-- 存量数据不回填，也回填不了：设备身份上线之前根本没有这个信息。
-- 为空的语义就是「上线前的登录」，查询侧要认这一点，不要把 NULL 当异常。


-- -------------------------------------------------------------------------------------
-- 三、提案表加 device_id
--
-- 这一列【不参与拦截判断】—— 发奖侧的设备维度限流走 Redis 计数（与 identify_limit
-- 现有的 risk:freq:* 同一套）。本列只服务离线分析：「这 200 笔奖是从几台设备发出去的」。
--
-- 没有它，团伙关联就只能靠 IP，而 ClientIp 的类注释已经写明 X-Forwarded-For 可伪造，
-- 只有入口网关【覆盖】而非追加时第一段才可信。
-- -------------------------------------------------------------------------------------
ALTER TABLE `t_proposal_record`
    ADD COLUMN `device_id` char(32) DEFAULT NULL
        COMMENT '发起发奖的设备号，供事后关联分析。不参与风控拦截判断'
        AFTER `member_name`, ALGORITHM = INSTANT;


-- =====================================================================================
-- 执行后必做
--
-- 本文件只是【让已有环境升上来】。另一半是让【新环境建出来就是最新的】：
--   ① schema-baseline.sql 已在本次提交里同步改好（手工加的，见下）；
--   ② tools/DumpSchema.java 的 GROUPS 已加入 t_device —— 不加的话，
--      下次导出时这张表会掉进「未分类」。
--
-- 🔴 但 schema-baseline.sql 这次是【手工改的】，而那个文件的价值全在
--    「它就是库里真实的样子」。所以在开发库执行完本文件之后，请重新跑一次
--    tools/DumpSchema.java 覆盖它，再用 git diff 核对是否与手工版一致。
--    差异只可能出在字符集、默认值的书写形式这类地方，但正是这类差异会让
--    「新环境和老环境结构不一样」而没人发现。
-- =====================================================================================
