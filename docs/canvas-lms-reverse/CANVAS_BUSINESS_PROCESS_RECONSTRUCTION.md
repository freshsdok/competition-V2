# Canvas LMS 核心业务流程反向重建

## 账户开通与登录
**证据等级**：`STATIC_INFERENCE`

1. 机构/站点配置 root account 和认证方式
2. 用户通过 pseudonym/OAuth/SAML 登录
3. 系统建立 session/token 并加载权限
4. 按账户/课程上下文控制功能访问

**代表证据**：`canvas-lms-master/app/controllers/login_controller.rb:21`, `canvas-lms-master/app/controllers/login/oauth2_controller.rb:21`, `canvas-lms-master/app/controllers/login/oauth_base_controller.rb:21`, `canvas-lms-master/app/controllers/login/otp_controller.rb:24`, `canvas-lms-master/app/controllers/login/saml_idp_discovery_controller.rb:21`, `canvas-lms-master/app/controllers/login/cas_controller.rb:23`, `canvas-lms-master/app/controllers/login/openid_connect_controller.rb:21`, `canvas-lms-master/app/controllers/login/external_auth_observers_controller.rb:20`, `canvas-lms-master/app/controllers/login/canvas_controller.rb:21`, `canvas-lms-master/app/controllers/login/oauth_controller.rb:21`, `canvas-lms-master/app/controllers/login/saml_controller.rb:21`, `canvas-lms-master/app/controllers/login/clever_controller.rb:22`

## 课程创建与选课
**证据等级**：`STATIC_INFERENCE`

1. 管理员或 SIS 创建课程/学期/班级
2. 教师或管理员配置课程导航和设置
3. 学生/教师通过 enrollment 加入课程
4. 课程上下文承载作业、文件、讨论、成绩

**代表证据**：`canvas-lms-master/app/controllers/courses_controller.rb:350`, `canvas-lms-master/app/controllers/self_enrollments_controller.rb:21`, `canvas-lms-master/app/controllers/enrollments_api_controller.rb:327`, `canvas-lms-master/app/controllers/temporary_enrollment_pairings_api_controller.rb:41`, `canvas-lms-master/app/controllers/course_pacing/student_enrollment_paces_api_controller.rb:20`, `canvas-lms-master/app/controllers/course_pacing/bulk_student_enrollment_paces_api_controller.rb:20`, `canvas-lms-master/app/models/enrollment_term.rb:21`, `canvas-lms-master/app/models/teacher_enrollment.rb:21`, `canvas-lms-master/app/models/student_view_enrollment.rb:21`, `canvas-lms-master/app/models/observer_enrollment.rb:21`, `canvas-lms-master/app/models/designer_enrollment.rb:21`, `canvas-lms-master/app/models/enrollment.rb:21`

## 作业发布与提交
**证据等级**：`STATIC_INFERENCE`

1. 教师创建 assignment 并设置截止时间/评分方式
2. 学生查看并提交文本/文件/工具作业
3. 系统形成 submission 和附件/评论
4. 教师通过 gradebook/speedgrader 评分并发布反馈

**代表证据**：`canvas-lms-master/app/controllers/assignments_controller.rb:22`, `canvas-lms-master/app/controllers/submissions_base_controller.rb:21`, `canvas-lms-master/app/controllers/anonymous_submissions_controller.rb:21`, `canvas-lms-master/app/controllers/submissions_api_controller.rb:213`, `canvas-lms-master/app/controllers/submissions_controller.rb:91`, `canvas-lms-master/app/controllers/polling/poll_submissions_controller.rb:53`, `canvas-lms-master/app/controllers/lti/submissions_api_controller.rb:140`, `canvas-lms-master/app/controllers/submissions/downloads_controller.rb:22`, `canvas-lms-master/app/controllers/submissions/downloads_base_controller.rb:22`, `canvas-lms-master/app/controllers/submissions/previews_base_controller.rb:22`, `canvas-lms-master/app/controllers/submissions/anonymous_previews_controller.rb:22`, `canvas-lms-master/app/controllers/submissions/previews_controller.rb:22`

