package solvela.scriptengine.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import solvela.base.module.redis.RedisService;
import solvela.exception.BusinessException;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.EngineContext;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;

import java.util.regex.Pattern;

/**
 * 内置能力：脚本自己的计数器与标记位（Redis）。脚本里写 {@code cache_incr(...)} 等。
 *
 * <p>存在的理由很具体：库里没有「参与流水」这张表的玩法（BASIC 活动就是），
 * 「这个人今天参与过几次」在脚本里没有任何办法算出来。给一个带 TTL 的计数器，
 * 这类判据才写得出来：
 * <pre>
 *   times = cache_incr('join:' + memberId, 86400);   // 当天有效
 *   if (times &gt; 3) {
 *       return null;                                 // 今天次数用完
 *   }
 *   return draw_executeDrawByScript('POOL_A');
 * </pre>
 *
 * <h3>🔴 三条硬约束，都不是可选项</h3>
 * <ol>
 *   <li><b>键名脚本说了不算。</b>实际的 key 是
 *       {@code 项目名:环境:script:{脚本编码}:{脚本给的名字}}。
 *       环境段来自 {@link RedisService#generateRedisKey}（多环境共用一个实例时的隔离位），
 *       脚本编码段保证<b>两个活动的脚本不会互相踩</b>，而且在线试跑用的是
 *       {@code online-test} 这个名字，试跑<b>碰不到线上脚本的键</b>。
 *       脚本能决定的只有最后一段，且只允许 {@code [A-Za-z0-9_:.-]}。</li>
 *   <li><b>TTL 必填，且有上限（{@value #MAX_TTL_SECONDS} 秒 = 30 天）。</b>
 *       没有 TTL 的键永不过期。运营写错一个键名，就在 Redis 里留下一批永远不会被清理、
 *       也永远不会有人发现的垃圾 —— 这类内存泄漏查起来毫无线索。</li>
 *   <li><b>没有 keys / scan / 批量删。</b>只能按名字单键读写删。
 *       给了模式匹配，一次 {@code cache_del('*')} 就能清掉整段业务缓存。</li>
 * </ol>
 *
 * <h3>🔴 写入不随事务回滚</h3>
 * 脚本在 {@code cache_incr} 之后抛异常，数据库事务会回滚，<b>但这次 +1 不会退回去</b>。
 * 所以计数器的语义是「尝试了几次」而不是「成功了几次」。要「成功几次」就去数业务表
 * （抽奖有 {@code t_draw_prize_log}，见 {@code draw_countDrawn}），别用这里。
 *
 * <p>本域的函数<b>刻意没有标 {@code sideEffect}</b>：那个标记是留给发奖、动账、扣库存的
 * 「一次执行只准调一次」硬约束，而计数器天然要读一次写一次，标上去脚本就没法写了。
 * 它们仍然是写操作，只是后果被 TTL 和键名前缀限制住了。
 */
@Component
@RequiredArgsConstructor
public class CacheScriptHandler implements ScriptFunctionHandler {

    /**
     * 所有脚本键的固定前缀。运维要按模式扫「脚本占了多少内存」时认这一段
     */
    private static final String KEY_PREFIX = "script:";

    /**
     * TTL 上限：30 天。再长的状态不该放在这里，那是业务表的事
     */
    public static final long MAX_TTL_SECONDS = 30 * 24 * 3600L;

    /**
     * 脚本能决定的那段键名允许的字符。不放行 {@code *} 和空格 —— 前者是模式匹配的入口
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_:.\\-]{1,64}");

    /**
     * 值长度上限。缓存里放的应该是计数和标记，不是一整个业务对象
     */
    private static final int MAX_VALUE_LENGTH = 512;

