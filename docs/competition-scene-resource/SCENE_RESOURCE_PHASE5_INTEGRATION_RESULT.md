# 大赛现场设备资源管理与预约第五阶段正式联调补测报告

生成时间：2026-07-01

## 1. 是否重启 9205

已重启并完成后台运行态切换。

- 原 9205 运行态未加载本轮 mapper 修复，已停止。
- 单模块 `mvn spring-boot:run` 启动会出现 JWT 签名不一致：`JWT signature does not match locally computed signature`，导致 `SecurityUtils.getLoginUser()` 为空。本轮未绕过 token，改用本地 `common-*`、`api-system`、`competition` 的 `target/classes` 优先的 classpath 启动。
- 当前 9205 通过 `screen` 后台会话运行：`competition-phase5-9205`。
- 当前监听进程：`java` PID `20983`，监听 `*:9205`。
- Nacos 注册日志显示：`teaching-competition 10.10.10.10:9205 register finished`。

## 2. 9205 health

通过网关 `http://127.0.0.1:9889` 使用测试用户 token 验证：

- `GET /competition/userCompetition/sceneResource/bookableList`：`code=200`。
- `GET /competition/userCompetition/sceneResourceReservation/myList`：`code=200`。
- 未出现 `No static resource`。
- 未出现 JWT 解析失败。

## 3. Controller 是否加载

已确认 `UserCompetitionSceneResourceController` 加载成功。

验证接口：

- `GET /competition/userCompetition/sceneResource/bookableList`
- `GET /competition/userCompetition/sceneResourceReservation/myList`

两个接口均通过网关返回业务响应，不再返回静态资源错误。

## 4. 第四阶段重叠校验复验结果

复验对象：`scheduleResourceId=3`，测试日期 `2027-08-02`。

| 场景 | 结果 |
| --- | --- |
| 新增基础时段 `09:00-09:30` | 通过 |
| 新增重叠时段 `09:15-09:45` | 拒绝，提示“预约时段与已有时段重叠” |
| 新增第二个非重叠时段 `10:00-10:30` | 通过 |
| 编辑第二个时段为 `09:15-09:45` | 拒绝，提示“预约时段与已有时段重叠” |
| 批量生成 `08:30-10:00` 覆盖已有时段 | 整体拒绝，提示“预约时段与已有时段重叠” |
| 清理测试时段 | 已清理，复查无遗留 |

第四阶段重叠校验复验通过。

## 5. 测试数据准备情况

测试库：`jiaoxue_test`。本轮未连接生产库。

测试账号：

- `ph5_user_a`，userId=`1591`
- `ph5_user_b`，userId=`1592`
- `ph5_user_c`，userId=`1593`
- `ph5_user_d`，userId=`1594`

个人预约数据：

- 共享赛场：`scheduleId=10`，`scheduleResourceId=10`，`slotId=25`
- 非共享赛场：`scheduleId=11`，`scheduleResourceId=11`，`slotId=26`
- `ph5_user_a`、`ph5_user_b`：有 `schedule_target` 与有效现场证件
- `ph5_user_c`：有 `schedule_target`，无有效现场证件
- `ph5_user_d`：无对应 `schedule_target`

团队预约数据：

- 团队赛场：`scheduleId=12`，`scheduleResourceId=12`，`slotId=27`
- `teamCode=PH5TEAM001`
- `ph5_user_a`、`ph5_user_b` 为团队有效成员
- 团队人数 2，`workstationsPerDevice=1`，后端自动计算预约设备数为 2
- 团队主体存在 `EFFECTIVE` 且未过期现场证件

清理结果：

- PH5 预约记录最终均为 `CANCELLED`，无 `RESERVED` 遗留。
- `slotId=25/26/27` 均恢复为 `OPEN`，`reservedDeviceCount=0`，`remainingDeviceCount=2`。

## 6. 用户端接口联调结果

正式网关联调用例共 27 项，全部通过。

