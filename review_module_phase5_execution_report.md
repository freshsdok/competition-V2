# 通用评审模块第五包执行报告

## 1. 完成的页面

- 新增专家 PC 端页面：`old-code-admin/src/views/review/my-review/index.vue`
- 页面路径：`/review/my-review`
- 页面能力：
  - 我的评审任务列表；
  - 支持按活动、轮次、项目名称、对象编号、评分状态、关键词查询；
  - 支持传入 `sessionId` 后每 3 秒轮询当前现场对象；
  - 当前现场对象在列表中置顶并高亮，显示“当前答辩中”；
  - 全屏评分窗口，左侧展示评审对象资料、成员、附件材料，右侧渲染评分表；
  - 支持 PDF/图片/文档等材料通过 URL 打开或下载；
  - 已提交评分后只读展示，不再显示保存/提交按钮。

## 2. 完成的后端接口

- `GET /review/my-review/list`
- `GET /review/my-review/{assignmentId}`
- `GET /review/my-review/{assignmentId}/criteria`
- `POST /review/my-review/{assignmentId}/draft`
- `POST /review/my-review/{assignmentId}/submit`

前端 API 文件：

- `old-code-admin/src/api/review/myReview.js`
- 增强 `old-code-admin/src/api/review/session.js`，新增当前现场对象查询。

## 3. 专家任务权限校验实现

- 新增 `IReviewMyReviewService` 与 `ReviewMyReviewServiceImpl`。
- 后端强制校验当前登录用户必须等于 `review_assignment.reviewer_user_id`。
- 不允许通过传 `userId` 查询他人任务。
- 只允许 `ASSIGNED / IN_PROGRESS / RETURNED` 状态的 assignment 保存或提交。
- `SUBMITTED / LOCKED / CANCELLED` 状态禁止继续编辑。
- 对象必须为 `LOCKED` 才允许评分。
- `INVALID / DRAFT / SUBMITTED / WITHDRAW_REQUESTED / WITHDRAW_APPROVED` 等未锁定或作废对象禁止评分。
- 提交后再次保存草稿接口返回：`评分已提交，不能再次编辑`。

## 4. 评分窗口实现

- 左右双栏全屏 Dialog。
- 左侧展示：
  - 项目名称、编号、单位、摘要、学科代码、分类、关键词；
  - 成员列表；
  - `visible_to_reviewer = Y` 且 `status = NORMAL` 的材料列表。
- 右侧展示：
  - 评分规则；
  - NUMBER / SINGLE_CHOICE / TEXT 三类指标；
  - 实时合计分；
  - 推荐意见、综合意见；
  - 保存草稿、提交评分。

## 5. 材料展示实现

- 后端只返回对专家可见、未删除材料。
- 浏览器联调中已展示材料：
  - `review_test_保留材料`
  - 文件：`review_test_keep.pdf`
  - URL：`http://127.0.0.1:8081/favicon.ico`

## 6. 评分表渲染实现

- 根据 assignment 的 `round_id` 查找 `review_round.rule_id` 或活动/轮次下启用规则。
- 按 `review_criteria.sort_order` 渲染指标。
- 支持：
  - NUMBER：数字输入，显示最低分/最高分；
  - SINGLE_CHOICE：解析 `options_json` 并显示选项分；
  - TEXT：文本评价。

## 7. 草稿保存实现

- `POST /review/my-review/{assignmentId}/draft`
- 保存后：
  - `review_record.record_status = DRAFT`
  - `review_assignment.status = IN_PROGRESS`
  - 覆盖写入 `review_score_detail`
  - 写入 `review_audit_log`

## 8. 评分提交实现

- `POST /review/my-review/{assignmentId}/submit`
- 提交前校验：
  - 必填评分项；
  - NUMBER 分值范围；
  - SINGLE_CHOICE 选项合法性；
  - 当前用户和 assignment 匹配；
  - 对象已锁定；
  - assignment 未提交/未锁定/未取消。
- 提交后：
  - `review_record.record_status = SUBMITTED`
  - `review_record.submitted_time` 写入；
  - `review_assignment.status = SUBMITTED`
  - `review_assignment.submitted_time` 写入；
  - 写入 `review_audit_log`。

