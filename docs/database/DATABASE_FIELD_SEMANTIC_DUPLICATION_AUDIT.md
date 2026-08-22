# 字段语义重复审计

本报告聚焦同名字段在不同业务域中的含义漂移，以及同义字段的命名分裂。结论基于测试库字段、字段注释、表名和代码引用静态分析。

## 总体判断

- `competition_id`、`competition_series_id`、`competition_main_info.id`、`competition_series_info.id` 之间存在明显历史命名分裂，部分表把赛事系列、赛事主体、赛程上下文混用为 competition。
- `team_id` 与 `team_code` 并存：`team_id` 更像内部团队主键，`team_code` 多处作为报名团队自然键或快照键，应明确事实源和快照用途。
- `status` 是最危险的泛化字段；不同域分别表示发布、审核、支付、预约、证件、评审、任务运行状态，建议避免跨域复用同一枚举解释。
- `deleted` 与 `del_flag` 同时存在，软删除语义和取值不统一，影响唯一约束和 active_key 设计。

## `competition_id`

- 出现位置：4 张表/字段。
- 代表表：`competition_main_info`（赛事主数据，赛事主数据表；注释：赛事id）；`competition_scene_notice`（赛事主数据，赛事现场通知；注释：赛事ID）；`competition_series_info`（赛事主数据，赛事系列信息表；注释：赛事id）；`user_collect`（其他，用户收藏信息表；注释：赛事id）
- 风险：同名不同义。部分场景可能指赛事主表、竞赛系列或赛事上下文，和 `competition_series_id` 并行时容易写错 JOIN。
- 建议：新增字段时使用 `competition_series_id`、`competition_main_id`、`scene_competition_id` 等明确命名；历史字段补注释。

## `competition_series_id`

- 出现位置：34 张表/字段。
- 代表表：`award_publicity`（赛事主数据，获奖公示管理主表；注释：赛事系列id(个人参赛)）；`candidate_cert_info`（其他，候选人证书表；注释：赛事系列id）；`cert_config_info`（现场证件，证书配置表；注释：赛事系列id）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：赛事系列id(个人参赛)）；`competition_cert_exchange_rule`（赛事主数据，赛证互通规则表；注释：赛事系列id）；`competition_course_config`（赛事主数据，赛事关联课程配置表；注释：赛事系列id）；`competition_enterprise_rela`（赛事主数据，赛事赞助企业关联关系表；注释：赛事系列id(个人参赛)）；`competition_grade_info`（报名与团队，成绩表；注释：赛事系列id）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：赛事系列id）；`competition_promoted_info`（报名与团队，赛事晋级表；注释：赛事系列id）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：赛事系列ID）；`competition_scene_credential_scope_grant`（一证多权 grant，赛事现场证件作用域授权表；注释：赛事系列ID）；`competition_scene_notice`（赛事主数据，赛事现场通知；注释：赛事届次ID）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：赛事系列ID）；`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：赛事系列ID）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：赛事系列ID）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：赛事系列ID）；`competition_scene_subject_operation_state`（现场 operation_state，赛事现场主体操作状态表；注释：赛事系列ID）；`competition_series_info`（赛事主数据，赛事系列信息表；注释：赛事系列id）；`competition_stage_config`（赛事主数据，赛事阶段配置表；注释：赛事系列id）；`competition_track_info`（赛事主数据，赛事赛道配置；注释：赛事id）；`competition_user_payment_record`（支付，参赛人员缴费记录表；注释：赛事系列id）；`competition_work_link_info`（赛事主数据，作品打分链接信息表；注释：赛事id）；`competition_works`（赛事主数据，赛事作品表；注释：竞赛id）；`file_upload_manager`（文件，文件上传管理表；注释：赛事）；`file_upload_record`（文件，文件上传管理表；注释：赛事）；`operation_config`（赛事主数据，操作权限配置表；注释：大赛ID）；`team_manager_info`（报名与团队，团队管理表；注释：关联比赛code）；`user_certificate`（现场证件，用户证书表；注释：赛事系列id）；`user_certificate_history`（现场证件，user certificate history；注释：赛事系列id）；`user_certificate_origin`（现场证件，user certificate origin；注释：赛事系列id）；`user_collect`（其他，用户收藏信息表；注释：赛事界id）；`user_grade_info`（报名与团队，用户成绩信息表；注释：赛事系列id）；`wx_qc_code_config`（其他，二维码配置管理表；注释：赛事系列id）
- 判断：更适合作为赛事系列事实源；在报名、证件、资源、赛程中应保留，快照表可冗余。

## `schedule_id`

- 出现位置：10 张表/字段。
- 代表表：`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：赛场安排ID，赛场级证件必填，大赛级直接发证可为空）；`competition_scene_notice_schedule`（赛事主数据，赛事通知可见赛场关系；注释：赛场安排ID）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：赛场安排ID）；`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：现场安排ID）；`competition_scene_resource_slot`（资源时段，赛事现场设备资源预约时段表；注释：现场安排ID）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：赛场安排ID）；`competition_scene_schedule_resource`（资源部署，赛事现场赛场资源布置表；注释：现场安排ID）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：赛场安排ID）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）
- 判断：赛场安排域核心外键，应统一指向 `competition_scene_schedule` / `competition_scene_schedule_target`；在日志和快照中应标注为操作时快照。

## `target_id`

- 出现位置：4 张表/字段。
- 代表表：`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：安排对象ID）；`competition_scene_notice`（赛事主数据，赛事现场通知；注释：现场绑定对象ID，个人通知使用）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：安排对象ID）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：安排对象ID）
- 判断：赛场安排域核心外键，应统一指向 `competition_scene_schedule` / `competition_scene_schedule_target`；在日志和快照中应标注为操作时快照。

