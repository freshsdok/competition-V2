# 大赛现场设备资源管理与预约第五阶段开发结果

完成时间：2026-07-01

## 1. 本阶段目标

已完成选手端资源预约主流程编码：

- 可预约资源查询；
- 预约主体识别；
- 有效现场证件校验；
- 赛场匹配对象校验；
- 后端自动计算预约设备数；
- 容量校验与条件扣减；
- 非共享占用冲突校验；
- 提交预约；
- 我的预约；
- 取消预约；
- PC 用户端页面；
- 小程序用户端页面。

本阶段未开发独立运维端、`opsConfirm`、自动过期任务、资产管理字段、`team_id`、`cancelDeadlineMinutes`。

## 2. 前置检查结论

前置检查已输出：

- `docs/competition-scene-resource/SCENE_RESOURCE_PHASE5_PRECHECK.md`

关键结论：

- 当前分支：`master`
- 当前提交：`33b95b47e937777756b850e2ddf2e8cb473a0b70`
- 正式网关可访问第四阶段 `sceneResourceSlot` Controller。
- 正式 9205 当前未加载第四阶段重叠校验修复，重叠时段仍能新增成功。
- 当前测试数据 `scheduleId=3` 的证件 `validTo=2026-06-30 00:00:00`，按当前日期 `2026-07-01` 已过期。
- 当前 `scheduleId=3` 的人工 TEAM target 缺少 `teamCode`，不满足第五阶段团队预约主体锁定规则。

## 3. 后端新增文件

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionSceneResourceController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/IUserCompetitionSceneResourceService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceReservationMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceReservationMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/exception/CompetitionSceneReservationException.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneReservationSubject.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceBookableQuery.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceBookableVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceReservationReq.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceReservationCancelReq.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceReservationQuery.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneResourceReservationVO.java`

## 4. 后端修改文件

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneScheduleResourceQuery.java`
  - 增加 `scheduleResourceId` 查询条件。
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneScheduleResourceMapper.xml`
  - 增加 `schedule_resource_id` 查询过滤。
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
  - 增加预约容量扣减和取消释放容量方法。
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`
  - 增加 `reserveCompetitionSceneResourceSlotCapacity` 条件扣减。
  - 增加 `releaseCompetitionSceneResourceSlotCapacity` 取消释放。

## 5. 新增用户端接口

Controller 路由兼容：

- `/userCompetition/...`
- `/competition/userCompetition/...`

接口清单：

- `GET /competition/userCompetition/sceneResource/bookableList`
- `GET /competition/userCompetition/sceneResource/{scheduleResourceId}`
- `GET /competition/userCompetition/sceneResourceSlot/list?scheduleResourceId=`
- `POST /competition/userCompetition/sceneResourceReservation`
- `GET /competition/userCompetition/sceneResourceReservation/myList`
- `POST /competition/userCompetition/sceneResourceReservation/cancel`

业务异常返回：

- 使用 `code=5008`；
- 返回 `errorCode`；
- 重复预约返回 `existingReservation`。

## 6. 核心规则实现

- 预约单位为设备，工位只用于展示和计算。
- 预约主体来自 `competition_scene_schedule_target`。
- 团队预约主体使用 `subject_type=TEAM`、`subject_code=teamCode`。
- 不使用数字型 `teamId`。
- TEAM target 缺少 `teamCode` 时返回 `SUBJECT_NOT_RESOLVED`。
- 团队成员资格通过报名成员关系校验，不校验队长身份。
- 个人主体使用 `subject_type=USER`、`subject_code=userId`。
- 同一 `schedule_id + subject_type + subject_code` 下，未过期且状态为 `RESERVED/CHECKED` 的预约会阻止重复预约。
- 过期判断按关联时段 `end_time` 动态计算，不自动修改数据库状态。
- 有效证件校验为 `credential_status=EFFECTIVE` 且未过期。
- 新预约必须属于当前赛场安排匹配对象。
- 团队设备数为 `ceil(参赛选手人数 / 每台设备工位数)`。
- 个人设备数固定为 1。
- 用户端不提交设备数。
- 非共享占用时，同一时段存在未过期 `RESERVED/CHECKED` 预约即拒绝。
- 提交预约使用 SQL 条件扣减容量，防止并发超卖。
- `RESERVED` 可取消，取消后释放容量。
- `CHECKED/CANCELLED/EXPIRED` 不可取消。

