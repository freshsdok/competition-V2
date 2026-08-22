# 资源预约第二包测试库联调验证结果

## 1. 测试环境

- 日期：2026-07-06
- Spring profile：`test`
- Nacos namespace：`4bc34c5b-b51f-4847-bf0b-a9f4e6486749`
- gateway：`127.0.0.1:9889`
- competition：`127.0.0.1:9205`
- auth：`127.0.0.1:9224`
- 数据库：`jiaoxue_test`
- 数据库类型：MySQL `8.0.46`

健康检查：

- gateway `/actuator/health`：`UP`
- competition `/actuator/health`：`UP`
- auth `/actuator/health`：`UP`

重要阻断：

- competition 9205 进程启动时间：`2026-07-06 13:54:23`
- 第二包 class 编译时间：`2026-07-06 17:59:46`
- 当前正式 9205 进程尚未加载第二包代码。按“不要使用临时进程结果替代正式联调结论”的要求，本轮未执行预约接口联调结论判定。

## 2. migration 验证

详见：

- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_P2_DB_VERIFY.md`

验证结果：

- `competition_scene_resource_schedule_scope`：OK
- `competition_scene_resource_slot_group_scope`：OK
- slot 容量字段：OK
- reservation 新增字段：OK
- `uk_scene_resource_active_reservation_key`：OK
- `uk_scene_resource_idempotency_key`：OK

初次发现并修复：

- `uk_scene_resource_active_reservation_key` 缺失；
- 幂等唯一索引仍为旧名 `uk_scene_reservation_idempotency`；
- 已按 migration 口径在测试库补齐索引，未执行任何清理 SQL。

历史数据干扰检查：

- 有效预约数量：0
- 非空 `active_reservation_key` 有效预约数量：0
- 非空 `idempotency_key` 数量：0
- 重复 `active_reservation_key` 分组数：0
- 重复 `idempotency_key` 分组数：0

## 3. 测试数据

详见：

- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_P2_TEST_DATA.md`

本轮未向测试库写入新的 `P2IT_` 联调业务数据。

原因：正式 9205 未加载第二包代码，写入测试数据后也无法得到可信接口结论。测试数据方案已准备，待 9205 重启到第二包代码后可落库或通过管理端配置。

## 4. 可预约资源列表验证

状态：未执行接口验证。

阻断原因：

- 当前 9205 正式进程未加载第二包代码；
- 继续调用接口会验证旧流程，不符合第二包联调要求。

待验证项：

- 用户所属赛场未命中 resource schedule scope 时，资源不返回；
- 用户所属赛场命中 resource schedule scope 时，资源返回；
- `booking_status != OPEN` 的资源不返回；
- 返回 VO 包含 `competitionSeriesId`、`userSourceScheduleId`、`groupCode`、`groupName`、`hasExistingReservation`、`existingReservation`。

## 5. slot 列表验证

状态：未执行接口验证。

待验证项：

- `slot_status != OPEN` 不返回；
- `slot.start_time <= now` 不返回；
- 共享占用时 `remaining_workstation_count > 0` 才返回；
- 非共享占用时 `remaining_device_count > 0 && remaining_workstation_count > 0` 才返回；
- slot 未配置 group scope 时不限组别；
- slot 配置 group scope 且命中时返回；
- slot 配置 group scope 且不命中时过滤；
- 返回 `allowedGroupNames`。

## 6. 预约提交验证

状态：未执行接口验证。

待验证项：

- `idempotency_key` 非空；
- 用户赛场命中 resource schedule scope；
- 用户 `group_code` 命中 slot group；
- slot `OPEN` 且未开始；
- 预约成功；
- reservation 写入以下字段：
  - `competition_series_id`
  - `reservation_source_schedule_id`
  - `subject_type`
  - `subject_code`
  - `operator_user_id`
  - `operator_name`
  - `group_code`
  - `group_name`
  - `occupy_people_count`
  - `reserved_device_count`
  - `reserved_workstation_count`
  - `shared_occupancy_snapshot`
  - `workstation_count_snapshot`
  - `active_reservation_key`
  - `idempotency_key`