## `team_id`

- 出现位置：4 张表/字段。
- 代表表：`change_log`（其他，参赛信息变动日志表；注释：队伍ID）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）；`team_manager_info`（报名与团队，团队管理表；注释：团队id）
- 判断：`team_code` 经常承担跨服务自然键和快照键，`team_id` 承担本地表内关联。建议把 `team_code_snapshot` 与事实源 `team_id` 分开表达。

## `team_code`

- 出现位置：25 张表/字段。
- 代表表：`award_details`（赛事主数据，获奖公示明细；注释：团队编号）；`candidate_cert_info`（其他，候选人证书表；注释：团队code）；`cert_player_info`（现场证件，证书人员表；注释：团队code）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：团队code）；`competition_grade_info`（报名与团队，成绩表；注释：团队code）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：团队code）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：团队编号）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：团队编号快照）；`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：团队编号冗余）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：团队编号）；`competition_works`（赛事主数据，赛事作品表；注释：团队code）；`file_upload_manager`（文件，文件上传管理表；注释：团队code）；`file_upload_record`（文件，文件上传管理表；注释：团队code）；`offline_v3_apply_dedup_20260705_001`（导入中间表，offline v3 apply dedup 20260705 001；注释：-）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_apply_dedup_20260705_001`（导入中间表，offline v4 apply dedup 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）；`operation_flow`（赛事主数据，团队信息操作和流程关联表；注释：团队编号）；`operation_times`（赛事主数据，队伍操作次数表；注释：队伍code）；`team_manager_info`（报名与团队，团队管理表；注释：团队code）；`team_member_rela`（报名与团队，团队关联关系表；注释：团队code）；`user_certificate`（现场证件，用户证书表；注释：团队code）；`user_certificate_history`（现场证件，user certificate history；注释：团队code）；`user_certificate_origin`（现场证件，user certificate origin；注释：团队code）；`user_grade_info`（报名与团队，用户成绩信息表；注释：团队id）
- 判断：`team_code` 经常承担跨服务自然键和快照键，`team_id` 承担本地表内关联。建议把 `team_code_snapshot` 与事实源 `team_id` 分开表达。

## `user_id`

