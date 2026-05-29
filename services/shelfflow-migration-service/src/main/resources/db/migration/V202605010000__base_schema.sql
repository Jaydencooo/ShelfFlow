CREATE TABLE IF NOT EXISTS `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int NOT NULL COMMENT '类型 1 商品分类 2 组合包分类',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '顺序',
  `status` int NOT NULL DEFAULT 1 COMMENT '分类状态 0禁用 1启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`),
  KEY `idx_category_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '商品分类 ID',
  `price` decimal(10,2) NOT NULL COMMENT '商品原价',
  `image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `description` varchar(255) DEFAULT NULL COMMENT '描述信息',
  `status` int NOT NULL DEFAULT 1 COMMENT '0 停售 1 起售',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `update_user` bigint NOT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_name` (`name`),
  KEY `idx_product_category_status` (`category_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品';

CREATE TABLE IF NOT EXISTS `product_spec` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品 ID',
  `name` varchar(32) DEFAULT NULL COMMENT '规格名称',
  `value` varchar(255) DEFAULT NULL COMMENT '规格值',
  PRIMARY KEY (`id`),
  KEY `idx_product_spec_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格';

CREATE TABLE IF NOT EXISTS `inventory_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品 ID',
  `batch_code` varchar(64) NOT NULL COMMENT '批次号',
  `production_time` datetime NOT NULL COMMENT '生产时间',
  `expiration_time` datetime NOT NULL COMMENT '过期时间',
  `stock_quantity` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `locked_quantity` int NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `sold_quantity` int NOT NULL DEFAULT 0 COMMENT '已售库存',
  `status` int NOT NULL DEFAULT 1 COMMENT '批次状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `update_user` bigint NOT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inventory_batch_code` (`batch_code`),
  KEY `idx_inventory_batch_product_status` (`product_id`, `status`),
  KEY `idx_inventory_batch_expiration` (`expiration_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存批次';

CREATE TABLE IF NOT EXISTS `pricing_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '规则名称',
  `discount_rate` decimal(10,2) NOT NULL COMMENT '折扣率',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `min_days_to_expire` int NOT NULL COMMENT '最小剩余天数',
  `max_days_to_expire` int NOT NULL COMMENT '最大剩余天数',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_pricing_rule_status_priority` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态定价规则';

CREATE TABLE IF NOT EXISTS `staff` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营人员';

CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(64) DEFAULT NULL COMMENT '登录标识',
  `name` varchar(32) DEFAULT NULL COMMENT '用户名',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` varchar(100) DEFAULT NULL COMMENT '密码哈希',
  `status` int NOT NULL DEFAULT 1 COMMENT '账号状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_openid` (`openid`),
  UNIQUE KEY `uq_user_phone` (`phone`),
  UNIQUE KEY `uq_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

CREATE TABLE IF NOT EXISTS `user_verification_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `target` varchar(100) NOT NULL COMMENT '验证码目标',
  `purpose` varchar(32) NOT NULL COMMENT '验证码用途',
  `code_hash` varchar(100) NOT NULL COMMENT '验证码哈希',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `consumed_at` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_verification_code_target` (`target`, `purpose`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户验证码';

CREATE TABLE IF NOT EXISTS `pickup_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `consignee` varchar(32) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(32) NOT NULL COMMENT '手机号',
  `label` varchar(16) DEFAULT NULL COMMENT '标签',
  `detail` varchar(120) DEFAULT NULL COMMENT '备注',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认',
  PRIMARY KEY (`id`),
  KEY `idx_pickup_contact_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自提联系人';

CREATE TABLE IF NOT EXISTS `pickup_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '自提点名称',
  `address` varchar(160) NOT NULL COMMENT '自提点地址',
  `contact_name` varchar(32) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `service_time` varchar(80) DEFAULT NULL COMMENT '服务时间',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pickup_point_name` (`name`),
  KEY `idx_pickup_point_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区自提点';

CREATE TABLE IF NOT EXISTS `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `product_id` bigint DEFAULT NULL COMMENT '商品 ID',
  `bundle_id` bigint DEFAULT NULL COMMENT '组合 ID',
  `batch_id` bigint DEFAULT NULL COMMENT '批次 ID',
  `product_spec` varchar(50) DEFAULT NULL COMMENT '规格',
  `number` int NOT NULL COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_cart_item_user` (`user_id`),
  KEY `idx_cart_item_selection` (`user_id`, `product_id`, `batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';

CREATE TABLE IF NOT EXISTS `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) DEFAULT NULL COMMENT '订单号',
  `status` int NOT NULL COMMENT '订单状态',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `pickup_contact_id` bigint DEFAULT NULL COMMENT '自提联系人 ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '支付时间',
  `pay_method` int NOT NULL COMMENT '支付方式',
  `pay_status` int NOT NULL COMMENT '支付状态',
  `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `pickup_point` varchar(255) DEFAULT NULL COMMENT '自提点',
  `user_name` varchar(32) DEFAULT NULL COMMENT '用户名',
  `consignee` varchar(32) DEFAULT NULL COMMENT '联系人',
  `preparation_mode` int NOT NULL DEFAULT 1 COMMENT '备货模式',
  `fulfillment_fee` int NOT NULL DEFAULT 0 COMMENT '履约服务费',
  `package_count` int NOT NULL DEFAULT 0 COMMENT '包装数量',
  `package_strategy` int NOT NULL DEFAULT 1 COMMENT '包装策略',
  `fulfillment_type` int NOT NULL DEFAULT 2 COMMENT '履约类型',
  `pickup_code` varchar(12) DEFAULT NULL COMMENT '自提码',
  `pickup_deadline` datetime DEFAULT NULL COMMENT '自提截止时间',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_number` (`number`),
  KEY `idx_orders_user_time` (`user_id`, `order_time`),
  KEY `idx_orders_status_time` (`status`, `order_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单';

CREATE TABLE IF NOT EXISTS `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单 ID',
  `product_id` bigint DEFAULT NULL COMMENT '商品 ID',
  `batch_id` bigint DEFAULT NULL COMMENT '批次 ID',
  `name` varchar(64) DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `product_spec` varchar(50) DEFAULT NULL COMMENT '规格',
  `number` int NOT NULL COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_detail_order` (`order_id`),
  KEY `idx_order_detail_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细';

CREATE TABLE IF NOT EXISTS `order_event_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单 ID',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型',
  `actor_type` varchar(16) NOT NULL COMMENT '操作者类型',
  `actor_id` bigint DEFAULT NULL COMMENT '操作者 ID',
  `from_status` int DEFAULT NULL COMMENT '原订单状态',
  `to_status` int DEFAULT NULL COMMENT '新订单状态',
  `from_pay_status` int DEFAULT NULL COMMENT '原支付状态',
  `to_pay_status` int DEFAULT NULL COMMENT '新支付状态',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_event_log_order_id` (`order_id`),
  KEY `idx_order_event_log_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态变更审计日志';

CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(80) NOT NULL COMMENT '标题',
  `category` varchar(32) NOT NULL COMMENT '分类',
  `content` text NOT NULL COMMENT '内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_ai_knowledge_category` (`category`),
  KEY `idx_ai_knowledge_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 知识库';

CREATE TABLE IF NOT EXISTS `ai_ops_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_user_id` bigint NOT NULL COMMENT '管理员用户 ID',
  `title` varchar(64) NOT NULL COMMENT '会话标题',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_chat_session_admin_update` (`admin_user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 运营助手会话';

CREATE TABLE IF NOT EXISTS `ai_ops_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint NOT NULL COMMENT '会话 ID',
  `role` varchar(16) NOT NULL COMMENT '消息角色',
  `content` text NOT NULL COMMENT '消息内容',
  `provider` varchar(32) DEFAULT NULL COMMENT '模型供应商',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名称',
  `references_json` text COMMENT '引用来源 JSON',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_chat_message_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 运营助手消息';

CREATE TABLE IF NOT EXISTS `ai_ops_suggestion_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `suggestion_id` varchar(96) NOT NULL COMMENT '建议 ID',
  `status` varchar(16) NOT NULL COMMENT '状态',
  `actor_id` bigint NOT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ai_ops_suggestion_action_id` (`suggestion_id`),
  KEY `idx_ai_ops_suggestion_action_status_time` (`status`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 运营建议处理记录';

CREATE TABLE IF NOT EXISTS `ai_ops_suggestion_action_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `suggestion_id` varchar(96) NOT NULL COMMENT '建议 ID',
  `action` varchar(24) NOT NULL COMMENT '动作',
  `status` varchar(16) NOT NULL COMMENT '状态',
  `target_type` varchar(32) NOT NULL COMMENT '目标类型',
  `target_id` varchar(64) NOT NULL COMMENT '目标 ID',
  `target_name` varchar(128) DEFAULT NULL COMMENT '目标名称',
  `operation_summary` varchar(255) NOT NULL COMMENT '操作摘要',
  `operation_payload` text COMMENT '操作参数',
  `actor_id` bigint NOT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_suggestion_action_log_suggestion` (`suggestion_id`, `create_time`),
  KEY `idx_ai_ops_suggestion_action_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 运营建议执行日志';

CREATE TABLE IF NOT EXISTS `admin_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `actor_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `module` varchar(32) NOT NULL COMMENT '模块',
  `action` varchar(32) NOT NULL COMMENT '动作',
  `method` varchar(8) NOT NULL COMMENT 'HTTP 方法',
  `path` varchar(255) NOT NULL COMMENT '请求路径',
  `status_code` int NOT NULL COMMENT '状态码',
  `request_id` varchar(80) DEFAULT NULL COMMENT '请求 ID',
  `summary` varchar(255) NOT NULL COMMENT '摘要',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_operation_log_time` (`create_time`),
  KEY `idx_admin_operation_log_actor_time` (`actor_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理端操作日志';
