package edu.university.researchfundsystem.model.dto;

import lombok.Data;

@Data
public class ProjectAuditDTO {
    /**
     * 项目ID
     */
    private Long id;

    /**
     * 审核后状态 (2: 通过, 3: 驳回)
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String auditRemark;
}
