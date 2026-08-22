# Review Engine V1.0 API 文档

## 1. 基本约定

- 服务内 Controller 路径：`/review/**`。
- 经前端网关访问路径：`/competition/review/**`。下文统一使用网关路径。
- JSON 普通响应：`AjaxResult`，核心字段为 `code`、`msg`、`data`。
- 分页响应：`TableDataInfo`，核心字段为 `code`、`msg`、`rows`、`total`；分页查询通常支持 `pageNum`、`pageSize`。
- 权限列完全来自 Controller 的 `@RequiresPermissions`。
- 实体请求/返回字段见[数据库字典](review_engine_database_dictionary.md)；复合 DTO/VO 字段见本文第 6、7 节。
- 本文登记 16 个 Controller 的全部 114 个 Spring Mapping，包括 4 个仍有路由但运行时固定拒绝的旧评分接口。

## 2. 管理端接口

### 2.1 ReviewActivityController（8 个）

基础路径：`/competition/review/activity`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建评审活动|`POST /competition/review/activity`|`competition:review:activity:add`|Body `ReviewActivity`|`ReviewActivity`|默认状态 `DRAFT`、对象类型 `PROJECT`、填报模式 `BUSINESS_IMPORTED`、匿名 `NONE`、发布模式 `NONE`|
|编辑评审活动|`PUT /competition/review/activity/{id}`|`competition:review:activity:edit`|Path `id: Long`；Body `ReviewActivity`|影响行数|仅 `DRAFT/SUBMITTING/SUBMIT_CLOSED/DISABLED` 可编辑；基础接口忽略传入状态|
|查询活动详情|`GET /competition/review/activity/{id}`|`competition:review:activity:query`|Path `id: Long`|`ReviewActivity`|按主键且 `del_flag=0` 查询|
|查询活动列表|`GET /competition/review/activity/list`|`competition:review:activity:list`|Query `ReviewActivity` + 分页|`rows: ReviewActivity[]`|按实体非空字段过滤|
|删除活动|`DELETE /competition/review/activity/{id}`|`competition:review:activity:remove`|Path `id: Long`|影响行数|仅 `DRAFT` 或 `DISABLED` 可软删除|
|预览文件任务导入|`POST /competition/review/activity/{activityId}/file-task/{fileTaskId}/import-preview`|`competition:review:object:import`|Path `activityId,fileTaskId: Long`；Body 可选 `ReviewObjectImportDTO`|`ReviewObjectImportPreviewVO[]`|强制来源类型 `FILE_UPLOAD_MANAGER`；默认仅已提交，不同步证件/材料|
|执行文件任务导入|`POST /competition/review/activity/{activityId}/file-task/{fileTaskId}/import`|`competition:review:object:import`|同上|`ReviewObjectImportResultVO`|导入对象，可按 DTO 配置覆盖、权限和材料同步；事务内写外部引用与审计|
|关闭活动填报|`POST /competition/review/activity/{activityId}/close-submission`|`competition:review:submission:close`|Path `activityId: Long`|`ReviewSubmissionCloseResultVO`|已提交对象锁定，未完成对象作废，活动变更为 `SUBMIT_CLOSED`|

### 2.2 ReviewRoundController（6 个）

基础路径：`/competition/review/round`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建轮次|`POST /competition/review/round`|`competition:review:round:add`|Body `ReviewRound`|`ReviewRound`|未传状态时默认 `DRAFT`|
|编辑轮次|`PUT /competition/review/round/{id}`|`competition:review:round:edit`|Path `id`；Body `ReviewRound`|影响行数|仅 `DRAFT/NOT_STARTED/DISABLED` 可编辑；状态不可经基础接口变更|
|查询轮次详情|`GET /competition/review/round/{id}`|`competition:review:round:query`|Path `id`|`ReviewRound`|按主键查询|
|查询轮次列表|`GET /competition/review/round/list`|`competition:review:round:list`|Query `ReviewRound` + 分页|`rows: ReviewRound[]`|常用过滤 `activityId/roundType/status`|
|删除轮次|`DELETE /competition/review/round/{id}`|`competition:review:round:remove`|Path `id`|影响行数|仅 `DRAFT/NOT_STARTED/DISABLED` 可软删除|
|绑定评分规则|`POST /competition/review/round/{roundId}/bind-rule`|`competition:review:round:edit`|Path `roundId`；Body `ReviewRoundBindRuleDTO`|影响行数|规则须启用且校验通过；规则与轮次属于同一活动；轮次未开始|

### 2.3 ReviewRuleController（9 个）

