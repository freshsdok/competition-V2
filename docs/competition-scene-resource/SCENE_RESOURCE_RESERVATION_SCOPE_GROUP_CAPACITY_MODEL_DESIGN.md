# 资源预约范围、组别与容量模型设计

更新时间：2026-07-06

## 1. 当前资源预约实现审计

审计范围：

- 后端用户端预约：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- 后端管理端资源布置与时段：`CompetitionSceneScheduleResourceServiceImpl.java`、`CompetitionSceneResourceSlotServiceImpl.java`
- MyBatis Mapper：`CompetitionSceneScheduleResourceMapper.xml`、`CompetitionSceneResourceSlotMapper.xml`、`CompetitionSceneResourceReservationMapper.xml`
- 数据库脚本：`db/migration/20260701_competition_scene_resource_p1_001.sql`、`db/migration/20260629_competition_scene_credential.sql`
- 管理端：`old-code-admin/src/views/tournament/sceneSchedule/components/*`
- 用户端小程序：`old-code-mini/pages/scene-resource/index.vue`
- 用户端 PC：`old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`
- 单测：`UserCompetitionSceneResourceServiceImplTest.java`

当前实现已经具备资源台账、赛场资源布置、预约时段、预约记录、用户端可预约资源列表、slot 列表、提交预约、我的预约、取消预约等基础链路。

当前实现的主要问题：

- 仍以证件有效性作为预约门槛，`submitReservation`、列表和详情都会调用 `requireValidCredential` / `hasValidCredential`。
- 不接入 credential grant，但也没有按本轮新边界改成“有效赛场 target + 资源允许赛场范围 + slot 组别范围”。
- 可预约资源来自 `competition_scene_schedule_resource.schedule_id` 直接绑定的赛场资源布置，不支持额外绑定赛场。
- slot 不支持允许组别。
- 人员组别没有进入预约主体模型。
- 同队/本人重复预约只限制在同一个 `schedule_resource_id` 下，单测还明确验证“同一主体可以预约同赛场另一个资源”，不满足“同一赛事/大赛只有一次有效资源预约机会”。
- active reservation 没有唯一键，不能防住同队多个成员并发预约。
- 共享占用算法会扣减设备数，不符合新口径。
- 非共享占用算法用“slot 是否已有预约”做排他，不支持同一 slot 内多台设备被不同队按整台占用。
- 预约时间当前看资源预约开放/关闭窗口和 slot end_time，提交时没有按 `now < slot_start_time` 判断。
- 取消能避免重复回补，但没有清空 `active_reservation_key`，因为当前表没有该字段。

## 2. 当前资源是否必须部署到赛场后才可预约

当前是。

用户端可预约资源列表查询的是 `competition_scene_schedule_resource`，且只取 `booking_status=OPEN` 的赛场资源布置。slot 和 reservation 都通过 `schedule_resource_id` 关联到该布置记录。也就是说资源台账 `competition_scene_resource` 本身不能直接预约，必须先在某个赛场安排下形成一条 `competition_scene_schedule_resource`。

需要保留这个边界：只有部署后的 `schedule_resource_id` 才能发布 slot 和产生预约。

## 3. 当前资源和赛场是什么绑定关系

当前是单表直接绑定：

- `competition_scene_schedule_resource.schedule_id`：部署资源的赛场安排。
- `competition_scene_schedule_resource.resource_id`：资源台账 ID。
- `competition_scene_resource_slot.schedule_resource_id`：slot 属于某个赛场资源布置。
- `competition_scene_resource_reservation.schedule_resource_id`：预约属于某个赛场资源布置。
- `competition_scene_resource_reservation.schedule_id`：当前保存的是部署资源的赛场安排 ID。

这个模型只能表达“某资源部署在某一个赛场安排上”。用户端解析预约主体时，也会以该部署赛场的 `schedule_id` 去找 `competition_scene_schedule_target`。

## 4. 当前是否支持额外绑定赛场

不支持。

当前没有 `competition_scene_resource_schedule_scope` 或类似表。一个 `schedule_resource_id` 只能通过自身 `schedule_id` 服务本赛场 target。其他赛场人员即使属于同一地点、同一赛事或被人工允许，也无法通过当前数据模型命中该资源。

