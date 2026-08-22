# 现场证件自动大赛级发证代码路径诊断

诊断时间：2026-07-04

本轮只做代码路径和数据表现诊断，不修改业务代码、不清理数据、不调整前端展示。

## 1. 结论摘要

当前代码中，“赛场发证”会先自动确保一张 `scope_type=COMPETITION` 的大赛级证件，再生成 `scope_type=SCHEDULE` 的赛场级证件。这不是历史脏数据，而是当前生成链路的显式逻辑。

自动生成的大赛级证件目前没有 `schedule_id`、`target_id`，数据库表也没有 `source_schedule_id`、`source_target_id`、`auto_created` 字段。因此删除匹配对象时，只按 `target_id` 删除的逻辑无法定位并删除这张自动大赛级证件。

## 2. ensureCompetitionCredentialForTarget() 调用链

### 2.1 管理端入口

管理端现场安排页面：

```text
old-code-admin/src/views/tournament/sceneSchedule/index.vue
handleGenerateTargets()
  -> generateSceneCredential({
       scheduleId,
       targetIds,
       regenerate: false
     })
```

管理端 API：

```text
old-code-admin/src/api/tournament/sceneSchedule.js
generateSceneCredential()
  -> POST /competition/sceneCredential/generate
```

后端 Controller：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java
POST /sceneCredential/generate
POST /competition/sceneCredential/generate
  -> competitionSceneCredentialService.generateCompetitionSceneCredential(req)
```

后端 Service：

```text
CompetitionSceneCredentialServiceImpl.generateCompetitionSceneCredential(req)
  1. 校验 scheduleId；
  2. 查询 schedule；
  3. 查询 selected target 或 schedule 下全部 target；
  4. 遍历 target；
  5. ensureCompetitionCredentialForTarget(schedule, target, competition);
  6. selectCompetitionSceneCredentialByTargetId(targetId);
  7. 不重生成且已有赛场证件则跳过；
  8. 重生成时按 target_id 撤销旧赛场证件；
  9. buildCredential(schedule, target, nextSequence);
 10. insertCredentialWithRetry(...);
