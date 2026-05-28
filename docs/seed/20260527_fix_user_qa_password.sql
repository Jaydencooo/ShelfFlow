-- Fix QA user password hashes.
-- Plaintext password for both accounts: Passw0rd!

UPDATE `user`
SET
    `password_hash` = '$2a$10$Sev26X/7JmknMGftSpCGauEcO34fgrah2flv/f/di0Q.iw8m.CArO',
    `status` = 1,
    `update_time` = NOW()
WHERE `openid` IN ('13800138111', 'qa.user@shelfflow.local')
   OR `phone` = '13800138111'
   OR `email` = 'qa.user@shelfflow.local';