基础路径：`/competition/review/rule`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建评分规则|`POST /competition/review/rule`|`competition:review:rule:add`|Body `ReviewRule`|`ReviewRule`|新增必须先保存为停用状态|
|编辑评分规则|`PUT /competition/review/rule/{id}`|`competition:review:rule:edit`|Path `id`；Body `ReviewRule`|影响行数|已有提交评分时禁止修改核心字段；启用状态走专用接口|
|查询规则详情|`GET /competition/review/rule/{id}`|`competition:review:rule:query`|Path `id`|`ReviewRule`|按主键查询|
|查询规则列表|`GET /competition/review/rule/list`|`competition:review:rule:list`|Query `ReviewRule` + 分页|`rows: ReviewRule[]`|按活动、轮次、启用状态等过滤|
|删除规则|`DELETE /competition/review/rule/{id}`|`competition:review:rule:remove`|Path `id`|影响行数|有已提交评分记录时禁止删除|
|校验规则|`POST /competition/review/rule/{id}/validate`|`competition:review:rule:query`|Path `id`|`ReviewRuleValidateVO`|校验指标结构、总分、权重和选项|
|启用规则|`POST /competition/review/rule/{id}/enable`|`competition:review:rule:edit`|Path `id`|影响行数|校验必须通过|
|停用规则|`POST /competition/review/rule/{id}/disable`|`competition:review:rule:edit`|Path `id`|影响行数|已有提交评分时的规则变更受保护|
|复制规则|`POST /competition/review/rule/{id}/copy`|`competition:review:rule:add`|Path `id`|新 `ReviewRule`|复制规则及指标，新规则保持停用以便调整|

### 2.4 ReviewCriteriaController（5 个）

基础路径：`/competition/review/criteria`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建评分指标|`POST /competition/review/criteria`|`competition:review:criteria:add`|Body `ReviewCriteria`|`ReviewCriteria`|校验名称、评分项类型、分值/选项和所属规则可编辑性|
|编辑评分指标|`PUT /competition/review/criteria/{id}`|`competition:review:criteria:edit`|Path `id`；Body `ReviewCriteria`|影响行数|规则已有提交评分或活动/轮次不可配置时拒绝|
|查询指标详情|`GET /competition/review/criteria/{id}`|`competition:review:criteria:query`|Path `id`|`ReviewCriteria`|按主键查询|
|查询指标列表|`GET /competition/review/criteria/list`|`competition:review:criteria:list`|Query `ReviewCriteria` + 分页|`rows: ReviewCriteria[]`|通常按 `ruleId/parentId/enabled` 过滤|
|删除评分指标|`DELETE /competition/review/criteria/{id}`|`competition:review:criteria:remove`|Path `id`|影响行数|规则不可修改时拒绝|

### 2.5 ReviewObjectController（15 个）

基础路径：`/competition/review/object`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建评审对象|`POST /competition/review/object`|`competition:review:object:add`|Body `ReviewObject`|`ReviewObject`|要求 activityId；补齐默认对象类型、状态与创建来源|
|编辑评审对象|`PUT /competition/review/object/{id}`|`competition:review:object:edit`|Path `id`；Body `ReviewObject`|影响行数|受对象状态和核心来源字段保护|
|查询对象详情|`GET /competition/review/object/{id}`|`competition:review:object:query`|Path `id`|`ReviewObject`|管理端主键查询|
|查询对象列表|`GET /competition/review/object/list`|`competition:review:object:list`|Query `ReviewObject` + 分页|`rows: ReviewObject[]`|按活动、名称、编码、状态、来源等过滤|
|删除评审对象|`DELETE /competition/review/object/{id}`|`competition:review:object:remove`|Path `id`|影响行数|软删除；业务 Service 校验可删除状态|
|外部导入预览|`POST /competition/review/object/import-preview`|`competition:review:object:import`|Body `ReviewObjectImportDTO`|`ReviewObjectImportPreviewVO[]`|只预览来源映射、数量、权限用户、告警与可导入性|
|执行外部业务导入|`POST /competition/review/object/import-from-business`|`competition:review:object:import`|Body `ReviewObjectImportDTO`|`ReviewObjectImportResultVO`|支持 Competition 与 FileTask 定向来源；同步对象、成员、权限、引用及可选证件/材料|
|同步文件任务材料|`POST /competition/review/object/sync-file-task-materials`|`competition:review:object:import`|Body `ReviewObjectImportDTO`|`ReviewObjectImportResultVO`|只针对已导入 FileTask 对象同步材料，使用来源字段追踪|
|查询对象成员|`GET /competition/review/object/{id}/members`|`competition:review:object:query`|Path `id`|`ReviewObjectMember[]`|按 objectId 查询未删除成员|
|查询填报权限|`GET /competition/review/object/{id}/permissions`|`competition:review:object:query`|Path `id`|`ReviewSubmissionPermission[]`|管理端查看对象授权|
|查询对象证件|`GET /competition/review/object/{id}/certificates`|`competition:review:object:query`|Path `id`|`ReviewObjectCertificateRef[]`|按 objectId 查询证件映射|
|查询外部引用|`GET /competition/review/object/{id}/external-refs`|`competition:review:object:query`|Path `id`|`ReviewObjectExternalRef[]`|按 objectId 查询外部关联|
|新增证件映射|`POST /competition/review/object/certificate`|`competition:review:object:add`|Body `ReviewObjectCertificateRef`|`ReviewObjectCertificateRef`|写入参赛证/人员证件映射|
|查询证件映射列表|`GET /competition/review/object/certificate/list`|`competition:review:object:list`|Query `ReviewObjectCertificateRef` + 分页|`rows: ReviewObjectCertificateRef[]`|按活动、轮次、对象、证件号等过滤|
|解析证件|`GET /competition/review/object/certificate/resolve`|`competition:review:object:query`|Query `activityId: Long`、`certificateCode: String`、可选 `sessionId: Long`|`ReviewCertificateResolveResultVO`|返回全部有效候选；传 sessionId 时标记/过滤场次关系，不自动切换当前对象|

