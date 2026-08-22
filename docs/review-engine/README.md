# Review Engine V1.0 技术资产

本文档集基于仓库中截至 2026-08-11 的实际代码、Mapper 与数据库迁移脚本生成，服务于 Review Engine 的维护、复用、独立部署评估和后续平台化演进。

## 文档导航

|文档|内容|
|-|-|
|[总体架构说明](review_engine_architecture_overview.md)|模块定位、分层架构、核心模型和不可篡改原则|
|[数据库字典](review_engine_database_dictionary.md)|24 张 Review Engine 业务表、字段、主键和索引|
|[API 文档](review_engine_api_documentation.md)|16 个 Controller、114 个实际映射接口、DTO/VO 和业务规则|
|[权限矩阵](review_engine_permission_matrix.md)|ADMIN、REVIEWER、SECRETARY、OBJECT_OWNER、USER 的能力边界|
|[代码结构说明](review_engine_code_structure.md)|后端与前端目录、关键调用链和依赖|
|[业务流程说明](review_engine_business_flow.md)|标准评审、现场评审和外部业务导入流程|
|[ER 图](review_engine_er_diagram.md)|核心实体与外部关联关系|
|[已知问题与规划](review_engine_known_issues.md)|当前限制、技术债和后续路线|
|[文档生成报告](review_engine_document_generation_report.md)|扫描范围、统计结果、未覆盖项和建议|

## 事实口径

- 后端 Controller 的类级路径是 `/review/**`；经 competition 服务网关访问时，前端使用 `/competition/review/**`。API 文档统一展示网关路径。
- 表结构以 `db/migration/20260703_review_module_phase1.sql`、`20260703_review_module_phase4_submission.sql` 及后续增量迁移为准。
- 表之间主要通过业务 ID 维护逻辑关联，迁移脚本没有声明数据库外键。
- Controller 的 `@RequiresPermissions` 是接口访问的技术权限；活动内角色、任务归属、填报授权和场次归属还会在 Service 层做数据权限校验。
- 文档中“规划能力 / 未实现”表示扫描范围内没有可用实现，不应作为现有接口或交付能力使用。

## V1.0 已实现边界

已实现活动、轮次、规则、指标、对象、材料、外部业务导入、填报、专家与专家组、任务分配、专家评分、现场场次、秘书控制台、参赛证解析、结果汇总/发布/撤回及审计日志。

当前实现位于 `teaching-competition` 服务内部，具备通用对象模型，但尚未拆分成可独立部署的 Review Engine 服务。通用 Business Adapter Framework、AI 辅助和自动专家匹配属于规划能力。
