-- Competition scene credential one-card active core credential key.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.
-- This migration only adds a nullable key column and unique index. It does not migrate or clean legacy test data.

SET NAMES utf8mb4;

ALTER TABLE `competition_scene_credential`
  ADD COLUMN `active_core_credential_key` varchar(255) DEFAULT NULL
    COMMENT '有效核心证件唯一键，COMPETITION且EFFECTIVE且del_flag=0时写入competitionSeriesId:subjectType:subjectCode:credentialType，撤销/删除/失效后置空'
    AFTER `scope_ref_id`;

ALTER TABLE `competition_scene_credential`
  ADD UNIQUE KEY `uk_scene_credential_active_core_key` (`active_core_credential_key`);
