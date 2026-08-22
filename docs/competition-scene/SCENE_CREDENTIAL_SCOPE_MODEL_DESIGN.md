# 现场证件作用域与能力配置模型设计

生成时间：2026-07-02  
适用范围：`competition_scene_*` 现场运行模块。  
设计阶段：先输出设计文档，不进入编码。

## 1. 背景与目标

现有现场证件已经形成基础链路：

```text
赛场安排 -> 匹配对象 -> 现场证件 -> 二维码扫码 -> 操作流水
```

现有证件直接绑定：

```text
schedule_id
target_id
```

这套模型适合“某个赛场安排下的参赛证、教师证、专家证”，但不适合以下真实现场场景：

- 大赛级统一报到。
- 大赛级统一资料领取。
- 不依赖某个赛场安排的直接发证。
- 贵宾证、临时证、工作人员证等非赛场参赛对象。
- 一个证件拥有多种能力，但这些能力不一定都属于同一个赛场安排。

本次升级目标：

1. 将证件作用域从“只支持赛场安排”升级为可表达大赛级、赛场级、专家、工作人员、贵宾、临时证等不同作用域。
2. 用 `scope_type + scope_ref_id` 表达证件归属范围。
3. 用 `issue_channel` 表达发证来源。
4. 用 `credential_name` 解耦证件展示名称。
5. 用 `ability_json` 表达证件能力。
6. 保持现有赛场发证和扫码链路可兼容。
7. 不用伪赛场安排解决大赛级发证问题。
8. 不引入 `MIXED`。

## 2. 为什么不能继续把证件直接绑定为赛场级唯一口径

### 2.1 赛场安排不是所有现场行为的天然边界

报道和资料领取常常是大赛级动作：

- 一个选手只需要在大赛现场统一报到一次。
- 资料领取也可能在大赛服务台统一完成。
- 报到地点、资料领取地点可能和具体赛场、考场无关。

如果所有证件都必须绑定 `schedule_id`，系统会被迫把大赛级动作拆到多个赛场安排里，导致重复报到、重复领资料、重复证件和状态不一致。

### 2.2 大赛级直接发证没有合适的 schedule_id

贵宾、工作人员、临时人员、统一资料领取对象，可能没有明确的赛场安排。

如果强制绑定 `schedule_id`，要么无法发证，要么只能创造一个不真实的赛场安排。这会污染业务模型。

### 2.3 状态去重维度不一致

不同操作的去重范围不同：

| 操作 | 合理状态范围 |
| --- | --- |
| 报道 | 大赛级 |
| 资料领取 | 大赛级个人主体 |
| 候场 | 赛场级 |
| 专家评审入口扫码 | 一般只写流水 |
| 设备预约资格 | 可基于赛场或资源规则 |

如果证件只有 `schedule_id`，系统很难表达“报道已完成，但某个赛场仍未候场”。

### 2.4 证件展示名称不应绑定证件类型

当前 `credential_type` 同时承担分类和展示含义。后续可能出现：

- 嘉宾证。
- 媒体证。
- 志愿者证。
- 工作证。
- 临时通行证。
- 某专项活动证。

这些名称不应通过无限扩展 `credential_type` 完成。`credential_type` 应保持结构分类，`credential_name` 用于展示。

## 3. 为什么不用伪赛场安排

本次明确禁止用“伪赛场安排”承载大赛级发证。

原因：

1. 伪赛场安排不是现场真实安排，会让管理端“赛场安排”列表混入非赛场数据。
2. 候场、赛场时间、考场、座位等字段在大赛级证件中无意义。
3. 后续统计会混乱，无法区分真实赛场和虚拟节点。
4. 伪赛场会影响资源预约、候场管理等依赖真实 `schedule_id` 的功能。
5. 伪赛场会让报到和资料领取仍被错误地建模为赛场级状态。
6. 一旦多个大赛级场景都用伪赛场，会产生更多命名、权限、排序、筛选和数据清理问题。

正确做法是将“作用域”显式建模：

```text
scope_type = COMPETITION
scope_ref_id = competition_series_id
```

而不是伪造：

```text
schedule_id = 某个虚拟赛场安排
```

## 4. 为什么取消 MIXED

不设计 `MIXED` 的原因：

