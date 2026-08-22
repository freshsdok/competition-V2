# 大赛现场设备资源管理与预约当前实现交接文档

生成时间：2026-07-02  
适用范围：当前代码库中已经完成的“大赛现场设备资源管理与预约”一期至五期实现。  
用途：作为后续业务口径修订的事实基线。请优先在本文件上标注需要调整的逻辑，再据此改造现有代码。

## 1. 当前结论

本功能已经形成一条从管理端资源登记、赛场资源布置、预约时段配置，到 PC / 小程序用户端提交预约、查看我的预约、取消预约的基础闭环。

当前实现不是传统资产管理，没有引入资产编号、归属单位、存放位置、资产生命周期、独立运维端、运维工单、`opsConfirm` 接口、`team_id` 字段、最晚取消时间字段。

当前实现仍然没有进入第六阶段能力。管理端预约记录强化、预约详情、核销、异常处理、现场使用状态统计、全链路冒烟脚本属于待开发范围。

## 2. 已完成阶段概览

| 阶段 | 已完成内容 | 状态 |
| --- | --- | --- |
| 第一阶段 | 数据库 migration、4 张表、资源台账后端 CRUD、基础接口验证 | 已完成 |
| 第二阶段 | 管理端资源管理菜单和页面、`competition_scene_schedule_resource` 后端 CRUD | 已完成 |
| 第三阶段 | 赛场安排页面新增“资源与预约”Tab，支持赛场资源布置 | 已完成 |
| 第四阶段 | 预约时段 CRUD、单个新增、批量生成、开放/关闭、重叠校验、容量计算 | 已完成 |
| 第五阶段 | 用户端可预约资源、主体识别、证件校验、预约、重复预约控制、取消、PC / 小程序入口 | 已完成 |
| 第六阶段 | 管理端预约记录强化、核销、统计、现场状态、全链路冒烟 | 仅完成计划，未编码 |

## 3. 当前数据库结构

Migration 文件：

```text
db/migration/20260701_competition_scene_resource_p1_001.sql
```

当前新增 4 张表：

```text
competition_scene_resource
competition_scene_schedule_resource
competition_scene_resource_slot
competition_scene_resource_reservation
```

### 3.1 资源台账表 competition_scene_resource

定位：维护可用于大赛现场运行的设备资源台账，不是资产台账。

核心字段：

| 字段 | 当前含义 |
| --- | --- |
| `resource_id` | 资源主键 |
| `resource_code` | 资源编号，未删除数据内唯一 |
| `resource_name` | 资源名称 |
| `resource_type` | 资源类型 |
| `resource_status` | 资源状态 |
| `brand_model` | 品牌型号 |
| `device_quantity` | 登记设备数量 |
| `workstation_count` | 单台设备工位数 |
| `default_slot_duration_minutes` | 默认单场占用周期，单位分钟 |
| `default_shared_occupancy` | 默认是否共享占用 |
| `need_ops_confirm` | 是否需要运维确认，仅提示字段，不触发运维流程 |
| `ops_contact_name` | 运维联系人 |
| `ops_contact_phone` | 运维联系电话 |
| `safety_notice` | 安全须知 |
| `attention_notes` | 注意事项 |
| `parameter_json` | 主要参数 JSON |
| `usage_instructions` | 使用说明 |
| `image_urls` | 设备图片 JSON 或字符串 |
| `admin_remark` | 管理员备注 |
| `sort_order` | 排序 |
| `deleted` | 逻辑删除标识 |

当前资源类型：

```text
ROOM
LAB
DEVICE
WORKSTATION
SERVER
SOFTWARE
OTHER
```

当前资源状态：

```text
ENABLED
DISABLED
MAINTENANCE
```

索引：

```text
uk_scene_resource_code(resource_code, deleted)
idx_scene_resource_type(resource_type)
idx_scene_resource_status(resource_status)
```

### 3.2 赛场资源布置表 competition_scene_schedule_resource

定位：把资源台账中的资源布置到某个赛场安排下，并维护预约发布配置。

核心字段：

| 字段 | 当前含义 |
| --- | --- |
| `schedule_resource_id` | 赛场资源布置主键 |
| `schedule_id` | 赛场安排 ID |
| `resource_id` | 资源台账 ID |
| `event_id` | 当前实现取 `competition_scene_schedule.competition_series_id` |
| `deployment_location` | 部署位置 |
| `deployed_device_count` | 部署设备数 |
| `workstations_per_device` | 每台设备工位数 |
| `total_workstations` | 总工位数，后端计算 |
| `slot_duration_minutes` | 单场占用周期，分钟 |
| `shared_occupancy` | 是否共享占用 |
| `need_ops_confirm` | 是否需要运维确认，仅提示 |
| `ops_contact_name` | 运维联系人 |
| `ops_contact_phone` | 运维联系电话 |
| `booking_status` | 预约发布状态 |
| `booking_open_time` | 预约开放时间 |
| `booking_close_time` | 预约关闭时间 |
| `safety_notice_override` | 安全须知覆盖值 |
| `attention_notes_override` | 注意事项覆盖值 |
| `usage_instructions_override` | 使用说明覆盖值 |
| `admin_remark` | 管理备注 |
| `deleted` | 逻辑删除 |

