# 提供、撤销、重新提供及最终确认：源码核验与最小读回

本补充最初由源码及前28条事件编制，现已按第1—41条浏览器事件、实际网络元数据和最终只读回执增量同步。编制者未运行数据库、未操作UI、未修改业务源码。用户明确授权向XINKESAI提供后，主执行者已实际提供、撤销、重新提供并最终人工确认。原28条及其“当时未提供/未完成”的历史证据保留；成功证据来自实际执行记录，而不是下面的源码推导。

## 关键事实与字段来源

| 对象 | 核实结果 | 当前源码依据 |
|---|---|---|
| 资料当前指针 | `settlement_profile_current`以`subject_id`为主键；`current_profile_version_id`是版本记录ID，`version`是当前指针并发版本 | [V2迁移](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/database/migration/V2_42_300__pilot_commerce_settlement_profile.sql:22)，[指针更新](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/infrastructure/persistence/JdbcSettlementProfileStore.java:87) |
| 资料版本记录 | `payout_profile_version.id`、`profile_subject_id`、`version_no`；当前独立本人资料的`settlement_case_id`为空。仅选这些技术字段，避免读取保护值、指纹或姓名 | [原版本表](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/database/migration/V2_41_900__payout_decision_and_settlement.sql:182)，[本人所有权迁移](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/database/migration/V2_42_300__pilot_commerce_settlement_profile.sql:12) |
| 单位访问关系 | `settlement_profile_tenant_access`复合主键`(subject_id,tenant_id)`；状态约束仅`PROVIDED`、`REVOKED`；`version`默认0；含创建/更新时间及actor Subject ID | [访问表DDL](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/database/migration/V2_42_300__pilot_commerce_settlement_profile.sql:35) |
| 单位身份 | `tenant.id`、`tenant_code`、`lifecycle_state`、`version`。以真实目录选择ID和代码核对，不猜测XINKESAI数字ID | [目录读取](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-core/src/main/java/cn/deshi/platform/core/infrastructure/persistence/JdbcPlatformCoreStore.java:108)，[生命周期约束](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/database/migration/V2_42_002__tenant_management_lifecycle.sql:1) |
| 提供/撤销命令 | 同一个POST `/api/v1/settlement-profiles/me/tenant-access`，body含`tenantId`、访问关系`version`及`provided`布尔值；请求version可为-1 | [Controller](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/api/SettlementProfileController.java:66)，[请求字段](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/api/SettlementProfileController.java:126) |
| 版本及幂等 | 首次无关系时expected=-1走INSERT，表默认访问version=0；已有关系用实际expected条件UPDATE，version+1。同状态且old version=expected或expected+1直接返回，不重复修改/审计 | [Service](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/application/SettlementProfileService.java:239)，[Store](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/infrastructure/persistence/JdbcSettlementProfileStore.java:120) |
| V2领域审计 | `platform_audit_entry`记录`SETTLEMENT_PROFILE_PROVIDED`或`SETTLEMENT_PROFILE_REVOKED`、`SUCCEEDED`、`HIGH`、TENANT scope；target为`COMMERCE_FINANCE/SETTLEMENT_PROFILE/<subject>`。安全摘要只取`profileVersionId`这一版本记录ID | [领域审计构造](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/application/SettlementProfileService.java:384)，[审计表](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/database/migration/V2_13_000__platform_audit.sql:16) |
| V2审计事务 | 此HIGH审计直接append并与业务事务共同提交；不是先排队等待异步审计。领域审计trace是新生成UUID，不能假定等于HTTP traceId；摘要无access version | [同步append](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-audit/src/main/java/cn/deshi/platform/audit/application/AuditApplicationService.java:22)，[安全摘要白名单](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-audit/src/main/java/cn/deshi/platform/audit/domain/AuditSummaryPolicy.java:15) |
| V1团队状态 | `v1_team_collection`有`status`、`holder_user_id`、`version`、`last_actor_user_id`、`last_action`、`started_at`、`confirmed_at`、`updated_at`；不读取handler_name | [V1 DDL](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/db/migration/20260913_team_collection.sql:5) |
| 最终确认 | 只允许当前IN_PROGRESS本人holder和当前version；成功状态为`USER_CONFIRMED_COMPLETE`，version+1，`last_action='USER_CONFIRM'`，confirmed_at写UTC；holder及started_at保留 | [V1 finish](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/TeamCollectionService.java:80) |
| V1事件 | `v1_team_collection_event`字段`id,team_id,actor_user_id,action,version,created_at`；完成action是`USER_CONFIRM`，不是`CONFIRM`或完成状态文本；version为变更后版本；唯一键`(team_id,version)` | [事件DDL](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/db/migration/20260913_team_collection.sql:42)，[事件写入](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/TeamCollectionService.java:158) |

**版本不能混用。** 14:00:35的历史回执为资料current.version=2、current_profile_version_id=5、team.version=1，无访问行。若后续操作前基线仍相同、无其他变更且每次只成功一次，预期访问关系依次`PROVIDED/v0 → REVOKED/v1 → PROVIDED/v2`，资料current.version仍2/版本ID仍5，最终团队才由v1变v2。该序列由源码默认值和更新规则推导，实际必须按新基线/响应读回；不能把上述预期直接填成实测结果。无关系行不是数据库中的REVOKED/v-1，-1只是请求约定。

## 最小只读SQL及执行方式

