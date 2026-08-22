# 重点业务域优化建议

以下建议仅为结构治理方案，不包含自动迁移或破坏性操作。

## 赛事 / 报名 / 团队

- 当前主要表：`act_id_membership`, `award_details`, `award_publicity`, `competition_apply_info`, `competition_awards_config`, `competition_cert_exchange_apply`, `competition_cert_exchange_rule`, `competition_check_data_package`, `competition_check_info`, `competition_config`, `competition_course_config`, `competition_enterprise_rela`, `competition_grade_info`, `competition_main_info`, `competition_promoted_apply_info`, `competition_promoted_info`, `competition_scene_notice`, `competition_scene_notice_schedule`, `competition_series_info`, `competition_stage_config`, `competition_title_notice`, `competition_track_config`, `competition_track_info`, `competition_work_link_info`, `competition_works`, `im_group_member`, `operation_config`, `operation_flow`, `operation_times`, `sponsoring_enterprise_info`, `team_manager_info`, `team_member_rela`, `user_grade_info`
- 主要问题：赛事主键、系列 ID、团队自然键、成员角色文本并存；报名事实和导入/快照字段边界不清。
- 短期优化：补字段注释，梳理 `team_code` 事实源，增加团队/成员 active 唯一性检查脚本。
- 中期重构：建立报名团队事实表和成员事实表，导入结果仅作为快照/来源关联。
- 不建议动的部分：历史报名流水、已发证件依赖的快照字段。
- 风险：直接改字段会影响证件、支付、评审、资源预约的跨域引用。

## 赛场安排 / schedule_target

- 当前主要表：`competition_scene_schedule`, `competition_scene_schedule_target`, `offline_v3_scene_player_20260705_001`, `offline_v3_target_match_20260705_001`, `offline_v4_scene_player_20260705_001`, `offline_v4_target_match_20260705_001`
- 主要问题：schedule、target、team_code、waiting_group 字段语义接近，唯一性依赖代码。
- 短期优化：明确 `competition_scene_schedule_target` 的自然唯一键和来源字段。
- 中期重构：把 target 作为现场对象事实源，所有证件/操作/资源绑定只引用 target_id 或 source_ref。
- 不建议动的部分：已有 schedule 与 review 绑定字段先保留兼容。
- 风险：赛程调整会联动扫码、证件、评审分组。

## 现场证件 / grant / operation_state / operation_log

- 当前主要表：`cert_config_info`, `cert_exchange_rule_detail`, `cert_org_info`, `cert_player_info`, `competition_scene_credential`, `competition_scene_credential_scope_grant`, `competition_scene_operation_log`, `competition_scene_subject_operation_state`, `user_certificate`, `user_certificate_history`, `user_certificate_origin`
- 主要问题：证件实例、grant、operation_state、operation_log 都保存主体信息，当前事实源容易分散。
- 短期优化：定义 `credential`、`grant`、`operation_state` 三个事实源边界；operation_log 仅做审计。
- 中期重构：补 active_key 唯一约束，统一 ability/operation 枚举注册表。
- 不建议动的部分：发证时姓名、学校、角色快照。
- 风险：一证多权和教师查看学生证件依赖兼容展示字段。

## 资源预约

- 当前主要表：`competition_scene_resource`, `competition_scene_resource_reservation`, `competition_scene_resource_schedule_scope`, `competition_scene_resource_slot`, `competition_scene_resource_slot_group_scope`, `competition_scene_schedule_resource`
- 主要问题：资源台账、赛程适用范围、时段、组范围、预约状态层级复杂，重复 scope 风险较高。
- 短期优化：为 schedule_scope、slot_group_scope、reservation 建重复检测和 active 唯一建议。
- 中期重构：统一可预约能力模型：resource -> schedule_scope -> slot -> slot_group_scope -> reservation。
- 不建议动的部分：预约时容量和组别快照。
- 风险：容量并发和取消释放需要数据库约束配合事务。

## 评审

- 当前主要表：`review_activity`, `review_activity_user_role`, `review_assignment`, `review_audit_log`, `review_criteria`, `review_expert_review_notes`, `review_group_specialist_relation`, `review_object`, `review_object_certificate_ref`, `review_object_external_ref`, `review_object_material`, `review_object_member`, `review_object_submit_log`, `review_panel`, `review_panel_member`, `review_record`, `review_result`, `review_result_publish_log`, `review_round`, `review_rule`, `review_score_detail`, `review_session`, `review_session_event_log`, `review_session_object`, `review_specialist_group_info`, `review_submission_permission`, `review_task_allot_group`, `review_task_allot_group_relation`, `review_task_info`, `review_task_specialist_relation`, `reviewer_profile`
- 主要问题：评审对象、材料、外部来源、评分项快照较多，状态分布在活动/轮次/任务/记录/结果。
- 短期优化：明确 review_object 与 external_ref 的幂等键，材料按 source 去重。
- 中期重构：建立评审状态机和统一枚举注册表，评分项快照不可回写规则表。
- 不建议动的部分：评分项名称、权重、材料文件名快照。
- 风险：已发布结果和评分记录不可破坏。

## 教师查看学生证件

- 当前主要表：`act_id_membership`, `administration_info`, `auth_info`, `cert_config_info`, `cert_exchange_rule_detail`, `cert_org_info`, `cert_player_info`, `competition_apply_info`, `competition_cert_exchange_apply`, `competition_check_data_package`, `competition_check_info`, `competition_grade_info`, `competition_promoted_apply_info`, `competition_promoted_info`, `competition_scene_credential`, `identity_info`, `im_group_member`, `nationwide_college_info`, `school_specialty_info`, `sys_audit_config`, `sys_audit_log`, `sys_audit_main_config`, `sys_audit_task`, `sys_audit_task_subinfo`, `sys_config`, `sys_dict`, `sys_dict_data`, `sys_dict_item`, `sys_dict_type`, `sys_error_log`, `sys_job`, `sys_job_log`, `sys_login_log`, `sys_logininfor`, `sys_menu`, `sys_notice`, `sys_oper_log`, `sys_operation_log`, `sys_org`, `sys_post` ...
- 主要问题：证件实例、grant、operation_state、operation_log 都保存主体信息，当前事实源容易分散。
- 短期优化：定义 `credential`、`grant`、`operation_state` 三个事实源边界；operation_log 仅做审计。
- 中期重构：补 active_key 唯一约束，统一 ability/operation 枚举注册表。
- 不建议动的部分：发证时姓名、学校、角色快照。
- 风险：一证多权和教师查看学生证件依赖兼容展示字段。

## 导入中间表

- 当前主要表：`offline_v3_apply_dedup_20260705_001`, `offline_v3_scene_excel_20260705_001`, `offline_v4_apply_dedup_20260705_001`, `offline_v4_scene_excel_20260705_001`
- 主要问题：离线导入表命名带批次，原始数据、清洗结果、匹配结果混在不同表。
- 短期优化：冻结历史批次写入，补充批次说明和保留周期。
- 中期重构：统一 import_batch、import_raw_row、import_match_result 三层模型。
- 不建议动的部分：近期上线使用过的 offline_v3/v4 表在确认前不要删。
- 风险：误删会丢失导入追溯链。

