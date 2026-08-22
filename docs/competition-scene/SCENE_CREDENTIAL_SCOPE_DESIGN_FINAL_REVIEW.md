# 现场证件作用域与操作状态设计定稿复核报告

生成时间：2026-07-02  
范围：本轮仅修订设计文档，不进入编码。

## 1. 修订文件清单

本轮修订文件：

```text
docs/competition-scene/SCENE_CREDENTIAL_SCOPE_MODEL_DESIGN.md
docs/competition-scene/SCENE_CREDENTIAL_OPERATION_STATE_DESIGN.md
docs/competition-scene/SCENE_CREDENTIAL_MATERIAL_DELEGATE_DESIGN.md
docs/competition-scene/SCENE_CREDENTIAL_SCOPE_UPGRADE_TASKS.md
```

本轮新增复核报告：

```text
docs/competition-scene/SCENE_CREDENTIAL_SCOPE_DESIGN_FINAL_REVIEW.md
```

## 2. 修订前后的关键差异

### 2.1 operation_state 唯一性

修订前：

- 建议建立包含 `deleted` 的唯一键。
- 依赖数据库唯一索引阻止重复状态。

修订后：

- 第一阶段不使用包含 `deleted` 的唯一键作为强约束。
- 改为建立普通查询索引。
- 由 Service 层通过事务查询和条件写入保证同一主体同一操作只有一个 `DONE`。
- 未来需要撤销/重做时，再设计 `active_flag` 或 `status_version`。

### 2.2 MATERIAL 主体口径

修订前：

- 已提出资料按个人领取，但团队证件入口和状态主体的边界还不够硬。

修订后：

- 第一阶段 `MATERIAL` 的 `subject_type` 永远是 `USER`。
- `subject_code` 为被领取人的 `userId`；没有稳定 userId 时才使用稳定成员编码。
- 团队证件只能作为资料领取入口。
- 不允许写 `TEAM + MATERIAL` 作为状态事实源。

### 2.3 资料代领交互

修订前：

- 偏向通过请求参数传入代领人信息，容易落入手工录入代替身份校验。

修订后：

- 第一阶段采用二次扫码。
- 工作人员先扫被领取人证件。
- 选择“本人领取”时直接确认。
- 选择“同队代领”时再扫代领人证件。
- 后端通过代领人证件解析 delegate，并校验同一 `team_code`。

### 2.4 ability_json

修订前：

- 结构是建议形式。
- 默认能力对教师证、专家证、工作人员证的口径仍偏宽。

修订后：

- 第一阶段固定六字段。
- 六个字段必须显式存在。
- 缺失字段后端按 false 处理。
- 前端不直接信任 `ability_json`，最终以后端 `allowedActions / availableActions` 为准。
- 默认能力矩阵已收敛。

### 2.5 大赛级直接发证

修订前：

- 任务范围偏大，容易扩展到导入、贵宾、媒体、临时证、人员库。

修订后：

- 第一阶段只做最小闭环：选择赛事、选择证件类型、填写展示名称、选择或录入对象、生成 `scope_type = COMPETITION` 证件。
- 默认能力只开启 `report/material`。
- 不做复杂批量导入。
- 不做贵宾证页面。
- 不做媒体证、临时证页面。
- 不做复杂人员库管理。

## 3. 最终 operation_state 唯一性方案

第一阶段新增表：

```text
competition_scene_subject_operation_state
```

不建立包含 `deleted` 的唯一键。

建立普通查询索引：

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

保证规则：

1. Service 层开启事务。
2. 写入前按 lookup 查询是否已有 `DONE`。
3. 未完成时执行条件写入。
4. 条件写入失败按重复操作处理。
5. 第一阶段不设计撤销/重做。
6. 后续如需撤销/重做，再补 `active_flag` 或 `status_version`。

## 4. 最终 MATERIAL subject 口径

第一阶段资料领取按个人领取。

固定规则：

```text
operation_type = MATERIAL
scope_type = COMPETITION
scope_ref_id = competition_series_id
subject_type = USER
subject_code = 被领取人的 userId 或稳定成员编码
```

明确禁止：

```text
TEAM + MATERIAL
```

说明：

- 团队证件只能作为资料领取入口。
- 状态事实源必须落在被领取人个人主体上。
- 同一大赛同一 `USER` 只能领取一次资料。

## 5. 最终代领交互口径

三类角色：

```text
subject = 被领取人
delegate = 代领人
operator = 发资料工作人员
```

第一阶段流程：

1. 发资料工作人员先扫被领取人证件。
2. 小程序展示“本人领取”和“同队代领”。
3. 选择本人领取时直接确认。
4. 选择同队代领时，再扫代领人证件。
5. 后端校验代领人与被领取人属于同一 `team_code`。
6. 校验通过后写 `operation_state`。
7. 不允许跨队代领。
8. 不允许重复领取。
9. 被代领人 PC / 小程序证件展示中显示代领人和领取时间。

## 6. 最终 ability_json 结构

第一阶段固定结构：

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

规则：

1. 六个字段必须显式存在。
2. 后端解析缺失字段时按 false 处理。
3. 前端不直接信任 `ability_json` 作权限判断。
4. 最终可执行动作以后端 `allowedActions / availableActions` 为准。

默认能力：

| 场景 | 默认能力 |
| --- | --- |
| `SCHEDULE + PARTICIPANT` | `report/material/waiting/resourceReservation = true` |
| `COMPETITION + PARTICIPANT` | `report/material = true` |
| `TEACHER` | `report = true` |
| `EXPERT` | `report/review = true` |
| `STAFF` | 默认不具备被扫码状态能力，扫码操作权限由角色矩阵决定 |
| `VIP` | `report/vipAccess = true`，第一期只做结构预留 |

## 7. 大赛级直接发证第一期边界

第一期只做最小功能：

1. 选择赛事。
2. 选择证件类型。
3. 填写证件展示名称。
4. 选择或录入发证对象。
5. 生成 `scope_type = COMPETITION` 的证件。
6. 默认 `ability_json` 只开启 `report/material`。

第一期不做：

1. 复杂批量导入。
2. 贵宾证页面。
3. 媒体证页面。
4. 临时证页面。
5. 复杂人员库管理。

## 8. 是否仍有待人工确认事项

无阻塞第一阶段编码的待确认项。

非阻塞后续事项：

1. 没有稳定 userId 的导入对象如何生成 `subject_code`。
2. 后续是否允许队长一次性批量领取全队资料。
3. 后续是否允许指导教师代领学生资料。
4. 后续贵宾证、媒体证、临时证的页面和权限规则。
5. 后续撤销/重做操作是否引入 `active_flag` 或 `status_version`。

## 9. 是否可以进入第一阶段编码

可以进入第一阶段编码。

建议第一阶段编码顺序：

1. 数据库 migration。
2. Domain / Mapper / Service。
3. 历史字段回填和兼容读取。
4. 赛场发证兼容改造。
5. scan / confirm 支持 `scope_type`、`ability_json` 和 `operation_state`。
6. 资料领取 USER 主体和二次扫码代领。
7. 管理端 tag 兼容展示。
8. PC / 小程序证件展示最小改造。

继续保持：

- 不使用伪赛场安排。
- 不恢复 `MIXED`。
- 不把 `credentialNo` 当权限。
- `operation_state` 是状态事实源。
- `operation_log` 是审计流水。
- `credential` 状态字段保留为兼容展示。
- 不修改报名、支付、成绩、证书主流程。
- 不连接生产数据库。

