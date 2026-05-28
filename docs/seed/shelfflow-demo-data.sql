-- ShelfFlow demo seed data for local verification.
-- Run migrations first, then execute this file in Navicat or MySQL CLI.

INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(901, 1, '社区临期乳品', 10, 1, NOW(), NOW(), 1, 1),
(902, 1, '短保烘焙鲜食', 20, 1, NOW(), NOW(), 1, 1),
(903, 1, '自提轻食便当', 30, 1, NOW(), NOW(), 1, 1),
(904, 1, '即饮饮品', 40, 1, NOW(), NOW(), 1, 1),
(905, 1, '清仓专区', 50, 1, NOW(), NOW(), 1, 1),
(906, 1, '预制鲜食', 60, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`update_time` = NOW(),
`update_user` = VALUES(`update_user`);

INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(1901, '社区特惠低温鲜奶 950ml', 901, 12.90, 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=900&auto=format&fit=crop&q=80', '临期低温乳品，适合社区自提当日带走。', 1, NOW(), NOW(), 1, 1),
(1902, '全麦吐司短保装 450g', 902, 9.90, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=900&auto=format&fit=crop&q=80', '短保烘焙商品，按批次折扣售卖。', 1, NOW(), NOW(), 1, 1),
(1903, '黑椒鸡胸轻食盒', 903, 19.90, 'https://images.unsplash.com/photo-1543352634-a1c51d9f1fa7?w=900&auto=format&fit=crop&q=80', '冷藏轻食便当，适合工作日晚间自提。', 1, NOW(), NOW(), 1, 1),
(1904, '希腊风味酸奶 200g*4', 901, 18.80, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=900&auto=format&fit=crop&q=80', '冷藏酸奶组合装，适合家庭早餐和加餐。', 1, NOW(), NOW(), 1, 1),
(1905, '燕麦拿铁即饮 300ml', 904, 8.90, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=900&auto=format&fit=crop&q=80', '低糖即饮咖啡，工作日自提更方便。', 1, NOW(), NOW(), 1, 1),
(1906, '鲜切水果杯 350g', 906, 16.90, 'https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=900&auto=format&fit=crop&q=80', '当日鲜切水果，建议当天食用。', 1, NOW(), NOW(), 1, 1),
(1907, '番茄牛肉意面 420g', 903, 22.90, 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=900&auto=format&fit=crop&q=80', '冷藏预制餐，适合晚餐自提。', 1, NOW(), NOW(), 1, 1),
(1908, '低温鲜鸡蛋 10枚', 905, 15.90, 'https://images.unsplash.com/photo-1518569656558-1f25e69d93d7?w=900&auto=format&fit=crop&q=80', '社区清仓民生商品，库存有限。', 1, NOW(), NOW(), 1, 1),
(1909, '贝果组合装 2枚', 902, 12.50, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=900&auto=format&fit=crop&q=80', '短保烘焙早餐组合，适合次日早餐。', 1, NOW(), NOW(), 1, 1),
(1910, '无糖气泡水 500ml*4', 904, 13.90, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=900&auto=format&fit=crop&q=80', '即饮饮品组合装，适合办公室囤货。', 1, NOW(), NOW(), 1, 1),
(1911, '轻食蔬菜沙拉 300g', 906, 17.90, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=900&auto=format&fit=crop&q=80', '冷藏轻食沙拉，适合当日午餐。', 1, NOW(), NOW(), 1, 1),
(1912, '芝士火腿三明治', 902, 11.90, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=900&auto=format&fit=crop&q=80', '短保三明治，自提后建议尽快食用。', 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`category_id` = VALUES(`category_id`),
`price` = VALUES(`price`),
`image` = VALUES(`image`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`update_time` = NOW(),
`update_user` = VALUES(`update_user`);

DELETE FROM `product_spec` WHERE `product_id` BETWEEN 1901 AND 1912;

INSERT INTO `product_spec` (`product_id`, `name`, `value`) VALUES
(1901, '储存温区', '["冷藏"]'),
(1901, '规格', '["950ml"]'),
(1902, '规格', '["450g"]'),
(1903, '规格', '["1盒"]'),
(1904, '规格', '["200g*4"]'),
(1905, '规格', '["300ml"]'),
(1906, '规格', '["350g"]'),
(1907, '规格', '["420g"]'),
(1908, '规格', '["10枚"]'),
(1909, '规格', '["2枚"]'),
(1910, '规格', '["500ml*4"]'),
(1911, '规格', '["300g"]'),
(1912, '规格', '["1份"]');

INSERT INTO `inventory_batch` (`product_id`, `batch_code`, `production_time`, `expiration_time`, `stock_quantity`, `locked_quantity`, `sold_quantity`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(1901, 'SF-DEMO-MILK-001', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 60, 0, 0, 1, NOW(), NOW(), 1, 1),
(1902, 'SF-DEMO-BREAD-001', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 35, 0, 0, 1, NOW(), NOW(), 1, 1),
(1903, 'SF-DEMO-LIGHTMEAL-001', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 40, 0, 0, 1, NOW(), NOW(), 1, 1),
(1904, 'SF-DEMO-YOGURT-001', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 48, 0, 0, 1, NOW(), NOW(), 1, 1),
(1905, 'SF-DEMO-LATTE-001', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 72, 0, 0, 1, NOW(), NOW(), 1, 1),
(1906, 'SF-DEMO-FRUIT-001', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 24, 0, 0, 1, NOW(), NOW(), 1, 1),
(1907, 'SF-DEMO-PASTA-001', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 32, 0, 0, 1, NOW(), NOW(), 1, 1),
(1908, 'SF-DEMO-EGG-001', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 80, 0, 0, 1, NOW(), NOW(), 1, 1),
(1909, 'SF-DEMO-BAGEL-001', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 30, 0, 0, 1, NOW(), NOW(), 1, 1),
(1910, 'SF-DEMO-SPARKLING-001', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 90, 0, 0, 1, NOW(), NOW(), 1, 1),
(1911, 'SF-DEMO-SALAD-001', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 26, 0, 0, 1, NOW(), NOW(), 1, 1),
(1912, 'SF-DEMO-SANDWICH-001', NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 34, 0, 0, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
`expiration_time` = VALUES(`expiration_time`),
`stock_quantity` = VALUES(`stock_quantity`),
`locked_quantity` = VALUES(`locked_quantity`),
`sold_quantity` = VALUES(`sold_quantity`),
`status` = VALUES(`status`),
`update_time` = NOW(),
`update_user` = VALUES(`update_user`);

INSERT INTO `pricing_rule` (`id`, `name`, `min_days_to_expire`, `max_days_to_expire`, `discount_rate`, `priority`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(901, '演示三日清仓价', 0, 3, 0.55, 90, 1, NOW(), NOW(), 1, 1),
(902, '演示七日临期价', 4, 7, 0.75, 80, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`min_days_to_expire` = VALUES(`min_days_to_expire`),
`max_days_to_expire` = VALUES(`max_days_to_expire`),
`discount_rate` = VALUES(`discount_rate`),
`priority` = VALUES(`priority`),
`status` = VALUES(`status`),
`update_time` = NOW(),
`update_user` = VALUES(`update_user`);

INSERT INTO `pickup_point` (`id`, `name`, `address`, `contact_name`, `contact_phone`, `service_time`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(9901, '滨江花园北门自提点', '滨江花园北门 12 号自提柜旁', '社区团长', '13800000001', '09:00-21:00', 10, 1, NOW(), NOW(), 1, 1),
(9902, '星河湾便利店自提点', '星河湾 3 期 8 栋楼下便利店', '门店店长', '13800000002', '10:00-22:00', 20, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`address` = VALUES(`address`),
`contact_name` = VALUES(`contact_name`),
`contact_phone` = VALUES(`contact_phone`),
`service_time` = VALUES(`service_time`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`update_time` = NOW(),
`update_user` = VALUES(`update_user`);

-- Dedicated user-side QA accounts.
-- Plaintext password for both accounts: Passw0rd!
INSERT INTO `user` (`openid`, `name`, `phone`, `email`, `password_hash`, `status`, `create_time`, `update_time`) VALUES
('13800138111', '用户端验收账号', '13800138111', NULL, '$2a$10$Sev26X/7JmknMGftSpCGauEcO34fgrah2flv/f/di0Q.iw8m.CArO', 1, NOW(), NOW()),
('qa.user@shelfflow.local', '用户端邮箱验收账号', NULL, 'qa.user@shelfflow.local', '$2a$10$Sev26X/7JmknMGftSpCGauEcO34fgrah2flv/f/di0Q.iw8m.CArO', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`phone` = VALUES(`phone`),
`email` = VALUES(`email`),
`password_hash` = VALUES(`password_hash`),
`status` = VALUES(`status`),
`update_time` = NOW();

SET @demo_phone_user_id := (
    SELECT `id`
    FROM `user`
    WHERE `phone` = '13800138111'
    LIMIT 1
);

INSERT INTO `pickup_contact` (`user_id`, `consignee`, `phone`, `label`, `detail`, `is_default`)
SELECT @demo_phone_user_id, '用户端验收账号', '13800138111', '默认自提', '滨江花园北门自提点', 1
WHERE @demo_phone_user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `pickup_contact`
      WHERE `user_id` = @demo_phone_user_id
        AND `phone` = '13800138111'
        AND `label` = '默认自提'
  );

SET @demo_email_user_id := (
    SELECT `id`
    FROM `user`
    WHERE `email` = 'qa.user@shelfflow.local'
    LIMIT 1
);

INSERT INTO `pickup_contact` (`user_id`, `consignee`, `phone`, `label`, `detail`, `is_default`)
SELECT @demo_email_user_id, '邮箱验收联系人', '13800138112', '默认自提', '星河湾便利店自提点', 1
WHERE @demo_email_user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `pickup_contact`
      WHERE `user_id` = @demo_email_user_id
        AND `phone` = '13800138112'
        AND `label` = '默认自提'
  );
