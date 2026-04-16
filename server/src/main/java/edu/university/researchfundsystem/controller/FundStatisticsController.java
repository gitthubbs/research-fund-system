package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.model.vo.*;
import edu.university.researchfundsystem.service.FundStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import edu.university.researchfundsystem.common.SecurityUtils;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.service.SysUserService;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class FundStatisticsController {

    private final FundStatisticsService statisticsService;
    private final SysUserService userService;
    private final HttpServletRequest request;

    @GetMapping("/execution-rate/{projectId}")
    public Result<ExecutionRateVO> getExecutionRate(@PathVariable Long projectId) {
        return Result.success(statisticsService.getExecutionRate(projectId));
    }

    @GetMapping("/expenditure/category/{projectId}")
    public Result<List<CategoryAmountVO>> getExpenditureByCategory(@PathVariable Long projectId) {
        return Result.success(statisticsService.getExpenditureByCategory(projectId));
    }

    @GetMapping("/expenditure/trend/{projectId}")
    public Result<List<TrendVO>> getMonthlyTrend(@PathVariable Long projectId) {
        return Result.success(statisticsService.getMonthlyTrend(projectId));
    }

    @GetMapping("/budget/category/{projectId}")
    public Result<List<CategoryAmountVO>> getBudgetByCategory(@PathVariable Long projectId) {
        return Result.success(statisticsService.getBudgetByCategory(projectId));
    }

    @PostMapping("/snapshot/create/{projectId}")
    public Result<String> saveSnapshot(@PathVariable Long projectId) {
        if (projectId == 0) {
            statisticsService.refreshGlobalSnapshot();
        } else {
            statisticsService.saveSnapshot(projectId);
        }
        return Result.success("Snapshot saved successfully");
    }

    // ★ 新增：看板概览
    @GetMapping("/overview")
    public Result<ExecutionRateVO> getOverview() {
        return Result.success(statisticsService.getOverview(getFilteredUserId()));
    }

    // ★ 新增：看板分类占比
    @GetMapping("/category-ratios")
    public Result<List<CategoryAmountVO>> getCategoryRatios() {
        return Result.success(statisticsService.getCategoryRatios(getFilteredUserId()));
    }

    // ★ 新增：看板月度趋势
    @GetMapping("/monthly-trend")
    public Result<MonthlyTrendVO> getMonthlyTrend() {
        return Result.success(statisticsService.getDashboardMonthlyTrend(getFilteredUserId()));
    }

    // ★ 新增：各项目执行率
    @GetMapping("/project-execution")
    public Result<List<ExecutionRateVO>> getProjectExecutionRates() {
        return Result.success(statisticsService.getProjectExecutionRates(getFilteredUserId()));
    }

    // ★ 新增：AI 助手建议
    @GetMapping("/assistant-advice")
    public Result<List<AssistantAdviceVO>> getAssistantAdvice() {
        Long userId = SecurityUtils.getCurrentUserId(request);
        return Result.success(statisticsService.getAssistantAdvice(userId));
    }

    // ★ 新增：标记建议已读
    @PostMapping("/mark-advice-read")
    public Result<Void> markAdviceRead(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        Integer businessType = (Integer) params.get("businessType");
        statisticsService.markAdviceAsRead(businessType, userId);
        return Result.success();
    }

    /**
     * 根据角色获取过滤 ID：若是科研人员则返回其 ID，若是管理员则返回 null（看全局）
     */
    private Long getFilteredUserId() {
        Long userId = SecurityUtils.getCurrentUserId(request);
        if (userId == null) return null;
        SysUser user = userService.getById(userId);
        if (user != null && "researcher".equals(user.getRole())) {
            return userId;
        }
        return null;
    }
}