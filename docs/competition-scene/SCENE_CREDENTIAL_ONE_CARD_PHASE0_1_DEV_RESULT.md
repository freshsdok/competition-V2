# 现场证件一证多权阶段 0 + 阶段 1 开发结果

完成时间：2026-07-05

本轮基于 `SCENE_CREDENTIAL_ONE_CARD_BACKEND_PILOT_WORK_PLAN.md` 进行编码前修订，并完成阶段 0 接口契约确认、阶段 1 grant 表和 grant 基础服务开发。

本轮未接入旧发证、旧扫码、PC、小程序、资源预约主流程。

## 1. 契约审计结果

已新增阶段 0 契约审计文档：

```text
docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE0_CONTRACT_AUDIT.md
```

审计结论：

1. 当前 PC 和小程序前端页面保持不动。
2. 当前扫码 scan 响应结构保持不动。
3. 当前 confirm 响应结构保持不动。
4. 当前我的证件接口响应结构保持不动。
5. 一证多权后续接入旧接口时，必须通过后端适配层继续返回旧字段。
6. SCHEDULE 动作必须携带 `scheduleId`。
7. 资料代领后续仍通过 `delegateQrContent` 识别代领人核心证件。
8. MATERIAL 状态仍写 `COMPETITION + USER`，不写 `TEAM + MATERIAL`。
9. operation_log 后续需要能追踪 grant 来源。

## 2. 工作计划修订

已修订：

```text
docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_BACKEND_PILOT_WORK_PLAN.md
```

补充并固化以下规则：

1. 核心证件唯一规则：

```text
competition_series_id + subject_type + subject_code + credential_type
```

禁止仅按 `competitionSeriesId + userId` 复用核心证件。

2. grant 幂等规则：

```text
credential_id
+ scope_type = SCHEDULE
+ scope_ref_id = scheduleId
+ source_target_id
+ grant_status = ACTIVE
```

3. 严格审计后，第一阶段已改为通过 `active_grant_key` 唯一约束兜住 ACTIVE grant 并发重复插入风险。
4. 没有 `currentScheduleId` 时，只返回大赛级动作。
5. 候场、赛场入场、资源预约等动作必须指定 `scheduleId`。
6. 阶段 1 暂不修改 operation_log 表，但预留 `grant_id/grant_scope_type/grant_scope_ref_id`。
7. 资料代领纳入一证多权旁路扫码矩阵。
8. grant 服务预留 `checkScheduleAbility(credentialId, scheduleId, abilityCode)`。

## 3. migration 文件

已新增：

```text
db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql
```

本 migration 只新增 grant 表，不迁移历史测试数据，不修改旧表，不接入旧业务路径。

## 4. 新增表结构

新增表：

```text
competition_scene_credential_scope_grant
```

字段：

1. `grant_id`
2. `credential_id`
3. `competition_series_id`
4. `scope_type`
5. `scope_ref_id`
6. `source_type`
7. `source_schedule_id`
8. `source_target_id`
9. `credential_type`
10. `role_code`
11. `subject_type`
12. `subject_code`
13. `ability_json`
14. `valid_from`
15. `valid_to`
16. `operation_window_json`
17. `grant_status`
18. `grant_snapshot_json`
19. `create_by`
20. `create_time`
21. `update_by`
22. `update_time`
23. `deleted`

索引：

1. `idx_grant_credential`
2. `idx_grant_competition_scope`
3. `idx_grant_source_schedule_target`
4. `idx_grant_subject`
5. `idx_grant_status`

唯一约束：

1. `uk_grant_active_key`

说明：`ACTIVE 且 deleted=0` 的 grant 写入 `active_grant_key`，撤销后置空。

## 5. 新增文件清单

文档：

1. `docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE0_CONTRACT_AUDIT.md`
2. `docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE0_1_DEV_RESULT.md`

数据库：

1. `db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql`

后端：

1. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneCredentialScopeGrant.java`
2. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialScopeGrantMapper.java`
3. `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialScopeGrantMapper.xml`
4. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneCredentialScopeGrantService.java`
5. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImpl.java`

## 6. grant service 方法

已实现：

1. `insertGrant`
2. `findActiveGrantsByCredential`
3. `findActiveScheduleGrant`
4. `ensureScheduleGrant`
5. `revokeGrant`
6. `revokeGrantsByTarget`
7. `hasAbility`
8. `checkScheduleAbility`
9. `buildDefaultScheduleGrantAbility`
10. `buildDefaultOperationWindowJson`

补充能力：

1. `ensureScheduleGrant` 内部会按 SCHEDULE grant 幂等规则查询。
2. 已存在 ACTIVE grant 时刷新 `ability_json`、`operation_window_json`、`grant_snapshot_json` 等可变字段。
3. 不存在 ACTIVE grant 时新增。
4. `grant_snapshot_json` 会递归清理手机号、身份证号、token、二维码内容和证件文件 URL 等敏感字段。
5. `checkScheduleAbility` 会校验 ACTIVE grant、授权时间窗口和能力码。

## 7. 幂等策略

阶段 1 幂等查询条件：

```text
credential_id
+ scope_type = SCHEDULE
+ scope_ref_id = scheduleId
+ source_target_id
+ grant_status = ACTIVE
+ deleted = 0
```

Service 层处理方式：

1. `ensureScheduleGrant` 使用事务。
2. 查询已存在授权时使用 `for update`。
3. 已存在则刷新可变字段并返回现有 grant。
4. 不存在则插入新 grant。
5. 数据库通过 `uk_grant_active_key` 防止多实例并发重复插入 ACTIVE grant。

说明：`SELECT FOR UPDATE` 在记录不存在时不能单独防止重复插入，因此严格审计后已增加 `active_grant_key` 唯一约束。Service 层事务幂等仍保留，用于正常路径下的读后刷新。

## 8. 测试结果

已完成：

1. Mapper XML 格式校验通过：

```text
xmllint --noout old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialScopeGrantMapper.xml
```

2. 后端编译通过：

```text
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

已补充完成：

1. 本地测试库 grant DML smoke test 已通过。
2. 测试使用本地 `dev-mysql57 / jiaoxue_test`，未连接生产库。
3. 测试数据在事务内执行并回滚，未保留测试 grant 数据。

本轮没有执行旧接口联调，因为阶段 1 明确不接入旧业务路径。

## 9. 构建结果

构建命令：

```text
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

编译范围：

1. `teaching-common`
2. `teaching-api-system`
3. `teaching-common-redis`
4. `teaching-common-security`
5. `teaching-common-datasource`
6. `teaching-common-datascope`
7. `teaching-common-log`
8. `teaching-common-swagger`
9. `teaching-modules-competition`

构建中仅出现既有 Maven model warning、编译 warning 和 Lombok warning，未出现本轮新增代码导致的编译错误。

## 10. 是否可以进入阶段 2 旁路发证

结论：grant 表和基础服务可靠性闭环已完成，但本轮不进入阶段 2。是否进入阶段 2 以 `SCENE_CREDENTIAL_ONE_CARD_PHASE1_STRICT_AUDIT_RESULT.md` 的准入结论为准。

进入阶段 2 前建议确认：

1. 在云端测试库执行 grant migration。
2. 验证 grant 插入、查询、幂等 ensure、撤销。
3. 明确旁路发证接口是否单独暴露测试 endpoint。
4. 明确核心证件生成复用条件严格使用：

```text
competition_series_id + subject_type + subject_code + credential_type
```

5. 明确旁路发证不生成 SCHEDULE 实体证件，只生成或复用核心证件并写入 grant。
