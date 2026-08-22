# 现场证件一证多权分阶段实施任务

撰写时间：2026-07-04

本文件只拆分实施阶段，不代表本轮执行代码或数据库变更。

## 阶段 0：只读审计和目标模型确认

### 改动范围

不改代码、不改数据库，只做审计和产品确认。

### 影响文件

无代码影响。产出设计与审计文档：

1. `SCENE_CREDENTIAL_ONE_CARD_MULTI_GRANT_MODEL.md`
2. `SCENE_CREDENTIAL_ONE_CARD_COMPATIBILITY_PLAN.md`
3. `SCENE_CREDENTIAL_ONE_CARD_DECISION_REVIEW.md`
4. 后续可补充 grant 数据审计 SQL 文档。

### 数据库 migration

无。

### 测试点

1. 统计当前 COMPETITION 证件数量。
2. 统计当前 SCHEDULE 证件数量。
3. 统计同主体多证情况。
4. 统计 operation_state 和 operation_log 对旧 credential 的引用。
5. 确认哪些用户/赛场适合作为试点。

### 回滚策略

无变更，无需回滚。

### 风险

1. 产品目标未确认前进入开发，会造成二次返工。
2. 没有识别历史数据边界，会导致迁移阶段误删或误隐藏数据。

## 阶段 1：新增 grant 表，双写 grant，不改变现有扫码

### 改动范围

1. 新增 `competition_scene_credential_scope_grant` 表。
2. 赛场发证时继续生成现有 SCHEDULE 实体证件。
3. 同时为核心 credential 写入 SCHEDULE grant。
4. 扫码逻辑不切换，仍按现有 credential 路径执行。
5. 增加后台只读审计接口或 SQL，用于比对 grant 与旧 SCHEDULE credential。

### 影响文件

后端可能影响：

1. `CompetitionSceneCredentialServiceImpl.java`
2. `CompetitionSceneCredentialMapper.java`
3. `CompetitionSceneCredentialMapper.xml`
4. 新增 `CompetitionSceneCredentialScopeGrant` domain。
5. 新增 `CompetitionSceneCredentialScopeGrantMapper`。
6. 新增 grant mapper XML。
7. 可选新增 grant service。

前端暂不影响。

### 数据库 migration

新增表：

```text
competition_scene_credential_scope_grant
```

建议索引：

1. `idx_grant_credential`
2. `idx_grant_competition_scope`
3. `idx_grant_source_target`
4. `idx_grant_status`
5. `idx_grant_role`

### 测试点

1. 新增 target 后生成证件，确认旧 SCHEDULE credential 仍生成。
2. 同时确认 grant 生成。
3. 重复生成时 grant 不重复。
4. `regenerate=false` 时幂等。
5. `regenerate=true` 时 grant 刷新策略符合预期。
6. 原扫码报道、资料、候场不受影响。
7. operation_log 正常写入。

### 回滚策略

1. 代码回滚到不写 grant。
2. grant 表保留不读，不影响旧流程。
3. 不删除 grant 表，避免丢审计数据。

### 风险

1. 双写失败导致 SCHEDULE credential 和 grant 不一致。
2. 核心 credential 匹配规则不准，grant 绑错 credential。
3. 幂等约束设计不准，产生重复 grant。

## 阶段 2：扫码 allowedActions 优先查 grant，兼容旧 credential

### 改动范围

1. 扫码动作矩阵构建时优先读取 grant。
2. 报道、资料仍基于 COMPETITION 能力和 operation_state。
3. 候场、赛场入场、资源预约优先基于 SCHEDULE grant。
4. grant 缺失时回退旧 SCHEDULE credential。
5. 日志中记录 grant 命中或 legacy fallback 命中。

### 影响文件

后端可能影响：

1. `CompetitionSceneVerifyServiceImpl.java`
2. `CompetitionSceneVerifyResult.java`
3. `CompetitionSceneScanAction.java`
4. `CompetitionSceneScheduleActionGroup.java`
5. `CompetitionSceneSubjectOperationStateServiceImpl.java`
6. grant mapper/service。

小程序扫码结果页可能需要识别新的 action/group 字段：

1. `old-code-mini/pages/scan/result.vue`

### 数据库 migration

可选：

1. operation_log 增加 `grant_id`。
2. 或先不改表，在 result/context 中记录，不落库。

建议如果要落库，新增：

```text
competition_scene_operation_log.grant_id
```

### 测试点

1. 扫核心 COMPETITION 证件，能返回大赛报到、资料动作。
2. 扫核心证件并指定 schedule，能返回该赛场候场动作。
3. 没有 SCHEDULE grant 的 schedule 不返回候场。
4. grant 缺失但旧 SCHEDULE credential 存在时，legacy fallback 可用。
5. 重复报道/资料/候场仍返回重复状态。
6. 操作成功仍写 operation_state 和 operation_log。
7. 专家评审入口提示不受影响。

### 回滚策略

1. 关闭 grant 优先读取开关。
2. 恢复旧 credential.schedule_id 路径。
3. 保留 grant 双写不读。

### 风险

1. allowedActions 变更影响小程序确认参数。
2. 多赛场 grant 下，如果请求未指定 schedule，可能无法判断候场对象。
3. 操作人角色也需要 grant 化，否则矩阵可能出现权限扩大或缩小。

## 阶段 3：PC / 小程序改为一证多权展示

### 改动范围

1. PC “我的赛事-我的证件”改为一张主二维码 + 赛场权限列表。
2. 小程序“我的参赛证”改为一张主二维码 + 赛场权限列表。
3. 不再把 SCHEDULE credential 展示成独立证件。
4. 报道、资料状态读取 COMPETITION operation_state。
5. 候场状态读取 SCHEDULE operation_state。
6. 赛场列表优先来自 grants，缺失时 fallback 旧 SCHEDULE credentials。

