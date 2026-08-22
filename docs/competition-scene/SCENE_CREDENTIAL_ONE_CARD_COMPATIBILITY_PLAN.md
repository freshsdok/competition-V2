# 现场证件一证多权兼容计划

撰写时间：2026-07-04

本轮只做兼容设计，不修改代码、不改数据库、不清理数据、不删除旧 SCHEDULE 证件。

## 1. 当前已有数据

当前现场模块已经存在以下数据和行为：

### 1.1 COMPETITION 证件

来源包括：

1. 大赛级直接发证：`issue_channel=COMPETITION_DIRECT`，`scope_type=COMPETITION`。
2. 赛场发证自动生成：`issue_channel=SCHEDULE_MATCH`，`scope_type=COMPETITION`。

当前作用：

1. PC/小程序作为顶部主证件展示。
2. 大赛报到、资料领取可基于它完成。
3. 扫码时可关联到相关赛场级证件以执行候场等动作。

### 1.2 SCHEDULE 证件

来源：

```text
赛场安排 target -> 生成 scope_type=SCHEDULE 的实体证件
```

当前作用：

1. 保存赛场安排快照。
2. 保存 `schedule_id`、`target_id`。
3. 承载候场、赛场级动作、资源预约等现场能力。
4. PC/小程序展示为赛场明细。

### 1.3 operation_state

`competition_scene_subject_operation_state` 已经承担动作状态事实：

1. 报道：`scope_type=COMPETITION`。
2. 资料领取：`scope_type=COMPETITION`。
3. 候场：`scope_type=SCHEDULE`。

它是从双证模型迁移到一证多权模型的关键基础，因为它已经把“做没做过”从证件实体上抽离出来。

### 1.4 operation_log

`competition_scene_operation_log` 保存扫码和确认流水。兼容计划必须保证：

1. 历史日志不被删除。
2. 历史日志中的 `credential_id/schedule_id/target_id` 仍可追溯。
3. 新模型下可以继续写入日志，并逐步补充 grant 上下文。

## 2. 如何兼容当前 SCHEDULE 级证件

短期不删除、不撤销、不隐藏所有旧 SCHEDULE 证件。

兼容策略：

1. 旧 SCHEDULE 证件继续作为 legacy credential 有效。
2. 新增 grant 表后，从旧 SCHEDULE 证件生成对应 SCHEDULE grant。
3. 扫码 allowedActions 逐步优先读取 grant；grant 不存在时回退旧 SCHEDULE credential。
4. PC/小程序逐步改为展示核心证件 + grant 列表；旧 SCHEDULE credential 只作为 grant 的数据来源或 fallback。

这样可以避免一次性破坏候场、资源预约、代领、已有扫码入口。

## 3. 是否需要保留旧 SCHEDULE 证件作为 legacy credential

需要保留，至少在过渡期保留。

原因：

1. 历史 operation_log 可能引用旧 SCHEDULE credential_id。
2. 历史 operation_state 可能由旧 SCHEDULE credential 触发。
3. PC/小程序当前展示仍依赖旧 SCHEDULE credential 的赛场字段。
4. 扫码确认中已有 `targetCredentialId/scheduleId` 选择逻辑，不能突然失效。
5. 数据治理前无法保证每张旧 SCHEDULE 证件都已成功转换为 grant。

建议定义：

```text
legacy SCHEDULE credential = 历史实体证件，只读兼容，不再作为长期新增目标。
```

## 4. 如何从旧 SCHEDULE 证件生成 grant

旧 SCHEDULE 证件映射为一条 SCHEDULE grant：

| grant 字段 | 来源 |
| --- | --- |
| `credential_id` | 优先关联同主体同大赛 COMPETITION 核心证件；没有则可临时使用旧 SCHEDULE credential 或创建核心证件 |
| `competition_series_id` | 旧 credential.competition_series_id |
| `scope_type` | `SCHEDULE` |
| `scope_ref_id` | 旧 credential.schedule_id |
| `source_type` | `LEGACY_SCHEDULE_CREDENTIAL` 或 `SYSTEM_MIGRATION` |
| `source_schedule_id` | 旧 credential.schedule_id |
| `source_target_id` | 旧 credential.target_id |
| `credential_type` | 旧 credential.credential_type |
| `role_code` | 旧 credential.competition_role_name |
| `ability_json` | 旧 credential.ability_json，必要时补默认 SCHEDULE 能力 |
| `grant_status` | 旧 credential 有效则 `ACTIVE`，撤销则 `REVOKED` |
| `deleted` | 0 |

