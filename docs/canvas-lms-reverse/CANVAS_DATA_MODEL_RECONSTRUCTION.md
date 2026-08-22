# Canvas LMS 数据模型反向重建

## Model 规模与热点

| 业务域 | Model 数 |
| --- | --- |
| 测验与题库 | 101 |
| 其他 | 75 |
| 用户、身份与沟通 | 72 |
| 作业、提交与评分 | 71 |
| 账户与租户管理 | 56 |
| 课程、班级与选课 | 49 |
| LTI、外部工具与集成 | 49 |
| 讨论、公告与协作 | 41 |
| 内容、文件与模块 | 41 |
| 搜索、AI 与无障碍 | 30 |
| SIS、导入导出与报表 | 27 |
| 学习成果与能力 | 16 |
| 分析、审计与运维 | 11 |

## 大模型 Top 80

| 业务域 | Model | 位置 | 行数 |
| --- | --- | --- | --- |
| 课程、班级与选课 | Course | `canvas-lms-master/app/models/course.rb:21` | 5057 |
| 作业、提交与评分 | AbstractAssignment | `canvas-lms-master/app/models/abstract_assignment.rb:23` | 4812 |
| 用户、身份与沟通 | User | `canvas-lms-master/app/models/user.rb:21` | 4124 |
| 作业、提交与评分 | Submission | `canvas-lms-master/app/models/submission.rb:23` | 3903 |
| 账户与租户管理 | Account | `canvas-lms-master/app/models/account.rb:21` | 3094 |
| 内容、文件与模块 | Attachment | `canvas-lms-master/app/models/attachment.rb:22` | 2829 |
| 讨论、公告与协作 | DiscussionTopic | `canvas-lms-master/app/models/discussion_topic.rb:20` | 2346 |
| 课程、班级与选课 | Enrollment | `canvas-lms-master/app/models/enrollment.rb:21` | 1725 |
| 内容、文件与模块 | ContentMigration | `canvas-lms-master/app/models/content_migration.rb:21` | 1686 |
| 测验与题库 | Quizzes::Quiz | `canvas-lms-master/app/models/quizzes/quiz.rb:21` | 1595 |
| LTI、外部工具与集成 | ContextExternalTool | `canvas-lms-master/app/models/context_external_tool.rb:21` | 1492 |
| 用户、身份与沟通 | Message | `canvas-lms-master/app/models/message.rb:21` | 1190 |
| 内容、文件与模块 | ContextModule | `canvas-lms-master/app/models/context_module.rb:21` | 1167 |
| 讨论、公告与协作 | Group | `canvas-lms-master/app/models/group.rb:21` | 1122 |
| SIS、导入导出与报表 | SisBatch | `canvas-lms-master/app/models/sis_batch.rb:21` | 1069 |
| 测验与题库 | Quizzes::QuizSubmission | `canvas-lms-master/app/models/quizzes/quiz_submission.rb:23` | 1030 |
| 内容、文件与模块 | CalendarEvent | `canvas-lms-master/app/models/calendar_event.rb:25` | 905 |
| 其他 | ContentTag | `canvas-lms-master/app/models/content_tag.rb:20` | 897 |
| 账户与租户管理 | ImpossibleCredentialsError | `canvas-lms-master/app/models/pseudonym.rb:20` | 891 |
| 内容、文件与模块 | WikiPage | `canvas-lms-master/app/models/wiki_page.rb:21` | 888 |
| 讨论、公告与协作 | DiscussionEntry | `canvas-lms-master/app/models/discussion_entry.rb:21` | 862 |
| 作业、提交与评分 | AssignmentImporter | `canvas-lms-master/app/models/importers/assignment_importer.rb:21` | 851 |
| 用户、身份与沟通 | Conversation | `canvas-lms-master/app/models/conversation.rb:21` | 838 |
| 账户与租户管理 | DeveloperKey | `canvas-lms-master/app/models/developer_key.rb:23` | 754 |
| 课程、班级与选课 | CourseContentImporter | `canvas-lms-master/app/models/importers/course_content_importer.rb:21` | 747 |
| 讨论、公告与协作 | GroupCategory | `canvas-lms-master/app/models/group_category.rb:21` | 746 |
| 内容、文件与模块 | ContentExport | `canvas-lms-master/app/models/content_export.rb:22` | 745 |
| 学习成果与能力 | LearningOutcome | `canvas-lms-master/app/models/learning_outcome.rb:21` | 741 |
| 作业、提交与评分 | Rubric | `canvas-lms-master/app/models/rubric.rb:21` | 705 |
| 内容、文件与模块 | Folder | `canvas-lms-master/app/models/folder.rb:21` | 695 |
| 用户、身份与沟通 | Notification | `canvas-lms-master/app/models/notification.rb:21` | 689 |
| 讨论、公告与协作 | WebConference | `canvas-lms-master/app/models/web_conference.rb:21` | 659 |
| 用户、身份与沟通 | ConversationParticipant | `canvas-lms-master/app/models/conversation_participant.rb:21` | 658 |
| 用户、身份与沟通 | CommunicationChannel | `canvas-lms-master/app/models/communication_channel.rb:21` | 640 |
| 作业、提交与评分 | Assignment | `canvas-lms-master/app/models/speed_grader/assignment.rb:22` | 634 |
| 作业、提交与评分 | SubmissionComment | `canvas-lms-master/app/models/submission_comment.rb:21` | 630 |
| 账户与租户管理 | AuthenticationProvider::SAML | `canvas-lms-master/app/models/authentication_provider/saml.rb:23` | 619 |
| 账户与租户管理 | AuthenticationProvider | `canvas-lms-master/app/models/authentication_provider.rb:24` | 606 |
| 作业、提交与评分 | AssignmentOverride | `canvas-lms-master/app/models/assignment_override.rb:21` | 606 |
| 讨论、公告与协作 | AppointmentGroup | `canvas-lms-master/app/models/appointment_group.rb:21` | 582 |
| 内容、文件与模块 | ContextModuleProgression | `canvas-lms-master/app/models/context_module_progression.rb:21` | 546 |
| 用户、身份与沟通 | SplitUsers | `canvas-lms-master/app/models/split_users.rb:20` | 544 |
| 其他 | StreamItem | `canvas-lms-master/app/models/stream_item.rb:21` | 496 |
| 课程、班级与选课 | CoursePace | `canvas-lms-master/app/models/course_pace.rb:21` | 495 |
| 课程、班级与选课 | CourseProxy | `canvas-lms-master/app/models/assignments/needs_grading_count_query.rb:23` | 491 |
| LTI、外部工具与集成 | Lti::IMS::Registration | `canvas-lms-master/app/models/lti/ims/registration.rb:20` | 473 |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:21` | 471 |
| 账户与租户管理 | RoleOverride | `canvas-lms-master/app/models/role_override.rb:21` | 469 |
| 讨论、公告与协作 | GroupMembership | `canvas-lms-master/app/models/group_membership.rb:21` | 467 |
| 账户与租户管理 | AuthenticationProvider | `canvas-lms-master/app/models/authentication_provider/open_id_connect.rb:21` | 462 |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:21` | 461 |
| 作业、提交与评分 | is | `canvas-lms-master/app/models/rubric_association.rb:24` | 461 |
| 用户、身份与沟通 | AssetUserAccessLog | `canvas-lms-master/app/models/asset_user_access_log.rb:20` | 458 |
| 测验与题库 | QuizImporter | `canvas-lms-master/app/models/importers/quiz_importer.rb:22` | 451 |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:20` | 445 |
| 其他 | AccessToken | `canvas-lms-master/app/models/access_token.rb:20` | 443 |
| 作业、提交与评分 | Auditors::GradeChange | `canvas-lms-master/app/models/auditors/grade_change.rb:21` | 432 |
| 测验与题库 | AssessmentQuestion | `canvas-lms-master/app/models/assessment_question.rb:21` | 431 |
| 测验与题库 | Quizzes::QuizStatistics::StudentAnalysis | `canvas-lms-master/app/models/quizzes/quiz_statistics/student_analysis.rb:21` | 428 |
| 用户、身份与沟通 | ConversationMessage | `canvas-lms-master/app/models/conversation_message.rb:21` | 426 |
| 讨论、公告与协作 | BigBlueButtonConference | `canvas-lms-master/app/models/big_blue_button_conference.rb:23` | 425 |
| 讨论、公告与协作 | Collaboration | `canvas-lms-master/app/models/collaboration.rb:21` | 423 |
| 其他 | GradingStandard | `canvas-lms-master/app/models/grading_standard.rb:21` | 408 |
| 内容、文件与模块 | ContextModuleImporter | `canvas-lms-master/app/models/importers/context_module_importer.rb:21` | 408 |
| 课程、班级与选课 | MasterCourses::MasterTemplate | `canvas-lms-master/app/models/master_courses/master_template.rb:20` | 401 |
| 其他 | ReleaseNote | `canvas-lms-master/app/models/release_note.rb:43` | 388 |
| 作业、提交与评分 | Assignment | `canvas-lms-master/app/models/assignment.rb:21` | 381 |
| 学习成果与能力 | LearningOutcomeGroup | `canvas-lms-master/app/models/learning_outcome_group.rb:21` | 379 |
| 课程、班级与选课 | EnrollmentState | `canvas-lms-master/app/models/enrollment_state.rb:20` | 364 |
| 课程、班级与选课 | MasterCourses::MasterMigration | `canvas-lms-master/app/models/master_courses/master_migration.rb:20` | 364 |
| LTI、外部工具与集成 | Lti::Registration | `canvas-lms-master/app/models/lti/registration.rb:20` | 363 |
| 测验与题库 | Quizzes::QuizSubmissionService | `canvas-lms-master/app/models/quizzes/quiz_submission_service.rb:20` | 358 |
| 用户、身份与沟通 | AssetUserAccess | `canvas-lms-master/app/models/asset_user_access.rb:24` | 357 |
| 账户与租户管理 | AuthenticationProvider::LDAP | `canvas-lms-master/app/models/authentication_provider/ldap.rb:21` | 351 |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:21` | 349 |
| LTI、外部工具与集成 | Lti::RegistrationHistoryEntry | `canvas-lms-master/app/models/lti/registration_history_entry.rb:21` | 346 |
| 作业、提交与评分 | RubricAssessment | `canvas-lms-master/app/models/rubric_assessment.rb:24` | 345 |
| LTI、外部工具与集成 | ContextExternalToolImporter | `canvas-lms-master/app/models/importers/context_external_tool_importer.rb:21` | 332 |
| 账户与租户管理 | Role | `canvas-lms-master/app/models/role.rb:21` | 327 |
| LTI、外部工具与集成 | Lti::ContextControl | `canvas-lms-master/app/models/lti/context_control.rb:19` | 318 |

