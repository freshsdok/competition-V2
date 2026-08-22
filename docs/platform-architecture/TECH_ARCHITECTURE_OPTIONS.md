# 技术架构与框架备选方案

## 1. 当前技术基础

当前项目已经不是从零开始。现有代码基础大致是：

```text
后端：
  Java 17
  Spring Boot 3.3.5
  Spring Cloud 2023.0.3
  Spring Cloud Alibaba 2023.0.1.2
  Gateway / Auth / Modules / API / Common 多模块
  MyBatis / MyBatis Plus / Mapper XML
  Redis / MinIO / Seata / Nacos 等微服务常见组件

前端：
  Vue 3
  Vite
  Element Plus
  Pinia
  VXE Table
  ECharts
  bpmn-js
  uni-app / 小程序端
```

因此，技术选型不应重新推倒，而应围绕以下目标评估：

1. 是否能支撑多租户、多组织、多 Workspace、多 Tool。
2. 是否能复用现有代码和团队经验。
3. 是否能逐步迁移，不要求一次性重写。
4. 是否能区分确定性流程和 AI 辅助。
5. 是否能控制复杂度，避免“大平台空转”。

## 2. 总体可选架构

### 方案 A：保守演进型，继续基于现有 Spring Cloud / RuoYi Cloud 体系

#### 架构形态

```text
teaching-gateway
teaching-auth
teaching-system
teaching-competition
teaching-wxApp
新增：
  teaching-platform-core
  teaching-workspace
  teaching-tool-registry
  teaching-ai-task
```

前端继续使用：

```text
Vue 3 + Vite + Element Plus
```

#### 核心思路

在现有微服务结构上增加平台骨架，不立即重构所有历史业务。

优先补：

```text
tenant
organization
workspace
workspace_tool
permission_grant
file_relation
workflow_state
operation_log
```

#### 优点

- 风险最低，最贴近当前项目。
- 团队学习成本低。
- 可以逐步把赛事和产业学院接入同一平台骨架。
- 不需要一次性迁移数据库。
- 现有前端、网关、鉴权、文件、Redis、配置中心可以继续使用。

#### 缺点

- 旧模块边界仍然存在，短期内会有新旧模型并存。
- 如果不设治理规则，容易继续堆表和堆接口。
- 现有微服务拆分未必等于领域边界，需要重整模块职责。

#### 适用阶段

```text
近期最推荐。
```

适合先做平台骨架和两个样板 Workspace。

---

### 方案 B：模块化单体内核，外围保留现有微服务

#### 架构形态

新建一个平台内核服务：

```text
platform-core
  tenant
  organization
  workspace
  tool
  permission
  file
  workflow
  log
  assessment
  resource
  certificate
```

历史业务服务逐步调用平台内核：

```text
competition -> platform-core
industry-college -> platform-core
wxApp -> platform-core
```

#### 可选框架

- Spring Boot；
- Spring Modulith；
- MyBatis / MyBatis Plus；
- Spring Events / 事务事件；
- Outbox 表。

#### 核心思路

把平台公共能力先做成一个“模块化单体内核”，内部按领域包隔离，但先不拆成十几个独立微服务。

#### 优点

- 比全微服务简单。
- 事务和一致性更容易控制。
- 适合快速打磨核心模型。
- 比传统单体更有模块边界。
- 后续如果某个模块成熟，可以再拆服务。

#### 缺点

- 和现有 Spring Cloud 体系要处理调用边界。
- 需要严格包边界、接口边界和测试，否则会退化成大单体。
- 对开发规范要求高。

#### 适用阶段

```text
非常适合作为平台内核建设方案。
```

如果担心现有微服务复杂度继续扩大，可以优先选这个。

---

### 方案 C：领域微服务平台，完整拆分 Core Services

#### 架构形态

```text
identity-service
organization-service
workspace-service
tool-service
permission-service
file-service
workflow-service
resource-service
assessment-service
certificate-service
payment-service
notification-service
ai-task-service
```

#### 核心思路

每个核心能力都是独立服务，业务 Tool 通过服务调用完成能力复用。

