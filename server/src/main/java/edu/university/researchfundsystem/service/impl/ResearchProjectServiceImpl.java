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
import java.time.LocalDateTime;
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
            item.setUpdateTime(project.getUpdateTime());
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
        
        // 设置状态及审核信息
        detail.setStatus(project.getStatus());
        detail.setStatusText(calculateStatus(project));
        detail.setAuditRemark(project.getAuditRemark());

        return detail;
    }

    @Override
    public boolean submitProject(Long id) {
        ResearchProject project = this.getById(id);
        if (project == null) {
            return false;
        }
        // 只有草稿(0)或已驳回(3)状态可以提交
        if (project.getStatus() != null && project.getStatus() != 0 && project.getStatus() != 3) {
            return false;
        }
        project.setStatus(1); // 变为待审核
        project.setUpdateTime(LocalDateTime.now());
        return this.updateById(project);
    }

    @Override
    public boolean auditProject(Long id, Integer status, String remark) {
        ResearchProject project = this.getById(id);
        if (project == null) {
            return false;
        }
        // 只有待审核(1)状态可以进行审核
        if (project.getStatus() == null || project.getStatus() != 1) {
            return false;
        }
        // 如果是审核通过(原本传2)，现在对应状态 2 (待编制)
        // 如果是驳回(原本传3)，依然对应状态 3
        project.setStatus(status);
        project.setAuditRemark(remark);
        project.setUpdateTime(LocalDateTime.now());
        return this.updateById(project);
    }

    @Override
    public boolean confirmBudget(Long id) {
        ResearchProject project = this.getById(id);
        if (project == null) {
            return false;
        }
        // 只有“待编制预算(2)”状态可以确认并启动项目
        if (project.getStatus() == null || project.getStatus() != 2) {
            return false;
        }

        // 强校验：分项额度之和必须等于项目总额
        LambdaQueryWrapper<FundBudget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(FundBudget::getProjectId, id);
        List<FundBudget> budgets = budgetMapper.selectList(budgetWrapper);
        BigDecimal allocatedTotal = budgets.stream()
                .map(FundBudget::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (project.getTotalBudget() == null || allocatedTotal.compareTo(project.getTotalBudget()) != 0) {
            throw new RuntimeException("预算分配不完整：项目总额为 " + project.getTotalBudget() + "，当前已分配 " + allocatedTotal);
        }

        project.setStatus(4); // 4 为执行中
        project.setUpdateTime(LocalDateTime.now());
        return this.updateById(project);
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
        Integer status = project.getStatus();
        if (status == null || status == 0) {
            return "未提交";
        }
        if (status == 1) {
            return "待审核";
        }
        if (status == 2) {
            return "待编制预算";
        }
        if (status == 3) {
            return "已驳回";
        }
        if (status == 4) {
            return "执行中";
        }
        if (status == 5) {
            return "已结题";
        }

        return "未知状态";
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