# Canvas LMS 架构反向重建

## 架构判断
Canvas LMS 是大型 Ruby on Rails 单体应用，叠加 GraphQL、REST/API Controller、大量 Rails models、React/TypeScript 前端 feature、后台任务、插件/gems、LTI/SIS 集成和 Docker/Jenkins 构建体系。`CONFIRMED_BY_CODE`

## 主要层次

| 层次 | 证据 | 职责反推 |
| --- | --- | --- |
| Web/Rails Controller | 307 个 Controller 类 | 处理页面、REST API、LTI、SIS、文件、评分等请求。 |
| Routing | 2160 条 routes.rb 声明候选 | 组织课程、账户、API、文件、测验、GraphQL 等访问路径。 |
| Domain Model | 639 个 Model 类 | 承载课程、用户、作业、提交、评分、账户等核心业务状态和关联。 |
| Persistence/Migration | 128 个 migration 文件 | 维护 PostgreSQL/Rails 数据库结构演进。 |
| GraphQL | 388 条 GraphQL 类型/字段证据 | 提供现代 API 聚合层。 |
| Frontend | 496 个 feature/package 目录 | 提供 React/TypeScript/JS 页面和组件能力。 |
| Internal Gems | 56 个 gems 目录 | 封装 Canvas 平台扩展、缓存、安全、导入、LTI、文本处理等能力。 |
| Ops/CI | Dockerfile、docker-compose、Jenkinsfile、yarn.lock | 支持本地开发、CI、构建和部署。 |

## 核心依赖/配置线索

| 文件 | 路径 | 行数 |
| --- | --- | --- |
| Gemfile | Gemfile | 113 |
| Gemfile.lock | Gemfile.lock | 1447 |
| yarn.lock | yarn.lock | 24696 |
| package.json | package.json | 486 |
| docker-compose.yml | docker-compose.yml | 22 |
| Dockerfile | Dockerfile | 90 |
| config/database.yml.example | config/database.yml.example | 30 |
| config/cache_store.yml.example | config/cache_store.yml.example | 18 |
| config/security.yml.example | config/security.yml.example | 22 |
| config/redis.yml.example | config/redis.yml.example | 28 |

## 架构风险与理解重点
- 单体体量很大，领域边界依赖命名和目录约定，需要按课程/作业/评分/LTI/SIS 等域建立读图。
- REST、GraphQL 和前端 feature 并存，同一功能可能有多个入口。
- Model 关联密集，重构或抽取服务前必须先建立数据模型和权限模型。
- LTI、SIS、文件和评分属于高风险集成域。
