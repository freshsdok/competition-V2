# www.ksup.cn 方案2收款部署

本目录对应当前 V1 方案2和 V2 `docker/compose.prod.yaml`，不是 demo 配置。只提供部署材料，本次未登录服务器、未发布、未改数据库。路径按生产仓库现有约定编写，执行前核对实际服务、Cookie 名称和端口；如果服务器使用其他 Compose 文件，必须按实际文件调整，不能混用 prod/demo 栈。

## 1. 本次变更与连接方式

- 个人中心只保留“我的收款信息v2”，仍按获奖名单显示。
- 旧 `/personal/settlement-profile` 自动跳转 `/personal/settlement-profile-v2`。
- 保留共用名单、队伍办理锁、释放和“办理人确认完成”记录；不要禁用或删除 `/system/teamCollection` 共用接口、数据表。
- 补齐方案2代理的 `GET /api/v1/auth/session/tenants` 白名单，兼容现有 V2 收款页的租户选择。
- 本次没有修改 V2 业务代码或数据库；服务器仍须部署已实现原生收款入口的 V2 版本。

```text
浏览器 https://www.ksup.cn/personal/settlement-profile-v2
  ├─ /prod-api/system/teamCollectionNative/... → 现有 V1 Gateway / system 服务
  ├─ iframe /v2-native/native-settlement       → 专门构建的 V2 静态页面
  └─ /v2-native-api/api/v1/...                 → 本机 Node 127.0.0.1:5180
       → https://web.ksup.cn/api/v1/... → V2 Gateway → V2 platform-app → V2 数据库
```

iframe 和浏览器接口保持 www 同源；不是直接嵌入 web 的普通收款页。Node 适配器复用现有白名单、办理凭证检查和 Cookie 映射，不能用一条普通 Nginx `proxy_pass` 完全替代。

原生页面使用 `V2_NATIVE_SESSION` / `V2_NATIVE_XSRF`；代理将它们转换为 V2 服务接受的 Cookie，响应时再转回来。不能转发 V1 Bearer Token，不能共享普通 V2 Web 登录 Cookie，不能去掉 CSRF 或伪造 Origin。

## 2. 上线前先修复 HTTPS

2026-09-14 本机只读实测：`https://www.ksup.cn/` 可访问；`https://web.ksup.cn/` TLS 校验失败，原因是返回证书不匹配 `web.ksup.cn`。这是当前公网链路的阻塞项，尚不能宣称生产已连通。

在 V2 的 443 虚拟主机配置包含 `web.ksup.cn` 的有效证书和完整证书链，核对 DNS、SNI、CDN/负载均衡的实际证书。先运行以下命令，不能加 `-k`：

```bash
curl --fail --show-error --silent --output /dev/null https://web.ksup.cn/
curl --fail --show-error --silent https://web.ksup.cn/api/v1/auth/session
```

第二条应返回 JSON，匿名会话一般为 `authenticated: false`，不能是 HTML。也要从 **V1 服务器**执行，以核实它的 DNS、出站 443 和 CA 信任。

当前仓库生产模板是 HTTP 起步配置，Cookie Secure 默认为 false。下面的 override 使用 Secure=true，必须先让既有 Web/Admin 登录站点都支持 HTTPS，按原发布流程更新它们的回跳域名、`WEB_ORIGIN` 等配置，再启用该 override；否则 HTTP 用户会丢失登录会话。保留所有仍在使用的合法 Origin，不要只留下 www。

## 3. V1 Java 配置与 PC 构建

将以下环境变量注入 **实际运行 teaching-system 的 Java 进程/容器**，更新应用后重启该服务。只修改 PC `.env` 或当前 SSH shell 不会传给已运行的 Java 进程。

```dotenv
V1_TEAM_COLLECTION_ENABLED=true
V1_TEAM_COLLECTION_NATIVE_ENABLED=true
V1_V2_INTERNAL_BRIDGE_HMAC_SECRET=与V2一致的现有桥接密钥
```

如已有 Nacos 外部配置，核对最终 `v1.team-collection.enabled`、`v1.team-collection.native-enabled`，避免外部配置覆盖环境占位符。共享密钥由代码从进程环境读取，不能只写入 Nacos 文本。已有名单导入和审核结果继续使用，本次无需重复导入、清空办理状态或执行新 SQL。

在 V1 `old-code-pc` 目录按现有依赖锁文件安装依赖后构建：

```bash
npm run build
```

将该目录 `dist/` 作为 V1 PC 的新发布包，按既有备份和发布流程替换 www 的 PC 静态文件。现有 `.env.production` 保持 `VITE_APP_BASE_API=/prod-api`。`VITE_V2_NATIVE_*` 是开发代理参数，生产 Nginx 不会读取它们。

## 4. 构建并部署 iframe 静态包

在包含原生入口代码的 V2 仓库 `frontend` 目录执行：

```bash
pnpm install --frozen-lockfile
pnpm --filter @deshi/web build:native
```

