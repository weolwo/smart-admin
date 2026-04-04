package net.lab1024.sa.marketing.ledger.coupon.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-会员优惠券实例表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 17:15:39
 * @Copyright weolwo
 */

@Data
public class MemberCouponVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "券模编码")
    private String couponCode;

    @Schema(description = "券名称")
    private String couponName;

    @Schema(description = "【字典】状态：0-未使用, 1-已使用, 2-已过期, 3-已作废")
    private Integer status;

    @Schema(description = "【字典】获取来源：TASK_PRIZE, MANUAL_SEND")
    private String sourceType;

    @Schema(description = "有效期开始")
    private LocalDateTime validStartTime;

    @Schema(description = "有效期结束")
    private LocalDateTime validEndTime;

    @Schema(description = "核销时间")
    private LocalDateTime usedTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
