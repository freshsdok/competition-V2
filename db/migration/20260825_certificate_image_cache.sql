-- 证书图片缓存、同步运行和负责人异步导出任务。
-- MySQL 8.0；图片正文存放在私有 OSS/MinIO，本表仅保存元数据和对象键。

CREATE TABLE IF NOT EXISTS certificate_image_cache (
  cache_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '缓存主键',
  cert_code VARCHAR(100) NOT NULL COMMENT '证书编号',
  contest_name VARCHAR(500) DEFAULT NULL COMMENT '赛事名称',
  recipient_name VARCHAR(200) DEFAULT NULL COMMENT '获证人姓名',
  session VARCHAR(50) DEFAULT NULL COMMENT '届数',
  contest_area VARCHAR(200) DEFAULT NULL COMMENT '赛区',
  runing_num_year INT DEFAULT NULL COMMENT '外部接口年份字段',
  object_key VARCHAR(1000) DEFAULT NULL COMMENT '私有对象存储键',
  file_name VARCHAR(255) DEFAULT NULL COMMENT '下载文件名',
  mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  file_size BIGINT DEFAULT NULL COMMENT '文件字节数',
  sha256 CHAR(64) DEFAULT NULL COMMENT '文件SHA-256',
  cache_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SYNCING/SUCCESS/NOT_FOUND/FAILED',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '最近一轮重试次数',
  last_error VARCHAR(1000) DEFAULT NULL COMMENT '最近失败原因',
  next_retry_time DATETIME DEFAULT NULL COMMENT '冷却结束时间',
  last_sync_time DATETIME DEFAULT NULL COMMENT '最后同步时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (cache_id),
  UNIQUE KEY uk_certificate_image_cache_cert_code (cert_code),
  KEY idx_certificate_image_cache_status_retry (cache_status, next_retry_time),
  KEY idx_certificate_image_cache_recipient (recipient_name),
  KEY idx_certificate_image_cache_contest (contest_name(100), contest_area(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书图片私有缓存元数据';

CREATE TABLE IF NOT EXISTS certificate_image_sync_run (
  run_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '运行主键',
  source VARCHAR(20) NOT NULL COMMENT 'MANUAL/SCHEDULED',
  run_status VARCHAR(20) NOT NULL COMMENT 'RUNNING/PAUSED/COMPLETED/FAILED',
  total_count BIGINT NOT NULL DEFAULT 0,
  processed_count BIGINT NOT NULL DEFAULT 0,
  success_count BIGINT NOT NULL DEFAULT 0,
  failure_count BIGINT NOT NULL DEFAULT 0,
  current_cert_code VARCHAR(100) DEFAULT NULL,
  last_error VARCHAR(1000) DEFAULT NULL,
  requests_per_second INT NOT NULL DEFAULT 1,
  operator_id BIGINT DEFAULT NULL,
  operator_name VARCHAR(100) DEFAULT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (run_id),
  KEY idx_certificate_image_sync_run_status (run_status, run_id),
  KEY idx_certificate_image_sync_run_start (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书图片批量同步运行记录';

CREATE TABLE IF NOT EXISTS certificate_export_task (
  task_id VARCHAR(64) NOT NULL COMMENT '异步任务ID',
  user_id BIGINT NOT NULL COMMENT '任务所属用户',
  export_scope VARCHAR(20) NOT NULL COMMENT 'ALL/SELECTED',
  selected_cert_codes LONGTEXT DEFAULT NULL COMMENT '创建时选中编号JSON',
  task_status VARCHAR(20) NOT NULL COMMENT 'QUEUED/RESOLVING/PACKAGING/UPLOADING/COMPLETED/PARTIAL/FAILED/EXPIRED',
  phase VARCHAR(100) DEFAULT NULL COMMENT '前端阶段说明',
  progress INT NOT NULL DEFAULT 0 COMMENT '0-100',
  total_count BIGINT NOT NULL DEFAULT 0,
  processed_count BIGINT NOT NULL DEFAULT 0,
  success_count BIGINT NOT NULL DEFAULT 0,
  failure_count BIGINT NOT NULL DEFAULT 0,
  zip_object_key VARCHAR(1000) DEFAULT NULL COMMENT 'ZIP私有对象键',
  zip_file_name VARCHAR(255) DEFAULT NULL,
  missing_cert_codes LONGTEXT DEFAULT NULL COMMENT '未下载证书编号JSON',
  last_error VARCHAR(1000) DEFAULT NULL,
  expires_at DATETIME DEFAULT NULL COMMENT '任务和ZIP过期时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  finish_time DATETIME DEFAULT NULL,
  PRIMARY KEY (task_id),
  KEY idx_certificate_export_task_user (user_id, create_time),
  KEY idx_certificate_export_task_expire (task_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='负责人证书异步ZIP导出任务';

-- 只预置负责人有权导出的已缴费团队学生证书；不在迁移阶段访问外部接口。
INSERT IGNORE INTO certificate_image_cache
  (cert_code, contest_name, recipient_name, session, runing_num_year,
   cache_status, retry_count, create_time, update_time)
SELECT uco.cert_code,
       MAX(NULLIF(uco.competition_name, '')),
       MAX(NULLIF(uco.user_name, '')),
       MAX(COALESCE(NULLIF(csi.competition_series, ''),
                    NULLIF(csi.competition_series_name, ''))),
       MAX(CASE WHEN uco.year REGEXP '^[0-9]{4}$' THEN CAST(uco.year AS UNSIGNED) END),
       'PENDING', 0, NOW(), NOW()
FROM user_certificate_origin uco
LEFT JOIN competition_series_info csi
  ON csi.competition_series_id = uco.competition_series_id
WHERE uco.del_flag = '0'
  AND uco.cert_code IS NOT NULL AND uco.cert_code != ''
  AND uco.team_code IS NOT NULL AND uco.team_code != ''
  AND EXISTS (
    SELECT 1 FROM competition_apply_info cai
    WHERE cai.competition_series_id = uco.competition_series_id
      AND cai.team_code = uco.team_code
      AND cai.leader_teacher_id IS NOT NULL
      AND cai.pay_status = 'paid'
      AND cai.del_flag = '0'
  )
GROUP BY uco.cert_code;

SET @certificate_manage_menu_id := (
  SELECT menu_id FROM sys_menu
  WHERE menu_name = '证书管理' AND parent_id = 0 AND platform_type = 'admin'
  ORDER BY menu_id LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark, platform_type
)
SELECT '证书图片同步', @certificate_manage_menu_id, 6, 'certificateImageSync',
       'certInterconnect/certificateImageSync/index', '', 'CertificateImageSync',
       1, 0, 'C', '0', '0', 'competition:certificateImageSync:view', 'picture',
       'system', NOW(), '', NULL, '证书图片缓存统计、同步控制和记录查询', 'admin'
WHERE @certificate_manage_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu
    WHERE component = 'certInterconnect/certificateImageSync/index'
      AND platform_type = 'admin'
  );

SET @certificate_image_sync_menu_id := (
  SELECT menu_id FROM sys_menu
  WHERE component = 'certInterconnect/certificateImageSync/index'
    AND platform_type = 'admin'
  ORDER BY menu_id LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark, platform_type
)
SELECT permission_name, @certificate_image_sync_menu_id, order_num, '', '', '', '',
       1, 0, 'F', '0', '0', perms, '#', 'system', NOW(), '', NULL, permission_name, 'admin'
FROM (
  SELECT '查看证书图片同步' permission_name, 1 order_num, 'competition:certificateImageSync:view' perms
  UNION ALL SELECT '启动证书图片同步', 2, 'competition:certificateImageSync:start'
  UNION ALL SELECT '暂停继续证书图片同步', 3, 'competition:certificateImageSync:pause'
  UNION ALL SELECT '重试证书图片失败记录', 4, 'competition:certificateImageSync:retry'
) permissions
WHERE @certificate_image_sync_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu existing
    WHERE existing.parent_id = @certificate_image_sync_menu_id
      AND existing.perms = permissions.perms
      AND existing.platform_type = 'admin'
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role_menu.role_id, role_menu.menu_id
FROM (
  SELECT r.role_id, m.menu_id
  FROM sys_role r
  JOIN sys_menu m ON (m.menu_id = @certificate_image_sync_menu_id
                      OR m.parent_id = @certificate_image_sync_menu_id)
  WHERE r.role_key = 'admin' AND m.platform_type = 'admin'
) role_menu
LEFT JOIN sys_role_menu existing
  ON existing.role_id = role_menu.role_id AND existing.menu_id = role_menu.menu_id
WHERE existing.menu_id IS NULL;