## 测验作答与统计
**证据等级**：`STATIC_INFERENCE`

1. 教师创建 quiz/题目/题库
2. 学生进入测验并记录作答事件
3. 系统保存 quiz submission 和答案
4. 教师查看统计、报告或导出结果

**代表证据**：`canvas-lms-master/app/controllers/new_quizzes_controller.rb:26`, `canvas-lms-master/app/controllers/quizzes_next/quizzes_api_controller.rb:20`, `canvas-lms-master/app/controllers/quizzes/quiz_submissions_controller.rb:21`, `canvas-lms-master/app/controllers/quizzes/quiz_submission_users_controller.rb:85`, `canvas-lms-master/app/controllers/quizzes/quiz_submission_questions_controller.rb:53`, `canvas-lms-master/app/controllers/quizzes/quiz_submission_events_controller.rb:21`, `canvas-lms-master/app/controllers/quizzes/quiz_assignment_overrides_controller.rb:96`, `canvas-lms-master/app/controllers/quizzes/quiz_submission_files_controller.rb:22`, `canvas-lms-master/app/controllers/quizzes/outstanding_quiz_submissions_controller.rb:24`, `canvas-lms-master/app/controllers/quizzes/course_quiz_extensions_controller.rb:61`, `canvas-lms-master/app/controllers/quizzes/quiz_reports_controller.rb:100`, `canvas-lms-master/app/controllers/quizzes/quiz_extensions_controller.rb:67`

## 课程内容组织
**证据等级**：`STATIC_INFERENCE`

1. 教师维护 wiki pages、files、folders、modules、calendar events
2. 学生通过课程导航访问内容
3. 系统控制可见性、锁定、发布和模块顺序

**代表证据**：`canvas-lms-master/app/controllers/wiki_pages_controller.rb:20`, `canvas-lms-master/app/controllers/wiki_pages_api_controller.rb:174`, `canvas-lms-master/app/models/wiki_pages/scoped_to_user.rb:21`, `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:5308`, `canvas-lms-master/app/controllers/files_controller.rb:128`, `canvas-lms-master/app/controllers/quizzes/quiz_submission_files_controller.rb:22`, `canvas-lms-master/db/migrate/20260130000001_create_ai_experience_context_files.rb:24`, `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:3402`, `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:4131`, `canvas-lms-master/db/migrate/20101210192618_init_canvas_db.rb:5210`, `canvas-lms-master/app/controllers/context_modules_controller.rb:21`, `canvas-lms-master/app/controllers/context_modules_api_controller.rb:126`

## 讨论与消息沟通
**证据等级**：`STATIC_INFERENCE`

1. 用户在课程/小组中创建 discussion/announcement
2. 参与者回复讨论或通过 conversations 私信
3. 通知系统按偏好发送站内/邮件/外部通知

**代表证据**：`canvas-lms-master/app/controllers/discussion_topic_users_controller.rb:19`, `canvas-lms-master/app/controllers/discussion_topics_api_controller.rb:24`, `canvas-lms-master/app/controllers/discussion_entries_controller.rb:22`, `canvas-lms-master/app/controllers/discussion_topics_controller.rb:271`, `canvas-lms-master/app/models/discussion_topic_summary.rb:19`, `canvas-lms-master/app/models/discussion_topic_insight.rb:19`, `canvas-lms-master/app/models/discussion_entry_version.rb:19`, `canvas-lms-master/app/models/discussion_topic_embedding.rb:19`, `canvas-lms-master/app/models/discussion_entry.rb:21`, `canvas-lms-master/app/models/discussion_topic_participant.rb:21`, `canvas-lms-master/app/models/discussion_topic.rb:20`, `canvas-lms-master/app/models/discussion_topic_section_visibility.rb:20`

