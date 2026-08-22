# 现场证件自动大赛级发证数据审计

审计时间：2026-07-04

审计库：本地测试库 `jiaoxue_test`

执行方式：MySQL 容器内客户端，只执行 `SELECT`，并使用只读事务；未执行 `UPDATE`、`DELETE`、`INSERT` 或 DDL。

敏感信息处理：审计输出未包含手机号、身份证号、二维码 token；`subject_code` 样例使用 SHA256 前 12 位。

## 1. 表结构确认

`competition_scene_credential` 当前存在字段：

| 字段 | 是否存在 | 说明 |
| --- | --- | --- |
| `schedule_id` | 是 | 可空 |
| `target_id` | 是 | 可空 |
| `issue_channel` | 是 | 发证渠道 |
| `scope_type` | 是 | 证件作用域 |
| `scope_ref_id` | 是 | 作用域引用 ID |
| `credential_type` | 是 | 证件类型 |
| `subject_type` | 是 | 主体类型 |
| `subject_code` | 是 | 主体编码 |
| `user_id` | 是 | 用户 ID |
| `credential_snapshot_json` | 是 | 生成快照 |
| `source_schedule_id` | 否 | 当前无法结构化追踪自动证件来源赛场 |
| `source_target_id` | 否 | 当前无法结构化追踪自动证件来源 target |
| `auto_created` | 否 | 当前无法结构化区分自动大赛证和直接发证 |

基础数量：

| 指标 | 数量 |
| --- | ---: |
| `competition_scene_credential` 总数 | 20 |
| 未删除且有效证件 | 18 |

## 2. 诊断结果总览

| 诊断 | 结果 | 判断 |
| --- | ---: | --- |
| D1 同一 `user_id + competition_series_id` 多张有效大赛级证件 | 0 组 | 当前库未发现脏数据 |
| D2 同一 `subject_type + subject_code + competition_series_id + credential_type` 多张有效大赛级证件 | 0 组 | 当前库未发现脏数据 |
| D3 `issue_channel=SCHEDULE_MATCH` 且 `scope_type=COMPETITION` 自动大赛级证件 | 3 张 | 当前代码逻辑导致 |
| D4 `scope_type=COMPETITION` 且 `schedule_id/target_id` 都为空 | 7 张 | 3 张自动大赛证 + 4 张直接发证 |
| D5 删除或不存在 target 后仍残留的大赛级自动证件 | 0 张 | 当前库暂未出现残留 |
| D6 同一 target 下多张有效 SCHEDULE 级证件 | 0 组 | 当前库未发现脏数据 |
| D7 `credential_type` 与 target.credential_type 不一致 | 0 张 | 当前库未发现漂移 |
| D8 role/name/school/group/phone 与当前 target 不一致 | 0 张 | 当前库未发现快照漂移 |
| D9 PC/小程序会混合展示大赛级和赛场级证件的用户分组 | 4 组 | 当前代码逻辑导致 |

## 3. D1 同一用户同一大赛多张有效大赛级证件

执行 SQL：

```sql
SELECT COUNT(*) AS groups_cnt, COALESCE(SUM(active_count),0) AS involved_credentials
FROM (
  SELECT competition_series_id, user_id, COUNT(*) AS active_count
  FROM competition_scene_credential
  WHERE del_flag = '0'
    AND credential_status = 'EFFECTIVE'
    AND scope_type = 'COMPETITION'
    AND user_id IS NOT NULL
  GROUP BY competition_series_id, user_id
  HAVING COUNT(*) > 1
) x;
```

结果：

| groups_cnt | involved_credentials |
| ---: | ---: |
| 0 | 0 |

结论：当前测试库没有同一用户同一大赛多张有效大赛级证件的历史脏数据证据。但代码复用条件仍然偏宽，风险仍存在。

## 4. D2 同一 subject + type 多张有效大赛级证件

执行 SQL：

