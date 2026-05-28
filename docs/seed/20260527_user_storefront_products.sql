-- Storefront QA catalog data.
-- Purpose: provide enough real sellable products for user-side storefront verification.

INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(901, 1, '社区临期乳品', 10, 1, NOW(), NOW(), 1, 1),
(902, 1, '短保烘焙鲜食', 20, 1, NOW(), NOW(), 1, 1),
(903, 1, '自提轻食便当', 30, 1, NOW(), NOW(), 1, 1),
(904, 1, '即饮饮品', 40, 1, NOW(), NOW(), 1, 1),
(905, 1, '清仓专区', 50, 1, NOW(), NOW(), 1, 1),
(906, 1, '预制鲜食', 60, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`type` = VALUES(`type`),
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
`category_id` = VALUES(`category_id`),
`price` = VALUES(`price`),
`image` = VALUES(`image`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`update_time` = NOW(),
`update_user` = VALUES(`update_user`);

DELETE FROM `product_spec` WHERE `product_id` BETWEEN 1901 AND 1912;

INSERT INTO `product_spec` (`product_id`, `name`, `value`) VALUES
(1901, '规格', '["950ml"]'),
(1901, '储存温区', '["冷藏"]'),
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
(1901, 'SF-DEMO-MILK-001', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 60, 0, 0, 1, NOW(), NOW(), 1, 1),
(1902, 'SF-DEMO-BREAD-001', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 35, 0, 0, 1, NOW(), NOW(), 1, 1),
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
