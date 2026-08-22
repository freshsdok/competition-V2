# Moodle 关键证据摘录

完整证据见 `MOODLE_EVIDENCE_INDEX.csv`。本文件只列出最能支撑设计判断的样例。

## 1. 版本证据

- `public/version.php`：`release=5.3dev (Build: 20260420)`，`branch=503`，`version=2026042000.00`。

## 2. 权限点样例

| 权限点 | 位置 |
|---|---|
| `mod/bigbluebuttonbn:addinstance` | `public/mod/bigbluebuttonbn/db/access.php:33` |
| `mod/bigbluebuttonbn:addinstancewithmeeting` | `public/mod/bigbluebuttonbn/db/access.php:45` |
| `mod/bigbluebuttonbn:addinstancewithrecording` | `public/mod/bigbluebuttonbn/db/access.php:56` |
| `mod/bigbluebuttonbn:join` | `public/mod/bigbluebuttonbn/db/access.php:67` |
| `mod/bigbluebuttonbn:view` | `public/mod/bigbluebuttonbn/db/access.php:80` |
| `mod/bigbluebuttonbn:managerecordings` | `public/mod/bigbluebuttonbn/db/access.php:92` |
| `mod/bigbluebuttonbn:viewallrecordingformats` | `public/mod/bigbluebuttonbn/db/access.php:103` |
| `mod/bigbluebuttonbn:publishrecordings` | `public/mod/bigbluebuttonbn/db/access.php:113` |
| `mod/bigbluebuttonbn:unpublishrecordings` | `public/mod/bigbluebuttonbn/db/access.php:123` |
| `mod/bigbluebuttonbn:protectrecordings` | `public/mod/bigbluebuttonbn/db/access.php:133` |
| `mod/bigbluebuttonbn:unprotectrecordings` | `public/mod/bigbluebuttonbn/db/access.php:143` |
| `mod/bigbluebuttonbn:deleterecordings` | `public/mod/bigbluebuttonbn/db/access.php:153` |
| `mod/bigbluebuttonbn:importrecordings` | `public/mod/bigbluebuttonbn/db/access.php:163` |
| `mod/bigbluebuttonbn:seepresentation` | `public/mod/bigbluebuttonbn/db/access.php:173` |
| `mod/qbank:view` | `public/mod/qbank/db/access.php:31` |
| `mod/qbank:addinstance` | `public/mod/qbank/db/access.php:41` |
| `mod/data:addinstance` | `public/mod/data/db/access.php:29` |
| `mod/data:viewentry` | `public/mod/data/db/access.php:41` |
| `mod/data:writeentry` | `public/mod/data/db/access.php:55` |
| `mod/data:rate` | `public/mod/data/db/access.php:69` |
| `mod/data:viewrating` | `public/mod/data/db/access.php:80` |
| `mod/data:viewanyrating` | `public/mod/data/db/access.php:91` |
| `mod/data:viewallratings` | `public/mod/data/db/access.php:104` |
| `mod/data:approve` | `public/mod/data/db/access.php:117` |
| `mod/data:manageentries` | `public/mod/data/db/access.php:130` |
| `mod/data:managetemplates` | `public/mod/data/db/access.php:143` |
| `mod/data:viewalluserpresets` | `public/mod/data/db/access.php:155` |
| `mod/data:manageuserpresets` | `public/mod/data/db/access.php:166` |
| `mod/data:exportentry` | `public/mod/data/db/access.php:177` |
| `mod/data:exportownentry` | `public/mod/data/db/access.php:190` |
| `mod/data:exportallentries` | `public/mod/data/db/access.php:202` |
| `mod/data:exportuserinfo` | `public/mod/data/db/access.php:215` |
| `mod/data:view` | `public/mod/data/db/access.php:228` |
| `mod/data:comment` | `public/mod/data/db/access.php:242` |
| `mod/data:managecomments` | `public/mod/data/db/access.php:243` |
| `mod/resource:view` | `public/mod/resource/db/access.php:28` |
| `mod/resource:addinstance` | `public/mod/resource/db/access.php:37` |
| `mod/resource:portfolioexport` | `public/mod/resource/db/access.php:50` |
| `mod/lesson:addinstance` | `public/mod/lesson/db/access.php:31` |
| `mod/lesson:edit` | `public/mod/lesson/db/access.php:43` |
| `mod/lesson:grade` | `public/mod/lesson/db/access.php:56` |
| `mod/lesson:viewreports` | `public/mod/lesson/db/access.php:68` |
| `mod/lesson:manage` | `public/mod/lesson/db/access.php:79` |
| `mod/lesson:manageoverrides` | `public/mod/lesson/db/access.php:91` |
| `mod/lesson:view` | `public/mod/lesson/db/access.php:100` |
| `mod/glossary:addinstance` | `public/mod/glossary/db/access.php:29` |
| `mod/glossary:view` | `public/mod/glossary/db/access.php:41` |
| `mod/glossary:write` | `public/mod/glossary/db/access.php:55` |
| `mod/glossary:manageentries` | `public/mod/glossary/db/access.php:69` |
| `mod/glossary:managecategories` | `public/mod/glossary/db/access.php:82` |
| `mod/glossary:comment` | `public/mod/glossary/db/access.php:95` |
| `mod/glossary:managecomments` | `public/mod/glossary/db/access.php:109` |
| `mod/glossary:import` | `public/mod/glossary/db/access.php:122` |
| `mod/glossary:export` | `public/mod/glossary/db/access.php:135` |
| `mod/glossary:approve` | `public/mod/glossary/db/access.php:146` |
| `mod/glossary:rate` | `public/mod/glossary/db/access.php:159` |
| `mod/glossary:viewrating` | `public/mod/glossary/db/access.php:170` |
| `mod/glossary:viewanyrating` | `public/mod/glossary/db/access.php:181` |
| `mod/glossary:viewallratings` | `public/mod/glossary/db/access.php:194` |
| `mod/glossary:exportentry` | `public/mod/glossary/db/access.php:207` |
| `mod/glossary:exportownentry` | `public/mod/glossary/db/access.php:220` |
| `mod/book:addinstance` | `public/mod/book/db/access.php:29` |
| `mod/book:read` | `public/mod/book/db/access.php:41` |
| `mod/book:viewhiddenchapters` | `public/mod/book/db/access.php:54` |
| `mod/book:edit` | `public/mod/book/db/access.php:64` |
| `booktool/exportimscp:export` | `public/mod/book/tool/exportimscp/db/access.php:28` |
| `booktool/print:print` | `public/mod/book/tool/print/db/access.php:28` |
| `booktool/importhtml:import` | `public/mod/book/tool/importhtml/db/access.php:28` |
| `mod/h5pactivity:view` | `public/mod/h5pactivity/db/access.php:30` |
| `mod/h5pactivity:addinstance` | `public/mod/h5pactivity/db/access.php:42` |
| `mod/h5pactivity:submit` | `public/mod/h5pactivity/db/access.php:52` |
| `mod/h5pactivity:reviewattempts` | `public/mod/h5pactivity/db/access.php:60` |
| `mod/assign:view` | `public/mod/assign/db/access.php:27` |
| `mod/assign:submit` | `public/mod/assign/db/access.php:40` |
| `mod/assign:grade` | `public/mod/assign/db/access.php:49` |
| `mod/assign:exportownsubmission` | `public/mod/assign/db/access.php:61` |
| `mod/assign:addinstance` | `public/mod/assign/db/access.php:73` |
| `mod/assign:editothersubmission` | `public/mod/assign/db/access.php:85` |
| `mod/assign:grantextension` | `public/mod/assign/db/access.php:91` |
| `mod/assign:revealidentities` | `public/mod/assign/db/access.php:102` |

