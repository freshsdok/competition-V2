# 大赛现场设备资源管理与预约 - 第四阶段正式网关联调补测结果

测试日期：2026-07-01  
范围：正式 `9205` competition 进程重启后，通过网关复验 `sceneResourceSlot` 接口，并在管理端页面复验“赛场安排 - 资源与预约 - 配置时段”。

## 一、Controller 是否加载

结论：已加载。

验证方式：

- 使用 `admin / qwe123!@#` 登录 `auth` 获取 token；
- 通过网关 `http://127.0.0.1:9889` 请求：

```text
GET /competition/sceneResourceSlot/list?scheduleResourceId=3&pageNum=1&pageSize=5
```

结果：

```json
{"total":0,"rows":[],"code":200,"msg":"查询成功"}
```

说明：

- 不再出现 `No static resource sceneResourceSlot/list`；
- `CompetitionSceneResourceSlotController` 已被当前正式 `9205` 运行态加载；
- 网关到 competition 的路由正常。

## 二、网关接口复验结果

测试均使用 admin token，通过网关访问，未绕过 token 和权限。

| 接口 | 结果 | 说明 |
| --- | --- | --- |
| `GET /competition/sceneResourceSlot/list?scheduleResourceId=3&pageNum=1&pageSize=5` | 通过 | 返回 `TableDataInfo`，`rows + total` 正常 |
| `POST /competition/sceneResourceSlot` | 通过 | 单个新增时段成功 |
| `GET /competition/sceneResourceSlot/{slotId}` | 通过 | 详情返回正常，包含资源名称、部署位置、容量字段 |
| `POST /competition/sceneResourceSlot/batch` | 通过 | 批量生成成功，返回新增数量 `2` |
| `PUT /competition/sceneResourceSlot` | 通过 | 编辑时段成功，版本号递增 |
| `POST /competition/sceneResourceSlot/changeStatus` | 通过 | `OPEN / CLOSED` 切换成功 |
| `DELETE /competition/sceneResourceSlot/{slotIds}` | 通过 | 删除成功，最终列表恢复为空 |

容量校验：

```text
POST /competition/sceneResourceSlot
deviceCapacity = 99
```

返回：

```json
{"msg":"设备容量不能大于部署设备数","code":500}
```

鉴权校验：

- 不带 token 访问列表接口，返回：

```json
{"code":401,"msg":"令牌不能为空"}
```

说明：

- token 鉴权正常；
- admin token 权限访问正常；
- 注册普通测试用户被系统关闭，常见非管理员测试账号无法登录，因此本轮未能拿到有效“无权限用户 token”复验 403。

## 三、重叠时段问题与修复

本轮正式网关补测发现一个真实问题：

- 同一 `scheduleResourceId` 下，已有 `2027-03-01 09:00:00 ~ 09:30:00` 时段；
- 再新增 `2027-03-01 09:15:00 ~ 09:45:00` 时，当前正式 `9205` 运行态仍返回成功；
- 这不符合“重叠时段错误提示正常”的验收口径。

已修复代码：

- `CompetitionSceneResourceSlotMapper.java`
- `CompetitionSceneResourceSlotMapper.xml`
- `CompetitionSceneResourceSlotServiceImpl.java`

修复内容：

- 新增 `countOverlappingSlots` 查询；
- 新增 / 编辑 / 批量生成时统一校验：

```text
same schedule_resource_id
existing.start_time < new.end_time
existing.end_time > new.start_time
deleted = 0
```

- 编辑时排除当前 `slotId`；
- 发现重叠时抛出：

```text
预约时段与已有时段重叠
```

编译结果：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：`BUILD SUCCESS`。

注意：

- 当前正式 `9205` 是 IntelliJ 已运行进程；
- 本轮修复发生在正式进程启动之后；
- 该进程不会自动热加载已加载过的 Service / Mapper；
- 因此需要再次重启正式 `9205` 后，补测重叠时段错误提示。

## 四、管理端页面联调结果

访问地址：

```text
http://127.0.0.1:8081/tournament/sceneSchedule
```

登录方式：

- 页面 UI 登录；
- 账号：`admin`；
- 密码：`qwe123!@#`。

复验结果：