## 5. 当前是否读取人员 group_code / group_name

不读取。

当前 `competition_scene_schedule_target` 表存在组别相关快照字段：

- `second_level_code` / `second_level_name`：组别编码/名称快照。
- `waiting_group_code` / `waiting_group_name`：候场分组。

但资源预约的 `CompetitionSceneReservationSubject` 只有 `scheduleId`、`targetId`、`subjectType`、`subjectCode`、`teamCode`、`teamName`、`userId`、`userName`、`participantCount`，没有 group 字段；`resolveSubject` 也没有读取或返回组别。

推荐后续统一口径：

- 优先使用报名信息或 target 快照中的赛事组别，即 `second_level_code` / `second_level_name`。
- 如业务必须使用通用字段名，可在 `competition_scene_schedule_target` 增补 `group_code` / `group_name`，并用 `second_level_code` / `second_level_name` 回填。
- 不建议把 `waiting_group_code` 当赛事组别，除非业务确认 slot 限制的是候场组。

## 6. 当前是否支持 slot 允许组别

不支持。

当前 slot 表只有时间、设备容量、工位容量、剩余容量、状态和版本字段，没有 allowed group 配置。管理端新增、编辑、批量生成 slot 时，也只有时间、设备容量、slot 状态等字段。

## 7. 当前团队预约主体如何判断

当前逻辑：

- 读取某个 `schedule_id` 下的 `competition_scene_schedule_target`。
- target 有 `team_code` 时识别为团队预约主体，`subject_type=TEAM`，`subject_code=team_code`。
- 当前登录用户只要是该团队有效报名成员，就允许以 TEAM 主体预约。
- 个人 target 识别为 `subject_type=USER`，`subject_code=userId`。
- 团队成员来自 `CompetitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCodeANoTeacher`，过滤指导老师、未支付、审核不通过等记录。

该逻辑基本符合“队内任意成员可预约，预约主体是 TEAM，操作人另记”的方向，但仍缺少：

- 来源赛场 `reservation_source_schedule_id`。
- 操作人姓名 `operator_name`。
- 组别快照。
- 同赛事/大赛级别的 active 唯一键。

## 8. 当前是否按操作人 1 人错误计算容量

团队场景当前不是按操作人 1 人计算。

当前团队预约会通过有效团队成员数计算 `participantCount`，再按 `ceil(participantCount / workstations_per_device)` 得到预约设备数。个人场景固定按 1 人。

但当前仍有两个问题：

- 没有把 `occupy_people_count` 保存到预约记录，取消和展示只能看到设备数与覆盖工位数。
- 共享占用也按整台设备推导覆盖工位，不能表达“按实际人数扣工位”。

## 9. 当前共享/非共享占用算法

当前共享占用：

- 仍使用 `calculateReservedDeviceCount = ceil(人数 / 每台设备工位数)`。
- `coveredWorkstationCount = reservedDeviceCount * workstations_per_device`。
- 扣减 `remaining_device_count` 和 `remaining_workstation_count`。

这不符合新口径。新口径下共享占用只要剩余工位足够即可，按实际人数扣工位，不应整台扣减设备。

当前非共享占用：

- 也计算 `reservedDeviceCount` 和 `coveredWorkstationCount`。
- 额外检查 `countEffectiveReservationBySlot > 0`，如果 slot 已有预约则拒绝。

这也不符合新口径。新口径下非共享不是整个 slot 独占，而是每次预约占用若干台设备的全部工位，多个队可以在同一 slot 使用不同设备，只要 `remaining_device_count` 和 `remaining_workstation_count` 足够。

## 10. 当前容量扣减和取消回补算法

当前扣减：

- `reserveCompetitionSceneResourceSlotCapacity` 是条件 UPDATE。
- 条件包含 `slot_status='OPEN'`、`remaining_device_count >= reservedDeviceCount`、`remaining_workstation_count >= reservedWorkstationCount`。
- 同时增加已预约设备/工位，减少剩余设备/工位，更新版本号。

该部分已经具备容量原子扣减基础，但共享/非共享扣减参数不符合新业务口径。

当前取消：

