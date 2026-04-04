package net.lab1024.sa.marketing.prize.config.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务级-发奖规则与奖品明细表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PrizeConfigQueryForm extends PageParam {

    @Schema(description = "关联的上层奖励包ID (tPrizeGroup.id)")
    private String prizeGroupId;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL")
    private String prizeType;

    @Schema(description = "奖品展示名称：如“双11特等奖”或“100积分”")
    private String prizeName;

    @Schema(description = "绑定的底层优惠兜底配置ID (指向 tPromotionConfig)")
    private String promotionConfigId;

    @Schema(description = "奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)")
    private Integer prizeLevel;

    @Schema(description = "【字典】状态：0-停用, 1-启用")
    private Integer status;

}
