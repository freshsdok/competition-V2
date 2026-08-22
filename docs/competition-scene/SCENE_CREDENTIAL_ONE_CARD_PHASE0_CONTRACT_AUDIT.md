# 现场证件一证多权阶段 0 接口契约审计

审计时间：2026-07-05

本文件用于冻结 PC 和小程序当前依赖的现场证件接口契约。阶段 1 只新增 grant 表和基础服务，不修改 PC、小程序页面，不接入旧发证、旧扫码、资源预约主流程。

## 1. 当前扫码 scan 接口响应字段

接口：

```text
POST /sceneVerify/scan
POST /competition/sceneVerify/scan
```

当前后端返回 `CompetitionSceneVerifyResult`，前端已依赖以下字段：

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

一证多权后端适配层必须继续返回这些字段。即使内部改为核心 credential + grant，也不能要求小程序改字段。

## 2. 当前 confirm 接口响应字段

接口：

```text
POST /sceneVerify/confirm
POST /competition/sceneVerify/confirm
```

当前 confirm 会先执行 scan 校验，再根据 `operationType`、`scheduleId`、`targetCredentialId` 确认操作。

confirm 响应沿用 scan 的字段结构：

1. `operationResult`
2. `resultMessage`
3. `duplicate`
4. `credential`
5. `availableActions`
6. `competitionActions`
7. `scheduleActionGroups`
8. `reportState`
9. `materialState`
10. `waitingState`
11. `delegateCredential`

当前小程序确认操作时会继续回填整个 `detail` 对象，因此后端不能只返回局部成功信息。

## 3. 当前我的证件接口响应字段

接口：

```text
GET /competition/userCompetition/sceneCredential/myList
```

当前接口返回当前登录用户可见证件列表。前端按列表自行分组，未要求后端返回“一张证 + 多授权”的新结构。

当前前端依赖的证件字段包括：

1. `credentialId`
2. `credentialNo`
3. `credentialToken`
4. `qrContent`
5. `credentialType`
6. `credentialName`
7. `credentialStatus`
8. `scopeType`
9. `scopeRefId`
10. `scheduleId`
11. `targetId`
12. `competitionSeriesId`
13. `competitionName`
14. `competitionStageName`
15. `competitionTrackName`
16. `secondLevelName`
17. `teamCode`
18. `teamName`
19. `memberId`
20. `userId`
21. `userName`
22. `schoolName`
23. `orgName`
24. `competitionRoleName`
25. `reportStartTime`
26. `reportEndTime`
27. `reportLocation`
28. `contestStartTime`
29. `contestEndTime`
30. `contestLocation`
31. `contestRoom`
32. `waitingStartTime`
33. `waitingEndTime`
34. `waitingLocation`
35. `waitingGroupCode`
36. `waitingGroupName`
37. `materialLocation`
38. `notice`
39. `credentialSnapshotJson`
40. `reportStatus`
41. `reportStateStatus`
42. `reportStateTime`
43. `materialStatus`
44. `materialStateStatus`
45. `materialStateTime`
46. `materialDelegateName`
47. `materialDelegateRelation`
48. `waitingStatus`
49. `waitingStateStatus`
50. `waitingStateTime`

一证多权切换后，如果后端仍走旧接口，必须通过适配层把 grant 信息折叠成上述证件展示字段。

## 4. 当前 PC 使用字段

PC 页面：

```text
old-code-pc/src/views/personal/personaltabs/Competition.vue
```

PC “我的赛事-我的证件”当前使用方式：

1. 使用 `qrContent`，没有则使用 `credentialToken` 生成二维码。
2. 使用 `credentialStatus` 显示证件状态 tag。
3. 使用 `credentialType` 和 `credentialName` 显示证件类型。
4. 使用 `scopeType` 判断大赛级或赛场级展示。
5. 使用 `competitionTrackName`、`secondLevelName`、`schoolName`、`orgName`、`competitionRoleName` 展示主体信息。
6. 使用 `reportStartTime/reportEndTime/reportLocation` 展示报道信息。
7. 使用 `contestStartTime/contestEndTime/contestLocation/contestRoom` 展示赛场信息。
8. 使用 `materialLocation` 展示资料领取地点。
9. 使用 `notice` 展示注意事项。
10. 使用 `reportStatus/reportStateStatus` 判断是否已报道。
11. 使用 `materialStatus/materialStateStatus` 判断资料是否已领取。
12. 使用 `credentialSnapshotJson` 作为标题、分组和兜底展示来源。

