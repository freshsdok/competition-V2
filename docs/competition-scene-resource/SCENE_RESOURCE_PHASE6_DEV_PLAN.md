# 大赛现场设备资源管理与预约第六阶段开发计划

生成时间：2026-07-01

## 2. 第六阶段功能边界

本阶段目标是补齐现场管理闭环，不扩大为复杂运维系统：

- 管理端预约记录列表强化。
- 管理端预约详情。
- 管理端预约核销。
- 管理端预约取消和异常处理。
- 预约状态、核销状态、过期展示筛选。
- 资源、赛场、时段维度统计。
- 现场使用状态展示。
- 全链路冒烟测试脚本与报告。
- PC / 小程序人工补测清单。

本阶段明确不做：

- 不做复杂审批流。
- 不做短信/微信通知。
- 不做 IoT 设备状态采集。
- 不做运维工单。
- 不改报名、支付、成绩、证书主流程。
- 不引入 `team_id`。
- 不恢复队长预约规则。
- 不绕过有效证件校验。
- 不绕过 `schedule_target` 校验。
- 不连接生产数据库。

## 3. 管理端预约记录页面设计

建议新增管理端页面：

- API：`old-code-admin/src/api/tournament/sceneResourceReservation.js`
- 页面：`old-code-admin/src/views/tournament/sceneResourceReservation/index.vue`

菜单建议：

- 菜单名称：资源预约记录
- 挂载位置：赛事管理或现场安排配置相关父菜单下。
- 组件：`tournament/sceneResourceReservation/index`

列表筛选：

- 赛事/赛场安排：`scheduleId`
- 布置资源：`scheduleResourceId`
- 资源名称：`resourceName`
- 时段：`slotId` 或开始/结束时间范围
- 主体类型：`subjectType`，`TEAM / USER`
- 主体编号：`subjectCode`
- 团队编号：`teamCode`
- 参赛用户：`userId`
- 操作人：`operatorUserId`
- 预约状态：`reservationStatus`
- 核销状态：`checkStatus`
- 是否已过期：前端展示字段，后端可用 `slotEndTime < now` 计算

列表字段：

- 预约编号
- 赛事/赛场安排
- 资源名称
- 资源类型
- 部署位置
- 时段开始/结束
- 是否已过期
- 主体类型
- 主体编号
- 团队编号
- 参赛用户
- 操作人
- 预约设备数
- 覆盖工位数
- 每台设备工位数
- 预约状态
- 核销状态
- 取消时间/取消原因
- 核销人/核销时间
- 创建时间

操作：

- 查看详情
- 核销
- 取消预约
- 查看关联赛场资源
- 查看关联时段

页面行为：

- 对 `RESERVED + UNCHECKED` 记录展示“核销”和“取消”。
- 对 `CHECKED` 记录禁用取消。
- 对 `CANCELLED` 记录禁用核销和取消。
- 对已过期但状态仍为 `RESERVED` 的记录展示“已过期”提示；数据库不自动改为 `EXPIRED`。

## 4. 预约核销业务规则

核销对象：

- 仅核销 `competition_scene_resource_reservation` 中的预约记录。
- 不开发独立运维端。
- 不开发扫码小程序核销新流程；现场扫码能力可在后续与已有二维码核验能力融合。

核销条件：

- `reservation_status = RESERVED`
- `check_status = UNCHECKED`
- 预约记录未逻辑删除。
- 关联时段、赛场资源、资源台账可查。
- 建议允许核销过期记录，但前端必须提示“时段已过期，请确认是否继续核销”。如需禁止过期核销，需要人工确认。

核销结果：

- `reservation_status` 更新为 `CHECKED`。
- `check_status` 更新为 `CHECKED`。
- 写入 `check_user_id` 和 `check_time`。
- 不释放容量。
- 核销后不可取消。

并发控制：

- 更新条件必须包含 `reservation_status = RESERVED` 和 `check_status = UNCHECKED`。
- 若更新行数为 0，返回“预约已被取消或已核销”。

日志建议：

- 记录到现有操作日志能力，或至少保留 `update_by/update_time`。
- 如复用 `competition_scene_operation_log`，日志类型建议：`RESOURCE_RESERVATION_CHECK`。

