CREATE TABLE IF NOT EXISTS pickup_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    address VARCHAR(160) NOT NULL,
    contact_name VARCHAR(32) NULL,
    contact_phone VARCHAR(32) NULL,
    service_time VARCHAR(80) NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_user BIGINT NULL,
    update_user BIGINT NULL,
    UNIQUE KEY uq_pickup_point_name (name),
    INDEX idx_pickup_point_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO pickup_point(name, address, contact_name, contact_phone, service_time, sort, status, create_time, update_time, create_user, update_user)
SELECT '滨江社区前置仓 A 区', '滨江花园北门 12 号自提柜旁', '社区团长', '13800000001', '09:00-21:00', 10, 1, NOW(), NOW(), 1, 1
WHERE NOT EXISTS (SELECT 1 FROM pickup_point WHERE name = '滨江社区前置仓 A 区');

INSERT INTO pickup_point(name, address, contact_name, contact_phone, service_time, sort, status, create_time, update_time, create_user, update_user)
SELECT '星河湾社区自提点', '星河湾 3 栋物业服务中心', '值班管家', '13800000002', '10:00-20:30', 20, 1, NOW(), NOW(), 1, 1
WHERE NOT EXISTS (SELECT 1 FROM pickup_point WHERE name = '星河湾社区自提点');
