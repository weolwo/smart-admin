package net.lab1024.sa.marketing.riskmanager.proposal.service;

import java.util.List;
import net.lab1024.sa.marketing.riskmanager.proposal.dao.PromotionProposalDao;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.entity.PromotionProposal;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalAddForm;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalQueryForm;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.form.PromotionProposalUpdateForm;
import net.lab1024.sa.marketing.riskmanager.proposal.domain.vo.PromotionProposalVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 资产域-统一发奖提案控制表 Service
 *
 * @Author weolwo
 * @Date 2026-04-03 18:46:32
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PromotionProposalService {

    private final PromotionProposalDao promotionProposalDao;

    /**
     * 分页查询
     */
    public PageResult<PromotionProposalVO> queryPage(PromotionProposalQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PromotionProposalVO> list = promotionProposalDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(PromotionProposalAddForm addForm) {
        PromotionProposal promotionProposal = SmartBeanUtil.copy(addForm, PromotionProposal.class);
        promotionProposalDao.insert(promotionProposal);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(PromotionProposalUpdateForm updateForm) {
        PromotionProposal promotionProposal = SmartBeanUtil.copy(updateForm, PromotionProposal.class);
        promotionProposalDao.updateById(promotionProposal);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        promotionProposalDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        promotionProposalDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
