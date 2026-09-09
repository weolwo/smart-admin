package solvela.app.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 重置密码的结果。
 *
 * @param revokedSessions 被吊销的会话数。<b>要展示给用户</b> ——
 *                        「已在 3 台设备上退出登录」是他判断「刚才是不是别人在动我账号」的依据。
 *                        只回一句「修改成功」，这条信息就白丢了
 */
@Schema(description = "重置密码结果")
public record PasswordResetView(
        @Schema(description = "已退出登录的设备数") int revokedSessions) {
}
