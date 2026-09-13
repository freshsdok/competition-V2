# 前端原型 17 与现行 V2 代码映射审计

> 审计日期：2026-09-01  
> 审计性质：只读静态核对；未修改 `deshi_competition_v2` 的前端、后端、数据库或契约文件  
> 现行代码基线：`/Users/wwang/Documents/ClaudeCode/deshi_competition_v2`，HEAD `65a45a59f16a`，并包含未提交的人类开发改动  
> 契约权威：`docs/api` 与 `docs/package_doc` 中的版本化 OpenAPI YAML；Controller 注解和前端 client 仅作为运行时/实现投影

## 1. 结论

四份附件不能作为“现行实现说明”直接冻结。两个 HTML 是无后台请求的交互原型；拆解说明与项目概览包含有价值的产品语义，但其代码基线、数量统计和若干能力结论已经落后于当前工作树。

- Admin 原型有 24 个一级可导航页面；拆解说明的 58 是部分页面内 tab 单元，不是页面总数。
- Web 原型有 32 个可导航页面；拆解说明统计表写 24，明细实有 26 行，仍漏掉 6 个页面。
- 两个 HTML 均未使用 `fetch`、Axios 或 `XMLHttpRequest`。其中出现的 API 文字是设计提示，不构成接口已经对接的证据。
- 附件冻结在 `origin/main f070700 / V2_40_700 / 72 migrations`；当前工作树 HEAD 为 `65a45a59f16a`，迁移文件 79 个，最新为 `V2_41_400__password_product_closure.sql`，Controller 文件 68 个。
- 附件称 engagement 是 mobile-only、Admin/Web 无入口，这一结论已失效。当前 Admin 有 `EngagementWorkbenchPanel`，Web 有 `/me/engagement`，后端以 `FeedbackCase` 统一拥有 `SUGGESTION / FEEDBACK / COMPLAINT / APPEAL` 生命周期。
- 附件把插件、学习、考试、商城标成“非本版本范围”，但现行代码、API client、Controller 和版本化 YAML 均已有实现。是否从产品版本移除属于 Human 范围决策，不能由旧原型反向删除现行能力。

整体判断：产品理念与根模型大体一致，页面级信息架构中度一致，现行路由和接口实现存在明显合并/拆分差异。原型可作为 UX 候选，不可作为代码改造指令或契约权威。

## 2. 核对方法与评分

评分是审计判断，不是自动测试结果：

- 30 分：领域语义、Fact Owner、状态机与当前根模型一致；
- 20 分：存在可达的现行 Repository 路由/入口；
- 30 分：存在对应前端组件和真实 API client 调用；
- 20 分：存在 Controller 与版本化 YAML 契约证据。

等级：A（90–100，高匹配）、B（75–89，较高匹配）、C（50–74，部分匹配）、D（0–49，低匹配或范围冲突）。“无独立路由”不等于能力不存在；Admin 当前采用 `?area=` 与组件内 tab，而原型采用 `showSection()`。

路径缩写：

- `A/`：`frontend/apps/admin/src/`
- `W/`：`frontend/apps/web/src/`
- `C/`：`frontend/packages/api-client/src/`
- `B/`：`backend/`
- `Y/`：`docs/api/` 或 `docs/package_doc/`

## 3. Admin 原型逐页映射（24 个一级页面）

