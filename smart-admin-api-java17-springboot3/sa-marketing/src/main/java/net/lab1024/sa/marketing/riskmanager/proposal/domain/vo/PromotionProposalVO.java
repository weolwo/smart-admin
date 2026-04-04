package net.lab1024.sa.marketing.riskmanager.proposal.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-统一发奖提案控制表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */

@Data
public class PromotionProposalVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "来源任务实例ID")
    private Long taskRecordId;

    @Schema(description = "关联的优惠配置ID")
    private String promotionConfigId;

    @Schema(description = "【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截")
    private Integer status;

    @Schema(description = "对应任务的哪个阶段")
    private Integer stageLevel;

    @Schema(description = "执行失败/风控拦截原因")
    private String remark;

    @Schema(description = "一审人")
    private String firstReviewer;

    @Schema(description = "一审时间")
    private LocalDateTime firstReviewTime;

    @Schema(description = "二审人")
    private String secondReviewer;

    @Schema(description = "二审时间")
    private LocalDateTime secondReviewTime;

    @Schema(description = "审核意见/驳回理由")
    private String reviewComment;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
