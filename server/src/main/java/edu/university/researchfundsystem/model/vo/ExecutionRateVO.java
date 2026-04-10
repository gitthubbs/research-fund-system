package edu.university.researchfundsystem.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExecutionRateVO {
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal rate;
}