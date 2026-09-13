# V1 数据资产盘点与 V2 迁移规范数据集

审计时间：2026-09-01 00:37:40（Asia/Shanghai）  
审计对象：本机 MySQL `jiaoxue_test` 只读查询结果、V1 代码/Mapper/字典、当前 V2 本地 Schema 与迁移文件  
执行边界：没有执行 `INSERT`、`UPDATE`、`DELETE`、DDL、数据导出或 V2 写入；没有把手机号、证件号、姓名、邮箱、文件 URL、请求参数等明文写入仓库。

配套文件：

- [`V1_V2_CANONICAL_DATASET_CATALOG_20260901.csv`](./V1_V2_CANONICAL_DATASET_CATALOG_20260901.csv)：规范数据集、粒度、来源、去重键、V2 目标与 Gate。
- [`V1_DATA_ASSET_PROFILE_READONLY_20260901.sql`](./V1_DATA_ASSET_PROFILE_READONLY_20260901.sql)：仅聚合、无 PII 明文输出的复核 SQL。
- [`V1_INVOICE_ORDER_RELATION_AUDIT_20260901.md`](./V1_INVOICE_ORDER_RELATION_AUDIT_20260901.md)：1,792 条有效发票与订单关系的数据库、页面和后端代码专项核查。

## 1. 先判断问题本身

### 1.1 事实

1. 当前可核查的 V1 是本机候选库 `jiaoxue_test`，不是已经由业务/运维签字确认的生产冻结快照。
2. 当前库有 246 张表、4,208 个字段、47 个物理外键，约 2,349.39 MiB；大量关系只在代码中以逻辑外键存在。
3. `@@read_only=0`、`@@super_read_only=0`。审计期间 `dev-mysql80` 容器发生一次重启，容器累计 `RestartCount=28`；重启原因本次未诊断。重启后核心表数量复核一致。
4. 当前 V2 本地库有 265 张表，但只应用了 47 个 Flyway migration，最高为 `2.35.500`；当前 V2 工作树已有 75 个 migration，最高到 `2.41.000`，且工作树存在未提交修改。
5. V2 本地库只有 2 个 `account`、2 个 `subject`，关键业务表均为 0；这些 2 条记录没有 V1 来源回执，不能计为迁移结果。当前本地 V2 也还没有 `migration_run`、`migration_receipt`、`migration_quarantine`、`migration_target_map`。

### 1.2 推理

“最没有重复但是完备”不能理解成“从每组相似表中留一张表”。正确做法是：

- 对同一事实只指定一个 Fact Owner；其他表作为快照、关系、版本或来源证据。
- 对同一自然人做证据分级解析，但不因手机号、姓名、学校相同而自动合并。
- 对文件按二进制哈希去重，但保留不同业务用途和版本的引用。
- 对订单、支付、退款、发票分别建事实，不用报名 `pay_status` 替代真实财务流水。
- 对获奖决定、证书代码、收件人绑定、证书文件分别建事实，不把多表行数直接相加。
- 现场签到、预约、二维码、临时参赛证不进入 V2 当前运行模型，但若有审计/合规需要，应以只读归档保证“没有丢失”。

### 1.3 风险

- 当前源库未冻结，不能直接作为权威迁移输入。
- 身份证、手机号重复/冲突，错误合并会导致账号接管、历史记录串人和越权。
- V2 代码与本地数据库基线不一致，当前不能执行权威迁移。
- 发票新流程把前端生成的开票申请随机号写入 `invoice_info.order_id`，真实 `order_info.id` 列表只写入 Redis；MySQL 缺少持久化发票—订单关系。靠金额、用户和时间猜测会产生财务归属错误。
- 数据库中只有文件元数据/URL，没有对象存储 manifest；只迁 URL 会形成不可用的“空壳材料”。
- 当前审计证明的是本机候选快照，不证明生产数据库、对象存储或服务器 V2 的现状。

## 2. 结论

建议形成三层数据产品，而不是复制 V1 的 246 张表：