1. `MIXED` 含义不稳定，容易成为所有例外场景的兜底桶。
2. 一旦有 `MIXED`，扫码时仍要继续判断它到底是大赛级、赛场级、专家、工作人员还是临时证。
3. `MIXED` 会让状态去重范围不清楚，例如报道是大赛级，候场是赛场级，混在一个 scope 内会产生歧义。
4. `MIXED` 会让权限、页面筛选、统计口径复杂化。
5. 能力差异应由 `ability_json` 表达，作用域差异应由 `scope_type` 表达，二者不应混为一个枚举。

替代原则：

```text
作用域归作用域：scope_type + scope_ref_id
能力归能力：ability_json
展示归展示：credential_name
```

## 5. scope_type 设计

新增字段：

```text
competition_scene_credential.scope_type
competition_scene_credential.scope_ref_id
```

`scope_type` 表示证件作用域类型。`scope_ref_id` 表示该作用域下的引用 ID。

### 5.1 COMPETITION

含义：大赛级证件。

引用：

```text
scope_ref_id = competition_series_id
```

适用场景：

- 大赛级统一报到。
- 大赛级资料领取。
- 大赛级直接发证。
- 不绑定具体赛场安排但属于某一届/某一个赛事系列的人员。

第一期能力：

```text
report = true
material = true
waiting = false
```

明确限制：

- 大赛级证件第一期不允许候场。
- 大赛级证件不应写入赛场级候场状态。

### 5.2 SCHEDULE

含义：赛场级证件。

引用：

```text
scope_ref_id = schedule_id
```

适用场景：

- 由赛场安排匹配对象生成的参赛证。
- 赛场级教师证。
- 赛场级专家证。
- 需要候场、赛场信息、座位号、考场、赛道组别的证件。

第一期能力：

```text
report = true
material = true
waiting = true
resourceReservation = true
```

说明：

- 兼容现有赛场发证。
- 原 `schedule_id` 仍保留。
- 新模型下 `scope_type = SCHEDULE` 且 `scope_ref_id = schedule_id`。

### 5.3 VIP

含义：贵宾证作用域。

引用：

```text
scope_ref_id = competition_series_id 或具体 VIP 活动 ID
```

第一期策略：

- 只做数据结构预留。
- 不开发管理端页面功能。
- 不开发用户端页面功能。
- 不参与现有扫码确认动作。

预留能力：

```text
vipAccess = true
```

### 5.4 EXPERT

含义：专家证作用域。

引用：

```text
scope_ref_id = competition_series_id 或 schedule_id
```

设计说明：

- 专家证可能服务于大赛级评审，也可能服务于某个赛场/分组评审。
- 第一阶段可继续兼容现有赛场专家证。
- 专家评审入口第一阶段仍以扫码提示为主，不强制写主体操作状态。

典型能力：

```text
review = true
```

### 5.5 STAFF

含义：工作人员证作用域。

引用：

```text
scope_ref_id = competition_series_id 或 schedule_id
```

设计说明：

- 工作人员证主要作为扫码操作人身份。
- 可表达签到工作人员、资料工作人员、现场工作人员等。
- 第一阶段可以继续复用现有 `competition_role_name` 识别操作角色。

典型能力：

```text
report = false
material = false
waiting = false
```

工作人员是否具备操作权限，不只看自身 `ability_json`，还要结合操作人角色矩阵。

### 5.6 TEMP

含义：临时证。

引用：

```text
scope_ref_id = competition_series_id 或临时访问上下文 ID
```

适用场景：

- 临时人员。
- 短时通行。
- 补发、应急、临时授权。

第一期策略：

- 仅预留作用域。
- 不优先开发页面。
- 能力必须显式配置，不给默认现场操作能力。

## 6. issue_channel 设计

新增字段：

```text
competition_scene_credential.issue_channel
```

`issue_channel` 表示发证渠道。

### 6.1 SCHEDULE_MATCH

含义：从赛场安排匹配对象发证。

来源：

```text
competition_scene_schedule_target
```

默认规则：

```text
scope_type = SCHEDULE
scope_ref_id = schedule_id
```

### 6.2 COMPETITION_DIRECT

含义：大赛级直接发证。

来源：

- 管理端大赛级发证页面。
- 赛事人员、报名人员、导入名单或手工选择对象。

默认规则：

```text
scope_type = COMPETITION
scope_ref_id = competition_series_id
```

### 6.3 MANUAL

含义：人工手动发证。

适用：

- 临时补发。
- 特殊人员。
- 少量例外对象。

注意：

- 手工发证仍必须明确 `scope_type`。
- 不允许用 `MANUAL` 替代作用域设计。

### 6.4 IMPORT

含义：导入发证。

适用：

- 批量导入工作人员。
- 批量导入嘉宾。
- 外部名单导入。

