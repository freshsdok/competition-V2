# Canvas LMS 按业务域反向功能需求

## 账户与租户管理

- 支持根账户、子账户、账户设置、品牌配置、服务条款、管理员和角色权限管理。 `STATIC_INFERENCE`

- 支持认证提供商、登录方式、OAuth/developer key 和安全策略配置。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| AccountCalendarsApiController | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:104` |
| RoleOverridesController | `canvas-lms-master/app/controllers/role_overrides_controller.rb:173` |
| PseudonymsController | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:23` |
| TermsApiController | `canvas-lms-master/app/controllers/terms_api_controller.rb:95` |
| CspSettingsController | `canvas-lms-master/app/controllers/csp_settings_controller.rb:27` |
| TermsController | `canvas-lms-master/app/controllers/terms_controller.rb:22` |
| BrandConfigsApiController | `canvas-lms-master/app/controllers/brand_configs_api_controller.rb:21` |
| AccountNotificationsController | `canvas-lms-master/app/controllers/account_notifications_controller.rb:83` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| OAuthClientConfig | `canvas-lms-master/app/models/oauth_client_config.rb:25` | 311 |
| AccountNotification | `canvas-lms-master/app/models/account_notification.rb:20` | 445 |
| Account | `canvas-lms-master/app/models/account.rb:21` | 3094 |
| DeveloperKeyAccountBinding | `canvas-lms-master/app/models/developer_key_account_binding.rb:21` | 194 |
| ImpossibleCredentialsError | `canvas-lms-master/app/models/pseudonym.rb:20` | 891 |
| UserAccountAssociation | `canvas-lms-master/app/models/user_account_association.rb:21` | 53 |
| SharedBrandConfig | `canvas-lms-master/app/models/shared_brand_config.rb:20` | 30 |
| PseudonymSession | `canvas-lms-master/app/models/pseudonym_session.rb:21` | 190 |

**数据表证据**
| 表 | migration |
| --- | --- |
| accounts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:107` |
| account_notifications | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:447` |
| account_notification_roles | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:466` |
| account_users | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:520` |
| auditor_account_user_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1057` |
| auditor_pseudonym_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1140` |
| authentication_providers | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1154` |
| brand_configs | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1245` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/account_admin_tools | 66 |
| ui/features/account_calendar_settings | 22 |
| ui/features/account_course_user_search | 53 |
| ui/features/account_grading_settings | 32 |
| ui/features/account_grading_standards | 25 |
| ui/features/account_manage | 6 |
| ui/features/account_notification_settings | 9 |
| ui/features/account_search | 2 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 用户、身份与沟通

- 支持用户资料、登录身份 pseudonym、通信渠道、通知偏好、站内消息/会话。 `STATIC_INFERENCE`

