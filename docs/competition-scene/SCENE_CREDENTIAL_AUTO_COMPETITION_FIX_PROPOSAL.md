# 现场证件自动大赛级发证修复方案建议

撰写时间：2026-07-04

本方案基于：

1. 当前代码路径诊断；
2. `jiaoxue_test` 只读数据审计；
3. 当前产品讨论方向：证件本身应更接近身份标志，能力和权限应与证件实体解耦。

本轮不修改代码、不清理数据，仅给出方案建议。

## 1. 当前问题归纳

当前最核心的问题不是单纯脏数据，而是模型边界不清：

1. 赛场发证被赋予了两个职责：生成赛场级证件，同时自动补大赛级证件；
2. 自动大赛级证件没有结构化来源字段，无法可靠追踪来自哪个 schedule/target；
3. 删除 target 只能删除 `target_id` 关联证件，无法处理自动大赛级证件；
4. 大赛级证件复用条件按 `userId` 或 `subjectCode` 过宽，没有约束角色、类型、渠道；
5. 证件字段是快照，target 更新后不会同步；
6. PC/小程序把大赛级证件作为主证件，使“多证”被用户明显感知。

测试库当前没有发现重复有效大赛证、target 删除后残留、快照漂移等脏数据，但已经存在自动大赛级证件和混合展示。这说明风险来自当前设计，而不是只靠数据清洗能解决。

## 2. 方案 A：保留赛场发证自动生成大赛级证件

### 2.1 适用前提

只有在产品明确要求“任何进入赛场配置的人员，都必须自动获得一个大赛总证二维码”时，才建议保留此方案。

### 2.2 必补能力

#### 2.2.1 增加来源字段

在 `competition_scene_credential` 增加：

| 字段 | 用途 |
| --- | --- |
| `source_schedule_id` | 自动大赛级证件来源赛场 |
| `source_target_id` | 自动大赛级证件来源 target |
| `auto_created` | 是否由赛场发证自动创建 |

自动大赛级证件写入：

```text
issue_channel = SCHEDULE_MATCH
scope_type = COMPETITION
schedule_id = NULL
target_id = NULL
source_schedule_id = schedule.scheduleId
source_target_id = target.targetId
auto_created = 1
```

直接发证写入：

```text
issue_channel = COMPETITION_DIRECT
scope_type = COMPETITION
source_schedule_id = NULL
source_target_id = NULL
auto_created = 0
```

#### 2.2.2 删除 target 时处理自动证件

删除 target 时，需要同时处理：

1. `target_id in (...)` 的赛场级证件；
2. `auto_created=1 and source_target_id in (...)` 的自动大赛级证件。

处理策略建议使用软删除或撤销，而不是物理删除：

```text
del_flag = '1'
或 credential_status = 'REVOKED'
```

如果同一自动大赛级证件可能被多个赛场复用，则不能简单删除，需要先检查该用户同大赛下是否还有其他有效 target。这个复杂度是方案 A 的主要成本。

#### 2.2.3 复用条件收窄

当前复用：

```text
competitionSeriesId + userId
或 competitionSeriesId + subjectType + subjectCode
```

建议至少收窄为：

```text
competitionSeriesId
+ scope_type = COMPETITION
+ subject_type
+ subject_code
+ credential_type
+ credential_status = EFFECTIVE
+ del_flag = 0
```

是否区分 `issue_channel` 需要产品决定：

1. 若直接发证和自动发证代表同一张大赛总证，可以不区分渠道；
2. 若直接发证是人工确认后的正式大赛证，自动发证只是系统补全，则必须区分 `issue_channel` 或优先级。

同时建议增加角色一致性校验：

```text
competition_role_name 与 target role 相容
```

#### 2.2.4 前端分组展示

PC/小程序需要明确区分：

1. 大赛总证；
2. 赛场证件；
3. 工作人员/专家/VIP/临时证件。

如果保留自动大赛级证件，前端不应把“自动补发的大赛证”和“人工直接发证的大赛证”混在一个无说明的主证件槽里。至少需要展示来源或隐藏自动大赛证的独立存在感。

