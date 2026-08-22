# 资源预约范围/组别/容量第二包开发结果

## 1. 审计结果

已完成开发前审计并输出：

- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_P2_FLOW_AUDIT.md`

审计确认原流程存在以下问题：

- 可预约资源列表按资源部署赛场解析用户主体，无法支持额外绑定赛场。
- 资源列表未读取 `competition_scene_resource_schedule_scope`。
- slot 列表未读取 `competition_scene_resource_slot_group_scope`。
- slot 时间判断使用 `end_time > now`，第二包已改为 `start_time > now`。
- 重复预约只限制同一 `schedule_resource`，第二包已改为同一 `competition_series_id + subject`。
- 原提交先扣容量再插预约，第二包已改为先插唯一键记录，再条件 UPDATE 原子扣容量，扣减失败事务回滚。
- 原共享占用会扣设备，第二包已拆分共享/非共享扣减 SQL。
- 原取消回补使用 `covered_workstation_count`，第二包已优先使用预约快照。

## 2. 修改文件清单

### 文档

- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_P2_FLOW_AUDIT.md`
- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_SCOPE_GROUP_CAPACITY_P2_DEV_RESULT.md`

### 后端 Java

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceReservationMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceBookableVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/contant/CompetitionSceneResourceConstants.java`

### MyBatis XML

- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceReservationMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`

### 测试

- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImplTest.java`

## 3. 可预约资源列表改造

方法：`UserCompetitionSceneResourceServiceImpl#selectBookableResourceList`

已改造为：

- 先解析当前用户所有有效 `schedule_target` 主体。
- 若查询参数带 `scheduleId`，按用户来源赛场过滤主体，而不是按资源部署赛场过滤资源。
- 资源仍要求 `booking_status = OPEN` 且预约窗口开放。
- 资源必须命中 `competition_scene_resource_schedule_scope.allowed_schedule_id`。
- 未配置 scope 或用户来源赛场未命中的资源不返回。
- VO 返回：
  - `competitionSeriesId`
  - `userSourceScheduleId`
  - `groupCode`
  - `groupName`
  - `hasExistingReservation`
  - `existingReservation`

本包不实现 `AUTO_LOCATION` 自动匹配，仅使用已有 scope 数据。

## 4. slot 列表改造

方法：`UserCompetitionSceneResourceServiceImpl#selectBookableSlotList`

已改造为：

- 资源详情先校验用户来源赛场命中 resource schedule scope。
- 只返回 `slot_status = OPEN` 的 slot。
- 只返回 `slot.start_time > now` 的 slot。
- 共享占用按 `remaining_workstation_count > 0` 判断展示可用。
- 非共享占用按 `remaining_device_count > 0 && remaining_workstation_count > 0` 判断展示可用。
- slot 未配置启用 group scope 时不限组别。
- slot 已配置启用 group scope 时，当前用户 `group_code` 必须命中。
- 不命中组别的 slot 直接过滤。
- VO 补充 `allowedGroupNames` 和 `disabledReason` 字段，其中 `allowedGroupNames` 已在返回 slot 时填充。

## 5. 预约提交算法

方法：`UserCompetitionSceneResourceServiceImpl#submitReservation`

新流程：

1. 校验登录用户非空。
2. 校验 `slotId` 非空。
3. 校验 `idempotency_key` 非空。
4. 按 `idempotency_key` 查询已有预约；存在则直接返回，不扣容量。
5. 查询 slot，并校验 `slot_status = OPEN` 且 `slot.start_time > now`。
6. 查询 schedule resource，并校验资源预约开放。
7. 解析当前用户有效主体。
8. 校验用户来源赛场命中 resource schedule scope。
9. 校验 slot group scope。
10. 团队按有效成员数计算 `occupy_people_count`，个人按 1。
11. 计算容量快照。
12. 生成 `active_reservation_key`。
13. 按 active key 查询已有有效预约；存在则返回 `ALREADY_RESERVED`。
14. 插入预约记录，写入第一包新增快照字段。
15. 捕获唯一键冲突：
    - `idempotency_key` 冲突：返回原预约；
    - `active_reservation_key` 冲突：返回 `ALREADY_RESERVED` 和已有预约。
16. 使用条件 UPDATE 原子扣减 slot 容量。
17. 扣减影响行数为 0 时抛出容量不足，事务回滚。
18. 返回预约成功记录。

本包未接入一证多权，未接入 credential grant，未修改扫码。

## 6. active_reservation_key 处理

生成格式：

```text
RESV:{competition_series_id}:{subject_type}:{subject_code}
```

处理方式：

- 预约记录写入 `active_reservation_key`。
- 查询有效预约使用 `selectEffectiveReservationByActiveKey`。
- 有效状态为 `RESERVED` / `CHECKED`。
- 取消时置空 `active_reservation_key`，释放唯一键。
- 主流程同时保留插入前查询和数据库唯一索引兜底；并发冲突时以唯一索引为最终防线。

## 7. idempotency_key 处理

处理方式：

- 提交最前置校验 `idempotency_key` 非空。
- 预约提交前按 `idempotency_key` 查询已有记录，存在则直接返回。
- 插入时写入 `idempotency_key`。
- 捕获 `uk_scene_resource_idempotency_key` 类唯一冲突后，再按 `idempotency_key` 查询并返回原记录。
- 重复提交不会重复扣减容量。

## 8. 共享/非共享容量扣减

新增 mapper 方法：

- `reserveSharedCompetitionSceneResourceSlotCapacity`
- `reserveExclusiveCompetitionSceneResourceSlotCapacity`

共享占用：

