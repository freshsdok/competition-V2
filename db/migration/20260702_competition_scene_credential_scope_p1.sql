-- Competition scene credential scope and subject operation state upgrade.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- Do not run against production before review.

SET NAMES utf8mb4;

ALTER TABLE `competition_scene_credential`
  MODIFY COLUMN `schedule_id` bigint DEFAULT NULL COMMENT '赛场安排ID，赛场级证件必填，大赛级直接发证可为空',
  ADD COLUMN `issue_channel` varchar(32) NOT NULL DEFAULT 'SCHEDULE_MATCH' COMMENT '发证渠道: SCHEDULE_MATCH赛场匹配/COMPETITION_DIRECT大赛直接/MANUAL手工/IMPORT导入' AFTER `credential_image_url`,
  ADD COLUMN `scope_type` varchar(32) NOT NULL DEFAULT 'SCHEDULE' COMMENT '证件作用域: COMPETITION大赛/SCHEDULE赛场/VIP贵宾/EXPERT专家/STAFF工作人员/TEMP临时' AFTER `issue_channel`,
  ADD COLUMN `scope_ref_id` bigint DEFAULT NULL COMMENT '作用域引用ID: 大赛级为competition_series_id，赛场级为schedule_id' AFTER `scope_type`,
  ADD COLUMN `credential_name` varchar(100) DEFAULT NULL COMMENT '证件展示名称，与credential_type解耦' AFTER `scope_ref_id`,
  ADD COLUMN `ability_json` text COMMENT '证件能力JSON，固定字段report/material/waiting/review/resourceReservation/vipAccess' AFTER `credential_name`;

ALTER TABLE `competition_scene_credential`
  ADD KEY `idx_scene_credential_scope` (`scope_type`, `scope_ref_id`),
  ADD KEY `idx_scene_credential_issue` (`issue_channel`),
  ADD KEY `idx_scene_credential_name` (`credential_name`);

UPDATE `competition_scene_credential`
SET `issue_channel` = 'SCHEDULE_MATCH',
    `scope_type` = 'SCHEDULE',
    `scope_ref_id` = `schedule_id`,
    `credential_name` = CASE
      WHEN `credential_type` IN ('PARTICIPANT', 'COMPETITOR') THEN '参赛证'
      WHEN `credential_type` = 'TEACHER' THEN '教师证'
      WHEN `credential_type` = 'EXPERT' THEN '专家证'
      WHEN `credential_type` = 'STAFF' THEN '工作证'
      ELSE '现场证件'
    END,
    `ability_json` = CASE
      WHEN `credential_type` IN ('PARTICIPANT', 'COMPETITOR') THEN '{"report":true,"material":true,"waiting":true,"review":false,"resourceReservation":true,"vipAccess":false}'
      WHEN `credential_type` = 'TEACHER' THEN '{"report":true,"material":false,"waiting":false,"review":false,"resourceReservation":false,"vipAccess":false}'
      WHEN `credential_type` = 'EXPERT' THEN '{"report":true,"material":false,"waiting":false,"review":true,"resourceReservation":false,"vipAccess":false}'
      WHEN `credential_type` = 'STAFF' THEN '{"report":false,"material":false,"waiting":false,"review":false,"resourceReservation":false,"vipAccess":false}'
      ELSE '{"report":false,"material":false,"waiting":false,"review":false,"resourceReservation":false,"vipAccess":false}'
    END
WHERE `scope_ref_id` IS NULL
  AND `del_flag` = '0';

CREATE TABLE IF NOT EXISTS `competition_scene_subject_operation_state` (
  `state_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主体操作状态ID',
  `competition_series_id` bigint NOT NULL COMMENT '赛事系列ID',
  `scope_type` varchar(32) NOT NULL COMMENT '状态作用域: COMPETITION/SCHEDULE/VIP/EXPERT/STAFF/TEMP',
  `scope_ref_id` bigint NOT NULL COMMENT '状态作用域引用ID',
  `subject_type` varchar(32) NOT NULL COMMENT '主体类型: TEAM/USER/EXPERT/STAFF/VIP/TEMP',
  `subject_code` varchar(128) NOT NULL COMMENT '主体编码: team_code/user_id/稳定外部编码',
  `operation_type` varchar(32) NOT NULL COMMENT '操作类型: REPORT/MATERIAL/WAITING',
  `operation_status` varchar(32) NOT NULL COMMENT '操作状态: DONE/CANCELLED/INVALID',
  `operation_time` datetime DEFAULT NULL COMMENT '操作完成时间',
  `credential_id` bigint DEFAULT NULL COMMENT '触发操作的证件ID',
  `operator_user_id` bigint DEFAULT NULL COMMENT '操作人用户ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `delegate_user_id` bigint DEFAULT NULL COMMENT '代领人用户ID',
  `delegate_name` varchar(100) DEFAULT NULL COMMENT '代领人姓名',
  `delegate_credential_id` bigint DEFAULT NULL COMMENT '代领人证件ID',
  `delegate_relation` varchar(64) DEFAULT NULL COMMENT '代领关系: SELF本人/TEAM_MEMBER同队成员',
  `last_log_id` bigint DEFAULT NULL COMMENT '最近一次成功操作流水ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识: 0正常/1删除',
  PRIMARY KEY (`state_id`),
  KEY `idx_scene_subject_operation_lookup` (`competition_series_id`, `scope_type`, `scope_ref_id`, `subject_type`, `subject_code`, `operation_type`, `operation_status`, `deleted`),
  KEY `idx_scene_subject_operation_series` (`competition_series_id`),
  KEY `idx_scene_subject_operation_scope` (`scope_type`, `scope_ref_id`),
  KEY `idx_scene_subject_operation_subject` (`subject_type`, `subject_code`),
  KEY `idx_scene_subject_operation_credential` (`credential_id`),
  KEY `idx_scene_subject_operation_delegate` (`delegate_user_id`),
  KEY `idx_scene_subject_operation_log` (`last_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赛事现场主体操作状态表';
