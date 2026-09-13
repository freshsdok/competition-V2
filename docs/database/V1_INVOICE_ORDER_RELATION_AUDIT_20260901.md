# V1 有效发票与订单关联专项核查

核查日期：2026-09-01（Asia/Shanghai）  
核查范围：本机候选库 `jiaoxue_test`、V1 PC/小程序开票页面、Controller/Service/Mapper/Redis 封装  
执行边界：只读 SQL、代码静态核查和本机容器识别；未读取或写出发票抬头、税号、手机号、邮箱、订单号、URL 等明文；未修改数据库或 Redis。

## 1. 结论

1. `invoice_info` 有 1,803 行，其中 `del_flag='0'` 的有效发票为 **1,792** 行；有效发票的 `order_id` 全部是 32 位十六进制字符串，且 1,792 个值全部唯一。
2. **2025 年的 1 条有效发票**沿用旧流程，`invoice_info.order_id` 可以直连 `order_info.order_id`。对应订单已软删除但支付状态为已支付；用户和金额也一致，因此它是一个可复核的历史关联候选，不应因订单软删除而丢弃。
3. **2026 年的 1,791 条有效发票**走新流程。其 `invoice_info.order_id` 不是订单号，而是 PC/小程序前端生成的 `randomId`，用于开票服务的申请号和回调关联号。
4. 新流程真正选择的是一组 `order_info.id` 和一组人员 ID；后端校验订单归属/支付/商户/业务类型后，只把这些列表写到 Redis：
   - `invoice_apply_order:{randomId}` → `List<order_info.id>`
   - `invoice_apply_user:{randomId}` → `List<user_id>`
5. MySQL 中没有 `invoice_order_relation` 或同类表，`invoice_info.order_id` 没有外键，也没有普通索引；因此当前 MySQL 快照无法无歧义恢复 1,791 条新流程发票的真实订单列表。
6. Redis List 写入方法没有 TTL，代码中也未发现删除这些业务键的逻辑。若能取得原 V1 Redis 在线只读快照、RDB 或 AOF，仍有较大机会恢复。当前运行中的 Redis 容器均不能确认为该 V1 数据源，仓库中也没有 RDB/AOF，因此本次没有把其他项目 Redis 当作证据。
7. “同用户 + 同金额 + 时间窗口”的 721 条候选不能代替原始关系。页面允许一张发票合并同一收款方的多个订单，也允许按订单内人员选择开票，实际关系是发票—订单及发票—人员的多对多关系。

迁移处置：**1,792 条发票文档本身是迁移候选；1 条旧流程历史关系进入人工复核候选；1,791 条新流程订单 allocation 保持 `HOLD_REDIS_RECOVERY_REQUIRED`。**

## 2. 事实、推理和风险

### 2.1 事实

| 核查项 | 结果 |
|---|---:|
| `invoice_info` 总行数 | 1,803 |
| 有效发票 `del_flag='0'` | 1,792 |
| 有效且有 PDF URL | 1,791 |
| 有效发票成功/失败状态 | 1,791 / 1 |
| 有效发票金额合计 | 2,974,320.04 |
| 有效发票 `order_id` 唯一值 | 1,792 |
| 有效发票 `order_id` 为 32 位十六进制 | 1,792 |
| 2025 年有效发票 | 1；可直连 1 条已支付、已软删除历史订单 |
| 2026 年有效发票 | 1,791；业务订单号、内部主键及支付/退款候选字段均无直接关系 |
| 发票—订单关系表 | 0 |
| `invoice_info.order_id` 外键 | 0 |
| `invoice_info.order_id` 索引 | 0 |

对 `order_info` 的候选字段复核：

| 候选字段 | 匹配行数 | 判断 |
|---|---:|---|
| `order_info.order_id` | 1 | 2025 旧流程历史关联 |
| `order_info.id` | 0 | 新流程真实主键没有落到发票表 |
| `order_info.out_order_id` | 0 | 无关系 |
| `order_info.cmb_order_id` | 0 | 无关系 |
| `order_info.biz_order_id` | 1 | 与上述同一条旧流程订单重合 |
| `order_info.target_order_id` | 0 | 无关系 |
| `order_info.refund_order_id` | 0 | 无关系 |

