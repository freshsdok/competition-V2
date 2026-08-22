# 按业务域反向功能需求

## 系统用户与权限

**业务含义**：用户登录注册、token 刷新、角色/菜单/权限、组织部门、字典、用户组、实名认证和个人中心。

**反向功能需求**：
- 用户可登录、登出、刷新 token、注册账号。 `CONFIRMED_BY_CODE`
- 管理员可维护用户、角色、菜单、部门、岗位、字典和用户组。 `CONFIRMED_BY_CODE`
- 系统可为业务模块提供内部用户、角色、学校和认证信息查询。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| AuthInfo |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/AuthInfo.java:80` |
| CertConfigInfo |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CertConfigInfo.java:80` |
| ChangeLog |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/ChangeLog.java:80` |
| ChapterAuditResult |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/ChapterAuditResult.java:36` |
| CompetitionAwardsConfig |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionAwardsConfig.java:80` |
| CompetitionCertExchangeApply |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionCertExchangeApply.java:80` |
| CompetitionConfig |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionConfig.java:80` |
| CompetitionCourseConfig |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionCourseConfig.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /system/task | `old-code-admin/src/api/business/index.js:11` |
| /system/user/group/list | `old-code-admin/src/api/certInterconnect/certConfig.js:67` |
| /competition/competition/competitionCertExchangeApply/list | `old-code-admin/src/api/certInterconnect/userCertInterconnect.js:6` |
| /competition/competition/competitionCertExchangeApply/export | `old-code-admin/src/api/certInterconnect/userCertInterconnect.js:15` |
| /content/contentColumn/getByMenuId/ | `old-code-admin/src/api/content/column.js:23` |
| /system/user/group/list | `old-code-admin/src/api/fileTask/index.js:6` |
| /competition/competitionManager/selectAllCompetitionDetailInfoForUserGroup | `old-code-admin/src/api/fileTask/index.js:15` |
| /system/userGroup | `old-code-admin/src/api/fileTask/index.js:24` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| review_activity_user_role | `db/migration/20260703_review_module_phase1.sql:299` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 内容管理

**业务含义**：页面、新闻、公告、Banner、内容文件和富文本展示。

**反向功能需求**：
- 管理员可维护页面、新闻、公告、Banner 和文件内容。 `CONFIRMED_BY_CODE`
- PC/小程序可查看公开资讯、公告和详情。 `CONFIRMED_BY_CODE`
- 内容审核状态可通过内部接口被流程模块更新。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionSceneNoticeController | /sceneNotice, /competition/sceneNotice | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:80` |
| CompetitionTitleNoticeController | /competition/competitionTitleNotice | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionTitleNoticeController.java:80` |
| ComponentLibraryInfoController | /subassembly | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/controller/ComponentLibraryInfoController.java:80` |
| ContentBannerInfoController | /bannerInfo | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/controller/ContentBannerInfoController.java:80` |
| ContentColumnController | /contentColumn | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/controller/ContentColumnController.java:80` |
| ContentDetailController | /contentDetail | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/controller/ContentDetailController.java:80` |
| ContentFileController | /contentFile | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/controller/ContentFileController.java:80` |
| DataSourceInfoController | /source | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/controller/DataSourceInfoController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /content/bannerInfo/list | `old-code-admin/src/api/content/bannerInfo.js:6` |
| /content/bannerInfo/ | `old-code-admin/src/api/content/bannerInfo.js:15` |
| /content/bannerInfo | `old-code-admin/src/api/content/bannerInfo.js:23` |
| /content/bannerInfo | `old-code-admin/src/api/content/bannerInfo.js:32` |
| /content/bannerInfo/ | `old-code-admin/src/api/content/bannerInfo.js:41` |
| /content/contentColumn/list | `old-code-admin/src/api/content/column.js:6` |
| /content/contentColumn/ | `old-code-admin/src/api/content/column.js:15` |
| /content/contentColumn/tree | `old-code-admin/src/api/content/column.js:31` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| competition_scene_notice | `db/migration/20260710_competition_scene_notice.sql:6` |
| competition_scene_notice_schedule | `db/migration/20260710_competition_scene_notice.sql:39` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 赛事主数据

**业务含义**：赛事、赛项/赛道、阶段、赛事配置、证书互通规则和奖项公示基础配置。

