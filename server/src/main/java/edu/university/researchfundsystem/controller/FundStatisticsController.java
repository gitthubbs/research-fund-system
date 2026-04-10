package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import edu.university.researchfundsystem.model.vo.ExecutionRateVO;
import edu.university.researchfundsystem.model.vo.TrendVO;
import edu.university.researchfundsystem.service.FundStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class FundStatisticsController {

    private final FundStatisticsService statisticsService;

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
        statisticsService.saveSnapshot(projectId);
        return Result.success("Snapshot saved successfully");
    }
}