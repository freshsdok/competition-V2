# Canvas LMS 反向需求缺口与确认问题

## 需要确认的问题

| ID | 领域 | 问题 | 证据等级 | 建议 |
| --- | --- | --- | --- | --- |
| C-G001 | 版本与运行环境 | zip 是 master 快照，是否对应目标部署版本未知。 | NEEDS_BUSINESS_CONFIRMATION | 确认 commit/tag、Canvas 版本和目标部署环境。 |
| C-G002 | 启用功能范围 | Canvas 中许多功能由 feature flags、插件和机构配置控制，静态代码不能判断实际启用。 | NEEDS_BUSINESS_CONFIRMATION | 导出目标环境 feature flags 和 account settings。 |
| C-G003 | 自定义改造 | 压缩包可能是上游代码，是否包含本地二开功能未知。 | NEEDS_BUSINESS_CONFIRMATION | 确认是否有本地分支或补丁。 |
| C-G004 | 数据库结构 | migration 可反推结构，但未运行 Rails schema，不能保证最终 schema。 | STATIC_INFERENCE | 在测试环境执行 schema dump 或读取现有 schema。 |
| C-G005 | 权限模型 | Canvas 权限大量分布在 policy/model/controller 中，静态 route 不足以描述完整权限。 | STATIC_INFERENCE | 专项生成权限矩阵。 |
| C-G006 | 前端入口 | React feature 与 Rails 视图混合，静态目录不能完全还原用户导航。 | STATIC_INFERENCE | 结合运行菜单/路由和页面截图确认。 |
| C-G007 | LTI/SIS/评分 | 这些是高风险集成域，需求需要按实际使用场景裁剪。 | NEEDS_BUSINESS_CONFIRMATION | 先确认机构是否使用 LTI Advantage、SIS batch、Grade Passback。 |
| C-G008 | 数据迁移 | 若要用 Canvas 作为参考或目标系统，需要映射本项目赛事/报名/评审/证件等业务到 Canvas 的课程/作业/评分模型。 | NEEDS_BUSINESS_CONFIRMATION | 单独做领域映射报告。 |

## 后续可执行任务
- 生成 Canvas 权限矩阵。
- 生成核心数据模型 ER 草图。
- 对比本项目业务域与 Canvas LMS 域模型。
- 抽取 Canvas 文件上传、作业提交、评分、SIS 导入、LTI 集成专项需求。
