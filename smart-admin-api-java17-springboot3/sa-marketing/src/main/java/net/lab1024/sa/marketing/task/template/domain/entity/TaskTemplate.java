package net.lab1024.sa.marketing.task.template.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统级-任务模板表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */

@Data
@TableName("t_task_template")
public class TaskTemplate {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板唯一标识，如 T_ORDER_001
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 【字典】流转类型：SIMPLE(单次节点型), COUNT(计次型), AMOUNT(计额型)
     */
    private String taskType;

    /**
     * 前端表单动态渲染规则 JSON
     */
    private String uiSchema;

    /**
     * 底层的 QLExpress 校验脚本
     */
    private String ruleScript;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
