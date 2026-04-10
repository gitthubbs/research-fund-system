package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.service.FundStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final FundStatisticsService statisticsService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // 模拟概览数据（实际应用中应从数据库获取真实数据）
        Map<String, Object> overview = new HashMap<>();
        overview.put("projectsCount", 12);
        overview.put("totalBudget", "1,250,000.00");
        overview.put("totalExpenditure", "890,500.00");
        overview.put("executionRate", 71.2);
        
        summary.put("overview", overview);
        summary.put("monthlyTrend", statisticsService.getMonthlyTrend()); // 使用新方法获取所有项目数据
        summary.put("categoryShare", statisticsService.getExpenditureByCategory()); // 使用新方法获取所有项目数据
        
        return Result.success(summary);
    }
}