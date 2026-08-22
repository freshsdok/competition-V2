# 大赛现场设备资源管理与预约接口设计

更新时间：2026-06-30

## 1. 接口边界

接口继续接入 competition 服务，不新建独立现场系统，不使用旧 `site_*` 接口体系。

接口分类：

- 资源台账。
- 赛场资源布置。
- 预约时段。
- 管理端预约记录。
- 用户端资源预约。

Controller 建议兼容网关剥离前缀，例如：

```java
@RequestMapping({"/sceneResource", "/competition/sceneResource"})
```

## 2. 统一预约流程

本版不再区分团队预约和个人预约两套流程。

用户端预约接口只提交：

- `slotId`
- `idempotencyKey`

后端统一执行：

1. 根据 `slotId` 查询 slot、schedule resource、schedule。
2. 根据当前登录用户和 `schedule_id` 解析 `reservation_subject`。
3. 校验当前用户是该 subject 的有效成员。
4. 校验 subject 在 `competition_scene_schedule_target` 中。
5. 校验 subject 拥有有效现场证件。
6. 查询同一 `schedule_id + subject_type + subject_code` 是否已有未过期有效预约。
7. 根据 subject 自动计算预约设备数。
8. 校验非共享占用和时段容量。
9. 条件更新扣减容量。
10. 创建预约记录。

## 3. reservation subject

后端返回和内部使用统一字段：

- `subjectType`：`TEAM` 或 `USER`。
- `subjectCode`：团队为 `teamCode`，个人为 `userId` 或 target 引用。
- `teamCode`：团队预约时返回。
- `userId`：个人预约时返回；团队预约可返回操作人 ID。
- `operatorUserId`：实际操作人。

说明：

- 现有系统不存在稳定数字型 `teamId`。
- 第一阶段以 `teamCode` 为团队主体标识。
- 后端接口字段使用 `teamCode`，不使用 `teamId`。

## 4. 预约查重和已有预约返回

有效预约锁定范围：

```text
scheduleId + subjectType + subjectCode
```

有效状态：

- `RESERVED`
- `CHECKED`

如果关联 slot 已过期：

- 查询列表可展示为“已过期”。
- 不自动修改数据库预约状态。
- 不阻止同一 subject 预约后续新时段。

重复预约时返回：

```json
{
  "code": 500,
  "msg": "当前参赛主体已有有效预约",
  "errorCode": "ALREADY_RESERVED_BY_SUBJECT",
  "existingReservation": {
    "reservationId": 1,
    "resourceName": "算法训练工作站",
    "deploymentLocation": "A馆 2层",
    "startTime": "2026-07-20 09:00:00",
    "endTime": "2026-07-20 10:00:00",
    "reservedDeviceCount": 2,
    "coveredWorkstationCount": 4,
    "reservationStatus": "RESERVED",
    "checkStatus": "UNCHECKED",
    "operatorUserId": 10001
  }
}
```

## 5. 设备数计算

用户不能自由调整预约设备数。

后端自动计算：

- 团队 subject：`ceil(参赛选手人数 / workstationsPerDevice)`。
- 个人 subject：`1`。
- `coveredWorkstationCount = reservedDeviceCount × workstationsPerDevice`。

如果剩余设备数不足，返回 `CAPACITY_NOT_ENOUGH`。

## 6. 资源台账接口

### 6.1 查询列表

`GET /competition/sceneResource/list`

参数：

- `resourceCode`
- `resourceName`
- `resourceType`
- `resourceStatus`
- `pageNum`
- `pageSize`

### 6.2 查询详情

`GET /competition/sceneResource/{resourceId}`

### 6.3 新增资源

`POST /competition/sceneResource`

请求体核心字段：

```json
{
  "resourceCode": "DEV-001",
  "resourceName": "算法训练工作站",
  "resourceType": "DEVICE",
  "resourceStatus": "ENABLED",
  "brandModel": "Brand X / Model Y",
  "deviceQuantity": 10,
  "workstationCount": 2,
  "defaultSlotDurationMinutes": 60,
  "defaultSharedOccupancy": true,
  "needOpsConfirm": false,
  "opsContactName": "张三",
  "opsContactPhone": "13800000000",
  "safetyNotice": "使用前确认电源和环境。",
  "attentionNotes": "请勿擅自更改配置。",
  "parameterJson": "[{\"name\":\"GPU\",\"value\":\"RTX\"}]",
  "usageInstructions": "按现场说明使用。",
  "imageUrls": "[\"https://example.com/device.png\"]",
  "adminRemark": "决赛使用",
  "sortOrder": 1
}
```

### 6.4 修改资源

`PUT /competition/sceneResource`

资源台账修改不自动影响已布置资源。

