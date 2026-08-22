# 大赛现场设备资源管理与预约数据库设计

更新时间：2026-06-30

## 1. 设计边界

本功能服务于大赛现场运行，不做传统资产管理，不使用旧 `site_*` 表体系，不新建独立现场系统，继续使用 `competition_scene_*` 命名体系。

当前已存在现场运行表：

- `competition_scene_schedule`：大赛现场安排。
- `competition_scene_schedule_target`：赛场安排匹配对象，也是本阶段预约主体来源。
- `competition_scene_credential`：参赛证、教师证、专家证。
- `competition_scene_operation_log`：二维码核验、报道签到、资料领取、候场确认流水。

本阶段新增表只解决：

- 设备资源台账登记。
- 在赛场安排中布置设备。
- 配置已布置设备的预约时段。
- 选手基于有效现场证件预约设备。
- 管理端查看、取消、核销预约。

不做：

- 资产编号、所属单位、库房位置等资产管理字段。
- 独立运维端、运维确认按钮、运维工单、复杂运维状态流转。
- 最晚取消时间。
- 报名、支付、成绩、证书主流程改造。

## 2. 核心关系

```text
competition_scene_schedule
  1 ── N competition_scene_schedule_target
  1 ── N competition_scene_credential
  1 ── N competition_scene_schedule_resource

competition_scene_resource
  1 ── N competition_scene_schedule_resource

competition_scene_schedule_resource
  1 ── N competition_scene_resource_slot

competition_scene_resource_slot
  1 ── N competition_scene_resource_reservation
```

关键口径：

- 选手预约的是 `competition_scene_schedule_resource` 中已布置到赛场安排、且已开放时段的资源，不是资源台账。
- 预约主体统一来自 `competition_scene_schedule_target`。
- 不再区分“团队预约流程”和“个人预约流程”两套逻辑。
- 团队参赛时，队伍中任意有效参赛成员均可代表本队预约。
- 个人参赛时，个人本人可预约。
- 操作人只是 `operator_user_id`；真正锁定的是 `reservation_subject`。

## 3. 预约主体模型

### 3.1 subject 字段

预约记录统一使用：

- `subject_type`：`TEAM` 或 `USER`。
- `subject_code`：团队时存 `team_code`，个人时存 `user_id` 或可稳定定位个人 target 的引用。
- `team_code`：团队预约时冗余保存。
- `user_id`：个人预约时保存；团队预约时可保存发起操作人的用户 ID 作为展示冗余。
- `operator_user_id`：实际提交预约的当前登录用户。

明确约束：

- 现有系统不存在稳定数字型 `teamId`。
- 第一阶段团队主体以 `team_code` 为准。
- 后端接口字段使用 `teamCode`，不使用 `teamId`。
- 数据库不设计 `team_id` 字段。

### 3.2 主体识别流程

后端统一流程：

1. 根据 `slot_id` 找到 `schedule_resource_id` 和 `schedule_id`。
2. 用当前登录用户在 `competition_scene_schedule_target` 中解析可预约主体。
3. 如果 target 是团队，得到 `subject_type = TEAM`、`subject_code = team_code`。
4. 如果 target 是个人，得到 `subject_type = USER`、`subject_code = user_id` 或 target 引用。
5. 校验当前用户是该 subject 的有效成员。
6. 校验有效现场证件。
7. 查重预约。
8. 自动计算预约设备数。
9. 校验容量。
10. 创建预约。

个人比赛识别不再用于决定不同预约流程，只用于解析当前用户对应的 `schedule_target`。

## 4. 预约锁定规则

同一 `schedule_id` 下，同一 `subject_type + subject_code` 只能存在一条有效预约。

有效预约锁定范围：

```text
schedule_id + subject_type + subject_code
```

有效预约状态：

- `RESERVED`
- `CHECKED`

过期处理：

- 不做自动过期任务。
- 不自动把数据库预约状态改成 `EXPIRED`。
- 列表展示时根据关联时段 `end_time` 计算“已过期”提示。
- 如果关联 slot 已过期，该预约不再阻止同一 subject 预约后续新时段。
- 新预约查重时，有效预约范围应排除已过期 slot。

重复预约时：

