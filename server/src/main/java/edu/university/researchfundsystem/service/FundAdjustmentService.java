package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.FundAdjustment;

import java.util.List;

public interface FundAdjustmentService extends IService<FundAdjustment> {
    
    /**
     * 提交申请
     */
    void apply(FundAdjustment adjustment);

    /**
     * 获取待审批列表
     */
    List<FundAdjustment> listPending();

    /**
     * 审批处理
     */
    void audit(Long id, Integer status, String auditRemark);
}
