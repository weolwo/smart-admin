package net.lab1024.sa.marketing.ledger.coupon.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产域-会员优惠券实例表 更新表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:15:39
 * @Copyright weolwo
 */

@Data
public class MemberCouponUpdateForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

}