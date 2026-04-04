package net.lab1024.sa.marketing.riskmanager.config.manager;

import net.lab1024.sa.marketing.riskmanager.config.dao.PromotionConfigDao;
import net.lab1024.sa.marketing.riskmanager.config.domain.entity.PromotionConfig;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 资产域-优惠配置(预算与风控兜底)表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PromotionConfigManager extends ServiceImpl<PromotionConfigDao, PromotionConfig> {


}