**反向功能需求**：
- 管理员可创建和维护赛事、赛项/赛道、阶段、规则和报名配置。 `CONFIRMED_BY_CODE`
- 系统可查询进行中赛事、赛事详情和下拉列表。 `CONFIRMED_BY_CODE`
- 支持奖项、公示、晋级配置及赛事状态变更。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionMainInfoController | /competitionManager | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:80` |
| CompetitionTrackConfigController | /competitionTrackConfig | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionTrackConfigController.java:80` |
| CompetitionTrackInfoController | /competitionTrackInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionTrackInfoController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/competitionManager/selectAllCompetitionDetailInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:58` |
| /competition/competitionManager/list | `old-code-admin/src/api/tournament/competition.js:6` |
| /competition/competitionManager/getCompetitionDetailInfoById | `old-code-admin/src/api/tournament/competition.js:15` |
| /competition/competitionManager/queryNowCompetitionStageConfig | `old-code-admin/src/api/tournament/competition.js:23` |
| /competition/competitionManager/saveCompetitionInfo | `old-code-admin/src/api/tournament/competition.js:32` |
| /competition/competitionManager/updateCompetitionInfo | `old-code-admin/src/api/tournament/competition.js:41` |
| /competition/competitionManager/removeCompetitionMainInfo | `old-code-admin/src/api/tournament/competition.js:50` |
| /competition/competitionManager/updateCompetitionInfoStatus | `old-code-admin/src/api/tournament/competition.js:58` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 报名与团队

**业务含义**：用户报名、团队创建、成员维护、报名审核、晋级报名、补缴/变更和导出。

**反向功能需求**：
- 用户可报名赛事、创建团队、维护成员和指导教师。 `CONFIRMED_BY_CODE`
- 管理员可审核报名与团队信息、导入/导出报名数据。 `CONFIRMED_BY_CODE`
- 系统按 team_code、member_id、competition_series_id 关联报名、团队和支付。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionApplyInfo |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionApplyInfo.java:80` |
| CompetitionApplyInfoExcel |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionApplyInfoExcel.java:54` |
| CompetitionApplyInfoVO |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionApplyInfoVO.java:80` |
| TeamManagerInfo |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/TeamManagerInfo.java:80` |
| TeamManagerInfoAwardsInfo |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/TeamManagerInfoAwardsInfo.java:80` |
| TeamMemberRela |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/TeamMemberRela.java:80` |
| CompetitionApplyInfoController | /competitionApply | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:80` |
| CompetitionCertExchangeApplyController | /competition/competitionCertExchangeApply | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/competition/certConfigInfo/cert/getCompetitionApplyInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:89` |
| /competition/promotedApplyInfo/list | `old-code-admin/src/api/tournament/promote.js:43` |
| /competition/promotedApplyInfo | `old-code-admin/src/api/tournament/promote.js:60` |
| /competition/promotedApplyInfo/export | `old-code-admin/src/api/tournament/promote.js:69` |
| /competition/promotedApplyInfo/${competitionSeriesId}/${teamCodes} | `old-code-admin/src/api/tournament/promote.js:78` |
| /competition/competitionApply/list | `old-code-admin/src/api/tournament/tournament.js:6` |
| /competition/teamManager/list | `old-code-admin/src/api/tournament/tournament.js:17` |
| /competition/teamManager/updateTeamManagerInfo | `old-code-admin/src/api/tournament/tournament.js:27` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 赛场安排

**业务含义**：赛程、赛场对象 target、队伍/个人/评审对象导入、排序、同步评审场次。

**反向功能需求**：
- 管理员可维护赛场日程和赛场对象 target。 `CONFIRMED_BY_CODE`
- 支持按队伍、个人、评审对象、手工方式生成 target。 `CONFIRMED_BY_CODE`
- 支持 target 排序、自动编号、同步评审场次。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionSceneScheduleController | /sceneSchedule, /competition/sceneSchedule, /scene/schedule, /competition/scene/schedule | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:80` |
| CompetitionSceneScheduleResourceController | /sceneScheduleResource, /competition/sceneScheduleResource | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleResourceController.java:78` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/sceneSchedule/list | `old-code-admin/src/api/tournament/sceneSchedule.js:7` |
| /competition/sceneSchedule/${scheduleId} | `old-code-admin/src/api/tournament/sceneSchedule.js:15` |
| /competition/sceneSchedule | `old-code-admin/src/api/tournament/sceneSchedule.js:22` |
| /competition/sceneSchedule | `old-code-admin/src/api/tournament/sceneSchedule.js:30` |
| /competition/sceneSchedule/${scheduleIds} | `old-code-admin/src/api/tournament/sceneSchedule.js:38` |
| /competition/sceneSchedule/match/${scheduleId} | `old-code-admin/src/api/tournament/sceneSchedule.js:45` |
| /competition/sceneSchedule/target/list | `old-code-admin/src/api/tournament/sceneSchedule.js:54` |
| /competition/sceneSchedule/target | `old-code-admin/src/api/tournament/sceneSchedule.js:62` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| competition_scene_schedule_target | `db/competition_scene_credential_resource_merged_20260705.sql:70` |
| competition_scene_schedule_target | `db/migration/20260629_competition_scene_credential.sql:51` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 现场证件

**业务含义**：现场证件生成、直接发证、证件列表、证件详情、教师查看学生证件。

**反向功能需求**：
- 管理员可按赛事/赛程生成、查询、直接发放现场证件。 `CONFIRMED_BY_CODE`
- 用户/教师可查看相关证件信息。 `CONFIRMED_BY_CODE`
- 证件保留姓名、学校、组别、角色等现场快照。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CandidateCertInfoController | /competition/candidateCertInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:80` |
| CertConfigInfoController | /competition/certConfigInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:80` |
| CertExchangeRuleDetailController | /competition/certExchangeRuleDetail | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:80` |
| CertOrgInfoController | /competition/certOrgInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:80` |
| CertPlayerInfoController | /competition/certPlayerInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:80` |
| CompetitionCertExchangeRuleController | /competition/competitionCertExchangeRule | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:80` |
| CompetitionSceneCredentialController | /sceneCredential, /competition/sceneCredential | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:80` |
| CompetitionSceneOneCardIssueController | /sceneOneCardIssue, /competition/sceneOneCardIssue | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardIssueController.java:41` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/competition/certConfigInfo/list | `old-code-admin/src/api/certInterconnect/certConfig.js:6` |
| /competition/competition/certConfigInfo/getCertConfigInfo/${id} | `old-code-admin/src/api/certInterconnect/certConfig.js:15` |
| /competition/competition/certConfigInfo/addCertConfigInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:23` |
| /competition/competition/certConfigInfo/updateCertConfigInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:32` |
| /competition/competition/certConfigInfo/${ids} | `old-code-admin/src/api/certInterconnect/certConfig.js:41` |
| /competition/competition/certConfigInfo/export | `old-code-admin/src/api/certInterconnect/certConfig.js:49` |
| /competition/competition/certOrgInfo/list | `old-code-admin/src/api/certInterconnect/certConfig.js:78` |
| /competition/competition/candidateCertInfo/list | `old-code-admin/src/api/certInterconnect/certConfig.js:99` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| competition_scene_schedule | `db/competition_scene_credential_resource_merged_20260705.sql:27` |
| competition_scene_credential | `db/competition_scene_credential_resource_merged_20260705.sql:124` |
| competition_scene_operation_log | `db/competition_scene_credential_resource_merged_20260705.sql:223` |
| competition_scene_resource | `db/competition_scene_credential_resource_merged_20260705.sql:291` |
| competition_scene_schedule_resource | `db/competition_scene_credential_resource_merged_20260705.sql:323` |
| competition_scene_resource_slot | `db/competition_scene_credential_resource_merged_20260705.sql:356` |
| competition_scene_resource_reservation | `db/competition_scene_credential_resource_merged_20260705.sql:384` |
| competition_scene_subject_operation_state | `db/competition_scene_credential_resource_merged_20260705.sql:495` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 一证多权 grant

**业务含义**：按证件、日程、能力授予现场操作权限，支持 active grant 查询和撤销。

**反向功能需求**：
- 系统可为证件授予指定日程/能力范围的 active grant。 `CONFIRMED_BY_CODE`
- 扫码确认时需校验证件是否具备对应能力。 `STATIC_INFERENCE`
- 支持授权撤销或失效。 `CONFIRMED_BY_CODE`

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 现场 operation_state

**业务含义**：记录某主体某日程某操作的当前完成状态，支撑扫码核验和重复操作判断。

**反向功能需求**：
- 系统记录现场对象某项操作是否已完成。 `CONFIRMED_BY_CODE`
- 扫码确认需避免重复 DONE。 `CONFIRMED_BY_CODE`
- 该状态应作为当前事实源，区别于日志流水。 `STATIC_INFERENCE`

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 现场 operation_log

**业务含义**：记录扫码/确认/核验/操作流水，用于后台查询和审计追踪。

**反向功能需求**：
- 系统记录扫码、确认、撤销等操作流水。 `CONFIRMED_BY_CODE`
- 后台可按条件查看现场操作日志。 `CONFIRMED_BY_CODE`
- 日志用于审计，不应替代当前状态判断。 `STATIC_INFERENCE`

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /schedule/job/log/list | `old-code-admin/src/api/monitor/jobLog.js:6` |
| /monitor/operlog/list | `old-code-admin/src/api/monitor/operlog.js:6` |
| /competition/review/session/event-log/list | `old-code-admin/src/api/review/session.js:89` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 资源台账

**业务含义**：资源基本信息维护，如设备、工位或现场可预约资源。

**反向功能需求**：
- 管理员可维护可预约资源基础信息。 `CONFIRMED_BY_CODE`
- 资源可被部署到赛程。 `CONFIRMED_BY_CODE`
- 资源状态影响是否可预约。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionSceneResourceController | /sceneResource, /competition/sceneResource | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:77` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/sceneResource/list | `old-code-admin/src/api/tournament/sceneResource.js:5` |
| /competition/sceneResource/${resourceId} | `old-code-admin/src/api/tournament/sceneResource.js:13` |
| /competition/sceneResource/${resourceIds} | `old-code-admin/src/api/tournament/sceneResource.js:36` |
| /competition/sceneResource/changeStatus | `old-code-admin/src/api/tournament/sceneResource.js:43` |
| /competition/userCompetition/sceneResource/bookableList | `old-code-mini/api/sceneResource.js:5` |
| /competition/userCompetition/sceneResource/${scheduleResourceId} | `old-code-mini/api/sceneResource.js:13` |
| /competition/userCompetition/sceneResource/bookableList | `old-code-pc/src/api/personal/sceneResource.js:5` |
| /competition/userCompetition/sceneResource/${scheduleResourceId} | `old-code-pc/src/api/personal/sceneResource.js:13` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 资源部署

