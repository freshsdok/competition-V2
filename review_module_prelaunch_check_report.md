# 通用评审模块上线前检查报告

生成时间：2026-07-06 23:45  
数据库环境：localhost:3306/jiaoxue_test，账号 dev  
前端环境：old-code-admin，本地 Vite http://localhost:8081  
后端环境：gateway 9889，competition 9205，system 9211，auth 9224

## 1. 本轮加固内容

1. 旧 `/review/record` 写入/读取接口已禁用，统一提示改用专家端 `/review/my-review/{assignmentId}/draft`、`/submit` 或结果只读接口，避免绕过专家分配与对象锁定校验。
2. 基础 CRUD 已收紧：活动、轮次、对象、规则、指标、任务分配、现场场次等基础接口增加状态保护，禁止通过通用编辑接口直接改业务状态、当前对象、系统计算分或已提交评分任务。
3. 新增/增强一键数据检查脚本：`scripts/review_module_prelaunch_check.py`。
4. 一键检查报告输出：`review_module_prelaunch_data_check_report.md`。

## 2. 数据库与迁移确认

已通过 `information_schema` 与一键脚本确认评审模块核心表存在，包括：

- review_activity / review_round / review_rule / review_criteria
- review_object / review_object_member / review_object_material
- review_submission_permission / review_object_external_ref / review_object_certificate_ref
- review_assignment / review_record / review_score_detail
- review_session / review_session_object / review_session_event_log
- review_result / review_result_publish_log / review_audit_log / review_object_submit_log

本轮使用的迁移脚本文件在仓库中可见：

- `db/migration/20260703_review_module_phase1.sql`
- `db/migration/20260703_review_module_phase3_menu.sql`
- `db/migration/20260703_review_module_phase4_submission.sql`
- `db/migration/20260703_review_module_phase5_my_review.sql`
- `db/migration/20260703_review_module_phase6_secretary_console.sql`
- `db/migration/20260706_review_module_phase7_result_publish.sql`

## 3. 菜单与权限

管理员账号 `admin` 浏览器登录成功，可访问：

- 评审管理
- 评审结果
- 现场评审控制台

专家账号 `ry` 浏览器真实登录成功。初次验证发现 `ry` 所属角色 `125 / 信科赛运维` 缺少专家端菜单权限，导致 `/review/my-review` 返回 404。本轮已在 `jiaoxue_test` 补齐最小专家端权限：

- 评审管理根菜单：2527
- 我的评审任务及按钮：2544、2545、2546、2547、2548

补齐后 `ry` 可访问“我的评审任务”，且只看到自己被分配的 1 条任务。

## 4. 真实库结果发布链路验证

使用活动：

- activityId：3
- 活动名称：review_test_phase7结果联调活动20260706224919
- roundId：1

验证结果：

- `POST /competition/review/result/generate` 成功生成 2 条结果。
- 对象 B 系统计算分为 95.00，排名 1。
- `PUT /competition/review/result/{id}/conclusion` 成功写入评价结论。
- `POST /competition/review/result/{id}/publish` 成功发布。
- `review_result_publish_log` 已写入发布日志。
- `review_audit_log` 已写入 GENERATE_RESULT、UPDATE_RESULT_CONCLUSION、PUBLISH_RESULT。

浏览器结果页可见已发布记录与评价结论。

## 5. 专家账号浏览器验证

账号：`ry`

验证结果：

- 登录成功，页面显示“若依”。
- `/review/my-review` 可访问。
- 仅返回该专家自己的 1 条评审任务。
- 带 `sessionId=1` 访问时，当前现场对象置顶高亮，显示“当前答辩中”。

当前该任务显示“评分规则未配置，不能评分”，属于测试数据配置问题，不是权限绕过问题。

## 6. 秘书端与扫码链路验证

测试库原本没有现场场次数据。本轮新增了命名清晰的测试数据：

- sessionId：1
- sessionCode：REVIEW_TEST_PRELAUNCH_SESSION_20260706
- sessionName：review_test_prelaunch现场联调场次
- 测试证件：REVIEW_TEST_PRELAUNCH_CERT_A、REVIEW_TEST_PRELAUNCH_CERT_B

接口验证：

- 证件号解析成功，`REVIEW_TEST_PRELAUNCH_CERT_A` 可解析到对象 A，且 `inSession=true`。
- 秘书确认后可设置当前对象。
- “下一位”可切换到对象 B。
- 延后操作可写入状态。
- 当前对象最终切回对象 A。
- `review_session_event_log` 已写入 SCAN_CERT、NEXT_OBJECT、DELAY、SET_CURRENT。

浏览器验证：

- 管理员可打开 `/review/secretary/session/index/1`。
- 页面显示场次信息、当前对象、模拟扫码输入、下一位、对象顺序、评分进度。

未完成项：

- 本轮未做手机真机摄像头扫码，只完成了浏览器输入证件编号的扫码兜底链路验证。真机摄像头权限和扫码组件需要现场设备补测。

## 7. 一键检查结果

脚本：

```bash
/tmp/review-phase7-venv/bin/python scripts/review_module_prelaunch_check.py
```

结果：

- PASS：15
- WARN：0
- FAIL：0

检查覆盖：

- 核心表完整性
- review_record 字段完整性
- 菜单和按钮权限完整性
- 专家分配账号菜单权限
- 秘书场次账号菜单权限
- 参赛证重复有效映射
- 填报权限 user_id
- 未锁定对象评审任务
- 提交任务与提交记录一致性
- 评分明细快照
- 发布结果日志
- 当前现场对象合法性

## 8. 编译与测试

后端：

```bash
cd old-code
mvn -pl teaching-modules/teaching-competition test
```

结果：通过，113 tests，0 failures，0 errors。

前端：

```bash
cd old-code-admin
npm run build:prod
```

结果：通过。仅存在项目原有 sourcemap / eval 类构建警告。

## 9. 已修复问题

1. 旧 `/review/record` 接口可绕过专家端评分闭环的问题已修复为禁用。
2. 基础 CRUD 可绕过业务状态的问题已增加 Service 层校验。
3. `ry` 专家账号缺少专家端菜单权限的问题已在 `jiaoxue_test` 补齐。
4. 一键检查脚本已补充菜单/专家/秘书权限检查项。

## 10. 尚未解决或需人工确认

1. 秘书端真机摄像头扫码未完成，需用真实手机浏览器或 WebView 验证摄像头权限、扫码识别速度和 HTTPS/域名限制。
2. `ry` 的当前测试任务缺少评分规则，无法用该账号完成完整“打开评分窗口并提交评分”的真实评分动作；需要补评分规则或另选完整测试任务。
3. 本轮后端服务使用本地临时启动参数覆盖数据源，不涉及生产配置；上线前仍需确认预发/生产 Nacos 配置指向正确数据库。

## 11. 上线建议

评审模块的数据结构、权限、结果发布、专家任务可见性、秘书现场控制台和核心日志链路已通过本地真实库补测。建议进入预发/UAT。

生产上线前建议设置两个硬闸口：

1. 完成一次真机扫码验证。
2. 使用带完整评分规则的专家账号完成一次真实评分提交。