当前预约发布状态：

```text
DRAFT
READY
OPEN
PAUSED
CLOSED
```

注意：修改资源台账不会自动同步已经布置到赛场的资源配置。赛场布置保存的是当时带出的配置快照。

### 3.3 预约时段表 competition_scene_resource_slot

定位：某个赛场资源布置下可预约的具体时段。

核心字段：

| 字段 | 当前含义 |
| --- | --- |
| `slot_id` | 时段主键 |
| `schedule_resource_id` | 赛场资源布置 ID |
| `schedule_id` | 赛场安排 ID |
| `resource_id` | 资源 ID |
| `event_id` | 赛事 ID |
| `start_time` | 时段开始时间 |
| `end_time` | 时段结束时间 |
| `device_capacity` | 本时段可预约设备数 |
| `reserved_device_count` | 已预约设备数 |
| `remaining_device_count` | 剩余设备数 |
| `workstation_capacity` | 本时段工位容量，`device_capacity * workstations_per_device` |
| `reserved_workstation_count` | 已覆盖工位数 |
| `remaining_workstation_count` | 剩余工位数 |
| `slot_status` | 时段状态 |
| `version` | 版本字段，预留并发控制 |
| `deleted` | 逻辑删除 |

当前时段状态：

```text
PENDING
OPEN
FULL
CLOSED
EXPIRED
```

当前实现不做自动过期任务。用户端展示时根据 `end_time` 和当前时间计算是否过期。

### 3.4 预约记录表 competition_scene_resource_reservation

定位：记录用户端提交的设备资源预约。

核心字段：

| 字段 | 当前含义 |
| --- | --- |
| `reservation_id` | 预约主键 |
| `slot_id` | 预约时段 ID |
| `schedule_resource_id` | 赛场资源布置 ID |
| `schedule_id` | 赛场安排 ID |
| `resource_id` | 资源 ID |
| `event_id` | 赛事 ID |
| `subject_type` | 预约主体类型，`TEAM` 或 `USER` |
| `subject_code` | 锁定主体编码。团队为 `team_code`，个人为用户 ID 字符串 |
| `team_code` | 团队预约时冗余保存 |
| `user_id` | 个人主体用户 ID；团队主体时保存 target 中的用户 ID |
| `operator_user_id` | 实际操作预约的登录用户 |
| `reserved_device_count` | 预约设备数 |
| `covered_workstation_count` | 覆盖工位数 |
| `reservation_status` | 预约状态 |
| `cancel_time` | 取消时间 |
| `cancel_reason` | 取消原因 |
| `check_status` | 核销状态 |
| `check_user_id` | 核销人 |
| `check_time` | 核销时间 |
| `idempotency_key` | 幂等键 |
| `deleted` | 逻辑删除 |

当前预约状态：

```text
RESERVED
CANCELLED
CHECKED
```

当前核销状态：

```text
UNCHECKED
CHECKED
```

当前没有 `team_id` 字段。第一阶段至第五阶段均以 `team_code` 作为团队主体稳定标识。

## 4. 明确未引入的字段和流程

当前数据库和业务实现不包含以下内容：

```text
asset_no
owner_unit
storage_location
cancel_deadline_minutes
team_id
ops_status
opsConfirm 接口
独立运维端
旧 site_* 表
```

当前没有修改报名、支付、成绩、证书主流程。

## 5. 后端代码结构

