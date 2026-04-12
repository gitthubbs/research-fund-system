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
@TableName("research_project")
public class ResearchProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_name")
    private String projectName;

    @TableField("project_code")
    private String projectCode;

    @TableField("principal_id")
    private Long principalId;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("total_budget")
    private BigDecimal totalBudget;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("status")
    private Integer status;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("update_time")
    private LocalDateTime updateTime;
}