#### 2.2.5 历史数据治理

对既有自动大赛级证件，需要用快照回填：

```text
source_schedule_id = credential_snapshot_json.schedule.scheduleId
source_target_id = credential_snapshot_json.target.targetId
auto_created = 1
```

回填前必须先做备份和只读核验。无法解析快照的数据进入人工复核清单。

### 2.3 优点

1. 保留当前 PC/小程序顶部“大赛证二维码”的产品体验；
2. 大赛级报道、资料领取可以天然落在大赛证上；
3. 与已存在的 `COMPETITION_DIRECT` 大赛直接发证模型形式一致。

### 2.4 缺点

1. 赛场发证仍然会产生两张证，违背“证件是身份标志、能力权限解耦”的方向；
2. 删除、复用、展示、历史治理都更复杂；
3. 同一人多角色时仍需大量规则避免误复用；
4. 前端和用户会继续感知到“大赛证 + 赛场证”的双证结构。

## 3. 方案 B：取消赛场发证自动生成大赛级证件

### 3.1 目标模型

赛场发证只生成 `SCHEDULE` 级证件：

```text
issue_channel = SCHEDULE_MATCH
scope_type = SCHEDULE
schedule_id = schedule.scheduleId
target_id = target.targetId
scope_ref_id = schedule.scheduleId
```

大赛级实体证件只通过“大赛级直接发证”页面生成：

```text
issue_channel = COMPETITION_DIRECT
scope_type = COMPETITION
schedule_id = NULL
target_id = NULL
scope_ref_id = competitionSeriesId
```

### 3.2 能力与状态

赛场级 `PARTICIPANT` 证件仍保留现场能力：

```json
{
  "report": true,
  "material": true,
  "waiting": true,
  "review": false,
  "resourceReservation": true,
  "vipAccess": false
}
```

报道和资料领取的状态不必依赖大赛级实体证件。当前 `competition_scene_subject_operation_state` 已经支持按主体写状态：

1. 报道：写 `scope_type=COMPETITION`、`scope_ref_id=competitionSeriesId`；
2. 资料领取：写 `scope_type=COMPETITION`、`scope_ref_id=competitionSeriesId`；
3. 候场：写 `scope_type=SCHEDULE`、`scope_ref_id=scheduleId`。

也就是说，可以用赛场级证件触发大赛级 operation_state，不必为了报道/资料强行生成一张大赛级实体证件。

### 3.3 前端展示

取消自动大赛级证件后，前端应调整为：

1. 有正式大赛级直接发证时，展示大赛总证；
2. 没有正式大赛级证件时，直接展示赛场级证件二维码；
3. 不再提示“暂无大赛证二维码，请联系现场工作人员”，除非产品明确要求必须发大赛总证；
4. 赛场信息仍按 schedule 展示。

本轮不做前端修改，只作为后续修复方向。

### 3.4 既有自动大赛级证件治理

建议先冻结新增，再治理历史：

1. 先停止赛场发证自动生成新的大赛级证件；
2. 用只读 SQL 列出既有自动大赛级证件：

```sql
select credential_id, competition_series_id, user_id, credential_type
from competition_scene_credential
where del_flag = '0'
  and credential_status = 'EFFECTIVE'
  and issue_channel = 'SCHEDULE_MATCH'
  and scope_type = 'COMPETITION';
```

3. 对没有业务操作记录的自动大赛证，建议标记撤销或软删除；
4. 对已经有报道/资料 operation_state 或 operation_log 的自动大赛证，建议保留流水，不删除日志，只将证件状态治理为不再展示；
5. 治理前输出备份表和人工复核清单。

### 3.5 优点

1. 符合“赛场发证只生成赛场证件”的直觉；
2. 证件实体和能力/权限更容易解耦；
3. 删除 target 的逻辑简单可靠；
4. 不再产生自动大赛证残留问题；
5. 不再需要为自动证件补 `source_schedule_id/source_target_id/auto_created` 承担长期复杂度；
6. PC/小程序不再天然出现“大赛证 + 赛场证”的双证困惑。