- 返回错误码 `ALREADY_RESERVED_BY_SUBJECT`。
- 同时返回 `existingReservation` 摘要。
- 前端展示已有预约信息，并拒绝重复预约。

## 5. 设备数和工位规则

预约单位始终是设备，不是工位。

- `reserved_device_count`：预约设备数。
- `workstations_per_device`：每台设备工位数。
- `covered_workstation_count = reserved_device_count × workstations_per_device`。

工位数用于：

- 容量展示。
- 团队人数计算。
- 判断预约需要覆盖多少人。

工位数不是预约单位。

设备数由后端自动计算，用户不能自由调整：

- 团队参赛：`reserved_device_count = ceil(参赛选手人数 / workstations_per_device)`。
- 个人参赛：默认 `reserved_device_count = 1`。
- 团队人数第一阶段只统计参赛选手，不统计指导教师、带队老师、专家。
- 如果剩余设备数不足，拒绝预约。

## 6. 有效现场证件规则

预约必须拥有有效现场证件。

第一阶段有效判断：

- `credential_status = EFFECTIVE`。
- 当前时间未超过 `valid_to`。
- 证件关联当前 `schedule_id` 或当前 `schedule_target`。

如未来需要冻结证件：

- 在 `credential_status` 中扩展 `FROZEN`。
- 不新增独立冻结字段。

## 7. 非共享占用规则

`shared_occupancy = false` 时：

- 同一 slot 只允许一个未过期的有效预约。
- 只要该 slot 存在 `RESERVED` 或 `CHECKED` 且未过期预约，拒绝新预约。
- 返回错误码 `EXCLUSIVE_SLOT_OCCUPIED`。

`shared_occupancy = true` 时：

- 按剩余设备数和剩余工位数校验容量。
- 同一 `schedule_id + subject_type + subject_code` 仍只能存在一条未过期有效预约。

## 8. 枚举设计

### 8.1 资源类型

`resource_type`：

- `ROOM`
- `LAB`
- `DEVICE`
- `WORKSTATION`
- `SERVER`
- `SOFTWARE`
- `OTHER`

### 8.2 资源状态

`resource_status`：

- `ENABLED`
- `DISABLED`
- `MAINTENANCE`

资源状态中已包含启用/停用/维护中，不再另设启用状态字段。

### 8.3 预约发布状态

`booking_status`：

- `DRAFT`
- `READY`
- `OPEN`
- `PAUSED`
- `CLOSED`

### 8.4 时段状态

`slot_status`：

- `PENDING`
- `OPEN`
- `FULL`
- `CLOSED`
- `EXPIRED`

说明：

- `EXPIRED` 可作为时段展示或手动维护状态。
- 本阶段不要求定时任务自动改时段状态。

### 8.5 预约状态

`reservation_status`：

- `RESERVED`
- `CANCELLED`
- `CHECKED`

说明：

- 预约表第一阶段不依赖数据库自动写入 `EXPIRED`。
- “已过期”作为列表展示状态，根据 slot `end_time` 派生。

### 8.6 核销状态

`check_status`：

- `UNCHECKED`
- `CHECKED`

## 9. 表设计

### 9.1 设备资源台账表

表名：`competition_scene_resource`

| 字段 | 类型建议 | 必填 | 说明 |
| --- | --- | --- | --- |
| `resource_id` | bigint | 是 | 主键 |
| `resource_code` | varchar(64) | 是 | 资源编号，唯一 |
| `resource_name` | varchar(255) | 是 | 资源名称 |
| `resource_type` | varchar(32) | 是 | 资源类型 |
| `resource_status` | varchar(32) | 是 | ENABLED/DISABLED/MAINTENANCE |
| `brand_model` | varchar(255) | 否 | 品牌型号 |
| `device_quantity` | int | 是 | 设备数量 |
| `workstation_count` | int | 是 | 单台设备工位数 |
| `default_slot_duration_minutes` | int | 是 | 默认单场占用周期，单位分钟 |
| `default_shared_occupancy` | tinyint(1) | 是 | 默认是否共享占用 |
| `need_ops_confirm` | tinyint(1) | 是 | 是否需要运维确认，仅提示字段 |
| `ops_contact_name` | varchar(100) | 否 | 运维联系人 |
| `ops_contact_phone` | varchar(32) | 否 | 运维联系电话 |
| `safety_notice` | text | 否 | 安全须知 |
| `attention_notes` | text | 否 | 注意事项 |
| `parameter_json` | longtext | 否 | 主要参数 JSON |
| `usage_instructions` | text | 否 | 使用说明 |
| `image_urls` | longtext | 否 | 图片 URL JSON 数组 |
| `admin_remark` | varchar(500) | 否 | 管理员备注 |
| `sort_order` | int | 否 | 排序 |
| `create_by` | varchar(64) | 否 | 创建者 |
| `create_time` | datetime | 否 | 创建时间 |
| `update_by` | varchar(64) | 否 | 更新者 |
| `update_time` | datetime | 否 | 更新时间 |
| `deleted` | tinyint(1) | 是 | 0 正常，1 删除 |