### 2.6 ReviewerProfileController（5 个）

基础路径：`/competition/review/reviewer`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建专家档案|`POST /competition/review/reviewer`|`competition:review:reviewer:add`|Body `ReviewerProfile`|`ReviewerProfile`|未传状态时默认 `ENABLED`|
|编辑专家档案|`PUT /competition/review/reviewer/{id}`|`competition:review:reviewer:edit`|Path `id`；Body `ReviewerProfile`|影响行数|按 ID 更新非空字段|
|查询专家详情|`GET /competition/review/reviewer/{id}`|`competition:review:reviewer:query`|Path `id`|`ReviewerProfile`|按主键查询|
|查询专家列表|`GET /competition/review/reviewer/list`|`competition:review:reviewer:list`|Query `ReviewerProfile` + 分页|`rows: ReviewerProfile[]`|按姓名、单位、学科、关键词、状态等过滤|
|删除专家档案|`DELETE /competition/review/reviewer/{id}`|`competition:review:reviewer:remove`|Path `id`|影响行数|逻辑删除；未自动级联任务|

### 2.7 ReviewAssignmentController（6 个）

基础路径：`/competition/review/assignment`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建单条评审任务|`POST /competition/review/assignment`|`competition:review:assignment:add`|Body `ReviewAssignment`|`ReviewAssignment`|对象不可作废；默认类型 `NORMAL`、状态 `ASSIGNED`、当前分配时间|
|批量分配任务|`POST /competition/review/assignment/batch`|`competition:review:assignment:add`|Body `ReviewAssignmentBatchDTO`|`ReviewAssignmentBatchResultVO`|对 objectIds × reviewerUserIds 分配；校验活动/轮次/对象；可选择覆盖未提交任务|
|编辑评审任务|`PUT /competition/review/assignment/{id}`|`competition:review:assignment:edit`|Path `id`；Body `ReviewAssignment`|影响行数|仅 `ASSIGNED/IN_PROGRESS/RETURNED` 可改；不能直接设 `SUBMITTED`|
|查询任务详情|`GET /competition/review/assignment/{id}`|`competition:review:assignment:query`|Path `id`|`ReviewAssignment`|管理端按主键查询|
|查询任务列表|`GET /competition/review/assignment/list`|`competition:review:assignment:list`|Query `ReviewAssignment` + 分页|`rows: ReviewAssignment[]`|按活动、轮次、对象、专家、专家组、状态过滤|
|删除评审任务|`DELETE /competition/review/assignment/{id}`|`competition:review:assignment:remove`|Path `id`|影响行数|已提交、锁定或取消任务不可删|

### 2.8 ReviewPanelController（5 个）

基础路径：`/competition/review/panel`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|查询专家组列表|`GET /competition/review/panel/list`|`competition:review:session:list`|Query `ReviewPanel` + 分页|`rows: ReviewPanel[]`|按活动、轮次、编码、状态过滤|
|查询专家组详情|`GET /competition/review/panel/{id}`|`competition:review:session:query`|Path `id`|`ReviewPanel`|按主键查询|
|创建专家组|`POST /competition/review/panel`|`competition:review:session:edit`|Body `ReviewPanel`|`ReviewPanel`|通用 CRUD；需保证活动/轮次引用正确|
|编辑专家组|`PUT /competition/review/panel/{id}`|`competition:review:session:edit`|Path `id`；Body `ReviewPanel`|影响行数|按 ID 更新|
|删除专家组|`DELETE /competition/review/panel/{id}`|`competition:review:session:edit`|Path `id`|影响行数|逻辑删除；未声明数据库级级联|

### 2.9 ReviewPanelMemberController（3 个）

基础路径：`/competition/review/panel-member`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|查询专家组成员|`GET /competition/review/panel-member/list`|`competition:review:session:list`|Query `ReviewPanelMember` + 分页|`rows: ReviewPanelMember[]`|通常按 panelId/activityId/roundId 过滤|
|新增专家组成员|`POST /competition/review/panel-member`|`competition:review:session:edit`|Body `ReviewPanelMember`|`ReviewPanelMember`|通用 CRUD，无单独批量接口|
|删除专家组成员|`DELETE /competition/review/panel-member/{id}`|`competition:review:session:edit`|Path `id`|影响行数|逻辑删除|

### 2.10 ReviewSessionController（12 个）