- [06_v2_provision_metadata.sql](sql/06_v2_provision_metadata.sql)：四条独立SELECT，依次绑定单位代码、本人Subject、本人Subject，以及审计查询的本人Subject/已核实单位ID/前次审计ID。返回单位身份、当前资料元数据、本人全部访问关系元数据、指定单位新审计。
- [07_v1_completion_metadata.sql](sql/07_v1_completion_metadata.sql)：分别绑定team ID以及team ID/前次event ID，读取团队和新增事件。初始event cursor可用0；确认前保留当前最大ID，确认后只比对新增部分。

SQL由编制者按源码核实，未由编制者运行。主执行者已经执行所需字段的等价只读技术查询，产生提供/撤销/重新提供、最终团队及领域审计回执；不声称这两个参数化文件被逐字执行。目标数据库由主执行者按已验证连接确定，V2不得猜schema名。每个`?`必须由驱动绑定；查询窗口不能直接执行未绑定占位符。SQL均以分号结尾，没有业务DML、DDL、临时表或FOR UPDATE。审计cursor初始0查询最多100行；若满100行，继续按最后ID翻页，不能将截断当成无新增。时间以`UTC_TIMESTAMP`写入的字段和事件实际采集时间分别记录。

## 每一阶段应留存的最少证据

| 阶段 | UI/实际请求应记录 | DB元数据应记录 | 不应推断 |
|---|---|---|---|
| 动作前基线 | 当前本人、XINKESAI真实tenantId，资料摘要、同意状态，当前V1办理权；不记录token/密码/表单原文 | 06前三条、07两条；保存最大审计/event ID。预检至第28条历史数据仅作比较起点 | 不能假定当前仍是14:00的状态 |
| 首次确认提供 | 明确勾选指定单位同意并点击提供；实际HTTP结果及响应中current version、tenantId/status/access version | 同一Subject全部access元数据、指定单位PROVIDED新审计；当前资料和团队状态 | checkbox本身、iframe消息或按钮变亮均不是提供成功证明；不应生成团队USER_CONFIRM |
| 提供所需安全验证 | 若真实403/STEP_UP_REQUIRED出现，记录弹框、成功验证及原提供的实际重试结果；保留本次单位和同意 | 403或取消后访问关系不应新增/递增；成功后按实际结果核对；相同命令幂等重试不应新增审计 | 不能因为无弹框判失败；不能把数据库读回代替实际重试次数观察 |
| 撤销 | 点击对应已提供单位撤销，记录真实响应REVOKED及顶部完成资格失效 | 同一access行更新到REVOKED、version+1，REVOKED审计；资料指针及团队仍未完成 | 不是删除收款资料、删除历史快照或撤销全部导出；无财务角色访问测试就不声称财务链已验收 |
| 重新提供 | 再次明确同意同一单位，使用界面真实最新access version；成功后记录PROVIDED及恢复待确认 | 同一access行PROVIDED且version+1，第二次PROVIDED新审计；对比其他单位状态未受影响 | 不是创建另一个Subject或第二行同单位关系 |
| Tour、收起、再次定位 | 提供后观察外层双读回后才启用，Tour出现；收起/Escape不完成，再次查看按钮仅定位 | 团队状态/版本不变，event cursor后无USER_CONFIRM | Tour可见不等于服务器完成 |
| 最终人工点击（最后） | 实际手动点击；随后重新GET本人资料和V1办理状态，再发V1confirm；响应成功、入口关闭及完成页/刷新 | access仍目标PROVIDED；团队USER_CONFIRMED_COMPLETE、版本+1、holder保留、confirmed_at非空、last_action USER_CONFIRM；新增一个同actor及新team version的USER_CONFIRM事件 | V1/V2不是跨库原子事务；确认仍为办理人声明，不表示验卡、付款或到账 |

外层实际读取字段是响应`View.version`和指定单位的`access.version`；不是以`profile.id`代替current version。[父页读回检查](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/old-code-pc/src/utils/nativeCollectionProgress.js:73)核对两版本，随后[父页办理权检查](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/old-code-pc/src/views/personal/TeamCollectionNative.vue:153)重读V1；[最终按钮](/Users/wwang/Documents/ClaudeCode/deshi_competition_2/old-code-pc/src/views/personal/TeamCollectionNative.vue:209)再次调用检查后才confirm。数据库最终快照无法单独证明浏览器执行了这个顺序；本轮`restore-network-metadata.txt`及`final-confirm-network-metadata.txt`补充正常操作的实际双读回和最终confirm链，不覆盖异常消息、并发或失败分支。

本补充最初编制时，报告和矩阵为截至第28条的10 PASS、17 PARTIAL、44 NOT_EXECUTED历史截面，原文另存`execution-report-before-provision.md`。当前截至第41条为21 PASS、19 PARTIAL、31 NOT_EXECUTED。事件29—35及三份读回证明tenant7实际PROVIDED/v0→REVOKED/v1→PROVIDED/v2；14:30:43的最终回执证明资料仍version2/id5、access仍PROVIDED/v2，团队363已USER_CONFIRMED_COMPLETE/v2，唯一USER_CONFIRM事件8，实际确认时间14:30:30.555642 UTC。14:34:14领域审计回执列出118/119/120三个SUCCEEDED/HIGH动作，均tenant7/profileID5；带明确query_scope的两Subject查询仅办理人有profile/access。编制者未运行SQL，不将等价读回说成文件逐字执行。最后焦点修复仅源码/54项回归通过，完成后未重走真实最终确认；未做的短信、并发、原入口自然过期、故障和财务链仍不通过推断补齐。