- 支持观察者/被观察学生关系和用户搜索。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| MessagesController | `canvas-lms-master/app/controllers/messages_controller.rb:23` |
| LmgbUserDetailsController | `canvas-lms-master/app/controllers/lmgb_user_details_controller.rb:21` |
| CommMessagesApiController | `canvas-lms-master/app/controllers/comm_messages_api_controller.rb:101` |
| ConversationsController | `canvas-lms-master/app/controllers/conversations_controller.rb:153` |
| DiscussionTopicUsersController | `canvas-lms-master/app/controllers/discussion_topic_users_controller.rb:19` |
| ObserverAlertThresholdsApiController | `canvas-lms-master/app/controllers/observer_alert_thresholds_api_controller.rb:21` |
| AiConversationsController | `canvas-lms-master/app/controllers/ai_conversations_controller.rb:23` |
| UsersController | `canvas-lms-master/app/controllers/users_controller.rb:82` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:21` | 58 |
| UserPastLtiId | `canvas-lms-master/app/models/user_past_lti_id.rb:45` | 76 |
| UserProfile | `canvas-lms-master/app/models/user_profile.rb:21` | 216 |
| UserMergeData | `canvas-lms-master/app/models/user_merge_data.rb:20` | 64 |
| UserMergeDataRecord | `canvas-lms-master/app/models/user_merge_data_record.rb:20` | 35 |
| CourseProfile | `canvas-lms-master/app/models/course_profile.rb:20` | 22 |
| UserLmgbOutcomeOrderings | `canvas-lms-master/app/models/user_lmgb_outcome_orderings.rb:21` | 52 |
| ObserverEnrollment | `canvas-lms-master/app/models/observer_enrollment.rb:21` | 91 |

**数据表证据**
| 表 | migration |
| --- | --- |
| ai_conversations | `canvas-lms-master/db/migrate/20251014000001_create_ai_conversations.rb:24` |
| canvas_career_user_experiences | `canvas-lms-master/db/migrate/20260331080000_create_canvas_career_user_experiences.rb:24` |
| users | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:304` |
| asset_user_accesses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:687` |
| communication_channels | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1383` |
| conversations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1720` |
| conversation_batches | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1732` |
| conversation_messages | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1747` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| packages/babel-preset-pretranslated-translations-package-format-message | 2 |
| packages/filter-console-messages | 2 |
| packages/format-message-estree-util | 3 |
| ui/features/ai_experiences_ai_conversations | 13 |
| ui/features/context_prior_users | 1 |
| ui/features/context_roster_user | 5 |
| ui/features/context_roster_user_services | 2 |
| ui/features/conversations | 2 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 课程、班级与选课

- 支持课程、班级 section、学期 term、选课 enrollment、课程设置、课程导航和自注册/退课。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| SelfEnrollmentsController | `canvas-lms-master/app/controllers/self_enrollments_controller.rb:21` |
| CourseNicknamesController | `canvas-lms-master/app/controllers/course_nicknames_controller.rb:54` |
| GradingPeriodSetsController | `canvas-lms-master/app/controllers/grading_period_sets_controller.rb:46` |
| TabsController | `canvas-lms-master/app/controllers/tabs_controller.rb:61` |
| SectionsController | `canvas-lms-master/app/controllers/sections_controller.rb:95` |
| CoursesController | `canvas-lms-master/app/controllers/courses_controller.rb:350` |
| CourseTagConversionController | `canvas-lms-master/app/controllers/course_tag_conversion_controller.rb:20` |
| CourseReportsController | `canvas-lms-master/app/controllers/course_reports_controller.rb:84` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| CourseSection | `canvas-lms-master/app/models/course_section.rb:21` | 461 |
| EnrollmentTerm | `canvas-lms-master/app/models/enrollment_term.rb:21` | 242 |
| TeacherEnrollment | `canvas-lms-master/app/models/teacher_enrollment.rb:21` | 25 |
| GradingPeriod | `canvas-lms-master/app/models/grading_period.rb:21` | 298 |
| StudentViewEnrollment | `canvas-lms-master/app/models/student_view_enrollment.rb:21` | 25 |
| CourseProgress | `canvas-lms-master/app/models/course_progress.rb:21` | 273 |
| DesignerEnrollment | `canvas-lms-master/app/models/designer_enrollment.rb:21` | 25 |
| Enrollment | `canvas-lms-master/app/models/enrollment.rb:21` | 1725 |

**数据表证据**
| 表 | migration |
| --- | --- |
| accessibility_course_statistics | `canvas-lms-master/db/migrate/20251218132705_create_accessibility_course_statistics.rb:24` |
| courses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:176` |
| course_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:254` |
| abstract_courses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:363` |
| auditor_course_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1081` |
| course_paces | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1826` |
| course_pace_module_items | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1848` |
| course_score_statistics | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1859` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/acceptable_use_policy | 9 |
| ui/features/all_courses | 4 |
| ui/features/blueprint_course_child | 12 |
| ui/features/blueprint_course_master | 36 |
| ui/features/copy_course | 29 |
| ui/features/course | 3 |
| ui/features/course_grading_standards | 10 |
| ui/features/course_link_validator | 6 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 作业、提交与评分

