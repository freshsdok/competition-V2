# 现场证件一证多权后端旁路试运行工作计划

撰写时间：2026-07-05

本文档用于汇总下一阶段工作安排。当前原则是：保留现有可运行代码，不调整 PC 和小程序前端界面，不改变前端当前依赖的数据结构；在后端新增一证多权旁路能力，先用新测试数据验证可用性，再评估是否替换现有双证实现。

## 1. 背景和判断

当前现场证件代码已经可以完成证件生成、扫码、报道、资料领取、候场和操作日志记录，但模型上存在以下问题：

1. 赛场发证时会同时生成大赛级证件和赛场级证件，容易形成“双证”认知。
2. 证件既承担身份识别，又承担权限和动作状态，职责偏重。
3. 证件有效期直接复用大赛时间或部分赛场时间，无法完整表达报道、资料、候场、资源预约等不同动作窗口。
4. `valid_from` 已落库但扫码当前未校验，`valid_to` 对候场时间覆盖不完整。
5. PC 和小程序前端已经相对稳定，本阶段不适合继续调整页面结构和字段结构。

因此下一阶段采用“后端旁路试运行”策略：

1. 不直接替换现有接口实现。
2. 不要求前端改页面。
3. 新增一证多权后端能力。
4. 用测试赛事和新测试数据验证。
5. 验证稳定后，再通过后端适配层和灰度开关逐步替换旧实现。

## 2. 总体目标

长期目标采用“现场一证多权”模型：

1. 一个主体在一场大赛中原则上只有一张核心现场证件。
2. 核心证件用于识别主体身份。
3. 赛场权限通过 grant 显式授权。
4. 报道、资料领取、候场、资源预约等动作由角色、grant、能力和时间窗口共同决定。
5. 动作完成状态写入 `competition_scene_subject_operation_state`。
6. 所有扫码和确认流水写入 `competition_scene_operation_log`。
7. PC 和小程序继续接收现有结构的响应，前端感知不到后端模型切换。

## 3. 本阶段不做事项

1. 不修改 PC 页面。
2. 不修改小程序页面。
3. 不调整前端展示字段、按钮结构和数据结构。
4. 不立即替换现有 `/sceneVerify/scan` 和 `/sceneVerify/confirm` 逻辑。
5. 不清理历史测试数据。
6. 不迁移现有历史 SCHEDULE 证件。
7. 不开发资源预约第六阶段。
8. 不开发正式评审页面。
9. 不开发独立运维端。
10. 不修改报名、支付、成绩、证书主流程。

## 4. 前端契约冻结

后端后续即使切换到一证多权，也必须继续兼容当前 PC 和小程序依赖的响应结构。

### 4.1 扫码接口响应结构

`/competition/sceneVerify/scan` 和 `/sceneVerify/scan` 需要继续返回：

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

### 4.2 确认接口响应结构

`/competition/sceneVerify/confirm` 和 `/sceneVerify/confirm` 继续沿用扫码响应结构，并保持以下行为：

1. 确认前先完成扫码校验。
2. 成功后刷新 `credential` 和动作矩阵。
3. 重复操作返回 `DUPLICATE`。
4. 失败时返回明确 `resultMessage`。

### 4.3 我的证件接口

当前“我的证件”只应返回当前登录用户证件。后端切换一证多权时，仍需保证：

1. 当前用户只能看到自己的证件。
2. 不再按 `teamCode` 拉整队证件。
3. 返回结构尽量保持现有页面可直接渲染。

## 5. 新模型核心设计约束

### 5.1 核心证件

核心证件仍存储在 `competition_scene_credential`。

职责：

1. 识别主体。
2. 保存二维码 token。
3. 保存主体基础快照。
4. 保存证件生命周期。
5. 不直接表达所有赛场权限。

核心证件建议使用：

```text
scope_type = COMPETITION
```

核心证件唯一和复用规则必须明确为：

```text
competition_series_id + subject_type + subject_code + credential_type
```

禁止仅按 `competitionSeriesId + userId` 复用核心证件。`user_id` 可以作为人员主体的派生字段或查询辅助字段，但不能作为核心证件唯一身份条件。原因是同一用户在同一赛事中可能存在不同主体、不同角色或不同证件类型，按 userId 复用会扩大命中范围，造成权限和身份混用。

