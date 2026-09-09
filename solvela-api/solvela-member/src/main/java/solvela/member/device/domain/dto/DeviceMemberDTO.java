package solvela.member.device.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 「这台设备碰过哪个账号」的一行。
 *
 * <h3>🔴 这是整套设备方案最终要产出的那张表</h3>
 * 方案里那句「有了 device_id，『一台设备碰过哪些账号』才查得出来，
 * 而那正是将来判断『要不要花钱买厂商指纹』的唯一依据」——
 * 说的就是这个查询。在它之前，device_id 只是躺在库里的一列。
 *
 * <p>数据来自 {@code t_member_login_log}，不是 {@code t_device}：
 * 设备表刻意<b>不带 member_id</b>（一台设备本来就可能有多个账号，
 * 放一个 member_id 只能记住最后一个），关联关系天然长在登录日志里。
 */
@Data
public class DeviceMemberDTO {

    private Long memberId;

    private String memberName;

    private String nickname;

    /** 这台设备上该账号的登录次数（含失败） */
    private Integer loginCount;

    private LocalDateTime firstLoginTime;

    private LocalDateTime lastLoginTime;
}