## 5. 预约取消/异常处理规则

本阶段不新增复杂异常状态，不新增审批流。

管理端取消条件：

- `reservation_status = RESERVED`
- `check_status = UNCHECKED`
- 预约记录未逻辑删除。
- 必填 `cancelReason`。

取消结果：

- `reservation_status` 更新为 `CANCELLED`。
- 写入 `cancel_time`、`cancel_reason`。
- 回补关联时段容量：
  - `reserved_device_count -= reservedDeviceCount`
  - `remaining_device_count += reservedDeviceCount`
  - `reserved_workstation_count -= coveredWorkstationCount`
  - `remaining_workstation_count += coveredWorkstationCount`
  - 回补后如果时段原为 `FULL` 且有剩余容量，应恢复为 `OPEN`。

不可取消：

- `CHECKED` 不可取消。
- `CANCELLED` 不可重复取消。
- 已核销不可取消。

异常处理口径：

- 现场资源故障、选手误约、赛场临时调整等，统一通过“管理端取消 + 必填原因”处理。
- 不新增 `ABNORMAL` 预约状态。
- 不新增运维工单。
- 不新增通知。

## 6. 统计指标设计

统计仅基于现有四张资源预约表和赛场安排表，不引入 IoT 设备状态。

总体统计：

- 可预约资源布置数
- 开放资源布置数
- 总时段数
- 开放时段数
- 已满时段数
- 总设备容量
- 已预约设备数
- 剩余设备数
- 总工位容量
- 已预约工位数
- 剩余工位数
- 有效预约数：`RESERVED + CHECKED` 且未过期
- 已核销数：`CHECKED`
- 已取消数：`CANCELLED`
- 已过期未核销数：`RESERVED` 且 `slotEndTime < now`
- 核销率：`CHECKED / (RESERVED + CHECKED + CANCELLED)`，展示时避免除零

按资源布置统计：

- `scheduleResourceId`
- 资源名称、类型、部署位置
- 部署设备数、每台工位数、总工位数
- 时段数、开放时段数、满员时段数
- 预约设备数、剩余设备数
- 预约记录数、核销数、取消数、过期未核销数

按赛场安排统计：

- `scheduleId`
- 赛场安排名称
- 资源布置数
- 时段数
- 预约主体数
- 预约设备数
- 核销数
- 取消数

按时段统计：

- `slotId`
- 开始时间、结束时间、时段状态
- 设备容量、已预约设备、剩余设备
- 工位容量、已预约工位、剩余工位
- 预约记录数、核销数、取消数
- 现场使用状态

现场使用状态建议：

- `NOT_STARTED`：当前时间早于时段开始时间
- `WAITING_CHECK`：时段已开始，存在 `RESERVED + UNCHECKED`
- `IN_USE`：存在 `CHECKED`
- `FULL`：剩余设备数为 0 或时段状态为 `FULL`
- `FINISHED`：当前时间晚于时段结束时间
- `CLOSED`：时段状态为 `CLOSED`

状态计算用于展示，不作为数据库新字段落库。

## 7. 后端接口设计

管理端预约记录：

- `GET /competition/sceneResourceReservation/list`
- `GET /competition/sceneResourceReservation/{reservationId}`
- `POST /competition/sceneResourceReservation/cancel`
- `POST /competition/sceneResourceReservation/check`

统计接口：

- `GET /competition/sceneResourceReservation/stat/overview`
- `GET /competition/sceneResourceReservation/stat/bySchedule`
- `GET /competition/sceneResourceReservation/stat/byResource`
- `GET /competition/sceneResourceReservation/stat/bySlot`
- `GET /competition/sceneResourceReservation/useStatus`

请求参数建议：

- `scheduleId`
- `scheduleResourceId`
- `resourceId`
- `slotId`
- `subjectType`
- `subjectCode`
- `teamCode`
- `reservationStatus`
- `checkStatus`
- `startTime`
- `endTime`

新增/完善后端类：

- `CompetitionSceneResourceReservationController`
- `ICompetitionSceneResourceReservationService`
- `CompetitionSceneResourceReservationServiceImpl`
- `CompetitionSceneResourceReservationCheckReq`
- `CompetitionSceneResourceReservationAdminCancelReq`（可复用现有 cancel req，但建议管理端独立命名）
- `CompetitionSceneResourceReservationStatsQuery`
- `CompetitionSceneResourceReservationOverviewVO`
- `CompetitionSceneResourceReservationStatVO`
- `CompetitionSceneResourceUseStatusVO`

