package net.lab1024.sa.marketing.riskmanager.config.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-优惠配置(预算与风控兜底)表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */

@Data
@TableName("t_promotion_config")
public class PromotionConfig {

    /**
     * 配置ID，业务主键如 PRM_1001
     */
    @TableId
    private String id;

    /**
     * 优惠配置名称，如：双11十元券兜底配置
     */
    private String promoName;

    /**
     * 【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)
     */
    private String prizeType;

    /**
     * 总库存(个数)：-1为不限制(适用于券/实物)
     */
    private Integer totalQuota;

    /**
     * 已消耗库存(个数)
     */
    private Integer usedQuota;

    /**
     * 总预算(金额)：-1为不限制(适用于积分/现金)
     */
    private BigDecimal totalAmount;

    /**
     * 已消耗预算(金额)
     */
    private BigDecimal usedAmount;

    /**
     * 单次最大数量兜底，超限阻断
     */
    private Integer singleMaxQuota;

    /**
     * 单次最大金额兜底，超限阻断
     */
    private BigDecimal singleMaxAmount;

    /**
     * 【字典】限制周期：LIFETIME(终身), DAILY(每日), WEEKLY(每周), MONTHLY(每月), CUSTOM
     */
    private String limitPeriod;

    /**
     * 同周期内，单会员ID最多领取次数 (-1为不限)
     */
    private Integer identifyLimit;

    /**
     * 同周期内，单手机号最多领取次数 (-1为不限)
     */
    private Integer phoneLimit;

    /**
     * 同周期内，单IP地址最多领取次数 (-1为不限)
     */
    private Integer ipLimit;

    /**
     * 同周期内，单设备硬件号(IMEI/IDFA)最多领取次数 (-1为不限)
     */
    private Integer deviceLimit;

    /**
     * 同周期内，单客户端指纹最多领取次数 (-1为不限)
     */
    private Integer fingerprintLimit;

    /**
     * 互斥规则：存互斥的优惠配置ID数组
     */
    private String mutexRule;

    /**
     * 【字典】人工审核：0-免审, 1-笔笔审, 2-超阈值审
     */
    private Integer reviewType;

    /**
     * 触发审核的阈值
     */
    private BigDecimal reviewThreshold;

    /**
     * 【字典】状态：0-停用, 1-启用
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
