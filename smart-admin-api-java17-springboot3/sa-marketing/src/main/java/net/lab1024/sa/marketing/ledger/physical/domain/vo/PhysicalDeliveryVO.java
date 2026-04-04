package net.lab1024.sa.marketing.ledger.physical.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-实物发货物流表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-04 16:12:19
 * @Copyright weolwo
 */

@Data
public class PhysicalDeliveryVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "来源发奖提案ID")
    private Long proposalId;

    @Schema(description = "触发此发货的奖品配置ID (t_prize_config.id)")
    private Long prizeConfigId;

    @Schema(description = "收件人姓名")
    private String receiverName;

    @Schema(description = "收件人电话")
    private String receiverPhone;

    @Schema(description = "收件详细地址")
    private String receiverAddress;

    @Schema(description = "【字典】物流公司：SF, JD, YTO")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "【字典】状态：0-待发货, 1-已发货, 2-已签收, 3-异常退回")
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
