package net.lab1024.sa.marketing.task.record.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运行时-会员任务进度实例表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:03:33
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskRecordQueryForm extends PageParam {

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "该实例生效开始时间")
    private LocalDate validStartTimeBegin;

    @Schema(description = "该实例生效开始时间")
    private LocalDate validStartTimeEnd;

    @Schema(description = "【字典】任务流转状态：0-进行中, 1-待发奖, 2-已发奖, 3-已过期")
    private Integer status;

}
