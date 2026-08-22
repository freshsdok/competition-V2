# 资源预约第二包并发唯一键冲突修复包 P2-FIX2 结果

执行时间：2026-07-06  
范围：仅修复并发 `active_reservation_key` / `idempotency_key` 唯一键冲突仍返回 SQL 500 的问题。未开发第三包页面，未修改扫码，未接入一证多权，未连接生产数据库。

## 1. 当前并发异常链路审计

- 预约提交入口：`UserCompetitionSceneResourceServiceImpl.submitReservation(...)`。
- reservation 插入方法：`CompetitionSceneResourceReservationMapper.insertCompetitionSceneResourceReservation(...)`。
- 事务边界：`submitReservation` 使用 `@Transactional(rollbackFor = Exception.class)`，插入 reservation 和 slot 容量扣减处于同一事务，保证成功预约与容量扣减一致回滚。
- P2 fix 前处理方式：捕获 `RuntimeException` 后直接在当前事务内查询 idempotency 或 active key。
- 失败原因：
  - 并发请求 A 插入成功后提交，B 在唯一键检查处收到 duplicate。
  - B 的事务在 duplicate 前已经做过多次 SELECT；MySQL `REPEATABLE READ` 下当前事务快照可能看不到 A 刚提交的 reservation。
  - 原逻辑在同一事务中查询已有预约，可能查不到，最终把 duplicate 重新抛出，前端收到 SQL 500。
  - MyBatis 异常消息还会包含完整 SQL 字段列表，不能仅凭 `active_reservation_key` / `idempotency_key` 字段名判断冲突类型，否则会误判。

已确认需识别的异常链包括：

- `DuplicateKeyException`
- `DataIntegrityViolationException`
- `MyBatisSystemException`
- `PersistenceException`
- `SQLIntegrityConstraintViolationException`
- `UndeclaredThrowableException`
- `TransactionSystemException`

## 2. 修改文件清单

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/contant/CompetitionSceneResourceConstants.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceReservationBaseMethodTest.java`

## 3. active_reservation_key 冲突处理

新增封装：

- `tryInsertReservationRecord(...)`
- `resolveDuplicateActiveReservationKey(...)`
- `findReservationByActiveKeyWithRetry(...)`

处理规则：

- 捕获 `uk_scene_resource_active_reservation_key` 冲突后，不再继续扣减容量。
- 使用新事务读取已有有效预约。
- 最多重试 3 次，每次间隔 50ms。
- 查询到已有预约后抛出业务异常 `ALREADY_RESERVED`，并携带 `existingReservation`。
- 3 次仍查不到时返回 `RESERVATION_CONFLICT_RETRY_LATER`，提示“预约冲突，请稍后刷新查看”。
- 非 active/idempotency 唯一键异常仍按系统异常抛出。

## 4. idempotency_key 冲突处理

新增封装：

- `resolveDuplicateIdempotencyKey(...)`
- `findReservationByIdempotencyKeyWithRetry(...)`

处理规则：

- 捕获 `uk_scene_resource_idempotency_key` 冲突后，不再继续扣减容量。
- 使用新事务按 idempotency key 查询原预约结果。
- 最多重试 3 次，每次间隔 50ms。
- 查询到后直接返回原预约 VO。
- 3 次仍查不到时返回 `IDEMPOTENCY_CONFLICT_RETRY_LATER`，提示“请求处理中，请稍后刷新查看”。

## 5. 新事务 / 重试查询策略

新增 `PlatformTransactionManager` 注入，并通过 `TransactionTemplate` 执行冲突后的查询：

- propagation：`PROPAGATION_REQUIRES_NEW`
- readOnly：`true`

这样 duplicate 发生后的查询不会复用原预约事务的旧快照，也避免在可能污染的事务中继续做复杂查询。单元测试中无 Spring 事务管理器时会走直接查询分支，正式运行态会使用 Spring 注入的事务管理器。

预约成功路径没有拆分事务：reservation 插入成功后仍在同一事务内执行 slot 容量原子扣减，容量扣减失败仍会回滚预约记录。

## 6. 异常识别规则

新增唯一键冲突分类：

- `ReservationUniqueConflictType.ACTIVE_RESERVATION_KEY`
- `ReservationUniqueConflictType.IDEMPOTENCY_KEY`
- `ReservationUniqueConflictType.NONE`

识别策略：

- 先确认异常链属于受支持的数据库/事务异常。
- 再确认文本包含 duplicate / unique / constraint 或目标唯一索引名。
- 冲突类型优先根据完整唯一索引名判断：
  - `uk_scene_resource_active_reservation_key`
  - `uk_scene_resource_idempotency_key`
- 若索引名不可用，再根据 `Duplicate entry '<activeKey/idempotencyKey>'` 的具体值判断。
- 不使用裸字段名 `active_reservation_key` / `idempotency_key` 作为冲突类型依据，避免 MyBatis SQL 字段列表造成误判。
- 其他 SQL 异常不转换为 `ALREADY_RESERVED` 或幂等成功。

## 7. 单元测试结果

已执行：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=UserCompetitionSceneResourceServiceImplTest,UserCompetitionSceneResourceReservationBaseMethodTest test
```

结果：`35` 个测试通过，`0` 失败，`0` 错误。

已执行扩展资源相关测试：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=UserCompetitionSceneResourceServiceImplTest,UserCompetitionSceneResourceReservationBaseMethodTest,CompetitionSceneResourceScheduleScopeServiceImplTest,CompetitionSceneResourceSlotGroupScopeServiceImplTest test
```

结果：`42` 个测试通过，`0` 失败，`0` 错误。

覆盖点：

- active key duplicate 后重试读取并返回 `ALREADY_RESERVED`；
- active key duplicate 后 3 次读不到返回 `RESERVATION_CONFLICT_RETRY_LATER`；
- idempotency duplicate 后重试读取并返回原预约；
- idempotency duplicate 后 3 次读不到返回 `IDEMPOTENCY_CONFLICT_RETRY_LATER`；
- MyBatis SQL 同时含两个字段时仍按唯一索引名分类；
- 非目标 SQL 异常不被误吞；
- 基础预约、顺序重复、顺序幂等、共享/非共享容量、FULL 状态、取消回补保持通过。

## 8. 构建结果

已执行：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：`BUILD SUCCESS`。

Maven 仍有既有 warning：

- 部分模块存在重复 dependency declaration；
- Java 17 编译提示 system modules path；
- 部分旧代码存在 deprecated / unchecked warning。

这些 warning 非本次 P2-FIX2 引入。

## 9. 是否可以重启 9205 并再次执行 P2 fix2 联调复测

可以。

建议下一步：

1. 重启正式测试用 `9205` competition，使其加载 P2-FIX2 class；
2. 复测同队并发预约：
   - 一个请求成功；
   - 另一个返回 `ALREADY_RESERVED`；
   - 包含 `existingReservation`；
   - 不返回 SQL 500；
   - 容量只扣一次。
3. 复测同 idempotency key 并发提交：
   - 只生成一条预约；
   - 其他请求返回原预约结果；
   - 不返回 SQL 500；
   - 容量只扣一次。
4. 回归 FULL 状态、取消回补、资源范围、slot 组别、基础预约和我的预约。
