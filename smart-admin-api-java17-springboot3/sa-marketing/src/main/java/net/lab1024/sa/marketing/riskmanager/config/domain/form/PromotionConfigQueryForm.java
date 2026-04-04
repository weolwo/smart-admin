package net.lab1024.sa.marketing.riskmanager.config.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产域-优惠配置(预算与风控兜底)表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PromotionConfigQueryForm extends PageParam {

    @Schema(description = "优惠配置名称，如：双11十元券兜底配置")
    private String promoName;

    @Schema(description = "【字典】资产类型：SCORE(积分), BALANCE(现金), COUPON(优惠券), PHYSICAL(实物)")
    private String prizeType;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

    @Schema(description = "【字典】状态：0-停用, 1-启用")
    private Integer status;

}