不加入字段：

- `asset_no`
- `owner_unit`
- `storage_location`
- `cancel_deadline_minutes`

### 9.2 赛场资源布置表

表名：`competition_scene_schedule_resource`

| 字段 | 类型建议 | 必填 | 说明 |
| --- | --- | --- | --- |
| `schedule_resource_id` | bigint | 是 | 主键 |
| `schedule_id` | bigint | 是 | 现场安排 ID |
| `resource_id` | bigint | 是 | 资源 ID |
| `event_id` | bigint | 否 | 第一阶段直接使用 competition_series_id |
| `deployment_location` | varchar(500) | 否 | 部署位置 |
| `deployed_device_count` | int | 是 | 部署设备数 |
| `workstations_per_device` | int | 是 | 每台设备工位数 |
| `total_workstations` | int | 是 | 总工位数，后端计算 |
| `slot_duration_minutes` | int | 是 | 单场占用周期 |
| `shared_occupancy` | tinyint(1) | 是 | 是否共享占用 |
| `need_ops_confirm` | tinyint(1) | 是 | 是否需要运维确认，仅提示字段 |
| `ops_contact_name` | varchar(100) | 否 | 运维联系人 |
| `ops_contact_phone` | varchar(32) | 否 | 运维联系电话 |
| `booking_status` | varchar(32) | 是 | DRAFT/READY/OPEN/PAUSED/CLOSED |
| `booking_open_time` | datetime | 否 | 预约开放时间 |
| `booking_close_time` | datetime | 否 | 预约关闭时间 |
| `safety_notice_override` | text | 否 | 安全须知覆盖 |
| `attention_notes_override` | text | 否 | 注意事项覆盖 |
| `usage_instructions_override` | text | 否 | 使用说明覆盖 |
| `admin_remark` | varchar(500) | 否 | 管理员备注 |
| `create_by` | varchar(64) | 否 | 创建者 |
| `create_time` | datetime | 否 | 创建时间 |
| `update_by` | varchar(64) | 否 | 更新者 |
| `update_time` | datetime | 否 | 更新时间 |
| `deleted` | tinyint(1) | 是 | 0 正常，1 删除 |

### 9.3 预约时段表

表名：`competition_scene_resource_slot`

| 字段 | 类型建议 | 必填 | 说明 |
| --- | --- | --- | --- |
| `slot_id` | bigint | 是 | 主键 |
| `schedule_resource_id` | bigint | 是 | 赛场资源布置 ID |
| `schedule_id` | bigint | 是 | 现场安排 ID |
| `resource_id` | bigint | 是 | 资源 ID |
| `event_id` | bigint | 否 | 第一阶段直接使用 competition_series_id |
| `start_time` | datetime | 是 | 时段开始 |
| `end_time` | datetime | 是 | 时段结束 |
| `device_capacity` | int | 是 | 本时段可预约设备数 |
| `reserved_device_count` | int | 是 | 已预约设备数 |
| `remaining_device_count` | int | 是 | 剩余设备数 |
| `workstation_capacity` | int | 是 | 工位容量 |
| `reserved_workstation_count` | int | 是 | 已占工位数 |
| `remaining_workstation_count` | int | 是 | 剩余工位数 |
| `slot_status` | varchar(32) | 是 | PENDING/OPEN/FULL/CLOSED/EXPIRED |
| `version` | bigint | 是 | 乐观锁版本 |
| `create_by` | varchar(64) | 否 | 创建者 |
| `create_time` | datetime | 否 | 创建时间 |
| `update_by` | varchar(64) | 否 | 更新者 |
| `update_time` | datetime | 否 | 更新时间 |
| `deleted` | tinyint(1) | 是 | 0 正常，1 删除 |

