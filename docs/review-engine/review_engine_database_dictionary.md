# Review Engine V1.0 数据库字典

## 1. 基准与约定

本字典以以下迁移脚本和对应实体/Mapper 为事实来源：

- `db/migration/20260703_review_module_phase1.sql`
- `db/migration/20260703_review_module_phase4_submission.sql`
- `db/migration/20260706_review_module_phase7_result_publish.sql`
- `db/migration/20260708_review_file_task_import.sql`
- `review/domain`、`review/mapper`、`resources/mapper/review`

共 24 张 Review Engine 业务表。类型为 MySQL 类型；“是否必填”严格按最终迁移 DDL 的 `NOT NULL` 判断，不代表所有应用层校验。迁移未声明外键，文中“关联”均为逻辑关联。

### 1.1 所有表共有字段

所有 24 张表均包含下列字段。后续每张表的字段表列出该表其余全部业务字段。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`id`|bigint|主键，自增|是|
|`remark`|varchar(500)|备注|否|
|`create_by`|varchar(64)|创建者|否|
|`create_time`|datetime|创建时间|否|
|`update_by`|varchar(64)|更新者|否|
|`update_time`|datetime|更新时间|否|
|`del_flag`|char(1)|逻辑删除标识：0 正常、1 删除；默认 0|是|

## 2. 活动与评分规则

### 2.1 `review_activity`

用途：评审活动顶层配置，定义来源、对象类型、填报/评审时间、匿名和发布策略。

主键：`id`。索引：唯一索引 `uk_review_activity_code(activity_code)`；普通索引 `idx_review_activity_status(status)`、`idx_review_activity_source(source_module, source_biz_type)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_name`|varchar(200)|评审活动名称|是|
|`activity_code`|varchar(100)|评审活动编码|是|
|`activity_type`|varchar(50)|活动类型|否|
|`source_module`|varchar(100)|来源业务模块|否|
|`source_biz_type`|varchar(100)|来源业务类型|否|
|`object_type`|varchar(50)|评审对象类型|否|
|`submission_mode`|varchar(50)|填报模式|否|
|`submit_start_time`|datetime|填报开始时间|否|
|`submit_deadline`|datetime|填报截止时间|否|
|`review_start_time`|datetime|评审开始时间|否|
|`review_end_time`|datetime|评审结束时间|否|
|`anonymous_mode`|varchar(50)|匿名模式|否|
|`result_publish_mode`|varchar(50)|结果发布模式|否|
|`status`|varchar(50)|活动状态|否|
|`description`|text|活动说明|否|

状态值：`DRAFT`、`SUBMITTING`、`SUBMIT_CLOSED`、`REVIEWING`、`SUMMARYING`、`PUBLISHED`、`ARCHIVED`、`DISABLED`。

### 2.2 `review_round`