## SIS 数据导入
**证据等级**：`CONFIRMED_BY_CODE`

1. 管理员在 root account 上传 SIS CSV/ZIP 或原始数据
2. 系统创建 SIS import batch
3. 导入课程、用户、班级、选课并处理 batch cleanup
4. 输出错误、警告和导入状态

**代表证据**：`canvas-lms-master/app/controllers/sis_imports_api_controller.rb:373`, `canvas-lms-master/app/controllers/rubric_assessment_imports_controller.rb:25`, `canvas-lms-master/app/controllers/sis_import_errors_api_controller.rb:57`, `canvas-lms-master/app/controllers/outcome_imports_api_controller.rb:106`, `canvas-lms-master/app/controllers/content_imports_controller.rb:22`, `canvas-lms-master/app/controllers/outcomes_academic_benchmark_import_api_controller.rb:21`, `canvas-lms-master/app/models/rubric_assessment_import.rb:21`, `canvas-lms-master/app/models/outcome_import_error.rb:21`, `canvas-lms-master/app/models/rubric_import.rb:21`, `canvas-lms-master/app/models/parallel_importer.rb:21`, `canvas-lms-master/app/models/importers.rb:44`

## LTI 工具配置与启动
**证据等级**：`STATIC_INFERENCE`

1. 管理员配置 external tool / LTI registration
2. 课程或作业建立 resource link
3. 用户从课程/作业发起 LTI launch
4. 系统校验签名/token、上下文、成员关系和结果服务

**代表证据**：`canvas-lms-master/app/controllers/lti_api_controller.rb:25`, `canvas-lms-master/app/controllers/test/mock_lti_controller.rb:23`, `canvas-lms-master/app/controllers/lti/tool_proxy_controller.rb:21`, `canvas-lms-master/app/controllers/lti/resource_links_controller.rb:150`, `canvas-lms-master/app/controllers/lti/platform_storage_controller.rb:42`, `canvas-lms-master/app/controllers/lti/asset_processor_controller.rb:21`, `canvas-lms-master/app/controllers/lti/tool_default_icon_controller.rb:21`, `canvas-lms-master/app/controllers/lti/token_controller.rb:21`, `canvas-lms-master/app/controllers/lti/context_controls_controller.rb:125`, `canvas-lms-master/app/controllers/lti/asset_processor_launch_controller.rb:21`, `canvas-lms-master/app/controllers/lti/public_jwk_controller.rb:22`, `canvas-lms-master/app/controllers/lti/asset_processor_tii_migrations_api_controller.rb:22`

## 成绩与学习成果分析
**证据等级**：`STATIC_INFERENCE`

1. 作业/测验/评分产生成绩
2. Outcome/Rubric/Proficiency 记录学习成果
3. Gradebook 和报表聚合成绩/能力数据
4. 审计和历史记录追踪变更

**代表证据**：`canvas-lms-master/app/controllers/outcomes_controller.rb:21`, `canvas-lms-master/app/controllers/outcome_results_controller.rb:189`, `canvas-lms-master/app/controllers/outcome_proficiency_api_controller.rb:66`, `canvas-lms-master/app/controllers/outcome_groups_controller.rb:21`, `canvas-lms-master/app/controllers/outcome_imports_api_controller.rb:106`, `canvas-lms-master/app/controllers/outcomes_academic_benchmark_import_api_controller.rb:21`, `canvas-lms-master/app/controllers/outcomes_api_controller.rb:165`, `canvas-lms-master/app/controllers/outcome_groups_api_controller.rb:141`, `canvas-lms-master/app/models/learning_outcome_result.rb:21`, `canvas-lms-master/app/models/user_lmgb_outcome_orderings.rb:21`, `canvas-lms-master/app/models/outcome_proficiency_rating.rb:21`, `canvas-lms-master/app/models/outcome_import_error.rb:21`
