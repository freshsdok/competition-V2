# Review Engine V1.0 文档生成报告

生成日期：2026-08-11

## 1. 已生成文档

已在 `docs/review-engine/` 生成 10 个 Markdown 文件：

1. `README.md`
2. `review_engine_architecture_overview.md`
3. `review_engine_database_dictionary.md`
4. `review_engine_api_documentation.md`
5. `review_engine_permission_matrix.md`
6. `review_engine_code_structure.md`
7. `review_engine_business_flow.md`
8. `review_engine_known_issues.md`
9. `review_engine_er_diagram.md`
10. `review_engine_document_generation_report.md`

## 2. 扫描的代码与数据目录

### 后端

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/mapper`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/dto`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/vo`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/enums`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/config`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/constant`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/support`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/review`

为确认外部适配与数据来源，还检查了 `ReviewObjectServiceImpl` 使用的 Competition Mapper/领域对象、FileTask 远程服务引用和现场排期关联。

### 前端

- `old-code-admin/src/views/review`
- `old-code-admin/src/api/review`

### 数据库迁移

- `db/migration/20260703_review_module_phase1.sql`
- `db/migration/20260703_review_module_phase3_menu.sql`
- `db/migration/20260703_review_module_phase4_submission.sql`
- `db/migration/20260703_review_module_phase5_my_review.sql`
- `db/migration/20260703_review_module_phase6_secretary_console.sql`
- `db/migration/20260706_review_module_phase7_result_publish.sql`
- `db/migration/20260707_review_assignment_management_menu.sql`
- `db/migration/20260707_review_module_scoring_rule_enhancement.sql`
- `db/migration/20260707_scene_schedule_review_binding.sql`
- `db/migration/20260708_review_file_task_import.sql`

## 3. 扫描统计

|项目|数量|说明|
|-|-:|-|
|Review Engine 业务表|24|23 张 Phase 1 表 + `review_object_submit_log`|
|Domain Java 文件|25|24 个表实体 + `ReviewBaseEntity`|
|Mapper 接口|24|与业务表对应|
|Mapper XML|24|与业务表对应|
|Controller|16|全部位于 `review/controller`|
|HTTP Mapping|114|含 4 个运行时固定拒绝的旧评分兼容路由|
|唯一 Controller 权限码|65|按 `@RequiresPermissions` 去重|
|Service 接口文件|29|含通用 CRUD 和文件预览等服务|
|Service 实现文件|30|含抽象 CRUD 与 Office 转换服务|
|Request DTO|17|含已禁用旧评分接口 DTO|
|Response/辅助 VO|24|含流式预览资源对象|
|Enum|27|覆盖活动、对象、评分、现场和结果状态|
|评审前端 Vue 文件|13|管理、填报、专家、秘书页面及组件|
|评审前端 API 文件|13|按业务领域拆分|

## 4. 数据库字典完成情况

- 已覆盖任务指定的全部活动、对象、专家、评分、现场和日志表。
- 额外覆盖代码实际使用的 `review_panel`、`review_panel_member`。
- 已纳入 20260708 增量增加的材料来源字段与索引。
- 已说明所有表共有主键、审计字段和逻辑删除字段。
- 已登记主键、唯一索引、普通索引、状态字段和外部关联字段。
- 已说明 `competition_scene_schedule_target` 是外部业务表扩展，不计入 24 张引擎表。

## 5. API 文档完成情况

- 已覆盖 16 个 Controller 的全部 114 个 Mapping。
- URL 与 Controller 路径逐项核对，并统一补充前端使用的 `/competition` 网关前缀。
- 已记录每个接口的权限码、参数类型、返回类型和主要业务规则。
- 已单列 Request DTO 与主要 Response VO 字段。
- 已明确 `/review/record/**` 4 个旧接口虽然存在 Mapping，但运行时已禁用。
- 已覆盖管理端、填报人、专家端和秘书端重点流程。

