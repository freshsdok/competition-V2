-- 后台“证书导入”菜单。
-- 依赖前后端证书导入功能部署完成；脚本可重复执行。

SET @certificate_manage_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE menu_name = '证书管理'
    AND parent_id = 0
    AND platform_type = 'admin'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark, platform_type
)
SELECT
  '证书导入', @certificate_manage_menu_id, 5, 'certificateImport',
  'certInterconnect/certificateImport/index', '', 'CertificateImport',
  1, 0, 'C', '0', '0', 'competition:certificateImport:generateSql', 'upload',
  'system', NOW(), '', NULL, '校验证书Excel并生成双表导入SQL，不直接写数据库', 'admin'
WHERE @certificate_manage_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE component = 'certInterconnect/certificateImport/index'
      AND platform_type = 'admin'
  );

SET @certificate_import_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE component = 'certInterconnect/certificateImport/index'
    AND platform_type = 'admin'
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @certificate_import_menu_id
FROM sys_role r
LEFT JOIN sys_role_menu rm
  ON rm.role_id = r.role_id
 AND rm.menu_id = @certificate_import_menu_id
WHERE r.role_key = 'admin'
  AND @certificate_import_menu_id IS NOT NULL
  AND rm.menu_id IS NULL;