- 出现位置：94 张表/字段。
- 代表表：`auth_info`（系统用户与权限，实名认证表；注释：用户id）；`candidate_cert_info`（其他，候选人证书表；注释：参赛者id）；`cert_player_info`（现场证件，证书人员表；注释：用户id）；`change_log`（其他，参赛信息变动日志表；注释：数据权限用户id）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：用户id）；`competition_awards_config`（赛事主数据，赛事奖项设置表；注释：数据权限用户id）；`competition_cert_exchange_apply`（报名与团队，赛证互通申请表；注释：用户id）；`competition_config`（赛事主数据，赛事配置表；注释：数据权限用户id）；`competition_course_config`（赛事主数据，赛事关联课程配置表；注释：数据权限用户id）；`competition_enterprise_rela`（赛事主数据，赛事赞助企业关联关系表；注释：数据权限用户id）；`competition_grade_info`（报名与团队，成绩表；注释：用户ID）；`competition_main_info`（赛事主数据，赛事主数据表；注释：数据权限用户id）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：用户id）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：用户ID）；`competition_scene_notice`（赛事主数据，赛事现场通知；注释：系统用户ID快照）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：用户ID快照）；`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：个人预约用户ID或团队预约操作人冗余）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：用户ID）；`competition_series_info`（赛事主数据，赛事系列信息表；注释：数据权限用户id）；`competition_stage_config`（赛事主数据，赛事阶段配置表；注释：数据权限用户id）；`competition_user_payment_record`（支付，参赛人员缴费记录表；注释：用户id）；`competition_works`（赛事主数据，赛事作品表；注释：用户id）；`component_library_info`（内容管理，组件库信息表；注释：数据权限用户id）；`component_library_info_copy1`（内容管理，组件库信息表；注释：数据权限用户id）；`content_banner_info`（内容管理，banner图管理；注释：数据权限用户id）；`course_chapter_info`（其他，章节信息表；注释：数据权限用户id）；`course_chapter_video`（其他，章节视频信息表；注释：数据权限用户id）；`course_classify_info`（其他，课程分类信息表；注释：数据权限用户id）；`course_info`（其他，课程信息表；注释：数据权限用户id）；`course_recommend_info`（其他，课程推荐信息表；注释：数据权限用户id）；`course_recommend_rela`（其他，课程推荐关联关系表；注释：数据权限用户id）；`data_source_info`（内容管理，数据源信息表；注释：数据权限用户id）；`export_manage`（文件，导出管理表；注释：用户id）；`file_download_record`（文件，文件下载记录；注释：下载用户id）；`file_task`（文件，文件分发任务表；注释：用户id）；`file_task_config`（文件，文件配置表；注释：用户id）；`file_upload_manager`（文件，文件上传管理表；注释：用户id）；`file_upload_record`（文件，文件上传管理表；注释：用户id）；`identity_info`（系统用户与权限，身份认证信息表；注释：用户id）；`im_customer_dialogue`（其他，客服对话管理表；注释：用户id）；`im_customer_service`（其他，客服人员信息；注释：数据权限用户id）；`im_friend`（其他，好友；注释：用户id）；`im_group_member`（报名与团队，群成员；注释：用户id）；`im_leave_message`（其他，离线留言信息表；注释：用户id）；`invoice_info`（支付，发票信息表；注释：数据权限用户id）；`invoice_per_info`（支付，开票信息记录；注释：用户id）；`merchant_param_config`（支付，商户参数配置表（支付和发票）；注释：数据权限用户id）；`merchant_work_scope`（支付，商户作用范围表；注释：数据权限用户id）；`message_info`（其他，消息管理；注释：数据权限用户id）；`news_classify`（内容管理，新闻分类表；注释：数据权限用户id）；`news_info`（内容管理，新闻信息表；注释：数据权限用户id）；`notice_info`（内容管理，notice info；注释：数据权限用户id）；`offline_v3_apply_dedup_20260705_001`（导入中间表，offline v3 apply dedup 20260705 001；注释：-）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_apply_dedup_20260705_001`（导入中间表，offline v4 apply dedup 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）；`operation_config`（赛事主数据，操作权限配置表；注释：数据权限用户id）；`order_info`（支付，订单信息表；注释：用户id）；`page_manager_info`（内容管理，页面管理信息表；注释：数据权限用户id）；`questions`（内容管理，常见问题；注释：数据权限用户id）；`review_activity_user_role`（评审任务，活动内用户角色表；注释：用户ID）；`review_group_specialist_relation`（评审任务，专家组与专家关联关系表；注释：专家id）；`review_object_certificate_ref`（评审材料，评审对象参赛证映射表；注释：用户ID）；`review_object_member`（评审材料，评审对象成员表；注释：用户ID）；`review_panel_member`（评审任务，专家组成员表；注释：用户ID）；`review_submission_permission`（评审材料，填报权限表；注释：用户ID）；`review_task_specialist_relation`（评审任务，任务分配专家关联关系表；注释：专家用户id）；`reviewer_profile`（评审任务，评审人画像表；注释：用户ID）；`sponsoring_enterprise_info`（赛事主数据，赞助企业信息表；注释：数据权限用户id）；`suggestion_feedback_info`（内容管理，意见反馈信息表；注释：数据权限用户id）；`sys_audit_config`（系统用户与权限，审核流程环节配置表；注释：数据权限用户id）；`sys_audit_log`（系统用户与权限，审计日志记录表；注释：用户ID）；`sys_audit_main_config`（系统用户与权限，系统审核流程配置表；注释：数据权限用户id）；`sys_audit_task`（系统用户与权限，审核任务表；注释：数据权限用户id）；`sys_audit_task_subinfo`（系统用户与权限，审核任务审核信息表；注释：数据权限用户id）；`sys_login_log`（系统用户与权限，系统登录日志表；注释：用户id）；`sys_notice`（系统用户与权限，系统通知公告表；注释：数据权限用户id）；`sys_user`（系统用户与权限，用户信息表；注释：用户ID）；`sys_user_org`（系统用户与权限，用户机构关联关系表；注释：用户id）；`sys_user_post`（系统用户与权限，用户与岗位关联表；注释：用户ID）；...
- 判断：跨域主用户标识，语义相对稳定；但在 IM、系统用户、报名成员中可能存在本地用户体系差异，跨库 JOIN 应明确来源。

