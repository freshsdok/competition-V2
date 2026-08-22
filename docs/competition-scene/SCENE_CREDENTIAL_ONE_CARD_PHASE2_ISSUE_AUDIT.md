# 现场证件一证多权阶段 2 旁路发证开发前审计

审计时间：2026-07-05

本轮审计只围绕“旁路发证”进入编码前确认。结论是：旧发证、旧扫码、PC、小程序、资源预约主流程均保持不接入；阶段 2 通过新增后端服务和测试接口并行验证一证多权模型。

## 1. 赛场 target 结构

对象：`CompetitionSceneScheduleTarget`

主要字段：

1. `target_id`：赛场匹配对象主键。
2. `schedule_id`：来源赛场安排。
3. `competition_series_id`：赛事 ID，可与 schedule 互相校验。
4. `credential_type`：对象证件类型，优先级高于 schedule。
5. `config_dimension`：`PERSON` / `TEAM`，影响主体类型。
6. `target_key` / `target_source`：匹配来源。
7. `team_code` / `team_name` / `member_id` / `user_id` / `user_name`：主体身份字段。
8. `competition_role_name`：角色来源，可映射 `MEMBER`、`CAPTAIN`、`TEACHER`、`EXPERT`、`CHECKIN_STAFF`、`MATERIAL_STAFF` 等。
9. `waiting_group_code` / `waiting_group_name`：候场分组。
10. `match_status` / `status` / `del_flag`：旁路发证需排除无效、停用、删除对象。

## 2. credential 核心字段

对象：`CompetitionSceneCredential`

一证多权核心证件继续复用 `competition_scene_credential`，阶段 2 只生成：

```text
scope_type = COMPETITION
issue_channel = SCHEDULE_MATCH
schedule_id = null
target_id = null
scope_ref_id = competition_series_id
credential_status = EFFECTIVE
del_flag = 0
```

核心证件唯一复用规则固定为：

```text
competition_series_id + subject_type + subject_code + credential_type
```

说明：需求中提到 `credential_status = VALID`，当前代码枚举实际为 `EFFECTIVE`，阶段 2 按现有常量 `CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE` 执行。

## 3. 当前 ensureCompetitionCredentialForTarget 逻辑

文件：

```text
old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialServiceImpl.java
```

调用链：

```text
generateCompetitionSceneCredential(req)
  -> ensureCompetitionCredentialForTarget(schedule, target, competition)
  -> selectExistingCompetitionCredential(...)
  -> buildCompetitionCredential(...)
  -> insertCompetitionSceneCredential(...)
  -> buildCredential(...) 生成旧 SCHEDULE 实体证件
```

旧逻辑特点：

1. 赛场发证先确保一张 `COMPETITION` 大赛级证件。
2. 然后再生成一张 `SCHEDULE` 赛场级实体证件。
3. 自动大赛证复用时存在按 `competition_series_id + user_id` 命中的旧兼容路径。
4. 这是前面双证问题的来源，阶段 2 不修改该旧流程。

## 4. 大赛级直接发证逻辑

当前入口：

```text
CompetitionSceneCredentialServiceImpl.competitionDirectIssue(req)
```

逻辑：

1. 校验 `competitionSeriesId`、`credentialType`、`subjectType`、`subjectCode`。
2. 调用 `selectExistingCompetitionCredential(...)` 判断是否已有有效大赛级证件。
3. 构建 `scope_type=COMPETITION`、`issue_channel=COMPETITION_DIRECT` 的证件。
4. 直接插入 `competition_scene_credential`。

注意：直接发证属于旧主流程，本阶段不改。

## 5. credential_no / token / qrContent 生成逻辑

旧逻辑：

1. 赛场级证件号：

```text
CSyyyyMMdd-scheduleId-sequence
```

2. 大赛级证件号：

```text
CCyyyyMMdd-competitionSeriesId-random8
```

3. `credential_token` 使用去横线 UUID，插入前查询 token 是否已存在。
4. `qr_content` 使用：

```text
csc_ + credential_token
```

阶段 2 旁路核心证件沿用大赛级 `CC` 证件号、UUID token 和 `csc_` 二维码内容生成方式，并保留冲突重试。

## 6. subject_type / subject_code 来源

旧赛场证件：

1. `credential_type=EXPERT` 时 `subject_type=EXPERT`。
2. `config_dimension=TEAM` 时 `subject_type=TEAM`。
3. 其他默认为 `USER`。
4. `subject_code` 依次使用：
   - TEAM：`team_code`
   - USER / EXPERT：`user_id`
   - 无 userId 时兜底 `MEMBER:{member_id}`

阶段 2 旁路发证沿用这一主体解析规则，但核心证件复用必须同时带上 `credential_type`。

## 7. credential_type / role_code 映射

证件类型来源优先级：

1. target.`credential_type`
2. target.`competition_role_name` 推断
3. schedule.`credential_type`

角色推断：

1. `MEMBER` / `CAPTAIN` -> `PARTICIPANT`
2. `TEACHER` -> `TEACHER`
3. `EXPERT` -> `EXPERT`
4. `CHECKIN_STAFF` / `MATERIAL_STAFF` / `STAFF` -> `STAFF`

旧枚举 `COMPETITOR` 在代码中归一为 `PARTICIPANT`。

## 8. competition_series_id 获取方式

阶段 2 旁路发证按如下顺序和校验：

1. 从 schedule 读取 `competition_series_id`。
2. 从 target 读取 `competition_series_id`。
3. 两者同时存在且不一致时拒绝发证。
4. 两者均为空时拒绝发证。
5. 有且仅有一个时使用该值。

## 9. 阶段 2 旁路发证约束

阶段 2 新增逻辑必须满足：

1. 只生成或复用 `COMPETITION` 核心证件。
2. 不生成 `SCHEDULE` 实体 credential。
3. 赛场权限写入 `competition_scene_credential_scope_grant`。
4. 核心证件查询禁止按 `user_id` 复用。
5. 旧发证、旧扫码、PC、小程序、资源预约主流程不接入。
6. 测试接口必须挂管理端权限，避免被小程序或 PC 误调用。

## 10. 审计结论

可以进入阶段 2 旁路发证开发。

本阶段新增严格核心证件查询，不替换旧查询；新增旁路 service 和测试接口，不接旧业务入口。核心证件多实例并发唯一性当前仍依赖 Service 查询和单实例同步，未新增 credential 级唯一键，因此阶段 2 仅允许测试/旁路试运行，不作为生产并发发证入口。
