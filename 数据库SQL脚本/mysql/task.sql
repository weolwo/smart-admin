-- ==========================================
-- 一、 核心配置域 (Config Domain)
-- ==========================================

CREATE TABLE `t_task_template`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `template_code` varchar(64)  NOT NULL COMMENT '模板唯一标识，如 T_ORDER_001',
    `template_name` varchar(128) NOT NULL COMMENT '模板名称',
    `task_type`     varchar(32)  NOT NULL COMMENT '【字典】流转类型：SIMPLE(单次节点型), COUNT(计次型), AMOUNT(计额型)',
    `ui_schema`     json         NOT NULL COMMENT '前端表单动态渲染规则 JSON',
    `rule_script`   text         NOT NULL COMMENT '底层的 QLExpress 校验脚本',
    `create_by`     varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_tpl_code` (`template_code`)
) COMMENT ='系统级-任务模板表';

CREATE TABLE `t_task_config`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `task_name`       varchar(128) NOT NULL COMMENT '面向C端展示的任务名称',
    `template_code`   varchar(64)  NOT NULL COMMENT '关联模板Code',
    `trigger_event`   varchar(64)  NOT NULL COMMENT '【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义)',
    `task_group`      varchar(32)           DEFAULT 'DEFAULT' COMMENT '【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)',
    `target_audience` varchar(32)           DEFAULT 'ALL' COMMENT '【字典】目标人群：ALL(全部), NEW_MEMBER(新会员), OLD_MEMBER(老会员)',
    `limit_type`      varchar(32)  NOT NULL DEFAULT 'ONCE' COMMENT '【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制)',
    `limit_count`     int          NOT NULL DEFAULT '1' COMMENT '配合频次类型，限制次数',
    `rule_config`     json         NOT NULL COMMENT '业务规则参数 JSON',
    `sort_weight`     int          NOT NULL DEFAULT '0' COMMENT '前端排序权重，越大越靠前',
    `action_url`      varchar(255)          DEFAULT NULL COMMENT '前端【去完成】跳转路由',
    `ui_config`       json                  DEFAULT NULL COMMENT '前端展示UI补充(图标/角标等)',
    `status`          tinyint      NOT NULL DEFAULT '0' COMMENT '【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线',
    `start_time`      datetime              DEFAULT NULL COMMENT '任务开始时间',
    `end_time`        datetime              DEFAULT NULL COMMENT '任务结束时间',
    `create_by`       varchar(64)           DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)           DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_t_biz_tsk_cfg_grp_aud` (`task_group`, `target_audience`),
    KEY `idx_t_biz_tsk_cfg_evt_sts` (`trigger_event`, `status`)
) COMMENT ='业务级-任务规则配置表';

-- ==========================================
-- 二、 奖品包与资产兜底域 (Reward & Asset Config)
-- ==========================================

CREATE TABLE `t_promotion_config`
(
    `id`                varchar(64)    NOT NULL COMMENT '配置ID，业务主键如 PRM_1001',
    `promo_name`        varchar(128)   NOT NULL COMMENT '优惠配置名称，如：双11十元券兜底配置',
    `prize_type`        varchar(32)    NOT NULL COMMENT '【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)',

    -- 预算与库存控制
    `total_quota`       int            NOT NULL DEFAULT '-1' COMMENT '总库存(个数)：-1为不限制(适用于券/实物)',
    `used_quota`        int            NOT NULL DEFAULT '0' COMMENT '已消耗库存(个数)',
    `total_amount`      decimal(18, 4) NOT NULL DEFAULT '-1.0000' COMMENT '总预算(金额)：-1为不限制(适用于积分/现金)',
    `used_amount`       decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '已消耗预算(金额)',

    -- 单次发放兜底限制
    `single_max_quota`  int            NOT NULL DEFAULT '1' COMMENT '单次最大数量兜底，超限阻断',
    `single_max_amount` decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '单次最大金额兜底，超限阻断',

    -- 风控与防刷规则
    `limit_period`      varchar(32)    NOT NULL DEFAULT 'LIFETIME' COMMENT '【字典】限制周期：LIFETIME(终身), DAILY(每日), WEEKLY(每周), MONTHLY(每月), CUSTOM',
    `identify_limit`    int            NOT NULL DEFAULT '1' COMMENT '同周期内，单会员ID最多领取次数 (-1为不限)',
    `phone_limit`       int            NOT NULL DEFAULT '1' COMMENT '同周期内，单手机号最多领取次数 (-1为不限)',
    `ip_limit`          int            NOT NULL DEFAULT '1' COMMENT '同周期内，单IP地址最多领取次数 (-1为不限)',
    `device_limit`      int            NOT NULL DEFAULT '1' COMMENT '同周期内，单设备硬件号(IMEI/IDFA)最多领取次数 (-1为不限)',
    `fingerprint_limit` int            NOT NULL DEFAULT '1' COMMENT '同周期内，单客户端指纹最多领取次数 (-1为不限)',
    `mutex_rule`        json                    DEFAULT NULL COMMENT '互斥规则：存互斥的优惠配置ID数组',

    -- 审批流控制
    `review_type`       tinyint        NOT NULL DEFAULT '0' COMMENT '【字典】人工审核：0-免审, 1-笔笔审, 2-超阈值审',
    `review_threshold`  decimal(18, 4)          DEFAULT '0.0000' COMMENT '触发审核的阈值',

    `status`            tinyint        NOT NULL DEFAULT '1' COMMENT '【字典】状态：0-停用, 1-启用',
    `create_by`         varchar(64)             DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         varchar(64)             DEFAULT NULL COMMENT '更新人',
    `update_time`       datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) COMMENT ='资产域-优惠配置(预算与风控兜底)表';