主要后端目录：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/
old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/
```

### 5.1 常量

```text
com.teaching.competition.contant.CompetitionSceneResourceConstants
```

包含资源类型、资源状态、预约发布状态、时段状态、主体类型、预约状态、核销状态和用户端错误码。

当前用户端预约错误码：

```text
NO_VALID_CREDENTIAL
NOT_SCHEDULE_TARGET
SUBJECT_NOT_RESOLVED
SUBJECT_MEMBER_INVALID
ALREADY_RESERVED_BY_SUBJECT
RESOURCE_NOT_OPEN
SLOT_NOT_OPEN
CAPACITY_NOT_ENOUGH
EXCLUSIVE_SLOT_OCCUPIED
DUPLICATE_RESERVATION
RESERVATION_NOT_CANCELABLE
IDEMPOTENCY_KEY_REQUIRED
```

### 5.2 资源台账后端

Controller：

```text
CompetitionSceneResourceController
```

映射：

```text
@RequestMapping({"/sceneResource", "/competition/sceneResource"})
```

接口：

| 方法 | 路径 | 权限码 |
| --- | --- | --- |
| GET | `/competition/sceneResource/list` | `competition:sceneResource:list` |
| GET | `/competition/sceneResource/{resourceId}` | `competition:sceneResource:query` |
| POST | `/competition/sceneResource` | `competition:sceneResource:add` |
| PUT | `/competition/sceneResource` | `competition:sceneResource:edit` |
| DELETE | `/competition/sceneResource/{resourceIds}` | `competition:sceneResource:remove` |
| POST | `/competition/sceneResource/changeStatus` | `competition:sceneResource:changeStatus` |

Service：

```text
ICompetitionSceneResourceService
CompetitionSceneResourceServiceImpl
```

Mapper：

```text
CompetitionSceneResourceMapper
CompetitionSceneResourceMapper.xml
```

核心校验：

- `resourceCode` 必填且未删除数据内唯一。
- `resourceName` 必填。
- `resourceType` 必须是当前允许类型。
- `resourceStatus` 必须是 `ENABLED / DISABLED / MAINTENANCE`。
- `deviceQuantity > 0`。
- `workstationCount > 0`，表示单台设备工位数。
- `defaultSlotDurationMinutes > 0`。
- `defaultSharedOccupancy` 不能为空。
- `needOpsConfirm` 不能为空。
- 删除前检查是否存在未删除的 `competition_scene_schedule_resource` 引用。
- 已布置资源不能删除，当前报错信息为“资源已布置到赛场安排，不能删除”。

### 5.3 赛场资源布置后端

Controller：

```text
CompetitionSceneScheduleResourceController
```

映射：

```text
@RequestMapping({"/sceneScheduleResource", "/competition/sceneScheduleResource"})
```

接口：

| 方法 | 路径 | 权限码 |
| --- | --- | --- |
| GET | `/competition/sceneScheduleResource/list?scheduleId=` | `competition:sceneScheduleResource:list` |
| GET | `/competition/sceneScheduleResource/{scheduleResourceId}` | `competition:sceneScheduleResource:query` |
| POST | `/competition/sceneScheduleResource` | `competition:sceneScheduleResource:add` |
| PUT | `/competition/sceneScheduleResource` | `competition:sceneScheduleResource:edit` |
| DELETE | `/competition/sceneScheduleResource/{scheduleResourceIds}` | `competition:sceneScheduleResource:remove` |
| POST | `/competition/sceneScheduleResource/changeBookingStatus` | `competition:sceneScheduleResource:changeBookingStatus` |

Service：

```text
ICompetitionSceneScheduleResourceService
CompetitionSceneScheduleResourceServiceImpl
```

核心校验和行为：

- `scheduleId` 必填，且对应赛场安排存在、未删除。
- `resourceId` 必填，且对应资源台账存在、未删除。
- 新增布置时资源必须为 `ENABLED`。
- `deployedDeviceCount > 0`。
- `workstationsPerDevice > 0`。
- `totalWorkstations = deployedDeviceCount * workstationsPerDevice`，由后端计算。
- `slotDurationMinutes > 0`。
- `sharedOccupancy` 不能为空。
- `needOpsConfirm` 不能为空。
- `bookingStatus` 只能为 `DRAFT / READY / OPEN / PAUSED / CLOSED`。
- 新增时默认从资源台账带出默认配置。
- 修改资源台账不自动影响已布置资源。
- 删除布置只逻辑删除赛场布置，不影响资源台账。
- `changeBookingStatus` 当前只校验合法状态，不做严格状态机。

### 5.4 预约时段后端

Controller：

```text
CompetitionSceneResourceSlotController
```

映射：

```text
@RequestMapping({"/sceneResourceSlot", "/competition/sceneResourceSlot"})
```

接口：

| 方法 | 路径 | 权限码 |
| --- | --- | --- |
| GET | `/competition/sceneResourceSlot/list?scheduleResourceId=` | `competition:sceneResourceSlot:list` |
| GET | `/competition/sceneResourceSlot/{slotId}` | `competition:sceneResourceSlot:query` |
| POST | `/competition/sceneResourceSlot` | `competition:sceneResourceSlot:add` |
| POST | `/competition/sceneResourceSlot/batch` | `competition:sceneResourceSlot:add` |
| PUT | `/competition/sceneResourceSlot` | `competition:sceneResourceSlot:edit` |
| DELETE | `/competition/sceneResourceSlot/{slotIds}` | `competition:sceneResourceSlot:remove` |
| POST | `/competition/sceneResourceSlot/changeStatus` | `competition:sceneResourceSlot:changeStatus` |

Service：

```text
ICompetitionSceneResourceSlotService
CompetitionSceneResourceSlotServiceImpl
```

核心校验和行为：

- 时段必须关联存在的 `scheduleResourceId`。
- 单个新增时，后端根据赛场资源布置补齐 `scheduleId / resourceId / eventId`。
- `startTime` 和 `endTime` 必填，且结束时间必须晚于开始时间。
- `deviceCapacity > 0`。
- `deviceCapacity <= deployedDeviceCount`。
- `workstationCapacity = deviceCapacity * workstationsPerDevice`。
- 初始 `reservedDeviceCount = 0`，`reservedWorkstationCount = 0`。
- 初始剩余容量等于总容量。
- `slotStatus` 为空时默认 `PENDING`。
- 新增、编辑、批量生成都校验同一 `scheduleResourceId` 下时段不能重叠。
- 批量生成按照 `startTime` 到 `endTime` 之间的连续整段生成。
- 批量生成只要有任意重叠则整体失败。
- 编辑时保留已有预约数，并重新计算剩余容量。
- 编辑时 `deviceCapacity` 不能小于已预约设备数。
- 编辑时计算出的工位容量不能小于已预约工位数。
- 删除时段前检查已有预约占用数，已存在预约占用的时段不能删除。
- 状态切换为 `OPEN` 时要求剩余设备数大于 0。
- 当前没有严格状态机。

并发与容量：

- 用户提交预约时通过 mapper 的条件更新扣减设备容量和工位容量。
- 扣减时要求 `slot_status = 'OPEN'`、剩余设备数足够、剩余工位数足够。
- 扣减后如果剩余设备数或工位数为 0，时段状态会变为 `FULL`。
- 取消预约释放容量后，如果时段原状态为 `FULL`，会回到 `OPEN`。

### 5.5 用户端资源预约后端

Controller：

```text
UserCompetitionSceneResourceController
```

映射：

```text
@RequestMapping({"/userCompetition", "/competition/userCompetition"})
```

接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/competition/userCompetition/sceneResource/bookableList` | 查询当前用户可预约资源 |
| GET | `/competition/userCompetition/sceneResource/{scheduleResourceId}` | 查询可预约资源详情 |
| GET | `/competition/userCompetition/sceneResourceSlot/list?scheduleResourceId=` | 查询可预约时段 |
| POST | `/competition/userCompetition/sceneResourceReservation` | 提交预约 |
| GET | `/competition/userCompetition/sceneResourceReservation/myList` | 我的预约 |
| POST | `/competition/userCompetition/sceneResourceReservation/cancel` | 取消预约 |

