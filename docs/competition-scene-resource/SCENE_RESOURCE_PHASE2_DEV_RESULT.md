# 大赛现场设备资源管理与预约 - 第二阶段开发结果

开发日期：2026-07-01  
范围：管理端资源管理页面、资源管理菜单 SQL、`competition_scene_schedule_resource` 后端基础 CRUD

## 一、前置检查结果

- 已完成前置检查文档：`docs/competition-scene-resource/SCENE_RESOURCE_PHASE2_PRECHECK.md`
- 网关访问资源台账接口正常：
  - `GET /competition/sceneResource/list`
  - 返回 `code=200`
- 当前 auth / gateway / competition 运行链路使用工作区 `target/classes` 时，JWT 签发与验签可互通。
- 风险仍需保留：本机 `.m2` 中已安装的 `teaching-common-core-3.6.6.jar` 与工作区编译产物存在 `JwtUtils` 差异。后续若改用 Maven 直接启动服务，需重新确认 common-core 与 JWT 密钥一致。

## 二、是否重启 9205

本轮未重启正式 `9205` competition 服务，避免影响当前测试环境。

说明：

- 正式 `9205` 进程尚未加载本轮新增的 `CompetitionSceneScheduleResourceController`。
- 当前直连 `9205` 访问 `GET /competition/sceneScheduleResource/list` 仍返回：

```json
{"msg":"No static resource competition/sceneScheduleResource/list.","code":500}
```

- 为验证新代码，本轮启动过临时 `19205` competition 进程，使用当前 `target/classes`，并设置 `spring.cloud.nacos.discovery.enabled=false`，不注册到 Nacos，不影响网关正式路由。
- 临时 `19205` 验证完成后已停止。

结论：第二阶段新接口正式联调前，必须重启正式测试用 `9205` competition 服务。

## 三、网关验证结果

已通过网关验证资源台账接口：

```http
GET http://127.0.0.1:9889/competition/sceneResource/list
```

返回：

```json
{"total":0,"rows":[],"code":200,"msg":"查询成功","totalSum":null}
```

`sceneScheduleResource` 新接口需在 `9205` 重启加载新 Controller 后再通过网关复验。

## 四、菜单 SQL

新增菜单 SQL：

- `db/migration/20260701_competition_scene_resource_phase2_menu.sql`

说明文档：

- `docs/competition-scene-resource/SCENE_RESOURCE_PHASE2_MENU_SQL.md`

SQL 特点：

- 不写死父菜单 ID；
- 通过 `赛事管理 / tournament / platform_type=admin` 动态定位父菜单；
- 新增菜单：`资源管理`
- 路径：`sceneResource`
- 组件：`tournament/sceneResource/index`
- 权限码与前端、后端保持一致。

本轮未直接导入菜单 SQL，需要人工在测试库执行。

## 五、管理端新增文件

- `old-code-admin/src/api/tournament/sceneResource.js`
- `old-code-admin/src/views/tournament/sceneResource/index.vue`

已实现：

- 列表查询；
- 条件筛选；
- 新增；
- 编辑；
- 删除；
- 批量删除；
- 状态切换；
- `resetQuery`；
- `loading`；
- `selection`；
- `right-toolbar`；
- `pagination`；
- `v-hasPermi` 权限控制；
- JSON 文本字段基础校验。

