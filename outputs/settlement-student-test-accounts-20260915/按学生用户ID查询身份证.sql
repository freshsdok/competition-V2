-- 仅选择本清单中的目标本人；默认NULL不返回身份证。
-- 可选正向学生ID：29001, 29041, 26023, 26029, 26013, 26088。
-- 查询结果敏感，不复制到普通报告、聊天或截图。
SET @v1_user_id = NULL;

SELECT id_card
FROM jiaoxue_test.auth_info
WHERE @v1_user_id IS NOT NULL
  AND user_id = @v1_user_id
  AND auth_status = '5'
  AND COALESCE(del_flag, '0') = '0'
ORDER BY auth_time DESC, auth_id DESC
LIMIT 1;
