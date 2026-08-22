# 大赛现场运行功能交接文档

更新时间：2026-07-02

## 1. 背景和当前调整方向

前一阶段已围绕“大赛现场安排、现场证件、二维码核验、报道签到、资料领取、候场管理”完成了一版基础能力。当前业务设计需要调整：现场运行不仅需要证件和签到，还需要管理现场设备资源，包括资源登记、赛场部署、可预约时段配置、选手预约和使用流转。

2026-07-02 补充：在原有现场运行模块基础上，已补齐“小程序现场扫码操作矩阵”和用户端证件展示能力。当前小程序扫码后会基于“扫码人角色 + 被扫对象角色”返回可执行动作，并支持报道、资料领取、候场确认和专家评审入口提示。资源预约第六阶段、正式评审页面、独立运维端仍不在本轮范围内。

新的讨论重点建议从“参赛证/赛场安排中心”扩展为“现场运行资源中心”：

- 保留已有现场安排、证件、扫码核验能力，作为人员身份、赛事场次、现场状态流转的基础。
- 新增独立的设备资源模型，不建议把设备资源字段直接塞进 `competition_scene_schedule`。
- 在赛场安排中通过关联表部署设备资源，并配置可预约时段。
- 小程序端基于当前登录用户的报名/证件/团队身份，展示可预约资源并提交预约。

## 2. 当前已完成开发范围

### 2.1 数据库

已新增迁移 SQL：

- `db/migration/20260629_competition_scene_credential.sql`
- `db/migration/20260629_competition_scene_admin_menu.sql`

当前新增 4 张核心表：

- `competition_scene_schedule`：现场赛场安排主表，维护报道时间、报道地点、比赛时间、候场、资料领取、注意事项等。
- `competition_scene_schedule_target`：安排对象表，记录某个安排匹配到的团队、个人或专家。
- `competition_scene_credential`：现场证件实例表，保存参赛证/教师证/专家证、二维码令牌、证件文件、现场状态快照。
- `competition_scene_operation_log`：现场操作流水表，记录扫码核验、报道签到、资料领取、候场确认等操作。当前扫码、确认、失败、重复、异常都会写入流水；成功的报道、资料领取、候场确认会同步更新 `competition_scene_credential` 对应状态字段。

当前表结构还没有覆盖设备资源、设备部署、可预约时段、预约记录、设备使用记录等模型。

### 2.2 后端模块

