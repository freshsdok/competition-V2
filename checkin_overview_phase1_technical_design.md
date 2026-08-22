# 签到概览 Phase 1 技术设计方案

## 1. 功能边界

本期实现后台管理员查看，不实现代签、补签、取消、修改、通知、导出、WebSocket、历史趋势和复杂数据权限。

统计人员仅包含有效、已匹配的队员赛场目标。`选手/PLAYER` 按队员处理；当前无队长记录，首期去除队长相关统计。角色必须经后端统一归一化后判断，不能在前端按中文硬编码。

## 2. 页面结构

页面路径建议：`old-code-admin/src/views/tournament/checkinOverview/index.vue`

页面区块：

1. 顶部范围选择：赛事系列、关联赛事、比赛日期、自动刷新开关、手动刷新、最后更新时间。
2. 统计卡片：应签到人数、已签到人数、未签到人数、整体签到率、赛场总数、预警赛场数。
3. 图表区：
   - 整体签到环形图。
   - 低签到率赛场排行。
   - 按比赛开始时间统计。
4. 筛选区：签到地点、比赛开始时间范围、关联赛事、签到状态、预警等级。
5. 赛场卡片列表：分页加载。
6. 赛场详情页或详情抽屉：赛场信息、团队统计、人员明细。

## 3. 路由与菜单

建议菜单：

- 父菜单：`赛事管理`，真实库 `menu_id = 2002`。
- 菜单名：`签到概览`
- 路由路径：`checkinOverview`
- 组件：`tournament/checkinOverview/index`
- 路由名称：`CompetitionCheckinOverview`
- 权限：`competition:checkinOverview:list`

后端接口权限：

- `competition:checkinOverview:list`：概览统计、赛场列表、详情、人员列表。

本阶段不提交菜单 SQL。Phase 2F 再提供脚本，并按现有 `db/migration/20260629_competition_scene_admin_menu.sql` 风格写幂等 SQL。

## 4. 前端组件拆分

建议目录：

- `src/api/tournament/checkinOverview.js`
- `src/views/tournament/checkinOverview/index.vue`
- `src/views/tournament/checkinOverview/components/OverviewFilter.vue`
- `src/views/tournament/checkinOverview/components/StatisticCards.vue`
- `src/views/tournament/checkinOverview/components/CheckinDonutChart.vue`
- `src/views/tournament/checkinOverview/components/LowRateRankChart.vue`
- `src/views/tournament/checkinOverview/components/StartTimeBarChart.vue`
- `src/views/tournament/checkinOverview/components/ScheduleCheckinCard.vue`
- `src/views/tournament/checkinOverview/components/ScheduleDetailDrawer.vue`
- `src/views/tournament/checkinOverview/constants.js`

图表使用已安装的 `echarts`，不引入 `vue-echarts`。如果后续需要更完整生命周期封装，可先封装本地 `useEcharts` composable。

## 5. 后端设计

包路径建议沿用 `com.teaching.competition`。

Controller：

- `CompetitionSceneCheckinOverviewController`
- `@RequestMapping({"/sceneCheckinOverview", "/competition/scene/checkin-overview"})`

Service：

- `ICompetitionSceneCheckinOverviewService`
- `CompetitionSceneCheckinOverviewServiceImpl`

Mapper：

- `CompetitionSceneCheckinOverviewMapper`
- XML：`mapper/competition/CompetitionSceneCheckinOverviewMapper.xml`

不要在 Controller 中写统计逻辑；Controller 只做权限、分页、参数接收、统一返回。

## 6. DTO、VO、Query

Query：

- `CompetitionSceneCheckinOverviewQuery`
  - `pageNum`
  - `pageSize`
  - `competitionSeriesId`
  - `competitionId`
  - `competitionTrackId`
  - `startTimeBegin`
  - `startTimeEnd`
  - `checkinLocation`
  - `checkinStatus`
  - `warningLevel`

VO：

- `CompetitionSceneCheckinOverviewStatisticsVO`
- `CompetitionSceneCheckinScheduleCardVO`
- `CompetitionSceneCheckinScheduleDetailVO`
- `CompetitionSceneCheckinTeamVO`
- `CompetitionSceneCheckinPersonVO`
- `CompetitionSceneCheckinTimeGroupVO`
- `CompetitionSceneCheckinRankItemVO`