- 支持作业、作业组、提交、匿名/临时评分、Rubric、Gradebook、SpeedGrader、补交/迟交策略。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| AssignmentOverridesController | `canvas-lms-master/app/controllers/assignment_overrides_controller.rb:113` |
| GradebookSettingsController | `canvas-lms-master/app/controllers/gradebook_settings_controller.rb:21` |
| PeerReviewsApiController | `canvas-lms-master/app/controllers/peer_reviews_api_controller.rb:76` |
| AssignmentsController | `canvas-lms-master/app/controllers/assignments_controller.rb:22` |
| AnonymousProvisionalGradesController | `canvas-lms-master/app/controllers/anonymous_provisional_grades_controller.rb:24` |
| AssignmentGroupsController | `canvas-lms-master/app/controllers/assignment_groups_controller.rb:96` |
| GradebookCsvsController | `canvas-lms-master/app/controllers/gradebook_csvs_controller.rb:21` |
| GradeChangeAuditApiController | `canvas-lms-master/app/controllers/grade_change_audit_api_controller.rb:125` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:21` | 349 |
| RubricAssessmentImport | `canvas-lms-master/app/models/rubric_assessment_import.rb:21` | 158 |
| RubricCriterion | `canvas-lms-master/app/models/rubric_criterion.rb:20` | 35 |
| GradebookUpload | `canvas-lms-master/app/models/gradebook_upload.rb:21` | 49 |
| CustomGradebookColumnDatum | `canvas-lms-master/app/models/custom_gradebook_column_datum.rb:21` | 73 |
| is | `canvas-lms-master/app/models/rubric_association.rb:24` | 461 |
| LatePolicy | `canvas-lms-master/app/models/late_policy.rb:21` | 87 |
| AssignmentOverrideStudent | `canvas-lms-master/app/models/assignment_override_student.rb:21` | 166 |

**数据表证据**
| 表 | migration |
| --- | --- |
| anonymous_or_moderation_events | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:582` |
| assignment_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:721` |
| assignments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:742` |
| assignment_configuration_tool_lookups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:844` |
| assignment_overrides | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:858` |
| assignment_override_students | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:920` |
| auditor_grade_change_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1108` |
| auto_grade_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1186` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/assignment_edit | 76 |
| ui/features/assignment_grade_summary | 54 |
| ui/features/assignment_index | 103 |
| ui/features/assignment_show | 4 |
| ui/features/assignments_peer_reviews | 6 |
| ui/features/assignments_peer_reviews_student | 34 |
| ui/features/assignments_show_student | 157 |
| ui/features/assignments_show_teacher | 17 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 测验与题库

- 支持 Classic/New Quizzes、题库、题目、测验提交、统计、发布/取消发布和 LTI 新测验入口。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| AssessmentQuestionBanksController | `canvas-lms-master/app/controllers/assessment_question_banks_controller.rb:227` |
| ItemBanksController | `canvas-lms-master/app/controllers/item_banks_controller.rb:21` |
| QuestionBanksController | `canvas-lms-master/app/controllers/question_banks_controller.rb:21` |
| NewQuizzesController | `canvas-lms-master/app/controllers/new_quizzes_controller.rb:26` |
| AssessmentQuestionsController | `canvas-lms-master/app/controllers/assessment_questions_controller.rb:21` |
| QuizzesNext::QuizzesApiController | `canvas-lms-master/app/controllers/quizzes_next/quizzes_api_controller.rb:20` |
| Quizzes::QuizSubmissionsController | `canvas-lms-master/app/controllers/quizzes/quiz_submissions_controller.rb:21` |
| QuizSubmissionUsersController | `canvas-lms-master/app/controllers/quizzes/quiz_submission_users_controller.rb:85` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| AssessmentQuestionBank | `canvas-lms-master/app/models/assessment_question_bank.rb:21` | 172 |
| QuizMigrationAlert | `canvas-lms-master/app/models/quiz_migration_alert.rb:21` | 32 |
| AssessmentQuestion | `canvas-lms-master/app/models/assessment_question.rb:21` | 431 |
| AssessmentQuestionBankUser | `canvas-lms-master/app/models/assessment_question_bank_user.rb:21` | 26 |
| ExportService | `canvas-lms-master/app/models/quizzes_next/export_service.rb:20` | 107 |
| Service | `canvas-lms-master/app/models/quizzes_next/service.rb:20` | 39 |
| QuizImporter | `canvas-lms-master/app/models/importers/quiz_importer.rb:22` | 451 |
| AssessmentQuestionImporter | `canvas-lms-master/app/models/importers/assessment_question_importer.rb:21` | 233 |

**数据表证据**
| 表 | migration |
| --- | --- |
| assessment_question_banks | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:633` |
| assessment_question_bank_users | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:647` |
| assessment_questions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:653` |
| quizzes | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4294` |
| quiz_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4344` |
| quiz_migration_alerts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4356` |
| quiz_questions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4363` |
| quiz_statistics | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4408` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/moderate_quiz | 6 |
| ui/features/new_quizzes | 5 |
| ui/features/question_bank | 8 |
| ui/features/question_banks | 3 |
| ui/features/quiz_history | 5 |
| ui/features/quiz_log_auditing | 80 |
| ui/features/quiz_migration_alerts | 2 |
| ui/features/quiz_show | 3 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 讨论、公告与协作

- 支持讨论主题、公告、小组、协作、会议和 ePortfolio。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| EportfolioCategoriesController | `canvas-lms-master/app/controllers/eportfolio_categories_controller.rb:20` |
| EportfoliosApiController | `canvas-lms-master/app/controllers/eportfolios_api_controller.rb:120` |
| EportfolioEntriesController | `canvas-lms-master/app/controllers/eportfolio_entries_controller.rb:21` |
| CollaborationsController | `canvas-lms-master/app/controllers/collaborations_controller.rb:126` |
| AnnouncementsController | `canvas-lms-master/app/controllers/announcements_controller.rb:21` |
| AnnouncementsApiController | `canvas-lms-master/app/controllers/announcements_api_controller.rb:26` |
| GroupsController | `canvas-lms-master/app/controllers/groups_controller.rb:150` |
| DiscussionTopicsApiController | `canvas-lms-master/app/controllers/discussion_topics_api_controller.rb:24` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| DiscussionTopicSummary | `canvas-lms-master/app/models/discussion_topic_summary.rb:19` | 36 |
| Announcement | `canvas-lms-master/app/models/announcement.rb:21` | 317 |
| EtherpadCollaboration | `canvas-lms-master/app/models/etherpad_collaboration.rb:21` | 45 |
| DiscussionTopicInsight | `canvas-lms-master/app/models/discussion_topic_insight.rb:19` | 234 |
| DiscussionEntryVersion | `canvas-lms-master/app/models/discussion_entry_version.rb:19` | 37 |
| WebConferenceParticipant | `canvas-lms-master/app/models/web_conference_participant.rb:21` | 30 |
| GroupMembership | `canvas-lms-master/app/models/group_membership.rb:21` | 467 |
| Collaboration | `canvas-lms-master/app/models/collaboration.rb:21` | 423 |

**数据表证据**
| 表 | migration |
| --- | --- |
| discussion_entry_drafts | `canvas-lms-master/db/migrate/20251111085656_remove_discussion_entry_drafts.rb:27` |
| appointment_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:599` |
| appointment_group_contexts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:617` |
| appointment_group_sub_contexts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:625` |
| collaborations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1338` |
| discussion_entries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2048` |
| discussion_entry_drafts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2081` |
| discussion_entry_participants | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2105` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/announcements | 27 |
| ui/features/announcements_on_home_page | 2 |
| ui/features/calendar_appointment_group_edit | 12 |
| ui/features/collaborations | 17 |
| ui/features/conferences | 23 |
| ui/features/context_roster_groups | 1 |
| ui/features/current_groups | 2 |
| ui/features/discussion_topic_edit_v2 | 102 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 内容、文件与模块

