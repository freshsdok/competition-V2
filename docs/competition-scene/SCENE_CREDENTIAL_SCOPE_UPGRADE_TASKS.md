# 现场证件作用域、能力配置和主体操作状态升级任务拆分

生成时间：2026-07-02  
适用范围：`competition_scene_*` 现场运行模块。  
阶段要求：本文件只拆分后续开发任务，不进入编码。

## 1. 总体原则

本次升级围绕：

```text
scope_type + issue_channel + credential_name + ability_json + operation_state
```

核心目标：

1. 支持大赛级证件。
2. 支持赛场级证件兼容旧流程。
3. 支持大赛级报道和资料领取。
4. 支持资料个人领取和同队代领。
5. 支持贵宾证结构预留。
6. 将操作状态从证件本体拆到主体状态表。

明确禁止：

1. 不用伪赛场安排。
2. 不恢复 `MIXED`。
3. 不把 `credentialNo` 当权限。
4. 不把 `operation_log` 作为唯一状态源。
5. 不删除旧字段。
6. 不破坏现有赛场发证。
7. 不修改报名、支付、成绩、证书主流程。
8. 不连接生产数据库。

## A. 数据库 migration

### A1. competition_scene_credential 增加字段

新增字段：

```text
issue_channel
scope_type
scope_ref_id
credential_name
ability_json
```

建议说明：

| 字段 | 说明 |
| --- | --- |
| `issue_channel` | 发证渠道：`SCHEDULE_MATCH/COMPETITION_DIRECT/MANUAL/IMPORT` |
| `scope_type` | 作用域类型：`COMPETITION/SCHEDULE/VIP/EXPERT/STAFF/TEMP` |
| `scope_ref_id` | 作用域引用 ID |
| `credential_name` | 证件展示名称 |
| `ability_json` | 能力配置 JSON |

索引建议：

```text
idx_scene_credential_scope(scope_type, scope_ref_id)
idx_scene_credential_issue(issue_channel)
idx_scene_credential_name(credential_name)
```

### A2. 新增 competition_scene_subject_operation_state

字段：

```text
state_id
competition_series_id
scope_type
scope_ref_id
subject_type
subject_code
operation_type
operation_status
operation_time
credential_id
operator_user_id
operator_name
delegate_user_id
delegate_name
delegate_credential_id
delegate_relation
last_log_id
remark
create_time
update_time
deleted
```

建议按项目惯例补充：

```text
create_by
update_by
```

第一阶段不建立包含 `deleted` 的唯一键作为强约束。

普通查询索引建议：

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

普通索引：

```text
idx_scene_subject_operation_series(competition_series_id)
idx_scene_subject_operation_scope(scope_type, scope_ref_id)
idx_scene_subject_operation_subject(subject_type, subject_code)
idx_scene_subject_operation_credential(credential_id)
idx_scene_subject_operation_delegate(delegate_user_id)
idx_scene_subject_operation_log(last_log_id)
```

唯一性策略：

- 第一阶段由 Service 层通过事务查询和条件写入保证同一主体同一操作只有一个 `DONE`。
- 写入前按 `idx_scene_subject_operation_lookup` 查询。
- 条件写入失败按重复操作处理。
- 未来如需要撤销/重做，再设计 `active_flag` 或 `status_version`。

### A3. 历史数据回填 SQL

回填：

```text
scope_type = SCHEDULE
scope_ref_id = schedule_id
issue_channel = SCHEDULE_MATCH
credential_name = 按 credential_type 映射
ability_json = 按 credential_type 和历史规则生成
```

`ability_json` 第一阶段固定六字段，缺失字段按 false：

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

旧状态回填：

- `report_status = 1` -> `REPORT`。
- `material_status = 1` -> `MATERIAL`。
- `waiting_status = 1` -> `WAITING`。

要求：

- 生成 migration 文件，不执行生产数据库。
- 回填 SQL 必须可重复检查。
- 冲突处理策略写入报告。

## B. 后端 Domain / Mapper / Service

### B1. Domain

新增/扩展：

```text
CompetitionSceneCredential
CompetitionSceneSubjectOperationState
CompetitionSceneSubjectOperationStateQuery
CompetitionSceneSubjectOperationStateVO
CompetitionSceneCredentialAbility
CompetitionSceneCredentialScope
```

### B2. Mapper

新增：

```text
CompetitionSceneSubjectOperationStateMapper.java
CompetitionSceneSubjectOperationStateMapper.xml
```

实现：

- 按 scope + subject + operation 查询状态。
- 插入 DONE 状态。
- 更新状态。
- 查询用户/团队证件展示状态。
- 管理端按证件批量查询状态 tag。

