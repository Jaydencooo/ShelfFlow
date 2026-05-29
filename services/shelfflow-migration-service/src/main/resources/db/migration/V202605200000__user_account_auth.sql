SET @user_password_hash_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'password_hash'
);
SET @user_password_hash_column_sql := IF(
    @user_password_hash_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `password_hash` VARCHAR(100) NULL AFTER `phone`',
    'SELECT 1'
);
PREPARE user_password_hash_column_stmt FROM @user_password_hash_column_sql;
EXECUTE user_password_hash_column_stmt;
DEALLOCATE PREPARE user_password_hash_column_stmt;

SET @user_status_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'status'
);
SET @user_status_column_sql := IF(
    @user_status_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 AFTER `password_hash`',
    'SELECT 1'
);
PREPARE user_status_column_stmt FROM @user_status_column_sql;
EXECUTE user_status_column_stmt;
DEALLOCATE PREPARE user_status_column_stmt;

SET @user_update_time_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'update_time'
);
SET @user_update_time_column_sql := IF(
    @user_update_time_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `update_time` DATETIME NULL AFTER `create_time`',
    'SELECT 1'
);
PREPARE user_update_time_column_stmt FROM @user_update_time_column_sql;
EXECUTE user_update_time_column_stmt;
DEALLOCATE PREPARE user_update_time_column_stmt;

SET @user_openid_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'uq_user_openid'
);
SET @user_openid_index_sql := IF(
    @user_openid_index_exists = 0,
    'CREATE UNIQUE INDEX uq_user_openid ON `user` (`openid`)',
    'SELECT 1'
);
PREPARE user_openid_index_stmt FROM @user_openid_index_sql;
EXECUTE user_openid_index_stmt;
DEALLOCATE PREPARE user_openid_index_stmt;

SET @user_phone_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'idx_user_phone'
);
SET @user_phone_index_sql := IF(
    @user_phone_index_exists = 0,
    'CREATE INDEX idx_user_phone ON `user` (`phone`)',
    'SELECT 1'
);
PREPARE user_phone_index_stmt FROM @user_phone_index_sql;
EXECUTE user_phone_index_stmt;
DEALLOCATE PREPARE user_phone_index_stmt;