## 主要关联样本

| 业务域 | Model | 关联类型 | 关联对象 | 位置 |
| --- | --- | --- | --- | --- |
| 账户与租户管理 | OAuthClientConfig | belongs_to | root_account | `canvas-lms-master/app/models/oauth_client_config.rb:36` |
| 账户与租户管理 | OAuthClientConfig | belongs_to | updated_by | `canvas-lms-master/app/models/oauth_client_config.rb:37` |
| 讨论、公告与协作 | DiscussionTopicSummary | belongs_to | root_account | `canvas-lms-master/app/models/discussion_topic_summary.rb:20` |
| 讨论、公告与协作 | DiscussionTopicSummary | belongs_to | user | `canvas-lms-master/app/models/discussion_topic_summary.rb:21` |
| 讨论、公告与协作 | DiscussionTopicSummary | belongs_to | discussion_topic | `canvas-lms-master/app/models/discussion_topic_summary.rb:22` |
| 讨论、公告与协作 | DiscussionTopicSummary | belongs_to | parent | `canvas-lms-master/app/models/discussion_topic_summary.rb:23` |
| 讨论、公告与协作 | DiscussionTopicSummary | has_many | feedback | `canvas-lms-master/app/models/discussion_topic_summary.rb:25` |
| 讨论、公告与协作 | Announcement | belongs_to | context | `canvas-lms-master/app/models/announcement.rb:22` |
| 课程、班级与选课 | CourseSection | belongs_to | course | `canvas-lms-master/app/models/course_section.rb:26` |
| 课程、班级与选课 | CourseSection | belongs_to | nonxlist_course | `canvas-lms-master/app/models/course_section.rb:27` |
| 课程、班级与选课 | CourseSection | belongs_to | root_account | `canvas-lms-master/app/models/course_section.rb:28` |
| 课程、班级与选课 | CourseSection | belongs_to | enrollment_term | `canvas-lms-master/app/models/course_section.rb:29` |
| 课程、班级与选课 | CourseSection | has_many | enrollments | `canvas-lms-master/app/models/course_section.rb:30` |
| 课程、班级与选课 | CourseSection | has_many | all_enrollments | `canvas-lms-master/app/models/course_section.rb:31` |
| 课程、班级与选课 | CourseSection | has_many | student_enrollments | `canvas-lms-master/app/models/course_section.rb:32` |
| 课程、班级与选课 | CourseSection | has_many | students | `canvas-lms-master/app/models/course_section.rb:33` |
| 课程、班级与选课 | CourseSection | has_many | all_student_enrollments | `canvas-lms-master/app/models/course_section.rb:34` |
| 课程、班级与选课 | CourseSection | has_many | all_students | `canvas-lms-master/app/models/course_section.rb:35` |
| 课程、班级与选课 | CourseSection | has_many | instructor_enrollments | `canvas-lms-master/app/models/course_section.rb:36` |
| 课程、班级与选课 | CourseSection | has_many | admin_enrollments | `canvas-lms-master/app/models/course_section.rb:37` |
| 课程、班级与选课 | CourseSection | has_many | users | `canvas-lms-master/app/models/course_section.rb:38` |
| 课程、班级与选课 | CourseSection | has_many | course_account_associations | `canvas-lms-master/app/models/course_section.rb:39` |
| 课程、班级与选课 | CourseSection | has_many | calendar_events | `canvas-lms-master/app/models/course_section.rb:40` |
| 课程、班级与选课 | CourseSection | has_many | assignment_overrides | `canvas-lms-master/app/models/course_section.rb:41` |
| 课程、班级与选课 | CourseSection | has_many | discussion_topic_section_visibilities | `canvas-lms-master/app/models/course_section.rb:42` |
| 课程、班级与选课 | CourseSection | has_many | discussion_topics | `canvas-lms-master/app/models/course_section.rb:47` |
| 课程、班级与选课 | CourseSection | has_many | course_paces | `canvas-lms-master/app/models/course_section.rb:48` |
| 课程、班级与选课 | CourseSection | has_many | sis_post_grades_statuses | `canvas-lms-master/app/models/course_section.rb:56` |
| 用户、身份与沟通 | ConversationMessageParticipant | belongs_to | conversation_message | `canvas-lms-master/app/models/conversation_message_participant.rb:26` |
| 用户、身份与沟通 | ConversationMessageParticipant | belongs_to | user | `canvas-lms-master/app/models/conversation_message_participant.rb:27` |
| 用户、身份与沟通 | ConversationMessageParticipant | belongs_to | conversation_participant | `canvas-lms-master/app/models/conversation_message_participant.rb:29` |
| 账户与租户管理 | AccountNotification | belongs_to | account | `canvas-lms-master/app/models/account_notification.rb:27` |
| 账户与租户管理 | AccountNotification | belongs_to | user | `canvas-lms-master/app/models/account_notification.rb:28` |
| 账户与租户管理 | AccountNotification | has_many | account_notification_roles | `canvas-lms-master/app/models/account_notification.rb:29` |
| 账户与租户管理 | AccountNotification | has_many | attachment_associations | `canvas-lms-master/app/models/account_notification.rb:30` |
| 作业、提交与评分 | AssignmentGroup | belongs_to | context | `canvas-lms-master/app/models/assignment_group.rb:39` |
| 作业、提交与评分 | AssignmentGroup | has_many | scores | `canvas-lms-master/app/models/assignment_group.rb:44` |
| 作业、提交与评分 | AssignmentGroup | has_many | assignments | `canvas-lms-master/app/models/assignment_group.rb:45` |
| 作业、提交与评分 | AssignmentGroup | has_many | active_assignments | `canvas-lms-master/app/models/assignment_group.rb:47` |
| 作业、提交与评分 | AssignmentGroup | has_many | published_assignments | `canvas-lms-master/app/models/assignment_group.rb:54` |
| SIS、导入导出与报表 | ErrorReport | belongs_to | user | `canvas-lms-master/app/models/error_report.rb:22` |
| SIS、导入导出与报表 | ErrorReport | belongs_to | account | `canvas-lms-master/app/models/error_report.rb:23` |
| 其他 | BlockEditor | belongs_to | context | `canvas-lms-master/app/models/block_editor.rb:22` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | user | `canvas-lms-master/app/models/learning_outcome_result.rb:25` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | learning_outcome | `canvas-lms-master/app/models/learning_outcome_result.rb:26` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | alignment | `canvas-lms-master/app/models/learning_outcome_result.rb:27` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | association_object | `canvas-lms-master/app/models/learning_outcome_result.rb:28` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | artifact | `canvas-lms-master/app/models/learning_outcome_result.rb:36` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | associated_asset | `canvas-lms-master/app/models/learning_outcome_result.rb:42` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | context | `canvas-lms-master/app/models/learning_outcome_result.rb:48` |
| 学习成果与能力 | LearningOutcomeResult | belongs_to | root_account | `canvas-lms-master/app/models/learning_outcome_result.rb:49` |
| 学习成果与能力 | LearningOutcomeResult | has_many | learning_outcome_question_results | `canvas-lms-master/app/models/learning_outcome_result.rb:50` |
| 内容、文件与模块 | Wiki | has_many | wiki_pages | `canvas-lms-master/app/models/wiki.rb:22` |
| 内容、文件与模块 | Wiki | has_one | course | `canvas-lms-master/app/models/wiki.rb:23` |
| 内容、文件与模块 | Wiki | has_one | group | `canvas-lms-master/app/models/wiki.rb:24` |
| 内容、文件与模块 | Wiki | belongs_to | root_account | `canvas-lms-master/app/models/wiki.rb:25` |
| 其他 | Collaborator | belongs_to | collaboration | `canvas-lms-master/app/models/collaborator.rb:22` |
| 其他 | Collaborator | belongs_to | group | `canvas-lms-master/app/models/collaborator.rb:23` |
| 其他 | Collaborator | belongs_to | user | `canvas-lms-master/app/models/collaborator.rb:24` |
| 其他 | ContentParticipation | belongs_to | content | `canvas-lms-master/app/models/content_participation.rb:27` |
| 其他 | ContentParticipation | belongs_to | user | `canvas-lms-master/app/models/content_participation.rb:28` |
| 用户、身份与沟通 | UserPastLtiId | belongs_to | user | `canvas-lms-master/app/models/user_past_lti_id.rb:46` |
| 用户、身份与沟通 | UserPastLtiId | belongs_to | context | `canvas-lms-master/app/models/user_past_lti_id.rb:47` |
| 其他 | MediaObject | belongs_to | user | `canvas-lms-master/app/models/media_object.rb:42` |
| 其他 | MediaObject | belongs_to | context | `canvas-lms-master/app/models/media_object.rb:43` |
| 其他 | MediaObject | belongs_to | attachment | `canvas-lms-master/app/models/media_object.rb:53` |
| 其他 | MediaObject | belongs_to | root_account | `canvas-lms-master/app/models/media_object.rb:54` |
| 其他 | MediaObject | has_many | media_tracks | `canvas-lms-master/app/models/media_object.rb:59` |
| 其他 | MediaObject | has_many | attachments_by_media_id | `canvas-lms-master/app/models/media_object.rb:60` |
| 内容、文件与模块 | ContextModule | belongs_to | context | `canvas-lms-master/app/models/context_module.rb:33` |
| 内容、文件与模块 | ContextModule | belongs_to | root_account | `canvas-lms-master/app/models/context_module.rb:34` |
| 内容、文件与模块 | ContextModule | has_many | context_module_progressions | `canvas-lms-master/app/models/context_module.rb:35` |
| 内容、文件与模块 | ContextModule | has_many | content_tags | `canvas-lms-master/app/models/context_module.rb:36` |
| 内容、文件与模块 | ContextModule | has_many | assignment_overrides | `canvas-lms-master/app/models/context_module.rb:37` |
| 内容、文件与模块 | ContextModule | has_many | assignment_override_students | `canvas-lms-master/app/models/context_module.rb:38` |
| 内容、文件与模块 | ContextModule | has_one | master_content_tag | `canvas-lms-master/app/models/context_module.rb:39` |
| 作业、提交与评分 | RubricAssessmentImport | belongs_to | course | `canvas-lms-master/app/models/rubric_assessment_import.rb:25` |
| 作业、提交与评分 | RubricAssessmentImport | belongs_to | assignment | `canvas-lms-master/app/models/rubric_assessment_import.rb:26` |
| 作业、提交与评分 | RubricAssessmentImport | belongs_to | attachment | `canvas-lms-master/app/models/rubric_assessment_import.rb:27` |
| 作业、提交与评分 | RubricAssessmentImport | belongs_to | root_account | `canvas-lms-master/app/models/rubric_assessment_import.rb:28` |
| 作业、提交与评分 | RubricAssessmentImport | belongs_to | user | `canvas-lms-master/app/models/rubric_assessment_import.rb:29` |
| 讨论、公告与协作 | DiscussionTopicInsight | belongs_to | root_account | `canvas-lms-master/app/models/discussion_topic_insight.rb:22` |
| 讨论、公告与协作 | DiscussionTopicInsight | belongs_to | user | `canvas-lms-master/app/models/discussion_topic_insight.rb:23` |
| 讨论、公告与协作 | DiscussionTopicInsight | belongs_to | discussion_topic | `canvas-lms-master/app/models/discussion_topic_insight.rb:24` |
| 讨论、公告与协作 | DiscussionTopicInsight | has_many | entries | `canvas-lms-master/app/models/discussion_topic_insight.rb:25` |
| 账户与租户管理 | Account | belongs_to | root_account | `canvas-lms-master/app/models/account.rb:37` |
| 账户与租户管理 | Account | belongs_to | parent_account | `canvas-lms-master/app/models/account.rb:38` |
| 账户与租户管理 | Account | has_many | courses | `canvas-lms-master/app/models/account.rb:40` |
| 账户与租户管理 | Account | has_many | custom_grade_statuses | `canvas-lms-master/app/models/account.rb:41` |
| 账户与租户管理 | Account | has_many | standard_grade_statuses | `canvas-lms-master/app/models/account.rb:42` |
| 账户与租户管理 | Account | has_many | favorites | `canvas-lms-master/app/models/account.rb:43` |
| 账户与租户管理 | Account | has_many | all_courses | `canvas-lms-master/app/models/account.rb:44` |
| 账户与租户管理 | Account | has_one | terms_of_service | `canvas-lms-master/app/models/account.rb:45` |
| 账户与租户管理 | Account | has_one | terms_of_service_content | `canvas-lms-master/app/models/account.rb:46` |
| 账户与租户管理 | Account | has_many | group_categories | `canvas-lms-master/app/models/account.rb:47` |
| 账户与租户管理 | Account | has_many | all_group_categories | `canvas-lms-master/app/models/account.rb:48` |
| 账户与租户管理 | Account | has_many | groups | `canvas-lms-master/app/models/account.rb:49` |
| 账户与租户管理 | Account | has_many | all_groups | `canvas-lms-master/app/models/account.rb:50` |
| 账户与租户管理 | Account | has_many | all_group_memberships | `canvas-lms-master/app/models/account.rb:51` |
| 账户与租户管理 | Account | has_many | differentiation_tag_categories | `canvas-lms-master/app/models/account.rb:52` |
| 账户与租户管理 | Account | has_many | all_differentiation_tag_categories | `canvas-lms-master/app/models/account.rb:53` |
| 账户与租户管理 | Account | has_many | differentiation_tags | `canvas-lms-master/app/models/account.rb:54` |
| 账户与租户管理 | Account | has_many | all_differentiation_tags | `canvas-lms-master/app/models/account.rb:55` |
| 账户与租户管理 | Account | has_many | all_differentiation_tag_memberships | `canvas-lms-master/app/models/account.rb:56` |
| 账户与租户管理 | Account | has_many | combined_groups_and_differentiation_tags | `canvas-lms-master/app/models/account.rb:57` |
| 账户与租户管理 | Account | has_many | combined_group_and_differentiation_tag_categories | `canvas-lms-master/app/models/account.rb:58` |
| 账户与租户管理 | Account | has_many | active_combined_group_and_differentiation_tag_categories | `canvas-lms-master/app/models/account.rb:59` |
| 账户与租户管理 | Account | has_many | enrollment_terms | `canvas-lms-master/app/models/account.rb:60` |
| 账户与租户管理 | Account | has_many | active_enrollment_terms | `canvas-lms-master/app/models/account.rb:61` |
| 账户与租户管理 | Account | has_many | grading_period_groups | `canvas-lms-master/app/models/account.rb:62` |
| 账户与租户管理 | Account | has_many | grading_periods | `canvas-lms-master/app/models/account.rb:63` |
| 账户与租户管理 | Account | has_many | enrollments | `canvas-lms-master/app/models/account.rb:64` |
| 账户与租户管理 | Account | has_many | all_enrollments | `canvas-lms-master/app/models/account.rb:65` |
| 账户与租户管理 | Account | has_many | temporary_enrollment_pairings | `canvas-lms-master/app/models/account.rb:66` |
| 账户与租户管理 | Account | has_many | sub_accounts | `canvas-lms-master/app/models/account.rb:67` |
| 账户与租户管理 | Account | has_many | all_accounts | `canvas-lms-master/app/models/account.rb:68` |
| 账户与租户管理 | Account | has_many | account_users | `canvas-lms-master/app/models/account.rb:69` |
| 账户与租户管理 | Account | has_many | active_account_users | `canvas-lms-master/app/models/account.rb:70` |
| 账户与租户管理 | Account | has_many | course_sections | `canvas-lms-master/app/models/account.rb:71` |
| 账户与租户管理 | Account | has_many | sis_batches | `canvas-lms-master/app/models/account.rb:72` |
| 账户与租户管理 | Account | has_many | abstract_courses | `canvas-lms-master/app/models/account.rb:73` |
| 账户与租户管理 | Account | has_many | root_abstract_courses | `canvas-lms-master/app/models/account.rb:74` |
| 账户与租户管理 | Account | has_many | user_account_associations | `canvas-lms-master/app/models/account.rb:75` |
| 账户与租户管理 | Account | has_many | all_users | `canvas-lms-master/app/models/account.rb:76` |
| 账户与租户管理 | Account | has_many | users | `canvas-lms-master/app/models/account.rb:77` |
| 账户与租户管理 | Account | has_many | user_past_lti_ids | `canvas-lms-master/app/models/account.rb:78` |
| 账户与租户管理 | Account | has_many | pseudonyms | `canvas-lms-master/app/models/account.rb:79` |
| 账户与租户管理 | Account | has_many | pseudonym_users | `canvas-lms-master/app/models/account.rb:80` |
| 账户与租户管理 | Account | has_many | role_overrides | `canvas-lms-master/app/models/account.rb:81` |
| 账户与租户管理 | Account | has_many | course_account_associations | `canvas-lms-master/app/models/account.rb:82` |
| 账户与租户管理 | Account | has_many | child_courses | `canvas-lms-master/app/models/account.rb:83` |
| 账户与租户管理 | Account | has_many | attachments | `canvas-lms-master/app/models/account.rb:84` |
| 账户与租户管理 | Account | has_many | active_assignments | `canvas-lms-master/app/models/account.rb:85` |
| 账户与租户管理 | Account | has_many | attachment_associations | `canvas-lms-master/app/models/account.rb:86` |
| 账户与租户管理 | Account | has_many | folders | `canvas-lms-master/app/models/account.rb:87` |
| 账户与租户管理 | Account | has_many | active_folders | `canvas-lms-master/app/models/account.rb:88` |
| 账户与租户管理 | Account | has_many | developer_keys | `canvas-lms-master/app/models/account.rb:89` |
| 账户与租户管理 | Account | has_many | developer_key_account_bindings | `canvas-lms-master/app/models/account.rb:90` |
| 账户与租户管理 | Account | has_many | lti_registration_account_bindings | `canvas-lms-master/app/models/account.rb:91` |
| 账户与租户管理 | Account | has_many | lti_overlays | `canvas-lms-master/app/models/account.rb:92` |
| 账户与租户管理 | Account | has_many | lti_overlay_versions | `canvas-lms-master/app/models/account.rb:93` |
| 账户与租户管理 | Account | has_many | lti_registration_history_entries | `canvas-lms-master/app/models/account.rb:94` |
| 账户与租户管理 | Account | has_many | lti_notice_handlers | `canvas-lms-master/app/models/account.rb:95` |
| 账户与租户管理 | Account | has_many | authentication_providers | `canvas-lms-master/app/models/account.rb:96` |
| 账户与租户管理 | Account | has_many | calendar_events | `canvas-lms-master/app/models/account.rb:100` |
| 账户与租户管理 | Account | has_many | account_reports | `canvas-lms-master/app/models/account.rb:102` |
| 账户与租户管理 | Account | has_many | institutional_tag_categories | `canvas-lms-master/app/models/account.rb:103` |
| 账户与租户管理 | Account | has_many | grading_standards | `canvas-lms-master/app/models/account.rb:104` |
| 账户与租户管理 | Account | has_many | assessment_question_banks | `canvas-lms-master/app/models/account.rb:105` |
| 账户与租户管理 | Account | has_many | assessment_questions | `canvas-lms-master/app/models/account.rb:106` |
| 账户与租户管理 | Account | has_many | roles | `canvas-lms-master/app/models/account.rb:107` |
| 账户与租户管理 | Account | has_many | all_roles | `canvas-lms-master/app/models/account.rb:108` |
| 账户与租户管理 | Account | has_many | progresses | `canvas-lms-master/app/models/account.rb:109` |
| 账户与租户管理 | Account | has_many | content_migrations | `canvas-lms-master/app/models/account.rb:110` |
| 账户与租户管理 | Account | has_many | sis_batch_errors | `canvas-lms-master/app/models/account.rb:111` |
| 账户与租户管理 | Account | has_many | canvadocs_annotation_contexts | `canvas-lms-master/app/models/account.rb:112` |
| 账户与租户管理 | Account | has_one | outcome_proficiency | `canvas-lms-master/app/models/account.rb:113` |
| 账户与租户管理 | Account | has_one | outcome_calculation_method | `canvas-lms-master/app/models/account.rb:114` |
| 账户与租户管理 | Account | has_many | rubric_imports | `canvas-lms-master/app/models/account.rb:115` |
| 账户与租户管理 | Account | has_many | rubric_assessment_imports | `canvas-lms-master/app/models/account.rb:116` |
| 账户与租户管理 | Account | has_many | auditor_account_user_records | `canvas-lms-master/app/models/account.rb:118` |
| 账户与租户管理 | Account | has_many | auditor_authentication_records | `canvas-lms-master/app/models/account.rb:122` |
| 账户与租户管理 | Account | has_many | auditor_course_records | `canvas-lms-master/app/models/account.rb:126` |
| 账户与租户管理 | Account | has_many | auditor_grade_change_records | `canvas-lms-master/app/models/account.rb:130` |
| 账户与租户管理 | Account | has_many | auditor_root_grade_change_records | `canvas-lms-master/app/models/account.rb:134` |
| 账户与租户管理 | Account | has_many | auditor_feature_flag_records | `canvas-lms-master/app/models/account.rb:139` |
| 账户与租户管理 | Account | has_many | auditor_pseudonym_records | `canvas-lms-master/app/models/account.rb:144` |
| 账户与租户管理 | Account | has_many | lti_resource_links | `canvas-lms-master/app/models/account.rb:148` |
| 账户与租户管理 | Account | has_many | lti_registrations | `canvas-lms-master/app/models/account.rb:153` |
| 账户与租户管理 | Account | has_many | block_editor_templates | `canvas-lms-master/app/models/account.rb:154` |
| 账户与租户管理 | Account | has_many | oauth_client_configs | `canvas-lms-master/app/models/account.rb:155` |
| 账户与租户管理 | Account | belongs_to | course_template | `canvas-lms-master/app/models/account.rb:156` |
| 账户与租户管理 | Account | belongs_to | grading_standard | `canvas-lms-master/app/models/account.rb:157` |
| 账户与租户管理 | Account | has_many | context_external_tools | `canvas-lms-master/app/models/account.rb:174` |
| 账户与租户管理 | Account | has_many | error_reports | `canvas-lms-master/app/models/account.rb:175` |
| 账户与租户管理 | Account | has_many | announcements | `canvas-lms-master/app/models/account.rb:176` |
| 账户与租户管理 | Account | has_many | alerts | `canvas-lms-master/app/models/account.rb:177` |
| 账户与租户管理 | Account | has_many | report_snapshots | `canvas-lms-master/app/models/account.rb:178` |
| 账户与租户管理 | Account | has_many | external_integration_keys | `canvas-lms-master/app/models/account.rb:179` |
| 账户与租户管理 | Account | has_many | shared_brand_configs | `canvas-lms-master/app/models/account.rb:180` |
| 账户与租户管理 | Account | belongs_to | brand_config | `canvas-lms-master/app/models/account.rb:181` |
| 账户与租户管理 | Account | has_many | blackout_dates | `canvas-lms-master/app/models/account.rb:182` |
| 讨论、公告与协作 | DiscussionEntryVersion | belongs_to | discussion_entry | `canvas-lms-master/app/models/discussion_entry_version.rb:20` |
| 讨论、公告与协作 | DiscussionEntryVersion | belongs_to | root_account | `canvas-lms-master/app/models/discussion_entry_version.rb:21` |
| 讨论、公告与协作 | DiscussionEntryVersion | belongs_to | user | `canvas-lms-master/app/models/discussion_entry_version.rb:22` |
| 讨论、公告与协作 | DiscussionEntryVersion | has_many | discussion_topic_insight_entries | `canvas-lms-master/app/models/discussion_entry_version.rb:23` |
| 讨论、公告与协作 | DiscussionEntryVersion | has_one | lti_asset | `canvas-lms-master/app/models/discussion_entry_version.rb:24` |
| 其他 | AllocationRule | belongs_to | course | `canvas-lms-master/app/models/allocation_rule.rb:25` |
| 其他 | AllocationRule | belongs_to | assignment | `canvas-lms-master/app/models/allocation_rule.rb:26` |
| 其他 | AllocationRule | belongs_to | assessor | `canvas-lms-master/app/models/allocation_rule.rb:27` |
| 其他 | AllocationRule | belongs_to | assessee | `canvas-lms-master/app/models/allocation_rule.rb:28` |
| 其他 | AssessmentRequest | belongs_to | user | `canvas-lms-master/app/models/assessment_request.rb:26` |
| 其他 | AssessmentRequest | belongs_to | asset | `canvas-lms-master/app/models/assessment_request.rb:27` |
| 其他 | AssessmentRequest | belongs_to | submission | `canvas-lms-master/app/models/assessment_request.rb:28` |
| 其他 | AssessmentRequest | belongs_to | assessor_asset | `canvas-lms-master/app/models/assessment_request.rb:29` |
| 其他 | AssessmentRequest | belongs_to | assessor | `canvas-lms-master/app/models/assessment_request.rb:30` |
| 其他 | AssessmentRequest | belongs_to | rubric_association | `canvas-lms-master/app/models/assessment_request.rb:31` |
| 其他 | AssessmentRequest | belongs_to | peer_review_sub_assignment | `canvas-lms-master/app/models/assessment_request.rb:32` |
| 其他 | AssessmentRequest | has_many | submission_comments | `canvas-lms-master/app/models/assessment_request.rb:33` |
| 其他 | AssessmentRequest | has_many | ignores | `canvas-lms-master/app/models/assessment_request.rb:34` |
| 其他 | AssessmentRequest | belongs_to | rubric_assessment | `canvas-lms-master/app/models/assessment_request.rb:35` |
| 其他 | Alert | belongs_to | context | `canvas-lms-master/app/models/alert.rb:22` |
| 其他 | Alert | has_many | criteria | `canvas-lms-master/app/models/alert.rb:23` |
| 用户、身份与沟通 | UserProfile | belongs_to | user | `canvas-lms-master/app/models/user_profile.rb:22` |
| 用户、身份与沟通 | UserProfile | has_many | links | `canvas-lms-master/app/models/user_profile.rb:26` |
| 其他 | UsageRights | belongs_to | context | `canvas-lms-master/app/models/usage_rights.rb:26` |
| 其他 | Score | belongs_to | enrollment | `canvas-lms-master/app/models/score.rb:24` |
| 其他 | Score | belongs_to | grading_period | `canvas-lms-master/app/models/score.rb:25` |
| 其他 | Score | belongs_to | assignment_group | `canvas-lms-master/app/models/score.rb:26` |
| 其他 | Score | belongs_to | custom_grade_status | `canvas-lms-master/app/models/score.rb:27` |
| 其他 | Score | has_one | course | `canvas-lms-master/app/models/score.rb:28` |
| 其他 | Score | has_one | score_metadata | `canvas-lms-master/app/models/score.rb:29` |
| 其他 | AlertCriterion | belongs_to | alert | `canvas-lms-master/app/models/alert_criterion.rb:22` |
| 账户与租户管理 | DeveloperKeyAccountBinding | belongs_to | account | `canvas-lms-master/app/models/developer_key_account_binding.rb:31` |
| 账户与租户管理 | DeveloperKeyAccountBinding | belongs_to | developer_key | `canvas-lms-master/app/models/developer_key_account_binding.rb:32` |
| 账户与租户管理 | DeveloperKeyAccountBinding | belongs_to | root_account | `canvas-lms-master/app/models/developer_key_account_binding.rb:33` |
| 账户与租户管理 | DeveloperKeyAccountBinding | has_one | lti_registration_account_binding | `canvas-lms-master/app/models/developer_key_account_binding.rb:34` |
| 其他 | CustomData | belongs_to | user | `canvas-lms-master/app/models/custom_data.rb:43` |
| 账户与租户管理 | ImpossibleCredentialsError | has_many | session_persistence_tokens | `canvas-lms-master/app/models/pseudonym.rb:33` |
| 账户与租户管理 | ImpossibleCredentialsError | belongs_to | account | `canvas-lms-master/app/models/pseudonym.rb:34` |

