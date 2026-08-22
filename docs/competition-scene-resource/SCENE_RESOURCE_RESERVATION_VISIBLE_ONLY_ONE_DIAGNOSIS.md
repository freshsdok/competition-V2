# 设备预约不可见诊断报告

诊断时间：2026-07-09 17:24:27（Asia/Shanghai，本机测试库 `jiaoxue_test`）。

本轮仅做数据和算法诊断：未修改代码，未删除或修改任何业务数据，未连接生产数据库。数据库核查通过本机 Docker MySQL `dev-mysql80` 的 `jiaoxue_test` 库完成；本地 `9205` 服务接口使用题述学生 token 做了只读验证。

## 1. 学生身份信息

| 字段 | 值 |
| --- | --- |
| userId | `26023` |
| userName / phone | `19856477631` |
| 姓名 | `余嘉俊` |
| 当前赛事 competition_series_id | `81` |
| 报名 team_code | `2026_2067237493743706112` |
| 报名 team_name | `DS_本科A_284742` |
| 报名组别 second_level_code | `CT_164974` |
| 报名组别 second_level_name | `本科A` |

该学生在赛事 `81` 下的有效队员数据如下。当前算法会排除指导教师，仅保留已缴费、审核通过或审核状态为空的参赛队员。

| user_id | user_name | role | pay_status | check_status | second_level_code | second_level_name |
| ---: | --- | --- | --- | --- | --- | --- |
| `26023` | `余嘉俊` | 队员 | `paid` | `NULL` | `CT_164974` | `本科A` |
| `26029` | `王婧涛` | 队员 | `paid` | `NULL` | `CT_164974` | `本科A` |

因此团队预约主体的人数按当前算法为 `2`。

## 2. 所属赛场和组别

当前团队在 `competition_scene_schedule_target` 中有两个来源赛场，每个赛场下同队两名队员各有一条 target 行：

| target_id | schedule_id | schedule_name | config_dimension | team_code | user_id | user_name | second_level_code | second_level_name |
| ---: | ---: | --- | --- | --- | ---: | --- | --- | --- |
| `392` | `82` | `2026/07/17(10:30-11:10)教二楼（理学院）4层404` | `PERSON` | `2026_2067237493743706112` | `26023` | `余嘉俊` | `CT_164974` | `本科AB组` |
| `1395` | `82` | `2026/07/17(10:30-11:10)教二楼（理学院）4层404` | `PERSON` | `2026_2067237493743706112` | `26029` | `王婧涛` | `CT_164974` | `本科AB组` |
| `730` | `111` | `2026/07/17(8:00-8:45)教三楼2层202室` | `PERSON` | `2026_2067237493743706112` | `26023` | `余嘉俊` | `CT_164974` | `本科AB组` |
| `1733` | `111` | `2026/07/17(8:00-8:45)教三楼2层202室` | `PERSON` | `2026_2067237493743706112` | `26029` | `王婧涛` | `CT_164974` | `本科AB组` |

注意：虽然目标行的 `config_dimension=PERSON`，但当前服务 `resolveAllSubjects` 的逻辑是只要 target 上存在 `team_code`，就优先构造团队主体。因此当前学生的预约主体按算法解析为：

| 字段 | 值 |
| --- | --- |
| subject_type | `TEAM` |
| subject_code | `2026_2067237493743706112` |
| team_code | `2026_2067237493743706112` |
| group_code | `CT_164974` |
| group_name | `本科AB组` |
| schedule_id | `82`, `111` |
| active_reservation_key | `RESV:81:TEAM:2026_2067237493743706112` |

## 3. resource_schedule_scope 命中情况

查询条件：

- `allowed_schedule_id in (82, 111)`
- `enabled = 1`
- `deleted = 0`

分别统计：

| allowed_schedule_id | scope 行数 | distinct schedule_resource_id | distinct resource_id | 命中的 schedule_resource_id |
| ---: | ---: | ---: | ---: | --- |
| `82` | `2` | `2` | `2` | `92052026070621`, `92052026070623` |
| `111` | `2` | `2` | `2` | `92052026070621`, `92052026070623` |

合并去重后：

| scope 行数 | schedule_resource_id 数 | resource_id 数 | 命中的 schedule_resource_id | 命中的 resource_id |
| ---: | ---: | ---: | --- | --- |
| `4` | `2` | `2` | `92052026070621`, `92052026070623` | `92052026070621`, `92052026070623` |

同库中当前 `booking_status=OPEN` 的资源布置共有 `4` 个；该学生所属来源赛场命中其中 `2` 个。

## 4. schedule_resource 状态过滤情况

命中的资源布置：

