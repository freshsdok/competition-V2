# 现场证件一证多权工程经验文档

日期：2026-08-11

## 1. 文档定位

本文档沉淀本轮“现场证件一证多权”开发过程中的核心理念、关键工程环节、数据库设计、流程设计和注意事项。

当前代码事实：

1. 新增的一证多权能力仍是旁路能力；
2. 旧发证、旧扫码、PC、小程序主流程尚未替换；
3. 当前整体状态是“旧双证实体流程 + 新一证多权旁路流程”并存；
4. 本文档描述的是长期目标模型和已验证的旁路实现经验，不代表旧主流程已完成替换。

## 2. 核心理念

一证多权的核心不是“把所有能力都塞进一张证”，而是把三个概念拆开：

1. 证件识别人；
2. 授权决定能做什么；
3. operation_state 记录做没做过。

旧模型容易混淆“证件实体”和“赛场权限”。现场赛场发证时，如果同时生成大赛级证件和赛场级证件，用户看到的是多张证，系统维护的是多份快照，后续删除 target、修改人员、扫码展示都会出现不一致。

一证多权模型把核心证件定位为主体身份凭证，把赛场相关能力放入 grant。这样同一个主体在一场大赛中原则上只维护一张核心现场证件，不同赛场、不同入口、不同动作通过显式授权扩展。

## 3. 三层职责划分

### 3.1 credential：核心身份凭证

`competition_scene_credential` 在一证多权模型中的主职责：

1. 标识主体身份；
2. 承载二维码 token；
3. 承载大赛级基础能力；
4. 提供 PC、小程序展示所需的主证件信息；
5. 作为 grant 和 operation_state 的核心关联点。

核心证件唯一规则：

```text
competition_series_id
+ subject_type
+ subject_code
+ credential_type
```

禁止只按 `competitionSeriesId + userId` 复用核心证件。原因是同一个 userId 可能在同一赛事中存在不同主体身份或不同证件类型，例如参赛者、工作人员、专家等。

### 3.2 grant：作用域授权

`competition_scene_credential_scope_grant` 负责表达“这张核心证件在某个作用域里拥有哪些能力”。

典型 SCHEDULE grant 表达：

```text
credential_id = 核心证件
scope_type = SCHEDULE
scope_ref_id = schedule_id
source_type = SCHEDULE_TARGET
source_schedule_id = schedule_id
source_target_id = target_id
ability_json = waiting / scheduleEntry / resourceReservation 等赛场能力
```

grant 幂等规则：

```text
credential_id
+ scope_type = SCHEDULE
+ scope_ref_id = scheduleId
+ source_target_id
+ grant_status = ACTIVE
+ deleted = 0
```

工程上使用 `active_grant_key` 唯一约束兜住并发重复插入风险。仅靠 `SELECT FOR UPDATE` 在记录不存在时不能防止多实例并发重复插入。

### 3.3 operation_state：动作事实源

`competition_scene_subject_operation_state` 记录某个主体在某个作用域下某个动作是否已完成。

推荐语义：

1. 报道：`COMPETITION + REPORT`
2. 资料领取：`COMPETITION + USER + MATERIAL`
3. 候场：`SCHEDULE + WAITING`
4. 资源预约：后续按资源模型补充作用域，不在本轮实现范围

operation_state 是“是否完成”的事实源，不应由 credential 的快照字段承担事实源职责。credential 上的状态字段可以作为展示缓存或兼容字段，但长期应从 operation_state 回填。

## 4. 数据库设计经验

### 4.1 核心证件唯一键

新增字段：

```sql
active_core_credential_key varchar(255)
```

当 credential 同时满足：

```text
scope_type = COMPETITION
credential_status = EFFECTIVE
del_flag = 0
competition_series_id 非空
subject_type 非空
subject_code 非空
credential_type 非空
```

写入：

```text
competitionSeriesId:subjectType:subjectCode:credentialType
```

并建立唯一索引：

```sql
uk_scene_credential_active_core_key(active_core_credential_key)
```

失效、撤销、删除时必须置空，避免历史无效证件占用唯一键。

### 4.2 grant 表设计

新增表：

```text
competition_scene_credential_scope_grant
```

关键字段：

1. `grant_id`：授权主键；
2. `credential_id`：核心证件 ID；
3. `competition_series_id`：赛事 ID；
4. `scope_type`：授权作用域；
5. `scope_ref_id`：作用域引用 ID，当前约定为内部数值 ID；
6. `active_grant_key`：ACTIVE 授权唯一键；
7. `source_type`：授权来源；
8. `source_schedule_id` / `source_target_id`：赛场来源；
9. `credential_type` / `role_code`：授权身份信息；
10. `subject_type` / `subject_code`：授权主体；
11. `ability_json`：授权能力；
12. `valid_from` / `valid_to`：授权有效期；
13. `operation_window_json`：动作窗口；
14. `grant_status`：ACTIVE / REVOKED / EXPIRED；
15. `grant_snapshot_json`：授权快照；
16. `deleted`：逻辑删除。

核心索引：

