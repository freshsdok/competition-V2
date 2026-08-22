# SQL 与 Mapper 审计

## 范围

- Mapper XML 文件数：161
- SQL/migration 文件数：23
- 静态识别表名数量：181

## 高频表引用 Top 80

| 表 | 引用次数 |
| --- | --- |
| id | 281 |
| sys_menu | 205 |
| competition_apply_info | 104 |
| competition_series_info | 70 |
| auth_info | 68 |
| sys_user | 63 |
| competition_main_info | 52 |
| competition_grade_info | 37 |
| competition_scene_credential | 34 |
| review_record | 34 |
| team_manager_info | 33 |
| sys_org | 33 |
| competition_track_config | 31 |
| competition_promoted_apply_info | 30 |
| information_schema | 29 |
| sys_role_menu | 26 |
| competition_track_info | 26 |
| nationwide_college_info | 25 |
| sys_role | 24 |
| review_task_specialist_relation | 23 |
| identity_info | 22 |
| competition_scene_resource_slot | 19 |
| order_info | 19 |
| cert_config_info | 19 |
| sys_user_role | 19 |
| award_details | 17 |
| competition_stage_config | 17 |
| team_member_rela | 17 |
| competition_scene_schedule | 16 |
| competition_scene_schedule_target | 16 |
| cert_exchange_rule_detail | 15 |
| competition_config | 15 |
| review_task_allot_group_relation | 15 |
| course_chapter_info | 14 |
| file_upload_manager | 14 |
| review_task_info | 14 |
| candidate_cert_info | 13 |
| sys_dict_data | 12 |
| content_column | 12 |
| course_chapter_video | 12 |
| file_task | 12 |
| sys_user_group | 12 |
| order_goods_relation | 12 |
| review_processed_relation | 12 |
| sys_audit_main_config | 12 |
| competition_scene_schedule_resource | 11 |
| award_publicity | 11 |
| competition_enterprise_rela | 11 |
| component_data_source_rela | 11 |
| course_info | 11 |
| file_task_config | 11 |
| notification_receiver | 11 |
| sys_audit_task | 11 |
| sys_audit_config | 11 |
| sponsoring_enterprise_info | 10 |
| competition_promoted_info | 10 |
| competition_works | 10 |
| user_certificate | 10 |
| wx_qc_code_record | 10 |
| course_classify_info | 9 |
| review_group_specialist_relation | 9 |
| sys_audit_log | 9 |
| wx_qc_code_config | 9 |
| cert_org_info | 8 |
| competition_cert_exchange_apply | 8 |
| competition_scene_resource | 8 |
| operation_config | 8 |
| user_certificate_origin | 8 |
| content_detail | 8 |
| news_info | 8 |
| page_manager_info | 8 |
| course_recommend_rela | 8 |
| gen_table_column | 8 |
| gen_table | 8 |
| review_task_allot_group | 8 |
| suggestion_feedback_info | 8 |
| sys_audit_task_subinfo | 8 |
| sys_role_org | 8 |
| competition_scene_resource_reservation | 7 |
| cert_player_info | 7 |

## 唯一键证据 Top 60