- 先读取预约记录。
- 条件 UPDATE 把 `reservation_status` 从 `RESERVED` 改成 `CANCELLED`。
- 只有影响行数大于 0 时才调用 slot 回补。
- 回补使用预约记录里的 `reserved_device_count` 和 `covered_workstation_count` 快照。

该部分具备“重复取消不重复回补”的基础能力。但后续需要：

- 预约记录字段统一为 `reserved_workstation_count` 或兼容 `covered_workstation_count`。
- 取消时清空 `active_reservation_key`。
- 按 `shared_occupancy_snapshot` 和快照字段展示，不重新计算容量。

## 11. 当前并发安全能力

已有能力：

- slot 容量扣减用条件 UPDATE，能防止容量字段被直接超卖。
- idempotency_key 有唯一索引，重复使用同一个 key 不会插入多条记录。
- 取消通过预约状态条件 UPDATE，重复取消不会重复回补。

不足：

- 同一团队多个成员使用不同 idempotency_key 并发预约时，没有 active 唯一键，可能产生多条有效预约。
- 同一个人重复点击如果每次生成不同 idempotency_key，后端无法仅靠幂等键合并。
- 当前同主体重复预约是“先查后插”，没有数据库唯一约束兜底。
- 非共享占用的 `countEffectiveReservationBySlot` 是先查后改，不具备并发排他语义。
- idempotency 查询按 `operator_user_id + idempotency_key`，但当前唯一索引是全局 `idempotency_key`，如果不同用户碰撞，异常处理不清晰。
- 没有 `active_reservation_key`，取消/失效后也无唯一键释放动作。

## 12. 新业务模型说明

新模型拆成四层：

1. 资源台账：`competition_scene_resource`，只维护设备或资源本体。
2. 资源部署：`competition_scene_schedule_resource`，表示资源被部署到一个来源赛场，只有部署后的资源才可预约。
3. 允许预约赛场范围：`competition_scene_resource_schedule_scope`，表示哪些赛场人员可以预约该部署资源。
4. slot 组别范围：`competition_scene_resource_slot_group_scope`，表示某个 slot 允许哪些 group_code 预约。

预约记录主体：

- 团队赛：`subject_type=TEAM`，`subject_code=team_code`。
- 个人赛：`subject_type=USER`，`subject_code` 使用稳定用户 ID 或报名成员编码。
- `operator_user_id` 只记录实际操作人。
- `reservation_source_schedule_id` 记录操作人所属赛场。
- `schedule_id` 保持为资源部署赛场，兼容现有查询。

时间口径：

- 不使用赛场实际比赛时间判断预约。
- 不接入 credential grant。
- 用户端预约只看资源是否已部署、资源是否发布、slot 是否 OPEN、当前时间是否早于 slot start_time、赛场范围和组别范围。

## 13. 推荐数据库设计

### 新增表 A：competition_scene_resource_schedule_scope

用途：表达某个部署资源允许哪些赛场 target 预约。

推荐字段：

- `scope_id` bigint PK
- `schedule_resource_id` bigint NOT NULL
- `resource_id` bigint NOT NULL
- `allowed_schedule_id` bigint NOT NULL
- `source_type` varchar(32) NOT NULL，取值 `AUTO_LOCATION` / `MANUAL_BIND`
- `enabled` tinyint(1) NOT NULL DEFAULT 1
- `create_by` varchar(64)
- `create_time` datetime
- `update_by` varchar(64)
- `update_time` datetime
- `deleted` tinyint(1) NOT NULL DEFAULT 0

推荐索引：

- `uk_scene_resource_scope(schedule_resource_id, allowed_schedule_id, deleted)`
- `idx_scene_resource_scope_allowed(allowed_schedule_id, enabled, deleted)`
- `idx_scene_resource_scope_resource(resource_id, deleted)`

第一期建议：

- 管理端手工绑定赛场。
- 创建部署资源时，至少为自身部署赛场写一条 enabled scope，避免旧行为丢失。
- `AUTO_LOCATION` 仅预留，位置自动匹配后续实现。

### 新增表 B：competition_scene_resource_slot_group_scope

用途：表达某个 slot 允许哪些组别预约。

推荐字段：

