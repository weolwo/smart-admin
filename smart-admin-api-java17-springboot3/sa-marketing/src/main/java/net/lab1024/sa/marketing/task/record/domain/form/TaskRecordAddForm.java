package net.lab1024.sa.marketing.task.record.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 运行时-会员任务进度实例表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:03:33
 * @Copyright weolwo
 */

@Data
public class TaskRecordAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "关联的任务配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联的任务配置ID 不能为空")
    private Long taskConfigId;

    @Schema(description = "业务期数标识(防重用)：NONE, 日期(20260402)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "业务期数标识(防重用)：NONE, 日期(20260402) 不能为空")
    private String periodKey;

    @Schema(description = "该实例生效开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "该实例生效开始时间 不能为空")
    private LocalDateTime validStartTime;

    @Schema(description = "该实例失效过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "该实例失效过期时间 不能为空")
    private LocalDateTime validEndTime;

    @Schema(description = "当前进度值：如已签到 3.0000 天")
    private BigDecimal currentMetric;

    @Schema(description = "【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期")
    private Integer status;

    @Schema(description = "接取任务时的规则快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "接取任务时的规则快照 不能为空")
    private String ruleSnapshot;

    @Schema(description = "接取任务时的奖励快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "接取任务时的奖励快照 不能为空")
    private String prizeSnapshot;

}