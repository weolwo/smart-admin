package net.lab1024.sa.marketing.task.config.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 业务级-任务规则配置表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */

@Data
public class TaskConfigAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "面向C端展示的任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "面向C端展示的任务名称 不能为空")
    private String taskName;

    @Schema(description = "关联模板Code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联模板Code 不能为空")
    private String templateCode;

    @Schema(description = "【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义) 不能为空")
    private String triggerEvent;

    @Schema(description = "【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)")
    private String taskGroup;

    @Schema(description = "【字典】目标人群：ALL(全部), NEW_MEMBER(新会员), OLD_MEMBER(老会员)")
    private String targetAudience;

    @Schema(description = "【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制) 不能为空")
    private String limitType;

    @Schema(description = "配合频次类型，限制次数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "配合频次类型，限制次数 不能为空")
    private Integer limitCount;

    @Schema(description = "业务规则参数 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "业务规则参数 JSON 不能为空")
    private String ruleConfig;

    @Schema(description = "前端排序权重，越大越靠前", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "前端排序权重，越大越靠前 不能为空")
    private Integer sortWeight;

    @Schema(description = "【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线 不能为空")
    private Integer status;

}