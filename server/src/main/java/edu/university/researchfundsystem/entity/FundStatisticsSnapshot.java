package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

    private Long projectId;

    private BigDecimal totalBudget;

    private BigDecimal totalExpenditure;

    private BigDecimal executionRate;

    private LocalDateTime snapshotTime;
}
