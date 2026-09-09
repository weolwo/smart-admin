package solvela.admin.module.member.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import solvela.enums.DeviceStatusEnum;

/**
 * 人工处置一台设备。
 *
 * <h3>🔴 刻意没有 operator 字段</h3>
 * 操作人由服务端从当前登录员工取，<b>不收前端传的</b>。收了就等于「谁都能署别人的名」，
 * 而 {@code t_device.operator} 这一列存在的唯一理由就是事后追得到人。
 * 判据与 {@code MemberOperationLimitController.unlock} 完全一样。
 */
@Data
@Schema(description = "人工处置设备")
public class DeviceDisposeForm {

    @Schema(description = "设备号（32 位 hex）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请指定设备号")
    private String deviceId;

    @Schema(description = "目标档位：NORMAL-正常 / OBSERVE-观察（登录要多验一道码） / BANNED-封禁",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定目标档位")
    private DeviceStatusEnum status;

    /**
     * 处置原因。
     *
     * <p>必填，而且是<b>刻意</b>的：封禁一台设备是有后果的决定，三个月后回头看
     * 「为什么这台被封了」，一句 remark 就是全部答案。让它可空的话，
     * 绝大多数记录都会是空的 —— 那时这一列和不存在没有区别。
     */
    @Schema(description = "处置原因，必填", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请填写处置原因")
    private String remark;
}
