package net.lab1024.sa.marketing.riskmanager.proposal.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产域-统一发奖提案控制表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PromotionProposalQueryForm extends PageParam {

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

    @Schema(description = "来源任务实例ID")
    private Long taskRecordId;

    @Schema(description = "【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截")
    private Integer status;

    @Schema(description = "关联的优惠配置ID")
    private String promotionConfigId;

}
