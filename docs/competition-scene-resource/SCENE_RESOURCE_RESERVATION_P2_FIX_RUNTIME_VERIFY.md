# 资源预约第二包修复后 Runtime 验证

验证时间：2026-07-06  
环境：正式测试用 gateway `9889`、auth `9224`、competition `9205`  
数据库：测试库 `jiaoxue_test`，仅使用 `P2IT_` 假数据；未连接生产数据库，未记录密码、token、真实手机号或身份证号。

## 1. 9205 重启记录

- 已停止旧 `9205` competition 进程：
  - 旧 Java PID：`65054`
  - 旧 Maven PID：`64488`
  - 旧启动时间：`Mon Jul 6 18:41:01 2026`
- 已按正式测试服务方式重启 `9205`：
  - Maven PID：`96186`
  - Java PID：`96235`
  - Java 启动时间：`Mon Jul 6 18:59:08 2026`
  - 应用：`com.teaching.competition.TeachingCompetitionApplication`
  - Profile：`test`
  - 启动方式：`mvn -pl teaching-modules/teaching-competition -Dspring-boot.run.profiles=test -DskipTests spring-boot:run`

本轮未使用临时 competition 进程替代正式联调结论。

## 2. Class 加载时间核对

修复包相关 class / mapper 编译时间：

| 文件 | 编译时间 |
| --- | --- |
| `UserCompetitionSceneResourceServiceImpl.class` | `2026-07-06 18:51:12 +0800` |
| `CompetitionSceneResourceSlotMapper.class` | `2026-07-06 18:51:11 +0800` |
| `CompetitionSceneResourceSlotMapper.xml` target copy | `2026-07-06 18:50:58 +0800` |

`9205` Java 进程启动时间为 `2026-07-06 18:59:08 +0800`，晚于修复包 class 编译时间，确认正式 `9205` 已加载 P2 fix 代码。

## 3. 健康检查

| 服务 | 地址 | 结果 |
| --- | --- | --- |
| competition | `http://127.0.0.1:9205/actuator/health` | `UP` |
| gateway | `http://127.0.0.1:9889/actuator/health` | `UP` |
| auth | `http://127.0.0.1:9224/actuator/health` | `UP` |

## 4. 结论

运行态验证通过：gateway/auth/competition 均为 `UP`，`9205` 正式测试进程已加载 P2 修复包代码，可以执行修复后联调复测。