### 5.2 赛场权限 grant

新增授权表：

```text
competition_scene_credential_scope_grant
```

职责：

1. 表达某张核心证件被授予了哪个赛场的权限。
2. 保存授权来源。
3. 保存授权能力。
4. 保存授权有效期。
5. 保存动作窗口。
6. 支持撤销和软删除。

建议字段：

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
11. `ability_json`
12. `valid_from`
13. `valid_to`
14. `operation_window_json`
15. `grant_status`
16. `create_time`
17. `update_time`
18. `deleted`

grant 幂等规则：

```text
credential_id
+ scope_type = SCHEDULE
+ scope_ref_id = scheduleId
+ source_target_id
+ grant_status = ACTIVE
```

同一组合只能有一条有效授权。严格审计后，阶段 1 采用 `active_grant_key` 唯一约束兜住并发重复插入风险：

```text
active_grant_key = credential_id:scope_type:scope_ref_id:source_target_id
```

仅 `ACTIVE 且 deleted=0` 的授权写入 `active_grant_key`；撤销后置空。Service 层仍保留事务幂等和 `SELECT FOR UPDATE`，数据库唯一键作为多实例并发保护。

### 5.3 时间职责

后续时间模型建议拆开：

1. `competition_series_info.competition_start_time/end_time`：赛事周期或默认兜底时间，不直接等同现场动作有效期。
2. `competition_scene_credential.valid_from/valid_to`：核心证件生命周期，表示证件整体是否可识别。
3. `competition_scene_credential_scope_grant.valid_from/valid_to`：赛场授权有效期，至少覆盖该赛场所有现场动作。
4. `operation_window_json`：报道、资料、候场、资源预约等动作各自窗口。
5. `operation_state.operation_time`：动作完成事实时间。
6. `operation_log.operation_time`：扫码或确认流水时间。

### 5.4 SCHEDULE 动作上下文

SCHEDULE 级动作必须有明确的 `scheduleId` 上下文。

规则：

1. 没有 `currentScheduleId` 时，只返回大赛级动作。
2. 候场、赛场入场、资源预约等动作必须指定 `scheduleId`。
3. 如果一个 credential 有多个 SCHEDULE grant，confirm 时必须指定 `scheduleId`。
4. 后端适配旧响应结构时，`scheduleActionGroups` 必须把每个赛场动作绑定到明确的 `scheduleId`。

### 5.5 operation_log 授权追踪预留

一证多权下，扫码和确认不仅要知道 credential，还要知道动作来自哪条 grant。

阶段 1 暂不修改 `competition_scene_operation_log` 表，但设计上预留：

1. `grant_id`
2. `grant_scope_type`
3. `grant_scope_ref_id`

阶段 3 旁路扫码时，至少应在上下文或日志扩展信息中记录授权来源，便于排查“为什么这个人能做这个动作”。

### 5.6 资料代领

资料代领必须纳入一证多权旁路扫码矩阵。

规则：

1. 被领取人和代领人都通过核心 credential 识别。
2. 同队关系通过 `teamCode`、target 或 `grant_snapshot_json` 校验。
3. MATERIAL 状态仍写：

```text
scope_type = COMPETITION
subject_type = USER
operation_type = MATERIAL
```

4. 不写 `TEAM + MATERIAL`。

### 5.7 资源预约能力预留

本阶段不开发资源预约第六阶段，但 grant 服务需要预留能力判断方法：

```text
checkScheduleAbility(credentialId, scheduleId, abilityCode)
```

后续资源预约可以通过该方法判断 `resourceReservation` 权限，不需要再回到 SCHEDULE 实体证件上判断。

## 6. 分阶段工作安排

## 阶段 0：现状冻结和契约确认

目标：确认前端零改动边界，冻结当前接口契约。

工作内容：

1. 梳理 PC 和小程序当前使用的证件相关接口。
2. 固定扫码、确认、我的证件接口响应结构。
3. 明确一证多权切换后仍由后端适配旧结构。
4. 明确测试数据可以重建，不考虑历史测试数据迁移。

产出：

1. 接口契约清单。
2. 前端零改动约束清单。
3. 一证多权测试赛事清单。

验收点：

