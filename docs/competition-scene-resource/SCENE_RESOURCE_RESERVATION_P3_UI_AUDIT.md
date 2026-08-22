# 资源预约第三包 UI 开发前审计

审计时间：2026-07-06  
审计范围：管理端“赛场安排 - 资源与预约”、PC 端“个人中心 - 设备预约”、P2 已通过的资源预约后端管理接口缺口。

## 1. 当前管理端资源与预约页面结构

- 资源与预约入口位于 `old-code-admin/src/views/tournament/sceneSchedule/components/ResourceReservationTab.vue`。
- 页面以当前 `schedule.scheduleId` 查询 `/competition/sceneScheduleResource/list`，展示资源布置列表。
- 当前资源列表支持新增设备布置、编辑、发布预约、暂停预约、关闭预约、配置时段、删除。
- 资源布置弹窗位于 `ScheduleResourceDialog.vue`，负责部署资源、设备数、每台工位数、预约状态、共享占用、运维信息和说明文本。
- 时段弹窗位于 `ResourceSlotDialog.vue`，负责展示某个 `schedule_resource` 下的 slot，支持新增时段、批量生成、编辑、开放/关闭、删除。
- 当前管理端没有独立预约记录 API 文件，资源预约 Tab 也没有预约记录列表入口。

## 2. 当前 slot 批量生成和编辑能力

- `ResourceSlotDialog.vue` 已支持单个 slot 新增/编辑，字段包括开始时间、结束时间、设备容量、时段状态。
- 已支持批量生成 slot，字段包括日期、开始时间、结束时间、单场时长、每时段设备容量、时段状态。
- 后端 `CompetitionSceneResourceSlotServiceImpl` 会根据部署资源自动初始化 `workstation_count`、`total_device_count`、`total_workstation_count`、剩余设备和剩余工位。
- 当前 slot 新增、编辑、批量生成均未携带允许组别字段。
- 当前管理端 slot 列表未展示 `allowedGroupNames`，虽然用户端 P2 slot VO 已有该字段。

## 3. 当前是否能配置 resource_schedule_scope

- P1/P2 已有 `CompetitionSceneResourceScheduleScope` domain、mapper、service。
- service 已支持：
  - `listByScheduleResourceId`
  - `addManualBindSchedule`
  - `removeManualBindSchedule`
  - `ensureManualBindSchedule`
- 当前缺少管理端 controller。
- 当前管理端缺少 API 文件和页面入口，不能查看、添加、移除资源允许预约赛场范围。
- 当前 scope domain/mapper 未返回 `allowedScheduleName`，管理端无法直接展示赛场名称。

## 4. 当前是否能配置 slot_group_scope

- P1/P2 已有 `CompetitionSceneResourceSlotGroupScope` domain、mapper、service。
- service 已支持：
  - `listBySlotId`
  - `listByScheduleResourceId`
  - `replaceSlotGroups`
  - `batchReplaceSlotGroups`
  - `isSlotGroupAllowed`
- 当前缺少管理端 controller。
- 当前管理端缺少 API 文件和页面入口，不能在单个 slot 或批量生成 slot 时配置允许组别。
- 当前缺少组别选项接口；可从 `competition_scene_schedule_target.second_level_code / second_level_name` distinct 读取。

## 5. 当前预约记录展示字段

- 后端 `CompetitionSceneResourceReservationMapper` 已支持按条件查询预约记录，并返回大部分 P2 字段：
  - `competitionSeriesId`
  - `reservationSourceScheduleId`
  - `subjectType / subjectCode`
  - `operatorUserId / operatorName`
  - `groupCode / groupName`
  - `occupyPeopleCount`
  - `reservedDeviceCount`
  - `reservedWorkstationCount`
  - `sharedOccupancySnapshot`
  - `workstationCountSnapshot`
- 当前未返回 `reservationSourceScheduleName`。
- 当前缺少管理端 reservation controller 和 API 文件。
- 当前管理端页面没有展示预约记录表。

## 6. 当前用户端预约入口

