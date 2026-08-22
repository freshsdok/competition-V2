# 通用评审模块第四包执行报告

## 1. 完成的页面

本包新增和增强了以下管理端/填报端页面：

- 我的评审填报：`old-code-admin/src/views/review/my-submission/index.vue`
  - 展示当前登录用户拥有填报权限的任务。
  - 支持查看提交状态、填报截止时间、是否可编辑、是否可撤回。
  - 按状态显示“填写/编辑”“查看”“提交”“申请撤回”。
- 我的评审填报详情：`old-code-admin/src/views/review/my-submission/detail.vue`
  - 支持编辑项目基础信息、分类字段、关键词。
  - 展示导入同步来的负责人、成员、指导教师、联系人。
  - 支持上传、查看/下载、删除材料。
  - 支持保存草稿、提交资料、申请撤回。
- 评审活动列表增强：`old-code-admin/src/views/review/activity/index.vue`
  - 增加“关闭填报”按钮。
- 评审对象列表增强：`old-code-admin/src/views/review/object/index.vue`
  - 对 `WITHDRAW_REQUESTED` 对象显示“通过撤回”“驳回”审批操作。
- 评审对象详情增强：`old-code-admin/src/views/review/object/detail.vue`
  - 对 `WITHDRAW_REQUESTED` 对象显示撤回审批操作。

新增前端 API：

- `old-code-admin/src/api/review/submission.js`

新增隐藏路由：

- `/review/my-submission-detail/index/:id`

## 2. 完成的后端接口

新增填报接口：

- `GET /review/submission/my-list`
- `GET /review/submission/{objectId}`
- `PUT /review/submission/{objectId}/draft`
- `POST /review/submission/{objectId}/material`
- `GET /review/submission/{objectId}/materials`
- `DELETE /review/submission/material/{materialId}`
- `POST /review/submission/{objectId}/submit`
- `POST /review/submission/{objectId}/withdraw-request`
- `POST /review/submission/{objectId}/withdraw-approve`
- `POST /review/submission/{objectId}/withdraw-reject`

新增活动关闭填报接口：

- `POST /review/activity/{activityId}/close-submission`

