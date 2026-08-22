-- Optional admin menu for competition scene schedule configuration.
-- Run after backend/frontend deployment if the menu does not already exist.

SET @parentId := (
  SELECT menu_id
  FROM sys_menu
  WHERE menu_type = 'M'
    AND menu_name IN ('赛事管理', '赛事中心', '竞赛管理', '大赛管理')
  ORDER BY menu_id
  LIMIT 1
);

SET @parentId := IFNULL(@parentId, 0);

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT
  '现场安排配置', @parentId, 80, 'sceneSchedule', 'tournament/sceneSchedule/index',
  1, 0, 'C', '0', '0', 'competition:sceneSchedule:list', 'date', 'admin', SYSDATE(), '', NULL, '赛事现场安排配置菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE component = 'tournament/sceneSchedule/index'
);

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
SELECT '现场安排查询', @sceneScheduleMenuId, 1, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneSchedule:query', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneSchedule:query');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场安排新增', @sceneScheduleMenuId, 2, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneSchedule:add', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneSchedule:add');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场安排修改', @sceneScheduleMenuId, 3, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneSchedule:edit', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneSchedule:edit');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场安排删除', @sceneScheduleMenuId, 4, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneSchedule:remove', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneSchedule:remove');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场证件查询', @sceneScheduleMenuId, 5, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneCredential:list', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneCredential:list');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场证件详情', @sceneScheduleMenuId, 6, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneCredential:query', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneCredential:query');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场证件生成', @sceneScheduleMenuId, 7, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneCredential:add', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneCredential:add');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场证件维护', @sceneScheduleMenuId, 8, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneCredential:edit', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneCredential:edit');

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '现场流水查询', @sceneScheduleMenuId, 9, '#', '', 1, 0, 'F', '0', '0',
       'competition:sceneVerify:list', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @sceneScheduleMenuId AND perms = 'competition:sceneVerify:list');
