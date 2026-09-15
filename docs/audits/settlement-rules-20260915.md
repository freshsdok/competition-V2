# V1 到 V2 银行卡资料收集规则核对清单

核对日期：2026-09-15。范围：V1 `/personal/settlement-profile-v2` → `/v2-native/native-settlement` → V2 本人收款资料接口。

依据两项目当前工作区源码，包括未提交修改。V1 HEAD 为 `3260e30a11b328a0330ba75ba7d99f3f577c472a`，V2 HEAD 为 `74fb7a4852d3f31246ebcf39f9375cd83c822758`；HEAD 本身不代表全部工作区实现。此次只读核对代码并整理文档，没有修改业务代码、运行配置或数据库，没有重跑浏览器测试。此前实测结果另见 [执行报告](settlement-e2e-20260914/execution-report.md)。

这里的“后端强制”指受测入口对应服务端代码具备该约束，不代表本次已完成所有绕过、并发或故障测试。“前端限制”不能独自当作服务端强制保证。10 分钟、8 小时等标注为代码默认值的参数，部署可以覆盖，本次未读取进程实际生效配置。

## 一 账号与办理资格

| 编号 | 当前规则 | 强制位置与需求含义 |
|---|---|---|
| A01 | 必须先登录 V1，收款功能及新版入口开关开启。 | V1 后端。功能关闭时不返回可办理列表或拒绝签发入口。 |
| A02 | 团队和学生都必须进入已启用的收款名单，且 V1 用户未删除、状态正常。 | V1 后端读取 `v1_team_collection` 和 `v1_team_collection_member`；不是每次打开页面实时从获奖证书重新计算资格。有获奖证书不自动等于已经进入填报名单。 |
| A03 | iframe 必须有 V2 正式登录会话；没有会话时进入 V2 登录。已有正确会话可以直接使用。 | 不强制每次重新打开 iframe 都输入一次密码。V1 登录不会自动替用户建立这个正式 V2 会话。 |
| A04 | V2 登录账号必须是 V1 当前办理人通过迁移谱系对应的 Account 和 Subject，两者都须匹配。 | V2 后端。判断依据是稳定映射，不是用户名字符串、昵称或手机号相同。另注册一个同名账号不能替代原账号。 |
| A05 | 映射须唯一且可用：对应账户 ACTIVE、唯一账户主体关系、没有未完成认领记录、没有待处理的同人争议等。 | V2 后端 `READY` 检查。单纯知道密码不保证可以进入本人填报页。 |
| A06 | 未认领原账号可通过“原账号 / 密码”完成正式认领后登录。 | 必须满足既有认领政策及部署开关；修改 pending 密码不等于完成认领，也不赋予团队资格。认领后的密码以 V2 当前密码为准，并非永久与 V1 密码同步。 |
| A07 | 用错 V2 账号时不显示本人填报表单，可退出当前账号再登录；后续资料请求也核对入口身份。 | 原生代理要求入口令牌，V2 Guard 再校验。其他窗口切换了原生会话，后续请求不能因此改为替另一个账号办理。 |

依据：[V1 名单与办理服务](../../old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/TeamCollectionService.java)、[新版签发服务](../../old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/TeamCollectionNativeService.java)、[V2 身份匹配](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-app/src/main/java/cn/deshi/platform/nativesettlement/NativeSettlementIdentity.java:36)、[迁移可用性检查](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-core/src/main/java/cn/deshi/platform/core/infrastructure/persistence/JdbcV1MigrationBootstrap.java:28)。

## 二 表单字段是否必须与 V1 一致

| 编号与字段 | 必填与格式 | 是否与 V1 或其他字段核对 |
|---|---|---|
| B01 收款人姓名 | 必填，去首尾空白后最长 80 字符。 | **没有与 V1 姓名或 V2 实名姓名做一致性比较，也没有与身份证对应姓名做联合核验。** |
| B02 收款联系手机号 | 必填，7—32 字符；仅允许数字、加号、ASCII 空格、连字符。 | **没有要求等于 V1 手机号、V2 绑定手机号或验证码手机号；保存此字段不发验证码。** 不限于中国大陆 11 位手机号规则。 |
| B03 身份号码 | 必填，6—32 字符；格式允许英文字母、数字、括号、ASCII 空格及连字符；另做本人身份相容检查。 | 读取当前 V2 Subject 的有效 VERIFIED 身份证据。若有证据，任一证据不相容或适配器不能安全核对即拒绝。**如果没有有效证据，此相容检查直接通过，不强制先完成实名。** |
| B04 开户省份、城市 | 两项必填，各最长 80 字符。 | 自由文本；没有行政区级联或省市归属验证。 |
| B05 银行名称、开户支行 | 两项必填，分别最长 160、240 字符。 | 自由文本；没有银行支行目录匹配或支行真实性验证。 |
| B06 银行账号或卡号 | 必填，8—64 字符；允许数字、ASCII 空格和连字符。保护存储前去掉 ASCII 空格。 | **没有银行卡四要素核验、持卡人归属核验、银行真实可用性验证，或不同账号/团队之间银行卡去重。** |
| B07 CNAPS 联行号 | 选填；非空时去首尾空白后必须为 12 位数字。 | 没有核对联行号是否真实存在、是否属于所填银行及支行。 |
| B08 资料用途 | 姓名、电话、证件号作为结算资料保存。 | 不回写 V1 用户，不修改 V2 登录手机号、联系渠道或实名证据。安全验证不会自动修正这些字段。 |

