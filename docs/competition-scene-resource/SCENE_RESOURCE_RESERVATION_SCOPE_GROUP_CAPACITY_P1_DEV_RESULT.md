# 资源预约范围/组别/容量第一包开发结果

## 1. 第一包范围

本次按已确认的第一包边界完成资源预约范围、slot 组别、容量快照和幂等/唯一键基础能力建设：

- 新增资源允许预约赛场范围表、slot 允许组别表。
- 补充 slot 容量总量/每台工位快照字段映射。
- 补充 reservation 预约主体、来源赛场、组别、占用人数、占用容量快照、有效预约唯一键字段映射。
- 新增范围/组别基础 domain、mapper、service。
- 在用户端预约服务中新增私有基础算法方法，但不改造主预约提交流程。
- 增加单元测试覆盖范围、组别、active key、idempotency、共享/非共享容量快照。

本包未接入一证多权、未接入 credential grant、未修改扫码代码、未修改 PC/小程序页面，也未把新校验直接接入预约提交主流程。

## 2. 修改文件清单

### 文档

- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_SCOPE_GROUP_CAPACITY_MODEL_DESIGN.md`
- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_CONCURRENCY_AND_LOCK_MODEL.md`
- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_SCOPE_GROUP_CAPACITY_P1_DEV_RESULT.md`

### Migration

- `db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql`

### 新增后端文件

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceScheduleScope.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotGroupScope.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceScheduleScopeMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotGroupScopeMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneResourceScheduleScopeService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneResourceSlotGroupScopeService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceScheduleScopeServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotGroupScopeServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceScheduleScopeMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotGroupScopeMapper.xml`

### 修改后端文件

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneReservationSubject.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceBookableVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceReservation.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceReservationVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlot.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceReservationMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`

### 新增测试

- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneResourceScheduleScopeServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotGroupScopeServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceReservationBaseMethodTest.java`

## 3. Migration

Migration 文件：`db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql`

执行内容：

- `CREATE TABLE IF NOT EXISTS competition_scene_resource_schedule_scope`
- `CREATE TABLE IF NOT EXISTS competition_scene_resource_slot_group_scope`
- 条件补充 slot 字段：`workstation_count`、`total_device_count`、`total_workstation_count`
- 条件补充 reservation 字段：`competition_series_id`、`reservation_source_schedule_id`、`operator_name`、`group_code`、`group_name`、`occupy_people_count`、`reserved_workstation_count`、`shared_occupancy_snapshot`、`workstation_count_snapshot`、`active_reservation_key`、`idempotency_key`
- 兼容回填 slot 总量和每台工位快照。
- 兼容回填 reservation 的赛事系列、来源赛场、占用人数、占用工位、每台工位快照。
- 增加唯一索引 `uk_scene_resource_active_reservation_key(active_reservation_key)`。
- 如已有旧幂等唯一索引 `uk_scene_reservation_idempotency`，重命名为 `uk_scene_resource_idempotency_key`；否则补充 `uk_scene_resource_idempotency_key(idempotency_key)`。

未连接生产数据库，未写危险自动清理 SQL。

## 4. 新增表

### competition_scene_resource_schedule_scope

用于描述某个 `schedule_resource_id` 允许哪些赛场人员预约。

关键字段：

- `scope_id`
- `schedule_resource_id`
- `resource_id`
- `allowed_schedule_id`
- `source_type`：`MANUAL_BIND` / `AUTO_LOCATION`
- `enabled`
- `deleted`

第一期实现手工绑定 `MANUAL_BIND`，`AUTO_LOCATION` 作为位置自动匹配预留。

### competition_scene_resource_slot_group_scope

用于描述某个 slot 允许哪些组别预约。

关键字段：

- `id`
- `slot_id`
- `schedule_resource_id`
- `allowed_group_code`
- `allowed_group_name`
- `enabled`
- `deleted`

规则：slot 未配置启用组别时不限组别；已配置时必须命中 `group_code`。

## 5. 新增字段

### Slot

本包补充并映射：

- `workstation_count`
- `total_device_count`
- `total_workstation_count`

原有容量扣减字段继续沿用：

- `remaining_device_count`
- `remaining_workstation_count`

### Reservation

本包补充并映射：

- `competition_series_id`
- `reservation_source_schedule_id`
- `operator_name`
- `group_code`
- `group_name`
- `occupy_people_count`
- `reserved_workstation_count`
- `shared_occupancy_snapshot`
- `workstation_count_snapshot`
- `active_reservation_key`
- `idempotency_key`

`reservation_status` 为现有字段，本包继续沿用；取消预约 SQL 已兼容置空 `active_reservation_key`，为后续唯一键释放做准备。

## 6. 预约资格算法

本包新增基础私有方法，尚未接入提交主流程：

- `buildActiveReservationKey(Long competitionSeriesId, String subjectType, String subjectCode)`
  - 格式：`RESV:{competition_series_id}:{subject_type}:{subject_code}`
  - 用于后续支撑同赛事/大赛下每队或每人一次有效预约。
- `calculateOccupyPeopleCount(CompetitionSceneReservationSubject subject)`
  - 团队按 `participantCount`，最小为 1。
  - 个人固定为 1。
- `checkScheduleScopeAllowed(Long scheduleResourceId, Long userSourceScheduleId)`
  - 基于 `competition_scene_resource_schedule_scope` 判断用户所属赛场是否命中资源允许范围。

