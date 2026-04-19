package net.lab1024.sa.lottery.prizerule.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 彩票奖励配置 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-19 11:50:34
 * @Copyright weolwo
 */

@Data
public class LotteryPrizeRuleVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "彩票编码")
    private String lotteryCode;

    @Schema(description = "奖励等级")
    private Integer prizeLevel;

    @Schema(description = "如: 终极大奖")
    private String prizeName;

    @Schema(description = "开奖个数")
    private Integer winCount;

    @Schema(description = "奖品编码")
    private String prizeCode;

    @Schema(description = "奖励价值")
    private BigDecimal prizeValue;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
