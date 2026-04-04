package net.lab1024.sa.marketing.prize.group.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 业务级-奖励包(大礼包)外壳表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */

@Data
public class PrizeGroupAddForm {

    @Schema(description = "奖励包ID，如 GRP_1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖励包ID，如 GRP_1001 不能为空")
    private String id;

    @Schema(description = "奖励包名称，如：新人注册大礼包", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖励包名称，如：新人注册大礼包 不能为空")
    private String groupName;

    @Schema(description = "给前端展示的文案/描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "给前端展示的文案/描述 不能为空")
    private String description;

    @Schema(description = "【字典】状态：0-停用, 1-启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-停用, 1-启用 不能为空")
    private Integer status;

}