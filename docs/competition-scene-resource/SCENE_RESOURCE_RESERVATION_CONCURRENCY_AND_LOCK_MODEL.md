# 资源预约并发与锁模型

更新时间：2026-07-06

## 1. 每队一次预约规则

新规则：

- 团队赛：同一 `competition_series_id` 下，同一 `team_code` 只能有一条有效资源预约。
- 个人赛：同一 `competition_series_id` 下，同一个个人主体只能有一条有效资源预约。
- 队内任意有效成员可以为团队预约，预约主体始终是 TEAM。
- 操作人只记录在 `operator_user_id` / `operator_name`，不能作为预约主体。
- 取消、失效、删除后释放本次有效预约资格。

当前实现问题：

- 当前只查询 `schedule_resource_id + subject_type + subject_code` 下是否已有有效预约。
- 这意味着同一队可以在同一赛事下预约另一个资源。
- 当前没有数据库唯一约束，无法防住同队成员并发提交。

推荐用 `active_reservation_key` 做数据库级兜底。

## 2. active_reservation_key 设计

推荐格式：

```text
RESV:{competition_series_id}:{subject_type}:{subject_code}
```

示例：

```text
RESV:20260701001:TEAM:T20260001
RESV:20260701001:USER:1357
```

落库规则：

- 新增预约且状态有效时，`active_reservation_key` 非空。
- 有效状态建议包括 `RESERVED`，如已核销仍视为占用机会，也包括 `CHECKED`。
- 取消状态 `CANCELLED`、失效状态 `EXPIRED/INVALID`、逻辑删除后必须置空。
- 唯一索引：`uk_scene_resource_active_reservation_key(active_reservation_key)`。
- MySQL 允许多条 NULL，因此取消或失效后的历史记录不会互相冲突。

处理唯一冲突：

1. 生成 active key。
2. 插入预约记录或创建预约占位时由唯一索引兜底。
3. 如果捕获 active key 唯一冲突，按 active key 查询已有有效预约。
4. 返回 `ALREADY_RESERVED`，并携带已有预约信息。

推荐插入顺序：

- 更稳妥的顺序是先在事务内写入带 active key 的预约记录，再扣减 slot 容量。
- 如果容量扣减失败，事务回滚，预约记录和 active key 一起回滚。
- 如果必须保持“先扣容量再写记录”，则捕获插入唯一冲突时必须回滚容量扣减，并在新事务或事务外查询已有预约。

## 3. idempotency_key 设计

用途：

- 防止同一次用户操作因网络重试、按钮重复触发而重复扣减容量。
- 不替代 active key。active key 负责“同队/本人一次有效预约”，idempotency key 负责“同一次请求只处理一次”。

推荐规则：

- 前端在用户确认预约后生成 UUID，直到请求完成前重复点击复用同一个 key。
- 后端要求 `idempotency_key` 非空。
- 唯一索引：`uk_scene_resource_idempotency_key(idempotency_key)`。
- 查询幂等结果时按 `idempotency_key` 查询，再校验 `operator_user_id` 是否一致。

返回策略：

- 相同操作人、相同 idempotency_key：返回第一次预约结果。
- 不同操作人碰撞同一 idempotency_key：返回幂等键冲突错误，不应返回他人预约。
- active key 冲突但 idempotency_key 不同：返回已有有效预约。

当前实现问题：

- 当前前端每次点击都会生成新 key，缺少 submitting 锁。
- 当前后端先按 `operator_user_id + idempotency_key` 查，但表上是全局唯一 key，冲突语义不完整。

## 4. slot 容量原子扣减算法

容量扣减必须使用单条条件 UPDATE，不能先查余量再普通 UPDATE。

共享占用推荐 SQL 语义：

```sql
UPDATE competition_scene_resource_slot
SET reserved_workstation_count = reserved_workstation_count + #{reservedWorkstationCount},
    remaining_workstation_count = remaining_workstation_count - #{reservedWorkstationCount},
    update_by = #{updateBy},
    update_time = NOW(),
    version = version + 1
WHERE slot_id = #{slotId}
  AND deleted = 0
  AND slot_status = 'OPEN'
  AND start_time > NOW()
  AND remaining_workstation_count >= #{reservedWorkstationCount};
```

非共享占用推荐 SQL 语义：

```sql
UPDATE competition_scene_resource_slot
SET reserved_device_count = reserved_device_count + #{reservedDeviceCount},
    remaining_device_count = remaining_device_count - #{reservedDeviceCount},
    reserved_workstation_count = reserved_workstation_count + #{reservedWorkstationCount},
    remaining_workstation_count = remaining_workstation_count - #{reservedWorkstationCount},
    update_by = #{updateBy},
    update_time = NOW(),
    version = version + 1
WHERE slot_id = #{slotId}
  AND deleted = 0
  AND slot_status = 'OPEN'
  AND start_time > NOW()
  AND remaining_device_count >= #{reservedDeviceCount}
  AND remaining_workstation_count >= #{reservedWorkstationCount};
```

注意：

- slot 是否开放和是否未开始必须进入 UPDATE 条件，避免校验后状态被并发改变。
- 容量不足时影响行数为 0，返回容量不足。
- 事务回滚必须覆盖预约记录写入和容量扣减。

## 5. 共享/非共享并发扣减区别

共享占用：

- 多个主体可以并发预约同一 slot。
- 只竞争 `remaining_workstation_count`。
- `remaining_device_count` 不因共享预约减少。
- `reserved_device_count` 在 slot 聚合层建议不作为共享容量扣减字段；预约记录里仍保存 `reserved_device_count` 快照用于展示。

非共享占用：