Service：

```text
IUserCompetitionSceneResourceService
UserCompetitionSceneResourceServiceImpl
```

当前登录用户来源：

```text
SecurityUtils.getLoginUser().getSysUser().getUserId()
```

异常返回：

- 业务异常类型为 `CompetitionSceneReservationException`。
- Controller 捕获后返回 `AjaxResult.error(...)`。
- 当前错误响应会带 `code = 5008`、`errorCode`。
- 重复预约时会额外返回 `existingReservation` 摘要。

## 6. 当前用户端预约业务逻辑

### 6.1 可预约资源列表 bookableList

入口：

```text
GET /competition/userCompetition/sceneResource/bookableList
```

当前筛选流程：

1. 查询 `bookingStatus = OPEN` 的赛场资源布置。
2. 可按 `scheduleId / scheduleResourceId / resourceId` 过滤。
3. 校验预约开放窗口：`bookingOpenTime <= now` 且 `bookingCloseTime > now`。为空视为不限制。
4. 解析当前用户在该 `scheduleId` 下的预约主体。
5. 主体解析失败时列表接口静默跳过。
6. 校验当前主体是否有有效现场证件。
7. 证件无效时列表接口静默跳过。
8. 查询该布置下未来、开放、有剩余设备数的时段。
9. 构造资源 VO。
10. 只有存在下一可预约时段，或存在有效预约记录时，才加入返回列表。

因此，管理端看到 `schedule_resource` 为 `OPEN` 并不代表用户端一定能看到。用户端还要求：

- 当前用户能解析到对应 `schedule_target`。
- 有效现场证件存在且未过期。
- 有未来开放时段，或者已有未过期有效预约。

### 6.2 预约主体解析

当前主体统一来自 `competition_scene_schedule_target`。

有效 target 条件：

```text
del_flag = '0'
status = '0'
match_status = 'MATCHED'
```

