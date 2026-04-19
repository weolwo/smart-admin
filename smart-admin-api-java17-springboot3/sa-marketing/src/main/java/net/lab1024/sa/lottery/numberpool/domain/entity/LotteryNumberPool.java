package net.lab1024.sa.lottery.numberpool.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 彩票号码池 实体类
 *
 * @Author weolwo
 * @Date 2026-04-19 11:31:09
 * @Copyright weolwo
 */

@Data
@TableName("t_lottery_number_pool")
public class LotteryNumberPool {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 彩票编码
     */
    private String lotteryCode;

    /**
     * 彩票号码
     */
    private String ticketNumber;

    /**
     * 发号序列号
     */
    private Integer sequenceNo;

}
