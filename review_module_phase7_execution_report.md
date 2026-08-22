# 通用评审模块第七包执行报告

任务名称：结果汇总、评分记录查看与评价结论发布

## 1. 完成的页面

- 管理端新增“评审结果”页面：`old-code-admin/src/views/review/result/index.vue`
  - 支持按评审活动、轮次、对象编号、项目名称、所属单位、结果状态、评分完成状态查询。
  - 支持展示分配专家数、已提交评分数、未提交数、完成度、系统计算分、系统排名、评价结论、结果状态、发布时间。
  - 支持生成/重算结果、批量生成、查看评分记录、查看评分明细、填写评价结论、发布结果、撤回发布。
  - 评分记录、评分明细、系统计算分、系统排名均为只读展示。
- 被评审人端“我的评审填报”页面增加“查看结果”入口：`old-code-admin/src/views/review/my-submission/index.vue`
  - 已发布结果可查看。
  - 未发布或撤回结果由后端拦截并返回友好提示。

## 2. 完成的后端接口

- `POST /review/result/generate`
  - 生成或重新生成评审结果。
- `GET /review/result/list`
  - 查询结果列表和评分完成度。
- `GET /review/result/{id}`
  - 查询结果详情。
- `PUT /review/result/{id}/conclusion`
  - 更新评价结论，不修改系统计算分。
- `POST /review/result/{id}/publish`
  - 发布结果。
- `POST /review/result/{id}/revoke`
  - 撤回发布。
- `GET /review/result/records?activityId=&roundId=&objectId=`
  - 管理员只读查看专家评分记录。
- `GET /review/result/{objectId}/records?activityId=&roundId=`
  - 兼容指令建议路径，管理员只读查看某评审对象专家评分记录。
- `GET /review/result/record/{recordId}/details`
  - 管理员只读查看评分明细快照。
- `GET /review/submission/{objectId}/result`
  - 被评审人查看已发布结果。

前端 API 统一通过 `/competition/review/...` 调用，后端控制器仍保持模块内 `/review/...` 路径，由网关前缀转发。

## 3. 结果汇总规则实现

- 汇总数据来源：
  - `review_assignment`
  - `review_record`
  - `review_object`
- 一期计算规则：
  - 系统计算分 = 已提交专家评分 `review_record.total_score` 的平均值。
  - 已提交评分以 `review_record.record_status = SUBMITTED` 或 `review_assignment.status = SUBMITTED` 对齐统计。
- 无已提交评分时：
  - 不生成结果。
  - 返回 warning，提示该对象暂无已提交评分。
- 专家未全部提交时：
  - 允许生成结果。
  - 返回 warning，提示“评分未完成”。
  - 结果中保存 `reviewer_count` 和 `submitted_count`，便于管理员判断。
- 已发布结果：
  - 不允许直接覆盖生成。
  - 需要先撤回发布，再重新生成。

## 4. 排名规则实现

- 排名范围：同一活动、同一轮次。
- 排名字段：`calculated_score` 降序。
- 同分处理：同分时按 `object_id` 升序稳定排序，不做并列排名。
- 重新生成结果后，会刷新该活动、该轮次的排名。

## 5. 评分完成度实现

列表返回字段包括：

- `assignedCount`：分配专家数。
- `submittedCount`：已提交评分数。
- `unsubmittedCount`：未提交评分数。
- `completionText`：如 `1/2`。
- `completionStatus`：
  - `NOT_STARTED`
  - `PARTIAL`
  - `COMPLETED`

管理端可以按评分完成状态过滤。

## 6. 评分记录只读查看实现

- 新增 `ReviewResultRecordVO`。
- 查询评分记录时返回：
  - 评分记录 ID
  - 任务 ID
  - 专家用户 ID
  - 专家姓名
  - 记录状态
  - 总分
  - 等级
  - 推荐意见
  - 评语
  - 提交时间
- 页面仅提供查看，不提供编辑入口。

## 7. 评分明细只读查看实现

- 新增 `ReviewScoreDetailReadonlyVO`。
- 明细返回评分时保存的快照字段：
  - 指标名称
  - 指标类型
  - 分值
  - 选项值
  - 文本评价
  - 权重
  - 排序
- 页面只读展示，不允许修改专家分值。

## 8. 评价结论实现

- 管理员可更新 `evaluation_conclusion`。
- 更新结论时写入 `conclusion_generated_by` 和 `conclusion_generated_time`。
- 不开放 `calculated_score`、`calculated_rank`、`review_record.total_score`、`review_score_detail.score_value` 修改接口。
- 评价结论定位为发布性结论，不是人工改分。

## 9. 发布和撤回实现

- 发布：
  - `result_status = PUBLISHED`
  - 写入 `published_by`、`published_time`
  - 写入 `review_result_publish_log`
  - 写入 `review_audit_log`
- 撤回：
  - `result_status = REVOKED`
  - 写入 `revoked_by`、`revoked_time`
  - 写入 `review_result_publish_log`
  - 写入 `review_audit_log`
- 被评审人接口只返回 `PUBLISHED` 状态结果。

## 10. 被评审人查看结果实现

- 被评审人查看结果接口要求：
  - 当前用户拥有该对象有效填报权限；或
  - 当前用户是对象成员中的负责人/联系人。
