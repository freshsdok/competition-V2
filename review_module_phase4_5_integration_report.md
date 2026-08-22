# 通用评审模块 4.5 包真实测试库浏览器级联调补测报告

## 1. 使用的数据库连接环境

- 数据库：`jiaoxue_test`
- 主机：`localhost:3306`
- 用户：`dev`
- 口令：使用用户提供的本地测试库口令
- 联调环境：本地网关 `http://127.0.0.1:9889`，competition 服务 `9205`，管理端前端本次临时使用当前工作区 `http://127.0.0.1:8082`
- 说明：未修改生产配置；数据库脚本通过临时 PyMySQL 执行器连接本地测试库执行。

## 2. 执行的迁移脚本

- `db/migration/20260703_review_module_phase1.sql`
- `db/migration/20260703_review_module_phase3_menu.sql`
- `db/migration/20260703_review_module_phase4_submission.sql`

补测中发现 phase4 菜单按钮 SQL 与当前 `sys_menu` 字段不一致，已修复后重新执行。执行后评审模块核心表、菜单、按钮权限均已在 `jiaoxue_test` 存在。

## 3. 菜单和权限检查结果

- 管理端菜单存在：`评审管理`
- 子菜单存在：`我的评审填报`、`评审活动`、`评审对象`、`导入评审对象`、`现场场次`
- 按钮权限存在：`competition:review:submission:query/edit/submit/withdraw/approve/close`
- `我的评审填报` 列表权限存在：`competition:review:submission:list`
- admin 角色已关联评审模块菜单和按钮权限，核验数量为 19 条。

## 4. 使用的测试账号

- 管理员账号：`admin`
- 实际可登录密码：`qwe123!@#`
- 说明：任务文本中的 `qwe123！@#` 使用的是全角感叹号，浏览器登录失败；ASCII 感叹号版本登录成功。
- 填报联调用户：admin。为浏览器补测临时给对象 1 增加了一条 `review_test_phase45` 来源的 `EDIT_SUBMIT` 填报权限。

## 5. 使用的测试活动

- 活动 1：`评审测试4.5浏览器联调活动`
  - ID：1
  - 编码：`REVIEW_TEST_45_1783049794`
  - 用途：完整填报、材料、提交、撤回、审批、关闭填报联调
  - 最终状态：`SUBMIT_CLOSED`
- 活动 2：`评审测试4.5浏览器导入页面活动`
  - ID：2
  - 编码：`REVIEW_TEST_45_UI_IMPORT`
  - 用途：浏览器页面导入预览和执行导入补测

## 6. 使用的测试团队 / 报名 / 参赛证数据

- 报名表：`competition_apply_info`
- 参赛证表：`competition_scene_credential`
- 测试团队编号：`PH5TEAM001`
- 报名成员：
  - `member_id=1`，`user_id=1591`，`ph5_user_a`，角色 `队长`
  - `member_id=2`，`user_id=1592`，`ph5_user_b`，角色 `队员`
- 新增测试参赛证：
  - `REVIEW_TEST_CERT_PH5_1`，关联 `member_id=1`
  - `REVIEW_TEST_CERT_PH5_2`，关联 `member_id=2`

新增测试数据命名均带 `REVIEW_TEST` 或 `review_test` 标识，未删除或覆盖已有正式样例数据。

## 7. 管理端导入联调结果

- API 方式先验证 `REGISTRATION` 导入：
  - `sourceBizId=1` 首次导入成功，对象 ID 为 1
  - 重复导入跳过成功，返回 `1：已导入`
  - 覆盖导入成功，旧证件映射标记为 `INVALID`，重新生成 `VALID` 映射
  - `sourceBizId=2` 导入成功，对象 ID 为 2
- 浏览器页面验证 `TEAM` 导入：
  - 页面：`/review/import?activityId=2`
  - 输入：`PH5TEAM001`
  - 预览结果：成员数 2，证件数 2，预计授权用户 `1591-ph5_user_a`
  - 执行结果：请求 1，成功 1，跳过 0，失败 0，新建/同步对象 ID 为 3

## 8. 被评审人填报联调结果

- 页面：`/review/my-submission`
- admin 仅能看到已授权对象 1。
- 对象 1 草稿保存成功，更新了项目名称、摘要、单位、联系人、联系方式、学科代码、分类字段和关键词。
- 提交后再次保存草稿被拒绝，返回 `当前状态不允许编辑填报资料`。
- 撤回审批通过后可再次保存并重新提交。
- 关闭填报后对象 1 页面只显示查看态，提示 `评审对象已锁定，只能查看。`

## 9. 材料上传联调结果

- 新增材料 1：`review_test_待删除材料`，写入 `review_object_material`
- 删除材料 1：逻辑删除成功，数据库为 `status=DELETED` 且 `del_flag=1`
- 新增材料 2：`review_test_保留材料`，保留为 `status=NORMAL` 且 `del_flag=0`
- 测试附件 URL：`http://127.0.0.1:8081/favicon.ico`
- URL 可访问性：HTTP 200

说明：本次验证的是评审材料业务绑定接口；完整物理文件上传服务未作为 4.5 包新增能力扩展。