## 9. 评分明细快照实现

`review_score_detail` 写入时保存：

- `criteria_id`
- `criteria_name`
- `score_type`
- `score_value`
- `option_value`
- `text_value`
- `weight`
- `sort_order`

浏览器联调最终记录：

- recordId：`1`
- total_score：`91.00`
- 明细包含 `技术创新性`、`应用价值`、`推荐等级`、`综合评语` 快照。

## 10. 当前现场对象置顶高亮实现

- 前端传入 `sessionId=1`。
- 页面轮询 `GET /competition/review/session/1/current-object`。
- 当前对象 `objectId=1` 在专家任务列表中置顶并高亮。
- 浏览器页面显示“当前答辩中”。

## 11. 菜单与权限

新增迁移脚本：

- `db/migration/20260703_review_module_phase5_my_review.sql`

新增菜单：

- 评审管理 / 我的评审任务

新增权限：

- `competition:review:my-review:list`
- `competition:review:my-review:query`
- `competition:review:my-review:edit`
- `competition:review:my-review:submit`
- `competition:review:session:query`

已在 `jiaoxue_test` 执行该脚本，并为 admin 角色补齐权限。

## 12. 测试结果

后端编译：

- `mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`
- 结果：通过

后端测试：

- `mvn -pl teaching-modules/teaching-competition test`
- 结果：通过
- 共 28 个测试：
  - `ReviewModulePhase1SmokeTest`：9 个通过；
  - `ReviewSubmissionServiceImplTest`：10 个通过；
  - `ReviewMyReviewServiceImplTest`：9 个通过。

前端构建：

- `npm run build:prod`
- 结果：通过

## 13. 浏览器联调结果

使用数据库：

- `jiaoxue_test`

使用账号：

- admin，`user_id = 1`
- 本次作为专家 `reviewer_user_id = 1` 模拟联调。

使用测试数据：

- activityId：`1`
- objectId：`1`
- roundId：`1`
- ruleId：`1`
- criteriaIds：`1, 2, 3, 4`
- assignmentId：`1`
- sessionId：`1`

浏览器验证结果：

- 可以登录管理端；
- 菜单中显示“我的评审任务”；
- `/review/my-review?sessionId=1` 可访问；
- 专家只能看到分配给自己的任务；
- 当前现场对象置顶并显示“当前答辩中”；
- 点击“查看并评分”打开全屏评分窗口；
- 左侧可见项目资料、成员、材料；
- 右侧可见评分表；
- 填写 55、26、强烈推荐、综合评语后合计分为 `91.00`；
- 保存草稿成功，列表状态变为“评分中”；
- 提交评分成功，列表状态变为“已提交”；
- 提交后操作变为“查看”，提示“评分已提交，不能再次编辑”；
- 再次调用草稿保存接口被后端拒绝。

数据库核验：

- `review_record.record_status = SUBMITTED`
- `review_record.total_score = 91.00`
- `review_assignment.status = SUBMITTED`
- `review_score_detail` 4 条明细均保存指标快照；
- `review_audit_log` 写入 `MY_REVIEW_DRAFT` 和 `MY_REVIEW_SUBMIT`。

## 14. 已知问题

- 本次浏览器联调使用 admin 模拟专家，符合本包允许的联调方式；后续建议补真实专家账号场景。
- 附件材料本包只做打开/下载，不做 Office/PDF 在线深度预览。
- 前端当前使用 activityId、roundId、sessionId 手工输入，后续可增加活动/轮次/场次选择器。
- 本地 8081 端口被另一个副本目录前端占用，本次使用当前仓库前端 `http://localhost:8082/` 联调。

## 15. 第六包建议

- 建设评审秘书移动端扫码页面；
- 支持现场扫码后专家端当前对象自动轻提示；
- 增加专家评分退回/重新提交流程；
- 增加管理端只读评分记录查看；
- 扩展评分规则配置页面；
- 扩展结果汇总的轮次维度和异常分处理；
- 增加真实专家账号、专家组、场次联动的完整联调用例。
