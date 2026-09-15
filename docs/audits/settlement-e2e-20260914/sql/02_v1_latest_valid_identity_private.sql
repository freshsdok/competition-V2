-- READ ONLY, SENSITIVE RESULT. One bound parameter: the selected approved V1 test user_id.
-- Result is for private in-memory use only. Do not print it, log bindings/results, or save an evidence export.
-- Do not replace missing current evidence with a registration/certificate snapshot.
SELECT id_card
FROM auth_info
WHERE user_id = ?
  AND auth_status = '5'
  AND COALESCE(del_flag, '0') = '0'
ORDER BY auth_time DESC, auth_id DESC
LIMIT 1;
