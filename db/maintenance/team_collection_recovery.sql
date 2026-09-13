-- 仅供有数据库维护权限的管理员核实后手工执行；不是普通队员接口，也不会自动执行。
-- 设置 @recovery_database=DATABASE(), @recovery_team_id, @recovery_expected_version,
-- @recovery_actor_user_id（实际操作管理员账号）, @recovery_action='ADMIN_RELEASE'或'ADMIN_REOPEN'。
-- ADMIN_RELEASE仅用于IN_PROGRESS；ADMIN_REOPEN仅用于误确认。两者都不能撤销旧V2窗口或删除V2资料。
-- 应在外部工单记录原因，并通知原办理人关闭所有旧填报窗口。
DELIMITER $$
DROP PROCEDURE IF EXISTS recover_v1_team_collection$$
CREATE PROCEDURE recover_v1_team_collection()
BEGIN
  DECLARE changed INT;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
  IF @recovery_database IS NULL OR BINARY @recovery_database<>BINARY DATABASE()
     OR @recovery_team_id IS NULL OR @recovery_expected_version IS NULL OR @recovery_expected_version<0
     OR @recovery_actor_user_id IS NULL OR @recovery_actor_user_id<=0
     OR @recovery_action IS NULL OR @recovery_action NOT IN ('ADMIN_RELEASE','ADMIN_REOPEN') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Explicit reviewed recovery parameters required';
  END IF;
  START TRANSACTION;
  UPDATE v1_team_collection SET status='AVAILABLE',holder_user_id=NULL,handler_name=NULL,
    started_at=NULL,confirmed_at=NULL,version=version+1,last_actor_user_id=@recovery_actor_user_id,
    last_action=@recovery_action,updated_at=UTC_TIMESTAMP(6)
    WHERE id=@recovery_team_id AND version=@recovery_expected_version
    AND ((@recovery_action='ADMIN_RELEASE' AND status='IN_PROGRESS')
      OR (@recovery_action='ADMIN_REOPEN' AND status='USER_CONFIRMED_COMPLETE'));
  SET changed=ROW_COUNT();
  IF changed<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='State/version changed; re-read before recovery'; END IF;
  INSERT INTO v1_team_collection_event(team_id,actor_user_id,action,version,created_at)
    VALUES(@recovery_team_id,@recovery_actor_user_id,@recovery_action,@recovery_expected_version+1,UTC_TIMESTAMP(6));
  COMMIT;
END$$
DELIMITER ;
CALL recover_v1_team_collection();
DROP PROCEDURE recover_v1_team_collection;