**业务含义**：把资源部署到具体赛程，控制 booking_status 和开放范围。

**反向功能需求**：
- 管理员可将资源绑定到赛程并配置开放状态。 `CONFIRMED_BY_CODE`
- 支持 schedule scope 和 group scope 限定。 `CONFIRMED_BY_CODE`
- 后台可查看赛程资源与预约情况。 `CONFIRMED_BY_CODE`

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 资源时段

**业务含义**：维护资源可预约时段、容量、状态、组别范围。

**反向功能需求**：
- 管理员可维护资源时段、容量和开放状态。 `CONFIRMED_BY_CODE`
- 时段可批量创建、修改、删除和改状态。 `CONFIRMED_BY_CODE`
- 时段容量影响预约创建。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionSceneResourceSlotController | /sceneResourceSlot, /competition/sceneResourceSlot | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:80` |
| CompetitionSceneResourceSlotGroupScopeController | /sceneResourceSlotGroupScope, /competition/sceneResourceSlotGroupScope | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotGroupScopeController.java:60` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/sceneResourceSlot/list | `old-code-admin/src/api/tournament/sceneResourceSlot.js:5` |
| /competition/sceneResourceSlot/${slotId} | `old-code-admin/src/api/tournament/sceneResourceSlot.js:13` |
| /competition/sceneResourceSlot | `old-code-admin/src/api/tournament/sceneResourceSlot.js:20` |
| /competition/sceneResourceSlot/batch | `old-code-admin/src/api/tournament/sceneResourceSlot.js:28` |
| /competition/sceneResourceSlot | `old-code-admin/src/api/tournament/sceneResourceSlot.js:36` |
| /competition/sceneResourceSlot/${slotIds} | `old-code-admin/src/api/tournament/sceneResourceSlot.js:44` |
| /competition/sceneResourceSlot/changeStatus | `old-code-admin/src/api/tournament/sceneResourceSlot.js:51` |
| /competition/sceneResourceSlotGroupScope/listBySlot | `old-code-admin/src/api/tournament/sceneResourceSlotGroupScope.js:5` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| competition_scene_resource_slot | `db/migration/20260701_competition_scene_resource_p1_001.sql:72` |
| competition_scene_resource_slot_group_scope | `db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql:26` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 资源预约

