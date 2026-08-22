# 通用评审模块第三包执行报告

## 1. 完成的页面

本包新增管理端评审管理基础页面，遵循现有 `old-code-admin` 的 Vue3、Element Plus、动态菜单和权限按钮风格。

- 评审活动页面：`old-code-admin/src/views/review/activity/index.vue`
  - 支持活动列表查询、查看详情、新增、编辑、删除入口。
  - 操作区提供“导入评审对象”和“评审对象”跳转。
- 导入评审对象页面：`old-code-admin/src/views/review/import/index.vue`
  - 支持选择来源模块、来源业务类型、来源业务 ID、默认对象类型、填报授权模式、覆盖导入、同步参赛证。
  - 支持调用导入预览接口，并展示可导入对象、成员数量、证件数量、授权用户、警告信息。
  - 支持执行导入并展示成功、跳过、失败、新建对象 ID、失败原因和跳过原因。
- 评审对象列表页面：`old-code-admin/src/views/review/object/index.vue`
  - 支持按活动、对象名称、对象编号、提交状态、来源业务类型、来源团队 ID、来源报名 ID、关键词、所属单位查询。
  - 展示对象编号、项目名称、活动、对象类型、所属单位、负责人、学科、分类、关键词、提交状态、来源信息等字段。
- 评审对象详情页面：`old-code-admin/src/views/review/object/detail.vue`
  - Tab 展示基本信息、成员信息、填报权限、参赛证映射、外部业务关联。
  - 参赛证映射 Tab 增加“重新同步参赛证”按钮，当前复用覆盖导入逻辑刷新外部同步数据。
- 现场场次预留页面：`old-code-admin/src/views/review/session/index.vue`
  - 提供现场场次基础列表入口，作为后续秘书端/现场管理扩展入口。

## 2. 完成的后端接口

本包在第二包接口基础上补充了管理端查询和导入预览能力。

- `POST /review/object/import-preview`
  - 导入预览，不写入数据库。
  - 返回预计对象名称、团队信息、负责人、成员数量、参赛证数量、授权用户、警告和是否可导入。
- `GET /review/object/{id}/members`
  - 查询评审对象成员。
- `GET /review/object/{id}/permissions`
  - 查询评审对象填报权限。
- `GET /review/object/{id}/certificates`
  - 查询评审对象参赛证映射。
- `GET /review/object/{id}/external-refs`
  - 查询评审对象外部业务来源关联。

