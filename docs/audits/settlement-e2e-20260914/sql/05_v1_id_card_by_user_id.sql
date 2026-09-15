-- MySQL query-window version. Set the reviewed V1 user ID before executing both statements.
-- NULL is deliberate: an unconfigured execution returns no identity value.
-- SET changes a connection-local variable only; this file does not modify account or business rows.
-- Sensitive SELECT result: keep private, do not copy to logs, screenshots or ordinary audit exports.
SET @v1_user_id = NULL;

SELECT id_card
FROM jiaoxue_test.auth_info
WHERE @v1_user_id IS NOT NULL
  AND @v1_user_id > 0
  AND user_id = @v1_user_id
  AND auth_status = '5'
  AND COALESCE(del_flag, '0') = '0'
ORDER BY auth_time DESC, auth_id DESC
LIMIT 1;
