package solvela.scriptengine.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.EngineContext;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;

/**
 * 内置工具：脚本打日志。脚本里写 {@code tool_log(...)} / {@code tool_logWarn(...)}。
 *
 * <p>脚本是一段没有断点、没有单步、跑完只剩一个返回值的逻辑。判定为什么是这个结果，
 * 事后除了重放没有第二种查法 —— 除非它自己把中间量说出来：
 * <pre>
 *   tool_log('准入判定', 'memberId=', memberId, 'level=', memberLevel);
 *   if (memberLevel &lt; 3) {
 *       tool_log('等级不够，拒绝');
 *       return false;
 *   }
 *   return true;
 * </pre>
 *
 * <h3>两个刻意的设计</h3>
 * <ol>
 *   <li><b>日志走 {@code solvela.script} 这个独立 logger</b>，不挂在本类的类名下：
 *       脚本日志的量和噪音水平跟引擎自身的日志完全不是一回事，独立 logger 才能在 logback 里
 *       单独调级别、单开 appender，而不用连引擎日志一起关掉。</li>
 *   <li><b>每次执行有行数上限</b>（{@value #MAX_LINES_PER_EXECUTION} 行），超了只再说一句就闭嘴。
 *       理由和 {@code @ScriptFunction#sideEffect} 那道约束是同一个：脚本是运营写的，
 *       一个循环里手滑放一行日志，在超时窗口内足够把磁盘刷满，而没人会主动来报。</li>
 * </ol>
 *
 * <p>返回值是拼好的那行文本（不是 null）—— 让 {@code x = tool_log(...)} 这种写法有个确定结果，
 * 也省得有人拿 null 去做判断。日志函数<b>没有副作用</b>，想调几次调几次。
 */
@Component
public class ToolLogHandler implements ScriptFunctionHandler {

    /**
     * 脚本日志专用 logger。名字挂在 {@code solvela} 下，默认跟着工程的日志级别走
     */
    private static final Logger SCRIPT_LOG = LoggerFactory.getLogger("solvela.script");

    /**
     * 单次执行最多输出多少行
     */
    public static final int MAX_LINES_PER_EXECUTION = 100;

    /**
     * 单行最多多少字符，超出截断。防的是往日志里灌一整个 payload
     */
    public static final int MAX_MESSAGE_LENGTH = 1000;

    /**
     * 本次执行已输出行数，存在<b>内部数据通道</b>里：脚本看不见也清不掉，
     * 否则这道上限等于把开关交给了被约束的人
     */
    public static final String LOG_COUNT_KEY = "__logLineCount";

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.TOOL;
    }

    @ScriptFunction(name = "log",
            description = "输出一行 INFO 日志，多个参数用空格拼接，如 tool_log('等级=', memberLevel)。返回拼好的文本")
    public String log(EngineContext context, Object... args) {
        return write(context, false, args);
    }

    @ScriptFunction(name = "logWarn",
            description = "输出一行 WARN 日志，用法同 tool_log。留给「走到了不该走的分支」这类需要被翻出来的情况")
    public String logWarn(EngineContext context, Object... args) {
        return write(context, true, args);
    }

    // ------------------------------------------------------------------

    private String write(EngineContext context, boolean warn, Object[] args) {
        String message = compose(args);
        if (!acquireQuota(context)) {
            return message;
        }
        String scriptName = scriptNameOf(context);
        if (warn) {
            SCRIPT_LOG.warn("[{}] {}", scriptName, message);
        } else {
            SCRIPT_LOG.info("[{}] {}", scriptName, message);
        }
        return message;
    }

    /**
     * 把脚本传来的实参拼成一行。
     *
     * <p>空格分隔而不是 slf4j 那种 {@code {}} 占位符：占位符要求参数个数和占位符个数对齐，
     * 对不齐时最需要的那个值恰好不会被打出来 —— 而这是排查现场，不该再有第二个坑。
     */
    private String compose(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (Object arg : args) {
            if (!text.isEmpty()) {
                text.append(' ');
            }
            text.append(String.valueOf(arg));
            if (text.length() > MAX_MESSAGE_LENGTH) {
                return text.substring(0, MAX_MESSAGE_LENGTH) + "...(已截断)";
            }
        }
        return text.toString();
    }

    /**
     * 领一个输出配额。
     *
     * @return false 表示本次执行的日志额度已经用完，这一行不要再打
     */
    private boolean acquireQuota(EngineContext context) {
        if (context == null) {
            // 没有上下文说明这不是一次业务执行（引擎自检、预校验），没什么可限的
            return true;
        }
        Integer printed = context.getInternal(LOG_COUNT_KEY, Integer.class);
        int current = (printed == null ? 0 : printed) + 1;
        context.bindInternal(LOG_COUNT_KEY, current);

        if (current <= MAX_LINES_PER_EXECUTION) {
            return true;
        }
        if (current == MAX_LINES_PER_EXECUTION + 1) {
            // 最后一行留给「为什么后面没有了」，否则日志会莫名其妙断掉，比刷屏更难查
            SCRIPT_LOG.warn("[{}] 脚本日志已达单次执行上限 {} 行，后续 tool_log 不再输出。"
                            + "多半是循环里打了日志 —— 把它挪到循环外，或只在关键分支上打",
                    scriptNameOf(context), MAX_LINES_PER_EXECUTION);
        }
        return false;
    }

    private String scriptNameOf(EngineContext context) {
        if (context == null) {
            return "unknown";
        }
        String scriptName = context.getInternal(EngineContext.INTERNAL_SCRIPT_NAME, String.class);
        return scriptName == null ? "unknown" : scriptName;
    }
}
