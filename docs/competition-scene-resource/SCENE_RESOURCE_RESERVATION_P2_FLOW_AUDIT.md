# 资源预约第二包主流程审计

## 1. 当前可预约资源列表算法

入口：`UserCompetitionSceneResourceController#bookableList`

服务方法：`UserCompetitionSceneResourceServiceImpl#selectBookableResourceList`

当前流程：

1. 按查询条件构造 `CompetitionSceneScheduleResourceQuery`。
2. 强制 `bookingStatus = OPEN`。
3. 查询 `competition_scene_schedule_resource` 资源布置列表。
4. 对每个资源布置，按资源部署赛场 `scheduleResource.scheduleId` 调用 `resolveSubject(scheduleId, userId, false)`。
5. 当前用户必须是该部署赛场 target，且必须有有效 credential。
6. 构造 `CompetitionSceneResourceBookableVO`。
7. 查询该资源的 OPEN slot，取最近一个 slot。
8. 仅当存在下一 slot 或存在同资源预约时返回。

问题：

- 当前用资源部署赛场解析用户主体，无法支持“资源额外绑定其他赛场人员可预约”。
- 未读取 `competition_scene_resource_schedule_scope`。
- 未按用户所属赛场 `allowed_schedule_id` 过滤资源。
- 仍依赖有效 credential，不符合第二包仅基于有效 schedule target 的主流程边界。
- 已有预约只查“同 schedule_resource + subject”，不是同 `competition_series_id + subject`。

## 2. 当前 slot 列表算法

入口：`UserCompetitionSceneResourceController#slotList`

服务方法：`UserCompetitionSceneResourceServiceImpl#selectBookableSlotList`

当前流程：

1. 读取 `scheduleResourceId` 对应资源布置。
2. 校验资源布置 OPEN 且预约窗口开放。
3. 按资源部署赛场 `scheduleResource.scheduleId` 解析用户主体。
4. 校验有效 credential。
5. 查询 `slot_status = OPEN` 的 slot。
6. 过滤 `end_time > now`。
7. 过滤 `remaining_device_count > 0`。

问题：

- 未校验用户来源赛场是否命中资源范围。
- 未读取 `competition_scene_resource_slot_group_scope`。
- 时间判断使用 slot 结束时间，第二包要求使用 `now < slot_start_time`。
- 共享占用时不应以 `remaining_device_count > 0` 作为唯一展示条件。
- VO 未填充 `allowedGroupNames` / `disabledReason`。

## 3. 当前预约提交算法

入口：`UserCompetitionSceneResourceController#reserve`

服务方法：`UserCompetitionSceneResourceServiceImpl#submitReservation`

当前流程：

1. 校验 `slotId` 和 `idempotencyKey` 非空。
2. 按 `operatorUserId + idempotencyKey` 查询预约记录，存在则直接返回。
3. 查询 slot。
4. 判断 slot `end_time > now` 且 `slot_status = OPEN`。
5. 查询 slot 所属 `schedule_resource`。
6. 校验资源布置 OPEN 且预约窗口开放。
7. 按资源部署赛场解析用户主体。
8. 校验有效 credential。
9. 查询“同 schedule_resource + subject”的有效预约，存在则抛出已预约异常。
10. 按团队人数和每台工位数计算 `reservedDeviceCount`。
11. 按 `reservedDeviceCount * workstationsPerDevice` 计算 `coveredWorkstationCount`。
12. 先检查 slot 当前剩余设备/工位。
13. 非共享资源额外检查该 slot 是否已有有效预约。
14. 调用 `reserveCompetitionSceneResourceSlotCapacity` 条件 UPDATE 扣减容量。
15. 插入 reservation。
16. 查询并返回预约 VO。

问题：

- 未校验资源允许赛场范围。
- 未校验 slot 允许组别。
- 重复预约判断不是同 `competition_series_id` 下主体一次。
- 未生成/写入 `active_reservation_key`。
- 插入前存在容量预检查，不是完全依赖条件 UPDATE。
- 先扣容量后插预约，active/idempotency 唯一冲突时需要额外回补；第二包应改为插预约成功后再原子扣减，扣减失败事务回滚。
- 共享占用仍会扣设备数。
- 非共享占用使用“slot 已有预约即排他”逻辑，不符合按整台设备数计算的模型。
- 未写入 `competition_series_id`、`reservation_source_schedule_id`、`operator_name`、`group_code`、`group_name`、`occupy_people_count`、`reserved_workstation_count`、`shared_occupancy_snapshot`、`workstation_count_snapshot`、`active_reservation_key`。

