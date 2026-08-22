# 教师查看自己指导学生参赛证运行态验证

生成时间：2026-07-07 23:55

## 1. 验证目标

本轮计划验证测试环境中以下服务是否已加载“教师查看自己指导学生参赛证”代码并可用于联调：

- gateway
- auth
- system
- competition
- PC 前端
- 测试库 `jiaoxue_test`
- Nacos / Redis

本轮未连接生产数据库，未启动临时单服务替代正式联调结论。

## 2. 已执行检查

执行已有 UAT 健康检查脚本：

```bash
MYSQL_HOST=10.10.10.10 MYSQL_PASSWORD=*** NACOS_HOST=10.10.10.10 scripts/review/check_review_uat.py
```

说明：口令通过环境变量注入，未写入报告。

检查结果摘要：

| 检查项 | 结果 | 说明 |
| --- | --- | --- |
| gateway `127.0.0.1:9889` | FAIL | Connection refused，无监听 |
| auth `127.0.0.1:9224` | FAIL | Connection refused，无监听 |
| system `127.0.0.1:9211` | FAIL | Connection refused，无监听 |
| competition `127.0.0.1:9205` | FAIL | Connection refused，无监听 |
| PC 前端 `127.0.0.1:8081` | FAIL | Connection refused，无监听 |
| gateway HTTP | FAIL | `/actuator/health` 无法连接 |
| auth HTTP | FAIL | `/actuator/health` 无法连接 |
| system HTTP | FAIL | `/actuator/health` 无法连接 |
| competition HTTP | FAIL | `/actuator/health` 无法连接 |
| PC 前端 HTTP | FAIL | 无法连接 |
| gateway 路由转发 | FAIL | 无法连接 |
| PC 代理转发 | FAIL | 无法连接 |
| MySQL `10.10.10.10:3306` | FAIL | TCP timed out |
| Nacos `10.10.10.10:8848` | FAIL | TCP timed out |
| Redis `127.0.0.1:6379` | PASS | `PING -> PONG` |

机器可读检查报告路径：

- `logs/review-uat/check_report.md`

## 3. 端口扫描结果

对本机与仓库中出现过的测试/部署候选地址做 TCP 探测：

- `127.0.0.1`
- `localhost`
- `10.10.10.10`
- `192.168.1.202`
- `192.168.0.70`

候选端口：

- `3306`
- `8848`
- `9889`
- `9224`
- `9205`
- `9211`
- `8081`
- `80`
- `18089`
- `18099`
- `30017`

结果：未发现开放端口。

## 4. 运行态结论

当前运行态不满足联调前置条件：

1. 本机没有 gateway / auth / system / competition / PC 前端服务运行；
2. 仓库历史 UAT 报告中记录的测试依赖地址 `10.10.10.10` 当前不可达；
3. 无法确认正式测试用 competition 服务是否已加载本次代码；
4. 无法确认 PC 前端是否已部署或加载新构建；
5. 无法获取登录 token；
6. 无法执行教师接口、详情接口、越权接口和页面验证。

因此，本轮不能形成“正式测试库联调通过”的结论。

## 5. 后续需要补齐

需要提供以下任一组可达运行环境：

- 已部署本次代码的测试环境 gateway / PC 访问地址；
- 可用的教师 A、教师 B、普通学生测试账号；
- 可访问测试库的网络/VPN；
- 或允许使用仓库 UAT 脚本启动完整本地测试环境，并确认该本地 UAT 环境可作为本次正式联调依据。

在上述条件满足前，本功能不能进入验收通过状态。
