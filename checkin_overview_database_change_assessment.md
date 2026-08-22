# 签到概览数据库变更评估

## 1. 结论

选择：B. 只需新增索引。

不建议新建重复签到表，不建议修改现有扫码/二维码/证件编号规则，不建议在本阶段执行数据库变更。

## 2. 原因

当前真实库已具备赛场级签到统计所需字段：

- `competition_scene_schedule_target.schedule_id + target_id` 可定义应签到人员。
- `competition_scene_credential.schedule_id + target_id + credential_id` 可关联现场证件。
- `competition_scene_subject_operation_state.scope_type + scope_ref_id + operation_type + operation_status + credential_id` 可作为签到事实源。
- `competition_scene_operation_log.schedule_id + target_id + operation_type + operation_stage + operation_result + result_status` 可做辅助审计。
- 首期业务口径已明确：状态事实源为 `SCHEDULE + REPORT + DONE`；`COMPETITION/REPORT` 全部排除；`REPORT_SIGN` 流水只作审计和最后签到时间辅助。

但现有索引对高频概览筛选不完整，特别是比赛开始时间、签到地点、赛场聚合、最后签到时间等场景。

## 3. 不需要补充签到记录与赛场关联字段的前提

后续实现必须遵守：

- 仅统计赛场级 `scope_type = 'SCHEDULE'`。
- `scope_ref_id` 必须等于 `schedule_id`。
- 通过 `credential_id` 回到 `competition_scene_credential`，再关联 `target_id`。
- 大赛级 `scope_type = 'COMPETITION'` 的 `REPORT` 不参与赛场签到统计。
- 重复、失败、取消和仅 `SCAN` 记录均不计入已签到。

## 4. 最小变更方案

Phase 2A 建议新增索引，示例名称如下，具体 SQL 在 Phase 2F 或 Phase 2A 执行包中提供：

```sql
-- 不在本阶段执行
idx_scene_schedule_overview_time
  on competition_scene_schedule (competition_series_id, del_flag, status, contest_start_time)

idx_scene_schedule_report_location_time
  on competition_scene_schedule (report_location, contest_start_time)

idx_scene_target_overview_role
  on competition_scene_schedule_target (schedule_id, competition_role_name, status, del_flag, match_status)

idx_scene_credential_schedule_target_status
  on competition_scene_credential (schedule_id, target_id, credential_status, del_flag)

idx_scene_log_checkin_overview
  on competition_scene_operation_log (schedule_id, operation_type, operation_stage, operation_result, result_status, operation_time)
```

`competition_scene_subject_operation_state` 已有：

- `idx_scene_subject_operation_lookup`
- `idx_scene_state_current`

这两个索引覆盖本期核心状态查询，暂不建议重复添加。

## 5. 兼容历史数据方案

- 历史 `COMPETITION/REPORT`：首期全部排除，不在概览、详情人数或团队统计中分摊、复用。
- 历史 `competition_scene_credential.report_status = '1'`：不作为人数主事实源；如需使用，只能作为异常排查或数据修复线索。
- 角色历史值：
  - `队员`、`MEMBER`、`选手`、`PLAYER` 归一为队员，并计入首期应签到人数。
  - `队长`、`CAPTAIN`、`LEADER` 首期不统计。
  - `指导教师`、`TEACHER`、`EXPERT`、`STAFF`、`CHECKIN_STAFF` 等排除。
- 当前真实库未发现赛场绑定目标中的队长记录，首期已确定去除队长相关统计，不自动伪造队长。

## 6. 数据回填方案

本结论不要求结构回填。

可选数据质量修复，仅在业务确认后执行：

- 将 `competition_scene_schedule_target.competition_role_name` 规范化为 `MEMBER/...`。
- 将历史 `credential_type = 'PLAYER'` 统一映射到 `PARTICIPANT`。
- 修正异常 `del_flag = '2'` 的含义。

这些属于数据治理，不是本期必要数据库结构变更。

## 7. 回滚方案

若 Phase 2A 仅新增索引，回滚方式为删除对应索引。

回滚影响：

- 不影响业务数据。
- 不影响扫码、证件、二维码规则。
- 只影响查询性能。

## 8. 对现有业务的影响

- 新增索引会增加少量写入成本，但现场签到写入量远小于 30 秒刷新读压力，收益更高。
- 不改变现有签到确认、取消、证件生成和扫码流程。
- 后端新增只读统计接口，不影响现有 `/competition/sceneVerify/*`、`/competition/sceneCredential/*`、`/competition/sceneSchedule/*`。

## 9. 最终判断

- 无需新增签到表。
- 无需新增 `schedule_id` 到操作流水，因为真实库已有。
- 无需新增 `target_id` 到操作流水，因为真实库已有。
- 暂不需要给状态表新增 `schedule_id/target_id`，但统计必须经 `credential_id` 关联证件。
- 需要新增索引。
- 签到口径、事实源、历史大赛级报到排除、`PLAYER/选手` 按队员处理、去除队长统计均已确认。
