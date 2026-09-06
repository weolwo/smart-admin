package solvela.prize.runtime;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import solvela.exception.BusinessException;
import solvela.prize.PrizeLog;
import solvela.prize.prizelog.dao.PrizeLogDao;
import solvela.prize.prizelog.service.PrizeLogService;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.EngineContext;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;
import solvela.scriptengine.spi.ScriptIdentity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 暴露给脚本的中奖记录查询函数。数据源是 {@code t_prize_log} —— 每中一次一行。
 *
 * <pre>
 *   if (prize_hasWon('GRAND_PRIZE')) {
 *       return draw_executeDrawByScript('POOL_CONSOLATION');   // 中过大奖的只能抽安慰池
 *   }
 * </pre>
 *
 * <h3>活动编码是脚本参数，会员号不是</h3>
 * 不对称是有理由的：
 * <ul>
 *   <li><b>会员号</b>走内部通道（{@link ScriptIdentity}）—— 脚本能改的话，
 *       就能查别人的中奖记录，也能拿别人的记录来放宽自己的限制；</li>
 *   <li><b>活动编码</b>由脚本传，不传就是不限活动。「这个人在别的活动里中过没有」
 *       是真实存在的判据（比如全站限一次的实物大奖），写死成当前活动就表达不了。
 *       脚本把它写错的后果只落在它自己的限制上，不涉及别人。</li>
 * </ul>
 *
 * <h3>🔴 返回 Map 不返回实体</h3>
 * 隔离策略下脚本读不到普通对象的字段（{@code loadField} 直接返回 null，且不报错），
 * 所以查询类函数只能返回 Map。顺带只放该放的字段：审批人、失败原因这些内部字段不给。
 *
 * <p>全部是只读函数，脚本里想调几次调几次。
 */
@Component
@RequiredArgsConstructor
public class PrizeScriptFunctions implements ScriptFunctionHandler {

    /**
     * 一次最多取多少条明细。脚本不是报表工具，取回一堆记录只会让执行变慢
     */
    private static final int MAX_LIMIT = 50;

    private final PrizeLogDao prizeLogDao;

    private final PrizeLogService prizeLogService;

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.PRIZE;
    }

    /**
     * 中奖次数。
     *
     * @param activityCode 可选，不传就是全部活动累计
     */
    @ScriptFunction(name = "countWon",
            description = "该会员中了几次奖。可选传活动编码，不传就是全部活动累计。"
                    + "数的是 t_prize_log 的行数，派发失败的那次也算中过")
    public Integer countWon(EngineContext context, Object... activityCode) {
        Long memberId = ScriptIdentity.requireMemberId(context, "prize_countWon");
        String scope = optionalActivityCode(activityCode, "prize_countWon");
        long count = prizeLogDao.selectCount(Wrappers.<PrizeLog>lambdaQuery()
                .eq(PrizeLog::getMemberId, memberId)
                .eq(scope != null, PrizeLog::getActivityCode, scope));
        return Math.toIntExact(count);
    }

    /**
     * 有没有中过某个奖品。
     *
     * <p>「一人一次大奖」这类判据用它，比把明细拉回来自己遍历省一个数量级。
     */
    @ScriptFunction(name = "hasWon",
            description = "该会员有没有中过某个奖品（按奖品编码）。做「一人限一次大奖」用")
    public Boolean hasWon(EngineContext context, String prizeCode) {
        if (prizeCode == null || prizeCode.isBlank()) {
            throw new BusinessException("prize_hasWon 没有给奖品编码。"
                    + "不给编码的话这个判断永远为真，会静默放行所有人");
        }
        Long memberId = ScriptIdentity.requireMemberId(context, "prize_hasWon");
        return prizeLogDao.exists(Wrappers.<PrizeLog>lambdaQuery()
                .eq(PrizeLog::getMemberId, memberId)
                .eq(PrizeLog::getPrizeCode, prizeCode));
    }

    /**
     * 最近的中奖明细，按时间倒序。
     *
     * @param limit 取几条，上限 {@value #MAX_LIMIT}
     */
    @ScriptFunction(name = "listRecent",
            description = "该会员最近的中奖记录，按时间倒序，每条是一个 map："
                    + "prizeCode/prizeName/prizeType/prizeLevel/prizeValue/activityCode/status/createTime。"
                    + "上限 " + MAX_LIMIT + " 条")
    public List<Object> listRecent(EngineContext context, int limit, Object... activityCode) {
        Long memberId = ScriptIdentity.requireMemberId(context, "prize_listRecent");
        if (limit <= 0) {
            throw new BusinessException("prize_listRecent 要取 " + limit + " 条。"
                    + "取 0 条不是一种查询 —— 想判断有没有中过用 prize_countWon");
        }
        String scope = optionalActivityCode(activityCode, "prize_listRecent");
        return prizeLogService.listRecentByMember(memberId, scope, Math.min(limit, MAX_LIMIT))
                .stream().map(this::project).map(Object.class::cast).toList();
    }

    // ------------------------------------------------------------------

    /**
     * 挑出能给脚本看的字段。白名单：以后表上加了什么列，默认不会流到脚本里。
     *
     * <p>审批人、失败原因、提案号这些内部字段刻意不给 —— 脚本能把拿到的东西打进日志。
     */
    private Map<String, Object> project(PrizeLog log) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("prizeCode", log.getPrizeCode());
        item.put("prizeName", log.getPrizeName());
        item.put("prizeType", log.getPrizeType());
        item.put("prizeLevel", log.getPrizeLevel());
        item.put("prizeValue", log.getPrizeValue());
        item.put("activityCode", log.getActivityCode());
        item.put("status", log.getStatus() == null ? null : log.getStatus().name());
        item.put("createTime", log.getCreateTime());
        return item;
    }

    /**
     * 可选的活动编码。返回 null 表示不限活动
     */
    private String optionalActivityCode(Object[] activityCode, String functionName) {
        if (activityCode == null || activityCode.length == 0 || activityCode[0] == null) {
            return null;
        }
        if (activityCode.length > 1) {
            throw new BusinessException(functionName + " 最多只收一个活动编码，实际给了 "
                    + activityCode.length + " 个");
        }
        String scope = String.valueOf(activityCode[0]).trim();
        return scope.isEmpty() ? null : scope;
    }
}
