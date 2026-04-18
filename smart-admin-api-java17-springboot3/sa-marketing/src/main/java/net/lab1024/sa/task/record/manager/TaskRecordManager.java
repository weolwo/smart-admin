package net.lab1024.sa.task.record.manager;

import net.lab1024.sa.task.record.domain.entity.TaskRecord;
import net.lab1024.sa.task.record.dao.TaskRecordDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 任务记录表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-18 21:02:56
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskRecordManager extends ServiceImpl<TaskRecordDao, TaskRecord> {


}
