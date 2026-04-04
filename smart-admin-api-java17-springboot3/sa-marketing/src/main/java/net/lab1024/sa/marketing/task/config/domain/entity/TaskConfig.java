package net.lab1024.sa.marketing.task.config.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-任务规则配置表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */

@Data
@TableName("t_task_config")
public class TaskConfig {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 面向C端展示的任务名称
     */
    private String taskName;

    /**
     * 关联模板Code
     */
    private String templateCode;

    /**
     * 【字典】触发事件：ORDER_PAID(支付), MEMBER_REGISTER(注册), DAILY_SIGN(签到), PAGE_VIEW(浏览), CUSTOM(自定义)
     */
    private String triggerEvent;

    /**
     * 【字典】任务分组：NEWBIE(新手), DAILY(日常), PROMO(大促), VIP(会员专属)
     */
    private String taskGroup;

    /**
     * 【字典】目标人群：ALL(全部), NEW_MEMBER(新会员), OLD_MEMBER(老会员)
     */
    private String targetAudience;

    /**
     * 【字典】参与频次：ONCE(终身一次), DAILY(每日重复), WEEKLY(每周重复), UNLIMITED(无限制)
     */
    private String limitType;

    /**
     * 配合频次类型，限制次数
     */
    private Integer limitCount;

    /**
     * 业务规则参数 JSON
     */
    private String ruleConfig;

    /**
     * 前端排序权重，越大越靠前
     */
    private Integer sortWeight;

    /**
     * 前端【去完成】跳转路由
     */
    private String actionUrl;

    /**
     * 前端展示UI补充(图标/角标等)
     */
    private String uiConfig;

    /**
     * 【字典】任务状态：0-草稿, 1-待生效(未到时间), 2-生效中, 3-已下线
     */
    private Integer status;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

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
