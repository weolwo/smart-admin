package net.lab1024.sa.marketing.prize.mapping.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-任务阶段与奖励包映射表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 17:01:05
 * @Copyright weolwo
 */

@Data
public class TaskPrizeMappingVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "关联的任务配置ID (t_task_config.id)")
    private Long taskConfigId;

    @Schema(description = "任务阶段：单次任务填1，阶梯任务填 1, 2, 3...")
    private Integer stageLevel;

    @Schema(description = "达标阈值快照：如 100.00 或 3")
    private String stageThreshold;

    @Schema(description = "触发的奖励包ID (t_prize_group.id)")
    private String prizeGroupId;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
