package net.lab1024.sa.lottery.config.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 彩票配置 实体类
 *
 * @Author weolwo
 * @Date 2026-04-19 11:16:39
 * @Copyright weolwo
 */

@Data
@TableName("t_lottery_config")
public class LotteryConfig {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 活动编码
     */
    private String activityCode;

    /**
     * 彩票编码
     */
    private String lotteryCode;

    /**
     * 彩票名称
     */
    private String lotteryName;

    /**
     * 字符集：0-9, A-Z
     */
    private String numberCharset;

    /**
     * 号码长度
     */
    private Integer numberLength;

    /**
     * 号池总数 (如: 1,000,000)
     */
    private Integer totalCount;

    /**
     * 入场成本类型 (SCORE, FREE)
     */
    private String costAssetType;

    /**
     * 入场成本-数值
     */
    private BigDecimal costValue;

    /**
     * 状态：0-下线, 1-上线
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
