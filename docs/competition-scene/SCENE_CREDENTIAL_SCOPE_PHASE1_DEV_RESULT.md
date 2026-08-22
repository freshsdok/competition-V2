# 现场证件作用域与操作状态升级第一阶段开发结果

## 1. 开发前审计结论

已审计现有现场运行链路代码：

- 后端证件、扫码、流水：
  - `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition`
  - `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition`
- 管理端赛场安排与现场证件页：
  - `old-code-admin/src/views/tournament/sceneSchedule/index.vue`
- PC 端我的赛事/参赛证展示：
  - `old-code-pc/src/views/personal/personaltabs/Competition.vue`
  - `old-code-pc/src/api/personal/index.js`
- 小程序现场证件与扫码：
  - `old-code-mini/pages/my-credential/index.vue`
  - `old-code-mini/pages/scan/result.vue`
  - `old-code-mini/api/scan.js`

审计结论：

- 现有链路为“赛场安排 -> 匹配对象 -> 现场证件 -> 扫码核验/确认 -> 操作流水”。
- 旧证件仍以 `schedule_id` / `target_id` 为主要绑定口径。
- 扫码确认原先主要依赖 `competition_scene_credential` 上的 `report_status` / `material_status` / `waiting_status` 字段做状态判断。
- `operation_log` 原先承担较多状态判断语义，本阶段已调整为审计流水定位。
- PC / 小程序主要展示证件类型和旧状态字段，本阶段已做最小兼容展示。

## 2. 数据库变更

新增 migration：

- `db/migration/20260702_competition_scene_credential_scope_p1.sql`

变更内容：

- `competition_scene_credential` 新增：
  - `issue_channel`
  - `scope_type`
  - `scope_ref_id`
  - `credential_name`
  - `ability_json`
- `competition_scene_credential.schedule_id` 调整为可空，用于兼容大赛级直接发证。
- 回填历史赛场证件：
  - `issue_channel = SCHEDULE_MATCH`
  - `scope_type = SCHEDULE`
  - `scope_ref_id = schedule_id`
  - `credential_name` 按证件类型回填。
  - `ability_json` 按第一阶段固定六字段结构回填。
- 新增表：
  - `competition_scene_subject_operation_state`
- 新增普通查询索引：
  - `idx_scene_subject_operation_lookup`

说明：

- 未使用包含 `deleted` 的唯一键。
- 第一阶段通过 Service 事务查询 + 条件写入保证同一主体同一操作只写一个 `DONE`。
- 未引入 `MIXED`、伪赛场安排、`team_id`、资产管理字段。

## 3. 后端新增/修改

新增 Domain / Query / VO：

- `CompetitionSceneCredentialAbility`
- `CompetitionSceneSubjectOperationState`
- `CompetitionSceneSubjectOperationStateQuery`
- `CompetitionSceneSubjectOperationStateVO`

新增 Mapper / Service：

- `CompetitionSceneSubjectOperationStateMapper`
- `CompetitionSceneSubjectOperationStateMapper.xml`
- `ICompetitionSceneSubjectOperationStateService`
- `CompetitionSceneSubjectOperationStateServiceImpl`

修改：

- `CompetitionSceneConstants`
- `CompetitionSceneCredential`
- `CompetitionSceneVerifyReq`
- `CompetitionSceneVerifyResult`
- `CompetitionSceneCredentialMapper.xml`
- `CompetitionSceneCredentialServiceImpl`
- `CompetitionSceneVerifyServiceImpl`

## 4. 已实现业务能力

证件模型：

- 新增 `issue_channel` 表达发证来源。
- 新增 `scope_type` / `scope_ref_id` 表达证件作用域。
- 新增 `credential_name`，证件展示名与 `credential_type` 解耦。
- 新增 `ability_json`，固定六字段能力结构：
  - `report`
  - `material`
  - `waiting`
  - `review`
  - `resourceReservation`
  - `vipAccess`

赛场发证兼容：

- 赛场匹配发证默认：
  - `issue_channel = SCHEDULE_MATCH`
  - `scope_type = SCHEDULE`
  - `scope_ref_id = schedule_id`
  - 参赛证能力默认开启报道、资料、候场、资源预约。

扫码确认：