基础路径：`/competition/review/session`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|创建现场场次|`POST /competition/review/session`|`competition:review:session:add`|Body `ReviewSession`|`ReviewSession`|未传状态时默认 `NOT_STARTED`|
|编辑现场场次|`PUT /competition/review/session/{id}`|`competition:review:session:edit`|Path `id`；Body `ReviewSession`|影响行数|进行中/结束/归档后不可基础编辑；当前对象和状态必须走专用流程|
|查询场次详情|`GET /competition/review/session/{id}`|`competition:review:session:query`|Path `id`|`ReviewSession`|管理端按主键查询|
|查询场次列表|`GET /competition/review/session/list`|`competition:review:session:list`|Query `ReviewSession` + 分页|`rows: ReviewSession[]`|按活动、轮次、专家组、秘书、状态过滤|
|删除现场场次|`DELETE /competition/review/session/{id}`|`competition:review:session:remove`|Path `id`|影响行数|进行中/结束/归档场次不可删|
|管理员设置当前对象|`POST /competition/review/session/{sessionId}/current-object`|`competition:review:session:edit`|Path `sessionId`；Body `ReviewSessionCurrentObjectDTO`|`ReviewSessionCurrentObjectVO`|对象必须在场次顺序内且同活动/轮次、未作废；更新场次与事件日志|
|查询当前对象|`GET /competition/review/session/{sessionId}/current-object`|`competition:review:session:query`|Path `sessionId`|`ReviewSessionCurrentObjectVO`|管理端通用查询，不执行专家任务范围校验|
|添加场次对象|`POST /competition/review/session/object`|`competition:review:session:add`|Body `ReviewSessionObject`|`ReviewSessionObject`|场次未开始；对象与场次同活动；同场次不可重复；缺省顺序自动递增|
|编辑场次对象|`PUT /competition/review/session/object/{id}`|`competition:review:session:edit`|Path `id`；Body `ReviewSessionObject`|影响行数|场次开始后不能由管理端维护|
|删除场次对象|`DELETE /competition/review/session/object/{id}`|`competition:review:session:remove`|Path `id`|影响行数|场次开始后不可删|
|查询场次对象列表|`GET /competition/review/session/object/list`|`competition:review:session:list`|Query `ReviewSessionObject` + 分页|`rows: ReviewSessionObject[]`|通常按 sessionId 查询并按 sequenceNo 排序|
|查询现场事件日志|`GET /competition/review/session/event-log/list`|`competition:review:session:list`|Query `ReviewSessionEventLog` + 分页|`rows: ReviewSessionEventLog[]`|按活动、轮次、场次、对象、事件过滤|

### 2.11 ReviewResultController（9 个）

基础路径：`/competition/review/result`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|生成评审结果|`POST /competition/review/result/generate`|`competition:review:result:generate`|Body `ReviewResultGenerateDTO`|`ReviewResultGenerateResponseVO`|按已提交记录平均分汇总；无提交则跳过；未全部完成时告警；已发布结果不覆盖|
|填写评价结论|`PUT /competition/review/result/{id}/conclusion`|`competition:review:result:edit`|Path `id`；Body `ReviewResultConclusionDTO`|`ReviewResult`|只更新发布性结论及操作人/时间，不改系统计算分|
|发布结果|`POST /competition/review/result/{id}/publish`|`competition:review:result:publish`|Path `id`；Body `ReviewResultPublishDTO`|`ReviewResult`|必须已有 `calculatedScore`；写发布日志与审计日志|
|撤回结果发布|`POST /competition/review/result/{id}/revoke`|`competition:review:result:revoke`|Path `id`；Body 可选 `ReviewResultRevokeDTO`|`ReviewResult`|仅 `PUBLISHED` 可撤回；状态变为 `REVOKED`|
|查询结果详情|`GET /competition/review/result/{id}`|`competition:review:result:query`|Path `id`|`ReviewResult`|按主键查询|
|查询结果列表|`GET /competition/review/result/list`|`competition:review:result:list`|Query `ReviewResultQueryDTO` + 分页|`rows: ReviewResultListVO[]`|支持完成状态、对象和结果状态过滤|
|查询对象评分记录|`GET /competition/review/result/records`|`competition:review:result:record`|Query `activityId,roundId,objectId: Long`|`ReviewResultRecordVO[]`|三项必填；只读返回专家记录摘要|
|按对象路径查询评分记录|`GET /competition/review/result/{objectId}/records`|`competition:review:result:record`|Path `objectId`；Query `activityId,roundId`|`ReviewResultRecordVO[]`|与上一接口同一 Service，三项均必填|
|查询评分明细|`GET /competition/review/result/record/{recordId}/details`|`competition:review:result:record`|Path `recordId`|`ReviewScoreDetailReadonlyVO[]`|只读返回评分项快照|

### 2.12 ReviewRecordController（4 个兼容路由，全部禁用）

基础路径：`/competition/review/record`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|旧评分草稿|`POST /competition/review/record/draft`|`competition:review:record:add`|Body `ReviewRecordDraftDTO`|固定业务异常|**已禁用**；改用 `/my-review/{assignmentId}/draft`|
|旧评分提交|`POST /competition/review/record/submit`|`competition:review:record:submit`|Body `ReviewRecordSubmitDTO`|固定业务异常|**已禁用**；改用 `/my-review/{assignmentId}/submit`|
|旧评分详情|`GET /competition/review/record/{id}`|`competition:review:record:query`|Path `id`|固定业务异常|**已禁用**；改用结果只读接口或专家任务详情|
|旧我的评分列表|`GET /competition/review/record/my-list`|`competition:review:record:list`|Query `ReviewRecord`|固定业务异常|**已禁用**；改用 `/my-review/list`|

## 3. 用户端接口

### 3.1 ReviewSubmissionController（11 个）

