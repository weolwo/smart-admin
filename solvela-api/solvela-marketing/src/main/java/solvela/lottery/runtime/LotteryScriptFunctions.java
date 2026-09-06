package solvela.lottery.runtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import solvela.activity.runtime.ActivityPlayContext;
import solvela.exception.BusinessException;
import solvela.lottery.LotteryRecord;
import solvela.lottery.runtime.domain.TicketObtainDTO;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 暴露给编排脚本的彩票函数。
 *
 * <pre>
 *   if (lottery_countMine('MID_AUTUMN', '2026_MID_01') &gt;= 3) {
 *       return null;                                       // 这期已经领满 3 张
 *   }
 *   return lottery_issue('MID_AUTUMN', '2026_MID_01');     // 最后一步，只准调一次
 * </pre>
 *
 * <h3>🔴 单人限购由脚本挡，发号引擎不管</h3>
 * {@link TicketIssueService} 是纯粹的号码派发引擎：它只做幂等、防刷、期号校验，
 * <b>刻意没有业务限购</b>（见那个类的注释）。也就是说，「一个人这期能领几张」
 * 这条判据<b>只存在于脚本里</b> —— 脚本不写，就是无限领。
 * 所以 {@link #countMine} 和 {@link #issue} 必须成对出现在脚本里。
 *
 * <h3>会员号与幂等键不从脚本参数取</h3>
 * 与抽奖函数同一条规矩（见 {@code DrawScriptFunctions}）：它们从内部通道取，
 * 脚本改不掉。脚本只决定「领哪个玩法的哪一期」—— 那才是这段脚本存在的理由。
 *
 * <h3>⚠️ 目前这两个函数还没有能用的出口</h3>
 * {@code ACTIVITY_PLAY} 的返回值最终要经 {@code ActivityFacade.draw} 交给调用方，
 * 而那个方法今天<b>只接受 {@code DrawResultView}</b>：脚本返回本类的结果会被判成
 * 「挂错了脚本」并抛出。彩票玩法要真正走脚本编排，还差活动域门面那一层的返回契约
 * （要么给 LOTTERY 玩法一条自己的出口，要么把玩法结果抽象成共同的返回类型）。
 * 在那之前，这两个函数可用于在线试跑与后续的批处理场景。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LotteryScriptFunctions implements ScriptFunctionHandler {

    private final TicketIssueService ticketIssueService;

    private final TicketQueryService ticketQueryService;

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.LOTTERY;
    }

    /**
     * 给当前会员发一个号码。
     *
     * <p>标了 {@code sideEffect = true}：引擎保证同一次执行里有副作用的函数最多调一次，
     * 而且这个保证是<b>跨函数</b>的 —— 与抽奖函数合计也只准一次，
     * 不存在「先抽一次奖再领一张票」偷偷多发。
     *
     * <p>🔴 号一旦发出去就收不回来：Redis 游标不随事务回滚（发重号比浪费一个号严重得多），
     * 所以它必须是脚本的最后一步。
     */
    @ScriptFunction(name = "issue", sideEffect = true,
            description = "给当前会员发一个彩票号码，返回 map：lotteryCode/issueNo/ticketNumber/obtainTime。"
                    + "会员号与幂等键由引擎从上下文取，脚本只传玩法编码与期号。"
                    + "⚠️ 有副作用：一次执行只准调一次，且应当是脚本的最后一步。"
                    + "单人限购不在这里挡，要自己先用 lottery_countMine 判")
    public Map<String, Object> issue(ActivityPlayContext play, String lotteryCode, String issueNo) {
        if (lotteryCode == null || lotteryCode.isBlank()) {
            throw new BusinessException("脚本调用彩票领号时没有给出玩法编码");
        }
        if (issueNo == null || issueNo.isBlank()) {
            throw new BusinessException("脚本调用彩票领号时没有给出期号。"
                    + "期号不能省 —— 省了就得由引擎替运营猜「当前是哪一期」，猜错就是发到了别的期上");
        }

        log.info("[彩票-脚本] lotteryCode: {}, issueNo: {}, memberId: {}",
                lotteryCode, issueNo, play.memberId());
        TicketObtainDTO ticket = ticketIssueService.obtain(
                lotteryCode, issueNo, play.memberId(), play.requestId());

        // 返回 Map 而不是 DTO：隔离策略下脚本读不到普通对象的字段（而且不报错，只是拿到 null）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lotteryCode", ticket.lotteryCode());
        result.put("issueNo", ticket.issueNo());
        result.put("ticketNumber", ticket.ticketNumber());
        result.put("obtainTime", ticket.obtainTime());
        // sequenceNo 与 securitySign 是对账与验真要素，不给脚本 —— 脚本会把它们打进日志
        return result;
    }

    /**
     * 当前会员在这一期已经领了几张。限购判据用它。
     *
     * @param issueNo 期号，可以为空表示不限期（整个玩法累计）
     */
    @ScriptFunction(name = "countMine",
            description = "当前会员在某个玩法/某一期已经领了几张号。期号传空串表示不限期。"
                    + "只读，可多次调用。单人限购必须靠它自己判 —— 发号引擎不管限购")
    public Integer countMine(ActivityPlayContext play, String lotteryCode, String issueNo) {
        if (lotteryCode == null || lotteryCode.isBlank()) {
            throw new BusinessException("lottery_countMine 没有给出玩法编码");
        }
        List<LotteryRecord> tickets = ticketQueryService.myTickets(
                lotteryCode, issueNo == null || issueNo.isBlank() ? null : issueNo, play.memberId());
        return tickets == null ? 0 : tickets.size();
    }
}
