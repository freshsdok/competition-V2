START TRANSACTION READ ONLY;
WITH targets AS (SELECT 1 ordinal,35594 user_id UNION ALL SELECT 2 ordinal,35605 user_id UNION ALL SELECT 3 ordinal,35606 user_id UNION ALL SELECT 4 ordinal,1588 user_id UNION ALL SELECT 5 ordinal,45064 user_id UNION ALL SELECT 6 ordinal,45196 user_id UNION ALL SELECT 7 ordinal,45201 user_id UNION ALL SELECT 8 ordinal,1367 user_id UNION ALL SELECT 9 ordinal,26321 user_id UNION ALL SELECT 10 ordinal,5781 user_id UNION ALL SELECT 11 ordinal,26324 user_id UNION ALL SELECT 12 ordinal,1034 user_id UNION ALL SELECT 13 ordinal,58423 user_id UNION ALL SELECT 14 ordinal,30134 user_id UNION ALL SELECT 15 ordinal,58389 user_id UNION ALL SELECT 16 ordinal,1477 user_id UNION ALL SELECT 17 ordinal,34071 user_id UNION ALL SELECT 18 ordinal,56116 user_id UNION ALL SELECT 19 ordinal,56117 user_id UNION ALL SELECT 20 ordinal,55868 user_id UNION ALL SELECT 21 ordinal,41694 user_id UNION ALL SELECT 22 ordinal,7984 user_id UNION ALL SELECT 23 ordinal,51390 user_id UNION ALL SELECT 24 ordinal,42839 user_id UNION ALL SELECT 25 ordinal,5423 user_id UNION ALL SELECT 26 ordinal,17849 user_id UNION ALL SELECT 27 ordinal,17854 user_id UNION ALL SELECT 28 ordinal,1196 user_id UNION ALL SELECT 29 ordinal,39790 user_id UNION ALL SELECT 30 ordinal,7280 user_id UNION ALL SELECT 31 ordinal,39781 user_id UNION ALL SELECT 32 ordinal,1197 user_id UNION ALL SELECT 33 ordinal,45341 user_id UNION ALL SELECT 34 ordinal,50937 user_id UNION ALL SELECT 35 ordinal,50938 user_id UNION ALL SELECT 36 ordinal,45344 user_id UNION ALL SELECT 37 ordinal,22048 user_id UNION ALL SELECT 38 ordinal,5285 user_id UNION ALL SELECT 39 ordinal,6240 user_id UNION ALL SELECT 40 ordinal,6244 user_id UNION ALL SELECT 41 ordinal,243 user_id UNION ALL SELECT 42 ordinal,336 user_id), ranked AS (
 SELECT auth_id,user_id,real_name,id_card,auth_time,
 ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY auth_time DESC,auth_id DESC) rn
 FROM jiaoxue_test.auth_info
 WHERE user_id IN (35594,35605,35606,1588,45064,45196,45201,1367,26321,5781,26324,1034,58423,30134,58389,1477,34071,56116,56117,55868,41694,7984,51390,42839,5423,17849,17854,1196,39790,7280,39781,1197,45341,50937,50938,45344,22048,5285,6240,6244,243,336)
 AND auth_status='5' AND COALESCE(del_flag,'0')='0'
)
SELECT t.ordinal,t.user_id,u.user_name AS login_name,r.real_name,r.id_card,r.auth_id,r.auth_time
FROM targets t
LEFT JOIN jiaoxue_test.sys_user u ON u.user_id=t.user_id
LEFT JOIN ranked r ON r.user_id=t.user_id AND r.rn=1
ORDER BY t.ordinal;
ROLLBACK;