### 2.2 推理

- 旧流程是一张发票对应一个 `orderInfo.getOrderId()`；新流程为支持按人员选择、同商户合并开票，改成了一个虚拟申请号对应多个内部订单主键。
- `order_id` 的数据库注释仍写“订单号”，但新流程实际语义已经变为“开票申请/回调关联号”。这是字段语义漂移，不是 1,791 条数据随机损坏。
- 一张发票可覆盖多个订单；同一订单的不同人员也可能分批开票。因此不能要求 `invoice_info.order_id` 与某个订单号一对一相等。
- 即便恢复 Redis 的订单列表，也还要结合 `invoice_apply_user:*`、报名/证书费用明细重新计算 allocation 金额，并校验合计等于 `invoice_info.amount`；Redis 列表本身不直接保存逐订单分摊金额。

### 2.3 风险

- 如果直接把 `invoice_info.order_id` 当订单号迁入 V2，会产生 1,791 个伪订单引用。
- 如果按金额/用户/时间猜测，多个订单、部分人员和同额订单会导致错误归属，属于财务数据污染。
- 如果只迁发票 PDF 而不区分 document 与 allocation，将来会误以为发票已完成订单对账。
- 旧流程命中的订单已软删除；迁移时仍需保留订单历史状态，不能只在 V2 当前订单集合中查找。
- 原 V1 Redis 是否仍存在、是否发生过清库/淘汰/覆盖，目前缺证据。恢复率不能预先承诺。

## 3. `invoice_info` 数据库结构

下表来自当前 `jiaoxue_test.information_schema.columns`。源库注释存在字符集显示问题，语义列以实体类、Mapper 和业务代码交叉确认。

| 字段 | 类型 | NULL/默认 | 键 | 业务语义 |
|---|---|---|---|---|
| `id` | `bigint` | NOT NULL / auto increment | PK | 发票记录主键 |
| `order_id` | `varchar(32)` | NOT NULL / `'0'` | 无 | 旧流程为业务订单号；新流程为前端 `randomId`，字段名/注释已漂移 |
| `invoice_serial_num` | `varchar(32)` | NULL | 无 | 外部开票流水号 |
| `invoice_num` | `varchar(64)` | NULL | 无 | 发票号码 |
| `invoice_code` | `varchar(32)` | NULL | 无 | 发票代码 |
| `invoice_class` | `varchar(1)` | NULL | 无 | 抬头类型：个人/企业 |
| `invoice_type` | `varchar(2)` | NULL | 普通索引 | 蓝票/红票或付款/退款类型 |
| `buyer_name` | `varchar(50)` | NULL | 无 | 购买方名称/抬头 |
| `buyer_tax_num` | `varchar(20)` | NULL | 无 | 购买方税号 |
| `buyer_phone` | `varchar(20)` | NULL | 无 | 购买方电话 |
| `buyer_email` | `varchar(50)` | NULL | 无 | 购买方邮箱 |
| `saler_tax_num` | `varchar(20)` | NULL | 无 | 销方税号 |
| `saler_tel` | `varchar(20)` | NULL | 无 | 销方电话 |
| `saler_address` | `varchar(80)` | NULL | 无 | 销方地址 |
| `invoice_date` | `datetime` | NULL | 无 | 申请/发票日期 |
| `clerk` | `varchar(20)` | NULL | 无 | 开票员 |
| `call_back_url` | `varchar(100)` | NULL | 无 | 外部开票回调地址 |
| `enterprise_name` | `varchar(32)` | NULL | 无 | 企业名称兼容字段 |
| `tax_code` | `varchar(32)` | NULL | 无 | 税务编码兼容字段 |
| `addr` | `varchar(50)` | NULL | 无 | 地址兼容字段 |
| `invoice_content` | `varchar(100)` | NULL | 无 | 开票内容/商品编码 |
| `phone` | `varchar(20)` | NULL | 无 | 电话兼容字段 |
| `bank_account` | `varchar(32)` | NULL | 无 | 银行账户 |
| `issued_status` | `varchar(2)` | NULL | 普通索引 | 开票中/成功/失败等状态 |
| `fail_reason` | `varchar(200)` | NULL | 无 | 失败原因 |
| `issued_time` | `datetime` | NULL | 普通索引 | 开具时间 |
| `check_status` | `varchar(10)` | NULL | 无 | 查验状态 |
| `create_by` | `varchar(64)` | NULL | 无 | 创建人审计字段 |
| `create_time` | `datetime` | NULL | 无 | 创建时间 |
| `update_by` | `varchar(64)` | NULL | 无 | 更新人审计字段 |
| `update_time` | `datetime` | NULL | 无 | 更新时间 |
| `version` | `bigint` | NULL / `0` | 无 | 版本字段 |
| `del_flag` | `varchar(1)` | NULL / `'0'` | 无 | 软删除标志 |
| `user_id` | `bigint` | NULL | 无 | 申请账号/用户 ID |
| `org_id` | `bigint` | NULL | 无 | 组织 ID |
| `c_url` | `varchar(255)` | NULL | 无 | PDF URL |
| `c_jpg_url` | `varchar(255)` | NULL | 无 | 图片/详情 URL |
| `remark` | `varchar(500)` | NULL | 无 | 发票备注 |
| `amount` | `decimal(10,2)` | NOT NULL / `0.00` | 无 | 开票总金额 |

