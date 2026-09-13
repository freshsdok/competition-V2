-- V1 system 数据源；手工审核后执行。不会修改V2，也不自动开放入口。
-- MySQL 5.7+/8.0；UTC时间由SQL函数明确生成，应用输出ISO UTC，页面显示北京时间。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS v1_team_collection (
  id BIGINT NOT NULL AUTO_INCREMENT,
  collection_code VARCHAR(80) COLLATE utf8mb4_bin NOT NULL,
  competition_series_id BIGINT NOT NULL,
  team_code VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
  team_name VARCHAR(255) NOT NULL DEFAULT '',
  award_level VARCHAR(40) NOT NULL,
  source_sha256 CHAR(64) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/IN_PROGRESS/USER_CONFIRMED_COMPLETE',
  holder_user_id BIGINT NULL,
  handler_name VARCHAR(100) NULL COMMENT '经审核学生名单姓名快照',
  started_at DATETIME(6) NULL,
  confirmed_at DATETIME(6) NULL COMMENT '办理人确认时间，不是V2保存时间',
  version BIGINT NOT NULL DEFAULT 0,
  last_actor_user_id BIGINT NULL,
  last_action VARCHAR(32) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_collection_team (collection_code,team_code),
  KEY ix_collection_enabled (enabled,collection_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS v1_team_collection_member (
  team_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  student_name VARCHAR(100) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  source_sha256 CHAR(64) NOT NULL,
  source_sheet VARCHAR(80) NOT NULL,
  source_row INT NOT NULL,
  source_column VARCHAR(10) NOT NULL,
  PRIMARY KEY (team_id,user_id),
  KEY ix_collection_member_user (user_id,enabled,team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS v1_team_collection_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  actor_user_id BIGINT NOT NULL,
  action VARCHAR(32) NOT NULL COMMENT 'START/RELEASE/USER_CONFIRM/ADMIN_RELEASE/ADMIN_REOPEN',
  version BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_collection_event_version (team_id,version),
  KEY ix_collection_event_actor (actor_user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 名单来源暂存区，没有user_id，不能直接用于授权。只包含“姓名N”学生列，教师列不导入。
CREATE TABLE IF NOT EXISTS v1_team_collection_source (
  collection_code VARCHAR(80) COLLATE utf8mb4_bin NOT NULL,
  source_sha256 CHAR(64) NOT NULL,
  source_sheet VARCHAR(80) NOT NULL,
  source_row INT NOT NULL,
  source_column VARCHAR(10) NOT NULL,
  team_code VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
  student_name VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
  award_level VARCHAR(40) NOT NULL,
  PRIMARY KEY (collection_code,source_sha256,source_sheet,source_row,source_column),
  KEY ix_collection_source_team (collection_code,team_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
