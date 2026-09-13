-- V1 data-asset aggregate profile (MySQL 8.x)
-- Scope: jiaoxue_test. SELECT-only. No PII values are returned.
-- Run against an approved read-only snapshot account for authoritative evidence.

SELECT NOW() AS audit_time,
       @@read_only AS read_only,
       @@super_read_only AS super_read_only;

SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'jiaoxue_test';

SELECT COUNT(*) AS column_count
FROM information_schema.columns
WHERE table_schema = 'jiaoxue_test';

SELECT COUNT(*) AS foreign_key_count
FROM information_schema.referential_constraints
WHERE constraint_schema = 'jiaoxue_test';

-- Account and identity profile.
SELECT COUNT(*) AS users,
       SUM(del_flag = '0') AS explicitly_active_users,
       COUNT(DISTINCT user_name) AS distinct_user_names,
       COUNT(DISTINCT NULLIF(TRIM(phonenumber), '')) AS distinct_phones,
       SUM(phonenumber IS NULL OR TRIM(phonenumber) = '') AS blank_phones
FROM sys_user;

SELECT COUNT(*) AS duplicate_phone_groups,
       COALESCE(SUM(row_count), 0) AS rows_in_duplicate_phone_groups
FROM (
    SELECT TRIM(phonenumber) AS normalized_phone, COUNT(*) AS row_count
    FROM sys_user
    WHERE phonenumber IS NOT NULL AND TRIM(phonenumber) <> ''
    GROUP BY TRIM(phonenumber)
    HAVING COUNT(*) > 1
) d;

SELECT COUNT(*) AS duplicate_active_phone_groups,
       COALESCE(SUM(row_count), 0) AS active_rows_in_duplicate_phone_groups
FROM (
    SELECT TRIM(phonenumber) AS normalized_phone, COUNT(*) AS row_count
    FROM sys_user
    WHERE del_flag = '0'
      AND phonenumber IS NOT NULL
      AND TRIM(phonenumber) <> ''
    GROUP BY TRIM(phonenumber)
    HAVING COUNT(*) > 1
) d;

SELECT COUNT(*) AS auth_rows,
       COUNT(DISTINCT user_id) AS auth_users,
       COUNT(DISTINCT NULLIF(TRIM(id_card), '')) AS distinct_legal_identifiers,
       SUM(del_flag = '0') AS explicitly_active_auth_rows,
       SUM(del_flag IS NULL) AS legacy_unknown_delete_rows
FROM auth_info;

SELECT COUNT(*) AS repeated_identifier_groups,
       COALESCE(SUM(row_count), 0) AS rows_in_repeated_identifier_groups
FROM (
    SELECT TRIM(id_card) AS normalized_identifier, COUNT(*) AS row_count
    FROM auth_info
    WHERE id_card IS NOT NULL AND TRIM(id_card) <> ''
    GROUP BY TRIM(id_card)
    HAVING COUNT(*) > 1
) d;

SELECT COUNT(*) AS cross_user_identifier_groups,
       COALESCE(SUM(user_count), 0) AS user_links_in_groups
FROM (
    SELECT TRIM(id_card) AS normalized_identifier,
           COUNT(DISTINCT user_id) AS user_count
    FROM auth_info
    WHERE (del_flag <> '1' OR del_flag IS NULL)
      AND id_card IS NOT NULL
      AND TRIM(id_card) <> ''
    GROUP BY TRIM(id_card)
    HAVING COUNT(DISTINCT user_id) > 1
) d;

SELECT certification_type, check_status, del_flag, COUNT(*) AS row_count
FROM identity_info
GROUP BY certification_type, check_status, del_flag
ORDER BY row_count DESC;

-- Registration case and member profile.
SELECT COUNT(*) AS application_rows,
       SUM(del_flag = '0') AS active_rows,
       SUM(del_flag = '2') AS withdrawn_rows,
       SUM(user_id IS NULL) AS rows_without_user,
       COUNT(DISTINCT competition_series_id) AS series_count
FROM competition_apply_info;

SELECT COUNT(DISTINCT CASE
           WHEN del_flag IN ('0', '2')
            AND team_code IS NOT NULL
            AND TRIM(team_code) <> ''
           THEN team_code
       END) AS active_or_withdrawn_registration_cases,
       SUM(del_flag IN ('0', '2')) AS active_or_withdrawn_member_snapshots
FROM competition_apply_info;

SELECT COUNT(*) AS true_duplicate_groups,
       COALESCE(SUM(row_count), 0) AS rows_in_groups
FROM (
    SELECT team_code,
           id_card,
           COALESCE(competition_role_name, '') AS role_name,
           COUNT(*) AS row_count
    FROM competition_apply_info
    WHERE del_flag IN ('0', '2')
      AND team_code IS NOT NULL
      AND TRIM(team_code) <> ''
      AND id_card IS NOT NULL
      AND TRIM(id_card) <> ''
    GROUP BY team_code, id_card, COALESCE(competition_role_name, '')
    HAVING COUNT(*) > 1
) d;

