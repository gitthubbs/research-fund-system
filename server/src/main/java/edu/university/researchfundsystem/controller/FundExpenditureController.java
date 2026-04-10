package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.model.vo.ExpenditureListItemVO;
import edu.university.researchfundsystem.service.FundExpenditureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenditures")
@RequiredArgsConstructor
public class FundExpenditureController {

    private final FundExpenditureService expenditureService;

    @GetMapping("/list/project/{projectId}")
    public Result<List<ExpenditureListItemVO>> getByProject(@PathVariable Long projectId) {
        return Result.success(expenditureService.listByProjectForView(projectId));
    }

    @PostMapping("/create")
    public Result<Long> save(@RequestBody FundExpenditure expenditure) {
        boolean success = expenditureService.save(expenditure);
        if (success) {
            return Result.success(expenditure.getId());
        }
        return Result.error("支出创建失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(expenditureService.removeById(id));
    }
}