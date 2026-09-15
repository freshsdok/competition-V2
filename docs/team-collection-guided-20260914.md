# 银行卡资料填报：引导式页面接入

2026-09-14，依据用户已确认的 [HTML 原型](prototypes/settlement-guided-20260914/bank-collection.html) 实施。入口为 `http://localhost:8082/personal/settlement-profile-v2`。

## 页面行为

- 外层保留“填写银行卡 → 确认提供 → 完成填报”三个阶段，仅展示当前打开队伍的进度。
- 将长说明收进“填报须知”。详细步骤仍用较大、加粗文字；页面统一使用“单位”。
- 本人信息与银行卡信息分组，CNAPS 收进选填区。点击“保存并继续”保存；接口明确返回 403 / `STEP_UP_REQUIRED` 才展示既有安全验证弹框。
- 验证成功仅继续刚才的操作一次；取消保留当前草稿。账号未绑定手机号时可使用已有密码验证；“收款联系手机号”仍按现有接口必填，不等于账号绑定手机号。
- 保存后展示脱敏摘要、接收单位与明确同意复选框。不会自动选择单位或替用户同意；改变单位会清除同意。
- 提供成功后核对真实状态，再自动高亮上方“我已完成填报”。引导可收起、可从填报窗口重新打开，收起不代表完成。
- 上方按钮本身作为最终人工确认，不再弹第二次确认框；以 V1 最新状态读回结果展示完成页；响应丢失时仍可据此恢复真实完成状态。放弃办理继续保留确认框。
- 修改资料、更新状态、重新加载单位、撤销提供等原功能仍可从“更多操作”使用。
- 窗口按内容高度调整；桌面队伍摘要吸顶，填写操作/安全验证期间取消吸顶以免遮挡弹框。手机宽度采用普通布局。

## 联动与核验

新增的消息仅表示界面进度，不能单独启用完成按钮：

1. 每次 iframe 文档加载创建独立随机 channel；父子两端校验精确同源 origin、当前窗口 source、协议版本与 channel。
2. 原生页每次挂载发送 ready，使“登录 → 返回填报”的 SPA 导航也能握手。消息只传 phase、profileVersion、tenantId、accessVersion 和界面高度，不传账号、银行卡、密码或入口 token。
3. 父页用现有原生代理 GET `/v2-native-api/api/v1/settlement-profiles/me` 独立核对最新资料版本、选定单位唯一 PROVIDED 记录及其版本。请求带本次原生 intent，由既有 V2 Guard 核对登录账号/Subject/来源绑定与入口有效期；不使用 V1 axios Authorization。
4. 同时重新读取 V1 团队的当前办理人和 version。用户点击最终确认时再次执行两项核验，随后调用原 V1 完成接口。
5. 编辑、单位改变、请求失败、窗口重开、身份检查失败及队伍变化均使在途旧核验失效。核验失败保留可用表单，用户可刷新状态重试。本人资料 GET 整体限时15秒，包含 JSON 读取；超时中止并回到重试状态。
6. 重新进入时不自动选择单位。用户明确选择既有 PROVIDED 单位后，可核验并继续最后一步，无需重复授予同一权限。

此接入没有新增后端接口、数据库迁移或生产写入。V1 完成仍是“办理人确认完成”；跨 V1/V2 的读取与最终确认不是一个事务，也不表示银行验卡、已发奖或到账。既有服务端 confirm API 仍为人工声明接口，前端核验不构成新的服务端强制提交约束。

队伍锁仍控制办理入口。放弃或完成不会立即吊销已签发的30分钟 intent，也不会撤销 V2 Session 或删除已提供资料；当前页面关闭不等于服务端撤销。这些既有边界未改变。

## 本轮验证

- V1：`cd old-code-pc && ./node_modules/.bin/vitest run --config vitest.team-collection.config.js`，5文件43项通过。
- V2：`cd frontend && pnpm --filter @deshi/web exec vitest run src/features/commerce/SettlementProfileView.test.ts src/features/commerce/SettlementProfileView.verification.test.ts src/features/commerce/SensitiveActionVerification.test.ts src/features/commerce/NativeSettlementView.test.ts src/services/native-settlement.test.ts src/services/native-settlement-progress.test.ts`，6文件41项通过。
- V1 `npm run build`、V2 `pnpm --filter @deshi/web build:native` 通过，后者包含 vue-tsc 类型检查。保留现有大包警告；V1 另有 Browserslist 数据较旧提示。
- V2 修改文件 ESLint 和两仓库 `git diff --check` 通过。
- CUA 浏览器操作实际 Vue 组件的独立合成环境：填写、接口要求验证、短信演示、保存转单位选择、明确同意、提供、跨 iframe 校验、自动漫游、收起/再打开、最终人工确认完成。另检查320宽度密码验证弹框与取消保留草稿。桌面、390/844、320/640尺寸的关键页面均目视检查。

合成环境位于 `/tmp/deshi-settlement-visual-1hi2ho9q`，使用当前 V1/V2 组件和真实前端联动工具；账号检查、业务数据与短信响应为 fixtures，仅由本地模拟服务器返回。不连接真实后台，不构成真实认证、短信 Provider、数据库或完整业务 E2E 验收。真实8082页面当前处于登录弹框；本轮没有输入真实凭证、发送短信或提交收款资料。

V1 原有未提交改动保留；V2 本轮起始 HEAD 为 `74fb7a48`，工作区当时干净。本轮没有提交、推送或部署。