```sql
SELECT COUNT(*) AS groups_cnt, COALESCE(SUM(active_count),0) AS involved_credentials
FROM (
  SELECT competition_series_id, subject_type, subject_code, credential_type, COUNT(*) AS active_count
  FROM competition_scene_credential
  WHERE del_flag = '0'
    AND credential_status = 'EFFECTIVE'
    AND scope_type = 'COMPETITION'
    AND subject_type IS NOT NULL
    AND subject_code IS NOT NULL
    AND credential_type IS NOT NULL
  GROUP BY competition_series_id, subject_type, subject_code, credential_type
  HAVING COUNT(*) > 1
) x;
```

结果：

| groups_cnt | involved_credentials |
| ---: | ---: |
| 0 | 0 |

结论：当前测试库没有同一 subject/type 多张有效大赛级证件的历史脏数据证据。

## 5. D3 自动生成的大赛级证件数量

执行 SQL：

```sql
SELECT COUNT(*) AS cnt
FROM competition_scene_credential
WHERE del_flag = '0'
  AND issue_channel = 'SCHEDULE_MATCH'
  AND scope_type = 'COMPETITION';
```

结果：

| cnt |
| ---: |
| 3 |

按大赛、证件类型、状态分布：

| competition_series_id | credential_type | credential_status | cnt |
| ---: | --- | --- | ---: |
| 1 | PARTICIPANT | EFFECTIVE | 2 |
| 1 | STAFF | EFFECTIVE | 1 |

脱敏样例：

| credential_id | competition_series_id | user_id | credential_type | subject_type | subject_code_hash | issue_channel | scope_type | schedule_id | target_id | credential_status |
| ---: | ---: | ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| 86 | 1 | 1592 | PARTICIPANT | USER | 8cca04ee02b8 | SCHEDULE_MATCH | COMPETITION | NULL | NULL | EFFECTIVE |
| 84 | 1 | 1591 | PARTICIPANT | USER | e52522a505f6 | SCHEDULE_MATCH | COMPETITION | NULL | NULL | EFFECTIVE |
| 79 | 1 | 305 | STAFF | USER | 090d3859ff68 | SCHEDULE_MATCH | COMPETITION | NULL | NULL | EFFECTIVE |

结论：这是当前代码逻辑直接产生的数据表现，不是脏数据。

## 6. D4 大赛级且 schedule_id/target_id 为空的证件

执行 SQL：

```sql
SELECT COUNT(*) AS cnt
FROM competition_scene_credential
WHERE del_flag = '0'
  AND scope_type = 'COMPETITION'
  AND schedule_id IS NULL
  AND target_id IS NULL;
```

结果：

| cnt |
| ---: |
| 7 |

按渠道、类型、状态分布：

| issue_channel | credential_type | credential_status | cnt |
| --- | --- | --- | ---: |
| COMPETITION_DIRECT | PARTICIPANT | EFFECTIVE | 4 |
| SCHEDULE_MATCH | PARTICIPANT | EFFECTIVE | 2 |
| SCHEDULE_MATCH | STAFF | EFFECTIVE | 1 |

脱敏样例：

| credential_id | competition_series_id | user_id | credential_type | subject_type | subject_code_hash | issue_channel | schedule_id | target_id | credential_status |
| ---: | ---: | ---: | --- | --- | --- | --- | --- | --- | --- |
| 86 | 1 | 1592 | PARTICIPANT | USER | 8cca04ee02b8 | SCHEDULE_MATCH | NULL | NULL | EFFECTIVE |
| 84 | 1 | 1591 | PARTICIPANT | USER | e52522a505f6 | SCHEDULE_MATCH | NULL | NULL | EFFECTIVE |
| 83 | 1 | 304 | PARTICIPANT | USER | d874e4e4a5df | COMPETITION_DIRECT | NULL | NULL | EFFECTIVE |
| 82 | 1 | 331 | PARTICIPANT | USER | 0bba869d7f39 | COMPETITION_DIRECT | NULL | NULL | EFFECTIVE |
| 81 | 1 | 306 | PARTICIPANT | USER | 38b83caefa1e | COMPETITION_DIRECT | NULL | NULL | EFFECTIVE |
| 79 | 1 | 305 | STAFF | USER | 090d3859ff68 | SCHEDULE_MATCH | NULL | NULL | EFFECTIVE |
| 68 | 1 | 1353 | PARTICIPANT | USER | 04ad22d76303 | COMPETITION_DIRECT | NULL | NULL | EFFECTIVE |

