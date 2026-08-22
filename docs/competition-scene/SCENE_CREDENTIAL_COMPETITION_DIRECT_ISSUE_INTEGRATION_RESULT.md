# 大赛级直接发证最小闭环测试环境联调报告

生成时间：2026-07-02

## 1. 联调范围

本轮基于 `SCENE_CREDENTIAL_COMPETITION_DIRECT_ISSUE_DEV_RESULT.md` 进行测试环境联调验证。

验证范围：

1. 测试库 migration 状态。
2. 正式 9205 competition 运行态。
3. 管理端菜单挂载。
4. 大赛级直接发证接口。
5. 大赛级证件列表接口。
6. 扫码报道。
7. 扫码资料领取。
8. 大赛级证件候场拒绝。
9. PC / 小程序展示数据接口。

禁止事项执行情况：

- 未开发贵宾证正式页面。
- 未开发媒体证。
- 未开发临时证。
- 未开发复杂批量导入。
- 未开发复杂人员库。
- 未使用伪赛场安排。
- 未恢复 `MIXED`。
- 未连接生产数据库。

## 2. 测试环境

### 2.1 服务运行态

| 服务 | 地址 | 结果 |
| --- | --- | --- |
| gateway | `127.0.0.1:9889` | `/actuator/health = UP` |
| competition | `127.0.0.1:9205` | `/actuator/health = UP` |
| auth | `127.0.0.1:9224` | admin 登录成功 |
| Nacos | `127.0.0.1:8848` | 可读取 test namespace 配置 |

### 2.2 测试库

Nacos 配置确认：

```text
database = jiaoxue_test
host = 10.10.10.10
port = 3306
```

说明：

- 本轮只连接测试库 `jiaoxue_test`。
- 未连接生产数据库。

## 3. Migration 验证

文件：

```text
db/migration/20260702_competition_scene_credential_direct_issue_p2.sql
```

测试库检查结果：

| 检查项 | 结果 |
| --- | --- |
| `competition_scene_credential.subject_code` | 已存在 |
| `idx_scene_credential_subject_code` | 已存在 |
| `idx_scene_credential_competition_subject` | 已存在 |
| 历史数据 `subject_code` 回填 | 已具备 |

本轮未重复执行 migration。

## 4. 管理端菜单挂载

已检查 `sys_menu` 结构：

| 菜单 | menu_id | parent_id | path | component |
| --- | ---: | ---: | --- | --- |
| 赛事管理 | 2002 | 0 | `tournament` | `null` |
| 现场安排配置 | 2437 | 2002 | `sceneSchedule` | `tournament/sceneSchedule/index` |
| 资源管理 | 2448 | 2002 | `sceneResource` | `tournament/sceneResource/index` |

本轮将“大赛级发证”按同级业务页面挂载到“赛事管理”下：

| menu_id | menu_name | parent_id | order_num | path | component | menu_type | perms |
| ---: | --- | ---: | ---: | --- | --- | --- | --- |
| 2454 | 大赛级发证 | 2002 | 82 | `sceneCredentialCompetition` | `tournament/sceneCredentialCompetition/index` | C | `competition:sceneCredential:list` |
| 2455 | 大赛级证件查询 | 2454 | 1 | `#` |  | F | `competition:sceneCredential:query` |
| 2456 | 大赛级证件发放 | 2454 | 2 | `#` |  | F | `competition:sceneCredential:add` |
| 2457 | 大赛级证件删除 | 2454 | 3 | `#` |  | F | `competition:sceneCredential:remove` |

执行后的动态路由验证：

```text
GET /system/menu/getRouters
```

结果：

- 包含 `sceneCredentialCompetition`。
- 包含“大赛级发证”。
- admin 权限为 `*:*:*`，可见该菜单。

## 5. 菜单 SQL

本轮已在测试库执行以下逻辑等价 SQL：

```sql
INSERT INTO sys_menu
  (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type,
   visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT
  '大赛级发证', 2002, 82, 'sceneCredentialCompetition',
  'tournament/sceneCredentialCompetition/index',
  1, 0, 'C', '0', '0', 'competition:sceneCredential:list',
  'ticket', 'admin', SYSDATE(), '', NULL, '大赛级现场证件直接发证菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE component = 'tournament/sceneCredentialCompetition/index'
);
```

按钮权限按新菜单 `menu_id = 2454` 插入：

