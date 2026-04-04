package net.lab1024.sa.marketing.prize.config.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 业务级-发奖规则与奖品明细表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */

@Data
public class PrizeConfigUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}