package net.lab1024.sa.marketing.prize.log.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产域-奖励发放执行明细与快照表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:42:42
 * @Copyright weolwo
 */

@Data
public class PrizeLogAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "关联的提案ID (t_promotion_proposal.id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联的提案ID (t_promotion_proposal.id) 不能为空")
    private Long proposalId;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "关联的大礼包ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联的大礼包ID 不能为空")
    private String prizeGroupId;

    @Schema(description = "触发此发放的具体奖品明细项ID (t_prize_config.id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "触发此发放的具体奖品明细项ID (t_prize_config.id) 不能为空")
    private Long prizeConfigId;

    @Schema(description = "实际扣减的兜底优惠池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "实际扣减的兜底优惠池ID 不能为空")
    private String promotionConfigId;

    @Schema(description = "奖品级别：1(一等奖), 0(无级别)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "奖品级别：1(一等奖), 0(无级别) 不能为空")
    private Integer prizeLevel;

    @Schema(description = "奖品名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖品名称快照 不能为空")
    private String prizeName;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL 不能为空")
    private String prizeType;

    @Schema(description = "发放的具体值(积分数/券ID)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "发放的具体值(积分数/券ID) 不能为空")
    private String prizeValue;

    @Schema(description = "【字典】执行状态：0-发放中, 1-成功, 2-失败", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】执行状态：0-发放中, 1-成功, 2-失败 不能为空")
    private Integer status;

}