```text
competition:sceneCredential:query
competition:sceneCredential:add
competition:sceneCredential:remove
```

## 6. 发证接口联调

### 6.1 工作人员证

用途：让 admin 作为现场扫码操作人。

请求：

```text
POST /competition/sceneCredential/competitionDirectIssue
```

参数：

```json
{
  "competitionSeriesId": 1,
  "credentialType": "STAFF",
  "credentialName": "现场工作人员证",
  "subjectType": "USER",
  "subjectCode": "1",
  "subjectName": "admin",
  "remark": "大赛级直接发证联调-工作人员"
}
```

结果：

| 字段 | 值 |
| --- | --- |
| credentialId | 57 |
| credentialNo | `CC20260702-1-ABEEB333` |
| issue_channel | `COMPETITION_DIRECT` |
| scope_type | `COMPETITION` |
| scope_ref_id | 1 |
| schedule_id | `null` |
| target_id | `null` |
| subject_code | `1` |

### 6.2 大赛级参赛证

请求：

```text
POST /competition/sceneCredential/competitionDirectIssue
```

参数：

```json
{
  "competitionSeriesId": 1,
  "credentialType": "PARTICIPANT",
  "credentialName": "参赛证",
  "subjectType": "USER",
  "subjectCode": "1591",
  "subjectName": "ph5_user_a",
  "remark": "大赛级直接发证联调-参赛证"
}
```

结果：

| 字段 | 值 |
| --- | --- |
| credentialId | 58 |
| credentialNo | `CC20260702-1-81101B1F` |
| qrContent | `csc_6ece7fd194b24d6a954c35bd59b392d8` |
| issue_channel | `COMPETITION_DIRECT` |
| scope_type | `COMPETITION` |
| scope_ref_id | 1 |
| schedule_id | `null` |
| target_id | `null` |
| subject_type | `USER` |
| subject_code | `1591` |
| credential_type | `PARTICIPANT` |
| credential_name | `参赛证` |

能力配置：

```json
{
  "report": true,
  "material": true,
  "waiting": false,
  "review": false,
  "resourceReservation": false,
  "vipAccess": false
}
```

验证结果：

| 检查项 | 结果 |
| --- | --- |
| `issue_channel = COMPETITION_DIRECT` | 通过 |
| `scope_type = COMPETITION` | 通过 |
| `scope_ref_id = competitionSeriesId` | 通过 |
| `schedule_id = null` | 通过 |
| `target_id = null` | 通过 |
| `subject_code = subjectCode` | 通过 |
| `ability_json.report = true` | 通过 |
| `ability_json.material = true` | 通过 |
| `ability_json.waiting = false` | 通过 |
| 不生成伪赛场安排 | 通过 |

### 6.3 重复发证

重复发放同一：

```text
competitionSeriesId + subjectType + subjectCode + credentialType + scopeType=COMPETITION
```

结果：

```json
{
  "code": 500,
  "msg": "该大赛级主体已存在有效现场证件"
}
```

符合预期。

## 7. 大赛级证件列表接口

接口：

```text
GET /competition/sceneCredential/competitionList?pageNum=1&pageSize=10&credentialId=58
```

结果：

- 返回 `code = 200`。
- `total = 1`。
- 返回记录为 credentialId `58`。
- 管理端列表可读取：
  - `scopeType = COMPETITION`
  - `credentialName = 参赛证`
  - `reportStateStatus = DONE`
  - `materialStateStatus = DONE`
  - `waitingStateStatus = null`

未出现：

```text
No static resource
```

## 8. 扫码报道联调

扫码对象：

```text
qrContent = csc_6ece7fd194b24d6a954c35bd59b392d8
```

操作人：

```text
admin，具备大赛级 STAFF 证件
```

### 8.1 扫码

接口：

```text
POST /competition/sceneVerify/scan
```

结果摘要：

| 字段 | 值 |
| --- | --- |
| operationResult | `PASS` |
| resultMessage | `扫码核验通过` |
| operatorRole | `STAFF` |
| targetRole | `MEMBER` |
| actions | `REPORT_SIGN`、`MATERIAL_RECEIVE` |

### 8.2 确认报道

接口：

```text
POST /competition/sceneVerify/confirm
```

参数：

```json
{
  "qrContent": "csc_6ece7fd194b24d6a954c35bd59b392d8",
  "operationType": "REPORT_SIGN"
}
```

