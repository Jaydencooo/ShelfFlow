CREATE TABLE IF NOT EXISTS user_order_event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    message_id VARCHAR(80) NOT NULL,
    exchange_name VARCHAR(128) NOT NULL,
    routing_key VARCHAR(128) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_retry_time DATETIME NOT NULL,
    last_error VARCHAR(512),
    published_time DATETIME,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_order_event_outbox_event_id (event_id),
    KEY idx_user_order_event_outbox_status_retry (status, next_retry_time),
    KEY idx_user_order_event_outbox_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订单事件可靠投递 outbox';