同时在预约主体和可预约资源 VO 中补充 `competitionSeriesId`、`roleCode`、`groupCode`、`groupName` 等基础字段，为后续主流程接入做准备。

## 7. 组别时段算法

本包新增：

- `CompetitionSceneResourceSlotGroupScopeServiceImpl#isSlotGroupAllowed`
- `UserCompetitionSceneResourceServiceImpl#checkSlotGroupAllowed`

规则：

- slot 未配置启用组别：允许预约。
- slot 已配置启用组别：`group_code` 非空且命中 `allowed_group_code` 才允许。
- 组别服务支持按 slot 替换配置，替换时逻辑删除旧配置，并对新配置按 `allowed_group_code` 去重。

## 8. 共享/非共享容量算法

本包新增 `calculateCapacitySnapshot(Integer occupyPeopleCount, Integer workstationCount, Boolean sharedOccupancy)`：

共享占用 `shared_occupancy=true`：

- `reserved_workstation_count = occupy_people_count`
- `reserved_device_count = ceil(occupy_people_count / workstation_count)`，仅作为快照和展示
- 后续接入主流程时不应整台扣减 `remaining_device_count`

非共享占用 `shared_occupancy=false`：

- `reserved_device_count = ceil(occupy_people_count / workstation_count)`
- `reserved_workstation_count = reserved_device_count * workstation_count`
- 后续接入主流程时需同时扣减设备数和工位数

本包只提供快照算法，不改造提交扣减 SQL。

## 9. 并发与幂等处理

本包完成基础模型：

- reservation 增加 `active_reservation_key` 并建立唯一索引。
- reservation 增加/确认 `idempotency_key` 并建立唯一索引。
- 新增 `checkIdempotencyKeyRequired`，空幂等键会抛出业务异常。
- 取消预约 SQL 已兼容 `active_reservation_key = null`，后续主流程接入后可释放有效预约唯一键。

本包未改造预约提交主流程，因此尚未实现：

- active key 唯一冲突返回已有预约。
- idempotency key 重复提交返回原结果。
- slot 容量条件 UPDATE 原子扣减。
- 取消只在状态更新影响 1 行后按快照回补。

这些属于下一包主流程改造内容。

## 10. 取消回补

本包仅补充字段和取消置空 `active_reservation_key` 的兼容 SQL。按 `reserved_device_count` / `reserved_workstation_count` 快照回补、重复取消不重复回补，需要在下一包改造取消主流程时完成。

## 11. 管理端改造

第一包未修改管理端页面。

已准备后端基础服务，可供后续管理端接入：

- 资源允许预约赛场范围配置。
- slot 允许组别配置。
- 批量生成/编辑 slot 时写入组别范围。
- slot 列表和预约记录展示新增字段。

## 12. 用户端改造

第一包未修改 PC/小程序用户端页面，也未调整预约提交接口行为。

已准备后端基础方法和字段，可供后续用户端接口接入：

- 当前用户所属赛场过滤资源。
- 当前用户 `group_code` 过滤 slot。
- 幂等键必传校验。
- 已有预约唯一键识别。
- 共享/非共享容量快照。

## 13. 测试结果

已执行定向单元测试：

```bash
mvn -pl teaching-modules/teaching-competition -am test -Dtest=CompetitionSceneResourceScheduleScopeServiceImplTest,CompetitionSceneResourceSlotGroupScopeServiceImplTest,UserCompetitionSceneResourceReservationBaseMethodTest -Dsurefire.failIfNoSpecifiedTests=false
```

结果：

- BUILD SUCCESS
- Tests run: 13
- Failures: 0
- Errors: 0
- Skipped: 0

覆盖点：

- 手工绑定赛场范围新增、查询、删除、命中判断。
- slot 组别范围替换、去重、查询。
- slot 未配置组别时不限组别。
- slot 已配置组别时命中/不命中判断。
- active reservation key 格式。
- idempotency key 空值拒绝。
- 团队占用人数按 `participantCount`。
- 共享占用容量快照。
- 非共享占用容量快照。
- 用户端基础私有方法对 mapper 的范围/组别判断委托。

## 14. 构建结果

已执行后端要求构建：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：BUILD SUCCESS。

本包未改管理端和 PC/小程序，因此未执行 `npm run build:stage` 和 `npm run build`。

## 15. 已知风险

- 本包只完成基础能力，预约提交、取消、我的预约、资源/slot 列表主流程尚未接入新规则。
- `active_reservation_key` 历史数据不会自动生成，需在主流程正式接入后只对有效预约写入。
- migration 增加唯一索引前，如果开发库已有重复非空 `idempotency_key` 或未来手工写入重复 `active_reservation_key`，需要人工处理数据后再执行索引。
- `AUTO_LOCATION` 仅预留字段，第一期未实现位置自动匹配。
- 团队有效成员数目前使用现有 subject 的 `participantCount` 基础字段，后续接入主流程时需确认其来源是否已过滤有效成员。

## 16. 是否可以进入联调

第一包可以进入后端第二包开发，但不建议单独进入完整业务联调。

原因：数据库模型、mapper/service、基础算法和测试已经就绪；但用户端预约主流程尚未接入范围、组别、active key、幂等、原子扣减和取消快照回补，业务行为仍需第二包完成后再联调。
