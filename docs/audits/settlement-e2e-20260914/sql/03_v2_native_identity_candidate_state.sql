-- READ ONLY. MySQL 8.0. Select the independently verified V2 runtime schema first.
-- One bind parameter: JSON array of candidate V1 user IDs returned by 01.
-- Mirrors JdbcV1MigrationBootstrap source/lifecycle conditions without FOR UPDATE or mutation.
-- READY_SOURCE_SNAPSHOT is not a login result, SMS delivery result, or browser acceptance.
WITH requested AS (
    SELECT DISTINCT roster.user_id
    FROM JSON_TABLE(?, '$[*]' COLUMNS (
        user_id BIGINT PATH '$' ERROR ON EMPTY ERROR ON ERROR
    )) AS roster
    WHERE roster.user_id > 0
), provenance AS (
    SELECT requested.user_id,
           COUNT(p.account_id) AS provenance_count,
           MAX(p.account_id) AS account_id,
           MAX(p.subject_id) AS subject_id,
           MAX(p.resolution_outcome = 'AMBIGUOUS_SEPARATE_REVIEW') AS ambiguous
    FROM requested
    LEFT JOIN account_creation_provenance p
      ON p.creation_source = 'IMPORT_ACTIVATION'
     AND p.evidence_owner = 'V1_MIGRATION'
     AND p.evidence_reference = CONCAT('sys_user:', requested.user_id)
    GROUP BY requested.user_id
), checks AS (
    SELECT p.*,
           EXISTS(SELECT 1 FROM subject s WHERE s.id = p.subject_id) AS subject_exists,
           (SELECT COUNT(*) FROM account_subject_relation r
             WHERE r.subject_id = p.subject_id) AS relation_count,
           (SELECT COUNT(*) FROM account_subject_relation r JOIN account a ON a.id = r.account_id
             WHERE r.subject_id = p.subject_id AND a.status = 'ACTIVE'
               AND a.id = p.account_id) AS matching_active_relation_count,
           (SELECT COUNT(*) FROM platform_imported_account_claim c
             WHERE c.account_id = p.account_id AND c.status <> 'CLAIMED') AS unresolved_claim_count,
           (SELECT COUNT(*) FROM same_person_resolution_candidate d
             WHERE (d.left_subject_id = p.subject_id OR d.right_subject_id = p.subject_id)
               AND d.review_status IN ('PENDING', 'CONFIRMED_CANDIDATE')) AS open_dispute_count,
           (SELECT COUNT(*) FROM subject_natural_identity_verification n
             WHERE n.subject_id = p.subject_id AND n.status = 'VERIFIED'
               AND (n.valid_until IS NULL OR n.valid_until > UTC_TIMESTAMP(6))) AS active_identity_evidence_count,
           (SELECT COUNT(*) FROM authentication_authenticator f
             WHERE f.account_id = p.account_id AND f.status = 'ACTIVE'
               AND f.authenticator_type = 'PHONE_OTP') AS active_phone_factor_count,
           (SELECT COUNT(*) FROM authentication_authenticator f
             WHERE f.account_id = p.account_id AND f.status = 'ACTIVE'
               AND f.authenticator_type = 'PASSWORD') AS active_password_factor_count
    FROM provenance p
)
SELECT CAST(user_id AS CHAR) AS v1_user_id,
       CAST(account_id AS CHAR) AS v2_account_id,
       CAST(subject_id AS CHAR) AS v2_subject_id,
       CASE WHEN provenance_count = 0 THEN 'MISSING'
            WHEN provenance_count <> 1 OR ambiguous = 1 OR subject_exists <> 1
              OR relation_count <> 1 OR matching_active_relation_count <> 1
              OR unresolved_claim_count <> 0 OR open_dispute_count <> 0 THEN 'HOLD'
            ELSE 'READY_SOURCE_SNAPSHOT' END AS native_identity_candidate_state,
       provenance_count, relation_count, matching_active_relation_count,
       unresolved_claim_count, open_dispute_count, active_identity_evidence_count,
       active_phone_factor_count, active_password_factor_count
FROM checks
ORDER BY user_id;