| 原型页面 | 现行代码文件 | 现行仓库入口 | API / Controller / YAML | 匹配度 | 核对结论 |
|---|---|---|---|---:|---|
| `tenants` 租户管理 | `A/App.vue` | `/?area=overview`，技术治理内 `governanceView=tenants`，tab 状态不入 URL | `C/platform-core.ts`；`PlatformCoreController`；`Y/platform-core-openapi-v1.0.yaml` | 82 B | 列表、创建、切换均存在；原型的激活/停用/租户设置命令当前无对应生命周期端点，不能接成假按钮。 |
| `organization` 组织管理 | `A/App.vue`；`A/features/platform/OrganizationHierarchyPanel.vue` | `/?area=overview`（技术治理）或 `/?area=organization`（业务管理） | `/api/v1/platform/organizations*`、`organization-relations*`；`PlatformCoreController`；platform-core YAML | 94 A | 关系式层级、时点查询、移动预览和补正与现行模型高度一致；原型一个入口对应当前两个管理语境。 |
| `platform-comm` 平台通信 | `A/features/communication/CommunicationPanel.vue` | 无同名入口；当前挂在 `/?area=onsite` 且要求选择 Activity | `C/communication.ts`；`CommunicationController`、`CommunicationProductController`；communication 两份 YAML | 58 C | 能力和正式 API 已存在，但现行 UI 归属是 Activity 现场区，不等同原型的 PLATFORM 通信中心；应先决策导航归属。 |
| `platform` 平台服务 | `A/features/platform-services/PlatformServicesPanel.vue` | `/?area=platform` | configuration/audit/tasks/metadata/providers/exchange API；对应多个 Admin Controller；部分能力仅运行时投影 | 90 A | 当前真实实现为八个内嵌 tab；原型总体概念匹配。 |
| `audit` 审计日志 | `A/features/platform-services/PlatformServicesPanel.vue` | `/?area=platform` 后手动切 `audit`；没有 `?area=audit` | `/api/v1/platform-services/audit`；`AuditAdminController`；当前 operationId 为派生投影 | 68 C | 原型的“独立唯一入口”不是现行路由事实；当前只有平台服务内 tab。三入口是原型自身重复，不是现行 Vue 的三入口。 |
| `data-gov` 数据治理 | `A/features/platform-services/PlatformServicesPanel.vue`、`LegalGovernancePanel.vue`、`AccountLifecyclePanel.vue` | `/?area=platform` 的 `legal` / `account-lifecycle` tab | `PrivacyGovernanceController`、`LegalVersionAdminController`、`AccountLifecycleAdminController`；`Y/privacy-governance-openapi-v1.yaml` | 62 C | 法律版本与账号生命周期已接入；隐私请求处理队列、保留策略和执行预览没有完整 Admin 页面，原型不能视作已实现。 |
| `credential` 凭证治理 | `A/features/credential/CredentialPanel.vue`、`CredentialEcosystemPanel.vue` | 无独立入口；当前在 `/?area=delivery`，且需 Activity | credential、credential-governance、credential-ecosystem API；四个 Credential Controller；三份相关 YAML | 84 B | 签发、Token、Issuer/Definition/Policy、钱包互通后台能力已实现；导航范围比原型窄，治理能力不应被 Activity 门禁误解为 Activity 所有。 |
| `recovery-review` 账号恢复审核 | `A/features/identity/RecoveryReviewPanel.vue` | `/?area=people`，与人员目录合并 | `/api/v1/admin/recovery-cases*`；当前新增 `PasswordRecoveryAdministrationController` 尚在未提交工作树；platform-entry 契约正在同步 | 90 A | 页面和命令链存在；附件基线未覆盖当前 PASSWORD-AUTH-CLOSE 工作。 |
| `plugins` 插件与扩展 | `A/features/plugins/PluginManagementPanel.vue` | `/?area=plugins` | `C/plugin-management.ts`；`PluginGovernanceController`；`Y/plugin-governance-openapi-v1.yaml` | 42 D | 原型写“非本版本/REMOVE”与现行完整实现直接冲突。不能据原型删除；需 Human 明确产品发布范围。 |
| `org-members` 成员管理 | `A/App.vue` | `/?area=organization` | `/api/v1/platform/organizations/{id}/memberships*`；`PlatformCoreController`；platform-core YAML | 94 A | 直接成员关系、有效投影、结束关系保留历史均一致。 |
| `org-growth` 组织目录/成长 | `A/features/platform/OrganizationGrowthPanel.vue` | `/?area=organization-growth` | `/api/v1/organization-growth/*`；`OrganizationGrowthController`；`Y/organization-growth-openapi-v1.yaml` | 96 A | 目录注册、激活、ActionLink、人员预览/应用与现行代码一致。 |
| `people-directory` 人员名册 | `A/features/identity/PeopleDirectoryPanel.vue` | `/?area=people` | `/api/v1/admin/identity-directory*`；`IdentityExperienceController` | 95 A | 人员目录与 Subject 360 已真实接入；不应把目录投影误当第二套 Subject 主数据。 |
| `imported-activation` 导入与激活 | `A/features/identity/AccountActivationManagementPanel.vue` | `/?area=people` | `/api/v1/admin/authentication/imported-subject-activations*`；`ImportedSubjectActivationController`；platform-entry YAML | 94 A | 当前工作树新增等待联系方式与补录链，附件只覆盖较早形态。 |
| `overview` 成员与权限 | `A/App.vue` | 技术治理 `/?area=overview` 与业务 `/?area=organization` 分担 | identities、roles、permissions、role-assignments；`PlatformCoreController` | 84 B | 创建和撤销 RoleAssignment 后端都存在；现行 Admin 尚未暴露撤销按钮。原型所称“无撤销端点”不准确，真正缺的是前端动作及角色编辑/停用契约。 |
| `content` 门户与 CMS | `A/features/cms/CmsWorkspace.vue` | `/?area=content` | `C/cms.ts`；`CmsAdminController`；`Y/cms-openapi-v1.yaml` | 96 A | 文章、栏目、页面、导航、策略、审核、预览和发布均有真实调用；原型可作为布局候选。 |
| `tenant-svc` 租户服务 | `A/features/platform-services/PlatformServicesPanel.vue` | `/?area=platform`，选择 Tenant 后使用 TENANT scope | configuration、audit、metadata、data-exchange；各平台服务 Controller | 72 C | 能力大多存在，但没有独立租户服务路由；公告消费不属于该组件，附件把多个 Fact Owner 合并成一页。 |
| `competition` 赛事中心 | `A/features/activity/SimpleCompetitionBuilderPanel.vue`、`CompetitionVersionGovernancePanel.vue`、`CompetitionEvaluationBuilderPanel.vue` | `/?area=competition` | competition product/governance/core-product/operational-composition；相关 Controller；四份 competition YAML | 93 A | Competition→Edition→Definition/Publication 的现行结构匹配；原型“考区管理”和部分运营表还没有一一对应组件。 |
| `learning` 学习管理 | `A/features/ecosystem/LearningMallOperationsPanel.vue` | `/?area=learning` 的 learning 部分 | `/api/v1/learning/*`；`LearningController`；`Y/learning-exam-mall-openapi-v1.yaml` | 48 D | 现行代码已实现，原型却标“非本版本”。且现行将三域合并为一个运营面，需 Human 决定拆分，不得按附件直接移除。 |
| `exam` 考试管理 | 同上 | `/?area=learning` 的 exam 部分 | `/api/v1/exams/*`；`ExamController`；learning-exam-mall YAML | 48 D | 与 learning 同类范围冲突；正式 API 和手工阅卷工作台已存在。 |
| `mall` 商城管理 | 同上 | `/?area=learning` 的 mall 部分 | `/api/v1/mall/*`；`MallController`；learning-exam-mall YAML | 48 D | 与 learning 同类范围冲突；产品、Offer、集合与订单接口已存在。 |
| `delivery` 材料提交与评审 | `ActivitySubmissionPanel.vue`、`ArtifactFilePanel.vue`、`CredentialPanel.vue`、`EvaluationPanel.vue`、`AwardDecisionPanel.vue` | `/?area=delivery`，需选 Activity | submission/artifact/credential/evaluation/award API；对应 Controller；多份域 YAML | 92 A | 五个 Fact Owner 在一个运营区组合，真实调用完整；页面状态不能替代各域正式状态。 |
| `onsite` 现场运行 | `ActivityRuntimePanel.vue`、`AttendancePanel.vue`、`ResourceReservationPanel.vue`、`CommunicationPanel.vue` | `/?area=onsite`，需选 Activity | runtime/pass/attendance/resource/communication API；对应 Controller 与 YAML | 88 B | 运行、考勤、资源链匹配；平台通信被一并放入现场区是与原型最大的信息架构差异。 |
| `finance` 费用运营 | `A/features/commerce/CommerceFinancePanel.vue` | `/?area=finance`，需选 Activity | `/api/v1/admin/commerce/*`；`CommerceFinanceAdminController`；`Y/commerce-finance-openapi-v1.yaml` | 90 A | 支付、开票、退款、对账、因果追踪均有真实接口；真实 Provider 成功仍需外部证据，不能由 UI 状态证明。 |
| `complaints` 投诉与举报 | `A/features/engagement/EngagementWorkbenchPanel.vue` | `/?area=engagement` | `/api/v1/engagement/admin/feedback-cases*`；`EngagementController`；`Y/platform-engagement-openapi-v1.yaml` | 60 C | 当前 Fact Owner 是 FeedbackCase，不存在附件假设的第二个 Complaints 域。原型六态/TS 编号与现行八态/caseNo 不同，必须适配而非复制。 |

