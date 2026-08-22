# Review Engine V1.0 权限矩阵

## 1. 口径说明

本矩阵同时表达业务角色和代码校验。符号说明：

- ✅：角色应具备，且已有对应接口；
- 🔒：仅本人/本人任务/本人场次/已授权对象等数据范围内；
- 👁：只读；
- —：不应授予；
- ⚠️：接口存在，但需要额外权限或当前实现存在耦合。

Controller 实际校验的是 `competition:review:*` 权限字符串，不直接读取业务角色名。`review_activity_user_role`、`review_assignment`、`review_submission_permission`、`review_session` 在 Service 层补充活动内数据权限。

## 2. 角色功能矩阵

|功能|管理员 ADMIN|评审专家 REVIEWER|评审秘书 SECRETARY|填报人 OBJECT_OWNER|普通用户 USER|
|-|-|-|-|-|-|
|活动创建/编辑/删除|✅|—|—|—|—|
|活动与轮次查看|✅|🔒 已分配轮次|🔒 本人场次|🔒 本人填报活动|—|
|轮次创建/编辑/规则绑定|✅|—|—|—|—|
|评审对象创建/编辑/删除|✅|—|—|🔒 仅填报字段|—|
|评审对象查看|✅|🔒 本人任务|🔒 本人场次|🔒 已授权对象|—|
|外部业务导入/材料同步|✅|—|—|—|—|
|成员、证件、外部引用查看|✅|👁 随任务详情|👁 随场次/解析结果|👁 随填报详情|—|
|填报草稿保存|⚠️ 可代审批但无管理员代填接口|—|—|🔒|—|
|材料上传/删除|—|—|—|🔒|—|
|材料查看/下载|✅ 通过管理页面能力|🔒 且 `visible_to_reviewer=1`|⚠️ 没有独立秘书材料接口|🔒 自己的材料|—|
|提交填报|—|—|—|🔒|—|
|申请撤回|—|—|—|🔒|—|
|撤回审批/关闭填报|✅|—|—|—|—|
|专家档案/专家组维护|✅|—|—|—|—|
|评审任务分配|✅|—|—|—|—|
|查看评分规则/指标|✅|🔒 随本人任务|—|—|—|
|保存评分草稿|—|🔒 本人 Assignment|—|—|—|
|提交评分|—|🔒 本人 Assignment|—|—|—|
|查看其他专家评分|👁|—|仅进度，不看明细|—|—|
|场次创建/对象编排|✅|—|—|—|—|
|参赛证解析|✅|—|⚠️ 本人场次，但接口要求 object:query|—|—|
|设置当前对象/下一位|✅|—|🔒 本人场次|—|—|
|更新到场、缺席、跳过、延后|✅|—|🔒 本人场次|—|—|
|轮询现场当前对象|✅|🔒 当前专家有该轮任务|🔒 本人场次详情|—|—|
|生成/重新生成结果|✅|—|—|—|—|
|填写评价结论|✅|—|—|—|—|
|发布/撤回结果|✅|—|—|—|—|
|查看已发布结果|✅|—|—|🔒 本人对象|规划能力 / 未实现公共查询|
|修改专家评分/系统计算分|—|仅提交前修改本人草稿|—|—|—|

## 3. 权限码分组

|领域|权限码|
|-|-|
|活动|`activity:list/query/add/edit/remove`|
|轮次|`round:list/query/add/edit/remove`|
|规则|`rule:list/query/add/edit/remove`|
|指标|`criteria:list/query/add/edit/remove`|
|对象|`object:list/query/add/edit/remove/import`|
|专家|`reviewer:list/query/add/edit/remove`|
|分配|`assignment:list/query/add/edit/remove`|
|场次/专家组|`session:list/query/add/edit/remove`|
|填报|`submission:list/query/edit/submit/withdraw/approve/close`|
|专家任务|`my-review:list/query/edit/submit`|
|秘书|`secretary:query/edit`|
|结果|`result:list/query/generate/edit/publish/revoke/record`|
|旧评分记录|`record:list/query/add/submit`，其中写接口已禁用|

完整权限字符串需加前缀 `competition:review:`。

## 4. 数据权限规则

### REVIEWER

- 活动轮次列表从 `review_assignment.reviewer_user_id = 当前用户` 聚合；未分配任务的活动/轮次不展示。
- 任务详情、评分保存和评分提交再次校验 Assignment 属于当前用户。
- 现场 current-object 查询要求场次活动/轮次与本人任务一致；显式 `panel_id` 不匹配时拒绝，Assignment 未限定专家组时视为轮次级任务。

### SECRETARY

- 优先校验 `review_session.secretary_user_id`；同时允许该场次专家组秘书，或活动内启用的 `SECRETARY/ADMIN/OPERATOR`。
- 设置当前对象时校验对象属于场次、活动和轮次，且未作废、未缺席、未跳过。

### OBJECT_OWNER

- 通过有效 `review_submission_permission` 获得对象范围。
- 权限类型区分 `CREATE`、`EDIT`、`SUBMIT`、`EDIT_SUBMIT`；状态需为 `ACTIVE`。
- 编辑和提交还受对象状态、活动填报截止时间限制。
- 结果仅在 `review_result.result_status = PUBLISHED` 且当前用户拥有该对象权限时可见。

### ADMIN

- 数据库迁移只自动为 `role_key = admin` 补齐评审菜单/按钮关系。
- 管理员不拥有修改专家评分或系统计算分的业务入口；应通过只读评分记录和结果生成接口工作。

## 5. 风险提示

1. “业务角色”和“系统权限码”不是自动一一映射，非 admin 角色需要在系统角色菜单中显式授权。
2. 秘书扫码 UI 调用 `/review/object/certificate/resolve`，该接口要求 `competition:review:object:query`，不是 `secretary:query`；部署时必须额外授权，建议后续拆分秘书专用解析权限。
3. `review_activity_user_role` 不能替代 Controller 权限；它只参与部分 Service 数据权限判断。