身份证规则需要特别注意：这里不是实时查询 V1 `auth_info` 后比较。V1 历史实名若已经迁移为 V2 有效证据，按迁移证据的指纹匹配；V1 后来变更的证件不一定已同步到 V2。历史匹配去除 ASCII 空格、回车、换行和 Tab，但保留字母大小写。多条有效证据也不是“匹配任意一条就通过”，当前代码在任一不匹配时拒绝。号码通过相容检查也不等于重新调用权威实名服务验证姓名与号码。

因此，“请填本人姓名和本人银行卡”目前包含用户陈述要求，不能理解成系统已证明姓名、证件、手机、银行卡四者一致。此前模拟银行资料能保存，亦不能作为验卡成功证据。

依据：[格式策略](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/domain/PayoutPolicy.java:28)、[保存服务](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/application/SettlementProfileService.java:161)、[身份相容检查](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-core/src/main/java/cn/deshi/platform/core/application/SubjectNaturalIdentityService.java:81)、[V1 历史证据匹配](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-core/src/main/java/cn/deshi/platform/core/application/V1HistoricalIdentityEvidence.java)。

## 三 安全弹框的触发与验证方式

| 编号 | 当前规则 | 用户实际遇到的表现 |
|---|---|---|
| C01 | 保存新版本、确认提供、撤销后续访问均要求正式登录、认证足够新近且会话达到 STRONG。 | 三种写操作都有可能要求安全验证；查看本人脱敏摘要、读取单位目录不因认证时间较旧而必然弹框。 |
| C02 | 页面先发用户提交的操作；仅捕获 `403` 且 `code=STEP_UP_REQUIRED` 才打开安全弹框。 | 普通无权限、入口过期、身份不符、表单错误不会都变成安全弹框。账号会话失效需要重新登录。 |
| C03 | 普通密码、短信、邮箱登录创建 BASIC 会话；再次认证可提升到 STRONG。 | 即使刚在 iframe 登录，也可能在第一次保存时再验证一次。已有新近 STRONG 会话则可能直接保存/提供，不重复弹框。 |
| C04 | 认证新近窗口 `stepUpTtl` 代码默认 10 分钟，从最近认证时间算；普通操作不会刷新这个认证时间。 | 例如安全验证后立即保存再提供，通常复用这次验证；超过新近窗口后再写入会重新要求验证。该 10 分钟不是入口的 30 分钟。 |
| C05 | 弹框调用当前 V2 账号的安全中心，只显示 `ACTIVE` 验证因子。 | 有 ACTIVE PHONE_OTP 就能显示手机验证码；有 ACTIVE PASSWORD 就显示当前密码；有 ACTIVE EMAIL_OTP 还能显示邮箱验证码。 |
| C06 | 多种因子时可自行选择一种；默认优先顺序为手机、密码、邮箱。 | **有手机的账号仍可以选密码。并未实现“有手机必须短信，只有没手机才允许密码”的强制规则。** |
| C07 | 只有一种可用因子时不显示方法切换按钮；没有可用因子则提示先去安全中心完成绑定。 | 只有密码不是因为收款表单手机号留空，而是当前账号没有可用手机/邮箱验证因子。V1 有手机号、用户名看起来像手机号、V2 有联系手机号，都不能替代 ACTIVE PHONE_OTP。 |
| C08 | 手机验证码完成时，服务端要求被验证手机号实际属于当前 V2 Account；邮箱同理。密码须为该账号当前有效密码。 | 不是任意能收到验证码的号码都可以验证；收款联系手机号也不会自动成为安全手机号。这里验证账号控制权，不核验银行卡。 |
| C09 | 三种再次认证方式均可单独产生 STRONG 会话，并轮换当前 Session。 | **不是密码 AND 短信双因素组合。** 轮换不改变用户权限或实名资料。 |
| C10 | 验证成功只自动续办刚才那一次操作；若重试仍返回 STEP_UP_REQUIRED，则停止并显示提示。 | 不无限弹框、不无限循环提交。保存、提供或撤销仍可能在后续业务校验处被拒绝。 |
| C11 | 取消、关闭、点遮罩或 Escape 关闭弹框，取消本次待续操作；当前表单草稿保留。错误验证清空密码和验证码输入。 | 草稿仅存当前页面；整页刷新、iframe 卸载、入口失效导致表单卸载后不保证保存草稿。成功保存后页面清空明文表单。 |

