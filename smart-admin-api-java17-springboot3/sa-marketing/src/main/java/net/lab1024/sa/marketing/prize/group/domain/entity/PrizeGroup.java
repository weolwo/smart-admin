package net.lab1024.sa.marketing.prize.group.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-奖励包(大礼包)外壳表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */

@Data
@TableName("t_prize_group")
public class PrizeGroup {

    /**
     * 奖励包ID，如 GRP_1001
     */
    @TableId
    private String id;

    /**
     * 奖励包名称，如：新人注册大礼包
     */
    private String groupName;

    /**
     * 给前端展示的文案/描述
     */
    private String description;

    /**
     * 【字典】状态：0-停用, 1-启用
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