- `id` bigint PK
- `slot_id` bigint NOT NULL
- `schedule_resource_id` bigint NOT NULL
- `allowed_group_code` varchar(64) NOT NULL
- `allowed_group_name` varchar(255)
- `enabled` tinyint(1) NOT NULL DEFAULT 1
- `create_by` varchar(64)
- `create_time` datetime
- `update_by` varchar(64)
- `update_time` datetime
- `deleted` tinyint(1) NOT NULL DEFAULT 0

推荐索引：

- `uk_scene_resource_slot_group(slot_id, allowed_group_code, deleted)`
- `idx_scene_resource_slot_group_slot(slot_id, enabled, deleted)`
- `idx_scene_resource_slot_group_schedule_resource(schedule_resource_id, enabled, deleted)`

### slot 容量字段

当前 slot 字段：

- `device_capacity`：相当于总设备容量。
- `workstation_capacity`：相当于总工位容量。
- `remaining_device_count`
- `remaining_workstation_count`
- `reserved_device_count`
- `reserved_workstation_count`

建议后续 migration 二选一：

方案 1：兼容现有字段，不改名。

- `device_capacity` 继续作为 `total_device_count`。
- `workstation_capacity` 继续作为 `total_workstation_count`。
- 每台工位数继续从 `competition_scene_schedule_resource.workstations_per_device` 获取。
- 代码层 VO 补充别名字段，减少 SQL 改造。

方案 2：补充新字段，语义更清晰。

- `total_device_count`，用 `device_capacity` 回填。
- `workstation_count`，保存 slot 创建时每台设备工位快照。
- `total_workstation_count`，用 `workstation_capacity` 回填。
- 保留旧字段一段时间，避免管理端旧查询断裂。

推荐采用方案 2，但短期兼容旧字段。

### reservation 字段

当前缺失或需要补充的字段：

- `competition_series_id`
- `reservation_source_schedule_id`
- `operator_name`
- `group_code`
- `group_name`
- `occupy_people_count`
- `reserved_workstation_count`，可从当前 `covered_workstation_count` 迁移或兼容映射。
- `shared_occupancy_snapshot`
- `workstation_count_snapshot`
- `active_reservation_key`
- `reservation_status` 状态值扩展或规范。

当前已存在：

- `subject_type`
- `subject_code`
- `operator_user_id`
- `reserved_device_count`
- `idempotency_key`

推荐唯一约束：

- `uk_scene_resource_active_reservation_key(active_reservation_key)`
- `uk_scene_resource_idempotency_key(idempotency_key)`

说明：

- MySQL 唯一索引允许多条 NULL，因此 `active_reservation_key` 只在有效预约状态下非空。
- 取消、失效、删除后必须置空 `active_reservation_key`。
- 取消状态值统一为 `CANCELLED`，后端和对外文档保持一致，避免全量改旧数据。

## 14. 推荐预约算法

1. 校验登录用户有效。
2. 解析用户所属有效 target，得到 `scheduleId`、`competitionSeriesId`、`teamCode`、`roleCode`、`groupCode`、`groupName`。
3. 加载 slot，要求 `slot_status=OPEN` 且 `now < slot.start_time`。
4. 加载 `schedule_resource`，确认资源部署存在且发布状态允许预约。
5. 校验 slot 属于该 `schedule_resource`。
6. 校验用户所属 `scheduleId` 命中 `competition_scene_resource_schedule_scope.allowed_schedule_id`。
7. 校验 slot group scope：未配置则不限组别；已配置则 `group_code` 必须命中 enabled scope。
8. 解析预约主体：
   - 团队：`subject_type=TEAM`，`subject_code=team_code`。
   - 个人：`subject_type=USER`，`subject_code=userId/memberCode`。
9. 团队预约计算全队有效成员数，个人预约为 1。
10. 生成 `active_reservation_key = RESV:{competition_series_id}:{subject_type}:{subject_code}`。
11. 幂等键存在时先按 `idempotency_key` 查询并返回原预约。
12. 计算容量快照。
13. 在事务内用 active key 唯一约束兜底同队/本人一次预约。
14. 使用条件 UPDATE 原子扣减 slot 容量。
15. 写入预约记录或在插入前创建预约记录并让容量扣减失败时事务回滚。
16. 返回预约结果。