注意：

- 导入数据仍必须映射到明确的 `scope_type`。
- 导入发证可跳过报名一致性校验，但需要保留导入快照。

## 7. credential_type 与 credential_name 解耦

新增字段：

```text
competition_scene_credential.credential_name
```

### 7.1 credential_type

用于结构分类和规则判断。

建议保留相对稳定的枚举：

```text
PARTICIPANT
TEACHER
EXPERT
STAFF
VIP
TEMP
```

历史兼容：

```text
COMPETITOR -> PARTICIPANT
```

### 7.2 credential_name

用于页面展示和打印标题。

示例：

```text
参赛证
教师证
专家证
工作证
嘉宾证
贵宾证
临时通行证
总决赛参赛证
颁奖典礼嘉宾证
```

规则：

1. 页面优先展示 `credential_name`。
2. 如果 `credential_name` 为空，则根据 `credential_type` 兜底展示。
3. 不再通过新增 `credential_type` 满足所有展示名称。
4. `credential_type` 用于规则，`credential_name` 用于展示。

## 8. ability_json 第一阶段设计

新增字段：

```text
competition_scene_credential.ability_json
```

第一阶段固定 JSON 结构：

```json
{
  "report": true,
  "material": true,
  "waiting": false,
  "review": false,
  "resourceReservation": false,
  "vipAccess": false
}
```

第一阶段约束：

1. 六个字段必须显式存在。
2. 后端解析缺失字段时一律按 `false` 处理。
3. 前端不直接信任 `ability_json` 作权限判断。
4. 扫码页最终可执行动作以后端返回的 `allowedActions / availableActions` 为准。
5. `ability_json` 只表达证件具备的业务能力，不表达操作人是否有权执行确认。

字段说明：

| 字段 | 含义 |
| --- | --- |
| `report` | 是否支持报道签到 |
| `material` | 是否支持资料领取 |
| `waiting` | 是否支持候场确认 |
| `review` | 是否支持专家评审入口 |
| `resourceReservation` | 是否可作为设备资源预约资格 |
| `vipAccess` | 是否支持贵宾通行/贵宾能力，第一期预留 |

设计原则：

1. 能力只表示“这个证件是否具备该类能力”。
2. 能力不直接表示“是否已经完成操作”。
3. 操作完成状态由 `competition_scene_subject_operation_state` 表示。
4. 扫码动作矩阵需要同时看证件能力、作用域和操作人角色。
5. 大赛级证件第一期即使手工配置了 `waiting=true`，后端也应拒绝或忽略候场能力。

第一阶段默认能力矩阵：

| 场景 | report | material | waiting | review | resourceReservation | vipAccess |
| --- | --- | --- | --- | --- | --- | --- |
| `SCHEDULE + PARTICIPANT` | true | true | true | false | true | false |
| `COMPETITION + PARTICIPANT` | true | true | false | false | false | false |
| `TEACHER` | true | false | false | false | false | false |
| `EXPERT` | true | false | false | true | false | false |
| `STAFF` | false | false | false | false | false | false |
| `VIP` | true | false | false | false | false | true |

说明：

- `STAFF` 默认不具备“被扫码后写状态”的业务能力。
- 工作人员扫码操作权限由操作人角色矩阵决定，不由工作人员证自身 `ability_json` 直接决定。
- `VIP` 第一阶段只做结构预留，不开发页面功能。

## 9. 赛场发证默认规则

赛场安排匹配对象发证保持兼容。

默认字段：

```text
issue_channel = SCHEDULE_MATCH
scope_type = SCHEDULE
scope_ref_id = schedule_id
credential_name = 按 credential_type 默认生成
```

默认能力按 `SCHEDULE + PARTICIPANT` 处理：

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

教师证默认能力：

```json
{
  "report": true,
  "material": false,
  "waiting": false,
  "review": false,
  "resourceReservation": false,
  "vipAccess": false
}
```

专家证默认能力：

```json
{
  "report": true,
  "material": false,
  "waiting": false,
  "review": true,
  "resourceReservation": false,
  "vipAccess": false
}
```

工作人员证默认能力：

```json
{
  "report": false,
  "material": false,
  "waiting": false,
  "review": false,
  "resourceReservation": false,
  "vipAccess": false
}
```

说明：

- 工作人员证的能力不是被扫后的业务能力，而更多是操作人身份凭据。
- 工作人员能做哪些操作仍由扫码角色矩阵和后端权限判断。

## 10. 大赛级直接发证默认规则