团队主体：

- 当 target 中 `team_code` 非空时，按团队处理。
- `subject_type = TEAM`。
- `subject_code = team_code`。
- `team_code = team_code`。
- 团队成员通过报名信息查询，排除指导老师。
- 仅保留已支付、审核通过或审核状态为空的成员。
- 当前登录用户只要是该团队有效成员，即可代表本队预约。
- 不做队长校验。

个人主体：

- 当 target 中 `team_code` 为空，且 `config_dimension` 不是团队维度，并且 `target.user_id = 当前用户` 时，按个人处理。
- `subject_type = USER`。
- `subject_code = String.valueOf(userId)`。
- 个人比赛默认参与人数为 1。

当前特殊口径：

- 如果 `config_dimension = TEAM` 但 target 没有 `team_code`，严格查询会返回 `SUBJECT_NOT_RESOLVED`。
- 列表查询场景会静默跳过这类数据。
- 这也是当前最容易和实际数据发生冲突的地方。

### 6.3 有效现场证件校验

当前预约必须有有效现场证件。

查询条件：

- `scheduleId` 匹配。
- `credentialStatus = EFFECTIVE`。
- 团队主体按 `teamCode` 查询。
- 个人主体按 `userId` 查询。

有效期判断：

```text
validFrom 为空或 validFrom <= now
validTo 为空或 validTo >= now
```

注意：如果 `validTo` 保存为某天 `00:00:00`，则当天零点之后会被认为已经过期。例如 `2026-06-30 00:00:00` 在 `2026-06-30 10:00:00` 已经过期。

当前未新增独立冻结字段。未来如需冻结，建议继续在 `credential_status` 中扩展。

### 6.4 提交预约

入口：

```text
POST /competition/userCompetition/sceneResourceReservation
```

请求字段：

```text
slotId
idempotencyKey
```

当前用户端不允许自由填写预约设备数。

提交流程：

1. 校验 `slotId` 必填。
2. 校验 `idempotencyKey` 必填。
3. 按 `operatorUserId + idempotencyKey` 查询幂等记录，存在则直接返回已有预约。
4. 查询时段，要求时段存在、`slotStatus = OPEN`、`endTime > now`。
5. 查询赛场资源布置，要求存在。
6. 校验赛场资源布置 `bookingStatus = OPEN` 且在预约开放窗口内。
7. 解析预约主体。
8. 校验有效现场证件。
9. 查重同一 `scheduleId + subjectType + subjectCode` 的未过期有效预约。
10. 自动计算预约设备数。
11. 校验设备容量和工位容量。
12. 非共享占用时，校验同一时段没有其他未过期有效预约。
13. 条件更新扣减时段容量。
14. 写入预约记录。
15. 返回预约详情。

重复预约锁定范围：

```text
schedule_id + subject_type + subject_code
```

有效预约状态：

```text
RESERVED
CHECKED
```

有效预约还要求关联时段 `end_time > now`。

重复预约错误：

```text
ALREADY_RESERVED_BY_SUBJECT
```

并返回：

```text
existingReservation
```

### 6.5 预约设备数计算

预约单位始终是设备，不是工位。

团队：

```text
reservedDeviceCount = ceil(团队有效参赛成员数 / workstationsPerDevice)
```

个人：

```text
reservedDeviceCount = 1
```

工位数用途：

- 展示每台设备可容纳人数。
- 展示可覆盖工位数。
- 用于计算团队需要几台设备。
- 用于容量扣减和剩余容量展示。

### 6.6 共享和非共享占用

`sharedOccupancy = true`：

- 允许不同预约主体在同一时段共同预约。
- 前提是剩余设备数和剩余工位数足够。

`sharedOccupancy = false`：

- 同一时段只允许一个未过期有效预约主体。
- 只要该时段已有 `RESERVED / CHECKED` 且未过期预约，就拒绝其他主体预约。
- 错误码：

```text
EXCLUSIVE_SLOT_OCCUPIED
```

### 6.7 取消预约

入口：

```text
POST /competition/userCompetition/sceneResourceReservation/cancel
```

请求字段：

```text
reservationId
cancelReason
```

规则：

- 只有 `RESERVED` 状态可取消。
- `CHECKED / CANCELLED` 不可取消。
- 当前操作用户必须仍能解析为该预约对应的主体成员。
- 取消后预约状态更新为 `CANCELLED`。
- 同步释放时段设备容量和工位容量。
- 如果时段原状态为 `FULL`，释放后会回到 `OPEN`。

### 6.8 过期处理

当前不做自动过期任务。

数据库中的 `reservation_status` 不会被自动改为 `EXPIRED`。

