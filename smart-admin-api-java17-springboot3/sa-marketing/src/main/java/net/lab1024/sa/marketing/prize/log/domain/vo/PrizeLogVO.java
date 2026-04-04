package net.lab1024.sa.marketing.prize.log.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-奖励发放执行明细与快照表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 18:42:42
 * @Copyright weolwo
 */

@Data
public class PrizeLogVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "关联的提案ID (t_promotion_proposal.id)")
    private Long proposalId;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "关联的大礼包ID")
    private String prizeGroupId;

    @Schema(description = "触发此发放的具体奖品明细项ID (t_prize_config.id)")
    private Long prizeConfigId;

    @Schema(description = "实际扣减的兜底优惠池ID")
    private String promotionConfigId;

    @Schema(description = "奖品级别：1(一等奖), 0(无级别)")
    private Integer prizeLevel;

    @Schema(description = "奖品名称快照")
    private String prizeName;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL")
    private String prizeType;

    @Schema(description = "发放的具体值(积分数/券ID)")
    private String prizeValue;

    @Schema(description = "【字典】执行状态：0-发放中, 1-成功, 2-失败")
    private Integer status;

    @Schema(description = "下游外部单号")
    private String externalBizNo;

    @Schema(description = "异常原因")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
