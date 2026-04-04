package net.lab1024.sa.marketing.prize.group.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-奖励包(大礼包)外壳表 列表VO
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */

@Data
public class PrizeGroupVO {


    @Schema(description = "奖励包ID，如 GRP_1001")
    private String id;

    @Schema(description = "奖励包名称，如：新人注册大礼包")
    private String groupName;

    @Schema(description = "给前端展示的文案/描述")
    private String description;

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
