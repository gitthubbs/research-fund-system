package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.annotation.Log; // ★ 新增
import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.FundAdjustment;
import edu.university.researchfundsystem.service.FundAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adjustments")
@RequiredArgsConstructor
public class FundAdjustmentController {

    private final FundAdjustmentService adjustmentService;

    @Log("预算调剂申请") // ★ 新增
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody FundAdjustment adjustment) {
        try {
            adjustmentService.apply(adjustment);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/pending")
    public Result<List<FundAdjustment>> getPending() {
        return Result.success(adjustmentService.listPending());
    }

    @Log("预算调剂审批") // ★ 新增
    @PostMapping("/audit")
    public Result<Void> audit(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String auditRemark = (String) params.get("auditRemark");
        
        try {
            adjustmentService.audit(id, status, auditRemark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