#### 优点

- 边界清晰。
- 适合大团队并行。
- 不同服务可以独立扩展。
- 长期平台化能力强。

#### 缺点

- 当前阶段过重。
- 分布式事务、链路追踪、部署、监控、版本兼容成本高。
- 业务模型还没稳定时，过早拆服务会导致大量返工。
- 对团队工程治理要求非常高。

#### 适用阶段

```text
中长期可选，不建议现在作为第一步。
```

更适合在平台骨架稳定、业务边界明确后逐步拆分。

---

### 方案 D：低代码/流程引擎驱动平台

#### 架构形态

```text
平台核心数据模型
  + 表单设计器
  + 流程引擎
  + BPMN
  + 动态表单
  + 动态审批
```

可选框架：

- Flowable；
- Camunda 8；
- Activiti；
- 自研轻量状态机；
- 前端 bpmn-js 作为建模器。

#### 核心思路

把报名审核、材料审核、证件发放、评审确认、资源审批、证书发放等流程交给流程引擎。

#### 优点

- 对审批流、人工任务、流程可视化很友好。
- 可以让业务人员理解流程图。
- 节点、任务、流转、历史记录能力成熟。

#### 缺点

- 流程引擎不能替代业务模型。
- 过早引入会把简单状态机复杂化。
- 赛事评审、资源预约这类高并发/强约束逻辑不能只靠 BPMN。
- 引擎表和业务表之间要设计清楚，否则排错困难。

#### 适用阶段

适合两类场景：

```text
1. 人工审批多、流程经常变；
2. 需要流程可视化和合规审计。
```

不适合把所有状态都交给流程引擎。

---

### 方案 E：前后端强分离 + 统一 Portal Shell

#### 架构形态

```text
前端：
  Vue 3 + Vite + Element Plus
  PortalLayout
  WorkspaceShell
  ToolContainer
  RecordDetailPage

后端：
  REST API / OpenAPI
  Workspace / Tool / Record 服务
```

可选增强：

- Vue 3；
- Element Plus；
- VXE Table；
- Pinia；
- Vue Router；
- OpenAPI 类型生成；
- 微前端：qiankun / Module Federation。

#### 核心思路

前端先统一“平台外壳 + Workspace + Tool”页面模型，不追求每个 Tool 独立前端应用。

#### 优点

- 最贴合当前前端技术栈。
- 能快速做出统一体验。
- 易于支撑管理后台、专家端、学生端、企业端差异化首页。
- 可以逐步替换旧页面。

#### 缺点

- 如果前端状态和权限模型不统一，会出现多个入口、多套菜单。
- 微前端过早引入会增加构建、部署和通信复杂度。
- 需要统一设计系统，否则 Element 页面会变成表格堆叠。

#### 适用阶段

```text
近期推荐。
```

前端建议先统一 Shell，不急着做微前端。

---

### 方案 F：AI 服务独立化

#### 架构形态

```text
业务系统
  ai-task-service
    ai_task
    ai_input
    ai_output
    ai_suggestion
    ai_review_decision
  AI Adapter
    OpenAI / 本地模型 / OCR / 向量检索
```

#### 核心思路

AI 不嵌入每个业务模块，而是统一作为建议层服务。

#### 优点

- 避免每个业务模块各接一套 AI。
- 可以统一记录输入、输出、模型版本、置信度、人工采纳结果。
- 便于合规审计。
- 方便后续更换模型供应商。

#### 缺点

- 初期需要额外设计 AI 任务和建议模型。
- AI 结果不能直接写业务事实表，需要业务流程配合。
- 对文件解析、OCR、向量检索、权限过滤要求较高。

#### 适用阶段

```text
建议作为平台公共服务建设，但先从低风险场景开始。
```

例如材料摘要、缺项提醒、评语草稿、专家匹配建议。

## 3. 流程与状态框架选择

### 3.1 自研轻量状态机

#### 适合

- 报名状态；
- 预约状态；
- 证件状态；
- 支付状态；
- 评审提交状态；
- 节点开放/关闭状态。

#### 优点

