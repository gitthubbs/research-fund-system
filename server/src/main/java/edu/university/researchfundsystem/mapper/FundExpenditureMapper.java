package edu.university.researchfundsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.researchfundsystem.entity.FundExpenditure;
import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import edu.university.researchfundsystem.model.vo.TrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FundExpenditureMapper extends BaseMapper<FundExpenditure> {

    @Select("<script>" +
            "SELECT e.category_id, c.category_name as category_name, SUM(e.amount) as amount " +
            "FROM fund_expenditure e " +
            "LEFT JOIN fund_category c ON e.category_id = c.id " +
            "<if test='userId != null'>JOIN research_project p ON e.project_id = p.id </if>" +
            "WHERE e.status = 1 " +
            "<if test='projectId != null'>AND e.project_id = #{projectId} </if>" +
            "<if test='userId != null'>AND p.principal_id = #{userId} </if>" +
            "GROUP BY e.category_id, c.category_name" +
            "</script>")
    List<CategoryAmountVO> selectExpenditureByCategory(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Select("<script>" +
            "SELECT DATE_FORMAT(e.expenditure_date, '%Y-%m') as month, SUM(e.amount) as amount " +
            "FROM fund_expenditure e " +
            "<if test='userId != null'>JOIN research_project p ON e.project_id = p.id </if>" +
            "WHERE e.status = 1 " +
            "<if test='projectId != null'>AND e.project_id = #{projectId} </if>" +
            "<if test='userId != null'>AND p.principal_id = #{userId} </if>" +
            "GROUP BY month ORDER BY month ASC" +
            "</script>")
    List<TrendVO> selectMonthlyTrend(@Param("projectId") Long projectId, @Param("userId") Long userId);
}