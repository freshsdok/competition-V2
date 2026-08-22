# OpenOLAT 代码集反向设计文档

本目录基于 `/Users/wwang/Downloads/OpenOLAT-master.zip` 做只读反向分析生成。分析对象为临时解压目录 `/tmp/openolat-reverse-src/OpenOLAT-master`，未修改原始压缩包和源码。

## 输出文档

- [总体反向设计概览](OPENOLAT_REVERSE_DESIGN_OVERVIEW.md)
- [架构与分层模型](OPENOLAT_ARCHITECTURE_MODEL.md)
- [模块与扩展机制](OPENOLAT_MODULE_EXTENSION_MODEL.md)
- [数据模型与 ER 图](OPENOLAT_DATA_MODEL_AND_ER.md)
- [用户、组织与权限模型](OPENOLAT_USER_ORGANISATION_PERMISSION_MODEL.md)
- [课程、资源仓库与测评模型](OPENOLAT_COURSE_REPOSITORY_ASSESSMENT_MODEL.md)
- [服务端 UI 与前端模型](OPENOLAT_SERVER_SIDE_UI_MODEL.md)
- [OpenOLAT 特点与对本项目的借鉴](OPENOLAT_REFERENCE_VALUE_FOR_DESHI.md)
- [与 Moodle / Canvas 的对比](OPENOLAT_MOODLE_CANVAS_COMPARISON.md)
- [包清单 CSV](OPENOLAT_PACKAGE_INVENTORY.csv)
- [实体清单 CSV](OPENOLAT_ENTITY_INVENTORY.csv)
- [表清单 CSV](OPENOLAT_TABLE_INVENTORY.csv)
- [证据索引 CSV](OPENOLAT_EVIDENCE_INDEX.csv)

## 扫描摘要

- Maven artifact：`openolat-lms`
- 版本：`21.0-SNAPSHOT`
- 打包方式：WAR
- Java 文件：12246
- 测试 Java 文件：1151
- JPA 实体：381
- Controller 类：2941
- Service/Manager 类：1086
- REST/JAX-RS 相关类：119
- Spring XML：128
- Velocity 模板：2376
- PostgreSQL 初始建表：339 张
