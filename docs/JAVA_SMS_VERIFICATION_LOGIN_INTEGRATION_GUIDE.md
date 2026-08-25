# Java 项目短信验证码登录接入指南

## 1. 文档用途

本文档用于指导另一个 Java/Spring Boot 项目的 AI 开发者复用当前系统的阿里云短信能力，完成“获取短信验证码 → Redis 暂存 → 校验验证码 → 签发登录 Token”的完整链路。

本文档基于当前项目的实际实现整理，但不是要求照抄原代码。当前实现中存在明文记录验证码、弱随机数、缺少发送频控等风险；新项目必须采用本文档给出的加固方案。

## 2. 当前系统的事实基线

当前系统使用：

- 阿里云短信 Java SDK：`com.aliyun:dysmsapi20170525:4.2.0`
- 配置前缀：`sms.ali`
- 验证码长度：6 位数字
- 短信模板变量：`code`
- Redis 键：`verificationCode_手机号`
- 有效期：5 分钟
- 获取登录验证码：`POST /system/auth/pc/captcha`
- 验证码登录：`POST /system/auth/pc/userInfoLogin`
- 登录成功后删除验证码并签发 Token

当前代码位置：

- 客户端配置：`teaching-common/teaching-common-core/.../sms/SmsConfigInfo.java`
- 短信发送：`teaching-common/teaching-common-core/.../sms/SmsUtil.java`
- 发送、Redis 存储和登录校验：`teaching-system/.../controller/UserAuthController.java`

## 3. 新项目应实现的目标架构

```text
客户端
  ├─ POST /api/auth/sms/code   手机号 + 场景
  │       ↓
  │   参数校验、账号检查、频控
  │       ↓
  │   生成安全随机验证码
  │       ↓
  │   调用阿里云短信
  │       ↓ 仅发送成功后
  │   Redis 保存验证码摘要、有效期和错误次数
  │
  └─ POST /api/auth/sms/login  手机号 + 验证码
          ↓
      原子校验并消费验证码
          ↓
      查询有效用户、签发 Token
```

必须将“短信发送能力”和“登录业务”分层：

- `SmsProperties`：配置绑定；
- `AliyunSmsClientConfig`：创建阿里云客户端；
- `SmsSender`：只负责发送短信，不操作 Redis、不签发 Token；
- `SmsVerificationService`：生成、缓存、频控和校验验证码；
- `SmsLoginService`：用户校验与 Token 签发；
- `AuthController`：HTTP 参数和响应协议。

## 4. Maven 依赖

为兼容当前系统，可先使用同一版本：

```xml
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dysmsapi20170525</artifactId>
    <version>4.2.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

如果新项目已有统一依赖管理，应由项目 BOM 管理版本；升级 SDK 时需要重新执行集成测试，不能仅替换版本号后直接发布。

## 5. 配置规范

不要把真实 AccessKey 写进 Git。Nacos、环境变量或密钥管理系统中配置：

```yaml
sms:
  ali:
    access-key-id: ${SMS_ALI_ACCESS_KEY_ID}
    access-key-secret: ${SMS_ALI_ACCESS_KEY_SECRET}
    endpoint: ${SMS_ALI_ENDPOINT:dysmsapi.aliyuncs.com}
    sign-name: ${SMS_ALI_SIGN_NAME}
    template-code: ${SMS_ALI_TEMPLATE_CODE}
  verification:
    ttl: 5m
    resend-interval: 60s
    max-verify-attempts: 5
    daily-phone-limit: 10
```

配置要求：

- `sign-name` 必须是阿里云审核通过的短信签名；
- `template-code` 必须是审核通过的验证码模板；
- 模板必须包含变量 `${code}`，SDK 发送时参数 JSON 为 `{"code":"123456"}`；
- RAM 身份只授予发送短信所需的最小权限；
- 开发、测试和生产使用独立 Namespace/配置，禁止复制生产密钥到开发机；
- 日志、Actuator `/env`、配置导出和异常信息不得暴露 Secret。

建议使用类型安全配置：

```java
@ConfigurationProperties(prefix = "sms.ali")
@Validated
public record AliyunSmsProperties(
        @NotBlank String accessKeyId,
        @NotBlank String accessKeySecret,
        @NotBlank String endpoint,
        @NotBlank String signName,
        @NotBlank String templateCode) {
}
```

```java
@Configuration
@EnableConfigurationProperties(AliyunSmsProperties.class)
public class AliyunSmsClientConfig {

    @Bean
    public com.aliyun.dysmsapi20170525.Client aliyunSmsClient(
            AliyunSmsProperties properties) throws Exception {
        var config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(properties.accessKeyId())
                .setAccessKeySecret(properties.accessKeySecret())
                .setEndpoint(properties.endpoint());
        return new com.aliyun.dysmsapi20170525.Client(config);
    }
}
```

## 6. 短信发送服务

发送服务必须检查阿里云返回体的业务码，不能把“SDK 没有抛异常”等同于发送成功。

```java
@Service
public class AliyunSmsSender {