1. PC 和小程序不需要改页面。
2. 当前接口字段被完整列出。
3. 灰度测试赛事明确。

## 阶段 1：新增 grant 表和基础代码

目标：新增一证多权基础数据结构，不影响旧逻辑。

改动范围：

1. 新增 `competition_scene_credential_scope_grant` 表。
2. 新增 grant domain、mapper、service。
3. 新增基础查询、插入、撤销、软删除能力。
4. 暂不改变旧扫码和旧发证逻辑。

数据库 migration：

1. 新增 grant 表。
2. 新增必要索引：
   - `idx_grant_credential`
   - `idx_grant_competition_scope`
   - `idx_grant_source_schedule_target`
   - `idx_grant_status`

测试点：

1. grant 可插入。
2. grant 可按 credential 查询。
3. grant 可按赛事和赛场查询。
4. grant 可撤销。
5. 旧接口不受影响。

回滚策略：

1. 不接入旧接口时，删除新接口调用即可回滚。
2. grant 表不影响旧业务路径。

风险：

1. 表字段设计过窄会影响后续动作窗口表达。
2. 缺少唯一约束会导致重复授权。

## 阶段 2：新增一证多权旁路发证

目标：新增后端旁路发证能力，不生成 SCHEDULE 实体证件。

建议新增：

1. `CompetitionSceneOneCardIssueService`
2. 一证多权测试发证接口。

核心逻辑：

1. 根据赛事和主体查找核心证件。
2. 没有核心证件则生成一张 `COMPETITION` 核心证件。
3. 根据赛场安排和 target 生成 SCHEDULE grant。
4. grant 写入能力、角色、来源、时间窗口。
5. 不生成新的 `scope_type=SCHEDULE` 实体证件。

测试点：

1. 同一个用户同一赛事只生成一张核心证件。
2. 同一核心证件可以拥有多个赛场 grant。
3. 删除或撤销 target 时可以撤销 grant。
4. 核心证件二维码可扫码识别。

回滚策略：

1. 旁路接口停用即可。
2. 不影响旧发证路径。

风险：

1. 核心证件复用条件过宽会造成多角色混用。
2. grant 重复写入会导致动作矩阵重复。

## 阶段 3：新增一证多权旁路扫码矩阵

目标：验证新扫码算法。

建议新增：

1. `CompetitionSceneOneCardVerifyService`
2. 一证多权测试扫码接口。
3. 一证多权测试确认接口。

扫码逻辑：

1. 解析二维码 token。
2. 查找核心证件。
3. 校验证件状态。
4. 校验证件 `valid_from/valid_to`。
5. 查询有效 grant。
6. 根据扫码人角色、被扫对象角色、grant 能力、时间窗口生成动作矩阵。
7. 返回与旧接口一致的数据结构。

确认逻辑：

1. 确认前重新执行扫码矩阵。
2. 校验动作是否存在且可执行。
3. 报道写 `COMPETITION` operation_state。
4. 资料领取写 `COMPETITION` operation_state。
5. 候场写 `SCHEDULE` operation_state。
6. 成功和失败都写 `competition_scene_operation_log`。

测试点：

1. 签到工作人员扫参赛人员证件返回报道动作。
2. 资料工作人员扫参赛人员证件返回资料领取动作。
3. 有 SCHEDULE grant 时返回候场动作。
4. 没有 SCHEDULE grant 时不返回候场动作。
5. grant 过期时动作不可执行。
6. 重复确认返回 `DUPLICATE`。
7. 操作状态写入正确 scope。
8. 操作流水完整。

回滚策略：

1. 不接旧接口，停用旁路接口即可。
2. 测试数据可重建。

风险：

1. 新矩阵与旧前端结构适配不完整。
2. 操作状态 scope 写错会影响重复判断。

## 阶段 4：后端适配层

目标：让旧前端在不改代码的情况下消费新模型数据。

工作内容：

1. 将核心证件和 grant 转换为旧响应结构。
2. 继续返回 `credential`。
3. 继续返回 `competitionActions`。
4. 继续返回 `scheduleActionGroups`。
5. 继续返回 `reportState/materialState/waitingState`。
6. 兼容旧 `targetCredentialId/scheduleId` 选择逻辑。

适配原则：

1. 前端字段不变。
2. 前端按钮判断不变。
3. 前端二维码内容不变。
4. 后端内部数据来源可变。