## 4. Web 原型逐页映射（32 个页面）

| 原型页面 | 现行代码文件 | 现行仓库路由 | API / Controller / YAML | 匹配度 | 核对结论 |
|---|---|---|---|---:|---|
| `portal` 门户首页 | `W/features/portal/PortalHomeView.vue` | `/`、`/pages/:slug` | public portal pages/home；`CmsPublicController`；cms YAML | 95 A | CMS 驱动页面已真实接入；原型卡片数据仍是静态演示。 |
| `article-detail` 资讯详情 | `W/features/portal/ArticleDetailView.vue` | `/articles/:slug` | platform/tenant article API；`CmsPublicController`；cms YAML | 97 A | 路由和数据源直接匹配。 |
| `news-list` 资讯频道 | `W/features/portal/ChannelListView.vue` | `/channels/:slug` | platform/tenant channel articles；`CmsPublicController`；cms YAML | 96 A | 路由和数据源直接匹配。 |
| `competitions` 赛事中心 | `CompetitionDiscoveryView.vue`、`MyActivitiesView.vue`、`CommerceCenterView.vue` | `/competitions`、`/me/activities`、`/me/commerce` | public competition、activity、commerce 三组 API | 70 C | 原型将三个事实域合为三 tab；现行仍是三条路由，项目概览对此标为 To-Be 是正确的。 |
| `registration` 报名六步 | `W/features/competition/CompetitionRegistrationView.vue` | `/registrations/:registrationId` | `/api/v1/competition-registrations/{id}*`；`CompetitionRegistrationController`；competition-product YAML | 94 A | 人员解析、邀请、材料、审批组织、提交、计费、finalize、修订均有真实调用。 |
| `workspace` 工作台 | `W/features/workspace/GlobalWorkspaceView.vue`、`WorkspaceBoard.vue` | `/workspace` | `/api/v1/workspaces/global`；`WorkspaceController`；workspace YAML | 97 A | 服务端投影与受控 actionCode 一致。 |
| `ws-activity` 赛事工作区 | `W/features/workspace/ActivityWorkspaceView.vue` | `/workspace/activities/:activityId` | `/api/v1/workspaces/activities/{id}`；WorkspaceController；workspace YAML | 97 A | 直接匹配。 |
| `ws-organization` 组织工作区 | `W/features/workspace/OrganizationWorkspaceView.vue` | `/workspace/organizations/:organizationId` | `/api/v1/workspaces/organizations/{id}`；WorkspaceController | 66 C | 路由/API 匹配，但 HTML 同页同时出现“指导老师审批”和“组织成员审批”两套互相冲突文案；现行契约以组织审批通道为准。 |
| `ws-onsite` 现场工作区 | `W/features/workspace/OnsiteWorkspaceView.vue` | `/workspace/activities/:activityId/onsite` | `/api/v1/workspaces/activities/{id}/onsite`；WorkspaceController | 96 A | 直接匹配。 |
| `submissions` 我的提交 | `W/features/submission/MySubmissionsView.vue` | `/me/submissions` | `/api/v1/activity-submissions/*`；`ActivitySubmissionController`；activity-submission YAML | 90 A | 草稿、Artifact 关联、正式提交和修订链已接入；原型须采用该命名，不应写成 `/api/v1/submissions/*`。 |
| `communications` 消息与帮助 | `W/features/communication/CommunicationCenterView.vue` | `/me/communications` | inbox/preferences/customer-service；Communication Controllers；communication YAML | 95 A | 通知、已读、偏好、会话和附件关联均是真实调用。 |
| `identity` 我的信息 | `W/features/identity/MyIdentityView.vue` | `/me/identity` | `/api/v1/me/identity`、`platform/me/model-view`、qualification、verification、context switch | 93 A | Subject/Human Model/Identity/Qualification 边界基本正确。 |
| `login` 登录 | `W/features/identity/LoginView.vue` | `/login` | SMS/email/password challenge/session；Authentication Controllers；platform-entry YAML 部分覆盖 | 95 A | 三种登录方式已真实接入；developmentCode 仍只可视为受控开发证据。 |
| `invitations` 邀请中心 | `W/features/competition/CompetitionInvitationCenterView.vue` | `/competition-invitations` | team/teacher/access invitation API；`CompetitionRegistrationController` | 91 A | 三类赛事邀请真实存在；Organization ActionLink 是另一事实链，不应合并成同一邀请实体。 |
| `artifacts` 我的材料 | `W/features/artifact/MyArtifactsView.vue` | `/me/artifacts` | `/api/v1/artifacts*` 与 `/versions/*`；`ArtifactFileController`；artifact YAML | 93 A | 版本追加、预览和下载均匹配；不可覆盖旧版本。 |
| `evaluations` 我的评审 | `W/features/evaluation/MyEvaluationsView.vue` | `/me/evaluations` | `/api/v1/evaluation/tasks*`；`EvaluationController`；evaluation YAML | 92 A | 工作台、草稿、幂等提交和冻结输入匹配。 |
| `attendance` 我的到场 | `W/features/attendance/MyAttendanceView.vue` | `/me/attendance` | `/api/v1/attendance/check-ins/me`、`executions/{id}/self-check-ins`；AttendanceController | 92 A | 当前 Web 使用挑战码自助签到；二维码扫描属于客户端增强而非另一契约。 |
| `reservations` 资源预约 | `W/features/resource/MyReservationsView.vue` | `/me/reservations` | offers/slots/reservations；`ResourceReservationController`；resource YAML | 91 A | Offer/Slot、容量 Ledger、确认和取消均有真实调用。 |
| `credentials` 我的证书 | `W/features/credential/MyCredentialsView.vue` | `/me/credentials` | credentials/me、award-entitlements、wallet/opportunities/conversion；Credential Controllers | 86 B | 现行已把证书和钱包/转换机会合并；原型另设 wallet 页，属于重复信息架构。 |
| `privacy` 隐私与数据 | `W/features/privacy/MyPrivacyView.vue` | `/me/privacy` | privacy requests、legal acknowledgements、account lifecycle；Privacy Controllers；privacy YAML | 94 A | 当前代码只自动执行自助允许的 ACCESS/EXPORT/ACCOUNT_CLOSURE；破坏性请求由治理权限继续，边界正确。 |
| `competition-detail` 赛事详情 | `W/features/competition/CompetitionDetailView.vue` | `/competitions/editions/:editionId`；兼容 `/competitions/:activityId` | public competition/edition、CTA、preview、create registration；Public/Registration Controller | 95 A | Edition 路由是当前优先形态；旧 activityId 路由仅兼容。 |
| `my-runtime` 我的现场任务 | `W/features/runtime/MyRuntimeView.vue` | `/me/runtime` | runtime tasks、competition pass proof；Runtime/Pass Controllers | 94 A | “参赛证明不等于已签到”的边界与现行设计一致。 |
| `approval` 报名审批 | `W/features/competition/CompetitionApprovalView.vue` | `/organization-registration-approvals/:approvalId` | get/decide organization approval；`CompetitionRegistrationController` | 68 C | 正式契约是组织报名审批。原型导航仍写“指导老师审批”，且页面含两套角色叙事，需统一为组织成员+精确权限。 |
| `complaints` 投诉与举报 | `W/features/engagement/EngagementCenterView.vue` | `/me/engagement` | feedback-cases/me、submit、artifacts；`EngagementController`；platform-engagement YAML | 58 C | 当前 COMPLAINT 是 FeedbackCase 类型，不是独立 Complaint 六态域；原型的 TS 编号、撤回/补充动作没有当前契约证据。 |
| `learning` 学习中心 | `W/features/ecosystem/LearningMallView.vue` | `/me/learning?tab=learning` | `/api/v1/learning/*`；LearningController；learning-exam-mall YAML | 82 B | 能力已接真，但现行三域共用一个组件；是否拆页是 UX 决策。 |
| `exam` 考试中心 | 同上 | `/me/learning?tab=exam` | `/api/v1/exams/*`；ExamController；learning-exam-mall YAML | 82 B | 同上；题目作答与提交必须保留幂等/版本语义。 |
| `mall` 竞赛商城 | 同上 | `/me/learning?tab=mall`，另有 `/mall` | `/api/v1/mall/*`；MallController；learning-exam-mall YAML | 84 B | API 已接真；独立公开 `/mall` 与登录后 tab 共用组件。 |
| `wallet` 凭证钱包 | `W/features/credential/MyCredentialsView.vue` | `/me/credentials` | credential wallet/opportunities/conversion API；CredentialEcosystemController | 82 B | 现行没有独立 wallet 路由；原型应与 credentials 合并或先批准新路由。 |
| `recovery` 安全恢复 | `W/features/identity/RecoveryView.vue` | `/recover` | `/api/v1/auth/recovery/requests*`；AuthenticationSecurityController | 94 A | 丢失手机号恢复链真实存在。 |
| `password-recovery` 密码找回 | `W/features/identity/PasswordRecoveryView.vue` | `/password-recovery` | password-recovery requests/completions/human-completions；PasswordRecovery Controllers；当前工作树正在闭环 | 95 A | 当前实现比附件基线更新；报告结论受未提交工作树影响，冻结前需以批准 commit 重跑。 |
| `account-activation` 账号激活 | `W/features/identity/AccountActivationView.vue` | `/account-activation/:token?` | inspect/complete activation + legal requirements；AccountActivationController | 96 A | 路由和完整链直接匹配。 |
| `profile-security` 账号安全中心 | `W/features/identity/ProfileSecurityView.vue` | `/me/profile` | profile/security/theme/contact replacement/account links/password；Identity/Authentication Controllers | 94 A | 原型遗漏在 26 行拆解表中，但现行页面和 API 已完整存在。 |

