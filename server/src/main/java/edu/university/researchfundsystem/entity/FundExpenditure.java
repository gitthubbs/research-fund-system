package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("fund_expenditure")
public class FundExpenditure {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long categoryId;

    private BigDecimal amount;

    private LocalDate expenditureDate;

    private String remark;
}
