package net.lab1024.sa.marketing.prize.mapping.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 业务级-任务阶段与奖励包映射表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:01:05
 * @Copyright weolwo
 */

@Data
public class TaskPrizeMappingUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}