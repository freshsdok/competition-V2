# 资源预约第二包测试库联调验证报告 RERUN

验证时间：2026-07-06  
结论：第二包联调未完全通过。资源范围、组别过滤、主流程预约、幂等、容量数值扣减、取消快照回补等主能力可用；但并发 active key 冲突返回 SQL 500、slot `FULL` 状态计算存在偏差，建议修复后再进入第三包页面改造。

## 1. 9205 Runtime 验证

- 已重启正式测试用 competition `9205`。
- 接口联调执行进程 PID：`34767`，启动时间：`Mon Jul 6 18:21:33 2026`
- 收尾后后台进程 PID：`65054`，启动时间：`Mon Jul 6 18:41:01 2026`
- 第二包目标 class 编译时间：`2026-07-06 18:18:38 +0800`
- 启动时间晚于 class 编译时间。
- `9205 /actuator/health = UP`
- `9889 /actuator/health = UP`
- `9224 /actuator/health = UP`

详见：`SCENE_RESOURCE_RESERVATION_P2_RUNTIME_VERIFY.md`

## 2. Migration 验证

沿用上一轮已确认结果：

- `competition_scene_resource_schedule_scope` 存在。
- `competition_scene_resource_slot_group_scope` 存在。
- slot 容量字段存在。
- reservation 新增字段存在。
- `uk_scene_resource_active_reservation_key` 存在。
- `uk_scene_resource_idempotency_key` 存在。
- 当前无历史有效预约数据干扰。

## 3. 测试数据

已落库 `P2IT_` 假数据：

- 1 个测试赛事；
- 1 个部署资源赛场；
- 1 个允许预约来源赛场；
- 1 个不允许预约来源赛场；
- 共享/非共享资源各 1 个；
- `OPEN/CLOSED/已开始/容量不足/FULL/并发` slot；
- `P2IT_G_A`、`P2IT_G_B` 两个组别；
- 队伍 A/B/C/D/E、个人用户、blocked 用户。

详见：`SCENE_RESOURCE_RESERVATION_P2_TEST_DATA_APPLIED.md`

## 4. 可预约资源列表验证

通过 gateway `9889` 和 direct competition `9205` 验证：

- allowed 用户资源列表返回 `92052026070611`、`92052026070612`。
- blocked 来源赛场用户返回 0 条。
- 已绑定 scope 但 `booking_status=CLOSED` 的 `92052026070613` 未返回。
- VO 包含 `competitionSeriesId`、`userSourceScheduleId`、`groupCode`、`groupName`、`hasExistingReservation`、`existingReservation`。
- direct `9205` 冒烟返回 2 条，证明 competition 服务本体可用。

结论：通过。

## 5. Slot 列表验证

- A 组共享 slot 返回：`92052026070901`、`92052026070902`、`92052026070911`、`92052026070913`、`92052026070915`。
- B 组用户可见 B 组 slot：`92052026070903`。
- A 组用户不可见 B 组 slot。
- 已开始 slot、CLOSED slot、共享工位 0 slot 均被过滤。
- 非共享设备 0、工位 0 slot 均被过滤。
- `allowedGroupNames` 返回 `第二包A组`。

结论：通过。

## 6. 预约提交验证

基础共享预约：

- `P2IT_USER_A1` 预约共享 A 组 slot 成功。
- 写入 `competition_series_id`、`reservation_source_schedule_id`、`subject_type=TEAM`、`subject_code=P2IT_TEAM_A`、`operator_user_id/operator_name`、`group_code/group_name`、`occupy_people_count=2`、容量快照、`active_reservation_key`、`idempotency_key`。

异常提交：

- group 不命中返回 `SLOT_GROUP_DENIED`。
- slot 已开始返回 `SLOT_NOT_OPEN`。
- slot CLOSED 返回 `SLOT_NOT_OPEN`。
- 共享工位不足返回 `CAPACITY_NOT_ENOUGH`。
- 非共享设备不足返回 `CAPACITY_NOT_ENOUGH`。
- 非共享工位不足返回 `CAPACITY_NOT_ENOUGH`。

结论：基础提交与强校验通过。

## 7. 同队重复预约验证

顺序重复：

- 队员 A 预约成功。
- 队员 B 再预约返回 `ALREADY_RESERVED`。
- 未新增预约，未重复扣容量。
- 返回 `existingReservation`。

并发重复：

- 队员 A/B 同时提交不同 `idempotency_key`。
- 最终只有 1 条有效预约成功。
- 另一请求返回了 SQL 唯一约束异常 `code=500`，而不是业务态 `ALREADY_RESERVED`。

结论：顺序重复通过；并发返回码不通过。

## 8. 幂等提交验证

- 同一个 `idempotency_key` 重复提交，返回同一预约记录。
- 未新增预约。
- 未重复扣容量。

结论：通过。

