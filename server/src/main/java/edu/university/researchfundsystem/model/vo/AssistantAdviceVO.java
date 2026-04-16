package edu.university.researchfundsystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantAdviceVO {
    /**
     * 标题
     */
    private String title;
    
    /**
     * 详细内容
     */
    private String content;
    
    /**
     * 建议类型: danger (紧急), warning (警告), info (通知)
     */
    private String type;
    
    /**
     * 跳转链接 (前端路由)
     */
    private String link;
    
    /**
     * 业务主键 (用于标记已读)
     */
    private Long businessId;
    
    /**
     * 业务类型: 1-报销驳回, 2-调剂驳回
     */
    private Integer businessType;
}
