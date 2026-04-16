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

    // ★ 新增：存储分类占比 JSON
    @TableField("category_data")
    private String categoryData;

    // ★ 新增：存储月度趋势 JSON
    @TableField("trend_data")
    private String trendData;

    @TableField("snapshot_time")
    private LocalDateTime snapshotTime;
}