相关代码：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewObjectController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/IReviewObjectService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewObjectServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/vo/ReviewObjectImportPreviewVO.java`

## 3. 管理端菜单路径

新增菜单 SQL：

- `db/migration/20260703_review_module_phase3_menu.sql`

菜单结构：

- 评审管理
  - 评审活动：`/review/activity`
  - 评审对象：`/review/object`
  - 导入评审对象：`/review/import`
  - 现场场次：`/review/session`

同时在前端路由中新增隐藏详情路由：

- `/review/object-detail/index/:id`

## 4. 导入预览实现情况

导入预览复用第二包已经梳理出的竞赛业务读取链路，支持 `TEAM` 和 `REGISTRATION` 两类来源。

预览规则：

- 校验评审活动存在。
- 校验来源业务 ID 不为空。
- 根据来源业务 ID 查询竞赛团队、报名、成员、参赛证数据。
- 判断是否已经导入。
- 返回预计生成对象、成员数量、参赛证数量和授权用户。
- 对无法找到来源数据、重复导入、成员或证件缺失等情况返回 warning。
- 预览接口不写入 `review_object`、成员、权限、参赛证或外部关联表。

## 5. 导入结果展示情况

导入页面调用第二包已有增强接口：

- `POST /review/object/import-from-business`

页面展示：

- 请求导入数量
- 成功数量
- 跳过数量
- 失败数量
- 新建评审对象 ID
- 跳过项和原因
- 失败项和原因

新建对象 ID 可跳转至评审对象详情页查看同步结果。

## 6. 评审对象详情展示情况

详情页已按 Tab 展示以下数据：

- 基本信息：对象编号、项目名称、对象类型、摘要、所属单位、联系人、联系方式、学科代码、分类、关键词、提交状态、来源模块、来源业务类型、来源业务 ID、来源团队 ID、来源报名 ID。
- 成员信息：姓名、角色、是否负责人、手机号、邮箱、所属单位、证件编号、证件类型、来源业务 ID。
- 填报权限：用户 ID、权限类型、权限状态、来源模块、来源业务 ID、授权人、授权时间、使用时间。
- 参赛证映射：证件编号、证件类型、持证人姓名、成员角色、有效状态、来源团队 ID、来源报名 ID、来源业务 ID。
- 外部业务关联：来源模块、来源业务类型、来源业务 ID、来源业务编码、来源团队 ID、来源报名 ID、关联类型、扩展数据。

## 7. 真实数据或模拟数据联调结果

当前本地环境未提供可连接的真实测试库地址、账号和稳定样例数据，因此本包未直接连接真实测试库执行导入。为保证链路可验证，使用可控模拟竞赛数据完成了服务层联调测试。

模拟联调覆盖：

- TEAM 导入：
  - 可预览 1 个团队。
  - 成功生成 `review_object`。
  - 同步生成 3 条成员数据。
  - 同步生成填报权限。
  - 同步生成外部业务关联。
  - 同步生成 2 条参赛证映射，满足一个评审对象关联多个参赛证。
- REGISTRATION 导入：
  - 可预览 1 条报名记录。
  - 生成对象编号 `REGISTRATION-101`。
  - 写入 `source_registration_id = 101`。
  - 同步生成 3 条成员数据和 2 条参赛证映射。
- 重复导入：
  - `overwriteExisting = false` 时跳过已有来源对象。
  - `overwriteExisting = true` 时旧有效证件映射置为 `INVALID`，新同步证件映射为 `VALID`。
- 参赛证解析：
  - 可通过 `activityId + certificateCode` 解析到评审对象。
  - 支持结合 `sessionId` 做场次对象校验。
- 现场当前对象：
  - 设置当前对象时校验对象属于当前活动、当前轮次、当前场次对象列表。
  - 未锁定或作废对象有明确拒绝处理。

## 8. 发现的数据质量问题

本地未连接真实测试库，暂未发现真实数据质量问题。基于模拟链路，后续真实库联调时需要重点关注：

- 团队负责人、联系人、普通成员在真实表中的角色编码是否一致。
- 参赛证编号是否存在空值、重复值、历史换证数据。
- 报名记录与团队 ID 的关联是否总是完整。
- 个人 `user_id`、`person_id` 与参赛证持有人是否能稳定对应。
- 项目名称、单位名称、联系电话等字段是否存在缺失。

## 9. 测试结果

后端测试：

- 执行目录：`old-code`
- 命令：`mvn test -pl teaching-modules/teaching-competition -am -DskipTests=false -DfailIfNoTests=false`
- 结果：通过。
- 用例结果：`Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`

前端构建：

- 执行目录：`old-code-admin`
- 命令：`npm run build:prod`
- 结果：通过。
- 说明：构建过程中存在项目既有依赖和既有工作流页面的 `eval`、sourcemap 警告，未发现第三包新增页面编译错误。

## 10. 已知问题

- 真实测试库联调未执行，原因是当前环境未提供真实测试库连接信息。
- “重新同步参赛证”按钮当前复用覆盖导入逻辑，会同步刷新对象基础信息、成员、权限和参赛证映射；后续可拆分为只刷新证件映射的专用接口。
- 菜单 SQL 执行后仍需在真实环境给管理员角色分配对应权限。
- 导入预览当前依赖输入来源业务 ID，尚未提供可视化选择团队/报名记录的检索弹窗。
- 现场场次页面仅作为预留入口，本包未实现复杂现场管理。
- 未开发专家端、秘书手机端、被评审人填报端，符合第三包边界。

## 11. 第四包建议

- 增加真实测试库联调脚本和固定样例数据清单。
- 导入页面增加团队/报名记录检索选择弹窗，减少手动输入来源 ID。
- 拆分“重新同步参赛证”专用接口，避免为了刷新证件而重刷完整对象链路。
- 完成评审对象填报端基础页面，支持对象信息、成员、材料维护。
- 建设专家评分 PC 端基础页面，接入分配校验和评分记录。
- 建设秘书端扫码/当前对象切换轻量页面。
- 完善字典配置、权限角色初始化和菜单授权脚本。
