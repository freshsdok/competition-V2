# 资源预约第二包测试数据方案

## 1. 数据准备状态

当前未向测试库写入新的联调业务数据。

原因：正式 competition 服务 `127.0.0.1:9205` 的进程启动时间早于第二包 class 编译时间，当前进程尚未加载第二包代码。为避免写入无法用当前正式服务验证的测试脏数据，本轮仅输出最小测试数据方案，待 9205 重启到第二包代码后再落库或通过管理端配置。

本方案不包含真实手机号、不包含身份证号、不包含 token。

## 2. 测试数据命名约定

- 前缀：`P2IT_`
- 用途：资源预约第二包联调
- 建议测试完成后由人工确认清理范围，再清理 `P2IT_` 前缀数据。

## 3. 核心 ID 规划

| 对象 | 建议值 | 说明 |
| --- | --- | --- |
| `competitionSeriesId` | `920520260706` | 第二包联调赛事 |
| 部署资源赛场 `deployScheduleId` | `92052026070601` | 部署设备资源的赛场 |
| 允许预约来源赛场 `allowedScheduleId` | `92052026070602` | 写入 resource schedule scope |
| 不允许预约来源赛场 `blockedScheduleId` | `92052026070603` | 用于验证资源不返回 |
| 共享资源 `sharedScheduleResourceId` | `92052026070611` | `shared_occupancy=true` |
| 非共享资源 `exclusiveScheduleResourceId` | `92052026070612` | `shared_occupancy=false` |
| 共享资源 `resourceId` | `92052026070621` | 测试资源 |
| 非共享资源 `resourceId` | `92052026070622` | 测试资源 |

## 4. 用户与团队

| 用户 | 建议 userId | 团队 | groupCode | groupName | 用途 |
| --- | --- | --- | --- | --- | --- |
| 队伍 A 成员 A | `920506001` | `P2IT_TEAM_A` | `P2IT_G_A` | `第二包A组` | 首次预约 |
| 队伍 A 成员 B | `920506002` | `P2IT_TEAM_A` | `P2IT_G_A` | `第二包A组` | 同队重复预约、我的预约 |
| 队伍 B 成员 A | `920506003` | `P2IT_TEAM_B` | `P2IT_G_A` | `第二包A组` | 多队抢共享/非共享 slot |
| 队伍 C 成员 A | `920506004` | `P2IT_TEAM_C` | `P2IT_G_B` | `第二包B组` | group 不命中验证 |
| 个人赛用户 | `920506005` | 空 | `P2IT_G_A` | `第二包A组` | 个人按 1 人扣容量 |

要求：

- `competition_scene_schedule_target` 中状态为有效、匹配成功、未删除。
- 队伍 A 至少 2 个有效成员。
- 队伍 B/C 至少 1 个有效成员。
- 团队有效成员需在报名信息中满足“非指导教师、已支付、审核通过或空审核状态”的有效成员口径。

## 5. resource schedule scope

至少写入：

| scheduleResourceId | allowedScheduleId | sourceType | enabled | 用途 |
| --- | --- | --- | --- | --- |
| `sharedScheduleResourceId` | `allowedScheduleId` | `MANUAL_BIND` | `1` | 允许来源赛场预约共享资源 |
| `exclusiveScheduleResourceId` | `allowedScheduleId` | `MANUAL_BIND` | `1` | 允许来源赛场预约非共享资源 |

不要写入：

| scheduleResourceId | blockedScheduleId | 用途 |
| --- | --- | --- |
| `sharedScheduleResourceId` | `blockedScheduleId` | 验证未命中资源范围时资源不返回 |

## 6. slot 规划

### 共享占用资源

