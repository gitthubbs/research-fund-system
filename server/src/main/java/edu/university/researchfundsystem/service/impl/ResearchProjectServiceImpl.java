package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.FundBudget;
import edu.university.researchfundsystem.entity.FundCategory;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.mapper.FundBudgetMapper;
import edu.university.researchfundsystem.mapper.FundExpenditureMapper;
import edu.university.researchfundsystem.mapper.ResearchProjectMapper;
import edu.university.researchfundsystem.model.vo.ProjectDetailVO;
import edu.university.researchfundsystem.model.vo.ProjectListItemVO;
import edu.university.researchfundsystem.service.FundCategoryService;
import edu.university.researchfundsystem.service.ResearchProjectService;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResearchProjectServiceImpl extends ServiceImpl<ResearchProjectMapper, ResearchProject>
        implements ResearchProjectService {

    private final FundBudgetMapper budgetMapper;
    private final FundExpenditureMapper expenditureMapper;
    private final FundCategoryService categoryService;
    private final SysUserService userService;

    @Override
    public List<ProjectListItemVO> listForView() {
        List<ResearchProject> projects = this.list();
        Map<Long, String> principalNameMap = buildPrincipalNameMap(projects);
        return projects.stream().map(project -> {
            ProjectListItemVO item = new ProjectListItemVO();
            item.setId(project.getId());
            item.setProjectName(project.getProjectName());
            item.setProjectCode(project.getProjectCode());
            item.setPrincipalId(project.getPrincipalId());
            item.setPrincipalName(principalNameMap.getOrDefault(project.getPrincipalId(), "未知负责人"));
            item.setStartDate(project.getStartDate());
            item.setEndDate(project.getEndDate());
            item.setTotalBudget(project.getTotalBudget());
            item.setStatus(calculateStatus(project));
            item.setPerformance(calculatePerformance(project.getId()));
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public ProjectDetailVO getProjectDetail(Long id) {
        ResearchProject project = this.getById(id);
        if (project == null) {
            return null;
        }

        ProjectDetailVO detail = new ProjectDetailVO();
        detail.setId(project.getId());
        detail.setProjectName(project.getProjectName());
        detail.setProjectCode(project.getProjectCode());
        detail.setPrincipalName(resolvePrincipalName(project.getPrincipalId()));
        detail.setPerformance(calculatePerformance(project.getId()));

        List<ProjectDetailVO.MilestoneVO> milestones = new ArrayList<>();
        if (project.getStartDate() != null) {
            ProjectDetailVO.MilestoneVO milestone = new ProjectDetailVO.MilestoneVO();
            milestone.setStage("立项");
            milestone.setContent("项目正式启动");
            milestone.setDate(project.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            milestones.add(milestone);

            ProjectDetailVO.MilestoneVO middleMilestone = new ProjectDetailVO.MilestoneVO();
            middleMilestone.setStage("中期检查");
            middleMilestone.setContent("项目进展中期评估");
            middleMilestone.setDate(project.getStartDate().plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            milestones.add(middleMilestone);
        }
        if (project.getEndDate() != null) {
            ProjectDetailVO.MilestoneVO milestone = new ProjectDetailVO.MilestoneVO();
            milestone.setStage("结题");
            milestone.setContent("项目完成验收");
            milestone.setDate(project.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            milestones.add(milestone);
        }
        detail.setMilestones(milestones);

        LambdaQueryWrapper<FundBudget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(FundBudget::getProjectId, id);
        List<FundBudget> budgets = budgetMapper.selectList(budgetWrapper);

        LambdaQueryWrapper<FundExpenditure> expenditureWrapper = new LambdaQueryWrapper<>();
        expenditureWrapper.eq(FundExpenditure::getProjectId, id);
        List<FundExpenditure> expenditures = expenditureMapper.selectList(expenditureWrapper);

        List<ProjectDetailVO.BudgetDetailVO> budgetDetails = new ArrayList<>();
        for (FundBudget budget : budgets) {
            ProjectDetailVO.BudgetDetailVO budgetDetail = new ProjectDetailVO.BudgetDetailVO();
            FundCategory category = categoryService.getById(budget.getCategoryId());
            budgetDetail.setCategoryName(category != null ? category.getCategoryName() : "未知类别");
            budgetDetail.setBudgetAmount(budget.getBudgetAmount());

            BigDecimal spentAmount = BigDecimal.ZERO;
            for (FundExpenditure expenditure : expenditures) {
                if (budget.getCategoryId() != null && budget.getCategoryId().equals(expenditure.getCategoryId())) {
                    spentAmount = spentAmount.add(expenditure.getAmount());
                }
            }
            budgetDetail.setSpentAmount(spentAmount);

            BigDecimal executionRate = BigDecimal.ZERO;
            if (budget.getBudgetAmount() != null && budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
                executionRate = spentAmount.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }
            budgetDetail.setExecutionRate(executionRate.doubleValue());
            budgetDetails.add(budgetDetail);
        }
        detail.setBudgets(budgetDetails);

        return detail;
    }

    private Map<Long, String> buildPrincipalNameMap(List<ResearchProject> projects) {
        List<Long> principalIds = projects.stream()
                .map(ResearchProject::getPrincipalId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (principalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(principalIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> displayName(user.getRealName(), user.getUsername())));
    }

    private String resolvePrincipalName(Long principalId) {
        if (principalId == null) {
            return "未知负责人";
        }
        SysUser user = userService.getById(principalId);
        if (user == null) {
            return "未知负责人";
        }
        return displayName(user.getRealName(), user.getUsername());
    }

    private String displayName(String realName, String username) {
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        return "未知用户";
    }

    private String calculateStatus(ResearchProject project) {
        LocalDate today = LocalDate.now();
        if (project.getStartDate() != null && today.isBefore(project.getStartDate())) {
            return "未开始";
        }
        if (project.getEndDate() != null && today.isAfter(project.getEndDate())) {
            return "已结题";
        }
        return "进行中";
    }

    private String calculatePerformance(Long projectId) {
        if (projectId == null) {
            return "中";
        }
        LambdaQueryWrapper<FundBudget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(FundBudget::getProjectId, projectId);
        List<FundBudget> budgets = budgetMapper.selectList(budgetWrapper);
        LambdaQueryWrapper<FundExpenditure> expenditureWrapper = new LambdaQueryWrapper<>();
        expenditureWrapper.eq(FundExpenditure::getProjectId, projectId);
        List<FundExpenditure> expenditures = expenditureMapper.selectList(expenditureWrapper);

        BigDecimal totalBudget = budgets.stream()
                .map(FundBudget::getBudgetAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spentAmount = expenditures.stream()
                .map(FundExpenditure::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return "中";
        }

        BigDecimal rate = spentAmount.multiply(new BigDecimal("100"))
                .divide(totalBudget, 2, RoundingMode.HALF_UP);
        if (rate.compareTo(new BigDecimal("95")) <= 0) {
            return "优";
        }
        if (rate.compareTo(new BigDecimal("105")) <= 0) {
            return "良";
        }
        if (rate.compareTo(new BigDecimal("120")) <= 0) {
            return "中";
        }
        return "差";
    }
}