```text
uk_grant_active_key(active_grant_key)
idx_grant_active_lookup(credential_id, scope_type, scope_ref_id, source_target_id, grant_status, deleted)
idx_grant_credential(credential_id, deleted)
idx_grant_competition_scope(competition_series_id, scope_type, scope_ref_id, deleted)
idx_grant_subject(competition_series_id, subject_type, subject_code, credential_type, deleted)
```

### 4.3 快照字段白名单

grant snapshot 和 log payload 都必须避免直接序列化完整 target 或 credential。

允许字段示例：

```text
scheduleId, scheduleName, targetId, targetName, roleCode,
credentialType, teamCode, subjectType, subjectCode, groupCode, groupName
```

禁止字段：

```text
phone, email, idCard, credentialToken, qrContent,
credentialFileUrl, credentialImageUrl, openId, unionId
```

## 5. 发证流程设计

一证多权旁路发证流程：

```text
读取 schedule_target
  -> 读取 schedule
  -> 解析 competitionSeriesId
  -> 解析 subjectType / subjectCode / credentialType / roleCode
  -> 按唯一规则查找核心 COMPETITION credential
  -> 不存在则创建核心 credential
  -> 存在则复用核心 credential
  -> ensureScheduleGrant
  -> 返回 credentialId + grantId + reusedCredential + reusedGrant
```

核心原则：

1. 通过赛场安排发证时，不再生成第二张 SCHEDULE 实体证件；
2. 赛场安排只给核心证件增加 SCHEDULE grant；
3. 大赛基础能力和赛场授权能力分开维护；
4. 同一个 target 重复发证不重复生成 credential，也不重复生成 grant；
5. 同一主体多个赛场复用同一核心 credential，生成多条 SCHEDULE grant；
6. 同一 userId 但不同 credentialType 不复用核心 credential。

## 6. 扫码矩阵流程设计

旁路扫码 preview 流程：

```text
解析 credentialToken / qrContent
  -> 查询核心 credential
  -> 校验核心 credential 有效
  -> 读取核心 ability_json
  -> 查询 ACTIVE grants
  -> 查询 operation_state
  -> 生成 competitionActions
  -> 生成 scheduleActionGroups
  -> 返回 allowedActions
```

核心 credential 校验必须包括：

1. `credentialId` 非空；
2. `delFlag` 正常；
3. `competitionSeriesId` 非空；
4. `subjectType` 非空；
5. `subjectCode` 非空；
6. `scope_type = COMPETITION`；
7. `credential_status = EFFECTIVE`；
8. 未过有效期。

### 6.1 报道

判定规则：

```text
credential.ability_json.report = true
+ 操作员具备报道/签到权限
```

状态写入：

```text
scope_type = COMPETITION
scope_ref_id = competition_series_id
operation_type = REPORT
```

### 6.2 资料领取

本人领取规则：

```text
credential.ability_json.material = true
+ 操作员具备资料领取权限
+ credential.subject_type = USER
```

状态写入：

```text
scope_type = COMPETITION
subject_type = USER
subject_code = userId
operation_type = MATERIAL
delegate_relation = SELF
```

同队代领规则：

1. 被领取人和代领人都必须是有效核心 credential；
2. 代领人 `subjectType` 必须为 `USER`；
3. 双方 `userId` 必须非空；
4. 双方 `teamCode` 必须非空且相同；
5. 代领人 credential 不能等于被领取人 credential；
6. 同一 userId 不允许伪装代领，必须走本人领取；
7. 跨队代领失败。

资料领取状态仍写在被领取人 USER 上，不写 TEAM + MATERIAL。

### 6.3 候场

候场不是大赛基础能力，必须由 SCHEDULE grant 授权。

判定规则：

```text
currentScheduleId 必填
+ 存在唯一 ACTIVE SCHEDULE grant
+ grant.scope_ref_id = currentScheduleId
+ grant.ability_json.waiting = true
+ 操作员具备签到/候场权限
+ 被扫对象角色为 MEMBER 或 CAPTAIN
```

默认不允许候场的角色：

```text
TEACHER, EXPERT, STAFF, UNKNOWN
```

同一 schedule 多 grant 时必须拒绝：

```text
当前赛场授权存在多条，请联系管理员处理
```

状态写入：

```text
scope_type = SCHEDULE
scope_ref_id = currentScheduleId
operation_type = WAITING
```

## 7. 为什么不能让大赛证天然拥有所有赛场权限

如果 COMPETITION credential 天然拥有所有 SCHEDULE 权限，会带来三个问题：

1. 授权边界失控：任何有大赛证的人都会被视为拥有所有赛场候场、入场、预约权限；
2. 数据无法解释：无法追踪某个赛场权限来自哪个 target、导入批次或人工授权；
3. 删除 target 失效：删除赛场匹配对象时无法准确撤销对应赛场权限。

正确做法：

```text
大赛基础能力由 credential 控制；
赛场能力由 SCHEDULE grant 显式授权；
完成状态由 operation_state 记录。
```

## 8. 删除和更新策略

删除 target 时，不应默认删除核心 credential。

原因：

