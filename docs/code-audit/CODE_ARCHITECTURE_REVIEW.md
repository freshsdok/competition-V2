# 代码架构审计

## 总体判断
系统是典型 Spring Cloud/RuoYi 风格多模块后端 + 多前端工程。核心业务已经从赛事报名扩展到现场证件、扫码、资源预约、评审、支付、内容和文件，业务复杂度主要集中在 `teaching-competition`。当前主要问题不是“代码是否能跑”，而是领域边界、状态事实源、唯一性和并发一致性没有形成统一治理层。

## 分层与依赖

| 层/模块 | 现状 | 风险 | 建议 |
| --- | --- | --- | --- |
| 网关/认证 | `teaching-gateway`、`teaching-auth` 承担入口、登录、token。 | 权限放行、菜单权限和 Controller 注解容易漂移。 | 生成接口权限清单并和菜单 migration 对账。 |
| 公共模块 | `teaching-common-*` 提供核心工具、日志、Redis、数据源、swagger 等。 | `common-core` 出现业务枚举/支付状态，公共模块被业务污染。 | 将业务常量下沉到领域 contract 包。 |
| 赛事业务 | `teaching-competition` 包含赛事、报名、团队、现场、资源、评审等。 | 模块过大，跨表事务和跨域状态难维护。 | 按领域拆应用服务边界，先拆包与接口，不急于拆服务。 |
| 系统/文件/内容 | `teaching-system`、`teaching-file`、`teaching-content` 独立存在。 | 文件、内容和前端下载链路需要统一鉴权与类型策略。 | 建立文件访问策略服务。 |
| 前端 | 管理端、PC、小程序三套工程。 | 接口封装和权限/登录态处理不统一。 | 建立跨端 API contract 与错误码规范。 |

## 核心问题

- `teaching-competition` 是事实上的业务“大单体”，现场证件、grant、operation_state、operation_log、资源预约、评审同时在该模块演进，推荐先做包内领域边界和接口清单。
- 状态事实分散：credential、grant、operation_state、reservation、review、pay 均有 status 字段，缺少统一枚举注册表。
- 前后端合同不足：接口返回对象、状态枚举、字典值在前端被硬编码消费，容易在状态拼写变化时失配。
- 工程目录污染：日志、构建产物、依赖目录、IDE 目录出现在工作区，应从源代码包和审计统计中排除。

## 关键证据

| 证据 | 位置 |
| --- | --- |
| competition 模块承载赛事、报名、现场、资源、评审、支付协同逻辑 | old-code/teaching-modules/teaching-competition |
| 仓库包含运行日志目录 | old-code/logs, old-code/teaching-auth/logs, old-code/teaching-modules/teaching-competition/logs |
| 前端多项目依赖和构建体系分散 | old-code-admin/package.json, old-code-pc/package.json, old-code-mini/package.json(未发现根 package.json) |
| 仓库工作区存在 IDE、构建和依赖目录 | old-code/.idea, old-code/bin, old-code-admin/dist, old-code-pc/node_modules |
| common-core 承载业务枚举和支付状态 | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant |
| 新旧评审能力并存 | old-code/teaching-modules/teaching-system, teaching-competition review classes |
| 未发现统一质量门禁报告 | pom.xml/package.json |
| 评审 UAT 脚本直接处理数据库连接参数 | scripts/review/*.py |
