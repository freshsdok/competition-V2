-- Competition scene target credential type口径调整。
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- 本脚本仅供人工审查后执行，不连接生产库，不删除旧字段。

SET NAMES utf8mb4;

-- 字段已在 20260629_competition_scene_credential.sql 中创建，本轮仅更新注释口径。
ALTER TABLE `competition_scene_schedule`
  MODIFY COLUMN `credential_type` varchar(32) NOT NULL COMMENT '历史兼容字段：证件类型已迁移到competition_scene_schedule_target.credential_type',
  MODIFY COLUMN `config_dimension` varchar(32) NOT NULL COMMENT '历史兼容字段：新安排默认按PERSON对象匹配',
  MODIFY COLUMN `waiting_group_code` varchar(64) DEFAULT NULL COMMENT '候场分组编码：系统维护，不再由前端手工录入';

ALTER TABLE `competition_scene_schedule_target`
  MODIFY COLUMN `credential_type` varchar(32) NOT NULL COMMENT '证件类型：PARTICIPANT参赛证/TEACHER教师证/EXPERT专家证/STAFF工作人员证',
  MODIFY COLUMN `competition_role_name` varchar(64) DEFAULT NULL COMMENT '现场角色：TEACHER/MEMBER/EXPERT/CAPTAIN/MATERIAL_STAFF/CHECKIN_STAFF';

ALTER TABLE `competition_scene_credential`
  MODIFY COLUMN `credential_type` varchar(32) NOT NULL COMMENT '证件类型：PARTICIPANT参赛证/TEACHER教师证/EXPERT专家证/STAFF工作人员证',
  MODIFY COLUMN `competition_role_name` varchar(64) DEFAULT NULL COMMENT '现场角色快照：TEACHER/MEMBER/EXPERT/CAPTAIN/MATERIAL_STAFF/CHECKIN_STAFF';

-- 说明：
-- 1. competition_scene_schedule_target.credential_type 已存在，无需重复 ADD COLUMN。
-- 2. competition_scene_credential.uk_scene_credential_no 已存在，无需重复 ADD UNIQUE KEY。
-- 3. 旧值 COMPETITOR 由代码兼容并在后续写入时归一为 PARTICIPANT。
-- 4. 如确认不再需要旧值，可人工审查后执行：
-- UPDATE competition_scene_schedule_target SET credential_type = 'PARTICIPANT' WHERE credential_type = 'COMPETITOR';
-- UPDATE competition_scene_credential SET credential_type = 'PARTICIPANT' WHERE credential_type = 'COMPETITOR';
