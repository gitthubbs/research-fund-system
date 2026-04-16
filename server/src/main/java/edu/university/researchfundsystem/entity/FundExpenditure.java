package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund_expenditure")
public class FundExpenditure {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("category_id")
    private Long categoryId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("expenditure_date")
    private LocalDate expenditureDate;

    @TableField("remark")
    private String remark;

    @TableField("status")
    private Integer status;

    @TableField("applicant_id")
    private Long applicantId;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    // ★ 新增
    @TableField("audit_remark")
    private String auditRemark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_read")
    private Integer isRead;
}