枚举/常量：

- `CheckinOverviewStatus`: `ALL/COMPLETED/PARTIAL/NOT_STARTED/WARNING`
- `CheckinPersonStatus`: `SIGNED/UNSIGNED`
- `CheckinTeamStatus`: `COMPLETED/PARTIAL/NOT_STARTED`
- `CheckinWarningLevel`: `NORMAL/YELLOW/ORANGE/RED`
- 角色归一：复用或抽取 `CompetitionSceneConstants.TARGET_ROLE_MEMBER`，并把 `队员/MEMBER/选手/PLAYER` 统一映射为队员。

## 7. 接口定义

### 7.1 概览统计

`GET /competition/scene/checkin-overview/statistics`

返回：

- `totalPersonCount`
- `signedPersonCount`
- `unsignedPersonCount`
- `checkinRate`
- `scheduleCount`
- `warningScheduleCount`
- `lastCheckinTime`
- `donut`
- `lowRateRank`
- `startTimeGroups`

建议合并为一个统计接口，理由：顶部卡片和三个图表使用同一筛选条件，合并可避免 30 秒刷新时发起多个聚合请求。

### 7.2 赛场概览分页

`GET /competition/scene/checkin-overview/schedules`

返回 `TableDataInfo`，每条记录包括题目要求的卡片字段。

首期卡片字段去除队长相关数量，只保留队员与团队统计：

- `memberTotalCount`
- `memberSignedCount`
- `teamTotalCount`
- `completedTeamCount`
- `partialTeamCount`
- `unsignedTeamCount`

### 7.3 赛场详情

`GET /competition/scene/checkin-overview/schedules/{scheduleId}`

返回赛场基本信息、签到汇总、团队统计。

### 7.4 赛场人员详情

`GET /competition/scene/checkin-overview/schedules/{scheduleId}/persons`

参数：

- `teamId/teamCode`
- `keyword`
- `role`
- `checkinStatus`

返回人员明细，默认按团队排序。

## 8. SQL 聚合方案

应签到基础集合：

```sql
select
  t.schedule_id,
  t.target_id,
  t.member_id,
  t.user_id,
  t.team_code,
  t.team_name,
  t.user_name,
  t.competition_role_name
from competition_scene_schedule_target t
where t.del_flag = '0'
  and t.status = '0'
  and t.match_status = 'MATCHED'
  and t.competition_role_name in ('队员', 'MEMBER', '选手', 'PLAYER')
```

已签到集合：

```sql
select
  c.schedule_id,
  c.target_id,
  max(st.operation_time) as checkin_time
from competition_scene_subject_operation_state st
join competition_scene_credential c
  on c.credential_id = st.credential_id
 and c.del_flag = '0'
where st.deleted = 0
  and st.scope_type = 'SCHEDULE'
  and st.scope_ref_id = c.schedule_id
  and st.operation_type = 'REPORT'
  and st.operation_status = 'DONE'
group by c.schedule_id, c.target_id
```

卡片聚合：

- `left join` 已签到集合到应签到基础集合。
- `count(distinct target_id)` 为应签到人数。
- `count(distinct signed.target_id)` 为已签到人数。
- `max(signed.checkin_time)` 为最后签到时间。

注意：状态事实源已明确为 `SCHEDULE + REPORT + DONE`。`COMPETITION/REPORT`、`SCAN`、重复、失败和取消记录均不计入人数。

## 9. 签到统计口径

- 应签到人员：有效、已匹配的赛场绑定目标，角色归一为队员。
- 队员角色：`队员`、`MEMBER`、`选手`、`PLAYER`。
- 排除角色：队长、指导教师、专家、工作人员、签到工作人员、资料发放工作人员、其他证件角色。
- 已签到人员：赛场级 `REPORT/DONE` 状态存在。
- 未签到人员：应签到集合中不存在已签到状态。
- 签到率：`signedPersonCount / totalPersonCount`，总数为 0 时返回 0。
- 取消后：`CANCELLED` 不计入已签到。
- 失败、异常、扫码通过但未确认：不计入已签到。
- 重复确认：不增加已签到人数。
- 流水表 `REPORT_SIGN`：只用于审计和最后签到时间辅助，不作为人数主事实源。

