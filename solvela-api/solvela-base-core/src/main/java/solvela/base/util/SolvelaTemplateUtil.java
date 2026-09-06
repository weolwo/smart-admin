package solvela.base.util;

import java.util.Map;

/**
 * {@code ${key}} 占位符替换，替代 {@code commons-text} 的 {@code StringSubstitutor}。
 *
 * <p>项目里只有邮件模板和站内信模板两处用它，且模板里只出现过最朴素的 {@code ${key}}，
 * 没用过默认值语法。为这两处留一个 259KB 的依赖不划算（而且除我们之外没人依赖 commons-text）。
 *
 * <h3>与 StringSubstitutor 的口径对照</h3>
 * <ul>
 *   <li>✅ {@code ${key}} → 参数值，行为一致</li>
 *   <li>✅ <b>解析不到的占位符原样保留</b>（key 不存在、或值为 null），行为一致 ——
 *       模板是外部传进来的字符串，里面出现非参数的 {@code ${...}} 很正常，不能吞掉</li>
 *   <li>✅ {@code $${key}} 转义成字面量 {@code ${key}}，行为一致；
 *       而单独的 {@code $} 或 {@code $$x}（后面不跟 <code>{</code>）原样保留，同样一致</li>
 *   <li>🔀 <b>不对替换后的值做递归解析</b>。StringSubstitutor 默认会把替换进去的值再扫一遍，
 *       值里带 {@code ${...}} 会被继续替换。这里刻意不这么做：模板参数常常来自用户数据，
 *       递归解析等于给了一条模板注入的路。这是一处<b>有意的口径收紧</b>，已固化成测试</li>
 * </ul>
 *
 * @Date 2026-08-08
 */
public final class SolvelaTemplateUtil {

    /** 预留一点余量：占位符展开后通常比模板本身长，省掉一两次扩容拷贝 */
    private static final int EXTRA_CAPACITY = 32;

    private SolvelaTemplateUtil() {
    }

    public static String render(String template, Map<String, ?> params) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        StringBuilder out = new StringBuilder(template.length() + EXTRA_CAPACITY);
        int index = 0;
        int length = template.length();
        while (index < length) {
            if (isEscapedDollarBrace(template, index, length)) {
                // $${ 是转义：吐出字面量 ${，后面的内容按普通文本走
                out.append("${");
                index += 3;
                continue;
            }

            int consumed = appendPlaceholder(template, index, length, params, out);
            if (consumed > 0) {
                index += consumed;
                continue;
            }

            out.append(template.charAt(index));
            index++;
        }
        return out.toString();
    }

    private static boolean isEscapedDollarBrace(String template, int index, int length) {
        return template.charAt(index) == '$' && index + 2 < length
                && template.charAt(index + 1) == '$' && template.charAt(index + 2) == '{';
    }

    /**
     * 尝试把 {@code index} 处的一个 <code>${key}</code> 展开写进 {@code out}。
     *
     * <p>🔴 取不到值时<b>原样保留整个占位符</b>，不能吞成空串：模板多半是运营配的文案，
     * 变量名打错时留着 <code>${nickname}</code> 一眼就能看出是哪里配错了，
     * 而吞成空串只会让人以为「这个用户没有昵称」。
     *
     * @return 消耗掉的字符数；返回 0 表示这里不是一个完整的占位符，由调用方按普通字符处理
     */
    private static int appendPlaceholder(String template, int index, int length,
                                         Map<String, ?> params, StringBuilder out) {
        if (template.charAt(index) != '$' || index + 1 >= length || template.charAt(index + 1) != '{') {
            return 0;
        }
        int close = template.indexOf('}', index + 2);
        if (close <= 0) {
            // 有 ${ 没有 } —— 不是占位符，是一段恰好长这样的普通文本
            return 0;
        }
        Object value = params == null ? null : params.get(template.substring(index + 2, close));
        if (value != null) {
            out.append(value);
        } else {
            out.append(template, index, close + 1);
        }
        return close + 1 - index;
    }
}