- 个人中心已经有“设备预约”标签，位于 `old-code-pc/src/views/personal/index.vue`。
- 设备预约页面为 `old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`。
- PC 端 API 已拆分到 `old-code-pc/src/api/personal/sceneResource.js`。
- 用户端已调用：
  - `/competition/userCompetition/sceneResource/bookableList`
  - `/competition/userCompetition/sceneResource/{scheduleResourceId}`
  - `/competition/userCompetition/sceneResourceSlot/list`
  - `/competition/userCompetition/sceneResourceReservation`
  - `/competition/userCompetition/sceneResourceReservation/myList`
  - `/competition/userCompetition/sceneResourceReservation/cancel`
- `Competition.vue` 仍是“我的赛事/现场证件”页，不直接承载资源预约；资源预约已独立成 `SceneResourceReservation.vue`。

## 7. 当前用户端是否生成 idempotency_key

- 当前预约提交会生成 `${Date.now()}-${Math.random().toString(16).slice(2)}` 作为 `idempotencyKey`。
- 当前 key 不是标准 UUID。
- 当前没有按“同一次请求重试复用同一个 key”管理 pending key。
- 当前没有全局预约按钮 pending 状态，用户可能重复点击并生成不同 key。

## 8. 当前用户端是否展示已有预约

- 可预约资源卡片已根据 `existingReservation` 显示“已预约/可预约”。
- 资源详情已通过 alert 简单展示已有预约的资源名和开始时间。
- 我的预约列表已展示资源、位置、时段、设备数、覆盖工位、状态、取消按钮。
- 当前未完整展示：
  - “本队已由 XXX 预约”
  - 预约主体 `subjectType / subjectCode`
  - 操作人 `operatorName`
  - 来源赛场 `reservationSourceScheduleId / reservationSourceScheduleName`
  - 组别 `groupCode / groupName`
  - 占用人数 `occupyPeopleCount`
  - 占用工位 `reservedWorkstationCount`
  - 允许组别 `allowedGroupNames`
- 当前仅识别旧错误码 `ALREADY_RESERVED_BY_SUBJECT`，P2 已统一为 `ALREADY_RESERVED`。

## 9. 本包需要修改文件清单

后端：

- 新增 `CompetitionSceneResourceScheduleScopeController.java`
- 新增 `CompetitionSceneResourceSlotGroupScopeController.java`
- 新增 `CompetitionSceneResourceReservationController.java`
- 补充 `CompetitionSceneResourceScheduleScope` 的 `allowedScheduleName`
- 补充 `CompetitionSceneResourceReservationVO` 的 `reservationSourceScheduleName`
- 补充 `CompetitionSceneResourceSlot` / `CompetitionSceneResourceSlotBatchReq` 的允许组别接收字段
- 补充 `CompetitionSceneResourceSlotServiceImpl` 在新增、编辑、批量生成时写入 slot group scope
- 补充 `CompetitionSceneResourceSlotServiceImpl` 管理端 slot 列表/详情的 `allowedGroupNames`
- 补充 `CompetitionSceneResourceScheduleScopeMapper` 查询赛场名称
- 补充 `CompetitionSceneResourceReservationMapper` 查询来源赛场名称
- 补充 `CompetitionSceneScheduleTargetMapper` 组别 distinct 查询

管理端：

- 新增 `old-code-admin/src/api/tournament/sceneResourceScheduleScope.js`
- 新增 `old-code-admin/src/api/tournament/sceneResourceSlotGroupScope.js`
- 新增 `old-code-admin/src/api/tournament/sceneResourceReservation.js`
- 修改 `ResourceReservationTab.vue`：增加可预约赛场范围配置和预约记录入口
- 修改 `ResourceSlotDialog.vue`：增加 slot 允许组别展示、单个编辑、批量生成配置

PC 用户端：

- 修改 `old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`
- 如需兼容统一导出，可补充 `old-code-pc/src/api/personal/index.js`，但当前预约页已直接使用 `api/personal/sceneResource.js`

不改动：

- 不修改扫码代码。
- 不接入一证多权。
- 不接入 credential grant。
- 不重写 P2 已通过的预约主流程算法。
