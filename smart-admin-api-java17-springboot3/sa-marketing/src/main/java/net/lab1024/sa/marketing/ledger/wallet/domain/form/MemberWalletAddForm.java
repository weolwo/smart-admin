package net.lab1024.sa.marketing.ledger.wallet.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 账务域-会员资金/积分主账表 新建表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:17:33
 * @Copyright weolwo
 */

@Data
public class MemberWalletAddForm {

    @Schema(description = "if", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "if 不能为空")
    private Long id;

    @Schema(description = "会员名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会员名 不能为空")
    private String memberName;

    @Schema(description = "积分余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分余额 不能为空")
    private BigDecimal scoreBalance;

    @Schema(description = "现金余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "现金余额 不能为空")
    private BigDecimal cashBalance;

    @Schema(description = "【字典】状态：0-冻结, 1-正常", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "【字典】状态：0-冻结, 1-正常 不能为空")
    private Integer status;

    @Schema(description = "乐观锁版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本号 不能为空")
    private Integer version;

}