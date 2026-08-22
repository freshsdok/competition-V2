-- Optional admin menu for competition scene checkin overview.
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
  '签到概览', @parentId, 85, 'checkinOverview', 'tournament/checkinOverview/index',
  1, 0, 'C', '0', '0', 'competition:checkinOverview:list', 'chart', 'admin', SYSDATE(), '', NULL, '赛事现场签到概览菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE component = 'tournament/checkinOverview/index'
);

SET @checkinOverviewMenuId := (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'tournament/checkinOverview/index'
  ORDER BY menu_id DESC
  LIMIT 1
);

INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '签到概览查询', @checkinOverviewMenuId, 1, '#', '', 1, 0, 'F', '0', '0',
       'competition:checkinOverview:list', '#', 'admin', SYSDATE(), '', NULL, ''
WHERE @checkinOverviewMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @checkinOverviewMenuId AND perms = 'competition:checkinOverview:list');
