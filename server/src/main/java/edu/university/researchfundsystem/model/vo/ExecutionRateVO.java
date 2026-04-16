package edu.university.researchfundsystem.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExecutionRateVO {
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal rate;
    // ★ 新增
    private Integer projectCount;
    private String projectName;
    private Long projectId;
    private BigDecimal timeProgress;
    private LocalDateTime snapshotTime;
}