索引只有：`PRIMARY(id)`、`Index_invoice_type(invoice_type)`、`Index_issued_status(issued_status)`、`Index_issued_time(issued_time)`。当前表没有任何外键。

## 4. 页面证据

### 4.1 小程序

1. 选择页明确展示“仅可选择已支付且未开票的项目；同一收款方将合并开票”。用户按赛事成员或赛证互通申请人选择，每个选项提交 `{orderId, userId}`。
2. 申请页调用金额汇总接口后，把响应中的 `orderIds`、`userIds` 带入表单，同时生成新的 32 位 `randomId`。
3. 页面最终把表单数组提交到 `/system/invoice/applyNew`。

证据位置：

- `old-code-mini/pages-personal/invoice/select.vue:3-44,61-67,110-115`
- `old-code-mini/pages-personal/invoice/apply.vue:104-130,217-242`
- `old-code-mini/api/invoice.js:21`

### 4.2 PC

1. 准备页按报名/赛证互通展示人员、开票状态和金额，实际选中项转换为 `{orderId, userId}`。
2. 申请页按收款单位/业务类型聚合，展示发票抬头、税号、内容和汇总金额；隐藏请求字段仍包含真实 `orderIds`、`userIds`。
3. 每个聚合申请另外生成一个 32 位 `randomId`，随后调用 `invoiceapplyNew`。

证据位置：

- `old-code-pc/src/views/personal/personaltabs/invoice-preparation.vue:1-8,44-100,292-315,501-544`
- `old-code-pc/src/views/personal/personaltabs/invoiceIssuance.vue:217-332,499-530,610-668`

页面因此证明：业务操作本来就是“人员 → 订单 → 同收款方聚合发票”，不是“一张发票页面只绑定一个订单号”。

## 5. 后端和缓存证据

```text
页面选择 order_info.id + user_id
  -> POST /system/invoice/applyNew
  -> requireCurrentUserOrders(orderIds): 校验订单存在、归属当前用户、已支付、未开票
  -> 校验同一商户、同一业务类型并由服务端重算金额
  -> randomId 作为外部开票 orderno
  -> invoice_info.order_id = randomId
  -> Redis invoice_apply_order:{randomId} = List<order_info.id>
  -> Redis invoice_apply_user:{randomId} = List<user_id>
  -> 回调以 randomId 查 invoice_info，再从 Redis 取订单/人员更新状态
```

关键代码：