## `member_id`

- 出现位置：15 张表/字段。
- 代表表：`candidate_cert_info`（其他，候选人证书表；注释：报名用户id）；`cert_player_info`（现场证件，证书人员表；注释：报名人员id）；`change_log`（其他，参赛信息变动日志表；注释：报名信息记录ID）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：报名id）；`competition_grade_info`（报名与团队，成绩表；注释：报名用户id）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：报名成员ID）；`competition_scene_notice`（赛事主数据，赛事现场通知；注释：报名成员ID快照）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：报名成员ID快照）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：报名成员ID）；`offline_v3_apply_dedup_20260705_001`（导入中间表，offline v3 apply dedup 20260705 001；注释：-）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_apply_dedup_20260705_001`（导入中间表，offline v4 apply dedup 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）；`operation_times`（赛事主数据，队伍操作次数表；注释：报名信息ID）；`review_object_certificate_ref`（评审材料，评审对象参赛证映射表；注释：成员ID）
- 风险：可能指报名成员、评审成员、证件成员或面板成员，同名不同义风险高。建议按域前缀命名。

## `credential_id`

- 出现位置：4 张表/字段。
- 代表表：`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：现场证件ID）；`competition_scene_credential_scope_grant`（一证多权 grant，赛事现场证件作用域授权表；注释：核心现场证件ID）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：现场证件ID）；`competition_scene_subject_operation_state`（现场 operation_state，赛事现场主体操作状态表；注释：触发操作的证件ID）
- 判断：现场证件域应以 `competition_scene_credential.id` 为事实源；日志、授权和核验表中的姓名/类型应作为快照保留。

## `credential_type`

- 出现位置：4 张表/字段。
- 代表表：`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：证件类型：PARTICIPANT参赛证/TEACHER教师证/EXPERT专家证/STAFF工作人员证）；`competition_scene_credential_scope_grant`（一证多权 grant，赛事现场证件作用域授权表；注释：授权对应证件类型）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：历史兼容字段：证件类型已迁移到competition_scene_schedule_target.credential_type）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：证件类型：PARTICIPANT参赛证/TEACHER教师证/EXPERT专家证/STAFF工作人员证）
- 判断：现场证件域应以 `competition_scene_credential.id` 为事实源；日志、授权和核验表中的姓名/类型应作为快照保留。

## `role_code`

- 出现位置：1 张表/字段。
- 代表表：`competition_scene_credential_scope_grant`（一证多权 grant，赛事现场证件作用域授权表；注释：授权角色编码）
- 判断：`role_code` 应是稳定枚举，`competition_role_name` 是展示快照；两者不应互相替代。

## `competition_role_name`

- 出现位置：9 张表/字段。
- 代表表：`candidate_cert_info`（其他，候选人证书表；注释：参赛角色）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：参赛角色）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：参赛角色）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：现场角色快照：TEACHER/MEMBER/EXPERT/CAPTAIN/MATERIAL_STAFF/CHECKIN_STAFF）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：现场角色：TEACHER/MEMBER/EXPERT/CAPTAIN/MATERIAL_STAFF/CHECKIN_STAFF）；`offline_v3_apply_dedup_20260705_001`（导入中间表，offline v3 apply dedup 20260705 001；注释：-）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_apply_dedup_20260705_001`（导入中间表，offline v4 apply dedup 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）
- 判断：`role_code` 应是稳定枚举，`competition_role_name` 是展示快照；两者不应互相替代。