| allowed_schedule_id | schedule_resource_id | resource_id | 部署 schedule_id | booking_status | 预约窗口 | shared_occupancy | resource_name | resource_status |
| ---: | ---: | ---: | ---: | --- | --- | ---: | --- | --- |
| `82` | `92052026070621` | `92052026070621` | `60` | `OPEN` | `2026-07-06 10:14:03` 至 `2026-07-15 00:00:00` | `1` | `P2IT共享资源` | `ENABLED` |
| `82` | `92052026070623` | `92052026070623` | `59` | `OPEN` | `2026-07-09 00:00:00` 至 `2026-07-15 00:00:00` | `0` | `5G商用设备实践赛赛前练习` | `ENABLED` |
| `111` | `92052026070621` | `92052026070621` | `60` | `OPEN` | `2026-07-06 10:14:03` 至 `2026-07-15 00:00:00` | `1` | `P2IT共享资源` | `ENABLED` |
| `111` | `92052026070623` | `92052026070623` | `59` | `OPEN` | `2026-07-09 00:00:00` 至 `2026-07-15 00:00:00` | `0` | `5G商用设备实践赛赛前练习` | `ENABLED` |

统计：

| 项 | 数量 |
| --- | ---: |
| scope 命中资源数 | `2` |
| 被 `booking_status` 过滤掉 | `0` |
| 被资源 `resource_status` 过滤掉 | `0` |
| 预约窗口不开放 | `0` |
| 最终可进入 slot 查询的资源数 | `2` |

当前数据下，赛场范围和资源开放状态都不是最终为空的原因。

## 5. slot 状态和时间过滤情况

查询资源：`schedule_resource_id in (92052026070621, 92052026070623)`。

条件：

- `slot_status = OPEN`
- 当前时间 `< start_time`

统计：

| 项 | 数量 |
| --- | ---: |
| 总 slot 数 | `19` |
| 非 OPEN 数 | `0` |
| 已开始 / 已过期数（`start_time <= now()`） | `1` |
| OPEN 且未开始数 | `18` |

按资源拆分：

| schedule_resource_id | resource_name | 总 slot 数 | OPEN 且未开始数 | 备注 |
| ---: | --- | ---: | ---: | --- |
| `92052026070621` | `P2IT共享资源` | `5` | `4` | `92052026070950` 已开始，不进入可预约 |
| `92052026070623` | `5G商用设备实践赛赛前练习` | `14` | `14` | 全部未开始 |

时间和 slot 状态过滤后，仍有 `18` 个候选 slot。

## 6. slot 组别过滤情况

当前学生 group_code：`CT_164974`。

规则：

- slot 未配置 group_scope：不限组别；
- slot 配置 group_scope：学生 `second_level_code` 必须命中 `allowed_group_code`。

对 `18` 个 `OPEN 且未开始` slot 的统计：

| 项 | 数量 |
| --- | ---: |
| 不限组别 slot 数 | `0` |
| 配置组别且命中 slot 数 | `0` |
| 配置组别但不命中 slot 数 | `18` |

按资源拆分：

| schedule_resource_id | resource_name | 未来 OPEN slot 数 | 命中当前组别 | 未命中当前组别 |
| ---: | --- | ---: | ---: | ---: |
| `92052026070621` | `P2IT共享资源` | `4` | `0` | `4` |
| `92052026070623` | `5G商用设备实践赛赛前练习` | `14` | `0` | `14` |

被过滤的 group_code 明细：

| allowed_group_code | allowed_group_name | 当前学生 group_code | 当前学生 group_name | slot 数 | 涉及 slot |
| --- | --- | --- | --- | ---: | --- |
| `CT_992271` | `中高职组` | `CT_164974` | `本科AB组` | `18` | `92052026070940`-`92052026070943`, `92052026070951`-`92052026070964` |

这是最终看不到可预约资源的直接原因：所有未来 OPEN slot 都限定给 `CT_992271 / 中高职组`，当前学生是 `CT_164974 / 本科AB组`，全部被 `checkSlotGroupAllowed` 过滤。

## 7. 容量过滤情况

容量规则：

- 共享占用：`remaining_workstation_count > 0`；
- 非共享占用：`remaining_device_count > 0` 且 `remaining_workstation_count > 0`。

统计：

| 统计范围 | 容量可用 slot 数 | 工位不足 slot 数 | 设备不足 slot 数 |
| --- | ---: | ---: | ---: |
| group 过滤前的未来 OPEN slot | `18` | `0` | `0` |
| group 过滤后的 slot | `0` | `0` | `0` |

容量不是瓶颈：

- `92052026070621` 为共享占用，4 个未来 slot 的剩余工位均大于 `0`；
- `92052026070623` 为非共享占用，14 个未来 slot 的剩余设备和剩余工位均大于 `0`。

