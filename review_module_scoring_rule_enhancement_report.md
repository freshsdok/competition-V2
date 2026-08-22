# 评审模块评分表配置增强包执行报告

生成时间：2026-07-07

## 1. 完成的页面

- 新增管理端页面：`old-code-admin/src/views/review/rule/index.vue`
- 菜单路径：评审管理 / 评分表配置
- 页面路径：`/review/rule`
- 页面能力：
  - 评分规则列表查询；
  - 新增、编辑、删除评分规则；
  - 校验、启用、停用评分规则；
  - 复制评分规则；
  - 配置评分指标；
  - 指标上移、下移排序；
  - 绑定评分规则到评审轮次。

## 2. 完成的后端接口

- `GET /competition/review/rule/list`
- `GET /competition/review/rule/{id}`
- `POST /competition/review/rule`
- `PUT /competition/review/rule/{id}`
- `DELETE /competition/review/rule/{id}`
- `POST /competition/review/rule/{id}/validate`
- `POST /competition/review/rule/{id}/enable`
- `POST /competition/review/rule/{id}/disable`
- `POST /competition/review/rule/{id}/copy`
- `GET /competition/review/criteria/list`
- `POST /competition/review/criteria`
- `PUT /competition/review/criteria/{id}`
- `DELETE /competition/review/criteria/{id}`
- `POST /competition/review/round/{roundId}/bind-rule`

新增或增强文件：

- `ReviewRuleController`
- `ReviewCriteriaController`
- `ReviewRoundController`
- `IReviewRuleService`
- `IReviewRoundService`
- `ReviewRuleServiceImpl`
- `ReviewCriteriaServiceImpl`
- `ReviewRoundServiceImpl`
- `ReviewRuleValidateVO`
- `ReviewRoundBindRuleDTO`

## 3. 评分规则配置实现

- 新增规则默认保存为停用状态 `enabled = N`。
- 禁止新增时直接启用规则，避免没有指标或未校验的规则进入专家评分链路。
- 规则启用必须先通过后端校验。
- 规则启用状态只能通过专用启用/停用接口变更，普通编辑接口不能绕过校验。
- 规则已存在提交评分记录时，禁止直接修改核心字段，建议复制后修改。

核心字段包括：

- `score_mode`
- `total_score`
- `anonymous_mode`
- `activity_id`
- `round_id`

## 4. 评分指标配置实现

支持三类指标：

- `NUMBER`：数字评分；
- `SINGLE_CHOICE`：单选赋分；
- `TEXT`：文本评价。

校验规则：

- 指标名称必填；
- 指标类型必须为合法枚举；
- `NUMBER` 最高分必填，最低分默认 0，最高分不能小于最低分；
- `SINGLE_CHOICE` 必须配置合法选项 JSON；
- `TEXT` 不参与总分计算；
- 已提交评分记录关联的规则禁止新增、编辑、删除指标。

## 5. 单选项配置实现

前端提供可视化选项表格：

- 选项名称；
- 选项值；
- 分值；
- 排序。

保存时转换为 `options_json`，格式示例：

```json
[
  { "label": "优秀", "value": "A", "score": 20 },
  { "label": "良好", "value": "B", "score": 15 }
]
```

后端校验：

- 必须是 JSON 数组；
- 至少一个选项；
- 每项必须是对象；
- `value` 不能为空；
- `value` 不可重复；
- `score` 必须是数字。

## 6. 权重计算规则调整说明

本包统一 `WEIGHTED_SUM` 为百分比权重：

```text
总分 = Σ 指标得分 * 权重 / 100
```

专家端评分提交计算已同步调整，不再使用旧的 `score * weight` 直乘逻辑。

相关实现：

- `ReviewMyReviewServiceImpl.calculateTotalScore`
- `ReviewRuleServiceImpl.validateRule`
- `ReviewMyReviewServiceImplTest.weightedSumUsesPercentageWeight`

## 7. 总分校验实现

`SUM` 模式：

- 参与计分指标的理论最高分合计必须等于 `review_rule.total_score`。
- 不相等时允许规则草稿保存，但禁止启用和绑定轮次。

`WEIGHTED_SUM` 模式：

- 参与计分指标权重不能为空；
- 权重合计必须为 100；
- 理论最高分不能超过规则总分；
- 理论最高分低于规则总分时返回 warning。

`AVERAGE` 模式：

- 允许保存和启用；
- 返回 warning：`total_score` 仅作为参考，最终分为可计分指标平均值。

## 8. 轮次绑定规则实现