## `group_code`

- 出现位置：1 张表/字段。
- 代表表：`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：预约主体组别编码快照）
- 判断：这些多为赛事分组/赛项/候场分组快照，合理保留，但建议补 `_snapshot` 或字段注释避免误认为主数据。

## `group_name`

- 出现位置：2 张表/字段。
- 代表表：`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：预约主体组别名称快照）；`review_specialist_group_info`（评审任务，专家组表；注释：专家组名称）
- 判断：这些多为赛事分组/赛项/候场分组快照，合理保留，但建议补 `_snapshot` 或字段注释避免误认为主数据。

## `second_level_code`

- 出现位置：22 张表/字段。
- 代表表：`candidate_cert_info`（其他，候选人证书表；注释：组别code）；`cert_config_info`（现场证件，证书配置表；注释：组别code）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：赛道二级分类编码(组别、赛道、子课题)）；`competition_awards_config`（赛事主数据，赛事奖项设置表；注释：组别）；`competition_cert_exchange_rule`（赛事主数据，赛证互通规则表；注释：组别code）；`competition_grade_info`（报名与团队，成绩表；注释：组别code）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：赛道二级分类编码(组别、赛道、子课题)）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：组别编码快照）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：组别编码快照）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：组别编码）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：组别编码快照）；`competition_track_config`（赛事主数据，赛道配置；注释：二级分类编码）；`file_upload_manager`（文件，文件上传管理表；注释：组别code）；`file_upload_record`（文件，文件上传管理表；注释：组别code）；`offline_v3_apply_dedup_20260705_001`（导入中间表，offline v3 apply dedup 20260705 001；注释：-）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_apply_dedup_20260705_001`（导入中间表，offline v4 apply dedup 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）；`team_manager_info`（报名与团队，团队管理表；注释：赛道二级分类编码）；`user_certificate`（现场证件，用户证书表；注释：组别code）；`user_certificate_history`（现场证件，user certificate history；注释：组别code）；`user_certificate_origin`（现场证件，user certificate origin；注释：组别code）
- 判断：这些多为赛事分组/赛项/候场分组快照，合理保留，但建议补 `_snapshot` 或字段注释避免误认为主数据。

## `second_level_name`

- 出现位置：17 张表/字段。
- 代表表：`candidate_cert_info`（其他，候选人证书表；注释：组别名称）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：二级分类名称）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：二级分类名称）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：组别名称快照）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：组别名称快照）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：组别名称快照）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：组别名称快照）；`competition_track_config`（赛事主数据，赛道配置；注释：二级分类名称）；`file_upload_manager`（文件，文件上传管理表；注释：组别名称）；`file_upload_record`（文件，文件上传管理表；注释：组别名称）；`offline_v3_apply_dedup_20260705_001`（导入中间表，offline v3 apply dedup 20260705 001；注释：-）；`offline_v3_target_match_20260705_001`（现场对象，offline v3 target match 20260705 001；注释：-）；`offline_v4_apply_dedup_20260705_001`（导入中间表，offline v4 apply dedup 20260705 001；注释：-）；`offline_v4_target_match_20260705_001`（现场对象，offline v4 target match 20260705 001；注释：-）；`team_manager_info`（报名与团队，团队管理表；注释：赛道二级分类名称）；`user_certificate_history`（现场证件，user certificate history；注释：组别名称）；`user_certificate_origin`（现场证件，user certificate origin；注释：组别名称）
- 判断：这些多为赛事分组/赛项/候场分组快照，合理保留，但建议补 `_snapshot` 或字段注释避免误认为主数据。

## `waiting_group_code`

- 出现位置：3 张表/字段。
- 代表表：`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：候场分组编码）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：候场分组编码：系统维护，不再由前端手工录入）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：候场分组编码）
- 判断：这些多为赛事分组/赛项/候场分组快照，合理保留，但建议补 `_snapshot` 或字段注释避免误认为主数据。

## `waiting_group_name`

