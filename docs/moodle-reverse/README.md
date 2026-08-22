# Moodle 代码集反向设计文档

本目录基于 `/Users/wwang/Downloads/moodle-main.zip` 进行只读反向分析生成。分析对象为压缩包内 `moodle-main/public` 为主的 Moodle 源码，忽略 `__MACOSX`、依赖包和明显第三方库。

## 代码版本

- 版本文件：`public/version.php`
- `$release`：`5.3dev (Build: 20260420)`
- `$branch`：`503`
- `$version`：`2026042000.00`

## 文档清单

- [总体反向设计概览](MOODLE_REVERSE_DESIGN_OVERVIEW.md)
- [功能需求反向确认](MOODLE_FUNCTIONAL_REQUIREMENTS_RECONSTRUCTION.md)
- [架构与插件模型](MOODLE_ARCHITECTURE_AND_PLUGIN_MODEL.md)
- [数据模型与 ER 图](MOODLE_DATA_MODEL_AND_ER.md)
- [用户、权限与多租户特征](MOODLE_ROLE_PERMISSION_USER_MODEL.md)
- [核心业务流程](MOODLE_CORE_PROCESS_FLOWS.md)
- [接口、前端与测试模型](MOODLE_API_FRONTEND_TESTING_MODEL.md)
- [对本项目的可借鉴点](MOODLE_REFERENCE_VALUE_FOR_DESHI.md)
- [插件清单 CSV](MOODLE_PLUGIN_INVENTORY.csv)
- [表清单 CSV](MOODLE_TABLE_INVENTORY.csv)
- [证据索引 CSV](MOODLE_EVIDENCE_INDEX.csv)

## 扫描摘要

- 可识别插件目录：358
- XMLDB 定义表：487
- 权限点：774
- 外部服务注册项：521
- PHP 文件：14739
- Mustache 模板：1272

本文档是基于代码结构、数据库定义、权限注册、服务注册和关键调用点反推出来的设计说明，不代表 Moodle 官方产品文档的完整替代。
