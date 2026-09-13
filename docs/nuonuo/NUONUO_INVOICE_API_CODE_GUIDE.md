# 诺诺（Nuonuo）开票接口代码说明

> 文档性质：基于当前仓库后端代码的静态梳理，不等同于诺诺开放平台官方接口文档，也不证明当前生产配置、商户授权或接口权限仍然有效。
>
> 代码范围：`old-code/teaching-modules/teaching-system` 及其 Feign/定时任务调用方。

## 1. 先说结论

### 1.1 事实

- 项目侧新版开票入口是 `POST /invoice/applyNew`；经网关访问时，现有前端使用 `POST /system/invoice/applyNew`。
- 新版开票业务方法是 `invoiceApplyNew`，最终统一通过 `InvoiceConfig.requestApi(...)` 调用诺诺 Java SDK。
- 诺诺“申请开票”的 API 方法名不是写死在代码中，而是从 `${invoice.applyMethod}` 注入。
- 当前仓库没有找到 `${invoice.applyMethod}` 的实际配置值，也没有找到字符串 `requestBillingNew`。
- 查询接口写死为 `nuonuo.OpeMplatform.queryInvoiceResult`。
- 重新开票接口写死为 `nuonuo.OpeMplatform.reInvoice`。
- `fastInvoiceRed` 当前也调用 `nuonuo.OpeMplatform.reInvoice`，并没有独立的“快捷冲红”诺诺方法名。

### 1.2 推理

`requestBillingNew` 很可能是期望配置到 `${invoice.applyMethod}` 的诺诺方法名，或是需求方对“新版开票申请”的业务称呼。但在拿到当前 Nacos/运行环境配置或诺诺应用后台接口授权清单前，不能把它写成已确认事实。

建议联调时先读取运行实例实际生效的：

```yaml
invoice:
  url: <实际网关地址>
  applyMethod: <需要确认是否为包含 requestBillingNew 的完整方法名>
  callBack: <诺诺可访问的完整回调 URL>
```

### 1.3 主要风险

1. `callback` 代码中没有看到签名、时间戳、重放或来源校验，存在伪造回调修改开票状态的风险。
2. `fastInvoiceRed` 名称与实际调用的 `reInvoice` 方法不一致，可能调用错诺诺 API。
3. `fastInvoiceRed` 使用 GET 执行有副作用的冲红操作，容易被缓存、预取或误触发。
4. `fastInvoiceRed` 成功后只返回新流水号，未看到新增红票记录或更新原票状态。
5. SDK 请求和响应正文被直接输出或记录日志，可能包含个人信息、税号、发票链接等敏感数据。

## 2. 总体调用关系与文件路径

| 功能 | 项目入口 | Service 方法 | 诺诺 API 方法 | 主要文件 |
|---|---|---|---|---|
| 新版开票申请 | `POST /invoice/applyNew` | `invoiceApplyNew` | `${invoice.applyMethod}`，仓库内值未知 | `controller/InvoiceInfoController.java`、`service/impl/InvoiceInfoServiceImpl.java`、`config/InvoiceConfig.java` |
| 开票结果查询 | `POST /invoice/queryInvoiceResult` | `queryPersonalInvoiceResult` → `queryInvoiceResult` | `nuonuo.OpeMplatform.queryInvoiceResult` | 同上，另见 `InvoiceQueryReq.java`、`InvoiceQueryResult.java` |
| 开票结果定时同步 | `GET /invoice/syncInvoiceResult`（内部调用） | `syncInvoiceResult` | 同查询接口 | `RemoteInvoiceService.java`、`InvoiceTask.java`、`InvoiceInfoMapper.xml` |
| 开票结果回调 | `POST /invoice/scan` | `applyCallback` | 诺诺主动回调，不是本系统主动请求 | `InvoiceInfoController.java`、`InvoiceInfoServiceImpl.java`、`ApplyCallBackContent.java` |
| 快捷冲红 | `GET /invoice/fastInvoiceRed?id=...` | `fastInvoiceRed` | **当前代码实际为** `nuonuo.OpeMplatform.reInvoice` | `InvoiceInfoController.java`、`InvoiceInfoServiceImpl.java`、`InvoiceConfig.java` |

网关前缀 `/system` 可由现有小程序 API 调用代码佐证，但部署环境最终路由仍需以网关配置为准。

## 3. 新版开票申请与 `requestBillingNew`

### 3.1 项目接口

```http
POST /system/invoice/applyNew
Content-Type: application/json
Authorization: Bearer <token>
```

请求体是数组，每个元素对应一个收款单位下的一次合并开票申请：