**业务含义**：用户端可预约列表、预约创建、我的预约、取消预约、后台预约查看。

**反向功能需求**：
- 用户可查看可预约资源、时段详情并提交预约。 `CONFIRMED_BY_CODE`
- 用户可查看我的预约并取消。 `CONFIRMED_BY_CODE`
- 后台可查询预约详情，系统需保证幂等和容量一致。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| CompetitionSceneResourceReservationController | /sceneResourceReservation, /competition/sceneResourceReservation | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceReservationController.java:42` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/sceneResourceReservation/list | `old-code-admin/src/api/tournament/sceneResourceReservation.js:5` |
| /competition/sceneResourceReservation/${reservationId} | `old-code-admin/src/api/tournament/sceneResourceReservation.js:13` |
| /competition/userCompetition/sceneResourceReservation | `old-code-mini/api/sceneResource.js:28` |
| /competition/userCompetition/sceneResourceReservation/myList | `old-code-mini/api/sceneResource.js:36` |
| /competition/userCompetition/sceneResourceReservation/cancel | `old-code-mini/api/sceneResource.js:43` |
| /competition/userCompetition/sceneResourceReservation | `old-code-pc/src/api/personal/sceneResource.js:28` |
| /competition/userCompetition/sceneResourceReservation/myList | `old-code-pc/src/api/personal/sceneResource.js:36` |
| /competition/userCompetition/sceneResourceReservation/cancel | `old-code-pc/src/api/personal/sceneResource.js:43` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| competition_scene_resource_reservation | `db/migration/20260701_competition_scene_resource_p1_001.sql:100` |
| competition_scene_resource_schedule_scope | `db/migration/20260706_competition_scene_resource_reservation_scope_group_capacity.sql:8` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 评审任务

**业务含义**：评审活动/场次/对象、专家/秘书任务、当前对象和下一个对象。

**反向功能需求**：
- 管理员可维护评审活动、对象、专家/秘书任务。 `CONFIRMED_BY_CODE`
- 秘书可查看当前场次对象、切换下一个对象、更新对象状态。 `CONFIRMED_BY_CODE`
- 专家可进入我的评审任务。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| ReviewActivityController | /review/activity | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewActivityController.java:80` |
| ReviewAssignmentController | /review/assignment | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewAssignmentController.java:76` |
| ReviewSecretaryController | /review/secretary | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewSecretaryController.java:69` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/review/activity/list | `old-code-admin/src/api/review/activity.js:5` |
| /competition/review/activity/${id} | `old-code-admin/src/api/review/activity.js:13` |
| /competition/review/activity | `old-code-admin/src/api/review/activity.js:20` |
| /competition/review/activity/${id} | `old-code-admin/src/api/review/activity.js:28` |
| /competition/review/activity/${id} | `old-code-admin/src/api/review/activity.js:36` |
| /competition/review/activity/${activityId}/close-submission | `old-code-admin/src/api/review/activity.js:43` |
| /competition/review/assignment/list | `old-code-admin/src/api/review/assignment.js:5` |
| /competition/review/assignment/${id} | `old-code-admin/src/api/review/assignment.js:13` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 评审材料

