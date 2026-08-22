# 现场证件一证多权阶段 3 旁路扫码矩阵严格补强结果

日期：2026-07-05

## 一、本轮边界

本轮只补强阶段 3 旁路扫码矩阵，不进入阶段 4。

已遵守：

1. 未替换旧扫码；
2. 未修改旧发证；
3. 未修改 PC；
4. 未修改小程序；
5. 未接入资源预约主流程；
6. 未连接生产数据库；
7. 未返回 token、二维码明文、手机号、身份证号；
8. 未把 COMPETITION 基础能力当成 SCHEDULE 权限；
9. 未使用 MIXED；
10. 未使用伪赛场安排。

## 二、修复文件清单

1. `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImpl.java`
2. `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImplTest.java`
3. `docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE3_STRICT_REVIEW_RESULT.md`

## 三、validateCoreCredential 修复

已补充核心证件校验：

1. `credentialId` 不能为空；
2. `delFlag` 必须为正常状态；
3. `competitionSeriesId` 不能为空；
4. `subjectType` 不能为空；
5. `subjectCode` 不能为空；
6. 继续要求 `scope_type = COMPETITION`；
7. 继续要求 `credential_status = EFFECTIVE`；
8. 继续校验 `valid_from` / `valid_to` 有效期。

失败时返回明确提示，例如：

1. `证件已删除或不可用`
2. `证件所属大赛不能为空`
3. `证件主体类型不能为空`
4. `证件主体编码不能为空`

## 四、confirm 事务和日志处理修复

已调整 confirm 一致性规则：

1. operation_state 写入成功后，业务结果以状态写入为准；
2. `writePilotLog` 改由 `safeWritePilotLog` 包裹；
3. `updateLastLogId` 改由 `safeUpdateLastLogId` 包裹；
4. 日志写入失败不会把已成功确认改成失败；
5. `last_log_id` 回填失败不会把已成功确认改成失败；
6. 状态写入前发生异常时返回 `EXCEPTION`；
7. 状态写入前异常会尝试标记当前事务回滚；
8. 不再出现“状态已 DONE，但返回扫码确认失败”的日志附属错误场景。

结果提示：

1. 日志失败但状态已完成：返回 `PASS`，并追加 `日志写入失败但业务已完成`；
2. lastLogId 更新失败但状态已完成：返回 `PASS`，并追加 `状态日志关联更新失败但业务已完成`。

## 五、资料代领边界修复

`MATERIAL_DELEGATE` 已补充：

1. `delegateCredentialId` 必填；
2. `delegateCredentialId` 不能等于被领取人的 `credentialId`；
3. 代领人 credential 必须是有效核心 credential；
4. 代领人 `subjectType` 必须为 `USER`；
5. 代领人 `userId` 不能为空；
6. 被领取人 `userId` 不能为空；
7. 双方 `teamCode` 必须非空；
8. 双方 `teamCode` 必须相同；
9. 同一 userId 不允许走代领，必须走 `MATERIAL_SELF`；
10. 跨队代领继续失败。

## 六、候场角色边界修复

`WAITING` 在 scan preview 和 confirm 两处同时补充被扫对象角色校验。

允许候场的被扫对象角色：

1. `MEMBER`
2. `CAPTAIN`

默认不允许候场：

1. `TEACHER`
2. `EXPERT`
3. `STAFF`
4. 其他非参赛主体角色

即使误配了 `grant.ability_json.waiting=true`，教师、专家、工作人员也不会被返回候场动作或允许候场确认。

## 七、多 grant 歧义修复

已将原先“命中第一条 grant”改为同赛场授权解析：

1. 同一 `currentScheduleId` 下 0 条 ACTIVE SCHEDULE grant：返回 `证件无当前赛场候场权限`；
2. 同一 `currentScheduleId` 下 1 条 ACTIVE SCHEDULE grant：正常继续校验 ability 和角色；
3. 同一 `currentScheduleId` 下超过 1 条 ACTIVE SCHEDULE grant：返回 `当前赛场授权存在多条，请联系管理员处理`；
4. 多 grant 歧义下不返回候场动作，不允许候场确认。

## 八、operation_state 并发幂等结论

审计结论：

`ICompetitionSceneSubjectOperationStateService.insertDoneOperationStateIfAbsent` 当前返回 `CompetitionSceneSubjectOperationState`，没有显式返回 `created=true/false` 标志。

本轮旁路 confirm 已补强：

1. 写入前先查询已有 DONE；
2. 写入时给本次 state 设置无敏感信息的内部 marker；
3. 如果 insert 返回状态的 marker 与本次 marker 相同，判定为本次新插入，返回 `PASS`；
4. 如果 insert 返回状态的 marker 与本次 marker 不同，判定为并发或重复下已有 DONE，返回 `DUPLICATE / alreadyDone=true`。

正式替换前建议：

将 operation_state service 增加显式返回对象，例如 `created + state`，避免长期依赖 remark marker 做旁路识别。该项不阻塞当前测试库旁路联调，但应作为正式替换前的增强项。

## 九、confirm 安全边界说明

当前旁路 confirm 只要求 `credentialId` 和 `actionType`，不要求 token 或 scanTicket。

安全边界：

1. 当前接口只能用于受控测试；
2. 不允许直接开放给普通前端；
3. 正式接入前必须增加 `scanTicket` 或 `credentialToken` 二次校验；
4. 正式接入前操作员角色不能由前端传入；
5. 操作员角色必须由登录态、工作人员授权或操作员证件解析得到。

## 十、单元测试结果

执行命令：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=CompetitionSceneOneCardVerifyServiceImplTest test
```

结果：

1. Tests run: 20
2. Failures: 0
3. Errors: 0
4. Skipped: 0
5. BUILD SUCCESS

新增覆盖：

1. `delFlag` 非正常 credential 被拒绝；
2. `competitionSeriesId` 为空被拒绝；
3. `subjectType` / `subjectCode` 为空被拒绝；
4. `MATERIAL_DELEGATE` 使用本人 credential 被拒绝；
5. `MATERIAL_DELEGATE` 代领人不是 USER 被拒绝；
6. `MATERIAL_DELEGATE` 代领人 userId 为空被拒绝；
7. `WAITING` 教师角色即使 grant.waiting=true 也被拒绝；
8. `WAITING` 同一 currentScheduleId 命中多条 grant 时被拒绝；
9. `writePilotLog` 抛异常时，已成功 operation_state 不会变成失败结果；
10. `updateLastLogId` 抛异常时，确认结果仍为 PASS；
11. 模拟 `insertDoneOperationStateIfAbsent` 返回已有状态时，返回 DUPLICATE。

## 十一、编译结果

执行命令：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

1. Reactor BUILD SUCCESS；
2. `teaching-modules-competition` 编译通过；
3. Maven 输出的 duplicate dependency 和 Lombok equals/hashCode 警告为项目既有警告，本轮未新增相关依赖问题。

## 十二、是否可以进入测试库联调

结论：

可以进入“一证多权旁路发证 + 旁路扫码”的测试库受控联调。

但仍不允许直接替换旧扫码主流程。正式替换前至少需要补齐：

1. confirm 增加 scanTicket 或 credentialToken 二次校验；
2. 操作员角色改为后端解析；
3. operation_state service 建议返回显式 `created` 标志；
4. 明确旁路 operation_log 是否需要结构化 grant 字段；
5. 与真实阶段 2 旁路发证数据做联调回归。
