# 资料领取与同队代领设计

生成时间：2026-07-02  
适用范围：现场证件扫码确认中的资料领取。  
设计阶段：先输出设计文档，不进入编码。

## 1. 设计目标

现有资料领取状态直接写在被扫证件上：

```text
material_status
material_time
material_receiver_name
material_receiver_phone
material_receiver_id_suffix
```

当前问题：

- 团队证件和个人成员之间的领取关系不清楚。
- 资料领取是否代表全队不清楚。
- 代领人和被领取人关系没有被结构化表达。
- 重复领取、跨队代领、防纠纷追溯需要更明确的状态模型。

第一期新规则：

1. 资料领取按个人领取。
2. 可以本人领取。
3. 可以同队成员代领。
4. 只有同一 `team_code` 的队员/队长可以代领。
5. 被领取人是 `subject`。
6. 代领人是 `delegate`。
7. 发资料工作人员是 `operator`。
8. `operation_state` 记录在被领取人主体上。
9. 代领信息写入 `delegate_*` 字段。
10. 被代领人 PC / 小程序证件展示中显示已领取、代领人、领取时间。
11. 不允许跨队代领。
12. 不允许重复领取。
13. 如被领取人已领取，提示已领取，不再写业务状态。
14. 第一阶段代领采用二次扫码，不用手工输入代领人替代身份校验。

## 2. 三类角色

### 2.1 subject 被领取人

资料归属主体。

第一期固定为个人主体：

```text
subject_type = USER
subject_code = 被领取人 user_id
```

口径约束：

- `MATERIAL` 操作的 `subject_type` 永远是 `USER`。
- `subject_code` 为被领取人的 `userId`；如果没有稳定 userId，才使用稳定成员编码。
- 团队证件只能作为资料领取入口，不能作为资料领取状态主体。
- 不允许写 `TEAM + MATERIAL` 作为状态事实源。

状态写入：

```text
competition_scene_subject_operation_state
operation_type = MATERIAL
```

### 2.2 delegate 代领人

实际拿走资料的人。

可能是：

- 本人。
- 同队队员。
- 同队队长。

字段：

```text
delegate_user_id
delegate_name
delegate_credential_id
delegate_relation
```

### 2.3 operator 发资料工作人员

扫码并确认资料发放的工作人员。

字段：

```text
operator_user_id
operator_name
```

operator 不等于 delegate。

## 3. operation_state 写入规则

资料领取成功后写入：

```text
competition_series_id = 当前赛事系列 ID
scope_type = COMPETITION
scope_ref_id = competition_series_id
subject_type = USER
subject_code = 被领取人 user_id
operation_type = MATERIAL
operation_status = DONE
operation_time = 当前时间
credential_id = 被领取人或被扫证件 ID
operator_user_id = 发资料工作人员用户 ID
operator_name = 发资料工作人员姓名
delegate_user_id = 代领人用户 ID
delegate_name = 代领人姓名
delegate_credential_id = 代领人证件 ID
delegate_relation = SELF / TEAM_MEMBER
```

去重键：

```text
competition_series_id
COMPETITION
competition_series_id
USER
被领取人 user_id
MATERIAL
```

含义：

同一个大赛下，同一个人只能领取一次资料。

## 4. 本人领取规则

本人领取条件：

```text
delegate_user_id = subject_user_id
```

写入：

```text
delegate_relation = SELF
```

允许情况：

- 本人出示自己的个人证件。
- 本人所在团队证件可解析到本人并选择本人作为被领取人。

第一期建议页面：

- 发资料工作人员先扫被领取人证件。
- 小程序展示“本人领取”和“同队代领”。
- 选择“本人领取”时直接确认。
- 后端写入 `subject = 被领取人`、`delegate = 被领取人`、`operator = 发资料工作人员`。

## 5. 同队代领规则

允许同队代领：

```text
subject.team_code = delegate.team_code
delegate.role in (CAPTAIN, MEMBER)
```

其中：

- subject 是被领取人。
- delegate 是拿资料的人。
- operator 是发资料工作人员。

写入：

```text
delegate_relation = TEAM_MEMBER
```

必须记录：

- 代领人用户 ID。
- 代领人姓名。
- 代领人证件 ID。
- 代领关系。
- 操作人。
- 操作时间。

第一期交互：

1. 发资料工作人员先扫被领取人证件。
2. 小程序展示“本人领取”和“同队代领”。
3. 选择“同队代领”后，进入二次扫码。
4. 工作人员再扫代领人证件。
5. 后端校验代领人与被领取人属于同一 `team_code`。
6. 校验通过后写入：
   - `subject = 被领取人`；
   - `delegate = 代领人`；
   - `operator = 发资料工作人员`。
7. 校验不通过时拒绝，不写业务状态。

## 6. 禁止跨队代领

