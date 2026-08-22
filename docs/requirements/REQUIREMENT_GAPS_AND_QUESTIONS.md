# 需求缺口与业务确认问题

## 需确认清单

| ID | 领域 | 问题 | 证据等级 | 建议处理 |
| --- | --- | --- | --- | --- |
| G-001 | 系统边界 | `tianda-miniprogram` 目录未发现，当前按 `old-code-mini` 反推小程序需求。 | NEEDS_BUSINESS_CONFIRMATION | 确认小程序真实代码仓库。 |
| G-002 | 文件交互 | zip 文件是否只允许上传，还是也支持在线预览、下载打包、解压校验。 | NEEDS_BUSINESS_CONFIRMATION | 定义 zip 的业务场景、大小、压缩层级、病毒/压缩炸弹策略。 |
| G-003 | 现场证件 | 教师查看学生证件的授权关系来源不完全明确。 | STATIC_INFERENCE | 确认依据指导教师、团队关系、学校关系还是 grant。 |
| G-004 | 一证多权 | grant 的能力码、范围、失效规则需业务确认。 | STATIC_INFERENCE | 整理 ability_code 注册表。 |
| G-005 | operation_state | DONE 是否允许撤销、重复确认如何处理需要确认。 | NEEDS_BUSINESS_CONFIRMATION | 定义状态机和唯一键。 |
| G-006 | 资源预约 | 同一团队/个人在同一 slot 是否只能有一个有效预约。 | STATIC_INFERENCE | 确认业务规则并落实 active_key。 |
| G-007 | 评审 | 秘书控制对象状态与专家评分状态的优先级需要明确。 | STATIC_INFERENCE | 建立评审 session/object/submission/score/result 状态机。 |
| G-008 | 支付 | 补缴、退款、发票、报名状态之间的最终口径需确认。 | NEEDS_BUSINESS_CONFIRMATION | 定义订单和业务状态同步规则。 |
| G-009 | 导入 | 导入失败、部分成功、重复导入的用户可见行为需确认。 | NEEDS_BUSINESS_CONFIRMATION | 定义导入批次、错误行、重跑策略。 |
| G-010 | 旧能力 | 即时通讯、Flowable、旧评审/新评审并存功能是否仍然使用。 | NEEDS_BUSINESS_CONFIRMATION | 确认保留、冻结或归档策略。 |
| G-011 | 权限 | inner/pilot/callback 接口是否只允许内部或指定环境访问。 | NEEDS_BUSINESS_CONFIRMATION | 建立敏感接口网关策略。 |
| G-012 | 数据迁移 | 新结构是否先做影子数据，何时切换事实源。 | NEEDS_BUSINESS_CONFIRMATION | 制定旧读新写/双读对账/切写计划。 |

## 重构前建议确认顺序
1. 文件交互和 zip 策略。
2. 现场证件、grant、operation_state 的事实源关系。
3. 资源预约唯一性与容量规则。
4. 支付和报名状态同步口径。
5. 评审状态机。
6. 旧系统能力保留/冻结范围。