### B3. Service

新增或扩展：

```text
ICompetitionSceneSubjectOperationStateService
CompetitionSceneSubjectOperationStateServiceImpl
```

能力：

- 解析操作状态作用域。
- 写入状态。
- 重复检测。
- 历史字段兼容更新。
- 查询状态摘要。
- 通过事务和条件写入保证同一主体同一操作只有一个 `DONE`，第一阶段不依赖含 `deleted` 的唯一键。

## C. 发证逻辑改造

### C1. 赛场发证保持兼容

改造：

```text
CompetitionSceneCredentialServiceImpl.generateCompetitionSceneCredential
```

赛场发证默认：

```text
issue_channel = SCHEDULE_MATCH
scope_type = SCHEDULE
scope_ref_id = schedule_id
ability_json = report/material/waiting/resourceReservation
```

要求：

- 不破坏现有 `POST /competition/sceneCredential/generate`。
- 旧前端不传新增字段也可正常生成。

### C2. 大赛级直接发证

新增后端能力：

```text
POST /competition/sceneCredential/competitionDirect/generate
```

第一期支持：

- 选择 `competition_series_id`。
- 选择赛事。
- 选择证件类型。
- 填写证件展示名称。
- 选择或录入发证对象。
- 生成 `scope_type = COMPETITION` 证件。
- 默认 `ability_json` 只开启 `report/material`。
- 禁止 `waiting = true`。
- 不做复杂批量导入。
- 不做贵宾证页面。
- 不做媒体证、临时证页面。
- 不做复杂人员库管理。

### C3. 贵宾证预留

后端枚举和字段支持：

```text
credential_type = VIP
scope_type = VIP
ability_json.vipAccess = true
```

第一期不开发页面。

## D. 扫码 scan/confirm 改造

### D1. scan 改造

改造：

```text
CompetitionSceneVerifyServiceImpl.scan
```

新增校验：

- 解析证件 `scope_type`。
- 解析 `ability_json`。
- 根据作用域决定可执行动作。
- 大赛级证件不返回候场动作。
- 贵宾证第一期不返回业务动作。
- 操作人角色仍需校验。

### D2. confirm 改造

改造：

```text
CompetitionSceneVerifyServiceImpl.confirm
```

新增流程：

1. scan 通过。
2. 校验证件能力。
3. 解析状态作用域。
4. 解析被操作主体。
5. 查询 operation_state 去重。
6. 写 operation_state。
7. 写 operation_log。
8. 兼容更新 credential 旧字段。

## E. 报道/资料/候场 operation_state 写入

### E1. REPORT

范围：

```text
COMPETITION
```

去重：

```text
competition_series_id + subject_type + subject_code + REPORT
```

### E2. MATERIAL

范围：

```text
COMPETITION
```

主体：

```text
USER
```

第一阶段固定口径：

- `MATERIAL` 的 `subject_type` 永远是 `USER`。
- `subject_code` 为被领取人的 `userId`；没有稳定 userId 时才使用稳定成员编码。
- 团队证件只能作为资料领取入口。
- 不允许写 `TEAM + MATERIAL` 作为状态事实源。

去重：

```text
competition_series_id + USER + user_id + MATERIAL
```

### E3. WAITING

范围：

```text
SCHEDULE
```

去重：

```text
competition_series_id + schedule_id + subject_type + subject_code + WAITING
```

限制：

- 大赛级证件不允许候场。
- 无 `schedule_id` 不允许候场。

## F. 资料代领

实现内容：

1. 发资料工作人员先扫被领取人证件。
2. 小程序展示“本人领取”和“同队代领”。
3. 选择本人领取时直接确认。
4. 选择同队代领时，需要再扫代领人证件。
5. confirm 请求支持 `subjectUserId/delegateCredentialToken/delegateQrContent`。
6. 后端通过代领人证件解析 `delegateUserId/delegateName/delegateCredentialId`。
7. 后端校验代领人与被领取人属于同一 `team_code`。
8. 校验通过后写入 `subject = 被领取人`、`delegate = 代领人`、`operator = 发资料工作人员`。
9. 不允许跨队代领。
10. 不允许重复领取。
11. 返回已有领取摘要。

本人领取写：

```text
delegate_relation = SELF
```

同队代领写：

```text
delegate_relation = TEAM_MEMBER
```

建议错误码：

```text
MATERIAL_ALREADY_RECEIVED
MATERIAL_SUBJECT_NOT_RESOLVED
MATERIAL_DELEGATE_NOT_RESOLVED
MATERIAL_DELEGATE_TEAM_MISMATCH
MATERIAL_OPERATION_NOT_ALLOWED
```