| 场景 | 结果 |
| --- | --- |
| 测试用户登录获取 token | 通过 |
| 可预约资源列表 | 通过 |
| 可预约资源详情 | 通过 |
| 可预约时段列表 | 通过 |
| 个人共享时段预约 | 通过 |
| 重复预约 | 返回 `ALREADY_RESERVED_BY_SUBJECT`，包含 `existingReservation` |
| 我的预约列表 | 通过 |
| 取消预约 | 通过 |
| 取消后容量回补 | 通过 |
| 取消后重新预约 | 通过 |
| 非 schedule_target 用户预约 | 返回 `NOT_SCHEDULE_TARGET` |
| 无有效现场证件用户预约 | 返回 `NO_VALID_CREDENTIAL` |
| 共享占用容量足够时其他主体预约 | 通过 |
| 共享时段容量用满 | `remainingDeviceCount=0`，`slotStatus=FULL` |
| 非共享占用已有预约后其他主体预约 | 返回 `EXCLUSIVE_SLOT_OCCUPIED` |
| 团队预约主体识别 | `subjectType=TEAM`，`subjectCode=PH5TEAM001` |
| 团队预约设备数自动计算 | `reservedDeviceCount=2` |
| 缺少幂等键 | 返回 `IDEMPOTENCY_KEY_REQUIRED` |
| 联调结束取消清理 | 通过 |

## 7. PC 页面联调结果

PC 端资源预约页面与接口封装已接入：

- 页面：`old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue`
- API：`old-code-pc/src/api/personal/sceneResource.js`
- 接口路径与第五阶段用户端接口一致。

验证结果：

- `npm run build` 执行成功。
- 构建过程中仅出现既有 Sass deprecated、包体积、历史 CSS 拼写 warning，未出现资源预约页面编译错误。
- 本轮未进行真实浏览器登录点击验证。

## 8. 小程序页面验证结果

小程序端资源预约页面与接口封装已接入：

- 页面：`old-code-mini/pages/scene-resource/index.vue`
- API：`old-code-mini/api/sceneResource.js`
- 页面注册：`old-code-mini/pages.json` 中存在 `pages/scene-resource/index`

验证结果：

- `pages.json` JSON 解析通过。
- 小程序 API 文件语法检查通过。
- 资源列表、资源详情、时段列表、预约提交、我的预约、取消预约接口路径均与网关联调通过的后端接口一致。
- 当前目录无独立 `package.json`，本轮未执行微信开发者工具/真机模拟器验证。

## 9. 失败项

本轮发现并已处理：

1. 时段容量更新会提前将仍有剩余容量的时段置为 `FULL`。
   - 原因：MySQL 单表 `UPDATE` 中字段赋值按顺序生效，`slot_status` 判断读取到了已扣减后的 `remaining_device_count`，再减一次参数导致提前命中 0。
   - 修复：`slot_status` 改为基于扣减后的 `remaining_device_count` / `remaining_workstation_count` 是否为 0 判断。

2. 单模块 Maven 启动存在 JWT/common-security 依赖错位。
   - 表现：`JWT signature does not match locally computed signature`，用户端接口无法获取登录用户。
   - 处理：正式补测运行态改用本地 `target/classes` 优先 classpath 启动，保持与现有 auth/gateway 运行态一致。
   - 风险仍需保留：后续正式部署需要统一 auth / gateway / competition 的 common-core/common-security 依赖版本与 JWT 密钥实现。

当前接口联调无失败项。

## 10. 修复文件清单

- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceSlotMapper.xml`
  - 修复预约扣减容量后 `slot_status` 过早变为 `FULL` 的 SQL 判断。

本报告：

- `docs/competition-scene-resource/SCENE_RESOURCE_PHASE5_INTEGRATION_RESULT.md`

未改动报名、支付、成绩、证书主流程。

## 11. 是否第五阶段完整验收通过

后端正式网关主流程验收通过。

- Controller 已加载。
- 正式网关用户端接口全量通过。
- 第四阶段重叠时段校验在当前运行态复验通过。
- 容量扣减、容量回补、重复预约、有效证件、赛场匹配对象、非共享占用、团队自动设备数计算均通过。
- PC 构建通过。
- 小程序页面注册与 API 静态验证通过。

如果验收口径要求“PC 浏览器人工点击 + 小程序模拟器/真机点击”，这两项仍需人工补测。

## 12. 是否可以进入第六阶段

可以进入第六阶段前置设计/编码准备。

建议进入第六阶段前先补两项人工确认：

1. PC 端使用真实登录态打开个人中心资源预约页面，确认页面交互与提示文案。
2. 小程序端使用微信开发者工具或真机打开 `pages/scene-resource/index`，确认授权、登录态、页面样式与交互。

