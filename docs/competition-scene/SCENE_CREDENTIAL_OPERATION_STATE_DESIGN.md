# 主体操作状态模型设计

生成时间：2026-07-02  
适用范围：现场证件扫码后的报道、资料领取、候场等主体状态。  
设计阶段：先输出设计文档，不进入编码。

## 1. 设计目标

现有系统把现场操作状态直接写在 `competition_scene_credential` 上：

```text
report_status
material_status
waiting_status
```

这在证件只绑定一个 `schedule_id` 时可以工作，但在引入大赛级证件、直接发证、资料按个人领取、同队代领后会出现问题：

- 同一个主体可能有多张证件。
- 一张大赛级证件不应承载赛场级候场状态。
- 资料领取需要按个人主体去重，而不是按团队证件去重。
- 操作流水适合审计，不适合作为唯一状态源。
- 证件状态字段需要保留兼容，但不应继续作为唯一事实来源。

因此新增主体操作状态表：

```text
competition_scene_subject_operation_state
```

核心思想：

```text
证件是凭据
operation_log 是流水
operation_state 是主体在某个作用域下某项操作是否完成的事实状态
```

## 2. 新增表 competition_scene_subject_operation_state

建议字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `state_id` | bigint | 主键 |
| `competition_series_id` | bigint | 赛事系列 ID |
| `scope_type` | varchar(32) | 状态作用域类型 |
| `scope_ref_id` | bigint | 状态作用域引用 ID |
| `subject_type` | varchar(32) | 主体类型 |
| `subject_code` | varchar(128) | 主体编码 |
| `operation_type` | varchar(32) | 操作类型 |
| `operation_status` | varchar(32) | 操作状态 |
| `operation_time` | datetime | 操作完成时间 |
| `credential_id` | bigint | 本次操作使用的证件 |
| `operator_user_id` | bigint | 操作人用户 ID |
| `operator_name` | varchar(100) | 操作人名称 |
| `delegate_user_id` | bigint | 代领人用户 ID |
| `delegate_name` | varchar(100) | 代领人姓名 |
| `delegate_credential_id` | bigint | 代领人证件 ID |
| `delegate_relation` | varchar(64) | 代领关系 |
| `last_log_id` | bigint | 最近一次成功操作流水 ID |
| `remark` | varchar(500) | 备注 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |
| `deleted` | tinyint | 逻辑删除 |

建议补充审计字段时可按项目惯例加入：

```text
create_by
update_by
```

## 3. 字段语义

### 3.1 competition_series_id

所有现场操作状态都必须归属到赛事系列。

原因：

- 报道是大赛级主体状态。
- 资料领取是大赛级个人主体状态。
- 候场虽然是赛场级，但仍属于某个赛事系列。

### 3.2 scope_type / scope_ref_id

表示状态的去重范围，不一定等于证件作用域。

典型规则：

| 操作 | 状态 scope_type | 状态 scope_ref_id |
| --- | --- | --- |
| `REPORT` | `COMPETITION` | `competition_series_id` |
| `MATERIAL` | `COMPETITION` | `competition_series_id` |
| `WAITING` | `SCHEDULE` | `schedule_id` |
| `REVIEW_SCAN` | 不写状态 | 不写状态 |

### 3.3 subject_type / subject_code

表示被操作主体。

建议主体类型：

```text
TEAM
USER
EXPERT
STAFF
VIP
TEMP
```

主体编码规则：

| 主体类型 | subject_code 建议 |
| --- | --- |
| TEAM | `team_code` |
| USER | `user_id` 字符串 |
| EXPERT | `user_id` 或专家外部编码 |
| STAFF | `user_id` 或工作人员编码 |
| VIP | 贵宾编码 |
| TEMP | 临时证编码 |

第一期注意：

- `MATERIAL` 按个人领取，因此 subject 应为 `USER`。
- 团队证件执行资料领取时，也要解析到被领取个人主体，不应直接写 TEAM 资料状态。
- 第一阶段 `MATERIAL` 的 `subject_type` 永远是 `USER`。
- `MATERIAL.subject_code` 为被领取人的 `userId`；如果没有稳定 userId，才使用稳定成员编码。
- 团队证件只能作为资料领取入口，不能作为资料领取状态主体。
- 不允许写 `TEAM + MATERIAL` 作为状态事实源。

### 3.4 operation_type

建议枚举：

```text
REPORT
MATERIAL
WAITING
```

扫码接口可继续兼容旧操作类型：

```text
REPORT_SIGN -> REPORT
MATERIAL_RECEIVE -> MATERIAL
WAITING_CHECK_IN -> WAITING
```

