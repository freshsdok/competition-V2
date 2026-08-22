# 现场证件一证多权阶段 3 旁路扫码开发前审计

审计时间：2026-07-05

本轮审计对象是当前旧扫码链路。阶段 3 只新增一证多权旁路扫码 preview / confirm，不替换旧 `/sceneVerify/scan`、`/sceneVerify/confirm`，不修改 PC、小程序或资源预约。

## 1. 当前旧扫码接口契约

Controller：

```text
CompetitionSceneVerifyController
```

路径：

```text
POST /sceneVerify/scan
POST /competition/sceneVerify/scan
POST /sceneVerify/confirm
POST /competition/sceneVerify/confirm
```

旧接口特点：

1. `scan` 只做预览和动作矩阵生成。
2. `confirm` 内部先调用 `scan`，通过后再执行指定操作。
3. `confirm` 会更新旧 `competition_scene_credential` 上的报道、资料、候场冗余状态字段。
4. `scan` 和 `confirm` 都写 `competition_scene_operation_log`。
5. 返回结果使用 `CompetitionSceneVerifyResult`，其中包含完整 `CompetitionSceneCredential` 对象。

## 2. 当前扫码请求字段

请求对象：

```text
CompetitionSceneVerifyReq
```

字段：

1. `credentialToken`
2. `qrContent`
3. `operationType`
4. `scheduleId`
5. `targetCredentialId`
6. `receiverName`
7. `receiverPhone`
8. `receiverIdSuffix`
9. `subjectUserId`
10. `delegateCredentialToken`
11. `delegateQrContent`
12. `operatorOpenId`
13. `operatorPhone`
14. `scanIp`
15. `deviceInfo`

注意：旧请求允许传 `operatorPhone`、代领二维码内容等字段。阶段 3 旁路接口不复用这些敏感字段作为日志明文。

## 3. 当前扫码响应字段

响应对象：

```text
CompetitionSceneVerifyResult
```

字段：

1. `operationResult`
2. `resultMessage`
3. `applyCheckResult`
4. `scheduleCheckResult`
5. `identityCheckResult`
6. `duplicate`
7. `credential`
8. `operatorRole`
9. `operatorRoleLabel`
10. `targetRole`
11. `targetRoleLabel`
12. `availableActions`
13. `competitionActions`
14. `scheduleActionGroups`
15. `reviewEntryAvailable`
16. `reviewEntryMessage`
17. `matrixMessage`
18. `reportState`
19. `materialState`
20. `waitingState`
21. `delegateCredential`

风险：旧响应中的 `credential` 是完整证件对象，可能包含 `credentialToken`、`qrContent`、手机号、身份证后缀等字段。阶段 3 旁路接口必须改用安全摘要 DTO。

## 4. 当前 allowedActions 结构

动作对象：

```text
CompetitionSceneScanAction
```

字段：

1. `actionType`
2. `actionLabel`
3. `actionKind`
4. `enabled`
5. `status`
6. `message`
7. `scheduleId`
8. `targetCredentialId`
9. `scheduleName`
10. `scheduleTime`
11. `scheduleLocation`

旧动作类型使用：

1. `REPORT_SIGN`
2. `MATERIAL_RECEIVE`
3. `WAITING_CHECK_IN`
4. `EXPERT_REVIEW_ENTRY`

阶段 3 旁路动作类型改为：

1. `REPORT`
2. `MATERIAL_SELF`
3. `MATERIAL_DELEGATE`
4. `WAITING`

多赛场授权继续使用 `CompetitionSceneScheduleActionGroup` 作为展示结构，但当 `currentScheduleId` 为空时只展示授权列表，不直接返回可确认候场动作。

## 5. 当前 operation_state 写入方式

Service：

```text
ICompetitionSceneSubjectOperationStateService
CompetitionSceneSubjectOperationStateServiceImpl
```

写入方法：

```text
insertDoneOperationStateIfAbsent(state)
```

旧确认逻辑：

1. `REPORT_SIGN` 映射为 `REPORT`。
2. `MATERIAL_RECEIVE` 映射为 `MATERIAL`。
3. `WAITING_CHECK_IN` 映射为 `WAITING`。
4. `REPORT` / `MATERIAL` 可写 `COMPETITION` 或旧 `SCHEDULE` scope，取决于旧 credential 的 scope。
5. `WAITING` 必须写 `SCHEDULE` scope。
6. 插入前会查已有 `DONE` 状态，重复时返回 duplicate。
7. 写入后可更新 `last_log_id`。