真正先发生的是组别过滤；容量层没有机会放行任何 slot。

## 8. 已有预约情况

按当前算法生成的有效预约唯一键：

- 团队主体：`RESV:81:TEAM:2026_2067237493743706112`
- 若按个人主体假设：`RESV:81:USER:26023`

查询条件：

- `competition_series_id = 81`
- 当前主体 `subject_type + subject_code`
- `reservation_status in ('RESERVED', 'CHECKED')`
- `active_reservation_key is not null`
- `deleted = 0`

结果：

| 检查项 | 结果 |
| --- | --- |
| 当前团队主体是否已有有效预约 | 否 |
| 当前个人主体是否已有有效预约 | 否 |
| `active_reservation_key` 是否命中 | 否 |
| 用户端是否只展示 `existingReservation` | 否，无 `existingReservation` 命中 |
| 操作人 / 资源 / slot | 不适用 |

本地接口只读验证：

| 接口 | 返回 |
| --- | --- |
| `GET /competition/userCompetition/sceneResource/bookableList` | `code=200`, `data=[]` |
| `GET /competition/userCompetition/sceneResourceReservation/myList` | `code=200`, `data=[]` |

因此，这次不是已有有效预约导致用户端只展示 `existingReservation`。

## 9. 结论：为什么最终看不到可预约数据

按本地测试库和当前预约列表算法，过滤链路如下：

| 过滤层 | 剩余数量 | 说明 |
| --- | ---: | --- |
| 来源赛场 scope | `2` 个资源 | `82/111` 命中 `92052026070621`, `92052026070623` |
| 资源开放状态 / 预约窗口 | `2` 个资源 | 两个资源均 `OPEN`、窗口内、资源 `ENABLED` |
| slot 状态 / 时间 | `18` 个 slot | 19 个 slot 中 1 个已开始，18 个未来 OPEN |
| slot 组别 | `0` 个 slot | 18 个未来 slot 全部仅允许 `CT_992271 / 中高职组` |
| 容量 | `0` 个 slot | group 已归零；group 前容量全部可用 |
| 已有预约 | 无 | 不存在有效 `active_reservation_key` |

最终不可见的直接原因是：`competition_scene_resource_slot_group_scope` 配置的 `allowed_group_code=CT_992271` 与当前学生 `second_level_code=CT_164974` 不一致，导致两个资源下的所有未来 OPEN slot 都被组别过滤。由于 `buildBookableVO` 只有在 `nextSlotId != null` 或存在 `existingReservation` 时才加入资源列表，所以 `bookableList` 最终返回空数组。

## 10. 问题归类

主要归类：配置 / 测试数据问题，不是当前代码算法 bug。

证据：

- 当前学生所属 `schedule_id=82/111` 能命中资源 scope；
- 命中资源均为 `OPEN`，预约窗口内，资源状态为 `ENABLED`；
- 未来 OPEN slot 数量充足，共 `18` 个；
- group_scope 全部配置为 `CT_992271 / 中高职组`，与当前学生 `CT_164974 / 本科AB组` 不匹配；
- 容量在 group 过滤前全部可用；
- 当前团队/个人主体均无有效预约，`existingReservation` 不存在；
- 本地服务接口返回 `bookableList.data=[]`，与算法推导一致。

补充风险：

- `competition_scene_schedule_target.config_dimension=PERSON` 但 target 行带 `team_code`，当前服务会解析为 `TEAM` 主体。这不导致本次不可见，但会影响预约唯一键、团队占用人数和“同队仅一个有效预约”的业务口径。
- 当前 `selectEffectiveReservationByActiveKey` 只按 `active_reservation_key + RESERVED/CHECKED` 判断，不按 slot 结束时间排除过期预约。本次没有命中已有预约，所以不影响本次结论。

## 11. 最小修正建议

1. 如果 `CT_164974 / 本科AB组` 应允许预约这批 slot：在 `competition_scene_resource_slot_group_scope` 中为目标 slot 增加 `allowed_group_code=CT_164974`，或把这些 slot 的 group_scope 移除，使其变为不限组别。

2. 如果这批 slot 只应给 `CT_992271 / 中高职组`：当前数据配置与算法结果一致，`userId=26023` 不应看到可预约数据；需要调整测试账号或测试预期。

3. 如果希望该学生只看到特定资源：保持或调整 `competition_scene_resource_schedule_scope` 即可；当前 `82/111` 已命中两个资源，不是资源范围缺失问题。

4. 建议统一 `config_dimension=PERSON` 与 `team_code` 的数据口径：若业务期望个人预约，生成 target 时不要携带会触发团队主体解析的 `team_code`，或在算法中明确按 `config_dimension` 优先判断主体类型。
