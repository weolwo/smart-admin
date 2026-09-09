package solvela.member.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solvela.auth.member.MemberTokenStore;
import solvela.crypto.PasswordCipher;
import solvela.crypto.PiiHasher;
import solvela.enums.MemberStatusEnum;
import solvela.member.Member;
import solvela.member.api.EmailCodeScene;
import solvela.member.api.EmailCodeVerifyResult;
import solvela.member.api.MemberPasswordResetCmd;
import solvela.member.api.MemberPasswordResetResult;
import solvela.member.api.MemberPasswordPolicy;
import solvela.member.api.PasswordResetFailReason;
import solvela.member.auth.MemberAuthDao;
import solvela.member.device.DeviceGuard;
import solvela.member.util.MemberEmailUtil;

/**
 * 用邮箱验证码重置密码。
 *
 * <h3>🔴 成功之后必须吊销全部会话</h3>
 * 用户点「忘记密码」的<b>最常见原因之一</b>就是「我怀疑号被人动过」。
 * 只改密码不吊销会话，攻击者手里那个 30 天有效期的令牌<b>照样能用</b> ——
 * 而用户以为自己已经把人赶出去了。这比不改密码更糟：他不会再采取任何行动。
 *
 * <p>吊销放在<b>事务内</b>：失败就整个回滚，让用户重来。反过来（先提交再吊销）
 * 一旦吊销失败，就回到了「密码改了但旧会话还在」——正是这里要消灭的状态。
 * 判据与 {@code MemberService.updateStatus} 的冻结吊销完全一致。
 *
 * <h3>冻结的账号不许自助找回</h3>
 * 允许的话，风控封掉一个刷子账号之后，他改个密码就能继续用 ——
 * 而冻结的本意正是让他用不了。
 *
 * @Date 2026-09-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPasswordResetService {

    private final MemberAuthDao memberAuthDao;

    private final MemberEmailCodeService emailCodeService;

    private final MemberTokenStore tokenStore;

    private final DeviceGuard deviceGuard;

    private final PiiHasher piiHasher;

    /**
     * 重置密码。
     *
     * <p>顺序：邮箱格式 → 验证码 → 新密码强度 → 找人 → 账号状态 → 改密 → 吊销会话。
     *
     * <p>🔴 <b>验证码排在找人之前</b>，而且查无此人时也要走完验证码那一步 ——
     * 否则「有账号」和「没账号」输错码得到的回答就不一样了，发码那一步
     * 靠 {@code MailDelivery.SUPPRESS} 藏住的东西会在这里漏光。
     * 与 {@code MemberAuthService.authenticate} 的邮箱验证码登录同一个处理。
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberPasswordResetResult reset(MemberPasswordResetCmd cmd) {
        String email = MemberEmailUtil.normalize(cmd.email());
        if (email == null) {
            return MemberPasswordResetResult.fail(PasswordResetFailReason.BAD_EMAIL_FORMAT);
        }

        EmailCodeVerifyResult codeResult =
                emailCodeService.verify(EmailCodeScene.RESET_PASSWORD, email, cmd.code());
        if (codeResult != EmailCodeVerifyResult.OK) {
            // 验证码错也算一次设备失败：「一台机器在挨个试不同账号的重置码」
            // 是很强的信号，而它只有在这里记得下来
            deviceGuard.recordLoginFailure(cmd.deviceId());
            return MemberPasswordResetResult.fail(switch (codeResult) {
                case NOT_FOUND -> PasswordResetFailReason.EMAIL_CODE_EXPIRED;
                case MISMATCH -> PasswordResetFailReason.EMAIL_CODE_MISMATCH;
                case TOO_MANY_ATTEMPTS -> PasswordResetFailReason.EMAIL_CODE_LOCKED;
                case OK -> throw new IllegalStateException("不可能走到：OK 已在上面判掉");
            });
        }

        // 强度校验排在验证码【之后】：它不查存储也不泄露信息，本可以更靠前，
        // 但放在这里能让「码对了才告诉你密码太弱」—— 而反过来的话，
        // 一个不知道码的人可以拿这个接口反复试探密码规则
        if (!MemberPasswordPolicy.isValid(cmd.newPassword())) {
            return MemberPasswordResetResult.fail(PasswordResetFailReason.WEAK_PASSWORD);
        }

        Member member = memberAuthDao.selectForLoginByEmail(piiHasher.hash(email));
        if (member == null) {
            // 走到这里说明他猜中了一个六位数（百万分之一，只有 5 次机会）——
            // 不构成可用的枚举手段，见 PasswordResetFailReason.ACCOUNT_NOT_FOUND
            return MemberPasswordResetResult.fail(PasswordResetFailReason.ACCOUNT_NOT_FOUND);
        }
        if (member.getStatus() != MemberStatusEnum.NORMAL) {
            log.info("【重置密码】账号状态不允许自助找回, memberId: {}, status: {}",
                    member.getMemberId(), member.getStatus());
            return MemberPasswordResetResult.fail(PasswordResetFailReason.ACCOUNT_UNAVAILABLE);
        }

        memberAuthDao.updatePassword(member.getMemberId(), PasswordCipher.encode(cmd.newPassword()));

        // 🔴 吊销全部会话，事务内。理由见类注释 —— 不吊销的话，攻击者手里那个
        //    30 天有效期的令牌照样能用，而用户以为自己已经把人赶出去了
        int revoked = tokenStore.revokeAll(member.getMemberId());

        log.info("【重置密码】成功, memberId: {}, 吊销会话 {} 个, email: {}",
                member.getMemberId(), revoked, MemberEmailUtil.mask(email));
        return MemberPasswordResetResult.ok(revoked);
    }
}
