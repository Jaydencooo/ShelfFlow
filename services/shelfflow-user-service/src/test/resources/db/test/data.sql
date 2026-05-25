INSERT INTO category(id, type, name, sort, status) VALUES
(11, 1, '乳品专区', 10, 1),
(12, 1, '烘焙专区', 20, 1),
(13, 1, '禁用分类', 30, 0),
(21, 2, '组合包分类', 5, 1);

INSERT INTO product(id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(1001, 'Fresh Milk', 11, 12.50, 'https://img/milk.png', '鲜牛奶', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(1002, 'Baguette', 12, 9.90, 'https://img/baguette.png', '法棍面包', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(1003, 'Disabled Product', 11, 8.80, 'https://img/disabled.png', '停售商品', 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(1004, 'No Stock Product', 11, 15.00, 'https://img/nostock.png', '无库存商品', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1);

INSERT INTO product_spec(id, product_id, name, value) VALUES
(3001, 1001, 'storageTemp', '["chilled"]'),
(3002, 1001, 'size', '["250ml","1L"]'),
(3003, 1002, 'portion', '["1pc"]');

INSERT INTO inventory_batch(id, product_id, batch_code, production_time, expiration_time, stock_quantity, locked_quantity, sold_quantity, status, create_time, update_time, create_user, update_user) VALUES
(2001, 1001, 'BATCH-MILK-001', CURRENT_TIMESTAMP(), DATEADD('DAY', 5, CURRENT_TIMESTAMP()), 20, 2, 3, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(2002, 1001, 'BATCH-MILK-002', CURRENT_TIMESTAMP(), DATEADD('DAY', 10, CURRENT_TIMESTAMP()), 8, 0, 1, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(2003, 1002, 'BATCH-BREAD-001', CURRENT_TIMESTAMP(), DATEADD('DAY', 1, CURRENT_TIMESTAMP()), 6, 0, 1, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(2004, 1003, 'BATCH-DISABLED-001', CURRENT_TIMESTAMP(), DATEADD('DAY', 3, CURRENT_TIMESTAMP()), 12, 0, 0, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1),
(2005, 1004, 'BATCH-NOSTOCK-001', CURRENT_TIMESTAMP(), DATEADD('DAY', 2, CURRENT_TIMESTAMP()), 5, 3, 2, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, 1);

INSERT INTO pricing_rule(id, min_days_to_expire, max_days_to_expire, discount_rate, priority, status) VALUES
(1, 0, 2, 0.50, 30, 1),
(2, 3, 7, 0.70, 20, 1),
(3, 8, 30, 0.85, 10, 1);

INSERT INTO user(id, openid, name, phone, password_hash, status, create_time, update_time) VALUES
(4001, 'openid-seeded', 'Seed User', '13800000000', '$2a$10$Exx5A4dYk7mFjP4r.m7t1.TbIdYqj8V4aHcRUW2KIiMzFxLpy0XbW', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4002, 'openid-recover', 'Recover User', '13800000001', '$2a$10$Exx5A4dYk7mFjP4r.m7t1.TbIdYqj8V4aHcRUW2KIiMzFxLpy0XbW', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO pickup_contact(id, user_id, consignee, phone, label, detail, is_default) VALUES
(8001, 4001, 'Seed Contact', '13800008888', '公司', '滨江社区前置仓 A 区', 1);

INSERT INTO orders(
    id, number, status, user_id, order_time, checkout_time, pay_method, pay_status, amount, remark, phone, pickup_point,
    user_name, consignee, preparation_mode, fulfillment_fee, package_count, package_strategy, fulfillment_type,
    pickup_code, pickup_deadline
) VALUES
(5001, 'SFU202605130001', 1, 4001, CURRENT_TIMESTAMP(), NULL, 1, 0, 17.50, '既有订单', '13800000000', '滨江社区前置仓 A 区',
 'Seed User', 'Seed User', 1, 0, 0, 1, 2, '452381', DATEADD('HOUR', 24, CURRENT_TIMESTAMP()));

INSERT INTO order_detail(id, order_id, product_id, batch_id, name, image, product_spec, number, amount) VALUES
(6001, 5001, 1001, 2001, 'Fresh Milk', 'https://img/milk.png', NULL, 2, 8.75);

INSERT INTO order_event_log(id, order_id, event_type, actor_type, actor_id, from_status, to_status, from_pay_status, to_pay_status, note, create_time) VALUES
(7001, 5001, 'submitted', 'user', 4001, NULL, 1, NULL, 0, '用户提交订单', CURRENT_TIMESTAMP());
