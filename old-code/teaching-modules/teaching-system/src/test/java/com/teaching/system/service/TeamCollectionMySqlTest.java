package com.teaching.system.service;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.integration.embedded.V2InternalEmbeddedBridgeClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.junit.*;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;
import org.testcontainers.containers.MySQLContainer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 仅启动隔离MySQL容器；不会读取项目Nacos配置或连接实际业务数据库。 */
public class TeamCollectionMySqlTest {
    @ClassRule public static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36");
    private JdbcTemplate jdbc;
    private TransactionTemplate tx;
    private TeamCollectionService service;
    private V2InternalEmbeddedBridgeClient bridge;

    @Before public void setup() throws Exception {
        var dataSource = new DriverManagerDataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
        jdbc = new JdbcTemplate(dataSource);
        tx = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        runSql(Files.readString(root().resolve("db/migration/20260913_team_collection.sql")));
        jdbc.execute("CREATE TABLE IF NOT EXISTS sys_user(user_id BIGINT PRIMARY KEY,del_flag CHAR(1),status CHAR(1))");
        for (String table : List.of("v1_team_collection_event", "v1_team_collection_member", "v1_team_collection", "v1_team_collection_source", "sys_user")) jdbc.execute("DELETE FROM " + table);
        jdbc.update("INSERT INTO sys_user VALUES(101,'0','0'),(102,'0','0'),(103,'0','0'),(999,'0','0')");
        jdbc.update("INSERT INTO v1_team_collection(id,collection_code,competition_series_id,team_code,team_name,award_level,source_sha256,enabled) VALUES(1,'TEST',81,'TEAM_A','合成队伍A','一等奖',REPEAT('a',64),1),(2,'TEST',81,'TEAM_B','合成队伍B','二等奖',REPEAT('a',64),1)");
        jdbc.update("INSERT INTO v1_team_collection_member(team_id,user_id,student_name,source_sha256,source_sheet,source_row,source_column) VALUES(1,101,'测试学生甲',REPEAT('a',64),'合成名单',2,'E'),(1,102,'测试学生乙',REPEAT('a',64),'合成名单',2,'F'),(2,103,'测试学生丙',REPEAT('a',64),'合成名单',3,'E')");
        bridge = mock(V2InternalEmbeddedBridgeClient.class);
        service = new TeamCollectionService(jdbc, new StaticListableBeanFactory(Map.of("bridge", bridge)).getBeanProvider(V2InternalEmbeddedBridgeClient.class), true);
    }

    private <T> T transaction(Supplier<T> action) { return tx.execute(status -> action.get()); }

    @Test public void nativeAndLegacyEntrancesShareHolderVersionAndCompletion() {
        transaction(() -> service.start(1,101,0));
        transaction(() -> { service.assertNativeHolder(1,101,1); return null; });
        assertThrows(ServiceException.class, () -> transaction(() -> { service.assertNativeHolder(1,102,1); return null; }));
        transaction(() -> service.release(1,101,1));
        transaction(() -> service.start(1,102,2));
        assertThrows(ServiceException.class, () -> transaction(() -> { service.assertNativeHolder(1,101,1); return null; }));
        transaction(() -> { service.assertNativeHolder(1,102,3); return null; });
        transaction(() -> service.confirm(1,102,3));
        assertThrows(ServiceException.class, () -> transaction(() -> { service.assertNativeHolder(1,102,4); return null; }));
        verifyNoInteractions(bridge);
    }

