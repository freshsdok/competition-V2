-- Competition-level direct scene credential issue support.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- Do not run against production before review.

SET NAMES utf8mb4;

ALTER TABLE `competition_scene_credential`
  ADD COLUMN `subject_code` varchar(128) DEFAULT NULL COMMENT '证件主体编码: team_code/user_id/稳定外部编码' AFTER `subject_type`;

ALTER TABLE `competition_scene_credential`
  ADD KEY `idx_scene_credential_subject_code` (`subject_type`, `subject_code`),
  ADD KEY `idx_scene_credential_competition_subject` (`competition_series_id`, `scope_type`, `subject_type`, `subject_code`, `credential_type`, `credential_status`, `del_flag`);

UPDATE `competition_scene_credential`
SET `subject_code` = CASE
  WHEN `subject_type` = 'TEAM' THEN `team_code`
  WHEN `user_id` IS NOT NULL THEN CAST(`user_id` AS CHAR)
  WHEN `member_id` IS NOT NULL THEN CONCAT('MEMBER:', `member_id`)
  ELSE NULL
END
WHERE `subject_code` IS NULL
  AND `del_flag` = '0';
