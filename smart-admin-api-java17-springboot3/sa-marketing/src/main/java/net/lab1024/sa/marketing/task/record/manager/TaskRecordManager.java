package net.lab1024.sa.marketing.task.record.manager;

import net.lab1024.sa.marketing.task.record.domain.entity.TaskRecord;
import net.lab1024.sa.marketing.task.record.dao.TaskRecordDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 运行时-会员任务进度实例表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 17:03:33
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskRecordManager extends ServiceImpl<TaskRecordDao, TaskRecord> {


}
