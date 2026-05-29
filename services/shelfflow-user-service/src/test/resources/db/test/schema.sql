CREATE TABLE category (
    id BIGINT PRIMARY KEY,
    type INT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort INT NOT NULL,
    status INT NOT NULL
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

CREATE TABLE product_spec (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    name VARCHAR(32),
    value VARCHAR(255)
);

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64),
    name VARCHAR(32),
    phone VARCHAR(11),
    email VARCHAR(100),
    password_hash VARCHAR(100),
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

CREATE UNIQUE INDEX uq_user_openid ON user(openid);
CREATE UNIQUE INDEX uq_user_phone ON user(phone);
CREATE UNIQUE INDEX uq_user_email ON user(email);

CREATE TABLE user_verification_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target VARCHAR(100) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    code_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    create_time TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_verification_code_target ON user_verification_code(target, purpose, expires_at);

CREATE TABLE pickup_contact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    consignee VARCHAR(32),
    phone VARCHAR(11),
    label VARCHAR(16),
    detail VARCHAR(120),
    is_default INT NOT NULL
);

CREATE INDEX idx_pickup_contact_user_default ON pickup_contact(user_id, is_default);

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
    id BIGINT PRIMARY KEY,
    min_days_to_expire INT NOT NULL,
    max_days_to_expire INT NOT NULL,
    discount_rate DECIMAL(10, 2) NOT NULL,
    priority INT NOT NULL,
    status INT NOT NULL
);

CREATE TABLE cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32),
    image VARCHAR(255),
    user_id BIGINT NOT NULL,
    product_id BIGINT,
    bundle_id BIGINT,
    batch_id BIGINT,
    product_spec VARCHAR(50),
    number INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    create_time TIMESTAMP
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    number VARCHAR(50),
    status INT NOT NULL,
    user_id BIGINT NOT NULL,
    pickup_contact_id BIGINT,
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

CREATE TABLE user_order_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(64) NOT NULL,
    order_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    pay_method INT NOT NULL,
    provider VARCHAR(32) NOT NULL,
    status INT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_time TIMESTAMP NOT NULL,
    paid_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uk_user_order_payment_no ON user_order_payment(payment_no);
CREATE UNIQUE INDEX uk_user_order_payment_order ON user_order_payment(order_id);
CREATE UNIQUE INDEX uk_user_order_payment_idempotency ON user_order_payment(idempotency_key);
