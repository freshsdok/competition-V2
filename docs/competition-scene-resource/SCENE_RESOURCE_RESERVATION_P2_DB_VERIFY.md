# 资源预约第二包测试库 DB 验证

## 1. 验证环境

- 验证时间：2026-07-06
- Nacos namespace：`4bc34c5b-b51f-4847-bf0b-a9f4e6486749`
- Spring profile：`test`
- competition 服务：`127.0.0.1:9205`
- gateway：`127.0.0.1:9889`
- 数据库：`jiaoxue_test`
- MySQL 版本：`8.0.46`
- 数据库主机：测试内网地址，文档中不记录账号、密码、完整连接串。

本次未连接生产数据库。

## 2. 服务状态

- `http://127.0.0.1:9205/actuator/health`：`UP`
- `http://127.0.0.1:9889/actuator/health`：`UP`
- `http://127.0.0.1:9224/actuator/health`：`UP`

注意：当前 9205 进程启动时间为 `2026-07-06 13:54:23`，第二包 class 编译时间为 `2026-07-06 17:59:46`。因此当前正式 9205 进程尚未加载第二包代码，需要重启 9205 后才能执行第二包接口联调。

## 3. Migration 验证

### 新增表

| 表名 | 验证结果 |
| --- | --- |
| `competition_scene_resource_schedule_scope` | OK |
| `competition_scene_resource_slot_group_scope` | OK |

### slot 字段

| 字段 | 验证结果 |
| --- | --- |
| `workstation_count` | OK |
| `total_device_count` | OK |
| `total_workstation_count` | OK |
| `remaining_device_count` | OK |
| `remaining_workstation_count` | OK |

### reservation 字段

| 字段 | 验证结果 |
| --- | --- |
| `competition_series_id` | OK |
| `reservation_source_schedule_id` | OK |
| `subject_type` | OK |
| `subject_code` | OK |
| `operator_user_id` | OK |
| `operator_name` | OK |
| `group_code` | OK |
| `group_name` | OK |
| `occupy_people_count` | OK |
| `reserved_device_count` | OK |
| `reserved_workstation_count` | OK |
| `shared_occupancy_snapshot` | OK |
| `workstation_count_snapshot` | OK |
| `active_reservation_key` | OK |
| `idempotency_key` | OK |
| `reservation_status` | OK |

### 唯一索引

初次验证发现：

- `uk_scene_resource_active_reservation_key` 缺失；
- `idempotency_key` 仍为旧索引名 `uk_scene_reservation_idempotency`。

执行的补齐动作：

- 将 `uk_scene_reservation_idempotency` 重命名为 `uk_scene_resource_idempotency_key`；
- 新增 `uk_scene_resource_active_reservation_key(active_reservation_key)`。

复核结果：

| 索引 | 列 | 唯一性 | 验证结果 |
| --- | --- | --- | --- |
| `uk_scene_resource_active_reservation_key` | `active_reservation_key` | UNIQUE | OK |
| `uk_scene_resource_idempotency_key` | `idempotency_key` | UNIQUE | OK |

## 4. 历史数据干扰检查

| 检查项 | 结果 |
| --- | --- |
| 有效预约中非空 `active_reservation_key` 数量 | 0 |
| 非空 `idempotency_key` 数量 | 0 |
| 重复 `idempotency_key` 分组数 | 0 |
| 重复 `active_reservation_key` 分组数 | 0 |
| 当前有效预约数量 | 0 |

结论：当前测试库不存在会干扰第二包 active key / idempotency key 验证的旧预约数据。

## 5. 清理说明

本次未执行任何自动清理 SQL，未删除业务数据，未清空预约表。

仅执行了 migration 范围内的索引补齐 DDL：

- 重命名幂等唯一索引；
- 新增有效预约唯一索引。

## 6. DB 验证结论

测试库 migration 验证通过，可以支撑第二包后端主流程联调。

但正式 9205 competition 服务需重启到第二包代码后，才能继续执行接口和并发验证。