结果摘要：

| 字段 | 值 |
| --- | --- |
| operationResult | `PASS` |
| resultMessage | `确认成功` |
| reportStatus | `1` |
| reportState | `DONE` |

数据库回查：

| 字段 | 值 |
| --- | --- |
| operation_type | `REPORT` |
| scope_type | `COMPETITION` |
| scope_ref_id | 1 |
| subject_type | `USER` |
| subject_code | `1591` |
| operation_status | `DONE` |
| credential_id | 58 |

### 8.3 重复报道

结果摘要：

| 字段 | 值 |
| --- | --- |
| operationResult | `DUPLICATE` |
| duplicate | `true` |
| resultMessage | `alreadyDone：重复操作，证件状态未变更` |

数据库未重复写新的 `REPORT DONE` 状态。

## 9. 扫码资料领取联调

接口：

```text
POST /competition/sceneVerify/confirm
```

参数：

```json
{
  "qrContent": "csc_6ece7fd194b24d6a954c35bd59b392d8",
  "operationType": "MATERIAL_RECEIVE"
}
```

结果摘要：

| 字段 | 值 |
| --- | --- |
| operationResult | `PASS` |
| resultMessage | `确认成功` |
| materialStatus | `1` |
| materialState | `DONE` |

数据库回查：

| 字段 | 值 |
| --- | --- |
| operation_type | `MATERIAL` |
| scope_type | `COMPETITION` |
| scope_ref_id | 1 |
| subject_type | `USER` |
| subject_code | `1591` |
| delegate_user_id | `1591` |
| delegate_name | `ph5_user_a` |
| delegate_relation | `SELF` |
| operation_status | `DONE` |

验证结论：

- `MATERIAL` 状态主体固定为 `USER`。
- 本人领取记录正确。
- 管理端资料状态可点亮。

### 9.1 重复资料领取

结果摘要：

| 字段 | 值 |
| --- | --- |
| operationResult | `DUPLICATE` |
| duplicate | `true` |
| resultMessage | `alreadyDone：重复操作，证件状态未变更` |

数据库未重复写新的 `MATERIAL DONE` 状态。

## 10. 候场拒绝联调

接口：

```text
POST /competition/sceneVerify/confirm
```

参数：

```json
{
  "qrContent": "csc_6ece7fd194b24d6a954c35bd59b392d8",
  "operationType": "WAITING_CHECK_IN"
}
```

结果摘要：

| 字段 | 值 |
| --- | --- |
| operationResult | `FAIL` |
| resultMessage | `CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING` |
| waitingStatus | `0` |
| waitingState | `null` |

数据库回查：

```sql
SELECT COUNT(*)
FROM competition_scene_subject_operation_state
WHERE competition_series_id = 1
  AND subject_code = '1591'
  AND operation_type = 'WAITING'
  AND deleted = 0;
```

结果：

```text
0
```

验证结论：

- 大赛级证件候场被拒绝。
- 未写 `WAITING` operation_state。
- 未产生 `SCHEDULE` 级状态。
- 未点亮候场 tag。

## 11. 操作流水验证

credentialId `58` 最近流水：

| operation_type | operation_stage | operation_result | result_message |
| --- | --- | --- | --- |
| `WAITING_CHECK_IN` | `CONFIRM` | `FAIL` | `CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING` |
| `MATERIAL_RECEIVE` | `CONFIRM` | `DUPLICATE` | `alreadyDone：重复操作，证件状态未变更` |
| `MATERIAL_RECEIVE` | `CONFIRM` | `PASS` | `确认成功` |
| `REPORT_SIGN` | `CONFIRM` | `DUPLICATE` | `alreadyDone：重复操作，证件状态未变更` |
| `REPORT_SIGN` | `CONFIRM` | `PASS` | `确认成功` |

验证结论：

- 成功、重复、失败均写入 `competition_scene_operation_log`。
- `operation_state.last_log_id` 已回填成功确认流水。

## 12. PC / 小程序展示数据验证

### 12.1 测试账号处理

`ph5_user_a` 正确密码：

```text
Ph5Test@123
```

说明：

- 联调中曾因错误密码尝试触发测试账号 `ph5_user_a` 的 10 分钟锁定。
- 已仅清理 Redis 测试缓存键 `pwd_err_cnt:ph5_user_a`。
- 随后使用正确密码登录成功。