## create_table 样本

| 业务域 | 表 | migration |
| --- | --- | --- |
| 用户、身份与沟通 | ai_conversations | `canvas-lms-master/db/migrate/20251014000001_create_ai_conversations.rb:24` |
| 其他 | nav_menu_links | `canvas-lms-master/db/migrate/20260123151144_create_nav_menu_links.rb:24` |
| 内容、文件与模块 | ai_experience_context_files | `canvas-lms-master/db/migrate/20260130000001_create_ai_experience_context_files.rb:24` |
| 讨论、公告与协作 | discussion_entry_drafts | `canvas-lms-master/db/migrate/20251111085656_remove_discussion_entry_drafts.rb:27` |
| 其他 | institutional_tags | `canvas-lms-master/db/migrate/20260312092459_create_institutional_tags.rb:24` |
| 其他 | translation_feedback | `canvas-lms-master/db/migrate/20260326140109_create_translation_feedback.rb:24` |
| 其他 | institutional_tag_associations | `canvas-lms-master/db/migrate/20260312100330_create_institutional_tag_associations.rb:24` |
| 其他 | institutional_tag_categories | `canvas-lms-master/db/migrate/20260312091756_create_institutional_tag_categories.rb:24` |
| 用户、身份与沟通 | canvas_career_user_experiences | `canvas-lms-master/db/migrate/20260331080000_create_canvas_career_user_experiences.rb:24` |
| 分析、审计与运维 | delayed_jobs | `canvas-lms-master/db/migrate/20101216224513_create_delayed_jobs.rb:26` |
| 分析、审计与运维 | failed_jobs | `canvas-lms-master/db/migrate/20101216224513_create_delayed_jobs.rb:249` |
| 其他 | external_content_references | `canvas-lms-master/db/migrate/20260306122857_create_external_content_references.rb:24` |
| 课程、班级与选课 | accessibility_course_statistics | `canvas-lms-master/db/migrate/20251218132705_create_accessibility_course_statistics.rb:24` |
| 账户与租户管理 | accounts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:107` |
| 其他 | cloned_items | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:170` |
| 课程、班级与选课 | courses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:176` |
| 课程、班级与选课 | course_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:254` |
| SIS、导入导出与报表 | sis_batches | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:273` |
| 用户、身份与沟通 | users | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:304` |
| 课程、班级与选课 | abstract_courses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:363` |
| 其他 | access_tokens | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:378` |
| 搜索、AI 与无障碍 | accessibility_issues | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:402` |
| 搜索、AI 与无障碍 | accessibility_resource_scans | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:423` |
| 账户与租户管理 | account_notifications | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:447` |
| 账户与租户管理 | account_notification_roles | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:466` |
| SIS、导入导出与报表 | account_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:478` |
| SIS、导入导出与报表 | account_report_runners | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:499` |
| SIS、导入导出与报表 | account_report_rows | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:511` |
| 账户与租户管理 | account_users | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:520` |
| 搜索、AI 与无障碍 | ai_experiences | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:532` |
| 其他 | alerts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:549` |
| 其他 | alert_criteria | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:557` |
| 其他 | allocation_rules | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:563` |
| 作业、提交与评分 | anonymous_or_moderation_events | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:582` |
| 讨论、公告与协作 | appointment_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:599` |
| 讨论、公告与协作 | appointment_group_contexts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:617` |
| 讨论、公告与协作 | appointment_group_sub_contexts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:625` |
| 测验与题库 | assessment_question_banks | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:633` |
| 测验与题库 | assessment_question_bank_users | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:647` |
| 测验与题库 | assessment_questions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:653` |
| 其他 | assessment_requests | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:669` |
| 用户、身份与沟通 | asset_user_accesses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:687` |
| 作业、提交与评分 | assignment_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:721` |
| 作业、提交与评分 | assignments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:742` |
| 作业、提交与评分 | assignment_configuration_tool_lookups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:844` |
| 作业、提交与评分 | assignment_overrides | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:858` |
| 作业、提交与评分 | assignment_override_students | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:920` |
| 内容、文件与模块 | attachments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:953` |
| 内容、文件与模块 | attachment_associations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1039` |
| 内容、文件与模块 | attachment_upload_statuses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1051` |
| 账户与租户管理 | auditor_account_user_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1057` |
| 分析、审计与运维 | auditor_authentication_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1071` |
| 课程、班级与选课 | auditor_course_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1081` |
| 分析、审计与运维 | auditor_feature_flag_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1094` |
| 作业、提交与评分 | auditor_grade_change_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1108` |
| 账户与租户管理 | auditor_pseudonym_records | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1140` |
| 账户与租户管理 | authentication_providers | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1154` |
| 作业、提交与评分 | auto_grade_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1186` |
| 其他 | blackout_dates | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1199` |
| 其他 | block_editors | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1209` |
| 其他 | block_editor_templates | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1220` |
| 其他 | bookmarks_bookmarks | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1237` |
| 账户与租户管理 | brand_configs | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1245` |
| 内容、文件与模块 | calendar_events | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1260` |
| 其他 | canvadocs | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1297` |
| 其他 | canvadocs_annotation_contexts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1305` |
| 作业、提交与评分 | canvadocs_submissions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1322` |
| 其他 | canvas_metadata | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1332` |
| 讨论、公告与协作 | collaborations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1338` |
| 其他 | collaborators | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1360` |
| 其他 | comment_bank_items | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1368` |
| 用户、身份与沟通 | communication_channels | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1383` |
| 其他 | conditional_release_rules | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1417` |
| 其他 | conditional_release_scoring_ranges | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1430` |
| 作业、提交与评分 | conditional_release_assignment_sets | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1448` |
| 作业、提交与评分 | conditional_release_assignment_set_associations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1464` |
| 作业、提交与评分 | conditional_release_assignment_set_actions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1486` |
| 内容、文件与模块 | content_exports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1508` |
| 内容、文件与模块 | content_migrations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1523` |
| 其他 | content_participations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1555` |
| 其他 | content_participation_counts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1571` |
| 其他 | content_shares | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1582` |
| 其他 | content_tags | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1597` |
| LTI、外部工具与集成 | context_external_tools | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1640` |
| LTI、外部工具与集成 | context_external_tool_placements | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1674` |
| 内容、文件与模块 | context_modules | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1681` |
| 内容、文件与模块 | context_module_progressions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1702` |
| 用户、身份与沟通 | conversations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1720` |
| 用户、身份与沟通 | conversation_batches | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1732` |
| 用户、身份与沟通 | conversation_messages | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1747` |
| 用户、身份与沟通 | conversation_message_participants | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1771` |
| 用户、身份与沟通 | conversation_participants | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1787` |
| 账户与租户管理 | course_account_associations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1813` |
| 课程、班级与选课 | course_paces | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1826` |
| 课程、班级与选课 | course_pace_module_items | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1848` |
| 课程、班级与选课 | course_score_statistics | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1859` |
| 课程、班级与选课 | course_sections | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1866` |
| 账户与租户管理 | csp_domains | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1900` |
| 其他 | custom_data | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1910` |
| 作业、提交与评分 | custom_grade_statuses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1919` |
| 作业、提交与评分 | custom_gradebook_columns | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1934` |
| 作业、提交与评分 | custom_gradebook_column_data | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1945` |
| 用户、身份与沟通 | delayed_messages | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1957` |
| 用户、身份与沟通 | delayed_notifications | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1981` |
| 账户与租户管理 | developer_keys | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:1990` |
| 账户与租户管理 | developer_key_account_bindings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2037` |
| 讨论、公告与协作 | discussion_entries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2048` |
| 讨论、公告与协作 | discussion_entry_drafts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2081` |
| 讨论、公告与协作 | discussion_entry_participants | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2105` |
| 讨论、公告与协作 | discussion_entry_versions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2120` |
| 讨论、公告与协作 | discussion_topics | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2131` |
| 讨论、公告与协作 | discussion_topic_insights | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2192` |
| 讨论、公告与协作 | discussion_topic_insight_entries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2203` |
| 讨论、公告与协作 | discussion_topic_materialized_views | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2227` |
| 讨论、公告与协作 | discussion_topic_participants | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2239` |
| 课程、班级与选课 | discussion_topic_section_visibilities | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2258` |
| 讨论、公告与协作 | discussion_topic_summaries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2265` |
| 讨论、公告与协作 | discussion_topic_summary_feedback | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2290` |
| 课程、班级与选课 | enrollment_dates_overrides | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2305` |
| 课程、班级与选课 | enrollment_states | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2319` |
| 课程、班级与选课 | enrollment_terms | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2337` |
| 课程、班级与选课 | enrollments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2361` |
| 讨论、公告与协作 | eportfolios | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2416` |
| 讨论、公告与协作 | eportfolio_categories | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2427` |
| 讨论、公告与协作 | eportfolio_entries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2435` |
| SIS、导入导出与报表 | epub_exports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2447` |
| SIS、导入导出与报表 | error_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2454` |
| 其他 | estimated_durations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2476` |
| 其他 | event_stream_failures | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2503` |
| 其他 | external_feeds | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2513` |
| 其他 | external_feed_entries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2532` |
| 其他 | external_integration_keys | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2548` |
| 其他 | favorites | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2558` |
| 分析、审计与运维 | feature_flags | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2569` |
| 内容、文件与模块 | folders | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2583` |
| 作业、提交与评分 | gradebook_csvs | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2632` |
| 作业、提交与评分 | gradebook_filters | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2641` |
| 作业、提交与评分 | gradebook_uploads | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2651` |
| 课程、班级与选课 | grading_period_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2661` |
| 课程、班级与选课 | grading_periods | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2672` |
| 其他 | grading_standards | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2684` |
| 讨论、公告与协作 | group_categories | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2703` |
| 讨论、公告与协作 | groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2725` |
| 讨论、公告与协作 | group_memberships | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2758` |
| 讨论、公告与协作 | group_and_membership_importers | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2772` |
| 用户、身份与沟通 | inbox_settings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2780` |
| 其他 | ignores | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2794` |
| 其他 | late_policies | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2804` |
| 学习成果与能力 | learning_outcomes | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2817` |
| 学习成果与能力 | learning_outcome_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2843` |
| 学习成果与能力 | learning_outcome_question_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2871` |
| 学习成果与能力 | learning_outcome_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2891` |
| 其他 | live_assessments_assessments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2928` |
| 其他 | live_assessments_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2938` |
| 作业、提交与评分 | live_assessments_submissions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2948` |
| 搜索、AI 与无障碍 | llm_responses | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2959` |
| LTI、外部工具与集成 | lti_assets | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:2976` |
| LTI、外部工具与集成 | lti_asset_processors | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3013` |
| LTI、外部工具与集成 | lti_asset_processor_eula_acceptances | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3035` |
| LTI、外部工具与集成 | lti_asset_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3050` |
| LTI、外部工具与集成 | lti_context_controls | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3076` |
| LTI、外部工具与集成 | lti_import_histories | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3099` |
| LTI、外部工具与集成 | lti_ims_registrations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3109` |
| LTI、外部工具与集成 | lti_line_items | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3137` |
| LTI、外部工具与集成 | lti_links | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3156` |
| 用户、身份与沟通 | lti_message_handlers | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3170` |
| LTI、外部工具与集成 | lti_notice_handlers | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3187` |
| LTI、外部工具与集成 | lti_overlays | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3201` |
| LTI、外部工具与集成 | lti_overlay_versions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3214` |
| LTI、外部工具与集成 | lti_product_families | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3226` |
| LTI、外部工具与集成 | lti_registrations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3242` |
| 账户与租户管理 | lti_registration_account_bindings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3261` |
| LTI、外部工具与集成 | lti_registration_history_entries | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3276` |
| LTI、外部工具与集成 | lti_registration_update_requests | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3288` |
| LTI、外部工具与集成 | lti_resource_handlers | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3310` |
| LTI、外部工具与集成 | lti_resource_links | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3327` |
| LTI、外部工具与集成 | lti_resource_placements | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3348` |
| LTI、外部工具与集成 | lti_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3361` |
| LTI、外部工具与集成 | lti_tool_configurations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3379` |
| 用户、身份与沟通 | lti_tool_consumer_profiles | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3402` |
| LTI、外部工具与集成 | lti_tool_proxies | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3410` |
| LTI、外部工具与集成 | lti_tool_proxy_bindings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3430` |
| LTI、外部工具与集成 | lti_tool_settings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3440` |
| 课程、班级与选课 | master_courses_child_content_tags | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3456` |
| 课程、班级与选课 | master_courses_child_subscriptions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3476` |
| 课程、班级与选课 | master_courses_master_content_tags | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3496` |
| 课程、班级与选课 | master_courses_master_migrations | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3519` |
| 课程、班级与选课 | master_courses_master_templates | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3537` |
| 课程、班级与选课 | master_courses_migration_results | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3559` |
| 其他 | media_objects | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3579` |
| 其他 | media_tracks | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3606` |
| 其他 | mentions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3620` |
| 用户、身份与沟通 | messages | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3630` |
| 讨论、公告与协作 | microsoft_sync_groups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3661` |
| LTI、外部工具与集成 | microsoft_sync_partial_sync_changes | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3677` |
| 用户、身份与沟通 | microsoft_sync_user_mappings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3690` |
| SIS、导入导出与报表 | migration_issues | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3701` |
| 作业、提交与评分 | moderation_graders | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3712` |
| 作业、提交与评分 | moderated_grading_provisional_grades | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3723` |
| 其他 | moderated_grading_selections | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3748` |
| 用户、身份与沟通 | notification_endpoints | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3762` |
| 用户、身份与沟通 | notifications | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3774` |
| 账户与租户管理 | oauth_client_configs | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3784` |
| 用户、身份与沟通 | notification_policies | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3807` |
| 用户、身份与沟通 | notification_policy_overrides | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3816` |
| 账户与租户管理 | oauth_requests | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3839` |
| 用户、身份与沟通 | observer_alert_thresholds | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3851` |
| 用户、身份与沟通 | observer_alerts | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3862` |
| 用户、身份与沟通 | observer_pairing_codes | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3874` |
| 其他 | one_time_passwords | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3882` |
| SIS、导入导出与报表 | originality_reports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3891` |
| 学习成果与能力 | outcome_calculation_methods | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3908` |
| 学习成果与能力 | outcome_friendly_descriptions | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3920` |
| 学习成果与能力 | outcome_imports | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3931` |
| 学习成果与能力 | outcome_import_errors | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3946` |
| 学习成果与能力 | outcome_proficiencies | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3954` |
| 学习成果与能力 | outcome_proficiency_ratings | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3966` |
| 学习成果与能力 | outcome_rollups | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3980` |
| 内容、文件与模块 | page_comments | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3998` |
| 内容、文件与模块 | page_views | `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4008` |

