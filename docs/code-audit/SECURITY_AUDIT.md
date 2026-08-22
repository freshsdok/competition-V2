# 安全审计

## 高风险发现

| ID | 严重级别 | 问题 | 位置 | 建议 |
| --- | --- | --- | --- | --- |
| F-001 | P1 | 现场“一证多权”存在 pilot confirm 路由 | old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java:25 | 将 pilot 路由纳入环境开关、权限注解和网关白名单审查；上线前确认是否下线。 |
| F-002 | P1 | 前端多处使用 v-html 渲染服务端内容 | old-code-admin/src/views/content/detailPage/index.vue:41 | 只允许已净化字段进入 v-html；统一封装富文本组件并标记 sanitized 来源。 |
| F-003 | P1 | 小程序和管理端保留大量 console.log | old-code-admin/src/components/Editor/index.vue:127 | 上线构建移除 console；对敏感响应启用脱敏日志。 |
| F-004 | P1 | 文件上传后端类型校验需要复核 | old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/service/OSSFileServiceImpl.java:69 | 在后端统一校验扩展名、MIME、大小和业务场景白名单；zip 新增时同步病毒/压缩炸弹策略。 |
| F-011 | P2 | 前端将上传/下载能力挂到 window 或全局属性 | old-code-pc/src/main.js:38 | 改为模块化 import 或组合式 hooks；下载前后端均校验文件归属。 |
| F-012 | P2 | 配置文件中出现密码/密钥类配置项 | old-code/docker/docker-compose.yml:40 | 迁移到环境变量/密钥管理；示例配置用占位符。 |
| F-026 | P2 | 权限策略横跨网关、认证、控制器注解 | old-code/teaching-gateway, old-code/teaching-auth, controllers | 为 Controller 生成权限清单并与 sys_menu migration 对账。 |
| F-034 | P3 | 客户端 token 刷新和错误处理分散 | old-code-mini/utils/request.js, old-code-admin/src/utils/request.js | 统一 token 失效码、刷新策略和退出流程。 |

## 前端 XSS 与日志

