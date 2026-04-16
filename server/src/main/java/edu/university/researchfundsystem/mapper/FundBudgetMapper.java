package edu.university.researchfundsystem.mapper;

import edu.university.researchfundsystem.entity.FundBudget;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FundBudgetMapper extends BaseMapper<FundBudget> {

    @Select("<script>" +
            "SELECT SUM(b.budget_amount) FROM fund_budget b " +
            "<if test='userId != null'>JOIN research_project p ON b.project_id = p.id </if>" +
            "WHERE 1=1 " +
            "<if test='projectId != null'>AND b.project_id = #{projectId} </if>" +
            "<if test='userId != null'>AND p.principal_id = #{userId} </if>" +
            "</script>")
    BigDecimal sumBudgetByProject(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Select("<script>" +
            "SELECT b.category_id, c.category_name as category_name, SUM(b.budget_amount) as amount " +
            "FROM fund_budget b " +
            "LEFT JOIN fund_category c ON b.category_id = c.id " +
            "<if test='userId != null'>JOIN research_project p ON b.project_id = p.id </if>" +
            "WHERE 1=1 " +
            "<if test='projectId != null'>AND b.project_id = #{projectId} </if>" +
            "<if test='userId != null'>AND p.principal_id = #{userId} </if>" +
            "GROUP BY b.category_id, c.category_name" +
            "</script>")
    List<CategoryAmountVO> selectBudgetByCategory(@Param("projectId") Long projectId, @Param("userId") Long userId);
}