测试点：

1. 小程序扫码页不改代码可渲染动作。
2. PC 我的证件不改代码可展示主证件。
3. 确认操作入参保持兼容。

回滚策略：

1. 适配层通过开关控制。
2. 出问题回旧 service。

风险：

1. 当前前端可能隐式依赖 SCHEDULE credential 字段。
2. grant 数据转换成旧结构时可能丢失赛场明细。

## 阶段 5：灰度开关

目标：只对指定测试赛事启用一证多权。

建议配置：

```text
scene.credential.one-card.enabled=false
scene.credential.one-card.enabled-series=1,2,3
```

灰度规则：

1. 默认所有赛事走旧逻辑。
2. 指定 `competitionSeriesId` 走一证多权逻辑。
3. 发证、扫码、确认、我的证件都按同一开关判断。
4. 出现问题可立即切回旧逻辑。

测试点：

1. 非灰度赛事不受影响。
2. 灰度赛事走新 service。
3. 切回旧逻辑后接口可用。

回滚策略：

1. 修改配置关闭灰度。
2. 不需要回滚前端。

风险：

1. 同一赛事部分接口走新逻辑、部分接口走旧逻辑会造成数据不一致。
2. 配置未生效时排查成本较高。

## 阶段 6：使用现有 PC 和小程序真实验证

目标：前端不改代码，用现有页面验证新后端。

测试场景：

1. 当前登录用户查看我的证件。
2. 工作人员扫码参赛人员证件。
3. 报道确认。
4. 资料领取确认。
5. 同队代领资料。
6. 候场确认。
7. 重复扫码和重复确认。
8. 证件未生效。
9. 证件已过期。
10. grant 被撤销。
11. grant 无候场能力。
12. 多赛场 grant。

验收标准：

1. PC 和小程序无需调整页面。
2. 旧接口结构保持兼容。
3. 测试赛事完成一证多权闭环。
4. 成功操作写 operation_state。
5. 所有扫码和确认写 operation_log。
6. 可通过配置回退旧逻辑。

## 阶段 7：评估替换旧实现

目标：根据旁路验证结果决定替换策略。

可选路径：

1. 旧接口内部切换到新 service，前端完全无感。
2. 旧逻辑和新逻辑长期按赛事灰度共存。
3. 新模型稳定后，停止生成 SCHEDULE 实体证件。
4. 后续再治理或隐藏历史 SCHEDULE 证件。

替换前置条件：

1. 新模型测试赛事发证稳定。
2. 扫码矩阵稳定。
3. 报道、资料、候场状态稳定。
4. PC 和小程序不改代码可正常使用。
5. 回滚开关可用。

## 7. 优先开发清单

第一批：

1. 阶段 0 接口契约确认。
2. 阶段 1 grant 表 migration。
3. grant domain、mapper、service。
4. 一证多权旁路发证 service。
5. 一证多权旁路扫码 service。
6. 单元测试或接口测试脚本。

第二批：

1. 后端适配层。
2. 灰度开关。
3. 旧接口按赛事灰度接入新 service。
4. PC 和小程序现有页面真实验证。

第三批：

1. 停用赛场发证生成 SCHEDULE 实体证件。
2. 历史测试数据治理。
3. 旧字段和旧逻辑收敛。

## 8. 风险清单

1. 一证多权 grant 设计不足，后续资源预约和专家评审扩展受限。
2. 旧前端隐式依赖 SCHEDULE credential，适配层需要充分测试。
3. 时间窗口规则不清，会导致扫码动作误开放或误拦截。
4. 多角色人员的核心证件复用条件需要严格设计。
5. 操作状态 scope 写错会影响重复判断和管理端 tag 点亮。
6. 灰度开关覆盖不完整会造成同一赛事数据混用。

## 9. 推荐执行顺序

建议先不要替换现有代码。下一步按以下顺序推进：

1. 完成接口契约确认。
2. 完成 grant 表和基础 service。
3. 完成旁路发证。
4. 完成旁路扫码矩阵。
5. 用新测试数据接口验证。
6. 做后端适配层。
7. 加灰度开关。
8. 使用现有 PC 和小程序页面验证。
9. 验证稳定后再讨论替换旧实现。