SELECT COUNT(*) AS linked_user_member_snapshots,
       SUM(r.rela_id IS NOT NULL) AS matching_team_relation,
       SUM(r.rela_id IS NULL) AS missing_team_relation
FROM competition_apply_info a
LEFT JOIN team_member_rela r
  ON r.team_code = a.team_code
 AND r.user_id = a.user_id
 AND r.del_flag IN ('0', '2')
WHERE a.del_flag IN ('0', '2')
  AND a.team_code IS NOT NULL
  AND TRIM(a.team_code) <> ''
  AND a.user_id IS NOT NULL;

-- Files and evaluation semantics.
SELECT COUNT(*) AS manager_rows,
       SUM(file_info IS NOT NULL AND JSON_VALID(file_info)) AS valid_file_json,
       SUM(del_flag = '0') AS active_manager_rows
FROM file_upload_manager;

SELECT upload_operation_type, del_flag, COUNT(*) AS row_count
FROM file_upload_record
GROUP BY upload_operation_type, del_flag
ORDER BY row_count DESC;

SELECT COUNT(*) AS review_rows,
       SUM(reviewer_user_id IS NOT NULL) AS reviewer_user_rows,
       SUM(expert_id IS NOT NULL) AS expert_rows,
       SUM(total_score IS NOT NULL) AS score_rows,
       SUM(grade IS NOT NULL AND TRIM(grade) <> '') AS grade_rows,
       SUM(comment_text IS NOT NULL AND TRIM(comment_text) <> '') AS comment_rows,
       SUM(submitted_time IS NOT NULL) AS submitted_rows,
       SUM(review_status = 1) AS reviewed_marker_rows
FROM review_record;

-- Award and credential two-layer profile.
SELECT COUNT(*) AS award_rows,
       COUNT(DISTINCT NULLIF(TRIM(team_code), '')) AS distinct_award_teams
FROM award_details
WHERE del_flag = '0';

SELECT COUNT(*) AS history_rows,
       COUNT(DISTINCT NULLIF(TRIM(cert_code), '')) AS distinct_certificate_codes
FROM user_certificate_history
WHERE del_flag = '0';

SELECT COUNT(*) AS duplicate_certificate_code_groups,
       COALESCE(SUM(row_count), 0) AS rows_in_duplicate_code_groups
FROM (
    SELECT TRIM(cert_code) AS certificate_code, COUNT(*) AS row_count
    FROM user_certificate_history
    WHERE del_flag = '0'
      AND cert_code IS NOT NULL
      AND TRIM(cert_code) <> ''
    GROUP BY TRIM(cert_code)
    HAVING COUNT(*) > 1
) d;

SELECT COUNT(*) AS origin_rows,
       COUNT(DISTINCT cert_code) AS origin_distinct_codes,
       COUNT(DISTINCT CONCAT_WS('|', cert_code, CAST(user_id AS CHAR))) AS unique_code_user_bindings,
       SUM(user_id IS NULL) AS origin_rows_without_user
FROM user_certificate_origin
WHERE del_flag = '0';

SELECT COUNT(*) AS origin_bindings_matching_history
FROM user_certificate_origin o
JOIN user_certificate_history h ON h.cert_code = o.cert_code
WHERE o.del_flag = '0' AND h.del_flag = '0';

-- Commerce and invoice profile.
SELECT COUNT(*) AS orders,
       SUM(del_flag = '0') AS active_orders,
       COUNT(DISTINCT order_id) AS distinct_business_order_ids,
       SUM(del_flag = '0' AND pay_status = 'paid') AS active_paid_orders
FROM order_info;

SELECT pay_method,
       pay_mode,
       SUM(target_order_id IS NULL OR TRIM(target_order_id) = '') AS missing_provider_transaction,
       COUNT(*) AS paid_rows
FROM order_info
WHERE del_flag = '0' AND pay_status = 'paid'
GROUP BY pay_method, pay_mode
ORDER BY paid_rows DESC;

SELECT COUNT(*) AS active_order_line_rows,
       SUM(o.id IS NOT NULL) AS matched_internal_order_rows,
       SUM(o.id IS NULL) AS orphan_rows
FROM order_goods_relation g
LEFT JOIN order_info o ON CAST(o.id AS CHAR) = g.order_id
WHERE g.del_flag = '0';

SELECT COUNT(*) AS exact_active_line_duplicate_groups,
       COALESCE(SUM(row_count), 0) AS rows_in_groups
FROM (
    SELECT order_id,
           commodity_id,
           COALESCE(users, '') AS users_snapshot,
           COALESCE(change_type, '') AS change_type_value,
           COALESCE(pay_status, '') AS line_status,
           COUNT(*) AS row_count
    FROM order_goods_relation
    WHERE del_flag = '0'
    GROUP BY order_id, commodity_id, COALESCE(users, ''),
             COALESCE(change_type, ''), COALESCE(pay_status, '')
    HAVING COUNT(*) > 1
) d;

SELECT COUNT(*) AS invoices,
       SUM(del_flag = '0') AS active_invoices,
       SUM(del_flag = '0' AND c_url IS NOT NULL AND TRIM(c_url) <> '') AS active_pdf_urls
