-- Competition on-site announcements and personal notices.
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `competition_scene_notice` (
  `notice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `notice_type` varchar(32) NOT NULL COMMENT '通知类型: ANNOUNCEMENT大赛公告/PERSONAL个人通知',
  `scope_type` varchar(32) NOT NULL COMMENT '可见范围: COMPETITION赛事级/SCHEDULE赛场级/PERSON个人级',
  `competition_series_id` bigint NOT NULL COMMENT '赛事届次ID',
  `competition_id` bigint DEFAULT NULL COMMENT '赛事ID',
  `target_id` bigint DEFAULT NULL COMMENT '现场绑定对象ID，个人通知使用',
  `user_id` bigint DEFAULT NULL COMMENT '系统用户ID快照',
  `member_id` bigint DEFAULT NULL COMMENT '报名成员ID快照',
  `recipient_name` varchar(100) DEFAULT NULL COMMENT '接收人姓名快照',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` longtext NOT NULL COMMENT '经白名单清洗后的富文本内容',
  `notice_level` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '级别: NORMAL/IMPORTANT/URGENT',
  `is_top` char(1) NOT NULL DEFAULT '0' COMMENT '是否置顶: 0否/1是',
  `sort_no` int NOT NULL DEFAULT 100 COMMENT '排序值，越小越靠前',
  `publish_status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态: DRAFT/PUBLISHED/DISABLED',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态: 0正常/1停用',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志: 0正常/1删除',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`),
  KEY `idx_scene_notice_competition` (`competition_series_id`, `notice_type`, `publish_status`, `status`, `del_flag`),
  KEY `idx_scene_notice_target` (`target_id`, `notice_type`, `del_flag`),
  KEY `idx_scene_notice_user` (`user_id`, `notice_type`, `del_flag`),
  KEY `idx_scene_notice_member` (`member_id`, `notice_type`, `del_flag`),
  KEY `idx_scene_notice_valid_time` (`publish_time`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赛事现场通知';

CREATE TABLE IF NOT EXISTS `competition_scene_notice_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT '通知ID',
  `schedule_id` bigint NOT NULL COMMENT '赛场安排ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_notice_schedule` (`notice_id`, `schedule_id`),
  KEY `idx_scene_schedule_notice` (`schedule_id`, `notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='赛事通知可见赛场关系';

