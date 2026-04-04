package net.lab1024.sa.marketing.prize.config.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 业务级-发奖规则与奖品明细表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */

@Data
public class PrizeConfigAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "关联的上层奖励包ID (t_prize_group.id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联的上层奖励包ID (t_prize_group.id) 不能为空")
    private String prizeGroupId;

    @Schema(description = "绑定的底层优惠兜底配置ID (指向 t_promotion_config)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "绑定的底层优惠兜底配置ID (指向 t_promotion_config) 不能为空")
    private String promotionConfigId;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL 不能为空")
    private String prizeType;

    @Schema(description = "奖品展示名称：如“双11特等奖”或“100积分”", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖品展示名称：如“双11特等奖”或“100积分” 不能为空")
    private String prizeName;

    @Schema(description = "【字典】发奖机制：FIXED, RANDOM, PROBABILITY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】发奖机制：FIXED, RANDOM, PROBABILITY 不能为空")
    private String grantMode;

    @Schema(description = "奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "奖品级别：1(一等奖), 2(二等奖), 0(普通奖品) 不能为空")
    private Integer prizeLevel;

    @Schema(description = "发放数量下限(固定发放时与上限一致)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发放数量下限(固定发放时与上限一致) 不能为空")
    private BigDecimal prizeAmountMin;

    @Schema(description = "发放数量上限", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发放数量上限 不能为空")
    private BigDecimal prizeAmountMax;

    @Schema(description = "前端展示排序权重", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "前端展示排序权重 不能为空")
    private Integer sortWeight;

    @Schema(description = "【字典】状态：0-停用, 1-启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-停用, 1-启用 不能为空")
    private Integer status;

}