- 简单直接。
- 数据库可控。
- 与现有业务表容易结合。
- 性能和一致性好控制。

#### 缺点

- 不适合复杂多人审批流。
- 流程可视化弱。
- 流程变更需要开发介入。

#### 建议

```text
近期默认选项。
```

先做：

```text
workflow_state
state_transition_rule
operation_log
```

### 3.2 Flowable

#### 适合

- 审批流；
- 人工任务；
- 表单流程；
- 可视化 BPMN；
- 与 Spring Boot 集成。

#### 优点

- Java 体系内集成自然。
- BPMN 2.0 能表达人工任务和审批流。
- 比 Camunda 8 部署简单。

#### 缺点

- 会引入一套引擎表。
- 业务人员未必能维护复杂 BPMN。
- 高并发预约、评分计算等不适合完全放入流程引擎。

#### 建议

```text
中期用于复杂审批，不作为所有状态的事实源。
```

### 3.3 Camunda 8

#### 适合

- 跨系统编排；
- 长流程；
- 人、系统、AI agent 混合流程；
- 高治理要求流程。

#### 优点

- 流程编排能力强。
- BPMN/DMN 治理能力好。
- 适合中大型企业级流程。

#### 缺点

- 部署和运维复杂。
- 对当前阶段偏重。
- 团队需要学习 Zeebe/Camunda 体系。

#### 建议

```text
暂不建议近期引入。
```

除非后续明确要做跨系统流程编排平台。

### 3.4 Temporal

#### 适合

- 长时间运行任务；
- 分布式可靠执行；
- 外部系统调用重试；
- AI/文件处理/异步任务编排。

#### 优点

- 对失败恢复、重试、长任务非常强。
- 适合异步可靠执行。

#### 缺点

- 不是传统审批流引擎。
- 需要单独运行 Temporal 服务。
- 对 Java 团队有额外学习成本。

#### 建议

```text
适合未来 AI 文件处理、批量导入、通知、异步任务，不适合替代业务状态机。
```

## 4. 多租户数据架构选择

### 4.1 单库共享表 + tenant_id

#### 优点

- 实现最简单。
- 现有数据库改造成本最低。
- 报表和平台统计方便。
- 适合当前数据量。

#### 缺点

- 必须严格做数据权限过滤。
- 大租户隔离性弱。
- 后续如果单租户数据很大，需要再分区或拆库。

#### 建议

```text
近期推荐。
```

所有核心表补 `tenant_id` 或通过 Workspace 间接追溯。

### 4.2 单库多 schema

#### 优点

- 租户隔离强于共享表。
- 备份恢复可按 schema 做。

#### 缺点

- 运维复杂。
- 跨租户统计麻烦。
- MyBatis 和迁移脚本复杂度上升。

#### 建议

```text
暂不建议。
```

### 4.3 租户独立数据库

#### 优点

- 隔离最强。
- 单租户备份恢复清晰。
- 大客户定制空间大。

#### 缺点

- 运维成本最高。
- 平台统计复杂。
- 版本升级和迁移脚本成本高。

#### 建议

```text
只适合未来大客户私有化或超大租户。
```

### 4.4 混合模式

```text
中小租户：共享库 + tenant_id
大租户：独立库
```

#### 建议

```text
作为长期预留，不作为第一阶段实现。
```

## 5. 身份认证与权限框架选择

### 5.1 沿用现有 Auth / Security

#### 优点

- 改造成本低。
- 和现有菜单、角色、Token 机制兼容。
- 最快能落地。

#### 缺点

- 多组织、多 Workspace、多角色能力需要补建。
- 外部 SSO、OIDC、SAML、企业微信等能力可能不足。

#### 建议

```text
近期推荐：保留现有认证，增强授权模型。
```

### 5.2 引入 Keycloak / OIDC

#### 优点

- 标准协议支持好：OIDC、OAuth2、SAML。
- 适合外部身份、统一登录、第三方系统集成。
- 多租户身份治理能力更强。

#### 缺点

- 引入新运维组件。
- 需要改造登录、Token、用户同步、角色映射。
- 不会自动解决业务权限，还需要平台 Permission Service。