基础路径：`/competition/review/submission`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|查询我的填报任务|`GET /competition/review/submission/my-list`|`competition:review:submission:list`|分页参数|`rows: ReviewSubmissionTaskVO[]`|只读取当前用户 `ACTIVE` 的 Submission Permission|
|查询填报详情|`GET /competition/review/submission/{objectId}`|`competition:review:submission:query`|Path `objectId`|`ReviewSubmissionDetailVO`|当前用户必须有该对象权限；返回对象、成员、材料、权限和可操作状态|
|查看已发布结果|`GET /competition/review/submission/{objectId}/result`|`competition:review:submission:query`|Path `objectId`|`ReviewSubmissionResultVO`|当前用户有对象权限且结果状态为 `PUBLISHED`|
|保存填报草稿|`PUT /competition/review/submission/{objectId}/draft`|`competition:review:submission:edit`|Path `objectId`；Body `ReviewSubmissionDraftDTO`|`ReviewObject`|需 `EDIT/EDIT_SUBMIT` 权限；对象可编辑且未过截止时间|
|新增填报材料|`POST /competition/review/submission/{objectId}/material`|`competition:review:submission:edit`|Path `objectId`；Body `ReviewSubmissionMaterialDTO`|`ReviewObjectMaterial`|需编辑权限；fileUrl 必填；默认专家可见和 `NORMAL`|
|查询填报材料|`GET /competition/review/submission/{objectId}/materials`|`competition:review:submission:query`|Path `objectId`|`ReviewObjectMaterial[]`|当前用户需具有任一有效对象权限|
|删除填报材料|`DELETE /competition/review/submission/material/{materialId}`|`competition:review:submission:edit`|Path `materialId`|影响行数|需编辑权限且对象可编辑；材料标记删除并写日志|
|提交填报|`POST /competition/review/submission/{objectId}/submit`|`competition:review:submission:submit`|Path `objectId`|`ReviewObject`|需 `SUBMIT/EDIT_SUBMIT`；校验状态、截止时间、对象名称和必要材料|
|申请撤回|`POST /competition/review/submission/{objectId}/withdraw-request`|`competition:review:submission:withdraw`|Path `objectId`；Body 可选 `ReviewSubmissionActionDTO`|`ReviewObject`|仅 `SUBMITTED` 且未过截止时间；状态改为 `WITHDRAW_REQUESTED`|
|审批撤回|`POST /competition/review/submission/{objectId}/withdraw-approve`|`competition:review:submission:approve`|Path `objectId`；Body 可选 `ReviewSubmissionActionDTO`|`ReviewObject`|仅 `WITHDRAW_REQUESTED`；状态改为 `WITHDRAW_APPROVED`|
|驳回撤回|`POST /competition/review/submission/{objectId}/withdraw-reject`|`competition:review:submission:approve`|Path `objectId`；Body 可选 `ReviewSubmissionActionDTO`|`ReviewObject`|仅 `WITHDRAW_REQUESTED`；当前实现回到 `SUBMITTED`|

## 4. 专家端接口

### 4.1 ReviewMyReviewController（7 个）

基础路径：`/competition/review/my-review`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|查询我的活动轮次卡片|`GET /competition/review/my-review/activity-rounds`|`competition:review:my-review:list`|无|`ReviewMyReviewActivityRoundVO[]`|仅从当前专家已有 Assignment 聚合；按活动+轮次分类；现场轮次解析可用 sessionId|
|轮询专家当前现场对象|`GET /competition/review/my-review/session/{sessionId}/current-object`|`competition:review:my-review:list`|Path `sessionId`|`ReviewSessionCurrentObjectVO`|场次活动/轮次必须存在本人任务；显式专家组需匹配，未限定 panel 的任务视为轮次级|
|查询我的评审任务|`GET /competition/review/my-review/list`|`competition:review:my-review:list`|Query 见下表|`rows: ReviewMyReviewTaskVO[]`|仅当前用户任务；指定 sessionId 时当前对象全局置顶后再手工分页|
|查询任务详情|`GET /competition/review/my-review/{assignmentId}`|`competition:review:my-review:query`|Path `assignmentId`|`ReviewMyReviewDetailVO`|Assignment 必须属于当前用户；返回对象、成员、可见材料、规则、指标和已有评分|
|查询任务评分指标|`GET /competition/review/my-review/{assignmentId}/criteria`|`competition:review:my-review:query`|Path `assignmentId`|`ReviewCriteria[]`|任务归属校验；按轮次绑定规则加载启用指标|
|保存评分草稿|`POST /competition/review/my-review/{assignmentId}/draft`|`competition:review:my-review:edit`|Path `assignmentId`；Body `ReviewMyReviewScoreDTO`|`ReviewRecord`|只能保存本人可评任务；校验评分项；记录状态 `DRAFT`，任务状态 `IN_PROGRESS`|
|提交评分|`POST /competition/review/my-review/{assignmentId}/submit`|`competition:review:my-review:submit`|Path `assignmentId`；Body `ReviewMyReviewScoreDTO`|`ReviewRecord`|校验全部必填项和总分；记录/任务均变为 `SUBMITTED`，提交后不可通过基础接口改分|

任务列表 Query 参数：

