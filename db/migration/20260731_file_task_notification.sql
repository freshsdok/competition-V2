-- 文件上传任务通知及查询索引。
-- Target database: MySQL 5.7+/8.0+，脚本可重复执行。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `file_task_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `file_task_id` bigint NOT NULL COMMENT '文件任务ID',
  `target_type` varchar(32) NOT NULL COMMENT '发送范围: SINGLE/ALL/UPLOADED/NOT_UPLOADED',
  `target_user_id` bigint DEFAULT NULL COMMENT '单人发送目标用户ID',
  `recipient_user_ids` longtext NOT NULL COMMENT '发送时实际收件人ID快照，升序逗号分隔',
  `recipient_count` int NOT NULL DEFAULT 0 COMMENT '实际收件人数',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` longtext NOT NULL COMMENT '经白名单清洗后的富文本正文',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/WITHDRAWN',
  `sender_user_id` bigint NOT NULL COMMENT '发送人用户ID',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `withdraw_user_id` bigint DEFAULT NULL COMMENT '撤回人用户ID',
  `withdraw_time` datetime DEFAULT NULL COMMENT '撤回时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_task_notice_task_status_time` (`file_task_id`, `status`, `send_time`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件上传任务通知';

SET @schemaName := DATABASE();
SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'file_task_notification'
    AND index_name = 'idx_file_task_notice_task_status_time'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_file_task_notice_task_status_time ON file_task_notification (file_task_id, status, send_time, id)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'file_upload_manager'
    AND index_name = 'idx_file_upload_task_user_state'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_file_upload_task_user_state ON file_upload_manager (file_task_id, user_id, del_flag, id)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 在现有文件任务菜单下增加发送/撤回权限，并复制“修改”权限已有角色的授权。
SET @fileTaskEditMenuId := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'system:fileDistributeTask:edit'
    AND platform_type = 'admin'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark, platform_type
)
SELECT
  '文件任务通知', parent_id, order_num + 1, '#', '', '', '',
  1, 0, 'F', '0', '0', 'system:fileDistributeTask:notify', '#',
  'system', NOW(), '', NULL, '发送及撤回文件上传任务通知', platform_type
FROM sys_menu
WHERE menu_id = @fileTaskEditMenuId
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE perms = 'system:fileDistributeTask:notify'
      AND platform_type = 'admin'
  );

SET @fileTaskNotifyMenuId := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'system:fileDistributeTask:notify'
    AND platform_type = 'admin'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT source_role.role_id, @fileTaskNotifyMenuId
FROM (
  SELECT DISTINCT role_id
  FROM sys_role_menu
  WHERE menu_id = @fileTaskEditMenuId
) source_role
LEFT JOIN sys_role_menu existing_grant
  ON existing_grant.role_id = source_role.role_id
 AND existing_grant.menu_id = @fileTaskNotifyMenuId
WHERE @fileTaskNotifyMenuId IS NOT NULL
  AND existing_grant.menu_id IS NULL;
