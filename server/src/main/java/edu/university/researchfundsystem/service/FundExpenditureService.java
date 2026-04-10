package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.model.vo.ExpenditureListItemVO;

import java.util.List;

public interface FundExpenditureService extends IService<FundExpenditure> {
    List<ExpenditureListItemVO> listByProjectForView(Long projectId);
}