- 支持课程文件、文件夹、Wiki 页面、课程模块、日历、计划器、内容导入导出和 syllabus。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| PlannerOverridesController | `canvas-lms-master/app/controllers/planner_overrides_controller.rb:90` |
| ContextModulesController | `canvas-lms-master/app/controllers/context_modules_controller.rb:21` |
| SyllabusApiController | `canvas-lms-master/app/controllers/syllabus_api_controller.rb:20` |
| FilePreviewsController | `canvas-lms-master/app/controllers/file_previews_controller.rb:20` |
| WikiPagesController | `canvas-lms-master/app/controllers/wiki_pages_controller.rb:20` |
| CalendarsController | `canvas-lms-master/app/controllers/calendars_controller.rb:21` |
| ContentExportsApiController | `canvas-lms-master/app/controllers/content_exports_api_controller.rb:82` |
| ContextModulesApiController | `canvas-lms-master/app/controllers/context_modules_api_controller.rb:126` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| Wiki | `canvas-lms-master/app/models/wiki.rb:21` | 245 |
| ContextModule | `canvas-lms-master/app/models/context_module.rb:21` | 1167 |
| PlannerNote | `canvas-lms-master/app/models/planner_note.rb:21` | 40 |
| WikiPageEmbedding | `canvas-lms-master/app/models/wiki_page_embedding.rb:19` | 25 |
| ContextModuleProgression | `canvas-lms-master/app/models/context_module_progression.rb:21` | 546 |
| AttachmentUploadStatus | `canvas-lms-master/app/models/attachment_upload_status.rb:19` | 62 |
| ContentMigration | `canvas-lms-master/app/models/content_migration.rb:21` | 1686 |
| CalendarEvent | `canvas-lms-master/app/models/calendar_event.rb:25` | 905 |