短信 Challenge 代码默认有效 5 分钟、最多 5 次验证码尝试，短信请求默认每分钟 3 次、登录/再次认证限流还受认证服务和 Provider 控制；这些是部署默认参数，不是本次确认过的线上固定额度。方法能显示不保证短信服务已配置、发送成功或用户实际收码；此前短信实测缺口仍然存在。

依据：[弹框与自动续办](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/frontend/apps/web/src/features/commerce/SettlementProfileView.vue:186)、[验证方式选择](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/frontend/apps/web/src/features/commerce/SensitiveActionVerification.vue:47)、[安全中心因子来源](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/authentication/src/main/java/cn/deshi/platform/authentication/application/AuthenticationSecurityService.java:65)、[默认时间参数](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/authentication/src/main/java/cn/deshi/platform/authentication/infrastructure/config/AuthenticationProperties.java:13)、[新近认证检查](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/authentication/src/main/java/cn/deshi/platform/authentication/application/AuthenticationApplicationService.java:314)、[密码再次认证](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/authentication/src/main/java/cn/deshi/platform/authentication/application/PasswordAuthenticationService.java:200)。

## 四 30 分钟到底限制什么

| 编号 | 当前规则 | 边界 |
|---|---|---|
| D01 | 每次 V1 成功签发填报入口时，产生固定 1800 秒有效期。 | 从签发开始，不从保存、首次键入或最后一次操作开始；持续填写或完成安全验证不会延长同一个入口。 |
| D02 | 入口证明包含 V1 来源用户与签发、过期时间，不负责建立 V2 登录。 | 入口有效期、V2 Session 有效期、安全认证新近窗口是三个独立概念。 |
| D03 | 服务端对携带该入口的资料请求核对过期时间；到期的后续请求拒绝。 | 不存在必须等前端倒计时结束才拒绝的规则；也不能承诺在到期前已开始的在途操作会被自动撤销。 |
| D04 | iframe 通常每 15 秒核对登录和入口；核对失败不再显示填写组件并使完成资格失效。 | 不是 30 分钟整点强制关闭浏览器窗口。网络延迟、后台标签页定时器节流可能使界面提示稍后出现；服务端仍按请求时校验。 |
| D05 | 到期不自动退出 V1 或注销 V2 Session，不删除已保存资料、不撤销单位访问，不释放团队办理名额。 | 普通 V2 Session 默认 8 小时，代码支持部署覆盖；安全验证轮换 Session 会创建新的 Session 生命周期，但不会延长原入口。 |
| D06 | 返回 V1“继续填报”或“重新进入填报”重新核对办理权并签发新入口。 | V2 正式会话仍有效且账号一致时不一定重新登录；新页面需重新选择接收单位并核对已提供状态。 |
| D07 | iframe 接收入口后清除地址 hash，令牌只存内存。 | 直接刷新 iframe 或直接打开无入口的 V2 原生页，可能未到 30 分钟也要求从 V1 重新进入；刷新不是自动续期机制。 |

时间例子：10:00 签发 → 10:03 安全验证 → 默认新近验证约到 10:13，但入口仍固定到 10:30。10:20 再次安全验证后，原入口仍于 10:30 到期。之后由仍有办理权的用户从 V1 重新进入，才得到新入口。10 分钟/8 小时示例以默认配置为前提。

依据：[V1 1800 秒签发](../../old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/integration/nativecollection/NativeCollectionIntent.java:22)、[V2 有效期检查](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/platform-app/src/main/java/cn/deshi/platform/nativesettlement/NativeSettlementIdentity.java:43)、[iframe 生命周期](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/frontend/apps/web/src/features/commerce/NativeSettlementView.vue:37)、[内存令牌](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/frontend/apps/web/src/services/native-settlement.ts:5)、[Session 轮换](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/authentication/src/main/java/cn/deshi/platform/authentication/infrastructure/redis/RedisAuthenticationStore.java:352)。

## 五 同队填写和最终确认