大赛级直接发证不依赖 `competition_scene_schedule`。

默认字段：

```text
issue_channel = COMPETITION_DIRECT
scope_type = COMPETITION
scope_ref_id = competition_series_id
schedule_id = null
target_id = null 或大赛级发证对象 ID
credential_name = 参赛证/工作证/嘉宾证等展示名
```

默认能力：

```json
{
  "report": true,
  "material": true,
  "waiting": false,
  "review": false,
  "resourceReservation": false,
  "vipAccess": false
}
```

限制：

1. 大赛级证件第一期只支持报道和资料领取。
2. 大赛级证件不允许候场。
3. 大赛级证件不直接作为设备资源预约资格。
4. 如果后续需要资源预约资格，应另行设计资源预约对大赛级证件的识别规则。

第一期大赛级直接发证只做最小功能：

1. 选择赛事。
2. 选择证件类型。
3. 填写证件展示名称。
4. 选择或录入发证对象。
5. 生成 `scope_type = COMPETITION` 的证件。
6. 默认 `ability_json` 只开启 `report/material`。
7. 不做复杂批量导入。
8. 不做贵宾证页面。
9. 不做媒体证、临时证页面。
10. 不做复杂人员库管理。

## 11. 大赛级证件不允许候场

候场是典型赛场级状态，必须知道具体：

- 赛场安排。
- 候场开始/结束时间。
- 候场地点。
- 候场分组。
- 赛道/组别/考场。

因此第一期规则：

```text
scope_type = COMPETITION 的证件不能执行 WAITING_CHECK_IN
```

即使 `ability_json.waiting = true`，后端也应拒绝，返回明确错误。

候场只能基于：

```text
scope_type = SCHEDULE
scope_ref_id = schedule_id
```

## 12. 贵宾证预留规则

第一期仅做结构预留。

建议字段：

```text
credential_type = VIP
credential_name = 贵宾证
scope_type = VIP
scope_ref_id = competition_series_id
issue_channel = MANUAL 或 IMPORT
ability_json = {"report": true, "material": false, "waiting": false, "review": false, "resourceReservation": false, "vipAccess": true}
```

第一期不做：

- 贵宾证管理页面。
- 贵宾证扫码通行页面。
- 贵宾权限点。
- 贵宾通行状态。

后续如开发贵宾能力，应避免复用报道、资料、候场状态，单独设计 `VIP_ACCESS` 类操作。

## 13. 历史数据兼容策略

### 13.1 字段兼容

不删除旧字段：

```text
schedule_id
target_id
credential_type
report_status
material_status
waiting_status
```

新增字段：

```text
issue_channel
scope_type
scope_ref_id
credential_name
ability_json
```

### 13.2 历史证件回填

对已有证件执行回填：

```text
scope_type = SCHEDULE
scope_ref_id = schedule_id
issue_channel = SCHEDULE_MATCH
credential_name = 按 credential_type 映射
```

能力回填：

- 参赛证/历史 COMPETITOR：按 `SCHEDULE + PARTICIPANT` 回填，`report/material/waiting/resourceReservation = true`。
- 教师证：`report = true`，`material/waiting/resourceReservation = false`。
- 专家证：`report = true`，`review = true`，其他默认 false。
- 工作人员证：作为操作人身份，业务能力默认 false。
- 贵宾证：第一期只做结构预留，`report = true`，`vipAccess = true`，其他默认 false。

### 13.3 历史状态兼容

新增 `competition_scene_subject_operation_state` 后：

1. 新操作成功时写新状态表。
2. 同时兼容更新旧 `credential` 状态字段。
3. 管理端展示 tag 优先读新状态表。
4. 新状态不存在时回退读旧字段。
5. 可通过一次性回填任务把旧 `report_status/material_status/waiting_status` 转换为 operation_state。

### 13.4 历史接口兼容

现有接口保持可用：

```text
GET /competition/sceneCredential/list
POST /competition/sceneCredential/generate
GET /competition/userCompetition/sceneCredential/myList
POST /competition/sceneVerify/scan
POST /competition/sceneVerify/confirm
```

新增字段在响应中返回，但不要求旧前端立即使用。

## 14. 禁止事项

1. 不用伪赛场安排。
2. 不恢复 `MIXED`。
3. 不把 `credentialNo` 当权限。
4. 不把 `operation_log` 作为唯一状态源。
5. 不删除旧字段。
6. 不破坏现有赛场发证。
7. 不修改报名、支付、成绩、证书主流程。
8. 不连接生产数据库。