```

关键点：`ensureCompetitionCredentialForTarget()` 在检查赛场级证件是否已存在之前执行。因此即使 `regenerate=false` 且该 target 已有赛场级证件，只要该用户没有可复用的大赛级证件，仍会先补一张大赛级证件。

## 3. 何时自动生成 COMPETITION 级证件

`ensureCompetitionCredentialForTarget(schedule, target, competition)` 的触发条件：

1. `schedule != null`；
2. `target != null`；
3. `target.userId != null`；
4. 通过 `selectExistingCompetitionCredential(...)` 没有找到同大赛下可复用的有效大赛级证件。

复用查找顺序：

1. 先按 `competitionSeriesId + userId` 查询最新有效 `scope_type=COMPETITION` 证件；
2. 未命中时，再按 `competitionSeriesId + subjectType + subjectCode` 查询最新有效 `scope_type=COMPETITION` 证件。

因此当前自动生成只覆盖有 `userId` 的 target。团队维度 target 若也带 `userId`，自动生成的大赛级证件仍按 `USER` 主体生成；`userId` 为空时不会自动生成。

## 4. 自动生成的大赛级证件字段

字段来源：`CompetitionSceneCredentialServiceImpl.buildCompetitionCredential(...)`。

| 字段 | 当前取值 |
| --- | --- |
| `issue_channel` | `SCHEDULE_MATCH` |
| `scope_type` | `COMPETITION` |
| `scope_ref_id` | `schedule.competitionSeriesId` |
| `schedule_id` | `NULL` |
| `target_id` | `NULL` |
| `source_schedule_id` | 当前表结构不存在该字段 |
| `source_target_id` | 当前表结构不存在该字段 |
| `auto_created` | 当前表结构不存在该字段 |
| `credential_type` | `resolveCredentialType(schedule, target)` |
| `credential_name` | `resolveCredentialName(credentialType)` |
| `ability_json` | `buildAbilityJson("COMPETITION", credentialType)` |
| `config_dimension` | 固定 `PERSON` |
| `subject_type` | 固定传入 `USER` |
| `subject_code` | `String.valueOf(target.userId)` |
| `user_id` | `target.userId` |
| `team_code/team_name/member_id/user_name/phone/school/group/role` | 从 target 复制 |
| `valid_from` | 大赛开始时间；没有则 schedule.reportStartTime；再没有则当前时间 |
| `valid_to` | 大赛结束时间；查不到大赛详情则为 `NULL` |
| `credential_snapshot_json` | 包含 `issueChannel/scopeType/competitionSeriesId/credentialType/subjectType/subjectCode/schedule/target` |

能力差异需要特别注意：`PARTICIPANT` 大赛级证件只开启 `report/material`，不会开启 `waiting/resourceReservation`；赛场级 `PARTICIPANT` 才开启 `waiting/resourceReservation`。

## 5. 赛场级证件生成字段

字段来源：`CompetitionSceneCredentialServiceImpl.buildCredential(...)`。

| 字段 | 当前取值 |
| --- | --- |
| `issue_channel` | `SCHEDULE_MATCH` |
| `scope_type` | `SCHEDULE` |
| `scope_ref_id` | `schedule.scheduleId` |
| `schedule_id` | `schedule.scheduleId` |
| `target_id` | `target.targetId` |
| `credential_type` | `resolveCredentialType(schedule, target)` |
| `credential_name` | `resolveCredentialName(credentialType)` |
| `ability_json` | `buildAbilityJson("SCHEDULE", credentialType)` |
| `config_dimension` | target.configDimension 优先，否则 schedule.configDimension |
| `subject_type` | EXPERT -> `EXPERT`；团队维度 -> `TEAM`；否则 `USER` |
| `subject_code` | TEAM 用 teamCode；否则 userId；再否则 `MEMBER:{memberId}` |
| `user_id/team/member/school/group/role` | 从 target/schedule 复制 |
| `report/contest/waiting/material/notice` | 从 schedule 复制，候场分组 target 优先 |
| `credential_snapshot_json` | `{"schedule": schedule, "target": target}` |
| `valid_from` | schedule.reportStartTime；没有则当前时间 |
| `valid_to` | schedule.contestEndTime；没有则 schedule.reportEndTime |

赛场级证件是当前现场候场、赛场级报到、赛场级资料领取动作的自然载体。

## 6. 删除 target 时实际删除哪些证件

管理端删除匹配对象：

```text
DELETE /competition/sceneSchedule/target/{targetIds}
  -> CompetitionSceneScheduleController.removeTarget(...)
  -> CompetitionSceneScheduleServiceImpl.deleteScheduleTargetByIds(targetIds)
```

当前删除逻辑：

```text
credentialMapper.deleteCompetitionSceneCredentialByTargetIds(targetIds, username);
targetMapper.deleteCompetitionSceneScheduleTargetByIds(targetIds, username);
```

Mapper SQL：

```sql
update competition_scene_credential
set del_flag = '1', update_by = #{updateBy}, update_time = now(), version = version + 1
where target_id in (...)
  and del_flag = '0'
```

实际结果：

1. 会删除 `target_id` 命中的赛场级证件；
2. 也会删除任何历史上带同一 `target_id` 的证件；
3. 不会删除自动生成的大赛级证件，因为自动大赛级证件的 `target_id` 为 `NULL`；
4. 不会按快照里的 `target.targetId` 删除，因为当前没有 SQL 解析 `credential_snapshot_json`；
5. 不会按 `source_target_id` 删除，因为当前表结构没有该字段。

重生成时也类似：`revokeCompetitionSceneCredentialByTargetId(targetId)` 只按 `target_id` 撤销有效证件，不影响自动大赛级证件。

## 7. 复用大赛级证件 SQL 条件

当前按 subject 复用：

```sql
select ...
from competition_scene_credential
where competition_series_id = #{competitionSeriesId}
  and scope_type = 'COMPETITION'
  and subject_type = #{subjectType}
  and subject_code = #{subjectCode}
  and credential_status = 'EFFECTIVE'
  and del_flag = '0'
order by credential_id desc
limit 1
```

当前按 userId 复用：

```sql
select ...
from competition_scene_credential
where competition_series_id = #{competitionSeriesId}
  and scope_type = 'COMPETITION'
  and user_id = #{userId}
  and credential_status = 'EFFECTIVE'
  and del_flag = '0'