共享占用：

- `reserved_workstation_count = occupy_people_count`
- `reserved_device_count = ceil(occupy_people_count / workstation_count)`，仅作为预约快照和展示。
- 容量条件：`remaining_workstation_count >= reserved_workstation_count`
- slot 不扣减 `remaining_device_count`

非共享占用：

- `reserved_device_count = ceil(occupy_people_count / workstation_count)`
- `reserved_workstation_count = reserved_device_count * workstation_count`
- 容量条件：`remaining_device_count >= reserved_device_count AND remaining_workstation_count >= reserved_workstation_count`
- 同时扣减设备和工位。

取消：

- 只能取消 `RESERVED` 的预约。
- 条件 UPDATE：`reservation_status='CANCELLED'`，`active_reservation_key=NULL`。
- 只有影响行数为 1 才回补容量。
- 按预约记录里的设备/工位快照回补，不重新计算。

## 15. 推荐管理端改造

资源布置页面：

- 新增“允许预约赛场范围”配置入口。
- 展示当前部署赛场和额外绑定赛场。
- 支持新增/删除 enabled scope。
- 第一期手工绑定赛场即可，`AUTO_LOCATION` 字段预留。

slot 页面：

- 批量生成 slot 时选择允许组别，可多选。
- 单个 slot 新增/编辑允许组别。
- slot 列表展示允许组别，未配置显示“不限组别”。
- slot 容量展示区分总设备、剩余设备、总工位、剩余工位。

预约记录页面：

- 当前管理端没有预约记录列表，需要新增或补齐。
- 展示预约主体、操作人、来源赛场、组别、占用人数、占用设备数、占用工位数、共享占用快照。
- 支持按资源、slot、来源赛场、组别、主体、状态筛选。

## 16. 推荐用户端改造

资源列表：

- 只展示当前用户所属赛场允许预约的资源。
- 如果本队/本人已有有效预约，展示已有预约信息。
- 队友进入时可看到本队已有预约。

slot 列表：

- 只展示当前用户 `group_code` 可预约的 slot。
- 未配置组别的 slot 对所有命中资源范围的 target 开放。
- 展示剩余工位、共享/非共享规则、预计占用人数/设备/工位。

提交与取消：

- 提交按钮增加 loading/防抖。
- 每次确认预约生成并复用一次 `idempotency_key`，直到请求完成。
- 服务端返回 `ALREADY_RESERVED` 或兼容的 `ALREADY_RESERVED_BY_SUBJECT` 时展示已有预约。
- 取消后刷新资源、slot 和我的预约。

## 17. 风险点和任务拆分

风险点：

- active key 引入后会改变既有单测和业务行为，旧逻辑允许同主体预约同赛事下多个资源。
- 当前 `schedule_id` 在预约表中表示部署赛场，新增 `reservation_source_schedule_id` 后需要避免查询语义混淆。
- 组别字段口径需要业务确认：使用报名组别 `second_level_code`，还是新增通用 `group_code`。
- 共享占用不扣设备后，slot 的 `reserved_device_count` 汇总含义需要明确，避免展示和容量字段不一致。
- 取消状态统一为 `CANCELLED`，需要关注历史文档或旧调用方的拼写差异。
- 如果预约过期后要释放 active key，需要明确由定时任务、状态刷新还是人工关闭处理。

任务拆分建议：

1. DB migration：新增 scope 表、slot group scope 表、预约快照和唯一键字段，补齐 slot 容量字段或别名。
2. 后端领域模型：新增 scope/group mapper、domain、VO 字段，调整 subject 解析为包含来源赛场和组别。
3. 后端用户接口：改资源列表、slot 列表、提交、我的预约、取消。
4. 后端并发：实现 active key、idempotency、容量条件 UPDATE、取消条件 UPDATE。
5. 管理端：资源允许赛场配置、slot 组别配置、预约记录展示。
6. 用户端 PC/小程序：列表过滤展示、已有预约提示、防抖与幂等键。
7. 测试：覆盖权限范围、团队规则、容量算法、并发、时间状态。
8. 构建：后端 `mvn -pl teaching-modules/teaching-competition -am compile -DskipTests`，管理端 `npm run build:stage`，PC `npm run build`。