结论：大赛级直接发证为空 `schedule_id/target_id` 是合理模型；但 `SCHEDULE_MATCH + COMPETITION + NULL schedule/target` 的 3 张是赛场发证自动创建的数据。

## 7. D5 删除或不存在 target 后仍残留的大赛级自动证件

执行 SQL：

```sql
SELECT COUNT(*) AS cnt
FROM (
  SELECT c.credential_id,
         CASE WHEN JSON_VALID(c.credential_snapshot_json)
              THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(c.credential_snapshot_json, '$.target.targetId')) AS UNSIGNED)
              ELSE NULL END AS snapshot_target_id
  FROM competition_scene_credential c
  WHERE c.del_flag = '0'
    AND c.credential_status = 'EFFECTIVE'
    AND c.issue_channel = 'SCHEDULE_MATCH'
    AND c.scope_type = 'COMPETITION'
) x
LEFT JOIN competition_scene_schedule_target t ON t.target_id = x.snapshot_target_id
WHERE x.snapshot_target_id IS NOT NULL
  AND (t.target_id IS NULL OR t.del_flag <> '0');
```

结果：

| cnt |
| ---: |
| 0 |

结论：当前测试库暂未出现“target 已删除或不存在，但自动大赛级证件仍有效”的残留数据。这个问题是当前删除逻辑下的确定性风险，但在本次数据样本中尚未发生。

## 8. D6 同一 target 下多张有效 SCHEDULE 级证件

执行 SQL：

```sql
SELECT COUNT(*) AS groups_cnt, COALESCE(SUM(active_count),0) AS involved_credentials
FROM (
  SELECT target_id, COUNT(*) AS active_count
  FROM competition_scene_credential
  WHERE del_flag = '0'
    AND credential_status = 'EFFECTIVE'
    AND scope_type = 'SCHEDULE'
    AND target_id IS NOT NULL
  GROUP BY target_id
  HAVING COUNT(*) > 1
) x;
```

结果：

| groups_cnt | involved_credentials |
| ---: | ---: |
| 0 | 0 |

结论：当前测试库没有同一 target 多张有效赛场级证件的脏数据证据。

## 9. D7 credential_type 与 target.credential_type 不一致

执行 SQL：

```sql
SELECT COUNT(*) AS cnt
FROM competition_scene_credential c
JOIN competition_scene_schedule_target t
  ON t.target_id = c.target_id
 AND t.del_flag = '0'
WHERE c.del_flag = '0'
  AND NOT (c.credential_type <=> t.credential_type);
```

结果：

| cnt |
| ---: |
| 0 |

自动大赛级证件按 snapshot target 反查：

| 指标 | 数量 |
| --- | ---: |
| 自动大赛级证件 credential_type 与当前 target.credential_type 不一致 | 0 |

结论：当前测试库没有证件类型漂移证据。

## 10. D8 证件快照字段与当前 target 不一致

比对字段：

1. `competition_role_name`；
2. `user_name`；
3. `school_name`；
4. `second_level_name`；
5. `waiting_group_name`；
6. `phone`，只比较是否不同，不输出值。

执行 SQL：