计算规则：

- `workstation_capacity = device_capacity × workstations_per_device`。
- `remaining_device_count = device_capacity - reserved_device_count`。
- `remaining_workstation_count = workstation_capacity - reserved_workstation_count`。

必须使用乐观锁或条件更新防止超卖。

### 9.4 预约记录表

表名：`competition_scene_resource_reservation`

| 字段 | 类型建议 | 必填 | 说明 |
| --- | --- | --- | --- |
| `reservation_id` | bigint | 是 | 主键 |
| `slot_id` | bigint | 是 | 预约时段 ID |
| `schedule_resource_id` | bigint | 是 | 赛场资源布置 ID |
| `schedule_id` | bigint | 是 | 现场安排 ID |
| `resource_id` | bigint | 是 | 资源 ID |
| `event_id` | bigint | 否 | 第一阶段直接使用 competition_series_id |
| `subject_type` | varchar(32) | 是 | TEAM/USER |
| `subject_code` | varchar(128) | 是 | 团队存 team_code，个人存 user_id 或 target 引用 |
| `team_code` | varchar(64) | 否 | 团队预约冗余 |
| `user_id` | bigint | 否 | 个人预约用户 ID；团队预约可冗余操作人 |
| `operator_user_id` | bigint | 是 | 实际操作人 |
| `reserved_device_count` | int | 是 | 预约设备数，后端计算 |
| `covered_workstation_count` | int | 是 | 覆盖工位数 |
| `reservation_status` | varchar(32) | 是 | RESERVED/CANCELLED/CHECKED |
| `cancel_time` | datetime | 否 | 取消时间 |
| `cancel_reason` | varchar(500) | 否 | 取消原因 |
| `check_status` | varchar(32) | 是 | UNCHECKED/CHECKED |
| `check_user_id` | bigint | 否 | 核销操作人 |
| `check_time` | datetime | 否 | 核销时间 |
| `idempotency_key` | varchar(128) | 是 | 幂等键 |
| `create_by` | varchar(64) | 否 | 创建者 |
| `create_time` | datetime | 否 | 创建时间 |
| `update_by` | varchar(64) | 否 | 更新者 |
| `update_time` | datetime | 否 | 更新时间 |
| `deleted` | tinyint(1) | 是 | 0 正常，1 删除 |

建议索引：

- `idx_scene_reservation_slot(slot_id, reservation_status, deleted)`。
- `idx_scene_reservation_subject(schedule_id, subject_type, subject_code, reservation_status, deleted)`。
- `idx_scene_reservation_operator(operator_user_id)`。
- `uk_scene_reservation_idempotency(idempotency_key)`。

说明：

- 不使用 `team_id`。
- 有效预约唯一性不能只靠数据库唯一索引完成，因为已过期 slot 不阻止后续新时段预约；需要服务层按 `slot.end_time` 判断。

## 10. 防超卖建议

预约成功必须在事务内完成：

1. 查询 slot、schedule resource、subject、credential。
2. 校验重复预约和独占占用。
3. 条件更新 slot 容量。
4. 插入预约记录。

条件更新示例：

```sql
UPDATE competition_scene_resource_slot
SET
  reserved_device_count = reserved_device_count + #{reservedDeviceCount},
  remaining_device_count = remaining_device_count - #{reservedDeviceCount},
  reserved_workstation_count = reserved_workstation_count + #{coveredWorkstationCount},
  remaining_workstation_count = remaining_workstation_count - #{coveredWorkstationCount},
  slot_status = CASE
    WHEN remaining_device_count - #{reservedDeviceCount} = 0 THEN 'FULL'
    ELSE slot_status
  END,
  version = version + 1,
  update_time = NOW()
WHERE slot_id = #{slotId}
  AND deleted = 0
  AND slot_status = 'OPEN'
  AND remaining_device_count >= #{reservedDeviceCount}
  AND remaining_workstation_count >= #{coveredWorkstationCount}
  AND version = #{version};
```
