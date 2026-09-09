package solvela.app.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 绑定 / 更换手机号。
 *
 * <p>🔴 刻意没有 memberId：会员号从令牌解析，<b>不收客户端传的</b>。
 * 收了等于「说自己是谁就是谁」。判据与 {@link EmailBindRequest} 一样。
 */
@Schema(description = "绑定/更换手机号")
public record PhoneBindRequest(

        @Schema(description = "新手机号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "请输入手机号") String phone,

        @Schema(description = "新手机号收到的验证码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "请输入验证码") String code,

        @Schema(description = "当前密码。更换手机号时与旧手机号验证码二选一")
        String currentPassword,

        @Schema(description = "旧手机号收到的验证码。更换时与当前密码二选一")
        String oldPhoneCode) {
}