## 7. 同队重复预约验证

状态：未执行接口验证。

待验证项：

- 同队成员 A 预约成功；
- 同队成员 B 再预约返回 `ALREADY_RESERVED`；
- 不新增预约；
- 不扣减容量；
- 返回已有预约信息。

## 8. 幂等提交验证

状态：未执行接口验证。

待验证项：

- 同一个 `idempotency_key` 重复提交；
- 返回同一预约结果；
- 不新增预约；
- 不重复扣容量。

## 9. 共享占用容量验证

状态：未执行接口验证。

待验证项：

- 团队人数为 `N`；
- `reserved_workstation_count = N`；
- `remaining_workstation_count` 减 `N`；
- `remaining_device_count` 不减少；
- `reserved_device_count` 仅作为快照展示。

## 10. 非共享占用容量验证

状态：未执行接口验证。

待验证项：

- 团队人数为 `N`；
- `workstation_count = W`；
- `reserved_device_count = ceil(N / W)`；
- `reserved_workstation_count = reserved_device_count * W`；
- `remaining_device_count` 减 `reserved_device_count`；
- `remaining_workstation_count` 减 `reserved_workstation_count`。

## 11. 取消预约验证

状态：未执行接口验证。

待验证项：

- 有效预约可取消；
- slot 已开始后不能取消；
- 取消时 `active_reservation_key` 置空；
- 共享模式只回补 `remaining_workstation_count`；
- 非共享模式回补 `remaining_device_count` 和 `remaining_workstation_count`；
- 重复取消不重复回补；
- `slot_status = FULL` 且容量恢复时可恢复 `OPEN`；
- `slot_status = CLOSED` 不得自动恢复 `OPEN`。

## 12. 我的预约验证

状态：未执行接口验证。

待验证项：

- 团队成员 A 预约后，团队成员 B 能看到本队预约；
- 返回 `operatorName`；
- 返回 `subjectType` / `subjectCode`；
- 返回 `occupyPeopleCount`；
- 返回 `reservedDeviceCount`；
- 返回 `reservedWorkstationCount`；
- 返回 `reservationSourceScheduleId`；
- 返回 `groupCode` / `groupName`。

## 13. 并发验证

状态：未执行接口验证。

待验证项：

- 同队两个成员同时预约，最终只有一条有效预约；
- 多队同时抢同一共享 slot，`remaining_workstation_count` 不为负；
- 多队同时抢同一非共享 slot，`remaining_device_count` 不为负；
- 重复 `idempotency_key` 不重复扣减；
- 重复取消不重复回补。

## 14. 失败项

无业务接口失败项。

本轮阻断项：

- 正式 9205 competition 服务进程未加载第二包代码，不能给出真实第二包接口联调结论。

## 15. 修复项

已修复测试库 migration 差异：

- 重命名幂等唯一索引为 `uk_scene_resource_idempotency_key`；
- 新增 `uk_scene_resource_active_reservation_key(active_reservation_key)`。

未修复项：

- 需要人工重启正式 9205 competition 服务，使其加载 17:59:46 之后的第二包 class。

## 16. 是否第二包联调通过

未通过联调，不是业务失败，而是环境阻断。

DB migration 验证已通过；接口、容量和并发验证待 9205 重启后执行。

## 17. 是否可以进入第三包

暂不建议进入第三包。

建议顺序：

1. 重启正式测试用 9205 competition 服务；
2. 确认 9205 新进程启动时间晚于第二包 class 编译时间；
3. 按 `SCENE_RESOURCE_RESERVATION_P2_TEST_DATA.md` 落库或配置测试数据；
4. 通过 gateway 9889 和 competition 9205 执行接口与并发验证；
5. 第二包联调通过后，再进入第三包：管理端配置和用户端页面改造。
