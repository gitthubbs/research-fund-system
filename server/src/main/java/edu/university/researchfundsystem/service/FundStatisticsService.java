package edu.university.researchfundsystem.service;

import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import edu.university.researchfundsystem.model.vo.ExecutionRateVO;
import edu.university.researchfundsystem.model.vo.TrendVO;

import java.util.List;

public interface FundStatisticsService {

    /**
     * 获取项目经费执行率
     */
    ExecutionRateVO getExecutionRate(Long projectId);

    /**
     * 获取项目支出分类统计
     */
    List<CategoryAmountVO> getExpenditureByCategory(Long projectId);

    /**
     * 获取项目预算结构统计
     */
    List<CategoryAmountVO> getBudgetByCategory(Long projectId);

    /**
     * 获取项目支出趋势（按月）
     */
    List<TrendVO> getMonthlyTrend(Long projectId);

    /**
     * 获取所有项目的支出分类统计
     */
    List<CategoryAmountVO> getExpenditureByCategory();

    /**
     * 获取所有项目的支出趋势（按月）
     */
    List<TrendVO> getMonthlyTrend();

    /**
     * 手动触发并保存统计快照
     */
    void saveSnapshot(Long projectId);
}