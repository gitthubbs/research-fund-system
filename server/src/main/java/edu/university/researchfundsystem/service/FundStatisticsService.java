package edu.university.researchfundsystem.service;

import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import edu.university.researchfundsystem.model.vo.ExecutionRateVO;
import edu.university.researchfundsystem.model.vo.MonthlyTrendVO;
import edu.university.researchfundsystem.model.vo.TrendVO;
import edu.university.researchfundsystem.model.vo.AssistantAdviceVO;

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

    // ★ 新增：看板概览统计（支持按用户隔离）
    ExecutionRateVO getOverview(Long userId);

    // ★ 新增：看板分类占比（支持按用户隔离）
    List<CategoryAmountVO> getCategoryRatios(Long userId);

    // ★ 新增：各项目执行率列表（支持按用户隔离）
    List<ExecutionRateVO> getProjectExecutionRates(Long userId);

    // ★ 新增：各项目执行率列表（全量）
    List<ExecutionRateVO> getProjectExecutionRates();

    // ★ 新增：看板月度趋势（支持按用户隔离）
    MonthlyTrendVO getDashboardMonthlyTrend(Long userId);

    // ★ 新增：刷新全局统计快照
    void refreshGlobalSnapshot();

    // ★ 新增：获取智能助手决策建议
    List<AssistantAdviceVO> getAssistantAdvice(Long userId);

    // ★ 新增：标记建议结果对应的业务记录为已读
    void markAdviceAsRead(Integer businessType, Long businessId);
}