```json
[
  {
    "merId": "merchant-id",
    "invoiceClass": "企业",
    "buyerName": "示例单位",
    "buyerTaxNumber": "buyer-tax-number",
    "email": "invoice@example.com",
    "phone": "13800000000",
    "remark": "报名费",
    "invoiceType": "1",
    "invoiceLine": "pc",
    "goodsCode": "3049900000000000000&fee_type",
    "userIds": [10001, 10002],
    "orderIds": [20001],
    "randomId": "client-generated-unique-order-no"
  }
]
```

字段来源：`domain/vo/invoice/InvoiceApplyReq.java`。其中 `invoiceAmount` 和 `commodityType` 虽然出现在 DTO 中，但新版流程会根据当前用户拥有的已支付订单及参赛/证书数据在服务端重新计算，不应信任前端传值。

### 3.2 后端校验与处理

`invoiceApplyNew` 对每个请求项依次处理：

1. 校验 `randomId` 非空且长度不超过 64。
2. 使用“当前用户 + 排序后的订单 ID + 用户 ID”形成 Redis 幂等键。
3. 校验订单存在、属于当前登录用户、已支付、未开票且不在开票中。
4. 校验所选订单属于同一收款单位、同一业务类型。
5. 服务端重新计算允许开票的人员和金额。
6. 按商户读取诺诺 `appKey`、`appSecret`、`accessToken`、税号、税率、开票员等配置。
7. 构建 `{"order": {...}}` 请求，调用 `InvoiceConfig.requestApi(...)`。
8. 诺诺响应 `code == "E0000"` 时读取 `result.invoiceSerialNum`，写入 `invoice_info`，并将关联订单/人员状态更新为开票中。

成功响应是项目统一 `AjaxResult.success("开票申请成功")`；部分商户失败时会汇总错误信息后返回失败。

### 3.3 发送给诺诺的主要字段

实际发送对象由 `buildAppDataReqParamNew` 生成，外层固定为 `order`：

```json
{
  "order": {
    "buyerName": "示例单位",
    "buyerTaxNum": "buyer-tax-number",
    "email": "invoice@example.com",
    "pushMode": "0",
    "buyerPhone": "13800000000",
    "salerTaxNum": "seller-tax-number",
    "salerAccount": "开户行 银行账号",
    "orderNo": "client-generated-unique-order-no",
    "invoiceDate": "yyyy-MM-dd HH:mm:ss",
    "remark": "报名费",
    "clerk": "开票员",
    "checker": "复核员",
    "invoiceType": "1",
    "invoiceLine": "pc",
    "callBackUrl": "<invoice.callBack>",
    "extensionNumber": "<分机号>",
    "invoiceDetail": []
  }
}
```

邮箱为空时 `pushMode = "-1"`，否则为 `"0"`。`invoiceLine` 未传时默认 `"pc"`。

### 3.4 `requestBillingNew` 的准确表述

当前代码只能确认以下调用：

```java
invoiceConfig.requestApi(
    invoiceConfig.getAPPLY_METHOD(),
    content,
    merchantParamConfig.getInvoiceAccessToken(),
    invoiceConfig.getCALLBACK(),
    merchantParamConfig
);
```

其中 `APPLY_METHOD` 来自 `${invoice.applyMethod}`。因此建议文档和配置验收使用以下状态：

| 检查项 | 当前结论 |
|---|---|
| 项目新版开票入口是否存在 | 已确认：`applyNew` / `invoiceApplyNew` |
| 是否通过诺诺 SDK 请求 | 已确认：`NNOpenSDK.sendPostSyncRequest(...)` |
| 是否使用 `requestBillingNew` | **无法从仓库确认** |
| 如何最终确认 | 检查当前运行实例/Nacos 的 `invoice.applyMethod`，并与诺诺应用授权接口清单核对 |

不要仅因方法名包含 “New” 就直接把 `${invoice.applyMethod}` 改成某个猜测值；诺诺 SDK 需要的通常是完整 API 方法名。

## 4. 开票结果查询

### 4.1 项目接口

```http
POST /system/invoice/queryInvoiceResult
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "serialNos": ["invoice-serial-no"],
  "orderNos": ["business-order-no"],
  "isOfferInvoiceDetail": "0"
}
```

代码注释说明 `serialNos` 与 `orderNos` 二选一；同时存在时诺诺侧以流水号为准，最多 50 个。个人接口会先校验请求中的每个订单号/流水号都属于当前登录用户，然后调用内部查询方法。

### 4.2 诺诺调用

```text
method = nuonuo.OpeMplatform.queryInvoiceResult
content = InvoiceQueryReq 的 JSON
credential = 根据本地首个订单号或流水号定位销方税号，再加载对应商户配置
```

项目先用请求中的第一个 `orderNo` 查本地 `invoice_info`；查不到再用第一个 `serialNo`。找到记录后，按 `saler_tax_num` 选择商户凭据，但会把请求中的完整数组继续发送给诺诺。