1. 核心 credential 代表主体在大赛中的身份；
2. 同一 credential 可能同时拥有多个赛场 grant；
3. 该 credential 可能已有报道、资料领取等大赛级 operation_state；
4. 物理或逻辑删除 credential 容易破坏历史操作事实。

推荐策略：

```text
删除 target
  -> revoke 对应 source_type = SCHEDULE_TARGET 的 grant
  -> 不删除核心 credential
  -> 不删除 operation_state
  -> 必要时重新计算展示状态
```

更新 target 时：

1. 可刷新 grant 的 `ability_json`、`operation_window_json`、`grant_snapshot_json`；
2. 不应直接改写历史 operation_state；
3. credential 上的主体身份字段要谨慎同步，避免破坏二维码和历史归属。

## 9. operation_log 经验

所有成功操作必须写 `competition_scene_operation_log`。

阶段性经验：

1. operation_log 是审计流水，不是业务成功的唯一依据；
2. operation_state 写入成功后，日志写入失败不能把业务结果改成失败；
3. 日志失败应记录后台异常，接口可以返回“日志写入失败但业务已完成”；
4. 当前 operation_log 表无 grant_id 字段时，可在 remark 或安全 payload 里记录 grant 上下文；
5. 正式替换前建议增加结构化字段：`grant_id`、`grant_scope_type`、`grant_scope_ref_id`。

日志禁止记录：

```text
credentialToken, qrContent, phone, idCard, idCardSuffix,
openId, unionId, credentialFileUrl, credentialImageUrl
```

## 10. 状态一致性经验

confirm 的关键原则：

```text
状态写入前异常，可以返回失败或异常；
状态写入后，业务结果必须以状态写入为准；
日志失败、lastLogId 回填失败，不能反向污染业务结果。
```

并发幂等经验：

1. 先查 DONE 状态；
2. 再调用 `insertDoneOperationStateIfAbsent`；
3. 如果并发情况下返回已有 DONE，应返回 `DUPLICATE / alreadyDone=true`；
4. 当前旁路用无敏感 marker 区分“本次插入”和“已有状态”；
5. 正式替换前建议把 operation_state service 返回值改为显式 `created + state`。

## 11. 前端兼容经验

PC 和小程序已经依赖旧接口字段，不应在后端模型调整时强制前端大改。

过渡期建议：

1. 后端新增旁路接口先联调；
2. 旧接口保持不变；
3. 一证多权适配层负责把核心 credential + grants 折叠成旧前端可理解字段；
4. 多个 SCHEDULE grant 展示为多个赛场分组；
5. confirm 涉及 SCHEDULE 动作时必须显式传 `currentScheduleId`。

当前代码中，小程序仍调用旧 `/competition/sceneVerify/scan` 和 `/competition/sceneVerify/confirm`，尚未切换到旁路接口。

## 12. 安全边界

阶段 3 pilot confirm 当前只依赖 `credentialId + actionType`，这是受控测试接口，不可直接开放给普通前端。

正式接入前必须补齐：

1. 增加 scanTicket 或 credentialToken 二次校验；
2. 操作员角色不能由前端传入；
3. 操作员角色必须来自登录态、工作人员授权或操作员证件；
4. confirm 必须校验 preview 和 confirm 的上下文一致性；
5. 日志和响应继续保持脱敏。

## 13. 推荐落地顺序

建议分阶段替换，不建议直接把旧流程一次性改掉：

1. 测试库部署 grant 表和 active core key；
2. 使用旁路发证生成核心 credential + grant；
3. 使用旁路扫码验证报道、资料、自领、代领、候场、重复操作；
4. 补 scanTicket 和后端操作员角色解析；
5. 将 operation_state service 改为显式返回 `created + state`；
6. 做旧接口适配层，保持前端字段稳定；
7. 灰度切换小程序扫码；
8. 再评估是否停止旧 SCHEDULE 实体证件生成；
9. 最后治理历史测试数据。

## 14. 关键风险清单

1. 旧主流程仍会生成 SCHEDULE 实体证件，不能误认为生产已经是一证多权；
2. 核心证件唯一规则如果退回 `competitionSeriesId + userId`，会再次引入误复用；
3. grant 如果没有唯一约束，会在并发发证时产生重复授权；
4. 候场如果只看 credential ability，会把大赛证误当成全赛场通行证；
5. 资料代领如果不校验同队和本人伪装，会产生领取责任不清；
6. operation_log 如果记录 token、二维码、手机号、身份证，会形成安全风险；
7. confirm 如果状态写入后因日志异常返回失败，会造成用户重复操作和状态误判；
8. 前端切换前如果不做契约适配，会破坏 PC、小程序稳定页面。

## 15. 一句话结论

一证多权的工程核心是：

```text
credential 只负责识别主体；
grant 负责显式授权；
operation_state 负责记录动作事实；
operation_log 负责审计追踪；
前端契约通过适配层稳定过渡。
```

这套模型适合现场业务长期扩展，但必须以旁路验证、接口适配、安全补强和灰度切换的方式落地，不能直接用新模型假设覆盖旧主流程。
