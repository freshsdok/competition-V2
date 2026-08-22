# 签到概览 Phase 1 现状审计报告

审计时间：2026-07-13  
项目根目录：`/Users/wwang/Documents/ClaudeCode/deshi_competition_2`

## 1. 审计结论

当前项目已具备“按赛场安排查看队员签到概览”的主要开发前提，且本次已明确首期统计口径：

- 页面签到采用赛场级签到口径，人数主事实源固定为 `competition_scene_subject_operation_state.scope_type = 'SCHEDULE'`、`operation_type = 'REPORT'`、`operation_status = 'DONE'` 的记录，并通过 `credential_id -> competition_scene_credential -> target_id/schedule_id` 回到赛场人员。
- 大赛级 `scope_type = 'COMPETITION'` 的 `REPORT` 记录全部排除，不分摊、不复用到赛场。
- `competition_scene_operation_log` 中的 `REPORT_SIGN` 流水只用于审计和最后签到时间辅助，不作为人数主事实源。
- 失败、重复、取消和仅 `SCAN` 记录均不计入已签到。
- 首期仅统计有效、已匹配的队员赛场目标；`选手/PLAYER` 按队员处理。当前无队长记录，首期去除队长相关统计。

数据库结论：建议按“B. 只需新增索引”进入 Phase 2。数据模型无需新增签到表，但需要补充概览高频查询索引；历史大赛级签到记录首期全部排除，不能分摊或复用到具体赛场。

## 2. 关键后端代码位置

