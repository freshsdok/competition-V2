-- 本轮 42 个测试账号的本人身份证查询。将 NULL 改为 Excel 中的 V1 用户 ID。
-- 默认不返回数据。身份证仅用于对应本人账号的测试填报，不写入账号清单或截图。
SET @v1_user_id = NULL;

SELECT id_card
FROM jiaoxue_test.auth_info
WHERE @v1_user_id IS NOT NULL
  AND user_id = @v1_user_id
  AND user_id IN (
    243, 336, 1034, 1196, 1197, 1367, 1477, 1588, 5285, 5423,
    5781, 6240, 6244, 7280, 7984, 17849, 17854, 22048, 26321, 26324,
    30134, 34071, 35594, 35605, 35606, 39781, 39790, 41694, 42839, 45064,
    45196, 45201, 45341, 45344, 50937, 50938, 51390, 55868, 56116, 56117,
    58389, 58423
  )
  AND auth_status = '5'
  AND COALESCE(del_flag, '0') = '0'
ORDER BY auth_time DESC, auth_id DESC
LIMIT 1;
