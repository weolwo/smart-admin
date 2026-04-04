package net.lab1024.sa.marketing.riskmanager.proposal.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-统一发奖提案控制表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */

@Data
@TableName("t_promotion_proposal")
public class PromotionProposal {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员名
     */
    private String memberName;

    /**
     * 来源任务实例ID
     */
    private Long taskRecordId;

    /**
     * 关联的优惠配置ID
     */
    private String promotionConfigId;

    /**
     * 【字典】状态：0-初始, 10-待一审, 11-待二审, 20-驳回, 30-待执行, 40-执行中, 50-成功, 60-部分成功, 70-彻底失败, 80-风控拦截
     */
    private Integer status;

    /**
     * 对应任务的哪个阶段
     */
    private Integer stageLevel;

    /**
     * 执行失败/风控拦截原因
     */
    private String remark;

    /**
     * 一审人
     */
    private String firstReviewer;

    /**
     * 一审时间
     */
    private LocalDateTime firstReviewTime;

    /**
     * 二审人
     */
    private String secondReviewer;

    /**
     * 二审时间
     */
    private LocalDateTime secondReviewTime;

    /**
     * 审核意见/驳回理由
     */
    private String reviewComment;

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
