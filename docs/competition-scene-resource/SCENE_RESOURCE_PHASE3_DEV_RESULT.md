# 大赛现场设备资源管理与预约 - 第三阶段开发结果

开发日期：2026-07-01  
范围：管理端“赛场安排”页面新增“资源与预约”Tab，接入 `competition_scene_schedule_resource` 基础 CRUD。

## 一、前置复验结果

已生成前置复验文档：

- `docs/competition-scene-resource/SCENE_RESOURCE_PHASE3_PRECHECK.md`

复验结论：

- `GET /competition/sceneResource/list` 经网关访问正常；
- `GET /competition/sceneScheduleResource/list?scheduleId=3` 经网关仍返回 `No static resource sceneScheduleResource/list`；
- 直连正式 `9205` 仍返回 `No static resource competition/sceneScheduleResource/list`；
- 原因：正式 `9205` 尚未重启，未加载第二阶段新增的 `CompetitionSceneScheduleResourceController`。

## 二、是否重启 9205

本轮未重启正式测试用 `9205` competition 服务。

处理原则：

- 不擅自杀掉当前 IntelliJ 启动的正式测试进程；
- 按需求记录联调阻塞；
- 继续完成第三阶段前端代码开发。

## 三、网关 sceneScheduleResource 复验结果

当前状态：阻塞。

阻塞接口：

- `GET /competition/sceneScheduleResource/list?scheduleId=3`
- `POST /competition/sceneScheduleResource`
- `PUT /competition/sceneScheduleResource`
- `POST /competition/sceneScheduleResource/changeBookingStatus`
- `DELETE /competition/sceneScheduleResource/{ids}`

解除条件：

- 重启正式 `9205` competition 服务；
- 或让网关路由到已加载 `CompetitionSceneScheduleResourceController` 的 competition 实例；
- 并保持 auth / gateway / competition 的 common-core / JWT 密钥一致。

## 四、新增 / 修改前端文件

新增：

- `old-code-admin/src/api/tournament/sceneScheduleResource.js`
- `old-code-admin/src/views/tournament/sceneSchedule/components/ResourceReservationTab.vue`
- `old-code-admin/src/views/tournament/sceneSchedule/components/ScheduleResourceDialog.vue`

修改：

- `old-code-admin/src/views/tournament/sceneSchedule/index.vue`

## 五、API 封装

新增 API 方法：

- `listSceneScheduleResource(query)`
- `getSceneScheduleResource(scheduleResourceId)`
- `addSceneScheduleResource(data)`
- `updateSceneScheduleResource(data)`
- `delSceneScheduleResource(scheduleResourceIds)`
- `changeSceneScheduleResourceBookingStatus(data)`

路径沿用现有 tournament API 风格，统一带 `/competition` 前缀：

- `/competition/sceneScheduleResource/list`
- `/competition/sceneScheduleResource/{id}`
- `/competition/sceneScheduleResource`
- `/competition/sceneScheduleResource/changeBookingStatus`

## 六、赛场安排页面改造说明

保留原有 Tab：

- 赛场安排；
- 匹配对象；
- 现场证件；
- 操作流水。

新增 Tab：

- 资源与预约。

主页面只新增：

- `ResourceReservationTab` 组件引入；
- `resourceTabRef`；
- 一个 `el-tab-pane`；
- `refreshActiveTab` 和 `handleTabChange` 中的 `resource` 分支。

原有赛场安排、匹配对象、证件、流水功能未改动业务逻辑。

## 七、新增组件清单

### ResourceReservationTab.vue

能力：

- 根据当前 `scheduleId` 查询赛场资源布置列表；
- 未选择赛场安排时显示“请先选择一个赛场安排。”；
- 新增布置；
- 编辑布置；
- 删除布置；
- 发布预约；
- 暂停预约；
- 关闭预约；
- 预留禁用的“配置时段”按钮，提示下一阶段开发。

表格字段：