| 层 | 用途 | 处置 |
|---|---|---|
| A. 规范业务事实 | V2 可查询、可继续使用的长期资产 | 迁入 V2 领域模型；所有记录带来源 lineage |
| B. 历史快照与证据 | 解释旧状态、支持审计、对账和争议处理 | 迁为不可变版本/证据，或进入加密只读档案 |
| C. 现场与技术运行档案 | 签到、预约、临时凭证、二维码、缓存、作业/引擎运行数据 | 不进入 V2 当前运行模型；按保留策略归档或经审批删除 |

本期应迁移的主链是：

```text
Subject/Account/Identity
  -> Organization/Membership
  -> Competition Activity/Policy
  -> Registration Case/Revision/Group/Member/Teacher
  -> Artifact/File Version + Commerce Payment
  -> Finalization/Participation
  -> Award/Credential
  -> Invoice/Financial Reconciliation
  -> Lineage/Decision/Quarantine
```

签到、预约和其他现场运行数据不进入这条主链。

## 3. 当前数据资产规模

### 3.1 账号、人员、组织

| 资产 | 当前行数 | 关键质量事实 | 迁移判断 |
|---|---:|---|---|
| `sys_user` | 65,369 | 65,037 条 `del_flag=0`；全部账号中有 248 个重复手机号组/510 行，活跃账号中只有 5 组/10 行；活跃用户名也有 5 组/10 行冲突 | 账号锚点必须迁；旧密码/Token/Session/Login IP 不迁 |
| `auth_info` | 60,834 | 60,501 个用户；582 个重复证件号原始组；526 个证件号跨用户复用组、涉及 1,064 个用户链接；45,221 行 `del_flag IS NULL`，不能当删除 | 作为实名证据迁；强冲突人工裁决，不做 latest-wins |
| `identity_info` | 59,157 | 58,350 个用户；723 个用户有多行有效关系；58,182 行 `check_status=6`（学生 55,773、教师 2,409） | 迁历史组织关系/身份材料；不直接推导当前资格和权限 |
| `teacher_tmp_info` | 1,247 | 1,247 个手机号均能匹配 `sys_user`；因重复手机号产生 1,250 个 join 行 | 不新建第二批人员；只作字段补充/来源证据；`pwd` 永不迁移 |
| 组织目录 | 7 + 2,941 | `sys_org` 7、院校目录 2,941；`sys_user_org` 为 0 | 以类型化命名空间去重，不能按裸 ID/名称自动合并 |
| 行政/专业字典 | 663,157 + 852 | 行政区划语义键未发现重复，但规模大；属于参考主数据而非用户资产 | 优先采用有版本/授权的权威种子，保留 V1 code mapping |

既有受控主体解析材料把 65,369 个账号锚点分成 59,394 个强 Subject 候选、4,931 个歧义候选、1,044 个矛盾候选。这个分类可复用为预检输入，但不能越过 Human 决策直接落库。

### 3.2 赛事、报名、团队和参赛履历

| 资产 | 当前行数 | 去重后的语义 | 迁移判断 |
|---|---:|---|---|
| 赛事主轴 | 17 主赛事、17 系列、11 阶段、36 赛道、124 赛道配置、111 赛事配置 | 系列通常是可报名的一届 Activity；阶段/赛道是 composition/category/policy version | 全部迁移，先冻结 Tenant、组织者和版本语义 |
| `competition_apply_info` | 130,507 | 一行是报名成员快照，不是一张独立订单或一支独立团队 | 迁到 Registration Case + Revision + Member/Teacher Snapshot |
| `team_manager_info` | 34,084 | 团队主记录；33,211 条当前有效 | 迁到 Activity Group；撤赛/删除历史保留为生命周期证据 |
| `team_member_rela` | 129,937 | 团队当前/关系事实；不是报名提交快照的替代品 | 迁 Group Member，并与 Revision Member 对账 |
| active/withdrawn 报名案例 | 28,956 案例、109,785 成员快照 | 所有案例均有 `team_code`；按团队聚合后才是报名案例 | 这是本期 Registration 主数据集的正确粒度 |
| 晋级 | 4 配置、8,739 申请 | 是晋级决定或子活动报名，不是资格标签 | 迁为 domain decision/child registration，禁止塞回旧状态字段 |