### 影响文件

PC：

1. `old-code-pc/src/views/personal/personaltabs/Competition.vue`
2. `old-code-pc/src/api/personal/index.js`

小程序：

1. `old-code-mini/pages/my-credential/index.vue`
2. `old-code-mini/api/sceneCredential.js`

后端：

1. 可能新增 my credential 聚合 VO。
2. 可能新增 `/myCredential/grants` 或扩展 `myList` 返回结构。

### 数据库 migration

无必需 migration，依赖阶段 1 grant 表。

### 测试点

1. 有核心证件 + 多个 grant：只显示一张二维码，多个赛场权限。
2. 只有旧 SCHEDULE 证件：仍能展示，避免空白。
3. 已报道/已领资料状态正确。
4. 已候场状态按赛场正确显示。
5. 下载二维码仍下载核心证件二维码。
6. 多角色用户展示不混乱。

### 回滚策略

1. 前端回滚到旧多证展示。
2. 后端保留旧 `myList` 返回。
3. grant 数据不影响旧展示。

### 风险

1. 用户对“赛场权限”概念需要 UI 表达清楚。
2. 旧 SCHEDULE 证件隐藏过早可能导致某些用户看不到赛场信息。
3. PC 和小程序展示逻辑需要同步，否则用户体验不一致。

## 阶段 4：停用赛场发证生成 SCHEDULE 实体证件

### 改动范围

1. 赛场发证只确保核心 credential。
2. 赛场发证只写或刷新 SCHEDULE grant。
3. 不再新增 `scope_type=SCHEDULE` 的实体 credential。
4. 扫码和前端必须已经可以基于 grant 工作。

### 影响文件

后端：

1. `CompetitionSceneCredentialServiceImpl.java`
2. `CompetitionSceneCredentialMapper.xml`
3. grant service/mapper。
4. 相关测试用例。

前端：

1. 管理端生成证件结果文案需要调整为“授权成功/新增授权”。
2. PC/小程序已在阶段 3 切换。

### 数据库 migration

无新增表；可选新增 feature flag 配置：

```text
scene_credential_generate_schedule_entity=false
```

### 测试点

1. 新 target 发证后，不产生新的 SCHEDULE credential。
2. 新 target 发证后，产生核心 credential 和 SCHEDULE grant。
3. 扫核心二维码能做该赛场候场。
4. 删除 target 撤销 grant。
5. 重新生成刷新 grant。
6. 原有旧 SCHEDULE credential 仍可 fallback。

### 回滚策略

1. 打开 feature flag，恢复生成 SCHEDULE credential。
2. grant 保留，继续双写。
3. 不清理已生成 grant。

### 风险

1. 扫码或前端如果仍有遗漏依赖 SCHEDULE credential，会暴露缺失。
2. 管理端证件列表数量会下降，需要产品确认口径。
3. 报表或导出若按 SCHEDULE credential 统计，需要同步改为 grant。

## 阶段 5：历史 SCHEDULE 证件治理和隐藏

### 改动范围

1. 将历史 SCHEDULE credential 转为 legacy hidden。
2. PC/小程序不再展示旧 SCHEDULE credential。
3. 后台证件列表可保留 legacy 查询入口。
4. 不删除历史 operation_state。
5. 不删除历史 operation_log。
6. 不物理删除旧 credential。

### 影响文件

后端：

1. 证件列表查询过滤或增加 legacy 标识。
2. 我的证件接口不返回 legacy SCHEDULE credential，或仅用于 fallback 聚合。
3. 审计接口保留。

前端：

1. 管理端证件列表增加 legacy 过滤或说明。
2. PC/小程序不显示 legacy credential。

### 数据库 migration

可选：

1. `competition_scene_credential` 增加 `legacy_flag` 或 `display_status`。
2. 或不加字段，通过 `scope_type=SCHEDULE` 且 grant 已存在判定为 legacy。

建议谨慎新增：

```text
display_status = VISIBLE/HIDDEN
legacy_flag = 0/1
```

### 测试点

1. 旧日志仍能打开并追溯旧 credential。
2. 用户端只看到一张核心证件。
3. 赛场权限来自 grant，数量正确。
4. 旧 SCHEDULE credential 不参与新增扫码动作，除非 fallback 开关开启。
5. 后台可查询 legacy 数据。

### 回滚策略

1. 取消隐藏条件。
2. 用户端恢复 legacy fallback 展示。
3. 不需要恢复数据，因为未物理删除。

### 风险

1. 历史数据治理误判导致用户看不到旧赛场。
2. 报表统计口径变化。
3. 如果没有充分审计 operation_log 引用，可能影响客服追溯。

## 总体里程碑

| 阶段 | 目标 | 是否影响用户 |
| --- | --- | --- |
| 阶段 0 | 审计和确认模型 | 否 |
| 阶段 1 | 双写 grant | 否 |
| 阶段 2 | 扫码优先 grant | 小程序扫码动作可能变化 |
| 阶段 3 | 一证多权展示 | 是，用户端体验变化 |
| 阶段 4 | 停止生成 SCHEDULE 实体证件 | 管理端发证口径变化 |
| 阶段 5 | 历史 SCHEDULE 治理隐藏 | 是，但应只减少多证困惑 |

## 总体回滚原则

1. 每阶段都保留旧数据，不物理删除。
2. grant 先双写不读，再读但 fallback，最后替代。
3. PC/小程序展示改造晚于扫码兼容。
4. 停止生成 SCHEDULE 实体证件必须在扫码和展示都稳定后进行。
5. 历史治理只隐藏，不删除。
