# 大赛现场设备资源管理与预约 - 第四阶段开发结果

开发日期：2026-07-01  
范围：管理端“赛场安排 - 资源与预约”Tab 补齐预约时段配置能力，完成 `competition_scene_resource_slot` 后端基础 CRUD、单个新增、批量生成、编辑、删除、开放/关闭。

## 一、前置检查结果

已生成前置检查文档：

- `docs/competition-scene-resource/SCENE_RESOURCE_PHASE4_PRECHECK.md`

检查结论：

- `competition_scene_resource_slot` 表字段完整；
- 已包含：
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
- 未发现本阶段禁止新增字段；
- 第三阶段 `sceneScheduleResource` 能力已在临时实例验证通过；
- 正式 `9205` 尚未加载第四阶段新增 Controller。

## 二、是否重启 9205

本轮未重启正式测试用 `9205` competition 服务。

当前状态：

- 正式 `9205` 仍是旧进程；
- 网关仍路由到旧 `9205`；
- 第四阶段新增 `CompetitionSceneResourceSlotController` 需重启正式 `9205` 后才能通过网关访问。

本轮为了验证功能，临时启动过 `19205` competition 实例进行接口联调。验证完成后已停止，`19205` 端口已释放。

## 三、网关 sceneResourceSlot 复验结果

正式网关复验结果：阻塞。

阻塞接口：

- `GET /competition/sceneResourceSlot/list?scheduleResourceId=3&pageNum=1&pageSize=5`

当前返回：

```json
{"msg":"No static resource sceneResourceSlot/list.","code":500}
```

直连正式 `9205` 返回：

```json
{"msg":"No static resource competition/sceneResourceSlot/list.","code":500}
```

原因判断：

- 正式 `9205` 未重启，未加载本阶段新增 `CompetitionSceneResourceSlotController`；
- 不是本阶段接口代码编译失败；
- 不是数据库表缺失。

解除条件：

- 重启正式测试用 `9205` competition 服务；
- 或让网关路由到已加载第四阶段代码的 competition 实例；
- 同时继续保持 auth / gateway / competition 的 common-core / JWT 密钥实现一致。

## 四、临时实例接口验证结果

使用临时 `19205` competition 实例完成第四阶段接口联调。

验证对象：

- `scheduleResourceId = 3`
- 部署设备数：`1`
- 每台设备工位数：`1`

验证通过项：

- 时段列表查询成功；
- 单个新增时段成功；
- `deviceCapacity > deployedDeviceCount` 被拒绝；
- `workstationCapacity = deviceCapacity × workstationsPerDevice` 自动计算；
- `remainingDeviceCount`、`remainingWorkstationCount` 自动计算；
- 时段详情查询成功；
- 时段编辑成功；
- 时段状态切换 `OPEN` 成功；
- 时段状态切换 `CLOSED` 成功；
- 批量生成时段成功；
- 批量生成数量正确；
- 删除未预约时段成功。

联调结论：

- 临时实例接口验证通过；
- 正式网关仍需待 `9205` 重启后复验。

## 五、新增后端文件

新增：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotBatchReq.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotQuery.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotStatusReq.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlotVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneResourceSlotService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`

复用第一阶段已新增 Domain：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceSlot.java`

## 六、新增 / 修改前端文件

新增：

- `old-code-admin/src/api/tournament/sceneResourceSlot.js`
- `old-code-admin/src/views/tournament/sceneSchedule/components/ResourceSlotDialog.vue`

修改：

- `old-code-admin/src/views/tournament/sceneSchedule/components/ResourceReservationTab.vue`

## 七、新增接口清单

预约时段管理：

- `GET /competition/sceneResourceSlot/list`
- `GET /competition/sceneResourceSlot/{slotId}`
- `POST /competition/sceneResourceSlot`
- `POST /competition/sceneResourceSlot/batch`
- `PUT /competition/sceneResourceSlot`
- `DELETE /competition/sceneResourceSlot/{slotIds}`
- `POST /competition/sceneResourceSlot/changeStatus`

兼容直连路径：

- `/sceneResourceSlot/**`

## 八、权限码清单

本阶段新增权限码建议：

- `competition:sceneResourceSlot:list`
- `competition:sceneResourceSlot:query`
- `competition:sceneResourceSlot:add`
- `competition:sceneResourceSlot:edit`
- `competition:sceneResourceSlot:remove`
- `competition:sceneResourceSlot:changeStatus`

说明：

- 前端按钮已使用上述权限码；
- 如非超级管理员角色需要使用该功能，需要在菜单 / 按钮权限中补充授权。

## 九、后端实现说明

已实现：

- 按 `scheduleResourceId` 查询时段列表；
- 查询时段详情；
- 单个新增时段；
- 批量生成时段；
- 编辑时段；
- 删除时段；
- 修改时段状态；
- 根据赛场资源布置自动带出：
  - `schedule_id`
  - `resource_id`
  - `event_id`
  - `workstations_per_device`
