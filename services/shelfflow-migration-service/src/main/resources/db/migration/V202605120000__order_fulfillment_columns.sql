-- ShelfFlow order fulfillment column cleanup.
-- This migration is idempotent and only rewrites columns when the legacy names still exist.

SET @orders_delivery_status_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'delivery_status'
);
SET @orders_delivery_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'delivery_time'
);
SET @orders_address_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'address'
);
SET @orders_pack_amount_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'pack_amount'
);
SET @orders_tableware_number_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'tableware_number'
);
SET @orders_tableware_status_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'tableware_status'
);

SET @orders_fulfillment_alter_sql := CONCAT(
    'ALTER TABLE `orders`',
    IF(@orders_delivery_status_exists > 0,
       ' CHANGE COLUMN `delivery_status` `preparation_mode` tinyint(1) NOT NULL DEFAULT ''1'' COMMENT ''履约准备模式 1立即备货 0预约自提''',
       ''),
    IF(@orders_delivery_time_exists > 0,
       CONCAT(IF(@orders_delivery_status_exists > 0, ',', ''), ' CHANGE COLUMN `delivery_time` `completed_time` datetime DEFAULT NULL COMMENT ''订单完成时间'''),
       ''),
    IF(@orders_address_exists > 0,
       CONCAT(
         IF(@orders_delivery_status_exists > 0 OR @orders_delivery_time_exists > 0, ',', ''),
         ' CHANGE COLUMN `address` `pickup_point` varchar(255) DEFAULT NULL COMMENT ''自提履约点'''
       ),
       ''),
    IF(@orders_pack_amount_exists > 0,
       CONCAT(
         IF(@orders_delivery_status_exists > 0 OR @orders_delivery_time_exists > 0 OR @orders_address_exists > 0, ',', ''),
         ' CHANGE COLUMN `pack_amount` `fulfillment_fee` int DEFAULT NULL COMMENT ''履约服务费'''
       ),
       ''),
    IF(@orders_tableware_number_exists > 0,
       CONCAT(
         IF(@orders_delivery_status_exists > 0 OR @orders_delivery_time_exists > 0 OR @orders_address_exists > 0 OR @orders_pack_amount_exists > 0, ',', ''),
         ' CHANGE COLUMN `tableware_number` `package_count` int DEFAULT NULL COMMENT ''履约包装数量'''
       ),
       ''),
    IF(@orders_tableware_status_exists > 0,
       CONCAT(
         IF(@orders_delivery_status_exists > 0 OR @orders_delivery_time_exists > 0 OR @orders_address_exists > 0 OR @orders_pack_amount_exists > 0 OR @orders_tableware_number_exists > 0, ',', ''),
         ' CHANGE COLUMN `tableware_status` `package_strategy` tinyint(1) NOT NULL DEFAULT ''1'' COMMENT ''履约包装策略 1按商品数量提供 0选择具体数量'''
       ),
       '')
);

SET @orders_fulfillment_alter_sql := IF(
    @orders_delivery_status_exists > 0
    OR @orders_delivery_time_exists > 0
    OR @orders_address_exists > 0
    OR @orders_pack_amount_exists > 0
    OR @orders_tableware_number_exists > 0
    OR @orders_tableware_status_exists > 0,
    @orders_fulfillment_alter_sql,
    'SELECT 1'
);

PREPARE orders_fulfillment_alter_stmt FROM @orders_fulfillment_alter_sql;
EXECUTE orders_fulfillment_alter_stmt;
DEALLOCATE PREPARE orders_fulfillment_alter_stmt;

SET @orders_estimated_delivery_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'estimated_delivery_time'
);
SET @orders_estimated_delivery_time_sql := IF(
    @orders_estimated_delivery_time_exists > 0,
    'ALTER TABLE `orders` DROP COLUMN `estimated_delivery_time`',
    'SELECT 1'
);
PREPARE orders_estimated_delivery_time_stmt FROM @orders_estimated_delivery_time_sql;
EXECUTE orders_estimated_delivery_time_stmt;
DEALLOCATE PREPARE orders_estimated_delivery_time_stmt;
