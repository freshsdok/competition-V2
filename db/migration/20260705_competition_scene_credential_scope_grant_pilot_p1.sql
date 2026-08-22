-- Competition scene credential one-card grant pilot phase 1.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- This migration only creates the grant table. It does not migrate legacy test data.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `competition_scene_credential_scope_grant` (
  `grant_id` bigint NOT NULL AUTO_INCREMENT COMMENT '证件授权ID',
  `credential_id` bigint NOT NULL COMMENT '核心现场证件ID',
  `competition_series_id` bigint NOT NULL COMMENT '赛事系列ID',
  `scope_type` varchar(32) NOT NULL COMMENT '授权作用域: COMPETITION/SCHEDULE/VIP/EXPERT/STAFF/TEMP',
  `scope_ref_id` bigint NOT NULL COMMENT '授权作用域引用ID，当前约定所有scope_ref_id均为内部数值ID，SCHEDULE为schedule_id',
  `active_grant_key` varchar(255) DEFAULT NULL COMMENT '有效授权唯一键，ACTIVE且deleted=0时写入credentialId:scopeType:scopeRefId:sourceTargetId，撤销后置空',
  `source_type` varchar(32) NOT NULL DEFAULT 'MANUAL' COMMENT '授权来源类型: SCHEDULE_TARGET/MANUAL/IMPORT/COMPETITION_DIRECT',
  `source_schedule_id` bigint DEFAULT NULL COMMENT '来源赛场安排ID',
  `source_target_id` bigint DEFAULT NULL COMMENT '来源赛场安排对象ID',
  `credential_type` varchar(32) NOT NULL COMMENT '授权对应证件类型',
  `role_code` varchar(64) DEFAULT NULL COMMENT '授权角色编码',
  `subject_type` varchar(32) NOT NULL COMMENT '主体类型',
  `subject_code` varchar(128) NOT NULL COMMENT '主体编码',
  `ability_json` text COMMENT '授权能力JSON',
  `valid_from` datetime DEFAULT NULL COMMENT '授权有效开始时间',
  `valid_to` datetime DEFAULT NULL COMMENT '授权有效结束时间',
  `operation_window_json` longtext COMMENT '动作窗口JSON',
  `grant_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '授权状态: ACTIVE有效/REVOKED撤销/EXPIRED过期',
  `grant_snapshot_json` longtext COMMENT '授权快照JSON，不记录手机号、身份证号、token等敏感信息',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识: 0正常/1删除',
  PRIMARY KEY (`grant_id`),
  UNIQUE KEY `uk_grant_active_key` (`active_grant_key`),
  KEY `idx_grant_credential` (`credential_id`, `deleted`),
  KEY `idx_grant_competition_scope` (`competition_series_id`, `scope_type`, `scope_ref_id`, `deleted`),
  KEY `idx_grant_source_schedule_target` (`source_schedule_id`, `source_target_id`, `deleted`),
  KEY `idx_grant_subject` (`competition_series_id`, `subject_type`, `subject_code`, `credential_type`, `deleted`),
  KEY `idx_grant_status` (`grant_status`, `deleted`),
  KEY `idx_grant_active_lookup` (`credential_id`, `scope_type`, `scope_ref_id`, `source_target_id`, `grant_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赛事现场证件作用域授权表';
