package net.lab1024.sa.marketing.ledger.wallet.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账务域-会员资金/积分主账表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:17:33
 * @Copyright weolwo
 */

@Data
public class MemberWalletUpdateForm {

    @Schema(description = "if", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "if 不能为空")
    private Long id;

}