```sql
SELECT COUNT(*) AS cnt
FROM competition_scene_credential c
JOIN competition_scene_schedule_target t
  ON t.target_id = c.target_id
 AND t.del_flag = '0'
WHERE c.del_flag = '0'
  AND (
    NOT (c.competition_role_name <=> t.competition_role_name)
    OR NOT (c.user_name <=> t.user_name)
    OR NOT (c.school_name <=> t.school_name)
    OR NOT (c.second_level_name <=> t.second_level_name)
    OR NOT (c.waiting_group_name <=> t.waiting_group_name)
    OR NOT (c.phone <=> t.phone)
  );
```

结果：

| cnt |
| ---: |
| 0 |

分字段统计：

| role_diff | name_diff | school_diff | second_group_diff | waiting_group_diff | phone_diff |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 0 | 0 | 0 | 0 | 0 | 0 |

结论：当前测试库没有发现 target 更新后证件快照未同步导致的字段漂移。但代码路径上确实不会自动同步，后续只要修改 target 而不重生成证件，就可能出现此类数据表现。

## 11. D9 PC/小程序混合展示情况

模拟 PC/小程序按用户和大赛拿到的证件集合：同一 `competition_series_id + user_id` 下同时存在大赛级证件和赛场级证件。

执行 SQL：

```sql
SELECT COUNT(*) AS groups_cnt,
       COALESCE(SUM(competition_cnt),0) AS competition_credentials,
       COALESCE(SUM(schedule_cnt),0) AS schedule_credentials
FROM (
  SELECT competition_series_id, user_id,
         SUM(scope_type = 'COMPETITION') AS competition_cnt,
         SUM(scope_type <> 'COMPETITION' OR scope_type IS NULL) AS schedule_cnt,
         COUNT(*) AS total_cnt
  FROM competition_scene_credential
  WHERE del_flag = '0'
    AND user_id IS NOT NULL
  GROUP BY competition_series_id, user_id
  HAVING competition_cnt > 0
     AND schedule_cnt > 0
) x;
```

结果：

| groups_cnt | competition_credentials | schedule_credentials |
| ---: | ---: | ---: |
| 4 | 4 | 4 |

脱敏样例：

| competition_series_id | user_id | competition_cnt | schedule_cnt | total_cnt | competition_credential_ids | schedule_credential_ids |
| ---: | ---: | ---: | ---: | ---: | --- | --- |
| 1 | 305 | 1 | 1 | 2 | 79 | 80 |
| 1 | 1353 | 1 | 1 | 2 | 68 | 76 |
| 1 | 1591 | 1 | 1 | 2 | 84 | 85 |
| 1 | 1592 | 1 | 1 | 2 | 86 | 87 |

结论：这不是脏数据，而是当前生成逻辑和展示逻辑共同形成的结果。PC 和小程序都会将大赛级证件作为顶部主证件，将赛场级证件作为赛场明细。

## 12. 数据归因

### 12.1 已确认是代码逻辑导致

1. 自动大赛级证件存在：3 张 `SCHEDULE_MATCH + COMPETITION`；
2. 大赛级证件无 `schedule_id/target_id`：自动大赛证和直接发证都符合当前结构；
3. PC/小程序混合展示：4 个用户分组会同时展示大赛级和赛场级证件。

### 12.2 当前库未发现历史脏数据

1. 未发现同一用户同一大赛多张有效大赛级证件；
2. 未发现同一 subject/type 多张有效大赛级证件；
3. 未发现 target 已删除或不存在后自动大赛级证件残留；
4. 未发现同一 target 多张有效赛场级证件；
5. 未发现证件类型与 target 类型不一致；
6. 未发现证件快照字段与当前 target 字段不一致。

### 12.3 仍需治理的潜在风险

当前测试库没撞到，不等于代码没有风险：

1. 删除 target 后，自动大赛级证件仍可能残留，因为自动大赛级证件没有 `target_id`；
2. 复用大赛级证件只按 `userId` 或 `subjectCode`，可能跨角色复用；
3. target 更新后证件字段不会同步，后续会出现快照漂移；
4. 前端以大赛级证件作为主证件，会放大“多证”感知。