新增接口：

```text
POST /competition/review/round/{roundId}/bind-rule
```

后端强校验：

- 规则必须存在；
- 规则必须启用；
- 规则与轮次必须属于同一评审活动；
- 规则若已限定其他轮次，则不能绑定当前轮次；
- 轮次必须处于可配置状态；
- 规则必须通过完整校验。

轮次普通编辑接口也会对 `rule_id` 变更执行同样校验，避免绕过专用绑定接口。

## 9. 规则复制实现

新增接口：

```text
POST /competition/review/rule/{ruleId}/copy
```

复制内容：

- `review_rule` 基础配置；
- `review_criteria` 指标；
- `options_json` 单选项配置。

复制后规则：

- `rule_name = 原规则名称 + 副本`
- `enabled = N`
- `round_id = null`

## 10. 历史评分数据兼容说明

- 不修改既有 `review_record`。
- 不修改既有 `review_score_detail`。
- 评分明细仍保存 `criteria_name`、`score_type`、`weight`、`sort_order` 等快照字段。
- 若规则已有 `SUBMITTED` 评分记录：
  - 禁止删除规则；
  - 禁止删除或修改规则下指标；
  - 禁止直接修改规则核心字段；
  - 支持复制规则后调整副本。

## 11. 菜单和权限 SQL

新增迁移脚本：

```text
db/migration/20260707_review_module_scoring_rule_enhancement.sql
```

脚本内容：

- 新增“评分表配置”菜单；
- 新增评分规则查询、新增、编辑、删除按钮权限；
- 新增评分指标列表、查询、新增、编辑、删除按钮权限；
- 新增轮次绑定规则所需的轮次编辑按钮权限；
- 将权限分配给 `admin` 角色。

执行结果：

- 已在本地 `jiaoxue_test` 执行，执行语句数：15。

## 12. 测试结果

后端相关测试复跑：

```bash
cd old-code
mvn -pl teaching-modules/teaching-competition test -Dtest=ReviewRuleServiceImplTest,ReviewMyReviewServiceImplTest
```

结果：

- Tests run: 17
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

本包前也执行过完整 competition 模块测试：

```bash
cd old-code
mvn -pl teaching-modules/teaching-competition test
```

结果：

- Tests run: 121
- Failures: 0
- Errors: 0
- Skipped: 0

前端构建：

```bash
cd old-code-admin
npm run build:prod
```

结果：

- 构建通过；
- `4214 modules transformed`；
- 仅存在仓库原有的 `eval` 和 sourcemap warning，未导致失败。

## 13. 浏览器联调结果

本包已完成数据库菜单 SQL 落库、后端测试和前端生产构建。

本次未完成真实浏览器端到端联调，原因：

- 本地检查时 `9211`、`9205`、`8081` 端口均无监听；
- `teaching-system`、`teaching-competition`、管理端 Vite 服务未处于可访问状态；
- 为避免伪造浏览器结果，本报告将该项标记为待补跑。

建议补跑路径：

1. 启动 gateway、auth、system、competition；
2. 启动 old-code-admin；
3. 管理员登录；
4. 进入“评审管理 / 评分表配置”；
5. 新建 SUM 规则和指标，校验并启用；
6. 新建 WEIGHTED_SUM 规则，验证权重 40/30/30 可按百分比计算；
7. 新建 SINGLE_CHOICE 和 TEXT 指标；
8. 绑定到 NOT_STARTED 轮次；
9. 专家端打开任务并提交评分；
10. 验证 `review_record.total_score` 和 `review_score_detail` 快照。

## 14. 已知问题

- 浏览器联调未完成，需在服务完整启动后补跑。
- 规则配置页面依赖动态菜单加载，必须先执行菜单 SQL 并刷新用户权限缓存。
- `WEIGHTED_SUM` 已按百分比权重实现；若历史未提交草稿中曾按直乘理解配置权重，需要业务侧复核。
- 规则已被提交评分记录使用后，本包采取保守策略禁止直接修改核心配置，可能需要运营通过“复制规则”方式处理后续调整。

## 15. 后续建议

1. 补跑真实浏览器联调并追加截图或操作记录。
2. 在专家评分提交前展示规则版本提示，进一步降低规则变更认知成本。
3. 为评分规则增加版本号字段，后续实现“启用即冻结版本”。
4. 在管理端轮次详情页增加已绑定规则的只读摘要。
5. 在上线前检查脚本中补充评分规则校验扫描，检查已启用规则是否仍然有效。
