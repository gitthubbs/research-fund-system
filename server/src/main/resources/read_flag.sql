-- 为报销记录增加已读标记
ALTER TABLE `fund_expenditure` ADD COLUMN `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读(0-未读, 1-已读)';

-- 为调剂申请增加已读标记
ALTER TABLE `fund_adjustment` ADD COLUMN `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读(0-未读, 1-已读)';
