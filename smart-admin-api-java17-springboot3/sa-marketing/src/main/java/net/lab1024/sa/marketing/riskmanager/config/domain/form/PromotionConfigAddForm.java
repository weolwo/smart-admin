package net.lab1024.sa.marketing.riskmanager.config.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 资产域-优惠配置(预算与风控兜底)表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */

@Data
public class PromotionConfigAddForm {

    @Schema(description = "配置ID，业务主键如 PRM_1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置ID，业务主键如 PRM_1001 不能为空")
    private String id;

    @Schema(description = "优惠配置名称，如：双11十元券兜底配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "优惠配置名称，如：双11十元券兜底配置 不能为空")
    private String promoName;

    @Schema(description = "【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物) 不能为空")
    private String prizeType;

    @Schema(description = "总库存(个数)：-1为不限制(适用于券/实物)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总库存(个数)：-1为不限制(适用于券/实物) 不能为空")
    private Integer totalQuota;

    @Schema(description = "已消耗库存(个数)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已消耗库存(个数) 不能为空")
    private Integer usedQuota;

    @Schema(description = "总预算(金额)：-1为不限制(适用于积分/现金)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总预算(金额)：-1为不限制(适用于积分/现金) 不能为空")
    private BigDecimal totalAmount;

    @Schema(description = "已消耗预算(金额)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已消耗预算(金额) 不能为空")
    private BigDecimal usedAmount;

    @Schema(description = "单次最大数量兜底，超限阻断", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单次最大数量兜底，超限阻断 不能为空")
    private Integer singleMaxQuota;

    @Schema(description = "单次最大金额兜底，超限阻断", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单次最大金额兜底，超限阻断 不能为空")
    private BigDecimal singleMaxAmount;

    @Schema(description = "【字典】限制周期：LIFETIME(终身), DAILY(每日), WEEKLY(每周), MONTHLY(每月), CUSTOM", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】限制周期：LIFETIME(终身), DAILY(每日), WEEKLY(每周), MONTHLY(每月), CUSTOM 不能为空")
    private String limitPeriod;

    @Schema(description = "同周期内，单会员ID最多领取次数 (-1为不限)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "同周期内，单会员ID最多领取次数 (-1为不限) 不能为空")
    private Integer identifyLimit;

    @Schema(description = "同周期内，单手机号最多领取次数 (-1为不限)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "同周期内，单手机号最多领取次数 (-1为不限) 不能为空")
    private Integer phoneLimit;

    @Schema(description = "同周期内，单IP地址最多领取次数 (-1为不限)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "同周期内，单IP地址最多领取次数 (-1为不限) 不能为空")
    private Integer ipLimit;

    @Schema(description = "同周期内，单设备硬件号(IMEI/IDFA)最多领取次数 (-1为不限)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "同周期内，单设备硬件号(IMEI/IDFA)最多领取次数 (-1为不限) 不能为空")
    private Integer deviceLimit;

    @Schema(description = "同周期内，单客户端指纹最多领取次数 (-1为不限)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "同周期内，单客户端指纹最多领取次数 (-1为不限) 不能为空")
    private Integer fingerprintLimit;

    @Schema(description = "【字典】人工审核：0-免审, 1-笔笔审, 2-超阈值审", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】人工审核：0-免审, 1-笔笔审, 2-超阈值审 不能为空")
    private Integer reviewType;

    @Schema(description = "【字典】状态：0-停用, 1-启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-停用, 1-启用 不能为空")
    private Integer status;

}