    private final RedisService redisService;

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.CACHE;
    }

    // ------------------------------------------------------------------
    // 计数器
    // ------------------------------------------------------------------

    /**
     * 计数 +1 并返回<b>累加后</b>的值，第一次计数时打上 TTL。
     *
     * <p>窗口是<b>固定窗口</b>：从第一次 +1 起算，TTL 到点整个计数清零，不会被后续调用续期。
     * 这一点由 {@link RedisService#increment} 保证（INCR 与 EXPIRE 在同一段 Lua 里，
     * 不会留下没有 TTL 的坏键）。
     *
     * <p>⚠️ 返回的是加完之后的值，所以「最多 3 次」要写成 {@code > 3} 而不是 {@code >= 3}。
     */
    @ScriptFunction(name = "incr",
            description = "计数 +1 并返回累加后的值；第一次计数时开始计 TTL（固定窗口，不续期）。"
                    + "⚠️ 返回的是加完之后的值，「最多 3 次」要写 > 3。写入不随事务回滚")
    public Long incr(EngineContext context, String name, long ttlSeconds) {
        return redisService.increment(keyOf(context, name), checkTtl(ttlSeconds));
    }

    /**
     * 读当前计数，键不存在返回 0。
     */
    @ScriptFunction(name = "count",
            description = "读当前计数值，键不存在或不是数字返回 0。只读，不影响 TTL")
    public Long count(EngineContext context, String name) {
        String value = redisService.get(keyOf(context, name));
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            // 同名键被 cache_set 写成了非数字。返回 0 而不是抛：
            // 这是脚本自己造成的，抛在这里只会让人去查 Redis，看不出是哪一行写坏的
            return 0L;
        }
    }

    // ------------------------------------------------------------------
    // 标记位
    // ------------------------------------------------------------------

    @ScriptFunction(name = "set",
            description = "写一个带 TTL 的值（最长 30 天，最多 512 字符），返回写入的值。写入不随事务回滚")
    public String set(EngineContext context, String name, String value, long ttlSeconds) {
        if (value == null) {
            throw new BusinessException("cache_set 的值是 null。要清掉一个键请用 cache_del");
        }
        if (value.length() > MAX_VALUE_LENGTH) {
            throw new BusinessException("cache_set 的值有 " + value.length() + " 个字符，超过上限 "
                    + MAX_VALUE_LENGTH + "。缓存里该放计数和标记，不是整个业务对象");
        }
        redisService.set(keyOf(context, name), value, checkTtl(ttlSeconds));
        return value;
    }

    @ScriptFunction(name = "get", description = "读一个值，键不存在返回 null")
    public String get(EngineContext context, String name) {
        return redisService.get(keyOf(context, name));
    }

    @ScriptFunction(name = "exists", description = "键还在不在（没过期）")
    public Boolean exists(EngineContext context, String name) {
        return redisService.get(keyOf(context, name)) != null;
    }

    @ScriptFunction(name = "del", description = "删掉一个键，返回被删的键名。只能单键删，没有模式匹配")
    public String del(EngineContext context, String name) {
        String key = keyOf(context, name);
        redisService.delete(key);
        return name;
    }

    /**
     * 剩余秒数。-1 表示键存在但没有 TTL，-2 表示键不存在。
     *
     * <p>两个负值原样透出去，不归一成 0 —— 脚本得分得清「已经过期了」和「压根没设过期」。
     */
    @ScriptFunction(name = "ttl",
            description = "剩余过期秒数。-2 = 键不存在，-1 = 键存在但没有过期时间。"
                    + "用来告诉用户「还要等多久」")
    public Long ttl(EngineContext context, String name) {
        return redisService.getExpire(keyOf(context, name));
    }

    // ------------------------------------------------------------------

    /**
     * 拼出实际的 Redis key：{@code 项目名:环境:script:{脚本编码}:{名字}}。
     *
     * <p>脚本编码从<b>内部通道</b>取（引擎在每次执行前写入），脚本读不到也改不掉 ——
     * 否则一段脚本可以把前缀改成别的活动的，去读、甚至清掉别人的计数。
     */
    private String keyOf(EngineContext context, String name) {
        if (name == null || !NAME_PATTERN.matcher(name).matches()) {
            throw new BusinessException("缓存键名 [" + name + "] 不合法：只允许字母、数字和 _ : . -，长度 1~64。"
                    + "不放行 * 和空格是刻意的 —— 那是模式匹配的入口");
        }
        return redisService.generateRedisKey(KEY_PREFIX, scriptNameOf(context) + ":" + name);
    }

    private String scriptNameOf(EngineContext context) {
        String scriptName = context == null
                ? null : context.getInternal(EngineContext.INTERNAL_SCRIPT_NAME, String.class);
        if (scriptName == null || scriptName.isBlank()) {
            // 正常执行路径一定有名字。到这里说明是引擎自检之类的场景，
            // 给个固定的兜底段，也不会和任何真实脚本的键撞上
            return "unknown";
        }
        return scriptName;
    }

    private long checkTtl(long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new BusinessException("缓存 TTL 必须大于 0 秒，实际给的是 " + ttlSeconds + "。"
                    + "不允许写不过期的键：写错一个键名就是一批永远不会被清理、也永远没人发现的垃圾");
        }
        if (ttlSeconds > MAX_TTL_SECONDS) {
            throw new BusinessException("缓存 TTL 是 " + ttlSeconds + " 秒，超过上限 " + MAX_TTL_SECONDS
                    + " 秒（30 天）。要存更久的状态，那是业务表该干的事");
        }
        return ttlSeconds;
    }
}