FROM invoice_info;

SELECT COUNT(*) AS active_invoice_business_order_matches
FROM invoice_info i
JOIN order_info o ON o.order_id = i.order_id
WHERE i.del_flag = '0';

-- The new invoice flow stores a frontend-generated application token in invoice_info.order_id.
-- This section proves the timeline/pattern and checks every plausible order identifier without
-- returning any identifier value. BINARY avoids source-column collation differences.
SELECT YEAR(i.create_time) AS invoice_year,
       COUNT(*) AS active_invoices,
       SUM(i.order_id REGEXP '^[0-9a-fA-F]{32}$') AS uuid_like_tokens,
       SUM(o.order_id IS NOT NULL) AS direct_business_order_matches
FROM invoice_info i
LEFT JOIN order_info o ON BINARY o.order_id = BINARY i.order_id
WHERE i.del_flag = '0'
GROUP BY YEAR(i.create_time)
ORDER BY invoice_year;

SELECT COUNT(*) AS active_invoices,
       COUNT(DISTINCT order_id) AS distinct_invoice_application_tokens,
       SUM(issued_status = '1') AS issued_success,
       SUM(issued_status = '2') AS issued_failed,
       SUM(c_url IS NOT NULL AND TRIM(c_url) <> '') AS pdf_urls,
       SUM(user_id IS NOT NULL) AS rows_with_owner,
       SUM(org_id IS NOT NULL) AS rows_with_org,
       SUM(amount) AS total_amount
FROM invoice_info
WHERE del_flag = '0';

SELECT 'order_info.order_id' AS candidate_column, COUNT(*) AS match_rows
FROM invoice_info i JOIN order_info o ON BINARY i.order_id = BINARY o.order_id
WHERE i.del_flag = '0'
UNION ALL
SELECT 'order_info.id', COUNT(*)
FROM invoice_info i JOIN order_info o ON i.order_id = CAST(o.id AS CHAR)
WHERE i.del_flag = '0'
UNION ALL
SELECT 'order_info.out_order_id', COUNT(*)
FROM invoice_info i JOIN order_info o ON BINARY i.order_id = BINARY o.out_order_id
WHERE i.del_flag = '0'
UNION ALL
SELECT 'order_info.cmb_order_id', COUNT(*)
FROM invoice_info i JOIN order_info o ON BINARY i.order_id = BINARY o.cmb_order_id
WHERE i.del_flag = '0'
UNION ALL
SELECT 'order_info.biz_order_id', COUNT(*)
FROM invoice_info i JOIN order_info o ON BINARY i.order_id = BINARY o.biz_order_id
WHERE i.del_flag = '0'
UNION ALL
SELECT 'order_info.target_order_id', COUNT(*)
FROM invoice_info i JOIN order_info o ON BINARY i.order_id = BINARY o.target_order_id
WHERE i.del_flag = '0'
UNION ALL
SELECT 'order_info.refund_order_id', COUNT(*)
FROM invoice_info i JOIN order_info o ON BINARY i.order_id = BINARY o.refund_order_id
WHERE i.del_flag = '0';

-- Schema evidence: invoice_info has no FK and no index on order_id.
SELECT column_name, column_type, is_nullable, column_default, column_key, column_comment
FROM information_schema.columns
WHERE table_schema = 'jiaoxue_test' AND table_name = 'invoice_info'
ORDER BY ordinal_position;

SELECT index_name, non_unique,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS indexed_columns
FROM information_schema.statistics
WHERE table_schema = 'jiaoxue_test' AND table_name = 'invoice_info'
GROUP BY index_name, non_unique
ORDER BY index_name;

SELECT constraint_name, column_name, referenced_table_name, referenced_column_name
FROM information_schema.key_column_usage
WHERE table_schema = 'jiaoxue_test'
  AND table_name = 'invoice_info'
  AND referenced_table_name IS NOT NULL;

-- Core reconciliation snapshot. Repeat before and after every migration wave.
SELECT (SELECT COUNT(*) FROM sys_user) AS sys_user,
       (SELECT COUNT(*) FROM auth_info) AS auth_info,
       (SELECT COUNT(*) FROM identity_info) AS identity_info,
       (SELECT COUNT(*) FROM competition_apply_info) AS applications,
       (SELECT COUNT(*) FROM team_manager_info) AS teams,
       (SELECT COUNT(*) FROM team_member_rela) AS team_members,
       (SELECT COUNT(*) FROM order_info) AS orders,
       (SELECT COUNT(*) FROM order_goods_relation) AS order_lines,
       (SELECT COUNT(*) FROM invoice_info) AS invoices,
       (SELECT COUNT(*) FROM user_certificate_history) AS certificate_history,
       (SELECT COUNT(*) FROM user_certificate_origin) AS certificate_origin,
       (SELECT COUNT(*) FROM award_details) AS awards,
       (SELECT COUNT(*) FROM file_upload_manager) AS file_manager,
       (SELECT COUNT(*) FROM file_upload_record) AS file_records,
       (SELECT COUNT(*) FROM review_record) AS review_records;
