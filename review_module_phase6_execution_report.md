# 通用评审模块第六包执行报告

## 1. 完成的页面

- 新增秘书移动端现场控制台：`old-code-admin/src/views/review/secretary/session.vue`
- 新增访问路径：
  - `/review/secretary/session/index/:sessionId`
  - `/review/secretary/session/:sessionId`
- 在“评审管理 / 现场场次”列表增加“现场控制台”入口。
- 页面支持手机宽度布局，包含场次信息、当前对象、证件编号模拟扫码、候选确认、下一位、场次对象顺序、到场/缺席/跳过/延后、专家评分进度展示。

## 2. 完成的后端接口

- `GET /review/secretary/session/{sessionId}`：秘书场次详情。
- `GET /review/secretary/session/{sessionId}/objects`：场次对象顺序列表，内含评分进度。
- `POST /review/secretary/session/{sessionId}/current-object`：秘书端设置当前评审对象。
- `POST /review/secretary/session/{sessionId}/next-object`：切换下一位。
- `POST /review/secretary/session-object/{sessionObjectId}/status`：更新到场、缺席、跳过、延后等状态。

前端网关调用前缀为 `/competition/review/secretary/...`。

## 3. 秘书权限校验实现

- 新增 `IReviewSecretaryService` 和 `ReviewSecretaryServiceImpl`。
- 后端强制校验当前用户必须是 `review_session.secretary_user_id` 或超级管理员。
- 秘书端接口权限：
  - `competition:review:secretary:query`
  - `competition:review:secretary:edit`
- 菜单权限迁移脚本：`db/migration/20260703_review_module_phase6_secretary_console.sql`
- 已在 `jiaoxue_test` 执行，admin 角色已授予 2 个秘书控制台权限。

## 4. 扫码 / 证件编号解析实现

- 秘书页面复用既有参赛证解析接口：
  - `GET /competition/review/object/certificate/resolve?activityId=&sessionId=&certificateCode=`
- 浏览器联调使用证件编号模拟扫码。
- 解析后展示候选对象、持证人、成员角色、对象状态和警告信息。
- 解析结果不会自动切换，必须由秘书二次确认。

## 5. 当前对象切换实现

- 秘书端确认后调用 `POST /competition/review/secretary/session/{sessionId}/current-object`。
- 后端校验：
  - 场次存在；
  - 当前用户为场次秘书或管理员；
  - 对象属于当前活动；
  - 对象属于当前场次；
  - 对象状态必须为 `LOCKED`；
  - `INVALID`、`ABSENT`、`SKIPPED` 对象禁止设为当前对象。
- 成功后更新：
  - `review_session.current_object_id`
  - `review_session.current_started_time`
  - `review_session.status = IN_PROGRESS`
  - `review_session_object.review_status = REVIEWING`
  - `review_session_object.actual_start_time` 首次设置
  - `review_session_event_log`
- 不修改 `review_assignment`，不授予专家额外评分权限。

## 6. 下一位实现

- 新增 `POST /review/secretary/session/{sessionId}/next-object`。
- 按 `sequence_no` 升序寻找下一对象。
- 跳过：
  - `review_status = COMPLETED`
  - `review_status = SKIPPED`
  - `checkin_status = ABSENT`
- 无下一位时返回明确异常：`没有可切换的下一位评审对象`。

## 7. 到场、缺席、跳过、延后实现

- 到场：`checkin_status = PRESENT`，写入 `PRESENT` 事件。
- 缺席：`checkin_status = ABSENT`，一期同时设置 `review_status = SKIPPED`，写入 `ABSENT` 事件。
- 跳过：`review_status = SKIPPED`，写入 `SKIP` 事件。
- 延后：`review_status = DELAYED`，新增枚举事件 `DELAY`，写入 `DELAY` 事件。
- 这些操作只更新 `review_session_object` 和事件日志，不改变专家任务与评分记录。

## 8. 专家评分进度实现

- 场次对象列表返回 `scoreProgress`：
  - `submittedCount`
  - `totalAssignedCount`
  - `unsubmittedCount`
  - `displayText`
- 数据来源：
  - `review_assignment`
  - `review_record`
- 已验证对象 `REVIEW_TEST_PHASE6_OBJ_1` 显示 `1/2`。

## 9. 事件日志实现