## 9. 共享占用容量验证

Team A 人数 `N=2`，每台设备工位 `W=4`。

预约后 DB 快照：

- reservation：`occupy_people_count=2`
- reservation：`reserved_device_count=1`
- reservation：`reserved_workstation_count=2`
- reservation：`shared_occupancy_snapshot=1`
- slot：`remaining_workstation_count 8 -> 6`
- slot：`remaining_device_count` 保持 `2`

取消后：

- slot：`remaining_workstation_count 6 -> 8`
- slot：`remaining_device_count` 保持 `2`
- 重复取消不重复回补。

结论：容量数值通过。

## 10. 非共享占用容量验证

Team A 人数 `N=2`，每台设备工位 `W=4`。

预约后 DB 快照：

- reservation：`reserved_device_count=ceil(2/4)=1`
- reservation：`reserved_workstation_count=1*4=4`
- reservation：`shared_occupancy_snapshot=0`
- slot：`remaining_device_count 2 -> 1`
- slot：`remaining_workstation_count 8 -> 4`

取消后：

- slot：`remaining_device_count 1 -> 2`
- slot：`remaining_workstation_count 4 -> 8`
- 重复取消不重复回补。

结论：容量数值通过。

## 11. 取消预约验证

- 有效预约可取消。
- 取消后 `active_reservation_key` 置空。
- 重复取消不重复回补。
- slot 已开始后取消返回 `RESERVATION_NOT_CANCELABLE`。
- 管理员将 slot 置为 `CLOSED` 后取消：容量回补，但 slot 仍保持 `CLOSED`，不会自动恢复 `OPEN`。

结论：主要取消规则通过。

## 12. 我的预约验证

- Team A 队员 A 预约后，队员 B 的 `myList` 可见该预约。
- 返回 `operatorName=P2IT_USER_A1`。
- 返回 `subjectType=TEAM`、`subjectCode=P2IT_TEAM_A`。
- 返回 `occupyPeopleCount=2`。
- 返回 `reservedDeviceCount`、`reservedWorkstationCount`。
- 返回 `reservationSourceScheduleId=92052026070602`。
- 返回 `groupCode=P2IT_G_A`。

结论：通过。

## 13. 并发验证

同队并发：

- 最终有效预约未超过 1 条。
- 失败请求暴露 SQL 500，不符合 `ALREADY_RESERVED` 期望。

多队共享并发：

- 容量未超卖，负容量数为 0。
- 但由于 slot `FULL` 状态计算偏差，第一条成功后 slot 可能过早变为 `FULL`，导致后续请求容量未充分使用。

多队非共享并发：

- 容量未超卖，负容量数为 0。
- 同样存在 slot 过早 `FULL` 的问题。

重复 idempotency：

- 未重复扣减。

重复取消：

- 未重复回补。

结论：无超卖，但并发业务返回码和 slot 状态不通过。

## 14. 失败项

1. 并发同队预约时，active key 唯一冲突返回 SQL 500。
   - 期望：返回 `ALREADY_RESERVED` 和已有预约信息。
   - 实际：`Duplicate entry 'RESV:920520260706:TEAM:P2IT_TEAM_A'` 透出为 `code=500`。

2. slot `FULL` 状态计算偏差。
   - 共享：剩余工位被扣到 0 时，slot 仍可能保持 `OPEN`。
   - 非共享：仍剩 1 台/4 工位时，slot 被错误置为 `FULL`。
   - 推断原因：MySQL `UPDATE ... SET` 中字段赋值按顺序影响后续 `CASE` 读取，`CASE` 使用了已更新后的 remaining 值。

3. 多队并发未超卖，但会因错误 `FULL` 状态提前拒绝后续可用容量。

4. 取消状态实际和对外契约统一为 `CANCELLED`。当前系统行为可用，但对外文档需保持一致。

## 15. 修复项

本轮按要求只做联调验证，未修改后端代码。

建议后续修复：

- 捕获并转换 active key 唯一冲突相关的 `DuplicateKeyException/DataIntegrityViolationException/PersistenceException`，返回 `ALREADY_RESERVED`。
- 调整 slot 容量扣减 SQL 的 `slot_status` 判断，避免在同一 `SET` 子句里读取已更新 remaining 值；可使用原始 remaining 表达式或子查询/二段表达式。
- 统一取消状态枚举文案：以 `CANCELLED` 为准。
- 修复后重跑同队并发、多队共享/非共享并发和 FULL 恢复用例。

## 16. 是否第二包联调通过

不通过。

说明：主流程能力基本可用，且未发现容量负数或重复取消多次回补；但并发错误返回码和 slot 状态计算会影响正式可用性，应在进入页面改造前修复。

## 17. 是否可以进入第三包

不建议直接进入第三包管理端配置和用户端页面改造。

建议先完成第二包后端修复并复测通过，再进入第三包。
