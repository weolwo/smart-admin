package net.lab1024.sa.marketing.task.record.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运行时-会员任务进度实例表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:03:33
 * @Copyright weolwo
 */

@Data
public class TaskRecordUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}