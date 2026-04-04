package net.lab1024.sa.marketing.ledger.physical.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产域-实物发货物流表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-04 16:12:19
 * @Copyright weolwo
 */

@Data
public class PhysicalDeliveryUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}