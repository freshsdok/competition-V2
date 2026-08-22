# 资源预约第二包 P2-FIX2 Runtime 验证

验证时间：2026-07-06  
环境：正式测试用 gateway `9889`、auth `9224`、competition `9205`  
数据库：测试库 `jiaoxue_test`，仅使用 `P2IT_` 假数据；未连接生产数据库，未记录密码、token、真实手机号或身份证号。

## 1. 9205 重启记录

- 曾发现 `9205` 被 IntelliJ 进程启动，虽然已加载 P2-FIX2 class，但不作为正式联调结论来源。
- 已停止该 IntelliJ 启动的 `9205` Java 进程。
- 已按正式测试服务命令重启 `9205`：
  - screen 会话：`p2fix2-9205`
  - Maven PID：`44879`
  - Java PID：`45000`
  - Java 启动时间：`Mon Jul 6 19:51:40 2026`
  - 应用：`com.teaching.competition.TeachingCompetitionApplication`
  - Profile：`test`
  - 启动命令：`mvn -pl teaching-modules/teaching-competition -Dspring-boot.run.profiles=test -DskipTests spring-boot:run`

本轮接口复测基于最终 screen 后台正式 `9205` 进程执行，未使用临时 competition 进程替代联调结论。

## 2. Class 加载时间核对

P2-FIX2 相关 class 编译时间：

| 文件 | 编译时间 |
| --- | --- |
| `UserCompetitionSceneResourceServiceImpl.class` | `2026-07-06 19:23:55 +0800` |
| `CompetitionSceneResourceConstants.class` | `2026-07-06 19:23:55 +0800` |
| `CompetitionSceneResourceReservationMapper.class` | `2026-07-06 19:23:20 +0800` |

`9205` Java 进程启动时间为 `2026-07-06 19:51:40 +0800`，晚于 P2-FIX2 class 编译时间，确认正式 `9205` 已加载 P2-FIX2 代码。

## 3. 健康检查

| 服务 | 地址 | 结果 |
| --- | --- | --- |
| competition | `http://127.0.0.1:9205/actuator/health` | `UP` |
| gateway | `http://127.0.0.1:9889/actuator/health` | `UP` |
| auth | `http://127.0.0.1:9224/actuator/health` | `UP` |

## 4. 结论

运行态验证通过：gateway/auth/competition 均为 `UP`，正式 `9205` 已加载 P2-FIX2 class，可以作为本轮联调复测结论来源。
