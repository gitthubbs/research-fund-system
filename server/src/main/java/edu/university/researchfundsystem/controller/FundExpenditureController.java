package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.annotation.Log; // ★ 新增
import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.model.vo.ExpenditureListItemVO;
import edu.university.researchfundsystem.common.SecurityUtils;
import edu.university.researchfundsystem.service.FundExpenditureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/expenditures")
@RequiredArgsConstructor
public class FundExpenditureController {

    private final FundExpenditureService expenditureService;
    private final HttpServletRequest request;

    @GetMapping("/list/project/{projectId}")
    public Result<List<ExpenditureListItemVO>> getByProject(@PathVariable Long projectId) {
        return Result.success(expenditureService.listByProjectForView(projectId));
    }

    // ★ 修改：支持 POST /api/expenditures 路径，并调用带校验的服务方法
    @Log("报销申请")
    @PostMapping
    public Result<String> submit(@RequestBody FundExpenditure expenditure) {
        try {
            expenditureService.submitExpenditure(expenditure);
            return Result.success("提交成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ★ 新增：待审核列表
    @GetMapping("/pending")
    public Result<List<ExpenditureListItemVO>> getPending() {
        return Result.success(expenditureService.getPendingList());
    }

    @Log("报销审批") // ★ 新增
    @PutMapping("/audit")
    public Result<String> audit(@RequestBody FundExpenditure auditRequest) {
        if (auditRequest.getId() == null || auditRequest.getStatus() == null) {
            return Result.error("ID和状态不能为空");
        }
        expenditureService.audit(auditRequest.getId(), auditRequest.getStatus(), auditRequest.getAuditRemark());
        return Result.success("审批成功");
    }

    @GetMapping("/search")
    public Result<List<ExpenditureListItemVO>> search(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(expenditureService.searchExpenditures(projectId, categoryId, startDate, endDate));
    }

    @Log("报销申请")
    @PostMapping("/create")
    public Result<Long> save(@RequestBody FundExpenditure expenditure) {
        // 保留原有接口以兼容，但建议调用新逻辑
        expenditureService.submitExpenditure(expenditure);
        return Result.success(expenditure.getId());
    }

    @Log("撤销/删除报销申请")
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(expenditureService.removeById(id));
    }

    @GetMapping("/alerts")
    public Result<List<ExpenditureListItemVO>> getAlerts() {
        Long userId = SecurityUtils.getCurrentUserId(request);
        return Result.success(expenditureService.findUserAlerts(userId));
    }

    @PutMapping("/{id}/read")
    public Result<String> read(@PathVariable Long id) {
        expenditureService.markAsRead(id);
        return Result.success("已标记为已读");
    }
}