# 资源预约第三包校验清单

整理时间：2026-07-06  
范围：第三包“管理端配置 + 用户端页面改造”  
基础：P2-FIX2 后端主流程已通过正式测试运行态联调

## 一、开发目标对照

本次开发目标是把 P2 已通过的能力在管理端和 PC 用户端完整呈现：

- 资源允许赛场范围配置；
- slot 允许组别配置；
- 管理端预约记录增强展示；
- PC 端预约提交携带 `idempotencyKey`；
- PC 端预约按钮防抖；
- PC 端展示已有预约；
- PC 端取消预约后刷新状态；
- 不开发第三包以外的能力；
- 不改动 P2 已通过的预约主流程算法。

## 二、本次新增后端接口

### 1. 资源允许赛场范围

Controller：

`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceScheduleScopeController.java`

接口：

- `GET /competition/sceneResourceScheduleScope/list`
- `POST /competition/sceneResourceScheduleScope/add`
- `POST /competition/sceneResourceScheduleScope/remove`
- `POST /competition/sceneResourceScheduleScope/ensure`

用途：

- 查看某个 `schedule_resource` 允许哪些赛场人员预约；
- 手工绑定允许预约来源赛场；
- 移除手工绑定赛场；
- 第一阶段只写入 `MANUAL_BIND`。

### 2. slot 允许组别

Controller：

`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotGroupScopeController.java`

接口：

- `GET /competition/sceneResourceSlotGroupScope/listBySlot`
- `GET /competition/sceneResourceSlotGroupScope/groupOptions`
- `POST /competition/sceneResourceSlotGroupScope/replace`
- `POST /competition/sceneResourceSlotGroupScope/batchReplace`

用途：

- 查询某个 slot 当前允许组别；
- 查询可选组别；
- 替换单个 slot 允许组别；
- 批量替换 slot 允许组别。

组别来源：

- `competition_scene_schedule_target.second_level_code`
- `competition_scene_schedule_target.second_level_name`

### 3. 预约记录查询

Controller：

`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceReservationController.java`

接口：

- `GET /competition/sceneResourceReservation/list`
- `GET /competition/sceneResourceReservation/{reservationId}`

用途：

- 管理端查看资源预约记录；
- 展示 P2 新增的预约主体、操作人、来源赛场、组别、容量快照等字段。

## 三、本次后端数据与展示字段调整

### 1. 资源允许赛场范围展示字段

文件：

`CompetitionSceneResourceScheduleScope.java`

新增字段：

- `allowedScheduleName`

Mapper：

`CompetitionSceneResourceScheduleScopeMapper.xml`

调整：

- 查询 scope 时关联 `competition_scene_schedule`；
- 返回 `allowed_schedule_name`。

### 2. 预约记录来源赛场展示字段

文件：

`CompetitionSceneResourceReservationVO.java`

新增字段：

- `reservationSourceScheduleName`

Mapper：

`CompetitionSceneResourceReservationMapper.xml`

调整：

- 查询预约记录时关联 `reservation_source_schedule_id` 对应的 `competition_scene_schedule`；
- 返回 `reservation_source_schedule_name`。

### 3. slot 允许组别提交字段

文件：

- `CompetitionSceneResourceSlot.java`
- `CompetitionSceneResourceSlotBatchReq.java`

新增字段：

- `allowedGroups`

用途：

- 单个 slot 新增/编辑时提交允许组别；
- 批量生成 slot 时提交允许组别。

### 4. slot service 调整

文件：

`CompetitionSceneResourceSlotServiceImpl.java`

调整：

- slot 管理端列表/详情填充 `allowedGroupNames`；
- 单个 slot 新增后写入 group scope；
- 单个 slot 编辑后执行 replace group scope；
- 批量生成 slot 时，如果传入 `allowedGroups`，逐条插入 slot 后写入 group scope；
- 未传 `allowedGroups` 时保留原批量插入逻辑。

说明：

- 空数组表示“不限组别”；
- `null` 表示不变更；
- replace 逻辑使用已有 service：逻辑删除旧组别，再插入新组别。

## 四、管理端改造

### 1. 新增 API 文件

- `old-code-admin/src/api/tournament/sceneResourceScheduleScope.js`
- `old-code-admin/src/api/tournament/sceneResourceSlotGroupScope.js`
- `old-code-admin/src/api/tournament/sceneResourceReservation.js`

### 2. 资源与预约 Tab

文件：

`old-code-admin/src/views/tournament/sceneSchedule/components/ResourceReservationTab.vue`

新增能力：

- 资源列表操作列新增“预约范围”；
- 资源列表操作列新增“预约记录”；
- “预约范围”弹窗支持：
  - 查看允许预约赛场；
  - 添加手工绑定赛场；
  - 移除手工绑定赛场；
  - 展示 `allowedScheduleName`、`sourceType`、`enabled`、`createTime`；
- “预约记录”弹窗支持展示：
  - 预约主体类型；
  - 预约主体编码；
  - 操作人；
  - 来源赛场；
  - 组别；
  - 预约时段；
  - 占用人数；
  - 占用设备数；
  - 占用工位数；
  - 共享占用快照；
  - 每台设备工位快照；
  - 预约状态；
  - 创建时间。