### 4.3 状态映射与本地写入

| 诺诺查询状态 | 代码含义 | 本地动作 |
|---|---|---|
| `2` | 开票完成 | 本地 `issued_status = 1`，写入号码、代码、开票时间、PDF URL，并同步订单/人员开票状态 |
| `20` | 开票中 | 不更新 |
| `21` | 开票成功、签章中 | 不更新 |
| `22` | 开票失败 | 本地 `issued_status = 2`，写入失败原因 |
| `24` | 开票成功、签章失败 | 本地 `issued_status = 2`，写入失败原因 |
| `3` | 发票已作废 | 只记日志，不更新本地状态 |
| `31` | 发票作废中 | 只记日志，不更新本地状态 |

接口成功时返回 `AjaxResult.success(list)`；返回模型为 `InvoiceQueryResult`，包括状态、失败原因、PDF/OFD/XML 地址、发票号码、数电票号码、金额、购销方信息及明细等字段。

### 4.4 定时同步

- `InvoiceInfoMapper.xml` 查询 `del_flag = 0 and issued_status != '1'` 的全部记录。
- `InvoiceTask.ryOrderPayTask()` 通过 `RemoteInvoiceService` 发起内部调用。
- 调度频率不写死在 Java 代码中，应在任务管理配置中核实。
- 每条待同步记录都会单独调用一次诺诺查询接口。

## 5. `callback` 回调

### 5.1 接口

Controller 中实际路由名称是 `scan`，方法名才是 `callback`：

```http
POST /system/invoice/scan
Content-Type: application/x-www-form-urlencoded
```

代码按表单参数 `Map<String, String>` 接收，至少依赖：

| 参数 | 作用 |
|---|---|
| `operater` | 只有值为 `callback` 才进入业务处理 |
| `orderno` | 本系统开票时传给诺诺的业务订单号，用于查 `invoice_info.order_id` |
| `content` | JSON 字符串，反序列化为 `ApplyCallBackContent` |

示意请求：

```text
operater=callback
orderno=client-generated-unique-order-no
content={"cStatus":"1","cOrderno":"client-generated-unique-order-no","cFpqqlsh":"...","cFpdm":"...","cFphm":"...","cUrl":"..."}
```

### 5.2 `content` 关键字段

| 字段 | 含义 |
|---|---|
| `cStatus` | `1` 开票完成；`2` 开票失败；`3` 开票成功但签章失败 |
| `cKprq` | 开票日期 |
| `cFpdm` / `cFphm` | 发票代码 / 发票号码 |
| `allElectronicInvoiceNumber` | 数电票号码 |
| `cOrderno` | 订单号 |
| `cFpqqlsh` | 发票流水号 |
| `cErrorMessage` | 失败原因 |
| `cUrl` / `cJpgUrl` | PDF 地址 / 发票详情地址 |
| `email` / `phone` | 购方邮箱 / 手机 |

### 5.3 处理结果

- 本地找不到 `orderno`：返回 `{"status":"0001","message":"订单号不存在"}`。
- `cStatus != "1"`：本地标记失败、记录失败原因，并把关联业务开票状态回滚为未开票。
- `cStatus == "1"`：本地标记成功，保存号码、代码、日期、PDF/详情地址，同步订单/人员状态并保存发票抬头。
- 正常处理完成：返回 `{"status":"0000","message":"同步成功"}`。
- `notifyMap` 为空或 `operater` 不是 `callback` 时，Controller 直接返回 `null`。

### 5.4 回调上线前必须补齐的确认

代码中未看到签名验签、回调时间窗、事件唯一键、防重放、IP 白名单或证书校验；网关是否放行 `/system/invoice/scan` 也不在仓库静态配置中。上线前至少应核对：

1. 诺诺官方回调签名字段、验签算法和原始报文规范；
2. 网关匿名白名单是否只放行该精确路径；
3. 回调重试时能否幂等处理；
4. 无效请求是否始终返回明确的失败 ACK，而不是 `null`；
5. 日志是否对税号、手机号、邮箱、发票地址和完整报文脱敏。

## 6. `fastInvoiceRed`

### 6.1 项目接口

```http
GET /system/invoice/fastInvoiceRed?id=<invoice_info.id>
Authorization: Bearer <token>
```

Controller 要求 `system:info:edit` 权限。

### 6.2 当前构造的诺诺请求

Service 先按 `invoice_info.id` 查询蓝票记录，再按销方税号获取商户凭据，构造：

```json
{
  "orderNo": "原业务订单号",
  "taxNum": "销方纳税人识别号",
  "billNo": "原发票号码",
  "billUuid": "后端临时生成的 UUID",
  "invoiceId": "原发票流水号"
}
```

