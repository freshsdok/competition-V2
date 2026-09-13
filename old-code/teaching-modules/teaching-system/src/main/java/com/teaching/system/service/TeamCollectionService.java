package com.teaching.system.service;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.integration.embedded.V2InternalEmbeddedBridgeClient;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

/** V1办理名额与人工确认。没有银行字段，不声称能撤销已经签发的V2会话。 */
@Service
public class TeamCollectionService {
    private final JdbcTemplate jdbc;
    private final ObjectProvider<V2InternalEmbeddedBridgeClient> bridges;
    private final boolean enabled;

    public TeamCollectionService(JdbcTemplate jdbc,
            ObjectProvider<V2InternalEmbeddedBridgeClient> bridges,
            @Value("${v1.team-collection.enabled:false}") boolean enabled) {
        this.jdbc = jdbc;
        this.bridges = bridges;
        this.enabled = enabled;
    }

    public record View(String id, String collectionCode, String teamCode, String teamName,
            String awardLevel, String status, String handlerName, String startedAt,
            String confirmedAt, long version, boolean heldByMe, boolean canStart) {}

    private record Row(long id, String collectionCode, String teamCode, String teamName,
            String awardLevel, String status, Long holder, String handlerName,
            LocalDateTime startedAt, LocalDateTime confirmedAt, long version,
            Long lastActor, String lastAction) {}

    public List<View> mine(long userId) {
        requireUser(userId);
        if (!enabled) return List.of();
        return jdbc.query("""
                SELECT t.* FROM v1_team_collection t
                JOIN v1_team_collection_member m ON m.team_id=t.id AND m.user_id=? AND m.enabled=1
                JOIN sys_user u ON u.user_id=m.user_id AND u.del_flag='0' AND u.status='0'
                WHERE t.enabled=1 ORDER BY t.collection_code,t.team_code
                """, (r, n) -> view(row(r), userId), userId);
    }

    @Transactional
    public View start(long id, long userId, long expected) {
        Row row = authorizedLock(id, userId, expected);
        if (retry(row, userId, expected, "START") && "IN_PROGRESS".equals(row.status)) return view(row, userId);
        if (row.version != expected || !"AVAILABLE".equals(row.status)) throw conflict();
        String name = jdbc.queryForObject("SELECT student_name FROM v1_team_collection_member WHERE team_id=? AND user_id=? AND enabled=1", String.class, id, userId);
        int changed = jdbc.update("""
                UPDATE v1_team_collection SET status='IN_PROGRESS',holder_user_id=?,handler_name=?,
                started_at=UTC_TIMESTAMP(6),confirmed_at=NULL,version=version+1,
                last_actor_user_id=?,last_action='START',updated_at=UTC_TIMESTAMP(6)
                WHERE id=? AND enabled=1 AND status='AVAILABLE' AND version=?
                """, userId, name, userId, id, expected);
        if (changed != 1) throw conflict();
        event(id, userId, "START", expected + 1);
        return view(lock(id), userId);
    }

    @Transactional
    public View confirm(long id, long userId, long expected) {
        return finish(id, userId, expected, true);
    }

    @Transactional
    public View release(long id, long userId, long expected) {
        return finish(id, userId, expected, false);
    }

    private View finish(long id, long userId, long expected, boolean confirm) {
        Row row = authorizedLock(id, userId, expected);
        String action = confirm ? "USER_CONFIRM" : "RELEASE";
        if (retry(row, userId, expected, action)) return view(row, userId);
        requireHolder(row, userId, expected);
        int changed = confirm
                ? jdbc.update("""
                    UPDATE v1_team_collection SET status='USER_CONFIRMED_COMPLETE',confirmed_at=UTC_TIMESTAMP(6),
                    version=version+1,last_actor_user_id=?,last_action='USER_CONFIRM',updated_at=UTC_TIMESTAMP(6)
                    WHERE id=? AND status='IN_PROGRESS' AND holder_user_id=? AND version=?
                    """, userId, id, userId, expected)
                : jdbc.update("""
                    UPDATE v1_team_collection SET status='AVAILABLE',holder_user_id=NULL,handler_name=NULL,
                    started_at=NULL,confirmed_at=NULL,version=version+1,last_actor_user_id=?,
                    last_action='RELEASE',updated_at=UTC_TIMESTAMP(6)
                    WHERE id=? AND status='IN_PROGRESS' AND holder_user_id=? AND version=?
                    """, userId, id, userId, expected);
        if (changed != 1) throw conflict();
        event(id, userId, action, expected + 1);
        return view(lock(id), userId);
    }