order by credential_id desc
limit 1
```

没有参与过滤的字段：

1. `credential_type`；
2. `competition_role_name`；
3. `issue_channel`；
4. `subject_type`，在 userId 查询中没有过滤；
5. `subject_code`，在 userId 查询中没有过滤；
6. `credential_name`；
7. 是否自动生成。

风险：同一用户在同一大赛中既是参赛者，又是工作人员或专家时，可能复用到不符合当前 target 角色的最新有效大赛级证件。

## 8. PC 展示排序和主证件选择逻辑

PC 请求：

```text
old-code-pc/src/api/personal/index.js
GET /competition/userCompetition/sceneCredential/myList
```

后端：

```text
CompetitionSceneCredentialController.myList()
UserCompetitionSceneCredentialController.myList()
  -> selectMyCompetitionSceneCredentialList(userId)
```

`selectMyCompetitionSceneCredentialList(userId)` 会合并：

1. `user_id = 当前用户` 的所有未删除证件；
2. `subject_type=USER and subject_code=当前用户ID` 的所有未删除证件；
3. 当前用户报名团队 `team_code` 对应的所有未删除证件。

底层列表 SQL 排序：

```sql
order by contest_start_time asc, credential_id desc
```

PC 前端：

```text
Competition.vue
credentialGetList()
  -> credentialList
credentialCompetitionList
  -> 按 competitionSeriesId / competitionName / teamCode / credentialId 建组
currentCompetitionCredential
  -> currentCredentials.filter(isCompetitionLevelCredential)
  -> pickCompetitionCredential(...)
```

`isCompetitionLevelCredential` 判定：

```text
scopeType in ["COMPETITION", "STAFF", "VIP", "EXPERT", "TEMP"]
或无 scopeType 且无 scheduleId 且 issueChannel=COMPETITION_DIRECT
```

`pickCompetitionCredential` 选择：

```text
优先返回第一张 credentialStatus=EFFECTIVE 的大赛级证件；
否则返回第一张大赛级证件；
否则 null。
```

因此只要后端返回了大赛级证件和赛场级证件，PC 就会把大赛级证件展示为顶部主证件，把赛场级证件展示为下方赛场明细。

## 9. 小程序展示排序和主证件选择逻辑

小程序入口：

```text
old-code-mini/pages/mine/index.vue
我的参赛证 -> old-code-mini/pages/my-credential/index.vue
```

小程序 API：

```text
old-code-mini/api/sceneCredential.js
GET /competition/userCompetition/sceneCredential/myList
```

页面分组：

```text
competitionGroups
  -> 遍历 credentialList
  -> 按 seriesId / competitionName / credentialId 建组
  -> group.credentials.filter(isCompetitionScope)
  -> group.credentials.filter(!isCompetitionScope)
```

`isCompetitionScope` 判定：

```text
(credential.scopeType || 'SCHEDULE') === 'COMPETITION'
```

主证件选择：

```text
pickCompetitionCredential(credentials)
  -> 第一张 credentialStatus=EFFECTIVE 的大赛级证件
  -> 否则第一张大赛级证件
  -> 否则 null
```

因此小程序与 PC 一样，会把 `scope_type=COMPETITION` 的证件作为顶部“大赛证二维码”，把 `scope_type=SCHEDULE` 的证件作为下方赛场信息。

## 10. 代码逻辑表现与脏数据边界

属于当前代码逻辑导致的数据表现：

1. 赛场发证生成一张自动大赛级证件；
2. 同一用户同时拥有大赛级证件和赛场级证件；
3. PC/小程序顶部显示大赛级证件；
4. 自动大赛级证件没有 `schedule_id/target_id`；
5. 删除 target 时不会命中自动大赛级证件。

更像历史脏数据或后续操作引入的问题：

1. 同一用户同一大赛多张有效大赛级证件；
2. 同一 subject/type 下多张有效大赛级证件；
3. target 删除后自动大赛级证件仍有效残留；
4. 同一 target 多张有效赛场级证件；
5. 证件字段与当前 target 字段不一致。

这些问题在当前测试库的实际结果见 `SCENE_CREDENTIAL_AUTO_COMPETITION_DATA_AUDIT.md`。
