package edu.university.researchfundsystem.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryAmountVO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
}