**数据表证据**
| 表 | migration |
| --- | --- |
| ai_experience_context_files | `canvas-lms-master/db/migrate/20260130000001_create_ai_experience_context_files.rb:24` |
| attachments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:953` |
| attachment_associations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1039` |
| attachment_upload_statuses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1051` |
| calendar_events | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1260` |
| content_exports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1508` |
| content_migrations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1523` |
| context_modules | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1681` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/calendar | 82 |
| ui/features/content_exports | 3 |
| ui/features/content_migrations | 125 |
| ui/features/context_module_progressions | 9 |
| ui/features/context_modules | 4 |
| ui/features/context_modules_publish_icon | 2 |
| ui/features/context_modules_publish_menu | 2 |
| ui/features/context_modules_v2 | 158 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 学习成果与能力

- 支持 Learning Outcomes、Outcome Groups、结果、能力/熟练度和学习掌握数据。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| OutcomesController | `canvas-lms-master/app/controllers/outcomes_controller.rb:21` |
| OutcomeResultsController | `canvas-lms-master/app/controllers/outcome_results_controller.rb:189` |
| OutcomeProficiencyApiController | `canvas-lms-master/app/controllers/outcome_proficiency_api_controller.rb:66` |
| OutcomeImportsApiController | `canvas-lms-master/app/controllers/outcome_imports_api_controller.rb:106` |
| OutcomesAcademicBenchmarkImportApiController | `canvas-lms-master/app/controllers/outcomes_academic_benchmark_import_api_controller.rb:21` |
| OutcomesApiController | `canvas-lms-master/app/controllers/outcomes_api_controller.rb:165` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:21` | 277 |
| OutcomeProficiencyRating | `canvas-lms-master/app/models/outcome_proficiency_rating.rb:21` | 68 |
| OutcomeImportError | `canvas-lms-master/app/models/outcome_import_error.rb:21` | 30 |
| OutcomeProficiency | `canvas-lms-master/app/models/outcome_proficiency.rb:21` | 201 |
| OutcomeCalculationMethod | `canvas-lms-master/app/models/outcome_calculation_method.rb:21` | 151 |
| LearningOutcome | `canvas-lms-master/app/models/learning_outcome.rb:21` | 741 |
| LearningOutcomeGroup | `canvas-lms-master/app/models/learning_outcome_group.rb:21` | 379 |
| LearningOutcomeQuestionResult | `canvas-lms-master/app/models/learning_outcome_question_result.rb:21` | 89 |

**数据表证据**
| 表 | migration |
| --- | --- |
| learning_outcomes | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2817` |
| learning_outcome_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2843` |
| learning_outcome_question_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2871` |
| learning_outcome_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2891` |
| outcome_calculation_methods | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3908` |
| outcome_friendly_descriptions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3920` |
| outcome_imports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3931` |
| outcome_import_errors | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3946` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/choose_mastery_path | 19 |
| ui/features/learning_mastery | 12 |
| ui/features/learning_mastery_v2 | 86 |
| ui/features/learning_outcomes | 5 |
| ui/features/outcome_alignment_v2 | 4 |
| ui/features/outcome_alignments | 2 |
| ui/features/outcome_management | 163 |
| ui/features/user_outcome_results | 2 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## LTI、外部工具与集成

