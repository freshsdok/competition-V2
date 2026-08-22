# 大赛现场设备资源管理与预约管理端界面计划

更新时间：2026-06-30

## 1. 界面边界

管理端不做独立运维端，不做资产管理页面，不接入旧 `site_*` 页面体系。

管理端包含：

- 资源管理菜单：设备资源台账登记。
- 现场安排配置页新增 `资源与预约` Tab：资源布置、时段配置、预约发布。
- 预约记录：查看、取消、核销。

预约主体逻辑已调整：

- 不展示“队长预约/非队长禁止预约”的规则。
- 团队参赛时，队伍中任意有效参赛成员都可以代表本队预约。
- 管理端预约记录以 `subjectType + subjectCode` 展示真正锁定的参赛主体。
- `operatorUserId` 只表示实际操作人。

## 2. 资源管理菜单

### 2.1 菜单位置

建议在现有赛事管理/大赛管理目录下新增：

- 菜单名称：资源管理。
- 组件路径：`tournament/sceneResource/index`。

权限建议：

- `competition:sceneResource:list`
- `competition:sceneResource:query`
- `competition:sceneResource:add`
- `competition:sceneResource:edit`
- `competition:sceneResource:remove`
- `competition:sceneResource:changeStatus`

### 2.2 查询区

字段：

- 资源编号。
- 资源名称。
- 资源类型。
- 资源状态。

### 2.3 列表字段

- 资源编号。
- 资源名称。
- 资源类型。
- 资源状态。
- 品牌型号。
- 设备数量。
- 单台设备工位数，字段为 `workstationCount`，对应数据库 `workstation_count`。
- 默认单场周期，字段为 `defaultSlotDurationMinutes`，对应数据库 `default_slot_duration_minutes`，单位分钟。
- 默认共享占用。
- 是否需要运维确认。
- 运维联系人。
- 运维联系电话。
- 排序。
- 操作。

操作：

- 查看。
- 编辑。
- 启用/停用/维护中。
- 删除。

### 2.4 资源表单

字段：

- 资源编号。
- 资源名称。
- 资源类型。
- 资源状态。
- 品牌型号。
- 设备数量。
- 单台设备工位数，字段为 `workstationCount`，对应数据库 `workstation_count`。
- 默认单场周期，字段为 `defaultSlotDurationMinutes`，对应数据库 `default_slot_duration_minutes`，单位分钟。
- 默认共享占用。
- 是否需要运维确认。
- 运维联系人。
- 运维联系电话。
- 安全须知。
- 注意事项。
- 主要参数列表。
- 使用说明。
- 设备图片。
- 管理员备注。
- 排序。

不展示：

- 资产编号。
- 所属单位。
- 存放位置。
- 最晚取消时间。
- 运维状态。
- 运维确认按钮。

## 3. 赛场安排 - 资源与预约 Tab

### 3.1 Tab 位置

在现有现场安排配置页新增：

- Tab 名称：资源与预约。
- 与当前选中的 `competition_scene_schedule` 绑定。

建议 Tab 顺序：

1. 赛场安排。
2. 匹配对象。
3. 现场证件。
4. 资源与预约。
5. 操作流水。

原因：

- 资源预约依赖赛场安排。
- 预约主体来自匹配对象。
- 预约必须校验有效现场证件。

### 3.2 顶部上下文

展示当前赛场安排：

- 安排名称。
- 赛事名称。
- 赛道/组别。
- 证件类型。
- 配置维度。
- 赛场时间。
- 赛场地点。

### 3.3 资源布置表格

字段：

- 资源名称。
- 资源类型。
- 部署位置。
- 部署设备数。
- 每台工位数。
- 总工位数。
- 单场周期。
- 共享占用。
- 预约状态。
- 开放时段数。
- 已预约设备数。
- 剩余设备数。
- 操作。

操作：

- 选择资源。
- 编辑布置。
- 配置时段。
- 发布预约。
- 暂停预约。
- 关闭预约。
- 移除资源。

