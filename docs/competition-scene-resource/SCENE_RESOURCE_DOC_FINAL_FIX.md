# 大赛现场设备资源设计文档最终命名修订记录

更新时间：2026-07-01

## 1. 默认单场周期字段

已统一命名：

- 数据库字段：`default_slot_duration_minutes`
- 接口字段：`defaultSlotDurationMinutes`

废弃旧命名：

- `default_slot_duration`
- `defaultSlotDuration`

## 2. 单台设备工位数字段

已明确：

- `competition_scene_resource.workstation_count` 表示单台设备工位数。
- 接口字段为 `workstationCount`。
- 工位数只用于容量展示和设备数计算，不是预约单位。

## 3. 修订范围

已同步修订：

- `SCENE_RESOURCE_DB_DESIGN.md`
- `SCENE_RESOURCE_API_DESIGN.md`
- `SCENE_RESOURCE_ADMIN_UI_PLAN.md`
- `SCENE_RESOURCE_DEV_TASKS.md`