## 状态字段/规则线索

| 业务域 | Model | 位置 | 代码 |
| --- | --- | --- | --- |
| 账户与租户管理 | OAuthClientConfig | `canvas-lms-master/app/models/oauth_client_config.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 账户与租户管理 | OAuthClientConfig | `canvas-lms-master/app/models/oauth_client_config.rb:94` | active.find_by(root_account:, type:, identifier:) |
| 账户与租户管理 | OAuthClientConfig | `canvas-lms-master/app/models/oauth_client_config.rb:115` | active.where(root_account:).where([:type, :identifier] => identifiers) |
| 讨论、公告与协作 | DiscussionTopicSummary | `canvas-lms-master/app/models/discussion_topic_summary.rb:8` | # the terms of the GNU Affero General Public License as published by the Free |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:58` | _old_draft_state, new_draft_state = changes["workflow_state"] |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:59` | errors.add :workflow_state, I18n.t("#announcements.error_draft_state", "This topic cannot be set to draft state because it is an announcement.") if new_draft_state == "unpublished" |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:68` | self.locked = true if !locked? && |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:78` | workflow_state: "active" |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:79` | ).update_all(locked: true) |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:90` | is_new_announcement = (record.previously_new_record? and !(record.post_delayed? \|\| record.unpublished?)) \|\| record.changed_state(:active, :unpublished) |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:99` | is_new_announcement = (record.previously_new_record? and !(record.post_delayed? \|\| record.unpublished?)) \|\| |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:100` | record.changed_state(:active, :unpublished) \|\| |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:101` | record.changed_state(:active, :post_delayed) |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:115` | given { \|user\| self.user.present? && self.user == user && discussion_entries.active.empty? } |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:131` | given { \|user, session\| context.grants_right?(user, session, :post_to_forum) && !locked? && !comments_disabled? } |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:171` | def published? |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:180` | return false if locked? && !grants_right?(user, :read_as_admin) |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:186` | return if !saved_changes.key?("workflow_state") \|\| saved_changes["workflow_state"][1] != "active" |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:189` | create_observer_alerts if course.enrollments.active.of_observer_type.where.not(associated_user_id: nil).exists? |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:193` | course.enrollments.active.of_observer_type.where.not(associated_user_id: nil).find_each do \|enrollment\| |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:220` | # Check if transitioning from post_delayed to active |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:221` | became_active = workflow_state_before_last_save == "post_delayed" && workflow_state == "active" |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:222` | if became_active && should_send_to_stream |
| 讨论、公告与协作 | Announcement | `canvas-lms-master/app/models/announcement.rb:311` | %i[title message workflow_state] |
| 其他 | Mailer | `canvas-lms-master/app/models/mailer.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:30` | has_many :enrollments, -> { preload(:user).where("enrollments.workflow_state<>'deleted'") } |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:32` | has_many :student_enrollments, -> { where("enrollments.workflow_state NOT IN ('deleted', 'completed', 'rejected', 'inactive')").preload(:user) }, class_name: "StudentEnrollment" |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:34` | has_many :all_student_enrollments, -> { where("enrollments.workflow_state<>'deleted'").preload(:user) }, class_name: "StudentEnrollment" |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:44` | where("discussion_topic_section_visibilities.workflow_state<>'deleted'") |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:51` | validates :course_id, :root_account_id, :workflow_state, presence: true |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:60` | after_save :delete_enrollments_later_if_deleted |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:83` | def delete_enrollments_later_if_deleted |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:84` | delay_if_production.delete_enrollments_if_deleted if workflow_state == "deleted" && saved_change_to_workflow_state? |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:87` | def delete_enrollments_if_deleted |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:88` | if workflow_state == "deleted" |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:89` | enrollments.where.not(workflow_state: "deleted").find_in_batches do \|batch\| |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:131` | delegate :available?, to: :course |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:133` | def concluded? |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:138` | course.concluded? |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:284` | self.default_section = (course.course_sections.active.empty?) |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:290` | assignment_overrides.active.destroy_all |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:291` | discussion_topic_section_visibilities.active.destroy_all |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:341` | if nonxlist_course.workflow_state == "deleted" |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:342` | nonxlist_course.workflow_state = "claimed" |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:359` | if deleted? && course.course_sections.active.empty? |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:365` | !enrollments.where.not(workflow_state: "rejected").not_fake.exists? |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:373` | state :active |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:374` | state :deleted |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:379` | self.workflow_state = "deleted" |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:394` | cs = CourseSection.where(id: batch).select(:id, :workflow_state).to_a |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:395` | data = SisBatchRollBackData.build_dependent_data(sis_batch:, contexts: cs, updated_state: "deleted", batch_mode_delete: batch_mode) |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:396` | CourseSection.where(id: cs.map(&:id)).update_all(workflow_state: "deleted", updated_at: Time.zone.now) |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:397` | Enrollment.where(course_section_id: cs.map(&:id)).active.find_in_batches do \|e_batch\| |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:410` | DiscussionTopicSectionVisibility.where(id: d_batch).update_all(workflow_state: "deleted") |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:415` | scope :active, -> { where("course_sections.workflow_state<>'deleted'") } |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:420` | users.all? { \|user\| student_enrollments.active.for_user(user).exists? } |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:435` | course_paces.published.find_each(&:create_publish_progress) |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:441` | if opts[:include_inactive] |
| 课程、班级与选课 | CourseSection | `canvas-lms-master/app/models/course_section.rb:446` | .where("enrollment_states.state IN ('active', 'invited', 'completed', 'pending_invited', 'pending_active')") |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:34` | scope :active, -> { where("(conversation_message_participants.workflow_state <> 'deleted' OR conversation_message_participants.workflow_state IS NULL)") } |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:35` | scope :deleted, -> { where(workflow_state: "deleted") } |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:43` | state :active |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:44` | state :deleted |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:49` | def self.query_deleted(user_id, options = {}) |
| 用户、身份与沟通 | ConversationMessageParticipant | `canvas-lms-master/app/models/conversation_message_participant.rb:50` | query = deleted.eager_load(:conversation_message).where(user_id:).order(deleted_at: :desc) |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:29` | has_many :account_notification_roles, -> { active }, dependent: :destroy |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:35` | after_save :create_alert, unless: -> { saved_change_to_workflow_state?(to: "deleted") } |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:36` | after_save :queue_message_broadcast, unless: -> { saved_change_to_workflow_state?(to: "deleted") } |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:76` | thresholds = ObserverAlertThreshold.active.where(observer: User.of_account(account), alert_type: "institution_announcement") |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:112` | all_account_ids = Course.where(id: course_ids).not_deleted |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:114` | all_account_ids += user.account_users.active.shard(user.in_region_associated_shards) |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:115` | .joins(:account).where(accounts: { workflow_state: "active" }) |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:255` | scope = AccountNotification.active |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:352` | workflow_state != "deleted" |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:402` | course_ids = Course.active.where(id: min_id..max_id, account_id: all_account_ids).pluck(:id) |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:416` | scope = AccountUser.where(id: min_id..max_id).active.where(account_id: all_account_ids) |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:431` | AND es.state IN ('active', 'invited', 'pending_invited', 'pending_active') |
| 账户与租户管理 | AccountNotification | `canvas-lms-master/app/models/account_notification.rb:439` | .having("COUNT(pseudonyms.id) = COUNT(CASE WHEN pseudonyms.workflow_state = 'suspended' THEN 1 END)") |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:24` | # Unlike our other soft-deletable models, assignment groups use 'available' instead of 'active' |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:25` | # to indicate a not-deleted state. This means we have to add the 'available' state here before |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:26` | # Canvas::SoftDeletable adds the 'active' and 'deleted' states, so that 'available' becomes the |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:28` | workflow { state :available } |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:40` | acts_as_list scope: { context: self, workflow_state: "available" } |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:44` | has_many :scores, -> { active } |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:49` | where("assignments.workflow_state<>'deleted'").order("assignments.position, assignments.due_at, assignments.title") |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:56` | where(workflow_state: "published").order("assignments.position, assignments.due_at, assignments.title") |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:60` | validates :context_id, :context_type, :workflow_state, presence: true |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:124` | # time that this group was last modified, that assignment was deleted |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:126` | # were deleted earlier. |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:129` | undestroy(active_state: "available") |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:190` | if (drop_lowest && drop_lowest > assignments.active.count) \|\| |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:191` | (drop_highest && drop_highest > assignments.active.count) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:201` | scope :active, -> { where("assignment_groups.workflow_state<>'deleted'") } |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:203` | scope :for_context_codes, ->(codes) { active.where(context_code: codes).ordered } |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:225` | record.changed_in_state(:available, fields: :group_weight) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:280` | context.active_assignments.published.where(assignment_group_id: assignment_groups) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:283` | .where(assignment_group_id: assignment_groups).published |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:290` | checkpoints_scope = SubAssignment.active.where(parent_assignment_id: scope_assignment_ids) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:303` | new_group = context.assignment_groups.active.find(move_to_id) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:304` | order = new_group.assignments.active.pluck(:id) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:305` | ids_to_change = assignments.active.pluck(:id) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:317` | set_scores_workflow_state_in_batches(:deleted) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:322` | set_scores_workflow_state_in_batches(:active, exclude_workflow_states: [:completed, :deleted]) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:325` | def set_scores_workflow_state_in_batches(new_workflow_state, exclude_workflow_states: [:completed]) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:329` | ).where.not(workflow_state: exclude_workflow_states) |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:334` | workflow_state: (new_workflow_state == :active) ? :deleted : :active |
| 作业、提交与评分 | AssignmentGroup | `canvas-lms-master/app/models/assignment_group.rb:338` | Score.where(id: score_ids_batch).update_all(workflow_state: new_workflow_state, updated_at: Time.zone.now) |
| SIS、导入导出与报表 | ErrorReport | `canvas-lms-master/app/models/error_report.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| SIS、导入导出与报表 | ErrorReport | `canvas-lms-master/app/models/error_report.rb:183` | # depending on the web server, and our invalid utf-8 stripping breaks on that |
| 讨论、公告与协作 | EtherpadCollaboration | `canvas-lms-master/app/models/etherpad_collaboration.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 其他 | BlockEditor | `canvas-lms-master/app/models/block_editor.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:151` | scope :with_active_link, -> { where("content_tags.workflow_state <> 'deleted'").joins(:alignment) } |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:154` | .joins("LEFT JOIN #{Assignment.quoted_table_name} ra ON ra.id = rassoc.association_id AND rassoc.association_type = 'Assignment' AND rassoc.purpose = 'grading' AND rassoc.workflow_ |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:163` | OR ra.grading_type = 'not_graded' |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:164` | OR qa.grading_type = 'not_graded' |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:165` | OR sa.grading_type = 'not_graded' |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:175` | out_results = LearningOutcomeResult.active.preload(:alignment).where(learning_outcome_id: alignment.learning_outcome_id, user_id: user.id).to_a |
| 学习成果与能力 | LearningOutcomeResult | `canvas-lms-master/app/models/learning_outcome_result.rb:184` | self.workflow_state = "deleted" |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:68` | published: created_at, |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:168` | # Pages created by a user without this permission will be automatically published |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:216` | def find_page(param, include_deleted: false) |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:222` | scope = if include_deleted |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:223` | wiki_pages.order(Arel.sql("CASE WHEN workflow_state <> 'deleted' THEN 0 ELSE 1 END")) |
| 内容、文件与模块 | Wiki | `canvas-lms-master/app/models/wiki.rb:225` | wiki_pages.not_deleted |
| 其他 | Collaborator | `canvas-lms-master/app/models/collaborator.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 其他 | Collaborator | `canvas-lms-master/app/models/collaborator.rb:37` | if context.workflow_state.in?(["available", "completed"]) |
| 其他 | Collaborator | `canvas-lms-master/app/models/collaborator.rb:41` | users = [] # do not send notifications to any users if the course is unpublished |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:24` | ACCESSIBLE_ATTRIBUTES = %i[content user workflow_state content_item].freeze |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:33` | validates :content_type, :content_id, :user_id, :workflow_state, :content_item, presence: true |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:49` | workflow_state = opts.fetch(:workflow_state, "unread") |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:50` | participate(content:, user:, workflow_state:) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:58` | return unless saved_change_to_workflow_state? |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:73` | def self.participate(content:, user:, workflow_state: "unread", content_item: "grade") |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:79` | participant = create_first_participation_item(participations, content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:80` | participant \|\|= update_existing_participation_item(participations, workflow_state, content_item, content) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:81` | participant \|\|= add_participation_item(participations, content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:88` | def self.create_first_participation_item(participations, content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:91` | participant = build_item(content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:92` | participant.unread_count_offset = (workflow_state == "unread") ? 1 : 0 |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:98` | def self.update_existing_participation_item(participations, workflow_state, content_item, content) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:101` | return participant if participant.nil? \|\| !content_posted?(content) \|\| same_workflow_state?(participant, workflow_state) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:106` | (workflow_state == "unread") ? 1 : -1 |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:110` | participant.workflow_state = workflow_state |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:115` | def self.add_participation_item(participations, content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:116` | participant = build_item(content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:117` | participant.unread_count_offset = (all_read?(participations) && workflow_state == "unread") ? 1 : 0 |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:123` | items.all? { \|participant\| participant.workflow_state == "read" } |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:127` | def self.build_item(content, user, workflow_state, content_item) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:128` | content.content_participations.build(user:, workflow_state:, content_item:) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:132` | def self.same_workflow_state?(participant, workflow_state) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:133` | participant.present? && participant.workflow_state == workflow_state |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:135` | private_class_method :same_workflow_state? |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:150` | ContentParticipation.where(content:, user:, content_item:).pluck(:workflow_state) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:152` | ContentParticipation.where(content:, user:).pluck(:workflow_state) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:161` | def self.items_by_submission(participations, workflow_state) |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:166` | unread_items[cp.content_id] << cp.content_item if cp.workflow_state == workflow_state |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:180` | .having("sum(case workflow_state when 'unread' then 1 else 0 end) = 0") |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:186` | { user_id:, content_id:, content_type: "Submission", content_item:, workflow_state: "read" } |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:220` | content_participations = ContentParticipation.where(user:, content: contents, workflow_state: "unread") |
| 其他 | ContentParticipation | `canvas-lms-master/app/models/content_participation.rb:222` | content_participations.update_all(workflow_state: "read") |
| 用户、身份与沟通 | UserPastLtiId | `canvas-lms-master/app/models/user_past_lti_id.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 用户、身份与沟通 | UserPastLtiId | `canvas-lms-master/app/models/user_past_lti_id.rb:24` | # when a user_a is merged into user_b, user_a is deleted, and user_b remains |
| 用户、身份与沟通 | UserPastLtiId | `canvas-lms-master/app/models/user_past_lti_id.rb:25` | # active. Now any courses, groups, or accounts |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:9` | # the terms of the GNU Affero General Public License as published by the Free |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:56` | validates :media_id, :workflow_state, presence: true |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:92` | # of the authorizing resource or fallback to the user's active |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:334` | self.workflow_state = "deleted" |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:391` | workflow_state: "pending_upload", |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:402` | return unless updated_attachment && ["pending_upload", "errored"].include?(updated_attachment.workflow_state) |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:417` | updated_attachment.workflow_state = "processed" |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:431` | def deleted? |
| 其他 | MediaObject | `canvas-lms-master/app/models/media_object.rb:432` | workflow_state == "deleted" |

## 数据模型重构提示
- Canvas 的核心实体应围绕 Account、User/Pseudonym、Course、Section、Enrollment、Assignment、Submission、Quiz、Attachment、ContextModule、Discussion、LearningOutcome、ExternalTool、SISImport 等建立概念图。
- `workflow_state` 是 Rails/Canvas 中大量实体的状态事实字段，重构时需要保留状态机语义。
- 关联关系密集，不能只按表名拆服务；必须结合权限和上下文。
