package net.lab1024.sa.task.prizemapping.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务阶段与奖励映射表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-18 20:41:02
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskPrizeMappingQueryForm extends PageParam {

    @Schema(description = "任务配置ID")
    private Long taskConfigId;

    @Schema(description = "奖励编码")
    private String prizeCode;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

}
