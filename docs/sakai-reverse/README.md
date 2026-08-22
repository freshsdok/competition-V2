# Sakai 代码集反向设计文档

本目录基于 `/Users/wwang/Downloads/sakai-master.zip` 只读反向分析生成。

## 扫描摘要

- Maven 顶层 artifact：`base`
- 父版本：`26-SNAPSHOT`
- README 说明 master 是当前开发线，文中提到 Sakai 24；POM 显示当前源码父版本为 26-SNAPSHOT。
- POM 文件：464
- Java 文件：6530
- 顶层/声明模块：192
- 实体：168
- 服务类：502
- Controller：85
- REST/Spring Web/JAX-RS 相关类：74
- SQL/HBM 表候选：296

## 文档清单

- [总体反向设计概览](SAKAI_REVERSE_DESIGN_OVERVIEW.md)
- [多模块与工具架构](SAKAI_MODULE_TOOL_ARCHITECTURE.md)
- [Kernel、服务与组件模型](SAKAI_KERNEL_SERVICE_MODEL.md)
- [数据模型与 ER 图](SAKAI_DATA_MODEL_AND_ER.md)
- [站点、工具与权限模型](SAKAI_SITE_TOOL_PERMISSION_MODEL.md)
- [教学工具与集成特点](SAKAI_LEARNING_TOOLS_AND_INTEGRATIONS.md)
- [对当前项目的借鉴](SAKAI_REFERENCE_VALUE_FOR_DESHI.md)
- [模块清单 CSV](SAKAI_MODULE_INVENTORY.csv)
- [表清单 CSV](SAKAI_TABLE_INVENTORY.csv)
- [实体/服务清单 CSV](SAKAI_ENTITY_SERVICE_INVENTORY.csv)
- [证据索引 CSV](SAKAI_EVIDENCE_INDEX.csv)
