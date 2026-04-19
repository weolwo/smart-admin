package net.lab1024.sa.draw.poolconfig.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 奖池配置 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-19 09:42:12
 * @Copyright weolwo
 */

@Data
public class PrizePoolConfigAddForm {

    @Schema(description = "租户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "租户id 不能为空")
    private String tenantId;

    @Schema(description = "活动编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "活动编码 不能为空")
    private String activityCode;

    @Schema(description = "奖池唯一编码 (如: VIP_POOL)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖池唯一编码 (如: VIP_POOL) 不能为空")
    private String poolCode;

    @Schema(description = "奖池名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖池名称 不能为空")
    private String poolName;

    @Schema(description = "消耗资产类型: CREDIT(积分), TICKET(抽奖券), NONE(无消耗)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消耗资产类型: CREDIT(积分), TICKET(抽奖券), NONE(无消耗) 不能为空")
    private String costAssetType;

    @Schema(description = "消耗数值(单价)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "消耗数值(单价) 不能为空")
    private BigDecimal costValue;

    @Schema(description = "重置周期，天，周，月，活动期间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "重置周期，天，周，月，活动期间 不能为空")
    private String resetPeriod;

    @Schema(description = "抽奖算法: 1-按概率(probability), 2-按库存比例(stock_ratio)")
    private Integer drawMode;

    @Schema(description = "进入该奖池的前置脚本")
    private String scriptId;

    @Schema(description = "0关闭，1开启")
    private Integer status;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}