- 赛场安排 Controller：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java`
- 赛场安排 Service：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneScheduleService.java`
- 赛场安排实现：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneScheduleServiceImpl.java`
- 赛场安排 Mapper：`old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneScheduleMapper.xml`
- 赛场绑定对象 Mapper：`old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneScheduleTargetMapper.xml`
- 现场证件 Controller：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java`
- 现场证件实现：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialServiceImpl.java`
- 扫码核验 Controller：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneVerifyController.java`
- 一证多权旁路核验 Controller：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java`
- 扫码核验实现：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneVerifyServiceImpl.java`
- 一证多权核验实现：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImpl.java`
- 主体操作状态实现：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneSubjectOperationStateServiceImpl.java`
- 常量定义：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/contant/CompetitionSceneConstants.java`

## 3. 关键前端代码位置

- 管理端赛场安排页：`old-code-admin/src/views/tournament/sceneSchedule/index.vue`
- 管理端赛场安排 API：`old-code-admin/src/api/tournament/sceneSchedule.js`
- 管理端赛事 API：`old-code-admin/src/api/tournament/competition.js`
- 动态路由加载：`old-code-admin/src/store/modules/permission.js`
- 权限指令：`old-code-admin/src/directive/permission/hasPermi.js`
- ECharts 示例：`old-code-admin/src/views/monitor/cache/index.vue`
- H5/小程序扫码页：`old-code-mini/pages/scan/result.vue`
- H5/小程序扫码 API：`old-code-mini/api/scan.js`
- 我的现场证件页：`old-code-mini/pages/my-credential/index.vue`

## 4. 相关数据库表

- `competition_scene_schedule`：赛场安排主表，一条记录建议对应一张签到概览赛场卡片。
- `competition_scene_schedule_target`：赛场绑定对象表，首期应以有效参赛人员记录作为应签到人员。
- `competition_scene_credential`：现场证件实例表，包含 `schedule_id`、`target_id`、`report_status/report_time`、`waiting_status/waiting_time`。
- `competition_scene_subject_operation_state`：主体操作状态事实源，支持 `scope_type/scope_ref_id/subject_type/subject_code/operation_type/operation_status`。
- `competition_scene_operation_log`：扫码/确认/取消流水，真实库已包含 `result_status`、`operation_code`、`idempotency_key` 等源码实体未完全覆盖的字段。
- `competition_apply_info`：报名人员与团队源数据。
- `competition_series_info`：赛事系列，关联 `competition_id`。
- `competition_main_info`：赛事主表。
- `competition_track_info`：赛道/赛项配置。
- `sys_menu`、`sys_role_menu`：后台菜单与角色授权。
- `sys_dict_data`、`sys_dict_type`：系统字典。

## 5. 现有签到业务链路

1. 管理端在“现场安排配置”中维护 `competition_scene_schedule`。
2. 管理端通过自动导入或手工绑定生成 `competition_scene_schedule_target`。
3. 管理端生成现场证件，写入 `competition_scene_credential`。赛场级证件有 `scope_type = 'SCHEDULE'`、`scope_ref_id = schedule_id`、`schedule_id`、`target_id`。
4. H5/小程序扫码调用 `/competition/sceneVerify/scan`，核验证件、报名、赛场和操作者角色。
5. 工作人员确认操作调用 `/competition/sceneVerify/confirm`。
6. 确认成功时：
   - 写入或复用 `competition_scene_subject_operation_state` 的 `DONE` 状态。
   - 更新 `competition_scene_credential.report_status/report_time` 或 `waiting_status/waiting_time`。
   - 写入 `competition_scene_operation_log`。
7. 取消操作会将状态表记录更新为 `CANCELLED`，并重置证件上的对应状态。

## 6. 签到记录与赛场关联方式

可用关联路径：

- 推荐事实源：`competition_scene_subject_operation_state`
  - 赛场级签到：`scope_type = 'SCHEDULE'`、`scope_ref_id = schedule_id`、`operation_type = 'REPORT'`、`operation_status = 'DONE'`、`deleted = 0`。
  - 通过 `credential_id` 关联 `competition_scene_credential.credential_id`，再取 `schedule_id`、`target_id`、`member_id`、`user_id`、`team_code`。
- 辅助字段：`competition_scene_credential`
  - `schedule_id + target_id` 对应绑定人员。
  - `report_status = '1'`、`report_time` 不作为人数主事实源；如需使用，只能作为异常排查、历史核对或最后签到时间辅助线索。
- 审计流水：`competition_scene_operation_log`
  - 有 `schedule_id`、`target_id`、`credential_id`。
  - 只能用于最后签到时间、操作审计、异常排查；不能直接把 `SCAN` 或 `DUPLICATE` 算成已签到。

判断：当前数据模型可以基于“人员 + 赛场安排”准确统计赛场级签到状态，但人数主事实源只能使用赛场级 `SCHEDULE + REPORT + DONE` 状态。大赛级 `COMPETITION/REPORT` 记录不能准确分配到具体赛场。

## 7. 队员角色识别方式

代码常量：

- 队员：`CompetitionSceneConstants.TARGET_ROLE_MEMBER = "MEMBER"`

代码中已有归一化逻辑，支持中文和英文别名：

- `CompetitionSceneScheduleServiceImpl.normalizeTargetRole`
- `CompetitionSceneCredentialServiceImpl.normalizeTargetRole`
- `CompetitionSceneVerifyServiceImpl.normalizeTargetRole`

真实库验证：

- `competition_apply_info.competition_role_name` 存在：`队员`、`指导教师`、`队长`、`选手`。
- `competition_scene_schedule_target.competition_role_name` 存在：`队员`、`选手`、`MEMBER`、`CHECKIN_STAFF`、`MATERIAL_STAFF`、`VOLUNTEER`。
- 当前真实库的赛场绑定目标中未查到 `队长/CAPTAIN`。首期按业务确认去除队长相关统计，只统计队员口径。
- `队员`、`MEMBER`、`选手`、`PLAYER` 统一归一为队员；`指导教师`、`CHECKIN_STAFF`、`MATERIAL_STAFF`、`VOLUNTEER` 等排除。

## 8. 赛事层级结构

- `competition_main_info.competition_id`：赛事主表。
- `competition_series_info.competition_series_id`：赛事系列/届次，含 `competition_id`。
- `competition_track_info.competition_track_id`：赛道/赛项，含 `competition_series_id`。
- `competition_scene_schedule.competition_series_id`：赛场安排直接关联赛事系列。
- `competition_scene_schedule.competition_track_id`、`second_level_code`：赛道/组别快照字段。

当前赛场安排没有直接 `competition_id` 字段，前端可通过 `competition_series_info` 或现有赛事选择接口补充赛事主表信息。

## 9. 可复用接口和组件

- 赛事选择数据：`getSelectCompetitionList()`，已在赛场安排页用于 `competitionOptions`。
- 赛道/赛项数据：`listCompetitionTracks(params)`。
- 统一请求：`old-code-admin/src/utils/request`。
- 统一分页：`TableDataInfo` + 前端 `Pagination` 组件。
- 权限：`@RequiresPermissions` + `v-hasPermi`。
- 图表：已安装并使用 `echarts@5.6.0`，不需要引入新图表库。
- 自动刷新能力：已安装 `@vueuse/core@13.3.0`，可使用 `useIntervalFn`、`useDocumentVisibility`，也可沿用现有 `setInterval` 清理模式。
- UI：管理端使用 Element Plus；H5/移动可继续使用响应式 CSS，不需要新框架。

## 10. 当前数据模型问题

1. `competition_scene_subject_operation_state` 源码实体未覆盖真实库新增的取消字段，后续如果需要展示取消详情需补实体字段。
2. `competition_scene_operation_log` 源码实体/XML 未覆盖真实库的 `operation_code`、`action_type`、`result_status`、`idempotency_key` 等字段。
3. 真实库 `competition_scene_schedule_target.del_flag` 存在值 `2`，源码常量只定义 `0/1`。
4. 真实库 `competition_scene_schedule_target.credential_type` 存在 `PLAYER`，源码常量未覆盖。
5. 当前赛场绑定目标中未见 `队长/CAPTAIN`，首期已确定去除队长相关统计。
6. `competition_scene_schedule` 缺少 `contest_start_time/report_location` 组合索引，不利于高频筛选。

## 11. 潜在统计风险

- 把 `COMPETITION/REPORT` 误当赛场签到，会造成同一人员多赛场重复算已签到。
- 把 `operation_log` 的 `SCAN` 或 `DUPLICATE` 计为已签到，会高估签到数。
- 使用姓名唯一识别人员会导致同名人员误合并，禁止使用。
- 当前数据中同一 `target_key` 会出现在多个 `schedule_id` 下，这是正常多赛场参与场景，统计必须带 `schedule_id`。
- 赛场安排可能存在同一开始时间、同一地点多个赛场，应以 `schedule_id` 为卡片主键。
- `target_type` 大量为空，不能依赖它判断队员范围。

## 12. 性能风险

- 概览页不能逐赛场、逐人员循环查询。
- 默认不能无条件扫全量历史赛场和报名表。
- `competition_apply_info` 约 12.4 万行，已有组合索引 `idx_apply_condition`，但概览不应每 30 秒回查报名表大范围数据。
- 高频统计应从 `competition_scene_schedule_target`、`competition_scene_credential`、`competition_scene_subject_operation_state` 聚合。
- `competition_scene_schedule` 的时间、地点筛选索引不足。

## 13. 必须回答的问题

1. 签到记录是否能够准确关联到 `schedule_id`：可以，但必须使用赛场级 `scope_type='SCHEDULE'`、`operation_type='REPORT'`、`operation_status='DONE'` 状态；大赛级签到不能分摊，流水不作为人数主事实源。
2. 是否可以区分同一人员的多个赛场签到：可以，统计键必须包含 `schedule_id`，建议 `schedule_id + target_id`。
3. 是否可以稳定识别队长和队员：首期不统计队长；队员可通过 `队员/MEMBER/选手/PLAYER` 归一化稳定识别。
4. 是否可以准确计算赛场应签到人数：可以，使用 `competition_scene_schedule_target` 中 `del_flag='0'`、`status='0'`、`match_status='MATCHED'` 且角色归一为队员的记录。
5. 是否存在重复统计风险：存在，主要来自大赛级签到复用、同一人员多赛场、流水重复、取消记录。
6. 是否需要数据库变更：不需要新增业务表或签到关联字段。
7. 是否需要新增索引：需要。
8. 当前项目是否具备进入正式开发阶段的条件：具备。签到口径、事实源、排除规则、`PLAYER/选手` 归属和去除队长统计均已明确。
9. 推荐下一开发包：Phase 2A 数据库索引和后端统计基础。
10. 下一包前需人工确认：角色菜单授权范围、默认赛事/日期筛选规则、预警阈值是否采用设计默认值。

## 14. 已执行验证

- 后端编译：`mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`，通过。
- 前端构建：`npm run build:prod`，通过；存在既有 eval/sourcemap/大包体积警告。
- 真实数据库只读检查：已连接 Docker 容器 `dev-mysql80` 的 `jiaoxue_test`，仅执行 `SHOW`、`SELECT`。
- 已检查菜单 SQL、真实菜单、图表依赖、响应式样式、扫码链路、角色值、索引。
