# Review Engine UAT 联调环境报告

生成时间：2026-07-07 09:20  
工作目录：`/Users/wwang/Documents/ClaudeCode/deshi_competition_2`

## 1. 识别出的服务清单

| 服务 | 目录 | 端口 | 说明 |
| --- | --- | ---: | --- |
| gateway | `old-code/teaching-gateway` | 9889 | 前端 Vite 代理实际指向 `http://127.0.0.1:9889` |
| auth | `old-code/teaching-auth` | 9224 | 登录鉴权服务 |
| system | `old-code/teaching-modules/teaching-system` | 9211 | 系统菜单、用户、权限服务；注意 9211 不是网关 |
| competition | `old-code/teaching-modules/teaching-competition` | 9205 | 评审模块所在后端服务 |
| old-code-admin | `old-code-admin` | 8081 | 管理端前端 |

提示中暂定 `9211` 为 gateway，但代码实际配置中 gateway 为 `9889`，`9211` 为 system 服务。本次脚本按代码配置执行，并保留环境变量覆盖。

## 2. 启动目录和启动命令

新增脚本目录：

- `scripts/review/start_review_uat.sh`
- `scripts/review/stop_review_uat.sh`
- `scripts/review/check_review_uat.py`
- `scripts/review/review_uat.env.example`
- `scripts/review/README.md`

Java 服务启动方式：

```bash
cd old-code
mvn -pl <module> -am -Dmaven.test.skip=true install
mvn -pl <module> spring-boot:run -Dmaven.test.skip=true \
  -Dspring-boot.run.profiles=test \
  -Dspring-boot.run.arguments="--server.port=<port> --spring.cloud.nacos.discovery.server-addr=<host>:8848 --spring.cloud.nacos.config.server-addr=<host>:8848"
```

前端启动方式：

```bash
cd old-code-admin
npm run dev -- --host 0.0.0.0 --port 8081
```

一键启动命令：

```bash
MYSQL_PASSWORD=dev_mysql_2026 MYSQL_HOST=10.10.10.10 NACOS_HOST=10.10.10.10 scripts/review/start_review_uat.sh
```

一键检查命令：

```bash
MYSQL_PASSWORD=dev_mysql_2026 MYSQL_HOST=10.10.10.10 NACOS_HOST=10.10.10.10 scripts/review/check_review_uat.py
```

一键停止命令：

```bash
scripts/review/stop_review_uat.sh
```

## 3. 健康检查地址

| 检查项 | 地址 |
| --- | --- |
| gateway | `http://127.0.0.1:9889/actuator/health` |
| auth | `http://127.0.0.1:9224/actuator/health` |
| system | `http://127.0.0.1:9211/actuator/health` |
| competition | `http://127.0.0.1:9205/actuator/health` |
| admin | `http://127.0.0.1:8081/` |
| gateway review route | `http://127.0.0.1:9889/competition/review/activity/list?pageNum=1&pageSize=1` |
| admin proxy route | `http://127.0.0.1:8081/dev-api/competition/review/activity/list?pageNum=1&pageSize=1` |
| Nacos | `http://10.10.10.10:8848/nacos/v1/console/health/readiness` |
| Redis | `127.0.0.1:6379 PING` |
| MySQL | `10.10.10.10:3306 / jiaoxue_test / dev` |

## 4. 本次启动结果

已执行一键启动脚本。首次发现 Maven 启动命令会把 `spring-boot:run` 打到父 POM，导致 `Unable to find a suitable main class`；已修复为“先预构建依赖，再只对目标模块执行 spring-boot:run”。

第二次启动后服务全部监听：

| 服务 | PID | 结果 |
| --- | ---: | --- |
| gateway | 28655 | PASS |
| auth | 31472 | PASS |
| system | 31645 | PASS |
| competition | 29513 | PASS |
| admin | 26405 | PASS |

pid 文件已同步到 `logs/review-uat/*.pid`，停止脚本只会停止这些 pid 文件中的进程。

## 5. 本次健康检查结果

最终复跑结果：

- gateway port：PASS
- auth port：PASS
- system port：PASS
- competition port：PASS
- admin port：PASS
- gateway HTTP：PASS
- auth HTTP：PASS
- system HTTP：PASS
- competition HTTP：PASS
- admin HTTP：PASS
- gateway review forwarding：PASS，返回 `{"code":401,"msg":"令牌不能为空"}`，说明路由可达但未携带登录令牌
- admin dev proxy to gateway：PASS，返回 `{"code":401,"msg":"令牌不能为空"}`
- mysql database `jiaoxue_test`：PASS
- nacos：PASS
- redis：PASS

完整机器可读报告：`logs/review-uat/check_report.md`

## 6. 日志路径

| 服务 | 日志 |
| --- | --- |
| gateway | `logs/review-uat/gateway.log` |
| auth | `logs/review-uat/auth.log` |
| system | `logs/review-uat/system.log` |
| competition | `logs/review-uat/competition.log` |
| admin | `logs/review-uat/admin.log` |

## 7. 发现的问题

1. `9211` 在代码中不是 gateway，而是 system 服务；gateway 实际为 `9889`。
2. 当前机器默认 `127.0.0.1:3306` 和 `127.0.0.1:8848` 不监听；可用依赖地址为 `10.10.10.10:3306` 和 `10.10.10.10:8848`。
3. MySQL 使用 `caching_sha2_password`，系统 Python 又没有 `PyMySQL`，健康检查初版只能做到 TCP 检查。
4. Maven 预构建使用 `-DskipTests` 时仍会编译测试源码，competition 测试源码中存在旧类引用，会影响联调启动。

## 8. 已修复的问题

1. 新增一键启动、一键停止、一键健康检查、env 示例和 README。
2. 启动脚本不再按端口粗暴 kill；端口被占用时输出 PID 和命令。
3. 启动脚本支持 `GATEWAY_PORT`、`COMPETITION_PORT`、`ADMIN_PORT` 等环境变量覆盖。
4. 启动脚本支持 `NACOS_HOST` 本地运行时覆盖，不修改生产配置。
5. 启动脚本修复 Maven 父 POM 启动问题。
6. 启动脚本预构建改为 `-Dmaven.test.skip=true`，避免测试源码编译阻断 UAT 启动。
7. 健康检查脚本补充了无第三方依赖的 MySQL `caching_sha2_password` 认证能力。
8. pid 文件已刷新为实际监听进程，停止脚本可按 pid 文件收尾。

## 9. 后续使用说明

建议创建本地未提交文件：

```bash
cp scripts/review/review_uat.env.example scripts/review/review_uat.env
```

本机联调推荐配置：

```bash
MYSQL_HOST=10.10.10.10
MYSQL_PASSWORD=dev_mysql_2026
NACOS_HOST=10.10.10.10
REDIS_HOST=127.0.0.1
```

以后总联调前按顺序执行：

```bash
scripts/review/start_review_uat.sh
scripts/review/check_review_uat.py
```

联调结束执行：

```bash
scripts/review/stop_review_uat.sh
```

本包未提供 PowerShell 版本；当前开发机为 macOS，已优先交付 shell 版本。Windows 环境如需联调，可按 README 中服务清单和命令补一份 ps1 包装脚本。
