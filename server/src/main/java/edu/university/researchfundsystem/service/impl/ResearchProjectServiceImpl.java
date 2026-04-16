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
import edu.university.researchfundsystem.common.SecurityUtils;
import javax.servlet.http.HttpServletRequest;

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
    private final HttpServletRequest request;

    @Override
    public List<ProjectListItemVO> listForView() {
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        SysUser user = currentUserId != null ? userService.getById(currentUserId) : null;
        
        LambdaQueryWrapper<ResearchProject> queryWrapper = new LambdaQueryWrapper<>();
        if (user != null && "researcher".equals(user.getRole())) {
            queryWrapper.eq(ResearchProject::getPrincipalId, currentUserId);
        }
        
        List<ResearchProject> projects = this.list(queryWrapper);
        Map<Long, String> principalNameMap = buildPrincipalNameMap(projects);
        
        // 批量查询所有相关项目的支出总额，避免 N+1 问题
        List<Long> projectIds = projects.stream().map(ResearchProject::getId).collect(Collectors.toList());
        Map<Long, BigDecimal> projectSpentMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            LambdaQueryWrapper<FundExpenditure> expWrapper = new LambdaQueryWrapper<>();
            expWrapper.in(FundExpenditure::getProjectId, projectIds);
            List<FundExpenditure> allExpenditures = expenditureMapper.selectList(expWrapper);
            projectSpentMap = allExpenditures.stream()
                    .collect(Collectors.groupingBy(FundExpenditure::getProjectId,
                            Collectors.reducing(BigDecimal.ZERO, FundExpenditure::getAmount, BigDecimal::add)));
        }

        final Map<Long, BigDecimal> finalProjectSpentMap = projectSpentMap;
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
            
            // 填充支出和执行率
            BigDecimal spentAmount = finalProjectSpentMap.getOrDefault(project.getId(), BigDecimal.ZERO);
            item.setSpentAmount(spentAmount);
            if (project.getTotalBudget() != null && project.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
                item.setExecutionRate(spentAmount.multiply(new BigDecimal("100"))
                        .divide(project.getTotalBudget(), 2, RoundingMode.HALF_UP));
            } else {
                item.setExecutionRate(BigDecimal.ZERO);
            }
            
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

        // ★ 重构：动态生成项目里程碑
        List<ProjectDetailVO.MilestoneVO> milestones = new ArrayList<>();
        Integer status = project.getStatus() != null ? project.getStatus() : 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 1. 项目申报
        ProjectDetailVO.MilestoneVO m1 = new ProjectDetailVO.MilestoneVO();
        m1.setStage("项目申报");
        m1.setContent("完成项目信息录入与申报");
        m1.setDate(project.getCreateTime() != null ? project.getCreateTime().format(dtf) : "-");
        m1.setType(status > 0 ? "success" : "primary");
        milestones.add(m1);

        // 2. 立项审核
        ProjectDetailVO.MilestoneVO m2 = new ProjectDetailVO.MilestoneVO();
        m2.setStage("立项审核");
        if (status == 3) {
            m2.setContent("审核未通过：" + project.getAuditRemark());
            m2.setType("danger");
        } else {
            m2.setContent(status >= 2 ? "审核通过，准予立项" : "等待系统审核中");
            m2.setType(status >= 2 ? "success" : (status == 1 ? "primary" : "info"));
        }
        m2.setDate(status >= 2 ? project.getUpdateTime().format(dtf) : "-");
        milestones.add(m2);

        // 3. 预算核定
        ProjectDetailVO.MilestoneVO m3 = new ProjectDetailVO.MilestoneVO();
        m3.setStage("预算核定");
        m3.setContent(status >= 4 ? "预算编制已确认，项目启动" : "待负责人编制分项预算");
        m3.setType(status >= 4 ? "success" : (status == 2 ? "primary" : "info"));
        m3.setDate(status >= 4 ? project.getUpdateTime().format(dtf) : "-");
        milestones.add(m3);

        // 4. 项目结题
        ProjectDetailVO.MilestoneVO m4 = new ProjectDetailVO.MilestoneVO();
        m4.setStage("结题验收");
        m4.setContent(status == 5 ? "项目已顺利结题" : "项目执行监控中");
        m4.setType(status == 5 ? "success" : (status == 4 ? "primary" : "info"));
        m4.setDate(status == 5 ? project.getEndDate().format(dtf) : (project.getEndDate() != null ? project.getEndDate().format(dtf) : "-"));
        milestones.add(m4);

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
        detail.setStartDate(project.getStartDate()); // ★ 新增
        detail.setEndDate(project.getEndDate());     // ★ 新增

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