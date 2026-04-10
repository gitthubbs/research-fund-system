package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.FundBudget;
import edu.university.researchfundsystem.model.vo.BudgetListItemVO;
import edu.university.researchfundsystem.service.FundBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class FundBudgetController {

    private final FundBudgetService budgetService;

    @GetMapping("/list/project/{projectId}")
    public Result<List<BudgetListItemVO>> getByProject(@PathVariable Long projectId) {
        return Result.success(budgetService.listByProjectForView(projectId));
    }

    @PostMapping("/create")
    public Result<Long> save(@RequestBody FundBudget budget) {
        boolean success = budgetService.save(budget);
        if (success) {
            return Result.success(budget.getId());
        }
        return Result.error("预算创建失败");
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody FundBudget budget) {
        if (budget.getId() == null) {
            return Result.error("预算ID不能为空");
        }
        return Result.success(budgetService.updateById(budget));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(budgetService.removeById(id));
    }
}