    @Test public void twoMembersRacingOnlyOneAcquiresAndOtherCannotIssueEntry() throws Exception {
        var ready = new CountDownLatch(2);
        var go = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Long>> futures = List.of(101L, 102L).stream().map(user -> executor.submit(() -> {
                ready.countDown(); assertTrue(go.await(5, TimeUnit.SECONDS));
                try { transaction(() -> service.start(1, user, 0)); return user; }
                catch (ServiceException conflict) { assertEquals(Integer.valueOf(409), conflict.getCode()); return 0L; }
            })).toList();
            assertTrue(ready.await(5, TimeUnit.SECONDS)); go.countDown();
            long first = futures.get(0).get(10, TimeUnit.SECONDS), second = futures.get(1).get(10, TimeUnit.SECONDS);
            assertTrue((first == 0) != (second == 0));
            long winner = first + second, loser = winner == 101 ? 102 : 101;
            assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM v1_team_collection_event", Long.class).longValue());
            assertEquals(Integer.valueOf(409), assertThrows(ServiceException.class, () -> transaction(() -> service.entry(1, loser, 1))).getCode());
            verifyNoInteractions(bridge);
        } finally { executor.shutdownNow(); }
    }

    @Test public void oldCommandsCannotConfirmReleaseOrEnterNewAttemptIncludingSameUser() {
        transaction(() -> service.start(1, 101, 0));
        transaction(() -> service.release(1, 101, 1));
        transaction(() -> service.start(1, 101, 2));
        for (Supplier<?> operation : List.<Supplier<?>>of(() -> service.confirm(1,101,1), () -> service.release(1,101,1), () -> service.entry(1,101,1))) {
            assertEquals(Integer.valueOf(409), assertThrows(ServiceException.class, () -> transaction(operation::get)).getCode());
        }
        assertEquals(3, service.mine(101).get(0).version());
    }

    @Test public void confirmationIsIdempotentAndPublicViewHasOnlyManualStatus() {
        transaction(() -> service.start(1,101,0));
        var confirmed = transaction(() -> service.confirm(1,101,1));
        assertEquals(confirmed, transaction(() -> service.confirm(1,101,1)));
        var peerView = service.mine(102).get(0);
        assertEquals("USER_CONFIRMED_COMPLETE", peerView.status());
        assertEquals("测试学生甲", peerView.handlerName());
        assertNotNull(peerView.confirmedAt());
        assertTrue(peerView.confirmedAt().endsWith("Z"));
        assertFalse(peerView.heldByMe());
        assertFalse(peerView.canStart());
        assertThrows(ServiceException.class, () -> transaction(() -> service.start(1,102,2)));
        assertThrows(ServiceException.class, () -> transaction(() -> service.entry(1,101,2)));
        assertThrows(ServiceException.class, () -> transaction(() -> service.release(1,101,2)));
        assertEquals(2L, jdbc.queryForObject("SELECT COUNT(*) FROM v1_team_collection_event", Long.class).longValue());
        assertFalse(java.util.Arrays.stream(TeamCollectionService.View.class.getRecordComponents()).anyMatch(c -> c.getName().toLowerCase().matches(".*(bank|phone|identity|subject|userid).*")));
    }

    @Test public void releaseAllowsPeerAndRepeatedCommandsDoNotAddEvents() {
        var started = transaction(() -> service.start(1,101,0));
        assertEquals(started, transaction(() -> service.start(1,101,0)));
        var released = transaction(() -> service.release(1,101,1));
        assertEquals(released, transaction(() -> service.release(1,101,1)));
        var peer = transaction(() -> service.start(1,102,2));
        assertTrue(peer.heldByMe());
        assertEquals("测试学生乙", peer.handlerName());
        assertThrows(ServiceException.class, () -> transaction(() -> service.confirm(1,101,3)));
        assertEquals(3L, jdbc.queryForObject("SELECT COUNT(*) FROM v1_team_collection_event", Long.class).longValue());
    }

    @Test public void outsiderOtherTeamRevokedAndDisabledAccountsAreDenied() {
        for (long user : List.of(103L,999L)) {
            assertTrue(service.mine(user).stream().noneMatch(r -> r.id().equals("1")));
            assertEquals(Integer.valueOf(403), assertThrows(ServiceException.class, () -> transaction(() -> service.start(1,user,0))).getCode());
        }
        jdbc.update("UPDATE v1_team_collection_member SET enabled=0 WHERE user_id=101");
        assertTrue(service.mine(101).isEmpty());
        assertThrows(ServiceException.class, () -> transaction(() -> service.start(1,101,0)));
        jdbc.update("UPDATE sys_user SET status='1' WHERE user_id=102");
        assertTrue(service.mine(102).isEmpty());
        assertThrows(ServiceException.class, () -> transaction(() -> service.start(1,102,0)));
    }

    @Test public void bridgeFailureKeepsHolderAndSuccessfulEntryUsesOnlyHolderIdentity() {
        transaction(() -> service.start(1,101,0));
        when(bridge.settlement(101)).thenThrow(new RestClientException("private upstream details"));
        var error = assertThrows(ServiceException.class, () -> transaction(() -> service.entry(1,101,1)));
        assertEquals(Integer.valueOf(503), error.getCode());
        assertFalse(error.getMessage().contains("private"));
        assertEquals("IN_PROGRESS", service.mine(101).get(0).status());
        var result = new V2InternalEmbeddedBridgeClient.Entry("/v2-embedded/embedded/settlement-profile#bootstrap=" + "a".repeat(43), Instant.now().plusSeconds(180));
        doReturn(result).when(bridge).settlement(101);
        assertSame(result, transaction(() -> service.entry(1,101,1)));
        verify(bridge,times(2)).settlement(101);
    }

    @Test public void auditFailureRollsBackHolderChange() {
        jdbc.update("INSERT INTO v1_team_collection_event(team_id,actor_user_id,action,version) VALUES(1,999,'SYNTHETIC_CONFLICT',1)");
        assertThrows(org.springframework.dao.DataAccessException.class, () -> transaction(() -> service.start(1,101,0)));
        assertEquals("AVAILABLE", service.mine(101).get(0).status());
        assertEquals(0, service.mine(101).get(0).version());
    }

    @Test public void disabledFeatureDoesNotReadSchemaAndNeverRestoresUngatedEntry() {
        var empty = mock(JdbcTemplate.class);
        var disabled = new TeamCollectionService(empty, new StaticListableBeanFactory().getBeanProvider(V2InternalEmbeddedBridgeClient.class), false);
        assertTrue(disabled.mine(101).isEmpty());
        assertThrows(ServiceException.class, () -> disabled.start(1,101,0));
        assertThrows(ServiceException.class, () -> disabled.entry(1,101,0));
        assertThrows(ServiceException.class, () -> disabled.mine(0));
        verifyNoInteractions(empty);
    }

    @Test public void sourceImportPreviewPublishAndRecoveryAreGuardedAndRepeatable() throws Exception {
        // 真实Excel结构 + 全部合成账号映射，仅验证SQL流程，不是实际业务账号核对证据。
        try (var connection = java.sql.DriverManager.getConnection(MYSQL.getJdbcUrl(),MYSQL.getUsername(),MYSQL.getPassword())) {
            var fixture = new JdbcTemplate(new SingleConnectionDataSource(connection,true));
            executeScript(fixture, Files.readString(root().resolve("db/team_collection/01_source.sql")));
            executeScript(fixture, Files.readString(root().resolve("db/team_collection/01_source.sql")));
            assertEquals(1216L, fixture.queryForObject("SELECT COUNT(*) FROM v1_team_collection_source",Long.class).longValue());
            fixture.execute("CREATE TABLE team_manager_info(team_id BIGINT AUTO_INCREMENT PRIMARY KEY,team_code VARCHAR(100),team_name VARCHAR(255),competition_series_id BIGINT,del_flag CHAR(1),check_status CHAR(1))");
            fixture.execute("CREATE TABLE competition_apply_info(team_code VARCHAR(100),competition_series_id BIGINT,del_flag CHAR(1),check_status CHAR(1),user_name VARCHAR(100),competition_role_name VARCHAR(30),user_id BIGINT)");
            fixture.execute("CREATE TABLE team_member_rela(team_code VARCHAR(100),user_id BIGINT,del_flag CHAR(1),check_status CHAR(1))");
            fixture.execute("CREATE TEMPORARY TABLE fixture_students AS SELECT s.*,10000+ROW_NUMBER() OVER(ORDER BY source_sheet,source_row,source_column) AS synthetic_user_id FROM v1_team_collection_source s");
            fixture.update("INSERT INTO team_manager_info(team_code,team_name,competition_series_id,del_flag,check_status) SELECT DISTINCT team_code,'SYNTHETIC',81,'0','4' FROM fixture_students");
            fixture.update("INSERT INTO competition_apply_info SELECT team_code,81,'0','4',student_name,'队员',synthetic_user_id FROM fixture_students");
            fixture.update("INSERT INTO team_member_rela SELECT team_code,synthetic_user_id,'0','2' FROM fixture_students");
            fixture.update("INSERT INTO sys_user SELECT synthetic_user_id,'0','0' FROM fixture_students");
            // 用户明确确认的3个账号仅进入本次名单；不回写报名或成员关系。
            fixture.execute("ALTER TABLE competition_apply_info ADD id_card VARCHAR(100), ADD id_card_type VARCHAR(10)");
            fixture.execute("ALTER TABLE team_member_rela ADD user_name VARCHAR(100), ADD team_role VARCHAR(30)");
            fixture.execute("CREATE TABLE auth_info(user_id BIGINT,real_name VARCHAR(100),id_card VARCHAR(100),id_card_type VARCHAR(10),del_flag CHAR(1),auth_status CHAR(1))");
            fixture.update("UPDATE team_member_rela m JOIN fixture_students s ON m.team_code=s.team_code AND m.user_id=s.synthetic_user_id SET m.user_name=s.student_name,m.team_role='队员'");
            for (Object[] reviewed : new Object[][]{{"方柏辉",44349L},{"徐启航",50719L},{"张清",44874L}}) {
                fixture.update("INSERT INTO sys_user VALUES (?,'0','0')",reviewed[1]);
                fixture.update("INSERT INTO auth_info VALUES (?,?,?,'1','0','5')",reviewed[1],reviewed[0],"SYNTHETIC-ID-"+reviewed[1]);
                fixture.update("UPDATE competition_apply_info SET user_id=NULL,id_card=?,id_card_type='1' WHERE user_name=?","SYNTHETIC-ID-"+reviewed[1],reviewed[0]);
                fixture.update("UPDATE team_member_rela SET user_id=NULL WHERE user_name=?",reviewed[0]);
            }
            String migration=Files.readString(root().resolve("db/team_collection/02_import.sql"));
            assertThrows(org.springframework.dao.DataAccessException.class, () -> executeScript(fixture,migration));
            fixture.execute("SET @reviewed_database=DATABASE(),@reviewed_series_id=81,@publish_team_collection=0");
            executeScript(fixture,migration);
            assertEquals(1216L,fixture.queryForObject("SELECT COUNT(*) FROM tc_matches WHERE candidate_users=1 AND candidate_teams=1",Long.class).longValue());
            assertEquals(0L,fixture.queryForObject("SELECT COUNT(*) FROM v1_team_collection WHERE collection_code='XINKE_13_2026'",Long.class).longValue());
            // 本地历史NULL兼容；不同届次、显式拒绝、老师角色、停用账号或实名歧义仍禁止发布。
            fixture.update("UPDATE competition_apply_info SET check_status=NULL");
            fixture.update("UPDATE team_member_rela SET check_status=NULL");
            executeScript(fixture,migration);
            fixture.execute("SET @publish_team_collection=1,@reviewed_series_id=82");
            assertThrows(org.springframework.dao.DataAccessException.class, () -> executeScript(fixture,migration));
            fixture.execute("SET @reviewed_series_id=81");
            for (String mutation : new String[]{
                    "UPDATE competition_apply_info SET check_status='5' WHERE user_id=10001",
                    "UPDATE team_member_rela SET check_status='3' WHERE user_id=10001",
                    "UPDATE team_manager_info SET check_status='5' WHERE team_id=1",
                    "UPDATE competition_apply_info SET competition_role_name='指导教师' WHERE user_id=10001",
                    "UPDATE auth_info SET auth_status='6' WHERE user_id=44349",
                    "UPDATE sys_user SET status='1' WHERE user_id=44349",
                    "UPDATE team_member_rela SET team_role='指导教师' WHERE user_name='方柏辉'",
                    "INSERT INTO auth_info SELECT 10001,real_name,id_card,id_card_type,del_flag,auth_status FROM auth_info WHERE user_id=44349"}) {
                fixture.update(mutation);
                assertThrows(org.springframework.dao.DataAccessException.class, () -> executeScript(fixture,migration));
                assertEquals(0L,fixture.queryForObject("SELECT COUNT(*) FROM v1_team_collection WHERE collection_code='XINKE_13_2026'",Long.class).longValue());
                fixture.update("UPDATE competition_apply_info SET check_status=NULL,competition_role_name='队员'");
                fixture.update("UPDATE team_member_rela SET check_status=NULL,team_role='队员'");
                fixture.update("UPDATE team_manager_info SET check_status='4'");
                fixture.update("UPDATE auth_info SET auth_status='5'");
                fixture.update("UPDATE sys_user SET status='0' WHERE user_id=44349");
                fixture.update("DELETE FROM auth_info WHERE user_id=10001");
            }
            // 正式导入重建全部预览，不依赖上次连接的临时表。
            fixture.execute("DROP TEMPORARY TABLE IF EXISTS tc_matches");
            executeScript(fixture,migration);
            assertEquals(3L,fixture.queryForObject("SELECT COUNT(*) FROM competition_apply_info WHERE user_id IS NULL",Long.class).longValue());
            assertEquals(3L,fixture.queryForObject("SELECT COUNT(*) FROM team_member_rela WHERE user_id IS NULL",Long.class).longValue());
            assertEquals(520L, fixture.queryForObject("SELECT COUNT(*) FROM v1_team_collection WHERE collection_code='XINKE_13_2026'",Long.class).longValue());
            assertEquals(1216L, fixture.queryForObject("SELECT COUNT(*) FROM v1_team_collection_member m JOIN v1_team_collection t ON t.id=m.team_id WHERE t.collection_code='XINKE_13_2026'",Long.class).longValue());
            assertEquals(3L,fixture.queryForObject("SELECT COUNT(*) FROM v1_team_collection_member WHERE user_id IN (44349,50719,44874)",Long.class).longValue());
            long team=fixture.queryForObject("SELECT team_id FROM v1_team_collection_member WHERE user_id=10001",Long.class);
            transaction(() -> service.start(team,10001,0));
            transaction(() -> service.confirm(team,10001,1));
            executeScript(fixture,migration);
            assertEquals("USER_CONFIRMED_COMPLETE",fixture.queryForObject("SELECT status FROM v1_team_collection WHERE id=?",String.class,team));
            fixture.update("UPDATE v1_team_collection_member SET enabled=0 WHERE user_id=44349");
            assertThrows(org.springframework.dao.DataAccessException.class, () -> executeScript(fixture,migration));
            assertEquals(0,fixture.queryForObject("SELECT enabled FROM v1_team_collection_member WHERE user_id=44349",Integer.class).intValue());
            String recovery=Files.readString(root().resolve("db/maintenance/team_collection_recovery.sql"));
            assertThrows(org.springframework.dao.DataAccessException.class, () -> executeScript(fixture,recovery));
            fixture.execute("SET @recovery_database=DATABASE(),@recovery_team_id="+team+",@recovery_expected_version=1,@recovery_actor_user_id=999,@recovery_action='ADMIN_REOPEN'");
            assertThrows(org.springframework.dao.DataAccessException.class, () -> executeScript(fixture,recovery));
            fixture.execute("SET @recovery_expected_version=2");
            executeScript(fixture,recovery);
            assertEquals("AVAILABLE",fixture.queryForObject("SELECT status FROM v1_team_collection WHERE id=?",String.class,team));
            assertEquals(3L,fixture.queryForObject("SELECT version FROM v1_team_collection WHERE id=?",Long.class,team).longValue());
        }
    }

    private static Path root() {
        Path p = Path.of("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("db/migration/20260913_team_collection.sql"))) p=p.getParent();
        return java.util.Objects.requireNonNull(p);
    }

    private void runSql(String source) {
        executeScript(jdbc,source);
    }

    private static void executeScript(JdbcTemplate target, String source) {
        StringBuilder sql = new StringBuilder();
        String delimiter = ";";
        for (String line : source.lines().toList()) {
            if (line.trim().startsWith("--") || line.isBlank()) continue;
            if (line.trim().toUpperCase().startsWith("DELIMITER ")) { delimiter=line.trim().substring(10).trim(); continue; }
            sql.append(line).append('\n');
            if (line.trim().endsWith(delimiter)) {
                String statement=sql.toString().trim();
                target.execute(statement.substring(0,statement.length()-delimiter.length()));
                sql.setLength(0);
            }
        }
    }
}
