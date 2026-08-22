# 大赛现场设备资源管理与预约第五阶段前置检查

检查时间：2026-07-01

## 1. 代码分支与工作区

- 当前分支：`master`
- 当前提交：`33b95b47e937777756b850e2ddf2e8cb473a0b70`
- 工作区状态：非干净
  - 已修改：
    - `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
    - `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotServiceImpl.java`
    - `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`
  - 未跟踪：
    - `docs/competition-scene-resource/SCENE_RESOURCE_PHASE4_INTEGRATION_RESULT.md`

说明：上述改动属于第四阶段预约时段重叠校验修复和联调报告遗留，不在本阶段回退。

## 2. 第四阶段代码提交状态

- 第四阶段代码尚未形成干净提交。
- 正式 9205 competition 进程已加载 `CompetitionSceneResourceSlotController`，但重叠校验修复未在正式运行态生效。

## 3. 网关接口可访问性

使用 `admin / qwe123!@#` 登录网关获取 token 后验证：

- `GET /competition/sceneResource/list?pageNum=1&pageSize=1`：成功，返回资源台账数据。
- `GET /competition/sceneScheduleResource/list?scheduleId=3&pageNum=1&pageSize=5`：成功，返回 `scheduleResourceId=3` 的资源布置。
- `GET /competition/sceneResourceSlot/list?scheduleResourceId=3&pageNum=1&pageSize=5`：成功，不再出现 `No static resource`。

结论：正式网关已经能路由到预约时段 Controller。

## 4. 重叠时段校验运行态复验

测试动作：

1. 新增 `2027-07-01 09:00:00 ~ 09:30:00` 时段；
2. 继续新增重叠的 `2027-07-01 09:15:00 ~ 09:45:00` 时段；
3. 清理测试产生的 `slotId=15,16`。

实际结果：

- 两个重叠时段均返回 `{"msg":"操作成功","code":200}`。
- 说明正式 9205 当前未加载第四阶段重叠校验修复。

风险：

- 第五阶段可以继续开发代码，但正式环境联调预约容量和时段选择前，需要重启 9205 或确认加载最新 class。

## 5. 资源与预约基础数据

当前测试数据：

- 资源台账：
  - `resourceId=5`
  - `resourceCode=test1`
  - `resourceName=电脑`
  - `resourceStatus=ENABLED`
  - `workstationCount=1`
  - `defaultSlotDurationMinutes=30`
- 赛场资源布置：
  - `scheduleResourceId=3`
  - `scheduleId=3`
  - `resourceId=5`
  - `bookingStatus=OPEN`
  - `deployedDeviceCount=1`
  - `workstationsPerDevice=1`
  - `sharedOccupancy=true`
  - `bookingOpenTime=2026-07-01 00:00:00`
  - `bookingCloseTime=2026-07-20 00:00:00`
- 开放预约时段：
  - 清理重叠测试数据后，`scheduleResourceId=3` 当前无可用时段。

结论：第五阶段联调前需要重新配置一个未来的 `OPEN` 时段。

## 6. 当前测试账号与参赛主体

使用 admin token 调用用户端证件接口：

- `GET /competition/userCompetition/sceneCredential/myList` 返回空数组。

`scheduleId=3` 当前存在 6 条匹配对象和 6 张证件，但需要注意：

- 匹配对象为人工维护数据，`configDimension=TEAM`，但 `teamCode` 为空，`targetKey` 形如 `USER:293`。
- 证件 `credentialStatus=EFFECTIVE`，但 `validTo=2026-06-30 00:00:00`。
- 当前日期为 `2026-07-01`，按第五阶段规则“证件未过期”判断，这批证件已过期。

结论：

- admin 账号不具备用户端预约成功条件。
- 现有 `scheduleId=3` 测试数据不满足团队预约以 `team_code` 锁定主体的规则。
- 第五阶段接口可实现并编译验证；成功预约联调用例需要补充真实用户账号、有效证件、有效 `schedule_target` 和未来开放时段。

## 7. 第五阶段实现注意事项

- 预约主体必须从 `competition_scene_schedule_target` 解析。
- 团队主体第一阶段以 `team_code` 为准，不假设数字型 `teamId`。
- 若团队维度 target 缺少 `team_code`，后端应返回 `SUBJECT_NOT_RESOLVED` 或 `NOT_SCHEDULE_TARGET`，不能回退为队长或 userId 锁定。
- 团队任意有效成员可代表团队预约，需要结合报名成员关系校验，不能只看 target 表保存的代表成员 `user_id`。
- 有效证件第一阶段判断为 `credential_status = EFFECTIVE` 且未过期。
- 本阶段不做自动过期任务，预约是否过期在查询和重复预约校验时按关联时段 `end_time` 动态判断。

## 8. 前置结论

- 可以进入第五阶段编码。
- 正式联调前必须处理两类数据/环境问题：
  1. 重启 9205 或确保正式 competition 实例加载第四阶段重叠校验修复；
  2. 准备满足第五阶段规则的测试主体数据：有效用户、有效现场证件、包含 `team_code` 或个人主体的 `schedule_target`、未来 `OPEN` 时段。
