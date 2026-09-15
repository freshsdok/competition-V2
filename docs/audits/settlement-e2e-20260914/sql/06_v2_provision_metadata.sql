-- V2 runtime database. Source-checked only; this file has NOT been executed by its author.
-- Execute each SELECT separately with prepared-statement bindings. Every statement ends in ';'.
-- No credentials, private profile fields, fingerprints, raw JSON payloads, or row locks are read.

-- 1. Bind: reviewed recipient tenant_code. Match this ID to the real UI/API directory selection.
SELECT id AS tenant_id, tenant_code, lifecycle_state, version AS tenant_version
FROM tenant
WHERE tenant_code = ?;

-- 2. Bind: reviewed owner subject_id. These three version identifiers are not interchangeable.
SELECT c.subject_id,
       c.current_profile_version_id,
       c.version AS profile_current_version,
       p.profile_subject_id AS version_owner_subject_id,
       p.version_no AS profile_version_no,
       p.settlement_case_id,
       c.created_at,
       c.updated_at
FROM settlement_profile_current c
LEFT JOIN payout_profile_version p ON p.id = c.current_profile_version_id
WHERE c.subject_id = ?;

-- 3. Bind: reviewed owner subject_id. Capture ALL access metadata for this owner before/after
-- each operation, so a different recipient's state is not concealed by the selected-unit filter.
-- No row means no relation; it is not a fictitious REVOKED/version=-1 database row.
SELECT a.subject_id, a.tenant_id, t.tenant_code,
       a.status, a.version AS access_version,
       a.created_at, a.updated_at, a.created_by, a.updated_by
FROM settlement_profile_tenant_access a
JOIN tenant t ON t.id = a.tenant_id
WHERE a.subject_id = ?
ORDER BY a.tenant_id;

-- 4. Optional audit correlation. Bind: owner subject_id, reviewed tenant_id, previous audit ID.
-- Use 0 for a bounded initial snapshot; record the greatest matching ID before the next action.
-- Metadata only. profile_version_id_at_action is the profile row ID, NOT current/access version.
-- Audit trace_id is generated separately inside the domain service; it need not equal HTTP traceId.
SELECT id, scope_type, tenant_id,
       actor_account_id, actor_subject_id, actor_identity_id,
       action_code, outcome, risk_level,
       target_domain, target_kind, target_reference,
       JSON_UNQUOTE(JSON_EXTRACT(safe_summary_json, '$.profileVersionId'))
         AS profile_version_id_at_action,
       trace_id, occurred_at, recorded_at
FROM platform_audit_entry
WHERE actor_subject_id = ?
  AND tenant_id = ?
  AND id > ?
  AND scope_type = 'TENANT'
  AND target_domain = 'COMMERCE_FINANCE'
  AND target_kind = 'SETTLEMENT_PROFILE'
  AND target_reference = CAST(actor_subject_id AS CHAR)
  AND action_code IN ('SETTLEMENT_PROFILE_PROVIDED', 'SETTLEMENT_PROFILE_REVOKED')
ORDER BY id ASC
LIMIT 100;
