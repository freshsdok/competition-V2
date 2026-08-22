# 代码库总体审计报告

## 1. 总体评价
本系统已经从通用教学/赛事平台演进为包含赛事、报名、现场证件、扫码、资源预约、评审、支付、文件和内容的综合业务系统。现有代码具备较完整业务覆盖，但架构边界、状态事实源、并发幂等、安全治理和测试基线明显落后于业务复杂度。

## 2. 代码规模与业务域分布

| 范围 | 文件数 | 行数 |
| --- | --- | --- |
| old-code | 1817 | 240854 |
| old-code-admin | 478 | 128995 |
| old-code-mini | 320 | 43132 |
| old-code-pc | 291 | 78543 |
| db | 23 | 3216 |
| scripts | 3 | 492 |

核心业务集中在 `old-code/teaching-modules/teaching-competition`；前端分为管理端、PC 端和小程序端；数据库变更集中在 `db/migration`。

## 3. 核心代码主轴
- 用户/权限：gateway/auth/system。
- 赛事/报名/团队：competition 基础主轴。
- 现场：schedule_target、credential、grant、operation_state、operation_log。
- 资源：resource、schedule_resource、slot、reservation。
- 评审：review session/object/submission/score/result。
- 文件：file 模块与前端 upload/download 封装。

## 4. 高风险问题

| ID | 级别 | 问题 | 位置 |
| --- | --- | --- | --- |
| F-001 | P1 | 现场“一证多权”存在 pilot confirm 路由 | old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java:25 |
| F-002 | P1 | 前端多处使用 v-html 渲染服务端内容 | old-code-admin/lib/vform/designer.umd.js:2067 |
| F-003 | P1 | 小程序和管理端保留大量 console.log | old-code-admin/lib/vform/designer.umd.js:13 |
| F-004 | P1 | 文件上传后端类型校验需要复核 | old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/service/OSSFileServiceImpl.java:69 |
| F-005 | P1 | 资源预约已存在 idempotency_key 唯一键但调用侧仍可能触发重复冲突 | db/migration/20260701_competition_scene_resource_p1_001.sql:120 |
| F-006 | P1 | 资源预约涉及查重、容量扣减、预约插入、状态更新的复合事务 | old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java:706 |
| F-007 | P1 | CANCELED/CANCELLED 枚举拼写并存 | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:13 |
| F-008 | P1 | competition 模块承载赛事、报名、现场、资源、评审、支付协同逻辑 | old-code/teaching-modules/teaching-competition |
| F-009 | P1 | Mapper XML 动态 SQL 需要逐项排查 ${} 注入面 | old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionApplyInfoMapper.xml:233 |
| F-010 | P1 | 测试集中在 competition 模块，认证/网关/文件/支付/前端覆盖不足 | old-code/teaching-modules/teaching-competition/src/test/java:1 |

## 5. 冗余与重复语义
状态、角色、组别、证件、团队、赛程、资源预约等字段在多表多端重复出现；部分应作为快照字段保留，部分应统一为事实源字段。建议和数据库审计的字段语义报告合并治理。

## 6. 缺失或需强化的约束
重点复核 active grant、operation_state DONE、团队/个人有效预约、资源 slot group scope、schedule resource scope、支付回调流水、团队报名和成员唯一性。现有 `idempotency_key` 唯一键是正确方向，但应用层应提供幂等响应。

## 7. 安全与合规
文件上传、富文本、调试日志、试点接口、配置密钥、下载鉴权是本轮最需要优先处理的安全面。

## 8. 测试与质量门禁
已有 competition 部分单测，但覆盖远不足以支撑并发、支付、文件、安全和多端前端。建议先建立 P1 风险回归集，再纳入 CI。

## 9. 近期优先级

| 优先级 | 任务 |
| --- | --- |
| P0 | 确认 zip 文件新增策略：后端白名单、大小、MIME、压缩包安全检测、下载鉴权。 |
| P0 | 处理 pilot 接口、v-html、console、敏感配置等安全风险。 |
| P1 | 资源预约和扫码 confirm 的幂等/并发测试与应用层幂等响应。 |
| P1 | 统一状态枚举，先治理 CANCELLED/CANCELED。 |
| P1 | 建立 Controller 权限清单与 sys_menu 对账。 |
| P2 | competition 包内领域拆分和 Mapper 风险清单。 |

## 10. 后续可执行任务清单
- 生成 Controller 权限清单。
- 生成 Mapper `${}` / like / deleted 条件专项清单。
- 为资源预约补并发测试设计。
- 为文件上传 zip 策略输出后端改造方案。
- 为前端 v-html 输出富文本净化改造清单。
- 为状态枚举建立注册表和兼容映射文档。

## 11. 本轮边界
本轮只读静态分析；未修改源代码，未格式化，未升级依赖，未修改数据库，未连接生产环境，未执行构建/测试。
