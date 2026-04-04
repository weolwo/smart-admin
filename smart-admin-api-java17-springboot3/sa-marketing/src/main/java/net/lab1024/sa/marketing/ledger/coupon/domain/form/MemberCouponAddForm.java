package net.lab1024.sa.marketing.ledger.coupon.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-会员优惠券实例表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:15:39
 * @Copyright weolwo
 */

@Data
public class MemberCouponAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "券模编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "券模编码 不能为空")
    private String couponCode;

    @Schema(description = "券名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "券名称 不能为空")
    private String couponName;

    @Schema(description = "【字典】状态：0-未使用, 1-已使用, 2-已过期, 3-已作废", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-未使用, 1-已使用, 2-已过期, 3-已作废 不能为空")
    private Integer status;

    @Schema(description = "【字典】获取来源：TASK_PRIZE, MANUAL_SEND", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "【字典】获取来源：TASK_PRIZE, MANUAL_SEND 不能为空")
    private String sourceType;

    @Schema(description = "有效期开始", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "有效期开始 不能为空")
    private LocalDateTime validStartTime;

    @Schema(description = "有效期结束", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "有效期结束 不能为空")
    private LocalDateTime validEndTime;

}