## 5. 关键冲突台账

| ID | 事实 | 推理 | 风险 / 建议 |
|---|---|---|---|
| C-01 | 拆解统计为 Admin 58、Web 24、Mobile 29、合计 111；Web 明细实际 26，且 HTML 可导航页为 32。 | 统计口径把“页面”“tab 单元”“Mobile 路由”混为一谈。 | 冻结前改成三张清单：一级路由、页面内 tab、组件；分别计数，禁止再以 111 作为验收分母。 |
| C-02 | 附件基线 `f070700/72 migrations`，现行工作树为 `65a45a59f16a` 加未提交改动、79 migrations。 | 后续密码认证、平台入口、Engagement 等工作使附件结论漂移。 | 选择一个批准 commit 后重新生成拆解；当前报告是工作树快照，不是可复现 release 基线。 |
| C-03 | 两个 HTML 的后台请求计数均为 0。 | API tooltip 和模拟 toast 只能证明设计意图。 | 不得把原型按钮标记为“已联调”“可发布”或真实 Provider 证据。 |
| C-04 | 现行 Admin/Web 已有 engagement 入口和 FeedbackCase API。 | 附件 X3 的“mobile-only/关系未定义”已被代码事实取代。 | 统一用 FeedbackCase；原型投诉页需改状态/编号/动作，不新增第二个 Complaint 主模型。 |
| C-05 | 插件、学习、考试、商城均有现行组件、client、Controller 和 YAML。 | 原型的“非本版本/REMOVE”是范围提案，不是实现事实。 | 提交 Human 范围决策；在批准前保持现状，不据旧 HTML 删除代码或数据库。 |
| C-06 | 现行 Admin 没有独立 audit、data-gov、credential、platform-comm 路由。 | 原型的信息架构与当前 `adminAreas` 不同。 | 先冻结导航 ACR，再决定拆路由或保持组合面；不要用 HTML 名称伪装成现行 Repository 路由。 |
| C-07 | Web `ws-organization` 与 `approval` 原型同时出现老师审批和组织审批叙事。 | 同一 HTML 内部存在 D2 前后口径残留。 | 以 `/organization-registration-approvals/{id}` 与服务端权限为准，清除“指导老师是审批人”的旧文案。 |
| C-08 | Tenant 激活/停用端点确实不存在；RoleAssignment 撤销端点已经存在，但 Admin 未暴露。 | 拆解把“无前端动作”和“无后端端点”混写。 | Tenant 生命周期登记为真实能力缺口；角色撤销登记为前端接入缺口；角色定义编辑/停用另立候选契约。 |
| C-09 | OpenAPI 注解实施报告显示 YAML/运行时仍有 path、status、header、required 和 schema 漂移。 | 有 Controller 或 api-client 函数不等于 YAML parity 已完成。 | 页面实施前逐域用版本化 YAML 对齐；不得由前端当前调用反向覆盖规范 YAML。 |
| C-10 | 当前 V2 工作树有大量未提交的人类开发改动。 | 本报告看到的是“HEAD + working tree”，不是单一 commit。 | 冻结原型映射前，由 Human 指定权威 commit；再运行静态覆盖、运行时 `/v3/api-docs` 与 Apifox 验收。 |

