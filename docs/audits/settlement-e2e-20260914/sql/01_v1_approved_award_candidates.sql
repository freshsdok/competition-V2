-- READ ONLY. MySQL 8.0. Connect to the independently verified V1 jiaoxue_test schema.
-- Positional bind parameters, in order:
-- 1. JSON array of approved test-roster V1 user IDs; do not use the entire award roster.
-- 2. Reviewed collection_code. 3. Reviewed competition_series_id.
-- Returns technical IDs/state/booleans only; no names, login identifiers, phones or credentials.
WITH approved_tests AS (
    SELECT DISTINCT roster.user_id
    FROM JSON_TABLE(?, '$[*]' COLUMNS (
        user_id BIGINT PATH '$' ERROR ON EMPTY ERROR ON ERROR
    )) AS roster
    WHERE roster.user_id > 0
)
SELECT CAST(m.user_id AS CHAR) AS v1_user_id,
       CAST(t.id AS CHAR) AS collection_team_id,
       t.collection_code,
       t.competition_series_id,
       t.team_code,
       t.award_level,
       t.status AS collection_status,
       t.version AS collection_version,
       CASE WHEN t.status = 'AVAILABLE' THEN 1 ELSE 0 END AS can_start,
       CASE WHEN t.status = 'IN_PROGRESS' AND t.holder_user_id = m.user_id
            THEN 1 ELSE 0 END AS held_by_candidate,
       CASE WHEN t.status = 'AVAILABLE' THEN 'AVAILABLE_FOR_EXPLICIT_START'
            WHEN t.status = 'IN_PROGRESS' AND t.holder_user_id = m.user_id THEN 'RESUME_OWN_ATTEMPT'
            WHEN t.status = 'IN_PROGRESS' THEN 'HELD_BY_ANOTHER_MEMBER'
            WHEN t.status = 'USER_CONFIRMED_COMPLETE' THEN 'ALREADY_MANUALLY_CONFIRMED'
            ELSE 'UNKNOWN_STATE' END AS candidate_state,
       COUNT(*) OVER (PARTITION BY t.id) AS approved_test_members_in_team,
       CASE WHEN a.auth_id IS NOT NULL THEN 1 ELSE 0 END AS has_latest_valid_identity_record,
       CASE WHEN NULLIF(TRIM(a.id_card), '') IS NOT NULL THEN 1 ELSE 0 END AS latest_valid_identity_has_identifier
FROM approved_tests approved
JOIN sys_user u ON u.user_id = approved.user_id AND u.del_flag = '0' AND u.status = '0'
JOIN v1_team_collection_member m ON m.user_id = u.user_id AND m.enabled = 1
JOIN v1_team_collection t ON t.id = m.team_id AND t.enabled = 1
LEFT JOIN auth_info a ON a.auth_id = (
    SELECT latest.auth_id
    FROM auth_info latest
    WHERE latest.user_id = m.user_id
      AND latest.auth_status = '5'
      AND COALESCE(latest.del_flag, '0') = '0'
    ORDER BY latest.auth_time DESC, latest.auth_id DESC
    LIMIT 1
)
WHERE t.collection_code = ?
  AND t.competition_series_id = ?
ORDER BY CASE WHEN t.status = 'AVAILABLE' THEN 0
              WHEN t.status = 'IN_PROGRESS' AND t.holder_user_id = m.user_id THEN 1
              WHEN t.status = 'IN_PROGRESS' THEN 2 ELSE 3 END,
         latest_valid_identity_has_identifier DESC,
         approved_test_members_in_team DESC,
         t.id, m.user_id;
