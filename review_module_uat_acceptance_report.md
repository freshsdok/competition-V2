# 评审模块 UAT 收口验收报告

生成时间：2026-07-07 00:45  
数据库：`localhost:3306/jiaoxue_test`  
数据库账号：`dev`  

## 1. 使用的环境

- 本地测试库：`jiaoxue_test`
- 网关端口：`9889`，检查时已有进程监听
- 认证端口：`9224`，检查时已有进程监听
- system 服务端口：`9211`，检查时未监听
- competition 服务端口：`9205`，检查时未监听
- 管理端端口：`8081`，检查时未监听

本轮尝试启动：

- `teaching-system`
- `teaching-competition`

启动结果：

- 两个服务均在 Druid 数据源初始化阶段失败。
- Nacos test 配置下发的数据源地址为：`jdbc:mysql://10.10.10.10:3306/jiaoxue_test...`
- 当前本机仅能通过 `localhost:3306` 访问 `jiaoxue_test`。
- 命令行覆盖本地库参数后仍未覆盖 Nacos 下发的数据源，服务无法进入可浏览器访问状态。

结论：本轮无法完成真实浏览器和真机端到端 UAT，只完成了代码补强、真实库数据准备、数据库核验、构建测试和上线前检查脚本复跑。

## 2. 使用的账号

- 管理员：`admin`
- 专家：`ry`
- `ry` 对应 `sys_user.user_id = 2`
- `ry` 当前角色：`信科赛运维`

权限核验：

- `ry` 已拥有：
  - `competition:review:my-review:list`
  - `competition:review:my-review:query`
  - `competition:review:my-review:edit`
  - `competition:review:my-review:submit`
- `ry` 未拥有秘书端权限，符合专家账号边界。

## 3. 使用的活动、轮次、场次、对象、证件、专家任务

活动：

- `review_activity.id = 3`
- 活动名称：`review_test_phase7结果联调活动20260706224919`
- 活动编码：`REVIEW_TEST_PHASE7_20260706224919`
- 状态：`REVIEWING`

轮次：

- `review_round.id = 1`
- 轮次名称：`review_test_phase7结果汇总轮次`
- 状态：`IN_PROGRESS`
- 已绑定规则：`review_rule.id = 1`

评分规则：

- 规则名称：`review_test_uat专家评分规则`
- 计算方式：`SUM`
- 总分：`100.00`
- 启用状态：`Y`

评分指标：

- `review_test_uat数字评分A`，`NUMBER`，0-40，必填
- `review_test_uat数字评分B`，`NUMBER`，0-60，必填
- `review_test_uat文本评价`，`TEXT`，必填

评审对象：

- `review_object.id = 2`
- 对象编号：`REVIEW_TEST_PHASE7_20260706224919_OBJ_A`
- 对象名称：`review_test_phase7测试项目A`
- 状态：`LOCKED`

专家任务：

- `review_assignment.id = 2`
- `reviewer_user_id = 2`
- 当前状态：`ASSIGNED`
- 当前没有真实提交记录。

材料：

- 已补充专家可见材料：`review_test_uat专家可见材料`
- 类型：`PDF`
- URL：`/profile/upload/review_test_uat_material.pdf`
- `visible_to_reviewer = Y`
- `status = NORMAL`

场次：

- `review_session.id = 1`
- 场次名称：`review_test_prelaunch现场联调场次`
- 状态：`IN_PROGRESS`
- 当前对象：`review_object.id = 2`

证件：

- 证件编号：`REVIEW_TEST_PRELAUNCH_CERT_A`
- 证件映射：`review_object_certificate_ref.id = 13`
- 映射对象：`review_object.id = 2`
- `valid_status = VALID`

## 4. 真机扫码验证结果

本轮新增秘书端轻量摄像头扫码能力：

- 文件：`old-code-admin/src/views/review/secretary/session.vue`
- 新增按钮：`打开摄像头扫码`
- 技术方案：
  - 使用浏览器原生 `navigator.mediaDevices.getUserMedia`
  - 使用浏览器原生 `BarcodeDetector`
  - 不引入大型扫码依赖
  - 保留证件编号手动输入兜底

