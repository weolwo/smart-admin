package net.lab1024.sa.task.taskconfig.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务配置表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-18 20:55:10
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskConfigQueryForm extends PageParam {

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "模板Code")
    private String templateCode;

    @Schema(description = "活动编码")
    private String activityCode;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "触发事件：ORDERPAID(支付), MEMBERREGISTER(注册), DAILYSIGN(签到), PAGEVIEW(浏览), CUSTOM(自定义)")
    private String triggerEvent;

}
