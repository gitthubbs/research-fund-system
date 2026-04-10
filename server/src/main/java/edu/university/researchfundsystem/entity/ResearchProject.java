package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("research_project")
public class ResearchProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectName;

    private String projectCode;

    private Long principalId;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalBudget;
}
