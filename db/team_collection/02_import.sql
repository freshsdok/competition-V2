-- 唯一名单匹配/发布入口：每次都重新匹配，自动纳入用户已确认的3个账号。
-- 先运行建表脚本及01_source.sql（已完成可跳过），再在当前连接设置：
-- SET @reviewed_database='jiaoxue_test'; SET @reviewed_series_id=81;
-- SET @publish_team_collection=0; -- 0仅预览；审阅通过后改为1，并重新执行本文件全文。
-- 不更新原报名/成员/实名表，不修改V2。重复发布不重置办理状态，也不重新启用已撤销名单。
-- 本脚本仅适用于固定源文件的520队/1216个学生姓名位置；完整操作步骤见README.md。
DELIMITER $$
DROP PROCEDURE IF EXISTS import_v1_team_collection$$
CREATE PROCEDURE import_v1_team_collection()
import_body: BEGIN
  DECLARE valid_count INT;
  DECLARE invalid_count INT;
  DECLARE source_count INT;
  DECLARE team_count INT;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
  IF @reviewed_database IS NULL OR BINARY @reviewed_database<>BINARY DATABASE()
     OR NOT (@reviewed_series_id <=> 81)
     OR @publish_team_collection IS NULL OR @publish_team_collection NOT IN (0,1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Set reviewed database, series 81, and publish mode 0 or 1';
  END IF;
  SET @collection_code = 'XINKE_13_2026';
  SET @source_sha256 = '967559dfddf2be3b70ac513b9e0074835ddbccec441fe29e626bf98a2740e9ec';

  DROP TEMPORARY TABLE IF EXISTS tc_candidates;
  CREATE TEMPORARY TABLE tc_candidates AS
  SELECT DISTINCT s.collection_code,s.source_sha256,s.source_sheet,s.source_row,s.source_column,
         s.team_code,s.student_name,s.award_level,t.team_id AS source_team_id,t.team_name,t.competition_series_id,a.user_id
  FROM v1_team_collection_source s
  JOIN team_manager_info t ON BINARY t.team_code=BINARY s.team_code AND t.del_flag='0' AND t.check_status='4'
  JOIN competition_apply_info a ON BINARY a.team_code=BINARY t.team_code
    AND a.competition_series_id=t.competition_series_id AND a.del_flag='0'
    AND (a.check_status='4' OR (t.competition_series_id=81 AND a.check_status IS NULL))
    AND BINARY TRIM(a.user_name)=BINARY s.student_name
    AND a.competition_role_name IN ('队长','队员','学生','选手','参赛选手','成员')
  JOIN team_member_rela m ON BINARY m.team_code=BINARY t.team_code AND m.user_id=a.user_id
    AND m.del_flag='0'
    AND (m.check_status='2' OR (t.competition_series_id=81 AND m.check_status IS NULL))
  JOIN sys_user u ON u.user_id=a.user_id AND u.del_flag='0' AND u.status='0'
  WHERE s.collection_code=@collection_code AND s.source_sha256=@source_sha256;

  -- 仅本次固定来源、81届次、审核通过队伍兼容历史报名/成员NULL状态；其他明确状态仍拒绝。
  -- 空角色、缺少账号、缺少正式成员关系都不会自动纳入；以下仅补充用户已逐人确认的3个账号。
  DROP TEMPORARY TABLE IF EXISTS tc_matches;
  CREATE TEMPORARY TABLE tc_matches AS
  SELECT s.*,COUNT(DISTINCT c.user_id) AS candidate_users,COUNT(DISTINCT c.source_team_id) AS candidate_teams,
         MIN(c.user_id) AS user_id,MIN(c.team_name) AS team_name,MIN(c.competition_series_id) AS competition_series_id
  FROM v1_team_collection_source s LEFT JOIN tc_candidates c
   ON c.collection_code=s.collection_code AND c.source_sha256=s.source_sha256
   AND c.source_sheet=s.source_sheet AND c.source_row=s.source_row AND c.source_column=s.source_column
  WHERE s.collection_code=@collection_code AND s.source_sha256=@source_sha256
  GROUP BY s.collection_code,s.source_sha256,s.source_sheet,s.source_row,s.source_column,s.team_code,s.student_name,s.award_level;

  DROP TEMPORARY TABLE IF EXISTS tc_reviewed_accounts;
  CREATE TEMPORARY TABLE tc_reviewed_accounts (
    source_sheet VARCHAR(80),source_row INT,source_column VARCHAR(10),
    team_code VARCHAR(100),student_name VARCHAR(100),reviewed_user_id BIGINT,
    PRIMARY KEY(source_sheet,source_row,source_column)
  ) DEFAULT CHARSET=utf8mb4;
  INSERT INTO tc_reviewed_accounts VALUES
    ('赛道一名单',265,'E','2026_2068284992587718656','方柏辉',44349),
    ('赛道一名单',354,'F','2026_2066544693663641600','徐启航',50719),
    ('赛道二名单',21,'H','2026_2066784759798194187','张清',44874);

  -- 不只按指定ID筛选实名：先核对所有符合实名条件的正常账号，确保仍唯一且等于已确认ID。
  DROP TEMPORARY TABLE IF EXISTS tc_reviewed_valid;
  CREATE TEMPORARY TABLE tc_reviewed_valid AS
  SELECT r.source_sheet,r.source_row,r.source_column,r.team_code,r.student_name,
         MIN(u.user_id) AS user_id,MIN(t.team_name) AS team_name,
         MIN(t.competition_series_id) AS competition_series_id
  FROM tc_reviewed_accounts r
  JOIN v1_team_collection_source s ON s.collection_code=@collection_code AND s.source_sha256=@source_sha256
    AND BINARY s.source_sheet=BINARY r.source_sheet AND s.source_row=r.source_row
    AND BINARY s.source_column=BINARY r.source_column AND BINARY s.team_code=BINARY r.team_code
    AND BINARY s.student_name=BINARY r.student_name
  JOIN team_manager_info t ON BINARY t.team_code=BINARY s.team_code
    AND t.competition_series_id=81 AND t.del_flag='0' AND t.check_status='4'
  JOIN competition_apply_info a ON BINARY a.team_code=BINARY s.team_code
    AND a.competition_series_id=t.competition_series_id AND BINARY TRIM(a.user_name)=BINARY s.student_name
    AND a.del_flag='0' AND (a.check_status='4' OR a.check_status IS NULL)
    AND a.competition_role_name IN ('队长','队员','学生','选手','参赛选手','成员')
    AND (a.user_id IS NULL OR a.user_id=r.reviewed_user_id)
  JOIN team_member_rela m ON BINARY m.team_code=BINARY s.team_code
    AND BINARY TRIM(m.user_name)=BINARY s.student_name AND m.del_flag='0'
    AND (m.check_status='2' OR m.check_status IS NULL)
    AND m.team_role IN ('队长','队员','学生','选手','参赛选手','成员')
    AND (m.user_id IS NULL OR m.user_id=r.reviewed_user_id)
  JOIN auth_info au ON NULLIF(TRIM(a.id_card),'') IS NOT NULL
    AND BINARY TRIM(au.id_card)=BINARY TRIM(a.id_card)
    AND BINARY au.id_card_type=BINARY a.id_card_type
    AND BINARY TRIM(au.real_name)=BINARY s.student_name AND au.del_flag='0' AND au.auth_status='5'
  JOIN sys_user u ON u.user_id=au.user_id AND u.del_flag='0' AND u.status='0'
  GROUP BY r.source_sheet,r.source_row,r.source_column,r.team_code,r.student_name,r.reviewed_user_id
  HAVING COUNT(DISTINCT u.user_id)=1 AND MIN(u.user_id)=r.reviewed_user_id AND COUNT(DISTINCT t.team_id)=1;
  SELECT COUNT(*) INTO valid_count FROM tc_reviewed_valid;
  IF valid_count<>3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Reviewed accounts no longer match source/identity/member/account checks';
  END IF;
  SELECT COUNT(*) INTO valid_count FROM tc_matches c JOIN tc_reviewed_valid r
    ON BINARY c.source_sheet=BINARY r.source_sheet AND c.source_row=r.source_row
    AND BINARY c.source_column=BINARY r.source_column AND BINARY c.team_code=BINARY r.team_code
    AND BINARY c.student_name=BINARY r.student_name
    WHERE c.collection_code=@collection_code AND c.source_sha256=@source_sha256
      AND ((c.candidate_users=0 AND c.candidate_teams=0 AND c.user_id IS NULL)
        OR (c.candidate_users=1 AND c.candidate_teams=1 AND c.user_id=r.user_id AND c.competition_series_id=81));
  IF valid_count<>3 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Preview is missing or conflicts with reviewed accounts; investigate source mapping';
  END IF;
  UPDATE tc_matches c JOIN tc_reviewed_valid r
    ON BINARY c.source_sheet=BINARY r.source_sheet AND c.source_row=r.source_row
    AND BINARY c.source_column=BINARY r.source_column AND BINARY c.team_code=BINARY r.team_code
    AND BINARY c.student_name=BINARY r.student_name
  SET c.user_id=r.user_id,c.team_name=r.team_name,c.competition_series_id=r.competition_series_id,
      c.candidate_users=1,c.candidate_teams=1
  WHERE c.collection_code=@collection_code AND c.source_sha256=@source_sha256;

  SELECT DATABASE() AS target_database,COUNT(*) AS source_student_cells,COUNT(DISTINCT team_code) AS source_teams,
    SUM(candidate_users=1 AND candidate_teams=1) AS uniquely_matched_cells,
    SUM(candidate_users<>1 OR candidate_teams<>1) AS unresolved_cells FROM tc_matches;
  SELECT source_sheet,source_row,source_column,team_code,student_name,candidate_users,candidate_teams
    FROM tc_matches WHERE candidate_users<>1 OR candidate_teams<>1 ORDER BY source_sheet,source_row,source_column;
  SELECT team_code,user_id,COUNT(*) AS duplicate_slots FROM tc_matches
    WHERE candidate_users=1 AND candidate_teams=1 GROUP BY team_code,user_id HAVING COUNT(*)>1;
  SELECT * FROM tc_reviewed_valid ORDER BY source_sheet,source_row,source_column;
  SELECT COUNT(*),COUNT(DISTINCT team_code),SUM(candidate_users<>1 OR candidate_teams<>1 OR competition_series_id<>@reviewed_series_id)
    INTO source_count,team_count,invalid_count FROM tc_matches;
  IF source_count<>1216 OR team_count<>520 OR invalid_count<>0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Incomplete, ambiguous or wrong-series student mapping';
  END IF;
  SELECT COUNT(*) INTO invalid_count FROM (SELECT team_code,user_id FROM tc_matches GROUP BY team_code,user_id HAVING COUNT(*)>1) d;
  IF invalid_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate student slots per team'; END IF;
  IF @publish_team_collection=0 THEN
    SELECT 'PREVIEW_OK' AS result,'No roster permissions were published' AS detail;
    LEAVE import_body;
  END IF;
  START TRANSACTION;
  -- 发布采用同一事务，不改已有team status/holder/version；不把撤销过的名单重新开启。
  INSERT INTO v1_team_collection(collection_code,competition_series_id,team_code,team_name,award_level,source_sha256,enabled,created_at,updated_at)
    SELECT collection_code,MIN(competition_series_id),team_code,MIN(team_name),MIN(award_level),MIN(source_sha256),1,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)
    FROM tc_matches GROUP BY collection_code,team_code
    ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id);
  SELECT COUNT(*) INTO invalid_count FROM v1_team_collection t JOIN tc_matches m
    ON t.collection_code=m.collection_code AND t.team_code=m.team_code
    WHERE t.source_sha256<>m.source_sha256 OR t.competition_series_id<>m.competition_series_id OR t.enabled<>1;
  IF invalid_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existing cohort differs or was disabled; explicit reconciliation required'; END IF;
  -- 已有发布成员必须与本次源名单完全一致；不向已运行的名单静默追加/替换人员。
  SELECT COUNT(*) INTO invalid_count FROM v1_team_collection_member e JOIN v1_team_collection t ON t.id=e.team_id
    LEFT JOIN tc_matches m ON m.collection_code=t.collection_code AND m.team_code=t.team_code AND m.user_id=e.user_id
    WHERE t.collection_code=@collection_code AND (m.user_id IS NULL OR e.source_sha256<>m.source_sha256 OR BINARY e.student_name<>BINARY m.student_name OR e.enabled<>1);
  IF invalid_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existing members differ or were revoked'; END IF;
  SELECT COUNT(*) INTO invalid_count FROM v1_team_collection_member e JOIN v1_team_collection t ON t.id=e.team_id WHERE t.collection_code=@collection_code;
  IF invalid_count<>0 AND invalid_count<>1216 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existing cohort is partial; explicit reconciliation required'; END IF;
  SELECT COUNT(*) INTO invalid_count FROM v1_team_collection WHERE collection_code=@collection_code;
  IF invalid_count<>520 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existing cohort has additional teams'; END IF;
  INSERT INTO v1_team_collection_member(team_id,user_id,student_name,source_sha256,source_sheet,source_row,source_column)
    SELECT t.id,m.user_id,m.student_name,m.source_sha256,m.source_sheet,m.source_row,m.source_column
    FROM tc_matches m JOIN v1_team_collection t ON t.collection_code=m.collection_code AND t.team_code=m.team_code
    ON DUPLICATE KEY UPDATE team_id=VALUES(team_id);
  COMMIT;
  SELECT 'PUBLISHED' AS result,t.collection_code,COUNT(DISTINCT t.id) AS teams,
    COUNT(*) AS member_rows,COUNT(DISTINCT m.user_id) AS distinct_accounts
    FROM v1_team_collection t JOIN v1_team_collection_member m ON m.team_id=t.id
    WHERE t.collection_code=@collection_code GROUP BY t.collection_code;
END$$
DELIMITER ;
CALL import_v1_team_collection();
DROP PROCEDURE import_v1_team_collection;
