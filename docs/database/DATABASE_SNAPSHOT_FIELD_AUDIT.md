# 快照字段审计

快照字段不是问题本身。问题在于命名不清时，调用方容易把操作时文本当成主数据事实源。

## 快照候选字段分布

| 表 | 业务域 | 快照候选字段 | 判断 |
|---|---|---|---|
| `act_id_membership` | 报名与团队 | `GROUP_ID_` | 建议补注释区分事实源/快照 |
| `cert_config_info` | 现场证件 | `cert_config_name`, `cert_manager_role`, `cert_link_name`, `awards_name` | 建议补注释区分事实源/快照 |
| `cert_exchange_rule_detail` | 现场证件 | `origin_cert_config_id`, `origin_cert_code`, `origin_cert_name`, `origin_cert_score`, `origin_year_limit`, `target_cert_name`, `target_cert_score` | 建议补注释区分事实源/快照 |
| `cert_org_info` | 现场证件 | `org_name`, `cert_link_name` | 建议补注释区分事实源/快照 |
| `competition_apply_info` | 报名与团队 | `competition_name`, `team_name`, `user_name`, `phone`, `email`, `guide_teacher`, `guide_teacher_phone`, `guide_teacher_email`, `real_name_auth_status`, `gap_score`, `competition_track_name`, `second_level_name`, `leader_teacher_id`, `leader_teacher`, `leader_teacher_phone`, `school`, `school_name`, `province_name`, `department_name`, `nationality_name` | 建议补注释区分事实源/快照 |
| `competition_cert_exchange_apply` | 报名与团队 | `origin_cert_code`, `origin_cert_id` | 建议补注释区分事实源/快照 |
| `competition_check_data_package` | 报名与团队 | `package_name` | 建议补注释区分事实源/快照 |
| `competition_check_info` | 报名与团队 | `check_item_name` | 建议补注释区分事实源/快照 |
| `competition_grade_info` | 报名与团队 | `user_name`, `score` | 建议补注释区分事实源/快照 |
| `competition_promoted_apply_info` | 报名与团队 | `competition_name`, `team_name`, `user_name`, `phone`, `email`, `guide_teacher`, `guide_teacher_phone`, `guide_teacher_email`, `competition_track_name`, `second_level_name`, `leader_teacher_id`, `school`, `school_name`, `province_name`, `nationality_name`, `competition_role_name` | 建议补注释区分事实源/快照 |
| `competition_scene_credential` | 现场证件 | `credential_name`, `competition_name`, `competition_stage_name`, `competition_track_name`, `second_level_name`, `team_name`, `user_name`, `phone`, `email`, `school`, `school_name`, `org_name`, `competition_role_name`, `leader_teacher_id`, `leader_teacher`, `guide_teacher`, `waiting_group_code`, `waiting_group_name`, `credential_snapshot_json`, `report_operator_name` | 合理，但姓名/学校/组别需标注为发证时快照 |
| `competition_scene_credential_scope_grant` | 一证多权 grant | `role_code`, `grant_snapshot_json` | 合理，但姓名/学校/组别需标注为发证时快照 |
| `competition_scene_operation_log` | 现场 operation_log | `team_name`, `user_name`, `competition_track_name`, `second_level_name`, `receiver_name`, `receiver_phone`, `operator_name`, `operator_phone`, `apply_snapshot_json` | 合理，日志应保存操作时快照 |
| `competition_scene_resource_reservation` | 资源预约 | `operator_name`, `group_code`, `group_name`, `shared_occupancy_snapshot`, `workstation_count_snapshot` | 合理，资源能力和占用口径应保存预约时快照 |
| `im_group_member` | 报名与团队 | `group_id`, `user_nick_name`, `remark_nick_name`, `remark_group_name` | 建议补注释区分事实源/快照 |
| `offline_v3_apply_dedup_20260705_001` | 导入中间表 | `user_name`, `team_name`, `school_name`, `phone`, `email`, `competition_role_name`, `competition_track_name`, `second_level_name`, `leader_teacher_id`, `leader_teacher`, `guide_teacher` | 合理，原始导入文本必须保留 |
| `offline_v3_scene_excel_20260705_001` | 导入中间表 | `source_sheet_name`, `school_name`, `player1_name`, `player2_name`, `venue_name`, `schedule_name` | 合理，原始导入文本必须保留 |
| `offline_v3_scene_player_20260705_001` | 现场对象 | `source_sheet_name`, `excel_school_name`, `player_name`, `venue_name`, `schedule_name` | 合理，原始导入文本必须保留 |
| `offline_v3_target_match_20260705_001` | 现场对象 | `source_sheet_name`, `excel_school_name`, `player_name`, `schedule_name`, `team_name`, `user_name`, `phone`, `email`, `school_name`, `competition_role_name`, `competition_track_name`, `second_level_name`, `leader_teacher_id`, `leader_teacher`, `guide_teacher` | 合理，原始导入文本必须保留 |
| `offline_v4_apply_dedup_20260705_001` | 导入中间表 | `user_name`, `team_name`, `school_name`, `phone`, `email`, `competition_role_name`, `competition_track_name`, `second_level_name`, `leader_teacher_id`, `leader_teacher`, `guide_teacher` | 合理，原始导入文本必须保留 |
| `offline_v4_scene_excel_20260705_001` | 导入中间表 | `source_sheet_name`, `school_name`, `player1_name`, `player2_name`, `venue_name`, `schedule_name` | 合理，原始导入文本必须保留 |
| `offline_v4_scene_player_20260705_001` | 现场对象 | `source_sheet_name`, `excel_school_name`, `player_name`, `venue_name`, `schedule_name` | 合理，原始导入文本必须保留 |
| `offline_v4_target_match_20260705_001` | 现场对象 | `source_sheet_name`, `excel_school_name`, `player_name`, `schedule_name`, `team_name`, `user_name`, `phone`, `email`, `school_name`, `competition_role_name`, `competition_track_name`, `second_level_name`, `leader_teacher_id`, `leader_teacher`, `guide_teacher` | 合理，原始导入文本必须保留 |
| `review_activity` | 评审任务 | `activity_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_activity_user_role` | 评审任务 | `role_type` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_audit_log` | 评审任务 | `operator_name` | 合理，日志应保存操作时快照 |
| `review_criteria` | 评审评分 | `criteria_name`, `score_type`, `min_score`, `max_score`, `weight` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_group_specialist_relation` | 评审任务 | `group_rela_id`, `group_id` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_object` | 评审任务 | `object_name`, `summary`, `org_name`, `contact_name`, `contact_phone`, `contact_email` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_object_certificate_ref` | 评审材料 | `member_name`, `member_role` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_object_material` | 评审材料 | `material_name`, `file_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_object_member` | 评审材料 | `member_name`, `member_role`, `phone`, `email`, `org_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_object_submit_log` | 评审材料 | `operator_name` | 合理，日志应保存操作时快照 |
| `review_panel` | 评审任务 | `panel_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_panel_member` | 评审任务 | `member_role` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_processed_relation` | 文件 | `old_file_name`, `new_file_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_record` | 评审评分 | `total_score`, `comment_text` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_result` | 评审评分 | `calculated_score` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_round` | 评审任务 | `round_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_rule` | 评审评分 | `rule_name`, `score_mode`, `total_score` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_score_detail` | 评审评分 | `criteria_name`, `score_type`, `score_value`, `text_value`, `weight` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_session` | 评审任务 | `session_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_specialist_group_info` | 评审任务 | `group_id`, `group_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_task_allot_group` | 评审任务 | `review_group_id`, `allot_group_name` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_task_allot_group_relation` | 评审任务 | `review_group_id` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `review_task_info` | 评审任务 | `review_name`, `review_group_id` | 合理，评审项/材料/评分需保留提交或评分时快照 |
| `team_manager_info` | 报名与团队 | `team_name`, `captain_name`, `competition_series_name`, `competition_track_name`, `second_level_name`, `leader_teacher` | 建议补注释区分事实源/快照 |
| `team_member_rela` | 报名与团队 | `team_role`, `user_name`, `instructor_phone`, `instructor_email` | 建议补注释区分事实源/快照 |
| `user_certificate` | 现场证件 | `cert_name`, `user_name`, `awards_name`, `guide_teacher` | 建议补注释区分事实源/快照 |
| `user_certificate_history` | 现场证件 | `cert_name`, `competition_name`, `competition_track_name`, `second_level_name`, `awards_name`, `awards_name_desc`, `user_name`, `guide_teacher`, `school`, `school_name`, `phone` | 建议补注释区分事实源/快照 |
| `user_certificate_origin` | 现场证件 | `cert_name`, `competition_name`, `competition_track_name`, `second_level_name`, `awards_name`, `awards_name_desc`, `user_name`, `guide_teacher`, `school`, `school_name`, `phone` | 建议补注释区分事实源/快照 |
| `user_grade_info` | 报名与团队 | `awards_name`, `score`, `competition_track_name`, `group_classify` | 建议补注释区分事实源/快照 |

## 合理快照

- 证件中的姓名、学校、组别、角色：发证后主数据变化不应改写已发证件。
- 预约中的工位数、共享占用、组别：预约时资源能力和适用范围可能后续变化。
- 操作日志中的操作人、对象：审计必须还原操作当时的上下文。
- 评审评分中的评分项名称、权重、选项文本：评审规则后续调整不应影响历史评分。
- 导入中间表原始文本：用于追溯清洗规则和人工核对。

## 命名建议

- 新增快照字段统一使用 `_snapshot` 后缀；历史字段至少补充 column comment。
- 容易误判为主数据的字段：`school_name`、`group_name`、`role_name`、`team_name`、`criteria_name`、`object_name`、`resource_name`。
