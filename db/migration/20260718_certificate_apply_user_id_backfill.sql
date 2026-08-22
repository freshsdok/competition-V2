-- 第十三届赛事（competition_series_id=77）已缴费参赛选手用户ID回填。
-- 匹配顺序：唯一且审核通过的实名认证账号优先；未实名时使用报名手机号唯一对应的正常账号。
-- 身份证号或手机号无法唯一确认时不会更新。

SET NAMES utf8mb4;
START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS `tmp_verified_auth_user`;
CREATE TEMPORARY TABLE `tmp_verified_auth_user` (
  `id_card` varchar(100) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tmp_verified_auth_user` (`id_card`, `user_id`)
SELECT TRIM(ai.`id_card`), MAX(ai.`user_id`)
FROM `auth_info` ai
JOIN `sys_user` su
  ON su.`user_id` = ai.`user_id`
 AND su.`del_flag` = '0'
WHERE ai.`del_flag` = '0'
  AND ai.`auth_status` = '5'
  AND ai.`user_id` IS NOT NULL
  AND ai.`id_card` IS NOT NULL
  AND TRIM(ai.`id_card`) <> ''
GROUP BY TRIM(ai.`id_card`)
HAVING COUNT(DISTINCT ai.`user_id`) = 1;

DROP TEMPORARY TABLE IF EXISTS `tmp_unique_phone_user`;
CREATE TEMPORARY TABLE `tmp_unique_phone_user` (
  `phone` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tmp_unique_phone_user` (`phone`, `user_id`)
SELECT TRIM(su.`phonenumber`), MAX(su.`user_id`)
FROM `sys_user` su
WHERE su.`del_flag` = '0'
  AND su.`status` = '0'
  AND su.`user_id` IS NOT NULL
  AND su.`phonenumber` IS NOT NULL
  AND TRIM(su.`phonenumber`) <> ''
GROUP BY TRIM(su.`phonenumber`)
HAVING COUNT(DISTINCT su.`user_id`) = 1;

DROP TEMPORARY TABLE IF EXISTS `tmp_apply_user_backfill`;
CREATE TEMPORARY TABLE `tmp_apply_user_backfill` (
  `member_id` bigint NOT NULL,
  `team_code` varchar(64) DEFAULT NULL,
  `user_name` varchar(100) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`member_id`),
  KEY `idx_tmp_backfill_team_user` (`team_code`, `user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tmp_apply_user_backfill` (`member_id`, `team_code`, `user_name`, `user_id`)
SELECT cai.`member_id`, cai.`team_code`, cai.`user_name`, vau.`user_id`
FROM `competition_apply_info` cai
JOIN `tmp_verified_auth_user` vau
  ON vau.`id_card` = TRIM(cai.`id_card`)
WHERE cai.`competition_series_id` = 77
  AND cai.`del_flag` = '0'
  AND cai.`pay_status` = 'paid'
  AND cai.`competition_role_name` <> '指导教师'
  AND cai.`user_id` IS NULL;

-- 未通过实名认证的数据，再按报名手机号唯一匹配正常账号。
INSERT INTO `tmp_apply_user_backfill` (`member_id`, `team_code`, `user_name`, `user_id`)
SELECT cai.`member_id`, cai.`team_code`, cai.`user_name`, upu.`user_id`
FROM `competition_apply_info` cai
JOIN `tmp_unique_phone_user` upu
  ON upu.`phone` = TRIM(cai.`phone`)
WHERE cai.`competition_series_id` = 77
  AND cai.`del_flag` = '0'
  AND cai.`pay_status` = 'paid'
  AND cai.`competition_role_name` <> '指导教师'
  AND cai.`user_id` IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `tmp_verified_auth_user` vau
    WHERE vau.`id_card` = TRIM(cai.`id_card`)
  );

-- 执行前确认：全新环境中为“实名认证匹配数 + 手机号匹配数”；重复执行只处理尚未回填的数据。
SELECT COUNT(*) AS `safe_apply_user_backfill_rows`
FROM `tmp_apply_user_backfill`;

UPDATE `competition_apply_info` cai
JOIN `tmp_apply_user_backfill` t ON t.`member_id` = cai.`member_id`
SET cai.`user_id` = t.`user_id`,
    cai.`update_time` = NOW(),
    cai.`version` = COALESCE(cai.`version`, 0) + 1
WHERE cai.`competition_series_id` = 77
  AND cai.`del_flag` = '0'
  AND cai.`user_id` IS NULL;

SELECT ROW_COUNT() AS `competition_apply_info_updated_rows`;

UPDATE `team_member_rela` tmr
JOIN (
  SELECT `team_code`, `user_name`, MAX(`user_id`) AS `user_id`
  FROM `tmp_apply_user_backfill`
  WHERE `team_code` IS NOT NULL
    AND `team_code` <> ''
  GROUP BY `team_code`, `user_name`
  HAVING COUNT(DISTINCT `user_id`) = 1
) t
  ON t.`team_code` = tmr.`team_code`
 AND t.`user_name` = tmr.`user_name`
SET tmr.`user_id` = t.`user_id`,
    tmr.`update_time` = NOW(),
    tmr.`version` = COALESCE(tmr.`version`, 0) + 1
WHERE tmr.`del_flag` = '0'
  AND tmr.`user_id` IS NULL;

SELECT ROW_COUNT() AS `team_member_rela_updated_rows`;

DROP TEMPORARY TABLE IF EXISTS `tmp_apply_user_backfill`;
DROP TEMPORARY TABLE IF EXISTS `tmp_unique_phone_user`;
DROP TEMPORARY TABLE IF EXISTS `tmp_verified_auth_user`;

COMMIT;
