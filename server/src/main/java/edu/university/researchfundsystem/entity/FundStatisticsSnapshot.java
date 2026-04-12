package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fund_statistics_snapshot")
public class FundStatisticsSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("total_budget")
    private BigDecimal totalBudget;

    @TableField("total_expenditure")
    private BigDecimal totalExpenditure;

    @TableField("execution_rate")
    private BigDecimal executionRate;

    @TableField("snapshot_time")
    private LocalDateTime snapshotTime;
}