CREATE TABLE `t_prize_group`
(
    `id`          varchar(64)  NOT NULL COMMENT '奖励包ID，如 GRP_1001',
    `group_name`  varchar(128) NOT NULL COMMENT '奖励包名称，如：新人注册大礼包',
    `description` varchar(255)          DEFAULT NULL COMMENT '给前端展示的文案/描述',
    `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '【字典】状态：0-停用, 1-启用',
    `create_by`   varchar(64)           DEFAULT NULL COMMENT '创建人',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   varchar(64)           DEFAULT NULL COMMENT '更新人',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) COMMENT ='业务级-奖励包(大礼包)外壳表';

CREATE TABLE `t_prize_config`
(
    `id`                  bigint         NOT NULL AUTO_INCREMENT,
    `prize_group_id`      varchar(64)    NOT NULL COMMENT '关联的上层奖励包ID (t_prize_group.id)',
    `promotion_config_id` varchar(64)    NOT NULL COMMENT '绑定的底层优惠兜底配置ID (指向 t_promotion_config)',

    `prize_type`          varchar(32)    NOT NULL COMMENT '【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL',
    `prize_name`          varchar(128)   NOT NULL COMMENT '奖品展示名称：如“双11特等奖”或“100积分”',
    `grant_mode`          varchar(32)    NOT NULL DEFAULT 'FIXED' COMMENT '【字典】发奖机制：FIXED, RANDOM, PROBABILITY',

    `prize_level`         int                     DEFAULT '0' COMMENT '奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)',
    `prize_amount_min`    decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '发放数量下限(固定发放时与上限一致)',
    `prize_amount_max`    decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '发放数量上限',
    `probability`         decimal(8, 4)           DEFAULT '100.0000' COMMENT '中奖概率(万分位)',
    `sort_weight`         int            NOT NULL DEFAULT '0' COMMENT '前端展示排序权重',

    `status`              tinyint        NOT NULL DEFAULT '1' COMMENT '【字典】状态：0-停用, 1-启用',
    `create_by`           varchar(64)             DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`           varchar(64)             DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_t_prz_cfg_grp` (`prize_group_id`)
) COMMENT ='业务级-发奖规则与奖品明细表';

CREATE TABLE `t_task_prize_mapping`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT,
    `task_config_id`  bigint      NOT NULL COMMENT '关联的任务配置ID (t_task_config.id)',

    `stage_level`     int         NOT NULL DEFAULT '1' COMMENT '任务阶段：单次任务填1，阶梯任务填 1, 2, 3...',
    `stage_threshold` varchar(255)         DEFAULT NULL COMMENT '达标阈值快照：如 "100.00" 或 "3"',
    `prize_group_id`  varchar(64) NOT NULL COMMENT '触发的奖励包ID (t_prize_group.id)',

    `create_by`       varchar(64)          DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)          DEFAULT NULL COMMENT '更新人',
    `update_time`     datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_stage` (`task_config_id`, `stage_level`),
    KEY `idx_prize_group` (`prize_group_id`)
) COMMENT ='业务级-任务阶段与奖励包映射表';

-- ==========================================
-- 三、 任务运行与发奖调度域 (Task Runtime & Proposal)
-- ==========================================

