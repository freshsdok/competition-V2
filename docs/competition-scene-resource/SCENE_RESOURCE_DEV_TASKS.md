# 大赛现场设备资源管理与预约开发任务拆分

更新时间：2026-06-30

## 1. 开发原则

本阶段只在现有 competition 体系内扩展，不新建独立现场系统，不使用旧 `site_*` 表，不修改报名、支付、成绩、证书主流程。

关键规则：

- 预约单位是设备，工位数只用于容量展示和设备数计算。
- 预约主体统一来自 `competition_scene_schedule_target`。
- 不再区分团队预约和个人预约两套流程。
- 团队参赛时，队伍中任意有效参赛成员均可代表本队预约。
- 个人参赛时，个人本人可预约。
- 同一 `schedule_id + subject_type + subject_code` 只能存在一条未过期有效预约。
- 必须有有效现场证件才能预约。
- 非共享占用下，同一 slot 只允许一个未过期有效预约。
- 现有系统不存在稳定数字型 teamId，第一阶段使用 `team_code/teamCode`。

## 2. 阶段一：数据库与基础模型

### 2.1 迁移 SQL

新增：

- `competition_scene_resource`
- `competition_scene_schedule_resource`
- `competition_scene_resource_slot`
- `competition_scene_resource_reservation`

注意：

- 不加入 `asset_no`、`owner_unit`、`storage_location`、`cancel_deadline_minutes`。
- 不设计 `team_id`。
- 预约表使用 `subject_type`、`subject_code`、`team_code`、`user_id`、`operator_user_id`。
- 预约状态第一阶段使用 `RESERVED/CANCELLED/CHECKED`。
- “已过期”根据 slot `end_time` 派生，不自动写库。

### 2.2 常量

新增或扩展：

- `CompetitionSceneResourceConstants`

常量：

- 资源类型：`ROOM/LAB/DEVICE/WORKSTATION/SERVER/SOFTWARE/OTHER`
- 资源状态：`ENABLED/DISABLED/MAINTENANCE`
- 预约发布状态：`DRAFT/READY/OPEN/PAUSED/CLOSED`
- 时段状态：`PENDING/OPEN/FULL/CLOSED/EXPIRED`
- 主体类型：`TEAM/USER`
- 预约状态：`RESERVED/CANCELLED/CHECKED`
- 核销状态：`UNCHECKED/CHECKED`
- 错误码：见接口设计文档

### 2.3 Domain/DTO

新增 Domain：

- `CompetitionSceneResource`
- `CompetitionSceneScheduleResource`
- `CompetitionSceneResourceSlot`
- `CompetitionSceneResourceReservation`

新增 DTO：

- `CompetitionSceneResourceStatusReq`
- `CompetitionSceneBookingStatusReq`
- `CompetitionSceneResourceSlotBatchReq`
- `CompetitionSceneResourceSlotStatusReq`
- `CompetitionSceneResourceReservationCancelReq`
- `CompetitionSceneResourceReservationCheckReq`
- `UserSceneResourceReservationCreateReq`
- `SceneReservationSubjectContext`
- `SceneExistingReservationSummary`

## 3. 阶段二：资源台账

### 3.1 Mapper/XML

新增：

- `CompetitionSceneResourceMapper.java`
- `CompetitionSceneResourceMapper.xml`

能力：

- 分页查询。
- 详情。
- 新增。
- 修改。
- 逻辑删除。
- 修改资源状态。
- 校验资源编号唯一。

### 3.2 Service

新增：

- `ICompetitionSceneResourceService`
- `CompetitionSceneResourceServiceImpl`

校验：

- `resourceCode` 唯一。
- `deviceQuantity > 0`。
- `workstationCount > 0`，表示单台设备工位数。
- `defaultSlotDurationMinutes > 0`。
- `resourceStatus` 合法。
- 存在未删除赛场布置时拒绝删除资源。

### 3.3 Controller

新增：

- `CompetitionSceneResourceController`

接口：