### 3.6 缺点

1. 需要调整 PC/小程序无大赛证时的展示文案和二维码来源；
2. 如果现场业务确实需要一个全局大赛总证二维码，需要通过直接发证单独发放；
3. 已有 3 张自动大赛级证件需要治理策略。

## 4. 方案对比

| 维度 | 方案 A 保留自动大赛证 | 方案 B 取消自动大赛证 |
| --- | --- | --- |
| 业务直觉 | 一次赛场发证产生两类证件，解释成本高 | 赛场发证只产生赛场证件，直观 |
| 删除 target | 必须新增来源字段和复杂删除规则 | 当前按 target 删除赛场证件即可 |
| 复用风险 | 仍需复杂条件避免跨角色复用 | 大幅降低 |
| 前端感知 | 仍会有大赛证/赛场证双层结构 | 可按赛场证件自然展示 |
| operation_state | 可由大赛证触发 | 可由赛场证触发，不冲突 |
| 历史治理 | 需要回填来源字段 | 只需识别并禁用既有自动证 |
| 与解耦方向 | 较弱 | 较强 |
| 实施复杂度 | 后端、DB、前端、治理都较重 | 后端生成逻辑和前端展示需要调整，模型更轻 |

## 5. 推荐方案

推荐采用方案 B：取消赛场发证自动生成大赛级证件。

理由：

1. 当前数据审计已经证明，自动大赛证会被 PC/小程序明显展示出来，形成用户可见的“双证”问题；
2. 自动大赛证不是必要实体，报道/资料领取可以通过 operation_state 写到 `COMPETITION` 作用域；
3. 赛场级证件已经具备 report/material/waiting/resourceReservation 能力，足以承载现场扫码入口；
4. 删除 target、重生成、复用、历史治理都会显著简化；
5. 更符合前面讨论的方向：证件是身份标志，现场能力和权限由能力矩阵、角色、operation_state 控制。

## 6. 后续建议分阶段

### 阶段 1：停止新增自动大赛级证件

目标：

1. 赛场发证不再调用 `ensureCompetitionCredentialForTarget()`；
2. 只生成 `scope_type=SCHEDULE` 的赛场级证件；
3. 大赛级直接发证入口保持不变。

验收：

1. 新增赛场发证后，不再出现新的 `SCHEDULE_MATCH + COMPETITION`；
2. SCHEDULE 级证件仍可扫码报道、资料领取、候场；
3. operation_log 和 operation_state 仍正常写入。

### 阶段 2：前端展示调整

目标：

1. PC/小程序无正式大赛级证件时，使用赛场级证件二维码；
2. 页面文案从“大赛证二维码”调整为更通用的“现场证件二维码”或按证件 scope 展示；
3. 多赛场时明确展示赛场列表，不误导用户必须有大赛总证。

### 阶段 3：历史自动大赛证治理

目标：

1. 输出既有自动大赛级证件清单；
2. 区分有无操作流水；
3. 对无流水证件撤销或软删除；
4. 对有流水证件保留审计记录，并从用户端展示中隐藏或标记停用。

### 阶段 4：复用条件收窄

即使取消自动大赛证，直接发证仍需避免跨角色复用。建议将大赛级直接发证复用条件调整为：

```text
competitionSeriesId
+ scope_type = COMPETITION
+ subject_type
+ subject_code
+ credential_type
+ credential_status = EFFECTIVE
+ del_flag = 0
```

是否允许同一 subject 同时拥有参赛证、专家证、工作证，应由产品规则明确，而不是由 `userId` 最新一张隐式决定。

## 7. 本轮不执行事项

本轮不执行：

1. 不修改 `CompetitionSceneCredentialServiceImpl`；
2. 不修改 Mapper SQL；
3. 不修改 PC/小程序展示；
4. 不执行任何 `update/delete` 数据治理；
5. 不新增字段；
6. 不提交 migration。

本轮产出仅作为后续决策和开发依据。
