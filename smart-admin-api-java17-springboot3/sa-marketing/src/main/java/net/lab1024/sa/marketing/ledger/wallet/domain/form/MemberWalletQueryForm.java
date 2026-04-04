package net.lab1024.sa.marketing.ledger.wallet.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账务域-会员资金/积分主账表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 17:17:33
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberWalletQueryForm extends PageParam {

    @Schema(description = "会员名")
    private String memberName;

    @Schema(description = "【字典】状态：0-冻结, 1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建时间")
    private LocalDate createTimeEnd;

}
