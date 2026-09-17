# V1 HTTP 支付回调转发到 V2

## 行为

V1 本地订单优先。仅当原通知含签名、订单号匹配当前 32 位十六进制或历史 V2 前缀格式、且 V1 数据库不存在该订单时，转发原始字段至配置的 V2 Provider 实例。V1 和 V2 都使用 UUID，不能只凭长度或前缀判定归属。

转发发生在旧支付服务移除 sign 之前，保留 biz_content 字符串和所有公共签名字段，不重建业务 JSON、不重新签名、不自行判定已付款。V2 负责验签、商户/金额/币种/订单/事件校验，并返回签名 ACK。原有 V1 订单仍由原流程处理。

该改动不替代原有 V1 回调安全审计；旧 V1 支付回调关闭验签的既有行为不在本次修改范围内。

## 配置（部署时显式启用）

将以下配置加入实际运行 V1 system 的配置源；不要仅修改未被该实例加载的文件。

```yaml
v2:
  commerce-callback:
    enabled: true
    api-base-url: http://127.0.0.1:18082/api/v1
    provider-instance-id: 3
    allow-insecure-http: true
    connect-timeout-ms: 3000
    read-timeout-ms: 5000
```

api-base-url 是包含 /api/v1 的 API 基础地址，客户端追加 /public/commerce/cmb-callbacks/3。此示例适用于 V1 Java 进程和 V2 Gateway 位于同一宿主机的现有环境；如果 V1 也运行在容器内，127.0.0.1 不再指向宿主机，必须使用实际可达地址。

银行通知入口继续保留 http://8.130.171.65:9998/system/order/paymentCallback，不修改 V2 Provider 的 paymentCallbackUrl。HTTP 出站需 allow-insecure-http 明确开启；不自动跟随重定向。

缺省 enabled=false，未配置时保持 V1 行为。仅更新 JAR 而未启用配置，不代表转发已开启。

## 验收

1. 部署后的运行 JAR 包含 V2CommerceCallbackDispatcher 和 V2CommerceCallbackClient；确认配置由当前进程加载。
2. 对原订单 43721258A9B545ABBE7C83F57C60B431 等待银行重试或在银行后台重发通知，不要求用户重复付款。
3. V1 日志应出现 V1_CMB_V2_FORWARD_START，随后 V1_CMB_V2_FORWARD_RESULT；returnCode/respCode 必须为 SUCCESS，不能只看 HTTP 200。
4. V2 应对应出现 callback request received、callback verified 和 callback converged。数据库需要回调记录和唯一支付交易，不能仅用页面已支付验收。
5. 相同通知重投不重复入账；篡改签名的通知由 V2 拒绝。转发 HTTP 异常、重定向或无效 ACK 不得回退为 V1 支付成功。

本次仅覆盖支付通知，不改变退款回调。

## 本地测试

```bash
mvn -f old-code/pom.xml -pl teaching-modules/teaching-system -am test \
  '-Dtest=V2CommerceCallback*Test' -Dsurefire.failIfNoSpecifiedTests=false
```

测试使用本地临时 HTTP 服务和模拟订单库，不调用银行、不修改真实订单。代码和测试通过不等于 V1 运行配置和线上转发已验收。
