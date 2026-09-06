package solvela.scriptengine.handler;

import org.springframework.stereotype.Component;
import solvela.exception.BusinessException;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 内置工具：集合处理。补的是 QLExpress 4.x 在<b>隔离策略下拿不到的那部分</b>。
 *
 * <h3>先说清楚哪些不需要本类 —— 直接写字面量就行</h3>
 * 4.x 有原生集合字面量，3.x 时代的 {@code NewList / NewMap} 在这里是多余的：
 * <pre>
 *   l = [1, 2, 3];          // ArrayList
 *   m = {"a": 1, "b": 2};   // LinkedHashMap
 *   m2 = {:};               // 空 map（不是 {}，那是空代码块）
 *   m['c'] = 3;  m.d = 4;   // map 写入可以，读也可以：m.c / m['c']
 *   v = l[0];  n = l.length;
 *   for (x : l) { ... }
 *   big = l.filter(x -&gt; x &gt; 1);   // filter / map 是 QL 内置扩展函数，隔离策略下也能用
 * </pre>
 *
 * <h3>🔴 隔离策略挡掉的是「调 Java 方法」，本类补的就是这部分</h3>
 * {@code QLSecurityStrategy.isolation()} 下，{@code ReflectLoader} 的
 * {@code loadMethod} 与 {@code loadConstructor} 一律返回 null，所以下面这些<b>全部执行失败</b>：
 * <pre>
 *   l.add(x)     l.size()     l.sort()     m.put(k,v)     m.keySet()
 *   new ArrayList()          new HashMap()               s.length()
 * </pre>
 * 而且在脚本顶部写 {@code import java.util.HashMap;} 救不了 ——
 * import 只影响「类名解析得出来吗」，不影响「能不能调用它」。
 * 唯一的开关是 {@code script.allow-java-reflect=true}，那等于把 RCE 面交给脚本内容，不要动。
 *
 * <h3>🔴 一个静默的坑：map 没有 length</h3>
 * {@code l.length} 对 List 有效，但 {@code m.length} 在 map 上会被当成
 * <b>取一个名叫 length 的键</b>，返回 null 而不是报错。集合大小一律用 {@link #size}。
 */
@Component
public class ToolCollectionHandler implements ScriptFunctionHandler {

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.TOOL;
    }

    // ------------------------------------------------------------------
    // 构造
    // ------------------------------------------------------------------

    @ScriptFunction(name = "listOf",
            description = "把若干个值组成一个列表，如 tool_listOf(a, b)。等价于字面量 [a, b]，"
                    + "适合把返回值包一层列表")
    public List<Object> listOf(Object... items) {
        List<Object> list = new ArrayList<>();
        if (items != null) {
            Collections.addAll(list, items);
        }
        return list;
    }

    /**
     * 往列表里追加一个元素，返回<b>同一个</b>列表。
     *
     * <p>脚本里 {@code l.add(x)} 不通（隔离策略挡住了 Java 方法调用），
     * 而列表字面量建好之后没有别的办法变长 —— 这个函数就是为这件事存在的。
     *
     * <p>返回列表本身而不是 void，是为了能连着写：
     * {@code r = tool_listAdd(tool_listAdd([], a), b)}。
     */
    @ScriptFunction(name = "listAdd",
            description = "往列表末尾追加一个元素，返回这个列表本身。脚本里 list.add() 是不通的，追加只能走它")
    public List<Object> listAdd(List<Object> list, Object item) {
        if (list == null) {
            throw new BusinessException("tool_listAdd 的第一个参数是 null。先用 [] 或 tool_listOf() 建一个列表");
        }
        list.add(item);
        return list;
    }

    @ScriptFunction(name = "mapOf",
            description = "按「键,值,键,值...」成对组成一个 map，如 tool_mapOf('a', 1, 'b', 2)。参数个数必须是偶数")
    public Map<Object, Object> mapOf(Object... keyValues) {
        Map<Object, Object> map = new LinkedHashMap<>();
        if (keyValues == null || keyValues.length == 0) {
            return map;
        }
        if (keyValues.length % 2 != 0) {
            throw new BusinessException("tool_mapOf 的参数必须成对出现（键,值,键,值...），实际给了 "
                    + keyValues.length + " 个。漏一个值的话后面所有键值都会错位，所以这里直接拒绝");
        }
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ------------------------------------------------------------------
    // 读取
    // ------------------------------------------------------------------

    /**
     * 安全取值：map 按键取、list 按下标取，取不到就返回默认值。
     *
     * <p>🔴 本类最该被用起来的一个。脚本里直接写 {@code params['tier']}，
     * 键不存在时拿到的是 null 且不报错，然后 null 一路往下走、判断静默走错分支，
     * 最后表现成「这个活动偶尔发错奖」。给个默认值，这条路就断了。
     */
    @ScriptFunction(name = "get",
            description = "安全取值：map 按键取、list 按下标取，取不到（含越界、null）返回默认值。"
                    + "比 params['x'] 安全 —— 后者取不到时是 null，会静默走错分支")
    public Object get(Object collection, Object key, Object defaultValue) {
        if (collection == null || key == null) {
            return defaultValue;
        }
        Object value = null;
        if (collection instanceof Map<?, ?> map) {
            value = map.get(key);
        } else if (collection instanceof List<?> list) {
            int index = toIndex(key);
            if (index >= 0 && index < list.size()) {
                value = list.get(index);
            }
        }
        return value == null ? defaultValue : value;
    }

    /**
     * 集合大小。
     *
     * <p>List 也可以写 {@code l.length}，但 <b>map 不行</b> —— {@code m.length}
     * 是在取一个名叫 length 的键，静默返回 null。所以一律用这个。
     */
    @ScriptFunction(name = "size",
            description = "集合/字符串的大小，null 返回 0。"
                    + "🔴 map 不能写 m.length —— 那是在取名叫 length 的键，会静默返回 null")
    public Integer size(Object collection) {
        if (collection == null) {
            return 0;
        }
        if (collection instanceof Collection<?> c) {
            return c.size();
        }
        if (collection instanceof Map<?, ?> m) {
            return m.size();
        }
        if (collection instanceof CharSequence s) {
            return s.length();
        }
        if (collection.getClass().isArray()) {
            return Array.getLength(collection);
        }
        throw new BusinessException("tool_size 不认识这个类型：" + collection.getClass().getSimpleName());
    }

    @ScriptFunction(name = "isEmpty", description = "集合/字符串为 null 或没有元素")
    public Boolean isEmpty(Object collection) {
        return size(collection) == 0;
    }

    /**
     * 包含判定：list 看元素、map 看键、字符串看子串。
     *
     * <p>🔴 数字按<b>数值</b>比，不按类型比：QL 里列表字面量里的 {@code 1} 是 Integer，
     * 而算出来的 {@code 0 + 1} 是 BigDecimal（{@code precise=true} 的后果），
     * 用 equals 判这两个是 false —— 写脚本的人不可能预料到这件事。
     */
    @ScriptFunction(name = "contains",
            description = "list 是否含某元素 / map 是否含某键 / 字符串是否含子串。"
                    + "数字按数值比较，不受 Integer 与 BigDecimal 之别影响")
    public Boolean contains(Object collection, Object item) {
        if (collection == null) {
            return false;
        }
        if (collection instanceof Map<?, ?> map) {
            return map.keySet().stream().anyMatch(key -> equalsLoosely(key, item));
        }
        if (collection instanceof Collection<?> c) {
            return c.stream().anyMatch(element -> equalsLoosely(element, item));
        }
        if (collection instanceof CharSequence s) {
            return item != null && s.toString().contains(item.toString());
        }
        return false;
    }

    @ScriptFunction(name = "join", description = "把列表用分隔符拼成字符串，null 元素输出为空串")
    public String join(List<Object> list, String separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(separator == null ? "," : separator);
        list.forEach(item -> joiner.add(item == null ? "" : String.valueOf(item)));
        return joiner.toString();
    }

    // ------------------------------------------------------------------

    /**
     * 宽松相等：数字比数值，其余比 equals。理由见 {@link #contains}
     */
    private boolean equalsLoosely(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a instanceof Number na && b instanceof Number nb) {
            return new BigDecimal(na.toString()).compareTo(new BigDecimal(nb.toString())) == 0;
        }
        return a.equals(b);
    }

    private int toIndex(Object key) {
        if (key instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(key.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