- 支持 LTI 工具配置、启动、部署、资源链接、Asset Processor、作业原创性/查重和外部工具。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| ExternalToolsController | `canvas-lms-master/app/controllers/external_tools_controller.rb:607` |
| LtiApiController | `canvas-lms-master/app/controllers/lti_api_controller.rb:25` |
| Test::MockLtiController | `canvas-lms-master/app/controllers/test/mock_lti_controller.rb:23` |
| AssetProcessorController | `canvas-lms-master/app/controllers/support_helpers/asset_processor_controller.rb:21` |
| TurnitinController | `canvas-lms-master/app/controllers/support_helpers/turnitin_controller.rb:21` |
| PlagiarismPlatformController | `canvas-lms-master/app/controllers/support_helpers/plagiarism_platform_controller.rb:21` |
| ToolProxyController | `canvas-lms-master/app/controllers/lti/tool_proxy_controller.rb:21` |
| Lti::ResourceLinksController | `canvas-lms-master/app/controllers/lti/resource_links_controller.rb:150` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| ContextExternalTool | `canvas-lms-master/app/models/context_external_tool.rb:21` | 1492 |
| ContextExternalToolPlacement | `canvas-lms-master/app/models/context_external_tool_placement.rb:20` | 26 |
| LtiContextControlImporter | `canvas-lms-master/app/models/importers/lti_context_control_importer.rb:20` | 71 |
| ContextExternalToolImporter | `canvas-lms-master/app/models/importers/context_external_tool_importer.rb:21` | 332 |
| LtiResourceLinkImporter | `canvas-lms-master/app/models/importers/lti_resource_link_importer.rb:21` | 100 |
| MicrosoftSync::PartialSyncChange | `canvas-lms-master/app/models/microsoft_sync/partial_sync_change.rb:20` | 86 |
| Lti::AssetProcessor | `canvas-lms-master/app/models/lti/asset_processor.rb:20` | 116 |
| AnalyticsService | `canvas-lms-master/app/models/lti/analytics_service.rb:23` | 80 |

**数据表证据**
| 表 | migration |
| --- | --- |
| context_external_tools | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1640` |
| context_external_tool_placements | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1674` |
| lti_assets | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2976` |
| lti_asset_processors | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3013` |
| lti_asset_processor_eula_acceptances | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3035` |
| lti_asset_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3050` |
| lti_context_controls | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3076` |
| lti_import_histories | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3099` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/external_tool_redirect | 3 |
| ui/features/external_tools_show | 4 |
| ui/features/lti_registrations | 271 |
| ui/shared/lti | 57 |
| ui/shared/lti-apps | 54 |
| ui/shared/lti-asset-processor | 69 |
| ui/shared/multi-select | 3 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## SIS、导入导出与报表

- 支持 SIS 导入、CSV/ZIP 导入、批处理清理、账户报表、成绩簿导入导出和内容迁移。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| EpubExportsController | `canvas-lms-master/app/controllers/epub_exports_controller.rb:95` |
| AccountReportsController | `canvas-lms-master/app/controllers/account_reports_controller.rb:199` |
| MigrationIssuesController | `canvas-lms-master/app/controllers/migration_issues_controller.rb:95` |
| WebZipExportsController | `canvas-lms-master/app/controllers/web_zip_exports_controller.rb:94` |
| SisApiController | `canvas-lms-master/app/controllers/sis_api_controller.rb:306` |
| SisImportErrorsApiController | `canvas-lms-master/app/controllers/sis_import_errors_api_controller.rb:57` |
| ContentImportsController | `canvas-lms-master/app/controllers/content_imports_controller.rb:22` |
| DisablePostToSisApiController | `canvas-lms-master/app/controllers/disable_post_to_sis_api_controller.rb:24` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| ErrorReport | `canvas-lms-master/app/models/error_report.rb:21` | 285 |
| WebZipExport | `canvas-lms-master/app/models/web_zip_export.rb:20` | 67 |
| MigrationIssue | `canvas-lms-master/app/models/migration_issue.rb:20` | 44 |
| AccountReportRow | `canvas-lms-master/app/models/account_report_row.rb:21` | 24 |
| AccountReport | `canvas-lms-master/app/models/account_report.rb:21` | 197 |
| ParallelImporter | `canvas-lms-master/app/models/parallel_importer.rb:21` | 64 |
| AccountReportRunner | `canvas-lms-master/app/models/account_report_runner.rb:21` | 80 |
| OriginalityReport | `canvas-lms-master/app/models/originality_report.rb:20` | 189 |

**数据表证据**
| 表 | migration |
| --- | --- |
| sis_batches | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:273` |
| account_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:478` |
| account_report_runners | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:499` |
| account_report_rows | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:511` |
| epub_exports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2447` |
| error_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2454` |
| migration_issues | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3701` |
| originality_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3891` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/account_reports | 13 |
| ui/features/epub_exports | 14 |
| ui/features/sis_import | 9 |
| ui/features/study_assist | 3 |
| ui/features/teacher_activity_report | 3 |
| ui/features/webzip_export | 12 |
| ui/shared/account_reports | 6 |
| ui/shared/sis | 6 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 分析、审计与运维