    private final com.aliyun.dysmsapi20170525.Client client;
    private final AliyunSmsProperties properties;

    public AliyunSmsSender(com.aliyun.dysmsapi20170525.Client client,
                           AliyunSmsProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public SmsSendResult sendLoginCode(String phone, String code) {
        try {
            var request = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(properties.signName())
                    .setTemplateCode(properties.templateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            var response = client.sendSmsWithOptions(
                    request, new com.aliyun.teautil.models.RuntimeOptions());
            var body = response.getBody();
            boolean accepted = body != null && "OK".equalsIgnoreCase(body.getCode());
            return new SmsSendResult(
                    accepted,
                    body == null ? null : body.getRequestId(),
                    body == null ? null : body.getBizId(),
                    body == null ? "EMPTY_RESPONSE" : body.getCode());
        } catch (Exception exception) {
            // 日志只记录异常类型和内部追踪号，不记录验证码、Secret 或完整手机号。
            throw new SmsDeliveryException("短信服务暂时不可用", exception);
        }
    }
}

public record SmsSendResult(
        boolean accepted, String requestId, String bizId, String providerCode) {
}
```

生产实现可用 JSON 序列化库构造 `templateParam`，避免以后增加模板参数时手工拼接 JSON。

## 7. 验证码生成、缓存与校验

### 7.1 Redis 键设计

不要沿用没有场景隔离的 `verificationCode_手机号`。建议：

```text
sms:code:login:{phone}          验证码摘要，TTL 5 分钟
sms:send:cooldown:{phone}       60 秒发送冷却
sms:verify:fail:login:{phone}   校验失败次数，TTL 5 分钟
sms:send:daily:{phone}:{date}   单手机号日发送次数
sms:send:ip:{ip}:{window}       单 IP 窗口发送次数
```

使用场景字段可以防止注册验证码被拿去执行登录、改密或敏感操作。

### 7.2 生成规则

```java
private static final SecureRandom SECURE_RANDOM = new SecureRandom();

private String generateSixDigitCode() {
    return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
}
```

禁止使用 `Math.random()`。验证码不能出现在接口响应、普通业务日志、数据库操作日志和链路追踪标签中。

### 7.3 推荐发送顺序

1. 校验手机号格式并标准化；
2. 检查账号是否允许使用短信登录；
3. 执行手机号、IP、设备和日累计频控；
4. 使用 `SecureRandom` 生成验证码；
5. 调用短信供应商；
6. 仅当供应商明确返回成功时，将验证码摘要写入 Redis，TTL 5 分钟；
7. 返回统一文案，不向客户端返回验证码、`bizId` 或供应商内部错误。

验证码缓存建议保存带服务端 Pepper 的 HMAC-SHA256 摘要，而不是明文。Pepper 必须来自密钥管理系统，且与短信 AccessKey 分离。

### 7.4 校验和消费规则

- 无验证码：返回“验证码无效或已过期”；
- 摘要不匹配：累计错误次数；达到 5 次后删除验证码并临时锁定；
- 匹配成功：必须原子删除验证码，使其只能使用一次；
- 并发请求下不能采用普通的 `GET` 后再 `DELETE`；应使用 Redis Lua 脚本或项目已验证的原子 `GETDEL` 方案；
- 验证成功不等于允许登录，仍需检查用户存在、状态正常、租户/组织有效等业务约束。

## 8. HTTP 接口协议

### 8.1 获取验证码

```http
POST /api/auth/sms/code
Content-Type: application/json

{
  "phone": "13800138000",
  "scene": "LOGIN"
}
```

推荐成功响应：

```json
{
  "code": 200,
  "message": "如果该手机号可用，验证码将发送至您的手机"
}
```

不要返回验证码。是否对未注册手机号使用统一文案，应结合产品的注册策略决定；面向公网时建议避免通过响应枚举账号。

### 8.2 验证码登录

```http
POST /api/auth/sms/login
Content-Type: application/json

{
  "phone": "13800138000",
  "code": "123456"
}
```

推荐成功响应：

```json
{
  "code": 200,
  "data": {
    "accessToken": "<token>",
    "expiresIn": 7200
  }
}
```

DTO 示例：

```java
public record SendSmsCodeRequest(
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$") String phone,
        @NotNull SmsScene scene) {
}

public record SmsLoginRequest(
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$") String phone,
        @NotBlank
        @Pattern(regexp = "^\\d{6}$") String code) {
}

public enum SmsScene {
    LOGIN, REGISTER, RESET_PASSWORD
}
```

## 9. 与当前系统兼容时的接口调用

如果另一个项目不是重新实现短信服务，而是通过网关调用当前系统，现有协议为：

获取验证码：

```http
POST /system/auth/pc/captcha
Content-Type: application/json

{
  "userName": "13800138000"
}
```

该接口会先检查用户已经注册、状态为正常且用户类型符合要求，然后发送短信。

验证码登录：

```http
POST /system/auth/pc/userInfoLogin
Content-Type: application/json

{
  "userName": "13800138000",
  "msgCode": "123456"
}
```

当前成功响应的 `data` 中包含 Token 信息，现有 PC 前端读取 `data.access_token`。

跨项目直接调用时还必须确认：

- 网关路由 `/system/**` 是否允许调用；
- 两个项目是否属于同一身份域和租户边界；
- Token 是否能被调用方资源服务器验证；
- 是否允许新项目共享当前系统的短信签名、模板额度和费用；
- 超时、重试和幂等策略是否会导致重复发送。

如果只是共享短信能力，不建议让新项目依赖整个 `teaching-system` 登录接口。更合理的方案是抽取受控的短信服务或在新项目中独立接入阿里云，并分别承担频控、审计和费用。

## 10. 当前实现不能照搬的部分

| 当前实现 | 风险 | 新项目要求 |
|---|---|---|
| `Math.random()` 生成验证码 | 随机性不足 | 使用 `SecureRandom` |
| `SmsUtil` 返回 `verificationCode` | 容易被接口或日志泄露 | 发送层绝不向外返回验证码 |
| 操作日志保存明文验证码 | 数据泄露和合规风险 | 只记录脱敏手机号、结果、RequestId |
| SDK 无异常即认为成功 | 供应商可能返回业务失败码 | 必须检查响应体 `code == OK` |
| 普通 `GET` 后 `DELETE` | 并发下可能重复消费 | 使用 Lua/GETDEL 原子校验消费 |
| 只有前端 60 秒倒计时 | 可绕过 | Redis 服务端强制冷却和限流 |
| 键不包含业务场景 | 验证码可能跨用途复用 | 键必须包含 `LOGIN/REGISTER/...` |
| 错误次数不限 | 可暴力枚举 6 位码 | 限制尝试次数并临时锁定 |
| 异常消息直接拼接返回 | 可能泄露供应商细节 | 对外统一错误，内部用追踪号诊断 |

## 11. 安全和运维要求

- AccessKey 泄露时立即轮换，不只是在配置页面删除；
- 手机号在日志中脱敏，例如 `138****8000`；
- 不记录验证码正文、短信模板完整参数和 Secret；
- 监控发送成功率、供应商错误码、延迟、频控命中率和费用异常；
- 对超时结果不要自动无条件重试，因为供应商可能已经受理；
- 建立单手机号、单 IP、单设备、单租户和系统总量限额；
- 对管理员、高价值操作和敏感账户，不应只依赖短信作为唯一认证因素；
- Redis 不可用时默认拒绝验证码登录，禁止降级为固定验证码或绕过校验；
- 测试环境使用专用签名/模板、白名单号码或 Mock Sender，禁止自动给真实用户发短信。

## 12. 测试与验收清单

交付前至少验证：

1. 正确手机号可以收到与模板匹配的 6 位验证码；
2. 供应商返回非 `OK` 时接口不报告成功，Redis 也不保存验证码；
3. 验证码 5 分钟后失效；
4. 验证码成功使用一次后立即失效；
5. 两个并发登录请求最多只有一个成功；
6. 错误验证码达到上限后被锁定；
7. 60 秒内重复发送被服务端拒绝；
8. 手机号/IP/设备/日累计限流生效；
9. 禁用、注销、非本租户或不存在的用户不能获得登录 Token；
10. Redis、Nacos和短信供应商异常时系统安全失败；
11. 日志、数据库、HTTP 响应和链路追踪中均无验证码及 Secret；
12. Token 的签发、有效期、刷新、注销和资源服务器校验符合新项目自身的认证合同。

## 13. 交给另一个项目 AI 的执行指令

可将下面内容连同本文档一起交给目标项目 AI：

> 先检查目标项目现有的认证、Redis、配置管理、异常响应、限流和 Token 体系，不要新建第二套冲突的基础设施。保持本文档中的短信配置字段兼容，但不要复制当前项目的安全缺陷。先输出目标项目的接入点、文件变更清单、接口合同和风险判断；确认无冲突后实现。实现必须包含单元测试、Redis 并发消费测试、供应商失败测试、接口集成测试和不泄密检查。真实 AccessKey、签名和模板编号只能从目标环境的密钥管理或 Nacos 注入，不得写入源码、测试快照、日志或提交记录。完成后按第 12 节逐项给出 PASS/FAIL 证据；没有真实短信回执时只能标记为候选通过，不能声称生产短信已验证。