- 多个主体可以并发预约同一 slot 的不同设备。
- 同时竞争 `remaining_device_count` 和 `remaining_workstation_count`。
- 不再使用 `countEffectiveReservationBySlot > 0` 排斥整个 slot。
- 每条预约按 `ceil(occupy_people_count / workstation_count)` 占用设备数，工位按整台设备数回补和扣减。

slot FULL 状态：

- 共享：`remaining_workstation_count = 0` 可置为 FULL。
- 非共享：`remaining_device_count = 0 OR remaining_workstation_count = 0` 可置为 FULL。
- 如果同一套 UPDATE 兼容两种模式，建议由后端传入 `sharedOccupancy` 或拆成两个 Mapper 方法，避免共享误扣设备。

## 6. 取消预约容量回补并发控制

取消必须分两步，但第一步必须是条件 UPDATE：

```sql
UPDATE competition_scene_resource_reservation
SET reservation_status = 'CANCELLED',
    active_reservation_key = NULL,
    cancel_time = NOW(),
    cancel_reason = #{cancelReason},
    update_by = #{updateBy},
    update_time = NOW()
WHERE reservation_id = #{reservationId}
  AND reservation_status = 'RESERVED'
  AND active_reservation_key IS NOT NULL
  AND deleted = 0;
```

只有影响行数为 1 时，才允许回补 slot 容量。

回补规则：

- 读取预约记录快照字段：
  - `reserved_device_count`
  - `reserved_workstation_count`
  - `shared_occupancy_snapshot`
  - `workstation_count_snapshot`
- 共享占用：只回补工位。
- 非共享占用：回补设备和工位。
- 不重新按当前团队人数、当前资源工位数或当前共享配置计算。

回补 SQL 建议：

- 使用 `LEAST` 防止超过总容量。
- 使用 `GREATEST` 防止已预约数低于 0。
- 状态从 FULL 回到 OPEN 时要确认 slot 未关闭且未过期；如果 slot 已 CLOSED，不应强行打开。

重复取消：

- 第二次取消因为 reservation 状态已不是 RESERVED，影响行数为 0。
- 影响行数为 0 时不能回补容量。
- 这能防住重复点击、重放请求和两个端同时取消。

## 7. 多用户同时预约测试用例

同队多个成员同时预约：

- 准备同一 TEAM 下成员 A、B。
- A、B 使用不同 idempotency_key 同时预约同一或不同 slot。
- 期望最终只有一条有效预约。
- 另一个请求返回 `ALREADY_RESERVED`，返回已有预约。
- slot 容量只扣一次。

多队同时抢同一共享 slot：

- slot `remaining_workstation_count=5`。
- 队伍 A 占 3 人，队伍 B 占 3 人。
- 并发提交。
- 期望只有一个成功，另一个容量不足。
- 最终 `remaining_workstation_count=2`，不出现负数。

多队同时抢同一非共享 slot：

- 每台设备 2 工位，剩余设备 2，剩余工位 4。
- 队伍 A 3 人，需要 2 台设备。
- 队伍 B 2 人，需要 1 台设备。
- 并发提交。
- 期望只有一个成功，不能超卖设备。

相同 idempotency_key 重复提交：

- 同一用户、同一 slot、同一 idempotency_key 重复 POST。
- 期望返回同一预约记录。
- slot 容量只扣一次。

不同 idempotency_key 重复点击：

- 同一用户连续生成两个不同 key。
- 期望第一条成功，第二条命中 active key，返回已有预约。
- slot 容量只扣一次。

重复取消：

- 同一预约同时发起两次取消。
- 期望只有一次状态更新成功。
- slot 容量只回补一次。

slot 已开始：

- slot `start_time <= now` 且 `slot_status=OPEN`。
- 期望提交失败。
- slot 容量不变。

## 8. 前端防抖建议

前端必须做体验层防抖，但不能作为并发安全边界。

PC：

- `reserve` 增加 `submittingSlotId` 或 `reservationSubmitting`。
- 点击确认后立即禁用当前 slot 的预约按钮。
- 请求完成前复用同一个 idempotency_key。
- 请求完成后清空 submitting 状态。
- `ALREADY_RESERVED` 时刷新我的预约并切到“我的预约”页签。

小程序：

- 增加 `submitting` 和 `pendingIdempotencyKey`。
- `uni.showModal` 确认后，如果正在提交直接 return。
- 同一次确认流程中只生成一次 idempotency_key。
- 取消按钮也增加 submitting，避免重复取消请求刷屏。

服务端：

- 即使前端防抖失效，也必须由 active key、idempotency key 和容量条件 UPDATE 保证正确性。

## 9. 是否需要数据库 migration

需要。

必须新增：

- `competition_scene_resource_schedule_scope`
- `competition_scene_resource_slot_group_scope`

必须补充或确认 slot 字段：

- 当前有 `device_capacity`，建议补充或映射为 `total_device_count`。
- 当前有 `workstation_capacity`，建议补充或映射为 `total_workstation_count`。
- 建议补充 `workstation_count`，保存 slot 创建时每台设备工位快照。
- 当前已有 `remaining_device_count`、`remaining_workstation_count`。

必须补充 reservation 字段：

- `competition_series_id`
- `reservation_source_schedule_id`
- `operator_name`
- `group_code`
- `group_name`
- `occupy_people_count`
- `reserved_workstation_count` 或兼容迁移 `covered_workstation_count`
- `shared_occupancy_snapshot`
- `workstation_count_snapshot`
- `active_reservation_key`

必须调整或新增唯一约束：

- `uk_scene_resource_active_reservation_key(active_reservation_key)`
- `uk_scene_resource_idempotency_key(idempotency_key)`

数据处理原则：

- 不连接生产数据库。
- 不写危险自动清理 SQL。
- 开发库测试数据可以人工清理重建。
- 老数据回填应只做字段补齐，不自动删除历史预约。

