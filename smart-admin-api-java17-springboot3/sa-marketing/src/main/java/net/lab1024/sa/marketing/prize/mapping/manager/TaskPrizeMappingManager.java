package net.lab1024.sa.marketing.prize.mapping.manager;

import net.lab1024.sa.marketing.prize.mapping.dao.TaskPrizeMappingDao;
import net.lab1024.sa.marketing.prize.mapping.domain.entity.TaskPrizeMapping;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 业务级-任务阶段与奖励包映射表  Manager
 *
 * @Author weolwo
 * @Date 2026-04-03 17:01:05
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class TaskPrizeMappingManager extends ServiceImpl<TaskPrizeMappingDao, TaskPrizeMapping> {


}