|字段|类型|说明|
|-|-|-|
|`activityId`|Long|活动 ID；卡片进入列表时携带|
|`roundId`|Long|轮次 ID；卡片进入列表时携带|
|`objectName`|String|对象名称模糊过滤|
|`objectCode`|String|对象编号过滤|
|`assignmentStatus`|String|任务状态过滤|
|`keywords`|String|关键词过滤|
|`sessionId`|Long|现场场次 ID，用于识别当前对象并置顶|
|`pageNum`|Integer|页码，默认 1|
|`pageSize`|Integer|每页条数，默认 10，最大 500|

### 4.2 ReviewMaterialController（3 个）

基础路径：`/competition/review/material`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|获取材料预览信息|`GET /competition/review/material/preview/{fileId}`|`competition:review:my-review:query`|Path `fileId`|`ReviewMaterialPreviewVO`|校验当前专家拥有该对象任务且材料专家可见；返回预览/下载 URL 和转换状态|
|获取材料预览流|`GET /competition/review/material/preview-stream/{fileId}`|`competition:review:my-review:query`|Path `fileId`|文件二进制流|同上；支持图片、PDF、文本和可转换 Office；不支持时返回 JSON 错误|
|下载材料|`GET /competition/review/material/download/{fileId}`|`competition:review:my-review:query`|Path `fileId`|附件二进制流|校验任务归属、材料可见性和文件路径安全|

## 5. 秘书端接口

### 5.1 ReviewSecretaryController（6 个）

基础路径：`/competition/review/secretary`

|接口名称|方法与 URL|权限|参数|返回|业务规则|
|-|-|-|-|-|-|
|查询我的现场场次|`GET /competition/review/secretary/session/my-list`|`competition:review:secretary:query`|Query `ReviewSession`|`ReviewSecretarySessionVO[]`|只返回当前用户作为场次秘书、专家组秘书或活动内秘书/管理员/操作员的场次|
|查询秘书场次详情|`GET /competition/review/secretary/session/{sessionId}`|`competition:review:secretary:query`|Path `sessionId`|`ReviewSecretarySessionVO`|先校验当前用户有权操作该场次|
|查询场次对象与评分进度|`GET /competition/review/secretary/session/{sessionId}/objects`|`competition:review:secretary:query`|Path `sessionId`|`ReviewSecretarySessionObjectVO[]`|返回顺序、到场/评审状态及已提交/总任务进度，不暴露评分明细|
|秘书设置当前对象|`POST /competition/review/secretary/session/{sessionId}/current-object`|`competition:review:secretary:edit`|Path `sessionId`；Body `ReviewSessionCurrentObjectDTO`|`ReviewSessionCurrentObjectVO`|二次确认后切换；同活动/轮次且在场次内；缺席/跳过/作废对象不能切换|
|切换下一位|`POST /competition/review/secretary/session/{sessionId}/next-object`|`competition:review:secretary:edit`|Path `sessionId`|`ReviewSessionCurrentObjectVO`|按 sequenceNo 查找可评下一位；无可切换对象时报错|
|更新场次对象状态|`POST /competition/review/secretary/session-object/{sessionObjectId}/status`|`competition:review:secretary:edit`|Path `sessionObjectId`；Body `ReviewSecretarySessionObjectStatusDTO`|`ReviewSecretarySessionObjectVO`|至少传签到、评审状态或备注之一；校验枚举并写事件日志|

证件解析不是 SecretaryController 自有接口。秘书页面实际调用 `GET /competition/review/object/certificate/resolve`，因此还需要 `competition:review:object:query` 权限；解析只返回候选，必须再调用秘书设置当前对象接口。

## 6. 请求 DTO 字段

### 6.1 导入与分配

|DTO|字段|类型|说明|
|-|-|-|-|
|`ReviewObjectImportDTO`|`activityId`|Long|目标活动|
||`sourceModule`|String|来源模块|
||`sourceBizType`|String|来源业务类型|
||`sourceBizIds`|List<String>|来源记录 ID 列表|
||`competitionSeriesId`|Long|竞赛系列 ID|
||`defenseScheduleText`|String|答辩排期文本|
||`fileTaskId`|Long|文件上传任务 ID|
||`submittedOnly`|Boolean|是否只导入已提交来源|
||`defaultObjectType`|String|默认 Review Object 类型|
||`permissionUserMode`|String|填报权限用户策略|
||`overwriteExisting`|Boolean|是否覆盖已有对象|
||`syncCertificate`|Boolean|是否同步证件|
||`syncMaterial`|Boolean|是否同步材料|
||`initialSubmitStatus`|String|初始状态，仅支持 `DRAFT/LOCKED`|
||`materialOverwriteMode`|String|材料覆盖策略|
||`specifiedUserIds`|List<Long>|指定授权用户|
|`ReviewAssignmentBatchDTO`|`activityId`|Long|活动 ID|
||`roundId`|Long|轮次 ID|
||`objectIds`|List<Long>|对象 ID|
||`reviewerUserIds`|List<Long>|专家用户 ID|
||`panelId`|Long|可选专家组|
||`assignmentType`|String|分配类型，默认 `NORMAL`|
||`overwriteExisting`|Boolean|覆盖未提交的已有任务|
||`remark`|String|备注|

