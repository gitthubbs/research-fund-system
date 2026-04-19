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

    // ★ 新增：全局风险摘要
    @GetMapping("/global-risk")
    public Result<GlobalRiskSummaryVO> getGlobalRiskSummary() {
        return Result.success(statisticsService.getGlobalRiskSummary());
    }

    // ★ 新增：看板概览
    @GetMapping("/overview")
    public Result<ExecutionRateVO> getOverview(@RequestParam(required = false) Long researcherId) {
        return Result.success(statisticsService.getOverview(getTargetUserId(researcherId)));
    }

    // ★ 新增：看板分类占比
    @GetMapping("/category-ratios")
    public Result<List<CategoryAmountVO>> getCategoryRatios(@RequestParam(required = false) Long researcherId) {
        return Result.success(statisticsService.getCategoryRatios(getTargetUserId(researcherId)));
    }

    // ★ 新增：看板月度趋势
    @GetMapping("/monthly-trend")
    public Result<MonthlyTrendVO> getDashboardMonthlyTrend(@RequestParam(required = false) Long researcherId) {
        return Result.success(statisticsService.getDashboardMonthlyTrend(getTargetUserId(researcherId)));
    }

    // ★ 新增：各项目执行率
    @GetMapping("/project-execution")
    public Result<List<ExecutionRateVO>> getProjectExecutionRates(@RequestParam(required = false) Long researcherId) {
        return Result.success(statisticsService.getProjectExecutionRates(getTargetUserId(researcherId)));
    }

    // ★ 新增：AI 助手建议
    @GetMapping("/assistant-advice")
    public Result<List<AssistantAdviceVO>> getAssistantAdvice(@RequestParam(required = false) Long researcherId) {
        return Result.success(statisticsService.getAssistantAdvice(getTargetUserId(researcherId)));
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
     * 解析目标用户 ID。
     * 只有管理员可以指定 researcherId；
     * 科研人员强制只能看自己；
     * 管理员未指定时返回 null（即看全局视图）。
     */
    private Long getTargetUserId(Long researcherId) {
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        if (currentUserId == null) return null;
        SysUser user = userService.getById(currentUserId);
        if (user == null) return null;

        // 若是管理员且指定了 ID
        if ("admin".equals(user.getRole()) && researcherId != null) {
            return researcherId;
        }
        // 若是科研人员，强制看自己
        if ("researcher".equals(user.getRole())) {
            return currentUserId;
        }
        // 管理员默认看全局
        return null;
    }

    /**
     * 获取过滤 ID（兼容老逻辑，建议逐步弃用切换到 getTargetUserId）
     */
    private Long getFilteredUserId() {
        return getTargetUserId(null);
    }
}