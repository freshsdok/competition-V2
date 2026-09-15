-- READ ONLY. Two bind parameters: approved V2 subject_id, then private identifier from 02.
-- Do not log the identifier binding. Returns counts/status only, never fingerprints or identifier values.
-- Only V1_HISTORICAL_IMPORT can be compared here; other providers require their existing service adapter.
WITH input AS (
    SELECT CAST(? AS UNSIGNED) AS subject_id, CAST(? AS CHAR) AS identifier
), evidence AS (
    SELECT n.provider_code, n.natural_identity_type, n.key_version,
           CASE WHEN n.provider_code = 'V1_HISTORICAL_IMPORT'
                  AND n.natural_identity_type = 'NATIONAL_ID' AND n.key_version = 1
                  AND LENGTH(REPLACE(REPLACE(REPLACE(REPLACE(input.identifier,
                      ' ', ''), CHAR(13), ''), CHAR(10), ''), CHAR(9), '')) > 0
                  AND n.document_fingerprint = UNHEX(SHA2(CONCAT('NATIONAL_ID', CHAR(0),
                      REPLACE(REPLACE(REPLACE(REPLACE(input.identifier,
                      ' ', ''), CHAR(13), ''), CHAR(10), ''), CHAR(9), '')), 256))
                THEN 1 ELSE 0 END AS historical_match
    FROM input
    JOIN subject_natural_identity_verification n ON n.subject_id = input.subject_id
    WHERE n.status = 'VERIFIED'
      AND (n.valid_until IS NULL OR n.valid_until > UTC_TIMESTAMP(6))
)
SELECT COUNT(*) AS active_verified_evidence_count,
       COALESCE(SUM(provider_code = 'V1_HISTORICAL_IMPORT'), 0) AS historical_evidence_count,
       COALESCE(SUM(historical_match), 0) AS historical_match_count,
       CASE WHEN COUNT(*) = 0 THEN 'NO_ACTIVE_VERIFIED_EVIDENCE'
            WHEN SUM(provider_code <> 'V1_HISTORICAL_IMPORT') > 0 THEN 'SERVICE_ADAPTER_CHECK_REQUIRED'
            WHEN SUM(historical_match) = COUNT(*) THEN 'HISTORICAL_IDENTIFIER_COMPATIBLE'
            ELSE 'HISTORICAL_IDENTIFIER_MISMATCH' END AS compatibility_state
FROM evidence;