阶段 1 不修改 PC 页面，也不改变上述字段含义。

## 5. 当前小程序使用字段

小程序页面：

```text
old-code-mini/pages/my-credential/index.vue
old-code-mini/pages/scan/result.vue
```

小程序“我的参赛证”当前使用方式：

1. 使用 `qrContent`，没有则使用 `credentialToken` 绘制二维码。
2. 使用 `credentialId/credentialNo/credentialToken` 生成 canvas id 和 key。
3. 使用 `credentialType` 显示角色标签。
4. 使用 `credentialStatus` 显示状态。
5. 使用 `scopeType` 判断大赛级和赛场级。
6. 使用 `competitionName`、`teamName`、`userName`、`credentialNo` 展示主信息。
7. 使用 `competitionTrackName`、`secondLevelName`、`schoolName`、`orgName`、`competitionRoleName` 展示主体信息。
8. 使用报道、赛场、资料、候场相关时间和地点字段展示安排。
9. 使用 `reportStatus/reportStateStatus`、`materialStatus/materialStateStatus` 判断完成状态。
10. 使用 `credentialSnapshotJson` 作为标题和分组兜底来源。

小程序扫码页当前使用方式：

1. 使用 `detail.operationResult` 判断成功或失败。
2. 使用 `detail.resultMessage` 显示失败或结果信息。
3. 使用 `detail.credential` 展示被扫证件。
4. 使用 `detail.operatorRoleLabel` 和 `detail.targetRoleLabel` 展示扫码人和被扫对象角色。
5. 使用 `detail.availableActions` 作为兜底动作列表。
6. 使用 `detail.competitionActions` 显示大赛级动作。
7. 使用 `detail.scheduleActionGroups` 显示赛场动作分组。
8. confirm 入参会传 `operationType`、`scheduleId`、`targetCredentialId`。
9. 资料领取代领会传 `delegateQrContent`。

阶段 1 不修改小程序页面，也不改变扫码响应结构。

## 6. 当前前端隐式依赖的 credential 字段

前端除了模板中直接展示字段外，还隐式依赖以下行为：

1. `qrContent` 的值可以直接绘制为二维码内容。
2. `credentialToken` 可作为二维码内容兜底。
3. `scopeType=COMPETITION` 表示顶部主证件。
4. 非 `COMPETITION` 证件表示赛场明细。
5. `credentialSnapshotJson` 中可能包含 `schedule` 和 `target` 快照，前端会读取其字段作为兜底。
6. `reportStatus/materialStatus/waitingStatus` 使用 `0/1` 表示未完成/已完成。
7. `reportStateStatus/materialStateStatus/waitingStateStatus` 可以覆盖旧状态字段。
8. `scheduleActionGroups[].actions[].scheduleId` 用于 confirm。
9. `scheduleActionGroups[].actions[].targetCredentialId` 用于 confirm。
10. `action.actionKind=PROMPT` 只展示提示，不执行确认。
11. `action.enabled=false` 时按钮不可点。
12. `action.status=DONE` 表示已完成。

一证多权适配层必须保持这些隐式行为。

## 7. 一证多权后端适配层必须保持的字段

后续新模型接入旧接口时，后端适配层必须保证：

1. 核心证件仍能映射为 `credential`。
2. grant 能映射为 `scheduleActionGroups`。
3. 大赛级报道和资料动作仍映射为 `competitionActions`。
4. SCHEDULE 动作必须携带 `scheduleId`。
5. 多个 SCHEDULE grant 必须返回多个明确的赛场分组。
6. confirm 时如果涉及 SCHEDULE 动作，必须能通过 `scheduleId` 找回 grant。
7. `operation_state` 仍优先作为完成状态事实源。
8. MATERIAL 状态仍写 `COMPETITION + USER`，不写 `TEAM + MATERIAL`。
9. 资料代领仍通过 `delegateQrContent` 识别代领人核心证件。
10. operation_log 后续需要能追踪 grant 来源。

## 8. 本阶段确认不修改 PC 和小程序页面

阶段 0 + 阶段 1 的边界：

1. 不改 PC 页面。
2. 不改小程序页面。
3. 不改前端 API 文件。
4. 不改旧扫码接口。
5. 不改旧 confirm 接口。
6. 不改旧我的证件接口。
7. 不接入旧发证流程。
8. 不接入资源预约主流程。
9. 只新增 grant 表、grant domain、grant mapper、grant service。

结论：阶段 0 契约审计确认，一证多权第一阶段必须以后端旁路方式推进，前端零改动。

