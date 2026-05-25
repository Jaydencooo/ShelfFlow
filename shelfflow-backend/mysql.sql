-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: shelfflow
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `pickup_contact`
--

DROP TABLE IF EXISTS `pickup_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='自提联系人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pickup_contact`
--

LOCK TABLES `pickup_contact` WRITE;
/*!40000 ALTER TABLE `pickup_contact` DISABLE KEYS */;
INSERT INTO `pickup_contact` VALUES (2,5,'周然','0','13329064744','12','天津市','1201','市辖区','120111','西城区','第五大道延长线393号','2',1),(4,5,'许宁','0','16677778888','12','天津市','1201','市辖区','120116','东城区','聚源路334号','1',0);
/*!40000 ALTER TABLE `pickup_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='ShelfFlow 商品及组合包分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (11,1,'临期乳品',10,1,'2026-04-01 22:09:18','2026-04-01 22:09:18',1,1),(12,1,'烘焙短保',9,1,'2026-04-01 22:09:32','2026-04-01 22:18:53',1,1),(13,2,'高周转组合',12,1,'2026-04-01 22:11:38','2026-04-02 11:04:40',1,1),(15,2,'门店补货组合',13,1,'2026-04-01 22:14:10','2026-04-02 11:04:48',1,1),(16,1,'鲜食便当',4,0,'2026-04-01 22:15:37','2026-04-15 14:27:25',1,1),(17,1,'冷藏熟食',5,1,'2026-04-01 22:16:14','2026-04-15 14:39:44',1,1),(18,1,'冷链轻食',6,1,'2026-04-01 22:17:42','2026-04-01 22:17:42',1,1),(19,1,'社区生鲜',7,0,'2026-04-01 22:18:12','2026-04-01 22:18:28',1,1),(20,1,'预制鲜食',8,1,'2026-04-01 22:22:29','2026-04-01 22:23:45',1,1),(21,1,'即饮饮品',11,1,'2026-04-02 10:51:47','2026-04-02 10:51:47',1,1),(23,1,'清仓专区',11,1,'2025-08-11 14:54:16','2025-08-11 14:54:16',1,1),(25,2,'晚间清仓组合',3,1,'2025-08-11 14:54:47','2025-08-23 17:34:29',1,1);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (46,'临期酸奶 200g',11,8.00,'https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png','临期折扣商品',1,'2026-04-01 22:40:47','2025-09-06 16:20:14',1,1),(47,'低温鲜奶 950ml',11,0.10,'https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png','临期促销，冷藏保存',1,'2026-04-02 09:18:49','2025-08-18 15:52:57',1,1),(48,'精品咖啡液 6杯装',11,4.00,'https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png','',1,'2026-04-02 09:22:54','2026-04-02 09:22:54',1,1),(49,'全麦吐司 450g',12,2.00,'https://assets.shelfflow.local/76752350-2121-44d2-b477-10791c23a8ec.png','精选五常大米',1,'2026-04-02 09:30:17','2026-04-02 09:30:17',1,1),(50,'杂粮餐包 6枚',12,1.00,'https://assets.shelfflow.local/475cc599-8661-4899-8f9e-121dd8ef7d02.png','优质面粉',1,'2026-04-02 09:34:28','2026-04-02 09:34:28',1,1),(51,'黑椒鸡胸轻食盒',20,56.00,'https://assets.shelfflow.local/4a9cefba-6a74-467e-9fde-6e687ea725d7.png','短保鲜食，需冷藏',1,'2026-04-02 09:40:51','2026-04-02 09:40:51',1,1),(52,'藜麦蔬菜轻食盒',20,66.00,'https://assets.shelfflow.local/5260ff39-986c-4a97-8850-2ec8c7583efc.png','短保鲜食，需冷藏',1,'2026-04-02 09:46:02','2026-04-02 09:46:02',1,1),(53,'番茄牛肉便当',20,38.00,'https://assets.shelfflow.local/a6953d5a-4c18-4b30-9319-4926ee77261f.png','当日加工，冷链履约',1,'2026-04-02 09:48:37','2026-04-02 09:48:37',1,1),(54,'有机小青菜',19,18.00,'https://assets.shelfflow.local/3613d38e-5614-41c2-90ed-ff175bf50716.png','社区生鲜，建议当日售卖',1,'2026-04-02 09:51:46','2026-04-02 09:51:46',1,1),(55,'娃娃菜净菜包',19,18.00,'https://assets.shelfflow.local/4879ed66-3860-4b28-ba14-306ac025fdec.png','净菜包装，建议冷藏',1,'2026-04-02 09:53:37','2026-04-02 09:53:37',1,1),(56,'西兰花净菜包',19,18.00,'https://assets.shelfflow.local/e9ec4ba4-4b22-4fc8-9be0-4946e6aeb937.png','净菜包装，建议冷藏',1,'2026-04-02 09:55:44','2026-04-02 09:55:44',1,1),(57,'圆白菜净菜包',19,18.00,'https://assets.shelfflow.local/22f59feb-0d44-430e-a6cd-6a49f27453ca.png','净菜包装，建议冷藏',1,'2026-04-02 09:58:35','2026-04-02 09:58:35',1,1),(58,'低脂鱼排轻食盒',18,98.00,'https://assets.shelfflow.local/c18b5c67-3b71-466c-a75a-e63c6449f21c.png','低脂轻食，冷藏保存',1,'2026-04-02 10:12:28','2026-04-02 10:12:28',1,1),(59,'酱香牛肉熟食包',18,138.00,'https://assets.shelfflow.local/a80a4b8c-c93e-4f43-ac8a-856b0d5cc451.png','冷藏熟食，开袋即热',1,'2026-04-02 10:24:03','2026-04-02 10:24:03',1,1),(60,'梅干菜扣肉预制包',18,58.00,'https://assets.shelfflow.local/6080b118-e30a-4577-aab4-45042e3f88be.png','预制熟食，开袋即热',1,'2026-04-02 10:26:03','2026-04-02 10:26:03',1,1),(61,'香辣鱼片预制包',18,66.00,'https://assets.shelfflow.local/13da832f-ef2c-484d-8370-5934a1045a06.png','预制鲜食，冷冻保存',1,'2026-04-02 10:28:54','2026-04-02 10:28:54',1,1),(62,'藤椒鸡块熟食包',17,88.00,'https://assets.shelfflow.local/7694a5d8-7938-4e9d-8b9e-2075983a2e38.png','冷藏熟食，临期促销',1,'2026-04-02 10:33:05','2026-04-02 10:33:05',1,1),(63,'香辣鸡块熟食包',17,88.00,'https://assets.shelfflow.local/f5ac8455-4793-450c-97ba-173795c34626.png','冷藏熟食，临期促销',1,'2026-04-02 10:35:40','2026-04-02 10:35:40',1,1),(64,'孜然鸡块熟食包',17,88.00,'https://assets.shelfflow.local/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png','冷藏熟食，临期促销',1,'2026-04-02 10:37:52','2026-04-02 10:37:52',1,1),(65,'家庭装鱼排 500g',16,68.00,'https://assets.shelfflow.local/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png','家庭装冷链鲜食',1,'2026-04-02 10:41:08','2026-04-02 10:41:08',1,1),(66,'冷冻鱼柳 500g',16,119.00,'https://assets.shelfflow.local/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png','家庭装冷链鲜食',0,'2026-04-02 10:42:42','2025-08-23 17:38:20',1,1),(67,'鲜切鱼柳 400g',16,72.00,'https://assets.shelfflow.local/8cfcc576-4b66-4a09-ac68-ad5b273c2590.png','家庭装冷链鲜食',1,'2026-04-02 10:43:56','2026-04-02 10:43:56',1,1),(68,'即饮燕麦奶',21,4.00,'https://assets.shelfflow.local/c09a0ee8-9d19-428d-81b9-746221824113.png','植物基饮品，临期折扣',1,'2026-04-02 10:54:25','2026-04-02 10:54:25',1,1),(69,'无糖豆乳',21,6.00,'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','植物基饮品，临期折扣',1,'2026-04-02 10:55:02','2026-04-02 10:55:02',1,1),(70,'气泡水',11,3.00,'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','运营测试',0,'2025-08-16 16:31:01','2025-08-16 16:31:01',1,1),(72,'柠檬苏打水',11,3.00,'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','运营测试',0,'2025-08-16 16:37:35','2025-08-16 16:37:35',1,1),(75,'临期果汁 1L',23,250.00,'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','',1,'2025-08-17 16:25:29','2025-08-23 17:38:22',1,1),(76,'橙味气泡水',23,250.00,'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','',0,'2025-08-17 16:32:37','2025-08-17 16:32:37',1,1),(77,'试运营 SKU',23,250.00,'https://assets.shelfflow.local/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','',0,'2025-08-17 16:33:26','2025-08-17 16:33:26',1,1);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_spec`
--

DROP TABLE IF EXISTS `product_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_spec` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格名称',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '规格数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='商品规格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_spec`
--

LOCK TABLES `product_spec` WRITE;
/*!40000 ALTER TABLE `product_spec` DISABLE KEYS */;
INSERT INTO `product_spec` VALUES (40,10,'糖度','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(41,7,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(42,7,'储存温区','[\"常温\",\"常温\",\"冷藏\",\"冰鲜\",\"冷冻\"]'),(45,6,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(46,6,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(47,5,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(48,5,'糖度','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(49,2,'糖度','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(50,4,'糖度','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(51,3,'糖度','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(52,3,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(86,52,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(87,52,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(88,51,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(89,51,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(92,53,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(93,53,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(94,54,'偏好','[\"不拆封\",\"少包装\",\"环保袋\"]'),(95,56,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(96,57,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(97,60,'偏好','[\"不拆封\",\"少包装\",\"环保袋\",\"无需袋装\"]'),(101,66,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(102,67,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(103,65,'折扣档位','[\"9折\",\"8折\",\"7折\",\"6折\"]'),(104,72,'糖度','[\"无糖\",\"全糖\"]'),(105,72,'储存温区','[\"常温\",\"冷冻\"]');
/*!40000 ALTER TABLE `product_spec` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_batch`
--

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ShelfFlow 库存批次';

INSERT INTO `inventory_batch` VALUES
(1,46,'SF-BATCH-202604-001','2026-04-01 08:00:00','2026-05-01 23:59:59',120,0,12,1,'2026-04-01 09:00:00','2026-04-01 09:00:00',1,1),
(2,49,'SF-BATCH-202604-002','2026-04-02 08:00:00','2026-04-12 23:59:59',80,0,20,1,'2026-04-02 09:00:00','2026-04-02 09:00:00',1,1),
(3,53,'SF-BATCH-202604-003','2026-04-02 08:00:00','2026-04-08 23:59:59',45,0,15,1,'2026-04-02 09:00:00','2026-04-02 09:00:00',1,1);

--
-- Table structure for table `pricing_rule`
--

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ShelfFlow 动态定价规则';

INSERT INTO `pricing_rule` VALUES
(1,'常规临期折扣',8,30,0.85,10,1,'2026-04-01 09:00:00','2026-04-01 09:00:00',1,1),
(2,'七日清仓折扣',3,7,0.70,20,1,'2026-04-01 09:00:00','2026-04-01 09:00:00',1,1),
(3,'三日极速流转',0,2,0.50,30,1,'2026-04-01 09:00:00','2026-04-01 09:00:00',1,1);

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,'管理员','admin','e10adc3949ba59abbe56e057f20f883e','13812312312','1','110101199001010047',1,'2026-04-01 15:51:20','2026-04-02 09:16:20',10,1),(2,'ops_demo','test','e10adc3949ba59abbe56e057f20f883e','13344445555','','5',1,'2025-08-07 17:41:38','2025-08-07 17:41:38',10,10);
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_detail`
--

DROP TABLE IF EXISTS `order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单商品明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
INSERT INTO `order_detail` (`id`,`name`,`image`,`order_id`,`product_id`,`bundle_id`,`product_spec`,`number`,`amount`) VALUES (5,'香辣鸡块熟食包','https://assets.shelfflow.local/f5ac8455-4793-450c-97ba-173795c34626.png',4,63,NULL,NULL,1,88.00),(6,'孜然鸡块熟食包','https://assets.shelfflow.local/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png',4,64,NULL,NULL,4,88.00),(7,'全麦吐司 450g','https://assets.shelfflow.local/76752350-2121-44d2-b477-10791c23a8ec.png',5,49,NULL,NULL,1,2.00),(8,'杂粮餐包 6枚','https://assets.shelfflow.local/475cc599-8661-4899-8f9e-121dd8ef7d02.png',5,50,NULL,NULL,1,1.00),(9,'精品咖啡液 6杯装','https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png',6,48,NULL,NULL,1,4.00),(10,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',6,46,NULL,NULL,1,8.00),(11,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',7,46,NULL,NULL,1,8.00),(12,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',7,47,NULL,NULL,1,0.10),(13,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',8,47,NULL,NULL,1,0.10),(14,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',9,46,NULL,NULL,1,8.00),(15,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',9,47,NULL,NULL,1,0.10),(16,'精品咖啡液 6杯装','https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png',9,48,NULL,NULL,1,4.00),(17,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',10,46,NULL,NULL,1,8.00),(18,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',10,47,NULL,NULL,1,0.10),(19,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',11,46,NULL,NULL,1,8.00),(20,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',12,47,NULL,NULL,1,0.10),(21,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',13,46,NULL,NULL,1,8.00),(22,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',13,47,NULL,NULL,1,0.10),(23,'精品咖啡液 6杯装','https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png',14,48,NULL,NULL,1,4.00),(24,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',14,46,NULL,NULL,1,8.00),(25,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',15,46,NULL,NULL,1,8.00),(26,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',16,46,NULL,NULL,1,8.00),(27,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',16,47,NULL,NULL,1,0.10),(28,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',17,46,NULL,NULL,1,8.00),(29,'家庭装鱼排 500g','https://assets.shelfflow.local/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png',18,65,NULL,'6折',1,68.00),(30,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',19,46,NULL,NULL,1,8.00),(31,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',19,47,NULL,NULL,1,0.10),(32,'精品咖啡液 6杯装','https://assets.shelfflow.local/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png',19,48,NULL,NULL,6,4.00),(33,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',20,47,NULL,NULL,1,0.10),(34,'临期酸奶 200g','https://assets.shelfflow.local/41bfcacf-7ad4-4927-8b26-df366553a94c.png',21,46,NULL,NULL,1,8.00),(35,'低温鲜奶 950ml','https://assets.shelfflow.local/4451d4be-89a2-4939-9c69-3a87151cb979.png',21,47,NULL,NULL,1,0.10);
/*!40000 ALTER TABLE `order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  `pickup_code` varchar(12) DEFAULT NULL COMMENT '自提核销码',
  `pickup_time` datetime DEFAULT NULL COMMENT '预约自提时间',
  `pickup_deadline` datetime DEFAULT NULL COMMENT '自提截止时间',
  `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
  `verify_staff_id` bigint DEFAULT NULL COMMENT '核销运营人员id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='履约订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` (`id`,`number`,`status`,`user_id`,`pickup_contact_id`,`order_time`,`checkout_time`,`pay_method`,`pay_status`,`amount`,`remark`,`phone`,`pickup_point`,`user_name`,`consignee`,`cancel_reason`,`rejection_reason`,`cancel_time`,`preparation_mode`,`completed_time`,`fulfillment_fee`,`package_count`,`package_strategy`,`fulfillment_type`,`pickup_code`,`pickup_time`,`pickup_deadline`,`verify_time`,`verify_staff_id`) VALUES
(1,'SF202604280001',4,5,NULL,'2026-04-28 10:00:00','2026-04-28 10:01:00',1,1,28.00,'到店自提','16629064744','滨江社区前置仓 A 区',NULL,'林可',NULL,NULL,NULL,1,NULL,0,1,1,2,'482913','2026-04-28 18:00:00','2026-04-28 20:00:00',NULL,NULL),
(2,'SF202604280002',5,5,NULL,'2026-04-28 11:00:00','2026-04-28 11:02:00',1,1,16.00,'已核销','16629064744','滨江社区前置仓 A 区',NULL,'林可',NULL,NULL,NULL,1,'2026-04-28 12:05:00',0,1,1,2,'735921','2026-04-28 12:00:00','2026-04-28 14:00:00','2026-04-28 12:05:00',1);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fulfillment_task`
--

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ShelfFlow 履约任务';

LOCK TABLES `fulfillment_task` WRITE;
/*!40000 ALTER TABLE `fulfillment_task` DISABLE KEYS */;
INSERT INTO `fulfillment_task` (`id`,`order_id`,`order_number`,`pickup_code`,`status`,`pickup_deadline`,`completed_time`,`create_time`,`update_time`) VALUES
(1,1,'SF202604280001','482913',2,'2026-04-28 20:00:00',NULL,'2026-04-28 10:01:00','2026-04-28 10:05:00'),
(2,2,'SF202604280002','735921',3,'2026-04-28 14:00:00','2026-04-28 12:05:00','2026-04-28 11:02:00','2026-04-28 12:05:00');
/*!40000 ALTER TABLE `fulfillment_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bundle`
--

DROP TABLE IF EXISTS `bundle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bundle`
--

LOCK TABLES `bundle` WRITE;
/*!40000 ALTER TABLE `bundle` DISABLE KEYS */;
INSERT INTO `bundle` VALUES (32,15,'组合包1test',155.00,1,'描述随便','https://america2030.oss-cn-beijing.aliyuncs.com/a336c6f7-0337-418d-ac1b-fb3f0d9776e4.jpg',NULL,'2025-09-06 16:20:17',NULL,1);
/*!40000 ALTER TABLE `bundle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bundle_product`
--

DROP TABLE IF EXISTS `bundle_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bundle_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bundle_id` bigint DEFAULT NULL COMMENT '组合包id',
  `product_id` bigint DEFAULT NULL COMMENT '商品id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称 （冗余字段）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品单价（冗余字段）',
  `copies` int DEFAULT NULL COMMENT '商品份数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='组合包商品关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bundle_product`
--

LOCK TABLES `bundle_product` WRITE;
/*!40000 ALTER TABLE `bundle_product` DISABLE KEYS */;
INSERT INTO `bundle_product` VALUES (55,32,48,'精品咖啡液 6杯装',4.00,1),(56,32,46,'临期酸奶 200g',8.00,1);
/*!40000 ALTER TABLE `bundle_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='选品车';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '微信用户唯一标识',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='用户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (4,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-12 11:32:00
