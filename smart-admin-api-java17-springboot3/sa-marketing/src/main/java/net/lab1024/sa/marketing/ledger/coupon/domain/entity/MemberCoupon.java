package net.lab1024.sa.marketing.ledger.coupon.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-会员优惠券实例表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-03 17:15:39
 * @Copyright weolwo
 */

@Data
@TableName("t_member_coupon")
public class MemberCoupon {

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
     * 券模编码
     */
    private String couponCode;

    /**
     * 券名称
     */
    private String couponName;

    /**
     * 【字典】状态：0-未使用, 1-已使用, 2-已过期, 3-已作废
     */
    private Integer status;

    /**
     * 【字典】获取来源：TASK_PRIZE, MANUAL_SEND
     */
    private String sourceType;

    /**
     * 有效期开始
     */
    private LocalDateTime validStartTime;

    /**
     * 有效期结束
     */
    private LocalDateTime validEndTime;

    /**
     * 核销时间
     */
    private LocalDateTime usedTime;

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
