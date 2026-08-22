# 资源预约第二包 P2-FIX2 运行态复测报告

复测时间：2026-07-06  
最终复测批次：`P2FIX2-1783338715819`  
结论：P2-FIX2 运行态复测通过。并发 `active_reservation_key` / `idempotency_key` 唯一键冲突均未再向前端透出 SQL 500 或 `Duplicate entry`，容量未超扣，关键回归通过。

## 1. 9205 Runtime 验证

- 正式测试用 `9205` 已重启并运行在 screen 会话 `p2fix2-9205`。
- Java PID：`45000`。
- Java 启动时间：`Mon Jul 6 19:51:40 2026`。
- P2-FIX2 class 编译时间：`2026-07-06 19:23:55 +0800`。
- gateway `9889`、auth `9224`、competition `9205` 均为 `UP`。
- 接口请求通过正式 gateway `9889` 调用，未用临时 competition 进程替代联调结论。

## 2. 同队并发预约复测结果

结果：通过。

验证场景：

- `P2IT_USER_A1`、`P2IT_USER_A2` 同队并发预约同一赛事下资源 slot `92052026070913`。
- 两个请求使用不同 `idempotency_key`。
- active key：`RESV:920520260706:TEAM:P2IT_TEAM_A`。

实际结果：

| 项目 | 实际 |
| --- | --- |
| 成功请求数 | `1` |
| `ALREADY_RESERVED` 请求数 | `1` |
| `existingReservation` | 已返回 |
| SQL 500 外泄 | `0` |
| Duplicate entry 外泄 | `0` |
| 有效预约数 | `1` |
| slot 剩余工位 | `6` |
| slot 剩余设备 | `2` |

接口响应摘要：

- 成功请求：`code=200`，`reservationId=48`。
- 并发冲突请求：`code=5008`，`errorCode=ALREADY_RESERVED`，`hasExistingReservation=true`。

active key 唯一约束仍然生效：最终 reservation 表只有一条有效预约，容量只扣一次。

## 3. idempotency_key 并发复测结果

结果：通过。

验证场景：

- 同一用户并发提交同一个 `idempotency_key`。
- slot：`92052026070901`。

实际结果：

| 项目 | 实际 |
| --- | --- |
| 成功响应数 | `2` |
| 返回 reservationId | 两个请求均为 `50` |
| 同 idempotency_key 预约记录数 | `1` |
| SQL 500 外泄 | `0` |
| Duplicate entry 外泄 | `0` |
| slot 剩余工位 | `6` |
| slot 剩余设备 | `2` |

幂等唯一约束仍然生效：最终只生成一条预约记录，容量只扣一次。

## 4. RETRY_LATER 分支验证情况

运行态未强制构造“唯一键冲突后 3 次仍查不到已有预约”的异常窗口，因为这需要人为制造提交/回滚或事务可见性极端时序，正式测试接口不适合为了覆盖该分支破坏正常流程。

该分支已在 P2-FIX2 单元测试中覆盖：

- active key 查不到时返回 `RESERVATION_CONFLICT_RETRY_LATER`；
- idempotency key 查不到时返回 `IDEMPOTENCY_CONFLICT_RETRY_LATER`；
- 均不返回 SQL 500。

## 5. 关键回归结果

结果：通过。

| 回归项 | 结果 |
| --- | --- |
| 基础预约成功 | `code=200` |
| 顺序同队重复预约 | `ALREADY_RESERVED`，包含 `existingReservation` |
| 顺序 idempotency_key 重复提交 | 返回同一预约结果 |
| 共享占用扣减 | 只扣工位，不扣设备 |
| 非共享占用扣减 | 扣设备和整台设备工位 |
| 共享 FULL 状态 | 工位扣到 `0` 后 `FULL`；工位仍可用时 `OPEN` |
| 非共享 FULL 状态 | 设备扣到 `0` 后 `FULL`；设备和工位仍可用时 `OPEN` |
| 取消预约回补 | 按预约快照回补 |
| 重复取消 | 不重复回补 |
| CLOSED slot 取消后 | 保持 `CLOSED`，不恢复 `OPEN` |
| 资源范围过滤 | 允许赛场返回 2 个资源，不允许赛场返回 0 个资源 |
| slot 组别过滤 | A 组可见 A 组 slot、不可见 B 组 slot；B 组可见 B 组 slot |
| 我的预约同队可见 | 队员 B 可见队员 A 预约 |

## 6. SQL 500 / Duplicate entry 外泄

- 接口响应 SQL 500 外泄：无。
- 接口响应 `Duplicate entry` 外泄：无。
- 接口响应 SQL 字段列表外泄：无。

说明：服务端 Druid 日志仍会记录数据库唯一键冲突堆栈，这是数据库层诊断日志；本轮验收口径为不得向前端/API 响应外泄 SQL 500、`Duplicate entry` 或 SQL 字段列表，该口径已通过。

## 7. 失败项

无。

## 8. 修复项

已验证 P2-FIX2 修复生效：

1. 并发 active key 唯一冲突转换为 `ALREADY_RESERVED`；
2. 并发 active key 冲突返回 `existingReservation`；
3. 并发 idempotency key 唯一冲突返回原预约结果；
4. 两类冲突均不再向前端透出 SQL 500；
5. 两类冲突均不继续扣减容量；
6. reservation 表无重复有效记录；
7. FULL 状态、取消回补、资源范围、slot 组别、我的预约均无回退。

## 9. 收尾状态

- 复测结束后已将 `P2IT_` 有效预约重置为 `CANCELLED`。
- `P2IT_` 有效预约数：`0`。
- P2IT slot 未出现负容量：`0` 条。
- 未记录登录 token、数据库密码、真实手机号或身份证号。

## 10. 最终结论

P2 第二包最终通过。

可以进入第三包：管理端配置和用户端页面改造。