| slot | slotStatus | startTime | group scope | 容量 | 用途 |
| --- | --- | --- | --- | --- | --- |
| `P2IT_SHARED_OPEN_ALL` | `OPEN` | 当前时间 + 2 小时 | 不配置 | 2 台、每台 4 工位、剩余 8 工位 | 不限组别、共享扣工位 |
| `P2IT_SHARED_GROUP_A` | `OPEN` | 当前时间 + 3 小时 | `P2IT_G_A` | 2 台、每台 4 工位、剩余 8 工位 | group 命中 |
| `P2IT_SHARED_GROUP_B` | `OPEN` | 当前时间 + 4 小时 | `P2IT_G_B` | 2 台、每台 4 工位、剩余 8 工位 | group 不命中过滤 |
| `P2IT_SHARED_STARTED` | `OPEN` | 当前时间 - 10 分钟 | 不配置 | 2 台、每台 4 工位、剩余 8 工位 | 已开始不返回/不可预约 |
| `P2IT_SHARED_CLOSED` | `CLOSED` | 当前时间 + 5 小时 | 不配置 | 2 台、每台 4 工位、剩余 8 工位 | CLOSED 不返回/不可预约 |
| `P2IT_SHARED_NO_CAPACITY` | `OPEN` | 当前时间 + 6 小时 | 不配置 | 剩余工位 0 | 容量不足 |

共享占用验证预期：

- 队伍 A 有效人数为 `N`。
- `reserved_workstation_count = N`。
- `remaining_workstation_count` 减 `N`。
- `remaining_device_count` 不减少。
- `reserved_device_count = ceil(N / workstation_count)` 仅作为快照展示。

### 非共享占用资源

| slot | slotStatus | startTime | group scope | 容量 | 用途 |
| --- | --- | --- | --- | --- | --- |
| `P2IT_EXCLUSIVE_OPEN_ALL` | `OPEN` | 当前时间 + 2 小时 | 不配置 | 2 台、每台 4 工位、剩余 2 台/8 工位 | 非共享整台扣减 |
| `P2IT_EXCLUSIVE_GROUP_A` | `OPEN` | 当前时间 + 3 小时 | `P2IT_G_A` | 2 台、每台 4 工位、剩余 2 台/8 工位 | group 命中 |
| `P2IT_EXCLUSIVE_DEVICE_SHORT` | `OPEN` | 当前时间 + 4 小时 | 不配置 | 剩余 0 台/8 工位 | 设备不足拒绝 |
| `P2IT_EXCLUSIVE_WORKSTATION_SHORT` | `OPEN` | 当前时间 + 5 小时 | 不配置 | 剩余 2 台/0 工位 | 工位不足拒绝 |

非共享占用验证预期：

- 队伍 A 有效人数为 `N`。
- `workstation_count = W`。
- `reserved_device_count = ceil(N / W)`。
- `reserved_workstation_count = reserved_device_count * W`。
- `remaining_device_count` 减 `reserved_device_count`。
- `remaining_workstation_count` 减 `reserved_workstation_count`。

## 7. 预约测试用 idempotency_key

建议按用例生成唯一值：

- `P2IT-SHARED-A-<timestamp>`
- `P2IT-SHARED-A-REPEAT-<timestamp>`
- `P2IT-EXCLUSIVE-A-<timestamp>`
- `P2IT-CONCURRENT-TEAM-A-<timestamp>`
- `P2IT-CONCURRENT-SHARED-<timestamp>-<seq>`
- `P2IT-CONCURRENT-EXCLUSIVE-<timestamp>-<seq>`

不在文档中记录真实 token。

## 8. 并发数据准备

共享并发：

- 准备一个 `remaining_workstation_count = 8` 的共享 slot。
- 准备 3 个不同团队，每队有效人数建议为 3。
- 预期最多成功 2 队，第三队容量不足，`remaining_workstation_count` 不为负。

非共享并发：

- 准备一个 `remaining_device_count = 2`、`workstation_count = 4` 的非共享 slot。
- 准备 3 个不同团队，每队有效人数建议为 3。
- 每队需要 1 台，预期最多成功 2 队，第三队容量不足，`remaining_device_count` 不为负。

同队并发：

- 队伍 A 成员 A/B 同时提交不同 `idempotency_key`。
- 预期只有一条有效预约，另一请求返回 `ALREADY_RESERVED`。

重复取消：

- 对同一有效预约并发/重复调用取消接口。
- 预期仅第一次回补容量，后续返回当前记录或不可重复回补。

## 9. 落库前置条件

落库前需满足：

1. 测试库 DB migration 已验证通过；
2. 正式 9205 competition 服务已重启并加载第二包 class；
3. gateway 9889、auth 9224、competition 9205 均为 `UP`；
4. 能获取测试用户 token，但 token 不写入文档；
5. 人工确认是否需要清理既有 `P2IT_` 测试数据。

当前第 2 项未满足，因此本轮未实际落库。
