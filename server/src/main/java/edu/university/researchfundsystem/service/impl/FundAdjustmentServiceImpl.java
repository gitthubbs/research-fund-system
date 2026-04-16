package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.FundAdjustment;
import edu.university.researchfundsystem.entity.FundBudget;
import edu.university.researchfundsystem.mapper.*;
import edu.university.researchfundsystem.service.FundAdjustmentService;
import edu.university.researchfundsystem.service.FundBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundAdjustmentServiceImpl extends ServiceImpl<FundAdjustmentMapper, FundAdjustment> implements FundAdjustmentService {

    private final FundBudgetService budgetService;
    private final FundBudgetMapper budgetMapper;
    private final ResearchProjectMapper projectMapper;
    private final FundCategoryMapper categoryMapper;
    private final SysUserMapper userMapper;

    @Override
    public void apply(FundAdjustment adjustment) {
        // 1. 校验余额
        BigDecimal available = budgetService.getAvailableBalance(adjustment.getProjectId(), adjustment.getFromCategoryId());
        if (available.compareTo(adjustment.getAmount()) < 0) {
            throw new RuntimeException("调出科目可用余额不足 (当前: " + available + ")");
        }
        
        // 2. 初始化申请
        adjustment.setStatus(0); // 待审核
        this.save(adjustment);
    }

    @Override
    public List<FundAdjustment> listPending() {
        List<FundAdjustment> list = this.list(new LambdaQueryWrapper<FundAdjustment>()
                .eq(FundAdjustment::getStatus, 0)
                .orderByDesc(FundAdjustment::getCreateTime));
        
        // 填充辅助字段用于展示
        return list.stream().peek(item -> {
            item.setProjectName(projectMapper.selectById(item.getProjectId()).getProjectName());
            item.setFromCategoryName(categoryMapper.selectById(item.getFromCategoryId()).getCategoryName());
            item.setToCategoryName(categoryMapper.selectById(item.getToCategoryId()).getCategoryName());
            item.setApplicantName(userMapper.selectById(item.getApplicantId()).getRealName());
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer status, String auditRemark) {
        FundAdjustment adj = this.getById(id);
        if (adj == null || adj.getStatus() != 0) {
            throw new RuntimeException("申请单不存在或已处理");
        }

        adj.setStatus(status);
        adj.setAuditRemark(auditRemark);
        this.updateById(adj);

        // 如果通过，则执行调剂逻辑
        if (status == 1) {
            executeAdjustment(adj);
        }
    }

    private void executeAdjustment(FundAdjustment adj) {
        // 1. 获取来源科目预算
        FundBudget fromBudget = budgetMapper.selectOne(new LambdaQueryWrapper<FundBudget>()
                .eq(FundBudget::getProjectId, adj.getProjectId())
                .eq(FundBudget::getCategoryId, adj.getFromCategoryId()));
        
        // 2. 获取目标科目预算
        FundBudget toBudget = budgetMapper.selectOne(new LambdaQueryWrapper<FundBudget>()
                .eq(FundBudget::getProjectId, adj.getProjectId())
                .eq(FundBudget::getCategoryId, adj.getToCategoryId()));

        if (fromBudget == null || toBudget == null) {
            throw new RuntimeException("预算科目信息异常");
        }

        // 3. 更新数值
        fromBudget.setBudgetAmount(fromBudget.getBudgetAmount().subtract(adj.getAmount()));
        toBudget.setBudgetAmount(toBudget.getBudgetAmount().add(adj.getAmount()));

        budgetMapper.updateById(fromBudget);
        budgetMapper.updateById(toBudget);
    }
}
