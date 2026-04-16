package edu.university.researchfundsystem.task;

import edu.university.researchfundsystem.service.FundStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计数据定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledStatisticsTask {

    private final FundStatisticsService statisticsService;

    /**
     * 每小时刷新一次全局统计快照
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * *")
    public void refreshStatistics() {
        log.info("定时任务开始：刷新全局统计快照");
        try {
            statisticsService.refreshGlobalSnapshot();
        } catch (Exception e) {
            log.error("定时刷新统计快照失败", e);
        }
    }
}