后端改动集中在：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition`

已新增/修改的主要类：

- 常量：
  - `CompetitionSceneConstants`
- Domain/DTO：
  - `CompetitionSceneSchedule`
  - `CompetitionSceneScheduleTarget`
  - `CompetitionSceneCredential`
  - `CompetitionSceneOperationLog`
  - `CompetitionSceneCredentialGenerateReq`
  - `CompetitionSceneVerifyReq`
  - `CompetitionSceneVerifyResult`
  - `CompetitionSceneScanAction`
  - `CompetitionSceneMatchResult`
- Mapper：
  - `CompetitionSceneScheduleMapper`
  - `CompetitionSceneScheduleTargetMapper`
  - `CompetitionSceneCredentialMapper`
  - `CompetitionSceneOperationLogMapper`
- Service：
  - `ICompetitionSceneScheduleService`
  - `CompetitionSceneScheduleServiceImpl`
  - `ICompetitionSceneCredentialService`
  - `CompetitionSceneCredentialServiceImpl`
  - `ICompetitionSceneVerifyService`
  - `CompetitionSceneVerifyServiceImpl`
- Controller：
  - `CompetitionSceneScheduleController`
  - `CompetitionSceneCredentialController`
  - `CompetitionSceneVerifyController`
  - `UserCompetitionSceneCredentialController`

### 2.3 已有后端接口

管理端现场安排：

- `GET /competition/sceneSchedule/list`
- `GET /competition/sceneSchedule/{scheduleId}`
- `POST /competition/sceneSchedule`
- `PUT /competition/sceneSchedule`
- `DELETE /competition/sceneSchedule/{scheduleIds}`
- `POST /competition/sceneSchedule/match/{scheduleId}`

管理端安排对象：

- `GET /competition/sceneSchedule/target/list`
- `POST /competition/sceneSchedule/target`
- `POST /competition/sceneSchedule/target/batch`
- `PUT /competition/sceneSchedule/target`
- `DELETE /competition/sceneSchedule/target/{targetIds}`

管理端现场证件：

- `GET /competition/sceneCredential/list`
- `GET /competition/sceneCredential/{credentialId}`
- `POST /competition/sceneCredential/generate`
- `PUT /competition/sceneCredential`
- `DELETE /competition/sceneCredential/{credentialIds}`

扫码核验：

- `POST /competition/sceneVerify/scan`
- `POST /competition/sceneVerify/confirm`
- `GET /competition/sceneVerify/log/list`

用户端/小程序端证件：

- `GET /competition/userCompetition/sceneCredential/myList`
- `GET /competition/userCompetition/sceneCredential/{credentialId}`

补充说明：

- `POST /competition/sceneVerify/scan` 当前返回扫码操作矩阵，核心字段包括：
  - `operatorRole` / `operatorRoleLabel`：扫码人现场角色。
  - `targetRole` / `targetRoleLabel`：被扫证件对象角色。
  - `availableActions`：当前可展示的动作列表，包含 `actionType`、`actionLabel`、`actionKind`、`enabled`、`status`、`message`。
  - `reviewEntryAvailable` / `reviewEntryMessage`：专家评审入口提示。
  - `matrixMessage`：当前账号无现场角色、无可执行动作或动作已完成时的提示。
- `POST /competition/sceneVerify/confirm` 会二次校验矩阵权限，不信任前端按钮状态。确认成功才会更新证件状态，并写入 `competition_scene_operation_log`。
- `EXPERT_REVIEW_ENTRY` 当前只作为小程序入口提示，不落正式评审业务状态。
- 对于 `targetSource=MANUAL` 或 `targetSource=IMPORT` 的现场对象，扫码核验跳过报名一致性校验，避免手工/导入对象因 `memberId/teamCode` 为空被误判失败；报名来源对象仍保留报名、审核、支付一致性校验。

注意：由于网关可能会剥离 `/competition` 前缀，管理端 Controller 目前兼容了两套路由，例如 `@RequestMapping({"/sceneSchedule", "/competition/sceneSchedule"})`。

### 2.4 管理端页面

管理端改动集中在：

- `old-code-admin/src/api/tournament/sceneSchedule.js`
- `old-code-admin/src/views/tournament/sceneSchedule/index.vue`

已实现页面：

- 菜单：现场安排配置
- Tab：
  - 赛场安排
  - 匹配对象
  - 现场证件
  - 操作流水

已支持能力：

- 新增/修改/删除现场安排。
- 按赛事、阶段、赛道、组别维护报道、赛场、候场、资料领取信息。
- 按团队或个人维度配置。
- 支持参赛证、教师证、专家证。
- 从报名信息匹配对象。
- 手工新增对象，支持从系统用户和用户组选择。
- 为安排或指定对象生成证件。
- 维护证件状态。
- 删除现场证件。
- 现场证件 Tab 根据 `reportStatus`、`materialStatus`、`waitingStatus` 点亮“报到/资料/候场”状态 tag。
- 维护功能中已移除二维码图片 URL、证件文件 URL、证件图片 URL，不再要求管理员维护这些字段。
- 查询现场扫码流水。
- 操作流水已补充“专家评审入口”操作类型文案。

### 2.5 PC 端页面

PC 端改动集中在：

- `old-code-pc/src/api/personal/index.js`
- `old-code-pc/src/views/personal/index.vue`
- `old-code-pc/src/views/personal/personaltabs/Competition.vue`

已实现入口：

- 个人中心 -> 我的赛事 -> 参赛证

已支持能力：

- 查询当前用户自己的现场证件。
- 在我的赛事卡片上展示“参赛证”入口和数量。
- 参赛证弹窗展示：
  - 报道时间、报道地点
  - 赛场时间、赛场地点、赛场/考场
  - 候场时间、候场地点、候场分组
  - 资料领取地点
  - 注意事项
  - 报到/资料领取/候场状态
  - 二维码
  - 下载证件
- 如果后台没有 `qrCodeUrl`，PC 端会使用 `qrcode.vue` 根据 `qrContent` 或 `credentialToken` 生成二维码。
- 已移除“查看证件”按钮。
- “下载证件”按钮当前可点击，会下载前端根据证件二维码内容生成的二维码图片。
- 如果“我的赛事”列表为空但证件列表有数据，PC 端会按证件快照聚合出赛事卡片，避免证件入口消失。

### 2.6 小程序端页面

小程序端改动集中在：

- `old-code-mini/api/sceneCredential.js`
- `old-code-mini/api/scan.js`
- `old-code-mini/components/custom-tabbar/custom-tabbar.vue`
- `old-code-mini/pages/my-credential/index.vue`
- `old-code-mini/pages/scan/result.vue`
- `old-code-mini/pages/mine/index.vue`
- `old-code-mini/pages.json`
- `old-code-mini/utils/qrcode.js`

已实现能力：

- “我的”页面增加“我的参赛证”入口。
- 新增“我的参赛证”页面，对照 PC 端展示当前用户现场证件。
- 小程序端可根据 `qrContent` 或 `credentialToken` 前端生成二维码。
- 展示证件基础信息、报道/资料/候场状态、现场安排、注意事项。
- 扫码入口兼容两类二维码：
  - 小程序页面路径二维码：继续按页面路径跳转。
  - 现场证件原始二维码内容，例如 `csc_xxx`：进入 `/pages/scan/result` 并调用现场扫码矩阵接口。
- 扫码结果页保留旧 `rid_` 签到二维码兼容路径。
- 现场证件扫码成功后展示扫码人角色、被扫对象角色、证件状态、现场安排和可执行动作。
- 支持在小程序扫码结果页执行报道、资料领取、候场确认。
- 专家扫码参赛对象时展示“专家评审入口”提示；正式评审页面未开发。

### 2.7 扫码操作矩阵规则

当前实现的矩阵规则如下：

- 扫码人角色识别优先使用当前登录用户 `userId` 在同一赛场安排、同一赛事下查找有效现场证件；请求携带 `operatorPhone` 时可按手机号兜底。
- 被扫对象角色优先使用证件 `competitionRoleName`，缺失时按 `credentialType` 推断。
- `CHECKIN_STAFF` 可对参赛侧对象执行：
  - `REPORT_SIGN`：报道签到。
  - `WAITING_CHECK_IN`：候场确认。
- `MATERIAL_STAFF` 可对参赛侧对象执行：
  - `MATERIAL_RECEIVE`：资料领取。
- 通用 `STAFF` 可执行报道、资料领取、候场确认。
- `EXPERT` 扫描队员/队长证件时返回 `EXPERT_REVIEW_ENTRY` 提示动作。
- 队员、教师等非现场操作角色扫码时，核验可以通过，但 `availableActions` 为空，并通过 `matrixMessage` 提示当前无可执行动作。
- 已完成的动作仍会返回到 `availableActions`，但 `enabled=false`、`status=DONE`，小程序端展示为已完成。
- `confirm` 接口只接受矩阵中 `actionKind=CONFIRM` 且 `enabled=true` 的动作；提示类动作不会写证件状态。

### 2.8 已验证情况

前一阶段已验证：

- 后端 competition 模块编译通过。
- 管理端构建通过。
- PC 端构建通过。

2026-07-02 本轮补充验证：

- 后端已执行 `mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`，编译通过。
- 小程序目录当前没有 `package.json`，未执行 npm 构建；已做静态检查和关键路径复核。

构建中存在一些原项目已有 warning，例如 Sass deprecation、browserlist、少量 CSS 拼写警告，不属于本轮新增功能造成的阻塞。

## 3. 当前业务模型评估

### 3.1 当前模型适合解决的问题

现有模型适合支撑：

- 某个赛事/阶段/赛道/组别的现场安排。
- 把团队或个人匹配到某个现场安排。
- 给团队、个人、教师、专家生成可核验的证件。
- PC 端展示证件和二维码。
- 现场扫码核验证件有效性、报名一致性、身份信息。
- 现场扫码后基于扫码人角色和被扫对象角色返回动作矩阵。
- 报道签到、资料领取、候场确认。
- 小程序端展示我的参赛证并执行现场扫码动作。
- 现场操作留痕。

### 3.2 当前模型不足

当前模型不适合直接支撑设备资源管理：

- 没有设备资源主数据表。
- 没有资源类型、数量、工位数、单场占用周期、共享占用规则。
- 没有资源参数、图片、安全须知、使用说明等结构化维护。
- 没有“赛场安排 -> 设备资源部署”的关联表。
- 没有部署后的可预约时段。
- 没有预约、取消、签到使用、占用释放等记录。
- 没有处理设备容量、工位容量、同组/非同组共享占用的约束。
- 没有小程序端资源预约接口。


## 4 已知风险和注意事项

- 当前 `CompetitionSceneCredentialController` 和 `UserCompetitionSceneCredentialController` 都存在 `myList` 能力，后续可以择机统一，避免重复入口。
- 证件文件下载当前没有真正实现后端生成 PDF/图片；PC 和小程序端主要以前端二维码生成兜底。
- 二维码图片当前不再由管理端维护 URL，但后端仍没有统一生成二维码图片服务。
- 专家评审入口当前只是扫码后的入口提示，没有正式评审页面和评审业务流转。
- 扫码角色矩阵依赖现场证件角色配置，测试时需要给操作人员生成 `CHECKIN_STAFF`、`MATERIAL_STAFF`、`STAFF` 或 `EXPERT` 等有效证件。
- 手工/导入现场对象会跳过报名一致性校验；如果后续要增强安全性，可在 `competition_scene_schedule_target` 增加结构化 `targetSource` 快照字段或更明确的核验策略，避免长期依赖 `credentialSnapshotJson` 解析。
- 小程序端现场预约尚未开发。
- 当前现场安排管理页是一个较大的单文件 Vue，继续叠加设备部署功能会变重，建议后续拆分组件或单独页面。
- 资源预约会涉及并发抢占，需要在后端做事务和行锁/乐观锁设计，不能只靠前端判断余量。
- 资源预约需要考虑容量计算口径：设备数量、工位数、预约数量、共享规则必须统一。
