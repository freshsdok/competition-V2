# Canvas LMS 反向文档集

来源文件：`/Users/wwang/Downloads/canvas-lms-master.zip`。

生成方式：解压到临时目录后进行只读静态扫描，忽略 `__MACOSX` 和常规构建/日志目录。文档中的源码路径均为压缩包内部路径，如 `canvas-lms-master/app/controllers/...`。

证据等级：
- `CONFIRMED_BY_CODE`：路由、Controller、Model、migration、GraphQL、前端模块或测试目录明确存在。
- `STATIC_INFERENCE`：根据命名、目录、关联关系和框架约定推断。
- `NEEDS_BUSINESS_CONFIRMATION`：代码无法确认业务意图，需要产品/业务确认。

文档清单：
- `CANVAS_REVERSE_REQUIREMENTS_OVERVIEW.md`
- `CANVAS_ARCHITECTURE_RECONSTRUCTION.md`
- `CANVAS_FUNCTIONAL_REQUIREMENTS_BY_DOMAIN.md`
- `CANVAS_BUSINESS_PROCESS_RECONSTRUCTION.md`
- `CANVAS_DATA_MODEL_RECONSTRUCTION.md`
- `CANVAS_API_AND_ROUTE_MAPPING.md`
- `CANVAS_FRONTEND_MODULE_MAPPING.md`
- `CANVAS_INTEGRATION_AND_SECURITY_REQUIREMENTS.md`
- `CANVAS_REQUIREMENT_GAPS_AND_QUESTIONS.md`
- `CANVAS_EVIDENCE_INDEX.csv`


本轮深化新增：
- `CANVAS_CONFIRMED_REQUIREMENTS.md`：按证据索引归并后的逐项需求说明。
- `CANVAS_REQUIREMENTS_TRACEABILITY_MATRIX.md` / `.csv`：需求到证据的追踪矩阵。
- `CANVAS_SYSTEM_STRUCTURE_DIAGRAM.md`：系统结构和业务域依赖图。
- `CANVAS_CORE_ER_DIAGRAM.md`：核心 ER 图。
- `CANVAS_MULTI_TENANCY_USER_MANAGEMENT_REFERENCE.md`：多租户与用户管理特点、可借鉴点和当前项目落地建议。
