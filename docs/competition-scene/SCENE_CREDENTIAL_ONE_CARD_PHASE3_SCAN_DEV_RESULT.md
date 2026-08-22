# 现场证件一证多权阶段 3：旁路扫码矩阵开发结果

日期：2026-07-05

## 一、本阶段边界

本阶段只新增“一证多权”旁路扫码预览和旁路扫码确认能力。

已遵守限制：

1. 未替换旧 `/sceneVerify` / `/competition/sceneVerify` 扫码链路；
2. 未接入旧发证流程；
3. 未修改 PC 前端；
4. 未修改小程序前端；
5. 未接入资源预约主流程；
6. 未执行生产库连接、数据清理或历史数据迁移；
7. 未调整旧 operation_state 的既有语义。

## 二、开发前审计结论

审计文档：

`docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE3_SCAN_AUDIT.md`

关键结论：

1. 旧扫码接口位于 `CompetitionSceneVerifyController`，入口为 `/sceneVerify/scan`、`/competition/sceneVerify/scan`、`/sceneVerify/confirm`、`/competition/sceneVerify/confirm`。
2. 旧扫码依赖 `competition_scene_credential` 的实体证件字段和旧证件状态字段，并在 confirm 后同步更新 credential 的 report/material/waiting 状态。
3. 旧扫码会写 `competition_scene_operation_log`，但旧 log 表没有 grant_id 字段。
4. 一证多权旁路扫码需要基于核心 COMPETITION credential + SCHEDULE grant 判定动作，不复用旧接口返回对象，避免污染旧前端契约。
5. 阶段 3 先通过新 Controller 提供 pilot 接口，旧扫码不受影响。

## 三、新增接口

新增 Controller：

`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java`

路径：

1. `POST /sceneOneCardVerify/pilot/scan`
2. `POST /competition/sceneOneCardVerify/pilot/scan`
3. `POST /sceneOneCardVerify/pilot/confirm`
4. `POST /competition/sceneOneCardVerify/pilot/confirm`

### 1. scan 请求字段

`CompetitionSceneOneCardVerifyReq` 支持：

1. `credentialToken`
2. `qrContent`
3. `currentScheduleId`
4. `operatorRole`
5. `actionScene`
6. `deviceId`
7. `deviceInfo`
8. `idempotencyKey`

说明：

`operatorRole` 仅用于 pilot 阶段模拟扫码人角色，正式接入时应由登录用户角色、工作人员授权或操作员证件解析得到。

### 2. confirm 请求字段

`CompetitionSceneOneCardVerifyReq` 支持：

1. `credentialId`
2. `actionType`
3. `currentScheduleId`
4. `delegateCredentialId`
5. `operatorRole`
6. `deviceId`
7. `deviceInfo`
8. `idempotencyKey`

动作类型：

1. `REPORT`
2. `MATERIAL_SELF`
3. `MATERIAL_DELEGATE`
4. `WAITING`

## 四、新增后端对象

新增 DTO：

1. `CompetitionSceneOneCardVerifyReq`
2. `CompetitionSceneOneCardVerifyResult`
3. `CompetitionSceneOneCardCredentialSummary`
4. `CompetitionSceneOneCardAction`
5. `CompetitionSceneOneCardScheduleActionGroup`

新增 Service：

1. `ICompetitionSceneOneCardVerifyService`
2. `CompetitionSceneOneCardVerifyServiceImpl`

新增单测：

`old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImplTest.java`

## 五、扫码预览算法

`scan` 核心流程：

1. 从 `credentialToken` 或 `qrContent` 解析 token；
2. 根据 token 查询 credential；
3. 校验必须是核心现场证件：
   - `scope_type = COMPETITION`
   - `credential_status = EFFECTIVE`
   - `del_flag = 0`
   - `valid_from <= now <= valid_to`
4. 读取核心 credential 的 `ability_json`；
5. 查询该 credential 的 ACTIVE grant；
6. 查询 operation_state，计算是否已完成；
7. 生成大赛级动作和赛场级动作组；
8. 写入脱敏 operation_log。

动作生成规则：

1. `REPORT`：核心 ability `report=true` 且扫码人为签到工作人员角色；
2. `MATERIAL_SELF` / `MATERIAL_DELEGATE`：核心 ability `material=true`、扫码人为资料工作人员角色、被扫主体为 `USER`；
3. `WAITING`：必须传入 `currentScheduleId`，且存在该赛场 ACTIVE grant，grant ability `waiting=true`，扫码人为签到工作人员角色；
4. 未传 `currentScheduleId` 时，只返回大赛级动作，并返回 `scheduleActionGroups` 用于提示该证件拥有的赛场授权，不返回可 confirm 的候场动作。

