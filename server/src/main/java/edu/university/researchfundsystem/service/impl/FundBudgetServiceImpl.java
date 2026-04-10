package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.FundBudget;
import edu.university.researchfundsystem.entity.FundCategory;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.mapper.FundBudgetMapper;
import edu.university.researchfundsystem.mapper.FundExpenditureMapper;
import edu.university.researchfundsystem.model.vo.BudgetListItemVO;
import edu.university.researchfundsystem.service.FundBudgetService;
import edu.university.researchfundsystem.service.FundCategoryService;
import edu.university.researchfundsystem.service.ResearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundBudgetServiceImpl extends ServiceImpl<FundBudgetMapper, FundBudget> implements FundBudgetService {

    private final FundExpenditureMapper expenditureMapper;
    private final FundCategoryService categoryService;
    private final ResearchProjectService projectService;

    @Override
    public List<BudgetListItemVO> listByProjectForView(Long projectId) {
        LambdaQueryWrapper<FundBudget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(FundBudget::getProjectId, projectId);
        List<FundBudget> budgets = this.list(budgetWrapper);

        LambdaQueryWrapper<FundExpenditure> expenditureWrapper = new LambdaQueryWrapper<>();
        expenditureWrapper.eq(FundExpenditure::getProjectId, projectId);
        List<FundExpenditure> expenditures = expenditureMapper.selectList(expenditureWrapper);

        ResearchProject project = projectService.getById(projectId);
        String projectName = project != null ? project.getProjectName() : "未知项目";

        return budgets.stream().map(budget -> {
            BudgetListItemVO item = new BudgetListItemVO();
            item.setId(budget.getId());
            item.setProjectId(budget.getProjectId());
            item.setProjectName(projectName);
            item.setCategoryId(budget.getCategoryId());

            FundCategory category = categoryService.getById(budget.getCategoryId());
            item.setCategoryName(category != null ? category.getCategoryName() : "未知类别");
            item.setBudgetAmount(budget.getBudgetAmount());

            BigDecimal spentAmount = expenditures.stream()
                    .filter(expenditure -> budget.getCategoryId() != null && budget.getCategoryId().equals(expenditure.getCategoryId()))
                    .map(FundExpenditure::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setSpentAmount(spentAmount);

            BigDecimal budgetAmount = budget.getBudgetAmount() == null ? BigDecimal.ZERO : budget.getBudgetAmount();
            double executionRate = 0D;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                executionRate = spentAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }
            item.setExecutionRate(executionRate);
            return item;
        }).collect(Collectors.toList());
    }
}