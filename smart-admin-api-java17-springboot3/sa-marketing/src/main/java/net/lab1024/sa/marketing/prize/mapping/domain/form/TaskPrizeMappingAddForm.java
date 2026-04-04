package net.lab1024.sa.marketing.prize.mapping.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 业务级-任务阶段与奖励包映射表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:01:05
 * @Copyright weolwo
 */

@Data
public class TaskPrizeMappingAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "关联的任务配置ID (t_task_config.id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联的任务配置ID (t_task_config.id) 不能为空")
    private Long taskConfigId;

    @Schema(description = "任务阶段：单次任务填1，阶梯任务填 1, 2, 3...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "任务阶段：单次任务填1，阶梯任务填 1, 2, 3... 不能为空")
    private Integer stageLevel;

    @Schema(description = "达标阈值快照：如 100.00 或 3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "达标阈值快照：如 100.00 或 3 不能为空")
    private String stageThreshold;

    @Schema(description = "触发的奖励包ID (t_prize_group.id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "触发的奖励包ID (t_prize_group.id) 不能为空")
    private String prizeGroupId;

}