## G. 管理端现场证件 tag 改造

页面：

```text
old-code-admin/src/views/tournament/sceneSchedule/index.vue
```

改造内容：

1. 现场证件列表新增显示 `scope_type`、`credential_name`。
2. 状态 tag 优先从 operation_state 计算。
3. 无新状态时回退旧 `credential` 字段。
4. 筛选项支持作用域类型、发证渠道。
5. 操作流水展示关联 `state_id/last_log_id`，如后端返回。

要求：

- 不破坏当前赛场安排页。
- 不影响已有证件维护和删除。

## H. 大赛级直接发证页面

新增管理端页面建议：

```text
old-code-admin/src/views/tournament/sceneCredentialDirect/index.vue
old-code-admin/src/api/tournament/sceneCredentialDirect.js
```

功能：

- 选择赛事。
- 选择证件类型。
- 填写证件展示名称。
- 选择或录入发证对象。
- 生成 `scope_type = COMPETITION` 的证件。
- 默认能力只开启报道和资料。
- 生成证件。
- 查询大赛级证件列表。

第一期不做：

- 复杂批量导入。
- 贵宾证页面。
- 媒体证页面。
- 临时证页面。
- 复杂人员库管理。

菜单建议：

```text
现场证件 / 大赛级发证
```

第一期不开发贵宾证页面。

## I. PC / 小程序证件展示

### I1. PC

文件：

```text
old-code-pc/src/views/personal/personaltabs/Competition.vue
```

改造：

- 展示 `credential_name`。
- 展示 `scope_type` 友好名称。
- 状态优先使用 operation_state 摘要。
- 资料领取显示代领人和领取时间。
- 大赛级证件不展示候场信息。

### I2. 小程序

文件：

```text
old-code-mini/pages/my-credential/index.vue
old-code-mini/pages/scan/result.vue
```

改造：

- 展示 `credential_name`。
- 大赛级证件隐藏候场区域。
- 扫码页动作矩阵按新 ability 显示。
- 资料领取支持选择被领取人和代领人。
- 展示已领取、代领人、领取时间。

## J. 历史数据兼容与回填

任务：

1. 历史证件字段回填。
2. 历史操作状态回填。
3. 旧状态字段读取兼容。
4. 老接口响应兼容。
5. 老前端不使用新字段也不报错。

验收：

- 旧赛场发证仍可生成证件。
- 旧证件仍可扫码。
- 旧证件状态仍可显示。
- 新状态表上线后，状态 tag 不丢失。

## K. 测试与冒烟

### K1. 数据库测试

1. migration 可执行。
2. 新字段存在。
3. 新表存在。
4. 历史回填正确。
5. 普通 lookup 索引存在。
6. Service 事务和条件写入阻止重复 `DONE` 状态。
7. 不删除旧字段。

### K2. 发证测试

1. 赛场发证兼容。
2. 赛场发证默认 `scope_type = SCHEDULE`。
3. 大赛级直接发证 `scope_type = COMPETITION`。
4. 大赛级证件默认只有报道和资料能力。
5. 贵宾证数据结构可保存但页面不开放。

### K3. 扫码测试

1. 赛场级证件报道成功。
2. 赛场级证件资料领取成功。
3. 赛场级证件候场成功。
4. 大赛级证件报道成功。
5. 大赛级证件资料领取成功。
6. 大赛级证件候场失败。
7. 无能力操作失败。
8. 重复操作返回重复。
9. operation_state 和 operation_log 均正确写入。
10. credential 旧状态字段兼容更新。

### K4. 代领测试

1. 本人领取成功。
2. 选择同队代领后可二次扫码代领人证件。
3. 同队成员代领成功。
4. 跨队代领失败。
5. 重复领取失败。
6. 被领取人页面显示代领人。
7. 操作流水记录 operator 和 delegate。

### K5. 页面测试

1. 管理端现场证件列表正常。
2. 管理端 tag 从新状态计算。
3. PC 端证件展示正常。
4. 小程序我的参赛证展示正常。
5. 小程序扫码确认正常。
6. 大赛级证件不展示候场信息。

## 2. 建议开发顺序

1. 数据库 migration 和历史回填脚本。
2. 后端 Domain / Mapper / Service。
3. 赛场发证兼容改造。
4. scan/confirm 基础改造。
5. operation_state 写入。
6. 资料代领。
7. 管理端证件 tag 改造。
8. PC / 小程序展示改造。
9. 大赛级直接发证页面。
10. 全链路冒烟测试。
