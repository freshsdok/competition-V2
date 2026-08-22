# 状态字段审计

本报告区分“事实源状态”“兼容展示状态”“流水操作类型”。泛化 `status`、`del_flag`、`deleted` 是当前一致性风险集中区。

## 状态字段分布

| 表 | 业务域 | 状态字段 | 判断 |
|---|---|---|---|
| `act_evt_log` | 其他 | `LOCK_OWNER_`, `LOCK_TIME_` | 流水操作类型/结果，不应作为当前事实源 |
| `act_hi_procinst` | 其他 | `BUSINESS_STATUS_` | 事实源/展示混用，需按业务确认 |
| `act_re_procdef` | 其他 | `SUSPENSION_STATE_` | 事实源/展示混用，需按业务确认 |
| `act_ru_event_subscr` | 其他 | `LOCK_TIME_`, `LOCK_OWNER_` | 事实源/展示混用，需按业务确认 |
| `act_ru_execution` | 其他 | `IS_ACTIVE_`, `SUSPENSION_STATE_`, `CACHED_ENT_STATE_`, `LOCK_TIME_`, `LOCK_OWNER_`, `IS_COUNT_ENABLED_`, `BUSINESS_STATUS_` | 事实源/展示混用，需按业务确认 |
| `act_ru_external_job` | 其他 | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 事实源/展示混用，需按业务确认 |
| `act_ru_history_job` | 其他 | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 事实源/展示混用，需按业务确认 |
| `act_ru_job` | 其他 | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 事实源/展示混用，需按业务确认 |
| `act_ru_task` | 其他 | `SUSPENSION_STATE_`, `IS_COUNT_ENABLED_` | 事实源/展示混用，需按业务确认 |
| `act_ru_timer_job` | 其他 | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 事实源/展示混用，需按业务确认 |
| `auth_info` | 系统用户与权限 | `auth_status`, `check_status`, `check_opinion`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `award_details` | 赛事主数据 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `award_publicity` | 赛事主数据 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `candidate_cert_info` | 其他 | `is_check`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `cert_config_info` | 现场证件 | `cert_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `cert_exchange_rule_detail` | 现场证件 | `rule_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `cert_org_info` | 现场证件 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `cert_player_info` | 现场证件 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `change_log` | 其他 | `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `competition_apply_info` | 报名与团队 | `check_status`, `real_name_auth_status`, `pay_status`, `del_flag`, `invoice_status` | 事实源/展示混用，需按业务确认 |
| `competition_awards_config` | 赛事主数据 | `del_flag` | 配置启停状态 |
| `competition_cert_exchange_apply` | 报名与团队 | `repay_amount`, `apply_status`, `pay_status`, `invoice_status`, `pay_time`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_cert_exchange_rule` | 赛事主数据 | `ruler_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_check_data_package` | 报名与团队 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_check_info` | 报名与团队 | `check_item_id`, `check_item_name`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_config` | 赛事主数据 | `del_flag` | 配置启停状态 |
| `competition_course_config` | 赛事主数据 | `del_flag` | 配置启停状态 |
| `competition_enterprise_rela` | 赛事主数据 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_grade_info` | 报名与团队 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_main_info` | 赛事主数据 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_promoted_apply_info` | 报名与团队 | `pay_status`, `invoice_status`, `apply_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_promoted_info` | 报名与团队 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_scene_credential` | 现场证件 | `active_core_credential_key`, `valid_from`, `valid_to`, `credential_status`, `report_status`, `material_status`, `waiting_status`, `del_flag` | 证件/授权事实源或快照 |
| `competition_scene_credential_scope_grant` | 一证多权 grant | `active_grant_key`, `valid_from`, `valid_to`, `grant_status`, `deleted` | 证件/授权事实源或快照 |
| `competition_scene_notice` | 赛事主数据 | `publish_status`, `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_scene_operation_log` | 现场 operation_log | `apply_check_result`, `schedule_check_result`, `identity_check_result`, `request_payload`, `response_payload`, `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `competition_scene_resource` | 资源台账 | `resource_status`, `deleted` | 事实源/展示混用，需按业务确认 |
| `competition_scene_resource_reservation` | 资源预约 | `active_reservation_key`, `reservation_status`, `cancel_time`, `cancel_reason`, `check_status`, `check_user_id`, `check_time`, `deleted` | 预约事实源 |
| `competition_scene_resource_schedule_scope` | 资源部署 | `enabled`, `deleted` | 事实源/展示混用，需按业务确认 |
| `competition_scene_resource_slot` | 资源时段 | `slot_status`, `deleted` | 事实源/展示混用，需按业务确认 |
| `competition_scene_resource_slot_group_scope` | 资源时段 | `enabled`, `deleted` | 事实源/展示混用，需按业务确认 |
| `competition_scene_schedule` | 赛场安排 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_scene_schedule_resource` | 资源部署 | `booking_status`, `booking_open_time`, `booking_close_time`, `deleted` | 事实源/展示混用，需按业务确认 |
| `competition_scene_schedule_target` | 赛场安排 | `review_object_id`, `match_status`, `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_scene_subject_operation_state` | 现场 operation_state | `state_id`, `operation_status`, `deleted` | 现场操作事实源 |
| `competition_series_info` | 赛事主数据 | `competition_status`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_stage_config` | 赛事主数据 | `del_flag` | 配置启停状态 |
| `competition_title_notice` | 赛事主数据 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_track_config` | 赛事主数据 | `del_flag` | 配置启停状态 |
| `competition_track_info` | 赛事主数据 | `check_status`, `del_flag`, `check_package_id` | 事实源/展示混用，需按业务确认 |
| `competition_user_payment_record` | 支付 | `payment_amount`, `payment_time`, `payment_status`, `del_flag` | 支付/票据事实源 |
| `competition_work_link_info` | 赛事主数据 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `competition_works` | 赛事主数据 | `works_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `component_data_source_rela` | 内容管理 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `component_library_info` | 内容管理 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `component_library_info_copy1` | 内容管理 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `content_banner_info` | 内容管理 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `content_column` | 内容管理 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `content_detail` | 内容管理 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `content_file` | 内容管理 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `course_chapter_info` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `course_chapter_video` | 其他 | `publish_status`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `course_classify_info` | 其他 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `course_info` | 其他 | `check_status`, `publish_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `course_recommend_info` | 其他 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `course_recommend_rela` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `data_source_info` | 内容管理 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `export_manage` | 文件 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `file_download_record` | 文件 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `file_task` | 文件 | `task_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `file_task_config` | 文件 | `del_flag` | 配置启停状态 |
| `file_upload_manager` | 文件 | `del_flag`, `submit_status` | 事实源/展示混用，需按业务确认 |
| `file_upload_record` | 文件 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `flw_ev_databasechangeloglock` | 其他 | `LOCKED`, `LOCKGRANTED`, `LOCKEDBY` | 事实源/展示混用，需按业务确认 |
| `flw_ru_batch` | 其他 | `STATUS_` | 事实源/展示混用，需按业务确认 |
| `flw_ru_batch_part` | 其他 | `STATUS_` | 事实源/展示混用，需按业务确认 |
| `identity_info` | 系统用户与权限 | `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `im_customer_dialogue` | 其他 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `im_customer_service` | 其他 | `work_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `im_friend` | 其他 | `deleted` | 事实源/展示混用，需按业务确认 |
| `im_group_message` | 其他 | `status` | 事实源/展示混用，需按业务确认 |
| `im_leave_message` | 其他 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `im_private_message` | 其他 | `status` | 事实源/展示混用，需按业务确认 |
| `im_sensitive_word` | 其他 | `enabled` | 事实源/展示混用，需按业务确认 |
| `invoice_info` | 支付 | `issued_status`, `check_status`, `del_flag` | 支付/票据事实源 |
| `invoice_per_info` | 支付 | `taxpayer_identification_number`, `del_flag` | 支付/票据事实源 |
| `menu_component_rela` | 内容管理 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `menu_info` | 其他 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `merchant_param_config` | 支付 | `pay_valid_time`, `pay_app_id`, `pay_app_secret`, `pay_private_key`, `pay_public_key`, `checker`, `status`, `del_flag` | 配置启停状态 |
| `merchant_work_scope` | 支付 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `message_info` | 其他 | `msg_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `news_classify` | 内容管理 | `classify_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `news_info` | 内容管理 | `news_status`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `notice_info` | 内容管理 | `notice_status`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `notification_receiver` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `notification_sender` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `offline_v3_target_match_20260705_001` | 现场对象 | `match_status` | 事实源/展示混用，需按业务确认 |
| `offline_v4_target_match_20260705_001` | 现场对象 | `match_status` | 事实源/展示混用，需按业务确认 |
| `operation_config` | 赛事主数据 | `del_flag` | 配置启停状态 |
| `operation_flow` | 赛事主数据 | `flow_status` | 事实源/展示混用，需按业务确认 |
| `order_goods_relation` | 支付 | `del_flag`, `pay_status` | 支付/票据事实源 |
| `order_info` | 支付 | `pay_mode`, `pay_status`, `pay_time`, `del_flag`, `pay_method`, `audit_opinion`, `payment_proof_files`, `invoice_status`, `refund_status`, `pay_order_id` | 支付/票据事实源 |
| `order_statement_record` | 支付 | `statement_file_name`, `status`, `del_flag` | 支付/票据事实源 |
| `page_manager_info` | 内容管理 | `publish_status`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `questions` | 内容管理 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `review_activity` | 评审任务 | `review_start_time`, `review_end_time`, `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_activity_user_role` | 评审任务 | `reviewer_id`, `enabled`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_assignment` | 评审任务 | `reviewer_id`, `reviewer_user_id`, `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_audit_log` | 评审任务 | `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `review_criteria` | 评审评分 | `enabled`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_expert_review_notes` | 评审任务 | `del_flag` | 评审流程事实源或阶段状态 |
| `review_group_specialist_relation` | 评审任务 | `del_flag` | 评审流程事实源或阶段状态 |
| `review_object` | 评审任务 | `submit_status`, `locked_time`, `invalid_time`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_object_certificate_ref` | 评审材料 | `valid_status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_object_external_ref` | 评审材料 | `del_flag` | 评审流程事实源或阶段状态 |
| `review_object_material` | 评审材料 | `visible_to_reviewer`, `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_object_member` | 评审材料 | `del_flag` | 评审流程事实源或阶段状态 |
| `review_object_submit_log` | 评审材料 | `before_status`, `after_status`, `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `review_panel` | 评审任务 | `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_panel_member` | 评审任务 | `reviewer_id`, `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_processed_relation` | 文件 | `del_flag`, `review_status`, `review_time` | 评审流程事实源或阶段状态 |
| `review_record` | 评审评分 | `reviewer_id`, `reviewer_user_id`, `record_status`, `locked_time`, `invalid_time`, `invalid_reason`, `review_status`, `review_time`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_result` | 评审评分 | `reviewer_count`, `result_status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_result_publish_log` | 评审评分 | `status`, `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `review_round` | 评审任务 | `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_rule` | 评审评分 | `enabled`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_score_detail` | 评审评分 | `del_flag` | 评审流程事实源或阶段状态 |
| `review_session` | 评审任务 | `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_session_event_log` | 评审任务 | `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `review_session_object` | 评审任务 | `checkin_status`, `review_status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_specialist_group_info` | 评审任务 | `allot_status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_submission_permission` | 评审材料 | `status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_task_allot_group` | 评审任务 | `review_group_id`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_task_allot_group_relation` | 评审任务 | `review_group_id`, `review_id`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_task_info` | 评审任务 | `review_id`, `review_name`, `review_start_time`, `review_end_time`, `review_desc`, `review_group_id`, `distribute_status`, `del_flag` | 评审流程事实源或阶段状态 |
| `review_task_specialist_relation` | 评审任务 | `review_id`, `allot_status`, `review_status`, `del_flag` | 评审流程事实源或阶段状态 |
| `reviewer_profile` | 评审任务 | `reviewer_name`, `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `school_specialty_info` | 系统用户与权限 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `search_config` | 内容管理 | `del_flag`, `search_status` | 配置启停状态 |
| `sponsoring_enterprise_info` | 赛事主数据 | `display_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `suggestion_feedback_info` | 内容管理 | `deal_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_audit_config` | 系统用户与权限 | `audit_id`, `check_person_type`, `check_person_org`, `check_person_role`, `check_person`, `del_flag` | 配置启停状态 |
| `sys_audit_log` | 系统用户与权限 | `audit_id`, `audit_type`, `audit_category`, `audit_status`, `audit_by`, `audit_time`, `audit_remark` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_audit_main_config` | 系统用户与权限 | `audit_id`, `audit_title`, `audit_type`, `del_flag` | 配置启停状态 |
| `sys_audit_task` | 系统用户与权限 | `audit_id`, `now_check_step`, `check_time`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_audit_task_subinfo` | 系统用户与权限 | `audit_config_id`, `check_per`, `check_time`, `check_opinion`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_dict` | 系统用户与权限 | `status`, `del_flag` | 配置启停状态 |
| `sys_dict_data` | 系统用户与权限 | `status` | 配置启停状态 |
| `sys_dict_item` | 系统用户与权限 | `del_flag` | 配置启停状态 |
| `sys_dict_type` | 系统用户与权限 | `status` | 配置启停状态 |
| `sys_error_log` | 系统用户与权限 | `status` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_job` | 系统用户与权限 | `status` | 事实源/展示混用，需按业务确认 |
| `sys_job_log` | 系统用户与权限 | `status` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_login_log` | 系统用户与权限 | `login_status`, `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_logininfor` | 系统用户与权限 | `status` | 事实源/展示混用，需按业务确认 |
| `sys_menu` | 系统用户与权限 | `status` | 事实源/展示混用，需按业务确认 |
| `sys_notice` | 系统用户与权限 | `status`, `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_oper_log` | 系统用户与权限 | `status` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_operation_log` | 系统用户与权限 | `status`, `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_org` | 系统用户与权限 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_post` | 系统用户与权限 | `status` | 事实源/展示混用，需按业务确认 |
| `sys_role` | 系统用户与权限 | `menu_check_strictly`, `dept_check_strictly`, `org_check_strictly`, `status`, `lock_flag`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_role_menu` | 系统用户与权限 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_role_org` | 系统用户与权限 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_sender_message_log` | 系统用户与权限 | `del_flag` | 流水操作类型/结果，不应作为当前事实源 |
| `sys_user` | 系统用户与权限 | `status`, `del_flag`, `auth_status`, `identity_status` | 事实源/展示混用，需按业务确认 |
| `sys_user_group` | 系统用户与权限 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_user_group_competition_relation` | 系统用户与权限 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_user_org` | 系统用户与权限 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `sys_user_role` | 系统用户与权限 | `status` | 事实源/展示混用，需按业务确认 |
| `teacher_tmp_info` | 系统用户与权限 | `status` | 事实源/展示混用，需按业务确认 |
| `team_manager_info` | 报名与团队 | `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `team_member_rela` | 报名与团队 | `check_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `undo_log` | 其他 | `log_status` | 流水操作类型/结果，不应作为当前事实源 |
| `user_certificate` | 现场证件 | `cert_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `user_certificate_history` | 现场证件 | `cert_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `user_certificate_origin` | 现场证件 | `cert_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `user_collect` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `user_grade_info` | 报名与团队 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `user_study_record` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `wechat_integration` | 其他 | `status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `wf_category` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `wf_copy` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `wf_form` | 其他 | `del_flag` | 事实源/展示混用，需按业务确认 |
| `work_order` | 其他 | `order_deal_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `wx_qc_code_config` | 其他 | `del_flag` | 配置启停状态 |
| `wx_qc_code_record` | 其他 | `code_status`, `del_flag` | 事实源/展示混用，需按业务确认 |
| `wx_sign_in_info` | 其他 | `del_flag`, `check_in_type` | 事实源/展示混用，需按业务确认 |

## 重点字段判断

- credential 状态字段：`competition_scene_credential` 及 grant 表应作为证件/授权事实源；证件展示页冗余字段只能作为快照。
- operation_state 状态字段：`competition_scene_subject_operation_state` 是现场操作当前事实源；`competition_scene_operation_log` 是审计流水，不应反向推导当前状态。
- operation_log 操作类型：建议统一 `operation_code/action_type/result_status` 三层，不要用单一 `status` 承载。
- reservation_status / booking_status：资源预约表应是预约事实源，取消态必须与 active 唯一键联动。
- slot_status：资源时段表属于可预约能力状态，和预约记录状态分离。
- review_status：评审活动、轮次、任务、记录、结果的状态应有流程层级，不建议互相覆盖。
- pay_status：订单/支付记录事实源应在订单和支付流水间明确主从，避免订单状态与支付记录状态互相修正。
- check_status：报名/审核/核验多处使用，应改名区分 `apply_check_status`、`credential_check_status`、`scene_verify_status`。
- deleted / del_flag：当前两种命名并存，建议统一软删除注册表，明确有效值、删除值、是否参与唯一键。

## 枚举命名不一致

- `CANCELED` / `CANCELLED`：代码中检出取消拼写相关引用文件 30 个，建议统一为一种枚举并保留兼容映射。
- 建议建立统一枚举注册表：字段名、所属域、允许值、中文含义、是否终态、是否可回退、兼容旧值。