Mapper 需要增强：

- 按 `checkStatus` 查询。
- 按资源名称模糊查询。
- 按时段开始/结束时间查询。
- 查询详情补充 subject/operator 显示字段。
- 管理端核销条件更新。
- 统计聚合 SQL。

权限码建议：

- `competition:sceneResourceReservation:list`
- `competition:sceneResourceReservation:query`
- `competition:sceneResourceReservation:cancel`
- `competition:sceneResourceReservation:check`
- `competition:sceneResourceReservation:stats`

## 8. 前端页面设计

管理端新增 API：

- `old-code-admin/src/api/tournament/sceneResourceReservation.js`

管理端新增页面：

- `old-code-admin/src/views/tournament/sceneResourceReservation/index.vue`

可拆组件：

- `components/ReservationDetailDialog.vue`
- `components/ReservationCancelDialog.vue`
- `components/ReservationStatsPanel.vue`

页面布局：

- 顶部统计摘要：预约数、已核销、已取消、过期未核销、剩余设备数。
- 查询表单：赛场、资源、主体、状态、时间范围。
- 表格：预约记录。
- 操作列：详情、核销、取消。
- 详情弹窗：预约主体、操作人、资源、时段、设备/工位、状态流转信息。
- 取消弹窗：填写取消原因。
- 核销确认弹窗：展示主体、资源、时段，二次确认。

与“赛场安排 - 资源与预约”Tab 的关系：

- 本阶段不强行把预约记录表格塞进已有 Tab。
- 可在资源与预约 Tab 中预留“查看预约记录”入口，跳转到预约记录页面并带 `scheduleResourceId`。
- 先保证独立预约记录页面完整可用。

## 9. PC / 小程序人工补测清单

PC 端人工补测：

- 登录测试用户。
- 进入个人中心资源预约页面。
- 可看到可预约资源列表。
- 可查看资源详情、安全须知、注意事项、使用说明。
- 可查看可预约时段。
- 预约成功。
- 重复预约时展示已有预约信息。
- 我的预约列表展示正确。
- 取消预约成功。
- 取消后可重新预约。
- 无有效证件用户不可预约。
- 非匹配对象用户不可预约。
- 非共享资源被其他主体预约后不可预约。

小程序人工补测：

- 使用微信开发者工具或真机打开 `pages/scene-resource/index`。
- 登录态可正常带 token。
- 可预约资源列表加载正常。
- 资源详情/时段列表正常。
- 预约成功。
- 重复预约提示已有预约。
- 我的预约显示正确。
- 取消预约成功。
- 异常错误码提示可读。

人工补测输出建议：

- 截图：资源列表、详情、预约确认、我的预约、错误提示。
- 记录测试账号、scheduleResourceId、slotId、reservationId。
- 标记 PC 和小程序是否存在样式或交互问题。

## 10. 全链路冒烟测试清单

管理端准备：

- 新增/启用资源台账。
- 在赛场安排中布置资源。
- 生成预约时段。
- 开放资源预约。
- 确认 `schedule_target` 匹配对象存在。
- 确认现场证件有效。

用户端预约：

- 个人用户预约成功。
- 团队成员 A 代表团队预约成功。
- 团队成员 B 再次预约同一赛场时返回已有预约信息。
- 无有效证件用户失败。
- 非匹配对象用户失败。
- 共享资源容量足够时多个主体预约成功。
- 非共享资源已有主体预约后其他主体失败。

管理端处理：

- 预约记录列表能查询到用户预约。
- 详情展示 subject/operator/资源/时段/设备数。
- 管理端核销成功。
- 核销后用户不可取消。
- 管理端取消未核销预约成功。
- 取消后容量回补。
- 统计面板数据与预约记录一致。
- 现场使用状态展示正确。

数据清理：

- 冒烟测试产生的预约记录取消或保留为测试记录。
- 确认无 `RESERVED` 脏数据影响后续测试。
- 确认时段容量回到预期值。


