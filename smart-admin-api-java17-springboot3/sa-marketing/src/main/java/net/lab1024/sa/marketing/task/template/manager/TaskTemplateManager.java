package net.lab1024.sa.marketing.task.template.manager;

import net.lab1024.sa.marketing.task.template.domain.entity.TaskTemplate;
import net.lab1024.sa.marketing.task.template.dao.TaskTemplateDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 系统级-任务模板表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 17:07:43
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskTemplateManager extends ServiceImpl<TaskTemplateDao, TaskTemplate> {


}
