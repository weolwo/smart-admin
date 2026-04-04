package net.lab1024.sa.marketing.task.template.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统级-任务模板表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */

@Data
public class TaskTemplateVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "模板唯一标识，如 T_ORDER_001")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "【字典】流转类型：SIMPLE(单次节点型), COUNT(计次型), AMOUNT(计额型)")
    private String taskType;

    @Schema(description = "前端表单动态渲染规则 JSON")
    private String uiSchema;

    @Schema(description = "底层的 QLExpress 校验脚本")
    private String ruleScript;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