| 位置 | 定义 |
| --- | --- |
| db/competition_scene_credential_resource_merged_20260705.sql:114 | UNIQUE KEY `uk_scene_target_key` (`schedule_id`, `target_key`), |
| db/competition_scene_credential_resource_merged_20260705.sql:207 | UNIQUE KEY `uk_scene_credential_no` (`credential_no`), |
| db/competition_scene_credential_resource_merged_20260705.sql:208 | UNIQUE KEY `uk_scene_credential_token` (`credential_token`), |
| db/competition_scene_credential_resource_merged_20260705.sql:318 | UNIQUE KEY `uk_scene_resource_code` (`resource_code`, `deleted`), |
| db/competition_scene_credential_resource_merged_20260705.sql:411 | UNIQUE KEY `uk_scene_reservation_idempotency` (`idempotency_key`), |
| db/competition_scene_credential_resource_merged_20260705.sql:444 | -- 2. competition_scene_credential.uk_scene_credential_no 已存在，无需重复 ADD UNIQUE KEY。 |
| db/competition_scene_credential_resource_merged_20260705.sql:565 | -- This migration only adds a nullable key column and unique index. It does not migrate or clean legacy test data. |
| db/competition_scene_credential_resource_merged_20260705.sql:574 | ADD UNIQUE KEY `uk_scene_credential_active_core_key` (`active_core_credential_key`); |
| db/competition_scene_credential_resource_merged_20260705.sql:613 | UNIQUE KEY `uk_grant_active_key` (`active_grant_key`), |
| db/migration/20260629_competition_scene_credential.sql:95 | UNIQUE KEY `uk_scene_target_key` (`schedule_id`, `target_key`), |
| db/migration/20260629_competition_scene_credential.sql:188 | UNIQUE KEY `uk_scene_credential_no` (`credential_no`), |
| db/migration/20260629_competition_scene_credential.sql:189 | UNIQUE KEY `uk_scene_credential_token` (`credential_token`), |
| db/migration/20260701_competition_scene_resource_p1_001.sql:34 | UNIQUE KEY `uk_scene_resource_code` (`resource_code`, `deleted`), |
| db/migration/20260701_competition_scene_resource_p1_001.sql:127 | UNIQUE KEY `uk_scene_reservation_idempotency` (`idempotency_key`), |
| db/migration/20260701_competition_scene_target_credential_type.sql:23 | -- 2. competition_scene_credential.uk_scene_credential_no 已存在，无需重复 ADD UNIQUE KEY。 |
| db/migration/20260703_review_module_phase1.sql:30 | UNIQUE KEY `uk_review_activity_code` (`activity_code`), |
| db/migration/20260703_review_module_phase1.sql:136 | UNIQUE KEY `uk_review_object_code` (`activity_id`, `object_code`), |
| db/migration/20260703_review_module_phase1.sql:378 | UNIQUE KEY `uk_review_assignment_user` (`activity_id`, `round_id`, `object_id`, `reviewer_user_id`), |
| db/migration/20260703_review_module_phase1.sql:535 | UNIQUE KEY `uk_review_result_object` (`activity_id`, `round_id`, `object_id`), |
| db/migration/20260705_competition_scene_credential_active_core_key_p2.sql:3 | -- This migration only adds a nullable key column and unique index. It does not migrate or clean legacy test data. |
| db/migration/20260705_competition_scene_credential_active_core_key_p2.sql:13 | ADD UNIQUE KEY `uk_scene_credential_active_core_key` (`active_core_credential_key`); |
| db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql:33 | UNIQUE KEY `uk_grant_active_key` (`active_grant_key`), |
| db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql:202 | 'UNIQUE KEY `uk_scene_resource_active_reservation_key` (`active_reservation_key`)' |
| db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql:234 | 'UNIQUE KEY `uk_scene_resource_idempotency_key` (`idempotency_key`)' |
| db/migration/20260710_competition_scene_notice.sql:46 | UNIQUE KEY `uk_scene_notice_schedule` (`notice_id`, `schedule_id`), |

## 动态 SQL 与模糊查询

