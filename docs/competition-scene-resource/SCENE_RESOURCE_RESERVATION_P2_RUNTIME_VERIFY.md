# 资源预约第二包 9205 Runtime 验证

验证时间：2026-07-06 18:38:19 +0800  
环境：本地正式测试链路，gateway `9889`，auth `9224`，competition `9205`，Spring profile `test`。

## 1. 重启动作

- 重启前 `9205` 监听进程：`java` PID `32053`。
- 目标第二包 class：
  - `old-code/teaching-modules/teaching-competition/target/classes/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.class`
  - 编译时间：`2026-07-06 18:18:38 +0800`
- 已向旧 `9205` 进程发送 `TERM`，端口正常释放。
- 使用同一 competition Spring Boot 应用、`test` profile、`9205` 端口重新启动：
  - 应用：`com.teaching.competition.TeachingCompetitionApplication`
  - profile：`test`
  - Nacos service：`teaching-competition`

## 2. 新进程验证

- 接口联调执行进程：`34767`，启动时间 `Mon Jul 6 18:21:33 2026`
- 收尾后转为后台 launchd 进程：`65054`，启动时间 `Mon Jul 6 18:41:01 2026`
- 新进程启动时间晚于第二包 class 编译时间 `2026-07-06 18:18:38 +0800`。
- 启动日志确认：
  - `The following 1 profile is active: "test"`
  - `Tomcat` 监听 `http-nio-9205`
  - Nacos 注册 `teaching-competition 10.10.10.10:9205`
  - `Started TeachingCompetitionApplication`

## 3. Health 验证

| 服务 | 端口 | `/actuator/health` |
| --- | --- | --- |
| gateway | `9889` | `UP` |
| auth | `9224` | `UP` |
| competition | `9205` | `UP` |

## 4. 结论

`9205` 已重启并加载第二包 class，gateway/auth/competition 均为 `UP`。本轮接口联调基于该正式测试用 `9205` 服务执行，未使用临时进程替代联调结论；收尾时已将 `9205` 以同一应用、同一 `test` profile 转入后台运行。

本报告不记录数据库密码、登录 token 或真实个人敏感信息。