### 6.5 删除资源

`DELETE /competition/sceneResource/{resourceIds}`

资源存在未删除赛场布置时，建议拒绝删除。

### 6.6 修改资源状态

`POST /competition/sceneResource/changeStatus`

```json
{
  "resourceId": 1,
  "resourceStatus": "MAINTENANCE"
}
```

## 7. 赛场资源布置接口

### 7.1 查询布置列表

`GET /competition/sceneScheduleResource/list?scheduleId=`

返回字段：

- `scheduleResourceId`
- `scheduleId`
- `resourceId`
- `resourceName`
- `resourceType`
- `brandModel`
- `deploymentLocation`
- `deployedDeviceCount`
- `workstationsPerDevice`
- `totalWorkstations`
- `slotDurationMinutes`
- `sharedOccupancy`
- `needOpsConfirm`
- `opsContactName`
- `opsContactPhone`
- `bookingStatus`
- `openSlotCount`
- `reservedDeviceCount`
- `remainingDeviceCount`

### 7.2 新增布置

`POST /competition/sceneScheduleResource`

```json
{
  "scheduleId": 1001,
  "resourceId": 10,
  "deploymentLocation": "A馆 2层 机房1",
  "deployedDeviceCount": 8,
  "workstationsPerDevice": 2,
  "slotDurationMinutes": 60,
  "sharedOccupancy": true,
  "needOpsConfirm": false,
  "opsContactName": "张三",
  "opsContactPhone": "13800000000",
  "safetyNoticeOverride": "",
  "attentionNotesOverride": "",
  "usageInstructionsOverride": "",
  "adminRemark": "上午场使用"
}
```

后端计算：

- `totalWorkstations = deployedDeviceCount × workstationsPerDevice`。
- 初始 `bookingStatus = DRAFT`。
- `eventId = competitionSeriesId`。

### 7.3 修改布置

`PUT /competition/sceneScheduleResource`

已有有效预约时，不允许把容量调低到低于已预约设备/工位数。

### 7.4 删除布置

`DELETE /competition/sceneScheduleResource/{ids}`

存在未过期 `RESERVED/CHECKED` 预约时拒绝删除。

### 7.5 修改预约发布状态

`POST /competition/sceneScheduleResource/changeBookingStatus`

```json
{
  "scheduleResourceId": 1,
  "bookingStatus": "OPEN"
}
```

不设计独立 `opsConfirm` 接口。

## 8. 预约时段接口

### 8.1 查询时段列表

`GET /competition/sceneResourceSlot/list?scheduleResourceId=`

返回：

- `slotId`
- `startTime`
- `endTime`
- `deviceCapacity`
- `reservedDeviceCount`
- `remainingDeviceCount`
- `workstationCapacity`
- `reservedWorkstationCount`
- `remainingWorkstationCount`
- `slotStatus`
- `version`
- `displayExpired`

### 8.2 新增时段

`POST /competition/sceneResourceSlot`

```json
{
  "scheduleResourceId": 1,
  "startTime": "2026-07-20 09:00:00",
  "endTime": "2026-07-20 10:00:00",
  "deviceCapacity": 4,
  "slotStatus": "PENDING"
}
```

### 8.3 批量生成时段

`POST /competition/sceneResourceSlot/batch`

```json
{
  "scheduleResourceId": 1,
  "date": "2026-07-20",
  "startTime": "09:00",
  "endTime": "18:00",
  "slotDurationMinutes": 60,
  "deviceCapacity": 4,
  "slotStatus": "OPEN"
}
```

系统自动计算：

- `workstationCapacity = deviceCapacity × workstationsPerDevice`。

### 8.4 修改时段

`PUT /competition/sceneResourceSlot`

已有预约时，不允许把容量改到小于已预约容量。

### 8.5 删除时段

`DELETE /competition/sceneResourceSlot/{slotIds}`

存在未过期 `RESERVED/CHECKED` 预约时拒绝删除。

### 8.6 修改时段状态

`POST /competition/sceneResourceSlot/changeStatus`

```json
{
  "slotId": 1,
  "slotStatus": "CLOSED"
}
```

## 9. 管理端预约记录接口

### 9.1 查询预约列表

`GET /competition/sceneResourceReservation/list`

参数：

- `scheduleId`
- `scheduleResourceId`
- `slotId`
- `resourceName`
- `subjectType`
- `subjectCode`
- `teamCode`
- `userId`
- `operatorUserId`
- `reservationStatus`
- `checkStatus`
- `pageNum`
- `pageSize`

返回字段：

