package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.annotation.Log;
import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.common.SecurityUtils;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.model.dto.ProjectAuditDTO;
import edu.university.researchfundsystem.model.vo.ProjectDetailVO;
import edu.university.researchfundsystem.model.vo.ProjectListItemVO;
import edu.university.researchfundsystem.service.ResearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ResearchProjectController {

    private final ResearchProjectService projectService;
    private final HttpServletRequest request;

    @Log("项目立项申请")
    @PostMapping("")
    public Result<Long> create(@RequestBody ResearchProject project) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未找到登录用户信息");
        }
        project.setPrincipalId(userId);
        project.setStatus(0); // 默认草稿
        boolean success = projectService.save(project);
        if (success) {
            return Result.success(project.getId());
        }
        return Result.error("项目创建失败");
    }

    @Log("提交项目审核")
    @PutMapping("/submit/{id}")
    public Result<Boolean> submit(@PathVariable Long id) {
        return Result.success(projectService.submitProject(id));
    }

    @Log("项目立项审批")
    @PutMapping("/audit")
    public Result<Boolean> audit(@RequestBody ProjectAuditDTO auditDTO) {
        if (auditDTO.getId() == null || auditDTO.getStatus() == null) {
            return Result.error("参数不完整");
        }
        return Result.success(projectService.auditProject(auditDTO.getId(), auditDTO.getStatus(), auditDTO.getAuditRemark()));
    }

    @GetMapping("/list")
    public Result<List<ProjectListItemVO>> list() {
        return Result.success(projectService.listForView());
    }

    @GetMapping("/detail/{id}")
    public Result<ProjectDetailVO> getById(@PathVariable Long id) {
        return Result.success(projectService.getProjectDetail(id));
    }

    @GetMapping("/{id}")
    public Result<ProjectDetailVO> getProjectDetail(@PathVariable Long id) {
        return Result.success(projectService.getProjectDetail(id));
    }

    @Log("修改项目信息")
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody ResearchProject project) {
        if (project.getId() == null) {
            return Result.error("项目ID不能为空");
        }
        return Result.success(projectService.updateById(project));
    }

    @Log("确认项目预算")
    @PutMapping("/confirm-budget/{id}")
    public Result<String> confirmBudget(@PathVariable Long id) {
        if (projectService.confirmBudget(id)) {
            return Result.success("项目预算已确认，进入执行阶段");
        }
        return Result.error("确认失败，请检查项目状态");
    }
}