## 6. 可采纳的设计原则

以下理念与当前设计一致，可保留：

- Subject 是唯一自然人主体；Identity、Membership、RoleAssignment、Qualification、Credential 各自拥有事实，不互相推断。
- Tenant、Organization、Activity/Competition/Edition 是不同上下文；切换展示不能替代服务端 Context 切换。
- Workspace 是服务端投影和受控动作目录，不是新的业务事实源。
- Artifact Version 追加而不覆盖；正式提交、评审、凭证和隐私导出均引用稳定版本。
- 高风险命令保留幂等键、版本号、原因、二次确认和失败关闭；UI 成功提示不能替代正式状态。
- Provider 调用、支付到账、发票开具、短信送达和生产准备度均需要外部运行证据，原型不能证明。

## 7. 建议的后续冻结顺序

1. Human 先冻结产品范围：插件、Learning、Exam、Mall 是否进入本版本；这一步不能由旧原型决定。
2. 冻结 Admin 一级信息架构：保留现行 15 个 `adminAreas`，或批准原型 24 个一级入口中的拆分项。
3. 以现行 `router.ts` 为 Web As-Is，逐项批准 competitions 三 tab、wallet 独立页、complaints 独立页等 To-Be 路由。
4. 以版本化 YAML 为接口权威，逐页生成“前端动作 → operationId → request/response schema → 状态/错误 → 权限”的验收矩阵。
5. 只有上述三项冻结后，才进入真实 API 对接和浏览器运行验收；本轮没有授权实施这些改动。

