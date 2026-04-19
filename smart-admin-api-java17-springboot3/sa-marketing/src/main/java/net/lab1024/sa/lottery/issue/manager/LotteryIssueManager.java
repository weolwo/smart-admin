package net.lab1024.sa.lottery.issue.manager;

import net.lab1024.sa.lottery.issue.domain.entity.LotteryIssue;
import net.lab1024.sa.lottery.issue.dao.LotteryIssueDao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * 期号配置  Manager
 *
 * @Author weolwo
 * @Date 2026-04-19 11:23:43
 * @Copyright weolwo
 */
@RequiredArgsConstructor
@Service
public class LotteryIssueManager extends ServiceImpl<LotteryIssueDao, LotteryIssue> {


}