关键关联质量：

- active/withdrawn 成员快照中 49,407 行没有 `user_id`，必须生成未认领 Subject 候选或隔离，不能按姓名自动绑定。
- 28,956 个报名团队中有 12 个找不到 active/withdrawn 团队主记录。
- 有 `user_id` 的 60,378 个成员快照中，57,467 行能匹配团队成员关系，2,911 行不能。
- 按“同团队 + 同用户/证件 + 同角色”检查，只有 1 组、2 行是真正的重复候选。按“同系列 + 同用户”得到的 3,528 组并不等于重复，因为同一个人可能参加不同团队或承担不同角色。
- `del_flag=2` 在当前业务 SQL 中表示“已退赛”，不能按普通软删除丢弃；它应迁为历史撤回/退出状态。

### 3.3 文件、材料、评审和成绩

| 资产 | 当前行数 | 事实判断 | 迁移判断 |
|---|---:|---|---|
| `file_task` | 21 | 材料任务/要求配置 | 迁为版本化 Submission Requirement |
| `file_upload_manager` | 1,989 | 当前/聚合元数据；1,888 条有效 | 迁 Artifact 聚合，但必须先取得真实文件 |
| `file_upload_record` | 6,896 | `add` 4,390、`update` 1,283、`delete` 1,222，表达文件历史而非纯重复 | 迁不可变 Artifact Version/操作历史 |
| `review_processed_relation` | 686 | 原文件到处理后文件的派生关系 | 迁 File Derivative，不能当新原件 |
| 评审任务/专家 | 686 / 1,376 | 明确的任务和范围内专家任命 | 本期不进入 V2；如有审计需要进入加密只读档案 |
| `review_record` | 689 | 683 条标记已阅，但 0 条有总分、等级、评语、推荐意见或正式提交时间 | 本期不进入 V2；不能称为评审成绩，只保留归档 disposition |
| 专家备注 | 72 | 9 位专家的备注 | 本期不进入 V2；按保留政策归档 |
| `user_grade_info` | 2 | 2 条都有分数、排名和获奖名称，但缺本次迁移所需的正式规则/发布证据 | 按用户决定暂不进入 V2；归档，后续单独重开成绩迁移 Gate |

数据库中的 8,885 条上传管理/历史记录不是 8,885 个可用文件。最终应以真实二进制的 `SHA-256 + size + MIME` 生成 `file_object`，再以业务用途和来源行生成 `artifact`/`artifact_version`。这样既去重二进制，又不丢不同用途和历史版本。

### 3.4 获奖与证书

| 资产 | 当前行数 | 去重结果 | 迁移判断 |
|---|---:|---|---|
| `award_publicity` | 1 | 只有一个公示头 | 缺独立发布状态/版本证据，需 Fact Owner 确认 |
| `award_details` | 12,940 | 12,940 个团队均能匹配团队主记录；12,936 个能按团队+奖项匹配证书历史 | 迁获奖决定候选；4 条证书链缺口隔离 |
| `user_certificate_history` | 139,061 | 139,049 个不同证书编号；12 个重复编号组/24 行，其中 8 组/16 行连人员/学校语义也相同 | 作为公开证书代码底账，重复组逐组裁决 |
| `user_certificate_origin` | 34,721 | `(证书编号, user_id)` 全部唯一；覆盖 31,748 个证书编号；所有行都能按编号匹配 history | 作为已绑定用户的收件人关系，不再创建第二份证书事实 |
| 未认领证书 | 107,301 个证书编号 | history 有记录但 origin 没有用户绑定 | 迁为可查询的未认领历史凭证，后续通过认领流程绑定 Subject |
| `certificate_image_cache` | 27,643 | 图片缓存/同步元数据 | 不作为证书事实；只用于找回正式文件，成功后按文件哈希入库 |

证书的最小无重复模型是：

```text
certificate_source_record (139049 code-level facts, pending 12 duplicate-group decisions)
  -> credential_recipient_binding (34721 unique code+user links)
  -> credential_entitlement / issue (only after award/source/Subject proof)
  -> artifact_version (official certificate binary)
```