- `GET /competition/sceneResource/list`
- `GET /competition/sceneResource/{resourceId}`
- `POST /competition/sceneResource`
- `PUT /competition/sceneResource`
- `DELETE /competition/sceneResource/{resourceIds}`
- `POST /competition/sceneResource/changeStatus`

## 4. 阶段三：赛场资源布置

### 4.1 Mapper/XML

新增：

- `CompetitionSceneScheduleResourceMapper.java`
- `CompetitionSceneScheduleResourceMapper.xml`

能力：

- 按 `scheduleId` 查询布置。
- 新增布置。
- 修改布置。
- 逻辑删除。
- 修改 `bookingStatus`。
- 查询开放时段数、已预约设备数、剩余设备数。

### 4.2 Service

新增：

- `ICompetitionSceneScheduleResourceService`
- `CompetitionSceneScheduleResourceServiceImpl`

核心逻辑：

- 新增布置时从资源台账带默认值。
- 保存时计算 `totalWorkstations`。
- `eventId = competitionSeriesId`。
- 资源必须 `ENABLED`。
- `deployedDeviceCount <= resource.deviceQuantity`。
- 初始 `bookingStatus = DRAFT`。
- 有未过期有效预约时，不允许把容量调低到低于已预约容量。
- 删除布置时校验不存在未过期 `RESERVED/CHECKED` 预约。

## 5. 阶段四：预约时段

### 5.1 Mapper/XML

新增：

- `CompetitionSceneResourceSlotMapper.java`
- `CompetitionSceneResourceSlotMapper.xml`

能力：

- 查询时段。
- 新增单个时段。
- 批量生成。
- 修改。
- 逻辑删除。
- 修改状态。
- 条件扣减容量。
- 取消预约时回补容量。

### 5.2 Service

新增：

- `ICompetitionSceneResourceSlotService`
- `CompetitionSceneResourceSlotServiceImpl`

核心逻辑：

- 计算工位容量。
- 不允许同一 `scheduleResourceId` 下时段重叠。
- 批量生成时跳过重叠时段。
- 使用乐观锁或条件更新防超卖。
- 容量满时自动置为 `FULL`。
- 取消释放容量后，在时段未关闭且未过期时可从 `FULL` 恢复 `OPEN`。

## 6. 阶段五：预约主体识别

新增服务建议：

- `CompetitionSceneReservationSubjectResolver`

输入：

- `scheduleId`
- `currentUserId`

输出：

- `subjectType`
- `subjectCode`
- `teamCode`
- `userId`
- `operatorUserId`
- `scheduleTarget`
- `memberCount`
- `credential`

职责：

- 从 `competition_scene_schedule_target` 解析当前用户对应的预约主体。
- 团队 target：确认当前用户是该团队有效参赛成员，返回 `TEAM + teamCode`。
- 个人 target：确认当前用户就是该个人，返回 `USER + userId/targetRef`。
- 无法识别返回 `SUBJECT_NOT_RESOLVED`。
- 非有效成员返回 `SUBJECT_MEMBER_INVALID`。
- 不做队长身份校验。

## 7. 阶段六：预约记录和用户预约

### 7.1 Mapper/XML

新增：

- `CompetitionSceneResourceReservationMapper.java`
- `CompetitionSceneResourceReservationMapper.xml`

能力：

- 管理端分页查询预约记录。
- 用户端查询我的预约。
- 查询同一 `scheduleId + subjectType + subjectCode` 未过期有效预约。
- 查询同一 slot 的未过期有效预约。
- 查询幂等键。
- 新增预约。
- 取消预约。
- 核销预约。

### 7.2 用户预约 Service

新增：

- `ICompetitionSceneResourceReservationService`
- `CompetitionSceneResourceReservationServiceImpl`

用户预约流程：

