# 现场安排、匹配对象、现场证件编号规则审计

更新时间：2026-07-01

## 1. 赛场安排表单现有字段

管理端页面：`old-code-admin/src/views/tournament/sceneSchedule/index.vue`

当前赛场安排查询区包含：

- 安排名称：`scheduleName`
- 关联赛事：`competitionSeriesId`
- 证件类型：`credentialType`
- 配置维度：`configDimension`
- 状态：`status`

当前赛场安排表格包含：

- 安排名称、赛事
- 证件类型：`credentialType`
- 配置维度：`configDimension`
- 报道信息、赛场信息、赛道/组别、状态

当前新增/编辑表单包含：

- 基础信息：`scheduleName`、`competitionSeriesId`、`competitionName`、`status`
- 赛事关联：`competitionStageId`、`competitionStageName`、`competitionTrackId`、`competitionTrackName`、`secondLevelCode`、`secondLevelName`
- 旧证件配置：`credentialType`、`configDimension`
- 报道安排：`reportStartTime`、`reportEndTime`、`reportLocation`
- 比赛安排：`contestStartTime`、`contestEndTime`、`contestLocation`、`contestRoom`
- 候场安排：`waitingStartTime`、`waitingEndTime`、`waitingLocation`、`waitingGroupCode`、`waitingGroupName`
- 资料与备注：`materialLocation`、`notice`

本轮需要从赛场安排查询、当前安排提示、列表和新增/编辑表单中隐藏或移除 `credentialType`、`configDimension`、`waitingGroupCode`，并避免隐藏后提交空值覆盖旧数据。

## 2. 匹配对象表单现有字段

管理端匹配对象列表当前展示：

- 对象来源：`targetSource`
- 团队：`teamName`、`teamCode`
- 人员：`userName`、`phone`、`email`
- 身份后六位：`idCardSuffix`
- 角色：`competitionRoleName`
- 学校/机构：`schoolName`、`orgName`
- 赛道/组别：`competitionTrackName`、`secondLevelName`
- 候场组：`waitingGroupName` 或 `waitingGroupCode`
- 状态：`status`

当前手工新增/编辑弹窗包含：

- 安排ID：`scheduleId`
- 对象来源：`targetSource`（编辑时）
- 团队、成员、用户、手机号、邮箱、证件号、学校、机构等人员信息
- 角色：`competitionRoleName`，当前是自由输入框
- 座位/工位：`seatNo`
- 候场组编码：`waitingGroupCode`，当前可手工输入
- 候场组名称：`waitingGroupName`，当前可手工输入
- 状态：`status`

当前匹配对象弹窗没有现场证件类型字段；虽然后端 target 表和 domain 已有 `credentialType`，但新增/编辑时由后端从 schedule 继承。

## 3. 现场证件编号当前生成逻辑

后端文件：`CompetitionSceneCredentialServiceImpl`

当前 `generateCredentialNo(schedule, target)` 逻辑：

- 使用系统当前日期：`yyyyMMdd`
- 根据 `schedule.credentialType` 选择前缀：
  - `COMPETITOR` -> `CS`
  - `TEACHER` -> `TC`
  - `EXPERT` -> `EC`
- 编号格式为：`prefix + date + "-" + scheduleId + "-" + targetId + "-" + 8位随机码`
- 8 位随机码来自 `UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase()`

示例形态：`CS20260701-3-20-1A2B3C4D`

本轮需要改为短编号：`CSyyyyMMdd-scheduleId-sequence`，不再追加随机码；二维码 token 仍由 `generateToken()` 独立生成，不应受证件编号变化影响。

## 4. 证件类型当前存储位置

当前有三处存储：

- `competition_scene_schedule.credential_type`
  - 旧口径下赛场安排必填，用于控制匹配和生成。
- `competition_scene_schedule_target.credential_type`
  - 表结构已存在，当前由后端从 schedule 复制。
- `competition_scene_credential.credential_type`
  - 证件快照字段，当前由后端从 schedule 复制。

当前 migration `db/migration/20260629_competition_scene_credential.sql` 已包含 `competition_scene_schedule_target.credential_type` 字段，因此本轮不需要再新增该字段。

## 5. 角色字段当前处理方式

当前角色字段为 `competitionRoleName`：