| 编号 | 当前规则 | 需求含义 |
|---|---|---|
| E01 | 团队初始 AVAILABLE；第一个成功点击申请办理的人将团队变为 IN_PROGRESS，并成为办理人。 | **锁定点在申请成功，不是第一个保存成功或第一个提交完整资料。** |
| E02 | 后端使用团队行锁、资格校验、状态和版本条件更新。 | 代码目标是同一时刻只有一名办理人；其他队员不能通过本队入口签发新填报链接、完成或释放名额。当前并发争抢实测仍未完成。 |
| E03 | 办理中其他队员可见办理人、开始时间和办理中状态。 | 不从这个页面获得办理人的银行卡、证件和电话。 |
| E04 | 关闭页面、掉线、刷新、长期不填写或入口到期都不自动释放名额。 | 没有按占用超时自动换人的机制；放弃前，其他成员会一直被阻止。 |
| E05 | 当前办理人可主动“放弃本次办理，释放名额”，需确认弹框；取消放弃不改变状态。 | 释放后团队回 AVAILABLE，其他合格队员可以申请；旧办理人的已保存资料和已提供权限仍保留。 |
| E06 | 释放、完成或团队版本变化后，旧版本不能再次签发、确认或释放。 | 已签发的 V2 入口没有与团队状态实时吊销联动：前端关闭 iframe 不等于令牌已被吊销；保留的旧入口在自身到期前可能仍用于原用户的 V2 本人资料操作。 |
| E07 | 保存资料、向单位确认提供、顶部“我已完成填报”是三个独立步骤。 | 保存并不会提供；提供并不会自动完成团队。Tour 只是引导，关闭 Tour 不算完成。 |
| E08 | 前端收到“已提供”消息后，独立读取 V2 本人资料与提供版本，再读取 V1 团队当前办理权，才能开启最终按钮。 | 消息本身不能证明完成；来源窗口、同源、随机通道和版本均需匹配。 |
| E09 | 点击最终按钮时前端再次做上述核对，再调用 V1 confirm。 | 资料读取设置 15 秒整体超时；失败不应直接确认为完成。后台 focus/轮询对已验证状态保留可点击，但真正确认仍独立校验。 |
| E10 | V1 confirm 后端核对名单、当前办理人、IN_PROGRESS 和版本，写入 USER_CONFIRMED_COMPLETE、完成时间和事件。 | **后端本身没有向 V2 强制核验资料已提供，也没有保存不可伪造的 V2 提供回执。** 前端检查与 V1 写入不是跨系统原子事务。 |
| E11 | 最终完成后本队入口关闭，所有队员看到完成回执；普通用户页面没有撤销完成/重新开放功能。 | 此结果是办理人的填报完成声明，不是资料审核通过、银行卡验证成功或奖金到账。 |

依据：[V1 团队状态服务](../../old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/TeamCollectionService.java:53)、[最终页面动作](../../old-code-pc/src/views/personal/TeamCollectionNative.vue:209)、[独立资料读回](../../old-code-pc/src/utils/nativeCollectionProgress.js:45)。

## 六 单位选择、资料更新和使用权限

| 编号 | 当前规则 | 需求含义 |
|---|---|---|
| F01 | 接收单位目录返回所有有效单位，不要求本人已加入该单位、有单位角色或已有付款项。 | **没有按 V1 团队/大赛强制锁定 XINKESAI 或唯一指定单位。** 用户需要人工选对。 |
| F02 | 页面选择单位不会自动提供，首次提供须明确勾选同意；切换单位清空勾选。 | 提交时后端核对单位存在且 ACTIVE、本人有已保存资料，并满足安全验证和访问版本条件。API以 `provided=true` 表达提供操作，不另传独立的复选框勾选记录或同意文案版本。 |
| F03 | 已经向该单位提供过的，重新进入再选该单位，读回成功即可恢复待最终确认。 | 不需要对同一有效提供关系重复授权；但不会在新页面自动替用户选单位。 |
| F04 | 可先后提供给多个单位；给新单位提供不会自动撤销旧单位。 | 顶部最终检查确认所选单位 PROVIDED，但没有核对它就是本队的大赛单位。 |
| F05 | 资料属于 V2 的个人 Subject，有一份当前资料指针及历史版本。提供关系属于“个人 Subject＋单位”。 | **不是每支 V1 团队独立一份银行卡，也没有在这条提供关系中绑定 V1 团队、奖项或批次。** 同一人办理不同团队可能使用同一份资料。 |
| F06 | 修改资料需重新填写完整表单，新建版本；旧版不直接覆盖删除。 | 已提供的单位关系不会因更新自动撤销；该单位财务可读取后续最新资料。给另一比赛改卡也可能影响此前仍获提供单位看到的当前资料。 |
| F07 | 并发保存、提供、撤销均带版本；不匹配则提示资料已变化。部分同操作重试可幂等返回。 | 不无条件用旧页面覆盖新版本；页面“更新已保存状态”保留当前草稿供重新核对，不自动再次提交。 |
| F08 | 撤销后状态 REVOKED，该单位不再从该提供关系获取当前资料；停用单位的已有关系也允许撤销。 | 不删除本人资料，不追回已经合法导出的文件，不改写历史结算快照；重新提供仍需用户明确操作。 |
| F09 | 财务访问需同时满足对应单位权限 `tenant.payout.finance.review` 和该个人对该单位仍为 PROVIDED。 | 仅看见单位名称或仅是管理员不等于能看全部银行卡。明文查看/导出要求新近 STRONG 并记录审计；列表要求新近正式会话。导出一次最多明确选定 100 份。 |
| F10 | V1 不保存银行卡正文；V2 敏感姓名、电话、证件、卡号经保护存储，普通本人视图返回脱敏摘要。 | 完整收款信息不是通过父页消息传给 V1；银行/支行名称、地区、CNAPS 等并不全部脱敏。 |
| F11 | 最终关闭团队入口不会冻结或撤销 V2 本人资料与单位提供关系。 | 有正常 V2 本人收款入口时仍可维护资料；团队完成与后续付款项快照、发奖处理是不同事实。 |

