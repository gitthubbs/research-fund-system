package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.model.vo.ProjectDetailVO;
import edu.university.researchfundsystem.model.vo.ProjectListItemVO;

import java.util.List;

public interface ResearchProjectService extends IService<ResearchProject> {
    List<ProjectListItemVO> listForView();

    ProjectDetailVO getProjectDetail(Long id);

    /**
     * 科研人员提交申请
     * @param id 项目ID
     * @return 是否成功
     */
    boolean submitProject(Long id);

    /**
     * 管理员审核项目
     * @param id 项目ID
     * @param status 审核后状态 (2: 通过, 3: 驳回)
     * @param remark 审核意见
     * @return 是否成功
     */
    /**
     * 科研人员确认预算编制，启动项目执行 (状态从 2 变为 4)
     * @param id 项目ID
     * @return 是否成功
     */
    boolean confirmBudget(Long id);

    boolean auditProject(Long id, Integer status, String auditRemark);

    // ★ 新增
    /**
     * 结题验收：将项目状态改为已结题 (状态由 4 变为 5)
     */
    boolean finishProject(Long id);
}
