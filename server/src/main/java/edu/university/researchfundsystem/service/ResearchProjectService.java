package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.model.vo.ProjectDetailVO;
import edu.university.researchfundsystem.model.vo.ProjectListItemVO;

import java.util.List;

public interface ResearchProjectService extends IService<ResearchProject> {
    List<ProjectListItemVO> listForView();

    ProjectDetailVO getProjectDetail(Long id);
}
