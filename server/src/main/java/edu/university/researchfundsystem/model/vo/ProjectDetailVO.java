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
    private LocalDate startDate; // ★ 新增
    private LocalDate endDate;   // ★ 新增
    private List<MilestoneVO> milestones;
    private List<BudgetDetailVO> budgets;
    
    // Getters and setters
    @Data
    public static class MilestoneVO {
        private String stage;
        private String content;
        private String date;
        private String type; // ★ 新增：success, primary, info
    }
    
    @Data
    public static class BudgetDetailVO {
        private String categoryName;
        private BigDecimal budgetAmount;
        private BigDecimal spentAmount;
        private Double executionRate;
    }
}