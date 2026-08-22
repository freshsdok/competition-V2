# 反向需求文档集

本目录基于当前代码仓库反向生成，用于重构前建立“现有系统实际功能需求基线”。

证据等级：
- `CONFIRMED_BY_CODE`：源码、前端页面、接口、Mapper 或 migration 明确存在。
- `STATIC_INFERENCE`：根据命名、调用路径、表结构和页面关系推断。
- `NEEDS_BUSINESS_CONFIRMATION`：代码无法确认业务意图，需要业务方确认。

文档清单：
- `REVERSE_ENGINEERED_REQUIREMENTS_OVERVIEW.md`：系统需求总览。
- `REVERSE_ENGINEERED_REQUIREMENTS_BY_DOMAIN.md`：按业务域拆分的功能需求。
- `BUSINESS_PROCESS_RECONSTRUCTION.md`：核心业务流程反推。
- `API_TO_FEATURE_MAPPING.md`：接口到功能映射。
- `PERMISSION_AND_ROLE_REQUIREMENTS.md`：权限与角色需求。
- `STATE_AND_RULE_REQUIREMENTS.md`：状态、规则和枚举需求。
- `REQUIREMENT_GAPS_AND_QUESTIONS.md`：需求缺口和需确认问题。

边界：本轮只做静态反向整理，未运行服务，未连接生产，未修改数据库。