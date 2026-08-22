# 资源预约第二包 P2IT 测试数据落库记录

落库时间：2026-07-06  
数据前缀：`P2IT_`  
测试库：`jiaoxue_test` 测试库，未连接生产数据库。

## 1. 旧数据检查

落库前检查：

- 规划核心 ID 未被占用。
- `competition_scene_resource`、`competition_scene_resource_schedule_scope`、`competition_scene_resource_slot_group_scope`、`competition_scene_resource_reservation` 未发现会冲突的 `P2IT_` 业务数据。
- 未执行危险自动清理 SQL。

## 2. 已落库核心对象

| 对象 | 数量/ID | 说明 |
| --- | --- | --- |
| 测试赛事 | `920520260706` | `P2IT第二包联调赛事` |
| 部署资源赛场 | `92052026070601` | 部署设备资源 |
| 允许预约来源赛场 | `92052026070602` | 写入 resource schedule scope |
| 不允许预约来源赛场 | `92052026070603` | 不写入 resource schedule scope |
| 共享资源 | `resource_id=92052026070621` | `default_shared_occupancy=1` |
| 非共享资源 | `resource_id=92052026070622` | `default_shared_occupancy=0` |
| 共享 schedule_resource | `92052026070611` | `booking_status=OPEN` |
| 非共享 schedule_resource | `92052026070612` | `booking_status=OPEN` |
| 关闭 schedule_resource | `92052026070613` | `booking_status=CLOSED`，已绑定 scope 用于验证不返回 |
| resource schedule scope | 3 条 | 共享、非共享、关闭资源均绑定 allowed schedule |
| slot group scope | 3 条 | 共享 A 组、共享 B 组、非共享 A 组 |
| slot | 18 条 | 覆盖 OPEN、CLOSED、已开始、容量不足、并发、FULL 场景 |
| schedule target | 13 条 | 团队、个人、blocked 来源赛场 |
| 报名成员 | 12 条 | 队伍 A/B/C/D/E、个人、blocked 队伍 |
| 假用户 | 12 个 | `P2IT_USER_*` |

## 3. 用户与组别

| 用户 | userId | 主体 | groupCode | 用途 |
| --- | --- | --- | --- | --- |
| `P2IT_USER_A1` | `920506001` | `P2IT_TEAM_A` | `P2IT_G_A` | 首次预约、容量、并发 |
| `P2IT_USER_A2` | `920506002` | `P2IT_TEAM_A` | `P2IT_G_A` | 同队重复、我的预约、取消 |
| `P2IT_USER_B1` | `920506003` | `P2IT_TEAM_B` | `P2IT_G_A` | 跨队验证 |
| `P2IT_USER_C1` | `920506004` | `P2IT_TEAM_C` | `P2IT_G_B` | group 不命中/命中对照 |
| `P2IT_USER_PERSON` | `920506005` | 个人 | `P2IT_G_A` | 个人赛按 1 人 |
| `P2IT_USER_D1/D2/D3` | `920506006-008` | `P2IT_TEAM_D` | `P2IT_G_A` | 并发容量 |
| `P2IT_USER_E1/E2/E3` | `920506009-011` | `P2IT_TEAM_E` | `P2IT_G_A` | 并发容量 |
| `P2IT_USER_BLOCKED` | `920506012` | `P2IT_TEAM_BLOCK` | `P2IT_G_A` | 未授权来源赛场 |

说明：

- 服务实际读取 `competition_scene_schedule_target.second_level_code / second_level_name` 作为预约组别来源，本轮按该字段落库。
- 曾误把 `P2IT_TEAM_BLOCK` 写入 allowed schedule target；已仅禁用该条 P2IT 误配 target，保留 blocked schedule target 用于未授权来源赛场验证。

## 4. slot 场景

| slotId | 资源 | 状态/容量 | 用途 |
| --- | --- | --- | --- |
| `92052026070901` | 共享 | OPEN，8 工位 | 不限组别、个人预约 |
| `92052026070902` | 共享 | OPEN，A 组 | group 命中、共享基础预约 |
| `92052026070903` | 共享 | OPEN，B 组 | A 组不命中过滤 |
| `92052026070904` | 共享 | OPEN，已开始 | 已开始不可预约 |
| `92052026070905` | 共享 | CLOSED | CLOSED 不可预约 |
| `92052026070906` | 共享 | 工位 0 | 容量不足 |
| `92052026070907` | 非共享 | OPEN，不限组别 | 非共享基础 |
| `92052026070908` | 非共享 | OPEN，A 组 | group 命中、非共享基础 |
| `92052026070909` | 非共享 | 设备 0 | 设备不足 |
| `92052026070910` | 非共享 | 工位 0 | 工位不足 |
| `92052026070911` | 共享 | 6 工位 | 多队并发共享 |
| `92052026070912` | 非共享 | 2 台/8 工位 | 多队并发非共享 |
| `92052026070913` | 共享 | OPEN | 同队并发 |
| `92052026070915` | 共享 | OPEN | 容量快照、取消边界、FULL 恢复 |
| `92052026070916` | 非共享 | OPEN | 非共享容量快照 |
| `92052026070917` | 共享 | FULL | FULL 基线 |
| `92052026070918` | 非共享 | FULL | FULL 基线 |

## 5. 登录态

- 为 `P2IT_USER_*` 假用户写入 bcrypt 测试密码，用于正式 auth 登录链路。
- 联调 token 通过 `9224 /login` 获取。
- 未在文档中记录 token。

## 6. 收尾状态

本轮验证结束后：

- `P2IT-%` 有效预约数：`0`
- `P2IT-%` 非空 active key 数：`0`
- P2IT slot 负容量数：`0`

保留 `P2IT_` 假数据供后续复测；未执行自动清理。
