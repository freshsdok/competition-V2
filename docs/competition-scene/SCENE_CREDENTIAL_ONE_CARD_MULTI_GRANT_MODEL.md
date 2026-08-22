# 现场证件一证多权模型设计

撰写时间：2026-07-04

本轮只做目标模型设计，不修改代码、不改数据库、不清理数据。

## 1. 为什么从双证模型调整为一证多权模型

当前现场证件模型已经出现了两个方向的拉扯：

1. 大赛级证件适合表达“这个主体是谁，属于哪个大赛，能不能进行大赛报到、资料领取、身份核验”。
2. 赛场级证件适合表达“这个主体是否被授权进入某个赛场，能不能候场、入场、预约资源”。
3. 但如果把二者都做成实体证件，赛场发证会让用户看到“大赛证 + 赛场证”两张证，产品理解成本高。
4. 删除匹配对象时，赛场证件和自动大赛证件生命周期不同，容易残留。
5. 同一人多角色、多赛场、多权限时，继续叠加实体证件会让复用、展示、扫码矩阵越来越复杂。

一证多权模型把这两个概念拆开：

1. 证件只有一张，负责识别主体。
2. 权限可以有多条，负责说明这张证在什么范围内能做什么。
3. 操作状态独立记录，负责说明事情是否已经做过。

这样既保留“大赛级身份凭证”的直觉，也避免“每进入一个赛场就多一张实体证件”的体验。

## 2. 核心原则

### 2.1 证件识别人

核心现场证件回答：

```text
这是谁？
属于哪场大赛？
二维码是否有效？
主体身份是什么？
```

核心证件不应该天然回答：

```text
这个人能进入哪些赛场？
这个人能预约哪些资源？
这个人是否已经候场？
```

这些问题交给 grant 和 operation_state。

### 2.2 权限决定能做什么

权限授权回答：

```text
这张证在某个 scope 下被授予了什么能力？
```

例如：

1. `scope_type=COMPETITION`：允许大赛报到、资料领取、身份核验。
2. `scope_type=SCHEDULE + scope_ref_id=scheduleId`：允许某个赛场的候场、入场、资源预约。
3. `scope_type=EXPERT/STAFF/VIP/TEMP`：允许特定身份或特定区域的动作。

同一张证件可以有多条 grant。

### 2.3 operation_state 记录做没做过

`competition_scene_subject_operation_state` 回答：

```text
这个主体在这个 scope 下，这个操作是否已经完成？
```

它不代表权限，也不代表证件实体。

建议继续保持：

1. 报道写 `COMPETITION` 级 operation_state。
2. 资料领取写 `COMPETITION` 级 operation_state。
3. 候场写 `SCHEDULE` 级 operation_state。
4. 资源预约如果需要状态沉淀，也按具体资源或赛场 scope 记录。

## 3. 证件主表的新职责

`competition_scene_credential` 在一证多权模型下应承担核心身份凭证职责：

1. 存储二维码 token、证件编号、证件状态。
2. 存储大赛主体：`competition_series_id`、`subject_type`、`subject_code`、`user_id/team_code/member_id`。
3. 存储主体展示信息：姓名、团队、学校、机构、联系方式脱敏基础等。
4. 存储核心证件类型和展示名：如参赛证、工作证、专家证。
5. 存储基础大赛能力：身份核验、报到、资料领取等。
6. 不再作为赛场权限的唯一来源。
7. 不再为了每个赛场生成一张实体证件。

建议新的职责边界：

| 内容 | 放在 credential | 放在 grant | 放在 operation_state |
| --- | --- | --- | --- |
| 二维码 token | 是 | 否 | 否 |
| 主体身份 | 是 | 可冗余少量索引 | 是，作为状态主体 |
| 大赛归属 | 是 | 是 | 是 |
| 赛场授权 | 否 | 是 | 否 |
| ability_json | 基础能力 | scope 授权能力 | 否 |
| 报道是否完成 | 否，可兼容旧字段 | 否 | 是 |
| 资料是否领取 | 否，可兼容旧字段 | 否 | 是 |
| 候场是否完成 | 否，可兼容旧字段 | 否 | 是 |
| 操作流水 | 否 | 否 | log/state |

## 4. 证件权限授权表设计

新增授权表用于表达“某张核心证件在某个现场范围下拥有哪些能力”。

建议表名：

```text
competition_scene_credential_scope_grant
```

该表是模型关键，不是简单扩展字段。它承担赛场安排、工作人员授权、专家授权、VIP 授权、临时授权等统一授权入口。

