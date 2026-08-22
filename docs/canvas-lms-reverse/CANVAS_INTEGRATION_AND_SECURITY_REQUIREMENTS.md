# Canvas LMS 集成、安全与权限需求反推

## 集成域

| 集成/能力 | 反向需求 | 证据等级 |
| --- | --- | --- |
| OAuth / Developer Keys | 支持第三方客户端授权、token、scope 和开发者密钥管理。 | CONFIRMED_BY_CODE |
| SAML / Authentication Providers | 支持机构级认证提供商和外部登录。 | CONFIRMED_BY_CODE |
| LTI 1.x / LTI Advantage | 支持工具注册、部署、启动、成员/作业/评分/Asset Processor 等服务。 | CONFIRMED_BY_CODE |
| SIS Import | 支持通过 CSV/ZIP/raw post 导入机构数据。 | CONFIRMED_BY_CODE |
| File Storage / Preview | 支持文件上传、下载、预览、配额和访问控制。 | CONFIRMED_BY_CODE |
| Notifications | 支持多通信渠道和通知偏好。 | CONFIRMED_BY_CODE |
| External plagiarism/media/search integrations | Turnitin、VeriCite、Kaltura、Google/Microsoft 等目录和控制器存在。 | CONFIRMED_BY_CODE |

## 安全/权限需求
- 多租户根账户/子账户隔离。 `STATIC_INFERENCE`
- Course/Account context 下的角色权限控制。 `STATIC_INFERENCE`
- OAuth/LTI/SIS/API token 的 scope 和签名校验。 `STATIC_INFERENCE`
- 文件下载/预览必须通过上下文和用户权限判断。 `STATIC_INFERENCE`
- 审计日志、page views、grade change audit、authentication audit 支撑合规追踪。 `CONFIRMED_BY_CODE`

## 代表 Controller 证据