扫码流程实现：

1. 打开摄像头扫码；
2. 识别二维码内容；
3. 将识别结果填入证件编号；
4. 调用证件解析；
5. 展示候选评审对象；
6. 必须由秘书点击“设为当前评审对象”后才切换当前对象。

安全与兼容说明：

- 手机浏览器摄像头通常要求 HTTPS 或 localhost 安全上下文。
- 非安全上下文会提示使用证件编号兜底输入。
- 不支持 `BarcodeDetector` 的浏览器会提示使用兜底输入。

真机结果：

- 未完成真实手机实机扫码。
- 阻塞原因：本地 `system`、`competition`、管理端服务未能启动到可访问状态，无法打开移动端真实页面执行真机扫码。

## 5. 当前对象切换验证结果

数据库核验结果：

- `review_session.id = 1`
- `current_object_id = 2`
- `review_session_object.review_status = REVIEWING`
- `review_session_object.actual_start_time` 有值
- `review_session_event_log` 中对象 2 相关事件存在：
  - `SCAN_CERT`，内容包含 `certificateCode=REVIEW_TEST_PRELAUNCH_CERT_A`
  - `SET_CURRENT`，内容为 `sourceType=MANUAL`

说明：

- 当前对象切换链路在历史联调数据中已落库。
- 本轮未重新通过真机或浏览器触发一次新的切换。

## 6. 专家端高亮验证结果

已有条件：

- 场次 1 当前对象为对象 2。
- 对象 2 已分配给专家 `ry`。
- 专家端已有 `/review/my-review?sessionId=1` 当前对象置顶高亮逻辑。

本轮结果：

- 未通过浏览器重新验证专家端置顶高亮。
- 阻塞原因同上：后端服务无法启动，前端无可用 API。

## 7. ry 专家账号完整评分验证结果

本轮已补齐 `ry` 完整评分前置条件：

- 对象状态：`LOCKED`
- assignment 状态：`ASSIGNED`
- round 存在：`round_id = 1`
- round 已绑定启用规则：`rule_id = 1`
- criteria：2 个 `NUMBER` + 1 个 `TEXT`
- 对象有专家可见材料
- `ry` 具备专家端菜单和按钮权限

未完成项：

- 未使用 `ry` 在浏览器完成“打开评分窗口、保存草稿、提交评分”。
- 未生成真实 `review_record` 提交记录。
- 未生成真实 `review_score_detail` 快照记录。

阻塞原因：

- `teaching-system` 与 `teaching-competition` 服务因 Nacos 数据源指向不可达地址 `10.10.10.10:3306` 启动失败。

## 8. 评分草稿、提交、明细快照数据库核验结果

当前数据库状态：

- `review_assignment.id = 2`
- `status = ASSIGNED`
- `submitted_time = null`
- `review_record` 中 `assignment_id = 2` 未查到记录
- `review_score_detail` 无本轮新增记录
- `review_audit_log` 无本轮 `MY_REVIEW_DRAFT` / `MY_REVIEW_SUBMIT`

结论：

- 真实专家评分硬闸口未通过。
- 本轮没有用数据库直写方式伪造专家提交记录。

## 9. 结果生成、评价结论、发布、撤回验证结果

当前对象 2 已存在一条历史结果：

- `review_result.id = 1`
- `activity_id = 3`
- `round_id = 1`
- `object_id = 2`
- `calculated_score = 88.00`
- `result_status = GENERATED`

本轮未完成：

- 在 `ry` 真实评分提交后重新生成结果；
- 填写评价结论；
- 发布结果；
- 撤回发布；
- 填报用户查看发布结果和撤回后不可见验证。

阻塞原因：

- 真实专家评分未完成，无法执行“评分后结果重新生成”的完整 UAT 链路。
- 管理端服务不可访问，无法完成浏览器发布/撤回动作。

安全确认：

- 本轮未新增任何直接修改 `calculated_score`、`review_record.total_score`、`review_score_detail.score_value` 的接口。
- 本轮没有通过数据库直写伪造评分结果。

