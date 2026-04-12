package edu.university.researchfundsystem.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectDetailVO {

    private Long id;
    private String projectName;
    private String projectCode;
    private String principalName;
    private String performance;
    private Integer status;
    private String statusText;
    private String auditRemark;
    private List<MilestoneVO> milestones;
    private List<BudgetDetailVO> budgets;
    
    // Getters and setters
    @Data
    public static class MilestoneVO {
        private String stage;
        private String content;
        private String date;
    }
    
    @Data
    public static class BudgetDetailVO {
        private String categoryName;
        private BigDecimal budgetAmount;
        private BigDecimal spentAmount;
        private Double executionRate;
    }
}