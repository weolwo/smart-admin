package net.lab1024.sa.marketing.prize.group.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务级-奖励包(大礼包)外壳表 分页查询表单
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class PrizeGroupQueryForm extends PageParam {

    @Schema(description = "奖励包名称，如：新人注册大礼包")
    private String groupName;

}
