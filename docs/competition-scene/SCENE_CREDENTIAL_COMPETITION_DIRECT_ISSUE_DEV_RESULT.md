# 大赛级直接发证最小闭环开发结果

生成时间：2026-07-02

## 1. 本阶段目标

本阶段在已完成的现场证件作用域与 `operation_state` 第一阶段基础上，补齐不绑定具体赛场安排的 `COMPETITION` 级现场证件最小闭环：

1. 管理端可手工发放大赛级现场证件。
2. 后端生成 `scope_type = COMPETITION`、`issue_channel = COMPETITION_DIRECT` 的证件。
3. 大赛级证件支持报道。
4. 大赛级参赛证支持资料领取。
5. 大赛级证件明确拒绝候场。
6. PC / 小程序证件展示支持大赛级作用域。
7. 不使用伪赛场安排，不恢复 `MIXED`。

## 2. 新增/修改文件

### 2.1 数据库 migration

- `db/migration/20260702_competition_scene_credential_direct_issue_p2.sql`

说明：

- 新增 `competition_scene_credential.subject_code`。
- 新增 `idx_scene_credential_subject_code`。
- 新增 `idx_scene_credential_competition_subject`。
- 回填历史有效证件 `subject_code`。
- 未执行生产数据库 migration。

### 2.2 后端

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneCompetitionDirectIssueReq.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneCredential.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/contant/CompetitionSceneConstants.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneCredentialService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneVerifyServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneSubjectOperationStateServiceImpl.java`

### 2.3 管理端

- `old-code-admin/src/api/tournament/sceneCredentialCompetition.js`
- `old-code-admin/src/views/tournament/sceneCredentialCompetition/index.vue`

### 2.4 PC / 小程序展示

- `old-code-pc/src/views/personal/personaltabs/Competition.vue`
- `old-code-mini/pages/my-credential/index.vue`
- `old-code-mini/pages/scan/result.vue`

## 3. 后端接口

### 3.1 大赛级证件列表

```text
GET /competition/sceneCredential/competitionList
GET /sceneCredential/competitionList
```

权限：

```text
competition:sceneCredential:list
```

实现要点：

- 强制查询 `scope_type = COMPETITION`。
- 复用现场证件分页结构 `TableDataInfo`。
- 支持 `competitionSeriesId`、`credentialType`、`subjectType`、`subjectCode`、`credentialNo` 等基础筛选。

### 3.2 大赛级直接发证

```text
POST /competition/sceneCredential/competitionDirectIssue
POST /sceneCredential/competitionDirectIssue
```

权限：

```text
competition:sceneCredential:add
```

请求字段：

```text
competitionSeriesId
credentialType
credentialName
subjectType
subjectCode
subjectName
remark
```

生成规则：

- `issue_channel = COMPETITION_DIRECT`
- `scope_type = COMPETITION`
- `scope_ref_id = competitionSeriesId`
- `schedule_id = null`
- `target_id = null`
- `subject_code = subjectCode`
- `credential_name` 为空时按证件类型生成默认名称
- `qr_content` 继续使用现有现场证件二维码内容格式

校验规则：

- `competitionSeriesId` 必填，并校验赛事存在。
- `credentialType` 必须合法。
- `subjectType` 必须合法，`PERSON` 会兼容转换为 `USER`。
- `subjectCode` 必填。
- 同一 `competitionSeriesId + subjectType + subjectCode + credentialType + scopeType=COMPETITION` 下不得重复发放有效证件。
- 大赛级证件不允许开启 `waiting=true`。
- 不接收 `scheduleId`，不生成伪赛场安排。

## 4. ability_json

本阶段继续使用第一阶段定稿的六字段能力模型。

### PARTICIPANT

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

### TEACHER

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

### EXPERT

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

### STAFF

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

### VIP

```json
{
  "report": true,
  "material": false,
  "waiting": false,
  "review": false,
  "resourceReservation": false,
  "vipAccess": true
}
```

VIP 仅保留后端结构能力，管理端页面未开放正式贵宾证功能。

## 5. 管理端页面

页面路径：

```text
old-code-admin/src/views/tournament/sceneCredentialCompetition/index.vue
```

API 路径：

```text
old-code-admin/src/api/tournament/sceneCredentialCompetition.js
```

已实现能力：

1. 查询大赛级证件列表。
2. 按赛事、证件类型、对象编码、证件编号筛选。
3. 新增大赛级证件。
4. 查看证件详情。
5. 展示二维码内容。
6. 展示报道状态、资料状态。
7. 候场列固定展示“无候场功能”。
8. 删除证件复用现有现场证件删除接口。

表单字段：

- 赛事
- 证件类型
- 证件展示名称
- 发证对象类型
- 发证对象编码
- 发证对象名称
- 备注

权限码：

```text
competition:sceneCredential:list
competition:sceneCredential:query
competition:sceneCredential:add
competition:sceneCredential:remove
```

菜单建议：

```text
菜单名称：大赛级发证
路径：sceneCredentialCompetition
组件：tournament/sceneCredentialCompetition/index
权限：competition:sceneCredential:list
建议挂载：现场安排配置 或 赛事管理相关父菜单
```

本阶段未直接写入菜单 SQL，避免误挂到不确定父菜单。上线前建议由人工按当前 `sys_menu` 结构挂载。

## 6. 扫码规则

### 6.1 报道

- 签到工作人员或现场工作人员扫码后，可对具备 `ability_json.report=true` 的大赛级证件确认报道。
- 成功后写入 `competition_scene_subject_operation_state`。
- 状态范围为 `COMPETITION` 级。
- 重复报道返回 `alreadyDone：重复操作，证件状态未变更`。

### 6.2 资料领取

- 发资料工作人员或现场工作人员扫码后，可对具备 `ability_json.material=true` 的大赛级证件确认资料领取。
- `MATERIAL` 状态主体固定为 `USER`。
- 直接 `USER` 证件使用 `user_id` 或 `subject_code` 作为状态主体编码。
- 团队证件不会把 `team_code` 作为 `USER + MATERIAL` 的状态事实源。
- 重复资料领取返回 `alreadyDone：重复操作，证件状态未变更`。

### 6.3 候场

- 大赛级证件不返回候场动作。
- 如果强制确认候场，后端返回：

```text
CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING
```

- 不写入 `SCHEDULE` 级 `WAITING` 状态。

### 6.4 专家证报道

- 专家证默认具备 `report=true`。
- 本阶段已将专家证纳入可报道对象判断。
- 专家评审入口仍按原有提示能力处理，不扩展正式评审业务。

## 7. operation_state 写入结果

报道：

```text
operation_type = REPORT
scope_type = COMPETITION
scope_ref_id = competition_series_id
subject_type = 证件主体类型
subject_code = subject_code / team_code / user_id
```

资料领取：

```text
operation_type = MATERIAL
scope_type = COMPETITION
scope_ref_id = competition_series_id
subject_type = USER
subject_code = 被领取人的 userId 或 USER 证件 subject_code
```

候场：

```text
COMPETITION 级证件不写 WAITING 状态
```

审计流水：

- 成功、失败、重复操作继续写入 `competition_scene_operation_log`。
- `operation_state.last_log_id` 在确认成功后回填。

兼容字段：

- 成功报道继续兼容更新 `competition_scene_credential.report_status/report_time`。
- 成功资料领取继续兼容更新 `competition_scene_credential.material_status/material_time`。
- 大赛级证件不更新候场字段。

## 8. PC / 小程序展示

### 8.1 PC

文件：

```text
old-code-pc/src/views/personal/personaltabs/Competition.vue
```

改造：

- 证件标题展示 `credentialName`。
- 增加作用域展示。
- 大赛级证件显示为“大赛级”。

### 8.2 小程序我的证件

文件：

```text
old-code-mini/pages/my-credential/index.vue
```

改造：

- 证件标题展示 `credentialName`。
- 增加作用域展示。
- 大赛级证件显示为“大赛级”。

### 8.3 小程序扫码结果

文件：

```text
old-code-mini/pages/scan/result.vue
```

改造：

- 扫码结果页展示证件作用域。
- 大赛级证件显示为“大赛级”。

## 9. 测试结果

### 9.1 静态口径检查

本阶段新增/修改文件检查未命中：

- `MIXED`
- `伪赛场`
- `team_id`
- `asset_no`
- `owner_unit`
- `storage_location`
- `cancelDeadlineMinutes`
- `cancel_deadline_minutes`

### 9.2 后端编译

命令：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

说明：

- Maven 输出存在项目既有重复依赖、deprecated、unchecked 警告。
- 未出现本阶段新增代码编译错误。

### 9.3 管理端构建

命令：

```bash
npm run build:stage
```

结果：

```text
构建成功
```

说明：

- Vite 输出存在项目既有 `eval`、sourcemap、包体积等警告。
- 未出现本阶段新增页面构建错误。

### 9.4 PC 构建

命令：

```bash
npm run build
```

结果：

```text
构建成功
```

说明：

- PC 构建会重新生成 `old-code-pc/pc` 下的静态产物，当前工作区中存在构建产物 hash 变化。

### 9.5 小程序检查

检查内容：

- 我的证件页作用域展示逻辑。
- 扫码结果页作用域展示逻辑。

结果：

```text
静态检查通过
```

说明：

- 本轮未启动小程序开发者工具做真机/模拟器运行验证。

## 10. 未完成项

1. 未连接生产数据库。
2. 未执行生产 migration。
3. 未做正式网关联调。
4. 未新增复杂批量导入。
5. 未开发贵宾证正式页面。
6. 未开发媒体证、临时证页面。
7. 未开发复杂人员库。
8. 管理端菜单 SQL 未自动落库，建议人工按当前 `sys_menu` 结构挂载。
9. 管理端详情第一期展示二维码内容，未新增二维码图片渲染依赖。

## 11. 后续联调建议

1. 在测试库执行 `20260702_competition_scene_credential_direct_issue_p2.sql`。
2. 重启正式测试用 competition 服务，确认新 Controller 已加载。
3. 挂载管理端菜单：
   - `path = sceneCredentialCompetition`
   - `component = tournament/sceneCredentialCompetition/index`
4. 使用管理端发放 `PARTICIPANT + USER` 大赛级证件。
5. 验证 `schedule_id`、`target_id` 均为空。
6. 验证 `scope_type = COMPETITION`。
7. 验证 `ability_json.waiting = false`。
8. 使用签到工作人员证件扫码确认报道。
9. 使用资料工作人员证件扫码确认资料领取。
10. 强制候场确认应返回 `CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING`。
11. PC / 小程序证件列表确认大赛级证件正常展示。

## 12. 结论

本阶段“大赛级直接发证最小闭环”编码已完成，编译与构建通过，未引入伪赛场、`MIXED`、`team_id` 或资产管理式字段。

建议进入测试环境联调：先执行测试库 migration、重启 competition 服务、挂载管理端菜单，再按后续联调建议验证发证、展示、报道、资料领取和候场拒绝。
