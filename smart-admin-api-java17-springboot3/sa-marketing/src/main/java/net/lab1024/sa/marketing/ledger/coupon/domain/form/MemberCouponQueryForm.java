package net.lab1024.sa.marketing.ledger.coupon.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产域-会员优惠券实例表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:15:39
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberCouponQueryForm extends PageParam {

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "券模编码")
    private String couponCode;

    @Schema(description = "券名称")
    private String couponName;

    @Schema(description = "有效期开始")
    private LocalDate validStartTimeBegin;

    @Schema(description = "有效期开始")
    private LocalDate validStartTimeEnd;

    @Schema(description = "【字典】状态：0-未使用, 1-已使用, 2-已过期, 3-已作废")
    private Integer status;

}