## 5. competition_scene_credential_scope_grant 字段建议

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `grant_id` | bigint PK | 授权 ID |
| `credential_id` | bigint | 核心现场证件 ID |
| `competition_series_id` | bigint | 大赛 ID，便于查询和隔离 |
| `scope_type` | varchar(32) | 授权范围，如 `COMPETITION`、`SCHEDULE`、`STAFF`、`EXPERT`、`VIP`、`TEMP` |
| `scope_ref_id` | bigint | 授权范围引用 ID。赛场授权为 `schedule_id`，大赛授权为 `competition_series_id` |
| `source_type` | varchar(32) | 来源，如 `SCHEDULE_TARGET`、`COMPETITION_DIRECT`、`MANUAL`、`IMPORT`、`SYSTEM_MIGRATION` |
| `source_schedule_id` | bigint | 来源赛场 ID，可空 |
| `source_target_id` | bigint | 来源 target ID，可空 |
| `credential_type` | varchar(32) | 授权时的证件类型快照，如 `PARTICIPANT`、`STAFF`、`EXPERT` |
| `role_code` | varchar(64) | 授权角色，如 `MEMBER`、`CAPTAIN`、`CHECKIN_STAFF`、`MATERIAL_STAFF`、`EXPERT` |
| `ability_json` | text/json | 该授权范围下的能力 |
| `grant_status` | varchar(32) | `ACTIVE`、`REVOKED`、`EXPIRED`、`SUSPENDED` |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |
| `deleted` | tinyint | 0 正常、1 删除 |

可选扩展字段：

| 字段 | 说明 |
| --- | --- |
| `valid_from` | 授权开始时间 |
| `valid_to` | 授权结束时间 |
| `grant_snapshot_json` | 授权时 schedule/target 快照 |
| `create_by` / `update_by` | 审计字段 |
| `remark` | 备注 |

建议唯一约束：

```text
credential_id
+ scope_type
+ scope_ref_id
+ source_type
+ source_target_id
+ deleted
```

或更严格：

```text
competition_series_id
+ credential_id
+ scope_type
+ scope_ref_id
+ credential_type
+ role_code
+ deleted
```

具体约束要根据“同一赛场是否允许同一人多角色授权”决定。

## 6. COMPETITION 基础能力和 SCHEDULE 授权能力

### 6.1 COMPETITION 基础能力

核心证件默认拥有或通过基础 grant 拥有大赛级能力：

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

解释：

1. 大赛级证件可以核验身份。
2. 大赛级证件可以进行大赛报到。
3. 大赛级证件可以进行大赛资料领取。
4. 大赛级证件不天然拥有候场、赛场入场、资源预约能力。

### 6.2 SCHEDULE 授权能力

赛场安排给核心证件增加一条 `SCHEDULE` grant：

```json
{
  "identityVerify": true,
  "report": false,
  "material": false,
  "waiting": true,
  "scheduleEntry": true,
  "resourceReservation": true,
  "review": false,
  "vipAccess": false
}
```

也可以按业务配置调整：

1. 只授权候场，不授权资源预约。
2. 只授权入场，不授权候场。
3. 资源预约按赛场、资源类型、时间段二次约束。

### 6.3 专家、工作人员等授权能力

专家 grant：

```json
{
  "identityVerify": true,
  "review": true,
  "report": true,
  "material": false,
  "waiting": false,
  "scheduleEntry": false,
  "resourceReservation": false
}
```

工作人员 grant：

```json
{
  "identityVerify": true,
  "operateReport": true,
  "operateMaterial": true,
  "operateWaiting": true,
  "scanMatrixOperate": true
}
```

工作人员是否按赛场授权，应由 `scope_type=SCHEDULE` 或专门角色 grant 控制，不能简单由大赛级工作证天然获得全部赛场操作权。

## 7. 为什么不能让 COMPETITION 证件天然拥有所有 SCHEDULE 权限

不能让大赛证自动拥有全部赛场权限，原因如下：

1. 一个大赛可能有多个赛场、批次、组别、候场区和资源池。
2. 同一参赛者可能只属于其中一个或几个赛场。
3. 候场和资源预约是现场秩序能力，不是身份能力。
4. 工作人员、专家、参赛者在不同赛场的动作权限不同。
5. 若大赛证天然拥有全部 SCHEDULE 权限，扫码矩阵无法判断该主体是否真的被安排到当前赛场。
6. 删除或调整赛场 target 时无法收回具体赛场权限。
7. 资源预约会被放大成全大赛可预约，破坏容量和排期控制。

因此：