#### 建议

```text
中期可选。
```

适合明确需要统一身份、学校/企业 SSO、第三方身份源时引入。

## 6. 前端框架选择

### 6.1 继续 Vue 3 + Element Plus

#### 优点

- 当前项目已经使用。
- 管理后台开发效率高。
- 表单、表格、弹窗、树、上传等组件成熟。
- 与现有代码兼容。

#### 缺点

- 如果没有统一设计系统，容易变成“表格 + 弹窗”堆砌。
- 复杂工作台需要额外设计组件。

#### 建议

```text
近期推荐。
```

重点不是换框架，而是做统一 PortalLayout / WorkspaceShell / ToolContainer。

### 6.2 微前端

可选：

- qiankun；
- Module Federation；
- iframe 隔离。

#### 优点

- 多团队并行。
- 历史前端可逐步接入。
- 不同 Tool 可以独立发布。

#### 缺点

- 对当前阶段偏重。
- 权限、路由、状态、样式隔离复杂。
- 容易出现 Sakai 式工具割裂感。

#### 建议

```text
先不引入。
```

等 Tool 边界稳定、团队规模扩大后再评估。

### 6.3 低代码表单

可选：

- 现有 `vform3-builds`；
- 自研表单元数据；
- 第三方表单设计器。

#### 优点

- 适合报名表、材料表、项目申报表。
- 配置化效率高。

#### 缺点

- 复杂业务规则仍要后端确定性校验。
- 表单模型容易和正式业务表脱节。

#### 建议

```text
可作为 Tool 内部能力，但不要替代业务模型。
```

## 7. 推荐组合方案

### 7.1 近期推荐架构

```text
后端：
  保留 Spring Cloud / Spring Boot 现有体系
  新增 platform-core 或 workspace 平台内核
  先使用共享库 + tenant_id
  先使用轻量状态机
  AI 独立为 ai-task-service

前端：
  继续 Vue 3 + Vite + Element Plus
  建立统一 PortalLayout / WorkspaceShell
  先做赛事和产业学院两个样板 Workspace

权限：
  沿用现有登录
  新增 Permission Service / Grant / Scope

流程：
  先自研状态机
  中期引入 Flowable 处理复杂审批
```

### 7.2 中期演进

```text
平台内核稳定后：
  拆分 Identity / Organization / Workspace / File / Permission 等服务
  引入 Keycloak 或 OIDC 统一身份
  对复杂审批引入 Flowable
  对异步长任务评估 Temporal
  对大租户预留独立库能力
```

### 7.3 不建议近期做

```text
不建议直接全量微服务重构；
不建议直接引入 Camunda 8 做所有流程；
不建议前端一开始微前端化；
不建议把 AI 做成直接写业务事实的核心引擎；
不建议租户一开始独立数据库；
不建议推倒当前 Vue/Spring 技术栈重来。
```

## 8. 最终建议

最稳妥的技术路线是：

```text
现有 Spring Cloud 体系
  + 平台内核 platform-core
  + Workspace / Tool / Node / Record 模型
  + 轻量状态机
  + 统一权限、文件、日志
  + Vue 统一 Workspace Shell
  + AI Task Service 作为建议层
```

这样既能支撑长期平台化，又不会在第一阶段失控。

## 9. 参考资料

- Spring Boot 官方文档：<https://docs.spring.io/spring-boot/>
- Spring Cloud 官方项目页：<https://spring.io/projects/spring-cloud>
- Spring Modulith 官方文档：<https://docs.spring.io/spring-modulith/reference/index.html>
- Element Plus 官方文档：<https://element-plus.org/>
- Flowable Spring Boot 文档：<https://www.flowable.com/open-source/docs/bpmn/ch05a-Spring-Boot>
- Camunda 8 Processes 文档：<https://docs.camunda.io/docs/components/concepts/processes/>
- Temporal 官方文档：<https://docs.temporal.io/>
- Keycloak OIDC 文档：<https://www.keycloak.org/securing-apps/oidc-layers>