- 报道写入 `operation_state` 的 `REPORT` 状态。
- 资料领取写入 `operation_state` 的 `MATERIAL` 状态。
- 候场写入 `operation_state` 的 `WAITING` 状态。
- `REPORT` / `MATERIAL` 使用 `COMPETITION` 级作用域。
- `WAITING` 使用 `SCHEDULE` 级作用域。
- 大赛级证件不会生成候场确认能力。
- 操作成功后同时写 `operation_log`，并回写 `last_log_id`。
- 旧 `credential` 状态字段仍兼容更新，供旧页面展示。

资料领取：

- 第一阶段 `MATERIAL` 状态主体固定为 `USER`。
- 不写 `TEAM + MATERIAL` 作为事实状态。
- 支持本人领取。
- 支持同队成员代领。
- 小程序扫码页资料领取采用二次扫码：
  1. 工作人员先扫被领取人证件。
  2. 选择“本人领取”直接确认。
  3. 选择“同队代领”后再扫代领人证件。
  4. 后端校验同一 `team_code` 后写入代领信息。
- 不允许跨队代领。
- 已领取后返回重复操作，不再重复写业务状态。

状态展示：

- 管理端现场证件列表展示 `credential_name`、`scope_type`。
- 管理端报到/资料/候场 tag 优先由 `operation_state` 回填后的状态展示。
- 管理端资料已领取时展示领取人/代领人。
- PC / 小程序证件页展示 `credential_name`。
- PC / 小程序大赛级证件隐藏候场状态。
- PC / 小程序资料已领取时展示领取人/代领人和领取时间。

## 5. 前端修改

管理端：

- `old-code-admin/src/views/tournament/sceneSchedule/index.vue`
  - 现场证件列表新增证件名称、作用域展示。
  - 状态 tag 兼容 `operation_state` 回填状态。
  - 资料领取显示领取人/代领人。

PC：

- `old-code-pc/src/views/personal/personaltabs/Competition.vue`
  - “参赛证”展示口径调整为“现场证件”。
  - 使用 `credentialName` 展示证件名。
  - 大赛级证件隐藏候场状态。
  - 展示资料领取人/代领人。

小程序：

- `old-code-mini/pages/my-credential/index.vue`
  - 使用 `credentialName` 展示证件名。
  - 大赛级证件隐藏候场状态。
  - 展示资料领取人/代领人。
- `old-code-mini/pages/scan/result.vue`
  - 使用 `credentialName` 展示证件名。
  - 大赛级证件隐藏候场状态。
  - 资料领取增加“本人领取 / 同队代领”交互。
  - 同队代领触发第二次扫码，并提交 `delegateQrContent`。

## 6. 验证结果

后端编译：

- 命令：`mvn -pl teaching-modules/teaching-competition -am compile -DskipTests`
- 结果：`BUILD SUCCESS`

管理端构建：

- 命令：`npm run build:stage`
- 结果：成功。
- 仅存在项目既有 eval / sourcemap / 大包体积类警告。

PC 构建：

- 命令：`npm run build`
- 结果：成功。
- 仅存在项目既有 Sass deprecation / browserslist / 大包体积类警告。

禁用字段静态检查：

- 对本阶段新增/核心改造文件扫描，未出现：
  - `team_id`
  - `asset_no`
  - `owner_unit`
  - `storage_location`
  - `cancel_deadline_minutes`
  - `cancelDeadlineMinutes`
  - `opsConfirm`
  - `MIXED`
  - 伪赛场安排相关实现

补充说明：

- 全仓扫描会命中历史报名/队伍模块已有 `team_id` 字段，属于既有业务表，不是本阶段新增或改造内容。

## 7. 未完成项

本阶段未连接生产数据库，未执行生产 migration。

本阶段未做：

- 大赛级直接发证管理页面。
- 贵宾证页面功能。
- 媒体证、临时证页面。
- 复杂批量导入。
- 复杂人员库管理。
- 状态撤销/重做。
- 独立通知、审批流。

## 8. 后续建议

进入联调前建议：

1. 在测试库执行 `20260702_competition_scene_credential_scope_p1.sql`。
2. 重启正式测试用 competition 服务，确认新 Mapper / Service 已加载。
3. 用历史赛场证件验证报道、资料、候场仍兼容。
4. 用大赛级测试证件验证报道、资料可用且候场不可用。
5. 用同队两张个人证件验证资料代领二次扫码。
6. 用跨队证件验证代领被拒绝。
7. 验证管理端现场证件 tag 与 PC/小程序证件展示。

## 9. 是否可以进入联调

可以进入第一阶段测试库 migration 与正式网关联调。

建议先做小范围联调，不直接扩大到大赛级直接发证页面开发。
