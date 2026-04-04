package net.lab1024.sa.marketing.ledger.physical.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产域-实物发货物流表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-04 16:12:19
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PhysicalDeliveryQueryForm extends PageParam {

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "来源发奖提案ID")
    private Long proposalId;

    @Schema(description = "【字典】状态：0-待发货, 1-已发货, 2-已签收, 3-异常退回")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

    @Schema(description = "物流单号")
    private String logisticsNo;

}
