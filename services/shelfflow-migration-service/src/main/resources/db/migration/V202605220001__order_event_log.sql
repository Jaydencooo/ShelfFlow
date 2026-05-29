CREATE TABLE IF NOT EXISTS `order_event_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `event_type` VARCHAR(32) NOT NULL,
  `actor_type` VARCHAR(16) NOT NULL,
  `actor_id` BIGINT DEFAULT NULL,
  `from_status` INT DEFAULT NULL,
  `to_status` INT DEFAULT NULL,
  `from_pay_status` INT DEFAULT NULL,
  `to_pay_status` INT DEFAULT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_event_log_order_id` (`order_id`),
  KEY `idx_order_event_log_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态变更审计日志';