- 自动计算：
  - `workstation_capacity = device_capacity × workstations_per_device`
  - `remaining_device_count = device_capacity - reserved_device_count`
  - `remaining_workstation_count = workstation_capacity - reserved_workstation_count`
- 新增 / 批量生成时 `reserved_device_count`、`reserved_workstation_count` 默认为 `0`；
- 编辑时保留已预约数量；
- 删除时拒绝删除已有预约数量的时段；
- 开放时段时校验剩余设备数；
- 批量生成只生成完整时段，不生成尾部不足一个周期的残缺时段。

关键校验：

- `scheduleResourceId` 必填，且对应未删除的赛场资源布置；
- `startTime`、`endTime` 必填；
- `endTime` 必须晚于 `startTime`；
- `deviceCapacity > 0`；
- `deviceCapacity <= deployedDeviceCount`；
- `slotDurationMinutes > 0`；
- `slotStatus` 只能为 `PENDING / OPEN / FULL / CLOSED / EXPIRED`；
- 调整容量时不能小于已预约设备数和已预约工位数。

## 十、前端实现说明

`ResourceReservationTab.vue`：

- “配置时段”按钮已从禁用预留改为可用；
- 点击后打开 `ResourceSlotDialog`；
- 保留第三阶段资源布置列表、新增、编辑、删除、发布、暂停、关闭功能。

`ResourceSlotDialog.vue`：

- 展示当前布置资源概要；
- 展示时段列表；
- 支持按时段状态筛选；
- 支持分页；
- 支持单个新增时段；
- 支持批量生成时段；
- 支持编辑时段；
- 支持删除时段；
- 支持开放 / 关闭时段；
- 展示设备容量、工位容量、已预约设备数、剩余设备数、已预约工位数、剩余工位数；
- 新增 / 编辑时前端预览工位容量，但最终以后端计算为准；
- 批量生成时前端预览生成场次数和每场工位容量。

## 十一、未实现功能

按本阶段要求未开发：

- 小程序预约页面；
- PC 用户预约页面；
- 完整预约提交逻辑；
- 用户端可预约资源接口；
- 预约取消；
- 预约核销；
- 独立运维端；
- `opsConfirm` 接口；
- 预约容量防超卖的最终提交条件更新；
- 非共享占用预约冲突；
- 有效现场证件预约校验；
- 预约主体识别。

## 十二、测试结果

后端编译：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：`BUILD SUCCESS`。

管理端构建：

```bash
npm run build:stage
```

结果：成功。

禁用字段静态检查：

- 对本阶段新增 / 修改代码文件执行扫描；
- 未命中：
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

接口联调：

- 临时 `19205` 验证通过；
- 正式网关待重启 `9205` 后复验。

## 十三、构建结果

后端：

- competition 模块编译通过；
- 未修改报名、支付、成绩、证书主流程。

前端：

- 管理端 `build:stage` 通过；
- 构建日志仅保留项目既有 sourcemap / eval 警告；
- 未发现本阶段新增页面编译错误。

## 十四、已知风险

1. 正式 `9205` 未重启

   - 当前网关访问 `sceneResourceSlot` 会继续返回 `No static resource`；
   - 需重启正式测试用 competition 服务后复验。

2. 权限授权需确认

   - 前端已接入 `competition:sceneResourceSlot:*` 权限；
   - 如普通管理员看不到按钮，需要补充菜单按钮权限。

3. JWT / common-core 风险仍需保留

   - auth / gateway / competition 的 common-core 与 JwtUtils 实现需保持一致；
   - 不建议通过绕过鉴权方式联调。

4. 临时实例日志服务告警

   - 临时 `19205` 为避免注册到 Nacos 关闭了 discovery；
   - 因此操作日志调用 `teaching-system` 出现 fallback 告警；
   - 该问题不影响本阶段时段接口业务验证。

## 十五、第五阶段建议

建议第五阶段进入用户预约后端准备，不先做小程序页面：

- 用户端可预约资源列表接口；
- 从 `competition_scene_schedule_target` 解析统一预约主体；
- 团队任意有效参赛成员可代表本队预约；
- 个人参赛者只能预约自己的参赛主体；
- 有效现场证件校验；
- 同一 `schedule_id + subject_type + subject_code` 有效预约查重；
- 设备数自动计算；
- 剩余设备数校验；
- 预约提交时段容量条件更新，防止超卖；
- 保留过期展示逻辑，不做自动过期任务。

## 十六、阶段结论

第四阶段编码已完成。

结论：

- 后端时段 CRUD / 批量生成已完成；
- 管理端“资源与预约”Tab 已接入“配置时段”能力；
- 临时实例接口联调通过；
- 后端编译通过；
- 管理端构建通过；
- 禁用字段检查通过；
- 正式网关联调需在重启 `9205` 后补做最终复验。
