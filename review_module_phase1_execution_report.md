# 通用评审模块第一包执行报告

## 1. 完成的数据表

迁移脚本已新增 23 张表：

`review_activity`、`review_round`、`review_rule`、`review_criteria`、`review_object`、`review_object_member`、`review_object_material`、`review_submission_permission`、`review_object_external_ref`、`review_object_certificate_ref`、`reviewer_profile`、`review_activity_user_role`、`review_panel`、`review_panel_member`、`review_assignment`、`review_record`、`review_score_detail`、`review_session`、`review_session_object`、`review_session_event_log`、`review_result`、`review_result_publish_log`、`review_audit_log`。

重点约束已落表：

- `review_object_certificate_ref` 独立于外部业务关联表，作为现场扫码第一查询入口。
- `review_object` 增加 `activity_id + object_code` 唯一索引。
- `review_assignment` 增加 `activity_id + round_id + object_id + reviewer_user_id` 唯一索引。
- `review_session` 保留 `current_object_id/current_started_time`。
- `review_score_detail` 冗余保存 `criteria_name/weight` 等快照字段。
- `review_result` 保留 `calculated_score` 和 `evaluation_conclusion` 分离字段。

## 2. 完成的实体类

新增包：`com.teaching.competition.review`

实体位于：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain`

已完成 23 个表实体，并统一继承 `ReviewBaseEntity`：

`ReviewActivity`、`ReviewRound`、`ReviewRule`、`ReviewCriteria`、`ReviewObject`、`ReviewObjectMember`、`ReviewObjectMaterial`、`ReviewSubmissionPermission`、`ReviewObjectExternalRef`、`ReviewObjectCertificateRef`、`ReviewerProfile`、`ReviewActivityUserRole`、`ReviewPanel`、`ReviewPanelMember`、`ReviewAssignment`、`ReviewRecord`、`ReviewScoreDetail`、`ReviewSession`、`ReviewSessionObject`、`ReviewSessionEventLog`、`ReviewResult`、`ReviewResultPublishLog`、`ReviewAuditLog`。

## 3. 完成的枚举

枚举位于：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/enums`

已完成：

`ReviewActivityStatus`、`ReviewRoundType`、`ReviewRoundStatus`、`ReviewSubmissionMode`、`ReviewObjectType`、`ReviewObjectStatus`、`ReviewObjectCreatedFrom`、`ReviewMemberRole`、`ReviewMaterialType`、`ReviewRuleScoreMode`、`ReviewCriteriaType`、`ReviewUserRoleType`、`ReviewAssignmentStatus`、`ReviewRecordStatus`、`ReviewSessionStatus`、`ReviewSessionObjectStatus`、`ReviewCheckinStatus`、`ReviewCertificateType`、`ReviewCertificateValidStatus`、`ReviewResultStatus`、`ReviewPublishMode`、`ReviewEventType`、`ReviewPermissionType`、`ReviewCertificateResolveSourceType`。

## 4. 完成的接口

Controller 位于：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller`

已完成基础接口：

- `/review/activity`：新增、修改、详情、列表、删除。
- `/review/round`：新增、修改、详情、列表、删除。
- `/review/object`：新增、修改、详情、列表、删除。
- `/review/rule`：新增、修改、详情、列表、删除。
- `/review/criteria`：新增、修改、详情、列表、删除。
- `/review/reviewer`：新增、修改、详情、列表、删除。
- `/review/assignment`：新增、修改、详情、列表、删除。
- `/review/session`：新增、修改、详情、列表、删除。

已完成专项接口：

- `POST /review/object/import-from-business`
- `POST /review/object/certificate`
- `GET /review/object/certificate/list`
- `GET /review/object/certificate/resolve?activityId=&certificateCode=`
- `POST /review/session/{sessionId}/current-object`
- `GET /review/session/{sessionId}/current-object`
- `POST /review/session/object`
- `GET /review/session/object/list`
- `GET /review/session/event-log/list`
- `POST /review/record/draft`
- `POST /review/record/submit`
- `GET /review/record/{id}`
- `GET /review/record/my-list`
- `POST /review/result/generate`
- `PUT /review/result/{id}/conclusion`
- `POST /review/result/{id}/publish`
- `GET /review/result/list`

所有新增 Controller 已按现有项目风格增加 `@RequiresPermissions` 和 `@Log`。

## 5. 骨架接口说明

以下接口属于第一包骨架或轻量实现：

- 外部业务导入：已创建 `review_object` 与 `review_object_external_ref`，真实报名、团队、参赛证表对接留 TODO。
- 填报权限同步：表和 Mapper 已具备，导入时真实授权规则留第二包接入。
- 成员与材料：表、实体、Mapper、基础 Service 已具备，暂未开放完整业务页面流程。
- 专家组与专家组成员：表、实体、Mapper、基础 Service 已具备，复杂分组和匹配留后续。
- 评分记录：支持草稿和提交，复杂评分规则、范围校验、规则版本化留后续。
- 结果汇总：当前按已提交 `review_record.total_score` 简单平均分生成。

## 6. 第二包待办

- 管理端配置页面：活动、轮次、规则、指标、对象、专家、场次维护。
- 被评审人填报端：资料填报、撤回申请、截止锁定、材料上传。
- 专家 PC 端：左材料右评分、指标校验、提交确认。
- 评审秘书移动端：扫码解析、当前对象切换、下一位、缺席、跳过。
- 外部竞赛业务真实导入：报名、团队、成员、参赛证映射、填报权限同步。
- 专家任务权限强校验：结合登录用户和 `review_assignment` 限制评分范围。
- 复杂多轮流转、专家匹配、回避、发布可见性、结果排名规则。

## 7. 数据库迁移脚本路径

`db/migration/20260703_review_module_phase1.sql`

## 8. 本地编译和测试结果

已执行并通过：

- `mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`
- `mvn -pl teaching-modules/teaching-competition -am test`

测试结果：

- `ReviewModulePhase1SmokeTest`
- 4 个用例通过，覆盖外部导入与参赛证解析、现场当前对象切换与事件日志、评审记录提交、简单平均分结果生成与结论填写。

数据库脚本验证：

- 在 MySQL 5.7 临时库 `review_phase1_tmp` 执行成功。
- 验证创建表数量：23。
- 验证后已删除临时库。

启动验证：

- 默认 `9205` 端口被本机既有 Java 进程占用。
- 使用临时端口 `19205` 执行短时启动验证成功，看到 `Started TeachingCompetitionApplication` 与赛事模块启动成功日志，随后主动停止进程。

## 9. 已知问题

- 本包未接入真实竞赛业务表，导入接口中的成员、填报权限、参赛证映射同步仍为 TODO。
- 当前结果汇总仅按提交记录总分平均，不处理去最高最低分、权重、专家组规则、缺失评审等复杂逻辑。
- 当前评分权限仅在传入 `assignmentId` 时做一致性校验，专家端上线时需要结合登录态强制校验。
- `review_result` 没有暴露直接修改 `calculated_score` 的 HTTP 接口；后续也应保持只允许汇总逻辑写入系统计算分。

## 10. 下一包建议

优先顺序建议：

1. 管理端基础配置页面和接口联调。
2. 外部竞赛业务导入适配，补齐成员、填报权限、参赛证映射。
3. 秘书端扫码解析和当前对象切换联调。
4. 专家端评分页面，接入分配权限校验和评分明细快照。
5. 结果汇总规则扩展与发布可见性配置。
