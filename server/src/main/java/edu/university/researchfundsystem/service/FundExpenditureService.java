package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.model.vo.ExpenditureListItemVO;

import java.util.List;

public interface FundExpenditureService extends IService<FundExpenditure> {
    List<ExpenditureListItemVO> listByProjectForView(Long projectId);

    // ★ 新增
    void submitExpenditure(FundExpenditure expenditure);

    // ★ 新增
    List<ExpenditureListItemVO> getPendingList();

    // ★ 修改：增加审核理由参数
    void audit(Long id, Integer status, String auditRemark);

    // ★ 新增：支出监控全量搜索
    List<ExpenditureListItemVO> searchExpenditures(Long projectId, Long categoryId, String startDate, String endDate);
}