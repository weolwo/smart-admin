package net.lab1024.sa.marketing.ledger.transaction.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账务域-资产变动交易明细表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:11:19
 * @Copyright weolwo
 */

@Data
public class MemberAssetTransactionUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}