# Canvas LMS 反向需求总览

## 扫描摘要

| 证据类型 | 数量 |
| --- | --- |
| 源码/配置文件 | 19533 |
| Controller 类 | 307 |
| Controller action | 3675 |
| routes.rb 路由声明 | 2160 |
| Model 类 | 639 |
| Model 关联声明 | 1658 |
| migration 文件 | 128 |
| create_table 证据 | 301 |
| GraphQL 证据 | 388 |
| 前端 feature/package 目录 | 496 |
| 测试文件候选 | 4555 |

## 目录与技术栈线索

| 目录/文件类型 | 文件数 | 行数 |
| --- | --- | --- |
| ui | 9683 | 1442843 |
| spec | 3237 | 1025695 |
| app | 2515 | 352321 |
| packages | 1336 | 323696 |
| gems | 1071 | 135748 |
| lib | 820 | 104101 |
| doc | 321 | 48523 |
| config | 242 | 1703457 |
| db | 128 | 10116 |
| docker-compose | 31 | 568 |
| build | 17 | 898 |
| vendor | 16 | 1359 |
| Gemfile.d | 15 | 559 |
| ui-build | 15 | 1452 |
| inst-cli | 12 | 888 |
| .claude | 7 | 794 |
| .github | 7 | 354 |
| script | 7 | 1578 |
| public | 5 | 7782 |
| .vscode | 3 | 142 |
| jest | 2 | 54 |
| .codeclimate.yml | 1 | 3 |
| .dependency-cruiser.js | 1 | 483 |
| .devcontainer.json | 1 | 19 |
| .groovylintrc.json | 1 | 98 |

## 业务域证据分布

| 业务域 | 证据数量 |
| --- | --- |
| 其他 | 423 |
| 作业、提交与评分 | 204 |
| 用户、身份与沟通 | 161 |
| 测验与题库 | 149 |
| 账户与租户管理 | 138 |
| LTI、外部工具与集成 | 124 |
| 课程、班级与选课 | 122 |
| 内容、文件与模块 | 114 |
| 讨论、公告与协作 | 112 |
| SIS、导入导出与报表 | 57 |
| 搜索、AI 与无障碍 | 54 |
| 学习成果与能力 | 44 |
| 分析、审计与运维 | 38 |
| GraphQL API | 3 |

## 总体功能定位
Canvas LMS 是一个面向教育机构的多租户学习管理系统，核心围绕账户/子账户、课程、用户与选课、作业提交、评分、测验、讨论、文件、学习成果、LTI 外部工具、SIS 导入和通知沟通展开。`CONFIRMED_BY_CODE`

## 重构使用方式
- 用本目录作为理解 Canvas 源码结构和业务能力边界的索引。
- 大量功能由 Rails 路由、Controller、Model、GraphQL 和前端 feature 共同组成，反向需求需要结合证据索引逐项确认。
- 本轮未运行服务、未连接数据库、未安装依赖。