### 6.2 评分

|DTO|字段|类型|说明|
|-|-|-|-|
|`ReviewMyReviewScoreDTO`|`scoreDetails`|List<ReviewScoreDetailDTO>|评分项列表|
||`commentText`|String|总体评语|
||`recommendation`|String|推荐意见|
|`ReviewScoreDetailDTO`|`criteriaId`|Long|指标 ID|
||`criteriaName`|String|客户端名称；服务端以规则指标为准|
||`scoreType`|String|评分类型|
||`scoreValue`|BigDecimal|数字得分|
||`optionValue`|String|单选值|
||`textValue`|String|文本评价|
||`weight`|BigDecimal|权重；服务端保存规则快照|
||`sortOrder`|Integer|排序|

`ReviewRecordDraftDTO` 和 `ReviewRecordSubmitDTO` 属于已禁用旧接口，不应由新客户端使用。

### 6.3 填报、现场与结果

|DTO|字段|类型|说明|
|-|-|-|-|
|`ReviewSubmissionDraftDTO`|`objectName, summary, orgName, contactName, contactPhone, contactEmail, subjectCode1-3, categoryCodes, keywords, extraData`|String|填报人可编辑对象快照|
|`ReviewSubmissionMaterialDTO`|`materialName, materialType, fileName, fileUrl, mimeType, fileExt, visibleToReviewer`|String|材料信息；fileUrl 必填|
||`fileSize`|Long|文件大小|
||`sortOrder`|Integer|排序|
|`ReviewSubmissionActionDTO`|`actionReason`|String|撤回/审批意见|
|`ReviewSessionCurrentObjectDTO`|`objectId`|Long|目标当前对象|
||`operatorUserId`|Long|操作用户；服务可取当前用户|
||`sourceType`|String|`SCAN/NEXT/MANUAL`|
||`certificateCode`|String|扫码证件号|
|`ReviewSecretarySessionObjectStatusDTO`|`checkinStatus`|String|`WAITING/PRESENT/ABSENT/LATE`|
||`reviewStatus`|String|`WAITING/REVIEWING/SCORED/COMPLETED/SKIPPED/DELAYED`|
||`secretaryNote`|String|秘书备注|
|`ReviewResultGenerateDTO`|`activityId`|Long|必填活动|
||`roundId`|Long|可选轮次|
||`objectIds`|List<Long>|可选对象范围|
||`generatedBy`|Long|汇总操作人；为空取当前用户|
||`forceRegenerate`|Boolean|请求字段；已发布结果仍不会直接覆盖|
|`ReviewResultConclusionDTO`|`evaluationConclusion, operatorUserId`|String, Long|发布性结论与操作人|
|`ReviewResultPublishDTO`|`publishScope, publishContent, operatorUserId`|String, String, Long|发布范围、内容和操作人|
|`ReviewResultRevokeDTO`|`revokeReason, operatorUserId`|String, Long|撤回原因和操作人|
|`ReviewResultQueryDTO`|`activityId, roundId`|Long|活动、轮次过滤|
||`objectCode, objectName, orgName, resultStatus, completionStatus`|String|对象、状态与完成度过滤|
|`ReviewRoundBindRuleDTO`|`ruleId`|Long|绑定的规则 ID|

## 7. 主要返回 VO 字段

### 7.1 导入、证件与分配

|VO|返回字段|
|-|-|
|`ReviewObjectImportPreviewVO`|`sourceBizId, sourceBizType, defenseOrder, inputOrgName, inputTeamName, inputLeaderName, teamCode, teamName, objectName, leaderName, memberCount, certificateCount, materialCount, permissionUsers, warnings, canImport`|
|`ReviewObjectImportResultVO`|`totalCount, successCount, skipCount, failedCount, importedCount, skippedCount, createdObjectIds, skippedItems, failedItems, message`|
|`ReviewCertificateResolveResultVO`|`activityId, certificateCode, matchedCount, candidates, warningMessage`|
|`ReviewCertificateResolveVO`|`objectId, objectCode, objectName, activityId, submitStatus, certificateCode, certificateType, memberName, memberRole, sourceTeamId, sourceRegistrationId, validStatus, inSession, warningMessage`|
|`ReviewAssignmentBatchResultVO`|`totalCount, successCount, skipCount, failedCount, createdAssignmentIds, skippedItems, failedItems`|

### 7.2 专家任务与材料

|VO|返回字段|
|-|-|
|`ReviewMyReviewActivityRoundVO`|`activityId, activityName, activityCode, activityType, objectType, status, roundId, roundName, roundNo, roundType, roundStatus, roundStartTime, roundEndTime, sessionId, sessionName, sessionStatus, reviewStartTime, reviewEndTime, taskCount, pendingTaskCount, submittedTaskCount, lastAssignedTime`|
|`ReviewMyReviewTaskVO`|`assignmentId, activityId, activityName, roundId, roundName, objectId, objectCode, objectName, objectStatus, orgName, summary, subjectCode1-3, categoryCodes, keywords, assignmentStatus, recordId, recordStatus, totalScore, submittedTime, canReview, cannotReviewReason, currentObject`|
|`ReviewMyReviewDetailVO`|`assignment, reviewObject, round, members, materials, rule, criteriaList, existingRecord, existingScoreDetails, canReview, cannotReviewReason`|
|`ReviewMaterialPreviewVO`|`fileId, fileName, fileType, previewType, previewUrl, downloadUrl, converted, message`|
|`ReviewSessionCurrentObjectVO`|`sessionId, activityId, roundId, objectId, objectCode, objectName, currentStartedTime, status`|