主要实现文件：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewSubmissionController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/IReviewSubmissionService.java`

## 3. 填报权限控制实现

填报端所有读取和操作均按当前登录用户 `userId` 校验 `review_submission_permission`。

- `my-list` 只返回当前用户 `ACTIVE` 权限对应对象。
- 详情页要求当前用户拥有对象填报权限。
- 保存草稿和材料新增/删除要求 `EDIT` 或 `EDIT_SUBMIT`。
- 提交要求 `SUBMIT` 或 `EDIT_SUBMIT`。
- 申请撤回要求当前用户拥有该对象填报权限。
- 管理员撤回审批和关闭填报通过 `@RequiresPermissions` 控制。

## 4. 保存草稿实现

`PUT /review/submission/{objectId}/draft` 支持更新：

- 项目名称
- 项目摘要
- 所属单位
- 联系人
- 联系方式
- 联系邮箱
- 学科代码1/2/3
- 分类字段
- 关键词
- 扩展数据

保存限制：

- 仅 `DRAFT`、`WITHDRAW_APPROVED` 可编辑。
- 超过填报截止时间不可编辑。
- `SUBMITTED`、`LOCKED`、`INVALID`、`REVIEWING`、`REVIEWED` 不允许保存。
- 保存后写入 `review_object_submit_log` 和 `review_audit_log`。

## 5. 材料上传实现

前端复用现有 `/file/upload` 文件上传接口，上传成功后调用：

- `POST /review/submission/{objectId}/material`

后端写入 `review_object_material`：

- 材料名称
- 材料类型
- 文件名
- 文件 URL
- 文件大小
- MIME 类型
- 扩展名
- 上传人
- 上传时间
- 材料状态 `NORMAL`

删除材料调用：

- `DELETE /review/submission/material/{materialId}`

删除规则：

- 仅可编辑状态允许删除。
- 删除时先将材料状态置为 `DELETED`，再逻辑删除 `del_flag = 1`。
- 删除操作写入提交状态日志和审计日志。

## 6. 提交流程实现

`POST /review/submission/{objectId}/submit` 实现：

- 校验提交权限。
- 校验未超过 `submit_deadline`。
- 校验状态为 `DRAFT` 或 `WITHDRAW_APPROVED`。
- 校验项目名称、项目摘要、联系方式必填。
- 校验至少存在负责人。
- 提交后状态变为 `SUBMITTED`。
- 写入 `submit_time`、`submitted_by`。
- 更新当前用户填报权限 `used_time`。
- 写入 `review_object_submit_log` 和 `review_audit_log`。

前端详情页提交前会先保存当前草稿，避免用户修改内容后忘记保存。

## 7. 撤回申请和审批实现

申请撤回：

- `POST /review/submission/{objectId}/withdraw-request`
- 要求当前用户拥有填报权限。
- 要求当前状态为 `SUBMITTED`。
- 要求未超过填报截止时间。
- 状态变为 `WITHDRAW_REQUESTED`。

管理员审批：

- `POST /review/submission/{objectId}/withdraw-approve`
- `POST /review/submission/{objectId}/withdraw-reject`

审批规则：

- 仅 `WITHDRAW_REQUESTED` 可审批。
- 通过后状态变为 `WITHDRAW_APPROVED`，可继续编辑并重新提交。
- 驳回后状态回到 `SUBMITTED`。
- 通过审批时若已超过提交截止时间，不允许通过。
- 审批意见写入状态日志和审计日志。

## 8. 截止锁定/作废实现

`POST /review/activity/{activityId}/close-submission` 实现手动关闭填报：

- `SUBMITTED` 对象变为 `LOCKED`，写入 `locked_time`。
- `DRAFT`、`WITHDRAW_APPROVED`、`WITHDRAW_REQUESTED` 对象变为 `INVALID`，写入 `invalid_time`。
- 活动状态更新为 `SUBMIT_CLOSED`。
- 每个对象状态变化均写入提交状态日志和审计日志。

本包只实现手动触发，后续可扩展定时任务。

## 9. 状态日志实现

新增表：

- `review_object_submit_log`

迁移脚本：

- `db/migration/20260703_review_module_phase4_submission.sql`

新增实体、Mapper、Service：

- `ReviewObjectSubmitLog`
- `ReviewObjectSubmitLogMapper`
- `ReviewObjectSubmitLogMapper.xml`
- `IReviewObjectSubmitLogService`
- `ReviewObjectSubmitLogServiceImpl`

记录的操作类型：

- `SAVE_DRAFT`
- `SUBMIT`
- `WITHDRAW_REQUEST`
- `WITHDRAW_APPROVE`
- `WITHDRAW_REJECT`
- `LOCK`
- `INVALID`
- `MATERIAL_ADD`
- `MATERIAL_DELETE`

## 10. 测试结果

后端测试：

- 执行目录：`old-code`
- 命令：`mvn test -pl teaching-modules/teaching-competition -am -DskipTests=false -DfailIfNoTests=false`
- 结果：通过。
- 用例结果：`Tests run: 19, Failures: 0, Errors: 0, Skipped: 0`
- 第四包新增测试：`ReviewSubmissionServiceImplTest`，共 10 个用例。

新增测试覆盖：

- 有权限用户可看到自己的填报任务。
- 无权限用户看不到任务。
- 草稿状态可保存。
- 已提交状态不可直接编辑。
- 已提交且未截止可申请撤回。
- 撤回通过后可编辑。
- 撤回驳回后回到提交状态。
- 截止后已提交对象变为 `LOCKED`。
- 截止后未提交对象变为 `INVALID`。
- 锁定对象不可编辑。
- 材料上传写入 `review_object_material`。
- 删除材料使用逻辑删除。
- 提交资料写入提交时间和提交人。
- 状态流转写入日志。

前端构建：

- 执行目录：`old-code-admin`
- 命令：`npm run build:prod`
- 结果：通过。
- 说明：构建过程中仍有项目既有依赖和既有工作流页面的 `eval`、sourcemap 警告，新增页面无编译错误。

## 11. 已知问题

- 本包未连接真实测试库做浏览器级联调，当前以单元测试和前端构建验证为主。
- 成员信息当前以导入同步数据展示为主，不允许填报人随意新增/删除成员；填报人可维护项目联系人和联系方式。
- 材料预览当前采用打开文件 URL，PDF/图片内嵌预览留待后续优化。
- `close-submission` 当前为管理员手动触发，尚未接入定时任务。
- 菜单和按钮权限 SQL 需要在目标环境执行，并给填报人/管理员角色分配对应权限。
- 附件材料是否必填暂未启用，符合本包“可选、后续扩展”的边界。

## 12. 第五包建议

- 建设专家 PC 端评分页面，接入评审分配、材料查看和评分记录。
- 建设秘书端扫码页面，复用参赛证解析和当前对象切换接口。
- 增加填报材料必填配置和材料类型规则配置。
- 增加成员联系方式的受控编辑接口，只允许修改联系方式类字段。
- 增加填报截止自动定时任务。
- 增加状态日志查看页面，便于管理员追踪提交、撤回、锁定、作废。
- 补充真实测试库端到端联调脚本。