| 页面项 | 结果 | 说明 |
| --- | --- | --- |
| 赛场安排页面正常 | 通过 | 页面可打开，测试安排已选中 |
| 资源与预约 Tab 正常 | 通过 | Tab 可切换，资源布置列表可加载 |
| 配置时段按钮可点击 | 通过 | “电脑”布置行可打开配置时段 |
| 时段弹窗正常打开 | 通过 | 标题为“电脑 - 配置时段” |
| 当前资源布置信息展示正确 | 通过 | 展示资源名称、部署位置、部署设备数、每台工位数、总工位数、默认周期、预约状态、共享占用 |
| 时段列表正常加载 | 通过 | 初始为空，新增后正常刷新 |
| 单个新增时段成功 | 通过 | 新增 `2027-02-01 09:00:00 ~ 09:30:00` 成功 |
| 批量生成时段成功 | 通过 | 生成 `13:00 ~ 13:30`、`13:30 ~ 14:00` 两个时段 |
| 设备容量校验生效 | 通过 | 网关接口校验 `deviceCapacity > deployedDeviceCount` 被拒绝；页面输入也受部署设备数上限约束 |
| 工位容量预览正确 | 通过 | 当前每台工位数为 `1`，设备容量 `1` 时预览工位容量为 `1` |
| 编辑时段成功 | 通过 | 修改为 `2027-02-01 10:00:00 ~ 10:30:00` 成功 |
| 开放状态切换成功 | 通过 | 状态切换为“开放”，开放按钮禁用 |
| 关闭状态切换成功 | 通过 | 状态切换为“关闭”，关闭按钮禁用 |
| 删除时段成功 | 通过 | 页面删除一条时段成功 |
| 重叠时段错误提示 | 未通过当前运行态 | 已修复代码，需重启 `9205` 后补测 |
| 页面 console error | 通过 | `tab.dev.logs({levels:['error']})` 返回空数组 |

测试数据清理：

- 页面删除 1 条；
- 通过网关删除剩余 2 条批量生成时段；
- 最终 `scheduleResourceId=3` 的时段列表 `total=0`。

## 五、权限码验证结果

已验证：

- admin token 可访问全部本阶段接口；
- 不带 token 返回 `401 / 令牌不能为空`；
- 前端按钮按以下权限码控制：
  - `competition:sceneResourceSlot:list`
  - `competition:sceneResourceSlot:add`
  - `competition:sceneResourceSlot:edit`
  - `competition:sceneResourceSlot:remove`
  - `competition:sceneResourceSlot:changeStatus`

未完成：

- 权限不足 `403` 未完成真实用户验证。

原因：

- 系统注册普通用户返回“当前系统没有开启注册功能！”；
- 尝试常见测试账号 `ry / user / test / student / expert / teacher` 均无法登录；
- 本轮未伪造 token，未绕过权限。

建议：

- 后续由人工提供一个“可登录但不具备 `competition:sceneResourceSlot:*` 权限”的测试账号；
- 或在测试环境通过菜单 / 角色体系创建最小权限账号后补测 403。

## 六、发现的问题

1. 重叠时段未拦截

   - 严重级别：中；
   - 影响：同一布置资源下可能出现时间段重叠，影响后续预约时段展示和容量口径；
   - 状态：代码已修复，编译通过；
   - 待办：重启正式 `9205` 后复验。

2. 缺少无权限测试账号

   - 严重级别：低；
   - 影响：无法完成权限不足 `403` 的真实账号复验；
   - 状态：环境阻塞；
   - 待办：准备非管理员低权限账号。

## 七、修复文件清单

本轮修复：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneResourceSlotMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotServiceImpl.java`

本轮未修改：

- 小程序；
- PC 用户端；
- 用户预约接口；
- 预约提交；
- 预约取消；
- 报名、支付、成绩、证书主流程。

禁用字段检查：

- 对本阶段新增 / 修改文件扫描，未命中：
  - `team_id`
  - `asset_no`
  - `owner_unit`
  - `storage_location`
  - `cancel_deadline_minutes`
  - `cancelDeadlineMinutes`
  - `ops_status`
  - `opsStatus`
  - `opsConfirm`
  - `site_`

## 八、是否第四阶段验收通过

结论：不建议标记为完全通过。

原因：

- 正式网关主链路、页面主链路、鉴权、容量校验、CRUD、批量生成、状态切换均已通过；
- 但“重叠时段错误提示”在当前正式 `9205` 运行态仍未通过；
- 该问题已修复并编译成功，但需要正式 `9205` 再重启一次后复验。

建议验收口径：

- 第四阶段主体功能：通过；
- 第四阶段完整验收：待 `9205` 重启并通过重叠时段补测后通过。

## 九、是否可以进入第五阶段

建议：暂缓进入第五阶段。

进入第五阶段前建议先完成：

1. 重启正式 `9205` competition；
2. 通过网关复测重叠新增 / 重叠编辑 / 重叠批量生成；
3. 准备低权限账号，补测权限不足 `403`；
4. 确认 `scheduleResourceId=3` 或新的测试布置资源仍可正常完成新增、批量、编辑、状态、删除。
