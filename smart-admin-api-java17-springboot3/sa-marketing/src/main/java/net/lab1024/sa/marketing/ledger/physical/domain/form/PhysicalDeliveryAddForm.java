package net.lab1024.sa.marketing.ledger.physical.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产域-实物发货物流表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-04 16:12:19
 * @Copyright weolwo
 */

@Data
public class PhysicalDeliveryAddForm {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id 不能为空")
    private Long id;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "来源发奖提案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源发奖提案ID 不能为空")
    private Long proposalId;

    @Schema(description = "触发此发货的奖品配置ID (t_prize_config.id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "触发此发货的奖品配置ID (t_prize_config.id) 不能为空")
    private Long prizeConfigId;

    @Schema(description = "收件人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收件人姓名 不能为空")
    private String receiverName;

    @Schema(description = "收件人电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收件人电话 不能为空")
    private String receiverPhone;

    @Schema(description = "收件详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收件详细地址 不能为空")
    private String receiverAddress;

    @Schema(description = "【字典】状态：0-待发货, 1-已发货, 2-已签收, 3-异常退回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-待发货, 1-已发货, 2-已签收, 3-异常退回 不能为空")
    private Integer status;

}