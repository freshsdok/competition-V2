# 代码审计发现清单

| ID | 类别 | 级别 | 证据等级 | 位置 | 问题 | 建议 |
| --- | --- | --- | --- | --- | --- | --- |
| F-001 | SECURITY | P1 | CONFIRMED_BY_CODE | old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java:25 | 现场“一证多权”存在 pilot confirm 路由 | 将 pilot 路由纳入环境开关、权限注解和网关白名单审查；上线前确认是否下线。 |
| F-002 | SECURITY | P1 | CONFIRMED_BY_CODE | old-code-admin/src/views/content/detailPage/index.vue:41 | 前端多处使用 v-html 渲染服务端内容 | 只允许已净化字段进入 v-html；统一封装富文本组件并标记 sanitized 来源。 |
| F-003 | SECURITY | P1 | CONFIRMED_BY_CODE | old-code-admin/src/components/Editor/index.vue:127 | 小程序和管理端保留大量 console.log | 上线构建移除 console；对敏感响应启用脱敏日志。 |
| F-004 | SECURITY | P1 | STATIC_INFERENCE | old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/service/OSSFileServiceImpl.java:69 | 文件上传后端类型校验需要复核 | 在后端统一校验扩展名、MIME、大小和业务场景白名单；zip 新增时同步病毒/压缩炸弹策略。 |
| F-005 | DATA_CONSISTENCY | P1 | CONFIRMED_BY_CODE | db/migration/20260701_competition_scene_resource_p1_001.sql:120 | 资源预约已存在 idempotency_key 唯一键但调用侧仍可能触发重复冲突 | 先查幂等结果再插入，捕获唯一键冲突并返回既有预约；保留数据库唯一键。 |
| F-006 | TRANSACTION | P1 | STATIC_INFERENCE | old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java:706 | 资源预约涉及查重、容量扣减、预约插入、状态更新的复合事务 | 为预约/取消梳理事务边界和条件更新；关键 Mapper 使用 affected rows 判定。 |
| F-007 | STATUS | P1 | CONFIRMED_BY_CODE | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:13 | CANCELED/CANCELLED 枚举拼写并存 | 建立统一状态注册表；旧值映射为兼容枚举，新增代码只使用一个标准拼写。 |
| F-008 | ARCHITECTURE | P1 | STATIC_INFERENCE | old-code/teaching-modules/teaching-competition | competition 模块承载赛事、报名、现场、资源、评审、支付协同逻辑 | 按“赛事报名/现场证件/资源预约/评审”拆分应用服务或包边界，先做接口隔离。 |
| F-009 | SQL | P1 | STATIC_INFERENCE | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionApplyInfoMapper.xml:233 | Mapper XML 动态 SQL 需要逐项排查 ${} 注入面 | 所有 ${} 必须限定枚举白名单；能用 #{} 的全部改为 #{}。 |
| F-010 | TEST | P1 | CONFIRMED_BY_CODE | old-code/teaching-modules/teaching-competition/src/test/java:1 | 测试集中在 competition 模块，认证/网关/文件/支付/前端覆盖不足 | 补充 auth、file、payment、gateway、frontend 关键路径测试；先覆盖 P1 风险。 |
| F-011 | SECURITY | P2 | CONFIRMED_BY_CODE | old-code-pc/src/main.js:38 | 前端将上传/下载能力挂到 window 或全局属性 | 改为模块化 import 或组合式 hooks；下载前后端均校验文件归属。 |
| F-012 | SECURITY | P2 | CONFIRMED_BY_CODE | old-code/docker/docker-compose.yml:40 | 配置文件中出现密码/密钥类配置项 | 迁移到环境变量/密钥管理；示例配置用占位符。 |
| F-013 | OPERABILITY | P2 | CONFIRMED_BY_CODE | old-code/logs, old-code/teaching-auth/logs, old-code/teaching-modules/teaching-competition/logs | 仓库包含运行日志目录 | 将 logs 加入忽略清单并清理历史提交；日志只进入运行环境。 |
| F-014 | SQL | P2 | CONFIRMED_BY_CODE | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:53 | Mapper 中存在大量 like concat 模糊查询 | 为高频查询设计搜索字段/索引或引入检索方案；分页接口压测。 |
| F-015 | BACKEND_QUALITY | P2 | CONFIRMED_BY_CODE | old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:60 | 后端大量 catch(Exception) 粗粒度捕获 | 按业务异常、外部依赖异常、系统异常分层处理；避免吞异常。 |
| F-016 | BACKEND_QUALITY | P2 | CONFIRMED_BY_CODE | old-code/teaching-auth/src/main/java/com/teaching/auth/TeachingAuthApplication.java:24 | 后端存在 System.out.println | 统一改为日志框架，删除启动横幅之外的调试输出。 |
| F-017 | FRONTEND_QUALITY | P2 | CONFIRMED_BY_CODE | old-code-mini/pages/scene-resource/index.vue, old-code-mini/pages/my-credential/index.vue | 小程序页面承担请求、状态、渲染、业务判断多重职责 | 拆分 API hooks、业务状态机和展示组件；保留页面只做编排。 |
| F-018 | BUILD | P2 | STATIC_INFERENCE | old-code-admin/package.json, old-code-pc/package.json; old-code-mini 根目录未发现 package.json | 前端多项目依赖和构建体系分散 | 建立前端依赖基线和构建矩阵；锁定 Node/npm/pnpm 版本。 |
| F-019 | BUILD | P2 | CONFIRMED_BY_CODE | old-code/.idea, old-code/bin, old-code-admin/dist, old-code-pc/node_modules | 仓库工作区存在 IDE、构建和依赖目录 | 确认 .gitignore 和归档脚本排除过程文件；若已入库需单独清理。 |
| F-020 | ARCHITECTURE | P2 | CONFIRMED_BY_CODE | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant | common-core 承载业务枚举和支付状态 | 将业务枚举迁移到领域模块或独立 contract 包。 |
| F-021 | BUSINESS_ALGORITHM | P2 | STATIC_INFERENCE | old-code/teaching-modules/teaching-competition/src/main/java | 证件、grant、operation_state、operation_log 状态事实源分散 | 明确 operation_state 为操作事实、credential 为主体事实、log 为审计事实。 |
| F-022 | DATA_CONSISTENCY | P2 | STATIC_INFERENCE | db/migration/20260702_competition_scene_credential_scope_p1.sql | operation_state DONE 唯一性需要复核 | 为 active/DONE 状态引入 active_key 或条件唯一索引。 |
| F-023 | DATA_CONSISTENCY | P2 | STATIC_INFERENCE | db/migration/20260705_competition_scene_credential_scope_grant_pilot_p1.sql | active grant 唯一性需要按能力/范围校验 | 建立 credential_id + schedule_id + ability_code + active_key 唯一约束。 |
| F-024 | SQL | P2 | STATIC_INFERENCE | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/AwardDetailsMapper.xml:48 | Mapper XML 体量大且业务 SQL 分散 | 对高频 Mapper 建立查询清单、索引说明和 SQL 单测。 |
| F-025 | TEST | P2 | CONFIRMED_BY_CODE | old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewModulePhase1SmokeTest.java:76 | 已有评审/现场模块单测，但缺少端到端并发用例 | 新增并发集成测试和幂等重放测试，使用测试库读写隔离。 |
| F-026 | SECURITY | P2 | STATIC_INFERENCE | old-code/teaching-gateway, old-code/teaching-auth, controllers | 权限策略横跨网关、认证、控制器注解 | 为 Controller 生成权限清单并与 sys_menu migration 对账。 |
| F-027 | FRONTEND_QUALITY | P2 | CONFIRMED_BY_CODE | tianda-miniprogram: NOT_FOUND; old-code-mini: audited | 需求中的 tianda-miniprogram 目录未发现 | 确认是否以 old-code-mini 代表小程序；若另有仓库需补充审计。 |
| F-028 | DEPLOYMENT | P2 | STATIC_INFERENCE | scripts/, old-code/docker, bootstrap*.yml | 部署脚本、Docker、Nacos 配置来源分散 | 统一配置模板、发布清单和回滚剧本；敏感配置只走环境变量。 |
| F-029 | BACKEND_QUALITY | P3 | STATIC_INFERENCE | old-code/teaching-modules/teaching-competition/domain, vo, dto | DTO/VO/Entity 命名边界不稳定 | 逐步引入请求/响应对象和转换层，先从高风险接口开始。 |
| F-030 | ARCHITECTURE | P3 | STATIC_INFERENCE | old-code/teaching-modules/teaching-system, teaching-competition review classes | 新旧评审能力并存 | 冻结旧入口写入，建立迁移映射和只读兼容层。 |
| F-031 | BUILD | P3 | STATIC_INFERENCE | pom.xml/package.json | 未发现统一质量门禁报告 | 建立 CI：编译、单测、lint、依赖扫描、关键 SQL 检查。 |
| F-032 | SQL | P3 | CONFIRMED_BY_CODE | db/migration/*.sql | 迁移脚本包含多个阶段性现场/评审变更 | 补充 migration 索引、执行顺序和回滚/验证说明。 |
| F-033 | OPERABILITY | P3 | STATIC_INFERENCE | scripts/review/*.py | 评审 UAT 脚本直接处理数据库连接参数 | 脚本默认拒绝生产 host；要求显式 --env test/uat。 |
| F-034 | SECURITY | P3 | STATIC_INFERENCE | old-code-mini/utils/request.js, old-code-admin/src/utils/request.js | 客户端 token 刷新和错误处理分散 | 统一 token 失效码、刷新策略和退出流程。 |
| F-035 | TEST | P3 | CONFIRMED_BY_CODE | old-code-admin, old-code-pc, old-code-mini | 未发现前端测试目录 | 为关键页面增加组件测试或 Playwright 冒烟测试。 |
