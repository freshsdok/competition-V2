# 赛场安排配置与评审对象绑定及顺序增强包执行报告

生成时间：2026-07-07

## 1. 当前赛场安排模块审查结果

已审查现有赛场安排配置模块，当前模块并非空白模块，已经存在赛场安排主表、安排对象表、证件表、资源预约等能力。

实际表和代码：

- 赛场安排主表：`competition_scene_schedule`
- 赛场安排对象表：`competition_scene_schedule_target`
- 赛场资源关系表：`competition_scene_schedule_resource`
- 赛场证件表：`competition_scene_credential`
- 主实体：`CompetitionSceneSchedule`
- 对象实体：`CompetitionSceneScheduleTarget`
- Mapper：`CompetitionSceneScheduleMapper`、`CompetitionSceneScheduleTargetMapper`
- Service：`ICompetitionSceneScheduleService`、`CompetitionSceneScheduleServiceImpl`
- Controller：`CompetitionSceneScheduleController`
- 前端页面：`old-code-admin/src/views/tournament/sceneSchedule/index.vue`
- 前端 API：`old-code-admin/src/api/tournament/sceneSchedule.js`

现有 `competition_scene_schedule_target` 已表达“某个赛场安排绑定某些人员/团队”，但字段偏向证件发放和人员配置，缺少评审对象绑定、绑定对象类型、证件编号快照、赛场顺序、来源业务字段。

## 2. 是否已有绑定表

已有绑定表：`competition_scene_schedule_target`。

本包未新增新表，而是在现有表上增强字段，避免破坏历史数据和既有证件生成功能。

## 3. 新增或增强的数据表

迁移脚本：

- `db/migration/20260707_scene_schedule_review_binding.sql`

增强表：

- `competition_scene_schedule_target`

## 4. 新增字段

新增字段均可为空，不加唯一约束，不强制迁移历史数据：

- `target_type`：绑定对象类型，支持 `REVIEW_OBJECT/TEAM/PERSON/USER/CREDENTIAL/MANUAL`
- `review_object_id`：评审对象 ID
- `target_name`：对象名称/团队名称/人员姓名
- `certificate_code`：参赛证或现场证件编号
- `sequence_no`：赛场内顺序号
- `source_module`：来源模块
- `source_biz_type`：来源业务类型
- `source_biz_id`：来源业务 ID

新增索引：

- `idx_scene_target_schedule_sequence(schedule_id, sequence_no)`
- `idx_scene_target_review_object(review_object_id)`
- `idx_scene_target_certificate_code(certificate_code)`
- `idx_scene_target_type(target_type)`

## 5. 新增接口

在 `CompetitionSceneScheduleController` 中补充：

- `POST /competition/scene/schedule/{scheduleId}/targets/review-objects`
- `POST /competition/scene/schedule/{scheduleId}/targets/teams`
- `POST /competition/scene/schedule/{scheduleId}/targets/persons`
- `POST /competition/scene/schedule/{scheduleId}/targets/manual`
- `POST /competition/scene/schedule/{scheduleId}/targets/sequence`
- `POST /competition/scene/schedule/{scheduleId}/targets/sequence/auto-generate`
- `POST /competition/scene/schedule/{scheduleId}/targets/sync-review-session`

保留原有接口：

- `/competition/sceneSchedule/...`
- `/competition/sceneSchedule/target/...`

兼容说明：后端同时注册了 `/scene/schedule` 和 `/competition/scene/schedule`，以兼容网关是否剥离 `/competition` 前缀的两种联调方式。

## 6. 管理端页面

已增强 `old-code-admin/src/views/tournament/sceneSchedule/index.vue`：

- 原“人员配置”Tab 调整为“绑定对象”
- 增加对象类型、评审对象 ID、证件编号查询条件
- 增加顺序号编辑列
- 增加对象类型、绑定对象、证件编号展示列
- 增加“添加评审对象”
- 增加“添加团队”
- 增加“添加人员”
- 增加“手工对象”
- 保留原“新增人员”和“生成所选证件”
- 增加“自动顺序”
- 增加“重排全部”
- 增加“保存顺序”
- 增加“同步评审场次”

## 7. 绑定评审对象实现

支持按评审对象 ID 批量绑定。

后端逻辑：

- 校验赛场安排存在
- 校验评审对象存在
- 写入 `target_type = REVIEW_OBJECT`
- 写入 `review_object_id`
- 冗余对象名称、单位、联系人、来源团队、来源报名等信息
- 尝试带出有效参赛证编号
- 已绑定对象跳过并返回 warning

