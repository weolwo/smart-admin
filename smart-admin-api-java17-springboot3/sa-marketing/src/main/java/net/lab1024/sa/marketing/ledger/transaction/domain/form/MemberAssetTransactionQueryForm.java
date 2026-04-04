package net.lab1024.sa.marketing.ledger.transaction.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账务域-资产变动交易明细表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:11:19
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberAssetTransactionQueryForm extends PageParam {

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE")
    private String assetType;

    @Schema(description = "【字典】业务类型：TASKPRIZE, CONSUME, MANUALADJUST")
    private String bizType;

    @Schema(description = "关联外部业务ID(如 prizeLogId)")
    private String bizRefId;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

}