## 六、扫码确认算法

`confirm` 核心流程：

1. 根据 `credentialId` 查询核心 credential；
2. 校验核心 credential 有效；
3. 查询 ACTIVE grants；
4. 根据 `actionType` 构造 operation_state 查询条件；
5. 如果已有状态，返回 `DUPLICATE` / `alreadyDone=true`；
6. 如果未完成，插入 operation_state；
7. 写入脱敏 operation_log；
8. 返回确认结果和当前动作矩阵。

状态写入规则：

1. `REPORT` 写入 `COMPETITION + REPORT`；
2. `MATERIAL_SELF` 写入 `COMPETITION + USER + MATERIAL`，代领关系为 `SELF`；
3. `MATERIAL_DELEGATE` 校验代领人核心 credential 有效，且与被领取人 `teamCode` 一致；状态仍写在被领取人 `USER + MATERIAL` 上，代领关系为 `TEAM_MEMBER`；
4. `WAITING` 必须有 `currentScheduleId`，且该 schedule grant 有 `waiting=true`，状态写入 `SCHEDULE + WAITING`。

## 七、operation_log 处理

阶段 3 未修改 operation_log 表结构。

grant 上下文通过 `remark` 和安全 payload 旁路记录：

1. `grantId`
2. `grantScopeType`
3. `grantScopeRefId`
4. `currentScheduleId`
5. `actionType`
6. `idempotencyKey`

脱敏约束：

1. 不记录 `credentialToken`；
2. 不记录 `qrContent`；
3. 不记录手机号；
4. 不记录身份证号或证件后缀；
5. 不记录 openId / unionId；
6. requestPayload / responsePayload 只保留动作、结果、credentialId、scheduleId、grantId 等非敏感上下文。

## 八、测试结果

专项单测命令：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=CompetitionSceneOneCardVerifyServiceImplTest test
```

结果：

1. Tests run: 9
2. Failures: 0
3. Errors: 0
4. Skipped: 0
5. BUILD SUCCESS

覆盖点：

1. 有效核心证件扫码返回报道动作；
2. 无效证件扫码失败；
3. 报道 confirm 写 COMPETITION operation_state，重复 confirm 返回已完成；
4. 资料工作人员 preview 返回 `MATERIAL_SELF` / `MATERIAL_DELEGATE`，不返回签到动作；
5. 资料自领 confirm 写 COMPETITION + USER + MATERIAL，重复 confirm 返回已完成；
6. 资料代领同队通过、跨队拒绝；
7. 候场 preview/confirm 必须依赖 currentScheduleId 和 SCHEDULE grant；
8. 多赛场 grant 未指定 currentScheduleId 时返回 scheduleActionGroups，不返回候场 confirm 动作；
9. operation_log 不包含 token、二维码内容、手机号、身份证相关字段。

模块编译命令：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

1. Reactor BUILD SUCCESS；
2. `teaching-modules-competition` 编译通过；
3. Maven 输出的重复 dependency 和 Lombok equals/hashCode 警告为项目既有警告，本阶段未新增相关依赖问题。

## 九、旧链路影响评估

本阶段新增独立 Controller、独立 DTO、独立 Service。

未改动：

1. `CompetitionSceneVerifyController`
2. 旧 `/sceneVerify/scan`
3. 旧 `/sceneVerify/confirm`
4. PC 我的证件页面
5. 小程序扫码页面
6. 旧发证流程
7. 资源预约流程

因此当前旧扫码、PC、小程序不会自动切到一证多权旁路链路。

## 十、风险与下一步

当前阶段可以用于后端自测和接口联调，但还不能替换正式扫码入口。

进入阶段 4 或正式替换前建议补齐：

1. `operatorRole` 改为从登录用户、工作人员授权或操作员证件解析，不再由前端传入；
2. operation_log 表如需要结构化追踪授权来源，可新增 `grant_id` / `grant_scope_type` / `grant_scope_ref_id` 字段；
3. 确认资料代领的同队关系是否只依赖 credential `teamCode`，还是需要补 target/grant_snapshot 兜底；
4. 与阶段 2 旁路发证接口做真实数据联调；
5. 决定 PC / 小程序是否进入一证多权展示和旁路扫码适配阶段。

结论：

阶段 3 旁路扫码矩阵开发完成，专项单测和后端编译通过。可以进入“一证多权旁路发证 + 旁路扫码”的受控联调，但不建议直接替换旧扫码主流程。
