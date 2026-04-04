package net.lab1024.sa.marketing.task.record.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 运行时-会员任务进度实例表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 17:03:33
 * @Copyright weolwo
 */

@Data
public class TaskRecordVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "关联的任务配置ID")
    private Long taskConfigId;

    @Schema(description = "业务期数标识(防重用)：NONE, 日期(20260402)")
    private String periodKey;

    @Schema(description = "该实例生效开始时间")
    private LocalDateTime validStartTime;

    @Schema(description = "该实例失效过期时间")
    private LocalDateTime validEndTime;

    @Schema(description = "当前进度值：如已签到 3.0000 天")
    private BigDecimal currentMetric;

    @Schema(description = "【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期")
    private Integer status;

    @Schema(description = "进度详情快照")
    private String progressData;

    @Schema(description = "接取任务时的规则快照")
    private String ruleSnapshot;

    @Schema(description = "接取任务时的奖励快照")
    private String prizeSnapshot;

    @Schema(description = "真正达标的时间")
    private LocalDateTime completeTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
