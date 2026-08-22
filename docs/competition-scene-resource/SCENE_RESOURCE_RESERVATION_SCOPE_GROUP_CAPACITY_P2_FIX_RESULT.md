# 资源预约第二包后端修复包结果

生成时间：2026-07-06

## 1. 修复文件清单

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImplTest.java`
- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_SCOPE_GROUP_CAPACITY_P2_INTEGRATION_RESULT_RERUN.md`
- `docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_SCOPE_GROUP_CAPACITY_MODEL_DESIGN.md`

本轮未修改管理端页面、PC/小程序页面、扫码代码，未接入一证多权，未接入 credential grant，未连接生产数据库。

## 2. active key 并发冲突修复

预约插入 `reservation` 时，`insertCompetitionSceneResourceReservation` 外层改为捕获 `RuntimeException` 并只对预约唯一键冲突做业务分流。

已覆盖异常来源：

- `DuplicateKeyException`，通过 `DataIntegrityViolationException` 父类覆盖；
- `DataIntegrityViolationException`；
- `MyBatisSystemException`；
- `PersistenceException`。

冲突识别会递归读取异常链消息，命中 `uk_scene_resource_active_reservation_key`、`active_reservation_key`、`RESV:{competition_series_id}:{subject_type}:{subject_code}` 或 duplicate/unique/constraint 语义后处理。

处理结果：

- 如果确认或兜底查到 `active_reservation_key` 已存在有效预约，查询 `selectEffectiveReservationByActiveKey`；
- 返回业务码 `ALREADY_RESERVED`；
- 通过业务异常携带 `existingReservation`；
- 不继续执行 slot 容量扣减；
- 其他数据库异常仍按系统异常抛出。

## 3. idempotency key 冲突处理

同一 `idempotency_key` 的唯一键冲突会优先查询 `selectReservationByIdempotencyKey`。

处理结果：

- 查到原预约时直接返回原预约结果；
- 不新增预约；
- 不扣减容量；
- 不返回 SQL 500。

## 4. slot FULL 状态计算修复

已将 slot 容量扣减 SQL 改为只负责原子扣减容量，不再在同一个 `UPDATE SET` 子句中通过 `CASE` 计算 `slot_status`，避免 MySQL 字段赋值顺序造成误判。

新流程：

1. 条件 `UPDATE` 原子扣减容量；
2. 影响行数大于 0 后重新查询 slot 最新容量；
3. service 按最新容量判断是否刷新 `FULL`；
4. 使用 `updateCompetitionSceneResourceSlotStatusIfCurrent` 做条件状态更新，避免覆盖管理员已改成 `CLOSED` 的状态。

共享占用判断：

- `remaining_workstation_count <= 0` 才置为 `FULL`；
- `remaining_workstation_count > 0` 保持 `OPEN`；
- 不看 `remaining_device_count`。

非共享占用判断：

- `remaining_device_count <= 0` 或 `remaining_workstation_count <= 0` 时置为 `FULL`；
- 两者都大于 0 时保持 `OPEN`。

取消回补判断：

- 回补 SQL 只恢复容量，不直接改 `slot_status`；
- 回补后重新查询 slot；
- 仅当当前状态仍为 `FULL`、slot 未开始且容量恢复时，条件恢复为 `OPEN`；
- `CLOSED` 不会被自动恢复为 `OPEN`。

## 5. CANCELLED 枚举统一

代码现有取消状态为 `CANCELLED`：

- `CompetitionSceneResourceConstants.RESERVATION_STATUS_CANCELLED = "CANCELLED"`；
- `CompetitionSceneResourceReservationMapper.xml` 取消 SQL 写入 `reservation_status = 'CANCELLED'`。

本轮确认代码不使用单 L 取消状态拼写，并同步修订相关文档表述。对外文档以 `CANCELLED` 为准。

## 6. 单元测试结果

已补充/覆盖：

- active key 包装唯一键异常返回 `ALREADY_RESERVED`；
- idempotency key 包装唯一键异常返回原预约；
- 唯一键冲突后不扣容量；
- 共享 slot 工位耗尽置 `FULL`；
- 共享 slot 工位未耗尽时保持 `OPEN`，即使设备数为 0；
- 非共享 slot 设备/工位均未耗尽时保持 `OPEN`；
- 非共享 slot 设备耗尽置 `FULL`；
- 非共享 slot 工位耗尽置 `FULL`；
- 取消后 `FULL` 且容量恢复时恢复 `OPEN`；
- `CLOSED` slot 取消回补后不自动恢复 `OPEN`；
- 取消状态常量为 `CANCELLED`；
- 重复取消不重复回补的既有用例继续通过。

执行命令：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=UserCompetitionSceneResourceServiceImplTest,UserCompetitionSceneResourceReservationBaseMethodTest,CompetitionSceneResourceScheduleScopeServiceImplTest,CompetitionSceneResourceSlotGroupScopeServiceImplTest test
```

结果：

- Tests run: 36
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

## 7. 构建结果

执行命令：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

- Reactor 14 个模块编译成功；
- `teaching-modules-competition` 编译成功；
- BUILD SUCCESS。

构建中仍存在仓库原有 Maven warning，包括部分模块重复 dependency 声明、Lombok equals/hashCode warning、deprecated/unchecked warning；本轮未新增失败项。

## 8. 是否可以重新执行 P2 联调复测

可以重新执行 P2 联调复测。

复测前建议：

- 重启正式测试用 9205 competition，使其加载本次编译后的 class；
- 继续使用已验证通过的 migration 和 `P2IT_` 测试数据口径；
- 重点复测同队并发预约、同 idempotency key 并发提交、多队共享/非共享抢容量、slot `FULL/OPEN/CLOSED` 状态和重复取消。

预期：

- 同队并发最终只有一条有效预约；
- 失败请求返回 `ALREADY_RESERVED` 和已有预约信息；
- 不再返回 SQL 500；
- 容量不重复扣减；
- 共享/非共享 `FULL` 状态与剩余容量一致；
- 取消按快照回补，重复取消不重复回补。
