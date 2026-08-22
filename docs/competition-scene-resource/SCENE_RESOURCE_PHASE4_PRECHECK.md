# 大赛现场设备资源管理与预约 - 第四阶段前置检查

检查日期：2026-07-01  
检查目标：确认第三阶段能力运行状态、预约时段表结构基础、第四阶段新增接口当前状态。

## 一、已确认表字段

`competition_scene_resource_slot` 已在第一阶段 migration 中建表，字段包括：

- `slot_id`
- `schedule_resource_id`
- `schedule_id`
- `resource_id`
- `event_id`
- `start_time`
- `end_time`
- `device_capacity`
- `reserved_device_count`
- `remaining_device_count`
- `workstation_capacity`
- `reserved_workstation_count`
- `remaining_workstation_count`
- `slot_status`
- `version`
- `create_by`
- `create_time`
- `update_by`
- `update_time`
- `deleted`

索引包括：

- `idx_scene_resource_slot_schedule_resource`
- `idx_scene_resource_slot_schedule`
- `idx_scene_resource_slot_resource`
- `idx_scene_resource_slot_event`

说明：`version` 已存在，本阶段只做时段维护；真正预约提交时的防超卖条件更新放到后续阶段。

## 二、第三阶段接口复验

使用 `admin / qwe123!@#` 登录 auth 获取 token 后，经网关访问：

```http
GET http://127.0.0.1:9889/competition/sceneScheduleResource/list?scheduleId=3&pageNum=1&pageSize=5
```

返回 `code=200`，并可查询到已布置资源：

```json
{
  "scheduleResourceId": 3,
  "scheduleId": 3,
  "resourceId": 5,
  "deploymentLocation": "123",
  "deployedDeviceCount": 1,
  "workstationsPerDevice": 1,
  "totalWorkstations": 1,
  "slotDurationMinutes": 30,
  "bookingStatus": "OPEN",
  "resourceName": "电脑"
}
```

结论：第三阶段 `sceneScheduleResource` 已在网关和正式 `9205` 运行态可用。

## 三、第四阶段接口当前状态

当前访问：

```http
GET http://127.0.0.1:9889/competition/sceneResourceSlot/list?scheduleResourceId=1&pageNum=1&pageSize=5
```

返回：

```json
{"msg":"No static resource sceneResourceSlot/list.","code":500}
```

结论：第四阶段 `sceneResourceSlot` 后端接口尚未开发，当前返回符合预期。

## 四、JWT / common-core 风险

继续保留既有风险：

- auth / gateway / competition 必须使用一致的 common-core；
- 当前工作区 `JwtUtils` 使用静态密钥 `abcdefghijklmnopqrstuvwxyz`；
- 本机 `.m2` 已安装的 `teaching-common-core-3.6.6.jar` 与工作区编译产物存在差异；
- 后续启动或重启服务后需要复验 token 签发与验签链路。

## 五、处理结论

第四阶段可以进入编码。

本阶段继续遵守：

- 不开发用户端预约；
- 不开发小程序；
- 不开发预约提交、取消、核销；
- 不开发独立运维端；
- 不引入 `team_id`、资产管理字段、`cancelDeadlineMinutes`、`opsConfirm`。
