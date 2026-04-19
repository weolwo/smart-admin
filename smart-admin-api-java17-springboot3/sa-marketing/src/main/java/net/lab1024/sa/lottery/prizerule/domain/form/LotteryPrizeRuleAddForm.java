package net.lab1024.sa.lottery.prizerule.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 彩票奖励配置 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-19 11:50:34
 * @Copyright weolwo
 */

@Data
public class LotteryPrizeRuleAddForm {

    @Schema(description = "租户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "租户id 不能为空")
    private String tenantId;

    @Schema(description = "彩票编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "彩票编码 不能为空")
    private String lotteryCode;

    @Schema(description = "奖励等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "奖励等级 不能为空")
    private Integer prizeLevel;

    @Schema(description = "如: 终极大奖", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "如: 终极大奖 不能为空")
    private String prizeName;

    @Schema(description = "开奖个数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开奖个数 不能为空")
    private Integer winCount;

    @Schema(description = "奖品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "奖品编码 不能为空")
    private String prizeCode;

    @Schema(description = "奖励价值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "奖励价值 不能为空")
    private BigDecimal prizeValue;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}