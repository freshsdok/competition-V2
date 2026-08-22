# 数据库表清单

审计来源：测试库 `jiaoxue_test` 的 `information_schema` 只读查询、`db/migration`、Mapper XML、domain/entity 与 service/controller 静态引用。未执行任何 UPDATE / DELETE。

- 基础表数量：242
- 字段数量：4134
- 外键约束数量：47

| 表名 | 中文含义 | 所属业务域 | 表类型 | 主键 | 主要外键/逻辑关联 | 主要状态字段 | deleted/del_flag | 代码引用 |
|---|---|---|---|---|---|---|---|---|
| `act_evt_log` | Flowable 工作流引擎表 | 其他 | 配置表 | `LOG_NR_` | - | `LOCK_OWNER_`, `LOCK_TIME_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ge_bytearray` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | `DEPLOYMENT_ID_->act_re_deployment.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ge_property` | Flowable 工作流引擎表 | 其他 | 配置表 | `NAME_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_actinst` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_attachment` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_comment` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_detail` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_entitylink` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_identitylink` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_procinst` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | `BUSINESS_STATUS_` | 否 | 是，Mapper（1处文件） |
| `act_hi_taskinst` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 是，Mapper（1处文件） |
| `act_hi_tsk_log` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_hi_varinst` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 是，Mapper（1处文件） |
| `act_id_bytearray` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_group` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_info` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_membership` | Flowable 工作流引擎表 | 报名与团队 | 配置表 | `USER_ID_`, `GROUP_ID_` | `GROUP_ID_->act_id_group.ID_`, `USER_ID_->act_id_user.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_priv` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_priv_mapping` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | `PRIV_ID_->act_id_priv.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_property` | Flowable 工作流引擎表 | 其他 | 配置表 | `NAME_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_token` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_id_user` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_procdef_info` | Flowable 工作流引擎表 | 其他 | 配置表 | `ID_` | `INFO_JSON_ID_->act_ge_bytearray.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_re_deployment` | 流程部署表--DeploymentEntityImpl | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_re_model` | 模型信息表(用于Web设计器)--ModelEntityImpl | 其他 | 配置表 | `ID_` | `DEPLOYMENT_ID_->act_re_deployment.ID_`, `EDITOR_SOURCE_VALUE_ID_->act_ge_bytearray.ID_`, `EDITOR_SOURCE_EXTRA_VALUE_ID_->act_ge_bytearray.ID_` | - | 否 | 是，Mapper（1处文件） |
| `act_re_procdef` | 流程定义信息表--ProcessDefinitionEntityImpl | 其他 | 配置表 | `ID_` | - | `SUSPENSION_STATE_` | 否 | 是，Mapper（2处文件） |
| `act_ru_actinst` | 正在运行的节点 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_deadletter_job` | 死信表--DeadLetterJobEntityImpl | 其他 | 业务事实表 | `ID_` | `CUSTOM_VALUES_ID_->act_ge_bytearray.ID_`, `EXCEPTION_STACK_ID_->act_ge_bytearray.ID_`, `EXECUTION_ID_->act_ru_execution.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_`, `PROCESS_INSTANCE_ID_->act_ru_execution.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_entitylink` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_event_subscr` | 事件订阅表--EventSubscriptionEntityImpl | 其他 | 业务事实表 | `ID_` | `EXECUTION_ID_->act_ru_execution.ID_` | `LOCK_TIME_`, `LOCK_OWNER_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_execution` | 流程实例与分支执行表--ExecutionEntityImpl | 其他 | 业务事实表 | `ID_` | `PARENT_ID_->act_ru_execution.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_`, `PROC_INST_ID_->act_ru_execution.ID_`, `SUPER_EXEC_->act_ru_execution.ID_` | `IS_ACTIVE_`, `SUSPENSION_STATE_`, `CACHED_ENT_STATE_`, `LOCK_TIME_`, `LOCK_OWNER_`, `IS_COUNT_ENABLED_`, `BUSINESS_STATUS_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_external_job` | Flowable 工作流引擎表 | 其他 | 业务事实表 | `ID_` | `CUSTOM_VALUES_ID_->act_ge_bytearray.ID_`, `EXCEPTION_STACK_ID_->act_ge_bytearray.ID_` | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_history_job` | 历史作业表(flowable)-- | 其他 | 业务事实表 | `ID_` | - | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_identitylink` | 参与者相关信息表--IdentityLinkEntityImpl | 其他 | 业务事实表 | `ID_` | `PROC_DEF_ID_->act_re_procdef.ID_`, `PROC_INST_ID_->act_ru_execution.ID_`, `TASK_ID_->act_ru_task.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_job` | 作业表--JobEntityImpl | 其他 | 业务事实表 | `ID_` | `CUSTOM_VALUES_ID_->act_ge_bytearray.ID_`, `EXCEPTION_STACK_ID_->act_ge_bytearray.ID_`, `EXECUTION_ID_->act_ru_execution.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_`, `PROCESS_INSTANCE_ID_->act_ru_execution.ID_` | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_suspended_job` | 暂停作业表--SuspendedJobEntityImpl | 其他 | 业务事实表 | `ID_` | `CUSTOM_VALUES_ID_->act_ge_bytearray.ID_`, `EXCEPTION_STACK_ID_->act_ge_bytearray.ID_`, `EXECUTION_ID_->act_ru_execution.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_`, `PROCESS_INSTANCE_ID_->act_ru_execution.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_task` | 用户任务表--TaskEntityImpl | 其他 | 业务事实表 | `ID_` | `EXECUTION_ID_->act_ru_execution.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_`, `PROC_INST_ID_->act_ru_execution.ID_` | `SUSPENSION_STATE_`, `IS_COUNT_ENABLED_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_timer_job` | 定时器表--TimerJobEntityImpl | 其他 | 业务事实表 | `ID_` | `CUSTOM_VALUES_ID_->act_ge_bytearray.ID_`, `EXCEPTION_STACK_ID_->act_ge_bytearray.ID_`, `EXECUTION_ID_->act_ru_execution.ID_`, `PROC_DEF_ID_->act_re_procdef.ID_`, `PROCESS_INSTANCE_ID_->act_ru_execution.ID_` | `LOCK_EXP_TIME_`, `LOCK_OWNER_` | 否 | 框架表，业务代码少量/无直接引用 |
| `act_ru_variable` | 变量信息--VariableInstanceEntityImpl | 其他 | 业务事实表 | `ID_` | `BYTEARRAY_ID_->act_ge_bytearray.ID_`, `EXECUTION_ID_->act_ru_execution.ID_`, `PROC_INST_ID_->act_ru_execution.ID_` | - | 否 | 框架表，业务代码少量/无直接引用 |
| `administration_info` | 行政区划表 | 系统用户与权限 | 主数据表 | `id` | `parent_id` | - | 否 | 否，未检出直接引用 |
| `auth_info` | 实名认证表 | 系统用户与权限 | 主数据表 | `auth_id` | `auth_id`, `user_id` | `auth_status`, `check_status`, `check_opinion`, `del_flag` | 是 | 是，Mapper（14处文件） |
| `award_details` | 获奖公示明细 | 赛事主数据 | 业务事实表 | `id` | `award_publicity_id`, `team_code` | `del_flag` | 是 | 是，Mapper（4处文件） |
| `award_publicity` | 获奖公示管理主表 | 赛事主数据 | 业务事实表 | `id` | `competition_series_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `candidate_cert_info` | 候选人证书表 | 其他 | 主数据表 | `candidate_id` | `candidate_id`, `cert_config_id`, `user_id`, `member_id`, `competition_series_id`, `competition_track_id`, `team_code`, `leader_teacher_id`, `guide_teacher_id` | `is_check`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `cert_config_info` | 证书配置表 | 现场证件 | 配置表 | `cert_config_id` | `cert_config_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id`, `course_id`, `training_program_id` | `cert_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `cert_exchange_rule_detail` | 赛证互通规则明细表 | 现场证件 | 配置表 | `detail_id` | `detail_id`, `rule_id`, `origin_cert_config_id`, `target_cert_config_id` | `rule_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `cert_org_info` | 证书颁发机构表 | 现场证件 | 主数据表 | `org_id` | `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `cert_player_info` | 证书人员表 | 现场证件 | 主数据表 | `rela_id` | `rela_id`, `user_id`, `member_id`, `team_code`, `cert_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `change_log` | 参赛信息变动日志表 | 其他 | 流水表 | `id` | `team_id`, `member_id`, `operator_user_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `competition_apply_info` | 赛事申请报名信息表 | 报名与团队 | 主数据表 | `member_id` | `member_id`, `competition_series_id`, `user_id`, `team_code`, `org_id`, `competition_track_id`, `leader_teacher_id` | `check_status`, `real_name_auth_status`, `pay_status`, `del_flag`, `invoice_status` | 是 | 是，Mapper+Service/Controller（16处文件） |
| `competition_awards_config` | 赛事奖项设置表 | 赛事主数据 | 配置表 | `awards_id` | `awards_id`, `competition_track_config_id`, `stage_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `competition_cert_exchange_apply` | 赛证互通申请表 | 报名与团队 | 业务事实表 | `apply_id` | `apply_id`, `order_id`, `rule_id`, `user_id`, `origin_cert_id`, `target_cert_id` | `repay_amount`, `apply_status`, `pay_status`, `invoice_status`, `pay_time`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_cert_exchange_rule` | 赛证互通规则表 | 赛事主数据 | 配置表 | `rule_id` | `rule_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id` | `ruler_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `competition_check_data_package` | 校验包表 | 报名与团队 | 业务事实表 | `package_id` | `package_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_check_info` | 校验项表 | 报名与团队 | 主数据表 | `check_item_id` | `check_item_id` | `check_item_id`, `check_item_name`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_config` | 赛事配置表 | 赛事主数据 | 配置表 | `config_id` | `config_id`, `competition_track_config_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（7处文件） |
| `competition_course_config` | 赛事关联课程配置表 | 赛事主数据 | 配置表 | `course_config_id` | `course_config_id`, `competition_series_id`, `course_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_enterprise_rela` | 赛事赞助企业关联关系表 | 赛事主数据 | 关系表 | `rela_id` | `rela_id`, `competition_series_id`, `competition_track_config_id`, `enterprise_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（5处文件） |
| `competition_grade_info` | 成绩表 | 报名与团队 | 主数据表 | `grade_id` | `grade_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id`, `course_id`, `training_program_id`, `member_id`, `user_id`, `team_code` | `del_flag` | 是 | 是，Mapper（4处文件） |
| `competition_main_info` | 赛事主数据表 | 赛事主数据 | 主数据表 | `competition_id` | `competition_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（15处文件） |
| `competition_promoted_apply_info` | 赛事晋级申请报名信息表 | 报名与团队 | 主数据表 | `apply_id` | `apply_id`, `competition_series_id`, `user_id`, `team_code`, `org_id`, `competition_track_id`, `leader_teacher_id` | `pay_status`, `invoice_status`, `apply_status`, `del_flag` | 是 | 是，Mapper（4处文件） |
| `competition_promoted_info` | 赛事晋级表 | 报名与团队 | 主数据表 | `promoted_id` | `promoted_id`, `competition_series_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_scene_credential` | 赛事现场证件实例表 | 现场证件 | 业务事实表 | `credential_id` | `credential_id`, `schedule_id`, `target_id`, `scope_ref_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id`, `team_code`, `member_id`, `user_id` | `active_core_credential_key`, `valid_from`, `valid_to`, `credential_status`, `report_status`, `material_status`, `waiting_status`, `del_flag` | 是 | 是，Mapper（8处文件） |
| `competition_scene_credential_scope_grant` | 赛事现场证件作用域授权表 | 一证多权 grant | 业务事实表 | `grant_id` | `grant_id`, `credential_id`, `competition_series_id`, `scope_ref_id`, `source_schedule_id`, `source_target_id` | `active_grant_key`, `valid_from`, `valid_to`, `grant_status`, `deleted` | 是 | 是，Mapper（3处文件） |
| `competition_scene_notice` | 赛事现场通知 | 赛事主数据 | 业务事实表 | `notice_id` | `notice_id`, `competition_series_id`, `competition_id`, `target_id`, `user_id`, `member_id` | `publish_status`, `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_scene_notice_schedule` | 赛事通知可见赛场关系 | 赛事主数据 | 业务事实表 | `id` | `notice_id`, `schedule_id` | - | 否 | 是，Mapper（2处文件） |
| `competition_scene_operation_log` | 赛事现场扫码操作流水表 | 现场 operation_log | 流水表 | `log_id` | `log_id`, `credential_id`, `schedule_id`, `target_id`, `competition_series_id`, `team_code`, `member_id`, `user_id`, `competition_track_id`, `operator_user_id` | `apply_check_result`, `schedule_check_result`, `identity_check_result`, `request_payload`, `response_payload`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `competition_scene_resource` | 赛事现场设备资源台账表 | 资源台账 | 业务事实表 | `resource_id` | `resource_id` | `resource_status`, `deleted` | 是 | 是，Mapper（6处文件） |
| `competition_scene_resource_reservation` | 赛事现场设备资源预约记录表 | 资源预约 | 业务事实表 | `reservation_id` | `reservation_id`, `slot_id`, `schedule_resource_id`, `schedule_id`, `resource_id`, `event_id`, `competition_series_id`, `reservation_source_schedule_id`, `team_code`, `user_id` | `active_reservation_key`, `reservation_status`, `cancel_time`, `cancel_reason`, `check_status`, `check_user_id`, `check_time`, `deleted` | 是 | 是，Mapper+Service/Controller（5处文件） |
| `competition_scene_resource_schedule_scope` | 赛事现场资源允许预约赛场范围表 | 资源部署 | 配置表 | `scope_id` | `scope_id`, `schedule_resource_id`, `resource_id`, `allowed_schedule_id` | `enabled`, `deleted` | 是 | 是，Mapper（2处文件） |
| `competition_scene_resource_slot` | 赛事现场设备资源预约时段表 | 资源时段 | 业务事实表 | `slot_id` | `slot_id`, `schedule_resource_id`, `schedule_id`, `resource_id`, `event_id` | `slot_status`, `deleted` | 是 | 是，Mapper（5处文件） |
| `competition_scene_resource_slot_group_scope` | 赛事现场资源预约时段允许组别表 | 资源时段 | 配置表 | `id` | `slot_id`, `schedule_resource_id` | `enabled`, `deleted` | 是 | 是，Mapper（2处文件） |
| `competition_scene_schedule` | 赛事现场赛场安排表 | 赛场安排 | 业务事实表 | `schedule_id` | `schedule_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id` | `status`, `del_flag` | 是 | 是，Mapper（10处文件） |
| `competition_scene_schedule_resource` | 赛事现场赛场资源布置表 | 资源部署 | 业务事实表 | `schedule_resource_id` | `schedule_resource_id`, `schedule_id`, `resource_id`, `event_id` | `booking_status`, `booking_open_time`, `booking_close_time`, `deleted` | 是 | 是，Mapper（7处文件） |
| `competition_scene_schedule_target` | 赛事现场赛场安排对象表 | 赛场安排 | 业务事实表 | `target_id` | `target_id`, `schedule_id`, `competition_series_id`, `review_object_id`, `team_code`, `member_id`, `user_id`, `org_id`, `competition_track_id`, `leader_teacher_id` | `review_object_id`, `match_status`, `status`, `del_flag` | 是 | 是，Mapper（6处文件） |
| `competition_scene_subject_operation_state` | 赛事现场主体操作状态表 | 现场 operation_state | 状态表 | `state_id` | `state_id`, `competition_series_id`, `scope_ref_id`, `credential_id`, `operator_user_id`, `delegate_user_id`, `delegate_credential_id`, `last_log_id` | `state_id`, `operation_status`, `deleted` | 是 | 是，Mapper（3处文件） |
| `competition_series_info` | 赛事系列信息表 | 赛事主数据 | 主数据表 | `competition_series_id` | `competition_series_id`, `competition_id`, `enterprise_id`, `user_id`, `org_id` | `competition_status`, `check_status`, `del_flag` | 是 | 是，Mapper（17处文件） |
| `competition_stage_config` | 赛事阶段配置表 | 赛事主数据 | 配置表 | `stage_id` | `stage_id`, `competition_series_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（7处文件） |
| `competition_title_notice` | 提示信息表 | 赛事主数据 | 业务事实表 | `notice_id` | `notice_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_track_config` | 赛道配置 | 赛事主数据 | 配置表 | `competition_track_config_id` | `competition_track_config_id`, `competition_track_id` | `del_flag` | 是 | 是，Mapper（10处文件） |
| `competition_track_info` | 赛事赛道配置 | 赛事主数据 | 主数据表 | `track_id` | `track_id`, `competition_track_id`, `competition_series_id`, `check_package_id` | `check_status`, `del_flag`, `check_package_id` | 是 | 是，Mapper（7处文件） |
| `competition_user_payment_record` | 参赛人员缴费记录表 | 支付 | 流水表 | `record_id` | `record_id`, `competition_series_id`, `user_id`, `org_id` | `payment_amount`, `payment_time`, `payment_status`, `del_flag` | 是 | 否，未检出直接引用 |
| `competition_work_link_info` | 作品打分链接信息表 | 赛事主数据 | 主数据表 | `link_id` | `link_id`, `works_id`, `competition_series_id`, `stage_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `competition_works` | 赛事作品表 | 赛事主数据 | 业务事实表 | `works_id` | `works_id`, `competition_series_id`, `stage_id`, `user_id`, `team_code`, `org_id` | `works_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `component_data_source_rela` | 页面、组件、数据源关联关系表 | 内容管理 | 关系表 | `rela_id` | `rela_id`, `page_id`, `component_id`, `data_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `component_library_info` | 组件库信息表 | 内容管理 | 主数据表 | `component_id` | `component_id`, `user_id`, `org_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `component_library_info_copy1` | 组件库信息表 | 内容管理 | 业务事实表 | `component_id` | `component_id`, `user_id`, `org_id` | `status`, `del_flag` | 是 | 否，未检出直接引用 |
| `content_banner_info` | banner图管理 | 内容管理 | 主数据表 | `id` | `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `content_column` | 内容栏目表 | 内容管理 | 业务事实表 | `column_id` | `column_id`, `parent_id`, `menu_id` | `status`, `del_flag` | 是 | 是，Mapper（4处文件） |
| `content_detail` | 内容详情表 | 内容管理 | 流水表 | `detail_id` | `detail_id`, `column_id` | `del_flag` | 是 | 是，Mapper+Service/Controller（4处文件） |
| `content_file` | 内容文件表 | 内容管理 | 业务事实表 | `file_id` | `file_id`, `column_id` | `status`, `del_flag` | 是 | 是，Mapper+Service/Controller（4处文件） |
| `course_chapter_info` | 章节信息表 | 其他 | 主数据表 | `chapter_id` | `chapter_id`, `course_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `course_chapter_video` | 章节视频信息表 | 其他 | 业务事实表 | `video_id` | `video_id`, `chapter_id`, `task_id`, `user_id`, `org_id` | `publish_status`, `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `course_classify_info` | 课程分类信息表 | 其他 | 主数据表 | `classify_id` | `classify_id`, `user_id`, `org_id` | `status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `course_info` | 课程信息表 | 其他 | 主数据表 | `course_id` | `course_id`, `classify_id`, `user_id`, `org_id` | `check_status`, `publish_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `course_recommend_info` | 课程推荐信息表 | 其他 | 主数据表 | `remd_id` | `remd_id`, `user_id`, `org_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `course_recommend_rela` | 课程推荐关联关系表 | 其他 | 关系表 | `id` | `remd_id`, `course_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `data_source_info` | 数据源信息表 | 内容管理 | 主数据表 | `data_id` | `data_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `export_manage` | 导出管理表 | 文件 | 业务事实表 | `id` | `user_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `file_download_record` | 文件下载记录 | 文件 | 流水表 | `id` | `user_id`, `task_id`, `file_task_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `file_task` | 文件分发任务表 | 文件 | 业务事实表 | `id` | `user_id`, `org_id` | `task_status`, `del_flag` | 是 | 是，Mapper（5处文件） |
| `file_task_config` | 文件配置表 | 文件 | 配置表 | `id` | `task_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `file_upload_manager` | 文件上传管理表 | 文件 | 主数据表 | `id` | `file_task_id`, `user_id`, `competition_series_id`, `competition_stage_id`, `leader_teacher_id`, `team_code`, `org_id` | `del_flag`, `submit_status` | 是 | 是，Mapper+Service/Controller（12处文件） |
| `file_upload_record` | 文件上传管理表 | 文件 | 流水表 | `id` | `file_task_id`, `user_id`, `competition_series_id`, `competition_stage_id`, `leader_teacher_id`, `team_code`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `flw_channel_definition` | Flowable 事件/批处理表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_ev_databasechangelog` | Flowable 事件/批处理表 | 其他 | 配置表 | - | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_ev_databasechangeloglock` | Flowable 事件/批处理表 | 其他 | 配置表 | `ID` | - | `LOCKED`, `LOCKGRANTED`, `LOCKEDBY` | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_event_definition` | Flowable 事件/批处理表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_event_deployment` | Flowable 事件/批处理表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_event_resource` | Flowable 事件/批处理表 | 其他 | 配置表 | `ID_` | - | - | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_ru_batch` | Flowable 事件/批处理表 | 其他 | 业务事实表 | `ID_` | - | `STATUS_` | 否 | 框架表，业务代码少量/无直接引用 |
| `flw_ru_batch_part` | Flowable 事件/批处理表 | 其他 | 业务事实表 | `ID_` | `BATCH_ID_->flw_ru_batch.ID_` | `STATUS_` | 否 | 框架表，业务代码少量/无直接引用 |
| `gen_table` | 代码生成业务表 | 其他 | 业务事实表 | `table_id` | `table_id` | - | 否 | 是，Mapper（2处文件） |
| `gen_table_column` | 代码生成业务表字段 | 其他 | 业务事实表 | `column_id` | `column_id`, `table_id` | - | 否 | 是，Mapper（3处文件） |
| `identity_info` | 身份认证信息表 | 系统用户与权限 | 主数据表 | `auth_id` | `auth_id`, `user_id`, `student_card_id`, `org_id` | `check_status`, `del_flag` | 是 | 是，Mapper（5处文件） |
| `im_customer_dialogue` | 客服对话管理表 | 其他 | 业务事实表 | `dialogue_id` | `dialogue_id`, `user_id`, `cust_id`, `org_id` | `status`, `del_flag` | 是 | 否，未检出直接引用 |
| `im_customer_service` | 客服人员信息 | 其他 | 业务事实表 | - | `cust_id`, `dept_id`, `user_id`, `org_id` | `work_status`, `del_flag` | 是 | 否，未检出直接引用 |
| `im_file_info` | 文件 | 其他 | 主数据表 | `id` | - | - | 否 | 是，代码（1处文件） |
| `im_friend` | 好友 | 其他 | 业务事实表 | `id` | `user_id`, `friend_id` | `deleted` | 是 | 是，代码（1处文件） |
| `im_group` | im group | 其他 | 业务事实表 | `id` | `owner_id` | - | 否 | 是，代码（1处文件） |
| `im_group_member` | 群成员 | 报名与团队 | 关系表 | `id` | `group_id`, `user_id` | - | 否 | 是，代码（1处文件） |
| `im_group_message` | 群消息 | 其他 | 业务事实表 | `id` | `tmp_id`, `group_id`, `send_id` | `status` | 否 | 是，代码（1处文件） |
| `im_leave_message` | 离线留言信息表 | 其他 | 业务事实表 | - | `msg_id`, `user_id`, `cust_id`, `org_id` | `status`, `del_flag` | 是 | 否，未检出直接引用 |
| `im_private_message` | 私聊消息 | 其他 | 业务事实表 | `id` | `tmp_id`, `send_id`, `recv_id` | `status` | 否 | 是，代码（1处文件） |
| `im_sensitive_word` | 敏感词 | 其他 | 业务事实表 | `id` | - | `enabled` | 否 | 是，代码（1处文件） |
| `im_user` | 用户 | 其他 | 业务事实表 | `id` | - | - | 否 | 是，代码（1处文件） |
| `invoice_info` | 发票信息表 | 支付 | 主数据表 | `id` | `order_id`, `user_id`, `org_id` | `issued_status`, `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `invoice_per_info` | 开票信息记录 | 支付 | 主数据表 | `id` | `enterprise_id`, `user_id` | `taxpayer_identification_number`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `menu_component_rela` | 菜单组件关系表 | 内容管理 | 关系表 | `menu_component_rela_id` | `menu_component_rela_id`, `menu_id`, `component_id`, `user_data_id`, `org_data_id` | `del_flag` | 是 | 否，未检出直接引用 |
| `menu_info` | 菜单信息表 | 其他 | 主数据表 | `menu_id` | `menu_id`, `page_id`, `user_data_id`, `org_data_id` | `status`, `del_flag` | 是 | 否，未检出直接引用 |
| `merchant_param_config` | 商户参数配置表（支付和发票） | 支付 | 配置表 | `id` | `mer_id`, `fee_user_id`, `term_id`, `pay_app_id`, `user_id`, `org_id` | `pay_valid_time`, `pay_app_id`, `pay_app_secret`, `pay_private_key`, `pay_public_key`, `checker`, `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `merchant_work_scope` | 商户作用范围表 | 支付 | 配置表 | `id` | `config_id`, `event_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `message_info` | 消息管理 | 其他 | 主数据表 | `msg_id` | `msg_id`, `user_id`, `org_id` | `msg_status`, `del_flag` | 是 | 否，未检出直接引用 |
| `nationwide_college_info` | 全国院校信息表 | 系统用户与权限 | 主数据表 | `id` | - | - | 否 | 是，Mapper（13处文件） |
| `news_classify` | 新闻分类表 | 内容管理 | 业务事实表 | `classify_id` | `classify_id`, `parent_classify_id`, `user_id`, `org_id` | `classify_status`, `del_flag` | 是 | 否，未检出直接引用 |
| `news_info` | 新闻信息表 | 内容管理 | 主数据表 | `news_id` | `news_id`, `classify_id`, `user_id`, `org_id` | `news_status`, `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `notice_info` | notice info | 内容管理 | 主数据表 | `notice_id` | `notice_id`, `user_id`, `org_id` | `notice_status`, `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `notification_receiver` | 站内信接收表 | 其他 | 业务事实表 | `id` | `notification_id->notification_sender.id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `notification_sender` | 站内信发送表 | 其他 | 业务事实表 | `id` | `sender_user_id`, `related_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `offline_v3_apply_dedup_20260705_001` | offline v3 apply dedup 20260705 001 | 导入中间表 | 导入中间表 | - | `team_code`, `member_id`, `user_id`, `org_id`, `competition_track_id`, `leader_teacher_id` | - | 否 | 否，未检出直接引用 |
| `offline_v3_scene_excel_20260705_001` | offline v3 scene excel 20260705 001 | 导入中间表 | 导入中间表 | `row_id` | `row_id` | - | 否 | 否，未检出直接引用 |
| `offline_v3_scene_player_20260705_001` | offline v3 scene player 20260705 001 | 现场对象 | 导入中间表 | `player_row_id` | `player_row_id`, `row_id` | - | 否 | 否，未检出直接引用 |
| `offline_v3_target_match_20260705_001` | offline v3 target match 20260705 001 | 现场对象 | 导入中间表 | `match_id` | `match_id`, `row_id`, `player_row_id`, `schedule_id`, `team_id`, `team_code`, `member_id`, `user_id`, `org_id`, `competition_track_id` | `match_status` | 否 | 否，未检出直接引用 |
| `offline_v4_apply_dedup_20260705_001` | offline v4 apply dedup 20260705 001 | 导入中间表 | 导入中间表 | - | `team_code`, `member_id`, `user_id`, `org_id`, `competition_track_id`, `leader_teacher_id` | - | 否 | 否，未检出直接引用 |
| `offline_v4_scene_excel_20260705_001` | offline v4 scene excel 20260705 001 | 导入中间表 | 导入中间表 | `row_id` | `row_id` | - | 否 | 否，未检出直接引用 |
| `offline_v4_scene_player_20260705_001` | offline v4 scene player 20260705 001 | 现场对象 | 导入中间表 | `player_row_id` | `player_row_id`, `row_id` | - | 否 | 否，未检出直接引用 |
| `offline_v4_target_match_20260705_001` | offline v4 target match 20260705 001 | 现场对象 | 导入中间表 | `match_id` | `match_id`, `row_id`, `player_row_id`, `schedule_id`, `team_id`, `team_code`, `member_id`, `user_id`, `org_id`, `competition_track_id` | `match_status` | 否 | 否，未检出直接引用 |
| `operation_config` | 操作权限配置表 | 赛事主数据 | 配置表 | `id` | `competition_series_id`, `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `operation_flow` | 团队信息操作和流程关联表 | 赛事主数据 | 业务事实表 | `team_code`, `flow_id` | `team_code`, `flow_id` | `flow_status` | 否 | 是，Mapper（2处文件） |
| `operation_times` | 队伍操作次数表 | 赛事主数据 | 业务事实表 | `id` | `team_code`, `member_id`, `config_id` | - | 否 | 是，Mapper（2处文件） |
| `order_goods_relation` | 订单商品关联表 | 支付 | 关系表 | `id` | `order_id`, `commodity_id`, `org_id` | `del_flag`, `pay_status` | 是 | 是，Mapper（3处文件） |
| `order_info` | 订单信息表 | 支付 | 主数据表 | `id` | `order_id`, `user_id`, `commodity_id`, `org_id`, `target_order_id`, `refund_order_id`, `mer_id`, `out_order_id`, `cmb_order_id`, `biz_order_id` | `pay_mode`, `pay_status`, `pay_time`, `del_flag`, `pay_method`, `audit_opinion`, `payment_proof_files`, `invoice_status`, `refund_status`, `pay_order_id` | 是 | 是，Mapper（3处文件） |
| `order_statement_record` | 对账单对账记录 | 支付 | 流水表 | `id` | `order_id`, `org_id` | `statement_file_name`, `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `page_manager_info` | 页面管理信息表 | 内容管理 | 主数据表 | `page_id` | `page_id`, `user_id`, `org_id` | `publish_status`, `check_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `questions` | 常见问题 | 内容管理 | 业务事实表 | `id` | `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper+Service/Controller（10处文件） |
| `review_activity` | 评审活动表 | 评审任务 | 业务事实表 | `id` | - | `review_start_time`, `review_end_time`, `status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `review_activity_user_role` | 活动内用户角色表 | 评审任务 | 关系表 | `id` | `activity_id`, `user_id`, `reviewer_id`, `panel_id` | `reviewer_id`, `enabled`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_assignment` | 评审任务分配表 | 评审任务 | 业务事实表 | `id` | `activity_id`, `round_id`, `object_id`, `reviewer_id`, `reviewer_user_id`, `panel_id` | `reviewer_id`, `reviewer_user_id`, `status`, `del_flag` | 是 | 是，Mapper+Service/Controller（5处文件） |
| `review_audit_log` | 审计日志表 | 评审任务 | 流水表 | `id` | `activity_id`, `round_id`, `object_id`, `biz_id`, `operator_user_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_criteria` | 评分指标表 | 评审评分 | 业务事实表 | `id` | `rule_id`, `parent_id` | `enabled`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_expert_review_notes` | 专家审阅备注信息记录 | 评审任务 | 业务事实表 | `id` | `processed_relation_id`, `expert_id` | `del_flag` | 是 | 是，Mapper（1处文件） |
| `review_group_specialist_relation` | 专家组与专家关联关系表 | 评审任务 | 关系表 | `group_rela_id` | `group_rela_id`, `group_id`, `user_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `review_object` | 评审对象表 | 评审任务 | 业务事实表 | `id` | `activity_id`, `source_biz_id`, `source_team_id`, `source_registration_id` | `submit_status`, `locked_time`, `invalid_time`, `del_flag` | 是 | 是，Mapper+Service/Controller（8处文件） |
| `review_object_certificate_ref` | 评审对象参赛证映射表 | 评审材料 | 关系表 | `id` | `activity_id`, `round_id`, `object_id`, `certificate_id`, `person_id`, `user_id`, `member_id`, `source_biz_id`, `source_team_id`, `source_registration_id` | `valid_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_object_external_ref` | 外部业务关联表 | 评审材料 | 关系表 | `id` | `activity_id`, `object_id`, `source_biz_id`, `source_team_id`, `source_registration_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_object_material` | 评审材料表 | 评审材料 | 业务事实表 | `id` | `activity_id`, `object_id`, `source_biz_id` | `visible_to_reviewer`, `status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `review_object_member` | 评审对象成员表 | 评审材料 | 关系表 | `id` | `activity_id`, `object_id`, `user_id`, `person_id`, `certificate_id`, `source_biz_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_object_submit_log` | 评审对象提交状态日志表 | 评审材料 | 流水表 | `id` | `activity_id`, `object_id`, `operator_user_id` | `before_status`, `after_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_panel` | 专家组表 | 评审任务 | 业务事实表 | `id` | `activity_id`, `round_id`, `leader_user_id`, `secretary_user_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_panel_member` | 专家组成员表 | 评审任务 | 关系表 | `id` | `activity_id`, `round_id`, `panel_id`, `user_id`, `reviewer_id` | `reviewer_id`, `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_processed_relation` | 评审文件处理前后对应关系 | 文件 | 关系表 | `id` | `manager_id` | `del_flag`, `review_status`, `review_time` | 是 | 是，Mapper（3处文件） |
| `review_record` | 专家文件阅读状态记录 | 评审评分 | 流水表 | `id` | `activity_id`, `round_id`, `object_id`, `assignment_id`, `reviewer_id`, `reviewer_user_id`, `expert_id`, `file_id` | `reviewer_id`, `reviewer_user_id`, `record_status`, `locked_time`, `invalid_time`, `invalid_reason`, `review_status`, `review_time`, `del_flag` | 是 | 是，Mapper（9处文件） |
| `review_result` | 评审结果表 | 评审评分 | 业务事实表 | `id` | `activity_id`, `round_id`, `object_id` | `reviewer_count`, `result_status`, `del_flag` | 是 | 是，Mapper+Service/Controller（3处文件） |
| `review_result_publish_log` | 结果发布日志表 | 评审评分 | 流水表 | `id` | `activity_id`, `round_id`, `object_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_round` | 评审轮次表 | 评审任务 | 业务事实表 | `id` | `activity_id`, `rule_id` | `status`, `del_flag` | 是 | 是，Mapper（4处文件） |
| `review_rule` | 评审规则表 | 评审评分 | 配置表 | `id` | `activity_id`, `round_id` | `enabled`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_score_detail` | 评分明细表 | 评审评分 | 流水表 | `id` | `record_id`, `activity_id`, `round_id`, `object_id`, `criteria_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `review_session` | 现场评审场次表 | 评审任务 | 业务事实表 | `id` | `activity_id`, `round_id`, `secretary_user_id`, `panel_id`, `current_object_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_session_event_log` | 现场事件日志表 | 评审任务 | 流水表 | `id` | `activity_id`, `round_id`, `session_id`, `object_id`, `operator_user_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_session_object` | 现场评审对象顺序表 | 评审任务 | 业务事实表 | `id` | `activity_id`, `round_id`, `session_id`, `object_id` | `checkin_status`, `review_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_specialist_group_info` | 专家组表 | 评审任务 | 主数据表 | `group_id` | `group_id` | `allot_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_submission_permission` | 填报权限表 | 评审材料 | 业务事实表 | `id` | `activity_id`, `object_id`, `user_id`, `org_id`, `source_biz_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_task_allot_group` | 评审任务分配组信息表 | 评审任务 | 业务事实表 | `review_group_id` | `review_group_id` | `review_group_id`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `review_task_allot_group_relation` | 评审任务分配组关联关系表 | 评审任务 | 关系表 | `relation_id` | `relation_id`, `review_group_id`, `review_id` | `review_group_id`, `review_id`, `del_flag` | 是 | 是，Mapper（4处文件） |
| `review_task_info` | 评审任务分配信息表 | 评审任务 | 主数据表 | `review_id` | `review_id`, `file_upload_manager_id`, `review_group_id` | `review_id`, `review_name`, `review_start_time`, `review_end_time`, `review_desc`, `review_group_id`, `distribute_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `review_task_specialist_relation` | 任务分配专家关联关系表 | 评审任务 | 关系表 | `rela_id` | `rela_id`, `review_id`, `user_id` | `review_id`, `allot_status`, `review_status`, `del_flag` | 是 | 是，Mapper（5处文件） |
| `reviewer_profile` | 评审人画像表 | 评审任务 | 业务事实表 | `id` | `user_id` | `reviewer_name`, `status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `school_specialty_info` | 专业信息表 | 系统用户与权限 | 主数据表 | `id` | - | `del_flag` | 是 | 是，Mapper（2处文件） |
| `search_config` | 检索项配置 | 内容管理 | 配置表 | `search_id` | `search_id`, `menu_id`, `user_data_id`, `org_data_id` | `del_flag`, `search_status` | 是 | 否，未检出直接引用 |
| `sponsoring_enterprise_info` | 赞助企业信息表 | 赛事主数据 | 主数据表 | `enterprise_id` | `enterprise_id`, `user_id`, `org_id` | `display_status`, `del_flag` | 是 | 是，Mapper（6处文件） |
| `suggestion_feedback_info` | 意见反馈信息表 | 内容管理 | 主数据表 | `sugg_back_id` | `sugg_back_id`, `user_id`, `org_id` | `deal_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `sys_audit_config` | 审核流程环节配置表 | 系统用户与权限 | 配置表 | `config_id` | `config_id`, `audit_id`, `user_id`, `org_id` | `audit_id`, `check_person_type`, `check_person_org`, `check_person_role`, `check_person`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `sys_audit_log` | 审计日志记录表 | 系统用户与权限 | 流水表 | `audit_id` | `audit_id`, `user_id`, `data_id` | `audit_id`, `audit_type`, `audit_category`, `audit_status`, `audit_by`, `audit_time`, `audit_remark` | 否 | 是，Mapper（2处文件） |
| `sys_audit_main_config` | 系统审核流程配置表 | 系统用户与权限 | 配置表 | `audit_id` | `audit_id`, `user_id`, `org_id` | `audit_id`, `audit_title`, `audit_type`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `sys_audit_task` | 审核任务表 | 系统用户与权限 | 业务事实表 | `task_id` | `task_id`, `business_id`, `audit_id`, `user_id`, `org_id` | `audit_id`, `now_check_step`, `check_time`, `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `sys_audit_task_subinfo` | 审核任务审核信息表 | 系统用户与权限 | 业务事实表 | `sub_id` | `sub_id`, `task_id`, `audit_config_id`, `video_id`, `user_id`, `org_id` | `audit_config_id`, `check_per`, `check_time`, `check_opinion`, `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `sys_config` | 参数配置表 | 系统用户与权限 | 配置表 | `config_id` | `config_id` | - | 否 | 是，Mapper（2处文件） |
| `sys_dict` | 字典表 | 系统用户与权限 | 配置表 | `id` | - | `status`, `del_flag` | 是 | 否，未检出直接引用 |
| `sys_dict_data` | 字典数据表 | 系统用户与权限 | 配置表 | `dict_code` | - | `status` | 否 | 是，Mapper（5处文件） |
| `sys_dict_item` | 字典表 | 系统用户与权限 | 配置表 | `dict_id` | `dict_id` | `del_flag` | 是 | 否，未检出直接引用 |
| `sys_dict_type` | 字典类型表 | 系统用户与权限 | 配置表 | `dict_id` | `dict_id` | `status` | 否 | 是，Mapper（2处文件） |
| `sys_error_log` | 错误日志记录表 | 系统用户与权限 | 流水表 | `error_id` | `error_id` | `status` | 否 | 是，Mapper（2处文件） |
| `sys_job` | 定时任务调度表 | 系统用户与权限 | 业务事实表 | `job_id`, `job_name`, `job_group` | `job_id` | `status` | 否 | 是，Mapper（2处文件） |
| `sys_job_log` | 定时任务调度日志表 | 系统用户与权限 | 流水表 | `job_log_id` | `job_log_id` | `status` | 否 | 是，Mapper（2处文件） |
| `sys_login_log` | 系统登录日志表 | 系统用户与权限 | 流水表 | `id` | `user_id` | `login_status`, `del_flag` | 是 | 否，未检出直接引用 |
| `sys_logininfor` | 系统访问记录 | 系统用户与权限 | 流水表 | `info_id` | `info_id` | `status` | 否 | 是，Mapper（2处文件） |
| `sys_menu` | 菜单权限表 | 系统用户与权限 | 主数据表 | `menu_id` | `menu_id`, `parent_id`, `bind_column_id`, `bind_file_id`, `bind_detail_id` | `status` | 否 | 是，Mapper（14处文件） |
| `sys_notice` | 系统通知公告表 | 系统用户与权限 | 业务事实表 | `notice_id` | `notice_id`, `user_id`, `org_id` | `status`, `check_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `sys_oper_log` | 操作日志记录 | 系统用户与权限 | 流水表 | `oper_id` | `oper_id` | `status` | 否 | 是，Mapper（1处文件） |
| `sys_operation_log` | 系统操作日志 | 系统用户与权限 | 流水表 | `log_code` | - | `status`, `del_flag` | 是 | 否，未检出直接引用 |
| `sys_org` | 系统机构信息表 | 系统用户与权限 | 业务事实表 | `org_id` | `org_id`, `parent_id` | `status`, `del_flag` | 是 | 是，Mapper（6处文件） |
| `sys_post` | 岗位信息表 | 系统用户与权限 | 业务事实表 | `post_id` | `post_id` | `status` | 否 | 是，Mapper（2处文件） |
| `sys_role` | 角色信息表 | 系统用户与权限 | 关系表 | `role_id` | `role_id` | `menu_check_strictly`, `dept_check_strictly`, `org_check_strictly`, `status`, `lock_flag`, `del_flag` | 是 | 是，Mapper（11处文件） |
| `sys_role_menu` | 角色和菜单关联表 | 系统用户与权限 | 业务事实表 | `role_id`, `menu_id` | `role_id`, `menu_id` | `del_flag` | 是 | 是，Mapper（10处文件） |
| `sys_role_org` | 角色和机构关联表 | 系统用户与权限 | 业务事实表 | - | `role_id`, `org_id` | `del_flag` | 是 | 是，Mapper（3处文件） |
| `sys_sender_message_log` | 推送信息日志表 | 系统用户与权限 | 流水表 | `send_id` | `send_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `sys_user` | 用户信息表 | 系统用户与权限 | 主数据表 | `user_id` | `user_id`, `org_id`, `sys_user_id`, `open_id` | `status`, `del_flag`, `auth_status`, `identity_status` | 是 | 是，Mapper+Service/Controller（19处文件） |
| `sys_user_group` | 用户组管理 | 系统用户与权限 | 业务事实表 | `id` | - | `del_flag` | 是 | 是，Mapper（3处文件） |
| `sys_user_group_competition_relation` | 用户组关联赛事关系表 | 系统用户与权限 | 关系表 | `id` | `user_group_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `sys_user_org` | 用户机构关联关系表 | 系统用户与权限 | 业务事实表 | `user_id`, `org_id` | `user_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `sys_user_post` | 用户与岗位关联表 | 系统用户与权限 | 业务事实表 | `user_id`, `post_id` | `user_id`, `post_id` | - | 否 | 是，Mapper（4处文件） |
| `sys_user_role` | 用户和角色关联表 | 系统用户与权限 | 关系表 | `user_id`, `role_id` | `user_id`, `role_id` | `status` | 否 | 是，Mapper（5处文件） |
| `teacher_tmp_info` | 教师导入临时表 | 系统用户与权限 | 主数据表 | - | - | `status` | 否 | 是，Mapper（2处文件） |
| `team_manager_info` | 团队管理表 | 报名与团队 | 主数据表 | `team_id` | `team_id`, `team_code`, `team_leader_id`, `competition_series_id`, `competition_track_id`, `user_id`, `org_id` | `check_status`, `del_flag` | 是 | 是，Mapper（7处文件） |
| `team_member_rela` | 团队关联关系表 | 报名与团队 | 关系表 | `rela_id` | `rela_id`, `team_code`, `user_id`, `org_id` | `check_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `undo_log` | 分布式事务使用 | 其他 | 流水表 | `id` | `branch_id` | `log_status` | 否 | 否，未检出直接引用 |
| `user_certificate` | 用户证书表 | 现场证件 | 业务事实表 | `cert_id` | `cert_id`, `user_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id`, `course_id`, `training_program_id`, `cert_exchange_id`, `cert_config_id`, `team_code` | `cert_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `user_certificate_history` | user certificate history | 现场证件 | 快照表 | `cert_id` | `cert_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id`, `course_id`, `training_program_id`, `cert_exchange_id`, `cert_config_id`, `team_code`, `user_id` | `cert_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `user_certificate_origin` | user certificate origin | 现场证件 | 业务事实表 | `cert_id` | `cert_id`, `competition_series_id`, `competition_stage_id`, `competition_track_id`, `course_id`, `training_program_id`, `cert_exchange_id`, `cert_config_id`, `team_code`, `user_id` | `cert_status`, `del_flag` | 是 | 是，Mapper（3处文件） |
| `user_collect` | 用户收藏信息表 | 其他 | 业务事实表 | `collect_id` | `collect_id`, `user_id`, `course_id`, `competition_id`, `competition_series_id`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `user_grade_info` | 用户成绩信息表 | 报名与团队 | 主数据表 | `grade_id` | `grade_id`, `competition_series_id`, `stage_id`, `user_id`, `team_code`, `org_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `user_study_record` | 用户学习记录表 | 其他 | 流水表 | `record_id` | `record_id`, `course_id`, `chapter_id`, `video_id`, `user_id`, `org_id` | `del_flag` | 是 | 否，未检出直接引用 |
| `wechat_integration` | 微信集成管理表 | 其他 | 业务事实表 | `id` | `user_id`, `wx_open_id` | `status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `wf_category` | 流程分类表 | 其他 | 配置表 | `category_id` | `category_id` | `del_flag` | 是 | 是，代码（1处文件） |
| `wf_copy` | 流程抄送表 | 其他 | 业务事实表 | `copy_id` | `copy_id`, `process_id`, `category_id`, `deployment_id`, `instance_id`, `task_id`, `user_id`, `originator_id` | `del_flag` | 是 | 是，代码（3处文件） |
| `wf_deploy_form` | 流程实例关联表单 | 其他 | 业务事实表 | `deploy_id`, `form_key`, `node_key` | `deploy_id` | - | 否 | 是，Mapper（2处文件） |
| `wf_form` | 流程表单信息表 | 其他 | 配置表 | `form_id` | `form_id` | `del_flag` | 是 | 是，Mapper（2处文件） |
| `work_order` | 工单信息表 | 其他 | 业务事实表 | `order_id` | `order_id`, `order_source_id`, `user_id`, `org_id` | `order_deal_status`, `del_flag` | 是 | 是，Mapper（2处文件） |
| `work_order_transfer` | 工单转单记录 | 其他 | 业务事实表 | `id` | `order_id` | - | 否 | 是，Mapper（2处文件） |
| `wx_qc_code_config` | 二维码配置管理表 | 其他 | 配置表 | `code_config_id` | `code_config_id`, `competition_series_id` | `del_flag` | 是 | 是，Mapper（4处文件） |
| `wx_qc_code_record` | 二维码生成记录表 | 其他 | 流水表 | `record_id` | `record_id`, `code_config_id`, `user_id` | `code_status`, `del_flag` | 是 | 是，Mapper+Service/Controller（5处文件） |
| `wx_sign_in_info` | 签到表 | 其他 | 主数据表 | `sign_id` | `sign_id`, `user_id`, `record_id` | `del_flag`, `check_in_type` | 是 | 是，Mapper（2处文件） |
