package net.lab1024.sa.marketing.task.config.manager;

import net.lab1024.sa.marketing.task.config.domain.entity.TaskConfig;
import net.lab1024.sa.marketing.task.config.dao.TaskConfigDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 业务级-任务规则配置表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 16:51:54
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskConfigManager extends ServiceImpl<TaskConfigDao, TaskConfig> {


}
