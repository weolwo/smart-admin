package net.lab1024.sa.marketing.prize.log.manager;

import net.lab1024.sa.marketing.prize.log.domain.entity.PrizeLog;
import net.lab1024.sa.marketing.prize.log.dao.PrizeLogDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 资产域-奖励发放执行明细与快照表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 18:42:42
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PrizeLogManager extends ServiceImpl<PrizeLogDao, PrizeLog> {


}
