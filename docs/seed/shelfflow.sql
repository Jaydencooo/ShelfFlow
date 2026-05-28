/*
 Navicat Premium Dump SQL

 Source Server         : Jayden_Mysql
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:3306
 Source Schema         : shelfflow

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 27/05/2026 13:08:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_log`;
CREATE TABLE `admin_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actor_id` bigint DEFAULT NULL,
  `module` varchar(32) NOT NULL,
  `action` varchar(32) NOT NULL,
  `method` varchar(8) NOT NULL,
  `path` varchar(255) NOT NULL,
  `status_code` int NOT NULL,
  `request_id` varchar(80) DEFAULT NULL,
  `summary` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_admin_operation_log_time` (`create_time`),
  KEY `idx_admin_operation_log_actor_time` (`actor_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of admin_operation_log
-- ----------------------------
BEGIN;
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (1, 1, '自提点管理', '更新', 'PUT', '/api/admin/pickup-points/1', 200, '7b271294-1305-469c-b6b7-f99bfd3a9c80', '自提点管理 更新 成功', '2026-05-25 15:44:20');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (2, 1, '自提点管理', '更新', 'PUT', '/api/admin/pickup-points/1', 200, '92bd16a6-0edc-40c3-b2fe-f3ec1064706c', '自提点管理 更新 成功', '2026-05-25 15:44:23');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (3, 1, '批次管理', '状态变更', 'POST', '/api/admin/inventory-batches/3/status', 409, 'fddb5244-07b7-466b-91b7-4c9ac86d237d', '批次管理 状态变更 失败', '2026-05-25 15:50:47');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (4, 1, '批次管理', '新增或执行', 'POST', '/api/admin/inventory-batches', 200, 'fdd230af-4e2e-48c7-818b-b70ea1b214af', '批次管理 新增或执行 成功', '2026-05-25 15:51:31');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (5, 1, 'AI 运营助手', '新增或执行', 'POST', '/api/admin/ai-ops/suggestions/EXPIRING_SOON-11/action', 200, 'e2c1301b-3288-435b-9a07-9be541a2c5c1', 'AI 运营助手 新增或执行 成功', '2026-05-25 15:51:59');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (6, 1, '订单履约', '状态变更', 'POST', '/api/admin/orders/22/status', 200, '5d1a4972-7c96-4cc0-9935-71ef4d4aa1f2', '订单履约 状态变更 成功', '2026-05-26 10:49:05');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (7, 1, '订单履约', '状态变更', 'POST', '/api/admin/orders/22/status', 200, '3be84a27-9e9d-48b0-8bdb-b3ff68d33b19', '订单履约 状态变更 成功', '2026-05-26 10:49:12');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (8, 1, '订单履约', '状态变更', 'POST', '/api/admin/orders/22/status', 200, 'fcf697ba-f964-4bf9-8390-e16d4224314f', '订单履约 状态变更 成功', '2026-05-26 11:15:30');
INSERT INTO `admin_operation_log` (`id`, `actor_id`, `module`, `action`, `method`, `path`, `status_code`, `request_id`, `summary`, `create_time`) VALUES (9, 1, '商品管理', '新增或执行', 'POST', '/api/admin/products', 200, '06b79cb8-c029-4401-b9f4-2e870fe5535e', '商品管理 新增或执行 成功', '2026-05-27 13:08:00');
COMMIT;

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(80) NOT NULL,
  `category` varchar(32) NOT NULL,
  `content` text NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `create_user` bigint DEFAULT NULL,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ai_knowledge_category` (`category`),
  KEY `idx_ai_knowledge_update_time` (`update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ai_knowledge_base
-- ----------------------------
BEGIN;
INSERT INTO `ai_knowledge_base` (`id`, `title`, `category`, `content`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, '乳制品临期处理规范', '处理规范', '乳制品剩余三天内应进入临期专区，结合会员推送和清仓折扣降低损耗。', '2026-05-24 13:48:35', '2026-05-24 13:48:35', 1, 1);
INSERT INTO `ai_knowledge_base` (`id`, `title`, `category`, `content`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (2, '动态定价策略', '定价策略', '临期商品应结合剩余效期、库存深度、历史动销和损耗成本设置折扣。', '2026-05-24 13:48:35', '2026-05-24 13:48:35', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for ai_ops_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_ops_chat_message`;
CREATE TABLE `ai_ops_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` bigint NOT NULL COMMENT '会话 ID',
  `role` varchar(16) NOT NULL COMMENT '消息角色 user/assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `provider` varchar(32) DEFAULT NULL COMMENT '模型供应商',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名称',
  `references_json` text COMMENT '引用来源 JSON',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_chat_message_session_time` (`session_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 运营助手消息';

-- ----------------------------
-- Records of ai_ops_chat_message
-- ----------------------------
BEGIN;
INSERT INTO `ai_ops_chat_message` (`id`, `session_id`, `role`, `content`, `provider`, `model`, `references_json`, `create_time`) VALUES (1, 1, 'user', '推荐最佳促销策略', NULL, NULL, '[]', '2026-05-24 23:45:13');
INSERT INTO `ai_ops_chat_message` (`id`, `session_id`, `role`, `content`, `provider`, `model`, `references_json`, `create_time`) VALUES (2, 1, 'assistant', '1. 关键判断：  \n- 当前系统监测到 **37个临期批次**（距保质期≤15天），覆盖12个SKU，总库存量4,826件，其中**高损耗风险批次19个**（距保质期≤7天且日均动销＜0.5件/天），对应库存1,317件，集中在乳制品（占比42%）、短保烘焙（31%）和鲜切果蔬（18%）三类；  \n- 近7日促销转化率数据显示：**“买一赠一”策略对乳制品临期品转化率最高（达68.3%，高于均值22.1pct）**，但客单价下降19%；而“阶梯满减+临期标识弹窗”在烘焙类中ROI最优（达1:4.7）；  \n- 仓储履约侧反馈：**3个高风险批次已触发“优先出库”预警（库龄＞临期阈值3天且未绑定促销计划）**，存在48小时内滞留导致不可逆损耗风险。\n\n2. 推荐动作：  \n✅ **立即执行分层促销组合策略（T+0启动）**：  \n- 对19个高风险批次（≤7天）：  \n → 乳制品（如A082酸奶）：启用「买一赠一 + 限时2小时闪购弹窗」，同步在APP首页“临期特惠”专区置顶，绑定LBS推送（仅推送给3km内30天有乳品购买记录用户）；  \n → 短保烘焙（如B115麻薯）：采用「满29减12 + 赠同款小样（库存冗余SKU抵扣）」，叠加商品页强提示“仅剩XX件，今日24点下架”倒计时；  \n → 鲜切果蔬（如C033混合沙拉）：启动B端临期直供通道，定向推送至周边15家社区食堂及轻食店，按成本价7折+次日达履约，2小时内完成订单锁定。  \n✅ 同步启动「临期批次履约熔断机制」：对未在4小时内绑定促销的预警批次，自动触发仓配强插单，优先分配至最近前置仓并标记“临期专拣”，确保T+1完成出库。\n\n3. 风险提醒：  \n⚠️ **价格穿透风险**：当前乳制品买赠策略若持续超48小时，可能冲击正常批次价格带，需在第36小时启动动态调价——将赠品替换为低毛利但长保质期关联品（如燕麦杯），避免主SKU价格锚定失效；  \n⚠️ **B端渠道回款滞后风险**：社区食堂类客户账期普遍为15–30天，建议对首单合作客户启用「预付30%锁货」弹窗，并同步在ERP中标记“临期应收专项跟踪”；  \n⚠️ **数据盲区风险**：系统暂未接入本地天气API，若未来48小时出现连续35℃+高温，鲜切果蔬损耗率或跃升至日均12%（当前模型按8%测算），需人工介入追加冰袋补贴或暂停C033线上上架。  \n→ 下一步动作：请运营同学于15分钟内确认是否开启高温应急开关，并同步检查B端客户预付协议模板是否已更新至V2.3版（含临期品免责条款）。', 'dashscope', 'qwen-plus', '[]', '2026-05-24 23:45:31');
COMMIT;

-- ----------------------------
-- Table structure for ai_ops_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_ops_chat_session`;
CREATE TABLE `ai_ops_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_user_id` bigint NOT NULL COMMENT '管理员用户 ID',
  `title` varchar(64) NOT NULL COMMENT '会话标题',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_chat_session_admin_update` (`admin_user_id`,`update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 运营助手会话';

-- ----------------------------
-- Records of ai_ops_chat_session
-- ----------------------------
BEGIN;
INSERT INTO `ai_ops_chat_session` (`id`, `admin_user_id`, `title`, `create_time`, `update_time`) VALUES (1, 1, 'AI 运营问答', '2026-05-24 23:45:13', '2026-05-24 23:45:30');
COMMIT;

-- ----------------------------
-- Table structure for ai_ops_suggestion_action
-- ----------------------------
DROP TABLE IF EXISTS `ai_ops_suggestion_action`;
CREATE TABLE `ai_ops_suggestion_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `suggestion_id` varchar(96) NOT NULL COMMENT '建议 ID',
  `status` varchar(16) NOT NULL COMMENT 'pending/executed/ignored',
  `actor_id` bigint NOT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_ops_suggestion_action_id` (`suggestion_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 运营建议处理记录';

-- ----------------------------
-- Records of ai_ops_suggestion_action
-- ----------------------------
BEGIN;
INSERT INTO `ai_ops_suggestion_action` (`id`, `suggestion_id`, `status`, `actor_id`, `create_time`, `update_time`) VALUES (1, 'EXPIRING_SOON-9', 'executed', 1, '2026-05-25 12:08:36', '2026-05-25 12:08:36');
INSERT INTO `ai_ops_suggestion_action` (`id`, `suggestion_id`, `status`, `actor_id`, `create_time`, `update_time`) VALUES (2, 'EXPIRING_SOON-11', 'executed', 1, '2026-05-25 15:51:59', '2026-05-25 15:51:59');
COMMIT;

-- ----------------------------
-- Table structure for ai_ops_suggestion_action_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_ops_suggestion_action_log`;
CREATE TABLE `ai_ops_suggestion_action_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `suggestion_id` varchar(96) NOT NULL,
  `action` varchar(24) NOT NULL,
  `status` varchar(16) NOT NULL,
  `target_type` varchar(32) NOT NULL,
  `target_id` varchar(64) NOT NULL,
  `target_name` varchar(128) DEFAULT NULL,
  `operation_summary` varchar(255) NOT NULL,
  `operation_payload` text,
  `actor_id` bigint NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ai_ops_suggestion_action_log_suggestion` (`suggestion_id`,`create_time`),
  KEY `idx_ai_ops_suggestion_action_log_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ai_ops_suggestion_action_log
-- ----------------------------
BEGIN;
INSERT INTO `ai_ops_suggestion_action_log` (`id`, `suggestion_id`, `action`, `status`, `target_type`, `target_id`, `target_name`, `operation_summary`, `operation_payload`, `actor_id`, `create_time`) VALUES (1, 'EXPIRING_SOON-11', 'execute', 'executed', 'inventory_batch', '11', 'B-P78-20260525-KWQPVY', 'B-P78-20260525-KWQPVY 已调整为paused', '{\"batchStatus\":\"paused\",\"executionPlan\":\"将批次 B-P78-20260525-KWQPVY 状态调整为 paused。\",\"operationNote\":\"\"}', 1, '2026-05-25 15:51:59');
COMMIT;

-- ----------------------------
-- Table structure for bundle
-- ----------------------------
DROP TABLE IF EXISTS `bundle`;
CREATE TABLE `bundle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '商品分类id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '组合包名称',
  `price` decimal(10,2) NOT NULL COMMENT '组合包价格',
  `status` int DEFAULT '1' COMMENT '售卖状态 0:停售 1:起售',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_bundle_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='组合包';

-- ----------------------------
-- Records of bundle
-- ----------------------------
BEGIN;
INSERT INTO `bundle` (`id`, `category_id`, `name`, `price`, `status`, `description`, `image`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (32, 15, '组合包1test', 155.00, 1, '描述随便', 'https://america2030.oss-cn-beijing.aliyuncs.com/a336c6f7-0337-418d-ac1b-fb3f0d9776e4.jpg', NULL, '2025-09-06 16:20:17', NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for bundle_product
-- ----------------------------
DROP TABLE IF EXISTS `bundle_product`;
CREATE TABLE `bundle_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bundle_id` bigint DEFAULT NULL COMMENT '组合包id',
  `product_id` bigint DEFAULT NULL COMMENT '商品id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称 （冗余字段）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品单价（冗余字段）',
  `copies` int DEFAULT NULL COMMENT '商品份数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='组合包商品关系';

-- ----------------------------
-- Records of bundle_product
-- ----------------------------
BEGIN;
INSERT INTO `bundle_product` (`id`, `bundle_id`, `product_id`, `name`, `price`, `copies`) VALUES (55, 32, 48, '精品咖啡液 6杯装', 4.00, 1);
INSERT INTO `bundle_product` (`id`, `bundle_id`, `product_id`, `name`, `price`, `copies`) VALUES (56, 32, 46, '临期酸奶 200g', 8.00, 1);
COMMIT;

-- ----------------------------
-- Table structure for cart_item
-- ----------------------------
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `user_id` bigint NOT NULL COMMENT '主键',
  `product_id` bigint DEFAULT NULL COMMENT '商品id',
  `bundle_id` bigint DEFAULT NULL COMMENT '组合包id',
  `batch_id` bigint DEFAULT NULL COMMENT '库存批次id',
  `product_spec` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='选品车';

-- ----------------------------
-- Records of cart_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int DEFAULT NULL COMMENT '类型   1 商品分类 2 组合包分类',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '顺序',
  `status` int DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=904 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='ShelfFlow 商品及组合包分类';

-- ----------------------------
-- Records of category
-- ----------------------------
BEGIN;
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (11, 1, '临期乳品', 10, 1, '2026-04-01 22:09:18', '2026-04-01 22:09:18', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (12, 1, '烘焙短保', 9, 0, '2026-04-01 22:09:32', '2026-05-24 21:23:23', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (13, 2, '高周转组合', 12, 1, '2026-04-01 22:11:38', '2026-04-02 11:04:40', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (15, 2, '门店补货组合', 13, 1, '2026-04-01 22:14:10', '2026-04-02 11:04:48', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (16, 1, '鲜食便当', 4, 0, '2026-04-01 22:15:37', '2026-04-15 14:27:25', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (17, 1, '冷藏熟食', 5, 1, '2026-04-01 22:16:14', '2026-04-15 14:39:44', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (18, 1, '冷链轻食', 6, 1, '2026-04-01 22:17:42', '2026-04-01 22:17:42', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (19, 1, '社区生鲜', 7, 0, '2026-04-01 22:18:12', '2026-04-01 22:18:28', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (20, 1, '预制鲜食', 60, 1, '2026-04-01 22:22:29', '2026-05-27 13:06:11', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (21, 1, '即饮饮品', 40, 1, '2026-04-02 10:51:47', '2026-05-27 13:06:11', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (23, 1, '清仓专区', 50, 1, '2025-08-11 14:54:16', '2026-05-27 13:06:11', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (25, 2, '晚间清仓组合', 3, 1, '2025-08-11 14:54:47', '2025-08-23 17:34:29', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (26, 1, '测试分类', 2, 0, '2026-05-24 20:00:54', '2026-05-24 20:33:10', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (27, 1, '奶', 21, 0, '2026-05-24 21:44:32', '2026-05-24 21:44:37', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (901, 1, '社区临期乳品', 10, 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (902, 1, '短保烘焙鲜食', 20, 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (903, 1, '自提轻食便当', 30, 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for fulfillment_task
-- ----------------------------
DROP TABLE IF EXISTS `fulfillment_task`;
CREATE TABLE `fulfillment_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `order_number` varchar(50) NOT NULL COMMENT '订单号',
  `pickup_code` varchar(12) DEFAULT NULL COMMENT '自提核销码',
  `status` int NOT NULL DEFAULT '1' COMMENT '任务状态 1待备货 2待自提 3已核销 4已取消',
  `pickup_deadline` datetime DEFAULT NULL COMMENT '自提截止时间',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fulfillment_task_order` (`order_id`),
  KEY `idx_fulfillment_task_status` (`status`),
  KEY `idx_fulfillment_task_order_number` (`order_number`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ShelfFlow 履约任务';

-- ----------------------------
-- Records of fulfillment_task
-- ----------------------------
BEGIN;
INSERT INTO `fulfillment_task` (`id`, `order_id`, `order_number`, `pickup_code`, `status`, `pickup_deadline`, `completed_time`, `create_time`, `update_time`) VALUES (1, 1, 'SF202604280001', '482913', 2, '2026-04-28 20:00:00', NULL, '2026-04-28 10:01:00', '2026-04-28 10:05:00');
INSERT INTO `fulfillment_task` (`id`, `order_id`, `order_number`, `pickup_code`, `status`, `pickup_deadline`, `completed_time`, `create_time`, `update_time`) VALUES (2, 2, 'SF202604280002', '735921', 3, '2026-04-28 14:00:00', '2026-04-28 12:05:00', '2026-04-28 11:02:00', '2026-04-28 12:05:00');
COMMIT;

-- ----------------------------
-- Table structure for inventory_batch
-- ----------------------------
DROP TABLE IF EXISTS `inventory_batch`;
CREATE TABLE `inventory_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品id',
  `batch_code` varchar(64) NOT NULL COMMENT '批次编号',
  `production_time` datetime DEFAULT NULL COMMENT '生产时间',
  `expiration_time` datetime NOT NULL COMMENT '过期时间',
  `stock_quantity` int NOT NULL DEFAULT '0' COMMENT '批次总库存',
  `locked_quantity` int NOT NULL DEFAULT '0' COMMENT '已锁定库存',
  `sold_quantity` int NOT NULL DEFAULT '0' COMMENT '已售库存',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0停用 1可售 2售罄 3过期',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_inventory_batch_code` (`batch_code`),
  KEY `idx_inventory_batch_product` (`product_id`),
  KEY `idx_inventory_batch_expiration` (`expiration_time`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ShelfFlow 库存批次';

-- ----------------------------
-- Records of inventory_batch
-- ----------------------------
BEGIN;
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, 46, 'SF-BATCH-202604-001', '2026-04-01 08:00:00', '2026-05-01 23:59:59', 120, 0, 12, 3, '2026-04-01 09:00:00', '2026-05-24 13:57:55', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (2, 49, 'SF-BATCH-202604-002', '2026-04-02 08:00:00', '2026-04-12 23:59:59', 80, 0, 20, 3, '2026-04-02 09:00:00', '2026-05-24 13:57:55', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (3, 53, 'SF-BATCH-202604-003', '2026-04-02 08:00:00', '2026-04-08 23:59:59', 45, 0, 15, 3, '2026-04-02 09:00:00', '2026-05-24 13:57:55', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (8, 78, 'B-P78-20260525-KOR0MJ', '2026-05-25 12:07:00', '2026-06-25 15:07:00', 5, 0, 0, 1, '2026-05-25 12:07:53', '2026-05-25 12:07:53', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (10, 69, 'B-P69-20260525-KOT2XT', '2026-05-25 12:09:00', '2026-05-25 15:09:00', 100, 0, 0, 3, '2026-05-25 12:09:28', '2026-05-25 15:10:00', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (11, 78, 'B-P78-20260525-KWQPVY', '2026-05-25 15:51:00', '2026-05-25 17:51:00', 1, 0, 0, 0, '2026-05-25 15:51:31', '2026-05-25 15:51:59', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (12, 1901, 'SF-DEMO-MILK-001', '2026-05-24 10:23:53', '2026-05-31 12:17:03', 60, 0, 0, 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (13, 1902, 'SF-DEMO-BREAD-001', '2026-05-25 10:23:53', '2026-05-28 12:17:03', 35, 0, 0, 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (14, 1903, 'SF-DEMO-LIGHTMEAL-001', '2026-05-26 10:23:53', '2026-05-30 12:58:44', 40, 0, 0, 1, '2026-05-26 10:23:53', '2026-05-27 12:58:44', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (24, 1904, 'SF-DEMO-YOGURT-001', '2026-05-24 11:53:42', '2026-06-01 12:17:03', 48, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (25, 1905, 'SF-DEMO-LATTE-001', '2026-05-22 11:53:42', '2026-06-03 12:17:03', 72, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (26, 1906, 'SF-DEMO-FRUIT-001', '2026-05-27 11:53:42', '2026-05-28 12:17:03', 24, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (27, 1907, 'SF-DEMO-PASTA-001', '2026-05-26 11:53:42', '2026-05-30 12:17:03', 32, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (28, 1908, 'SF-DEMO-EGG-001', '2026-05-23 11:53:42', '2026-06-02 12:17:03', 80, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (29, 1909, 'SF-DEMO-BAGEL-001', '2026-05-26 11:53:42', '2026-05-29 12:17:03', 30, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (30, 1910, 'SF-DEMO-SPARKLING-001', '2026-05-17 11:53:42', '2026-06-06 12:17:03', 90, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (31, 1911, 'SF-DEMO-SALAD-001', '2026-05-27 11:53:42', '2026-05-28 12:17:03', 26, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `inventory_batch` (`id`, `product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (32, 1912, 'SF-DEMO-SANDWICH-001', '2026-05-27 11:53:42', '2026-05-29 12:17:03', 34, 0, 0, 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '名字',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `product_id` bigint DEFAULT NULL COMMENT '商品id',
  `bundle_id` bigint DEFAULT NULL COMMENT '组合包id',
  `batch_id` bigint DEFAULT NULL COMMENT '库存批次id',
  `product_spec` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单商品明细表';

-- ----------------------------
-- Records of order_detail
-- ----------------------------
BEGIN;
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (5, '香辣鸡块熟食包', 'https://assets.shelfflow.local/f5ac8455-4793-450c-97ba-173795c34626.png', 4, 63, NULL, NULL, NULL, 1, 88.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (6, '孜然鸡块熟食包', 'https://assets.shelfflow.local/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png', 4, 64, NULL, NULL, NULL, 4, 88.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (7, '全麦吐司 450g', 'https://assets.shelfflow.local/76752350-2121-44d2-b477-10791c23a8ec.png', 5, 49, NULL, NULL, NULL, 1, 2.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (8, '杂粮餐包 6枚', 'https://assets.shelfflow.local/475cc599-8661-4899-8f9e-121dd8ef7d02.png', 5, 50, NULL, NULL, NULL, 1, 1.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (9, '精品咖啡液 6杯装', 'https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png', 6, 48, NULL, NULL, NULL, 1, 4.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (10, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 6, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (11, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 7, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (12, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 7, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (13, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 8, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (14, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 9, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (15, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 9, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (16, '精品咖啡液 6杯装', 'https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png', 9, 48, NULL, NULL, NULL, 1, 4.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (17, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 10, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (18, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 10, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (19, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 11, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (20, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 12, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (21, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 13, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (22, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 13, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (23, '精品咖啡液 6杯装', 'https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png', 14, 48, NULL, NULL, NULL, 1, 4.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (24, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 14, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (25, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 15, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (26, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 16, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (27, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 16, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (28, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 17, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (29, '家庭装鱼排 500g', 'https://assets.shelfflow.local/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png', 18, 65, NULL, NULL, '6折', 1, 68.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (30, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 19, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (31, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 19, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (32, '精品咖啡液 6杯装', 'https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png', 19, 48, NULL, NULL, NULL, 6, 4.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (33, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 20, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (34, '临期酸奶 200g', 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', 21, 46, NULL, NULL, NULL, 1, 8.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (35, '低温鲜奶 950ml', 'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png', 21, 47, NULL, NULL, NULL, 1, 0.10);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (36, '全麦吐司短保装 450g', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=900&auto=format&fit=crop&q=80', 22, 1902, NULL, 13, NULL, 1, 5.45);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (37, '全麦吐司短保装 450g', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=900&auto=format&fit=crop&q=80', 23, 1902, NULL, 13, NULL, 1, 5.45);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `product_id`, `bundle_id`, `batch_id`, `product_spec`, `number`, `amount`) VALUES (38, '社区特惠低温鲜奶 950ml', 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=900&auto=format&fit=crop&q=80', 23, 1901, NULL, 12, NULL, 1, 9.68);
COMMIT;

-- ----------------------------
-- Table structure for order_event_log
-- ----------------------------
DROP TABLE IF EXISTS `order_event_log`;
CREATE TABLE `order_event_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `event_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `actor_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `actor_id` bigint DEFAULT NULL,
  `from_status` int DEFAULT NULL,
  `to_status` int DEFAULT NULL,
  `from_pay_status` int DEFAULT NULL,
  `to_pay_status` int DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_event_log_order_id` (`order_id`),
  KEY `idx_order_event_log_event_type` (`event_type`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态变更审计日志';

-- ----------------------------
-- Records of order_event_log
-- ----------------------------
BEGIN;
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (1, 1, 'fulfillment_updated', 'admin', 1, 4, 5, 1, 1, '管理员将订单状态从 ready_for_pickup 更新为 completed', '2026-05-24 16:35:59');
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (2, 22, 'submitted', 'user', 15, NULL, 1, NULL, 0, '用户提交订单', '2026-05-26 10:48:36');
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (3, 22, 'paid', 'user', 15, 1, 2, 0, 1, '用户确认支付', '2026-05-26 10:48:44');
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (4, 22, 'fulfillment_updated', 'admin', 1, 2, 3, 1, 1, '管理员将订单状态从 to_prepare 更新为 preparing', '2026-05-26 10:49:05');
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (5, 22, 'fulfillment_updated', 'admin', 1, 3, 4, 1, 1, '管理员将订单状态从 preparing 更新为 ready_for_pickup', '2026-05-26 10:49:12');
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (6, 22, 'fulfillment_updated', 'admin', 1, 4, 5, 1, 1, '管理员将订单状态从 ready_for_pickup 更新为 completed', '2026-05-26 11:15:30');
INSERT INTO `order_event_log` (`id`, `order_id`, `event_type`, `actor_type`, `actor_id`, `from_status`, `to_status`, `from_pay_status`, `to_pay_status`, `note`, `create_time`) VALUES (7, 23, 'submitted', 'user', 15, NULL, 1, NULL, 0, '用户提交订单', '2026-05-27 11:43:42');
COMMIT;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单号',
  `status` int NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待备货 3备货中 4待自提/待核销 5已完成 6已取消 7退款',
  `user_id` bigint NOT NULL COMMENT '下单用户',
  `pickup_contact_id` bigint DEFAULT NULL COMMENT '自提联系人id，自提订单可为空',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10,2) NOT NULL COMMENT '实收金额',
  `remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `pickup_point` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '自提履约点',
  `user_name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '用户名称',
  `consignee` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单取消原因',
  `rejection_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单拒绝原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '订单取消时间',
  `preparation_mode` tinyint(1) NOT NULL DEFAULT '1' COMMENT '履约准备模式 1立即备货 0预约自提',
  `completed_time` datetime DEFAULT NULL COMMENT '订单完成时间',
  `fulfillment_fee` int DEFAULT NULL COMMENT '履约服务费',
  `package_count` int DEFAULT NULL COMMENT '履约包装数量',
  `package_strategy` tinyint(1) NOT NULL DEFAULT '1' COMMENT '履约包装策略 1按商品数量提供 0选择具体数量',
  `fulfillment_type` int NOT NULL DEFAULT '2' COMMENT '履约方式 1平台履约 2到店自提',
  `pickup_code` varchar(12) COLLATE utf8mb3_bin DEFAULT NULL COMMENT '自提核销码',
  `pickup_time` datetime DEFAULT NULL COMMENT '预约自提时间',
  `pickup_deadline` datetime DEFAULT NULL COMMENT '自提截止时间',
  `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
  `verify_staff_id` bigint DEFAULT NULL COMMENT '核销运营人员id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='履约订单表';

-- ----------------------------
-- Records of orders
-- ----------------------------
BEGIN;
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `pickup_contact_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `amount`, `remark`, `phone`, `pickup_point`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `preparation_mode`, `completed_time`, `fulfillment_fee`, `package_count`, `package_strategy`, `fulfillment_type`, `pickup_code`, `pickup_time`, `pickup_deadline`, `verify_time`, `verify_staff_id`) VALUES (1, 'SF202604280001', 5, 5, NULL, '2026-04-28 10:00:00', '2026-04-28 10:01:00', 1, 1, 28.00, '到店自提', '16629064744', '滨江社区前置仓 A 区', NULL, '林可', NULL, NULL, NULL, 1, NULL, 0, 1, 1, 2, '482913', '2026-04-28 18:00:00', '2026-04-28 20:00:00', NULL, NULL);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `pickup_contact_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `amount`, `remark`, `phone`, `pickup_point`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `preparation_mode`, `completed_time`, `fulfillment_fee`, `package_count`, `package_strategy`, `fulfillment_type`, `pickup_code`, `pickup_time`, `pickup_deadline`, `verify_time`, `verify_staff_id`) VALUES (2, 'SF202604280002', 5, 5, NULL, '2026-04-28 11:00:00', '2026-04-28 11:02:00', 1, 1, 16.00, '已核销', '16629064744', '滨江社区前置仓 A 区', NULL, '林可', NULL, NULL, NULL, 1, '2026-04-28 12:05:00', 0, 1, 1, 2, '735921', '2026-04-28 12:00:00', '2026-04-28 14:00:00', '2026-04-28 12:05:00', 1);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `pickup_contact_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `amount`, `remark`, `phone`, `pickup_point`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `preparation_mode`, `completed_time`, `fulfillment_fee`, `package_count`, `package_strategy`, `fulfillment_type`, `pickup_code`, `pickup_time`, `pickup_deadline`, `verify_time`, `verify_staff_id`) VALUES (22, 'SFU20260526104836905', 5, 15, 6, '2026-05-26 10:48:36', '2026-05-26 10:48:44', 1, 1, 5.45, NULL, '18888888888', '滨江社区前置仓 A 区｜滨江花园北门 12 号自提柜旁', '用户端验收账号', 'test', NULL, NULL, NULL, 1, NULL, 0, 0, 1, 2, '438468', NULL, '2026-05-27 10:48:36', NULL, NULL);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `pickup_contact_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `amount`, `remark`, `phone`, `pickup_point`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `preparation_mode`, `completed_time`, `fulfillment_fee`, `package_count`, `package_strategy`, `fulfillment_type`, `pickup_code`, `pickup_time`, `pickup_deadline`, `verify_time`, `verify_staff_id`) VALUES (23, 'SFU20260527114342706', 6, 15, 6, '2026-05-27 11:43:42', NULL, 1, 0, 15.13, NULL, '18888888888', '滨江社区前置仓 A 区｜滨江花园北门 12 号自提柜旁', '用户端验收账号', 'test', '用户支付超时，自动取消订单', NULL, '2026-05-27 11:59:00', 1, NULL, 0, 0, 1, 2, '104174', NULL, '2026-05-28 11:43:42', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for pickup_contact
-- ----------------------------
DROP TABLE IF EXISTS `pickup_contact`;
CREATE TABLE `pickup_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `consignee` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '联系人',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `province_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '自提联系备注或所在区域',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标签',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '默认 0 否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_pickup_contact_user_default` (`user_id`,`is_default`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='自提联系人';

-- ----------------------------
-- Records of pickup_contact
-- ----------------------------
BEGIN;
INSERT INTO `pickup_contact` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (2, 5, '周然', '0', '13329064744', '12', '天津市', '1201', '市辖区', '120111', '西城区', '第五大道延长线393号', '2', 1);
INSERT INTO `pickup_contact` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (4, 5, '许宁', '0', '16677778888', '12', '天津市', '1201', '市辖区', '120116', '东城区', '聚源路334号', '1', 0);
INSERT INTO `pickup_contact` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (5, 15, '用户端验收账号', NULL, '13800138111', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '默认自提', 0);
INSERT INTO `pickup_contact` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (6, 15, 'test', NULL, '18888888888', NULL, NULL, NULL, NULL, NULL, NULL, 'test人', 'test', 1);
INSERT INTO `pickup_contact` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (7, 16, '邮箱验收联系人', NULL, '13800138112', NULL, NULL, NULL, NULL, NULL, NULL, '星河湾便利店自提点', '默认自提', 1);
COMMIT;

-- ----------------------------
-- Table structure for pickup_point
-- ----------------------------
DROP TABLE IF EXISTS `pickup_point`;
CREATE TABLE `pickup_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `address` varchar(160) NOT NULL,
  `contact_name` varchar(32) DEFAULT NULL,
  `contact_phone` varchar(32) DEFAULT NULL,
  `service_time` varchar(80) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `create_user` bigint DEFAULT NULL,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pickup_point_name` (`name`),
  KEY `idx_pickup_point_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=9903 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of pickup_point
-- ----------------------------
BEGIN;
INSERT INTO `pickup_point` (`id`, `name`, `address`, `contact_name`, `contact_phone`, `service_time`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, '滨江社区前置仓 A 区', '滨江花园北门 12 号自提柜旁', '社区团长', '13800000001', '09:00-21:00', 10, 1, '2026-05-25 15:39:06', '2026-05-25 15:44:23', 1, 1);
INSERT INTO `pickup_point` (`id`, `name`, `address`, `contact_name`, `contact_phone`, `service_time`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (2, '星河湾社区自提点', '星河湾 3 栋物业服务中心', '值班管家', '13800000002', '10:00-20:30', 20, 1, '2026-05-25 15:39:06', '2026-05-25 15:39:06', 1, 1);
INSERT INTO `pickup_point` (`id`, `name`, `address`, `contact_name`, `contact_phone`, `service_time`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (9901, '滨江花园北门自提点', '滨江花园北门 12 号自提柜旁', '社区团长', '13800000001', '09:00-21:00', 10, 1, '2026-05-26 10:23:53', '2026-05-26 21:08:05', 1, 1);
INSERT INTO `pickup_point` (`id`, `name`, `address`, `contact_name`, `contact_phone`, `service_time`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (9902, '星河湾便利店自提点', '星河湾 3 期 8 栋楼下便利店', '门店店长', '13800000002', '10:00-22:00', 20, 1, '2026-05-26 10:23:53', '2026-05-26 21:08:05', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for pricing_rule
-- ----------------------------
DROP TABLE IF EXISTS `pricing_rule`;
CREATE TABLE `pricing_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '规则名称',
  `min_days_to_expire` int NOT NULL COMMENT '距过期最小天数',
  `max_days_to_expire` int NOT NULL COMMENT '距过期最大天数',
  `discount_rate` decimal(5,2) NOT NULL COMMENT '折扣率',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0停用 1启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=903 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ShelfFlow 动态定价规则';

-- ----------------------------
-- Records of pricing_rule
-- ----------------------------
BEGIN;
INSERT INTO `pricing_rule` (`id`, `name`, `min_days_to_expire`, `max_days_to_expire`, `discount_rate`, `priority`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, '常规临期折扣', 8, 30, 0.85, 10, 1, '2026-04-01 09:00:00', '2026-04-01 09:00:00', 1, 1);
INSERT INTO `pricing_rule` (`id`, `name`, `min_days_to_expire`, `max_days_to_expire`, `discount_rate`, `priority`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (2, '七日清仓折扣', 0, 7, 0.65, 20, 1, '2026-04-01 09:00:00', '2026-05-24 16:33:44', 1, 1);
INSERT INTO `pricing_rule` (`id`, `name`, `min_days_to_expire`, `max_days_to_expire`, `discount_rate`, `priority`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (901, '演示三日清仓价', 0, 3, 0.55, 90, 1, '2026-05-26 10:23:53', '2026-05-26 21:08:05', 1, 1);
INSERT INTO `pricing_rule` (`id`, `name`, `min_days_to_expire`, `max_days_to_expire`, `discount_rate`, `priority`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (902, '演示七日临期价', 4, 7, 0.75, 80, 1, '2026-05-26 10:23:53', '2026-05-26 21:08:05', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '商品分类id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `status` int DEFAULT '1' COMMENT '0 停售 1 起售',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_product_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1914 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='商品';

-- ----------------------------
-- Records of product
-- ----------------------------
BEGIN;
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (46, '临期酸奶 200g', 11, 8.00, 'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png', '临期折扣商品', 0, '2026-04-01 22:40:47', '2026-05-24 20:33:43', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (49, 'deleted-49-全麦吐司 450g', 12, 2.00, 'https://assets.shelfflow.local/76752350-2121-44d2-b477-10791c23a8ec.png', '精选五常大米', -1, '2026-04-02 09:30:17', '2026-05-24 21:23:20', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (51, '黑椒鸡胸轻食盒', 903, 19.90, 'https://images.unsplash.com/photo-1543352634-a1c51d9f1fa7?w=900&auto=format&fit=crop&q=80', '冷藏轻食便当，适合工作日晚间自提。', 1, '2026-04-02 09:40:51', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (52, '藜麦蔬菜轻食盒', 20, 66.00, 'https://assets.shelfflow.local/5260ff39-986c-4a97-8850-2ec8c7583efc.png', '短保鲜食，需冷藏', 1, '2026-04-02 09:46:02', '2026-04-02 09:46:02', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (53, '番茄牛肉便当', 20, 38.00, 'https://assets.shelfflow.local/a6953d5a-4c18-4b30-9319-4926ee77261f.png', '当日加工，冷链履约', 1, '2026-04-02 09:48:37', '2026-04-02 09:48:37', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (54, '有机小青菜', 19, 18.00, 'https://assets.shelfflow.local/3613d38e-5614-41c2-90ed-ff175bf50716.png', '社区生鲜，建议当日售卖', 1, '2026-04-02 09:51:46', '2026-04-02 09:51:46', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (55, '娃娃菜净菜包', 19, 18.00, 'https://assets.shelfflow.local/4879ed66-3860-4b28-ba14-306ac025fdec.png', '净菜包装，建议冷藏', 1, '2026-04-02 09:53:37', '2026-04-02 09:53:37', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (56, '西兰花净菜包', 19, 18.00, 'https://assets.shelfflow.local/e9ec4ba4-4b22-4fc8-9be0-4946e6aeb937.png', '净菜包装，建议冷藏', 1, '2026-04-02 09:55:44', '2026-04-02 09:55:44', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (57, '圆白菜净菜包', 19, 18.00, 'https://assets.shelfflow.local/22f59feb-0d44-430e-a6cd-6a49f27453ca.png', '净菜包装，建议冷藏', 1, '2026-04-02 09:58:35', '2026-04-02 09:58:35', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (58, '低脂鱼排轻食盒', 18, 98.00, 'https://assets.shelfflow.local/c18b5c67-3b71-466c-a75a-e63c6449f21c.png', '低脂轻食，冷藏保存', 1, '2026-04-02 10:12:28', '2026-04-02 10:12:28', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (59, '酱香牛肉熟食包', 18, 138.00, 'https://assets.shelfflow.local/a80a4b8c-c93e-4f43-ac8a-856b0d5cc451.png', '冷藏熟食，开袋即热', 1, '2026-04-02 10:24:03', '2026-04-02 10:24:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (60, '梅干菜扣肉预制包', 18, 58.00, 'https://assets.shelfflow.local/6080b118-e30a-4577-aab4-45042e3f88be.png', '预制熟食，开袋即热', 1, '2026-04-02 10:26:03', '2026-04-02 10:26:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (61, '香辣鱼片预制包', 18, 66.00, 'https://assets.shelfflow.local/13da832f-ef2c-484d-8370-5934a1045a06.png', '预制鲜食，冷冻保存', 1, '2026-04-02 10:28:54', '2026-04-02 10:28:54', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (62, '藤椒鸡块熟食包', 17, 88.00, 'https://assets.shelfflow.local/7694a5d8-7938-4e9d-8b9e-2075983a2e38.png', '冷藏熟食，临期促销', 1, '2026-04-02 10:33:05', '2026-04-02 10:33:05', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (63, '香辣鸡块熟食包', 17, 88.00, 'https://assets.shelfflow.local/f5ac8455-4793-450c-97ba-173795c34626.png', '冷藏熟食，临期促销', 1, '2026-04-02 10:35:40', '2026-04-02 10:35:40', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (64, '孜然鸡块熟食包', 17, 88.00, 'https://assets.shelfflow.local/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png', '冷藏熟食，临期促销', 1, '2026-04-02 10:37:52', '2026-04-02 10:37:52', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (65, '家庭装鱼排 500g', 16, 68.00, 'https://assets.shelfflow.local/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png', '家庭装冷链鲜食', 1, '2026-04-02 10:41:08', '2026-04-02 10:41:08', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (66, '冷冻鱼柳 500g', 16, 119.00, 'https://assets.shelfflow.local/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png', '家庭装冷链鲜食', 0, '2026-04-02 10:42:42', '2025-08-23 17:38:20', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (67, '鲜切鱼柳 400g', 16, 71.00, 'https://assets.shelfflow.local/8cfcc576-4b66-4a09-ac68-ad5b273c2590.png', '家庭装冷链鲜食', 0, '2026-04-02 10:43:56', '2026-05-24 16:25:27', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (68, '即饮燕麦奶', 21, 4.00, 'https://assets.shelfflow.local/c09a0ee8-9d19-428d-81b9-746221824113.png', '植物基饮品，临期折扣', 1, '2026-04-02 10:54:25', '2026-04-02 10:54:25', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (69, '无糖豆乳', 21, 6.00, 'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png', '植物基饮品，临期折扣', 1, '2026-04-02 10:55:02', '2026-04-02 10:55:02', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (70, '气泡水', 11, 3.00, 'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png', '运营测试', 0, '2025-08-16 16:31:01', '2025-08-16 16:31:01', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (72, '柠檬苏打水', 11, 3.00, 'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png', '运营测试', 0, '2025-08-16 16:37:35', '2025-08-16 16:37:35', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (75, '临期果汁 1L', 23, 230.00, 'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png', NULL, 1, '2025-08-17 16:25:29', '2026-05-24 20:49:53', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (76, '橙味气泡水', 23, 250.00, 'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png', '', 0, '2025-08-17 16:32:37', '2025-08-17 16:32:37', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (77, '试运营 SKU', 23, 250.00, 'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png', '', 0, '2025-08-17 16:33:26', '2025-08-17 16:33:26', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (78, '麻薯', 13, 10.90, NULL, 'test', 1, '2026-05-24 14:02:05', '2026-05-24 14:02:05', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (79, '杨枝甘露', 21, 10.00, NULL, NULL, 0, '2026-05-24 16:16:27', '2026-05-24 16:28:12', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1901, '社区特惠低温鲜奶 950ml', 901, 12.90, 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=900&auto=format&fit=crop&q=80', '临期低温乳品，适合社区自提当日带走。', 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1902, '全麦吐司短保装 450g', 902, 9.90, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=900&auto=format&fit=crop&q=80', '短保烘焙商品，按批次折扣售卖。', 1, '2026-05-26 10:23:53', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1903, '社区黑椒鸡胸轻食盒 1盒', 903, 19.90, 'https://images.unsplash.com/photo-1543352634-a1c51d9f1fa7?w=900&auto=format&fit=crop&q=80', '冷藏轻食便当，适合工作日晚间自提。', 1, '2026-05-27 12:58:44', '2026-05-27 12:58:44', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1904, '希腊风味酸奶 200g*4', 901, 18.80, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=900&auto=format&fit=crop&q=80', '冷藏酸奶组合装，适合家庭早餐和加餐。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1905, '燕麦拿铁即饮 300ml', 904, 8.90, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=900&auto=format&fit=crop&q=80', '低糖即饮咖啡，工作日自提更方便。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1906, '鲜切水果杯 350g', 906, 16.90, 'https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=900&auto=format&fit=crop&q=80', '当日鲜切水果，建议当天食用。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1907, '番茄牛肉意面 420g', 903, 22.90, 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=900&auto=format&fit=crop&q=80', '冷藏预制餐，适合晚餐自提。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1908, '低温鲜鸡蛋 10枚', 905, 15.90, 'https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=900&auto=format&fit=crop&q=80', '社区清仓民生商品，库存有限。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1909, '贝果组合装 2枚', 902, 12.50, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=900&auto=format&fit=crop&q=80', '短保烘焙早餐组合，适合次日早餐。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1910, '无糖气泡水 500ml*4', 904, 13.90, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=900&auto=format&fit=crop&q=80', '即饮饮品组合装，适合办公室囤货。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1911, '轻食蔬菜沙拉 300g', 906, 17.90, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=900&auto=format&fit=crop&q=80', '冷藏轻食沙拉，适合当日午餐。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1912, '芝士火腿三明治', 902, 11.90, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=900&auto=format&fit=crop&q=80', '短保三明治，自提后建议尽快食用。', 1, '2026-05-27 11:53:42', '2026-05-27 12:17:03', 1, 1);
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1913, '测试商品', 17, 10.00, NULL, NULL, 1, '2026-05-27 13:08:00', '2026-05-27 13:08:00', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for product_spec
-- ----------------------------
DROP TABLE IF EXISTS `product_spec`;
CREATE TABLE `product_spec` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格名称',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='商品规格表';

-- ----------------------------
-- Records of product_spec
-- ----------------------------
BEGIN;
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (40, 10, '糖度', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (41, 7, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (42, 7, '储存温区', '[\"常温\",\"常温\",\"冷藏\",\"冰鲜\",\"冷冻\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (45, 6, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (46, 6, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (47, 5, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (48, 5, '糖度', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (49, 2, '糖度', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (50, 4, '糖度', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (51, 3, '糖度', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (52, 3, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (86, 52, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (87, 52, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (88, 51, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (89, 51, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (92, 53, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (93, 53, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (94, 54, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (95, 56, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (96, 57, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (97, 60, '偏好', '[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (101, 66, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (102, 67, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (103, 65, '折扣档位', '[\"9折\",\"8折\",\"7折\",\"6折\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (104, 72, '糖度', '[\"无糖\",\"全糖\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (105, 72, '储存温区', '[\"常温\",\"冷冻\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (135, 1901, '规格', '[\"950ml\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (136, 1901, '储存温区', '[\"冷藏\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (137, 1902, '规格', '[\"450g\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (139, 1904, '规格', '[\"200g*4\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (140, 1905, '规格', '[\"300ml\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (141, 1906, '规格', '[\"350g\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (142, 1907, '规格', '[\"420g\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (143, 1908, '规格', '[\"10枚\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (144, 1909, '规格', '[\"2枚\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (145, 1910, '规格', '[\"500ml*4\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (146, 1911, '规格', '[\"300g\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (147, 1912, '规格', '[\"1份\"]');
INSERT INTO `product_spec` (`id`, `product_id`, `name`, `value`) VALUES (148, 1903, '规格', '[\"1盒\"]');
COMMIT;

-- ----------------------------
-- Table structure for schema_migration
-- ----------------------------
DROP TABLE IF EXISTS `schema_migration`;
CREATE TABLE `schema_migration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `applied_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_schema_migration_filename` (`filename`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of schema_migration
-- ----------------------------
BEGIN;
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (1, '20260512_order_fulfillment_columns.sql', '1e99ea557ff3a038584da9631508f2c44e4d7429ae6285b71c7ccaafb28ed51d', '2026-05-24 13:57:06');
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (2, '20260520_user_account_auth.sql', 'ee44bfab69f92d4272b32febb29afc04bdc106393f891fc5acd2fd1599538e66', '2026-05-24 13:57:06');
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (3, '20260522_ai_ops_knowledge.sql', '793655ee3347c78fdd1f81b05df5b5997c3e52869f13c16a00bafa6f4158edf8', '2026-05-24 13:57:06');
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (4, '20260522_order_event_log.sql', 'c8484973e2228473459de1362710a6e50378857bc9741655682a64d20f8a1769', '2026-05-24 13:57:06');
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (5, '20260524_ai_ops_chat_and_suggestions.sql', '7969ca3c43c9ae5c1621070272b4246417235bc0884fa73739ab6d9b7348bfb5', '2026-05-25 15:42:28');
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (6, '20260525_admin_operation_ai_actions.sql', 'b23ca6a066e7028d297f6dd2b4da3526e3901b8938ce86e046f5fc7526c43516', '2026-05-25 15:42:28');
INSERT INTO `schema_migration` (`id`, `filename`, `checksum`, `applied_at`) VALUES (7, '20260525_pickup_points.sql', '254f097c00a3a5fdb855a2cc64cbcf60cf8fb3857589ea2cbf07356e75bd84f9', '2026-05-25 15:42:28');
COMMIT;

-- ----------------------------
-- Table structure for staff
-- ----------------------------
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='ShelfFlow 运营人员信息';

-- ----------------------------
-- Records of staff
-- ----------------------------
BEGIN;
INSERT INTO `staff` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812312312', '1', '110101199001010047', 1, '2026-04-01 15:51:20', '2026-04-02 09:16:20', 10, 1);
INSERT INTO `staff` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (2, 'ops_demo', 'test', 'e10adc3949ba59abbe56e057f20f883e', '13344445555', '', '5', 1, '2025-08-07 17:41:38', '2025-08-07 17:41:38', 10, 10);
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '微信用户唯一标识',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL,
  `password_hash` varchar(100) COLLATE utf8mb3_bin DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_openid` (`openid`),
  UNIQUE KEY `uq_user_email` (`email`),
  UNIQUE KEY `uq_user_phone` (`phone`),
  KEY `idx_user_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='用户信息';

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (4, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (6, 'sf-user-demo-01', '陈晓', '13810000001', NULL, '$2a$10$tgXzbDJ0Xyq6WC5WpsHt7O0OrZ6M481NHUDRUrR.BjfbjgIG/lw2u', 1, NULL, NULL, NULL, '2026-05-24 13:57:49', '2026-05-25 15:43:01');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (7, 'sf-user-demo-02', '林悦', '13810000002', NULL, '$2a$10$WLQ1EPzxY3epuK7xHRDwfOCdqAMF76/VrIwvOBrgr0tnpjAcLOZoe', 1, NULL, NULL, NULL, '2026-05-24 13:57:49', '2026-05-25 15:43:01');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (8, 'sf-user-ui-smoke', '本地测试用户', '13800138000', NULL, '$2a$10$Exx5A4dYk7mFjP4r.m7t1.TbIdYqj8V4aHcRUW2KIiMzFxLpy0XbW', 1, NULL, NULL, NULL, '2026-05-24 13:57:49', '2026-05-26 10:23:53');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (9, 'admin', 'admin', '18923636558', NULL, '$2a$10$/a4MxKA0LTf7feKGtVghqOHtdkNaI1Dyu0LllRFfjMa1cELd/Vhn6', 1, NULL, NULL, NULL, '2026-05-25 20:59:08', '2026-05-25 21:02:17');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (10, 'admin123456', 'admin01', NULL, NULL, '$2a$10$TPGVk9/rxcCf4ysiJZJZJ.mykIpHGcJZiql3iCQCkMfHU2odCBpkq', 1, NULL, NULL, NULL, '2026-05-25 21:09:01', '2026-05-25 21:09:01');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (11, 'admin3333', 'admin', NULL, NULL, '$2a$10$7K0wHLD1blVio8V0U7fIyuT6QnDo7jPH6YkLUhM92aI8r52QfVvna', 1, NULL, NULL, NULL, '2026-05-25 21:11:18', '2026-05-25 21:11:18');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (12, 'sfsadasd', '123123', NULL, NULL, '$2a$10$m5Jehs6H881O25AZSssXT.F7Z6/l7u6hnlS2Ig7H2axCMqfljwKwO', 1, NULL, NULL, NULL, '2026-05-25 21:14:05', '2026-05-25 21:14:05');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (13, 'shelfflow.user@example.com', '邮箱测试用户', NULL, 'shelfflow.user@example.com', '$2a$10$Exx5A4dYk7mFjP4r.m7t1.TbIdYqj8V4aHcRUW2KIiMzFxLpy0XbW', 1, NULL, NULL, NULL, '2026-05-26 10:23:53', '2026-05-26 10:23:53');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (15, '13800138111', '用户端验收账号', '13800138111', NULL, '$2a$10$Sev26X/7JmknMGftSpCGauEcO34fgrah2flv/f/di0Q.iw8m.CArO', 1, NULL, NULL, NULL, '2026-05-26 10:27:04', '2026-05-27 12:17:15');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `email`, `password_hash`, `status`, `sex`, `id_number`, `avatar`, `create_time`, `update_time`) VALUES (16, 'qa.user@shelfflow.local', '用户端邮箱验收账号', NULL, 'qa.user@shelfflow.local', '$2a$10$Sev26X/7JmknMGftSpCGauEcO34fgrah2flv/f/di0Q.iw8m.CArO', 1, NULL, NULL, NULL, '2026-05-26 10:27:04', '2026-05-27 12:17:15');
COMMIT;

-- ----------------------------
-- Table structure for user_verification_code
-- ----------------------------
DROP TABLE IF EXISTS `user_verification_code`;
CREATE TABLE `user_verification_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `purpose` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `consumed_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_verification_code_target` (`target`,`purpose`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of user_verification_code
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