- 当前对象切换写入：
  - `SCAN_CERT`
  - `NEXT_OBJECT`
  - `SET_CURRENT`
- 状态更新写入：
  - `PRESENT`
  - `ABSENT`
  - `SKIP`
  - `DELAY`
- 事件表：`review_session_event_log`

## 10. 移动端适配说明

- 使用现有 Vue3 + Element Plus，无新增大型移动端框架。
- 页面采用单列卡片布局，按钮高度和间距适配手机浏览器。
- 宽屏下页面最大宽度约束为 760px，适合 PC 调试。

## 11. 浏览器 / 移动端联调结果

联调环境：

- 数据库：`jiaoxue_test`
- 后端：本地 `teaching-competition`，端口 `9205`
- 网关：本地 `9889`
- 前端：本地 `old-code-admin`，端口 `8082`
- 账号：`admin`

测试数据：

- 活动：`activityId=3`，`REVIEW_TEST_PHASE6`
- 轮次：`roundId=2`
- 场次：`sessionId=2`，`REVIEW_TEST_PHASE6_SESSION`
- 对象：`objectId=4/5/6`
- 证件：
  - `REVIEW_TEST_PHASE6_CERT_1`
  - `REVIEW_TEST_PHASE6_CERT_2`
  - `REVIEW_TEST_PHASE6_CERT_3`

浏览器验证结果：

- 秘书控制台可访问。
- 场次信息、当前对象区、顺序列表可展示。
- 证件 `REVIEW_TEST_PHASE6_CERT_1` 可解析到对象 `REVIEW_TEST_PHASE6_OBJ_1`。
- 解析后需二次确认，确认后当前对象切换为 `OBJ_1`。
- 专家端 `/review/my-review?sessionId=2` 可将 `OBJ_1` 置顶并标识“当前答辩中”。
- 秘书端点击“下一位”后当前对象切换为 `OBJ_2`。
- 专家端再次打开后 `OBJ_2` 置顶并标识“当前答辩中”。
- 秘书端完成：
  - `OBJ_2` 标记缺席并跳过；
  - `OBJ_1` 标记跳过；
  - `OBJ_3` 标记延后。

数据库核验：

- `review_session.id=2`：
  - `current_object_id=5`
  - `status=IN_PROGRESS`
- `review_session_object`：
  - `OBJ_1`：`review_status=SKIPPED`
  - `OBJ_2`：`checkin_status=ABSENT`，`review_status=SKIPPED`
  - `OBJ_3`：`review_status=DELAYED`
- `review_session_event_log` 已生成：
  - `SCAN_CERT`
  - `NEXT_OBJECT`
  - `ABSENT`
  - `SKIP`
  - `DELAY`
- 有效参赛证映射数：3。
- 专家任务分配数：4。

## 12. 测试结果

- 后端编译：`mvn -pl teaching-modules/teaching-competition -DskipTests compile` 通过。
- 后端测试：`mvn -pl teaching-modules/teaching-competition test` 通过，36 个测试全部成功。
- 新增测试：`ReviewSecretaryServiceImplTest`，覆盖秘书权限、管理员访问、扫码设当前、下一位跳过规则、缺席、延后和事件日志。
- 前端构建：`npm run build:prod` 通过。
- 浏览器移动端模拟联调：通过。

## 13. 已知问题

- 本包未接入真实摄像头扫码组件，移动端页面提供证件编号模拟扫码入口；如项目已有扫码组件，后续可替换输入框触发解析。
- 标记缺席后若该对象正好是当前对象，本包不自动清空 `review_session.current_object_id`，需要秘书点击“下一位”继续推进。
- 延后对象本包不调整排序；后续可在现场管理中增加拖拽排序或“移至队尾”。
- 事件内容当前为简洁文本/Map 字符串，后续可统一为 JSON。

## 14. 第七包建议

- 建设结果汇总与发布页面，支持按活动/轮次查看评分完成度、平均分和排名。
- 增加现场大屏或只读投屏页，展示当前答辩对象和候场队列。
- 引入真实扫码组件，支持摄像头扫描二维码并解析参赛证编号。
- 增强现场队列：延后自动移至队尾、批量导入排序、重新排序。
- 增加评分进度提醒和异常提示，如专家未提交、对象缺席、评分未完成。