## 10. 团队统计口径

- 团队主键优先 `team_code`，空时使用 `NO_TEAM:{target_id}`。
- 团队总人数：团队下应签到人员数。
- 已签到人数：团队下已签到人员数。
- 团队状态：
  - `COMPLETED`：已签到人数 = 总人数，且总人数 > 0。
  - `PARTIAL`：0 < 已签到人数 < 总人数。
  - `NOT_STARTED`：已签到人数 = 0。

## 11. 预警规则

集中放在后端常量类，例如 `CompetitionSceneCheckinOverviewConstants`：

- `YELLOW_THRESHOLD_MINUTES = 30`
- `YELLOW_RATE = 80`
- `ORANGE_THRESHOLD_MINUTES = 10`

优先级：

`RED > ORANGE > YELLOW > NORMAL`

规则：

- `RED`：比赛已经开始且仍存在未签到人员。
- `ORANGE`：距离比赛开始小于等于 10 分钟且仍存在未签到人员。
- `YELLOW`：距离比赛开始小于等于 30 分钟且签到率低于 80%。
- `NORMAL`：其他。

系统存在 `sys_config`，但本期建议先用后端集中常量，后续再接系统参数。

## 12. 自动刷新方案

默认 30 秒刷新。

前端实现建议：

- 使用 `@vueuse/core` 的 `useIntervalFn` 和 `useDocumentVisibility`。
- 页面不可见时暂停。
- 重新可见时立即刷新一次。
- 离开页面清理。
- `loading` 或 `requestPending` 为 true 时跳过下一次自动刷新。
- 保留筛选条件。
- 只在成功响应后更新 `lastSuccessUpdateTime`。
- 图表实例复用，只调用 `setOption`，不要每次重新 `init`。

## 13. H5 适配方案

管理端页面本身做响应式：

- `<= 768px` 使用移动布局。
- 顶部筛选折叠为筛选按钮 + 抽屉。
- 统计卡片两列网格或横向滑动。
- 图表纵向排列。
- 赛场卡片单列。
- 详情人员不使用宽表格，改为团队折叠卡片 + 人员列表。
- 图表监听容器宽度变化并 resize。

## 14. 性能方案

- 统计接口使用聚合 SQL。
- 赛场列表分页聚合。
- 人员明细进入详情后再查。
- 默认要求选择赛事系列或比赛日期范围；无筛选时默认当前赛事或当前日期。
- 避免返回 `notice`、`target_snapshot_json`、`credential_snapshot_json`、`request_payload`、`response_payload` 等大字段。
- 不做 N+1 查询。

## 15. 索引建议

建议新增：

- `competition_scene_schedule`: `(competition_series_id, del_flag, status, contest_start_time)`
- `competition_scene_schedule`: `(report_location, contest_start_time)`
- `competition_scene_schedule_target`: `(schedule_id, competition_role_name, status, del_flag, match_status)`
- `competition_scene_credential`: `(schedule_id, target_id, credential_status, del_flag)`
- `competition_scene_subject_operation_state`: 现有 lookup/current 索引已覆盖核心查询，可保留。
- `competition_scene_operation_log`: 如使用最后签到时间兜底，建议 `(schedule_id, operation_type, operation_stage, operation_result, result_status, operation_time)`。

## 16. 测试策略

后端：

- 角色归一化单元测试：`队员/MEMBER/选手/PLAYER` 计入，队长/教师/工作人员排除。
- 聚合 Mapper 测试：空赛场、全签、部分签、取消、重复、同一人多赛场。
- 预警规则单元测试：30 分钟、10 分钟、已开始边界。
- 权限注解检查。

前端：

- 筛选参数组装测试。
- 图表数据为空、全签、部分签渲染。
- 自动刷新 pending 跳过。
- H5 断点截图检查。

## 17. 后续开发包拆分建议

推荐先做 Phase 2A：数据库索引、后端统计 Mapper、角色归一化与口径测试。后续再接接口、页面、详情、H5 和菜单 SQL。
