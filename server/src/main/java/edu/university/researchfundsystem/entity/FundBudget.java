package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("fund_budget")
public class FundBudget {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long categoryId;

    private BigDecimal budgetAmount;
}