**业务含义**：参赛对象提交材料、材料预览、材料状态和秘书/专家查看。

**反向功能需求**：
- 参赛对象可提交评审材料。 `CONFIRMED_BY_CODE`
- 专家/秘书可预览材料。 `CONFIRMED_BY_CODE`
- 系统记录提交状态和材料文件快照。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| ReviewMaterialController | /review/material | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewMaterialController.java:80` |
| ReviewSubmissionController | /review/submission | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewSubmissionController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/review/object/sync-file-task-materials | `old-code-admin/src/api/review/object.js:59` |
| /competition/review/submission/my-list | `old-code-admin/src/api/review/submission.js:5` |
| /competition/review/submission/${objectId} | `old-code-admin/src/api/review/submission.js:13` |
| /competition/review/submission/${objectId}/draft | `old-code-admin/src/api/review/submission.js:20` |
| /competition/review/submission/${objectId}/material | `old-code-admin/src/api/review/submission.js:28` |
| /competition/review/submission/${objectId}/materials | `old-code-admin/src/api/review/submission.js:36` |
| /competition/review/submission/material/${materialId} | `old-code-admin/src/api/review/submission.js:43` |
| /competition/review/submission/${objectId}/submit | `old-code-admin/src/api/review/submission.js:50` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| review_object_material | `db/migration/20260703_review_module_phase1.sql:171` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 评审评分

**业务含义**：评分规则、评分记录、评审结果汇总和结果发布/查询。

**反向功能需求**：
- 管理员可配置评分规则。 `CONFIRMED_BY_CODE`
- 专家可提交评分记录。 `CONFIRMED_BY_CODE`
- 系统可汇总评审结果并查询结果状态。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| ReviewRecordController | /review/record | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewRecordController.java:58` |
| ReviewResultController | /review/result | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewResultController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/review/result/list | `old-code-admin/src/api/review/result.js:5` |
| /competition/review/result/generate | `old-code-admin/src/api/review/result.js:13` |
| /competition/review/result/${id}/conclusion | `old-code-admin/src/api/review/result.js:21` |
| /competition/review/result/${id}/publish | `old-code-admin/src/api/review/result.js:29` |
| /competition/review/result/${id}/revoke | `old-code-admin/src/api/review/result.js:37` |
| /competition/review/result/records | `old-code-admin/src/api/review/result.js:45` |
| /competition/review/result/record/${recordId}/details | `old-code-admin/src/api/review/result.js:53` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| review_score_detail | `db/migration/20260703_review_module_phase1.sql:413` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 支付