| 类型 | 位置 | 代码 |
| --- | --- | --- |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionApplyInfoMapper.xml:233 | <!--        ${params.dataScope}--> |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionApplyInfoMapper.xml:961 | <!--        ${params.dataScope}--> |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionMainInfoMapper.xml:154 | ${params.dataScope} |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionTrackInfoMapper.xml:154 | ${params.dataScope} |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionWorksMapper.xml:79 | ${params.dataScope} |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/SponsoringEnterpriseInfoMapper.xml:81 | ${params.dataScope} |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/TeamManagerInfoMapper.xml:383 | <!--        ${params.dataScope}--> |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/TeamManagerInfoMapper.xml:453 | <!--        ${params.dataScope}--> |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/TeamManagerInfoMapper.xml:521 | <!--        ${params.dataScope}--> |
| ${} | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/TeamManagerInfoMapper.xml:589 | <!--        ${params.dataScope}--> |
| ${} | old-code/teaching-modules/teaching-flowable/src/main/resources/mapper/flowable/WfFormMapper.xml:24 | ${ew.getCustomSqlSegment} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/IdentityInfoMapper.xml:74 | ${params.dataScope} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/MessageTemplateSourceMapper.xml:99 | ${targetColumn} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/MessageTemplateSourceMapper.xml:104 | ${targetTable} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/MessageTemplateSourceMapper.xml:107 | and ${targetConditionColumn} = #{targetConditionName} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/MessageTemplateSourceMapper.xml:110 | and ${conditionColumn2} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/SysAuditTaskMapper.xml:201 | select * from ${businessTable} where id = #{businessId} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/SysAuditTaskMapper.xml:205 | update ${businessTable} set check_status = #{checkStatus} where id = #{businessId} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/SysOrgMapper.xml:52 | ${params.dataScope} |
| ${} | old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/SysRoleMapper.xml:67 | ${params.dataScope} |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:53 | <if test="awardsName != null  and awardsName != ''"> and awards_name like concat('%', #{awardsName}, '%')</if> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:82 | <!--        <if test="awardsName != null  and awardsName != ''"> and ad.awards_name like concat('%', #{awardsName}, '%')</if>--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:84 | <!--            and ad.team_name like concat('%', #{teamName}, '%')--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:87 | <!--            and ad.school_name like concat('%', #{schoolName}, '%')--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:91 | <!--            where user_name like concat('%', #{userName}, '%') and del_flag = '0' and pay_status = 'paid'--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:96 | <!--            where guide_teacher like concat('%', #{guiderTeacherName}, '%') and del_flag = '0' and pay_status = 'paid')--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:99 | <!--            and (cai.competition_track_name like concat('%', #{competitionTrackName}, '%')--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:100 | <!--            or cai.second_level_name like concat('%', #{competitionTrackName}, '%')--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:101 | <!--            or cai.competition_name like concat('%', #{competitionTrackName}, '%'))--> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:139 | <if test="awardsName != null  and awardsName != ''"> and ad.awards_name like concat('%', #{awardsName}, '%')</if> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:141 | and ad.team_name like concat('%', #{teamName}, '%') |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:144 | and cai.school in (select id from nationwide_college_info where school_name like concat('%', #{schoolName}, '%')) |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:148 | where user_name like concat('%', #{userName}, '%') and del_flag = '0' and pay_status = 'paid' |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:153 | where guide_teacher like concat('%', #{guiderTeacherName}, '%') and del_flag = '0' and pay_status = 'paid') |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:156 | and (cai.competition_track_name like concat('%', #{competitionTrackName}, '%') |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:157 | or cai.second_level_name like concat('%', #{competitionTrackName}, '%') |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:158 | or cai.competition_name like concat('%', #{competitionTrackName}, '%')) |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardPublicityMapper.xml:39 | <if test="competitionName != null  and competitionName != ''"> and competition_name like concat('%', #{competitionName}, '%')</if> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardPublicityMapper.xml:42 | <if test="createBy != null and createBy != ''"> and create_by like concat('%',#{createBy},'%') </if> |
| LIKE CONCAT | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CandidateCertInfoMapper.xml:85 | and cci.user_name like concat('%', #{userName}, '%') |

## 建议
- 建立 Mapper 风险清单：`${}`、列表分页、跨表 join、状态字段过滤、deleted/del_flag 过滤。
- 对预约、grant、operation_state、订单回调建立唯一键和条件更新说明。
- 高频 `like concat` 接口做慢 SQL 采样和索引评估。