- `reservationId`
- `resourceName`
- `deploymentLocation`
- `startTime`
- `endTime`
- `displayExpired`
- `subjectType`
- `subjectCode`
- `subjectName`
- `teamCode`
- `userId`
- `operatorUserId`
- `reservedDeviceCount`
- `coveredWorkstationCount`
- `reservationStatus`
- `checkStatus`
- `createTime`

### 9.2 管理端取消预约

`POST /competition/sceneResourceReservation/cancel`

```json
{
  "reservationId": 1,
  "cancelReason": "现场调整"
}
```

规则：

- `RESERVED` 且 `UNCHECKED` 可取消。
- `CHECKED/CANCELLED` 不可取消。

### 9.3 管理端核销预约

`POST /competition/sceneResourceReservation/check`

```json
{
  "reservationId": 1
}
```

第一阶段保留管理端核销接口。现场管理员小程序扫码核销可复用已有 `sceneVerify` 能力，但不在本阶段做复杂流程。

## 10. 用户端接口

### 10.1 可预约资源列表

`GET /competition/userCompetition/sceneResource/bookableList`

过滤规则：

- 当前用户能解析到 `reservation_subject`。
- 当前用户是该 subject 的有效成员。
- subject 在 `schedule_target` 中。
- subject 有有效现场证件。
- 资源布置 `bookingStatus = OPEN`。
- 存在 `slotStatus = OPEN` 且有剩余设备的未过期时段。

返回：

- `scheduleResourceId`
- `scheduleId`
- `resourceId`
- `resourceName`
- `resourceType`
- `brandModel`
- `deploymentLocation`
- `remainingDeviceCount`
- `remainingWorkstationCount`
- `workstationsPerDevice`
- `nextAvailableStartTime`
- `nextAvailableEndTime`
- `subjectType`
- `subjectCode`
- `teamCode`
- `userId`
- `hasExistingReservation`
- `existingReservation`

### 10.2 用户端资源详情

`GET /competition/userCompetition/sceneResource/{scheduleResourceId}`

返回：

- 资源和部署信息。
- 安全须知、注意事项、参数、说明、图片。
- subject 信息。
- 后端计算的 `calculatedDeviceCount`。
- `coveredWorkstationCount`。
- 当前未过期已有预约摘要。

### 10.3 用户端时段列表

`GET /competition/userCompetition/sceneResourceSlot/list?scheduleResourceId=`

返回：

- `slotId`
- `startTime`
- `endTime`
- `remainingDeviceCount`
- `remainingWorkstationCount`
- `workstationsPerDevice`
- `calculatedDeviceCount`
- `coveredWorkstationCount`
- `canReserve`
- `disabledReason`
- `existingReservation`

### 10.4 用户提交预约

`POST /competition/userCompetition/sceneResourceReservation`

```json
{
  "slotId": 1,
  "idempotencyKey": "client-generated-uuid"
}
```

说明：

- 不传 `subjectType`。
- 不传 `subjectCode`。
- 不传 `reservedDeviceCount`。
- 不传 `teamId`。
- 后端自动解析 subject 和计算设备数。

### 10.5 我的预约列表

`GET /competition/userCompetition/sceneResourceReservation/myList`

团队 subject：

- 团队任意有效参赛成员可查看本队预约。

个人 subject：

- 个人参赛者只能查看自己的预约。

### 10.6 用户取消预约

`POST /competition/userCompetition/sceneResourceReservation/cancel`

```json
{
  "reservationId": 1,
  "cancelReason": "计划调整"
}
```

团队 subject：

- 第一阶段建议团队任意有效参赛成员可取消本队未核销预约。
- 该取消权限仍建议业务确认；若需限制，可后续收紧为预约操作人或指定角色。

个人 subject：

- 个人本人可取消自己的未核销预约。

## 11. 错误码

新增：

- `ALREADY_RESERVED_BY_SUBJECT`：当前参赛主体已有有效预约。
- `SUBJECT_NOT_RESOLVED`：无法识别当前用户对应的参赛主体。
- `SUBJECT_MEMBER_INVALID`：当前用户不是该参赛主体有效成员。

保留：

- `NO_VALID_CREDENTIAL`：未找到有效现场证件。
- `NOT_SCHEDULE_TARGET`：当前主体不在赛场安排匹配对象中。
- `RESOURCE_NOT_OPEN`：资源未开放预约。
- `SLOT_NOT_OPEN`：时段未开放。
- `CAPACITY_NOT_ENOUGH`：剩余设备或工位不足。
- `EXCLUSIVE_SLOT_OCCUPIED`：非共享时段已被占用。
- `DUPLICATE_RESERVATION`：重复提交或幂等冲突。
- `RESERVATION_NOT_CANCELABLE`：当前预约不可取消。
- `IDEMPOTENCY_KEY_REQUIRED`：缺少幂等键。
