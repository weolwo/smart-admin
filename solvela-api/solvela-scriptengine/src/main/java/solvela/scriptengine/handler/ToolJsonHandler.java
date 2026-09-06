package solvela.scriptengine.handler;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import solvela.base.json.JsonUtils;
import solvela.exception.BusinessException;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;

import java.util.List;
import java.util.Map;

/**
 * 内置工具：JSON 序列化、解析与路径取值。
 *
 * <p>最常用的两处：把上游塞在 {@code payload} 里的自定义字段挖出来，
 * 以及把一段结构打进日志好排查。
 * <pre>
 *   amount = tool_jsonGet(payload, 'order.amount', 0);
 *   tool_log('事件内容', tool_toJson(payload));
 * </pre>
 *
 * <h3>为什么 {@code tool_jsonGet} 的第一个参数不限定是字符串</h3>
 * 场景变量里的 {@code payload} / {@code params} <b>已经是 Map 了</b>，不是 JSON 串；
 * 而缓存里取出来的是串。两种都得能用，否则脚本作者要先判断「我手上这个是什么」——
 * 那个判断他做不了，也不该由他做。
 *
 * <h3>🔴 深层取值不要用 {@code a['b']['c']}</h3>
 * 中间任何一层不存在，QL 都会在下一次取键时报错或静默给 null，而错误信息里
 * <b>看不出是哪一层断的</b>。{@code tool_jsonGet} 断在哪一层都返回默认值，且路径写在一处。
 */
@Component
public class ToolJsonHandler implements ScriptFunctionHandler {

    /**
     * 解析输入与序列化输出的长度上限。脚本不是数据处理管道，超过这个量级说明用错地方了
     */
    private static final int MAX_JSON_LENGTH = 16_000;

    private static final TypeReference<Object> ANY = new TypeReference<>() {
    };

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.TOOL;
    }

    // ------------------------------------------------------------------
    // 序列化
    // ------------------------------------------------------------------

    /**
     * 转成 JSON 串。
     *
     * <p>⚠️ 传字符串进来会原样返回（沿用 {@link JsonUtils#toJson} 的约定），
     * 不会给你加一对引号 —— 它的用途是「把结构变成能看的一行」，不是产出严格合法的 JSON 文档。
     */
    @ScriptFunction(name = "toJson",
            description = "把 map/list 等结构转成一行 JSON 串，常用于打日志或写进缓存。"
                    + "⚠️ 传字符串进来原样返回，不会加引号")
    public String toJson(Object value) {
        if (value == null) {
            return "";
        }
        String json = JsonUtils.toJson(value);
        if (json != null && json.length() > MAX_JSON_LENGTH) {
            throw new BusinessException("tool_toJson 序列化出来有 " + json.length()
                    + " 个字符，超过上限 " + MAX_JSON_LENGTH + "。脚本里不该搬运这么大的结构");
        }
        return json == null ? "" : json;
    }

    @ScriptFunction(name = "toJsonPretty",
            description = "同 tool_toJson，但带缩进换行。只适合排查时打日志，不要写进缓存")
    public String toJsonPretty(Object value) {
        if (value == null) {
            return "";
        }
        try {
            String json = JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
            if (json.length() > MAX_JSON_LENGTH) {
                throw new BusinessException("tool_toJsonPretty 序列化出来有 " + json.length()
                        + " 个字符，超过上限 " + MAX_JSON_LENGTH);
            }
            return json;
        } catch (JacksonException e) {
            throw new BusinessException("tool_toJsonPretty 序列化失败：" + e.getOriginalMessage());
        }
    }

    // ------------------------------------------------------------------
    // 解析
    // ------------------------------------------------------------------

    /**
     * 解析 JSON 串，得到 map 或 list。
     *
     * <p>解析失败<b>抛异常而不是返回 null</b>：返回 null 的话，后面每一次取值都是 null，
     * 脚本会静默走完一条错误的分支，最后表现成「这个活动偶尔判错」。
     */
    @ScriptFunction(name = "parseJson",
            description = "把 JSON 串解析成 map 或 list。解析失败直接报错（不返回 null —— "
                    + "那会让脚本静默走完一条错误分支）")
    public Object parseJson(Object value) {
        if (value == null) {
            throw new BusinessException("tool_parseJson 的入参是 null");
        }
        if (value instanceof Map || value instanceof List) {
            // 已经是结构了，原样还回去：脚本作者不必先判断手上这个是串还是 map
            return value;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            throw new BusinessException("tool_parseJson 的入参是空串");
        }
        if (text.length() > MAX_JSON_LENGTH) {
            throw new BusinessException("tool_parseJson 的入参有 " + text.length()
                    + " 个字符，超过上限 " + MAX_JSON_LENGTH);
        }
        try {
            return JsonUtils.getMapper().readValue(text, ANY);
        } catch (JacksonException e) {
            throw new BusinessException("tool_parseJson 解析失败：" + e.getOriginalMessage()
                    + "。内容开头是：" + text.substring(0, Math.min(50, text.length())));
        }
    }

    /**
     * 按路径取值，取不到就给默认值。
     *
     * @param source 可以是 map、list，也可以是 JSON 串
     * @param path   点号分隔，如 {@code order.items.0.skuId}；纯数字的一段按下标取
     */
    @ScriptFunction(name = "jsonGet",
            description = "按路径取值，如 tool_jsonGet(payload, 'order.amount', 0)。"
                    + "点号分隔，纯数字的一段按列表下标取；中间任何一层不存在都返回默认值。"
                    + "入参可以是 map/list，也可以是 JSON 串")
    public Object jsonGet(Object source, String path, Object defaultValue) {
        if (source == null || path == null || path.isBlank()) {
            return defaultValue;
        }
        Object current = source instanceof CharSequence ? parseQuietly(source) : source;
        for (String segment : path.split("\\.")) {
            if (current == null || segment.isEmpty()) {
                return defaultValue;
            }
            current = step(current, segment);
        }
        return current == null ? defaultValue : current;
    }

    // ------------------------------------------------------------------

    private Object step(Object current, String segment) {
        if (current instanceof Map<?, ?> map) {
            return map.get(segment);
        }
        if (current instanceof List<?> list) {
            int index = toIndex(segment);
            return index >= 0 && index < list.size() ? list.get(index) : null;
        }
        // 走到标量还没走完路径：路径写长了，按「取不到」处理
        return null;
    }

    private int toIndex(String segment) {
        try {
            return Integer.parseInt(segment);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 取值路径上的解析失败按「取不到」处理，返回默认值即可 ——
     * 与 {@link #parseJson} 的严格态度不同：那里解析是目的，这里解析只是手段
     */
    private Object parseQuietly(Object source) {
        String text = String.valueOf(source).trim();
        if (text.isEmpty() || text.length() > MAX_JSON_LENGTH) {
            return null;
        }
        try {
            return JsonUtils.getMapper().readValue(text, ANY);
        } catch (JacksonException e) {
            return null;
        }
    }
}