- 资源名称；
- 资源类型；
- 部署位置；
- 部署设备数；
- 每台工位数；
- 总工位数；
- 单场周期；
- 共享占用；
- 是否需要运维确认；
- 运维联系人；
- 运维联系电话；
- 预约状态；
- 开放时间；
- 关闭时间；
- 更新时间。

### ScheduleResourceDialog.vue

能力：

- 新增资源布置；
- 编辑资源布置；
- 新增时加载 `ENABLED` 状态资源；
- 选择资源后自动带出台账默认值；
- 前端预览 `部署设备数 × 每台设备工位数`；
- 提交时不把 `totalWorkstations` 当作可信字段提交，由后端重新计算；
- 校验时间范围。

## 八、已实现功能

- 在赛场安排页面中新增“资源与预约”Tab；
- 接入 `competition_scene_schedule_resource` 列表接口；
- 接入资源布置新增接口；
- 接入资源布置编辑接口；
- 接入资源布置删除接口；
- 接入预约发布状态切换接口；
- 使用资源台账接口加载启用资源；
- 选择资源自动带出：
  - `workstationCount → workstationsPerDevice`
  - `defaultSlotDurationMinutes → slotDurationMinutes`
  - `defaultSharedOccupancy → sharedOccupancy`
  - `needOpsConfirm`
  - `opsContactName`
  - `opsContactPhone`
  - `safetyNotice → safetyNoticeOverride`
  - `attentionNotes → attentionNotesOverride`
  - `usageInstructions → usageInstructionsOverride`
- 权限按钮使用：
  - `competition:sceneScheduleResource:list`
  - `competition:sceneScheduleResource:add`
  - `competition:sceneScheduleResource:edit`
  - `competition:sceneScheduleResource:remove`
  - `competition:sceneScheduleResource:changeBookingStatus`

## 九、未实现功能

按本阶段要求未开发：

- 小程序预约页面；
- PC 用户预约页面；
- 完整预约提交逻辑；
- 预约时段批量生成；
- 预约容量防超卖；
- 非共享占用预约冲突；
- 有效证件预约校验；
- 用户端可预约资源接口；
- 独立运维端；
- `opsConfirm` 接口。

## 十、测试结果

已完成：

- 资源台账接口经网关复验通过；
- `sceneScheduleResource` 经网关复验确认阻塞原因；
- 前端禁用字段静态检查通过；
- 管理端构建通过。

禁用字段静态检查未命中：

- `team_id`
- `asset_no`
- `owner_unit`
- `storage_location`
- `cancel_deadline_minutes`
- `cancelDeadlineMinutes`
- `ops_status`
- `opsStatus`
- `opsConfirm`
- `site_`

未完成真实浏览器联调：

- 因正式 `9205` 未加载新 Controller，资源与预约 Tab 的真实新增、编辑、删除、状态切换需待 `9205` 重启后验证。

## 十一、构建结果

管理端构建命令：

```bash
npm run build:stage
```

结果：成功。

说明：构建输出中仍存在项目原有 `eval` / sourcemap 警告，和本次新增页面无关。

后端本轮未改动，因此未重新执行 Maven 编译。

## 十二、已知风险

- 正式 `9205` 未重启前，网关无法访问 `sceneScheduleResource` 新接口；
- auth / gateway / competition 的 common-core / JWT 密钥实现差异风险仍需保留；
- 菜单权限已导入资源管理权限，但 `sceneScheduleResource` 按钮权限是否已写入角色授权，需要测试环境菜单/角色数据确认；
- 资源与预约 Tab 已接入前端闭环，但完整交互需依赖后端新 Controller 进入正式 `9205` 运行态。

## 十三、第四阶段建议

建议第四阶段进入预约时段基础维护：

1. 配置时段弹窗；
2. 单个新增时段；
3. 批量生成时段；
4. 设备容量和工位容量计算；
5. 时段状态开放 / 关闭；
6. 后端条件更新防止超卖；
7. 为用户端预约列表和预约提交做准备。
