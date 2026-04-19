package edu.university.researchfundsystem.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectListItemVO {
    private Long id;
    private String projectName;
    private String projectCode;
    private Long principalId;
    private String principalName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalBudget;
    private String status;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private BigDecimal spentAmount;
    private BigDecimal executionRate;
    private java.util.List<String> intelligentTags;
}