用途：活动内评审轮次及其类型、时间和评分规则绑定。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`rule_id -> review_rule.id`。索引：`idx_review_round_activity(activity_id)`、`idx_review_round_type(round_type)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|所属评审活动|是|
|`round_name`|varchar(200)|轮次名称|是|
|`round_no`|int|轮次序号|是|
|`round_type`|varchar(50)|轮次类型|否|
|`start_time`|datetime|开始时间|否|
|`end_time`|datetime|结束时间|否|
|`rule_id`|bigint|绑定的评分规则 ID|否|
|`status`|varchar(50)|轮次状态|否|
|`description`|text|轮次说明|否|

轮次类型：`MATERIAL_REVIEW`、`ONSITE_DEFENSE`、`QUALIFICATION_CHECK`、`GROUP_REVIEW`、`FINAL_CONFIRM`。状态：`DRAFT`、`NOT_STARTED`、`IN_PROGRESS`、`ENDED`、`ARCHIVED`、`DISABLED`。

### 2.3 `review_rule`

用途：评分规则主表，定义计分模式、总分和匿名策略。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`。索引：`idx_review_rule_activity_round(activity_id, round_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|限定评审轮次 ID；为空时可作为活动级规则|否|
|`rule_name`|varchar(200)|规则名称|是|
|`score_mode`|varchar(50)|计分模式|否|
|`total_score`|decimal(10,2)|规则总分|否|
|`anonymous_mode`|varchar(50)|匿名模式|否|
|`description`|text|规则说明|否|
|`enabled`|char(1)|是否启用|否|

计分模式：`SUM`、`WEIGHTED_SUM`、`AVERAGE`。规则必须校验通过后才能启用和绑定轮次。

### 2.4 `review_criteria`

用途：评分规则下的评分指标，可通过 `parent_id` 构成层级。

主键：`id`。逻辑关联：`rule_id -> review_rule.id`，`parent_id -> review_criteria.id`。索引：`idx_review_criteria_rule(rule_id, parent_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`rule_id`|bigint|评分规则 ID|否|
|`parent_id`|bigint|父指标 ID|否|
|`criteria_name`|varchar(200)|指标名称|是|
|`criteria_desc`|text|指标说明|否|
|`score_type`|varchar(50)|评分项类型|否|
|`min_score`|decimal(10,2)|最小分|否|
|`max_score`|decimal(10,2)|最大分|否|
|`weight`|decimal(10,4)|权重|否|
|`required`|char(1)|是否必填|否|
|`options_json`|text|单选项等选项 JSON|否|
|`sort_order`|int|排序|否|
|`enabled`|char(1)|是否启用|否|

评分项类型：`NUMBER`、`SINGLE_CHOICE`、`TEXT`。

## 3. 评审对象与填报

### 3.1 `review_object`

用途：通用被评对象主表，保存评审所需业务快照及外部来源摘要。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`。索引：唯一索引 `uk_review_object_code(activity_id, object_code)`；普通索引 `idx_review_object_source(source_module, source_biz_type, source_biz_id)`、`idx_review_object_status(activity_id, submit_status)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`object_code`|varchar(100)|活动内对象编号|是|
|`object_name`|varchar(300)|对象名称|是|
|`object_type`|varchar(50)|对象类型|否|
|`summary`|text|对象摘要|否|
|`subject_code_1`|varchar(100)|学科代码 1|否|
|`subject_code_2`|varchar(100)|学科代码 2|否|
|`subject_code_3`|varchar(100)|学科代码 3|否|
|`category_codes`|text|分类字段|否|
|`keywords`|text|关键词|否|
|`org_name`|varchar(200)|所属单位|否|
|`contact_name`|varchar(100)|联系人|否|
|`contact_phone`|varchar(100)|联系方式|否|
|`contact_email`|varchar(200)|联系邮箱|否|
|`submit_status`|varchar(50)|填报状态|否|
|`submit_time`|datetime|提交时间|否|
|`submitted_by`|bigint|提交用户 ID|否|
|`locked_time`|datetime|锁定时间|否|
|`invalid_time`|datetime|作废时间|否|
|`created_from`|varchar(50)|创建来源|否|
|`source_module`|varchar(100)|来源模块|否|
|`source_biz_type`|varchar(100)|来源业务类型|否|
|`source_biz_id`|varchar(100)|外部业务 ID|否|
|`source_team_id`|varchar(100)|来源团队 ID|否|
|`source_registration_id`|varchar(100)|来源报名 ID|否|
|`extra_data`|text|扩展字段 JSON|否|

对象类型：`PROJECT`、`TEAM`、`PERSON`、`WORK`、`OTHER`。创建来源：`OPEN`、`ASSIGNED_USER`、`BUSINESS_IMPORTED`、`ADMIN_CREATED`。填报状态：`DRAFT`、`SUBMITTED`、`WITHDRAW_REQUESTED`、`WITHDRAW_APPROVED`、`WITHDRAW_REJECTED`、`LOCKED`、`INVALID`、`REVIEWING`、`REVIEWED`、`ARCHIVED`。

### 3.2 `review_object_member`

用途：对象负责人、成员、联系人、指导教师等人员快照。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`object_id -> review_object.id`。索引：`idx_review_object_member_object(object_id)`、`idx_review_object_member_user(user_id)`、`idx_review_object_member_cert(certificate_code)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`object_id`|bigint|评审对象 ID|否|
|`user_id`|bigint|系统用户 ID|否|
|`person_id`|varchar(100)|外部人员 ID|否|
|`member_name`|varchar(100)|成员姓名|否|
|`member_role`|varchar(50)|成员角色|否|
|`is_primary`|char(1)|是否主要成员|否|
|`phone`|varchar(100)|电话|否|
|`email`|varchar(200)|邮箱|否|
|`org_name`|varchar(200)|单位|否|
|`certificate_id`|varchar(100)|证件 ID|否|
|`certificate_code`|varchar(100)|证件编码|否|
|`certificate_type`|varchar(50)|证件类型|否|
|`source_module`|varchar(100)|来源模块|否|
|`source_biz_id`|varchar(100)|来源业务 ID|否|
|`sort_order`|int|排序|否|

成员角色：`LEADER`、`MEMBER`、`CONTACT`、`TEACHER`、`OTHER`。

### 3.3 `review_object_material`

用途：评审对象材料及其外部文件来源追踪。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`object_id -> review_object.id`。索引：`idx_review_object_material_object(object_id)`；增量迁移增加 `idx_review_material_source(source_module, source_biz_type, source_biz_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`object_id`|bigint|评审对象 ID|否|
|`material_name`|varchar(300)|材料名称|否|
|`material_type`|varchar(50)|材料类型|否|
|`file_name`|varchar(300)|文件名称|否|
|`file_url`|varchar(1000)|文件地址|否|
|`file_size`|bigint|文件大小|否|
|`mime_type`|varchar(200)|MIME 类型|否|
|`file_ext`|varchar(50)|文件扩展名|否|
|`visible_to_reviewer`|char(1)|专家是否可见|否|
|`sort_order`|int|排序|否|
|`upload_by`|bigint|上传用户 ID|否|
|`upload_time`|datetime|上传时间|否|
|`status`|varchar(50)|材料状态|否|
|`source_module`|varchar(100)|来源模块；由 20260708 增量增加|否|
|`source_biz_type`|varchar(100)|来源业务类型；由 20260708 增量增加|否|
|`source_biz_id`|varchar(100)|来源业务 ID；由 20260708 增量增加|否|
|`source_material_key`|varchar(1000)|来源材料幂等/追踪标识；由 20260708 增量增加|否|

材料类型：`DECLARATION`、`PPT`、`VIDEO`、`IMAGE`、`PDF`、`DOC`、`ZIP`、`OTHER`。业务代码使用 `NORMAL`、`DELETED` 材料状态。

### 3.4 `review_submission_permission`

用途：给用户/组织授予某个对象的填报编辑与提交权限。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`object_id -> review_object.id`。索引：`idx_review_permission_user(activity_id, user_id)`、`idx_review_permission_object(object_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`object_id`|bigint|评审对象 ID|否|
|`user_id`|bigint|用户 ID|否|
|`org_id`|bigint|组织 ID|否|
|`permission_type`|varchar(50)|权限类型|否|
|`status`|varchar(50)|权限状态|否|
|`source_module`|varchar(100)|授权来源模块|否|
|`source_biz_id`|varchar(100)|授权来源业务 ID|否|
|`granted_by`|bigint|授权人|否|
|`granted_time`|datetime|授权时间|否|
|`used_time`|datetime|使用时间|否|

权限类型：`CREATE`、`EDIT`、`SUBMIT`、`EDIT_SUBMIT`。状态：`ACTIVE`、`USED`、`EXPIRED`、`DISABLED`。

### 3.5 `review_object_external_ref`

用途：连接 Review Object 与一个或多个外部业务记录，是通用业务适配的核心关联表。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`object_id -> review_object.id`。索引：`idx_review_external_object(object_id)`、`idx_review_external_source(source_module, source_biz_type, source_biz_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`object_id`|bigint|评审对象 ID|否|
|`source_module`|varchar(100)|来源模块|否|
|`source_biz_type`|varchar(100)|来源业务类型|否|
|`source_biz_id`|varchar(100)|来源业务 ID|否|
|`source_biz_code`|varchar(100)|来源业务编码|否|
|`source_team_id`|varchar(100)|来源团队 ID|否|
|`source_registration_id`|varchar(100)|来源报名 ID|否|
|`relation_type`|varchar(50)|关联类型|否|
|`extra_data`|text|扩展字段 JSON|否|

### 3.6 `review_object_certificate_ref`

用途：参赛证/人员证件与 Review Object 的映射，支持现场证件解析。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`object_id -> review_object.id`，`member_id -> review_object_member.id`。索引：`idx_cert_activity_code(activity_id, certificate_code)`、`idx_cert_object(object_id)`、`idx_cert_source(source_module, source_biz_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`certificate_id`|varchar(100)|证件 ID|否|
|`certificate_code`|varchar(100)|证件编码|否|
|`certificate_type`|varchar(50)|证件类型|否|
|`person_id`|varchar(100)|人员 ID|否|
|`user_id`|bigint|用户 ID|否|
|`member_id`|bigint|对象成员 ID|否|
|`member_name`|varchar(100)|成员姓名|否|
|`member_role`|varchar(50)|成员角色|否|
|`source_module`|varchar(100)|来源模块|否|
|`source_biz_id`|varchar(100)|来源业务 ID|否|
|`source_team_id`|varchar(100)|来源团队 ID|否|
|`source_registration_id`|varchar(100)|来源报名 ID|否|
|`valid_status`|varchar(50)|有效状态|否|

证件类型：`CONTESTANT`、`TEACHER`、`EXPERT`、`STAFF`。有效状态：`VALID`、`INVALID`、`REVOKED`、`REPLACED`。

### 3.7 `review_object_submit_log`

用途：记录对象填报草稿、提交、撤回、锁定、作废和材料变更的状态轨迹。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`object_id -> review_object.id`。索引：`idx_review_submit_log_object(object_id)`、`idx_review_submit_log_activity(activity_id, action_type)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`object_id`|bigint|评审对象 ID|是|
|`action_type`|varchar(50)|操作类型|是|
|`before_status`|varchar(50)|操作前状态|否|
|`after_status`|varchar(50)|操作后状态|否|
|`operator_user_id`|bigint|操作人 ID|否|
|`operator_name`|varchar(100)|操作人姓名|否|
|`operate_time`|datetime|操作时间|否|
|`action_reason`|varchar(500)|操作原因或意见|否|

操作类型：`SAVE_DRAFT`、`SUBMIT`、`WITHDRAW_REQUEST`、`WITHDRAW_APPROVE`、`WITHDRAW_REJECT`、`LOCK`、`INVALID`、`MATERIAL_ADD`、`MATERIAL_DELETE`。

## 4. 专家、角色与任务分配

### 4.1 `reviewer_profile`

用途：评审专家档案、学科标签、关键词与任务容量信息。

主键：`id`。逻辑关联：`user_id -> sys_user.user_id`。索引：`idx_reviewer_profile_user(user_id)`、`idx_reviewer_profile_status(status)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`user_id`|bigint|系统用户 ID|否|
|`reviewer_name`|varchar(100)|评审人姓名|是|
|`org_name`|varchar(200)|单位|否|
|`phone`|varchar(100)|电话|否|
|`email`|varchar(200)|邮箱|否|
|`subject_code_1`|varchar(100)|学科代码 1|否|
|`subject_code_2`|varchar(100)|学科代码 2|否|
|`subject_code_3`|varchar(100)|学科代码 3|否|
|`category_codes`|text|分类字段|否|
|`keywords`|text|关键词|否|
|`max_task_count`|int|最大任务数|否|
|`status`|varchar(50)|专家档案状态|否|

业务代码新增档案时默认状态为 `ENABLED`。

### 4.2 `review_activity_user_role`

用途：维护用户在特定评审活动内的业务角色及专家/专家组引用。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`user_id -> sys_user.user_id`，`reviewer_id -> reviewer_profile.id`，`panel_id -> review_panel.id`。索引：`idx_review_role_activity_user(activity_id, user_id)`、`idx_review_role_reviewer(reviewer_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`user_id`|bigint|用户 ID|否|
|`role_type`|varchar(50)|活动内角色类型|否|
|`reviewer_id`|bigint|评审人档案 ID|否|
|`panel_id`|bigint|专家组 ID|否|
|`enabled`|char(1)|是否启用|否|

角色类型：`ADMIN`、`OPERATOR`、`REVIEWER`、`SECRETARY`、`OBJECT_OWNER`、`AUDITOR`。

### 4.3 `review_panel`

用途：活动/轮次内的专家组，记录组长和秘书。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`leader_user_id/secretary_user_id -> sys_user.user_id`。索引：`idx_review_panel_activity_round(activity_id, round_id)`、`idx_review_panel_code(panel_code)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`panel_name`|varchar(200)|专家组名称|否|
|`panel_code`|varchar(100)|专家组编码|否|
|`leader_user_id`|bigint|组长用户 ID|否|
|`secretary_user_id`|bigint|秘书用户 ID|否|
|`status`|varchar(50)|专家组状态|否|

### 4.4 `review_panel_member`

用途：专家组成员关系。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`panel_id -> review_panel.id`，`user_id -> sys_user.user_id`，`reviewer_id -> reviewer_profile.id`。索引：`idx_review_panel_member_panel(panel_id)`、`idx_review_panel_member_user(user_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`panel_id`|bigint|专家组 ID|否|
|`user_id`|bigint|用户 ID|否|
|`reviewer_id`|bigint|评审人档案 ID|否|
|`member_role`|varchar(50)|组内角色|否|
|`status`|varchar(50)|成员状态|否|

### 4.5 `review_assignment`

用途：将某活动、轮次和对象分配给专家，是专家访问和评分的核心数据权限边界。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`object_id -> review_object.id`，`reviewer_id -> reviewer_profile.id`，`reviewer_user_id -> sys_user.user_id`，`panel_id -> review_panel.id`。索引：唯一索引 `uk_review_assignment_user(activity_id, round_id, object_id, reviewer_user_id)`；普通索引 `idx_review_assignment_reviewer(reviewer_id)`、`idx_review_assignment_status(status)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`reviewer_id`|bigint|评审人档案 ID|否|
|`reviewer_user_id`|bigint|评审专家用户 ID|否|
|`panel_id`|bigint|专家组 ID；为空时表示未限定专家组|否|
|`assignment_type`|varchar(50)|分配类型|否|
|`status`|varchar(50)|任务状态|否|
|`assigned_by`|bigint|分配人|否|
|`assigned_time`|datetime|分配时间|否|
|`submitted_time`|datetime|专家提交时间|否|

状态：`ASSIGNED`、`IN_PROGRESS`、`SUBMITTED`、`RETURNED`、`LOCKED`、`CANCELLED`。

## 5. 评分与结果

### 5.1 `review_record`

用途：单个专家对单个 Assignment 的评分记录主表。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`object_id -> review_object.id`，`assignment_id -> review_assignment.id`，`reviewer_id -> reviewer_profile.id`。索引：`idx_review_record_assignment(assignment_id)`、`idx_review_record_object(activity_id, round_id, object_id)`、`idx_review_record_reviewer(reviewer_user_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是；早期兼容迁移可能为可空|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`assignment_id`|bigint|评审任务 ID|否|
|`reviewer_id`|bigint|评审人档案 ID|否|
|`reviewer_user_id`|bigint|评审用户 ID|否|
|`record_status`|varchar(50)|记录状态|否|
|`total_score`|decimal(10,2)|总分|否|
|`grade`|varchar(50)|等级|否|
|`recommendation`|varchar(100)|推荐意见|否|
|`comment_text`|text|评语|否|
|`submitted_time`|datetime|提交时间|否|
|`returned_time`|datetime|退回时间|否|
|`locked_time`|datetime|锁定时间|否|
|`invalid_time`|datetime|作废时间|否|
|`invalid_reason`|varchar(500)|作废原因|否|

状态：`DRAFT`、`SUBMITTED`、`RETURNED`、`LOCKED`、`INVALID`。20260706 迁移会为早期同名表补齐缺失字段，但不会删除旧字段，因此历史库应单独做 schema 对账。

### 5.2 `review_score_detail`

用途：评分记录的逐指标明细，同时保存指标名称、类型和权重快照。

主键：`id`。逻辑关联：`record_id -> review_record.id`，`criteria_id -> review_criteria.id`。索引：`idx_review_score_record(record_id)`、`idx_review_score_object(activity_id, round_id, object_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`record_id`|bigint|评审记录 ID|否|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`criteria_id`|bigint|评分指标 ID|否|
|`criteria_name`|varchar(200)|指标名称快照|是|
|`score_type`|varchar(50)|评分项类型快照|否|
|`score_value`|decimal(10,2)|数值得分|否|
|`option_value`|varchar(200)|选项值|否|
|`text_value`|text|文本评价|否|
|`weight`|decimal(10,4)|权重快照|否|
|`sort_order`|int|排序快照|否|

### 5.3 `review_result`

用途：按活动、轮次、对象汇总的系统计算结果及发布状态。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`object_id -> review_object.id`。索引：唯一索引 `uk_review_result_object(activity_id, round_id, object_id)`；普通索引 `idx_review_result_status(result_status)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`reviewer_count`|int|应评专家数|否|
|`submitted_count`|int|已提交数|否|
|`calculated_score`|decimal(10,2)|系统计算分；只由汇总逻辑写入|否|
|`calculated_grade`|varchar(50)|系统计算等级|否|
|`calculated_rank`|int|系统计算排名|否|
|`evaluation_conclusion`|varchar(500)|发布性评价结论，不是改分字段|否|
|`conclusion_generated_by`|bigint|结论填写人|否|
|`conclusion_generated_time`|datetime|结论填写时间|否|
|`result_status`|varchar(50)|结果状态|否|
|`generated_by`|bigint|汇总人|否|
|`generated_time`|datetime|汇总时间|否|
|`published_by`|bigint|发布人|否|
|`published_time`|datetime|发布时间|否|
|`revoked_by`|bigint|撤回人|否|
|`revoked_time`|datetime|撤回时间|否|

状态：`GENERATED`、`PUBLISHED`、`REVOKED`、`ARCHIVED`。

### 5.4 `review_result_publish_log`

用途：记录结果发布与撤回历史、范围和发布内容。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`object_id -> review_object.id`。索引：`idx_review_publish_activity(activity_id, round_id)`、`idx_review_publish_object(object_id)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`publish_scope`|varchar(50)|发布范围|否|
|`publish_content`|text|发布内容或撤回原因|否|
|`published_by`|bigint|操作人|否|
|`published_time`|datetime|操作时间|否|
|`status`|varchar(50)|发布日志状态|否|

## 6. 现场评审

### 6.1 `review_session`

用途：现场评审场次，关联轮次、专家组、秘书和当前评审对象。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`panel_id -> review_panel.id`，`current_object_id -> review_object.id`。索引：`idx_review_session_activity_round(activity_id, round_id)`、`idx_review_session_code(session_code)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`session_name`|varchar(200)|场次名称|是|
|`session_code`|varchar(100)|场次编码|是|
|`location`|varchar(200)|地点|否|
|`start_time`|datetime|开始时间|否|
|`end_time`|datetime|结束时间|否|
|`secretary_user_id`|bigint|秘书用户 ID|否|
|`panel_id`|bigint|专家组 ID|否|
|`current_object_id`|bigint|当前评审对象 ID|否|
|`current_started_time`|datetime|当前对象开始时间|否|
|`status`|varchar(50)|场次状态|否|

状态：`NOT_STARTED`、`IN_PROGRESS`、`PAUSED`、`ENDED`、`ARCHIVED`。

### 6.2 `review_session_object`

用途：场次内对象的顺序、签到和评审状态。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`session_id -> review_session.id`，`object_id -> review_object.id`。索引：`idx_session_object(session_id, object_id)`、`idx_session_sequence(session_id, sequence_no)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`session_id`|bigint|现场场次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`sequence_no`|int|场次内顺序号|否|
|`checkin_status`|varchar(50)|签到/到场状态|否|
|`review_status`|varchar(50)|现场评审状态|否|
|`actual_start_time`|datetime|实际开始时间|否|
|`actual_end_time`|datetime|实际结束时间|否|
|`secretary_note`|varchar(500)|秘书备注|否|

签到状态：`WAITING`、`PRESENT`、`ABSENT`、`LATE`。评审状态：`WAITING`、`REVIEWING`、`SCORED`、`COMPLETED`、`SKIPPED`、`DELAYED`。

### 6.3 `review_session_event_log`

用途：现场扫码、设置当前对象、下一位、到场、缺席、跳过等事件日志。

主键：`id`。逻辑关联：`activity_id -> review_activity.id`，`round_id -> review_round.id`，`session_id -> review_session.id`，`object_id -> review_object.id`。索引：`idx_review_event_session(session_id)`、`idx_review_event_object(object_id)`、`idx_review_event_time(event_time)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`session_id`|bigint|场次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`event_type`|varchar(50)|事件类型|否|
|`event_content`|text|事件内容|否|
|`operator_user_id`|bigint|操作人用户 ID|否|
|`event_time`|datetime|事件时间|否|

事件类型：`SCAN_CERT`、`SET_CURRENT`、`NEXT_OBJECT`、`SKIP`、`ABSENT`、`PRESENT`、`DELAY`、`PAUSE`、`RESUME`、`END`。

## 7. 审计日志

### 7.1 `review_audit_log`

用途：记录导入、填报、评分结果生成、结论、发布、撤回等业务审计事件。

主键：`id`。逻辑关联：可按活动、轮次、对象或 `biz_type + biz_id` 关联业务。索引：`idx_review_audit_biz(biz_type, biz_id)`、`idx_review_audit_object(activity_id, round_id, object_id)`、`idx_review_audit_time(operate_time)`。

|字段|类型|说明|是否必填|
|-|-|-|-|
|`activity_id`|bigint|评审活动 ID|是|
|`round_id`|bigint|评审轮次 ID|否|
|`object_id`|bigint|评审对象 ID|否|
|`biz_type`|varchar(100)|业务类型|否|
|`biz_id`|varchar(100)|业务 ID|否|
|`action_type`|varchar(100)|操作类型|否|
|`action_content`|text|操作内容|否|
|`operator_user_id`|bigint|操作人用户 ID|否|
|`operator_name`|varchar(100)|操作人名称|否|
|`operate_time`|datetime|操作时间|否|
|`ip_addr`|varchar(100)|IP 地址|否|

## 8. 外部业务关联表说明

`competition_scene_schedule_target` 不是 Review Engine 自有表，不计入上述 24 张表，但 `20260707_scene_schedule_review_binding.sql` 为其增加 `review_object_id`、`target_type`、`certificate_code`、`sequence_no` 和 `source_*` 字段，用于竞赛现场排期绑定 Review Object。相关索引包括：

- `idx_scene_target_schedule_sequence(schedule_id, sequence_no)`；
- `idx_scene_target_review_object(review_object_id)`；
- `idx_scene_target_certificate_code(certificate_code)`；
- `idx_scene_target_type(target_type)`。

## 9. 数据完整性注意事项

1. 迁移脚本没有外键，删除均以 `del_flag` 软删除为主；跨表删除和导入覆盖必须通过 Service。
2. `review_assignment` 与 `review_result` 有关键业务唯一索引；其他多对多关系主要靠应用层去重。
3. `review_score_detail` 保存指标快照，历史评分不应根据当前 `review_criteria` 反向重写。
4. `review_result.calculated_score` 是派生字段，只允许结果生成流程写入。
5. 老库可能存在早期 `review_record` 结构，部署前必须核验列、索引和历史字段。
