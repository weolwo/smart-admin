package solvela.scriptengine.spi;

import solvela.exception.BusinessException;

/**
 * 「这次脚本执行是替谁跑的」—— 内部数据通道里的<b>标准身份</b>。
 *
 * <h3>为什么这个键定在引擎，而不是某个业务域的契约模块里</h3>
 * 会员号是<b>跨域</b>的：会员域要拿它查资料、奖品域要拿它查中奖记录、
 * 抽奖域要拿它发奖。如果键定义在 {@code solvela-marketing-api}（今天
 * {@code ActivityPlayKeys} 所在的地方），那会员域为了读一个字符串常量
 * 就得反过来依赖营销域的契约 —— 方向是错的，会员是营销的上游。
 *
 * <p>所以这里只上收<b>身份</b>这一个概念。活动编码、幂等键这类营销语义
 * 仍然留在 {@code ActivityPlayKeys}，引擎不认识它们。
 *
 * <h3>🔴 值必须与 {@code ActivityPlayKeys.MEMBER_ID} 完全一致</h3>
 * 绑定方是活动域（{@code ActivityPlayContext.bindInto}），读取方是各域的脚本函数。
 * 两个字面量不一致的表现是：函数拿到 null，<b>不报错</b>，
 * 然后「查不到这个人的中奖记录」被当成「他没中过奖」。
 * {@code solvela-marketing} 里有一条测试专门钉住这两个值相等。
 */
public final class ScriptIdentity {

    /**
     * 会员号在内部数据通道里的键。
     *
     * <p>放内部通道而不是脚本变量：脚本变量里那份 {@code memberId} 是脚本能改的，
     * 一句 {@code memberId = 10086} 就能让函数替别人查、替别人发。
     */
    public static final String MEMBER_ID = "__memberId";

    private ScriptIdentity() {
    }

    /**
     * 取当前执行的会员号，拿不到就抛。
     *
     * <p>🔴 <b>绝不兜底成 null 或 0</b>：那会让「上下文没绑」静默变成
     * 「这个人没有任何记录」，于是所有基于历史的限制当场失效，而且不会有任何报错。
     *
     * @param functionName 正在执行的脚本函数名，进报错信息，让人一眼看出是哪个函数用错了场景
     */
    public static Long requireMemberId(EngineContext context, String functionName) {
        Long memberId = context == null ? null : context.getInternal(MEMBER_ID, Long.class);
        if (memberId == null) {
            throw new BusinessException("脚本函数 [" + functionName + "] 拿不到会员号。"
                    + "这类函数只能在绑定了会员身份的场景里调用（如 ACTIVITY_PLAY），"
                    + "其它场景的内部通道里没有这个值。");
        }
        return memberId;
    }
}