跨队判断：

```text
subject.team_code != delegate.team_code
```

处理：

- 拒绝领取。
- 返回明确错误提示。
- 不写 `operation_state`。
- 可写失败 `operation_log`，用于现场追踪。

建议错误码：

```text
MATERIAL_DELEGATE_TEAM_MISMATCH
```

## 7. 禁止重复领取

领取前查询：

```text
competition_scene_subject_operation_state
operation_type = MATERIAL
operation_status = DONE
scope_type = COMPETITION
scope_ref_id = competition_series_id
subject_type = USER
subject_code = 被领取人 user_id
```

如果已存在：

1. 返回“该人员资料已领取”。
2. 返回已有领取摘要。
3. 不再写业务状态。
4. 可写 `DUPLICATE` 操作流水。

建议错误码：

```text
MATERIAL_ALREADY_RECEIVED
```

## 8. 扫码确认参数建议

现有 confirm 请求已有：

```text
credentialToken
qrContent
operationType
receiverName
receiverPhone
receiverIdSuffix
operatorOpenId
operatorPhone
scanIp
deviceInfo
```

升级后建议资料领取采用二次扫码参数：

```text
subjectUserId
delegateCredentialToken
delegateQrContent
```

兼容规则：

- 如果不传 `subjectUserId`，且被扫证件只对应一个用户，则默认该用户为 subject。
- 选择本人领取时不传 `delegateCredentialToken/delegateQrContent`，默认 delegate 为 subject。
- 选择同队代领时必须传 `delegateCredentialToken` 或 `delegateQrContent`。
- 后端通过代领人证件解析 `delegateUserId/delegateName/delegateCredentialId`。
- 如果传了 `receiverName/receiverPhone/receiverIdSuffix`，可作为代领信息补充或旧字段兼容。

注意：

- 不把 `receiverName` 当唯一身份依据。
- 不把 `credentialNo` 当权限。
- 代领权限必须通过被领取人证件、代领人证件、用户、团队关系判断。
- 第一阶段不使用手工输入代领人信息绕过二次扫码。

## 9. 证件展示规则

被领取人 PC / 小程序证件展示中应显示：

```text
已领取
领取时间
领取方式
代领人
```

本人领取：

```text
已领取
本人领取
领取时间
```

同队代领：

```text
已领取
同队成员代领
代领人：xxx
领取时间：yyyy-MM-dd HH:mm:ss
```

如果旧字段存在但新状态不存在：

- 页面回退显示旧 `material_status/material_time/material_receiver_name`。
- 但新功能上线后优先展示 `operation_state`。

## 10. 团队证件与资料领取

第一期明确：

资料领取按个人领取，不按团队一次性完成。

团队证件扫码后可以作为入口，但必须落到个人主体：

- 如果团队只有一个可领取成员，默认该成员。
- 如果团队有多个成员，工作人员需要选择被领取人。
- 如果由同队其他成员代领，必须二次扫码代领人证件并记录 delegate。

团队证件本身不再作为“全队资料已领取”的唯一依据。
系统不允许写 `TEAM + MATERIAL` 作为资料领取状态事实源。

## 11. 操作流水规则

成功领取：

- 写 `operation_state`。
- 写 `operation_log`，结果 `PASS`。
- 兼容更新相关 credential 旧字段。

重复领取：

- 不写新的业务状态。
- 可写 `operation_log`，结果 `DUPLICATE`。

跨队代领：

- 不写业务状态。
- 可写 `operation_log`，结果 `FAIL`。

## 12. 兼容旧字段

旧字段保留：

```text
material_status
material_time
material_receiver_name
material_receiver_phone
material_receiver_id_suffix
material_operator_id
material_operator_name
```

兼容更新建议：

- 本人领取：旧 `material_receiver_name` 写本人姓名。
- 同队代领：旧 `material_receiver_name` 写代领人姓名。
- 新状态表保留被领取人与代领人的结构化关系。

## 13. 待人工确认点

1. 团队资料是否未来需要“一次领取全队资料”的快捷能力。
2. 指导教师是否可以代领学生资料。
3. 队长是否可以代领全队资料并批量写个人状态。
4. 工作人员是否需要扫描代领人证件，还是手工选择代领人。
5. 没有用户账号的导入人员是否允许作为 subject。
6. 没有用户账号的导入人员是否允许作为 delegate。
7. 资料领取是否需要领取数量、物料包编号等字段。

## 14. 禁止事项

1. 不用伪赛场安排。
2. 不恢复 `MIXED`。
3. 不把 `credentialNo` 当权限。
4. 不把 `operation_log` 作为唯一状态源。
5. 不删除旧字段。
6. 不破坏现有赛场发证。
7. 不修改报名、支付、成绩、证书主流程。
8. 不连接生产数据库。