阶段 3 旁路规则：

1. `REPORT` 固定写 `COMPETITION + REPORT`。
2. `MATERIAL_SELF` / `MATERIAL_DELEGATE` 固定写 `COMPETITION + USER + MATERIAL`。
3. `WAITING` 固定写 `SCHEDULE + WAITING`，必须带 `currentScheduleId` 和 ACTIVE grant。
4. 不更新旧 credential 上的冗余状态字段。

## 6. 当前 operation_log 写入方式

Mapper：

```text
CompetitionSceneOperationLogMapper
```

旧 `writeLog` 行为：

1. 写入 `credential_id`、`schedule_id`、`target_id`、`competition_series_id`。
2. 写入 `credential_no`、`credential_token`。
3. 写入姓名、手机号、身份证后缀、请求 payload、响应 payload。
4. scan 和 confirm 都写日志。

阶段 3 旁路要求：

1. 继续写 `competition_scene_operation_log`。
2. 不写 token、二维码明文、手机号、身份证号。
3. `request_payload` / `response_payload` 使用安全摘要。
4. 当前表没有 `grant_id` 字段，本阶段在 `remark` 里记录：
   - `grantId`
   - `scopeType`
   - `scopeRefId`
   - `currentScheduleId`
   - `actionType`

## 7. 资料代领现有逻辑

旧逻辑：

1. 从 `delegateCredentialToken` 或 `delegateQrContent` 解析代领人 credential。
2. 代领人 credential 必须存在且有效。
3. 若被领取人没有团队信息且代领人与被领取人不同，拒绝。
4. 若双方都有 `teamCode` 且不同，拒绝。
5. 若被领取人有团队但代领人无团队，拒绝。
6. operation_state 写在被领取人主体上。
7. delegate 字段写入 state。

阶段 3 旁路调整：

1. 代领接口使用 `delegateCredentialId`，不传代领二维码明文。
2. 被领取人与代领人都必须是有效核心 credential。
3. 代领状态仍写被领取人的 `COMPETITION + USER + MATERIAL`。
4. 同队关系使用 `teamCode` 校验。
5. 跨队代领失败。

## 8. 当前扫码人角色识别逻辑

旧逻辑：

1. 优先从登录用户 `userId` 查询同赛场或同赛事的有效证件。
2. 也支持 `operatorPhone` 辅助查询。
3. 按角色优先级选择操作员证件：
   - `CHECKIN_STAFF` / `MATERIAL_STAFF`
   - `STAFF`
   - `EXPERT`
   - `TEACHER` / `CAPTAIN` / `MEMBER`

阶段 3 旁路策略：

1. 正式切换前应从登录态和操作员证件解析角色。
2. 本阶段 pilot 接口允许临时传 `operatorRole`，仅用于测试。
3. 报告中标记该字段为测试字段，不能作为正式权限依据。

## 9. 旁路扫码需要复用和不能复用的逻辑

可复用：

1. 二维码内容 token 解析规则。
2. `operation_state` 幂等插入服务。
3. `operation_log` 表和 mapper。
4. `CompetitionSceneScanAction` / `CompetitionSceneScheduleActionGroup` 展示结构。
5. grant service 的 `hasAbility` / `checkScheduleAbility`。

不能直接复用：

1. 旧 `CompetitionSceneVerifyResult`，因为会返回完整 credential。
2. 旧 `confirm`，因为会更新旧 credential 冗余状态，并使用旧 SCHEDULE credential 模型。
3. 旧 `writeLog`，因为会写 token、手机号、身份证后缀和完整请求响应。
4. 旧 related schedule credential 逻辑，因为一证多权应以 grant 为赛场权限来源。

## 10. 本阶段新增文件清单

计划新增：

```text
CompetitionSceneOneCardVerifyController.java
CompetitionSceneOneCardVerifyReq.java
CompetitionSceneOneCardVerifyResult.java
CompetitionSceneOneCardCredentialSummary.java
ICompetitionSceneOneCardVerifyService.java
CompetitionSceneOneCardVerifyServiceImpl.java
CompetitionSceneOneCardVerifyServiceImplTest.java
SCENE_CREDENTIAL_ONE_CARD_PHASE3_SCAN_DEV_RESULT.md
```

本阶段不新增数据库 migration，不修改旧扫码接口，不修改前端。
