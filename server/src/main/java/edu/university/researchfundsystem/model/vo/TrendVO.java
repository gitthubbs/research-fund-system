package edu.university.researchfundsystem.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendVO {
    private String month;
    private BigDecimal amount;
}