若诺诺响应 `code != "E0000"`，项目返回 `describe` 错误；成功时读取并返回 `result.invoiceSerialNum`。

### 6.3 代码与名称不一致

当前调用代码是：

```java
invoiceConfig.requestApi(
    invoiceConfig.getReInvoiceMethod(), // nuonuo.OpeMplatform.reInvoice
    content,
    config.getInvoiceAccessToken(),
    invoiceConfig.getCALLBACK(),
    config
);
```

这与 `fastInvoiceRed` 的“数电快捷冲红”语义不一致。项目自己的 `InvoiceApplyData` 注释也明确写着“数电票冲红请对接数电快捷冲红接口”。因此当前状态应标记为：

**HUMAN_REVIEW_REQUIRED：必须用诺诺当前租户的官方接口文档和已授权 API 清单确认快捷冲红的完整 method 名、参数、回调与状态查询协议。**

在确认前，不建议把该接口用于真实冲红。即使诺诺返回成功，当前代码也没有：

- 新建红票 `invoice_info` 记录；
- 将原蓝票标记为冲红中/已冲红；
- 保存返回的红票流水号；
- 防重复冲红的持久化幂等控制；
- 校验发票是否属于可冲红状态；
- 校验 `invoiceInfo` 和商户配置是否为空。

## 7. 文件路径索引

以下路径均相对于仓库根目录：

| 功能点 | 文件路径 | 关键位置 |
|---|---|---|
| HTTP 接口定义 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/controller/InvoiceInfoController.java` | `applyNew`、`callback`、`queryInvoiceResult`、`syncInvoiceResult`、`fastInvoiceRed` |
| Service 接口 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/IInvoiceInfoService.java` | `invoiceApplyNew`、`applyCallback`、`queryInvoiceResult`、`fastInvoiceRed` |
| 核心业务实现 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/InvoiceInfoServiceImpl.java` | 订单校验、幂等、请求组装、回调、状态同步、冲红 |
| 诺诺 SDK 封装/方法名 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/config/InvoiceConfig.java` | `${invoice.applyMethod}`、`queryMethod`、`reInvoiceMethod`、`requestApi` |
| 新版开票入口 DTO | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/InvoiceApplyReq.java` | 项目接口请求字段 |
| 诺诺开票请求 DTO | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/InvoiceApplyData.java` | 发送给诺诺的 `order` 字段结构 |
| 发票明细 DTO | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/InvoiceDetail.java` | 商品/税率/金额明细 |
| 查询请求 DTO | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/InvoiceQueryReq.java` | `serialNos`、`orderNos`、`isOfferInvoiceDetail` |
| 查询响应 DTO | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/InvoiceQueryResult.java` | 诺诺查询结果字段 |
| 回调 DTO | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/ApplyCallBackContent.java` | 回调 `content` 字段 |
| 发票持久化实体 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/InvoiceInfo.java` | 本地发票记录及状态字段 |
| 商户凭据与销方配置 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/MerchantParamConfig.java` | appKey/appSecret/accessToken/税号/税率等；禁止在文档或日志中填写真实值 |
| 发票 SQL | `old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/InvoiceInfoMapper.xml` | 按订单号/流水号查询、待同步记录筛选、更新入库 |
| 内部同步 Feign | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/RemoteInvoiceService.java` | `/invoice/syncInvoiceResult` |
| 定时任务入口 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/task/InvoiceTask.java` | `ryOrderPayTask` |
| 小程序调用封装 | `old-code-mini/api/invoice.js` | 网关路径 `/system/invoice/applyNew`、`queryInvoiceResult` |
| 管理端查询调用 | `old-code-admin/src/api/iPayment/index.js` | 网关路径 `/system/invoice/queryInvoiceResult` |

## 8. 联调验收清单

- [ ] 从实际运行配置确认 `invoice.url`、`invoice.applyMethod`、`invoice.callBack`，但不导出 secret/token 值。
- [ ] 确认 `${invoice.applyMethod}` 是否确为诺诺当前授权的 `requestBillingNew` 完整方法名。
- [ ] 用诺诺当前官方文档确认查询状态码、回调 ACK、快捷冲红 method 和字段。
- [ ] 回调完成验签、防重放、幂等和精确白名单后再开放公网。
- [ ] 使用测试企业和测试票完成：申请 → 回调 → 主动查询 → 本地状态一致性验证。
- [ ] 单独验证失败、签章失败、作废、重复回调、重复查询和超时重试。
- [ ] 修正并验证 `fastInvoiceRed` 后，再完成：蓝票 → 冲红申请 → 红票结果 → 原票/红票双记录闭环。
- [ ] 清理或脱敏 SDK 请求、响应及回调原文日志。

