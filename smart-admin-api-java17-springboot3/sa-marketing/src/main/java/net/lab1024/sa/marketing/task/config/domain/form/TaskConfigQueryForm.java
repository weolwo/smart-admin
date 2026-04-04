package net.lab1024.sa.marketing.task.config.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务级-任务规则配置表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskConfigQueryForm extends PageParam {

    @Schema(description = "面向C端展示的任务名称")
    private String taskName;

    @Schema(description = "关联模板Code")
    private String templateCode;

    @Schema(description = "【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)")
    private String taskGroup;

    @Schema(description = "【字典】目标人群：ALL(全部), NEWMEMBER(新会员), OLDMEMBER(老会员)")
    private String targetAudience;

    @Schema(description = "任务开始时间")
    private LocalDate startTimeBegin;

    @Schema(description = "任务开始时间")
    private LocalDate startTimeEnd;

}
