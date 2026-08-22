# 大赛现场设备资源管理与预约 - 第一阶段数据库验证报告

验证时间：2026-07-01  
验证库：`dev-mysql57 / jiaoxue_test`  
验证范围：第一阶段 migration `db/migration/20260701_competition_scene_resource_p1_001.sql`

## 一、执行命令

```sql
SHOW TABLES LIKE 'competition_scene_resource%';
SHOW TABLES LIKE 'competition_scene_schedule_resource';
SHOW CREATE TABLE competition_scene_resource;
SHOW CREATE TABLE competition_scene_schedule_resource;
SHOW CREATE TABLE competition_scene_resource_slot;
SHOW CREATE TABLE competition_scene_resource_reservation;
```

另使用 `information_schema.columns`、`information_schema.statistics`、`information_schema.tables` 做字段、索引和字符集反查。

## 二、表存在性

`SHOW TABLES LIKE 'competition_scene_resource%'` 返回：

- `competition_scene_resource`
- `competition_scene_resource_reservation`
- `competition_scene_resource_slot`

`SHOW TABLES LIKE 'competition_scene_schedule_resource'` 返回：

- `competition_scene_schedule_resource`

结论：四张第一阶段表均已存在。

## 三、competition_scene_resource 验证

字段完整，包含：

- `resource_id`
- `resource_code`
- `resource_name`
- `resource_type`
- `resource_status`
- `brand_model`
- `device_quantity`
- `workstation_count`
- `default_slot_duration_minutes`
- `default_shared_occupancy`
- `need_ops_confirm`
- `ops_contact_name`
- `ops_contact_phone`
- `safety_notice`
- `attention_notes`
- `parameter_json`
- `usage_instructions`
- `image_urls`
- `admin_remark`
- `sort_order`
- `create_by`
- `create_time`
- `update_by`
- `update_time`
- `deleted`

关键字段说明已落库：

- `workstation_count` 注释为“单台设备工位数”
- `default_slot_duration_minutes` 注释为“默认单场占用周期，单位分钟”
- `need_ops_confirm` 注释为“是否需要运维确认，仅提示字段”

索引已存在：

- `PRIMARY(resource_id)`
- `uk_scene_resource_code(resource_code, deleted)`
- `idx_scene_resource_type(resource_type)`
- `idx_scene_resource_status(resource_status)`

## 四、其余三表验证

`competition_scene_schedule_resource` 已包含赛场资源布置基础字段，含：

- `schedule_resource_id`
- `schedule_id`
- `resource_id`
- `event_id`
- `deployment_location`
- `deployed_device_count`
- `workstations_per_device`
- `total_workstations`
- `slot_duration_minutes`
- `shared_occupancy`
- `need_ops_confirm`
- `booking_status`
- `booking_open_time`
- `booking_close_time`
- 覆盖说明字段、审计字段、`deleted`

`competition_scene_resource_slot` 已包含时段容量字段和 `version` 乐观锁字段。

`competition_scene_resource_reservation` 已按最新人工确认保留统一预约主体字段：

- `subject_type`
- `subject_code`
- `team_code`
- `user_id`
- `operator_user_id`

未出现数字型 `team_id`。

## 五、禁用字段反查

反查以下字段：

- `team_id`
- `asset_no`
- `owner_unit`
- `storage_location`
- `cancel_deadline_minutes`
- `ops_status`

结果：四张新表中均不存在。

## 六、字符集与排序规则

四张新表均为：

- `DEFAULT CHARSET=utf8mb4`
- `COLLATE=utf8mb4_general_ci`

当前已有现场运行表：

- `competition_scene_schedule`
- `competition_scene_schedule_target`
- `competition_scene_credential`
- `competition_scene_operation_log`

均为 `utf8mb4_general_ci`。新表与现有 `competition_scene_*` 现场运行表保持一致。

说明：测试库默认库级排序规则为 `utf8mb4_unicode_ci`，但现有现场运行表使用 `utf8mb4_general_ci`，因此本次 migration 与现场运行表体系一致。

## 七、验证结论

数据库结构验证通过。

- 四张表均已建立；
- `competition_scene_resource` 字段完整；
- `workstation_count` 已明确为单台设备工位数；
- `default_slot_duration_minutes` 已存在；
- 禁用字段均未出现；
- 索引已建立；
- 字符集与现有现场运行表一致。
