CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type INT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    category_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image VARCHAR(255),
    description VARCHAR(255),
    status INT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL
);

CREATE TABLE inventory_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    batch_code VARCHAR(64) NOT NULL,
    production_time TIMESTAMP NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    stock_quantity INT NOT NULL,
    locked_quantity INT NOT NULL,
    sold_quantity INT NOT NULL,
    status INT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL
);

CREATE UNIQUE INDEX uq_inventory_batch_code ON inventory_batch(batch_code);

CREATE TABLE pricing_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    discount_rate DECIMAL(10, 2) NOT NULL,
    status INT NOT NULL,
    min_days_to_expire INT NOT NULL,
    max_days_to_expire INT NOT NULL,
    priority INT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    number VARCHAR(50),
    status INT NOT NULL,
    user_id BIGINT NOT NULL,
    order_time TIMESTAMP NOT NULL,
    checkout_time TIMESTAMP,
    pay_method INT NOT NULL,
    pay_status INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    remark VARCHAR(100),
    phone VARCHAR(11),
    pickup_point VARCHAR(255),
    user_name VARCHAR(32),
    consignee VARCHAR(32),
    preparation_mode INT NOT NULL,
    fulfillment_fee INT NOT NULL,
    package_count INT NOT NULL,
    package_strategy INT NOT NULL,
    fulfillment_type INT NOT NULL,
    pickup_code VARCHAR(12),
    pickup_deadline TIMESTAMP,
    cancel_reason VARCHAR(255),
    cancel_time TIMESTAMP
);

CREATE TABLE order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    batch_id BIGINT,
    name VARCHAR(32),
    image VARCHAR(255),
    product_spec VARCHAR(50),
    number INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL
);

CREATE TABLE order_event_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    actor_type VARCHAR(16) NOT NULL,
    actor_id BIGINT,
    from_status INT,
    to_status INT,
    from_pay_status INT,
    to_pay_status INT,
    note VARCHAR(255),
    create_time TIMESTAMP NOT NULL
);

CREATE TABLE ai_knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(80) NOT NULL,
    category VARCHAR(32) NOT NULL,
    content CLOB NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE ai_ops_chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    title VARCHAR(64) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

CREATE TABLE ai_ops_chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content CLOB NOT NULL,
    provider VARCHAR(32),
    model VARCHAR(64),
    references_json CLOB,
    create_time TIMESTAMP NOT NULL
);

CREATE TABLE ai_ops_suggestion_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    suggestion_id VARCHAR(96) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL,
    actor_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

CREATE TABLE ai_ops_suggestion_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    suggestion_id VARCHAR(96) NOT NULL,
    action VARCHAR(24) NOT NULL,
    status VARCHAR(16) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    target_name VARCHAR(128),
    operation_summary VARCHAR(255) NOT NULL,
    operation_payload CLOB,
    actor_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL
);

CREATE TABLE pickup_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    address VARCHAR(160) NOT NULL,
    contact_name VARCHAR(32),
    contact_phone VARCHAR(32),
    service_time VARCHAR(80),
    sort INT NOT NULL,
    status INT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id BIGINT,
    module VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    method VARCHAR(8) NOT NULL,
    path VARCHAR(255) NOT NULL,
    status_code INT NOT NULL,
    request_id VARCHAR(80),
    summary VARCHAR(255) NOT NULL,
    create_time TIMESTAMP NOT NULL
);
