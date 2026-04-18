package net.lab1024.sa.activity.activityconfig.manager;

import net.lab1024.sa.activity.activityconfig.dao.ActivityConfigDao;
import net.lab1024.sa.activity.activityconfig.domain.entity.ActivityConfig;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 活动配置  Manager
 *
 * @Author weolwo
 * @Date 2026-04-18 19:31:49
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class ActivityConfigManager extends ServiceImpl<ActivityConfigDao, ActivityConfig> {


}
