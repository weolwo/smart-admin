package net.lab1024.sa.marketing.riskmanager.config.service;

import java.util.List;
import net.lab1024.sa.marketing.riskmanager.config.dao.PromotionConfigDao;
import net.lab1024.sa.marketing.riskmanager.config.domain.entity.PromotionConfig;
import net.lab1024.sa.marketing.riskmanager.config.domain.form.PromotionConfigAddForm;
import net.lab1024.sa.marketing.riskmanager.config.domain.form.PromotionConfigQueryForm;
import net.lab1024.sa.marketing.riskmanager.config.domain.form.PromotionConfigUpdateForm;
import net.lab1024.sa.marketing.riskmanager.config.domain.vo.PromotionConfigVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 资产域-优惠配置(预算与风控兜底)表 Service
 *
 * @Author weolwo
 * @Date 2026-04-03 18:44:52
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PromotionConfigService {

    private final PromotionConfigDao promotionConfigDao;

    /**
     * 分页查询
     */
    public PageResult<PromotionConfigVO> queryPage(PromotionConfigQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PromotionConfigVO> list = promotionConfigDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(PromotionConfigAddForm addForm) {
        PromotionConfig promotionConfig = SmartBeanUtil.copy(addForm, PromotionConfig.class);
        promotionConfigDao.insert(promotionConfig);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(PromotionConfigUpdateForm updateForm) {
        PromotionConfig promotionConfig = SmartBeanUtil.copy(updateForm, PromotionConfig.class);
        promotionConfigDao.updateById(promotionConfig);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        promotionConfigDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(String id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        promotionConfigDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