- 支持 page view、审计事件、后台任务、报表统计、feature flag 和运维诊断。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| JobsController | `canvas-lms-master/app/controllers/jobs_controller.rb:20` |
| AuthenticationAuditApiController | `canvas-lms-master/app/controllers/authentication_audit_api_controller.rb:72` |
| JobsV2Controller | `canvas-lms-master/app/controllers/jobs_v2_controller.rb:20` |
| RateLimitingSettingsController | `canvas-lms-master/app/controllers/rate_limiting_settings_controller.rb:25` |
| AnalyticsHubController | `canvas-lms-master/app/controllers/analytics_hub_controller.rb:20` |
| DocviewerAuditEventsController | `canvas-lms-master/app/controllers/docviewer_audit_events_controller.rb:21` |
| ErrorsController | `canvas-lms-master/app/controllers/errors_controller.rb:66` |
| AuditorApiController | `canvas-lms-master/app/controllers/auditor_api_controller.rb:21` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| AuditEventService | `canvas-lms-master/app/models/audit_event_service.rb:20` | 59 |
| FeatureFlag | `canvas-lms-master/app/models/feature_flag.rb:21` | 159 |
| ServiceError | `canvas-lms-master/app/models/conditional_release/service.rb:22` | 254 |
| Auditors::Authentication | `canvas-lms-master/app/models/auditors/authentication.rb:21` | 125 |
| Record | `canvas-lms-master/app/models/auditors/record.rb:22` | 29 |
| Auditors::FeatureFlag | `canvas-lms-master/app/models/auditors/feature_flag.rb:21` | 126 |
| DelayedAlertSender | `canvas-lms-master/app/models/alerts/delayed_alert_sender.rb:21` | 104 |
| ReplyFrom | `canvas-lms-master/app/models/incoming_mail/errors.rb:22` | 40 |

