package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.FundCategory;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.entity.ResearchProject;
import edu.university.researchfundsystem.mapper.FundExpenditureMapper;
import edu.university.researchfundsystem.model.vo.ExpenditureListItemVO;
import edu.university.researchfundsystem.service.FundCategoryService;
import edu.university.researchfundsystem.service.FundExpenditureService;
import edu.university.researchfundsystem.service.ResearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundExpenditureServiceImpl extends ServiceImpl<FundExpenditureMapper, FundExpenditure>
        implements FundExpenditureService {

    private final FundCategoryService categoryService;
    private final ResearchProjectService projectService;

    @Override
    public List<ExpenditureListItemVO> listByProjectForView(Long projectId) {
        LambdaQueryWrapper<FundExpenditure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FundExpenditure::getProjectId, projectId)
                .orderByDesc(FundExpenditure::getExpenditureDate);
        List<FundExpenditure> expenditures = this.list(queryWrapper);

        ResearchProject project = projectService.getById(projectId);
        String projectName = project != null ? project.getProjectName() : "未知项目";

        return expenditures.stream().map(expenditure -> {
            ExpenditureListItemVO item = new ExpenditureListItemVO();
            item.setId(expenditure.getId());
            item.setProjectId(expenditure.getProjectId());
            item.setProjectName(projectName);
            item.setCategoryId(expenditure.getCategoryId());

            FundCategory category = categoryService.getById(expenditure.getCategoryId());
            item.setCategoryName(category != null ? category.getCategoryName() : "未知类别");

            item.setAmount(expenditure.getAmount());
            item.setExpenditureDate(expenditure.getExpenditureDate());
            item.setRemark(expenditure.getRemark());
            return item;
        }).collect(Collectors.toList());
    }
}