package edu.university.researchfundsystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalRiskSummaryVO {
    private Integer overdueProjects;
    private Integer laggingProjects;
    private Integer alertProjects; // 即将到期的项目
}
