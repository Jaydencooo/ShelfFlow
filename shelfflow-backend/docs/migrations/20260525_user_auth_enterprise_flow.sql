-- User enterprise auth migration.
-- Safe to run repeatedly in Navicat/MySQL CLI after selecting the shelfflow database.

SET @user_email_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'email'
);
SET @user_email_column_sql := IF(
    @user_email_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) NULL AFTER `phone`',
    'SELECT 1'
);
PREPARE user_email_column_stmt FROM @user_email_column_sql;
EXECUTE user_email_column_stmt;
DEALLOCATE PREPARE user_email_column_stmt;

-- Normalize empty values before unique indexes. MySQL unique indexes allow multiple NULL values.
UPDATE `user`
SET `phone` = NULL
WHERE `phone` = '';

UPDATE `user`
SET `email` = NULL
WHERE `email` = '';

-- If old local data contains duplicated phone/email values, keep the earliest user and clear the rest.
UPDATE `user` u
JOIN (
    SELECT phone, MIN(id) AS keep_id
    FROM `user`
    WHERE phone IS NOT NULL
    GROUP BY phone
    HAVING COUNT(*) > 1
) duplicated_phone ON duplicated_phone.phone = u.phone
SET u.phone = NULL
WHERE u.id <> duplicated_phone.keep_id;

UPDATE `user` u
JOIN (
    SELECT email, MIN(id) AS keep_id
    FROM `user`
    WHERE email IS NOT NULL
    GROUP BY email
    HAVING COUNT(*) > 1
) duplicated_email ON duplicated_email.email = u.email
SET u.email = NULL
WHERE u.id <> duplicated_email.keep_id;

SET @user_email_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'uq_user_email'
);
SET @user_email_index_sql := IF(
    @user_email_index_exists = 0,
    'CREATE UNIQUE INDEX uq_user_email ON `user` (`email`)',
    'SELECT 1'
);
PREPARE user_email_index_stmt FROM @user_email_index_sql;
EXECUTE user_email_index_stmt;
DEALLOCATE PREPARE user_email_index_stmt;

SET @user_phone_unique_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND index_name = 'uq_user_phone'
);
SET @user_phone_unique_index_sql := IF(
    @user_phone_unique_index_exists = 0,
    'CREATE UNIQUE INDEX uq_user_phone ON `user` (`phone`)',
    'SELECT 1'
);
PREPARE user_phone_unique_index_stmt FROM @user_phone_unique_index_sql;
EXECUTE user_phone_unique_index_stmt;
DEALLOCATE PREPARE user_phone_unique_index_stmt;

CREATE TABLE IF NOT EXISTS `user_verification_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `target` VARCHAR(100) NOT NULL,
    `purpose` VARCHAR(32) NOT NULL,
    `code_hash` VARCHAR(100) NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `consumed_at` DATETIME NULL,
    `create_time` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_user_verification_code_target` (`target`, `purpose`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
