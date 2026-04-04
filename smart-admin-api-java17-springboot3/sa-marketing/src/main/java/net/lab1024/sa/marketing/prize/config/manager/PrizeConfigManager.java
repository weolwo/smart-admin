package net.lab1024.sa.marketing.prize.config.manager;

import net.lab1024.sa.marketing.prize.config.domain.entity.PrizeConfig;
import net.lab1024.sa.marketing.prize.config.dao.PrizeConfigDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 业务级-发奖规则与奖品明细表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PrizeConfigManager extends ServiceImpl<PrizeConfigDao, PrizeConfig> {


}
