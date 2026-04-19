package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.common.SecurityUtils;
import edu.university.researchfundsystem.entity.FundBudget;
import edu.university.researchfundsystem.entity.FundCategory;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.mapper.FundExpenditureMapper;
import edu.university.researchfundsystem.model.vo.ExpenditureListItemVO;
import edu.university.researchfundsystem.service.FundBudgetService; // ★ 新增
import edu.university.researchfundsystem.service.FundCategoryService;
import edu.university.researchfundsystem.service.FundExpenditureService;
import edu.university.researchfundsystem.service.ResearchProjectService;
import edu.university.researchfundsystem.service.SysUserService; // ★ 新增
import edu.university.researchfundsystem.entity.SysUser; // ★ 新增
import edu.university.researchfundsystem.service.FundStatisticsService; // ★ 新增
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ★ 新增
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal; // ★ 新增
import java.time.LocalDate; // ★ 新增
import java.time.LocalDateTime; // ★ 新增
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // ★ 新增
@Service
@RequiredArgsConstructor
public class FundExpenditureServiceImpl extends ServiceImpl<FundExpenditureMapper, FundExpenditure>
        implements FundExpenditureService {

    private final FundCategoryService categoryService;
    private final ResearchProjectService projectService;
    private final FundBudgetService budgetService;
    private final SysUserService sysUserService; // ★ 新增
    private final FundStatisticsService statisticsService; // ★ 新增
    private final HttpServletRequest request;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<ExpenditureListItemVO> listByProjectForView(Long projectId) {
        LambdaQueryWrapper<FundExpenditure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FundExpenditure::getProjectId, projectId)
                .orderByDesc(FundExpenditure::getExpenditureDate);
        List<FundExpenditure> expenditures = this.list(queryWrapper);

        ResearchProject project = projectService.getById(projectId);
        String projectName = project != null ? project.getProjectName() : "未知项目";

        return expenditures.stream().map(expenditure -> {
            ExpenditureListItemVO item = new ExpenditureListItemVO();
            item.setId(expenditure.getId());
            item.setProjectId(expenditure.getProjectId());
            item.setProjectName(projectName);
            item.setCategoryId(expenditure.getCategoryId());

            FundCategory category = categoryService.getById(expenditure.getCategoryId());
            item.setCategoryName(category != null ? category.getCategoryName() : "未知类别");

            item.setAmount(expenditure.getAmount());
            item.setExpenditureDate(expenditure.getExpenditureDate());
            item.setRemark(expenditure.getRemark());
            item.setStatus(expenditure.getStatus()); // ★ 修复：设置状态字段
            item.setAuditRemark(expenditure.getAuditRemark()); // ★ 新增
            item.setIsRead(expenditure.getIsRead()); // ★ 新增集
            if (expenditure.getCreateTime() != null) {
                item.setCreateTime(expenditure.getCreateTime().format(dateTimeFormatter));
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public void submitExpenditure(FundExpenditure expenditure) {
        // ★ 新增逻辑：预算校验
        // 1. 获取科目名称
        FundCategory category = categoryService.getById(expenditure.getCategoryId());
        String categoryName = category != null ? category.getCategoryName() : "未知科目";

        // 2. 查询预算限额
        LambdaQueryWrapper<FundBudget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(FundBudget::getProjectId, expenditure.getProjectId())
                .eq(FundBudget::getCategoryId, expenditure.getCategoryId());
        FundBudget budget = budgetService.getOne(budgetWrapper);

        // 校验科目是否匹配（预算书中是否存在）
        if (budget == null) {
            throw new RuntimeException(String.format("报销失败：项目预算中未包含科目【%s】。\n\n决策建议：请核对预算书确认科目归属，或申请预算调剂以新增该科目预算。", 
                    categoryName));
        }

        BigDecimal budgetLimit = budget.getBudgetAmount();

        // 3. 查询已通过的支出总额 (status = 1)
        LambdaQueryWrapper<FundExpenditure> usedWrapper = new LambdaQueryWrapper<>();
        usedWrapper.eq(FundExpenditure::getProjectId, expenditure.getProjectId())
                .eq(FundExpenditure::getCategoryId, expenditure.getCategoryId())
                .eq(FundExpenditure::getStatus, 1);
        List<FundExpenditure> approvedExpenditures = this.list(usedWrapper);
        BigDecimal usedAmount = approvedExpenditures.stream()
                .map(FundExpenditure::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 计算可用余额
        BigDecimal availableBalance = budgetLimit.subtract(usedAmount);

        // 5. 校验拦截：超支校验
        if (expenditure.getAmount().compareTo(availableBalance) > 0) {
            throw new RuntimeException(String.format("报销失败：科目【%s】余额不足（剩余 %s 元），无法报销 %s 元。\n\n决策建议：请申请预算调剂增加该科目额度，或核对已有报销记录是否准确。",
                    categoryName, availableBalance.toPlainString(), expenditure.getAmount().toPlainString()));
        }

        // 6. 保存记录，初始状态为待审核 (0)
        expenditure.setStatus(0);
        // ★ 新增：设置申请人ID，修复数据库必填项缺失问题
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        expenditure.setApplicantId(currentUserId);
        
        if (expenditure.getCreateTime() == null) {
            expenditure.setCreateTime(LocalDateTime.now());
        }
        this.save(expenditure);
    }

    @Override
    public List<ExpenditureListItemVO> getPendingList() {
        // 1. 查询所有状态为待审核的记录
        LambdaQueryWrapper<FundExpenditure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FundExpenditure::getStatus, 0)
                .orderByDesc(FundExpenditure::getCreateTime);
        List<FundExpenditure> expenditures = this.list(queryWrapper);

        return expenditures.stream().map(expenditure -> {
            ExpenditureListItemVO item = new ExpenditureListItemVO();
            item.setId(expenditure.getId());
            item.setProjectId(expenditure.getProjectId());
            item.setCategoryId(expenditure.getCategoryId());
            item.setAmount(expenditure.getAmount());
            item.setExpenditureDate(expenditure.getExpenditureDate());
            item.setRemark(expenditure.getRemark());
            item.setStatus(expenditure.getStatus());

            // 填充项目名
            ResearchProject project = projectService.getById(expenditure.getProjectId());
            item.setProjectName(project != null ? project.getProjectName() : "未知项目");

            // 填充科目名
            FundCategory category = categoryService.getById(expenditure.getCategoryId());
            item.setCategoryName(category != null ? category.getCategoryName() : "未知科目");

            // 填充申请人姓名
            SysUser applicant = sysUserService.getById(expenditure.getApplicantId());
            item.setApplicantName(applicant != null ? applicant.getRealName() : "未知用户");
            item.setAuditRemark(expenditure.getAuditRemark()); // ★ 新增
            item.setIsRead(expenditure.getIsRead()); // ★ 新增

            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public void audit(Long id, Integer status, String auditRemark) {
        FundExpenditure expenditure = this.getById(id);
        if (expenditure == null) {
            throw new RuntimeException("支出记录不存在");
        }
        expenditure.setStatus(status);
        expenditure.setAuditRemark(auditRemark); // ★ 新增
        expenditure.setAuditTime(LocalDateTime.now()); // ★ 修改：记录审核时间
        
        this.updateById(expenditure);

        // ★ 新增：如果审核通过，刷新全局统计快照
        if (status == 1) {
            try {
                statisticsService.refreshGlobalSnapshot();
            } catch (Exception e) {
                // 快照刷新失败不应影响主业务逻辑
                log.error("自动刷新统计快照失败", e);
            }
        }
    }

    @Override
    public List<ExpenditureListItemVO> searchExpenditures(Long projectId, Long categoryId, String startDate, String endDate) {
        LambdaQueryWrapper<FundExpenditure> queryWrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            queryWrapper.eq(FundExpenditure::getProjectId, projectId);
        }
        if (categoryId != null) {
            queryWrapper.eq(FundExpenditure::getCategoryId, categoryId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge(FundExpenditure::getExpenditureDate, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le(FundExpenditure::getExpenditureDate, endDate);
        }
        
        queryWrapper.orderByDesc(FundExpenditure::getExpenditureDate);
        List<FundExpenditure> list = this.list(queryWrapper);
        
        return list.stream().map(expenditure -> {
            ExpenditureListItemVO vo = new ExpenditureListItemVO();
            vo.setId(expenditure.getId());
            vo.setProjectId(expenditure.getProjectId());
            vo.setCategoryId(expenditure.getCategoryId());
            vo.setAmount(expenditure.getAmount());
            vo.setExpenditureDate(expenditure.getExpenditureDate());
            vo.setRemark(expenditure.getRemark());
            vo.setStatus(expenditure.getStatus());
            
            // 补充项目和科目名称
            ResearchProject project = projectService.getById(expenditure.getProjectId());
            vo.setProjectName(project != null ? project.getProjectName() : "未知项目");
            
            FundCategory category = categoryService.getById(expenditure.getCategoryId());
            vo.setCategoryName(category != null ? category.getCategoryName() : "未知科目");
            
            vo.setAuditRemark(expenditure.getAuditRemark());
            vo.setIsRead(expenditure.getIsRead());
            if (expenditure.getCreateTime() != null) {
                vo.setCreateTime(expenditure.getCreateTime().format(dateTimeFormatter));
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ExpenditureListItemVO> findUserAlerts(Long userId) {
        // 查询该用户申请的、待审核(0)或已驳回未读(2 & is_read=0)的项
        LambdaQueryWrapper<FundExpenditure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundExpenditure::getApplicantId, userId)
                .and(w -> w.eq(FundExpenditure::getStatus, 0)
                        .or(sw -> sw.eq(FundExpenditure::getStatus, 2).eq(FundExpenditure::getIsRead, 0)))
                .orderByDesc(FundExpenditure::getCreateTime);
        
        List<FundExpenditure> list = this.list(wrapper);
        return list.stream().map(e -> {
            ExpenditureListItemVO vo = new ExpenditureListItemVO();
            vo.setId(e.getId());
            vo.setProjectId(e.getProjectId());
            vo.setAmount(e.getAmount());
            vo.setStatus(e.getStatus());
            vo.setAuditRemark(e.getAuditRemark());
            vo.setIsRead(e.getIsRead());
            vo.setRemark(e.getRemark());
            if (e.getCreateTime() != null) {
                vo.setCreateTime(e.getCreateTime().format(dateTimeFormatter));
            }
            
            ResearchProject p = projectService.getById(e.getProjectId());
            vo.setProjectName(p != null ? p.getProjectName() : "未知项目");
            
            FundCategory c = categoryService.getById(e.getCategoryId());
            vo.setCategoryName(c != null ? c.getCategoryName() : "未知科目");
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id) {
        FundExpenditure expenditure = this.getById(id);
        if (expenditure != null) {
            expenditure.setIsRead(1);
            this.updateById(expenditure);
        }
    }
}