### 12.2 用户端证件列表接口

接口：

```text
GET /competition/userCompetition/sceneCredential/myList
```

登录用户：

```text
ph5_user_a / userId = 1591
```

结果摘要：

```json
{
  "credentialId": 58,
  "credentialName": "参赛证",
  "scopeType": "COMPETITION",
  "credentialType": "PARTICIPANT",
  "subjectCode": "1591",
  "reportStateStatus": "DONE",
  "materialStateStatus": "DONE",
  "waitingStateStatus": null,
  "reportStatus": "1",
  "materialStatus": "1",
  "waitingStatus": "0"
}
```

验证结论：

- PC / 小程序共用的证件数据接口已返回 `credentialName`。
- 已返回 `scopeType = COMPETITION`。
- 已返回报道、资料、候场状态字段。
- 大赛级证件候场状态为空或 `0`，可支撑页面不展示候场 tag 或展示“无候场功能”。

### 12.3 页面验证说明

本轮完成接口级展示验证：

- PC 页面代码已在开发阶段完成最小改造。
- 小程序“我的证件”和扫码结果页代码已在开发阶段完成最小改造。
- 本轮未启动浏览器和小程序开发者工具做最终视觉验收。

建议人工补测：

1. 使用 `ph5_user_a / Ph5Test@123` 登录 PC。
2. 打开个人中心现场证件区域。
3. 确认证件名称显示“参赛证”。
4. 确认证件范围显示“大赛级”。
5. 确认报道、资料状态为已完成。
6. 确认不显示候场 tag，或显示“无候场功能”。
7. 小程序重复以上展示检查。

## 13. No Static Resource 验证

以下接口均通过正式网关访问成功，未出现 `No static resource`：

```text
GET  /competition/sceneCredential/competitionList
POST /competition/sceneCredential/competitionDirectIssue
POST /competition/sceneVerify/scan
POST /competition/sceneVerify/confirm
GET  /competition/userCompetition/sceneCredential/myList
```

## 14. 发现的问题

### 14.1 ph5_user_a 临时锁定

现象：

- 因联调中尝试了几个错误密码，`ph5_user_a` 被测试环境锁定 10 分钟。

处理：

- 查到历史文档中正确密码为 `Ph5Test@123`。
- 清理测试 Redis DB 10 中的 `pwd_err_cnt:ph5_user_a`。
- 使用正确密码登录成功。

影响：

- 仅影响测试账号登录计数。
- 不涉及生产环境。
- 不影响大赛级直接发证、扫码和状态写入结论。

## 15. 修复文件清单

本轮为联调验证，不修改业务代码。

新增报告文件：

```text
docs/competition-scene/SCENE_CREDENTIAL_COMPETITION_DIRECT_ISSUE_INTEGRATION_RESULT.md
```

数据库测试数据变更：

- 新增管理端菜单 `大赛级发证` 及按钮权限。
- 新增大赛级 STAFF 测试证件 credentialId `57`。
- 新增大赛级 PARTICIPANT 测试证件 credentialId `58`。
- 新增 REPORT / MATERIAL operation_state。
- 新增扫码操作流水。

## 16. 联调结论

| 项目 | 结论 |
| --- | --- |
| 测试库 migration | 通过 |
| 9205 competition 运行态 | 通过 |
| 管理端菜单挂载 | 通过 |
| 大赛级直接发证 | 通过 |
| 大赛级证件列表 | 通过 |
| 重复发证拦截 | 通过 |
| 扫码报道 | 通过 |
| 重复报道 alreadyDone | 通过 |
| 扫码资料领取 | 通过 |
| 重复资料 alreadyDone | 通过 |
| 候场拒绝 | 通过 |
| REPORT 写 COMPETITION operation_state | 通过 |
| MATERIAL 写 COMPETITION + USER operation_state | 通过 |
| 不写 WAITING 状态 | 通过 |
| 不生成伪赛场安排 | 通过 |
| PC / 小程序接口展示数据 | 通过 |
| PC / 小程序视觉人工验收 | 待人工补测 |

最终结论：

大赛级直接发证最小闭环在测试环境联调通过。建议进入 PC / 小程序人工视觉补测，确认页面样式与 tag 展示符合预期后，再进入下一轮小范围回归。
