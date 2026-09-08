package solvela.biz.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solvela.auth.device.DeviceTokenCodec;
import solvela.member.device.DeviceGuard;
import solvela.member.device.DeviceGuardProperties;
import solvela.member.device.DeviceGuardRule;
import solvela.member.device.DeviceGuardVerdict;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 设备闸门的四条规则，连真实 Redis。
 *
 * <h3>这套用例真正要钉住的是 dry-run 这一档</h3>
 * 「命中」和「拦截」是<b>两件事</b>：dry-run 期间命中了照样放行。
 * 把它们合成一个布尔，这一档就没法表达了，而它恰恰是整个方案里最先要跑的那一档 ——
 * 四个阈值全是拍出来的，不先用真实流量校准就开拦截，最可能的结果是拦住
 * 一整栋写字楼里共用出口的正常用户，而<b>他们不会来报障，只会不再打开</b>。
 *
 * <p>每条用例用一个全新的 deviceId，避免互相烧配额 —— 计数键是按设备分的。
 *
 * @Date 2026-09-09
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class DeviceGuardTest {

    @Autowired
    private DeviceGuard guard;

    @Autowired
    private DeviceGuardProperties properties;

    private boolean originalDryRun;

    @AfterEach
    void restore() {
        properties.setDryRun(originalDryRun);
    }

    /** 切到真拦截档，并记住原值好还原。 */
    private void enforce() {
        originalDryRun = properties.isDryRun();
        properties.setDryRun(false);
    }

    private static String freshDevice() {
        return DeviceTokenCodec.newDeviceId();
    }

    // ============================== 老客户端 ==============================

    @Test
    @DisplayName("🔴 deviceId 为空 → 四条规则一律放行，且不命中")
    void 没有设备号一律放行() {
        enforce();
        for (String none : new String[]{null, "", "  "}) {
            assertPass(guard.checkLogin(none), "checkLogin");
            assertPass(guard.checkRegister(none), "checkRegister");
            assertPass(guard.checkMemberFanout(none, 1L), "checkMemberFanout");
            // 不该抛，也不该在 Redis 里留下键
            guard.recordLoginFailure(none);
        }
    }

    private static void assertPass(DeviceGuardVerdict v, String which) {
        assertTrue(v.allowed(), which + " 应当放行");
        assertFalse(v.hit(), which + " 不该命中任何规则");
        assertNull(v.rule());
    }

    // ============================== 登录频次 ==============================

    @Test
    @DisplayName("dry-run：登录超限【命中但放行】—— 这一档的全部意义")
    void 登录超限时dryRun照常放行() {
        properties.setDryRun(true);
        originalDryRun = true;
        String device = freshDevice();

        for (int i = 0; i < properties.getMaxLoginPerDay(); i++) {
            assertFalse(guard.checkLogin(device).hit(), "配额内不该命中");
        }
        DeviceGuardVerdict over = guard.checkLogin(device);

        assertTrue(over.hit(), "第 N+1 次必须命中");
        assertEquals(DeviceGuardRule.LOGIN_TOO_MANY, over.rule());
        assertTrue(over.allowed(), "🔴 dry-run 期间必须照常放行 —— 否则这一档没有存在的意义");
        assertTrue(over.retryAfterSeconds() > 0, "要告诉调用方还得等多久");
    }

    @Test
    @DisplayName("关掉 dry-run：同样的量级就真的拦了")
    void 关掉dryRun就拦() {
        enforce();
        String device = freshDevice();

        for (int i = 0; i < properties.getMaxLoginPerDay(); i++) {
            guard.checkLogin(device);
        }
        DeviceGuardVerdict over = guard.checkLogin(device);

        assertTrue(over.hit());
        assertFalse(over.allowed(), "关掉 dry-run 之后必须真的拦下来");
    }

    @Test
    @DisplayName("计数按设备隔离 —— 一台被限不影响另一台")
    void 按设备隔离() {
        enforce();
        String busy = freshDevice();
        for (int i = 0; i <= properties.getMaxLoginPerDay(); i++) {
            guard.checkLogin(busy);
        }
        assertFalse(guard.checkLogin(busy).allowed());

        assertTrue(guard.checkLogin(freshDevice()).allowed(),
                "限流键没按设备分开的话，一个人能把全站登录打瘫");
    }

    // ============================== 失败次数 ==============================

    @Test
    @DisplayName("🔴 失败计数只由 recordLoginFailure 累加，checkLogin 不能把正常登录算成失败")
    void 失败计数不被正常登录污染() {
        enforce();
        String device = freshDevice();

        // 只登录、不失败：登录次数远没到上限，失败规则也不该命中
        for (int i = 0; i < properties.getMaxFailPerHour() + 5; i++) {
            DeviceGuardVerdict v = guard.checkLogin(device);
            assertTrue(v.rule() != DeviceGuardRule.FAIL_TOO_MANY,
                    "checkLogin 里如果 incr 了失败计数，正常登录就会把自己顶成「连续失败」");
        }
    }

    @Test
    @DisplayName("连续失败超限 → 下一次登录被 FAIL_TOO_MANY 挡下")
    void 失败超限() {
        enforce();
        String device = freshDevice();

        for (int i = 0; i <= properties.getMaxFailPerHour(); i++) {
            guard.recordLoginFailure(device);
        }
        DeviceGuardVerdict v = guard.checkLogin(device);

        assertEquals(DeviceGuardRule.FAIL_TOO_MANY, v.rule());
        assertFalse(v.allowed());
    }

    // ============================== 一机多号 ==============================

    @Test
    @DisplayName("同一会员反复登录不算多号 —— 记的是【不同】会员数")
    void 同一会员不累加() {
        enforce();
        String device = freshDevice();

        for (int i = 0; i < properties.getMaxMembersPerDay() + 10; i++) {
            assertTrue(guard.checkMemberFanout(device, 777L).allowed(),
                    "同一个会员登第 " + i + " 次就被判成一机多号，那是把「频次」当成了「多号」");
        }
    }

    @Test
    @DisplayName("🔴 一机多号超限 → 命中 MEMBER_FANOUT")
    void 一机多号() {
        enforce();
        String device = freshDevice();
        long base = System.nanoTime();

        for (int i = 0; i < properties.getMaxMembersPerDay(); i++) {
            assertTrue(guard.checkMemberFanout(device, base + i).allowed(), "配额内不该拦");
        }
        DeviceGuardVerdict over = guard.checkMemberFanout(device, base + 999);

        assertEquals(DeviceGuardRule.MEMBER_FANOUT, over.rule(),
                "一机多号是养号的必然特征，四条里信号最强的一条");
        assertFalse(over.allowed());
    }

    @Test
    @DisplayName("关联集合每次都续期 —— 否则一台持续活跃的设备会在 24h 后计数归零")
    void 关联集合续期() throws Exception {
        properties.setDryRun(true);
        originalDryRun = true;
        String device = freshDevice();

        guard.checkMemberFanout(device, 1L);
        Thread.sleep(1100);
        guard.checkMemberFanout(device, 2L);

        // 第二次 add 之后 TTL 应当被重新推回接近一整个窗口，而不是只剩「窗口 - 1 秒」
        long ttl = ttlOfMemberSet(device);
        assertTrue(ttl > properties.dayWindow().toSeconds() - 1,
                "集合没有续期。只在首次设 TTL 的话，一台持续活跃的设备会在第一次关联的 24 小时后"
                        + "整个集合消失、计数归零 —— 那正是养号要的效果。实际 TTL=" + ttl);
    }

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redis;

    @Autowired
    private solvela.base.module.redis.RedisService redisService;

    private long ttlOfMemberSet(String device) {
        Long ttl = redis.getExpire(redisService.generateRedisKey("dev:member:", device));
        return ttl == null ? -1L : ttl;
    }

    // ============================== 注册 ==============================

    @Test
    @DisplayName("一台设备一天建号超限 → 命中 REGISTER_TOO_MANY")
    void 注册超限() {
        enforce();
        String device = freshDevice();

        for (int i = 0; i < properties.getMaxRegisterPerDay(); i++) {
            assertTrue(guard.checkRegister(device).allowed());
        }
        DeviceGuardVerdict over = guard.checkRegister(device);

        assertEquals(DeviceGuardRule.REGISTER_TOO_MANY, over.rule());
        assertFalse(over.allowed());
    }

    @Test
    @DisplayName("默认配置必须是 dry-run —— 新的拦截能力不该一上来就生效")
    void 默认dryRun() {
        assertTrue(properties.isDryRun(),
                "默认必须只观察不拦截：阈值全是拍出来的，直接开拦最可能拦住的是正常用户，"
                        + "而他们不会来报障，只会不再打开");
    }
}
