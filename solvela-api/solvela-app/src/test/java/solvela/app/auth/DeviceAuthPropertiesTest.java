package solvela.app.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @DisplayName("默认档是 off —— 服务端先上线时不能影响任何存量客户端")
    void 默认档() {
        assertEquals(DeviceAuthProperties.Mode.OFF, properties.mode(),
                "默认必须是 OFF。漏配的后果不对称：OFF 只是「防刷还没生效」，"
                        + "ENFORCE 是「所有老客户端立刻用不了」");
    }

    @Test
    @DisplayName("头名从 yaml 读，不是硬编码")
    void 头名() {
        assertEquals("X-Device-Token", properties.header());
    }
}