CREATE TABLE `t_task_record`
(
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `member_id`        varchar(64)    NOT NULL COMMENT '会员ID',
    `task_config_id`   bigint         NOT NULL COMMENT '关联的任务配置ID',

    `period_key`       varchar(32)    NOT NULL DEFAULT 'NONE' COMMENT '业务期数标识(防重用)：NONE, 日期(20260402)',
    `valid_start_time` datetime       NOT NULL COMMENT '该实例生效开始时间',
    `valid_end_time`   datetime       NOT NULL COMMENT '该实例失效过期时间',
    `current_metric`   decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '当前进度值：如已签到 3.0000 天',

    `status`           tinyint        NOT NULL DEFAULT '0' COMMENT '【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期',
    `progress_data`    json                    DEFAULT NULL COMMENT '进度详情快照',
    `rule_snapshot`    json           NOT NULL COMMENT '接取任务时的规则快照',
    `prize_snapshot`   json           NOT NULL COMMENT '接取任务时的奖励快照',
    `complete_time`    datetime                DEFAULT NULL COMMENT '真正达标的时间',

    `create_by`        varchar(64)             DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64)             DEFAULT NULL COMMENT '更新人',
    `update_time`      datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_tsk_rec_mbr_cfg_prd` (`member_id`, `task_config_id`, `period_key`),
    KEY `idx_t_tsk_rec_mbr_sts` (`member_id`, `status`),
    KEY `idx_t_tsk_rec_expire` (`status`, `valid_end_time`)
) COMMENT ='运行时-会员任务进度实例表';

CREATE TABLE `t_promotion_proposal`
(
    `id`                  bigint      NOT NULL AUTO_INCREMENT,
    `member_id`           varchar(64) NOT NULL,
    `task_record_id`      bigint      NOT NULL COMMENT '来源任务实例ID',
    `promotion_config_id` varchar(64) NOT NULL COMMENT '关联的优惠配置ID',

    `status`              int         NOT NULL DEFAULT '0' COMMENT '【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截',
    `stage_level`         int         NOT NULL DEFAULT '1' COMMENT '对应任务的哪个阶段',
    `remark`              varchar(255)         DEFAULT NULL COMMENT '执行失败/风控拦截原因',

    `first_reviewer`      varchar(64)          DEFAULT NULL COMMENT '一审人',
    `first_review_time`   datetime             DEFAULT NULL COMMENT '一审时间',
    `second_reviewer`     varchar(64)          DEFAULT NULL COMMENT '二审人',
    `second_review_time`  datetime             DEFAULT NULL COMMENT '二审时间',
    `review_comment`      varchar(255)         DEFAULT NULL COMMENT '审核意见/驳回理由',

    `create_by`           varchar(64)          DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`           varchar(64)          DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_prm_prop_tsk` (`task_record_id`),
    UNIQUE KEY `uk_t_prm_prop_tsk_stg` (`task_record_id`, `stage_level`),
    KEY `idx_t_prm_prop_sts` (`status`)
) COMMENT ='资产域-统一发奖提案控制表';

CREATE TABLE `t_prize_log`
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT,
    `proposal_id`         bigint       NOT NULL COMMENT '关联的提案ID (t_promotion_proposal.id)',
    `member_id`           varchar(64)  NOT NULL,

    `prize_group_id`      varchar(64)  NOT NULL COMMENT '关联的大礼包ID',
    `prize_config_id`     bigint       NOT NULL COMMENT '触发此发放的具体奖品明细项ID (t_prize_config.id)',
    `promotion_config_id` varchar(64)  NOT NULL COMMENT '实际扣减的兜底优惠池ID',

    `prize_level`         int                   DEFAULT '0' COMMENT '奖品级别：1(一等奖), 0(无级别)',
    `prize_name`          varchar(128) NOT NULL COMMENT '奖品名称快照',
    `prize_type`          varchar(32)  NOT NULL COMMENT '【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL',
    `prize_value`         varchar(128) NOT NULL COMMENT '发放的具体值(积分数/券ID)',

    `status`              tinyint      NOT NULL DEFAULT '0' COMMENT '【字典】执行状态：0-发放中, 1-成功, 2-失败',
    `external_biz_no`     varchar(128)          DEFAULT NULL COMMENT '下游外部单号',
    `remark`              varchar(255)          DEFAULT NULL COMMENT '异常原因',

    `create_by`           varchar(64)           DEFAULT NULL COMMENT '创建人',
    `create_time`         datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`           varchar(64)           DEFAULT NULL COMMENT '更新人',
    `update_time`         datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_prz_log_prop_strg` (`proposal_id`, `prize_config_id`)
) COMMENT ='资产域-奖励发放执行明细与快照表';

-- ==========================================
-- 四、 资产账务与实例域 (Ledger & Asset Instances)
-- ==========================================

CREATE TABLE `t_member_wallet`
(
    `id`            bigint         NOT NULL AUTO_INCREMENT,
    `member_id`     varchar(64)    NOT NULL,
    `score_balance` decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '积分余额',
    `cash_balance`  decimal(18, 4) NOT NULL DEFAULT '0.0000' COMMENT '现金余额',
    `status`        tinyint        NOT NULL DEFAULT '1' COMMENT '【字典】状态：0-冻结, 1-正常',
    `version`       int            NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',

    `create_by`     varchar(64)             DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)             DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_biz_mbr_mbr` (`member_id`)
) COMMENT ='账务域-会员资金/积分主账表';