核心证件匹配顺序建议：

1. 同 `competition_series_id + subject_type + subject_code` 的 COMPETITION credential。
2. 同 `competition_series_id + user_id` 的 COMPETITION credential。
3. 同 `competition_series_id + team_code` 的 COMPETITION credential。
4. 若都没有，过渡期可先不创建核心证件，仅记录待治理清单。

不建议自动盲目创建核心证件，除非产品确认所有旧 SCHEDULE 证件都必须在用户端合并为一张核心证件。

## 5. 过渡期是否双写

有三种策略。

### 5.1 继续生成 SCHEDULE 证件，同时写 grant

做法：

1. 赛场发证仍生成旧 SCHEDULE credential。
2. 同时为核心 credential 写一条 SCHEDULE grant。
3. 扫码仍按旧逻辑，grant 仅用于影子验证和新展示。

优点：

1. 风险最低。
2. 不影响现有扫码。
3. 可对比旧 credential 和新 grant 是否一致。

缺点：

1. 双写期间仍会继续产生旧 SCHEDULE 证件。
2. 用户端如果未改展示，仍可能看到多证。

适合阶段 1。

### 5.2 只写 grant，不生成 SCHEDULE 证件

做法：

1. 赛场发证只确保核心 credential。
2. 写 SCHEDULE grant。
3. 扫码、展示都基于 grant。

优点：

1. 模型最干净。
2. 不再新增旧 SCHEDULE 证件。

缺点：

1. 需要扫码和前端先完成切换。
2. 一次性风险较高。

适合阶段 4，而不是一开始。

### 5.3 继续生成 SCHEDULE 证件，不写 grant

不推荐。

这等于维持现状，不能验证一证多权模型，也不能解决长期复杂度。

## 6. 推荐过渡策略

推荐：

```text
阶段 1：继续生成 SCHEDULE 证件，同时写 grant。
阶段 2：扫码 allowedActions 优先查 grant，旧 SCHEDULE credential fallback。
阶段 3：PC/小程序改为一证多权展示。
阶段 4：停止生成 SCHEDULE 实体证件，只写 grant。
阶段 5：历史 SCHEDULE 证件治理和隐藏。
```

这样不会破坏已通过功能，也能逐步收敛模型。

## 7. 扫码逻辑如何从 credential.schedule_id 切到 grant

当前扫码中，赛场动作常从 credential 本身读取：

1. `credential.schedule_id`；
2. `credential.target_id`；
3. `credential.scope_type`；
4. `credential.ability_json`；
5. `credential.report/material/waiting` 旧状态字段。

目标切换：

### 7.1 扫码定位

二维码仍只定位核心 credential：

```text
qr token -> competition_scene_credential
```

### 7.2 构建动作矩阵

新逻辑：

1. 查询 credential 的 active grants。
2. `COMPETITION` grant 或 credential 基础能力用于报道、资料。
3. `SCHEDULE` grant 用于候场、赛场入场、资源预约。
4. 操作人角色也通过 credential + grant 判断。
5. 如果 grant 缺失，过渡期回退查询旧 SCHEDULE credential。

### 7.3 确认操作

确认候场时：

1. 请求携带 `scheduleId` 或 `grantId`。
2. 后端验证核心 credential 是否有该 SCHEDULE grant。
3. 写 `SCHEDULE operation_state`。
4. operation_log 中记录 credential_id，同时建议记录 schedule_id、target_id、grant_id。

过渡期如果没有 grant，但存在旧 SCHEDULE credential，仍允许按旧逻辑执行。

## 8. PC / 小程序展示如何从多证改为一证多权

目标展示：

```text
一张主证件二维码
  - 大赛状态：已报到/未报到，已领取资料/未领取资料
  - 赛场权限列表：每条 grant 对应一个赛场
  - 特殊身份权限：专家、工作人员、VIP、临时
```

过渡期数据来源：

