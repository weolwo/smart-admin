package solvela.member.register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DuplicateKeyException;
import solvela.base.module.redis.RedisService;
import solvela.crypto.PiiCipher;
import solvela.crypto.PiiHasher;
import solvela.member.api.MemberRegisterCmd;
import solvela.member.api.MemberRegisterResult;
import solvela.member.api.RegisterFailReason;
import solvela.member.id.MemberIdAllocator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 会员注册：<b>顺序决定了能不能拿它当手机号探测器</b>。
 *
 * <h3>为什么限频必须排在查重前面</h3>
 * 反过来的话，「这个号注册过没有」可以无限次免费提问 —— {@code PHONE_TAKEN}
 * 这个枚举本身就是答案，它藏不掉。把限频提到查重之前，探测的成本才真的存在。
 *
 * <p>而格式与强度校验又排在限频<b>之前</b>：它们不查库、不泄露任何信息，
 * 让一个手滑打错格式的用户去消耗限频额度没有道理。
 *
 * <h3>撞唯一约束是预期结果，不是意外</h3>
 * 查重和插入之间有窗口，两个请求同时注册同一个号必然有一个撞唯一键。
 * 把它当异常抛出去就是 500 —— 而对用户这明明就是「已被注册」。
 *
 * <h3>密文与摘要必须来自同一个规范化后的字符串</h3>
 * 否则「解密出来的号」和「能登录的号」会是两个东西，而且要等到用户登不上才发现。
 *
 * @Author alaric
 * @Date 2026-09-06
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberRegisterServiceTest {

    private static final String PHONE = "13800000000";
    private static final String PHONE_HASH = "ABCDEF";
    private static final String STRONG_PASSWORD = "Passw0rd!";
    private static final String CLIENT_IP = "10.0.0.7";
    private static final long MEMBER_ID = 900001L;

    @Mock
    private MemberRegisterDao memberRegisterDao;
    @Mock
    private MemberIdAllocator memberIdAllocator;
    @Mock
    private RedisService redisService;
    @Mock
    private PiiHasher piiHasher;
    @Mock
    private PiiCipher piiCipher;

    private MemberRegisterProperties properties;
    private MemberRegisterService service;

    @BeforeEach
    void setUp() {
        properties = new MemberRegisterProperties();
        service = new MemberRegisterService(memberRegisterDao, memberIdAllocator, properties,
                redisService, piiHasher, piiCipher);

        when(piiHasher.hash(PHONE)).thenReturn(PHONE_HASH);
        when(piiCipher.encrypt(PHONE)).thenReturn("加密后的号");
        when(memberIdAllocator.nextMemberId()).thenReturn(MEMBER_ID);
        when(memberRegisterDao.countByPhoneHash(PHONE_HASH)).thenReturn(0);
        when(memberRegisterDao.insertMember(anyLong(), anyString(), anyString(), anyInt(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString())).thenReturn(1);
        when(redisService.generateRedisKey(anyString(), anyString())).thenReturn("k");
        // 默认放行：第 1 次尝试，上限 10
        when(redisService.increment(anyString(), anyLong())).thenReturn(1L);
    }

    // ------------------------------------------------------------------ 正常路径

    @Test
    @DisplayName("注册成功：会员号由分配器发、账号与昵称按会员号生成")
    void 注册成功() {
        MemberRegisterResult result = service.register(cmd(PHONE, STRONG_PASSWORD));

        assertTrue(result.success());
        assertEquals(MEMBER_ID, result.identity().memberId());
        assertTrue(result.identity().memberName().endsWith(String.valueOf(MEMBER_ID)));
    }

    @Test
    @DisplayName("🔴 密文与摘要来自同一个规范化后的号码")
    void 密文与摘要同源() {
        service.register(cmd("138 0000 0000", STRONG_PASSWORD));

        // 不同源的表现是「解密出来的号」和「能登录的号」不是一个，而且要等到用户登不上才发现
        verify(piiCipher).encrypt(PHONE);
        verify(piiHasher).hash(PHONE);
        verify(piiCipher, never()).encrypt("138 0000 0000");
    }

    @Test
    @DisplayName("注册来源缺省不为空：留空的话来源统计从第一天起就是错的")
    void 来源缺省() {
        service.register(new MemberRegisterCmd(PHONE, STRONG_PASSWORD, "H5", CLIENT_IP, null));

        verify(memberRegisterDao).insertMember(anyLong(), anyString(), anyString(), anyInt(),
                anyString(), anyString(), anyString(), anyInt(),
                org.mockito.ArgumentMatchers.argThat(s -> s != null && !s.isBlank()), anyString());
    }

    @Test
    @DisplayName("刻意不写登录日志：注册这件事已经完整记在 t_member 的三列上")
    void 注册不写登录日志() {
        service.register(cmd(PHONE, STRONG_PASSWORD));
        // 再写一条 LOGIN_SUCCESS 只会让登录轨迹里多一条语义不同的行，
        // 查「这个人什么时候登过」时反而要先把它剔掉
        verify(memberRegisterDao).insertMember(anyLong(), anyString(), anyString(), anyInt(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString());
    }

    // ------------------------------------------------------------------ 顺序

    @Test
    @DisplayName("🔴 顺序：格式 → 强度 → 限频 → 查重。限频必须在查重之前")
    void 校验顺序() {
        service.register(cmd(PHONE, STRONG_PASSWORD));

        // 查重在限频之前的话，「这个号注册过没有」可以无限次免费提问
        InOrder order = inOrder(redisService, memberRegisterDao);
        order.verify(redisService).increment(anyString(), anyLong());
        order.verify(memberRegisterDao).countByPhoneHash(PHONE_HASH);
    }

    @Test
    @DisplayName("格式不合法：不消耗限频额度，也不查库")
    void 格式不合法不消耗额度() {
        MemberRegisterResult result = service.register(cmd("1380000", STRONG_PASSWORD));

        assertEquals(RegisterFailReason.BAD_PHONE_FORMAT, result.reason());
        // 手滑打错格式不该占掉他今天的注册机会
        verify(redisService, never()).increment(anyString(), anyLong());
        verify(memberRegisterDao, never()).countByPhoneHash(anyString());
    }

    @Test
    @DisplayName("弱密码：同样排在限频之前，不查库")
    void 弱密码() {
        MemberRegisterResult result = service.register(cmd(PHONE, "123456"));

        assertEquals(RegisterFailReason.WEAK_PASSWORD, result.reason());
        verify(redisService, never()).increment(anyString(), anyLong());
    }

    // ------------------------------------------------------------------ 限频

    @Test
    @DisplayName("超过 IP 配额：告诉他还要等多久，且不查重不建号")
    void 超出IP配额() {
        when(redisService.increment(anyString(), anyLong()))
                .thenReturn((long) properties.getMaxAttemptsPerIp() + 1);
        when(redisService.getExpire(anyString())).thenReturn(600L);

        MemberRegisterResult result = service.register(cmd(PHONE, STRONG_PASSWORD));

        assertEquals(RegisterFailReason.TOO_MANY_ATTEMPTS, result.reason());
        assertEquals(600L, result.retryAfterSeconds(), "让用户点第二次才知道被限，是投诉的主要来源");
        verify(memberRegisterDao, never()).countByPhoneHash(anyString());
    }

    @Test
    @DisplayName("恰好用满配额那一次仍然放行（阈值是「超过」不是「达到」）")
    void 用满配额仍放行() {
        when(redisService.increment(anyString(), anyLong()))
                .thenReturn((long) properties.getMaxAttemptsPerIp());

        assertTrue(service.register(cmd(PHONE, STRONG_PASSWORD)).success());
    }

    @Test
    @DisplayName("限频键没有 TTL 时至少报 1 秒，不返回 0 或负数")
    void 等待秒数至少一秒() {
        when(redisService.increment(anyString(), anyLong()))
                .thenReturn((long) properties.getMaxAttemptsPerIp() + 1);
        // -1 = 键存在但没有过期时间，-2 = 键不存在
        when(redisService.getExpire(anyString())).thenReturn(-1L);

        // 返回 0 的话，前端的「x 秒后重试」会显示成「0 秒后重试」然后立刻又被拒
        assertEquals(1L, service.register(cmd(PHONE, STRONG_PASSWORD)).retryAfterSeconds());
    }

    @Test
    @DisplayName("🔴 拿不到客户端 IP 时放行，不是一律拒绝")
    void 没有IP时放行() {
        MemberRegisterResult result =
                service.register(new MemberRegisterCmd(PHONE, STRONG_PASSWORD, "H5", null, "APP"));

        // 一律拒绝会让任何一次取 IP 失败变成「全站注册不可用」，那种故障比放过几个注册严重得多
        assertTrue(result.success());
        verify(redisService, never()).increment(anyString(), anyLong());
    }

    // ------------------------------------------------------------------ 查重与并发

    @Test
    @DisplayName("手机号已注册：给人话，不建号")
    void 手机号已注册() {
        when(memberRegisterDao.countByPhoneHash(PHONE_HASH)).thenReturn(1);

        assertEquals(RegisterFailReason.PHONE_TAKEN, service.register(cmd(PHONE, STRONG_PASSWORD)).reason());
        verify(memberIdAllocator, never()).nextMemberId();
    }

    @Test
    @DisplayName("🔴 并发撞唯一约束：收成「已被注册」，不是 500")
    void 并发重复注册() {
        // 查重和插入之间的窗口，靠库上的唯一约束闭合
        when(memberRegisterDao.insertMember(anyLong(), anyString(), anyString(), anyInt(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString()))
                .thenThrow(new DuplicateKeyException("uk_member_phone_hash"));

        MemberRegisterResult result = service.register(cmd(PHONE, STRONG_PASSWORD));

        assertEquals(RegisterFailReason.PHONE_TAKEN, result.reason(),
                "这是完全预期内的结果，抛出去就变成 500 了");
    }

    private MemberRegisterCmd cmd(String phone, String password) {
        return new MemberRegisterCmd(phone, password, "H5", CLIENT_IP, "APP");
    }
}
