package net.lab1024.sa.marketing.prize.mapping.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-任务阶段与奖励包映射表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 17:01:05
 * @Copyright weolwo
 */

@Data
@TableName("t_task_prize_mapping")
public class TaskPrizeMapping {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的任务配置ID (t_task_config.id)
     */
    private Long taskConfigId;

    /**
     * 任务阶段：单次任务填1，阶梯任务填 1, 2, 3...
     */
    private Integer stageLevel;

    /**
     * 达标阈值快照：如 "100.00" 或 "3"
     */
    private String stageThreshold;

    /**
     * 触发的奖励包ID (t_prize_group.id)
     */
    private String prizeGroupId;

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