    /** 签发时持有队伍行锁，避免校验后被释放又给旧办理人签发新入口。远端失败不自动释放名额。 */
    @Transactional
    public V2InternalEmbeddedBridgeClient.Entry entry(long id, long userId, long expected) {
        Row row = authorizedLock(id, userId, expected);
        requireHolder(row, userId, expected);
        var bridge = bridges.getIfAvailable();
        if (bridge == null) throw new ServiceException("新版安全办理入口暂未开放，办理名额已保留，可稍后重试或主动放弃", 503);
        try {
            return bridge.settlement(userId);
        } catch (RestClientException | IllegalStateException unavailable) {
            throw new ServiceException("安全入口暂不可用，办理名额已保留，请重试或主动放弃", 503);
        }
    }

    private Row authorizedLock(long id, long userId, long expected) {
        requireUser(userId);
        if (!enabled) throw new ServiceException("收款资料办理暂未开放", 503);
        if (id <= 0 || expected < 0 || expected == Long.MAX_VALUE) throw denied();
        Row row = lock(id);
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM v1_team_collection_member m
                JOIN sys_user u ON u.user_id=m.user_id AND u.del_flag='0' AND u.status='0'
                WHERE m.team_id=? AND m.user_id=? AND m.enabled=1
                """, Integer.class, id, userId);
        if (count == null || count != 1) throw denied();
        return row;
    }

    private Row lock(long id) {
        return jdbc.query("SELECT * FROM v1_team_collection WHERE id=? AND enabled=1 FOR UPDATE",
                (r, n) -> row(r), id).stream().findFirst().orElseThrow(TeamCollectionService::denied);
    }

    private static Row row(ResultSet r) throws SQLException {
        return new Row(r.getLong("id"), r.getString("collection_code"), r.getString("team_code"),
                r.getString("team_name"), r.getString("award_level"), r.getString("status"),
                r.getObject("holder_user_id", Long.class), r.getString("handler_name"),
                r.getObject("started_at", LocalDateTime.class), r.getObject("confirmed_at", LocalDateTime.class),
                r.getLong("version"), r.getObject("last_actor_user_id", Long.class), r.getString("last_action"));
    }

    private static View view(Row r, long userId) {
        return new View(Long.toString(r.id), r.collectionCode, r.teamCode, r.teamName, r.awardLevel,
                r.status, r.handlerName, utc(r.startedAt), utc(r.confirmedAt), r.version,
                Objects.equals(r.holder, userId) && "IN_PROGRESS".equals(r.status), "AVAILABLE".equals(r.status));
    }

    private static String utc(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private static boolean retry(Row row, long userId, long expected, String action) {
        return row.version == expected + 1 && Objects.equals(row.lastActor, userId) && action.equals(row.lastAction);
    }

    private static void requireHolder(Row row, long userId, long expected) {
        if (row.version != expected || !"IN_PROGRESS".equals(row.status) || !Objects.equals(row.holder, userId)) throw conflict();
    }

    private void event(long id, long actor, String action, long version) {
        jdbc.update("INSERT INTO v1_team_collection_event(team_id,actor_user_id,action,version,created_at) VALUES(?,?,?,?,UTC_TIMESTAMP(6))",
                id, actor, action, version);
    }

    private static void requireUser(long userId) {
        if (userId <= 0) throw new ServiceException("请先登录原平台", 401);
    }

    private static ServiceException denied() { return new ServiceException("您不在该队伍的学生填报名单内，或办理已暂停", 403); }
    private static ServiceException conflict() { return new ServiceException("本队办理状态已变化，请刷新查看；仅当前办理人可确认或放弃", 409); }
}