1. 优先用核心 credential + grants。
2. 如果 grant 接口未就绪，继续读取旧 SCHEDULE credentials 生成赛场列表。
3. 对用户隐藏“这是一张旧 SCHEDULE 证件”的技术概念。

主证件选择：

1. 优先 `scope_type=COMPETITION` 且 `credential_status=EFFECTIVE` 的核心证件。
2. 如果没有核心证件，过渡期可选择最早/最新有效 SCHEDULE credential 作为临时展示二维码。
3. 最终不再把每个 SCHEDULE credential 展示为独立证件。

## 9. 删除 target、更新 target、重新生成证件

### 9.1 删除 target

目标行为：

1. 撤销该 target 产生的 SCHEDULE grant。
2. 不删除核心 credential。
3. 不删除 operation_state 和 operation_log。
4. 旧 SCHEDULE credential 过渡期可软删除或标记 legacy hidden，具体放到阶段 5。

### 9.2 更新 target

按字段性质区分：

1. 身份字段变更：姓名、学校、手机号等，需要决定是否同步核心 credential 展示字段。
2. 赛场字段变更：候场分组、座位、赛场安排，需要更新 grant 快照或重建 grant。
3. 权限字段变更：role、credential_type、ability，需要更新 grant 的 `role_code/credential_type/ability_json`。

不建议仅依赖 credential 主表快照同步，因为赛场授权已经从 credential 抽到 grant。

### 9.3 重新生成证件

未来“生成证件”更准确应拆成两件事：

1. 确保核心 credential 存在。
2. 为 target 写入或刷新 SCHEDULE grant。

如果核心 credential 已存在：

1. 不换二维码 token。
2. 不新建实体证件。
3. 只新增或更新 grant。

如果 grant 已存在：

1. 幂等跳过；
2. 或按 `regenerate=true` 刷新 grant ability 和快照。

## 10. 避免破坏已有功能

### 10.1 报道

保持：

```text
COMPETITION operation_state
```

兼容：

1. 旧 COMPETITION credential 可继续报道。
2. 旧 SCHEDULE credential 扫码时，可解析到核心 credential 或回退旧逻辑。

### 10.2 资料领取

保持：

```text
COMPETITION operation_state
delegate 逻辑不变
```

兼容：

1. 同队代领仍需基于 subject/team 关系判断。
2. 不因隐藏旧 SCHEDULE 证件而丢失代领关系。

### 10.3 候场

保持：

```text
SCHEDULE operation_state
```

切换：

1. 优先用 SCHEDULE grant 判定候场能力。
2. grant 缺失时回退旧 SCHEDULE credential。

### 10.4 大赛级直接发证

保持：

1. 直接发证仍创建核心 COMPETITION credential。
2. 不自动拥有全部 SCHEDULE 权限。
3. 如果需要赛场权限，必须通过 grant 授权。

### 10.5 操作流水

保持：

1. 所有扫码、确认、失败、重复、异常继续写 log。
2. 历史 log 不重写。
3. 新 log 建议逐步补 `grant_id` 或在 `extra` 字段记录 grant 上下文。

## 11. 兼容期风险控制

1. grant 与旧 SCHEDULE credential 双写不一致。
2. 同一 target 多次生成导致重复 grant。
3. 没有核心 COMPETITION credential 的旧 SCHEDULE 数据无法直接合并。
4. 多角色用户可能需要多条 role grant，但仍共用一个二维码。
5. 前端展示切换期间可能出现旧证和新 grant 同时可见。

建议用影子审计降低风险：

1. 双写后每天统计 grant 与 SCHEDULE credential 数量差异。
2. 扫码时记录“grant 命中 / legacy fallback 命中”。
3. 前端切换前，对典型用户做一证多权聚合预览。

## 12. 迁移边界

本兼容计划不做：

1. 不删除旧 SCHEDULE 证件。
2. 不修改历史 operation_state。
3. 不修改历史 operation_log。
4. 不把 COMPETITION 证件授权成所有赛场可用。
5. 不使用伪赛场安排承载大赛能力。
6. 不恢复任何混合临时模型作为长期目标。

长期目标是：

```text
核心 credential 保持一张；
赛场能力来自 grant；
操作完成事实来自 operation_state；
历史 SCHEDULE credential 只作兼容和审计。
```