## 六、后端新增文件

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneScheduleResourceQuery.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneScheduleResourceStatusReq.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneScheduleResourceVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneScheduleResourceMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneScheduleResourceMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneScheduleResourceService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneScheduleResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleResourceController.java`

## 七、新增接口清单

`competition_scene_schedule_resource` 基础维护接口：

- `GET /competition/sceneScheduleResource/list?scheduleId=`
- `GET /competition/sceneScheduleResource/{scheduleResourceId}`
- `POST /competition/sceneScheduleResource`
- `PUT /competition/sceneScheduleResource`
- `DELETE /competition/sceneScheduleResource/{scheduleResourceIds}`
- `POST /competition/sceneScheduleResource/changeBookingStatus`

兼容本服务直连路径：

- `/sceneScheduleResource/**`
- `/competition/sceneScheduleResource/**`

## 八、权限码清单

资源台账：

- `competition:sceneResource:list`
- `competition:sceneResource:query`
- `competition:sceneResource:add`
- `competition:sceneResource:edit`
- `competition:sceneResource:remove`
- `competition:sceneResource:changeStatus`

赛场资源布置：

- `competition:sceneScheduleResource:list`
- `competition:sceneScheduleResource:query`
- `competition:sceneScheduleResource:add`
- `competition:sceneScheduleResource:edit`
- `competition:sceneScheduleResource:remove`
- `competition:sceneScheduleResource:changeBookingStatus`

## 九、业务规则落实情况

已落实：

- 新增布置时校验 `scheduleId` 对应赛场安排存在且未删除；
- 新增布置时校验 `resourceId` 对应资源存在且未删除；
- 新增布置只允许选择 `resource_status = ENABLED` 的资源；
- `deployedDeviceCount > 0`；
- `workstationsPerDevice > 0`；
- `slotDurationMinutes > 0`；
- `sharedOccupancy` 必填；
- `needOpsConfirm` 必填；
- `bookingStatus` 限定为 `DRAFT / READY / OPEN / PAUSED / CLOSED`；
- `totalWorkstations` 由后端按 `deployedDeviceCount × workstationsPerDevice` 计算；
- 新增布置时从资源台账带出默认配置；
- 修改资源台账不自动同步已布置资源；
- 删除布置不影响资源台账；
- 未引入 `opsStatus`；
- 未引入 `opsConfirm` 接口；
- 未引入 `team_id`；
- 未引入资产管理字段；
- 未引入 `cancelDeadlineMinutes`。

## 十、测试结果

后端编译：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：`BUILD SUCCESS`

管理端构建：

```bash
npm run build:stage
```

结果：构建成功。输出中存在项目原有 `eval` / sourcemap 警告，与本次新增页面无关。

禁用字段静态检查：

- `team_id`：未命中；
- `asset_no`：未命中；
- `owner_unit`：未命中；
- `storage_location`：未命中；
- `cancel_deadline_minutes` / `cancelDeadlineMinutes`：未命中；
- `ops_status` / `opsStatus`：未命中；
- `opsConfirm`：未命中；
- `site_`：未命中。

临时 `19205` 接口验证：

- 新增启用资源成功；
- 新增停用资源成功；
- 停用资源新增布置被拒绝；
- 启用资源新增布置成功；
- 新增布置自动带出资源台账默认值；
- `totalWorkstations` 自动计算成功；
- 存在布置时删除资源被拒绝；
- 布置详情查询成功；
- 布置修改成功；
- `bookingStatus` 切换 `DRAFT / READY / OPEN / PAUSED / CLOSED` 成功；
- 删除布置成功；
- 删除布置后资源台账仍存在；
- 测试资源已逻辑删除清理。

接口验证结论：`PHASE2_API_TEMP_VERIFY_PASS`

补充说明：临时 `19205` 为避免影响网关路由关闭了 Nacos discovery，因此操作日志异步调用 `teaching-system` 出现 fallback 日志；业务接口返回正常，不影响本次资源布置 CRUD 验证。

## 十一、未完成项

本阶段按要求未开发：

- 小程序预约页面；
- 完整预约提交逻辑；
- 预约时段批量生成；
- 赛场安排“资源与预约”Tab 完整前端；
- 独立运维端；
- `opsConfirm` 接口。

待后续执行：

- 导入资源管理菜单 SQL；
- 重启正式测试用 `9205` competition 服务；
- 通过网关复验 `sceneScheduleResource` 新接口；
- 在 admin 端实际菜单中点击验证资源管理页面。

## 十二、第三阶段建议

建议第三阶段优先做：

1. 重启 `9205` 后完成网关级 `sceneScheduleResource` 联调；
2. 在赛场安排页面增加“资源与预约”Tab 的轻量入口；
3. 接入 `sceneScheduleResource` 前端 API 封装；
4. 开发资源布置弹窗；
5. 再进入预约时段基础维护；
6. 最后再做用户端可预约资源展示和预约流程。