## 3. 服务注册样例

| 服务 | 位置 |
|---|---|
| `mod_data_get_databases_by_courses` | `public/mod/data/db/services.php:29` |
| `mod_data_view_database` | `public/mod/data/db/services.php:38` |
| `mod_data_get_data_access_information` | `public/mod/data/db/services.php:46` |
| `mod_data_get_entries` | `public/mod/data/db/services.php:54` |
| `mod_data_get_entry` | `public/mod/data/db/services.php:62` |
| `mod_data_get_fields` | `public/mod/data/db/services.php:70` |
| `mod_data_search_entries` | `public/mod/data/db/services.php:78` |
| `mod_data_approve_entry` | `public/mod/data/db/services.php:86` |
| `mod_data_delete_entry` | `public/mod/data/db/services.php:94` |
| `mod_data_add_entry` | `public/mod/data/db/services.php:102` |
| `mod_data_update_entry` | `public/mod/data/db/services.php:110` |
| `mod_data_delete_saved_preset` | `public/mod/data/db/services.php:118` |
| `mod_data_get_mapping_information` | `public/mod/data/db/services.php:125` |
| `mod_resource_view_resource` | `public/mod/resource/db/services.php:31` |
| `mod_resource_get_resources_by_courses` | `public/mod/resource/db/services.php:39` |
| `mod_lesson_get_lessons_by_courses` | `public/mod/lesson/db/services.php:30` |
| `mod_lesson_get_lesson_access_information` | `public/mod/lesson/db/services.php:39` |
| `mod_lesson_view_lesson` | `public/mod/lesson/db/services.php:47` |
| `mod_lesson_get_questions_attempts` | `public/mod/lesson/db/services.php:55` |
| `mod_lesson_get_user_grade` | `public/mod/lesson/db/services.php:63` |
| `mod_lesson_get_user_attempt_grade` | `public/mod/lesson/db/services.php:71` |
| `mod_lesson_get_content_pages_viewed` | `public/mod/lesson/db/services.php:79` |
| `mod_lesson_get_user_timers` | `public/mod/lesson/db/services.php:87` |
| `mod_lesson_get_pages` | `public/mod/lesson/db/services.php:95` |
| `mod_lesson_launch_attempt` | `public/mod/lesson/db/services.php:103` |
| `mod_lesson_get_page_data` | `public/mod/lesson/db/services.php:111` |
| `mod_lesson_process_page` | `public/mod/lesson/db/services.php:119` |
| `mod_lesson_finish_attempt` | `public/mod/lesson/db/services.php:127` |
| `mod_lesson_get_attempts_overview` | `public/mod/lesson/db/services.php:135` |
| `mod_lesson_get_user_attempt` | `public/mod/lesson/db/services.php:143` |
| `mod_lesson_get_pages_possible_jumps` | `public/mod/lesson/db/services.php:151` |
| `mod_lesson_get_lesson` | `public/mod/lesson/db/services.php:159` |
| `mod_glossary_get_glossaries_by_courses` | `public/mod/glossary/db/services.php:30` |
| `mod_glossary_view_glossary` | `public/mod/glossary/db/services.php:39` |
| `mod_glossary_view_entry` | `public/mod/glossary/db/services.php:48` |
| `mod_glossary_get_entries_by_letter` | `public/mod/glossary/db/services.php:58` |
| `mod_glossary_get_entries_by_date` | `public/mod/glossary/db/services.php:67` |
| `mod_glossary_get_categories` | `public/mod/glossary/db/services.php:76` |
| `mod_glossary_get_entries_by_category` | `public/mod/glossary/db/services.php:85` |
| `mod_glossary_get_authors` | `public/mod/glossary/db/services.php:94` |
| `mod_glossary_get_entries_by_author` | `public/mod/glossary/db/services.php:103` |
| `mod_glossary_get_entries_by_author_id` | `public/mod/glossary/db/services.php:112` |
| `mod_glossary_get_entries_by_search` | `public/mod/glossary/db/services.php:121` |
| `mod_glossary_get_entries_by_term` | `public/mod/glossary/db/services.php:130` |
| `mod_glossary_get_entries_to_approve` | `public/mod/glossary/db/services.php:139` |
| `mod_glossary_get_entry_by_id` | `public/mod/glossary/db/services.php:148` |
| `mod_glossary_add_entry` | `public/mod/glossary/db/services.php:158` |
| `mod_book_view_book` | `public/mod/book/db/services.php:31` |
| `mod_book_get_books_by_courses` | `public/mod/book/db/services.php:40` |
| `mod_assign_copy_previous_attempt` | `public/mod/assign/db/services.php:28` |
| `mod_assign_get_grades` | `public/mod/assign/db/services.php:37` |
| `mod_assign_get_assignments` | `public/mod/assign/db/services.php:46` |
| `mod_assign_get_submissions` | `public/mod/assign/db/services.php:55` |
| `mod_assign_get_user_flags` | `public/mod/assign/db/services.php:64` |
| `mod_assign_set_user_flags` | `public/mod/assign/db/services.php:73` |
| `mod_assign_get_user_mappings` | `public/mod/assign/db/services.php:83` |
| `mod_assign_revert_submissions_to_draft` | `public/mod/assign/db/services.php:92` |
| `mod_assign_lock_submissions` | `public/mod/assign/db/services.php:101` |
| `mod_assign_unlock_submissions` | `public/mod/assign/db/services.php:110` |
| `mod_assign_save_submission` | `public/mod/assign/db/services.php:119` |
| `mod_assign_submit_for_grading` | `public/mod/assign/db/services.php:128` |
| `mod_assign_save_grade` | `public/mod/assign/db/services.php:137` |
| `mod_assign_save_grades` | `public/mod/assign/db/services.php:146` |
| `mod_assign_save_user_extensions` | `public/mod/assign/db/services.php:155` |
| `mod_assign_reveal_identities` | `public/mod/assign/db/services.php:164` |
| `mod_assign_view_grading_table` | `public/mod/assign/db/services.php:173` |
| `mod_assign_view_submission_status` | `public/mod/assign/db/services.php:183` |
| `mod_assign_get_submission_status` | `public/mod/assign/db/services.php:193` |
| `mod_assign_list_participants` | `public/mod/assign/db/services.php:203` |
| `mod_assign_submit_grading_form` | `public/mod/assign/db/services.php:214` |
| `mod_assign_get_participant` | `public/mod/assign/db/services.php:224` |
| `mod_assign_view_assign` | `public/mod/assign/db/services.php:234` |
| `mod_lti_get_tool_launch_data` | `public/mod/lti/db/services.php:31` |
| `mod_lti_get_ltis_by_courses` | `public/mod/lti/db/services.php:40` |
| `mod_lti_view_lti` | `public/mod/lti/db/services.php:50` |
| `mod_lti_get_tool_proxies` | `public/mod/lti/db/services.php:59` |
| `mod_lti_create_tool_proxy` | `public/mod/lti/db/services.php:68` |
| `mod_lti_delete_tool_proxy` | `public/mod/lti/db/services.php:77` |
| `mod_lti_get_tool_proxy_registration_request` | `public/mod/lti/db/services.php:86` |
| `mod_lti_get_tool_types` | `public/mod/lti/db/services.php:95` |

## 4. 关键调用机制

| 机制 | 样例位置 | 说明 |
|---|---|---|
| `require_login` | `public/editmode.php:37` | 页面入口要求登录 |
| `has_capability` | `public/index.php:58` | 在上下文中判断能力 |
| `context_course::instance` | `public/course/info.php:50` | 课程上下文 |
| `context_system::instance` | `public/index.php:58` | 系统上下文 |
| `get_file_storage` | `public/file.php:82` | 文件池访问 |
| `get_fast_modinfo` | `public/index.php:134` | 课程模块信息缓存 |
