CREATE TABLE IF NOT EXISTS `ai_ops_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_user_id` bigint NOT NULL COMMENT '管理员用户 ID',
  `title` varchar(64) NOT NULL COMMENT '会话标题',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_chat_session_admin_update` (`admin_user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 运营助手会话';

CREATE TABLE IF NOT EXISTS `ai_ops_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint NOT NULL COMMENT '会话 ID',
  `role` varchar(16) NOT NULL COMMENT '消息角色 user/assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `provider` varchar(32) DEFAULT NULL COMMENT '模型供应商',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名称',
  `references_json` text COMMENT '引用来源 JSON',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_chat_message_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 运营助手消息';

CREATE TABLE IF NOT EXISTS `ai_ops_suggestion_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `suggestion_id` varchar(96) NOT NULL COMMENT '建议 ID',
  `status` varchar(16) NOT NULL COMMENT 'pending/executed/ignored',
  `actor_id` bigint NOT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_ops_suggestion_action_id` (`suggestion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 运营建议处理记录';
