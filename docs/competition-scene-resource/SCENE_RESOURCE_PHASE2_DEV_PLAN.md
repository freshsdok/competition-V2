# 大赛现场设备资源管理与预约 - 第二阶段开发计划

## 一、第二阶段目标

第二阶段只做两部分：

1. 管理端资源管理菜单和页面；
2. `competition_scene_schedule_resource` 后端 CRUD，为后续赛场安排“资源与预约”Tab 做准备。

本阶段仍不开发：

- 小程序预约页面；
- 完整预约提交逻辑；
- 预约时段批量生成；
- 赛场安排“资源与预约”Tab 完整前端；
- 独立运维端；
- `opsConfirm` 接口。

## 二、管理端资源管理页面

建议页面路径：

- `old-code-admin/src/views/tournament/sceneResource/index.vue`

页面风格：

- 延续 `old-code-admin/src/views/tournament/sceneSchedule/index.vue` 的 Element Plus + RuoYi 风格；
- 使用 `app-container`、查询表单、工具栏、`right-toolbar`、`pagination`；
- 不引入独立现场系统，不使用 `site_*` 命名。

## 三、资源管理 API 封装

建议新增：

- `old-code-admin/src/api/tournament/sceneResource.js`

封装接口：

- `listSceneResource(query)`
- `getSceneResource(resourceId)`
- `addSceneResource(data)`
- `updateSceneResource(data)`
- `delSceneResource(resourceIds)`
- `changeSceneResourceStatus(data)`

接口路径：

- `GET /competition/sceneResource/list`
- `GET /competition/sceneResource/{resourceId}`
- `POST /competition/sceneResource`
- `PUT /competition/sceneResource`
- `DELETE /competition/sceneResource/{resourceIds}`
- `POST /competition/sceneResource/changeStatus`

## 四、列表字段

资源管理列表建议字段：

- 资源编号：`resourceCode`
- 资源名称：`resourceName`
- 资源类型：`resourceType`
- 资源状态：`resourceStatus`
- 品牌型号：`brandModel`
- 设备数量：`deviceQuantity`
- 单台设备工位数：`workstationCount`
- 默认单场周期：`defaultSlotDurationMinutes`
- 默认共享占用：`defaultSharedOccupancy`
- 是否需要运维确认：`needOpsConfirm`
- 运维联系人：`opsContactName`
- 运维联系电话：`opsContactPhone`
- 排序：`sortOrder`
- 更新时间：`updateTime`
- 操作

筛选条件：

- 资源编号；
- 资源名称；
- 资源类型；
- 资源状态。

## 五、表单字段

新增/编辑弹窗字段：

- 资源编号；
- 资源名称；
- 资源类型；
- 资源状态；
- 品牌型号；
- 设备数量；
- 单台设备工位数；
- 默认单场周期，单位分钟；
- 默认共享占用；
- 是否需要运维确认；
- 运维联系人；
- 运维联系电话；
- 安全须知；
- 注意事项；
- 主要参数列表；
- 使用说明；
- 设备图片；
- 管理员备注；
- 排序。

说明：

- `workstationCount` 明确展示为“单台设备工位数”；
- `defaultSlotDurationMinutes` 明确展示为“默认单场周期（分钟）”；
- `needOpsConfirm` 仅作为提示字段，不触发运维流程。

## 六、表单校验

前端校验建议与后端保持一致：

- `resourceCode` 必填；
- `resourceName` 必填；
- `resourceType` 必填且只能为 `ROOM/LAB/DEVICE/WORKSTATION/SERVER/SOFTWARE/OTHER`；
- `resourceStatus` 必填且只能为 `ENABLED/DISABLED/MAINTENANCE`；
- `deviceQuantity > 0`；
- `workstationCount > 0`；
- `defaultSlotDurationMinutes > 0`；
- `defaultSharedOccupancy` 必填；
- `needOpsConfirm` 必填；
- `opsContactPhone` 可选，若填写建议做手机号/联系电话宽松校验；
- `parameterJson` 第一版可用文本域录入 JSON，后续再升级成键值对编辑器；
- `imageUrls` 第一版可复用现有图片上传组件，保存为 JSON 数组字符串。

## 七、状态变更按钮

操作列建议：

- 修改；
- 删除；
- 启用；
- 停用；
- 维护中。

按钮规则：

- 当前状态为 `ENABLED`：展示“停用”“维护中”；
- 当前状态为 `DISABLED`：展示“启用”“维护中”；
- 当前状态为 `MAINTENANCE`：展示“启用”“停用”；
- 点击后调用 `changeSceneResourceStatus({ resourceId, resourceStatus })`。

## 八、删除确认

删除前弹出确认：

```text
确认删除资源“{resourceName}”吗？
```

后端已有保护：