### 7.3 填报

|VO|返回字段|
|-|-|
|`ReviewSubmissionTaskVO`|`permissionId, activityId, activityName, objectId, objectCode, objectName, orgName, submitStatus, submitDeadline, editable, withdrawable, lastUpdateTime`|
|`ReviewSubmissionDetailVO`|`object, members, materials, permissions, currentStatus, editable, submittable, withdrawable, warningMessage`|
|`ReviewSubmissionResultVO`|`activityId, activityName, roundId, roundName, objectId, objectCode, objectName, calculatedScore, calculatedGrade, calculatedRank, evaluationConclusion, resultStatus, publishedTime`|
|`ReviewSubmissionCloseResultVO`|`activityId, lockedCount, invalidCount, ignoredCount, message`|

### 7.4 秘书控制台

|VO|返回字段|
|-|-|
|`ReviewSecretarySessionVO`|`sessionId, sessionName, sessionCode, location, startTime, endTime, activityId, activityName, roundId, roundName, status, objectCount, currentObjectId, currentObjectCode, currentObjectName, currentStartedTime`|
|`ReviewSecretarySessionObjectVO`|`sessionObjectId, sequenceNo, objectId, objectCode, objectName, orgName, leaderName, checkinStatus, reviewStatus, scoreProgress, actualStartTime, actualEndTime, secretaryNote`|
|`ReviewScoreProgressVO`|`submittedCount, totalAssignedCount, unsubmittedCount, displayText`|

### 7.5 结果与规则

|VO|返回字段|
|-|-|
|`ReviewResultGenerateResponseVO`|`totalCount, generatedCount, skippedCount, warningCount, warnings, results`|
|`ReviewResultListVO`|`resultId, activityId, activityName, roundId, roundName, objectId, objectCode, objectName, orgName, submitStatus, reviewerCount, submittedCount, unsubmittedCount, completionText, completionStatus, calculatedScore, calculatedGrade, calculatedRank, evaluationConclusion, resultStatus, generatedTime, publishedTime, revokedTime`|
|`ReviewResultRecordVO`|`recordId, assignmentId, reviewerId, reviewerUserId, reviewerName, recordStatus, totalScore, grade, recommendation, commentText, submittedTime`|
|`ReviewScoreDetailReadonlyVO`|`detailId, recordId, criteriaId, criteriaName, scoreType, scoreValue, optionValue, textValue, weight, sortOrder`|
|`ReviewRuleValidateVO`|`valid, errors, warnings, scoreMode, totalScore, maxScoreSum, weightSum, weightedMaxScore, countableCriteriaCount`|

## 8. 核心业务与安全规则

### 8.1 权限校验

接口权限不是完整的数据权限。Service 还会执行：

- 专家：`assignment.reviewer_user_id = 当前用户`；
- 填报人：存在当前用户、对象范围内且状态有效的 `review_submission_permission`；
- 秘书：当前用户是场次秘书、专家组秘书，或活动内启用的秘书/管理员/操作员；
- 材料：专家拥有对象 Assignment，且材料 `visible_to_reviewer` 可见；
- 结果查看：填报人拥有对象权限，且结果已发布。

### 8.2 状态限制

- 关键实体状态不能通过通用编辑接口任意改变；活动、轮次、场次、评分、填报和结果由专用流程写入。
- 专家任务已提交、锁定或取消后，管理端不能基础编辑/删除。
- 专家提交评分后，只能通过后续正式退回流程重新开放；V1.0 没有管理端直接改分入口。
- 场次开始后不能从管理端改变对象编排；现场切换由管理员专用 current-object 或秘书控制台处理。
- 已发布结果不能直接重新生成，必须先撤回。

### 8.3 数据来源

- 列表与详情类接口由对应 Review Mapper 查询自有表。
- 外部导入由 `ReviewObjectServiceImpl` 读取 Competition 报名/团队/证件/答辩排期或 FileTask 远程数据，再落为 Review Object 快照及 External Ref。
- 结果生成只聚合 `review_record.record_status = SUBMITTED` 的评分记录。
- 秘书评分进度来自场次对象关联的 Assignment 总数与已提交 Record 数。

## 9. API 数量核对

|Controller|Mapping 数|
|-|-:|
|ReviewActivityController|8|
|ReviewAssignmentController|6|
|ReviewCriteriaController|5|
|ReviewMaterialController|3|
|ReviewMyReviewController|7|
|ReviewObjectController|15|
|ReviewPanelController|5|
|ReviewPanelMemberController|3|
|ReviewRecordController|4（全部禁用）|
|ReviewResultController|9|
|ReviewRoundController|6|
|ReviewRuleController|9|
|ReviewSecretaryController|6|
|ReviewSessionController|12|
|ReviewSubmissionController|11|
|ReviewerProfileController|5|
|**合计**|**114**|