- 出现位置：3 张表/字段。
- 代表表：`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：候场分组名称）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：候场分组名称）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：候场分组名称）
- 判断：这些多为赛事分组/赛项/候场分组快照，合理保留，但建议补 `_snapshot` 或字段注释避免误认为主数据。

## `status`

- 出现位置：45 张表/字段。
- 代表表：`competition_scene_notice`（赛事主数据，赛事现场通知；注释：状态: 0正常/1停用）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：状态: 0启用/1停用）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：状态: 0有效/1停用）；`component_library_info`（内容管理，组件库信息表；注释：状态（1启用 /0 禁用））；`component_library_info_copy1`（内容管理，组件库信息表；注释：状态（1启用 /0 禁用））；`content_column`（内容管理，内容栏目表；注释：状态（0正常 1停用））；`content_file`（内容管理，内容文件表；注释：状态（0正常 1停用））；`course_classify_info`（其他，课程分类信息表；注释：状态字典subassembly_status）；`course_recommend_info`（其他，课程推荐信息表；注释：推荐状态）；`export_manage`（文件，导出管理表；注释：导出状态）；`im_customer_dialogue`（其他，客服对话管理表；注释：状态（在线 / 离线 / 已结束））；`im_group_message`（其他，群消息；注释：状态 0:未发出  2:撤回 ）；`im_leave_message`（其他，离线留言信息表；注释：状态（待处理 / 处理中 / 已回复 / 已关闭））；`im_private_message`（其他，私聊消息；注释：状态 0:未读 1:已读 2:撤回 3:已读）；`menu_info`（其他，菜单信息表；注释：状态（启用 / 禁用））；`merchant_param_config`（支付，商户参数配置表（支付和发票）；注释：开启状态(0-关闭，1-开启)）；`order_statement_record`（支付，对账单对账记录；注释：对账状态）；`review_activity`（评审任务，评审活动表；注释：活动状态）；`review_assignment`（评审任务，评审任务分配表；注释：分配状态）；`review_object_material`（评审材料，评审材料表；注释：材料状态）；`review_panel`（评审任务，专家组表；注释：状态）；`review_panel_member`（评审任务，专家组成员表；注释：状态）；`review_result_publish_log`（评审评分，结果发布日志表；注释：状态）；`review_round`（评审任务，评审轮次表；注释：轮次状态）；`review_session`（评审任务，现场评审场次表；注释：场次状态）；`review_submission_permission`（评审材料，填报权限表；注释：权限状态）；`reviewer_profile`（评审任务，评审人画像表；注释：状态）；`sys_dict`（系统用户与权限，字典表；注释：状态（0正常 1停用））；`sys_dict_data`（系统用户与权限，字典数据表；注释：状态（0正常 1停用））；`sys_dict_type`（系统用户与权限，字典类型表；注释：状态（0正常 1停用））；`sys_error_log`（系统用户与权限，错误日志记录表；注释：处理状态（0未处理 1已处理 2已忽略））；`sys_job`（系统用户与权限，定时任务调度表；注释：状态（0正常 1暂停））；`sys_job_log`（系统用户与权限，定时任务调度日志表；注释：执行状态（0正常 1失败））；`sys_logininfor`（系统用户与权限，系统访问记录；注释：登录状态（0成功 1失败））；`sys_menu`（系统用户与权限，菜单权限表；注释：菜单状态（0正常 1停用））；`sys_notice`（系统用户与权限，系统通知公告表；注释：状态）；`sys_oper_log`（系统用户与权限，操作日志记录；注释：操作状态（0正常 1异常））；`sys_operation_log`（系统用户与权限，系统操作日志；注释：状态）；`sys_org`（系统用户与权限，系统机构信息表；注释：状态）；`sys_post`（系统用户与权限，岗位信息表；注释：状态（0正常 1停用））；`sys_role`（系统用户与权限，角色信息表；注释：角色状态（0正常 1停用））；`sys_user`（系统用户与权限，用户信息表；注释：账号状态（0正常 1停用））；`sys_user_role`（系统用户与权限，用户和角色关联表；注释：状态）；`teacher_tmp_info`（系统用户与权限，教师导入临时表；注释：账号状态）；`wechat_integration`（其他，微信集成管理表；注释：状态）
- 风险：同名不同义最高；建议逐步改为 `review_status`、`reservation_status`、`credential_status`、`publish_status` 等域内显式状态。

## `deleted`

- 出现位置：9 张表/字段。
- 代表表：`competition_scene_credential_scope_grant`（一证多权 grant，赛事现场证件作用域授权表；注释：删除标识: 0正常/1删除）；`competition_scene_resource`（资源台账，赛事现场设备资源台账表；注释：删除标识: 0正常/1删除）；`competition_scene_resource_reservation`（资源预约，赛事现场设备资源预约记录表；注释：删除标识: 0正常/1删除）；`competition_scene_resource_schedule_scope`（资源部署，赛事现场资源允许预约赛场范围表；注释：删除标识: 0正常/1删除）；`competition_scene_resource_slot`（资源时段，赛事现场设备资源预约时段表；注释：删除标识: 0正常/1删除）；`competition_scene_resource_slot_group_scope`（资源时段，赛事现场资源预约时段允许组别表；注释：删除标识: 0正常/1删除）；`competition_scene_schedule_resource`（资源部署，赛事现场赛场资源布置表；注释：删除标识: 0正常/1删除）；`competition_scene_subject_operation_state`（现场 operation_state，赛事现场主体操作状态表；注释：删除标识: 0正常/1删除）；`im_friend`（其他，好友；注释：删除标识  0：正常   1：已删除）
- 风险：软删除字段命名和取值不统一。建议统一 active 判定，并在唯一约束中使用 `active_key` 或等价生成列。

## `del_flag`

- 出现位置：147 张表/字段。
- 代表表：`auth_info`（系统用户与权限，实名认证表；注释：删除标识）；`award_details`（赛事主数据，获奖公示明细；注释：删除标识）；`award_publicity`（赛事主数据，获奖公示管理主表；注释：删除标识）；`candidate_cert_info`（其他，候选人证书表；注释：删除标识(0 未删除   1删除)）；`cert_config_info`（现场证件，证书配置表；注释：删除标识(0 未删除   1删除)）；`cert_exchange_rule_detail`（现场证件，赛证互通规则明细表；注释：删除标识(0 未删除   1删除)）；`cert_org_info`（现场证件，证书颁发机构表；注释：删除标识(0 未删除   1删除)）；`cert_player_info`（现场证件，证书人员表；注释：删除标识(0 未删除   1删除)）；`change_log`（其他，参赛信息变动日志表；注释：0-存在，2-删除）；`competition_apply_info`（报名与团队，赛事申请报名信息表；注释：删除标识）；`competition_awards_config`（赛事主数据，赛事奖项设置表；注释：删除标识）；`competition_cert_exchange_apply`（报名与团队，赛证互通申请表；注释：删除标识(0 未删除   1删除)）；`competition_cert_exchange_rule`（赛事主数据，赛证互通规则表；注释：删除标识(0 未删除   1删除)）；`competition_check_data_package`（报名与团队，校验包表；注释：删除标识）；`competition_check_info`（报名与团队，校验项表；注释：删除标识）；`competition_config`（赛事主数据，赛事配置表；注释：删除标识）；`competition_course_config`（赛事主数据，赛事关联课程配置表；注释：删除标识）；`competition_enterprise_rela`（赛事主数据，赛事赞助企业关联关系表；注释：删除标识）；`competition_grade_info`（报名与团队，成绩表；注释：删除标识(0 未删除   1删除)）；`competition_main_info`（赛事主数据，赛事主数据表；注释：删除标识）；`competition_promoted_apply_info`（报名与团队，赛事晋级申请报名信息表；注释：删除标识）；`competition_promoted_info`（报名与团队，赛事晋级表；注释：删除标识）；`competition_scene_credential`（现场证件，赛事现场证件实例表；注释：删除标识: 0正常/1删除）；`competition_scene_notice`（赛事主数据，赛事现场通知；注释：删除标志: 0正常/1删除）；`competition_scene_operation_log`（现场 operation_log，赛事现场扫码操作流水表；注释：删除标识: 0正常/1删除）；`competition_scene_schedule`（赛场安排，赛事现场赛场安排表；注释：删除标识: 0正常/1删除）；`competition_scene_schedule_target`（赛场安排，赛事现场赛场安排对象表；注释：删除标识: 0正常/1删除）；`competition_series_info`（赛事主数据，赛事系列信息表；注释：删除标识）；`competition_stage_config`（赛事主数据，赛事阶段配置表；注释：删除标识）；`competition_title_notice`（赛事主数据，提示信息表；注释：删除标识(0 未删除   1删除)）；`competition_track_config`（赛事主数据，赛道配置；注释：删除标识）；`competition_track_info`（赛事主数据，赛事赛道配置；注释：删除标识）；`competition_user_payment_record`（支付，参赛人员缴费记录表；注释：删除标识）；`competition_work_link_info`（赛事主数据，作品打分链接信息表；注释：删除标识）；`competition_works`（赛事主数据，赛事作品表；注释：删除标识）；`component_data_source_rela`（内容管理，页面、组件、数据源关联关系表；注释：删除标识）；`component_library_info`（内容管理，组件库信息表；注释：删除标识(0存在，2删除)）；`component_library_info_copy1`（内容管理，组件库信息表；注释：删除标识(0存在，2删除)）；`content_banner_info`（内容管理，banner图管理；注释：删除标识）；`content_column`（内容管理，内容栏目表；注释：删除标志（0代表存在 2代表删除））；`content_detail`（内容管理，内容详情表；注释：删除标志（0代表存在 2代表删除））；`content_file`（内容管理，内容文件表；注释：删除标志（0代表存在 2代表删除））；`course_chapter_info`（其他，章节信息表；注释：删除标识）；`course_chapter_video`（其他，章节视频信息表；注释：删除标识）；`course_classify_info`（其他，课程分类信息表；注释：删除标识）；`course_info`（其他，课程信息表；注释：删除标识）；`course_recommend_info`（其他，课程推荐信息表；注释：删除标识）；`course_recommend_rela`（其他，课程推荐关联关系表；注释：删除标识）；`data_source_info`（内容管理，数据源信息表；注释：删除标识）；`export_manage`（文件，导出管理表；注释：删除标志（0代表存在 1代表删除））；`file_download_record`（文件，文件下载记录；注释：删除标志（0代表存在 1代表删除））；`file_task`（文件，文件分发任务表；注释：删除标志（0代表存在 1代表删除））；`file_task_config`（文件，文件配置表；注释：删除标志（0代表存在 1代表删除））；`file_upload_manager`（文件，文件上传管理表；注释：删除标志（0代表存在 1代表删除））；`file_upload_record`（文件，文件上传管理表；注释：删除标志（0代表存在 1代表删除））；`identity_info`（系统用户与权限，身份认证信息表；注释：删除标识）；`im_customer_dialogue`（其他，客服对话管理表；注释：删除标识）；`im_customer_service`（其他，客服人员信息；注释：删除标识）；`im_leave_message`（其他，离线留言信息表；注释：删除标识）；`invoice_info`（支付，发票信息表；注释：删除标识）；`invoice_per_info`（支付，开票信息记录；注释：删除标识）；`menu_component_rela`（内容管理，菜单组件关系表；注释：删除标识）；`menu_info`（其他，菜单信息表；注释：删除标识）；`merchant_param_config`（支付，商户参数配置表（支付和发票）；注释：删除标识）；`merchant_work_scope`（支付，商户作用范围表；注释：删除标识）；`message_info`（其他，消息管理；注释：删除标识）；`news_classify`（内容管理，新闻分类表；注释：删除标识）；`news_info`（内容管理，新闻信息表；注释：删除标识）；`notice_info`（内容管理，notice info；注释：0-存在，2-删除）；`notification_receiver`（其他，站内信接收表；注释：0-存在，1-删除）；`notification_sender`（其他，站内信发送表；注释：0-存在，1-删除）；`operation_config`（赛事主数据，操作权限配置表；注释：0-存在，2-删除）；`order_goods_relation`（支付，订单商品关联表；注释：删除标识）；`order_info`（支付，订单信息表；注释：删除标识）；`order_statement_record`（支付，对账单对账记录；注释：删除标识）；`page_manager_info`（内容管理，页面管理信息表；注释：删除标识）；`questions`（内容管理，常见问题；注释：删除标识）；`review_activity`（评审任务，评审活动表；注释：删除标识: 0正常/1删除）；`review_activity_user_role`（评审任务，活动内用户角色表；注释：删除标识: 0正常/1删除）；`review_assignment`（评审任务，评审任务分配表；注释：删除标识: 0正常/1删除）；...
- 风险：软删除字段命名和取值不统一。建议统一 active 判定，并在唯一约束中使用 `active_key` 或等价生成列。

## 可统一字段

- 新设计优先使用：`competition_series_id`、`schedule_id`、`target_id`、`team_id`、`team_code_snapshot`、`role_code`、`deleted`。
- 历史兼容保留但补注释：`competition_id`、`competition_role_name`、`second_level_name`、`waiting_group_name`、泛化 `status`。

## 应保留为快照的字段

- 证件姓名、学校、角色、赛项、组别；资源预约的工位数、组别、共享占用；操作日志的操作人和对象描述；评审评分项名称、权重、评分标准；导入表原始文本。
