package solvela.member.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import solvela.base.domain.SystemEnvironment;
import solvela.base.mail.MailService;
import solvela.base.mail.MailTemplateCodeEnum;
import solvela.crypto.PiiHasher;
import solvela.member.api.EmailCodeFailReason;
import solvela.member.api.EmailCodeScene;
import solvela.member.api.EmailCodeSendResult;
import solvela.member.api.EmailCodeVerifyResult;
import solvela.member.api.MailDelivery;
import solvela.member.code.CodeIssueOutcome;
import solvela.member.code.CodeVerifyOutcome;
import solvela.member.code.VerificationCodeProperties;
import solvela.member.code.VerificationCodeStore;
import solvela.member.util.MemberEmailUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮箱验证码：<b>只管邮箱那一半</b>。
 *
 * <h3>与 {@link VerificationCodeStore} 的分工</h3>
 * 生成、冷却、日限、验错计数、消费 —— 那些<b>安全攸关且与通道无关</b>的性质
 * 全在 store 里，邮箱和短信共用一份。本类只负责三件邮箱自己的事：
 * <b>规范化地址</b>、<b>选模板发信</b>、<b>把结果翻译成邮箱那套原因枚举</b>。
 *
 * <p>🔴 照抄一份给短信是另一条路，代价是那五条性质各有两份 ——
 * 而漂移的表现是「邮箱那边的失败次数上限生效、短信那边悄悄没有」，没有任何报错。
 *
 * <h3>职责边界：不管业务</h3>
 * 「这个邮箱能不能注册」「有没有对应的会员」不在这里 —— 那是
 * {@link MemberEmailCodeIssuer} 的事。而且那些判断的结果<b>绝不能反映在返回值里</b>：
 * 「已注册 / 未注册」两种回答不同，发码接口就成了账号枚举器。
 *
 * @Date 2026-09-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberEmailCodeService {

    /** 进 Redis key 的通道标识。两条通道的码互不相干。 */
    private static final String CHANNEL = "email";

    private final VerificationCodeStore codeStore;

    private final MailService mailService;

    private final PiiHasher piiHasher;

    private final VerificationCodeProperties properties;

    private final SystemEnvironment systemEnvironment;

    /**
     * 🔴 生产环境不许用 LOG 通道，<b>启动即失败</b>。
     *
     * <p>把验证码打进日志，等于把「接管任意账号」的能力交给每一个能看日志的人 ——
     * 而日志的访问面通常比数据库宽得多，还会被采集到 ELK、被转发、被长期保留。
     *
     * <p>为什么是失败而不是静默降级：降级的话，有人在生产配了 LOG 却什么都没发生，
     * 他会以为这个开关不生效、转头去别处找原因 —— 而真正的问题
     * （生产配置文件里躺着一个危险开关）没有任何人知道。
     */
    @jakarta.annotation.PostConstruct
    void checkTransport() {
        if (properties.getEmailTransport() != VerificationCodeProperties.Transport.LOG) {
            return;
        }
        if (systemEnvironment.isProd()) {
            throw new IllegalStateException(
                    "solvela.member.code.email-transport=LOG 不允许在生产环境使用："
                            + "它会把每个人的验证码打进日志，而看得到日志的人就能接管任意账号。"
                            + "生产请配成 REAL，并配好 spring.mail.*。");
        }
        log.warn("【邮箱验证码】当前是 LOG 通道：不发信，验证码直接打进日志。"
                        + "搜关键字【邮箱验证码-LOG】。当前环境 {}。"
                        + "🔴 这个开关只允许在非生产环境使用，配到生产会启动失败。",
                systemEnvironment.getCurrentEnvironment());
    }

    /** 发一封验证码邮件。 */
    public EmailCodeSendResult send(EmailCodeScene scene, String rawEmail, String clientIp) {
        return send(scene, rawEmail, clientIp, MailDelivery.DELIVER);
    }

    /**
     * 发一封验证码邮件，可以指定<b>不真的寄出去</b>。
     *
     * <p>{@link MailDelivery#SUPPRESS} 用于「这个邮箱与场景不匹配」的情形：
     * 照常存码、照常计入限频，只是不寄信。这样两条路径的<b>后续行为逐字相同</b>，
     * 否则校验那一步会把发送这一步藏住的东西漏出去。
     *
     * <p>分支顺序：<b>格式 → 冷却 → 日限 → 发信</b>。格式校验排最前面，
     * 因为它不查任何存储、不泄露任何信息 —— 让一个手滑打错格式的用户去消耗当天配额没有道理。
     */
    public EmailCodeSendResult send(EmailCodeScene scene, String rawEmail, String clientIp,
                                    MailDelivery delivery) {
        String email = MemberEmailUtil.normalize(rawEmail);
        if (email == null) {
            return EmailCodeSendResult.fail(EmailCodeFailReason.BAD_EMAIL_FORMAT);
        }
        String hash = piiHasher.hash(email);

        CodeIssueOutcome issued = codeStore.issue(CHANNEL, scene.name(), hash, clientIp);
        if (!issued.ok()) {
            return switch (issued.status()) {
                case TOO_FREQUENT -> EmailCodeSendResult.tooFrequent(issued.retryAfterSeconds());
                case DAILY_LIMIT -> EmailCodeSendResult.dailyLimit(issued.retryAfterSeconds());
                case OK -> throw new IllegalStateException("不可能走到：OK 已在上面判掉");
            };
        }

        if (delivery == MailDelivery.SUPPRESS) {
            log.info("【邮箱验证码】场景与账号状态不匹配，已静默计入但不发信, scene: {}, email: {}",
                    scene, MemberEmailUtil.mask(email));
            return EmailCodeSendResult.ok();
        }

        if (properties.getEmailTransport() == VerificationCodeProperties.Transport.LOG) {
            // 🔴 这里打【完整的邮箱和完整的码】—— 那就是这个通道的全部用途，
            //    打码就没法用了。生产环境走不到这里（checkTransport 已经拦下）
            log.warn("【邮箱验证码-LOG】scene={}, email={}, code={}, 有效期 {} 分钟",
                    scene, email, issued.code(), properties.ttl().toMinutes());
            return EmailCodeSendResult.ok();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("code", issued.code());
            params.put("minutes", properties.ttl().toMinutes());
            mailService.sendMail(templateOf(scene), params, Collections.singletonList(email));
        } catch (Exception e) {
            // 🔴 把刚存的码删掉：留着它会让冷却生效，于是用户在收不到信的同时
            //    还被告知「请稍后再试」
            codeStore.discard(CHANNEL, scene.name(), hash);
            log.error("【邮箱验证码】发送失败, scene: {}, email: {}", scene, MemberEmailUtil.mask(email), e);
            return EmailCodeSendResult.fail(EmailCodeFailReason.SEND_FAILED);
        }

        log.info("【邮箱验证码】已发送, scene: {}, email: {}", scene, MemberEmailUtil.mask(email));
        return EmailCodeSendResult.ok();
    }

    /** 校验并<b>消费</b>验证码。通过之后同一个码不能再用第二次。 */
    public EmailCodeVerifyResult verify(EmailCodeScene scene, String rawEmail, String inputCode) {
        String email = MemberEmailUtil.normalize(rawEmail);
        if (email == null) {
            return EmailCodeVerifyResult.NOT_FOUND;
        }
        return toEmailResult(codeStore.verify(CHANNEL, scene.name(), piiHasher.hash(email), inputCode));
    }

    private static EmailCodeVerifyResult toEmailResult(CodeVerifyOutcome outcome) {
        return switch (outcome) {
            case OK -> EmailCodeVerifyResult.OK;
            case NOT_FOUND -> EmailCodeVerifyResult.NOT_FOUND;
            case MISMATCH -> EmailCodeVerifyResult.MISMATCH;
            case TOO_MANY_ATTEMPTS -> EmailCodeVerifyResult.TOO_MANY_ATTEMPTS;
        };
    }

    /**
     * 场景 → 邮件模板。
     *
     * <p>用 switch 表达式而不是把模板编码塞进 {@link EmailCodeScene}：
     * 那个枚举在 {@code solvela-member-api} 里，而契约模块<b>不该依赖 base-mail</b> ——
     * 它是给网关也要用的，而网关一个 base 模块都不许有。
     *
     * <p>新增场景时这里<b>编译不过</b>，而不是悄悄落进某个默认模板。
     */
    private static MailTemplateCodeEnum templateOf(EmailCodeScene scene) {
        return switch (scene) {
            case REGISTER -> MailTemplateCodeEnum.MEMBER_REGISTER_CODE;
            case LOGIN -> MailTemplateCodeEnum.MEMBER_LOGIN_CODE;
            case BIND -> MailTemplateCodeEnum.MEMBER_BIND_EMAIL_CODE;
            case RESET_PASSWORD -> MailTemplateCodeEnum.MEMBER_RESET_PASSWORD_CODE;
        };
    }
}
