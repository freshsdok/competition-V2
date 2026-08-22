# 资源预约第三包 UI 开发结果

开发日期：2026-07-06  
范围：管理端资源允许赛场范围配置、slot 允许组别配置、预约记录展示增强、PC 用户端幂等预约与已有预约展示。  
结论：代码开发与构建验证通过，可以进入 P3 测试库联调。

## 1. 审计结果

- 已输出审计文档：`docs/competition-scene-resource/SCENE_RESOURCE_RESERVATION_P3_UI_AUDIT.md`。
- 管理端原有资源与预约 Tab 仅支持资源布置和 slot 维护，缺少 resource schedule scope、slot group scope 和预约记录展示入口。
- 后端已有 P1/P2 scope/group service，但缺少管理端 controller。
- PC 端已有独立设备预约页，但幂等键不是 UUID，缺少提交期间按钮锁定和完整已有预约展示。

## 2. 修改文件清单

后端：

- `CompetitionSceneResourceScheduleScopeController.java`
- `CompetitionSceneResourceSlotGroupScopeController.java`
- `CompetitionSceneResourceReservationController.java`
- `CompetitionSceneResourceSlotGroupScopeReplaceReq.java`
- `CompetitionSceneResourceScheduleScope.java`
- `CompetitionSceneResourceReservationVO.java`
- `CompetitionSceneResourceSlot.java`
- `CompetitionSceneResourceSlotBatchReq.java`
- `CompetitionSceneScheduleTargetMapper.java`
- `CompetitionSceneResourceSlotServiceImpl.java`
- `CompetitionSceneResourceScheduleScopeMapper.xml`
- `CompetitionSceneResourceReservationMapper.xml`
- `CompetitionSceneScheduleTargetMapper.xml`

管理端：

- `old-code-admin/src/api/tournament/sceneResourceScheduleScope.js`
- `old-code-admin/src/api/tournament/sceneResourceSlotGroupScope.js`
- `old-code-admin/src/api/tournament/sceneResourceReservation.js`
- `ResourceReservationTab.vue`
- `ResourceSlotDialog.vue`

PC 用户端：

- `old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`
- `old-code-pc/src/api/personal/index.js`

文档：

- `SCENE_RESOURCE_RESERVATION_P3_UI_AUDIT.md`
- `SCENE_RESOURCE_RESERVATION_SCOPE_GROUP_CAPACITY_P3_UI_DEV_RESULT.md`

## 3. 管理端资源允许赛场配置

- 新增接口：
  - `GET /competition/sceneResourceScheduleScope/list`
  - `POST /competition/sceneResourceScheduleScope/add`
  - `POST /competition/sceneResourceScheduleScope/remove`
  - `POST /competition/sceneResourceScheduleScope/ensure`
- 资源列表新增“预约范围”入口。
- 弹窗支持查看、手工绑定、移除允许预约来源赛场。
- 第一阶段仅写入 `MANUAL_BIND`，`AUTO_LOCATION` 仅保留展示和不可移除口径。
- scope 查询补充 `allowedScheduleName`，列表展示赛场名称、sourceType、enabled、createTime。

## 4. 管理端 slot 组别配置

- 新增接口：
  - `GET /competition/sceneResourceSlotGroupScope/listBySlot`
  - `POST /competition/sceneResourceSlotGroupScope/replace`
  - `POST /competition/sceneResourceSlotGroupScope/batchReplace`
  - `GET /competition/sceneResourceSlotGroupScope/groupOptions`
- 组别选项来自 `competition_scene_schedule_target.second_level_code / second_level_name` distinct。
- slot 列表展示 `allowedGroupNames`；未配置显示“不限组别”。
- 单个 slot 新增/编辑支持多选允许组别；清空组别即不限组别。
- 批量生成 slot 支持多选允许组别，并写入每个新生成 slot。
- 后端复用 `replaceSlotGroups`：逻辑删除旧组别，再插入新组别。

