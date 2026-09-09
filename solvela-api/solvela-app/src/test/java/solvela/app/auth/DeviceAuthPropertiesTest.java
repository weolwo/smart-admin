package solvela.app.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import solvela.trace.DeviceContract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 钉住 yaml 里那两行真的绑进来了。
 *
 * <h3>🔴 为什么这条用例值得单独存在：{@code mode: off} 是个 YAML 陷阱</h3>
 * YAML 1.1 把 {@code off / on / yes / no} 解析成<b>布尔</b>，不是字符串。
 * 三个档位里只有 {@code off} 撞上这条规则 —— 也就是说「默认档」恰好是唯一
 * 有歧义的那一个。
 *
 * <p>所以 yaml 里写的是带引号的 {@code "off"}。<b>去掉引号会直接启动失败</b>
 * （2026-09-08 实测：布尔 false 转不成 {@code Mode}，本用例的两条一起报
 * 「Failed to load ApplicationContext」）。
 *
 * <p>这个失败方式是<b>好的</b>——它响，而且响在启动那一刻。要警惕的是反过来：
 * 如果哪天有人给 {@code mode} 加了「转不过就当默认值」的兜底，
 * 这个陷阱就从「启动失败」退化成「配置写了不生效」，而后者没有任何迹象。
 *
 * <p>本用例断言的是<b>读到的值</b>，不是<b>行为的结果</b>，正是为了把这两者分开：
 * 断言行为的话，「读到 OFF」和「读失败兜底成 OFF」看起来一模一样。
 *
 * @Date 2026-09-08
 */
@SpringBootTest
class DeviceAuthPropertiesTest {

    @Autowired
    private DeviceAuthProperties properties;

    @Test
    @DisplayName("当前档是 observe —— 客户端已经在带令牌了，覆盖率必须开始记")
    void 当前档() {
        assertEquals(DeviceAuthProperties.Mode.OBSERVE, properties.mode(),
                "observe 与 off 对请求的处理完全一样（都放行），差别只是记不记覆盖率。"
                        + "而那个比例是决定「能不能切 enforce」的唯一依据 —— "
                        + "停在 off 等于把整套方案的产出关着");
    }

    @Test
    @DisplayName("兜底档仍是 off —— 漏配的后果不对称")
    void 兜底档() {
        assertEquals(DeviceAuthProperties.Mode.OFF, new DeviceAuthProperties(null, null).mode(),
                "漏配的后果不对称：OFF 只是「防刷还没生效」，"
                        + "ENFORCE 是「所有老客户端立刻用不了」");
    }

    @Test
    @DisplayName("🔴 头名是 X-Device-Token，与进程间那个 X-Device-Id 不是一回事")
    void 头名() {
        assertEquals("X-Device-Token", properties.header());

        /*
         * 🔴 这两个头方向不同、内容不同，【不能合并】：
         *   · X-Device-Token  客户端 → 网关，装【令牌】，网关要验签
         *   · X-Device-Id     网关 → 内部服务，装【验签通过的设备号】
         * 令牌绝不原样透传下去 —— 那等于把凭证散给所有内部服务
         * （见 DownstreamClientConfig 的注释）。
         *
         * 写混的代价是【静默的】：网关读不到自己要的那个头，就当作「没有设备身份」
         * 照常放行，请求全部成功，只是 device_id 恒为 NULL、覆盖率恒为 0%。
         * 2026-09-10 客户端第一版就是把令牌塞进了 X-Device-Id，
         * 一整套设备身份空转，没有任何报错。
         */
        assertNotEquals(DeviceContract.HEADER, properties.header(),
                "两个头一旦同名，「令牌不透传」这条就没法在代码上表达了");
    }
}