**业务含义**：订单创建、支付状态、退款、发票、补缴、支付结果同步。

**反向功能需求**：
- 系统可创建订单、查询支付状态、同步支付/退款结果。 `CONFIRMED_BY_CODE`
- 支持补缴、退款、发票状态和订单关联团队变更。 `CONFIRMED_BY_CODE`
- 支付回调和结果同步应幂等。 `STATIC_INFERENCE`

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /system/order/list | `old-code-admin/src/api/iPayment/index.js:5` |
| /system/order/${id} | `old-code-admin/src/api/iPayment/index.js:12` |
| /system/order/cancelOrder/${id} | `old-code-admin/src/api/iPayment/index.js:19` |
| /system/order/proofAudit | `old-code-admin/src/api/iPayment/index.js:26` |
| /system/invoice/list?pageNum=${pam}&pageSize=${psise} | `old-code-admin/src/api/iPayment/index.js:34` |
| /system/invoice/apply | `old-code-admin/src/api/iPayment/index.js:42` |
| /system/invoice/reInvoice | `old-code-admin/src/api/iPayment/index.js:50` |
| /system/invoice/queryInvoiceResult | `old-code-admin/src/api/iPayment/index.js:58` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 文件

**业务含义**：普通文件/OSS 上传、下载、预览、文件任务、提交记录、导出打包。

**反向功能需求**：
- 系统支持本地/OSS 上传、下载、预览、删除、导出打包。 `CONFIRMED_BY_CODE`
- 支持文件任务、用户提交、下载记录。 `CONFIRMED_BY_CODE`
- 文件类型和访问权限需后端统一控制。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| RemoteOssUploadServiceFactory |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/factory/RemoteOssUploadServiceFactory.java:58` |
| ReviewerProfileController | /review/reviewer | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewerProfileController.java:68` |
| OSSController | /oss | `old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/controller/OSSController.java:80` |
| SysFileController |  | `old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/controller/SysFileController.java:80` |
| FileController |  | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/FileController.java:38` |
| FileDownloadRecordController | /downLoadRecord | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/controller/FileDownloadRecordController.java:80` |
| FileUploadManagerController | /fileUploadManager | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/controller/FileUploadManagerController.java:80` |
| FileUploadRecordController | /fileUploadRecord | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/controller/FileUploadRecordController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /file/upload | `old-code-admin/src/api/content/file.js:65` |
| /file/uploadVideo | `old-code-admin/src/api/course/chapterVideo.js:96` |
| /system/fileUploadRecord/list | `old-code-admin/src/api/fileTask/index.js:67` |
| /system/fileUploadManager/list | `old-code-admin/src/api/fileTask/index.js:76` |
| /system/fileUploadRecord/exportFiles | `old-code-admin/src/api/fileTask/index.js:85` |
| /system/fileUploadRecord/export | `old-code-admin/src/api/fileTask/index.js:93` |
| /system/fileUploadManager/export | `old-code-admin/src/api/fileTask/index.js:102` |
| /system/fileUploadManager/exportFiles | `old-code-admin/src/api/fileTask/index.js:111` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| reviewer_profile | `db/migration/20260703_review_module_phase1.sql:274` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 导入中间表

**业务含义**：报名、晋级、奖项、赛场对象、证件等 Excel 导入和临时数据承接。

**反向功能需求**：
- 系统支持 Excel 导入赛事、报名、晋级、奖项、赛场对象等数据。 `CONFIRMED_BY_CODE`
- 导入数据需要保留原始文本和错误信息。 `CONFIRMED_BY_CODE`
- 导入结果进入正式表前应可校验和回滚。 `STATIC_INFERENCE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| FileReviewImportMaterial |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/FileReviewImportMaterial.java:50` |
| FileReviewImportSource |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/FileReviewImportSource.java:80` |
| RemoteFileReviewImportFallbackFactory |  | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/factory/RemoteFileReviewImportFallbackFactory.java:35` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /competition/competition/candidateCertInfo/importCandidateCertInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:125` |
| /competition/competition/competitionGradeInfo/importGradeInfo | `old-code-admin/src/api/certInterconnect/gradesManagement.js:42` |
| /competition/review/object/import-preview | `old-code-admin/src/api/review/object.js:43` |
| /competition/review/object/import-from-business | `old-code-admin/src/api/review/object.js:51` |
| /code/gen/importTable | `old-code-admin/src/api/tool/gen.js:40` |
| /competition/publicity/importData | `old-code-admin/src/api/tournament/awardPublicity.js:25` |
| /competition/promotedApplyInfo/import | `old-code-admin/src/api/tournament/promote.js:24` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 流程审批

