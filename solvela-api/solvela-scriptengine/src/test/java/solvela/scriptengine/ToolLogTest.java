package solvela.scriptengine;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.core.DefaultScriptEngine;
import solvela.scriptengine.core.QLExpressEvaluator;
import solvela.scriptengine.core.ScriptEngineProperties;
import solvela.scriptengine.domain.EngineFunctionMeta;
import solvela.scriptengine.domain.ExecutableScript;
import solvela.scriptengine.handler.ToolLogHandler;
import solvela.scriptengine.spi.EngineContext;
import solvela.scriptengine.spi.ScriptEngine;
import solvela.scriptengine.spi.ScriptEvaluator;
import solvela.scriptengine.spi.ScriptFunctionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code tool_log} 的行为固化测试。
 *
 * <p>钉住四件事：日志里认得出是哪个脚本、脚本变量的值真的被打出来了、
 * 单次执行的行数上限拦得住循环刷屏、以及计数器脚本改不掉。
 */
public class ToolLogTest {

    private ScriptEngine scriptEngine;

    private ScriptEvaluator evaluator;

    private ch.qos.logback.classic.Logger scriptLogger;

    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        this.evaluator = new QLExpressEvaluator(new ScriptEngineProperties());
        this.scriptEngine = new DefaultScriptEngine(evaluator);
        bind(new ToolLogHandler());

        // 直接挂在脚本日志那个 logger 上，验的就是「这行确实进了日志」，不是「方法被调过」
        this.scriptLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("solvela.script");
        this.scriptLogger.setLevel(Level.INFO);
        this.appender = new ListAppender<>();
        this.appender.start();
        this.scriptLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        scriptLogger.detachAppender(appender);
    }

    @Test
    @DisplayName("tool_log 打出的行带脚本名，且脚本变量的值被拼了进去")
    void log_line_carries_script_name_and_variable_values() {
        EngineContext context = EngineContext.create().bind("memberLevel", 3);

        Object result = scriptEngine.evaluate(
                ExecutableScript.trusted("draw/vip_entry",
                        "tool_log('准入判定', 'level=', memberLevel); return memberLevel >= 3;"),
                context);

        assertEquals(Boolean.TRUE, result);
        assertEquals(1, appender.list.size());

        ILoggingEvent event = appender.list.get(0);
        assertEquals(Level.INFO, event.getLevel());
        assertEquals("[draw/vip_entry] 准入判定 level= 3", event.getFormattedMessage(),
                "日志要认得出是哪个脚本打的，否则一堆脚本共用一个日志文件时没法排查");
    }

    @Test
    @DisplayName("tool_logWarn 走 WARN 级别，返回值是拼好的那行文本")
    void log_warn_uses_warn_level_and_returns_composed_text() {
        Object result = scriptEngine.evaluate(
                ExecutableScript.trusted("task/streak", "return tool_logWarn('走到了兜底分支');"),
                EngineContext.create());

        assertEquals("走到了兜底分支", result);
        assertEquals(Level.WARN, appender.list.get(0).getLevel());
    }

    @Test
    @DisplayName("日志函数没有副作用，一次执行里想调几次调几次")
    void log_is_not_limited_like_side_effect_functions() {
        scriptEngine.evaluate(
                ExecutableScript.trusted("test/multi", """
                        tool_log('第一行');
                        tool_log('第二行');
                        return true;
                        """),
                EngineContext.create());

        assertEquals(2, appender.list.size());
    }

    @Test
    @DisplayName("🔴 循环里刷日志会被单次执行的行数上限拦住，且最后一行说明了为什么断掉")
    void log_lines_are_capped_per_execution() {
        int loops = ToolLogHandler.MAX_LINES_PER_EXECUTION + 50;

        scriptEngine.evaluate(
                ExecutableScript.trusted("test/flood",
                        "for (i = 0; i < " + loops + "; i++) { tool_log('刷屏', i); } return true;"),
                EngineContext.create());

        // 上限内的正常输出 + 一条「已达上限」的说明，之后彻底闭嘴
        assertEquals(ToolLogHandler.MAX_LINES_PER_EXECUTION + 1, appender.list.size(),
                "超过上限后不能再往日志里写，否则运营在循环里手滑一行就能把磁盘刷满");

        ILoggingEvent last = appender.list.get(appender.list.size() - 1);
        assertEquals(Level.WARN, last.getLevel());
        assertTrue(last.getFormattedMessage().contains("上限"),
                "日志不能莫名其妙断掉，最后一行要说明原因。实际: " + last.getFormattedMessage());
    }

    @Test
    @DisplayName("行数计数器和脚本名都在内部通道，脚本看不见也改不掉")
    void log_bookkeeping_is_invisible_to_script() {
        EngineContext context = EngineContext.create();

        scriptEngine.evaluate(
                ExecutableScript.trusted("test/bookkeeping", "tool_log('一行'); return true;"), context);

        assertFalse(context.getScriptVariables().containsKey(ToolLogHandler.LOG_COUNT_KEY));
        assertFalse(context.getScriptVariables().containsKey(EngineContext.INTERNAL_SCRIPT_NAME));
        assertEquals(1, context.getInternal(ToolLogHandler.LOG_COUNT_KEY, Integer.class));
        assertEquals("test/bookkeeping", context.getInternal(EngineContext.INTERNAL_SCRIPT_NAME, String.class));
    }

    // ------------------------------------------------------------------

    /**
     * 把 handler 上所有 @ScriptFunction 方法绑进 evaluator，等价于启动期 EngineFunctionScanner 做的事
     */
    private void bind(ScriptFunctionHandler handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            ScriptFunction annotation = method.getAnnotation(ScriptFunction.class);
            if (annotation == null) {
                continue;
            }
            boolean injectContext = method.getParameterCount() > 0
                    && EngineContext.class.isAssignableFrom(method.getParameterTypes()[0]);
            List<String> params = Arrays.stream(method.getParameters())
                    .skip(injectContext ? 1 : 0)
                    .map(p -> p.getType().getSimpleName() + " " + p.getName())
                    .collect(Collectors.toList());
            evaluator.registerFunction(EngineFunctionMeta.builder()
                    .domain(handler.domain())
                    .functionName(handler.domain().qualify(annotation.name()))
                    .simpleName(annotation.name())
                    .targetBean(handler)
                    .method(method)
                    .injectContext(injectContext)
                    .sideEffect(annotation.sideEffect())
                    .description(annotation.description())
                    .returnType(method.getReturnType().getSimpleName())
                    .params(params)
                    .build());
        }
    }
}
