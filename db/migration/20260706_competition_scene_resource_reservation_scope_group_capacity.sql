-- Competition scene resource reservation scope/group/capacity phase 1.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- This migration adds reservation scope/group base tables and compatible snapshot fields.
-- It does not delete or clean business data.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `competition_scene_resource_schedule_scope` (
  `scope_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源允许预约赛场范围ID',
  `schedule_resource_id` bigint NOT NULL COMMENT '赛场资源布置ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `allowed_schedule_id` bigint NOT NULL COMMENT '允许预约的赛场安排ID',
  `source_type` varchar(32) NOT NULL DEFAULT 'MANUAL_BIND' COMMENT '来源类型: MANUAL_BIND手工绑定/AUTO_LOCATION位置自动匹配预留',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用: 0否/1是',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识: 0正常/1删除',
  PRIMARY KEY (`scope_id`),
  KEY `idx_resource_schedule_scope_sr` (`schedule_resource_id`, `deleted`),
  KEY `idx_resource_schedule_scope_allowed` (`allowed_schedule_id`, `enabled`, `deleted`),
  KEY `idx_resource_schedule_scope_resource` (`resource_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赛事现场资源允许预约赛场范围表';

CREATE TABLE IF NOT EXISTS `competition_scene_resource_slot_group_scope` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约时段允许组别ID',
  `slot_id` bigint NOT NULL COMMENT '预约时段ID',
  `schedule_resource_id` bigint NOT NULL COMMENT '赛场资源布置ID',
  `allowed_group_code` varchar(64) NOT NULL COMMENT '允许预约组别编码',
  `allowed_group_name` varchar(128) DEFAULT NULL COMMENT '允许预约组别名称',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用: 0否/1是',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识: 0正常/1删除',
  PRIMARY KEY (`id`),
  KEY `idx_slot_group_scope_slot` (`slot_id`, `enabled`, `deleted`),
  KEY `idx_slot_group_scope_group` (`allowed_group_code`, `enabled`, `deleted`),
  KEY `idx_slot_group_scope_sr` (`schedule_resource_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赛事现场资源预约时段允许组别表';

DROP PROCEDURE IF EXISTS add_scene_resource_reservation_scope_column;
DELIMITER $$
CREATE PROCEDURE add_scene_resource_reservation_scope_column(
    IN p_table_name varchar(128),
    IN p_column_name varchar(128),
    IN p_column_definition varchar(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_slot',
  'workstation_count',
  '`workstation_count` int DEFAULT 1 COMMENT ''每台设备工位数快照'' AFTER `end_time`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_slot',
  'total_device_count',
  '`total_device_count` int DEFAULT 0 COMMENT ''总设备数'' AFTER `workstation_count`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_slot',
  'total_workstation_count',
  '`total_workstation_count` int DEFAULT 0 COMMENT ''总工位数'' AFTER `total_device_count`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'competition_series_id',
  '`competition_series_id` bigint DEFAULT NULL COMMENT ''赛事系列ID'' AFTER `event_id`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'reservation_source_schedule_id',
  '`reservation_source_schedule_id` bigint DEFAULT NULL COMMENT ''预约来源赛场安排ID'' AFTER `competition_series_id`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'operator_name',
  '`operator_name` varchar(128) DEFAULT NULL COMMENT ''操作人姓名'' AFTER `operator_user_id`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'group_code',
  '`group_code` varchar(64) DEFAULT NULL COMMENT ''预约主体组别编码快照'' AFTER `operator_name`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'group_name',
  '`group_name` varchar(128) DEFAULT NULL COMMENT ''预约主体组别名称快照'' AFTER `group_code`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'occupy_people_count',
  '`occupy_people_count` int DEFAULT 1 COMMENT ''占用人数快照'' AFTER `group_name`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'reserved_workstation_count',
  '`reserved_workstation_count` int DEFAULT 0 COMMENT ''预约占用工位数快照'' AFTER `reserved_device_count`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'shared_occupancy_snapshot',
  '`shared_occupancy_snapshot` tinyint DEFAULT NULL COMMENT ''共享占用快照: 0否/1是'' AFTER `covered_workstation_count`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'workstation_count_snapshot',
  '`workstation_count_snapshot` int DEFAULT NULL COMMENT ''每台设备工位数快照'' AFTER `shared_occupancy_snapshot`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'active_reservation_key',
  '`active_reservation_key` varchar(255) DEFAULT NULL COMMENT ''有效预约唯一键'' AFTER `workstation_count_snapshot`'
);

CALL add_scene_resource_reservation_scope_column(
  'competition_scene_resource_reservation',
  'idempotency_key',
  '`idempotency_key` varchar(128) DEFAULT NULL COMMENT ''幂等键'' AFTER `active_reservation_key`'
);

DROP PROCEDURE IF EXISTS add_scene_resource_reservation_scope_column;

UPDATE competition_scene_resource_slot sl
LEFT JOIN competition_scene_schedule_resource sr
       ON sl.schedule_resource_id = sr.schedule_resource_id
      AND sr.deleted = 0
SET sl.workstation_count = COALESCE(NULLIF(sl.workstation_count, 0), sr.workstations_per_device, 1),
    sl.total_device_count = COALESCE(NULLIF(sl.total_device_count, 0), sl.device_capacity, 0),
    sl.total_workstation_count = COALESCE(NULLIF(sl.total_workstation_count, 0), sl.workstation_capacity, 0)
WHERE sl.deleted = 0;

UPDATE competition_scene_resource_reservation r
LEFT JOIN competition_scene_schedule s
       ON r.schedule_id = s.schedule_id
      AND s.del_flag = '0'
LEFT JOIN competition_scene_schedule_resource sr
       ON r.schedule_resource_id = sr.schedule_resource_id
      AND sr.deleted = 0
SET r.competition_series_id = COALESCE(r.competition_series_id, s.competition_series_id, r.event_id),
    r.reservation_source_schedule_id = COALESCE(r.reservation_source_schedule_id, r.schedule_id),
    r.occupy_people_count = COALESCE(NULLIF(r.occupy_people_count, 0), 1),
    r.reserved_workstation_count = COALESCE(NULLIF(r.reserved_workstation_count, 0), r.covered_workstation_count, 0),
    r.workstation_count_snapshot = COALESCE(r.workstation_count_snapshot, sr.workstations_per_device)
WHERE r.deleted = 0;

DROP PROCEDURE IF EXISTS add_scene_resource_reservation_scope_index;
DELIMITER $$
CREATE PROCEDURE add_scene_resource_reservation_scope_index(
    IN p_table_name varchar(128),
    IN p_index_name varchar(128),
    IN p_index_definition varchar(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD ', p_index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_scene_resource_reservation_scope_index(
  'competition_scene_resource_reservation',
  'uk_scene_resource_active_reservation_key',
  'UNIQUE KEY `uk_scene_resource_active_reservation_key` (`active_reservation_key`)'
);

DROP PROCEDURE IF EXISTS rename_scene_resource_idempotency_index;
DELIMITER $$
CREATE PROCEDURE rename_scene_resource_idempotency_index()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'competition_scene_resource_reservation'
          AND INDEX_NAME = 'uk_scene_reservation_idempotency'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'competition_scene_resource_reservation'
          AND INDEX_NAME = 'uk_scene_resource_idempotency_key'
    ) THEN
        ALTER TABLE `competition_scene_resource_reservation`
          RENAME INDEX `uk_scene_reservation_idempotency` TO `uk_scene_resource_idempotency_key`;
    END IF;
END$$
DELIMITER ;

CALL rename_scene_resource_idempotency_index();
DROP PROCEDURE IF EXISTS rename_scene_resource_idempotency_index;

CALL add_scene_resource_reservation_scope_index(
  'competition_scene_resource_reservation',
  'uk_scene_resource_idempotency_key',
  'UNIQUE KEY `uk_scene_resource_idempotency_key` (`idempotency_key`)'
);

DROP PROCEDURE IF EXISTS add_scene_resource_reservation_scope_index;