列表和详情展示时根据时段 `end_time` 与当前时间计算 `expired` 标识。

重复预约查重时只阻止 `slot.end_time > now` 的有效预约。已过期预约不阻止后续新时段预约。

## 7. 管理端实现

管理端项目：

```text
old-code-admin
```

### 7.1 API 封装

```text
old-code-admin/src/api/tournament/sceneResource.js
old-code-admin/src/api/tournament/sceneScheduleResource.js
old-code-admin/src/api/tournament/sceneResourceSlot.js
```

当前 API 路径均使用 `/competition/...` 前缀。

### 7.2 资源管理页面

页面：

```text
old-code-admin/src/views/tournament/sceneResource/index.vue
```

功能：

- 资源列表查询。
- 按资源编号、资源名称、资源类型、资源状态筛选。
- 新增资源。
- 编辑资源。
- 删除资源。
- 批量删除。
- 资源状态切换。
- 权限按钮控制。
- 表单前端基础校验。

列表字段包括：

```text
资源编号
资源名称
资源类型
资源状态
品牌型号
设备数量
单台设备工位数
默认单场周期（分钟）
默认共享占用
是否需要运维确认
运维联系人
运维联系电话
排序
更新时间
```

表单字段包括：

```text
资源编号
资源名称
资源类型
资源状态
品牌型号
设备数量
单台设备工位数
默认单场周期（分钟）
默认共享占用
是否需要运维确认
运维联系人
运维联系电话
安全须知
注意事项
主要参数列表
使用说明
设备图片
管理员备注
排序
```

### 7.3 赛场安排“资源与预约”Tab

改造页面：

```text
old-code-admin/src/views/tournament/sceneSchedule/index.vue
```

新增组件：

```text
old-code-admin/src/views/tournament/sceneSchedule/components/ResourceReservationTab.vue
old-code-admin/src/views/tournament/sceneSchedule/components/ScheduleResourceDialog.vue
old-code-admin/src/views/tournament/sceneSchedule/components/ResourceSlotDialog.vue
```

保留原有 Tab：

```text
赛场安排
匹配对象
现场证件
操作流水
```

新增 Tab：

```text
资源与预约
```

当前功能：

- 未选择赛场安排时提示“请先选择一个赛场安排”。
- 选择赛场安排后查询该 `scheduleId` 下资源布置列表。
- 新增资源布置。
- 编辑资源布置。
- 删除资源布置。
- 发布预约，切换 `OPEN`。
- 暂停预约，切换 `PAUSED`。
- 关闭预约，切换 `CLOSED`。
- 打开配置时段弹窗。

资源布置弹窗行为：

- 新增时加载 `ENABLED` 状态资源。
- 选择资源后带出资源台账默认值。
- 前端预览总工位数，但后端计算为准。
- 编辑时不允许修改 `scheduleId`。

时段弹窗行为：

- 查询当前赛场资源布置下时段列表。
- 单个新增时段。
- 批量生成时段。
- 编辑时段。
- 删除时段。
- 开放 / 关闭时段。
- 展示设备容量、已预约设备数、剩余设备数、工位容量、已预约工位数、剩余工位数。
- 重叠时段由后端校验。

## 8. PC 端实现

PC 项目：

```text
old-code-pc
```

API：

```text
old-code-pc/src/api/personal/sceneResource.js
```

页面：

```text
old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue
old-code-pc/src/views/personal/index.vue
```

当前能力：

- 在个人中心挂载资源预约组件。
- 查询可预约资源列表。
- 查询我的预约。
- 查看资源基本信息、部署位置、容量、主体、建议预约设备数、可覆盖工位数。
- 提交预约时只传 `slotId` 和 `idempotencyKey`。
- 不允许用户自由填写预约设备数。
- 重复预约时展示已有预约信息。
- 支持取消 `RESERVED` 状态预约。

当前 PC 页面不是完整独立预约中心，属于个人中心内的预约入口。

## 9. 小程序实现

小程序项目：

```text
old-code-mini
```

API：

```text
old-code-mini/api/sceneResource.js
```

页面：

```text
old-code-mini/pages/scene-resource/index.vue
old-code-mini/pages/mine/index.vue
old-code-mini/pages.json
```

当前能力：

- `pages.json` 已注册 `pages/scene-resource/index`。
- 我的页面中有跳转到资源预约页面的方法。
- 资源预约页面调用用户端预约接口。
- 支持查询可预约资源、我的预约、提交预约、取消预约。
- 提交预约同样只传 `slotId` 和 `idempotencyKey`。

## 10. 权限码汇总

资源台账：

```text
competition:sceneResource:list
competition:sceneResource:query
competition:sceneResource:add
competition:sceneResource:edit
competition:sceneResource:remove
competition:sceneResource:changeStatus
```

