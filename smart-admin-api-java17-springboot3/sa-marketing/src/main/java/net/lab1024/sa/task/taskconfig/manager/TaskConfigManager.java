package net.lab1024.sa.task.taskconfig.manager;

import net.lab1024.sa.task.taskconfig.domain.entity.TaskConfig;
import net.lab1024.sa.task.taskconfig.dao.TaskConfigDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 任务配置表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-18 20:55:10
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskConfigManager extends ServiceImpl<TaskConfigDao, TaskConfig> {


}
