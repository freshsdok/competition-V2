# 现场证件一证多权阶段 2 严格补强结果

执行时间：2026-07-05

本轮只补强阶段 2 旁路发证可靠性：未进入阶段 3 旁路扫码，未替换旧发证，未替换旧扫码，未修改 PC、小程序或资源预约主流程，未连接生产数据库。

## 1. 文件版本确认

基于以下阶段 2 文件继续补强：

1. `SCENE_CREDENTIAL_ONE_CARD_PHASE2_ISSUE_AUDIT.md`
2. `SCENE_CREDENTIAL_ONE_CARD_PHASE2_ISSUE_DEV_RESULT.md`
3. `CompetitionSceneOneCardIssueController.java`
4. `CompetitionSceneOneCardIssueReq.java`
5. `CompetitionSceneOneCardIssueResult.java`
6. `ICompetitionSceneOneCardIssueService.java`
7. `CompetitionSceneOneCardIssueServiceImpl.java`
8. `CompetitionSceneCredentialMapper.java`
9. `CompetitionSceneCredentialMapper.xml`
10. `CompetitionSceneCredentialScopeGrantMapper.xml`
11. grant domain / mapper / service 基础文件

## 2. 修改文件清单

新增：

```text
db/migration/20260705_competition_scene_credential_active_core_key_p2.sql
docs/competition-scene/SCENE_CREDENTIAL_ONE_CARD_PHASE2_STRICT_REVIEW_RESULT.md
```

修订：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneCredential.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardIssueController.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneOneCardIssueServiceImpl.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImpl.java
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialMapper.java
old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml
old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneOneCardIssueServiceImplTest.java
old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImplTest.java
```

## 3. Migration 修订

新增 migration：

```text
db/migration/20260705_competition_scene_credential_active_core_key_p2.sql
```

内容：

1. 为 `competition_scene_credential` 新增：

```sql
active_core_credential_key varchar(255) null
```

2. 建立唯一索引：

```sql
uk_scene_credential_active_core_key(active_core_credential_key)
```

3. migration 只执行 `ALTER TABLE` 加列和加唯一索引，不包含 `UPDATE`、`DELETE` 或历史数据清理。

本地测试库已执行该 migration，验证列和唯一索引存在。

## 4. active_core_credential_key 设计

写入条件：

```text
scope_type = COMPETITION
credential_status = EFFECTIVE
del_flag = 0
competition_series_id 非空
subject_type 非空
subject_code 非空
credential_type 非空
```

格式：

```text
competition_series_id:subject_type:subject_code:credential_type
```

示例：

```text
1:USER:1353:PARTICIPANT
```

撤销、删除、失效时处理：

1. `deleteCompetitionSceneCredentialByIds` 置空 `active_core_credential_key`。
2. `deleteCompetitionSceneCredentialByTargetIds` 置空 `active_core_credential_key`。
3. `revokeCompetitionSceneCredentialByTargetId` 置空 `active_core_credential_key`。
4. 通用 `updateCompetitionSceneCredential` 中，如果 `credentialStatus != EFFECTIVE`，置空 `active_core_credential_key`。

唯一键允许多条 `NULL`，因此不影响非核心 credential、旧 SCHEDULE credential 或未写 key 的历史测试数据。

## 5. 核心 Credential 唯一算法

阶段 2 旁路发证继续严格使用：

```text
competition_series_id
+ subject_type
+ subject_code
+ credential_type
+ credential_status = EFFECTIVE
+ del_flag = 0
```

Mapper 方法：

```text
selectEffectiveCompetitionScopeCredentialStrict(...)
```

旁路发证禁止使用：

```text
selectEffectiveCompetitionScopeCredentialByUserId(...)
```

单元测试已验证旁路 service 不调用 userId 宽查询。

## 6. DuplicateKeyException Fallback

修订 `CompetitionSceneOneCardIssueServiceImpl`：

1. 新建核心 credential 前先严格查询。
2. 新建核心 credential 时写入 `active_core_credential_key`。
3. 插入时如果遇到 `DuplicateKeyException`：
   - 立即按严格 key 回查已有核心 credential；
   - 如果回查成功，返回该 credential；
   - `reusedCredential=true`；
   - 继续写入或复用 grant；
   - 不直接抛 500。
4. 如果冲突不是核心唯一键，且严格回查为空，则刷新 `credential_no` 和 token 后重试。

单实例 `synchronized` 保留，但数据库唯一键才是并发兜底。

## 7. Mapper XML 审计结果

审计文件：

```text
CompetitionSceneCredentialMapper.xml
CompetitionSceneCredentialScopeGrantMapper.xml
```

结果：

1. 核心 credential strict 查询包含 `credential_type`、`credential_status='EFFECTIVE'`、`del_flag='0'`。
2. 阶段 2 旁路 service 未使用 `selectEffectiveCompetitionScopeCredentialByUserId`。
3. grant active 查询均包含 `grant_status='ACTIVE'` 和 `deleted=0`。
4. `revokeGrantsByTarget` 限定 `source_type='SCHEDULE_TARGET'`。
5. update 类 SQL 均更新 `update_time`。
6. 撤销/删除 credential 时会置空 `active_core_credential_key`。
7. SQL 使用 `#{}` 参数绑定，没有 `${}` 字符串拼接。
8. `selectCompetitionSceneCredentialByNo` 不加 `del_flag=0` 是为了证件号冲突检测，避免复用历史编号，不作为有效证件查询。
9. 旁路接口返回 `CompetitionSceneOneCardIssueResult`，不返回 mapper 查询出的 credential 全对象，因此不暴露 token、二维码明文、手机号或身份证字段。