**业务含义**：Flowable 流程发起、待办、任务详情、流程模型和审批结果。

**反向功能需求**：
- 系统支持发起流程、查看待办/详情、流程模型管理。 `CONFIRMED_BY_CODE`
- 业务对象可进入审核流。 `CONFIRMED_BY_CODE`
- 流程状态反写业务状态。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| OperationFlowController | /flow | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/OperationFlowController.java:80` |
| WfCategoryController | /category | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfCategoryController.java:80` |
| WfDeployController | /deploy | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfDeployController.java:80` |
| WfFormController | /form | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfFormController.java:80` |
| WfInstanceController | /workflow/instance | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfInstanceController.java:71` |
| WfModelController | /model | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfModelController.java:80` |
| WfProcessController | /process | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfProcessController.java:80` |
| WfTaskController | /task | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/controller/WfTaskController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /system/task/list | `old-code-admin/src/api/system/process.js:61` |
| /system/task/finish | `old-code-admin/src/api/system/process.js:69` |
| /system/task/${params} | `old-code-admin/src/api/system/process.js:78` |
| /system/task/audit | `old-code-admin/src/api/system/process.js:85` |
| /system/task/videoAudit | `old-code-admin/src/api/system/process.js:93` |
| /system/task/pic/ | `old-code-admin/src/api/system/process.js:151` |
| /system/task/audits | `old-code-admin/src/api/system/process.js:158` |
| /system/reviewTaskInfo/getTaskInfoByProcessedId | `old-code-admin/src/api/tournament/reviewManage.js:170` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 扫码/小程序

**业务含义**：微信登录、扫码签到/核验、二维码配置、签到记录、小程序端现场能力。

**反向功能需求**：
- 小程序支持微信登录、扫码、签到/核验、查看现场通知和资源预约。 `CONFIRMED_BY_CODE`
- 二维码配置和扫码记录由后台维护。 `CONFIRMED_BY_CODE`
- 扫码失败支持重试和 pending 处理。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| WxAuthController | /wxAuth | `old-code/teaching-modules/teaching-wxApp/src/main/java/com/teaching/wxApp/controller/WxAuthController.java:80` |
| WxCleanRedisKeyController | /wxClean | `old-code/teaching-modules/teaching-wxApp/src/main/java/com/teaching/wxApp/controller/WxCleanRedisKeyController.java:39` |
| WxQcCodeConfigController | /wxQcCodeConfig | `old-code/teaching-modules/teaching-wxApp/src/main/java/com/teaching/wxApp/controller/WxQcCodeConfigController.java:80` |
| WxQcCodeRecordController | /wxQcCodeRecord | `old-code/teaching-modules/teaching-wxApp/src/main/java/com/teaching/wxApp/controller/WxQcCodeRecordController.java:80` |
| WxQcCodeRecordUserController | /wxQcCodeRecordUser | `old-code/teaching-modules/teaching-wxApp/src/main/java/com/teaching/wxApp/controller/WxQcCodeRecordUserController.java:59` |
| WxSignInInfoController | /wxSignInInfo | `old-code/teaching-modules/teaching-wxApp/src/main/java/com/teaching/wxApp/controller/WxSignInInfoController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /wxApp/wxSignInInfo/list | `old-code-admin/src/api/tournament/competition.js:188` |
| /wxApp/wxSignInInfo/export | `old-code-admin/src/api/tournament/competition.js:197` |
| /wxApp/wxQcCodeConfig/list | `old-code-admin/src/api/tournament/signInQrCode.js:8` |
| /wxApp/wxQcCodeConfig | `old-code-admin/src/api/tournament/signInQrCode.js:17` |
| /wxApp/wxQcCodeConfig/${codeConfigIds} | `old-code-admin/src/api/tournament/signInQrCode.js:35` |
| /wxApp/wxQcCodeConfig | `old-code-admin/src/api/tournament/signInQrCode.js:54` |
| /wxApp/wxQcCodeRecord/list | `old-code-admin/src/api/tournament/signInQrCode.js:75` |
| /wxApp/wxQcCodeRecord/codeBase/${recordId} | `old-code-admin/src/api/tournament/signInQrCode.js:84` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 即时通讯