不能把 history 139,061 和 origin 34,721 相加成 173,782 张证书。

### 3.5 订单、支付、退款和发票

| 资产 | 当前行数 | 关键质量事实 | 迁移判断 |
|---|---:|---|---|
| `order_info` | 5,459 | 5,459 个业务订单号全部唯一；5,382 条有效；4,788 条有效已支付；退款链 365 条，1 条父订单未解析 | 是财务事实源，拆成 Billing/Payment/Refund 事实迁移 |
| 已支付渠道 | 4,788 | 微信线上 3,560、支付宝线上 973、线下 243、银联 10、其他 2；缺渠道流水的 254 条中 243 条是线下 | 线下缺渠道流水可有合理解释，但要保留付款证明和审核证据 |
| `order_goods_relation` | 63,888 | 字段 `order_id` 实际关联 `order_info.id`，不是业务 `order_id`；27,992 条有效关系全部能按内部 ID 匹配 | 迁 Payable Line/Allocation；修正文档语义 |
| 有效行重复候选 | 491 组、1,088 行 | 即使包含 change_type/pay_status 后仍完全相同；不能在未对账金额/人数前直接删除 | 进入 allocation reconciliation，确认后合并并保留 source lineage |
| 已支付团队覆盖 | 27,871 个报名团队 | 25,813 个能匹配有效已支付订单，2,058 个不能 | 报名 `pay_status` 不能替代支付事实；差异必须隔离 |
| `invoice_per_info` | 661 | 656 个用户；未发现同 owner/title/tax identity 的精确重复 | 迁历史 Profile Version，不自动设为当前默认 |
| `invoice_info` | 1,803 | 1,792 条有效、1,791 条有 PDF URL；2025 年 1 条有效记录沿用旧流程并直连一条已软删除但已支付的历史订单，2026 年 1,791 条有效记录保存的是随机申请号 | 发票本身可迁；1 条旧流程历史关系可候选提交，1,791 条新流程 allocation 等待原 V1 Redis/RDB/AOF 恢复 |

专项复核确认：新流程入参包含真实 `order_info.id` 列表，但 `invoice_info.order_id` 被赋值为前端生成的 32 位随机号；真实订单列表只存于无 TTL 的 Redis List `invoice_apply_order:{randomId}`。当前 MySQL 没有关系表、外键或索引来持久化该多订单关系。曾得到的 721 条“同用户 + 同金额 + 7 天内”命中只是启发式候选，禁止自动关联。详见专项报告。

### 3.6 审批、变更与审计证据

- `sys_audit_task` 2,236 条，其中 2,165 条针对 `identity_info`；`sys_audit_task_subinfo` 1,737 条，1,266 条通过、471 条拒绝，另有 31 条孤儿子任务。
- `change_log` 7,421 条，记录团队/报名变更，应支持 Registration/Commerce 的历史解释。
- `sys_audit_log` 5,948 条和 Flowable 历史只应提取最终决定/关键证据或进入加密档案，不能在 V2 中重放成新审计事件或运行中的流程。
- 大量日志含 PII、请求参数、响应和 IP，必须先定保留期、脱敏和访问控制。

## 4. 规范数据集设计

完整字段级目录见配套 CSV。核心不变量如下：

1. **Account 与 Subject 分离**：一个人可有账号历史；一个账号锚点不能证明跨来源自然人相同。
2. **当前主数据与历史快照分离**：报名时姓名、学校、角色进入 Revision Snapshot，不覆盖 Subject/Organization 当前值。
3. **关系与资格/权限分离**：学生、教师、专家标签只证明历史 persona/关系，不自动生成 V2 资格或授权。
4. **Registration 与 Participation 分离**：报名、审核、支付、正式参赛是不同事实；`pay_status=paid` 不能单独生成 Participation。
5. **Award 与 Credential 分离**：获奖决定先于权益与发证；临时现场参赛证不是获奖证书。
6. **Order、Payment、Refund、Invoice 分离**：财务事件不可压成一个 status 字段。
7. **File Object 与 Artifact Version 分离**：二进制按哈希去重，业务用途、版本、签署/提交语义仍完整保留。
8. **所有记录可追溯**：每条目标事实必须有 `(snapshot_id, source_table, source_pk, source_fingerprint, decision_id, target_type, target_id, run_id)`。
9. **冲突不猜测**：无法唯一解析的记录进入 `HOLD/QUARANTINE`，不能为了迁移率自动合并。
10. **软删除按领域解释**：例如报名 `del_flag=2` 是已退赛；不能使用一个全局删除规则。

