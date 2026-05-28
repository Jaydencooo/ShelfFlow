CREATE TABLE IF NOT EXISTS ai_ops_chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    title VARCHAR(64) NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_ai_ops_chat_session_admin_update (admin_user_id, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_ops_chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    provider VARCHAR(32) NULL,
    model VARCHAR(64) NULL,
    references_json TEXT NULL,
    create_time DATETIME NOT NULL,
    INDEX idx_ai_ops_chat_message_session_time (session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_ops_suggestion_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    suggestion_id VARCHAR(96) NOT NULL,
    status VARCHAR(16) NOT NULL,
    actor_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    UNIQUE KEY uq_ai_ops_suggestion_action_id (suggestion_id),
    INDEX idx_ai_ops_suggestion_action_status_time (status, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_ops_suggestion_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    suggestion_id VARCHAR(96) NOT NULL,
    action VARCHAR(24) NOT NULL,
    status VARCHAR(16) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    target_name VARCHAR(128) NULL,
    operation_summary VARCHAR(255) NOT NULL,
    operation_payload TEXT NULL,
    actor_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    INDEX idx_ai_ops_suggestion_action_log_suggestion (suggestion_id, create_time),
    INDEX idx_ai_ops_suggestion_action_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id BIGINT NULL,
    module VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    method VARCHAR(8) NOT NULL,
    path VARCHAR(255) NOT NULL,
    status_code INT NOT NULL,
    request_id VARCHAR(80) NULL,
    summary VARCHAR(255) NOT NULL,
    create_time DATETIME NOT NULL,
    INDEX idx_admin_operation_log_time (create_time),
    INDEX idx_admin_operation_log_actor_time (actor_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