## 8. 审计证据

- 附件：`/Users/wwang/Downloads/admin-demo (17).html`，SHA-256 `dcbe1c0a5ceb7145216f9da4a3963fae38183fcea41f7071b188c37ceb54260d`
- 附件：`/Users/wwang/Downloads/web-demo (17).html`，SHA-256 `990976ab7ea4356376e1bf2d37bc94aae29e936b671e91d711e6c3ccef51c5fe`
- 附件：`/Users/wwang/Downloads/前端原型功能拆解说明书.md`，SHA-256 `8a63c884634d79b11e36e25b7c453ed2677ae6ced094ab1be4522840014822bd`
- 附件：`/Users/wwang/Downloads/项目概览 (12).md`，SHA-256 `e18a302e4cfe299ac9ffeb910bedd5eb7471d053020bfe4b2c02d9b3bce04604`
- 现行前端路由：`frontend/apps/web/src/router.ts`、`frontend/apps/admin/src/App.vue`
- 现行 API client：`frontend/packages/api-client/src/*.ts`
- 现行 Controller：`backend/**/*Controller.java`
- 规范性契约：`docs/api/*.yaml`、`docs/package_doc/*openapi*.yaml`
- 运行时契约审计：`docs/audits/openapi-annotation-preflight-2026-09-01/OPENAPI-RUNTIME-PROJECTION-IMPLEMENTATION-REPORT.md`

## 9. 审计边界

本轮未启动应用、未访问数据库、未调用任何写接口、未执行 Provider 请求，也未做浏览器真实运行。匹配度反映附件与当前源代码/契约的静态一致性，不代表端到端联调通过、Human 验收通过或生产就绪。
