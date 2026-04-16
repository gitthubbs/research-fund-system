package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.FundBudget;
import edu.university.researchfundsystem.model.vo.BudgetListItemVO;

import java.math.BigDecimal;
import java.util.List;

public interface FundBudgetService extends IService<FundBudget> {
    List<BudgetListItemVO> listByProjectForView(Long projectId);

    /**
     * 获取可用余额
     */
    BigDecimal getAvailableBalance(Long projectId, Long categoryId);
}