1. 校验 `idempotencyKey`。
2. 查询 slot、schedule resource、schedule。
3. 校验资源布置 `bookingStatus = OPEN`。
4. 校验 slot `slotStatus = OPEN` 且未过期。
5. 解析 reservation subject。
6. 校验 subject 是 `schedule_target` 匹配对象。
7. 校验有效现场证件。
8. 查询同一 schedule 下 subject 是否已有未过期 `RESERVED/CHECKED` 预约。
9. 如已有，返回 `ALREADY_RESERVED_BY_SUBJECT` 和 `existingReservation`。
10. 后端自动计算 `reservedDeviceCount`。
11. 校验非共享占用。
12. 校验容量。
13. 条件更新扣减容量。
14. 插入预约记录。

取消流程：

1. 校验预约存在。
2. 校验当前用户是该 subject 有效成员。
3. 校验 `reservationStatus = RESERVED`。
4. 校验 `checkStatus = UNCHECKED`。
5. 更新为 `CANCELLED`。
6. 回补时段容量。

核销流程：

1. 管理端或后续小程序扫码定位预约。
2. 校验 `reservationStatus = RESERVED`。
3. 更新为 `CHECKED`。
4. 写入 `checkUserId/checkTime/checkStatus`。

## 8. 阶段七：Controller

管理端：

- `CompetitionSceneResourceController`
- `CompetitionSceneScheduleResourceController`
- `CompetitionSceneResourceSlotController`
- `CompetitionSceneResourceReservationController`

用户端：

- `UserCompetitionSceneResourceController`
- `UserCompetitionSceneResourceReservationController`

用户端接口重点：

- `POST /competition/userCompetition/sceneResourceReservation` 只接收 `slotId` 和 `idempotencyKey`。
- 不接收 `reservedDeviceCount`。
- 不接收 `teamId`。
- 不接收 `subjectType/subjectCode`。

## 9. 阶段八：管理端前端

新增资源管理页面：

- `old-code-admin/src/views/tournament/sceneResource/index.vue`

新增现场安排资源与预约组件：

- `ScheduleResourceTab.vue`
- `ScheduleResourceForm.vue`
- `ResourceSlotDialog.vue`
- `ResourceReservationTable.vue`

预约记录展示：

- `subjectType`
- `subjectCode`
- `teamCode`
- `userId`
- `operatorUserId`
- 是否已过期。
- 已有预约摘要。

## 10. 阶段九：用户端前端

页面：

- 可预约资源列表。
- 资源详情。
- 预约确认。
- 我的预约。

交互重点：

- 不展示队长限制。
- 团队任意有效成员可预约。
- 已有预约时展示 `existingReservation`。
- 用户不能调整设备数。
- 设备数由后端返回。

## 11. 测试重点

服务测试：

- 团队任意有效成员可解析为同一 `TEAM + teamCode`。
- 个人参赛者解析为 `USER + userId/targetRef`。
- 无法解析 subject 返回 `SUBJECT_NOT_RESOLVED`。
- 非有效成员返回 `SUBJECT_MEMBER_INVALID`。
- 无有效现场证件返回 `NO_VALID_CREDENTIAL`。
- 团队一名成员预约后，其他成员再次预约返回 `ALREADY_RESERVED_BY_SUBJECT` 和已有预约。
- 已过期 slot 的预约不阻止后续新时段预约。
- 用户不能传设备数影响后端计算。
- 非共享 slot 已占用返回 `EXCLUSIVE_SLOT_OCCUPIED`。
- 并发预约不超卖。

验收测试：

- 管理员可维护资源台账。
- 管理员可布置资源和配置时段。
- 用户只看到自己有资格预约的资源。
- 团队任意有效参赛成员可代表本队预约。
- 同一赛场安排下本队已有预约后，其他成员看到已有预约并被拒绝重复预约。
- 个人参赛者只能查看和预约自己的资源。
- 预约以设备为单位。
- 工位容量展示正确。
- `RESERVED` 可取消。
- `CHECKED/CANCELLED` 不可取消。

## 12. 不做事项

本阶段不做：

- 传统资产管理字段。
- 独立现场系统。
- 旧 `site_*` 表。
- 独立运维端。
- 运维确认按钮。
- 运维工单。
- 最晚取消时间。
- 自动过期任务。
- 自动把预约状态改为 `EXPIRED`。
- 队长身份校验。
