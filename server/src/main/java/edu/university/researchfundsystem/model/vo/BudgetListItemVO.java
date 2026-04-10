package edu.university.researchfundsystem.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetListItemVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private Double executionRate;
}
