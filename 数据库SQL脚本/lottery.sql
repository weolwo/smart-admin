DROP TABLE IF EXISTS `t_lottery_config`;
CREATE TABLE `t_lottery_config`
(
    `id`              bigint         NOT NULL AUTO_INCREMENT comment 'id',
    `tenant_id`       varchar(16)    NOT NULL DEFAULT '0',
    `activity_code`   varchar(32)    NOT NULL COMMENT '挂载的活动编码',
    `lottery_code`    varchar(32)    NOT NULL COMMENT '彩票玩法唯一编码',
    `lottery_name`    varchar(128)   NOT NULL COMMENT '彩票名称 (如: 五一狂欢盲盒券)',
    `number_charset`  varchar(32)    NOT NULL DEFAULT '0-9' COMMENT '字符集：0-9, A-Z',
    `number_length`   tinyint        NOT NULL DEFAULT 5 COMMENT '号码长度',
    `total_count`     int            NOT NULL COMMENT '号池总数 (如: 1,000,000)',
    `cost_asset_type` varchar(32)    NOT NULL DEFAULT 'SCORE' COMMENT '入场成本-资产类型 (SCORE, FREE)',
    `cost_value`      decimal(18, 4) NOT NULL DEFAULT '0' COMMENT '入场成本-数值',

    `status`          tinyint        NOT NULL DEFAULT '1' COMMENT '状态：0-下线, 1-上线',
    `create_time`     datetime                DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lottery_code` (`lottery_code`)
) COMMENT ='彩票配置';
DROP TABLE IF EXISTS `t_lottery_issue`;
CREATE TABLE `t_lottery_issue`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT comment 'id',
    `tenant_id`       varchar(16) NOT NULL DEFAULT '0',
    `lottery_code`    varchar(32) NOT NULL COMMENT '关联玩法编码',
    `issue_no`        varchar(32) NOT NULL COMMENT '期号 (如: 20260405_01)',
    `sold_count`      int         NOT NULL DEFAULT 0 COMMENT '已售/已派发数量',

    -- 【时间与开奖控制】
    `sell_start_time` datetime    NOT NULL COMMENT '开始售卖/派发时间',
    `sell_end_time`   datetime    NOT NULL COMMENT '结束时间',
    `open_time`       datetime             DEFAULT NULL COMMENT '计划开奖时间(可空，支持随时开)',

    `can_repeat`      tinyint     NOT NULL DEFAULT 0 COMMENT '是否可重复开奖：1-支持边卖边开/多轮开奖',
    `winning_number`  json                 DEFAULT NULL COMMENT '开奖号码快照 (支持存入多个中奖号,如 ["123456", "888888"])',
    `status`          tinyint     NOT NULL DEFAULT '0' COMMENT '状态: 0-待开奖, 1-售卖中, 2-已开奖',
    `create_time`     datetime             DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_issue_no` (`lottery_code`, `issue_no`)
) COMMENT ='期号配置';
DROP TABLE IF EXISTS `t_lottery_prize_rule`;
CREATE TABLE `t_lottery_prize_rule`
(
    `id`           bigint         NOT NULL AUTO_INCREMENT,
    `tenant_id`    varchar(16)    NOT NULL DEFAULT '0',
    `lottery_code` varchar(32)    NOT NULL COMMENT '关联玩法编码',

    `prize_level`  int            NOT NULL COMMENT '奖级: 1(一等奖), 2(二等奖)',
    `prize_name`   varchar(64)    NOT NULL COMMENT '如: 终极大奖',
    `win_count`    int            NOT NULL DEFAULT '1' COMMENT '该奖级本期开出几个号码？(如一等奖开1个，二等奖开10个)',

    -- 【关联底层财务与资产】
    `prize_code`   varchar(64)    NOT NULL COMMENT '奖品编码',
    `prize_value`  decimal(18, 4) NOT NULL COMMENT '运营输入的奖金金额/积分数量',
    `create_time`  datetime                DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lottery_level` (`lottery_code`, `prize_level`)
) COMMENT ='奖级规则配置';

DROP TABLE IF EXISTS `t_lottery_ticket_record`;
CREATE TABLE `t_lottery_ticket_record`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `tenant_id`     varchar(16)  NOT NULL DEFAULT '0',
    `lottery_code`  varchar(32)  NOT NULL COMMENT '关联玩法编码',
    `issue_no`      varchar(32)  NOT NULL COMMENT '归属期号',
    `ticket_number` varchar(32)  NOT NULL COMMENT '提前生成的号码 (如: 000001)',

    -- 【用户归属】初始为NULL，售出时UPDATE
    `member_name`   varchar(64)           DEFAULT NULL COMMENT '获得该号码的会员名',
    `source_type`   varchar(32)           DEFAULT NULL COMMENT '获取来源: EXCHANGE, TASK_REWARD',
    `source_biz_id` varchar(64)           DEFAULT NULL COMMENT '溯源单号',
    `obtain_time`   datetime              DEFAULT NULL COMMENT '用户获取时间',

    -- 【状态管理】
    `win_status`    tinyint      NOT NULL DEFAULT 0 COMMENT '中奖状态: 0-未开奖, 1-未中奖, 已开奖',
    `prize_level`   int                   DEFAULT NULL COMMENT '若中奖，对应的奖级',
    `security_sign` varchar(128) NOT NULL COMMENT '防篡改签名',
    `create_time`   datetime              DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    -- 【核心索引】
    UNIQUE KEY `uk_issue_ticket` (`lottery_code`, `issue_no`, `ticket_number`),
    -- C端用户查询我的号码簿索引
    KEY `idx_member` (`member_name`, `issue_no`)
) COMMENT ='用户号码记录';
DROP TABLE IF EXISTS `t_lottery_number_pool`;
CREATE TABLE `t_lottery_number_pool`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `lottery_code`  varchar(32) NOT NULL COMMENT '关联活动编码',
    `ticket_number` varchar(32) NOT NULL COMMENT '原始号码字符串',
    -- 【终极发号游标键】每个活动内部绝对连续的序号 (1, 2, 3... 100000)
    `sequence_no`   int         NOT NULL COMMENT '发号序列号',
    PRIMARY KEY (`id`),
    -- 索引用于：开奖时根据中奖号字符串反查 ID
    UNIQUE KEY `uk_lottery_num` (`lottery_code`, `ticket_number`)
) COMMENT ='号码池';