## 10. 提交 / 撤回 / 审批 / 关闭填报联调结果

对象 1 完成以下状态链路：

- `DRAFT -> SUBMITTED`
- `SUBMITTED -> WITHDRAW_REQUESTED`
- `WITHDRAW_REQUESTED -> WITHDRAW_APPROVED`
- `WITHDRAW_APPROVED -> SUBMITTED`
- `SUBMITTED -> WITHDRAW_REQUESTED`
- `WITHDRAW_REQUESTED -> SUBMITTED`
- `SUBMITTED -> LOCKED`

对象 2 用于关闭填报作废验证：

- `DRAFT -> INVALID`

关闭填报接口返回：锁定 1 个，作废 1 个，忽略 0 个。

## 11. 数据库表核验结果

- `review_activity`
  - 活动 1 已变为 `SUBMIT_CLOSED`
  - 活动 2 保持 `SUBMITTING`
- `review_object`
  - 对象 1：`LOCKED`
  - 对象 2：`INVALID`
  - 对象 3：`DRAFT`，来源 `TEAM-PH5TEAM001`
- `review_object_member`
  - 对象 1、2、3 均同步 2 名成员，且均有 1 名负责人
- `review_submission_permission`
  - 对象 1 有 admin 临时填报权限，以及 1591/1592 真实业务权限
  - 对象 2 有 1591/1592 真实业务权限
  - 对象 3 有 1591 负责人填报权限
- `review_object_external_ref`
  - 对象 1、2、3 均有外部业务关联
- `review_object_certificate_ref`
  - 对象 1 覆盖导入后存在旧 `INVALID` 和新 `VALID` 映射
  - 对象 2、3 均有两条 `VALID` 参赛证映射
- `review_object_material`
  - 材料 1 逻辑删除，材料 2 正常
- `review_object_submit_log`
  - 已记录 `SAVE_DRAFT`、`MATERIAL_ADD`、`MATERIAL_DELETE`、`SUBMIT`、`WITHDRAW_REQUEST`、`WITHDRAW_APPROVE`、`WITHDRAW_REJECT`、`LOCK`、`INVALID`
- `review_audit_log`
  - 已记录导入、重复跳过、覆盖导入、证件映射新增/失效、证件解析、填报状态流转等关键操作

## 12. 发现的问题

1. phase4 菜单按钮 SQL 字段数量与当前 `sys_menu` 不匹配，导致按钮权限未插入。
2. admin 角色缺少部分新评审菜单 / 按钮授权。
3. 前端 review API 使用 `/review/...`，经当前网关会 404；真实可访问路径需要 `/competition/review/...`。
4. `TeamManagerInfoMapper.xml` 查询了当前 `team_manager_info` 表不存在的 `guide_teacher`、`guide_teacher_phone`、`guide_teacher_email` 字段。
5. PH5 报名数据存在，但原有参赛证记录缺少 `team_code/member_id`，无法支撑评审导入时的参赛证映射。
6. 测试环境 `/profile/review_test_phase45_keep.pdf` 无实际静态文件映射。

## 13. 已修复的问题

1. 修复 `20260703_review_module_phase4_submission.sql` 中按钮菜单插入字段不匹配问题。
2. 在 phase4 SQL 中补充 admin 角色评审菜单 / 按钮授权的幂等插入。
3. 修复管理端 review API 前缀，统一走 `/competition/review/...`。
4. 修复 `TeamManagerInfoMapper.xml` 中不存在的 `guide_teacher*` 字段引用，改为 `null as ...` 兼容当前测试库结构。
5. 新增两条 `REVIEW_TEST_CERT_PH5_*` 测试参赛证，打通真实报名数据到评审证件映射链路。
6. 材料 URL 使用当前可访问的本地资源地址完成 URL 可访问性验证。

## 14. 尚未解决的问题

- `team_manager_info` 当前测试库为空，TEAM 导入主要依赖 `competition_apply_info.team_code` 兜底；页面预览中团队名称列为空，但预计对象名称能正确显示为 `PH5测试团队`。
- 关闭填报不会自动将作废对象的参赛证映射标记为 `INVALID`。当前证件解析会返回对象状态，并对 `INVALID` 对象提示“已作废，不能进入评审”，现场当前对象接口仍会禁止作废对象进入评审。
- 前端生产构建存在既有的大包、eval 和 stream externalized 警告，不影响本次构建通过。

## 15. 编译和构建结果

- 后端编译：`mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`，成功。
- 前端构建：`npm run build:prod`，成功。
- 浏览器联调：使用当前工作区前端 `8082` 完成后已停止该临时服务。

## 16. 是否建议进入第五包专家评分端开发

建议进入第五包。当前管理端导入、被评审人填报、材料、提交撤回、审批、关闭填报、参赛证解析和数据库日志链路均已在真实测试库完成浏览器级补测。第五包可在此基础上进入专家评分端，但建议同时补充：

- 专家端只允许评分已分配且对象已锁定的任务；
- 专家评分前对 `INVALID`、未锁定对象做前后端双重拦截；
- 对证件解析候选列表增加对象状态筛选或更醒目的状态提示；
- 补充真实文件上传服务的端到端验证。
