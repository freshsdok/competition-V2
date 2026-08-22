# 唯一性和一致性审计

审计依据：`information_schema.statistics` 唯一索引、外键约束、Mapper/Service 静态引用。

## 已有唯一键概览

测试库非主键唯一索引数量：25。

| 表 | 唯一索引 | 字段 | 业务域 |
|---|---|---|---|
| `act_hi_procinst` | `PROC_INST_ID_` | `PROC_INST_ID_` | 其他 |
| `act_id_priv` | `ACT_UNIQ_PRIV_NAME` | `NAME_` | 其他 |
| `act_procdef_info` | `ACT_UNIQ_INFO_PROCDEF` | `PROC_DEF_ID_` | 其他 |
| `act_re_procdef` | `ACT_UNIQ_PROCDEF` | `KEY_`, `VERSION_`, `DERIVED_VERSION_`, `TENANT_ID_` | 其他 |
| `competition_scene_credential` | `uk_scene_credential_active_core_key` | `active_core_credential_key` | 现场证件 |
| `competition_scene_credential` | `uk_scene_credential_no` | `credential_no` | 现场证件 |
| `competition_scene_credential` | `uk_scene_credential_token` | `credential_token` | 现场证件 |
| `competition_scene_credential_scope_grant` | `uk_grant_active_key` | `active_grant_key` | 一证多权 grant |
| `competition_scene_notice_schedule` | `uk_scene_notice_schedule` | `notice_id`, `schedule_id` | 赛事主数据 |
| `competition_scene_resource` | `uk_scene_resource_code` | `resource_code`, `deleted` | 资源台账 |
| `competition_scene_resource_reservation` | `uk_scene_resource_active_reservation_key` | `active_reservation_key` | 资源预约 |
| `competition_scene_resource_reservation` | `uk_scene_resource_idempotency_key` | `idempotency_key` | 资源预约 |
| `competition_scene_schedule_target` | `uk_scene_target_key` | `schedule_id`, `target_key` | 赛场安排 |
| `flw_channel_definition` | `ACT_IDX_CHANNEL_DEF_UNIQ` | `KEY_`, `VERSION_`, `TENANT_ID_` | 其他 |
| `flw_event_definition` | `ACT_IDX_EVENT_DEF_UNIQ` | `KEY_`, `VERSION_`, `TENANT_ID_` | 其他 |
| `im_user` | `idx_user_name` | `user_name` | 其他 |
| `operation_config` | `uk_competition_type` | `competition_series_id`, `operation_type` | 赛事主数据 |
| `review_activity` | `uk_review_activity_code` | `activity_code` | 评审任务 |
| `review_assignment` | `uk_review_assignment_user` | `activity_id`, `round_id`, `object_id`, `reviewer_user_id` | 评审任务 |
| `review_object` | `uk_review_object_code` | `activity_id`, `object_code` | 评审任务 |
| `review_result` | `uk_review_result_object` | `activity_id`, `round_id`, `object_id` | 评审评分 |
| `suggestion_feedback_info` | `uk_back_code` | `back_code` | 内容管理 |
| `sys_dict` | `AK_Key_2` | `dict_type` | 系统用户与权限 |
| `sys_dict_type` | `dict_type` | `dict_type` | 系统用户与权限 |
| `undo_log` | `ux_undo_log` | `xid`, `branch_id` | 其他 |

## 重点一致性项

### 同一团队报名是否唯一
- 关联表：`competition_apply_info`, `team_manager_info`, `team_member_rela`
- 已有唯一键：未检出业务唯一键或仅有主键
- 风险等级：高
- 建议：建议对 `competition_series_id/team_code/deleted` 或报名事实源补 active 唯一键；历史导入可先做数据审计。

### 同一团队成员是否唯一
- 关联表：`team_member_rela`, `competition_apply_info`, `review_object_member`
- 已有唯一键：未检出业务唯一键或仅有主键
- 风险等级：高
- 建议：建议 `team_id/member_id/role_code/active_key` 或 `team_code/user_id/role_code/active_key`。

### 同一赛场 target 是否唯一
- 关联表：`competition_scene_schedule_target`
- 已有唯一键：`competition_scene_schedule_target.uk_scene_target_key`(schedule_id,target_key)
- 风险等级：高
- 建议：建议 `schedule_id + target_type + source_biz_id + active_key` 或 `schedule_id + team_code + active_key`。

### 同一主体核心证件是否唯一
- 关联表：`competition_scene_credential`
- 已有唯一键：`competition_scene_credential.uk_scene_credential_active_core_key`(active_core_credential_key), `competition_scene_credential.uk_scene_credential_no`(credential_no), `competition_scene_credential.uk_scene_credential_token`(credential_token)
- 风险等级：高
- 建议：建议 `subject_type + subject_id/team_code + credential_type + active_key`。

### 同一 active grant 是否唯一
- 关联表：`competition_scene_credential_scope_grant`
- 已有唯一键：`competition_scene_credential_scope_grant.uk_grant_active_key`(active_grant_key)
- 风险等级：高
- 建议：建议 `credential_id + scope_type + scope_ref_id + ability_code + active_key`。

### 同一 operation_state DONE 是否唯一
- 关联表：`competition_scene_subject_operation_state`
- 已有唯一键：未检出业务唯一键或仅有主键
- 风险等级：高
- 建议：建议 `schedule_id + subject_type + subject_id + operation_code + active_key`，DONE 事实不应多源分裂。

### 同一团队/个人有效预约是否唯一
- 关联表：`competition_scene_resource_reservation`
- 已有唯一键：`competition_scene_resource_reservation.uk_scene_resource_active_reservation_key`(active_reservation_key), `competition_scene_resource_reservation.uk_scene_resource_idempotency_key`(idempotency_key)
- 风险等级：高
- 建议：建议 `resource_id/slot_id + subject_type + subject_id + active_key`，取消态用 inactive key。

### idempotency_key 是否唯一
- 关联表：`competition_scene_resource_reservation`
- 已有唯一键：`competition_scene_resource_reservation.uk_scene_resource_active_reservation_key`(active_reservation_key), `competition_scene_resource_reservation.uk_scene_resource_idempotency_key`(idempotency_key)
- 风险等级：高
- 建议：凡存在 `idempotency_key` 应有唯一索引或按业务域加唯一组合。

### 同一 slot group scope 是否可能重复
- 关联表：`competition_scene_resource_slot_group_scope`
- 已有唯一键：未检出业务唯一键或仅有主键
- 风险等级：中高
- 建议：建议 `slot_id + group_code + active_key` 或 `slot_group_id + scope_type + scope_ref_id + active_key`。

### 同一 resource schedule scope 是否可能重复
- 关联表：`competition_scene_resource_schedule_scope`
- 已有唯一键：未检出业务唯一键或仅有主键
- 风险等级：中高
- 建议：建议 `resource_id + schedule_id + scope_type + scope_ref_id + active_key`。

## 仅靠代码保证的唯一性

- 多数业务写入路径通过 Service 查询后插入或更新保证“当前只有一条”，但数据库未强制，存在并发重复、补数据绕过和脚本导入重复风险。
- 资源预约、证件 grant、operation_state、review object external ref 等高并发/幂等场景不应只靠代码判断。

## active_key 建议

- 对有软删除或取消态的表使用 `active_key`：有效记录取固定值如 `1`，取消/删除记录取主键或空，使唯一约束只限制 active 事实。
- 优先级：证件核心实例、grant、operation_state、预约、slot scope、schedule scope、报名团队成员。
