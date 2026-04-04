package net.lab1024.sa.marketing.ledger.physical.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资产域-实物发货物流表 实体类
 *
 * @Author weolwo
 * @Date 2026-04-04 16:12:19
 * @Copyright weolwo
 */

@Data
@TableName("t_physical_delivery")
public class PhysicalDelivery {

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
     * 来源发奖提案ID
     */
    private Long proposalId;

    /**
     * 触发此发货的奖品配置ID (t_prize_config.id)
     */
    private Long prizeConfigId;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 收件详细地址
     */
    private String receiverAddress;

    /**
     * 【字典】物流公司：SF, JD, YTO
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 【字典】状态：0-待发货, 1-已发货, 2-已签收, 3-异常退回
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
