-- Ensure pickup contact table supports the user-service contact model.
-- Safe to run repeatedly in Navicat/MySQL CLI after selecting the shelfflow database.

SET @pickup_contact_label_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'pickup_contact'
      AND column_name = 'label'
);
SET @pickup_contact_label_column_sql := IF(
    @pickup_contact_label_column_exists = 0,
    'ALTER TABLE `pickup_contact` ADD COLUMN `label` VARCHAR(16) NULL AFTER `phone`',
    'SELECT 1'
);
PREPARE pickup_contact_label_column_stmt FROM @pickup_contact_label_column_sql;
EXECUTE pickup_contact_label_column_stmt;
DEALLOCATE PREPARE pickup_contact_label_column_stmt;

SET @pickup_contact_detail_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'pickup_contact'
      AND column_name = 'detail'
);
SET @pickup_contact_detail_column_sql := IF(
    @pickup_contact_detail_column_exists = 0,
    'ALTER TABLE `pickup_contact` ADD COLUMN `detail` VARCHAR(120) NULL AFTER `label`',
    'SELECT 1'
);
PREPARE pickup_contact_detail_column_stmt FROM @pickup_contact_detail_column_sql;
EXECUTE pickup_contact_detail_column_stmt;
DEALLOCATE PREPARE pickup_contact_detail_column_stmt;

SET @pickup_contact_is_default_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'pickup_contact'
      AND column_name = 'is_default'
);
SET @pickup_contact_is_default_column_sql := IF(
    @pickup_contact_is_default_column_exists = 0,
    'ALTER TABLE `pickup_contact` ADD COLUMN `is_default` TINYINT NOT NULL DEFAULT 0 AFTER `detail`',
    'SELECT 1'
);
PREPARE pickup_contact_is_default_column_stmt FROM @pickup_contact_is_default_column_sql;
EXECUTE pickup_contact_is_default_column_stmt;
DEALLOCATE PREPARE pickup_contact_is_default_column_stmt;

SET @pickup_contact_user_default_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'pickup_contact'
      AND index_name = 'idx_pickup_contact_user_default'
);
SET @pickup_contact_user_default_index_sql := IF(
    @pickup_contact_user_default_index_exists = 0,
    'CREATE INDEX idx_pickup_contact_user_default ON `pickup_contact` (`user_id`, `is_default`)',
    'SELECT 1'
);
PREPARE pickup_contact_user_default_index_stmt FROM @pickup_contact_user_default_index_sql;
EXECUTE pickup_contact_user_default_index_stmt;
DEALLOCATE PREPARE pickup_contact_user_default_index_stmt;