专家评审入口：

```text
REVIEW_SCAN
```

第一期规则：

- `REVIEW_SCAN` 只写 `competition_scene_operation_log`。
- 不写 `competition_scene_subject_operation_state`。

### 3.5 operation_status

第一期建议：

```text
DONE
CANCELLED
INVALID
```

第一期实际确认操作只写：

```text
DONE
```

后续如果管理端支持撤销或异常处理，可扩展 `CANCELLED / INVALID`。

### 3.6 credential_id

表示触发本次状态完成的证件。

说明：

- 证件只是凭据。
- 状态锁定在主体上。
- 同一个主体后续换证，状态仍可在主体维度保留。

### 3.7 delegate 字段

用于资料代领。

只有 `operation_type = MATERIAL` 且存在代领时写入：

```text
delegate_user_id
delegate_name
delegate_credential_id
delegate_relation
```

本人领取时：

```text
delegate_user_id = subject user_id 或为空
delegate_relation = SELF
```

同队代领时：

```text
delegate_relation = TEAM_MEMBER
```

## 4. 操作状态范围规则

### 4.1 REPORT 是 COMPETITION 级主体状态

报道完成表示主体已经完成大赛现场报到。

状态写入：

```text
competition_series_id = credential.competition_series_id
scope_type = COMPETITION
scope_ref_id = competition_series_id
operation_type = REPORT
```

主体：

- 团队报到可按 `TEAM` 主体。
- 个人报到可按 `USER` 主体。
- 教师报到是否按 `USER` 主体，需要后续业务确认。

去重：

同一大赛、同一主体只能报道一次。

### 4.2 MATERIAL 是 COMPETITION 级个人主体状态

资料领取第一期按个人领取。

状态写入：

```text
competition_series_id = credential.competition_series_id
scope_type = COMPETITION
scope_ref_id = competition_series_id
subject_type = USER
subject_code = 被领取人 user_id
operation_type = MATERIAL
```

说明：

- 即使扫描的是团队证件，也要落到被领取人个人主体。
- `MATERIAL` 操作的 `subject_type` 固定为 `USER`。
- `subject_code` 优先使用被领取人的 `userId` 字符串；无法取得 userId 时才使用稳定成员编码。
- 团队证件只能作为资料领取入口，不是资料领取状态主体。
- 不允许将 `TEAM + MATERIAL` 写入 `operation_state` 作为事实源。
- 支持本人领取。
- 支持同队成员代领。
- 不允许跨队代领。

去重：

同一大赛、同一 `USER` 只能领取一次资料。

### 4.3 WAITING 是 SCHEDULE 级主体状态

候场必须属于具体赛场安排。

状态写入：

```text
competition_series_id = credential.competition_series_id
scope_type = SCHEDULE
scope_ref_id = schedule_id
operation_type = WAITING
```

主体：

- 团队赛可按 `TEAM`。
- 个人赛可按 `USER`。

去重：

同一赛场安排、同一主体只能候场确认一次。

限制：

- `scope_type = COMPETITION` 的证件第一期不允许候场。
- 没有有效 `schedule_id` 的证件不能执行候场。

### 4.4 REVIEW_SCAN 只写 log

专家评审入口第一期不写状态。

原因：

- 当前只是入口提示，不是明确完成状态。
- 专家评审本身属于评审业务，不应在现场证件模块里过早固化状态。

处理：

```text
只写 competition_scene_operation_log
不写 competition_scene_subject_operation_state
```

## 5. 唯一性与去重

要求：

同一组合只能有一个 `DONE`：

```text
competition_series_id
scope_type
scope_ref_id
subject_type
subject_code
operation_type
```

第一阶段不使用包含 `deleted` 的唯一键作为强约束。

原因：

1. `deleted` 参与唯一键会让撤销、重做、历史回填和脏数据修复变复杂。
2. 第一阶段只需要保证同一主体同一操作只有一个 `DONE`。
3. 后续如果需要撤销/重做，应再设计 `active_flag` 或 `status_version`，不在第一期复杂化。

第一阶段建立普通查询索引：

```text
idx_scene_subject_operation_lookup(
  competition_series_id,
  scope_type,
  scope_ref_id,
  subject_type,
  subject_code,
  operation_type,
  operation_status,
  deleted
)
```

唯一性由 Service 层通过事务查询和条件写入保证。

重复操作处理：

1. 先查 `operation_state` 是否已有 `DONE`。
2. 已有 `DONE` 时返回重复提示。
3. 不再写新的业务状态。
4. 可按需写一条 `operation_log`，结果为 `DUPLICATE`。

