INSERT INTO category (id, type, name) VALUES
    (11, 1, '临期乳品'),
    (12, 1, '临期烘焙');

INSERT INTO product (
    id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user
) VALUES
    (1001, 'Fresh Milk', 11, 12.50, 'https://img/milk.png', '原味鲜牛奶', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
    (1002, 'Whole Wheat Bread', 12, 9.90, 'https://img/bread.png', '全麦面包', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

INSERT INTO inventory_batch (
    id, product_id, batch_code, production_time, expiration_time, stock_quantity, locked_quantity, sold_quantity, status, create_time, update_time, create_user, update_user
) VALUES
    (
        2001,
        1001,
        'BATCH-1001-A',
        DATEADD('DAY', -1, CURRENT_TIMESTAMP),
        DATEADD('DAY', 3, CURRENT_TIMESTAMP),
        30,
        5,
        8,
        1,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        1,
        1
    ),
    (
        2002,
        1002,
        'BATCH-1002-A',
        DATEADD('DAY', -2, CURRENT_TIMESTAMP),
        DATEADD('DAY', 12, CURRENT_TIMESTAMP),
        20,
        0,
        0,
        1,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        1,
        1
    );

INSERT INTO pricing_rule (
    id, name, discount_rate, status, min_days_to_expire, max_days_to_expire, priority, create_time, update_time, create_user, update_user
) VALUES
    (3001, '七日清仓折扣', 0.70, 1, 0, 7, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
    (3002, '常规临期折扣', 0.90, 1, 8, 30, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

INSERT INTO orders(
    id, number, status, user_id, order_time, checkout_time, pay_method, pay_status, amount, remark, phone, pickup_point,
    user_name, consignee, preparation_mode, fulfillment_fee, package_count, package_strategy, fulfillment_type,
    pickup_code, pickup_deadline
) VALUES
    (
        5001,
        'SFA202605190001',
        2,
        4001,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        1,
        1,
        17.50,
        '已支付待备货',
        '13800000000',
        '滨江社区前置仓 A 区',
        'Seed User',
        'Seed User',
        1,
        0,
        0,
        1,
        2,
        '452381',
        DATEADD('HOUR', 24, CURRENT_TIMESTAMP)
    ),
    (
        5002,
        'SFA202605190002',
        4,
        4001,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        1,
        1,
        8.75,
        '待自提',
        '13800000000',
        '滨江社区前置仓 A 区',
        'Seed User',
        'Seed User',
        1,
        0,
        0,
        1,
        2,
        '721903',
        DATEADD('HOUR', 24, CURRENT_TIMESTAMP)
    ),
    (
        5003,
        'SFA202605190003',
        1,
        4001,
        CURRENT_TIMESTAMP,
        NULL,
        1,
        0,
        9.90,
        '未支付',
        '13800000000',
        '滨江社区前置仓 A 区',
        'Seed User',
        'Seed User',
        1,
        0,
        0,
        1,
        2,
        '331802',
        DATEADD('HOUR', 24, CURRENT_TIMESTAMP)
    );

INSERT INTO order_detail(id, order_id, product_id, batch_id, name, image, product_spec, number, amount) VALUES
    (6001, 5001, 1001, 2001, 'Fresh Milk', 'https://img/milk.png', NULL, 2, 17.50),
    (6002, 5002, 1001, 2001, 'Fresh Milk', 'https://img/milk.png', NULL, 1, 8.75),
    (6003, 5003, 1002, 2002, 'Whole Wheat Bread', 'https://img/bread.png', NULL, 1, 9.90);

INSERT INTO order_event_log(id, order_id, event_type, actor_type, actor_id, from_status, to_status, from_pay_status, to_pay_status, note, create_time) VALUES
    (7001, 5001, 'submitted', 'user', 4001, NULL, 1, NULL, 0, '用户提交订单', CURRENT_TIMESTAMP),
    (7002, 5001, 'paid', 'user', 4001, 1, 2, 0, 1, '用户确认支付', CURRENT_TIMESTAMP),
    (7003, 5002, 'fulfillment_updated', 'admin', 99, 3, 4, 1, 1, '管理员将订单状态从 preparing 更新为 ready_for_pickup', CURRENT_TIMESTAMP);

INSERT INTO ai_knowledge_base(id, title, category, content, create_time, update_time, create_user, update_user) VALUES
    (8001, '乳制品临期处理规范', '处理规范', '乳制品剩余三天内应进入临期专区，结合会员推送和清仓折扣降低损耗。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
    (8002, '动态定价策略', '定价策略', '临期商品应结合剩余效期、库存深度、历史动销和损耗成本设置折扣。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

INSERT INTO admin_operation_log(id, actor_id, module, action, method, path, status_code, request_id, summary, create_time) VALUES
    (9001, 1, 'PRODUCT', 'UPDATE', 'PUT', '/api/admin/products/1001', 200, 'req-log-1', 'PRODUCT UPDATE SUCCESS', DATEADD('MINUTE', -3, CURRENT_TIMESTAMP)),
    (9002, 1, 'ORDER_FULFILLMENT', 'PICKUP_VERIFY', 'POST', '/api/admin/orders/5002/pickup-verification', 200, 'req-log-2', 'ORDER_FULFILLMENT PICKUP_VERIFY SUCCESS', DATEADD('MINUTE', -2, CURRENT_TIMESTAMP)),
    (9003, 1, 'INVENTORY_BATCH', 'DELETE', 'DELETE', '/api/admin/inventory-batches/2002', 409, 'req-log-3', 'INVENTORY_BATCH DELETE FAILED', DATEADD('MINUTE', -1, CURRENT_TIMESTAMP));

INSERT INTO pickup_point(id, name, address, contact_name, contact_phone, service_time, sort, status, create_time, update_time, create_user, update_user) VALUES
    (9001, '滨江社区前置仓 A 区', '滨江花园北门 12 号自提柜旁', '社区团长', '13800000001', '09:00-21:00', 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);
