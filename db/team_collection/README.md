# 获奖队伍收款名单迁移操作

本目录是本次名单导入的唯一操作入口。适用目标为已核对的本地 V1 数据库 `jiaoxue_test`、赛事届次 `81`、活动 `XINKE_13_2026`。V2 不参与本次迁移。

## 现在保留哪些文件

| 文件 | 用途 | 执行时机 |
|---|---|---|
| [建表脚本](../migration/20260913_team_collection.sql) | 创建4张V1名单/办理/事件表 | 首次初始化；已建表可跳过 |
| [01_source.sql](01_source.sql) | 导入Excel学生姓名位置，不赋予权限 | 首次导入；已导入可跳过 |
| [02_import.sql](02_import.sql) | 自动匹配、补齐3个确认账号、校验、预览或发布 | 正常导入唯一入口 |
| [source-summary.json](source-summary.json) | 源文件摘要及人数口径 | 供核对，不执行 |

旧版分开的预览、发布、诊断、三人补充SQL已合并或移除，不再执行旧版02/03/05/06/07。管理员恢复脚本移到 [db/maintenance/team_collection_recovery.sql](../maintenance/team_collection_recovery.sql)，不属于正常导入步骤。

## 1. 准备与选择数据库

在 Navicat 连接本地 `127.0.0.1:3306`，使用有建表、临时表、存储过程及名单写入权限的账号。正式写入前保存目标库备份，保持 V1 功能开关 `V1_TEAM_COLLECTION_ENABLED=false`。

新建查询窗口，执行：

```sql
USE jiaoxue_test;
SELECT DATABASE() AS target_database;
```

结果必须为 `jiaoxue_test`。以下步骤仅适用于此库；其他环境需先核对数据库、名单和账号，不能直接套用本地账号确认结果。

## 2. 首次初始化（你当前可以跳过）

如果已完成建表和源名单导入，从第3步开始。

全新环境依次执行两个文件的完整内容：

1. `db/migration/20260913_team_collection.sql`。
2. 本目录 `01_source.sql`。

使用Navicat查询编辑器执行完整SQL文本，不要使用Excel导入向导，不要只执行最后一句。两个文件可重复执行，不会清除现有办理数据，但不能用重跑来覆盖已有错误名单。

检查源名单：

```sql
SELECT COUNT(*) AS source_student_cells,
       COUNT(DISTINCT team_code) AS source_teams
FROM v1_team_collection_source
WHERE collection_code = 'XINKE_13_2026'
  AND source_sha256 = '967559dfddf2be3b70ac513b9e0074835ddbccec441fe29e626bf98a2740e9ec';
```

必须返回 `1216`、`520`。1216指学生姓名位置/队伍成员记录，不是不同账号人数。教师姓名列未导入。

## 3. 运行预览

在准备执行脚本的查询窗口先运行：

```sql
USE jiaoxue_test;
SET @reviewed_database = 'jiaoxue_test';
SET @reviewed_series_id = 81;
SET @publish_team_collection = 0;
```

然后在**同一连接**粘贴并执行 [02_import.sql](02_import.sql) 的完整内容。也可以把上述参数放在文件全文最前面一起执行。不要只运行 `CALL`，保留文件中的 `DELIMITER` 语句，使用英文分号 `;`。

此模式创建会话级临时表和辅助存储过程，读取源数据并校验，但不向正式名单表写入权限。

查看各结果集：

| 项目 | 应有结果 |
|---|---|
| `source_student_cells` | 1216 |
| `source_teams` | 520 |
| `uniquely_matched_cells` | 1216 |
| `unresolved_cells` | 0 |
| 未解决位置明细 | 空结果 |
| 同队重复账号明细 | 空结果 |
| 最后 `result` | `PREVIEW_OK` |

另一个结果集列出用户已逐人核对确认的映射：

| 学生 | Excel位置 | 账号 |
|---|---|---:|
| 方柏辉 | 赛道一名单 E265 | 44349 |
| 徐启航 | 赛道一名单 F354 | 50719 |
| 张清 | 赛道二名单 H21 | 44874 |

