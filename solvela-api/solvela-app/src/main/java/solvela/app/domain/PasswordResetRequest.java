package solvela.app.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 用邮箱验证码重置密码。
 *
 * <p>⚠️ 这是<b>匿名</b>接口 —— 用户正是因为进不去才走这条路。
 * 成功之后他在所有设备上的会话都会被吊销，包括当前这个（如果他还登着）。
 *
 * @param email       账号邮箱
 * @param code        发到该邮箱的验证码
 * @param newPassword 新密码。<b>强度规则不在这里</b>，在会员域的
 *                    {@code MemberPasswordPolicy}，只有那一处
 */
@Schema(description = "重置密码")
public record PasswordResetRequest(

        @Schema(description = "账号邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "请输入邮箱") String email,

        @Schema(description = "邮箱收到的验证码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "请输入验证码") String code,

        @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "请输入新密码") String newPassword) {
}
