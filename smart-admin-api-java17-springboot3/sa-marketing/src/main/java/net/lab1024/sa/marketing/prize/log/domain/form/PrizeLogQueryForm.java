package net.lab1024.sa.marketing.prize.log.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产域-奖励发放执行明细与快照表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:42:42
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PrizeLogQueryForm extends PageParam {

    @Schema(description = "关联的提案ID (tPromotionProposal.id)")
    private Long proposalId;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "奖品级别：1(一等奖), 0(无级别)")
    private Integer prizeLevel;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

}
