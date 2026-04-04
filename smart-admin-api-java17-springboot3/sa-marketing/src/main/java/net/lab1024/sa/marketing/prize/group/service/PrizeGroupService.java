package net.lab1024.sa.marketing.prize.group.service;

import java.util.List;
import net.lab1024.sa.marketing.prize.group.dao.PrizeGroupDao;
import net.lab1024.sa.marketing.prize.group.domain.entity.PrizeGroup;
import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupAddForm;
import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupQueryForm;
import net.lab1024.sa.marketing.prize.group.domain.form.PrizeGroupUpdateForm;
import net.lab1024.sa.marketing.prize.group.domain.vo.PrizeGroupVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 业务级-奖励包(大礼包)外壳表 Service
 *
 * @Author weolwo
 * @Date 2026-04-03 18:41:40
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class PrizeGroupService {

    private final PrizeGroupDao prizeGroupDao;

    /**
     * 分页查询
     */
    public PageResult<PrizeGroupVO> queryPage(PrizeGroupQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<PrizeGroupVO> list = prizeGroupDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(PrizeGroupAddForm addForm) {
        PrizeGroup prizeGroup = SmartBeanUtil.copy(addForm, PrizeGroup.class);
        prizeGroupDao.insert(prizeGroup);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(PrizeGroupUpdateForm updateForm) {
        PrizeGroup prizeGroup = SmartBeanUtil.copy(updateForm, PrizeGroup.class);
        prizeGroupDao.updateById(prizeGroup);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        prizeGroupDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(String id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        prizeGroupDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