- 如资源已存在未删除的 `competition_scene_schedule_resource` 布置记录，则拒绝删除；
- 前端展示后端错误信息“资源已布置到赛场安排，不能删除”。

## 九、权限码

资源管理页面使用：

- `competition:sceneResource:list`
- `competition:sceneResource:query`
- `competition:sceneResource:add`
- `competition:sceneResource:edit`
- `competition:sceneResource:remove`
- `competition:sceneResource:changeStatus`

## 十、菜单 SQL 是否需要

需要。

原因：

- 第一阶段接口联调发现当前菜单表中不存在 `competition:sceneResource:*` 权限码；
- 真实管理端通过 `v-hasPermi` 和后端 `@RequiresPermissions` 双重控制；
- 若不写菜单 SQL，页面按钮不可见或接口返回无权限。

建议第二阶段新增菜单 SQL：

- 新增资源管理菜单：`资源管理`
- 菜单路径：`sceneResource`
- 组件路径：`tournament/sceneResource/index`
- 平台：`admin`
- 建议挂载到现有赛事管理目录或现场安排配置附近；
- 新增 6 个按钮权限码。

具体父菜单 ID 需以测试库/正式库菜单结构为准，避免写死错误父级。

## 十一、schedule_resource 后端接口设计

第二阶段后端新增 `competition_scene_schedule_resource` 基础 CRUD。

建议新增后端类：

- `CompetitionSceneScheduleResource`
- `CompetitionSceneScheduleResourceQuery`
- `CompetitionSceneScheduleResourceVO`
- `CompetitionSceneScheduleResourceStatusReq`
- `CompetitionSceneScheduleResourceMapper`
- `CompetitionSceneScheduleResourceMapper.xml`
- `ICompetitionSceneScheduleResourceService`
- `CompetitionSceneScheduleResourceServiceImpl`
- `CompetitionSceneScheduleResourceController`

接口：

- `GET /competition/sceneScheduleResource/list?scheduleId=`
- `GET /competition/sceneScheduleResource/{scheduleResourceId}`
- `POST /competition/sceneScheduleResource`
- `PUT /competition/sceneScheduleResource`
- `DELETE /competition/sceneScheduleResource/{ids}`
- `POST /competition/sceneScheduleResource/changeBookingStatus`

本阶段只做基础布置数据维护，不做时段批量生成，不做预约提交。

### 关键校验

- `scheduleId` 必填，且对应 `competition_scene_schedule` 未删除；
- `resourceId` 必填，且对应 `competition_scene_resource` 未删除；
- 只能选择 `resource_status = ENABLED` 的资源进行新布置；
- `deployedDeviceCount > 0`；
- `workstationsPerDevice > 0`；
- `totalWorkstations = deployedDeviceCount * workstationsPerDevice` 由后端计算；
- `slotDurationMinutes > 0`；
- `sharedOccupancy` 必填；
- `needOpsConfirm` 必填；
- `bookingStatus` 只能为 `DRAFT/READY/OPEN/PAUSED/CLOSED`；
- 新增布置时默认从资源台账带出：单台工位数、默认周期、共享占用、运维联系人、安全须知、注意事项、使用说明；
- 修改资源台账不自动同步已布置资源。

## 十二、第二阶段测试清单

管理端页面：

- 资源列表可加载；
- 查询条件生效；
- 新增资源成功；
- 必填项校验生效；
- 枚举字段校验生效；
- 数值字段校验生效；
- 编辑资源成功；
- 状态切换成功；
- 删除确认正常；
- 已布置资源删除失败提示正常；
- 权限码控制按钮显示。

资源 API：

- `sceneResource` 六个接口通过网关验证；
- 新增菜单 SQL 后 admin token 可正常访问；
- 旧 `9205` 服务重启后不再出现 `No static resource sceneResource/list`。

`schedule_resource` 后端：

- 新增布置成功；
- 选择停用/维护中资源新增布置被拒绝；
- `totalWorkstations` 自动计算；
- 列表按 `scheduleId` 查询；
- 详情查询正常；
- 修改布置成功；
- 预约状态切换 `DRAFT/READY/OPEN/PAUSED/CLOSED`；
- 删除布置成功；
- 删除布置不影响资源台账；
- 禁用字段静态检查继续通过。

编译：

- `mvn -pl teaching-modules/teaching-competition -am compile -DskipTests`
- 管理端按项目现有命令完成 lint/build 或至少本地页面编译检查。

## 十三、进入第二阶段前建议处理

1. 重启测试环境 `teaching-competition`，确保第一阶段 Controller 加载。
2. 统一 auth、gateway、competition 的 common-core 依赖/源码版本，避免 JWT secret 验签不一致。
3. 确认资源管理菜单挂载父级菜单 ID。
4. 第二阶段先做资源管理页面，再做 `schedule_resource` 后端 CRUD。
