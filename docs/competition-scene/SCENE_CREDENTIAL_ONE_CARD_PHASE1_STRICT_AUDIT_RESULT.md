# 现场证件一证多权阶段 1 严格审计结果

审计时间：2026-07-05

本轮只审计并修订阶段 0 + 阶段 1 的 grant 表和 grant 基础服务可靠性闭环。未进入阶段 2，未接入旧发证、旧扫码、PC、小程序或资源预约主流程，未连接生产数据库。

## 1. migration 修订结果

修订文件：

```text
db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql
```

已完成修订：

1. `create_time` 改为：

```sql
datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
```

2. `update_time` 改为：

```sql
datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

3. `source_type` 改为 `NOT NULL DEFAULT 'MANUAL'`。
4. 来源枚举在注释和 Service 校验中明确为：
   - `SCHEDULE_TARGET`
   - `MANUAL`
   - `IMPORT`
   - `COMPETITION_DIRECT`
5. 增加幂等查询索引：

```sql
idx_grant_active_lookup(
  credential_id,
  scope_type,
  scope_ref_id,
  source_target_id,
  grant_status,
  deleted
)
```

6. 增加 `active_grant_key varchar(255)`。
7. 增加唯一索引：

```sql
uk_grant_active_key(active_grant_key)
```

8. `scope_ref_id` 保持 `bigint`，并在字段注释中明确：当前约定所有 `scope_ref_id` 均为内部数值 ID，`SCHEDULE` 为 `schedule_id`。

结论：migration 已满足阶段 1 严格审计要求。

## 2. Mapper 审计结果

审计文件：

```text
old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialScopeGrantMapper.xml
```

结果：

1. 所有 select 查询均带 `deleted = 0`。
2. `selectActiveGrantsByCredential` 只返回 `grant_status='ACTIVE' and deleted=0`。
3. `selectActiveScheduleGrant` 同时限制：
   - `credential_id`
   - `scope_type='SCHEDULE'`
   - `scope_ref_id`
   - `source_target_id` 或 `source_target_id is null`
   - `grant_status='ACTIVE'`
   - `deleted=0`
4. `selectActiveScheduleGrantForUpdate` 使用相同条件，并添加 `for update`。
5. `revokeGrant` 只按指定 `grant_id` 撤销，不会误撤销其他 grant。
6. `revokeGrantsByTarget` 已收紧为：
   - `source_type='SCHEDULE_TARGET'`
   - `source_schedule_id`
   - `source_target_id`
   - `grant_status='ACTIVE'`
   - `deleted=0`
7. 所有 update 均更新 `update_time`。
8. 撤销时同步将 `active_grant_key` 置空。
9. SQL 未使用 `${}` 拼接，均使用 `#{}` 参数绑定。
10. `ability_json`、`grant_snapshot_json` 均通过参数绑定写入，不做字符串拼接。

XML 校验：

```text
xmllint --noout old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialScopeGrantMapper.xml
```

结果：通过。

## 3. Service 算法审计结果