- `InvoiceApplyReq.java:57-70`：同时定义 `userIds`、`orderIds` 和 `randomId`。
- `InvoiceInfoController.java:143-148`：新接口接收 `List<InvoiceApplyReq>`。
- `InvoiceInfoServiceImpl.java:103-123`：`orderIds` 是 `order_info.id`，并校验归属、支付和开票状态。
- `InvoiceInfoServiceImpl.java:293-310`：按真实订单校验后，把前端随机号作为虚拟 `orderId`。
- `InvoiceInfoServiceImpl.java:319-343`：发票落库，真实订单/人员列表仅写入 Redis。
- `InvoiceInfoServiceImpl.java:446-468`：`invoice_info.order_id = applyReq.randomId`。
- `InvoiceInfoServiceImpl.java:662-731,763-829`：回调以随机号查发票，并依赖 Redis 列表更新订单/人员状态。
- `OrderInfoMapper.xml:296-301`：列表参数实际查询 `order_info.id IN (...)`。
- `InvoiceInfoMapper.xml:260-263`：回调只按 `invoice_info.order_id` 查询。
- `RedisService.java:129-141`：List 写入/读取未设置 TTL。

旧接口 `/apply` 已标记废弃；旧构建方法使用 `orderInfo.getOrderId()` 写入发票表，解释了 2025 年的直接关联记录。

## 6. V1 → V2 迁移处理

### 6.1 可以独立迁移的发票事实

- `invoice_application`：来源主键、随机申请号、申请账号、组织、抬头类型、申请时间、内容、金额、状态和来源 lineage。
- `invoice_document`：发票代码/号码、外部流水、开具时间、状态、失败原因。
- `artifact_version/file_object`：下载 PDF 后校验可打开性，计算 SHA-256、size、MIME；不能只迁 URL。
- PII 字段按 V2 的加密、访问控制和最小化策略迁移，不进入普通日志或报告。

### 6.2 订单关系恢复 Gate

取得原 V1 Redis/RDB/AOF 后，以源快照方式恢复，不直接在生产缓存上加工：

1. 对每条 2026 年有效发票，用 `invoice_info.order_id` 构造精确键 `invoice_apply_order:{token}` 和 `invoice_apply_user:{token}`。
2. 订单 List 的每个值必须唯一匹配 `order_info.id`；不存在、重复、跨账号、非已支付或商户/业务类型不一致均进入隔离。
3. 人员 List 必须能回到对应订单的报名成员或赛证互通申请，不能把两个 List 直接做无条件笛卡尔积。
4. 用源费用明细重算逐人员/逐订单 allocation，要求 allocation 合计严格等于发票 `amount`。
5. 生成恢复回执：`invoice_source_id`、token 指纹、Redis 快照指纹、订单内部 ID、订单业务号、人员 ID、分摊金额、校验状态和决策 ID。
6. `1791 = recovered + quarantined_missing_key + quarantined_invalid_reference + quarantined_amount_mismatch`，任何缺项都不得自动提交。

建议 V2 至少分开保存：

```text
invoice_application
invoice_document
invoice_allocation(invoice_id, billing_case/order_id, subject/member_id, amount, source_evidence_id)
migration_source_evidence(redis_snapshot_id, redis_key_fingerprint, source_value_fingerprint)
```

### 6.3 当前 disposition

| 数据 | 数量 | 当前处置 |
|---|---:|---|
| 有效发票 application/document | 1,792 | `CANDIDATE`，仍需 PDF 实体和 PII/财务 Gate |
| 2025 旧流程历史订单关系 | 1 | `HOLD_FACT_OWNER_REVIEW`；订单为已支付但软删除 |
| 2026 新流程订单 allocation | 1,791 | `HOLD_REDIS_RECOVERY_REQUIRED` |
| 721 条启发式候选 | 721 | `EVIDENCE_ONLY_DO_NOT_COMMIT` |
| `review_record` | 689 | `ARCHIVE_ONLY_NOT_IN_V2` |
| `user_grade_info` | 2 | `ARCHIVE_ONLY_NOT_IN_V2` |

## 7. 信息缺口和下一步

当前唯一能改变 1,791 条 allocation 结论的权威新证据是：

- 原 V1 Redis 的只读访问；或
- 与发票生成同期、未被重写的 RDB/AOF；或
- 外部开票服务/审计日志中同时包含虚拟申请号与真实订单主键列表的不可变回执。

若三者均不存在，应迁移发票文档但将订单关系显式标为 `UNKNOWN_SOURCE_RELATION_LOST`，而不是补造关系。长期修复应在 V1/V2 业务流程中把发票—订单—人员 allocation 与发票申请同事务持久化，Redis 只做缓存。
