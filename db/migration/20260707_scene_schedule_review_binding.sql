-- Scene schedule review binding and sequence enhancement.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- This migration extends the existing competition_scene_schedule_target table.
-- Existing scene schedule data remains valid; all new columns are nullable.

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_scene_schedule_target_column;
DELIMITER $$
CREATE PROCEDURE add_scene_schedule_target_column(
    IN p_column_name varchar(128),
    IN p_column_definition varchar(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'competition_scene_schedule_target'
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `competition_scene_schedule_target` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_scene_schedule_target_column(
  'target_type',
  '`target_type` varchar(50) DEFAULT NULL COMMENT ''绑定对象类型: REVIEW_OBJECT/TEAM/PERSON/USER/CREDENTIAL/MANUAL'' AFTER `target_source`'
);

CALL add_scene_schedule_target_column(
  'review_object_id',
  '`review_object_id` bigint DEFAULT NULL COMMENT ''评审对象ID'' AFTER `target_type`'
);

CALL add_scene_schedule_target_column(
  'target_name',
  '`target_name` varchar(300) DEFAULT NULL COMMENT ''对象名称/团队名称/人员姓名'' AFTER `review_object_id`'
);

CALL add_scene_schedule_target_column(
  'certificate_code',
  '`certificate_code` varchar(100) DEFAULT NULL COMMENT ''证件编号/参赛证编号'' AFTER `id_card_suffix`'
);

CALL add_scene_schedule_target_column(
  'sequence_no',
  '`sequence_no` int DEFAULT NULL COMMENT ''赛场内顺序号'' AFTER `seat_no`'
);

CALL add_scene_schedule_target_column(
  'source_module',
  '`source_module` varchar(100) DEFAULT NULL COMMENT ''来源模块'' AFTER `match_status`'
);

CALL add_scene_schedule_target_column(
  'source_biz_type',
  '`source_biz_type` varchar(100) DEFAULT NULL COMMENT ''来源业务类型'' AFTER `source_module`'
);

CALL add_scene_schedule_target_column(
  'source_biz_id',
  '`source_biz_id` varchar(100) DEFAULT NULL COMMENT ''来源业务ID'' AFTER `source_biz_type`'
);

DROP PROCEDURE IF EXISTS add_scene_schedule_target_column;

DROP PROCEDURE IF EXISTS add_scene_schedule_target_index;
DELIMITER $$
CREATE PROCEDURE add_scene_schedule_target_index(
    IN p_index_name varchar(128),
    IN p_index_definition varchar(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'competition_scene_schedule_target'
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `competition_scene_schedule_target` ADD INDEX ', p_index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_scene_schedule_target_index(
  'idx_scene_target_schedule_sequence',
  '`idx_scene_target_schedule_sequence` (`schedule_id`, `sequence_no`)'
);

CALL add_scene_schedule_target_index(
  'idx_scene_target_review_object',
  '`idx_scene_target_review_object` (`review_object_id`)'
);

CALL add_scene_schedule_target_index(
  'idx_scene_target_certificate_code',
  '`idx_scene_target_certificate_code` (`certificate_code`)'
);

CALL add_scene_schedule_target_index(
  'idx_scene_target_type',
  '`idx_scene_target_type` (`target_type`)'
);

DROP PROCEDURE IF EXISTS add_scene_schedule_target_index;