```text
COMPETITION 证明你属于这场大赛；
SCHEDULE grant 证明你被授权参与某个赛场。
```

## 8. 现场动作判定

### 8.1 报道

判定输入：

1. 扫描到核心 credential。
2. credential 有效且属于当前大赛。
3. 操作人有报道操作 grant 或角色。
4. 被扫主体有 `COMPETITION` 基础能力 `report=true`。
5. `operation_state` 中未存在同主体同大赛报道完成记录。

状态写入：

```text
scope_type = COMPETITION
scope_ref_id = competition_series_id
operation_type = REPORT
subject_type/subject_code = credential 主体
```

### 8.2 资料领取

判定输入：

1. credential 有效。
2. 被扫主体有 `COMPETITION` 基础能力 `material=true`。
3. 操作人有资料发放 grant 或角色。
4. 代领逻辑按当前 subject、team、delegate credential 继续校验。

状态写入：

```text
scope_type = COMPETITION
scope_ref_id = competition_series_id
operation_type = MATERIAL
```

### 8.3 候场

判定输入：

1. credential 有效。
2. 请求或扫码上下文能确定 `schedule_id`。
3. credential 存在 `scope_type=SCHEDULE + scope_ref_id=schedule_id` 的 active grant。
4. 该 grant 的 `ability_json.waiting=true`。
5. 操作人有候场确认权限。
6. `operation_state` 中未存在同主体同赛场候场完成记录。

状态写入：

```text
scope_type = SCHEDULE
scope_ref_id = schedule_id
operation_type = WAITING
```

### 8.4 资源预约

判定输入：

1. credential 有效。
2. 存在当前赛场或资源范围的 active grant。
3. grant 的 `ability_json.resourceReservation=true`。
4. 资源开放、容量、时间段、主体限制继续由资源模块判断。

资源预约不应只看大赛级 credential，否则会绕过赛场授权。

### 8.5 专家扫码和评审入口

专家有两类判定：

1. 操作人是否有专家 grant 或专家角色。
2. 被扫对象是否是可评审主体，或者被扫证件是否有评审入口能力。

专家入口可以由：

```text
operator grant: EXPERT/review=true
target grant: SCHEDULE 或 COMPETITION 下 reviewTarget=true
```

共同决定。

正式评审页面不在本模型中实现，但入口提示应基于 grant 而不是基于实体证件数量。

## 9. 删除 target 时撤销 grant，而不是删除 credential

删除匹配对象时，应撤销由该 target 产生的赛场授权：

```text
competition_scene_credential_scope_grant
where source_type = SCHEDULE_TARGET
  and source_target_id in (...)
  and scope_type = SCHEDULE
  and deleted = 0
```

建议处理：

```text
grant_status = REVOKED
deleted = 1 或保留 deleted=0 仅改状态
```

不应直接删除核心 credential，原因：

1. 核心证件属于主体和大赛，不属于单个 target。
2. 同一主体可能还有其他赛场 grant。
3. 核心证件可能已有报道、资料领取、日志流水。
4. 直接删除 credential 会破坏 PC/小程序主证件和历史扫码追溯。
5. 直接删除 credential 可能误删大赛直接发证结果。

只有当该核心 credential 是系统自动创建、没有任何有效 grant、没有 operation_state/log、没有人工直接发证来源时，才可以进入后续治理清单。

## 10. PC / 小程序一证多权展示

展示结构建议：

```text
我的参赛证
  - 顶部：一张核心现场证件二维码
  - 状态：大赛报到、资料领取
  - 赛场权限列表：
      - 赛场 A：候场时间、地点、状态、资源预约入口
      - 赛场 B：候场时间、地点、状态、资源预约入口
  - 特殊授权：
      - 专家评审入口
      - 工作人员扫码入口
      - VIP/临时权限
```

展示原则：

1. 用户只看到一张主二维码。
2. 赛场不是另一张证，而是这张证上的授权。
3. 没有 SCHEDULE grant 的赛场不展示候场/资源预约入口。
4. 报道、资料状态展示来自 `COMPETITION operation_state`。
5. 候场状态展示来自 `SCHEDULE operation_state`。
6. 历史 SCHEDULE 证件在过渡期可隐藏为 legacy 来源，不作为主证件展示。

## 11. 模型摘要

一证多权模型可以用一句话概括：

```text
一张 credential 证明主体身份，多条 grant 证明授权范围，多条 operation_state 证明动作事实。
```

这比双证模型更接近现场业务本质，也为后续赛场入场、资源预约、专家评审、工作人员权限矩阵留下了统一扩展点。
