# 现场安排、匹配对象、现场证件编号调整结果

更新时间：2026-07-01

## 1. 修改背景

本轮按新的现场运行业务口径调整：

- 赛场安排不再由用户维护证件类型、配置维度、候场组编码。
- 证件类型下沉到匹配对象。
- 匹配对象角色改为固定枚举。
- 手工新增匹配对象时，候场组信息只从赛场安排继承。
- 现场证件编号改为短编号，不再追加随机尾码。

本轮未修改资源管理、资源部署、资源时段、资源预约、小程序预约、报名、支付、成绩、证书主流程。

## 2. 已修改文件清单

- `docs/competition-scene/SCENE_SCHEDULE_CREDENTIAL_RULE_AUDIT.md`
- `docs/competition-scene/SCENE_SCHEDULE_TARGET_CREDENTIAL_ADJUST_RESULT.md`
- `db/migration/20260701_competition_scene_target_credential_type.sql`
- `old-code-admin/src/views/tournament/sceneSchedule/index.vue`
- `old-code-pc/src/views/personal/personaltabs/Competition.vue`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/contant/CompetitionSceneConstants.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneScheduleServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneScheduleTargetMapper.xml`

工作区已有但本轮未触碰的脏文件仍保留原状：`old-code-mini/config.js`、`CompetitionSceneResourceSlotMapper.xml`、`docs/competition-scene-resource/*`。

## 3. 前端表单调整

管理端现场安排页：

- 赛场安排查询区移除 `credentialType` / `configDimension`。
- 当前安排提示和赛场安排列表移除证件类型、配置维度展示。
- 新增/编辑赛场安排表单移除证件类型、配置维度、候场组编码。
- 赛场安排保存改为白名单 payload，不再提交隐藏字段，避免空值覆盖旧数据。
- 候场组名称保留。

匹配对象页：

- 新增证件类型下拉：`PARTICIPANT`、`TEACHER`、`EXPERT`、`STAFF`。
- 角色改为固定下拉：`TEACHER`、`MEMBER`、`EXPERT`、`CAPTAIN`、`MATERIAL_STAFF`、`CHECKIN_STAFF`。
- 匹配对象列表新增证件类型列，角色列显示中文。
- 查询筛选新增证件类型和角色下拉。
- 手工新增/编辑对象时，候场组编码和候场组名称只读展示，提交 payload 不包含这两个字段。

PC 我的赛事/参赛证：

- 仅补充 `PARTICIPANT` / `STAFF` 和角色枚举的中文展示映射，避免新证件数据显示英文枚举值。

## 4. 后端接口调整

`CompetitionSceneScheduleServiceImpl`：

- 新增赛场安排时，旧表结构必填字段 `credentialType` / `configDimension` 由后端给兼容默认值，不再要求前端传入。
- 自动匹配对象时，不再按 schedule 证件类型筛人；匹配到报名对象后按角色推断 target 证件类型。
- 新增/批量新增 target 时，强制以后端 schedule 的候场组编码/名称为准。
- 编辑 target 时保留历史候场组编码/名称，不允许前端覆盖。
- 保存 target 前校验证件类型和角色枚举，旧中文角色会映射到新枚举。

`CompetitionSceneCredentialServiceImpl`：

- 生成证件时优先读取 `target.credentialType`。
- `COMPETITOR` 兼容映射为 `PARTICIPANT`。
- target 缺少证件类型时按角色推断，无法推断再回退旧 schedule 字段，仍无法确定则报错。
- 二维码 token / qrContent 生成逻辑保持不变。

## 5. 数据库 migration

新增人工审查脚本：

- `db/migration/20260701_competition_scene_target_credential_type.sql`

审计结论：

- `competition_scene_schedule_target.credential_type` 已存在，不重复添加字段。
- `competition_scene_credential.uk_scene_credential_no` 已存在，不重复添加唯一键。
- migration 只更新字段注释并给出可选的旧 `COMPETITOR` 数据迁移 SQL 注释，不自动改历史数据。
- 本轮未连接生产数据库。

## 6. 证件编号规则

新编号格式：

```text
CS + yyyyMMdd + '-' + scheduleId + '-' + sequence
```

示例：

```text
CS20260630-3-20
```

日期优先级：

- 赛场安排比赛开始时间 `contestStartTime`
- 报道开始时间 `reportStartTime`
- 生成时间

sequence 规则：

- 按同一 `scheduleId` 下已有未删除证件数量递增。
- 从 1 开始。
- 批量生成时连续递增。
- 生成前按 `credential_no` 查重。
- 遇到唯一键并发冲突时重新计算并最多重试 3 次。
- 不再追加随机码。

二维码核验仍使用 `credentialToken` / `qrContent`，`credentialNo` 只用于展示和现场人工沟通。

## 7. 字典/常量

新增/整理常量：

- 证件类型：`PARTICIPANT`、`TEACHER`、`EXPERT`、`STAFF`
- 历史兼容：`COMPETITOR`
- 角色：`TEACHER`、`MEMBER`、`EXPERT`、`CAPTAIN`、`MATERIAL_STAFF`、`CHECKIN_STAFF`

系统字典 SQL 建议：

```sql
-- scene_credential_type: PARTICIPANT/参赛证, TEACHER/教师证, EXPERT/专家证, STAFF/工作人员证
-- scene_target_role: TEACHER/教师, MEMBER/队员, EXPERT/专家, CAPTAIN/队长,
--                    MATERIAL_STAFF/发资料工作人员, CHECKIN_STAFF/签到工作人员
```

本轮未强行写入系统字典表。

## 8. 兼容旧数据说明

- 不删除 `competition_scene_schedule` 上的旧字段。
- 旧 `COMPETITOR` 值由代码兼容为 `PARTICIPANT`。
- 旧中文角色如 `指导教师`、`队员`、`队长`、`专家` 可映射为新枚举。
- 无法识别的旧角色列表仍显示原值；编辑时需要重新选择固定角色。
- 历史 target 的候场组信息在编辑时保留，不允许前端改写。
- 老证件仍可按原查询逻辑查询，二维码 token 核验链路不变。

## 9. 测试结果

已执行：

- `mvn -pl teaching-modules/teaching-competition -am -DskipTests compile`
- `npm run build:prod`（`old-code-admin`）
- `npm run build`（`old-code-pc`）

覆盖到的需求点：

- 后端编译验证新增常量、mapper、service 逻辑可编译。
- 管理端构建验证赛场安排/匹配对象页面语法可打包。
- PC 构建验证参赛证展示映射调整可打包。

未执行：

- 未连接数据库执行 migration。
- 未做真实接口联调和并发压测。

## 10. 构建结果

- 后端 competition 模块：构建成功。
- 管理端：构建成功。
- PC 端：构建成功。

构建 warning：

- Maven 原有 duplicate dependency / Lombok / deprecated / unchecked warning。
- 管理端原有 Vite eval、sourcemap、chunk size 等 warning。
- PC 端原有 browserslist、Sass deprecation、CSS 拼写、eval、chunk size warning。

以上 warning 非本轮业务改动新增阻塞。

## 11. 已知风险

- 新证件编号按已有数量递增，若历史数据中存在删除后空洞，不回填旧序号。
- 并发冲突已在 service 层重试，但更高强度并发仍建议依赖数据库唯一键兜底并做接口压测。
- 自动匹配现在按报名对象角色推断证件类型；个别历史报名角色无法识别时会默认按队员/参赛证处理。
- migration 仅供人工审查执行，生产执行前需确认字段注释变更和可选旧值迁移策略。