- 自动匹配时来自报名数据 `CompetitionApplyInfo.competitionRoleName`，通常为中文，如 `队长`、`队员`、`指导教师`。
- 手工新增/编辑时前端是自由输入框。
- 后端保存前没有枚举校验。
- 列表直接展示原始值。

本轮需要前端改为固定下拉；后端保存前校验角色合法，并兼容旧中文角色值映射。

## 6. 候场组编码/名称当前来源

当前来源链路：

- 赛场安排表保存 `waitingGroupCode` 和 `waitingGroupName`。
- 自动匹配对象时，`buildTargetFromApply()` 直接从 schedule 复制候场组编码/名称。
- 手工新增/批量新增对象时，`fillTargetBaseInfo()` 仅在 target 候场组字段为空时从 schedule 补齐；如果前端传入不同候场组，后端会保留前端值。
- 编辑 target 时同样可能更新 `waitingGroupCode` / `waitingGroupName`。
- 生成证件时优先使用 target 候场组，缺失时回退 schedule。

本轮需要改为：新增 target 时始终从当前 schedule 继承；编辑 target 时不允许前端修改候场组；提交时以后端 schedule 当前值为准。

## 7. 需要修改的前端文件

主要修改：

- `old-code-admin/src/views/tournament/sceneSchedule/index.vue`
  - 赛场安排查询区、当前安排提示、列表、表单隐藏/移除 `credentialType`、`configDimension`、`waitingGroupCode`
  - 赛场安排提交 payload 使用白名单，避免隐藏字段空值覆盖旧数据
  - 匹配对象增加 `credentialType`
  - 匹配对象角色改为固定下拉并显示中文
  - 匹配对象候场组编码/名称改为继承展示且禁用
  - target 查询如增加 role 条件，使用下拉框
  - credential 列表证件类型展示兼容新旧值

兼容补充：

- `old-code-pc/src/views/personal/personaltabs/Competition.vue`
  - 如后端生成新枚举 `PARTICIPANT` / `STAFF`，PC 我的赛事参赛证展示需补中文映射，避免显示英文枚举值。

接口文件 `old-code-admin/src/api/tournament/sceneSchedule.js` 当前只是透传请求，暂不需要新增接口。

## 8. 需要修改的后端文件

主要修改：

- `CompetitionSceneConstants`
  - 增加新证件类型：`PARTICIPANT`、`TEACHER`、`EXPERT`、`STAFF`
  - 兼容旧证件类型：`COMPETITOR`
  - 增加目标角色枚举：`TEACHER`、`MEMBER`、`EXPERT`、`CAPTAIN`、`MATERIAL_STAFF`、`CHECKIN_STAFF`
- `CompetitionSceneScheduleServiceImpl`
  - target 新增/批量新增/编辑前校验证件类型和角色
  - 自动匹配时给 target 设置可用证件类型
  - 手工新增/编辑时强制从 schedule 继承候场组编码/名称
  - 角色旧中文值映射为内部枚举
- `CompetitionSceneCredentialServiceImpl`
  - 生成证件时优先读取 `target.credentialType`
  - target 缺失证件类型时兼容 schedule/targetType 推断，无法推断时报明确错误
  - 编号改为 `CSyyyyMMdd-scheduleId-sequence`
  - sequence 按同一 schedule 下已有证件数递增并检查唯一性
  - token 生成保持不变
- `CompetitionSceneCredentialMapper.java` / `CompetitionSceneCredentialMapper.xml`
  - 增加按 schedule 统计证件数、按 credentialNo 查询或计数的方法

按需修改：

- `CompetitionSceneScheduleTargetMapper.xml`
  - 角色查询条件可增加 `competitionRoleName`
  - target 更新可以继续使用动态 SQL，但 service 层应清理候场组字段，避免前端改写

## 9. 是否需要数据库 migration

审计结论：

- `competition_scene_schedule_target.credential_type` 已存在，不需要新增字段。
- `competition_scene_credential.credential_no` 已有唯一键 `uk_scene_credential_no`，不需要新增唯一索引。
- 不应删除旧 schedule 上的 `credential_type`、`config_dimension`、`waiting_group_code` 字段，以免破坏历史数据兼容。

建议新增一个人工审查用 migration，主要用于兼容旧枚举值和注释口径：

- 将新口径说明同步到字段注释。
- 可选地把历史 `COMPETITOR` 迁移为 `PARTICIPANT`，或暂不迁移、由代码兼容两套枚举。

本轮不连接生产数据库，不自动执行 migration。