- `reserved_workstation_count += occupy_people_count`
- `remaining_workstation_count -= occupy_people_count`
- 不扣 `remaining_device_count`
- 条件包含：
  - `slot_status = 'OPEN'`
  - `start_time > now`
  - `remaining_workstation_count >= reservedWorkstationCount`

非共享占用：

- `reserved_device_count += ceil(occupy_people_count / workstation_count)`
- `remaining_device_count -= reserved_device_count`
- `reserved_workstation_count += reserved_device_count * workstation_count`
- `remaining_workstation_count -= reserved_workstation_count`
- 条件包含：
  - `slot_status = 'OPEN'`
  - `start_time > now`
  - `remaining_device_count >= reservedDeviceCount`
  - `remaining_workstation_count >= reservedWorkstationCount`

slot 容量扣减影响行数为 0 时，返回容量不足或时段不可预约，事务回滚预约插入。

## 9. 取消预约回补

方法：`UserCompetitionSceneResourceServiceImpl#cancelReservation`

已改造为：

- 查询预约记录后，按 `reservation_source_schedule_id` 优先解析当前用户主体。
- 校验当前用户是该预约主体成员或本人。
- 已取消/非 `RESERVED` 状态直接返回当前记录，不回补容量。
- `RESERVED` 状态下校验 `slot.start_time > now`。
- 条件更新预约状态为 `CANCELLED` 并置空 `active_reservation_key`。
- 只有状态更新影响行数为 1 时，才回补容量。
- 回补使用预约记录快照：
  - 共享：只回补 `remaining_workstation_count`；
  - 非共享：回补 `remaining_device_count` 和 `remaining_workstation_count`。
- slot 为 `FULL` 时取消后恢复 `OPEN`。
- slot 为管理员 `CLOSED` 时不会自动恢复 `OPEN`。

新增 mapper 方法：

- `releaseSharedCompetitionSceneResourceSlotCapacity`
- `releaseExclusiveCompetitionSceneResourceSlotCapacity`

## 10. 我的预约改造

方法：`UserCompetitionSceneResourceServiceImpl#selectMyReservationList`

已改造 mapper 查询条件：

- 从 `(schedule_id, subject_type, subject_code)` 改为 `(competition_series_id, subject_type, subject_code)`。
- 团队任意成员解析为同一 `TEAM + team_code` 主体后，可查看本队预约。
- 返回 VO 已包含：
  - `operatorName`
  - `subjectType`
  - `subjectCode`
  - `occupyPeopleCount`
  - `reservedDeviceCount`
  - `reservedWorkstationCount`
  - `reservationSourceScheduleId`
  - `groupCode`
  - `groupName`

## 11. 测试结果

已执行定向测试：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=UserCompetitionSceneResourceServiceImplTest,UserCompetitionSceneResourceReservationBaseMethodTest,CompetitionSceneResourceScheduleScopeServiceImplTest,CompetitionSceneResourceSlotGroupScopeServiceImplTest test
```

结果：

- BUILD SUCCESS
- Tests run: 26
- Failures: 0
- Errors: 0
- Skipped: 0

覆盖点：

- 用户所属赛场未绑定资源范围，资源不返回。
- 用户所属赛场已绑定资源范围，资源返回。
- slot 未配置组别，允许。
- slot 配置组别且命中，允许。
- slot 配置组别且不命中，过滤/拒绝。
- slot 已开始拒绝。
- slot CLOSED 拒绝。
- 团队预约按成员数扣容量。
- 个人预约按 1 人扣容量。
- active key 冲突返回 `ALREADY_RESERVED`。
- idempotency key 重复提交返回同一记录，不扣容量。
- 共享占用只扣工位。
- 非共享占用扣设备和整台设备工位。
- 容量原子扣减失败拒绝并回滚。
- 我的预约团队成员可见。
- 取消按共享/非共享快照回补。
- 重复取消不重复回补。

另执行过带 `-am` 的同组测试：

```bash
mvn -pl teaching-modules/teaching-competition -am test -Dtest=UserCompetitionSceneResourceServiceImplTest,UserCompetitionSceneResourceReservationBaseMethodTest,CompetitionSceneResourceScheduleScopeServiceImplTest,CompetitionSceneResourceSlotGroupScopeServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
```

结果同为 BUILD SUCCESS，Tests run: 26。

## 12. 构建结果

已执行后端构建：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：BUILD SUCCESS。

构建过程中仅出现项目既有 Maven model/deprecated/unchecked 警告，未出现本包编译错误。

## 13. 已知风险

- 本包未做管理端配置页面，因此 scope 和 group scope 仍需通过已有数据或后续管理端配置能力写入。
- `AUTO_LOCATION` 仍只作为第一包字段预留，本包不实现自动位置匹配。
- `idempotency_key` 当前按数据库唯一索引全局唯一处理，前端必须生成足够随机的唯一键。
- 已有历史预约如果没有 `active_reservation_key`，不会被新的一次预约唯一键自动识别；建议联调前处理有效历史数据或清理开发库测试数据。
- 预约记录 `operator_name` 当前取登录用户名兜底，后续如需展示真实姓名，可在用户上下文中补齐来源。
- 资源预约主流程已不依赖 credential grant；代码中旧 credential 校验私有方法仍保留但不在本包主流程调用。

## 14. 是否可以进入管理端配置和用户端页面改造

可以进入。

后端主流程已接入范围、组别、active key、idempotency、共享/非共享容量扣减、取消快照回补和团队可见性。下一包建议进入：

- 管理端资源允许赛场范围配置。
- 管理端 slot 允许组别配置。
- 批量生成 slot 时组别配置。
- 用户端资源/slot 展示和已有预约提示。
- 用户端提交按钮防抖和 `idempotency_key` 生成。
