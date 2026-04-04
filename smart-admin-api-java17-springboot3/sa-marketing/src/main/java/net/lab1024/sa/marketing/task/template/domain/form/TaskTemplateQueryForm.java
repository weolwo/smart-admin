package net.lab1024.sa.marketing.task.template.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统级-任务模板表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskTemplateQueryForm extends PageParam {

    @Schema(description = "模板唯一标识，如 TORDER001")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "【字典】流转类型：SIMPLE(单次节点型), COUNT(计次型), AMOUNT(计额型)")
    private String taskType;

}