发布预约提示：

> 发布后，符合该赛场安排匹配对象且已有有效现场证件的参赛主体可预约。团队参赛时，队伍中任意有效参赛成员均可代表本队预约；同一赛场安排下同一参赛主体只能有一条未过期有效预约。

### 3.4 资源布置弹窗

字段：

- 选择资源。
- 部署位置。
- 部署设备数。
- 每台设备工位数。
- 单场占用周期。
- 是否共享占用。
- 是否需要运维确认。
- 运维联系人。
- 运维联系电话。
- 安全须知。
- 注意事项。
- 使用说明。
- 管理员备注。

默认带入：

- 品牌型号。
- 资源台账设备数量。
- 单台设备工位数。
- 默认单场周期。
- 默认共享占用。
- 运维联系人和电话。
- 安全须知、注意事项、使用说明。

校验提示：

- 部署设备数不能大于资源台账设备数量。
- 总工位数由系统计算。
- 已有未过期有效预约时，不允许将容量调低到低于已预约容量。

### 3.5 时段配置弹窗

字段：

- 日期。
- 开始时间。
- 结束时间。
- 每场时长。
- 每时段设备容量。
- 系统自动计算每时段工位容量。
- 是否立即开放。

展示计算：

- 每时段工位容量 = 每时段设备容量 × 每台设备工位数。
- 可生成时段数量。
- 重叠时段跳过数量。

### 3.6 时段列表

字段：

- 开始时间。
- 结束时间。
- 设备容量。
- 已预约设备数。
- 剩余设备数。
- 工位容量。
- 已预约工位数。
- 剩余工位数。
- 时段状态。
- 是否已过期。
- 操作。

说明：

- “已过期”根据当前时间和 `endTime` 展示。
- 本阶段不做自动过期任务。

## 4. 预约记录管理

### 4.1 列表字段

- 资源名称。
- 部署位置。
- 预约时段。
- 是否已过期。
- 主体类型。
- 主体编码。
- 团队编号。
- 个人用户。
- 实际操作人。
- 预约设备数。
- 覆盖工位数。
- 预约状态。
- 核销状态。
- 创建时间。
- 操作。

字段说明：

- `subjectType`：TEAM/USER。
- `subjectCode`：真正锁定的预约主体。
- `teamCode`：团队预约时展示。
- `operatorUserId`：实际提交预约的人，不作为查重主体。

### 4.2 操作

- 取消预约。
- 核销预约。

取消规则：

- `RESERVED` 且未核销可取消。
- `CHECKED/CANCELLED` 不可取消。

核销规则：

- `RESERVED` 可核销。
- 核销后不可取消。

### 4.3 已有预约展示

当用户端返回 `ALREADY_RESERVED_BY_SUBJECT` 时，管理端预约记录应能定位：

- 哪个 `subjectCode` 已预约。
- 哪个成员提交了预约。
- 预约资源、时间、设备数。

## 5. 前端文件建议

新增 API：

- `old-code-admin/src/api/tournament/sceneResource.js`
- `old-code-admin/src/api/tournament/sceneScheduleResource.js`
- `old-code-admin/src/api/tournament/sceneResourceSlot.js`
- `old-code-admin/src/api/tournament/sceneResourceReservation.js`

新增页面：

- `old-code-admin/src/views/tournament/sceneResource/index.vue`

现场安排页建议拆组件：

- `components/ScheduleResourceTab.vue`
- `components/ScheduleResourceForm.vue`
- `components/ResourceSlotDialog.vue`
- `components/ResourceReservationTable.vue`

## 6. 管理端验收

必须能验证：

- 管理员可维护资源台账。
- 管理员可在赛场安排中布置资源。
- 管理员可批量生成预约时段。
- 管理员可发布/暂停/关闭预约。
- 预约记录展示 subject 和 operator。
- 团队中不同成员发起的预约最终锁定同一 `teamCode`。
- 管理端可看到已有预约并核销。
