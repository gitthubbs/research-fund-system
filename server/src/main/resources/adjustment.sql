CREATE TABLE `fund_adjustment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
  `from_category_id` bigint(20) DEFAULT NULL COMMENT '调出科目ID',
  `to_category_id` bigint(20) DEFAULT NULL COMMENT '调入科目ID',
  `amount` decimal(19,4) DEFAULT NULL COMMENT '调剂金额',
  `reason` varchar(500) DEFAULT NULL COMMENT '调剂原因',
  `status` int(11) DEFAULT '0' COMMENT '状态: 0-待审核, 1-已通过, 2-已驳回',
  `applicant_id` bigint(20) DEFAULT NULL COMMENT '申请人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
