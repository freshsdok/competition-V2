# 现场证件一证多权阶段 2 旁路发证开发与测试报告

开发时间：2026-07-05

本阶段只新增一证多权旁路发证能力；未替换旧发证，未替换旧扫码，未修改 PC，未修改小程序，未接入资源预约主流程。

## 1. 开发范围

已新增：

1. 一证多权旁路发证 Service。
2. 测试用旁路发证接口。
3. 核心证件严格复用查询。
4. 旁路发证单元测试。
5. grant 能力白名单补充 `identityVerify` / `scheduleEntry`。

未变更：

1. `generateCompetitionSceneCredential` 旧发证流程。
2. `sceneVerify/scan` 旧扫码流程。
3. `sceneVerify/confirm` 旧确认流程。
4. PC 页面。
5. 小程序页面。
6. 资源预约主流程。

## 2. 新增接口

Controller：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardIssueController.java
```

接口：

```text
POST /sceneOneCardIssue/issueByTarget
POST /competition/sceneOneCardIssue/issueByTarget
```

权限：

```text
competition:sceneCredential:add
```

请求：

```json
{
  "scheduleId": 13,
  "targetId": 44
}
```

返回：

```json
{
  "credentialId": 1000,
  "grantId": 9000,
  "reusedCredential": false,
  "reusedGrant": false,
  "alreadyGranted": false,
  "competitionSeriesId": 1,
  "subjectType": "USER",
  "subjectCode": "1353",
  "credentialType": "PARTICIPANT",
  "roleCode": "MEMBER",
  "scheduleId": 13,
  "targetId": 44
}
```

## 3. 新增后端文件

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneOneCardIssueReq.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneOneCardIssueResult.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneOneCardIssueService.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneOneCardIssueServiceImpl.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardIssueController.java
old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneOneCardIssueServiceImplTest.java
```

