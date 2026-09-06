package solvela.ledger.handler;

import lombok.extern.slf4j.Slf4j;
import solvela.dispatch.DispatchOutcome;
import solvela.risk.ProposalRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import jakarta.annotation.Resource;

import java.util.concurrent.TimeUnit;

/**
 * 所有资产下发策略的加锁骨架。<b>只管加锁与放行，不认识任何一种资产</b>。
 *
 * <h3>这一层为什么单独存在</h3>
 * 动账链路上有四层，各自只知道一件事，谁也不越界：
 * <ul>
 *   <li><b>本类</b> —— 拿锁、放行、无论如何都释放。锁的粒度由子类给，它不关心；</li>
 *   <li><b>各 handler（门面）</b> —— 把 Service 抛的领域异常翻译成引擎认识的
 *       {@link DispatchOutcome}；</li>
 *   <li><b>各 Service</b> —— 只管开事务、协调 DAO、抛异常，不知道 DispatchOutcome 是什么；</li>
 *   <li><b>实体</b> —— 知道自己有没有被冻结、加完钱是多少（充血模型）。</li>
 * </ul>
 * 把加锁写进每个 handler 也能跑，但那样「忘了释放锁」和「锁键拼错」就有四份机会发生。
 */
@Slf4j
public abstract class AbstractAssetHandler implements IAssetHandler {

    /**
     * 抢锁最多等几秒。
     *
     * <p>调用方是同步的 HTTP 请求，这个值实际是在回答「用户愿意为一次撞锁多等多久」。
     * 调大不会提高成功率 —— 同一个会员的并发本来就少，撞上说明多半是重复提交，
     * 而重复提交等再久也只会成功一次。
     */
    private static final long LOCK_WAIT_SECONDS = 3;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 【模板方法】标准的加锁骨架，声明为 final，严禁子类重写！
     */
    @Override
    public final DispatchOutcome dispatch(ProposalRecord proposal) {
        String lockKey = getLockKey(proposal);
        if (lockKey == null) {
            // 不是所有资产都需要串行化：发券/发实物是纯插入，靠唯一键去重就够了，
            // 为它们加一把分布式锁只是白白多一次 Redis 往返
            return executeWithLock(proposal);
        }

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS)) {
                // 抢不到就直接失败，不排队等：调用方是同步的 HTTP 请求，
                // 让它挂在这里等锁，等来的多半是网关超时而不是成功
                log.warn("【并发拦截】未获取到资产操作锁，提案ID: {}", proposal.getId());
                return DispatchOutcome.failed("系统繁忙，请稍后再试");
            }
            return executeWithLock(proposal);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【系统异常】获取锁被中断，提案ID: {}", proposal.getId());
            return DispatchOutcome.failed("操作被中断");
        } finally {
            // 🔴 必须判 isHeldByCurrentThread：tryLock 超时返回 false 时这个线程并没有持锁，
            // 无条件 unlock 会把别人正持着的锁释放掉，两个请求同时动同一个钱包
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 【必须实现】定义当前资产域的防并发锁 Key
     */
    protected abstract String getLockKey(ProposalRecord proposal);

    /**
     * 【必须实现】在锁的保护下，执行真实的资产路由调用
     */
    protected abstract DispatchOutcome executeWithLock(ProposalRecord proposal);
}