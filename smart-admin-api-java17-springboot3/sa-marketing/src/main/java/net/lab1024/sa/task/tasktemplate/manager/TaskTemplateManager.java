package net.lab1024.sa.task.tasktemplate.manager;

import net.lab1024.sa.task.tasktemplate.domain.entity.TaskTemplate;
import net.lab1024.sa.task.tasktemplate.dao.TaskTemplateDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 任务模板表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-18 21:12:49
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskTemplateManager extends ServiceImpl<TaskTemplateDao, TaskTemplate> {


}