并发处理建议：

- Service 层使用事务。
- 写状态前按 `idx_scene_subject_operation_lookup` 查询是否已有 `DONE`。
- 未完成时执行条件写入，条件必须再次限定同一 lookup 范围下没有 `DONE`。
- 并发场景下如果条件写入失败，按重复操作处理。
- 第一阶段不引入 `active_flag`、`status_version` 或复杂撤销模型。

## 6. 报道、资料、候场去重口径

### 6.1 报道在大赛级去重

示例：

同一团队有多张赛场级参赛证，只要已完成大赛级报道，后续扫描其他赛场证做报道应提示已报到。

去重键：

```text
competition_series_id + COMPETITION + competition_series_id + subject_type + subject_code + REPORT
```

### 6.2 资料领取在大赛级个人去重

示例：

同一人有多张证件，只能领取一次资料。

去重键：

```text
competition_series_id + COMPETITION + competition_series_id + USER + user_id + MATERIAL
```

### 6.3 候场在赛场级去重

示例：

同一团队在 schedule A 已候场，不代表 schedule B 已候场。

去重键：

```text
competition_series_id + SCHEDULE + schedule_id + subject_type + subject_code + WAITING
```

## 7. 操作成功后的写入顺序

建议成功确认后执行：

1. 解析证件能力和作用域。
2. 解析操作类型。
3. 解析被操作主体。
4. 校验是否允许执行该操作。
5. 查询 `operation_state` 是否已 `DONE`。
6. 未完成则插入或更新 `operation_state` 为 `DONE`。
7. 写 `operation_log`，记录 `PASS`。
8. 兼容更新 `competition_scene_credential` 旧状态字段。

兼容更新字段：

| operation_type | 旧字段 |
| --- | --- |
| `REPORT` | `report_status/report_time/report_operator_id/report_operator_name` |
| `MATERIAL` | `material_status/material_time/material_receiver_name/...` |
| `WAITING` | `waiting_status/waiting_time/waiting_operator_id/waiting_operator_name` |

注意：

- 新状态表是新事实来源。
- 旧字段只为兼容旧页面和旧接口。
- 更新旧字段时，应优先更新被扫证件；如果资料领取按个人主体，后续可考虑同步该主体相关证件。

## 8. 管理端 tag 计算规则

升级后管理端证件列表上的状态 tag 应优先从 `operation_state` 计算。

优先级：

1. 查询 `operation_state`。
2. 如果存在 `DONE`，显示已完成。
3. 如果不存在新状态，回退读取 `competition_scene_credential` 旧状态字段。
4. 如果旧字段也未完成，显示未完成。

这样可以兼容历史数据，也能支持大赛级状态跨证件共享。

## 9. 与 operation_log 的关系

`competition_scene_operation_log` 继续保留。

定位：

```text
operation_log = 审计流水
operation_state = 当前事实状态
```

禁止事项：

- 不把 `operation_log` 作为唯一状态源。
- 不靠查最后一条 log 判断是否已领取。
- 不靠 log 去做强一致去重。

operation_state 中的 `last_log_id` 指向最近一次成功状态变更流水，便于追溯。

## 10. 与 credential 旧状态字段的关系

旧字段不删除：

```text
report_status
material_status
waiting_status
```

新逻辑：

- 写状态时同步更新旧字段。
- 读状态时优先新表，回退旧字段。
- 历史数据可逐步回填到新表。

原因：

- 保护现有页面。
- 保护历史接口。
- 降低一次性改造风险。

## 11. 历史数据回填建议

回填逻辑：

1. 遍历 `competition_scene_credential` 未删除数据。
2. 对 `report_status = 1` 生成 `REPORT` 状态。
3. 对 `material_status = 1` 生成 `MATERIAL` 状态。
4. 对 `waiting_status = 1` 生成 `WAITING` 状态。
5. 遇到冲突时保留最早完成时间或最新更新时间，需要人工确认。

默认作用域：

- 历史报道：按 `COMPETITION`。
- 历史资料：按 `COMPETITION`。
- 历史候场：按 `SCHEDULE`。

## 12. 禁止事项

1. 不用伪赛场安排。
2. 不恢复 `MIXED`。
3. 不把 `credentialNo` 当权限。
4. 不把 `operation_log` 作为唯一状态源。
5. 不删除旧字段。
6. 不破坏现有赛场发证。
7. 不修改报名、支付、成绩、证书主流程。
8. 不连接生产数据库。