| Controller | 位置 |
| --- | --- |
| JobsController | `canvas-lms-master/app/controllers/jobs_controller.rb:20` |
| AccountCalendarsApiController | `canvas-lms-master/app/controllers/account_calendars_api_controller.rb:104` |
| RoleOverridesController | `canvas-lms-master/app/controllers/role_overrides_controller.rb:173` |
| AuthenticationAuditApiController | `canvas-lms-master/app/controllers/authentication_audit_api_controller.rb:72` |
| PseudonymsController | `canvas-lms-master/app/controllers/pseudonyms_controller.rb:23` |
| JobsV2Controller | `canvas-lms-master/app/controllers/jobs_v2_controller.rb:20` |
| TermsApiController | `canvas-lms-master/app/controllers/terms_api_controller.rb:95` |
| CspSettingsController | `canvas-lms-master/app/controllers/csp_settings_controller.rb:27` |
| TermsController | `canvas-lms-master/app/controllers/terms_controller.rb:22` |
| BrandConfigsApiController | `canvas-lms-master/app/controllers/brand_configs_api_controller.rb:21` |
| AccountNotificationsController | `canvas-lms-master/app/controllers/account_notifications_controller.rb:83` |
| AuthenticationProvidersController | `canvas-lms-master/app/controllers/authentication_providers_controller.rb:233` |
| ExternalToolsController | `canvas-lms-master/app/controllers/external_tools_controller.rb:607` |
| AdminsController | `canvas-lms-master/app/controllers/admins_controller.rb:49` |
| RateLimitingSettingsController | `canvas-lms-master/app/controllers/rate_limiting_settings_controller.rb:25` |
| DeveloperKeysController | `canvas-lms-master/app/controllers/developer_keys_controller.rb:193` |
| LoginController | `canvas-lms-master/app/controllers/login_controller.rb:21` |
| SubAccountsController | `canvas-lms-master/app/controllers/sub_accounts_controller.rb:23` |
| AnalyticsHubController | `canvas-lms-master/app/controllers/analytics_hub_controller.rb:20` |
| DocviewerAuditEventsController | `canvas-lms-master/app/controllers/docviewer_audit_events_controller.rb:21` |
| OAuthProxyController | `canvas-lms-master/app/controllers/oauth_proxy_controller.rb:21` |
| LtiApiController | `canvas-lms-master/app/controllers/lti_api_controller.rb:25` |
| BrandConfigsController | `canvas-lms-master/app/controllers/brand_configs_controller.rb:20` |
| DeveloperKeyAccountBindingsController | `canvas-lms-master/app/controllers/developer_key_account_bindings_controller.rb:57` |
| ErrorsController | `canvas-lms-master/app/controllers/errors_controller.rb:66` |
| AuditorApiController | `canvas-lms-master/app/controllers/auditor_api_controller.rb:21` |
| OAuth2ProviderController | `canvas-lms-master/app/controllers/oauth2_provider_controller.rb:21` |
| AccountsController | `canvas-lms-master/app/controllers/accounts_controller.rb:303` |
| SharedBrandConfigsController | `canvas-lms-master/app/controllers/shared_brand_configs_controller.rb:62` |
| AccountGradingSettingsController | `canvas-lms-master/app/controllers/account_grading_settings_controller.rb:24` |
| FeatureFlagsController | `canvas-lms-master/app/controllers/feature_flags_controller.rb:136` |
| StatsController | `canvas-lms-master/app/controllers/conditional_release/stats_controller.rb:21` |
| Test::MockLtiController | `canvas-lms-master/app/controllers/test/mock_lti_controller.rb:23` |
| AssetProcessorController | `canvas-lms-master/app/controllers/support_helpers/asset_processor_controller.rb:21` |
| TurnitinController | `canvas-lms-master/app/controllers/support_helpers/turnitin_controller.rb:21` |
| PlagiarismPlatformController | `canvas-lms-master/app/controllers/support_helpers/plagiarism_platform_controller.rb:21` |
| ToolProxyController | `canvas-lms-master/app/controllers/lti/tool_proxy_controller.rb:21` |
| Lti::ResourceLinksController | `canvas-lms-master/app/controllers/lti/resource_links_controller.rb:150` |
| PlatformStorageController | `canvas-lms-master/app/controllers/lti/platform_storage_controller.rb:42` |
| AssetProcessorController | `canvas-lms-master/app/controllers/lti/asset_processor_controller.rb:21` |
| ToolDefaultIconController | `canvas-lms-master/app/controllers/lti/tool_default_icon_controller.rb:21` |
| Lti::TokenController | `canvas-lms-master/app/controllers/lti/token_controller.rb:21` |
| ContextControlsController | `canvas-lms-master/app/controllers/lti/context_controls_controller.rb:125` |
| AssetProcessorLaunchController | `canvas-lms-master/app/controllers/lti/asset_processor_launch_controller.rb:21` |
| PublicJwkController | `canvas-lms-master/app/controllers/lti/public_jwk_controller.rb:22` |
| AssetProcessorTiiMigrationsApiController | `canvas-lms-master/app/controllers/lti/asset_processor_tii_migrations_api_controller.rb:22` |
| PlagiarismAssignmentsApiController | `canvas-lms-master/app/controllers/lti/plagiarism_assignments_api_controller.rb:64` |
| Lti::ToolConfigurationsApiController | `canvas-lms-master/app/controllers/lti/tool_configurations_api_controller.rb:41` |
| AccountLookupController | `canvas-lms-master/app/controllers/lti/account_lookup_controller.rb:66` |
| LtiAppsController | `canvas-lms-master/app/controllers/lti/lti_apps_controller.rb:93` |
| SubscriptionsApiController | `canvas-lms-master/app/controllers/lti/subscriptions_api_controller.rb:46` |
| DeploymentsController | `canvas-lms-master/app/controllers/lti/deployments_controller.rb:84` |
| AccountExternalToolsController | `canvas-lms-master/app/controllers/lti/account_external_tools_controller.rb:29` |
| EulaLaunchController | `canvas-lms-master/app/controllers/lti/eula_launch_controller.rb:21` |
| OriginalityReportsApiController | `canvas-lms-master/app/controllers/lti/originality_reports_api_controller.rb:113` |
| DataServicesController | `canvas-lms-master/app/controllers/lti/data_services_controller.rb:65` |
| MembershipServiceController | `canvas-lms-master/app/controllers/lti/membership_service_controller.rb:23` |
| FeatureFlagsController | `canvas-lms-master/app/controllers/lti/feature_flags_controller.rb:44` |
| Lti::RegistrationsController | `canvas-lms-master/app/controllers/lti/registrations_controller.rb:1112` |
| Login::OAuth2Controller | `canvas-lms-master/app/controllers/login/oauth2_controller.rb:21` |
| Login::OAuthBaseController | `canvas-lms-master/app/controllers/login/oauth_base_controller.rb:21` |
| Login::OtpController | `canvas-lms-master/app/controllers/login/otp_controller.rb:24` |
| Login::SamlIdpDiscoveryController | `canvas-lms-master/app/controllers/login/saml_idp_discovery_controller.rb:21` |
| Login::CasController | `canvas-lms-master/app/controllers/login/cas_controller.rb:23` |
| Login::OpenidConnectController | `canvas-lms-master/app/controllers/login/openid_connect_controller.rb:21` |
| Login::ExternalAuthObserversController | `canvas-lms-master/app/controllers/login/external_auth_observers_controller.rb:20` |
| Login::CanvasController | `canvas-lms-master/app/controllers/login/canvas_controller.rb:21` |
| Login::OAuthController | `canvas-lms-master/app/controllers/login/oauth_controller.rb:21` |
| Login::SamlController | `canvas-lms-master/app/controllers/login/saml_controller.rb:21` |
| CleverController | `canvas-lms-master/app/controllers/login/clever_controller.rb:22` |
| ToolProxyController | `canvas-lms-master/app/controllers/lti/ims/tool_proxy_controller.rb:23` |
| AssetProcessorController | `canvas-lms-master/app/controllers/lti/ims/asset_processor_controller.rb:26` |
| AuthorizationController | `canvas-lms-master/app/controllers/lti/ims/authorization_controller.rb:60` |
| LineItemsController | `canvas-lms-master/app/controllers/lti/ims/line_items_controller.rb:74` |
| ResultsController | `canvas-lms-master/app/controllers/lti/ims/results_controller.rb:62` |
| NoticeHandlersController | `canvas-lms-master/app/controllers/lti/ims/notice_handlers_controller.rb:86` |
| DeepLinkingController | `canvas-lms-master/app/controllers/lti/ims/deep_linking_controller.rb:22` |
| AuthenticationController | `canvas-lms-master/app/controllers/lti/ims/authentication_controller.rb:26` |
| NamesAndRolesController | `canvas-lms-master/app/controllers/lti/ims/names_and_roles_controller.rb:264` |
| AssetProcessorEulaController | `canvas-lms-master/app/controllers/lti/ims/asset_processor_eula_controller.rb:26` |
| ProgressController | `canvas-lms-master/app/controllers/lti/ims/progress_controller.rb:100` |
| DynamicRegistrationController | `canvas-lms-master/app/controllers/lti/ims/dynamic_registration_controller.rb:26` |
| ScoresController | `canvas-lms-master/app/controllers/lti/ims/scores_controller.rb:71` |
| ToolSettingController | `canvas-lms-master/app/controllers/lti/ims/tool_setting_controller.rb:25` |