## 4. 当前取消预约算法

入口：`UserCompetitionSceneResourceController#cancel`

服务方法：`UserCompetitionSceneResourceServiceImpl#cancelReservation`

当前流程：

1. 查询预约记录。
2. 校验预约存在且状态为 `RESERVED`。
3. 按预约记录 `scheduleId` 解析当前用户主体。
4. 校验当前用户主体与预约主体一致。
5. 条件更新预约状态为 `CANCELLED`，并置空 `active_reservation_key`。
6. 只在影响行数大于 0 后调用容量回补 SQL。
7. 回补使用 `reservedDeviceCount` 和 `coveredWorkstationCount`。
8. 查询并返回取消后的预约 VO。

问题：

- 未校验 `slot_start_time` 未开始。
- 用户主体解析仍绑定预约部署赛场，不适配额外绑定来源赛场。
- 回补使用 `coveredWorkstationCount`，未优先使用 `reserved_workstation_count` 快照。
- 回补 SQL 总是回补设备数和工位数，未区分共享/非共享快照。
- FULL 恢复 OPEN 逻辑存在，但未区分管理员 CLOSED；当前 SQL 只在 FULL 时改 OPEN，CLOSED 不会恢复。

## 5. 当前我的预约算法

入口：`UserCompetitionSceneResourceController#myReservationList`

服务方法：`UserCompetitionSceneResourceServiceImpl#selectMyReservationList`

当前流程：

1. 调用 `resolveAllSubjects(userId)` 查询当前用户所有有效 target 并解析主体。
2. 根据 `(schedule_id, subject_type, subject_code)` 查询可见预约。
3. 返回预约 VO。

问题：

- 团队成员可见性已有基础能力。
- 查询条件仍包含 `schedule_id`，同队在同一赛事下但来源赛场/部署赛场不一致时容易漏查。
- VO 第一包已补字段，但查询和业务填充还需确保 `operator_name`、占用人数、占用设备/工位、来源赛场、组别信息完整。

## 6. 当前容量扣减 SQL

方法：`CompetitionSceneResourceSlotMapper#reserveCompetitionSceneResourceSlotCapacity`

当前 SQL：

- 同时增加 `reserved_device_count` 和 `reserved_workstation_count`。
- 同时扣减 `remaining_device_count` 和 `remaining_workstation_count`。
- 条件包含 `slot_status = 'OPEN'`、剩余设备足够、剩余工位足够。
- `slot_status` 在剩余设备或工位为 0 时置为 `FULL`。

问题：

- 共享占用也会扣设备，不符合第二包要求。
- 需要拆分共享/非共享两个条件 UPDATE。

## 7. 当前容量回补 SQL

方法：`CompetitionSceneResourceSlotMapper#releaseCompetitionSceneResourceSlotCapacity`

当前 SQL：

- 同时减少已预约设备/工位。
- 同时回补剩余设备/工位。
- 回补上限使用 `device_capacity` / `workstation_capacity`。
- `slot_status = FULL` 时恢复为 `OPEN`，其他状态保持不变。

问题：

- 共享占用取消时不应回补设备。
- 应按 reservation 快照 `reserved_device_count`、`reserved_workstation_count`、`shared_occupancy_snapshot` 回补。

## 8. 当前重复预约判断

当前使用：

- `selectEffectiveReservationByScheduleResourceAndSubject(scheduleResourceId, subjectType, subjectCode, now)`

问题：

- 只限制同一资源布置重复预约。
- 第二包要求同一 `competition_series_id` 下每队/每人只有一次有效预约。
- 第一包已新增 `active_reservation_key` 唯一索引，本包需要生成 `RESV:{competition_series_id}:{subject_type}:{subject_code}` 并写入。

## 9. 当前幂等处理

当前使用：

- `selectReservationByIdempotencyKey(operatorUserId, idempotencyKey)`
- migration 已有 `uk_scene_resource_idempotency_key(idempotency_key)`

问题：

- 查询包含 `operator_user_id`，而唯一索引是全局 `idempotency_key`。
- 插入唯一冲突尚未捕获并转换为返回原预约结果。
- 幂等键已要求非空，但需要在主流程最前置。

## 10. 本包需要修改的文件清单

预计修改：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceReservationMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceReservationMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceBookableVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotVO.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImplTest.java`

可能补充：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/contant/CompetitionSceneResourceConstants.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceReservationBaseMethodTest.java`

用户端预约 Controller 现有接口路径可以复用，本包暂不需要修改 Controller。
