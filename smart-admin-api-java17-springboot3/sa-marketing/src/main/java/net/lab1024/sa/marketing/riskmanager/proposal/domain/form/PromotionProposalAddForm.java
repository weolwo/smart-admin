package net.lab1024.sa.marketing.riskmanager.proposal.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产域-统一发奖提案控制表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */

@Data
public class PromotionProposalAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "来源任务实例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源任务实例ID 不能为空")
    private Long taskRecordId;

    @Schema(description = "关联的优惠配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联的优惠配置ID 不能为空")
    private String promotionConfigId;

    @Schema(description = "【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截 不能为空")
    private Integer status;

    @Schema(description = "对应任务的哪个阶段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "对应任务的哪个阶段 不能为空")
    private Integer stageLevel;

}