- 未发布、已撤回或无权限时不可查看。
- 一期返回：
  - 对象编号
  - 项目名称
  - 活动名称
  - 轮次名称
  - 系统计算分
  - 系统排名
  - 等级
  - 评价结论
  - 发布时间
- 不返回专家姓名、专家评分明细和专家评语。

## 11. 数据库迁移

新增迁移脚本：

- `db/migration/20260706_review_module_phase7_result_publish.sql`

脚本内容：

- 新增管理端“评审结果”菜单。
- 新增结果查询、生成、结论填写、发布、撤回、评分记录查看按钮权限。
- 补充结果页依赖的轮次列表/查询按钮权限。
- 授权给 `role_id = 1` 管理员角色。
- 对早期测试库中已有但字段不完整的 `review_record` 表做兼容补列。

## 12. jiaoxue_test 真实库核验结果

连接环境：

- host: `localhost`
- port: `3306`
- database: `jiaoxue_test`
- username: `dev`

已执行迁移脚本：

- `db/migration/20260706_review_module_phase7_result_publish.sql`

核验结果：

- “评审结果”菜单已存在：`menu_id = 2551`，`path = result`，`component = review/result/index`。
- 结果相关按钮权限已存在 7 个：
  - `competition:review:result:list`
  - `competition:review:result:query`
  - `competition:review:result:generate`
  - `competition:review:result:edit`
  - `competition:review:result:publish`
  - `competition:review:result:revoke`
  - `competition:review:result:record`
- 管理员角色已绑定上述 7 个结果权限。
- `review_record` 已具备第七包依赖字段：
  - `activity_id`
  - `round_id`
  - `object_id`
  - `assignment_id`
  - `reviewer_user_id`
  - `record_status`
  - `total_score`

联调测试数据：

- 活动：`REVIEW_TEST_PHASE7_20260706224919`
- 活动 ID：`3`
- 轮次 ID：`1`
- 评审对象：
  - `2`：`review_test_phase7测试项目A`，状态 `LOCKED`
  - `3`：`review_test_phase7测试项目B`，状态 `LOCKED`
- 分配任务：
  - 对象 2 分配 2 名专家，1 条已提交评分，完成度 `1/2`
  - 对象 3 分配 1 名专家，1 条已提交评分，完成度 `1/1`
- 评分记录：
  - 对象 2：`88.00`
  - 对象 3：`95.00`
- 因完整登录态和网关链路不可用，本次未通过浏览器/API 在该测试活动下写入 `review_result`、`review_result_publish_log`、`review_audit_log`；生成、发布、撤回链路由单元测试覆盖。

## 13. 测试结果

后端测试：

- 命令：`mvn -pl teaching-modules/teaching-competition test`
- 结果：通过
- 统计：`113 tests, 0 failures, 0 errors, 0 skipped`
- 覆盖重点：
  - 无评分记录生成结果返回提示
  - 多专家评分平均分计算
  - 未完成评分生成 warning
  - 同轮次多对象排名刷新
  - 发布和撤回写入日志

前端构建：

- 命令：`npm run build:prod`
- 结果：通过
- 说明：仅存在项目既有 `vform/workflow` 相关 eval/sourcemap 警告，无新增构建错误。

## 14. 浏览器联调结果

本次已完成真实库迁移、测试数据准备、后端服务直连启动和权限链路验证；完整浏览器点击闭环未完成。

原因：

- 管理端开发代理依赖网关 `http://127.0.0.1:9889`。
- 当前本地只成功启动了 competition 服务直连端口 `9205`。
- 网关、认证服务、系统服务及 Nacos 配置链路未完整可用。
- 直接访问 `http://localhost:9205/review/result/list?...` 返回：
  - HTTP `200`
  - 业务码 `403`
  - 消息：`没有访问权限，请联系管理员授权`
- 该结果说明后端接口和权限注解已生效，但没有有效登录态时不能绕过权限调用。
- 手工直连启动时还发现一个本地参数问题：JDBC URL 通过命令行传入时 `&` 被转义成了 `\&`，导致健康检查中出现 `zeroDateTimeBehavior=convertToNull\` 的 MySQL 参数错误；后续复测建议使用本地配置文件或用单引号完整包住 `-D...url=...&...`，避免 shell 转义污染 JDBC 参数。

本次未伪造 Redis 登录态，避免污染本地登录系统。完整浏览器联调建议在网关、认证服务、系统服务可用后补跑。

## 15. 已知问题

- 完整浏览器级联调依赖本地网关和认证链路，本次环境未能完整启动，因此第七包浏览器闭环需补测。
- 真实库测试数据已准备到评分记录层，但结果生成/发布未在真实库中实际落表；需要在可登录的网关环境补跑。
- `jiaoxue_test` 中原有 `review_record` 表曾存在旧业务同名表结构，已通过兼容迁移补齐本模块必要字段；其他环境执行时需确认不存在同名旧表冲突。
- 排名采用“分数降序 + object_id 升序”的稳定排序，不做并列排名；如业务需要并列名次，需后续扩展。

## 16. 第八包建议

- 在可用网关/认证环境中补跑第七包浏览器联调。
- 增加结果发布范围配置，如仅负责人可见、团队成员可见、组织可见、公开公示。
- 扩展复杂汇总规则：去最高最低分、加权轮次汇总、多轮综合排名。
- 增加结果导出能力。
- 增加只读的管理员评分记录审计页面。
- 增加发布前校验清单，提示未完成评分、未填写结论、异常分数等风险。
