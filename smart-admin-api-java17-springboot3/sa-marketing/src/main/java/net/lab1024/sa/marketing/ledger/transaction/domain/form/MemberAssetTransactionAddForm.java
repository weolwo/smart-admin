package net.lab1024.sa.marketing.ledger.transaction.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 账务域-资产变动交易明细表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:11:19
 * @Copyright weolwo
 */

@Data
public class MemberAssetTransactionAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】资产类型：SCORE, BALANCE 不能为空")
    private String assetType;

    @Schema(description = "【字典】资金流向：1-收入, 2-支出", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】资金流向：1-收入, 2-支出 不能为空")
    private Integer transactionType;

    @Schema(description = "变动绝对值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变动绝对值 不能为空")
    private BigDecimal changeAmount;

    @Schema(description = "变动后最新余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变动后最新余额 不能为空")
    private BigDecimal balanceAfter;

    @Schema(description = "【字典】业务类型：TASK_PRIZE, CONSUME, MANUAL_ADJUST", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】业务类型：TASK_PRIZE, CONSUME, MANUAL_ADJUST 不能为空")
    private String bizType;

    @Schema(description = "关联外部业务ID(如 prize_log_id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联外部业务ID(如 prize_log_id) 不能为空")
    private String bizRefId;

}