赛场资源布置：

```text
competition:sceneScheduleResource:list
competition:sceneScheduleResource:query
competition:sceneScheduleResource:add
competition:sceneScheduleResource:edit
competition:sceneScheduleResource:remove
competition:sceneScheduleResource:changeBookingStatus
```

预约时段：

```text
competition:sceneResourceSlot:list
competition:sceneResourceSlot:query
competition:sceneResourceSlot:add
competition:sceneResourceSlot:edit
competition:sceneResourceSlot:remove
competition:sceneResourceSlot:changeStatus
```

用户端预约接口当前不使用管理端菜单权限码，但要求正常登录 token。

## 11. 已验证结果摘要

第一阶段：

- migration 已人工导入测试数据库。
- 四张表存在。
- 字段、索引、字符集与项目现有 `competition_scene_*` 表一致。
- 未出现 `team_id`、资产管理字段、`cancel_deadline_minutes`、`ops_status`。
- 资源台账接口用例通过。
- 后端编译通过。

第二阶段：

- 管理端资源管理菜单已导入并可见。
- 管理端资源管理页面可正常打开。
- 资源台账页面 CRUD 通过。
- `competition_scene_schedule_resource` 后端 CRUD 通过。
- 管理端 build 通过。

第三阶段：

- 赛场安排页面新增“资源与预约”Tab。
- 资源布置新增、编辑、删除、发布、暂停、关闭通过。
- 管理端 build 通过。

第四阶段：

- 时段列表、单个新增、批量生成、编辑、删除、开放 / 关闭通过。
- 设备容量和工位容量计算通过。
- 重叠时段校验通过。
- 管理端 build 通过。

第五阶段：

- 9205 已重启并加载 `UserCompetitionSceneResourceController`。
- 正式网关用户端接口全量通过。
- 个人预约通过。
- 团队预约通过。
- 重复预约返回 `ALREADY_RESERVED_BY_SUBJECT` 和 `existingReservation`。
- 有效证件校验通过。
- `schedule_target` 校验通过。
- 非共享占用校验通过。
- 共享占用容量足够时可由其他主体预约。
- 容量扣减和取消回补通过。
- 团队任意有效成员代表团队预约通过。
- 不使用 `team_id`。
- 不做队长校验。
- PC `npm run build` 通过。
- 小程序页面注册和 API 静态验证通过。

## 12. 当前测试账号和测试数据说明

第五阶段联调曾使用测试账号：

```text
ph5_user_a / Ph5Test@123 / userId 1591
ph5_user_b / Ph5Test@123 / userId 1592
ph5_user_c / Ph5Test@123 / userId 1593
ph5_user_d / Ph5Test@123 / userId 1594
```

第五阶段测试数据曾包含：

```text
scheduleId 10：共享个人预约场景
scheduleResourceId 10
slotId 25

scheduleId 11：非共享个人预约场景
scheduleResourceId 11
slotId 26

scheduleId 12：团队预约场景
teamCode PH5TEAM001
scheduleResourceId 12
slotId 27
```

这些数据为测试数据库数据，不应视为生产数据。

## 13. 已发现的实际数据口径问题

### 13.1 scheduleId = 3 管理端有资源布置，但用户端 bookableList 为空

现象：

管理端接口：

```text
GET /competition/sceneScheduleResource/list?pageNum=1&pageSize=10&scheduleId=3
```

可以查询到 `scheduleResourceId = 3`，资源 `电脑`，`bookingStatus = OPEN`。

用户端接口：

```text
GET /competition/userCompetition/sceneResource/bookableList
```

返回：

```json
{
  "msg": "操作成功",
  "code": 200,
  "data": []
}
```

排查结论：

1. `competition_scene_schedule_resource` 本身是 `OPEN`，开放窗口为 `2026-07-01 00:00:00` 到 `2026-07-20 00:00:00`。
2. 曾存在开放时段 `slot_id = 30`，时间为 `2026-07-01 00:00:00` 到 `2026-07-01 18:00:00`，但在 2026-07-02 已经过期。
3. `scheduleId = 3` 下的 `competition_scene_schedule_target` 多条数据为 `config_dimension = TEAM`，但 `team_code = null`。
4. 当前用户端主体解析逻辑认为团队 target 必须有 `team_code`，否则不能解析为团队主体。
5. `scheduleId = 3` 下现场证件 `valid_to` 多为 `2026-06-30 00:00:00`，按当前实现已经过期。

因此用户端列表为空不是单纯接口错误，而是当前实现的数据准入条件共同导致：

- 团队 target 缺少 `team_code`。
- 现场证件已过期。
- 可预约时段已过期。