## 5. 管理端预约记录展示

- 新增接口：
  - `GET /competition/sceneResourceReservation/list`
  - `GET /competition/sceneResourceReservation/{reservationId}`
- 资源列表新增“预约记录”入口。
- 展示预约主体、操作人、来源赛场、组别、时段、占用人数、占用设备数、占用工位数、共享占用快照、每台设备工位快照、状态、创建时间。
- 后端 VO 补充 `reservationSourceScheduleName`。
- 幂等键未在普通管理端表格展示。

## 6. 用户端 idempotency_key

- PC 预约提交改为每次点击生成 UUID。
- 请求完成前锁定预约按钮，避免重复点击生成不同 key 并发提交。
- 同一次 pending 请求复用同一个 `idempotencyKey`。
- `ALREADY_RESERVED` 和旧兼容码 `ALREADY_RESERVED_BY_SUBJECT` 均可识别。
- `IDEMPOTENCY_CONFLICT_RETRY_LATER` / `RESERVATION_CONFLICT_RETRY_LATER` 展示刷新确认提示。

## 7. 用户端按钮防抖

- 新增 `reservingSlotId` 和 `reservationIdempotencyKey` 状态。
- 预约按钮在提交期间显示 loading，并禁用其他 slot 的预约按钮。
- 已有预约时禁用继续预约。

## 8. 用户端已有预约展示

- 资源卡片继续显示“已预约/可预约”。
- 资源详情新增已有预约摘要：
  - 本队/本人已由谁预约；
  - 资源名称；
  - 预约时段；
  - 占用人数、设备数、工位数。
- 我的预约列表补充展示：
  - 预约主体；
  - 操作人；
  - 来源赛场；
  - 组别；
  - 占用人数；
  - 占用设备数；
  - 占用工位数。
- slot 列表展示允许组别，未配置显示“不限组别”。

## 9. 用户端取消预约交互

- 取消前保留二次确认。
- 成功后刷新可预约资源、slot 和我的预约。
- 已开始不可取消时展示明确提示并刷新我的预约。
- 重复取消或状态变化时不按系统错误处理，刷新当前状态。

## 10. 测试结果

- 后端编译通过。
- 管理端构建通过。
- PC 构建通过。
- 本轮未执行浏览器人工点测和测试库接口联调；这些应进入 P3 测试库联调阶段验证。

## 11. 构建结果

- 后端：`mvn -pl teaching-modules/teaching-competition -am compile -DskipTests`，`BUILD SUCCESS`。
- 管理端：`npm run build:stage`，构建成功；存在项目既有 eval/sourcemap 警告。
- PC：`npm run build`，构建成功；存在项目既有 Browserslist、Sass legacy API、CSS 拼写警告。

## 12. 已知风险

- 管理端新增接口使用现有资源/时段权限点，若测试账号菜单权限未配置，需要补菜单权限或复用现有权限策略。
- `groupOptions` 当前按 `scheduleId / competitionSeriesId` 从 schedule target distinct 查询，若目标尚未匹配或组别字段为空，前端组别选项会为空。
- 批量生成带组别时，为拿到 slotId 后写入 group scope，后端改为逐条插入 slot；普通不带组别仍保留批量插入。
- 本轮未改小程序页面。
- 本轮未重启正式 9205，也未连接生产数据库。

## 13. 是否可以进入 P3 测试库联调

可以进入 P3 测试库联调。

建议联调重点：

1. 管理端可配置 resource schedule scope，配置后 PC 资源列表即时按后端过滤变化；
2. 管理端 slot 组别配置保存后，PC slot 列表只展示命中组别；
3. PC 预约请求携带 UUID `idempotencyKey`；
4. 快速重复点击不会发起不同 key 的并发预约；
5. `ALREADY_RESERVED` 能展示已有预约；
6. 取消后容量、已有预约和 slot 状态刷新正确。