## 5. 明确迁移、选择性迁移与排除清单

### 5.1 必须迁入 V2 运行模型

- 账号认领锚点、Subject、经证明的身份验证证据。
- 组织目录、历史成员关系和必要的人员档案快照。
- 赛事、届次、赛道/组别/阶段组合、报名政策和材料要求版本。
- 报名案例、Revision、团队、成员、指导教师、晋级决定。
- 正式材料、作品、原文件、版本和派生文件。
- 获奖决定；评审任务、`review_record`、专家备注和 2 条成绩候选按本次决定暂不进入 V2。
- 证书代码底账、收件人绑定、权益、签发事实和正式证书文件。
- 订单、支付、线下付款证明、退款、商品/人员 allocation。
- 发票档案与 PDF；订单关联不明的先隔离。
- 迁移来源映射、决策、隔离、回执和对账指标。

### 5.2 选择性迁移或只读证据

- 审批最终决定、报名/团队/财务变更记录。
- 仍需展示的新闻、公告、内容和附件。
- 有法律或客服价值的通知送达/读取事实。
- 登录/操作/Flowable 历史中的关键决定；其余按保留策略归档。
- 行政区划、学校、专业和状态字典的版本化映射。

### 5.3 本期不进入 V2 运行模型

- `wx_sign_in_info` 133,516 条签到。
- 现场 schedule/target/credential/state/log 共 34,699 条。
- 现场资源、部署、slot、scope、reservation 共 639 条。
- 二维码 2,836 条、临时通行证、候场/材料/核验状态。
- Token、Session、验证码、缓存、证书图片缓存、临时表、`offline_v3_*`/`offline_v4_*`。
- Flowable 当前运行表、作业日志、错误日志、代码生成配置、复制表。
- 评审任务/专家任命、`review_record` 689 条、专家备注 72 条和 `user_grade_info` 2 条成绩候选；本期只生成归档 manifest，不写入 V2 Evaluation/Grade 运行表。
- V1 旧密码、永久公开文件 URL、旧菜单/角色权限的直接复制。

这些记录不进入本期运行模型，不等于可以无审批删除。先生成精确表清单、行数、主键范围、状态分布、导出校验和和保留期限，再由数据/法务 Fact Owner 决定归档或删除。

## 6. 推荐迁移波次

| Wave | 数据 | 前置条件 | 核心验收 |
|---|---|---|---|
| 0 | 权威源快照与目标基线 | V1 停写/一致性快照；V2 clean Git SHA；Schema 升级并验证 | dump SHA-256、GTID/binlog 边界、表/行 fingerprint 可重复 |
| 1 | Tenant、组织目录、参考映射 | Human 批准 Tenant/Organization 矩阵 | typed namespace 无误合并，所有依赖记录可定位组织 |
| 2 | Subject、身份、账号认领 | PII 加密/HMAC、冲突裁决流程 | 账号不串人；旧密码未迁；未认领 Subject 可后续 claim |
| 3 | Activity、赛事结构、政策版本 | organizer/tenant/版本冻结 | 17 个 series 全部有唯一 disposition 与版本映射 |
| 4A | Registration、团队、Revision、指导教师 | Subject/Activity 可用 | 28,956 案例、109,785 active/withdrawn 快照逐项对账 |
| 4B | Artifact/File 与 Commerce | 对象存储 manifest、财务来源合同 | 文件真实可下载且哈希一致；订单/退款/line allocation 平衡 |
| 5 | Finalization、Participation、晋级 | 报名审批、组织、支付共同闭环 | 不从单一状态字段推导正式参赛 |
| 6 | Award、Credential | Participation、正式材料、规则/发布版本可证明 | Award→Entitlement→Issue lineage 完整；Evaluation/Grade 本期零写入 |
| 7 | Invoice 与历史证据 | Billing/Payment、发票文件与原 V1 Redis/RDB/AOF 可用；Fact Owner 裁决 | 1,791 个有效 PDF 实体校验；恢复 `invoice_apply_order:*` 后逐条对账，未恢复的 allocation 保持 HOLD |
| 8 | 选择性内容与归档 manifest | 保留/隐私政策批准 | 现场数据零写入 V2 运行表；归档可检索且访问受控 |

