# 资源预约第二包修复后联调复测报告

复测时间：2026-07-06  
复测批次：`P2FIX-1783336376707`  
结论：第二包修复后联调仍未通过。`FULL` 状态计算、取消回补、`CANCELLED` 状态和关键回归已通过；但并发 `active_reservation_key` 和并发 `idempotency_key` 唯一冲突仍返回 SQL `500`，未按业务期望返回 `ALREADY_RESERVED` 或原预约结果。

## 1. 9205 Runtime 验证

- 正式测试用 `9205` 已重启，Java PID：`96235`。
- `9205` 启动时间：`Mon Jul 6 18:59:08 2026`。
- P2 fix class 编译时间：`2026-07-06 18:51:12 +0800`。
- gateway `9889`、auth `9224`、competition `9205` 均为 `UP`。
- 接口请求通过正式 gateway `9889` 调用，未用临时 competition 进程替代联调结论。

## 2. 同队并发预约复测

结果：不通过。

验证场景：

- 同队成员 `P2IT_USER_A1`、`P2IT_USER_A2` 同时预约 slot `92052026070913`。
- 两个请求使用不同 `idempotency_key`。
- 预约主体 active key：`RESV:920520260706:TEAM:P2IT_TEAM_A`。

实际结果：

| 项目 | 实际 |
| --- | --- |
| 成功请求数 | `1` |
| `ALREADY_RESERVED` 请求数 | `0` |
| SQL 500 请求数 | `1` |
| 有效预约数 | `1` |
| slot 剩余工位 | `6` |
| slot 剩余设备 | `2` |

失败响应仍为唯一键异常：

```text
SQLIntegrityConstraintViolationException: Duplicate entry 'RESV:920520260706:TEAM:P2IT_TEAM_A'
for key 'competition_scene_resource_reservation.uk_scene_resource_active_reservation_key'
```

容量只扣减一次，未超卖；但失败请求没有转换为 `ALREADY_RESERVED`，也没有返回已有预约信息。

## 3. idempotency_key 并发复测

结果：不通过。

验证场景：

- 同一用户并发提交同一个 `idempotency_key`。
- slot：`92052026070901`。

实际结果：

| 项目 | 实际 |
| --- | --- |
| 成功响应数 | `1` |
| SQL 500 响应数 | `1` |
| 同 idempotency_key 预约记录数 | `1` |
| slot 剩余工位 | `6` |
| slot 剩余设备 | `2` |

失败响应仍为唯一键异常：

```text
SQLIntegrityConstraintViolationException: Duplicate entry 'P2FIX-1783336376707-IDEMP-A'
for key 'competition_scene_resource_reservation.uk_scene_resource_idempotency_key'
```

容量只扣减一次，未重复扣减；但并发重复提交没有返回同一预约结果，仍返回 SQL `500`。

## 4. 共享 FULL 状态复测

结果：通过。

| 场景 | 结果 |
| --- | --- |
| 共享 slot 剩余工位从 `2` 扣到 `0` | `slot_status=FULL` |
| 共享 slot 剩余工位仍为 `2` | `slot_status=OPEN` |
| 共享模式 `remaining_device_count=0` 但工位仍可用 | 保持 `OPEN` |
| 共享预约扣减 | 只扣 `remaining_workstation_count`，不扣 `remaining_device_count` |

## 5. 非共享 FULL 状态复测

结果：通过。

| 场景 | 结果 |
| --- | --- |
| 扣减后剩 `1` 台 / `4` 工位 | 保持 `OPEN` |
| 扣减后 `remaining_device_count=0` | `slot_status=FULL` |
| 扣减后 `remaining_workstation_count=0` | `slot_status=FULL` |

未再出现“仍有设备和工位却提前 FULL”的问题。

## 6. 取消回补复测

结果：通过。

| 场景 | 结果 |
| --- | --- |
| 有效预约取消 | 成功 |
| 取消后 active key | 已置空 |
| 取消状态 | `CANCELLED` |
| 共享取消回补 | 只回补工位，不变更设备 |
| 非共享取消回补 | 回补设备和工位 |
| 重复取消 | 不重复回补 |
| `FULL` 且容量恢复、slot 未开始 | 恢复 `OPEN` |
| slot 已开始后取消 | 返回 `RESERVATION_NOT_CANCELABLE` |
| `CLOSED` slot 取消后 | 保持 `CLOSED` |

## 7. 关键回归结果

结果：通过。

| 回归项 | 结果 |
| --- | --- |
| 可预约资源列表按 resource schedule scope 过滤 | 通过 |
| slot 列表按 group scope 过滤 | 通过 |
| group 不命中不可预约 | 返回 `SLOT_GROUP_DENIED` |
| slot 已开始不可预约 | 返回 `SLOT_NOT_OPEN` |
| slot CLOSED 不可预约 | 返回 `SLOT_NOT_OPEN` |
| 基础预约 | 成功 |
| 顺序同队重复预约 | 返回 `ALREADY_RESERVED`，包含 `existingReservation` |
| 我的预约 | 同队成员可看到本队预约 |

## 8. 失败项

1. 并发同队预约下，`uk_scene_resource_active_reservation_key` 唯一冲突仍返回 SQL `500`。
2. 并发相同 `idempotency_key` 提交下，`uk_scene_resource_idempotency_key` 唯一冲突仍返回 SQL `500`。

这两个失败项均未造成多条有效预约，也未造成容量重复扣减；但接口响应不符合第二包验收要求。

## 9. 修复项验证

已生效：

- slot `FULL` 判断已按共享/非共享分支正确执行。
- 取消回补已按预约快照执行。
- 取消状态已统一为 `CANCELLED`。
- 重复取消未重复回补。

未生效或不完整：

- 并发唯一键冲突仍未稳定转换为业务响应。
- 推断原因：唯一键冲突发生后，在当前事务内查询已有记录可能无法稳定读到并发成功请求的记录，或异常处理未覆盖该并发路径的实际异常传播时机；需要在下一轮修复中通过事务外/新事务读取、短暂重试读取已有预约，或调整插入冲突处理流程来兜住并发唯一冲突。

## 10. 收尾状态

- 复测结束后已将 `P2IT_` 有效预约重置为 `CANCELLED`。
- `P2IT_` 有效预约数：`0`。
- P2IT slot 未出现负容量：`0` 条。
- 未记录登录 token、数据库密码、真实手机号或身份证号。

## 11. 最终结论

第二包修复后联调复测未通过。

暂不建议进入第三包管理端配置和用户端页面改造；需先补充修复并重新复测：

- 并发 `active_reservation_key` 冲突返回 `ALREADY_RESERVED` 和已有预约信息；
- 并发 `idempotency_key` 冲突返回原预约结果；
- 两类唯一冲突均不得向前端透出 SQL `500`。
