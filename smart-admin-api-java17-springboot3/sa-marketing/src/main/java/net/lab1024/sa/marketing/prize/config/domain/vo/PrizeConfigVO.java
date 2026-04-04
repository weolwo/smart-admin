package net.lab1024.sa.marketing.prize.config.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-发奖规则与奖品明细表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */

@Data
public class PrizeConfigVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "关联的上层奖励包ID (t_prize_group.id)")
    private String prizeGroupId;

    @Schema(description = "绑定的底层优惠兜底配置ID (指向 t_promotion_config)")
    private String promotionConfigId;

    @Schema(description = "【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL")
    private String prizeType;

    @Schema(description = "奖品展示名称：如“双11特等奖”或“100积分”")
    private String prizeName;

    @Schema(description = "【字典】发奖机制：FIXED, RANDOM, PROBABILITY")
    private String grantMode;

    @Schema(description = "奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)")
    private Integer prizeLevel;

    @Schema(description = "发放数量下限(固定发放时与上限一致)")
    private BigDecimal prizeAmountMin;

    @Schema(description = "发放数量上限")
    private BigDecimal prizeAmountMax;

    @Schema(description = "中奖概率(万分位)")
    private BigDecimal probability;

    @Schema(description = "前端展示排序权重")
    private Integer sortWeight;

    @Schema(description = "【字典】状态：0-停用, 1-启用")
    private Integer status;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
