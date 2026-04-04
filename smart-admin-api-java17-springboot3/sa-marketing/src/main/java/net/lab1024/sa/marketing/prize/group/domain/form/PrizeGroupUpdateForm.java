package net.lab1024.sa.marketing.prize.group.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 业务级-奖励包(大礼包)外壳表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */

@Data
public class PrizeGroupUpdateForm {

    @Schema(description = "奖励包ID，如 GRP_1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖励包ID，如 GRP_1001 不能为空")
    private String id;

}