## 7. 对账与验收标准

每个数据集必须同时满足：

1. `source_total = committed + quarantined + archived + rejected_with_reason`。
2. 同一 `(snapshot, source_table, source_pk)` 只有一个最终 disposition。
3. 同一规范事实键不能出现两个互相冲突的 active 目标事实。
4. 重跑不新增重复目标记录，所有写入有 idempotent receipt。
5. 金额使用 Decimal，支付、退款、allocation 和发票总额分别对账；不得用浮点或报名状态代替。
6. 文件必须实际下载、扫描、计算 SHA-256/size/MIME，并抽样打开；URL 可访问不等于文件迁移成功。
7. 证书按 code fact 和 recipient binding 分别对账；未认领证书仍保留公开历史查询能力。
8. Human 抽样必须覆盖：身份冲突、匿名报名、撤赛、退款、重复 allocation、无订单发票、重复证书编号、未认领证书。
9. 页面验收要验证用户可见的历史赛事、报名、材料、支付、发票、获奖和证书，不以 SQL 行数代替业务验收。
10. 生产迁移、服务器写入和切换仍是独立授权 Gate；本报告不构成授权。

## 8. 当前可执行的下一步

1. 先冻结一个权威 V1 快照，并重跑配套只读 SQL，生成带 SHA-256 的数据画像 manifest。
2. 冻结一条干净 V2 Git/Schema 基线；当前本地 `2.35.500` 与工作树 `2.41.000` 不一致，不能直接开跑。
3. 将配套 CSV 的 17 个数据集逐一建立 sidecar staging、source fingerprint、decision、target map、receipt 和 quarantine。
4. 先做 Wave 1–3 的持久化非生产 dry-run，再用 28,956 个报名案例做 Wave 4A 全量预演。
5. 优先向运维取得原 V1 Redis 在线只读快照、RDB 或 AOF，按 `invoice_apply_order:{invoice_info.order_id}` 恢复 2026 年发票的订单列表；严禁用 721 条启发式候选自动补关系。
6. 身份冲突、发票恢复结果、获奖发布证据和证书重复编号必须由各自 Fact Owner 批准；未批准的记录保持 `HOLD`。Evaluation/Grade 本期按用户决定为 `DEFERRED/ARCHIVE_ONLY`。

## 9. 当前状态

```text
LOCAL_V1_ASSET_INVENTORY = PASS_FOR_PREFLIGHT
CANONICAL_DATASET_SCOPE = DEFINED
ONSITE_RUNTIME_EXCLUSION = DEFINED_AS_ARCHIVE_ONLY
EVALUATION_AND_GRADE_V2_IMPORT = DEFERRED_ARCHIVE_ONLY
INVOICE_DOCUMENT_SCOPE = 1792_ACTIVE_CANDIDATES
INVOICE_ORDER_RELATION = 1_DIRECT_CANDIDATE_PLUS_1791_REDIS_RECOVERY_REQUIRED
AUTHORITATIVE_V1_SNAPSHOT = NOT_ESTABLISHED
TARGET_V2_BASELINE = NOT_FROZEN
ROW_LEVEL_HUMAN_DECISIONS = INCOMPLETE
PERSISTENT_NONPROD_MIGRATION = NOT_STARTED
AUTHORITATIVE_V1_TO_V2_COMMIT = NOT_STARTED
PRODUCTION_MIGRATION = NOT_AUTHORIZED
```
