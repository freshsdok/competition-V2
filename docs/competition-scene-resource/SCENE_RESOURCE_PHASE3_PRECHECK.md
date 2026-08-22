# 大赛现场设备资源管理与预约 - 第三阶段前置复验

检查日期：2026-07-01  
检查目标：确认资源台账接口、赛场资源布置接口在网关和正式 `9205` 上的可用状态。

## 一、复验接口

使用 `admin / qwe123!@#` 登录 auth 获取 token 后执行复验。

### 1. 资源台账接口

```http
GET http://127.0.0.1:9889/competition/sceneResource/list?pageNum=1&pageSize=5
Authorization: Bearer <token>
```

返回：

```json
{"total":1,"rows":[{"resourceId":5,"resourceCode":"test1","resourceName":"电脑","resourceType":"DEVICE","resourceStatus":"ENABLED"}],"code":200,"msg":"查询成功","totalSum":null}
```

结论：资源台账接口经网关访问正常。

### 2. 赛场资源布置接口

```http
GET http://127.0.0.1:9889/competition/sceneScheduleResource/list?scheduleId=3&pageNum=1&pageSize=5
Authorization: Bearer <token>
```

返回：

```json
{"msg":"No static resource sceneScheduleResource/list.","code":500}
```

直连正式 `9205`：

```http
GET http://127.0.0.1:9205/competition/sceneScheduleResource/list?scheduleId=3&pageNum=1&pageSize=5
Authorization: Bearer <token>
```

返回：

```json
{"msg":"No static resource competition/sceneScheduleResource/list.","code":500}
```

结论：正式测试用 `9205` 尚未加载第二阶段新增的 `CompetitionSceneScheduleResourceController`，网关仍路由到旧 `9205`，第三阶段网关联调暂时阻塞。

## 二、运行状态

当前 `9205` 监听进程：

- PID：`31518`
- 端口：`9205`
- 运行方式：IntelliJ 启动的 `TeachingCompetitionApplication`
- classpath 包含当前工作区 `old-code/teaching-modules/teaching-competition/target/classes`

虽然 classpath 指向工作区，但该进程启动时间早于第二阶段新增 Controller 编译完成，因此运行中的 Spring 容器未注册新 Controller。

## 三、JWT / common-core 风险

继续保留第二阶段风险：

- 工作区 `target/classes` 中 `JwtUtils` 使用静态密钥 `abcdefghijklmnopqrstuvwxyz`；
- 本机 `.m2` 已安装的 `teaching-common-core-3.6.6.jar` 与工作区编译产物存在 `JwtUtils` 差异；
- auth / gateway / competition 必须使用同一份 common-core 和一致 JWT 密钥；
- 不允许在业务代码中绕过 token 验签或权限校验。

## 四、处理结论

- 本轮不重启正式 `9205`，避免影响当前测试服务；
- 继续完成第三阶段管理端前端代码开发；
- 第三阶段真实网关联调需在正式 `9205` 重启或网关路由到已加载新 Controller 的 competition 实例后执行。
