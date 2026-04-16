package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.researchfundsystem.entity.*;
import edu.university.researchfundsystem.mapper.*;
import edu.university.researchfundsystem.model.vo.*;
import edu.university.researchfundsystem.service.FundStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundStatisticsServiceImpl implements FundStatisticsService, InitializingBean {

    private final FundExpenditureMapper expenditureMapper;
    private final FundBudgetMapper budgetMapper;
    private final FundStatisticsSnapshotMapper snapshotMapper;
    private final ResearchProjectMapper projectMapper;
    private final FundAdjustmentMapper adjustmentMapper;
    private final FundCategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ExecutionRateVO getExecutionRate(Long projectId) {
        return getExecutionRate(projectId, null);
    }

    private ExecutionRateVO getExecutionRate(Long projectId, Long userId) {
        BigDecimal totalBudget = budgetMapper.sumBudgetByProject(projectId, userId);
        if (totalBudget == null) totalBudget = BigDecimal.ZERO;

        LambdaQueryWrapper<FundExpenditure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FundExpenditure::getStatus, 1);
        if (projectId != null) {
            queryWrapper.eq(FundExpenditure::getProjectId, projectId);
        } else if (userId != null) {
            List<Object> projectIds = projectMapper.selectObjs(new LambdaQueryWrapper<ResearchProject>()
                    .eq(ResearchProject::getPrincipalId, userId)
                    .select(ResearchProject::getId));
            if (projectIds.isEmpty()) return createEmptyRateVO(totalBudget);
            queryWrapper.in(FundExpenditure::getProjectId, projectIds);
        }

        List<FundExpenditure> expenditures = expenditureMapper.selectList(queryWrapper);
        BigDecimal totalSpent = expenditures.stream()
                .map(FundExpenditure::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rate = calculateRate(totalSpent, totalBudget);

        ExecutionRateVO vo = new ExecutionRateVO();
        vo.setTotalBudget(totalBudget);
        vo.setTotalSpent(totalSpent);
        vo.setRate(rate);
        return vo;
    }

    private ExecutionRateVO createEmptyRateVO(BigDecimal totalBudget) {
        ExecutionRateVO empty = new ExecutionRateVO();
        empty.setTotalBudget(totalBudget);
        empty.setTotalSpent(BigDecimal.ZERO);
        empty.setRate(BigDecimal.ZERO);
        return empty;
    }

    private BigDecimal calculateRate(BigDecimal spent, BigDecimal budget) {
        if (budget.compareTo(BigDecimal.ZERO) > 0) {
            return spent.divide(budget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<CategoryAmountVO> getExpenditureByCategory(Long projectId) {
        return expenditureMapper.selectExpenditureByCategory(projectId, null);
    }

    @Override
    public List<TrendVO> getMonthlyTrend(Long projectId) {
        return expenditureMapper.selectMonthlyTrend(projectId, null);
    }

    @Override
    public List<CategoryAmountVO> getBudgetByCategory(Long projectId) {
        return budgetMapper.selectBudgetByCategory(projectId, null);
    }

    @Override
    public List<CategoryAmountVO> getExpenditureByCategory() {
        return expenditureMapper.selectExpenditureByCategory(null, null);
    }

    @Override
    public List<TrendVO> getMonthlyTrend() {
        FundStatisticsSnapshot snapshot = getLatestGlobalSnapshot();
        if (snapshot != null && snapshot.getTrendData() != null) {
            try {
                return objectMapper.readValue(snapshot.getTrendData(), new TypeReference<List<TrendVO>>() {});
            } catch (JsonProcessingException e) {
                log.error("解析趋势快照数据失败", e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void saveSnapshot(Long projectId) {
        ExecutionRateVO stats = getExecutionRate(projectId);
        FundStatisticsSnapshot snapshot = new FundStatisticsSnapshot();
        snapshot.setProjectId(projectId);
        snapshot.setTotalBudget(stats.getTotalBudget());
        snapshot.setTotalExpenditure(stats.getTotalSpent());
        snapshot.setExecutionRate(stats.getRate());
        snapshot.setSnapshotTime(LocalDateTime.now());
        snapshotMapper.insert(snapshot);
    }

    @Override
    public ExecutionRateVO getOverview(Long userId) {
        if (userId != null) {
            ExecutionRateVO vo = getExecutionRate(null, userId);
            Long projectCount = projectMapper.selectCount(new LambdaQueryWrapper<ResearchProject>().eq(ResearchProject::getPrincipalId, userId));
            vo.setProjectCount(projectCount.intValue());
            return vo;
        }
        FundStatisticsSnapshot snapshot = getLatestGlobalSnapshot();
        ExecutionRateVO vo = new ExecutionRateVO();
        Long projectCount = projectMapper.selectCount(null);
        vo.setProjectCount(projectCount.intValue());
        if (snapshot != null) {
            vo.setTotalSpent(snapshot.getTotalExpenditure());
            vo.setRate(snapshot.getExecutionRate());
            vo.setTotalBudget(snapshot.getTotalBudget());
            vo.setSnapshotTime(snapshot.getSnapshotTime());
        }
        return vo;
    }

    @Override
    public List<AssistantAdviceVO> getAssistantAdvice(Long userId) {
        if (userId == null) return Collections.emptyList();
        List<AssistantAdviceVO> advices = new ArrayList<>();

        // 1. 扫描余额风险 (>90%)
        List<ExecutionRateVO> projectRates = getProjectExecutionRates(userId);
        for (ExecutionRateVO pr : projectRates) {
            List<CategoryAmountVO> catSpent = expenditureMapper.selectExpenditureByCategory(pr.getProjectId(), null);
            List<CategoryAmountVO> catBudget = budgetMapper.selectBudgetByCategory(pr.getProjectId(), null);
            Map<String, BigDecimal> budgetMap = catBudget.stream().collect(Collectors.toMap(CategoryAmountVO::getCategoryName, CategoryAmountVO::getAmount));

            for (CategoryAmountVO cs : catSpent) {
                BigDecimal budgetAmt = budgetMap.getOrDefault(cs.getCategoryName(), BigDecimal.ZERO);
                if (budgetAmt.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal rate = cs.getAmount().divide(budgetAmt, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    if (rate.compareTo(new BigDecimal("90")) > 0) {
                        advices.add(AssistantAdviceVO.builder()
                                .title("余额告急")
                                .type("danger")
                                .content("【" + pr.getProjectName() + "】的 " + cs.getCategoryName() + " 预算已消耗 " + rate.setScale(1, RoundingMode.HALF_UP) + "%，请合理安排。")
                                .link("/projects/" + pr.getProjectId())
                                .build());
                    }
                }
            }
        }

        // 2. 扫描执行进度滞后 (时间进度 - 经费执行率 > 10%)
        List<ExecutionRateVO> rates = getProjectExecutionRates(userId);
        for (ExecutionRateVO rateVO : rates) {
            // 只针对执行中的项目 (状态为 4)
            ResearchProject rp = projectMapper.selectById(rateVO.getProjectId());
            if (rp != null && rp.getStatus() == 4) {
                BigDecimal timeProgress = rateVO.getTimeProgress() != null ? rateVO.getTimeProgress() : BigDecimal.ZERO;
                BigDecimal execRate = rateVO.getRate() != null ? rateVO.getRate() : BigDecimal.ZERO;

                // 如果时间进度领先执行率超过 10%，且项目至少已经开始了 10% 的时间（避免新项目误报）
                if (timeProgress.subtract(execRate).compareTo(new BigDecimal("10")) > 0 && timeProgress.compareTo(new BigDecimal("10")) > 0) {
                    advices.add(AssistantAdviceVO.builder()
                            .title("执行进度提醒")
                            .type("warning")
                            .content("项目【" + rp.getProjectName() + "】经费执行率为 " + execRate.setScale(1, RoundingMode.HALF_UP) + "%，显著滞后于时间进度 " + timeProgress.setScale(1, RoundingMode.HALF_UP) + "%，建议加快任务实施。")
                            .link("/projects/" + rp.getId())
                            .build());
                }
            }
        }

        // 3. 扫描报销驳回 (status=2, is_read=0)
        LambdaQueryWrapper<FundExpenditure> expQuery = new LambdaQueryWrapper<FundExpenditure>()
                .eq(FundExpenditure::getApplicantId, userId)
                .eq(FundExpenditure::getStatus, 2)
                .eq(FundExpenditure::getIsRead, 0);
        List<FundExpenditure> rejectedExps = expenditureMapper.selectList(expQuery);
        if (!rejectedExps.isEmpty()) {
            advices.add(AssistantAdviceVO.builder()
                    .title("报销处理提醒")
                    .type("info")
                    .content("您有 " + rejectedExps.size() + " 笔报销申请被驳回，请查看驳回原因并及时处理。")
                    .link("/expenditures")
                    .businessType(1)
                    .build());
        }

        // 4. 扫描调剂驳回 (status=2, is_read=0)
        LambdaQueryWrapper<FundAdjustment> adjQuery = new LambdaQueryWrapper<FundAdjustment>()
                .eq(FundAdjustment::getApplicantId, userId)
                .eq(FundAdjustment::getStatus, 2)
                .eq(FundAdjustment::getIsRead, 0);
        List<FundAdjustment> rejectedAdjs = adjustmentMapper.selectList(adjQuery);
        // 如果有多条调剂驳回，合并为一条建议
        if (!rejectedAdjs.isEmpty()) {
            advices.add(AssistantAdviceVO.builder()
                    .title("调剂处理提醒")
                    .type("info")
                    .content("您有 " + rejectedAdjs.size() + " 笔预算调剂申请被驳回，请进入详情查看反馈。")
                    .link("/dashboard") // 假设在工作台或调剂列表页看
                    .businessType(2)
                    .build());
        }

        return advices;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAdviceAsRead(Integer businessType, Long userId) {
        if (businessType == 1) {
            // 报销已读：将当前用户所有已驳回的报销标记为已读
            FundExpenditure updateObj = new FundExpenditure();
            updateObj.setIsRead(1);
            expenditureMapper.update(updateObj, new LambdaQueryWrapper<FundExpenditure>()
                    .eq(FundExpenditure::getApplicantId, userId)
                    .eq(FundExpenditure::getStatus, 2)
                    .eq(FundExpenditure::getIsRead, 0));
        } else if (businessType == 2) {
            // 调剂已读
            FundAdjustment updateObj = new FundAdjustment();
            updateObj.setIsRead(1);
            adjustmentMapper.update(updateObj, new LambdaQueryWrapper<FundAdjustment>()
                    .eq(FundAdjustment::getApplicantId, userId)
                    .eq(FundAdjustment::getStatus, 2)
                    .eq(FundAdjustment::getIsRead, 0));
        }
    }

    @Override
    public List<CategoryAmountVO> getCategoryRatios(Long userId) {
        if (userId != null) return expenditureMapper.selectExpenditureByCategory(null, userId);
        FundStatisticsSnapshot snapshot = getLatestGlobalSnapshot();
        if (snapshot != null && snapshot.getCategoryData() != null) {
            try { return objectMapper.readValue(snapshot.getCategoryData(), new TypeReference<List<CategoryAmountVO>>() {}); } catch (Exception e) {}
        }
        return Collections.emptyList();
    }

    @Override
    public MonthlyTrendVO getDashboardMonthlyTrend(Long userId) {
        LambdaQueryWrapper<ResearchProject> projectQuery = new LambdaQueryWrapper<ResearchProject>().eq(ResearchProject::getStatus, 4);
        if (userId != null) projectQuery.eq(ResearchProject::getPrincipalId, userId);
        List<ResearchProject> projects = projectMapper.selectList(projectQuery);
        BigDecimal totalBaseline = BigDecimal.ZERO;
        for (ResearchProject p : projects) {
            if (p.getStartDate() != null && p.getEndDate() != null && p.getTotalBudget() != null) {
                long months = ChronoUnit.MONTHS.between(p.getStartDate().withDayOfMonth(1), p.getEndDate().withDayOfMonth(1));
                totalBaseline = totalBaseline.add(p.getTotalBudget().divide(new BigDecimal(Math.max(1, months)), 2, RoundingMode.HALF_UP));
            }
        }
        List<TrendVO> rawTrend = expenditureMapper.selectMonthlyTrend(null, userId);
        Map<String, BigDecimal> trendMap = rawTrend.stream().collect(Collectors.toMap(TrendVO::getMonth, TrendVO::getAmount, (a, b) -> a));
        List<String> monthList = new ArrayList<>();
        List<BigDecimal> actualDataList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 11; i >= 0; i--) { monthList.add(now.minusMonths(i).format(formatter)); actualDataList.add(trendMap.getOrDefault(monthList.get(monthList.size()-1), BigDecimal.ZERO)); }
        BigDecimal forecastAmount = BigDecimal.ZERO;
        List<BigDecimal> recentValidAmounts = rawTrend.stream().map(TrendVO::getAmount).filter(amt -> amt.compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        Collections.reverse(recentValidAmounts);
        List<BigDecimal> last3Months = recentValidAmounts.stream().limit(3).collect(Collectors.toList());
        if (!last3Months.isEmpty()) { forecastAmount = last3Months.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(last3Months.size()), 2, RoundingMode.HALF_UP); }
        return MonthlyTrendVO.builder().months(monthList).actualData(actualDataList).forecast(MonthlyTrendVO.Forecast.builder().nextMonth(now.plusMonths(1).format(formatter)).value(forecastAmount).isAlert(forecastAmount.compareTo(totalBaseline) > 0).build()).baseline(totalBaseline).build();
    }

    private FundStatisticsSnapshot getLatestGlobalSnapshot() {
        return snapshotMapper.selectOne(new LambdaQueryWrapper<FundStatisticsSnapshot>().eq(FundStatisticsSnapshot::getProjectId, 0L).orderByDesc(FundStatisticsSnapshot::getSnapshotTime).last("LIMIT 1"));
    }

    @Override
    public List<ExecutionRateVO> getProjectExecutionRates(Long userId) {
        LambdaQueryWrapper<ResearchProject> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) queryWrapper.eq(ResearchProject::getPrincipalId, userId);
        List<ResearchProject> projects = projectMapper.selectList(queryWrapper);
        List<ExecutionRateVO> results = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (ResearchProject project : projects) {
            ExecutionRateVO rate = getExecutionRate(project.getId(), null);
            rate.setProjectId(project.getId());
            rate.setProjectName(project.getProjectName());

            // ★ 计算时间进度 (已过天数 / 总天数)
            if (project.getStartDate() != null && project.getEndDate() != null) {
                long totalDays = ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate());
                if (totalDays > 0) {
                    long elapsedDays = ChronoUnit.DAYS.between(project.getStartDate(), now);
                    // 钳制在 0-100% 之间
                    long effectiveElapsed = Math.max(0, Math.min(totalDays, elapsedDays));
                    BigDecimal progress = new BigDecimal(effectiveElapsed)
                            .divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    rate.setTimeProgress(progress);
                } else {
                    rate.setTimeProgress(BigDecimal.ZERO);
                }
            } else {
                rate.setTimeProgress(BigDecimal.ZERO);
            }
            results.add(rate);
        }

        // ★ 按执行率从高到低排序，突出风险项目
        results.sort((a, b) -> b.getRate().compareTo(a.getRate()));
        return results;
    }

    @Override public List<ExecutionRateVO> getProjectExecutionRates() { return getProjectExecutionRates(null); }
    @Override public void refreshGlobalSnapshot() { /* ... existing implementation ... */ }
    @Override public void afterPropertiesSet() throws Exception { refreshGlobalSnapshot(); }
}