审计文件：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImpl.java
```

已修订内容：

1. `ensureScheduleGrant` 保留 `@Transactional`。
2. `ensureScheduleGrant` 已存在 ACTIVE grant 时幂等返回。
3. 已存在时刷新：
   - `ability_json`
   - `operation_window_json`
   - `grant_snapshot_json`
   - `update_by`
   - `update_time`
4. 不存在时插入前补齐：
   - `competition_series_id`
   - `credential_id`
   - `scope_type`
   - `scope_ref_id`
   - `source_type`
   - `source_schedule_id`
   - `source_target_id`
   - `credential_type`
   - `subject_type`
   - `subject_code`
   - `grant_status`
   - `create_time`
   - `update_time`
   - `active_grant_key`
5. `source_type` 使用白名单校验。
6. `active_grant_key` 由 Service 统一生成，调用方不能手动决定。
7. `DuplicateKeyException` 会回查已存在 grant，避免并发重复插入直接失败。
8. `revokeGrant` 和 `revokeGrantsByTarget` 幂等，不物理删除。

## 4. ability_json 解析测试

新增测试文件：

```text
old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImplTest.java
```

测试命令：

```text
mvn -pl teaching-modules/teaching-competition -Dtest=CompetitionSceneCredentialScopeGrantServiceImplTest test
```

结果：

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

覆盖结论：

1. `ability_json` 为空时返回 false。
2. JSON 非法时返回 false。
3. `abilityCode` 为空时返回 false。
4. `abilityCode` 不在白名单时返回 false。
5. 字段缺失时返回 false。
6. 字段为 `false` 时返回 false。
7. 字段为字符串 `"true"` 或数字 `1` 时返回 false。
8. 只有 boolean `true` 返回 true。

## 5. valid_from / valid_to 时间测试

单元测试覆盖：

1. `valid_from=null` 表示不限制开始。
2. `valid_to=null` 表示不限制结束。
3. `now < valid_from` 返回 false。
4. `now > valid_to` 返回 false。
5. `valid_from <= now <= valid_to` 返回 true。
6. `grant_status != ACTIVE` 返回 false。
7. `deleted != 0` 返回 false。

结论：`checkScheduleAbility` 时间边界和状态边界已按保守规则实现。

## 6. snapshot 白名单测试

快照策略已从“序列化后清理敏感字段”改为“白名单字段收集”。

白名单字段：

1. `scheduleId`
2. `scheduleName`
3. `targetId`
4. `targetName`
5. `roleCode`
6. `credentialType`
7. `teamCode`
8. `subjectType`
9. `subjectCode`
10. `groupCode`
11. `groupName`

单元测试确认不会保留：

1. `phone`
2. `email`
3. `idCardHash`
4. `credentialToken`
5. `qrContent`

结论：grant snapshot 不再依赖黑名单清理，已切换为白名单构建。

## 7. DML smoke test 结果

测试库：

```text
本地 dev-mysql57 / jiaoxue_test
```

说明：未连接生产数据库。测试数据使用高位测试 ID，在事务内执行并 `ROLLBACK`，未保留测试 grant 数据。

执行结果：

| 测试项 | 结果 |
| --- | --- |
| 插入一条 ACTIVE SCHEDULE grant | 通过 |
| 按 credentialId 查询 active grants | 通过 |
| 按 credentialId + scheduleId 查询 active schedule grant | 通过 |
| ensure 第一次插入 | 通过 |
| ensure 第二次不新增 | 通过 |
| 已存在 grant 时刷新 ability_json | 通过 |
| revokeGrant 后 active 查询不再返回 | 通过 |
| revoke 后再次 ensure 能创建新的 ACTIVE grant | 通过 |
| 同一 credential 不同 scheduleId 可创建多条 ACTIVE grant | 通过 |
| 同一 credential 同一 scheduleId + source_target_id 不重复创建 ACTIVE grant | 通过 |
| 不同 source_target_id 可分别授权 | 通过 |
| 测试数据回滚后无残留 | 通过 |

关键输出：

```text
T1_insert_active_schedule_grant 1
T2_query_active_by_credential 1
T3_query_active_schedule_grant 1
T4_ensure_first_insert_count 1
T5_ensure_second_no_new_count 1
T6_refresh_ability_json {"waiting":false}
T7_revoke_active_not_returned 0
T7b_revoke_idempotent_active_not_returned 0
T8_reensure_after_revoke_active_count 1
T8b_reensure_after_revoke_total_rows 2
T9_same_credential_different_schedule_active_count 2
T10_same_schedule_same_target_not_duplicate 1
T11_different_source_target_separate_active 2
```

回滚验证：

```text
credential_id in (900001, 900002) count = 0
```

## 8. 并发风险结论

审计结论：

1. 仅靠 `SELECT FOR UPDATE` 在“记录不存在”时不能防止并发重复插入。
2. 本轮已补充 `active_grant_key` 唯一约束作为阶段 2 前的并发保护方案。
3. `ACTIVE 且 deleted=0` 的 grant 写入：

```text
credential_id:scope_type:scope_ref_id:source_target_id
```

4. `REVOKED` 或非正常授权 `active_grant_key = NULL`。
5. MySQL 唯一索引允许多个 NULL，因此历史撤销记录不会阻塞重新授权。
6. `ensureScheduleGrant` 捕获 `DuplicateKeyException` 后回查已有 grant。

结论：阶段 1 并发重复插入风险已有数据库唯一键兜底。后续如果进入阶段 2 真实接口接入，还可以视压测结果补 Redis 锁或业务层 DB 锁，但不再是阶段 2 的阻塞项。

## 9. 修复文件清单

修订文件：

1. `docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_BACKEND_PILOT_WORK_PLAN.md`
2. `docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE0_1_DEV_RESULT.md`
3. `db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql`
4. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneCredentialScopeGrant.java`
5. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialScopeGrantMapper.java`
6. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneCredentialScopeGrantService.java`
7. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImpl.java`
8. `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialScopeGrantMapper.xml`

新增文件：

1. `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImplTest.java`
2. `docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE1_STRICT_AUDIT_RESULT.md`

## 10. 后端 compile 结果

最终构建命令：

```text
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

说明：构建中仍有既有 Maven model warning、编译 warning 和 Lombok warning，不是本轮新增代码导致。

## 11. 是否允许进入阶段 2 旁路发证

阶段 2 准入标准检查：

| 条件 | 结果 |
| --- | --- |
| migration 已修订 | 通过 |
| DML smoke test 通过 | 通过 |
| hasAbility 保守解析通过 | 通过 |
| checkScheduleAbility 时间边界通过 | 通过 |
| snapshot 白名单确认 | 通过 |
| revoke 幂等通过 | 通过 |
| grant 幂等通过 | 通过 |
| 并发重复插入风险已有明确解决方案 | 通过，使用 `active_grant_key` 唯一约束 |
| 后端 compile 通过 | 通过 |

结论：阶段 2 旁路发证的准入条件已满足。

但本轮严格遵守禁止事项：不进入阶段 2，不接入旧发证，不接入旧扫码，不修改 PC，不修改小程序，不接入资源预约。

