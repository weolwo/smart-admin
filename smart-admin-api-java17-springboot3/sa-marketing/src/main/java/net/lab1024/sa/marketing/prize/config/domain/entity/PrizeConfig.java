package net.lab1024.sa.marketing.prize.config.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 业务级-发奖规则与奖品明细表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */

@Data
@TableName("t_prize_config")
public class PrizeConfig {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的上层奖励包ID (t_prize_group.id)
     */
    private String prizeGroupId;

    /**
     * 绑定的底层优惠兜底配置ID (指向 t_promotion_config)
     */
    private String promotionConfigId;

    /**
     * 【字典】资产类型：SCORE, BALANCE, COUPON, PHYSICAL
     */
    private String prizeType;

    /**
     * 奖品展示名称：如“双11特等奖”或“100积分”
     */
    private String prizeName;

    /**
     * 【字典】发奖机制：FIXED, RANDOM, PROBABILITY
     */
    private String grantMode;

    /**
     * 奖品级别：1(一等奖), 2(二等奖), 0(普通奖品)
     */
    private Integer prizeLevel;

    /**
     * 发放数量下限(固定发放时与上限一致)
     */
    private BigDecimal prizeAmountMin;

    /**
     * 发放数量上限
     */
    private BigDecimal prizeAmountMax;

    /**
     * 中奖概率(万分位)
     */
    private BigDecimal probability;

    /**
     * 前端展示排序权重
     */
    private Integer sortWeight;

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
