-- Optional indexes for competition scene checkin overview.
-- Do not run blindly in production; verify existing indexes and execution window first.

SET @schemaName := DATABASE();

SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'competition_scene_schedule'
    AND index_name = 'idx_scene_schedule_overview_time'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_scene_schedule_overview_time ON competition_scene_schedule (competition_series_id, del_flag, status, contest_start_time)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'competition_scene_schedule'
    AND index_name = 'idx_scene_schedule_report_location_time'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_scene_schedule_report_location_time ON competition_scene_schedule (report_location, contest_start_time)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'competition_scene_schedule_target'
    AND index_name = 'idx_scene_target_overview_role'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_scene_target_overview_role ON competition_scene_schedule_target (schedule_id, competition_role_name, status, del_flag, match_status)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'competition_scene_credential'
    AND index_name = 'idx_scene_credential_schedule_target_status'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_scene_credential_schedule_target_status ON competition_scene_credential (schedule_id, target_id, credential_status, del_flag)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idxExists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = @schemaName
    AND table_name = 'competition_scene_operation_log'
    AND index_name = 'idx_scene_log_checkin_overview'
);
SET @sql := IF(@idxExists = 0,
  'CREATE INDEX idx_scene_log_checkin_overview ON competition_scene_operation_log (schedule_id, operation_type, operation_stage, operation_result, result_status, operation_time)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