**业务含义**：好友、私聊/群聊、WebRTC 呼叫、离线消息。

**反向功能需求**：
- 系统支持好友、私聊、群聊、离线消息和 WebRTC 呼叫。 `CONFIRMED_BY_CODE`
- 消息可撤回和标记已读。 `CONFIRMED_BY_CODE`
- 该能力与赛事主链路相对独立。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| FriendController | /friend | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/FriendController.java:64` |
| GroupController | /group | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/GroupController.java:80` |
| GroupMessageController | /message/group | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/GroupMessageController.java:73` |
| PrivateMessageController | /message/private | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/PrivateMessageController.java:74` |
| SystemController | /system | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/SystemController.java:36` |
| WebrtcPrivateController | /webrtc/private | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/controller/WebrtcPrivateController.java:76` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。

## 其他

**业务含义**：未能明确归域的历史或工具能力。

**反向功能需求**：
- 保留历史/工具类能力，重构前需确认是否仍在使用。 `CONFIRMED_BY_CODE`

**后端入口证据**：
| 类/功能 | 基础路径 | 位置 |
| --- | --- | --- |
| BaseController |  | `old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/web/controller/BaseController.java:80` |
| AwardDetailsController | /details | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/AwardDetailsController.java:80` |
| AwardPublicityController | /publicity | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/AwardPublicityController.java:80` |
| ChangeLogController | /log | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/ChangeLogController.java:80` |
| CompetitionAwardsConfigController | /competitionAwardsConfig | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionAwardsConfigController.java:80` |
| CompetitionCheckDataPackageController | /checkPackage | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCheckDataPackageController.java:80` |
| CompetitionCheckInfoController | /checkInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCheckInfoController.java:80` |
| CompetitionGradeInfoController | /competition/competitionGradeInfo | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionGradeInfoController.java:80` |

**前端 API 证据**：
| URL | 调用位置 |
| --- | --- |
| /course/courseInfo/list | `old-code-admin/src/api/course/chapterVideo.js:6` |
| /course/courseInfo/ | `old-code-admin/src/api/course/chapterVideo.js:15` |
| /course/courseInfo | `old-code-admin/src/api/course/chapterVideo.js:23` |
| /course/courseInfo | `old-code-admin/src/api/course/chapterVideo.js:32` |
| /course/chapterVideo/ | `old-code-admin/src/api/course/chapterVideo.js:41` |
| /course/chapterVideo/getInfoByChapterId/ | `old-code-admin/src/api/course/chapterVideo.js:49` |
| /course/chapterVideo | `old-code-admin/src/api/course/chapterVideo.js:57` |
| /course/chapterVideo | `old-code-admin/src/api/course/chapterVideo.js:66` |

**数据表证据**：
| 表 | migration |
| --- | --- |
| competition_scene_resource | `db/migration/20260701_competition_scene_resource_p1_001.sql:7` |
| competition_scene_schedule_resource | `db/migration/20260701_competition_scene_resource_p1_001.sql:39` |
| review_activity | `db/migration/20260703_review_module_phase1.sql:6` |
| review_round | `db/migration/20260703_review_module_phase1.sql:35` |
| review_rule | `db/migration/20260703_review_module_phase1.sql:57` |
| review_criteria | `db/migration/20260703_review_module_phase1.sql:77` |
| review_object | `db/migration/20260703_review_module_phase1.sql:101` |
| review_object_member | `db/migration/20260703_review_module_phase1.sql:141` |

**重构注意**：先保持现有接口/字段兼容；写操作切换前必须补充对账和回归测试。
