package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.model.vo.ProjectDetailVO;
import edu.university.researchfundsystem.model.vo.ProjectListItemVO;
import edu.university.researchfundsystem.service.ResearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ResearchProjectController {

    private final ResearchProjectService projectService;

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

    @PostMapping("/create")
    public Result<Long> save(@RequestBody ResearchProject project) {
        boolean success = projectService.save(project);
        if (success) {
            return Result.success(project.getId());
        }
        return Result.error("项目创建失败");
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody ResearchProject project) {
        if (project.getId() == null) {
            return Result.error("项目ID不能为空");
        }
        return Result.success(projectService.updateById(project));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(projectService.removeById(id));
    }
}