### 3. slot 配置弹窗

文件：

`old-code-admin/src/views/tournament/sceneSchedule/components/ResourceSlotDialog.vue`

新增能力：

- slot 列表展示允许组别；
- 未配置允许组别时显示“不限组别”；
- 单个 slot 新增/编辑支持多选允许组别；
- 批量生成 slot 支持多选允许组别；
- 清空组别表示不限组别；
- 保存时提交 `allowedGroups`。

## 五、PC 用户端改造

### 1. API 聚合

文件：

`old-code-pc/src/api/personal/index.js`

调整：

- 导出 `sceneResource.js` 中资源预约相关 API。

### 2. 设备预约页面

文件：

`old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`

新增能力：

- 资源卡片展示来源赛场、组别、剩余设备、剩余工位；
- 资源详情展示来源赛场、组别；
- slot 列表展示允许组别；
- 未配置允许组别显示“不限组别”；
- 预约提交生成 UUID 格式 `idempotencyKey`；
- 请求完成前预约按钮 loading 并禁用；
- 同一次 pending 请求复用同一个 `idempotencyKey`；
- 已有预约时禁用继续预约；
- 识别 `ALREADY_RESERVED`；
- 兼容旧错误码 `ALREADY_RESERVED_BY_SUBJECT`；
- 已有预约展示：
  - 本队/本人已由谁预约；
  - 资源名称；
  - 预约时段；
  - 占用人数；
  - 占用设备数；
  - 占用工位数；
- 我的预约列表展示：
  - 预约主体；
  - 操作人；
  - 来源赛场；
  - 组别；
  - 占用人数；
  - 占用设备数；
  - 占用工位数；
- 取消预约前二次确认；
- 取消成功后刷新资源、slot、我的预约；
- 时段已开始不可取消时展示明确提示。

## 六、未改动范围

本次未做以下事项：

- 未接入一证多权；
- 未接入 credential grant；
- 未修改扫码相关代码；
- 未连接生产数据库；
- 未开发小程序页面；
- 未重写 P2 已通过的预约主流程算法；
- 未把赛场时间作为预约时间判断依据；
- 未把 credential grant 作为资源预约判断依据。

## 七、构建与验证结果

### 1. 后端

执行命令：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：

- `BUILD SUCCESS`

### 2. 管理端

执行命令：

```bash
npm run build:stage
```

结果：

- 构建成功；
- 存在项目既有 eval / sourcemap 类警告。

### 3. PC

执行命令：

```bash
npm run build
```

结果：

- 构建成功；
- 存在项目既有 Browserslist、Sass legacy API、CSS 拼写类警告。

## 八、建议校验顺序

### 1. 管理端资源范围

- 打开“赛场安排 - 资源与预约”；
- 点击某个资源的“预约范围”；
- 添加一个允许预约来源赛场；
- 确认列表展示 `allowedScheduleName`；
- 移除该绑定；
- 确认是逻辑移除，不物理删除。

### 2. 管理端 slot 组别

- 点击资源“配置时段”；
- 确认 slot 列表展示允许组别；
- 编辑单个 slot，选择一个或多个组别；
- 保存后重新打开确认组别回显；
- 清空组别后确认显示“不限组别”；
- 批量生成 slot 时选择允许组别，确认新生成 slot 均带组别。

### 3. 管理端预约记录

- 点击资源“预约记录”；
- 确认展示预约主体、操作人、来源赛场、组别、占用人数、设备数、工位数、共享快照；
- 确认普通表格不展示 `idempotencyKey`。

### 4. PC 可预约资源与 slot

- 用允许赛场用户登录；
- 确认只看到后端返回的资源；
- 确认只看到当前组别可预约 slot；
- 确认 slot 显示剩余工位和允许组别；
- 用不命中组别用户登录，确认对应 slot 被过滤。

### 5. PC 预约提交

- 点击预约；
- 抓包确认请求包含 `idempotencyKey`；
- 快速重复点击，确认按钮被锁定，不发多个不同 key 请求；
- 预约成功后展示已有预约；
- 同队成员进入后能看到本队已有预约。

### 6. PC 取消预约

- 点击取消预约；
- 确认有二次确认；
- 成功后刷新我的预约；
- 成功后刷新资源和 slot 容量；
- slot 已开始不可取消时展示明确提示。

## 九、已知风险

- 管理端新增接口复用现有资源/时段权限点；如果测试账号菜单权限未配置，需要补权限或临时使用超管校验。
- 组别选项来自 schedule target distinct；如果赛场 target 尚未匹配或组别字段为空，组别下拉会为空。
- 批量生成 slot 带组别时，为获取每个新 slotId 写 group scope，后端会逐条插入；不带组别仍走原批量插入。
- 本轮只完成代码构建验证，尚未执行 P3 测试库联调。

## 十、结论

第三包代码开发与构建验证通过。

可以进入 P3 测试库联调。