## 10. 上线前检查脚本复跑结果

执行命令：

```bash
REVIEW_CHECK_OUTPUT=review_module_uat_prelaunch_data_check_report.md \
/tmp/review-phase7-venv/bin/python scripts/review_module_prelaunch_check.py
```

输出：

```text
PASS=15 WARN=0 FAIL=0
```

报告文件：

- `review_module_uat_prelaunch_data_check_report.md`

结论：

- 数据结构、权限、对象/证件/场次/评分结果一致性检查通过。
- 该检查不替代真实手机和真实专家浏览器 UAT。

## 11. 构建和自动化测试结果

后端相关测试：

```bash
cd old-code
mvn -pl teaching-modules/teaching-competition test \
  -Dtest=ReviewMyReviewServiceImplTest,ReviewSecretaryServiceImplTest,ReviewResultServiceImplTest,ReviewRuleServiceImplTest
```

结果：

- Tests run: 28
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

前端生产构建：

```bash
cd old-code-admin
npm run build:prod
```

结果：

- 构建通过；
- `4214 modules transformed`
- 仅存在仓库既有的 eval/sourcemap warning，不影响构建结果。

## 12. 发现的问题

1. 本地 test Nacos 下发的 system / competition 数据源为 `10.10.10.10:3306`，当前机器不可达。
2. 命令行覆盖 `spring.datasource.dynamic.datasource.master.*` 未覆盖该 Nacos 数据源配置。
3. 因服务未启动，无法进行真实浏览器和真机 UAT。
4. 原秘书端只有证件编号输入兜底，没有摄像头扫码入口。
5. `ry` 的现有任务缺少评分规则和专家可见材料。

## 13. 已修复的问题

1. 已为秘书端新增轻量摄像头扫码入口。
2. 已保留证件编号兜底输入。
3. 已确保扫码后只解析候选对象，不自动切换当前对象。
4. 已为 `ry` 的 UAT 任务补齐评分规则：
   - 2 个 `NUMBER` 指标；
   - 1 个 `TEXT` 指标；
   - 规则启用；
   - round 绑定规则。
5. 已为对象 2 补充专家可见材料。
6. 已复跑上线前检查脚本，结果 PASS 15 / WARN 0 / FAIL 0。

## 14. 尚未解决的问题

1. 真机扫码未完成。
2. 真实专家 `ry` 浏览器保存草稿未完成。
3. 真实专家 `ry` 浏览器提交评分未完成。
4. 评分提交后的 `review_record` / `review_score_detail` / `review_assignment` 数据库核验未完成。
5. 评分后结果重新生成、结论填写、发布、撤回未完成。
6. 专家端当前对象置顶高亮未在本轮浏览器复测。
7. 服务启动依赖的 Nacos 数据源配置仍需修正为当前可访问的 `localhost:3306` 或建立到 `10.10.10.10:3306` 的可用连通性。

## 15. 是否建议进入预发/生产试运行

不建议直接进入预发/生产试运行。

原因：

- 本包两个硬闸口仍未完成：
  - 真机扫码验证；
  - 真实专家账号完整评分验证。
- 虽然代码构建、核心测试、真实库检查脚本均通过，但 UAT 的端到端人工验收链路被本地服务环境阻塞。

建议下一步：

1. 修复本地/测试 Nacos 数据源，使 system、competition、admin 均可访问。
2. 用手机打开 `/review/secretary/session/index/1` 或 `/review/secretary/session/1`。
3. 使用证件 `REVIEW_TEST_PRELAUNCH_CERT_A` 的二维码或手输兜底完成扫码确认。
4. 用 `ry` 登录并打开 `/review/my-review?sessionId=1`。
5. 对 assignment 2 保存草稿并提交评分。
6. 核验 `review_record`、`review_score_detail`、`review_assignment`、`review_audit_log`。
7. 管理员重新生成结果、填写结论、发布、撤回。
8. 再复跑 `scripts/review_module_prelaunch_check.py`，要求 FAIL=0。
