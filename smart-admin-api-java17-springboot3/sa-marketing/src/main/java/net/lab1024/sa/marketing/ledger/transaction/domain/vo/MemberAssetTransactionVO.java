package net.lab1024.sa.marketing.ledger.transaction.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 账务域-资产变动交易明细表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 17:11:19
 * @Copyright weolwo
 */

@Data
public class MemberAssetTransactionVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE")
    private String assetType;

    @Schema(description = "【字典】资金流向：1-收入, 2-支出")
    private Integer transactionType;

    @Schema(description = "变动绝对值")
    private BigDecimal changeAmount;

    @Schema(description = "变动后最新余额")
    private BigDecimal balanceAfter;

    @Schema(description = "【字典】业务类型：TASK_PRIZE, CONSUME, MANUAL_ADJUST")
    private String bizType;

    @Schema(description = "关联外部业务ID(如 prize_log_id)")
    private String bizRefId;

    @Schema(description = "C端展示摘要")
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
