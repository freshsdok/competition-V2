# 大赛现场设备资源管理与预约 - 第一阶段接口联调验证报告

验证时间：2026-07-01  
验证范围：资源台账 CRUD  
验证服务：临时启动 `teaching-competition`，端口 `19205`  
测试数据库：`jiaoxue_test`

## 一、联调环境说明

本机已有 `9205` 端口运行 `teaching-competition`，但该进程未加载第一阶段新增 Controller：

```http
GET /competition/sceneResource/list
=> {"msg":"No static resource competition/sceneResource/list.","code":500}
```

因此本轮使用当前工作区代码临时启动 `teaching-competition` 到 `19205` 进行联调。

网关 `9889` 当前仍会负载到旧 `9205` 实例，所以接口验证阶段未通过网关验证最终路由，只验证当前代码在微服务侧的 HTTP 接口行为。

补充说明：当前运行中的 auth 服务与本轮临时 competition 服务在 JWT 密钥实现上存在本地依赖差异。为完成微服务侧接口验证，本轮使用 auth 登录后 Redis 中的 `LoginUser`，并用临时服务运行依赖一致的 `JwtUtils` 生成测试 token。未修改业务数据权限表，未新增菜单 SQL。

## 二、验证接口

- `GET /competition/sceneResource/list`
- `GET /competition/sceneResource/{resourceId}`
- `POST /competition/sceneResource`
- `PUT /competition/sceneResource`
- `DELETE /competition/sceneResource/{resourceIds}`
- `POST /competition/sceneResource/changeStatus`

临时直连时实际使用等价映射：

- `/sceneResource/list`
- `/sceneResource/{resourceId}`
- `/sceneResource`
- `/sceneResource/changeStatus`

## 三、测试数据

主测试编号：

- `P1VERIFY-20260701101843`

引用拒删测试编号：

- `P1VERIFY-20260701101843-REF`

测试结束后：

- `competition_scene_resource.resource_id = 1` 已逻辑删除；
- `competition_scene_resource.resource_id = 2` 已逻辑删除；
- 临时插入的 `competition_scene_schedule_resource.schedule_resource_id = 1` 已逻辑删除。

## 四、接口用例结果

| 序号 | 用例 | 结果 | 关键返回 |
| --- | --- | --- | --- |
| 1 | 新增资源成功 | 通过 | `code=200` |
| 2 | `resourceCode` 重复拒绝 | 通过 | `资源编号已存在` |
| 3 | `resourceName` 为空拒绝 | 通过 | `资源名称不能为空` |
| 4 | `resourceType` 非法拒绝 | 通过 | `资源类型不合法` |
| 5 | `resourceStatus` 非法拒绝 | 通过 | `资源状态不合法` |
| 6 | `deviceQuantity <= 0` 拒绝 | 通过 | `设备数量必须大于0` |
| 7 | `workstationCount <= 0` 拒绝 | 通过 | `单台设备工位数必须大于0` |
| 8 | `defaultSlotDurationMinutes <= 0` 拒绝 | 通过 | `默认单场周期必须大于0分钟` |
| 9 | `defaultSharedOccupancy` 为空拒绝 | 通过 | `默认共享占用不能为空` |
| 10 | `needOpsConfirm` 为空拒绝 | 通过 | `是否需要运维确认不能为空` |
| 11 | 资源列表 `rows + total` 正常 | 通过 | `total=1` |
| 12 | 资源详情正常 | 通过 | 返回资源完整字段 |
| 13 | 资源修改正常 | 通过 | `code=200` |
| 14 | 状态变更 `DISABLED` | 通过 | 详情状态为 `DISABLED` |
| 15 | 状态变更 `MAINTENANCE` | 通过 | 详情状态为 `MAINTENANCE` |
| 16 | 状态变更 `ENABLED` | 通过 | 详情状态为 `ENABLED` |
| 17 | 存在赛场布置时资源删除被拒绝 | 通过 | `资源已布置到赛场安排，不能删除` |
| 18 | 资源逻辑删除正常 | 通过 | `code=200` |
| 19 | 删除后列表不可见 | 通过 | 原资源不再返回 |
| 20 | 引用清理后资源可删除 | 通过 | `code=200` |

实际脚本统计：

```text
SUMMARY {"code":"P1VERIFY-20260701101843","resourceId":1,"referenceResourceId":2,"total":22,"failed":[]}
```

## 五、静态检查

检查范围：

- `db/migration/20260701_competition_scene_resource_p1_001.sql`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneResourceMapper.xml`

检查关键词：

- `team_id`
- `asset_no`
- `owner_unit`
- `storage_location`
- `cancel_deadline_minutes`
- `cancelDeadlineMinutes`
- `ops_status`
- `opsConfirm`

结果：无匹配，静态检查通过。

## 六、编译结果

执行：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：`BUILD SUCCESS`

存在项目既有 Maven warning，包括重复依赖声明、部分 deprecated/unchecked 编译提示；未阻塞编译。

## 七、发现的问题与处理

1. 当前 `9205` competition 进程未加载第一阶段新增接口。
   - 处理：本轮使用 `19205` 临时服务完成接口验证。
   - 建议：进入管理端页面联调前，重启正式测试用 competition 服务。

2. 网关 `9889` 当前会路由到旧 `9205` 实例。
   - 处理：本轮未通过网关最终验证资源台账接口。
   - 建议：重启/下线旧实例后，再通过网关验证 `/competition/sceneResource/**`。

3. 本地工作区源码与当前运行依赖中的 JWT 密钥实现存在差异。
   - 处理：本轮使用与临时 competition 运行依赖一致的 token 进行接口验证。
   - 建议：第二阶段前统一 auth、gateway、competition 的 common-core 依赖/源码版本，避免重启后 token 验签不一致。

## 八、验证结论

资源台账 CRUD 和基础校验接口通过第一阶段联调验证。

本轮未发现需要修改资源台账业务代码的问题。