## 8. 绑定团队实现

支持按团队编号批量绑定。

后端逻辑：

- 优先尝试通过 `review_object.source_team_id` 匹配评审对象
- 匹配到时写入 `review_object_id`
- 未匹配到时使用竞赛报名数据或手工结构保存为赛场安排对象
- 写入 `target_type = TEAM`
- 写入 `team_code/source_biz_id`
- 不强制要求团队已生成评审对象

## 9. 绑定人员实现

支持按报名成员 ID 批量绑定。

后端逻辑：

- 优先通过 `review_object_member.person_id` 匹配评审对象
- 成员 ID 支持字符串；能解析为数字时兼容旧 `member_id` 字段
- 匹配到时写入 `review_object_id`
- 未匹配到时使用竞赛报名数据或手工结构保存为赛场安排对象
- 写入 `target_type = PERSON`
- 写入 `source_biz_id`

## 10. 手工新增实现

支持手工新增现场安排对象：

- `target_type = MANUAL`
- `source_module = MANUAL`
- 保存对象名称、所属单位、联系方式、备注
- 手工对象不强制进入评审评分
- 后续如需评分，可再扩展“手工对象生成评审对象”

## 11. 顺序维护实现

已实现：

- 绑定对象列表排序：有 `sequence_no` 的排前，无顺序的排后，再按创建时间和 `target_id` 稳定排序
- 手动编辑顺序
- 批量保存顺序
- 同一赛场安排下非空 `sequence_no` 不允许重复
- 自动生成顺序
- 重排全部顺序
- 按姓名批量排序：支持输入换行、逗号、顿号、分号或空白分隔的姓名名单，按输入顺序匹配 `userName/targetName/teamName/leaderTeacher/guideTeacher`，匹配项排前，未匹配项保留原相对顺序排后

## 12. 与评审模块同步实现

已实现将赛场安排绑定对象同步到 `review_session_object`。

匹配优先级：

1. `review_object_id`
2. `team_code` -> `review_object.source_team_id`
3. `member_id/source_biz_id` -> `review_object_member.person_id`
4. `user_id` -> `review_object_member.user_id`
5. `certificate_code` -> `review_object_certificate_ref.certificate_code`

同步规则：

- 匹配到评审对象时写入或更新 `review_session_object`
- `sequence_no` 同步到 `review_session_object.sequence_no`
- 已存在同场次同对象时更新顺序，不重复新增
- 匹配不到对象时跳过并返回 warning
- 不改变扫码校验
- 不改变专家分配
- 不改变评分记录
- 管理端“同步评审场次”已改为分页选择评审场次，不再要求手工输入场次 ID

## 13. 测试结果

已执行：

- 后端编译：`mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`
- 结果：通过

- 前端构建：`npm run build:prod`
- 目录：`old-code-admin`
- 结果：通过
- 说明：存在项目既有 `eval` 和 sourcemap warning，未阻断构建。

## 14. 浏览器联调结果

本次完成代码级和构建级验证。

尚未在浏览器中完成真实测试库联调，原因是本轮未启动网关、competition 服务和 admin 前端服务。建议下一步在 UAT 环境启动后按以下流程验证：

1. 创建或选择赛场安排；
2. 在“绑定对象”Tab 添加评审对象；
3. 添加团队；
4. 添加人员；
5. 手工新增对象；
6. 自动生成顺序；
7. 手动调整顺序并保存；
8. 同步到评审现场场次；
9. 打开秘书端验证“下一位”按 `review_session_object.sequence_no` 推进；
10. 扫码验证仍按原逻辑，不强制来自赛场安排。

## 15. 已知问题

- 添加评审对象/团队/人员当前采用批量输入 ID 或编码的轻量方式，尚未做复杂检索弹窗。
- 同步评审场次当前需要手动输入 `sessionId`，后续可改为场次选择器。
- 团队编号匹配评审对象时，如果多个活动复用了同一个团队编号，可能需要结合评审活动进一步限定。
- 浏览器真实库联调尚未执行。

## 16. 是否建议上线

建议先进入测试环境联调，不建议直接上生产。

上线前建议补充：

- 执行迁移脚本；
- 浏览器验证绑定对象 Tab；
- 使用真实评审活动和现场场次验证同步；
- 验证秘书端“下一位”顺序；
- 验证旧的人员配置和证件生成不受影响。
