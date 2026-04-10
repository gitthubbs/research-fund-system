package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.entity.FundStatisticsSnapshot;
import edu.university.researchfundsystem.mapper.FundBudgetMapper;
import edu.university.researchfundsystem.mapper.FundExpenditureMapper;
import edu.university.researchfundsystem.mapper.FundStatisticsSnapshotMapper;
import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import edu.university.researchfundsystem.model.vo.ExecutionRateVO;
import edu.university.researchfundsystem.model.vo.TrendVO;
import edu.university.researchfundsystem.service.FundStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundStatisticsServiceImpl implements FundStatisticsService {

    private final FundExpenditureMapper expenditureMapper;
    private final FundBudgetMapper budgetMapper;
    private final FundStatisticsSnapshotMapper snapshotMapper;

    @Override
    public ExecutionRateVO getExecutionRate(Long projectId) {
        // 1. 计算总预算
        BigDecimal totalBudget = budgetMapper.sumBudgetByProject(projectId);
        if (totalBudget == null)
            totalBudget = BigDecimal.ZERO;

        // 2. 计算总支出
        LambdaQueryWrapper<FundExpenditure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FundExpenditure::getProjectId, projectId);
        List<FundExpenditure> expenditures = expenditureMapper.selectList(queryWrapper);
        BigDecimal totalSpent = expenditures.stream()
                .map(FundExpenditure::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 计算执行率
        BigDecimal rate = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            rate = totalSpent.divide(totalBudget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        ExecutionRateVO vo = new ExecutionRateVO();
        vo.setTotalBudget(totalBudget);
        vo.setTotalSpent(totalSpent);
        vo.setRate(rate);
        return vo;
    }

    @Override
    public List<CategoryAmountVO> getExpenditureByCategory(Long projectId) {
        return expenditureMapper.selectExpenditureByCategory(projectId);
    }

    @Override
    public List<TrendVO> getMonthlyTrend(Long projectId) {
        return expenditureMapper.selectMonthlyTrend(projectId);
    }

    @Override
    public List<CategoryAmountVO> getBudgetByCategory(Long projectId) {
        return budgetMapper.selectBudgetByCategory(projectId);
    }

    @Override
    public List<CategoryAmountVO> getExpenditureByCategory() {
        // 获取所有项目的支出分类统计
        return expenditureMapper.selectExpenditureByCategory(null);
    }

    @Override
    public List<TrendVO> getMonthlyTrend() {
        // 获取所有项目的支出趋势
        return expenditureMapper.selectMonthlyTrend(null);
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
}