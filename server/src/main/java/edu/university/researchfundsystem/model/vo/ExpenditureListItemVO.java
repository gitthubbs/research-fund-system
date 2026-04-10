package edu.university.researchfundsystem.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenditureListItemVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate expenditureDate;
    private String remark;
}