这部分非常适合在后续修订中明确真实业务口径，例如：

- 团队维度但没有 `team_code` 时是否允许按 `target_id` 或报名记录推导主体。
- 证件有效期如果只配置日期，是否应默认有效至当天 23:59:59。
- 用户端列表在无时段但有开放资源时是否仍展示。

### 13.2 JWT / common-core 风险

历史联调中发现：

- 单模块 `mvn spring-boot:run` 启动 competition 时，可能因依赖或 JWT 密钥实现不一致导致 token 验签问题。
- 正式联调采用加载正确 classpath 的 9205 competition 服务后通过。

建议后续继续关注 auth / gateway / competition 的 common-core 依赖版本和 JwtUtils 实现一致性。

## 14. 当前仍未开发的能力

明确未做：

- 管理端预约记录强化列表。
- 管理端预约详情。
- 管理端预约核销。
- 管理端预约取消或异常处理。
- 资源、赛场、时段维度统计。
- 现场使用状态展示。
- 预约核销二维码流程与资源预约记录打通。
- 短信、微信通知。
- IoT 设备状态采集。
- 运维工单。
- 独立运维端。
- 复杂审批流。
- 自动过期任务。
- 生产数据库 migration 执行。

## 15. 后续修订最可能影响的代码点

如果要调整业务逻辑，优先关注以下位置。

主体识别：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java
```

重点方法：

```text
resolveSubject
resolveAllSubjects
buildTeamSubject
buildUserSubject
selectTeamPlayers
```

证件校验：

```text
hasValidCredential
requireValidCredential
isCredentialValid
```

预约查重和提交：

```text
submitReservation
calculateReservedDeviceCount
```

取消预约：

```text
cancelReservation
```

有效预约 SQL：

```text
old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceReservationMapper.xml
selectEffectiveReservationBySubject
countEffectiveReservationBySlot
selectVisibleCompetitionSceneResourceReservationList
```

容量扣减和回补：

```text
old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml
reserveCompetitionSceneResourceSlotCapacity
releaseCompetitionSceneResourceSlotCapacity
```

时段校验：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotServiceImpl.java
```

PC 页面：

```text
old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue
old-code-pc/src/api/personal/sceneResource.js
```

小程序页面：

```text
old-code-mini/pages/scene-resource/index.vue
old-code-mini/api/sceneResource.js
```

管理端资源与预约 Tab：

```text
old-code-admin/src/views/tournament/sceneSchedule/components/ResourceReservationTab.vue
old-code-admin/src/views/tournament/sceneSchedule/components/ScheduleResourceDialog.vue
old-code-admin/src/views/tournament/sceneSchedule/components/ResourceSlotDialog.vue
```

## 16. 修订时建议重点确认的问题

请在本文件基础上优先确认以下业务问题，再进入下一轮改造。

1. 团队 target 如果没有 `team_code`，是否允许预约。
2. 如果允许，主体锁定字段应使用什么：`target_id`、报名 ID、队伍名称、还是补齐 `team_code`。
3. `config_dimension = TEAM` 但实际按个人参赛的历史数据如何兼容。
4. 团队有效成员的认定是否仍依赖报名表中的支付状态和审核状态。
5. 指导老师是否永远不能作为预约操作人。
6. 现场证件有效期只配置日期时，结束日期是否应自动扩展到当天 23:59:59。
7. 用户端可预约资源列表是否必须存在未来开放时段才展示。
8. 已有预约但时段过期后，用户端是否仍展示历史预约。
9. 同一 `schedule_id` 下是否只允许一个有效预约，还是允许按不同资源分别预约。
10. 非共享占用是否只限制同一时段，还是限制同一资源整天或同一赛场。
11. 取消预约是否需要限制赛前多少分钟不可取消。
12. 管理端是否允许强制取消用户预约。
13. 预约核销是否和现有现场扫码核验合并，还是新增资源预约核销入口。
14. 统计维度以赛场、资源、时段、主体、核销状态中的哪些为主。
15. PC 和小程序页面是否需要拆成独立菜单，而不是放在个人中心 / 我的页入口。

## 17. 建议的下一轮改造方式

建议流程：

1. 先由人工在本文件中直接修改业务口径。
2. 对修改处标记“必须改代码”或“仅文案 / 流程说明调整”。
3. 根据修订后的文件生成差异清单。
4. 先改后端主体解析、证件校验、预约查重和列表过滤。
5. 再改 PC / 小程序展示和错误提示。
6. 最后补管理端统计、核销和全链路冒烟测试。

如果本文件中的描述与最新人工确认不一致，应以最新人工确认后的修订版为准。