| 类型 | 位置 | 代码 |
| --- | --- | --- |
| v-html | old-code-admin/src/views/content/detailPage/index.vue:41 | <div class="detail-content" v-html="detailData.detailContent"></div> |
| v-html | old-code-admin/src/views/course/courseInfo/index.vue:147 | <div v-html="scope.row.details" style="max-height: 50px; overflow: hidden; text-overflow: ellipsis;"></div> |
| v-html | old-code-admin/src/views/tournament/promote/index.vue:148 | v-html="scope.row.promotedHint" |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:22 | <div v-if="scope.row.changeType === 'changeTeacher'" v-html="highlightDiff(scope.row.teacherNameOld, scope.row.teacherNameNew)"></div> |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:23 | <div v-else v-html="highlightDiff(scope.row.memberNameOld, scope.row.memberNameNew)"></div> |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:28 | <div v-if="scope.row.changeType === 'changeTeacher'" v-html="highlightDiff(scope.row.teacherNameNew, scope.row.teacherNameOld)"></div> |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:29 | <div v-else v-html="highlightDiff(scope.row.memberNameNew, scope.row.memberNameOld)"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_list_one.vue:79 | <div class="content-desc text-ellipsis rich-content" v-html="item.competitionDesc"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_support_home.vue:32 | <div class="ml-[15px]" v-html="highlightKeyword(item.questions, keyWord)"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_support_home.vue:35 | <div class="text-[#666666] text-[16px]" v-html="highlightKeyword(item.answer, keyWord)"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_tournament_home.vue:33 | <div class="w-full rich-content tur-content-right-desc" v-html="tourDetail?.competitionDesc \\|\\| ''"></div> |
| v-html | old-code-pc/src/views/awardPublicity/index.vue:45 | <div class="rich-content ql-editor" v-html="currentCompetition?.tipInfo \\|\\| ''"> |
| console.log | old-code-admin/src/components/Editor/index.vue:127 | console.log('图片上传服务端返回：', res); |
| console.log | old-code-admin/src/components/Editor/index.vue:157 | console.log('视频上传服务端返回：', res); |
| console.log | old-code-admin/src/components/Editor/index.vue:216 | console.log('focus', editor); |
| console.log | old-code-admin/src/components/Editor/index.vue:219 | console.log('blur', editor); |
| console.log | old-code-admin/src/components/Editor/index.vue:225 | console.log('ClipboardEvent 粘贴事件对象', event); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:180 | console.log('Video Audit - props.form:', JSON.stringify(newVal, null, 2)); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:195 | console.log('Found courseChapterVideos at top level'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:201 | console.log('Found courseChapterVideos in businessDetail'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:209 | console.log('Found chapterVideoList in businessDetail'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:212 | console.log('Found videoList in businessDetail'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:218 | console.log('Found chapterVideoList in form'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:222 | console.log('Found videoList in form'); |

## 配置敏感信息扫描

| 位置 | 命中内容 |
| --- | --- |
| db/competition_scene_credential_resource_merged_20260705.sql:5 | -- Target: MySQL 5.7+/8.0+, utf8mb4 |
| db/competition_scene_credential_resource_merged_20260705.sql:22 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/competition_scene_credential_resource_merged_20260705.sql:287 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/competition_scene_credential_resource_merged_20260705.sql:424 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/competition_scene_credential_resource_merged_20260705.sql:457 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/competition_scene_credential_resource_merged_20260705.sql:536 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/competition_scene_credential_resource_merged_20260705.sql:564 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/competition_scene_credential_resource_merged_20260705.sql:583 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260629_competition_scene_credential.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260701_competition_scene_resource_p1_001.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260701_competition_scene_target_credential_type.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260702_competition_scene_credential_direct_issue_p2.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260702_competition_scene_credential_scope_p1.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260703_review_module_phase1.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260703_review_module_phase1.sql:325 | `secretary_user_id` bigint DEFAULT NULL COMMENT '秘书用户ID', |
| db/migration/20260703_review_module_phase1.sql:447 | `secretary_user_id` bigint DEFAULT NULL COMMENT '秘书用户ID', |
| db/migration/20260703_review_module_phase1.sql:474 | `secretary_note` varchar(500) DEFAULT NULL COMMENT '秘书备注', |
| db/migration/20260703_review_module_phase4_submission.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260703_review_module_phase5_my_review.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260703_review_module_phase6_secretary_console.sql:17 | select '秘书控制台查询', @review_session_id, 10, '', '', '1', '0', 'F', '0', '0', 'competition:review:secretary:query', '#', 'system', 'admin', now() |
| db/migration/20260703_review_module_phase6_secretary_console.sql:19 | and not exists (select 1 from sys_menu where perms = 'competition:review:secretary:query' and platform_type = 'admin'); |
| db/migration/20260703_review_module_phase6_secretary_console.sql:22 | select '秘书控制台操作', @review_session_id, 11, '', '', '1', '0', 'F', '0', '0', 'competition:review:secretary:edit', '#', 'system', 'admin', now() |
| db/migration/20260703_review_module_phase6_secretary_console.sql:24 | and not exists (select 1 from sys_menu where perms = 'competition:review:secretary:edit' and platform_type = 'admin'); |
| db/migration/20260703_review_module_phase6_secretary_console.sql:34 | 'competition:review:secretary:query', |
| db/migration/20260703_review_module_phase6_secretary_console.sql:35 | 'competition:review:secretary:edit' |
| db/migration/20260705_competition_scene_credential_active_core_key_p2.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260706_review_module_phase7_result_publish.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |
| db/migration/20260707_review_module_scoring_rule_enhancement.sql:2 | -- Target database: MySQL 5.7+/8.0+, charset utf8mb4. |

## 安全建议优先级
1. 文件上传后端强制白名单与 zip 风险策略。
2. pilot/调试接口环境隔离。
3. v-html 统一净化证明。
4. 客户端调试日志清理。
5. 配置密钥迁移到环境变量或密钥管理。