CREATE TABLE `t_member_asset_transaction`
(
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `member_id`        varchar(64)    NOT NULL,
    `asset_type`       varchar(32)    NOT NULL COMMENT '【字典】资产类型：SCORE, BALANCE',
    `transaction_type` tinyint        NOT NULL COMMENT '【字典】资金流向：1-收入, 2-支出',
    `change_amount`    decimal(18, 4) NOT NULL COMMENT '变动绝对值',
    `balance_after`    decimal(18, 4) NOT NULL COMMENT '变动后最新余额',
    `biz_type`         varchar(64)    NOT NULL COMMENT '【字典】业务类型：TASK_PRIZE, CONSUME, MANUAL_ADJUST',
    `biz_ref_id`       varchar(128)   NOT NULL COMMENT '关联外部业务ID(如 prize_log_id)',
    `remark`           varchar(255) DEFAULT NULL COMMENT 'C端展示摘要',

    `create_by`        varchar(64)  DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64)  DEFAULT NULL COMMENT '更新人',
    `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_t_biz_mbr_ast_txn_time` (`member_id`, `asset_type`, `create_time`),
    UNIQUE KEY `uk_t_biz_mbr_ast_txn_ref` (`biz_ref_id`, `asset_type`)
) COMMENT ='账务域-资产变动交易明细表';

CREATE TABLE `t_member_coupon`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `member_id`        varchar(64)  NOT NULL,
    `coupon_code`      varchar(64)  NOT NULL COMMENT '券模编码',
    `coupon_name`      varchar(128) NOT NULL COMMENT '券名称',
    `status`           tinyint      NOT NULL DEFAULT '0' COMMENT '【字典】状态：0-未使用, 1-已使用, 2-已过期, 3-已作废',
    `source_type`      varchar(64)  NOT NULL COMMENT '【字典】获取来源：TASK_PRIZE, MANUAL_SEND',
    `valid_start_time` datetime     NOT NULL COMMENT '有效期开始',
    `valid_end_time`   datetime     NOT NULL COMMENT '有效期结束',
    `used_time`        datetime              DEFAULT NULL COMMENT '核销时间',

    `create_by`        varchar(64)           DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64)           DEFAULT NULL COMMENT '更新人',
    `update_time`      datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_t_biz_mbr_cpn_sts` (`member_id`, `coupon_code`)
) COMMENT ='资产域-会员优惠券实例表';

CREATE TABLE `t_physical_delivery`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT,
    `member_id`         varchar(64)  NOT NULL,
    `proposal_id`       bigint       NOT NULL COMMENT '来源发奖提案ID',
    `prize_config_id`   bigint       NOT NULL COMMENT '触发此发货的奖品配置ID (t_prize_config.id)',

    `receiver_name`     varchar(64)  NOT NULL COMMENT '收件人姓名',
    `receiver_phone`    varchar(32)  NOT NULL COMMENT '收件人电话',
    `receiver_address`  varchar(255) NOT NULL COMMENT '收件详细地址',
    `logistics_company` varchar(64)           DEFAULT NULL COMMENT '【字典】物流公司：SF, JD, YTO',
    `logistics_no`      varchar(128)          DEFAULT NULL COMMENT '物流单号',
    `status`            tinyint      NOT NULL DEFAULT '0' COMMENT '【字典】状态：0-待发货, 1-已发货, 2-已签收, 3-异常退回',

    `create_by`         varchar(64)           DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         varchar(64)           DEFAULT NULL COMMENT '更新人',
    `update_time`       datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_biz_phy_dlv_prop_pool` (`proposal_id`, `prize_config_id`)
) COMMENT ='资产域-实物发货物流表';