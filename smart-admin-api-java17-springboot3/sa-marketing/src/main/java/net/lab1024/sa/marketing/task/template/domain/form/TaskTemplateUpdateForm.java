package net.lab1024.sa.marketing.task.template.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 系统级-任务模板表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */

@Data
public class TaskTemplateUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}