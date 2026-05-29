CREATE TABLE IF NOT EXISTS ai_knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(80) NOT NULL,
    category VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    INDEX idx_ai_knowledge_category (category),
    INDEX idx_ai_knowledge_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO ai_knowledge_base(title, category, content, create_time, update_time, create_user, update_user)
SELECT '乳制品临期处理规范', '处理规范', '乳制品剩余三天内应进入临期专区，结合会员推送和清仓折扣降低损耗。', NOW(), NOW(), 1, 1
WHERE NOT EXISTS (SELECT 1 FROM ai_knowledge_base WHERE title = '乳制品临期处理规范');

INSERT INTO ai_knowledge_base(title, category, content, create_time, update_time, create_user, update_user)
SELECT '动态定价策略', '定价策略', '临期商品应结合剩余效期、库存深度、历史动销和损耗成本设置折扣。', NOW(), NOW(), 1, 1
WHERE NOT EXISTS (SELECT 1 FROM ai_knowledge_base WHERE title = '动态定价策略');
