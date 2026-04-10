package edu.university.researchfundsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import edu.university.researchfundsystem.model.vo.TrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FundExpenditureMapper extends BaseMapper<FundExpenditure> {

    @Select("SELECT e.category_id, c.name as category_name, SUM(e.amount) as amount " +
            "FROM fund_expenditure e " +
            "LEFT JOIN fund_category c ON e.category_id = c.id " +
            "WHERE e.project_id = #{projectId} " +
            "GROUP BY e.category_id, c.name")
    List<CategoryAmountVO> selectExpenditureByCategory(Long projectId);

    @Select("SELECT DATE_FORMAT(expenditure_date, '%Y-%m') as month, SUM(amount) as amount " +
            "FROM fund_expenditure WHERE project_id = #{projectId} " +
            "GROUP BY month ORDER BY month ASC")
    List<TrendVO> selectMonthlyTrend(Long projectId);
}