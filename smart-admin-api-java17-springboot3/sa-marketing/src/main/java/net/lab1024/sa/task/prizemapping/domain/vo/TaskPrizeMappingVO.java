package net.lab1024.sa.task.prizemapping.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 任务阶段与奖励映射表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-18 20:41:02
 * @Copyright weolwo
 */

@Data
public class TaskPrizeMappingVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "任务配置ID")
    private Long taskConfigId;

    @Schema(description = "阶段达标条件")
    private String stageCondition;

    @Schema(description = "任务阶段：单次任务填1，阶梯任务填 1, 2, 3...")
    private Integer stageLevel;

    @Schema(description = "奖励编码")
    private String prizeCode;

    @Schema(description = "计算类型：FIXED(固定), RATIO(比例), FORMULA(公式)")
    private String prizeMode;

    @Schema(description = "动态发奖策略")
    private String prizeStrategy;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
