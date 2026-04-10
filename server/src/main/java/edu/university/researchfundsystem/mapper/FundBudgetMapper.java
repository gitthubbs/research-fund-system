package edu.university.researchfundsystem.mapper;

import edu.university.researchfundsystem.entity.FundBudget;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.researchfundsystem.model.vo.CategoryAmountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FundBudgetMapper extends BaseMapper<FundBudget> {

    @Select("SELECT SUM(budget_amount) FROM fund_budget WHERE project_id = #{projectId}")
    BigDecimal sumBudgetByProject(Long projectId);

    @Select("SELECT b.category_id, c.name as category_name, SUM(b.budget_amount) as amount " +
            "FROM fund_budget b " +
            "LEFT JOIN fund_category c ON b.category_id = c.id " +
            "WHERE b.project_id = #{projectId} " +
            "GROUP BY b.category_id, c.name")
    List<CategoryAmountVO> selectBudgetByCategory(Long projectId);
}