XML 校验：

```bash
xmllint --noout old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialScopeGrantMapper.xml
```

结果：通过。

## 8. Controller 参数校验结果

修订：

```text
CompetitionSceneOneCardIssueController.issueByTarget
```

新增校验：

1. `req == null` 返回 `发证参数不能为空`。
2. `scheduleId == null` 返回 `赛场安排ID不能为空`。
3. `targetId == null` 返回 `赛场对象ID不能为空`。
4. 不允许 null 参数进入 `issueOneCardByScheduleTarget`。

路径保留：

```java
@RequestMapping({"/sceneOneCardIssue", "/competition/sceneOneCardIssue"})
```

保留原因：当前同模块 `CompetitionSceneVerifyController`、`CompetitionSceneCredentialController` 均使用双路径兼容网关和直接模块路径。阶段 2 测试接口跟随该规范，避免环境路由差异导致测试入口不可用。

## 9. DML Smoke Test 结果

测试库：

```text
本地 dev-mysql57 / jiaoxue_test
```

说明：

1. 未连接生产库。
2. 测试 1-5 和测试 8 使用高位测试 ID，在事务内执行并 `ROLLBACK`，未保留测试记录。
3. 已对本地测试库执行新增 migration。

结果：

```text
test1_first_issue
core_count=1
active_grant_count=1
schedule_credential_count=0
active_core_key_nonnull=1
active_grant_key_nonnull=1

test2_repeat_same_target
core_count=1
active_grant_count=1
reused_credential=1
reused_grant=1

test3_same_subject_second_schedule
core_count=1
active_grant_count=2
distinct_schedule_count=2

test4_same_user_different_type
core_count=2
different_credential=1
different_active_core_key=1

test5_different_subject_code
core_count=2
different_credential=1

test8_sensitive_snapshot
sensitive_hits_in_grant_snapshot=0
sensitive_hits_in_credential_snapshot=0
```

结论：

1. 同一 target 首次发证生成 1 条核心 credential 和 1 条 ACTIVE SCHEDULE grant。
2. 同一 target 重复发证不增加 credential，不增加 grant。
3. 同一主体第二个 schedule target 复用 credential，新增第二条 grant。
4. 同一 userId 不同 credentialType 不误复用。
5. 不同 subjectCode 不误复用。
6. 不生成 SCHEDULE credential。
7. snapshot 未命中手机号、邮箱、身份证、token、二维码字段。

## 10. 并发测试结果

测试库：

```text
本地 dev-mysql57 / jiaoxue_test
```

并发方式：

1. 使用同一 `active_core_credential_key` 发起 10 个并发插入。
2. 插入后查询最终核心 credential 数量。
3. 清理本组高位测试 key 生成的本地测试行。

结果：

```text
test6_concurrent_insert_unique_key
insert_success_count=1
duplicate_key_count=9
final_core_count=1
```

结论：

1. 数据库唯一键能够阻止并发重复核心 credential。
2. 服务层 `DuplicateKeyException fallback` 已通过单元测试验证，遇到唯一键冲突后回查并返回复用，不直接 500。

## 11. 单元测试结果

测试命令：

```bash
mvn -pl teaching-modules/teaching-competition -Dtest=CompetitionSceneOneCardIssueServiceImplTest,CompetitionSceneCredentialScopeGrantServiceImplTest test
```

结果：

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

覆盖：

1. 首次发证。
2. 重复发证。
3. 多赛场复用 credential。
4. 不同 credentialType 不复用。
5. 不同 subjectCode 不复用。
6. `DuplicateKeyException fallback`。
7. `active_core_credential_key` 生成。
8. 不调用 `selectEffectiveCompetitionScopeCredentialByUserId`。
9. grant `identityVerify` / `scheduleEntry` 能力保守解析。

## 12. 构建结果

构建命令：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

构建仅存在项目既有 Maven warning，例如重复依赖声明和 Lombok superclass warning；本轮未新增编译错误。

## 13. 是否可以进入阶段 3 旁路扫码

阶段 3 准入项检查：

1. `active_core_credential_key` 唯一约束已建立：通过。
2. 同一 target 重复发证不重复生成 credential：通过。
3. 同一 target 重复发证不重复生成 grant：通过。
4. 同一主体多赛场复用 credential：通过。
5. 同一 userId 不同 credentialType 不误复用：通过。
6. 并发重复发证不产生重复 credential：通过。
7. `DuplicateKeyException fallback` 正常：通过。
8. 接口不返回敏感信息：通过。
9. 单元测试通过：通过。
10. 后端 compile 通过：通过。

结论：可以进入阶段 3 旁路扫码设计和开发准备。

注意：本轮没有进入阶段 3，也没有替换旧发证、旧扫码、PC、小程序或资源预约主流程。