**数据表证据**
| 表 | migration |
| --- | --- |
| delayed_jobs | `canvas-lms-master/db/migrate/20101216224513_create_delayed_jobs.rb:26` |
| failed_jobs | `canvas-lms-master/db/migrate/20101216224513_create_delayed_jobs.rb:249` |
| auditor_authentication_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1071` |
| auditor_feature_flag_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1094` |
| feature_flags | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2569` |
| plugin_settings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4083` |
| settings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4725` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/analytics_hub | 2 |
| ui/features/error_form | 2 |
| ui/features/job_stats | 8 |
| ui/features/jobs | 4 |
| ui/features/jobs_v2 | 27 |
| ui/features/rate_limiting_settings | 7 |
| ui/features/settings_sidebar | 2 |
| ui/shared/conditional-release-stats | 34 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 搜索、AI 与无障碍

- 支持搜索、Smart Search、AI/LLM 相关体验、无障碍问题统计和沉浸式阅读。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| AccessibilityIssuesController | `canvas-lms-master/app/controllers/accessibility_issues_controller.rb:20` |
| AccessibilityController | `canvas-lms-master/app/controllers/accessibility_controller.rb:20` |
| CareerExperienceController | `canvas-lms-master/app/controllers/career_experience_controller.rb:43` |
| ImmersiveReaderController | `canvas-lms-master/app/controllers/immersive_reader_controller.rb:23` |
| SmartSearchController | `canvas-lms-master/app/controllers/smart_search_controller.rb:63` |
| SearchController | `canvas-lms-master/app/controllers/search_controller.rb:23` |
| AiExperiencesController | `canvas-lms-master/app/controllers/ai_experiences_controller.rb:73` |
| CareerController | `canvas-lms-master/app/controllers/career_controller.rb:21` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| LLMConfig | `canvas-lms-master/app/models/llm_config.rb:20` | 69 |
| AccessibilityIssue | `canvas-lms-master/app/models/accessibility_issue.rb:19` | 55 |
| AiExperience | `canvas-lms-master/app/models/ai_experience.rb:20` | 269 |
| AccessibilityResourceScan | `canvas-lms-master/app/models/accessibility_resource_scan.rb:19` | 136 |
| LLMResponse | `canvas-lms-master/app/models/llm_response.rb:21` | 24 |
| generates | `canvas-lms-master/app/models/accessibility/issue.rb:21` | 145 |
| ContentLoader | `canvas-lms-master/app/models/accessibility/content_loader.rb:21` | 104 |
| Rule | `canvas-lms-master/app/models/accessibility/rule.rb:21` | 130 |

**数据表证据**
| 表 | migration |
| --- | --- |
| accessibility_issues | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:402` |
| accessibility_resource_scans | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:423` |
| ai_experiences | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:532` |
| llm_responses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2959` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/features/accessibility | 157 |
| ui/features/ai_experiences_edit | 12 |
| ui/features/ai_experiences_index | 10 |
| ui/features/ai_experiences_show | 8 |
| ui/features/canvas_career | 1 |
| ui/features/search | 24 |
| ui/shared/search-item-selector | 5 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## GraphQL API

- 提供 GraphQL 查询/变更层，用于前端或 API 聚合访问。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| GraphQLController | `canvas-lms-master/app/controllers/graphql_controller.rb:21` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| ui/shared/graphql | 13 |
| ui/shared/graphql-query-mock | 3 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。

## 其他

- 保留未明确归类的历史、测试或平台能力。 `STATIC_INFERENCE`

**Controller 证据**
| Controller | 位置 |
| --- | --- |
| HistoryController | `canvas-lms-master/app/controllers/history_controller.rb:85` |
| BlackoutDatesController | `canvas-lms-master/app/controllers/blackout_dates_controller.rb:62` |
| ApplicationController | `canvas-lms-master/app/controllers/application_controller.rb:24` |
| TokensController | `canvas-lms-master/app/controllers/tokens_controller.rb:41` |
| ReleaseNotesController | `canvas-lms-master/app/controllers/release_notes_controller.rb:20` |
| FavoritesController | `canvas-lms-master/app/controllers/favorites_controller.rb:46` |
| ExternalFeedsController | `canvas-lms-master/app/controllers/external_feeds_controller.rb:72` |
| GradingStandardsApiController | `canvas-lms-master/app/controllers/grading_standards_api_controller.rb:90` |

**Model 证据**
| Model | 位置 | 行数 |
| --- | --- | --- |
| Mailer | `canvas-lms-master/app/models/mailer.rb:23` | 91 |
| BlockEditor | `canvas-lms-master/app/models/block_editor.rb:21` | 51 |
| Collaborator | `canvas-lms-master/app/models/collaborator.rb:21` | 63 |
| ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:21` | 227 |
| MediaObject | `canvas-lms-master/app/models/media_object.rb:21` | 471 |
| AllocationRule | `canvas-lms-master/app/models/allocation_rule.rb:20` | 142 |
| AssessmentRequest | `canvas-lms-master/app/models/assessment_request.rb:21` | 217 |
| Alert | `canvas-lms-master/app/models/alert.rb:21` | 106 |

**数据表证据**
| 表 | migration |
| --- | --- |
| nav_menu_links | `canvas-lms-master/db/migrate/20260123151144_create_nav_menu_links.rb:24` |
| institutional_tags | `canvas-lms-master/db/migrate/20260312092459_create_institutional_tags.rb:24` |
| translation_feedback | `canvas-lms-master/db/migrate/20260326140109_create_translation_feedback.rb:24` |
| institutional_tag_associations | `canvas-lms-master/db/migrate/20260312100330_create_institutional_tag_associations.rb:24` |
| institutional_tag_categories | `canvas-lms-master/db/migrate/20260312091756_create_institutional_tag_categories.rb:24` |
| external_content_references | `canvas-lms-master/db/migrate/20260306122857_create_external_content_references.rb:24` |
| cloned_items | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:170` |
| access_tokens | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:378` |

**前端/包证据**
| 目录 | 文件数 |
| --- | --- |
| packages/bootstrap-dropdown | 3 |
| packages/bootstrap-select | 3 |
| packages/browserslist-config-canvas-lms | 2 |
| packages/canvas-media | 142 |
| packages/canvas-rce | 793 |
| packages/date-js | 6 |
| packages/date-js-alias | 2 |
| packages/defer-promise | 4 |

**重构注意**：先按角色、权限、数据模型和 API 入口建立边界，再改写流程。
