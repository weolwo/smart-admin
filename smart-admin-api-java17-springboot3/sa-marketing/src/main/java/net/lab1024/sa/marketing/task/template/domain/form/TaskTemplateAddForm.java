package net.lab1024.sa.marketing.task.template.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 系统级-任务模板表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */

@Data
public class TaskTemplateAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "模板唯一标识，如 T_ORDER_001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板唯一标识，如 T_ORDER_001 不能为空")
    private String templateCode;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称 不能为空")
    private String templateName;

    @Schema(description = "【字典】流转类型：SIMPLE(单次节点型), COUNT(计次型), AMOUNT(计额型)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】流转类型：SIMPLE(单次节点型), COUNT(计次型), AMOUNT(计额型) 不能为空")
    private String taskType;

    @Schema(description = "前端表单动态渲染规则 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "前端表单动态渲染规则 JSON 不能为空")
    private String uiSchema;

    @Schema(description = "底层的 QLExpress 校验脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "底层的 QLExpress 校验脚本 不能为空")
    private String ruleScript;

}