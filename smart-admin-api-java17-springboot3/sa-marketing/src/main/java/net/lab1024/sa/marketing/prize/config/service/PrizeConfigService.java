package net.lab1024.sa.marketing.prize.config.service;

import java.util.List;
import net.lab1024.sa.marketing.prize.config.dao.PrizeConfigDao;
import net.lab1024.sa.marketing.prize.config.domain.entity.PrizeConfig;
import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigAddForm;
import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigQueryForm;
import net.lab1024.sa.marketing.prize.config.domain.form.PrizeConfigUpdateForm;
import net.lab1024.sa.marketing.prize.config.domain.vo.PrizeConfigVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 业务级-发奖规则与奖品明细表 Service
 *
 * @Author weolwo
 * @Date 2026-04-03 18:39:36
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PrizeConfigService {

    private final PrizeConfigDao prizeConfigDao;

    /**
     * 分页查询
     */
    public PageResult<PrizeConfigVO> queryPage(PrizeConfigQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PrizeConfigVO> list = prizeConfigDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(PrizeConfigAddForm addForm) {
        PrizeConfig prizeConfig = SmartBeanUtil.copy(addForm, PrizeConfig.class);
        prizeConfigDao.insert(prizeConfig);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(PrizeConfigUpdateForm updateForm) {
        PrizeConfig prizeConfig = SmartBeanUtil.copy(updateForm, PrizeConfig.class);
        prizeConfigDao.updateById(prizeConfig);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        prizeConfigDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        prizeConfigDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
