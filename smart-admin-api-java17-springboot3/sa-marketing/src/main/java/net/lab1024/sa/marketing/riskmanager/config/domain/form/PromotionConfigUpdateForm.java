package net.lab1024.sa.marketing.riskmanager.config.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 资产域-优惠配置(预算与风控兜底)表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */

@Data
public class PromotionConfigUpdateForm {

    @Schema(description = "配置ID，业务主键如 PRM_1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置ID，业务主键如 PRM_1001 不能为空")
    private String id;

}