修订文件：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialMapper.java
old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImpl.java
old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImplTest.java
```

## 4. 核心证件复用规则

新增 Mapper 方法：

```text
selectEffectiveCompetitionScopeCredentialStrict(
  competitionSeriesId,
  subjectType,
  subjectCode,
  credentialType
)
```

SQL 条件：

```text
competition_series_id
+ scope_type = COMPETITION
+ subject_type
+ subject_code
+ credential_type
+ credential_status = EFFECTIVE
+ del_flag = 0
```

阶段 2 旁路发证未使用旧的：

```text
selectEffectiveCompetitionScopeCredentialByUserId(...)
```

因此不会出现仅按 `competitionSeriesId + userId` 误复用核心证件的问题。

## 5. 旁路发证逻辑

入口：

```text
ICompetitionSceneOneCardIssueService.issueOneCardByScheduleTarget(scheduleId, targetId)
```

流程：

1. 读取 `competition_scene_schedule_target`。
2. 读取 `competition_scene_schedule`。
3. 校验 target 和 schedule 未删除、未停用、未无效。
4. 校验 schedule/target 的 `competition_series_id` 一致。
5. 解析 `credential_type`、`role_code`、`subject_type`、`subject_code`。
6. 按严格 key 查询有效核心证件。
7. 已存在则复用核心证件。
8. 不存在则创建 `scope_type=COMPETITION` 的核心证件。
9. 调用阶段 1 grant service 的 `findActiveScheduleGrant` 判断是否已有授权。
10. 调用 `ensureScheduleGrant` 写入或刷新 `SCHEDULE` grant。
11. 返回 credential/grant 以及复用状态。

## 6. 核心证件字段

旁路新建核心证件时：

```text
issue_channel = SCHEDULE_MATCH
scope_type = COMPETITION
scope_ref_id = competition_series_id
schedule_id = null
target_id = null
credential_status = EFFECTIVE
del_flag = 0
```

核心证件默认 `ability_json`：

```json
{
  "identityVerify": true,
  "report": true,
  "material": true,
  "waiting": false,
  "scheduleEntry": false,
  "resourceReservation": false,
  "review": false,
  "vipAccess": false
}
```

证件号、token、二维码内容沿用现有安全生成方式：

```text
credential_no = CCyyyyMMdd-competitionSeriesId-random8
credential_token = UUID without "-"
qr_content = csc_ + credential_token
```

## 7. SCHEDULE grant 字段

旁路发证写入：

```text
credential_id = core credential id
competition_series_id = schedule/target competition_series_id
scope_type = SCHEDULE
scope_ref_id = schedule_id
source_type = SCHEDULE_TARGET
source_schedule_id = schedule_id
source_target_id = target_id
credential_type = resolved credential type
role_code = resolved role
subject_type = resolved subject type
subject_code = resolved subject code
grant_status = ACTIVE
deleted = 0
```

grant `ability_json` 当前策略：

1. `PARTICIPANT`：`waiting=true`、`scheduleEntry=true`、`resourceReservation=true`。
2. `TEACHER`：`scheduleEntry=true`。
3. `EXPERT`：`scheduleEntry=true`、`review=true`。
4. `STAFF`：`scheduleEntry=true`。
5. `VIP`：`scheduleEntry=true`、`vipAccess=true`。
6. `TEMP`：`scheduleEntry=true`。
7. `report` / `material` 不写入 SCHEDULE grant，保留在核心证件能力中。

grant snapshot 使用白名单字段：

```text
scheduleId, scheduleName, targetId, targetName, roleCode,
credentialType, teamCode, subjectType, subjectCode, groupCode, groupName
```

不包含手机号、身份证、token、二维码、文件 URL。

## 8. 测试覆盖

新增测试：

```text
CompetitionSceneOneCardIssueServiceImplTest
```

覆盖：

1. 同一 target 第一次发证：生成核心 credential + SCHEDULE grant。
2. 同一 target 第二次发证：复用核心 credential + 复用 grant。
3. 同一主体第二个 schedule target 发证：复用 credential + 新增第二条 grant。
4. 同一 userId 不同 credentialType：生成不同核心 credential。
5. 不同 subjectCode：不得误复用 credential。
6. 核心证件 `schedule_id` / `target_id` 保持 `null`。
7. 阶段 2 不调用旧 `selectEffectiveCompetitionScopeCredentialByUserId`。
8. grant ability 中 `scheduleEntry` / `resourceReservation` / `waiting` 符合预期。

回归测试：

```text
CompetitionSceneCredentialScopeGrantServiceImplTest
```

补充确认：

1. `identityVerify` 能力可被保守解析。
2. `scheduleEntry` 能力可被保守解析。
3. 原有 `waiting`、`material`、非法 JSON、未知能力等判断不变。

测试命令：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=CompetitionSceneOneCardIssueServiceImplTest,CompetitionSceneCredentialScopeGrantServiceImplTest test
```

结果：

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 9. 构建结果

XML 校验：

```bash
xmllint --noout old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml
```

结果：通过。

后端编译：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

## 10. 风险与限制

1. 阶段 2 旁路发证未接入旧主流程，因此不会影响现有功能。
2. grant 幂等已有 `active_grant_key` 唯一约束兜底。
3. 核心 credential 目前未新增数据库唯一键，阶段 2 通过 Service 查询和单实例 `synchronized` 降低测试重复插入风险，但不作为生产多实例并发发证入口。
4. 如果阶段 3 要进入真实旁路发证或灰度替换，建议先评估核心证件唯一约束或分布式锁：

```text
competition_series_id + subject_type + subject_code + credential_type + credential_status + del_flag
```

5. 本阶段没有做真实数据库 DML smoke test，没有清理数据，也未连接生产库。

## 11. 是否可以进入阶段 3

阶段 2 的后端旁路发证能力已经完成并通过单元测试和编译。

是否进入阶段 3 旁路扫码，建议先确认：

1. 是否接受核心 credential 暂无数据库唯一键、仅做测试旁路发证。
2. 是否需要先补一个 credential 级并发保护 migration。
3. 阶段 3 是否只做旁路扫码接口，不替换旧 `sceneVerify/scan`。
