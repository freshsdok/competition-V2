-- V1 verified runtime database (the previous evidence used jiaoxue_test).
-- Source-checked only; not executed by this file's author.
-- Execute SELECTs separately with driver-bound values, not SQL string interpolation.
-- No student/handler names, identity values, credentials, or row locks are selected.

-- 1. Bind: reviewed team_id. Record before provision, after revoke/re-provide, and before/after
-- the final manual click. Confirmation retains the holder; release is a different transition.
SELECT id AS team_id, enabled, status, holder_user_id,
       version AS team_version, last_actor_user_id, last_action,
       started_at, confirmed_at, updated_at
FROM v1_team_collection
WHERE id = ?;

-- 2. Bind: reviewed team_id, previous team event ID (0 for the bounded initial snapshot).
-- Actual completion action is USER_CONFIRM; the resulting team status is USER_CONFIRMED_COMPLETE.
-- An exact retry must not create another event with the same team/version.
SELECT id AS event_id, team_id, actor_user_id, action,
       version AS resulting_team_version, created_at
FROM v1_team_collection_event
WHERE team_id = ?
  AND id > ?
ORDER BY id ASC
LIMIT 100;