## 6. 权限角色统计

文档矩阵包含 5 个对外角色：

1. 管理员 `ADMIN`
2. 评审专家 `REVIEWER`
3. 评审秘书 `SECRETARY`
4. 填报人 `OBJECT_OWNER`
5. 普通用户 `USER`

同时说明了活动内 `OPERATOR`、`AUDITOR` 角色，以及系统权限码、活动内角色和数据归属校验之间的区别。

## 7. 未覆盖内容

- 未连接运行数据库执行 `information_schema` 对账，因此字典描述的是迁移脚本目标结构，不保证某个历史环境已完整执行全部迁移。
- 未调用运行中接口或导出 Swagger/OpenAPI；接口文档以 Controller、DTO、VO 和 Service 实现为准。
- 未展开 Competition、FileTask、统一用户、菜单和文件存储模块的完整数据字典，只记录其与 Review Engine 的边界。
- 未分析部署环境中的实际角色菜单授权结果；权限矩阵表达代码要求和建议授权边界。
- 未生成公共用户结果门户、AI 接口或自动专家匹配接口，因为扫描代码中不存在这些实现。
- 本任务只生成文档，未修改业务代码、数据库、接口、配置或测试。

## 8. 发现的问题

1. Review Engine 尚未拆成独立服务，核心导入 Service 对 Competition/FileTask 耦合较深。
2. 数据库没有声明外键，完整性依赖 Service；历史库 `review_record` 可能存在兼容结构。
3. 秘书扫码复用 `object:query` 权限，存在角色权限耦合。
4. 旧评分接口保留路由但固定报错，容易被调用方误判为可用接口。
5. 部分枚举状态没有完整专用流转接口；撤回驳回不会落 `WITHDRAW_REJECTED`。
6. 部分评分完成时仍可生成当前平均分；同分排名、等级策略尚未通用化。
7. 现场专家页面采用 3 秒轮询，未实现 WebSocket/SSE，需评估并发压力。
8. 自动专家匹配、复杂回避、AI 辅助、通用动态表单和多轮自动流转未实现。
9. 缺少完整主流程、权限、迁移、并发和外部适配契约测试证据。

详细说明见 `review_engine_known_issues.md`。

## 9. 后续建议

1. 在目标环境执行一次迁移版本核对和 24 张表 schema diff，并把结果纳入发布清单。
2. 将本文 API 资产转成版本化 OpenAPI，并在 CI 中自动比对 Controller 变更。
3. 为 ADMIN、REVIEWER、SECRETARY、OBJECT_OWNER 建立可重复执行的最小权限初始化脚本和权限回归测试。
4. 优先抽取 `BusinessSourceAdapter`，将 Competition 与 FileTask 定向实现移出核心对象 Service。
5. 为评分提交、现场切换、结果生成/发布增加事务、并发、幂等和不可篡改专项测试。
6. 废弃或移除旧 `/review/record/**` 路由前，先完成调用方扫描和版本公告。
7. 以本文档集作为 V1.0 基线；后续表、Controller 或状态机变更必须同步更新文档和统计。

## 10. 完成标准核对

生成后校验结果：

- 通过自动对照确认 Controller 与文档均为 114 个唯一方法+路径，差集为 0；
- 通过自动对照确认迁移脚本与数据库字典均为 24 张业务表，差集为 0；
- 10 个 Markdown 文件的代码围栏成对，且每个文件只有一个一级标题；
- 使用 Mermaid 11.12.0 官方解析器实际解析 12 个 Mermaid 图块，失败数为 0；
- `git diff --check` 未发现文档空白错误。

|完成项|状态|
|-|-|
|`docs/review-engine` 目录|完成|
|数据库字典|完成|
|API 文档|完成|
|权限矩阵|完成|
|ER 图|完成|
|架构说明|完成|
|业务流程|完成|
|代码结构说明|完成|
|已知问题与规划|完成|
|文档生成报告|完成|