依据：[独立收款服务](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/application/SettlementProfileService.java:238)、[存储所有权及版本](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/infrastructure/persistence/JdbcSettlementProfileStore.java:21)、[接口输入与返回](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/backend/commerce-finance/src/main/java/cn/deshi/platform/commerce/payout/api/SettlementProfileController.java)、[单位选择与勾选](/Users/wwang/Documents/ClaudeCode/deshi_competition_v2/frontend/apps/web/src/features/commerce/SettlementProfileView.vue:60)。

## 七 结合需求最需要作决定的事项

下表为从上述代码事实推导的需求差异，不是已修改的实现方案。

| 需要决定的真实要求 | 当前实现能否保证 | 若要求严格成立，需要补的约束 |
|---|---|---|
| iframe 必须是 V1 办理人同一个已迁移账号 | 有后端映射检查 | 确认历史迁移、认领和同人争议处理流程足够可用。 |
| 有手机必须短信，没手机才密码兜底 | 不能；有手机也可选密码，还可能有邮箱方式 | 定义必须采用的认证方式，并由后端按方式校验，而不只隐藏前端按钮。 |
| 每次办理都要重新登录并再次验证 | 不能；有效正式会话与新近 STRONG 可复用 | 明确“每次入口、每次保存、每次提供”哪一级必须新验证，以及可接受的窗口。 |
| 必须已实名，且姓名、证件与 V1 完全一致 | 不能；姓名不比对，无有效实名证据时不强制阻止 | 定义权威实名来源、V1/V2差异处理，以及缺少证据时是否阻止。 |
| 收款电话必须是本人已验证的手机号 | 不能；收款联系手机号独立自由填写 | 区分联系号码与登录因子；如需一致，明确比对对象或增加该联系号码验证。 |
| 银行卡必须真实、有效且属于本人 | 不能；目前格式与用户陈述为主 | 需要相应银行卡归属/真实性核验能力和失败处理。 |
| 资料只能提供给本队指定大赛 | 不能；目录全部有效单位，最终未绑定大赛单位 | 服务端绑定大赛/团队与接收单位，提供及最终确认都核验。 |
| 每队永久只收一人的一份固定银行卡 | 只能保证当前团队入口单人占用 | 明确放弃接手、旧入口吊销、旧授权处理；需要团队/批次与最终资料版本的固定关系。 |
| 不填或 30 分钟后自动释放给队友 | 不能；入口到期不释放名额 | 定义占用期限、提示、续期与释放后的旧入口失效处理。 |
| 点击完成必须由服务端证明已正确提供，且锁定最终版本 | 不能；V1当前是人工声明，前端双读回并非原子事务 | 增加服务端可信回执/校验与资料版本绑定，并定义跨系统失败和重试。 |

本清单不把当前代码存在的限制全部视为缺陷。例如联系手机号允许与登录手机号不同、提供关系包含后续更新、有效会话可以复用，都可以是合理需求；但必须与大赛真实业务规则一致。当前最需要避免的误判是：把“账号本人一致”当作“姓名手机银行卡全部一致”，把“入口到期”当作“退出及释放”，以及把“前端已检查”当作“V1后端已强制核验”。
