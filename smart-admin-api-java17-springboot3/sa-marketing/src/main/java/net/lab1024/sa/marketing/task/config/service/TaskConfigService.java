package net.lab1024.sa.marketing.task.config.service;

import java.util.List;
import net.lab1024.sa.marketing.task.config.dao.TaskConfigDao;
import net.lab1024.sa.marketing.task.config.domain.entity.TaskConfig;
import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigAddForm;
import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigQueryForm;
import net.lab1024.sa.marketing.task.config.domain.form.TaskConfigUpdateForm;
import net.lab1024.sa.marketing.task.config.domain.vo.TaskConfigVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 业务级-任务规则配置表 Service
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskConfigService {

    private final TaskConfigDao taskConfigDao;

    /**
     * 分页查询
     */
    public PageResult<TaskConfigVO> queryPage(TaskConfigQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<TaskConfigVO> list = taskConfigDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(TaskConfigAddForm addForm) {
        TaskConfig taskConfig = SmartBeanUtil.copy(addForm, TaskConfig.class);
        taskConfigDao.insert(taskConfig);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(TaskConfigUpdateForm updateForm) {
        TaskConfig taskConfig = SmartBeanUtil.copy(updateForm, TaskConfig.class);
        taskConfigDao.updateById(taskConfig);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        taskConfigDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        taskConfigDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