这3条只补充本次收款名单，不修改原报名/队员关系或身份认证。脚本每次核对实名唯一性、正常账号、同队学生关系等条件；不需要另外执行补丁。

## 4. 正式导入

预览符合上表后，执行：

```sql
USE jiaoxue_test;
SET @reviewed_database = 'jiaoxue_test';
SET @reviewed_series_id = 81;
SET @publish_team_collection = 1;
```

随后在同一连接**再次完整执行 `02_import.sql`**。

正式模式会重新读取并匹配全部数据，不依赖上次预览的临时表。预览和正式导入可使用不同连接，但每次都必须重新设置以上参数并执行完整文件。脚本校验通过才在一个事务中发布队伍和成员名单。

成功时最后返回：

- `result = PUBLISHED`
- `teams = 520`
- `member_rows = 1216`
- `distinct_accounts` 为实际不同账号数，不要求等于1216。

## 5. 独立核验

```sql
SELECT t.collection_code,
       COUNT(DISTINCT t.id) AS teams,
       COUNT(*) AS member_rows,
       COUNT(DISTINCT m.user_id) AS distinct_accounts
FROM v1_team_collection t
JOIN v1_team_collection_member m ON m.team_id = t.id
WHERE t.collection_code = 'XINKE_13_2026'
GROUP BY t.collection_code;

SELECT m.student_name, m.user_id, t.team_code
FROM v1_team_collection_member m
JOIN v1_team_collection t ON t.id = m.team_id
WHERE t.collection_code = 'XINKE_13_2026'
  AND ((m.source_sheet = '赛道一名单' AND m.source_row = 265 AND m.source_column = 'E')
    OR (m.source_sheet = '赛道一名单' AND m.source_row = 354 AND m.source_column = 'F')
    OR (m.source_sheet = '赛道二名单' AND m.source_row = 21 AND m.source_column = 'H'));
```

第一项应为520队/1216条成员记录，第二项应与上述3个确认账号一致。保留执行结果作为本次导入记录。

## 6. 部署与启用

数据库导入成功不等于页面功能已启用。部署本次配套V1后端和PC前端，确认已有V2桥接配置可用，再将V1配置 `V1_TEAM_COLLECTION_ENABLED=true` 并刷新/重启配置。

使用实际账号验证：名单内入口可见、名单外不可见；同队两人竞争仅一人取得办理权；放弃后可换人；办理人确认完成后同队可见姓名/确认时间且不能再申请。V2仍沿用原iframe流程，V1人工确认不证明V2已保存成功。

## 失败、重跑与恢复

- **参数错误**：确认 `SET`、库名引号及英文分号正确，在执行文件的当前连接重新设置三个审核/模式参数。
- **三人核对条件发生变化**：脚本会报 `Reviewed accounts no longer match...`。重新核对账号、实名和报名/成员状态；不要删除校验或改用其他同名账号。
- **匹配缺失、歧义、错届次或同队重复账号**：查看导入脚本输出的明细，在问题处理后从预览模式重新运行。
- **已有名单不一致、被撤销或部分发布**：脚本拒绝静默补写/重新启用。先核对现有名单，不要清空表绕过保护。
- **重复导入**：相同来源和账号名单可重跑正式模式，不重置已有办理人、确认时间、状态或版本；原已撤销记录仍拒绝自动启用。脚本报错时若客户端停止在中间，下次执行完整文件即可重建辅助过程与临时结果。
- **失败回滚范围**：正式发布中的队伍/成员写入一起回滚；此前的建表和01源数据导入不会因此消失。每次执行应使用独立迁移会话，不与其他尚未提交的业务SQL混在一起。
- **运行后暂停功能**：关闭 `V1_TEAM_COLLECTION_ENABLED`，保留名单和事件数据。长期占用或误确认才使用维护目录的恢复脚本，按其注释填写目标队伍、当前版本、管理员账号及动作。

本次整理仅修改文件并使用隔离MySQL测试，不代表已在你的业务库执行正式导入。
