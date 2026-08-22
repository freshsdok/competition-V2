# 通用评审模块第二包执行报告

## 1. 查找到的竞赛业务表和字段

本包以当前仓库实际代码为准，未猜测不存在的表名。

- 报名表：`competition_apply_info`，实体 `CompetitionApplyInfo`
  - 关键字段：`member_id`、`competition_series_id`、`user_id`、`team_code`、`team_name`、`team_sort`、`user_name`、`phone`、`email`、`org_id`、`org_name`、`school_name`、`competition_role_name`、`leader_teacher_id`、`leader_teacher`、`leader_teacher_phone`、`leader_teacher_email`、`competition_track_id`、`competition_track_name`、`second_level_code`、`second_level_name`
  - 使用 Mapper：`CompetitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId`、`CompetitionApplyInfoMapper.selectCompetitionApplyTeamCode`

- 团队表：`team_manager_info`，实体 `TeamManagerInfo`
  - 关键字段：`team_id`、`team_code`、`team_name`、`team_leader_id`、`school_name`、`leader_teacher`、`leader_teacher_phone`、`leader_teacher_email`、`competition_track_id`、`competition_track_name`、`second_level_code`、`second_level_name`
  - 使用 Mapper：`TeamManagerInfoMapper.selectTeamManagerInfoByTeamCode`

- 团队成员关系表：`team_member_rela`，实体 `TeamMemberRela`
  - 已识别字段：`rela_id`、`team_code`、`user_id`、`user_name`、`team_role`、`phone`、`email`、`org_id`、`org_name`、`instructor`、`instructor_phone`、`instructor_email`
  - 本包主同步链路未直接依赖该表，因为 `competition_apply_info` 已提供报名成员、角色、排序和联系方式。

- 现场参赛证表：`competition_scene_credential`，实体 `CompetitionSceneCredential`
  - 关键字段：`credential_id`、`credential_no`、`credential_type`、`credential_status`、`team_code`、`team_name`、`member_id`、`user_id`、`user_name`、`phone`、`email`、`competition_role_name`
  - 使用 Mapper：`CompetitionSceneCredentialMapper.selectCompetitionSceneCredentialList`

## 2. 完成的导入逻辑

- `POST /review/object/import-from-business` 已支持 `sourceBizType=TEAM` 和 `sourceBizType=REGISTRATION`。
- TEAM 导入：按团队编号/团队 ID 查询团队，按 `team_code` 查询报名成员，按有效证件同步参赛证映射。
- REGISTRATION 导入：按 `member_id` 查询报名记录，再根据 `team_code` 补齐团队成员和证件。
- 非 competition 来源或未知业务类型保留通用占位导入能力，不硬编码未知业务表。

## 3. 评审对象字段映射规则

- `review_object.object_name`：优先团队名称/报名团队名称。
- `object_type`：默认 `PROJECT`。
- `source_module`：默认 `competition`。
- `source_biz_type`：`TEAM` 或 `REGISTRATION`。
- `source_biz_id`：导入入参中的外部业务 ID。
- `source_team_id`：真实 `team_code`。
- `source_registration_id`：报名 `member_id`。
- `created_from`：`BUSINESS_IMPORTED`。
- `submit_status`：新建默认为 `DRAFT`。
- `object_code`：TEAM 使用团队编号，REGISTRATION 使用报名 ID，避免同队多报名对象撞码。

## 4. 成员同步规则

- 从 `competition_apply_info` 同步 `review_object_member`。
- 队长/负责人识别为 `LEADER`，普通参赛人识别为 `MEMBER`，指导教师识别为 `TEACHER`。
- 带队老师/联系人字段同步为 `CONTACT`。
- 同步 `user_id`、`person_id(member_id)`、姓名、电话、邮箱、单位、证件编号和来源业务 ID。

## 5. 填报权限同步规则

- 默认授权模式为 `LEADER`：负责人和联系人获得 `EDIT_SUBMIT`。
- `ALL_MEMBERS`：全体成员获得权限，负责人/联系人为 `EDIT_SUBMIT`，其他成员为 `EDIT`。
- `CONTACT`：仅联系人获得 `EDIT_SUBMIT`。
- `SPECIFIED`：按 `specifiedUserIds` 授权 `EDIT_SUBMIT`。
- 权限状态统一为 `ACTIVE`，新增枚举 `ReviewPermissionStatus`。

## 6. 参赛证映射同步规则

- 从 `competition_scene_credential` 中同步 `credential_status=EFFECTIVE` 的证件。
- `PARTICIPANT`、历史 `COMPETITOR` 映射为 `CONTESTANT`；`TEACHER`、`EXPERT`、`STAFF` 分别映射到评审模块同名证件类型。
- 写入 `review_object_certificate_ref`，`valid_status=VALID`。
- 同一个评审对象可关联多个参赛证。

## 7. 重复导入处理规则

- 判断条件：`activity_id + source_module + source_biz_type + source_biz_id`。
- `overwriteExisting=false`：跳过并写审计日志。
- `overwriteExisting=true`：更新评审对象基础信息，逻辑清理旧成员和旧权限，将旧有效证件映射标记为 `INVALID`，再重新同步当前有效证件。
- 不物理删除旧参赛证映射。

## 8. 完成和增强的接口

- 增强 `POST /review/object/import-from-business`
  - 新增入参：`permissionUserMode`、`overwriteExisting`、`syncCertificate`、`specifiedUserIds`
  - 返回：总数、成功数、跳过数、失败数、对象 ID、跳过项、失败项。

- 增强 `GET /review/object/certificate/resolve`
  - 支持可选 `sessionId`。
  - 只返回 `valid_status=VALID` 的映射。
  - 返回 `matchedCount`、`candidates`、对象提交状态、证件类型、来源团队/报名 ID、场次归属和警告信息。

- 增强 `POST /review/session/{sessionId}/current-object`
  - 校验场次存在、对象同活动、对象在当前场次对象列表内、轮次一致、对象未作废且已锁定。
  - 保持第一包约束：只切换当前对象，不改变 `review_assignment`，不授予额外评分权限。

## 9. 测试结果

执行命令：

```bash
mvn test -pl teaching-modules/teaching-competition -am -DskipTests=false -DfailIfNoTests=false
```

结果：

- 编译通过。
- 测试通过：`Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`。
- 覆盖内容包括：团队导入生成对象、成员、权限、外部关联、参赛证映射；证件解析；重复导入跳过；覆盖导入证件失效和重建；设置当前对象场次校验；未锁定对象拒绝；评分记录提交；平均分结果生成与评价结论更新。

## 10. 已知问题

- 本包完成 Service 层和 Mapper 层适配，未执行真实数据库集成导入；真实数据联调时需用测试库验证各业务表数据质量。
- `team_member_rela` 已审查但未纳入主链路，当前以 `competition_apply_info` 为权威成员来源。
- 竞赛业务若后续新增独立“项目名称/作品名称”字段，应进一步优先映射到 `review_object.object_name`。
- 未开发任何前端页面，符合第二包边界。

## 11. 第三包建议

- 增加管理端导入预览页面和导入结果明细展示。
- 增加填报端对象详情、材料上传和提交/撤回流程。
- 增加秘书端扫码最小可用页面，调用增强后的证件解析和当前对象切换接口。
- 增加专家端“我的评审任务”权限校验和评分表渲染。
- 基于真实测试库补充 MyBatis/接口级集成测试。
