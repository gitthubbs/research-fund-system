package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fund_adjustment")
public class FundAdjustment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("from_category_id")
    private Long fromCategoryId;

    @TableField("to_category_id")
    private Long toCategoryId;

    private BigDecimal amount;

    private String reason;

    /**
     * 状态: 0-待审核, 1-已通过, 2-已驳回
     */
    private Integer status;

    @TableField("applicant_id")
    private Long applicantId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("audit_remark")
    private String auditRemark;

    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String fromCategoryName;
    @TableField(exist = false)
    private String toCategoryName;
    @TableField(exist = false)
    private String applicantName;

    @TableField("is_read")
    private Integer isRead;
}
