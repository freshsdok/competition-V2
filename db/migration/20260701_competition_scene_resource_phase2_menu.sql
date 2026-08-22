-- Competition scene resource phase 2 admin menu.
-- This script adds the admin menu and button permissions for scene resource management.
-- It does not hard-code parent menu id; parent menu is resolved by menu name/path.

SET NAMES utf8mb4;

SET @parent_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_name = '赛事管理'
      AND path = 'tournament'
      AND menu_type = 'M'
      AND platform_type = 'admin'
    ORDER BY menu_id
    LIMIT 1
);

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark, platform_type
)
SELECT
    '资源管理', @parent_id, 81, 'sceneResource', 'tournament/sceneResource/index', NULL, 'SceneResource',
    1, 0, 'C', '0', '0', 'competition:sceneResource:list', 'tool',
    'phase2', NOW(), '大赛现场设备资源管理', 'admin'
WHERE @parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE path = 'sceneResource'
        AND component = 'tournament/sceneResource/index'
        AND platform_type = 'admin'
  );

SET @resource_menu_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE path = 'sceneResource'
      AND component = 'tournament/sceneResource/index'
      AND platform_type = 'admin'
    ORDER BY menu_id
    LIMIT 1
);

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark, platform_type
)
SELECT
    btn.menu_name, @resource_menu_id, btn.order_num, '#', NULL, NULL, '',
    1, 0, 'F', '0', '0', btn.perms, '#',
    'phase2', NOW(), btn.remark, 'admin'
FROM (
    SELECT '资源查询' AS menu_name, 1 AS order_num, 'competition:sceneResource:list' AS perms, '资源管理列表权限' AS remark
    UNION ALL SELECT '资源详情', 2, 'competition:sceneResource:query', '资源管理详情权限'
    UNION ALL SELECT '资源新增', 3, 'competition:sceneResource:add', '资源管理新增权限'
    UNION ALL SELECT '资源修改', 4, 'competition:sceneResource:edit', '资源管理修改权限'
    UNION ALL SELECT '资源删除', 5, 'competition:sceneResource:remove', '资源管理删除权限'
    UNION ALL SELECT '资源状态变更', 6, 'competition:sceneResource:changeStatus', '资源管理状态变更权限'
) btn
WHERE @resource_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu m
      WHERE m.perms = btn.perms
        AND m.platform_type = 'admin'
  );
