package solvela.auth.device;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 设备令牌的签发与验签。
 *
 * <p>这套用例盯的是<b>「验签必须真的在验」</b>——一个只检查格式、不检查签名的实现
 * 能通过往返测试，但会让任何人都能自签令牌，而整套设备防刷的前提正是「deviceId 不可伪造」。
 * 所以下面每一段（版本 / 载荷 / 签名）都有一条独立的篡改用例。
 */
class DeviceTokenCodecTest {

    private static final String KEY_V1 = "v1-key-0123456789abcdef0123456789abcdef";
    private static final String KEY_V2 = "v2-key-fedcba9876543210fedcba9876543210";

    private static final String DEVICE_ID = "0123456789abcdef0123456789abcdef";

    private static DeviceTokenCodec codec(int keyVersion, Map<Integer, String> keys) {
        DeviceTokenProperties properties = new DeviceTokenProperties();
        properties.setKeyVersion(keyVersion);
        properties.setKeys(keys);
        return new DeviceTokenCodec(properties);
    }

    private static DeviceTokenCodec codecV1() {
        return codec(1, new LinkedHashMap<>(Map.of(1, KEY_V1)));
    }

    // ================================ 往返 ================================

    @Test
    @DisplayName("签发的令牌能验回同一个身份")
    void 往返() {
        DeviceTokenCodec codec = codecV1();
        String token = codec.issue(DEVICE_ID, "APP");

        DeviceIdentity identity = codec.verify(token);

        assertNotNull(identity);
        assertEquals(DEVICE_ID, identity.deviceId());
        assertEquals("APP", identity.deviceType());
        assertEquals(1, identity.keyVersion());
        // 秒精度，允许跨秒边界
        assertTrue(Math.abs(identity.issuedAt().getEpochSecond() - Instant.now().getEpochSecond()) <= 5);
    }

    @Test
    @DisplayName("令牌带 dv_ 前缀，与会员令牌的 mb_ 区分得开")
    void 前缀() {
        assertTrue(codecV1().issue(DEVICE_ID, "H5").startsWith("dv_"));
    }

    // ============================== 篡改 ==============================

    @Test
    @DisplayName("🔴 改载荷 → 验不过")
    void 篡改载荷() {
        DeviceTokenCodec codec = codecV1();
        String token = codec.issue(DEVICE_ID, "APP");
        String[] parts = token.substring(3).split("\\.");

        // 换成另一个设备号：这正是攻击者最想做的事 —— 拿一个合法令牌改成别人的设备
        String forged = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("ffffffffffffffffffffffffffffffff|APP|" + Instant.now().getEpochSecond())
                        .getBytes(StandardCharsets.UTF_8));

