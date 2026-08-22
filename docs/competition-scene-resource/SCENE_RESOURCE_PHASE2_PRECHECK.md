# 大赛现场设备资源管理与预约 - 第二阶段前置检查

检查时间：2026-07-01  
检查环境：本机测试环境，MySQL `jiaoxue_test`

## 一、运行进程

当前监听端口：

- `9889`：gateway，PID `4908`
- `9224`：auth，PID `4914`
- `9205`：competition，PID `31518`

说明：第一阶段报告中提到的旧 `9205` 未加载 Controller 问题，在本次检查时已恢复。当前 `9205` 已可访问资源台账接口。

## 二、网关资源台账验证

使用 `admin / qwe123!@#` 登录 auth 获取 token 后，通过网关访问：

```http
GET http://127.0.0.1:9889/competition/sceneResource/list
Authorization: Bearer <token>
```

返回：

```json
{"total":0,"rows":[],"code":200,"msg":"查询成功","totalSum":null}
```

直连 competition 验证：

```http
GET http://127.0.0.1:9205/competition/sceneResource/list
Authorization: Bearer <token>
```

返回：

```json
{"total":0,"rows":[],"code":200,"msg":"查询成功","totalSum":null}
```

结论：网关和 `9205` 均已可访问资源台账接口，不再出现 `No static resource sceneResource/list`。

## 三、common-core / JwtUtils 检查

运行中的 gateway、auth、competition 均从当前工作区 `old-code` 的 `target/classes` 加载相关工程类。

当前工作区 `JwtUtils` 反编译结果显示：

- `JwtUtils.createToken` 使用静态字段 `JwtUtils.secret`
- `JwtUtils.parseToken` 使用同一静态字段验签
- 静态初始化密钥为 `abcdefghijklmnopqrstuvwxyz`

当前 `TokenConstants.class` 与 `JwtUtils.class` 校验摘要：

```text
JwtUtils.class       b1da18d10fb7a473cc528fae05b3b8fa54bb30b04557945e02f2fcffe0f616df
TokenConstants.class eaeb47e7f6e9d808fb207c632895c772e89a68d9f1d8d9598206f2e1a46d8314
```

结论：当前运行环境中 auth、gateway、competition 的 token 签发和验签链路已能正常互通。

## 四、风险提示

第一阶段联调时曾发现 `.m2` 已安装版本的 `teaching-common-core` 与工作区源码存在差异：`.m2` 版 `JwtUtils` 支持动态读取 `teaching.jwt.secret`，工作区当前编译产物为静态密钥版。

本次检查时运行中的服务均已回到工作区编译产物，网关验证通过。后续仍建议：

1. 保持 auth、gateway、competition 使用同一份 common-core 编译产物；
2. 若执行 `mvn install` 或切换运行方式，重新验证 `/competition/sceneResource/list`；
3. 不在业务代码中绕过 token 验证或权限校验。

## 五、结论

第二阶段可以进入编码。

本次未重启 `9205`，但检查时 `9205` 已是可访问资源台账接口的运行状态。
