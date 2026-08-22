-- Review file task import enhancement.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- Adds material-level source tracing for files imported from file task upload records.

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_review_object_material_source_column;
DELIMITER $$
CREATE PROCEDURE add_review_object_material_source_column(
    IN p_column_name varchar(128),
    IN p_column_definition varchar(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'review_object_material'
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `review_object_material` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_review_object_material_source_column(
  'source_module',
  '`source_module` varchar(100) DEFAULT NULL COMMENT ''来源模块'' AFTER `status`'
);

CALL add_review_object_material_source_column(
  'source_biz_type',
  '`source_biz_type` varchar(100) DEFAULT NULL COMMENT ''来源业务类型'' AFTER `source_module`'
);

CALL add_review_object_material_source_column(
  'source_biz_id',
  '`source_biz_id` varchar(100) DEFAULT NULL COMMENT ''来源业务ID'' AFTER `source_biz_type`'
);

CALL add_review_object_material_source_column(
  'source_material_key',
  '`source_material_key` varchar(1000) DEFAULT NULL COMMENT ''来源材料标识'' AFTER `source_biz_id`'
);

DROP PROCEDURE IF EXISTS add_review_object_material_source_column;

DROP PROCEDURE IF EXISTS add_review_object_material_source_index;
DELIMITER $$
CREATE PROCEDURE add_review_object_material_source_index(
    IN p_index_name varchar(128),
    IN p_index_definition varchar(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'review_object_material'
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `review_object_material` ADD INDEX ', p_index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_review_object_material_source_index(
  'idx_review_material_source',
  '`idx_review_material_source` (`source_module`, `source_biz_type`, `source_biz_id`)'
);

DROP PROCEDURE IF EXISTS add_review_object_material_source_index;