        assertNull(codec.verify("dv_" + parts[0] + "." + forged + "." + parts[2]));
    }

    @Test
    @DisplayName("🔴 改签名 → 验不过")
    void 篡改签名() {
        DeviceTokenCodec codec = codecV1();
        String token = codec.issue(DEVICE_ID, "APP");
        String[] parts = token.substring(3).split("\\.");

        // 🔴 必须按【字节】改，不能按字符改。
        // 16 字节的 base64url 是 22 个字符：前 21 个编掉 126 位，最后一个字符只有高 2 位有效，
        // 低 4 位是填充位，解码器直接忽略。所以「把最后一个字符换成另一个」有约 1/4 的概率
        // 解出完全相同的字节 —— 签名压根没被改动，用例就会假失败。
        // 本用例最初就是这么写的，跑了两次都是绿的，第三次才炸。
        byte[] sig = Base64.getUrlDecoder().decode(parts[2]);
        sig[0] ^= 0x01;
        String tampered = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);

        assertNull(codec.verify("dv_" + parts[0] + "." + parts[1] + "." + tampered));
    }

    @Test
    @DisplayName("🔴 改版本号 → 验不过（版本参与签名，不能只是个提示）")
    void 篡改版本号() {
        DeviceTokenCodec codec = codec(2, new LinkedHashMap<>(Map.of(1, KEY_V1, 2, KEY_V2)));
        String token = codec.issue(DEVICE_ID, "APP");   // 用 v2 签
        String[] parts = token.substring(3).split("\\.");
        assertEquals("2", parts[0]);

        // 谎称是 v1 签的。若版本没进签名材料，这里会用 v1 的密钥去验 v2 的签名而失败；
        // 但若实现是「用令牌自报的版本重算」且版本没参与签名，就会有绕过面。
        assertNull(codec.verify("dv_1." + parts[1] + "." + parts[2]));
    }

    @Test
    @DisplayName("未知的密钥版本 → 验不过")
    void 未知版本() {
        assertNull(codecV1().verify("dv_9.YWJj.YWJj"));
    }

    @Test
    @DisplayName("换了密钥内容之后，旧令牌验不过")
    void 换密钥() {
        String token = codecV1().issue(DEVICE_ID, "APP");

        DeviceTokenCodec rekeyed = codec(1, new LinkedHashMap<>(Map.of(1, "another-key-0000000000000000000000000000")));

        assertNull(rekeyed.verify(token));
    }

    // ============================== 密钥轮换 ==============================

    @Test
    @DisplayName("轮换：新令牌用新版本签，旧令牌仍按旧版本验得过")
    void 轮换() {
        String oldToken = codecV1().issue(DEVICE_ID, "APP");

        // 加了 v2 并把签发切到 v2 —— v1 仍留在 keys 里
        DeviceTokenCodec rotated = codec(2, new LinkedHashMap<>(Map.of(1, KEY_V1, 2, KEY_V2)));

        DeviceIdentity old = rotated.verify(oldToken);
        assertNotNull(old, "旧令牌必须还能用，否则轮换等于让所有用户重新注册设备");
        assertEquals(1, old.keyVersion());

        DeviceIdentity fresh = rotated.verify(rotated.issue(DEVICE_ID, "APP"));
        assertNotNull(fresh);
        assertEquals(2, fresh.keyVersion(), "新令牌必须用 key-version 指定的那一把签");
    }

    @Test
    @DisplayName("旧密钥被删掉之后，用它签的令牌就验不过了")
    void 删掉旧密钥() {
        String oldToken = codecV1().issue(DEVICE_ID, "APP");

        DeviceTokenCodec onlyV2 = codec(2, new LinkedHashMap<>(Map.of(2, KEY_V2)));

        assertNull(onlyV2.verify(oldToken));
    }

    // ============================== 形状 ==============================

    @Test
    @DisplayName("前缀不对、段数不对、空值 → 一律 null，不抛异常")
    void 形状不对() {
        DeviceTokenCodec codec = codecV1();
        String token = codec.issue(DEVICE_ID, "APP");
        String body = token.substring(3);

        assertNull(codec.verify(null));
        assertNull(codec.verify(""));
        assertNull(codec.verify("mb_abc"), "会员令牌不该被当成设备令牌");
        assertNull(codec.verify(body), "少了 dv_ 前缀");
        assertNull(codec.verify("dv_1.onlytwo"));
        assertNull(codec.verify("dv_" + body + ".extra"));
        assertNull(codec.verify("dv_1..sig"));
        assertNull(codec.verify("dv_x.YWJj.YWJj"), "版本号不是数字");
        assertNull(codec.verify("dv_0.YWJj.YWJj"), "版本号必须为正");
        assertNull(codec.verify("dv_1.!!!not-base64!!!.YWJj"));
    }

    @Test
    @DisplayName("🔴 末尾空段不能被 split 吞掉：签名为空的令牌必须验不过")
    void 空签名段() {
        DeviceTokenCodec codec = codecV1();
        String[] parts = codec.issue(DEVICE_ID, "APP").substring(3).split("\\.");

        assertNull(codec.verify("dv_" + parts[0] + "." + parts[1] + "."));
    }

    @Test
    @DisplayName("🔴 签名合法但载荷内容非法 → 仍然拒绝")
    void 载荷校验不依赖签名() throws Exception {
        // 用真密钥签一个 deviceId 形状不对的载荷：模拟「签发侧出了 bug」或「密钥泄露后
        // 攻击者自签」。签名这一关过了，载荷校验必须自己拦住
        DeviceTokenCodec codec = codecV1();

        assertNull(codec.verify(forge("not-hex|APP|123")), "deviceId 不是 32 位 hex");
        assertNull(codec.verify(forge(DEVICE_ID + "||123")), "deviceType 为空");
        assertNull(codec.verify(forge(DEVICE_ID + "|APP|not-a-number")), "签发时间不是数字");
        assertNull(codec.verify(forge(DEVICE_ID + "|APP")), "字段数不对");
        assertNull(codec.verify(forge(DEVICE_ID + "|APP|1|extra")), "字段数不对");
        assertNull(codec.verify(forge(DEVICE_ID.toUpperCase() + "|APP|123")), "hex 必须小写，与库里一致");
    }

    /** 用 v1 真密钥给任意载荷签一个格式完全合法的令牌。 */
    private static String forge(String rawPayload) throws Exception {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String payload = encoder.encodeToString(rawPayload.getBytes(StandardCharsets.UTF_8));
        String signed = "1." + payload;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(KEY_V1.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] full = mac.doFinal(signed.getBytes(StandardCharsets.UTF_8));
        byte[] truncated = new byte[16];
        System.arraycopy(full, 0, truncated, 0, 16);

        return "dv_" + signed + "." + encoder.encodeToString(truncated);
    }

    // ============================== 签发的入参 ==============================

    @Test
    @DisplayName("签发的入参非法 → 抛异常（与 verify 返回 null 相反：这是代码写错了）")
    void 签发入参() {
        DeviceTokenCodec codec = codecV1();

        assertThrows(IllegalArgumentException.class, () -> codec.issue(null, "APP"));
        assertThrows(IllegalArgumentException.class, () -> codec.issue("short", "APP"));
        assertThrows(IllegalArgumentException.class, () -> codec.issue(DEVICE_ID.toUpperCase(), "APP"));
        assertThrows(IllegalArgumentException.class, () -> codec.issue(DEVICE_ID, null));
        assertThrows(IllegalArgumentException.class, () -> codec.issue(DEVICE_ID, "  "));
        assertThrows(IllegalArgumentException.class, () -> codec.issue(DEVICE_ID, "AP|P"),
                "含分隔符的 deviceType 会让载荷多切一段，签出来的令牌自己验不过");
    }

    // ============================== 设备号 ==============================

    @Test
    @DisplayName("newDeviceId 生成 32 位小写 hex，且不重复")
    void 设备号() {
        Set<String> ids = IntStream.range(0, 1000)
                .mapToObj(i -> DeviceTokenCodec.newDeviceId())
                .collect(Collectors.toSet());

        assertEquals(1000, ids.size(), "1000 次生成不该有碰撞");
        ids.forEach(id -> assertTrue(id.matches("[0-9a-f]{32}"), "形状不对：" + id));
        // 生成出来的必须能被自己签发、验回来 —— 生成与校验分居两处时最容易漂移的就是这一点
        DeviceTokenCodec codec = codecV1();
        ids.stream().limit(20).forEach(id ->
                assertEquals(id, codec.verify(codec.issue(id, "APP")).deviceId()));
    }

    // ============================== 启动期校验 ==============================

    @Test
    @DisplayName("🔴 没配密钥 → 启动即失败，不给默认密钥兜底")
    void 没配密钥() {
        assertThrows(IllegalStateException.class, () -> codec(1, new LinkedHashMap<>()));
        DeviceTokenProperties nullKeys = new DeviceTokenProperties();
        nullKeys.setKeys(null);
        assertThrows(IllegalStateException.class, () -> new DeviceTokenCodec(nullKeys));
    }

    @Test
    @DisplayName("密钥太短 → 启动即失败（弱密钥 = 任何人都能自签，且日志里看不出来）")
    void 密钥太短() {
        assertThrows(IllegalStateException.class, () -> codec(1, new LinkedHashMap<>(Map.of(1, "too-short"))));
    }

    @Test
    @DisplayName("🔴 key-version 指向一把不存在的密钥 → 启动即失败")
    void 签发版本不存在() {
        // 轮换时把顺序搞反（先改 key-version 再加密钥）就是这个下场。
        // 不在启动时拦住的话，表现是「从这一刻起新签的令牌全部验不过」
        assertThrows(IllegalStateException.class, () -> codec(2, new LinkedHashMap<>(Map.of(1, KEY_V1))));
    }

    @Test
    @DisplayName("版本号必须是正整数")
    void 版本号非正() {
        assertThrows(IllegalStateException.class, () -> codec(1, new LinkedHashMap<>(Map.of(0, KEY_V1))));
        assertThrows(IllegalStateException.class, () -> codec(1, new LinkedHashMap<>(Map.of(-1, KEY_V1))));
    }

    @Test
    @DisplayName("同一设备两次签发得到的令牌可以不同，但都验得过")
    void 重复签发() {
        DeviceTokenCodec codec = codecV1();

        DeviceIdentity a = codec.verify(codec.issue(DEVICE_ID, "APP"));
        DeviceIdentity b = codec.verify(codec.issue(DEVICE_ID, "H5"));

        assertNotNull(a);
        assertNotNull(b);
        assertEquals(a.deviceId(), b.deviceId());
        assertNotEquals(a.deviceType(), b.deviceType(), "deviceType 是各签各的，不是全局状态");
    }
}
