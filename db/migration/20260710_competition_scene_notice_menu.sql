-- Button permissions under the existing 现场安排配置 menu.

SET @sceneScheduleMenuId := (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'tournament/sceneSchedule/index'
  ORDER BY menu_id DESC
  LIMIT 1
);

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场通知列表', @sceneScheduleMenuId, 30, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneNotice:list', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @sceneScheduleMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'competition:sceneNotice:list');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场通知详情', @sceneScheduleMenuId, 31, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneNotice:query', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @sceneScheduleMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'competition:sceneNotice:query');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场通知新增', @sceneScheduleMenuId, 32, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneNotice:add', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @sceneScheduleMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'competition:sceneNotice:add');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场通知修改', @sceneScheduleMenuId, 33, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneNotice:edit', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @sceneScheduleMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'competition:sceneNotice:edit');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场通知删除', @sceneScheduleMenuId, 34, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneNotice:remove', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @sceneScheduleMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'competition:sceneNotice:remove');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场通知发布', @sceneScheduleMenuId, 35, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneNotice:publish', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @sceneScheduleMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'competition:sceneNotice:publish');