产物是 `frontend/apps/web/dist-native/`。将其**内容**复制到 V1 服务器：

```text
/data/tianda/v1-native-settlement/www/v2-native/index.html
/data/tianda/v1-native-settlement/www/v2-native/assets/...
```

服务器目录可按运维规范修改，同时改 `www.locations.conf.example` 的 root。先部署新的 assets 再切换 index.html，保留上一版静态包以便回滚，避免旧浏览器加载不到哈希资源。原生包与 V2 backend 必须来自相容版本。

不要复制普通 `dist/` 代替 `dist-native/`，不要在生产运行 Vite 5177。普通 `web.ksup.cn` 的 Web 镜像继续使用原来构建方式，其 `X-Frame-Options: DENY` 不需要取消。

## 5. 在 V1 服务器运行 API 适配器

使用服务器维护的 Node LTS（已用 Node 22+ 内置 API 兼容方式编写），无需 npm 依赖。将 V1 仓库中的以下两个文件复制到 `/opt/ksup-native-proxy/`：

```text
scripts/native-collection/api-server.mjs
scripts/native-collection/proxy.mjs
```

复制本目录 `proxy.env.example` 为 `/etc/ksup-native-proxy.env`，配置：

```dotenv
V2_NATIVE_API_TARGET=https://web.ksup.cn
V2_NATIVE_SESSION_COOKIE_NAME=DESHI_PROD_SESSION
V2_NATIVE_PROXY_PORT=5180
```

`DESHI_PROD_SESSION` 来自当前 V2 `compose.prod.yaml`；若服务器实际使用其他名称，要与 **Gateway 和 platform-app** 的 `PLATFORM_AUTHENTICATION_SESSION_COOKIE_NAME` 保持一致。默认开发名称 `DESHI_SESSION` 不能照搬生产。

复制本目录 `ksup-native-proxy.service` 到 `/etc/systemd/system/`。先核对 `command -v node` 和 `id www-data`，按实际安装路径、专用低权限用户修订 unit，再执行：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now ksup-native-proxy
sudo systemctl status ksup-native-proxy --no-pager
```

更新过代理代码或 env 后执行 `sudo systemctl restart ksup-native-proxy`。5180 只监听回环地址，不对公网开放。若 Nginx 在容器中，容器的 127.0.0.1 不是宿主机，需要按实际容器网络配置私有地址；本模板针对宿主机 Nginx。

只有核实 V1/V2 在同一宿主机时，才可考虑把目标改为 `http://127.0.0.1:28082` 直连本机 V2 Gateway。不同服务器应使用有效 HTTPS 或已确认安全的专网通道，不能关闭证书校验来绕过当前错误。

## 6. V2 注入原生入口环境变量

不要修改现有认证 HMAC、密码 pepper 或已有银行卡加密密钥。另建受限环境文件，例如 `/data/tianda/prod_app/runtime-config/native-settlement.env`（chmod 600），填写真实配置：

```dotenv
V1_V2_INTERNAL_BRIDGE_HMAC_SECRET=与V1相同的现有桥接密钥
V2_NATIVE_ALLOWED_ORIGINS=https://web.ksup.cn,https://tenant.ksup.cn,https://www.ksup.cn
AUTH_LEGACY_CLAIM_ENABLED=true
PAYOUT_DATA_ENCRYPTION_KEY=现有银行卡加密密钥
PAYOUT_DATA_FINGERPRINT_KEY=现有银行卡指纹密钥
PAYOUT_DATA_KEY_VERSION=1
```

以上密钥是占位说明，不能照抄。桥接密钥至少32字符，两端必须完全一致，且系统时间应同步，否则办理凭证可能被拒绝。两个银行卡密钥必须各自为32字节或解码后32字节的 Base64；有历史加密数据时必须复用原密钥及版本，不能为本次部署重生成。版本1只是默认例子，以既有服务为准。如果使用其他 secret-handle，要保留既有配置，不能盲目切回 env。

`AUTH_LEGACY_CLAIM_ENABLED=true` 用于尚未认领的 V1 用户通过原密码登录认领。这是 V2 全局既有认领开关，并非只影响 iframe；若全部目标账号已激活且原策略关闭，应填 false。保留既有密码启用、最小长度和 provider 设置；模板不调整这些规则。手机/密码验证仍由 V2 原有流程处理。

将本目录 `compose.v2-native.override.yaml` 复制到 V2 服务器 `/data/tianda/prod_app/runtime-config/compose.v2-native.override.yaml`。在 V2 仓库 `/data/tianda/prod_app/deshi_competition_v2` 下验证：

```bash
docker compose \
  --env-file /data/tianda/prod_app/runtime-config/platform.env \
  --env-file /data/tianda/prod_app/runtime-config/native-settlement.env \
  -f docker/compose.prod.yaml \
  -f /data/tianda/prod_app/runtime-config/compose.v2-native.override.yaml \
  config --quiet
```

确认要发布的 platform-app 镜像已包含原生入口代码、既有数据库迁移处于正常状态后，按原部署流程发布该镜像；如只更新配置且镜像已经正确，可用同一组参数执行：

