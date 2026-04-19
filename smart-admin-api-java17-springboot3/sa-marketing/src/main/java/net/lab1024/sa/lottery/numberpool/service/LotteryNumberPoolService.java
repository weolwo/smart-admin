package net.lab1024.sa.lottery.numberpool.service;

import java.util.List;
import net.lab1024.sa.lottery.numberpool.dao.LotteryNumberPoolDao;
import net.lab1024.sa.lottery.numberpool.domain.entity.LotteryNumberPool;
import net.lab1024.sa.lottery.numberpool.domain.form.LotteryNumberPoolAddForm;
import net.lab1024.sa.lottery.numberpool.domain.form.LotteryNumberPoolQueryForm;
import net.lab1024.sa.lottery.numberpool.domain.form.LotteryNumberPoolUpdateForm;
import net.lab1024.sa.lottery.numberpool.domain.vo.LotteryNumberPoolVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 彩票号码池 Service
 *
 * @Author weolwo
 * @Date 2026-04-19 11:31:09
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class LotteryNumberPoolService {

    private final LotteryNumberPoolDao lotteryNumberPoolDao;

    /**
     * 分页查询
     */
    public PageResult<LotteryNumberPoolVO> queryPage(LotteryNumberPoolQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<LotteryNumberPoolVO> list = lotteryNumberPoolDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(LotteryNumberPoolAddForm addForm) {
        LotteryNumberPool lotteryNumberPool = SmartBeanUtil.copy(addForm, LotteryNumberPool.class);
        lotteryNumberPoolDao.insert(lotteryNumberPool);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(LotteryNumberPoolUpdateForm updateForm) {
        LotteryNumberPool lotteryNumberPool = SmartBeanUtil.copy(updateForm, LotteryNumberPool.class);
        lotteryNumberPoolDao.updateById(lotteryNumberPool);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        lotteryNumberPoolDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        lotteryNumberPoolDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
