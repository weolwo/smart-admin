package net.lab1024.sa.marketing.prize.log.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-奖励发放执行明细与快照表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 18:42:42
 * @Copyright weolwo
 */

@Data
@TableName("t_prize_log")
public class PrizeLog {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的提案ID (t_promotion_proposal.id)
     */
    private Long proposalId;

    /**
     * 会员名
     */
    private String memberName;

    /**
     * 关联的大礼包ID
     */
    private String prizeGroupId;

    /**
     * 触发此发放的具体奖品明细项ID (t_prize_config.id)
     */
    private Long prizeConfigId;

    /**
     * 实际扣减的兜底优惠池ID
     */
    private String promotionConfigId;

    /**
     * 奖品级别：1(一等奖), 0(无级别)
     */
    private Integer prizeLevel;

    /**
     * 奖品名称快照
     */
    private String prizeName;

    /**
     * 【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL
     */
    private String prizeType;

    /**
     * 发放的具体值(积分数/券ID)
     */
    private String prizeValue;

    /**
     * 【字典】执行状态：0-发放中, 1-成功, 2-失败
     */
    private Integer status;

    /**
     * 下游外部单号
     */
    private String externalBizNo;

    /**
     * 异常原因
     */
    private String remark;

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