```bash
docker compose \
  --env-file /data/tianda/prod_app/runtime-config/platform.env \
  --env-file /data/tianda/prod_app/runtime-config/native-settlement.env \
  -f docker/compose.prod.yaml \
  -f /data/tianda/prod_app/runtime-config/compose.v2-native.override.yaml \
  up -d --no-deps --force-recreate platform-app
```

后续所有生产发布也必须包含这两个 env 文件和 override，避免下次发布丢配置。`.env` 不会自动把全部变量注入容器，故提供显式 `environment` override；`restart` 不会应用新的容器环境变量。参见 [Compose 变量注入](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/) 和 [restart 限制](https://docs.docker.com/reference/cli/docker/compose/restart/)。避免把完整 `compose config` 或 `docker inspect` 的秘密输出到工单。

## 7. Nginx 配置

1. www 的现有 HTTPS `server` 引入本目录 `www.locations.conf.example` 内容；保留现有 V1 root、`/prod-api/`、其他业务代理。
2. web 的现有 HTTPS `server` 合并 `web-api.location.conf.example`；不要重复定义 `/api/`。28082 是当前生产 Gateway 默认宿主机端口，若已调整则同步修改。
3. 保留 www 原页面 CSP 所需资源规则，并允许 `frame-src 'self'`、`connect-src 'self'`。原生静态页不能被额外的 `X-Frame-Options: DENY` 或 `frame-ancestors 'none'` 拦截。核对 CDN/WAF 也没有覆盖响应头。
4. 模板里的 `add_header` 会改变 Nginx 父级响应头继承；合并时补回站点已有 HSTS 等必要头，不能整体替换现有安全配置。
5. 两端 API 均关闭请求/响应缓冲和缓存，避免银行卡请求体写入 Nginx 临时文件；排查 WAF/APM 和应用日志同样不采集银行卡、Cookie、密码。依据 [Nginx 请求缓冲文档](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_request_buffering)。V1 不保存银行卡到数据库，但转发链路仍会处理请求字节。

两台服务器分别先执行 `sudo nginx -t`，通过后 `sudo systemctl reload nginx`。若已有全局鉴权、限流或 WAF，请保留并验证兼容性；不应要求 `/v2-native-api/` 带 V1 Bearer Token，它使用独立 V2 会话和办理凭证。

## 8. 验收与回退

本次本地验证：V1前端20项、代理6项测试通过（含生产 Cookie 名称及 Origin 透传）；V1生产构建、V2原生前缀构建和类型检查通过；两个 Nginx 片段在隔离 Nginx 1.27 容器内通过 `nginx -t`；production Compose 与 override 使用合成配置通过 `config --quiet`。这些验证不代表真实服务器配置已生效或真实银行卡流程已验收。

先做不写业务数据的检查：

```bash
curl --fail --show-error --silent --output /dev/null https://www.ksup.cn/v2-native/native-settlement
curl --fail --show-error --silent https://www.ksup.cn/v2-native-api/api/v1/auth/session
curl --show-error --silent --output /dev/null --write-out '%{http_code}\n' https://www.ksup.cn/v2-native-api/api/v1/native-settlement/context
curl --show-error --silent --output /dev/null --write-out '%{http_code}\n' https://www.ksup.cn/v2-native-api/api/v1/settlement-profiles/admin
```

预期：静态页200；session为匿名 JSON；后两项403。还需检查 HTML 中脚本以 `/v2-native/assets/` 开头且请求返回 JS，不能200却返回 V1首页、404 HTML或开发服务器源码。

浏览器使用获奖名单中的正式队员账号：

- 个人中心只显示“我的收款信息v2”；旧地址自动跳转。非名单账号无入口且后端拒绝办理。
- 点击开始或继续填报，iframe 成功加载；未登录则显示 V2 登录。可验证登录及退出，不必为了连通性测试创建真实银行卡资料。
- 登录后账号一致性校验、租户列表、原有安全验证正常；Network 中银行请求始终走 www `/v2-native-api/`。
- Cookie 为 `V2_NATIVE_*`、Secure、无父域 Domain；会话 Cookie 为 HttpOnly；普通 web V2 登录不被替换。
- 正式业务验收再检查同队互斥、失败释放、办理人手动确认以及真实保存结果；基础连通测试不能代替这些验收。

故障定位：iframe 空白优先看静态资源路径/CSP；代理502优先查 TLS、V2目标、代理服务；登录后反复匿名查生产 Cookie 名称/Secure；403查 Origin、办理凭证和账号对应关系；租户列表403检查是否部署了最新版 proxy.mjs；银行卡加密503检查原有密钥注入。

回退时恢复前端、代理、Nginx及容器配置的上一版备份，重建对应服务；不清理名单、办理状态或 V2银行卡数据，不轮换秘密。若仅临时停用新入口，可在 V1 将 native-enabled 关闭并重启 system；共享 team-collection 总开关和旧桥接其他用途保持原策略。
