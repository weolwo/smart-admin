package net.lab1024.sa.marketing.task.template.service;

import java.util.List;
import net.lab1024.sa.marketing.task.template.dao.TaskTemplateDao;
import net.lab1024.sa.marketing.task.template.domain.entity.TaskTemplate;
import net.lab1024.sa.marketing.task.template.domain.form.TaskTemplateAddForm;
import net.lab1024.sa.marketing.task.template.domain.form.TaskTemplateQueryForm;
import net.lab1024.sa.marketing.task.template.domain.form.TaskTemplateUpdateForm;
import net.lab1024.sa.marketing.task.template.domain.vo.TaskTemplateVO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * 系统级-任务模板表 Service
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskTemplateService {

    private final TaskTemplateDao taskTemplateDao;

    /**
     * 分页查询
     */
    public PageResult<TaskTemplateVO> queryPage(TaskTemplateQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<TaskTemplateVO> list = taskTemplateDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ResponseDTO<String> add(TaskTemplateAddForm addForm) {
        TaskTemplate taskTemplate = SmartBeanUtil.copy(addForm, TaskTemplate.class);
        taskTemplateDao.insert(taskTemplate);
        return ResponseDTO.ok();
    }

    /**
     * 更新
     *
     */
    public ResponseDTO<String> update(TaskTemplateUpdateForm updateForm) {
        TaskTemplate taskTemplate = SmartBeanUtil.copy(updateForm, TaskTemplate.class);
        taskTemplateDao.updateById(taskTemplate);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除
     */
    public ResponseDTO<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return ResponseDTO.ok();
        }

        taskTemplateDao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    /**
     * 单个删除
     */
    public ResponseDTO<String> delete(Long id) {
        if (null == id){
            return ResponseDTO.ok();
        }

        taskTemplateDao.deleteById(id);
        return ResponseDTO.ok();
    }
}