## 7. PC 用户端新增/修改

新增：

- `old-code-pc/src/api/personal/sceneResource.js`
- `old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`

修改：

- `old-code-pc/src/views/personal/index.vue`

实现：

- 个人中心新增“设备预约”标签；
- 可预约资源列表；
- 资源详情；
- 开放时段列表；
- 预约确认；
- 已有预约提示；
- 我的预约；
- 取消预约。

## 8. 小程序新增/修改

新增：

- `old-code-mini/api/sceneResource.js`
- `old-code-mini/pages/scene-resource/index.vue`

修改：

- `old-code-mini/pages.json`
- `old-code-mini/pages/mine/index.vue`
- `old-code-mini/utils/request.js`

实现：

- “我的”页新增“设备预约”入口；
- 设备预约页展示可预约资源；
- 资源详情与开放时段；
- 预约确认；
- 已有预约提示；
- 我的预约；
- 取消预约；
- `5008` 业务异常透传完整响应，便于展示已有预约。

## 9. 验证结果

后端编译：

- 命令：`mvn -pl teaching-modules/teaching-competition -am compile -DskipTests`
- 结果：`BUILD SUCCESS`

PC 构建：

- 命令：`npm run build`
- 目录：`old-code-pc`
- 结果：构建成功。
- 说明：输出存在仓库既有 Sass legacy API、Browserslist、eval、大包体积告警；非本阶段新增阻断。

小程序静态检查：

- `pages.json` JSON 解析通过。
- 小程序工程无独立 `package.json`，未执行 npm 构建。

禁用字段检查：

- 本阶段新增/修改资源预约文件未出现：
  - `team_id`
  - `asset_no`
  - `owner_unit`
  - `storage_location`
  - `cancel_deadline`
  - `cancelDeadline`
  - `ops_status`
  - `opsConfirm`

## 10. 未完成联调项

由于正式 9205 尚未加载第五阶段新增 Controller，本轮未完成正式网关联调：

- `GET /competition/userCompetition/sceneResource/bookableList`
- `GET /competition/userCompetition/sceneResource/{scheduleResourceId}`
- `GET /competition/userCompetition/sceneResourceSlot/list`
- `POST /competition/userCompetition/sceneResourceReservation`
- `GET /competition/userCompetition/sceneResourceReservation/myList`
- `POST /competition/userCompetition/sceneResourceReservation/cancel`

正式联调前需要：

1. 重启 9205 或确保网关路由到已加载第五阶段代码的 competition 实例；
2. 确认第四阶段重叠时段校验已在正式运行态生效；
3. 准备有效测试主体：
   - 有效用户账号；
   - `competition_scene_schedule_target` 匹配对象；
   - 团队主体必须有 `teamCode`；
   - `competition_scene_credential` 有效且未过期；
   - 已发布的 `competition_scene_schedule_resource`；
   - 未来的 `OPEN` 时段。

## 11. 已知风险

- 现有 `scheduleId=3` 测试数据是 `configDimension=TEAM` 但 `teamCode` 为空，按第五阶段定稿规则会被拒绝预约。
- 当前证件样例已过期，不能用于成功预约联调。
- 正式 9205 未加载第四阶段重叠校验修复时，时段数据可能继续产生重叠，需要先重启和复验。
- 当前没有自动过期任务，过期展示和重复预约校验均依赖查询时动态判断。

## 12. 下一步建议

1. 重启正式测试用 competition 9205；
2. 先复验第四阶段重叠校验；
3. 准备一组个人主体和一组团队主体的有效测试数据；
4. 逐项联调第五阶段用户端接口；
5. 再进行 PC 和小程序页面真机/浏览器验证。
