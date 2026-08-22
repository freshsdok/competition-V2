# 赛事现场通知体系执行报告

## 1. 交付结论

本次已完成赛事现场通知的代码闭环：

- 独立通知主表与公告赛场关系表；
- Admin 端大赛公告管理；
- Admin 端绑定对象个人通知管理；
- 后端通知 CRUD、发布、停用、删除及用户可见性计算；
- 小程序“我的证件”大赛证区域展示；
- PC 用户端现场证件“大赛总证”区域展示；
- 权限 SQL；
- 富文本白名单清洗单元测试与聚合逻辑单元测试。

通知未被加入单个赛场卡片循环。当前赛事没有有效通知时，PC 和小程序均不渲染通知容器。

## 2. 实际修改文件

### 数据库

- `db/migration/20260710_competition_scene_notice.sql`
- `db/migration/20260710_competition_scene_notice_menu.sql`

### 后端

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNotice.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNoticeAccessVo.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNoticeSchedule.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNoticeQuery.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNoticeForm.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNoticeVo.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/MyCompetitionSceneNoticeVo.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneNoticeMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneNoticeService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneNoticeServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneNoticeContentCodec.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneNoticeHtmlSanitizer.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneNoticeMapper.xml`

### Admin

- `old-code-admin/src/api/tournament/sceneNotice.js`
- `old-code-admin/src/views/tournament/sceneSchedule/components/SceneNoticeManagerDialog.vue`
- `old-code-admin/src/views/tournament/sceneSchedule/index.vue`

### 小程序

- `old-code-mini/api/sceneNotice.js`
- `old-code-mini/pages/my-credential/index.vue`

### PC 用户端

- `old-code-pc/src/api/personal/index.js`
- `old-code-pc/src/views/personal/personaltabs/Competition.vue`

### 测试

- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneNoticeHtmlSanitizerTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneNoticeServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneNoticeContentCodecTest.java`

## 3. 数据库结构

### competition_scene_notice

统一存储两类消息：

- `ANNOUNCEMENT`：大赛公告；
- `PERSONAL`：个人通知。

支持三种可见范围：

- `COMPETITION`：赛事级；
- `SCHEDULE`：指定赛场级；
- `PERSON`：个人级。

表中保存赛事、接收身份快照、标题、LONGTEXT 富文本、级别、置顶、排序、发布状态、发布时间、失效时间、业务状态和逻辑删除字段。已为赛事查询、接收对象、用户、报名成员和有效时间建立索引。

### competition_scene_notice_schedule

保存赛场级公告与一个或多个 `schedule_id` 的关系，使用 `(notice_id, schedule_id)` 唯一约束防止重复绑定。

赛事级公告不写关系记录；个人通知也不写关系记录。

## 4. 接口清单

| 方法 | 路径 | 用途 | 权限 |
|---|---|---|---|
| GET | `/competition/sceneNotice/list` | 管理端分页查询 | `competition:sceneNotice:list` |
| GET | `/competition/sceneNotice/{noticeId}` | 通知详情 | `competition:sceneNotice:query` |
| POST | `/competition/sceneNotice` | 新增草稿 | `competition:sceneNotice:add` |
| PUT | `/competition/sceneNotice` | 修改通知 | `competition:sceneNotice:edit` |
| DELETE | `/competition/sceneNotice/{noticeIds}` | 逻辑删除 | `competition:sceneNotice:remove` |
| PUT | `/competition/sceneNotice/changeStatus` | 草稿/停用状态切换 | `competition:sceneNotice:publish` |
| POST | `/competition/sceneNotice/publish/{noticeId}` | 发布通知 | `competition:sceneNotice:publish` |
| GET | `/competition/sceneNotice/myList` | 当前登录人有效通知 | 登录用户 |

新增和修改使用事务。新增接口强制保存为草稿，发布只能通过具有 `publish` 权限的发布接口完成，避免通过新增/修改接口绕过发布权限。

## 5. 通知可见性计算

用户端只查询同时满足以下条件的通知：

- `publish_status = PUBLISHED`；
- `status = 0`；
- `del_flag = 0`；
- 发布时间为空或不晚于当前时间；
- 失效时间为空或晚于当前时间。

用户端查询采用两步法：先一次性查询当前用户的报名成员身份、有效证件和有效赛场绑定范围，并在 Java 中去重为 `memberIds/targetIds/seriesIds/scheduleIds`；再使用这些小集合查询通知。这样避免在每条通知上重复执行多层报名表、证件表和绑定对象相关子查询。

### 赛事级公告

当前用户在该赛事下存在有效现场证件或有效赛场绑定对象时可见。

团队证和团队绑定对象通过当前用户在 `competition_apply_info` 中的 `team_code` 关联，避免只支持个人证的情况。

### 赛场级公告

除公告有效条件外，当前用户必须在公告关系表指定的赛场中存在有效证件或有效绑定对象。

通知查询使用赛场 ID 集合和关系表 `exists`，不直接连接关系表，因此用户绑定多个赛场时同一公告仍只返回一次。

### 个人通知

按通知保存的 `user_id`、`member_id` 或仍有效的 `target_id` 匹配当前登录人。用户端接口没有接收 `targetId/userId/memberId` 查询参数，普通用户不能通过改参数读取他人通知。

## 6. 用户身份匹配

个人通知创建时，前端只提交 `targetId` 和通知内容字段。后端重新查询 `competition_scene_schedule_target`，自动写入：

- `competitionSeriesId`；
- `competitionId`；
- `userId`；
- `memberId`；
- `recipientName` 快照。

前端无法覆盖这些接收身份字段。

绑定对象没有 `userId` 且没有 `memberId` 时，Admin 端先提示并阻止打开管理器，后端也会拒绝保存。全链路不使用姓名作为通知身份匹配条件。

## 7. 富文本安全

Admin 在提交时先将富文本编码为 UTF-8 Base64，后端解码后再清洗，避免网关把完整 JSON 当作 HTML 过滤并破坏富文本属性中的转义引号。直接调用接口仍兼容原 `content` 字段。

服务端复用项目已引入的 Hutool `HTMLFilter`，配置显式白名单：

- 允许常用段落、标题、列表、表格、强调、链接和图片标签；
- `style` 仅保留居中、字号、行高、颜色、宽高、边距等安全排版属性，删除 `url()`、`expression`、定位等非白名单 CSS；
- 链接和图片协议仅允许 `http`、`https`、`mailto` 及安全相对路径；
- 禁止 `script`、`iframe`、`object`、`embed`、表单、SVG、MathML 和 style 标签；
- 不允许 `onclick`、`onerror` 等事件属性；
- 保存前清洗一次，返回管理端和用户端前再次清洗一次。

PC 端的 `v-html` 只渲染后端白名单清洗结果；小程序使用 `rich-text` 渲染同一份清洗结果。

## 8. Admin 显示与操作

- “新增赛场安排”旁已增加“大赛公告”按钮；
- 按钮打开公告管理弹框，支持筛选、列表、新增、修改、删除、发布和停用；
- 公告表单支持赛事级/赛场级范围；
- 选择赛场级时按赛事动态加载赛场并允许多选；
- 使用系统现有 `Editor` 富文本组件；
- “绑定对象”操作列调整为约 240px，顺序为“个人通知、修改、删除”；
- 个人通知管理弹框按当前 `targetId` 查询历史记录并显示接收人。

## 9. PC 与小程序显示位置

### 小程序

通知区域位于 `pages/my-credential/index.vue` 的大赛证 `.hero` 内，在二维码/“下载大赛证”之后、赛场信息区域之前。

### PC

通知区域位于 `Competition.vue` 现场证件弹框的 `.competition-credential-hero` 内，在大赛总证二维码之后、`.scene-credential-section` 之前。

两端统一按以下顺序展示：

1. 个人通知；
2. 大赛公告。

每类内部沿用服务端排序：置顶、紧急程度、排序值、发布时间、通知 ID。

两端均使用 `/competition/sceneNotice/myList`。通知接口失败时只清空通知数据，不阻断证件数据展示。

## 10. 权限

权限迁移 SQL 已增加：

- `competition:sceneNotice:list`
- `competition:sceneNotice:query`
- `competition:sceneNotice:add`
- `competition:sceneNotice:edit`
- `competition:sceneNotice:remove`
- `competition:sceneNotice:publish`

权限按钮挂在已有“现场安排配置”菜单下，SQL 使用 `NOT EXISTS`，可重复执行。

## 11. 已完成验证

### 自动化验证

- 后端模块及依赖编译成功；
- 通知专项单元测试 12 个全部通过：0 failure、0 error；
- 使用 Maven reactor 运行 competition 模块全量测试 139 个，全部通过：0 failure、0 error；
- 富文本攻击载荷验证覆盖 `script`、`iframe`、事件属性、`javascript:` 协议；
- 用户提供的带 `style`、图片 URL、空属性和中文内容的富文本完成 Base64 往返测试；
- 用户通知按赛事聚合、个人/公告分类及返回前二次清洗测试通过；
- 赛场跨赛事绑定、无稳定身份个人对象和无效时间范围的拒绝测试通过；
- Admin 生产构建成功；
- PC 生产构建成功；
- 小程序 SFC 使用 Vue 编译器完成脚本和模板语法编译；
- Mapper XML 通过 `xmllint` 语法校验；
- 相关文件通过 `git diff --check`。

### 需求测试项与实现覆盖

1. 赛事级公告：SQL 按赛事有效证件/绑定对象判断；待部署数据库后做多人联调。
2. 赛场级公告：SQL 按公告赛场关系和用户赛场身份判断；待部署数据库后联调。
3. 多赛场去重：主查询不连接关系表，使用 `exists`，每个 `notice_id` 只返回一行。
4. 单人通知：用户端只使用登录身份匹配，接口不接收目标身份参数。
5. 不同赛事隔离：所有可见性分支均带 `competition_series_id`，并按该字段聚合。
6. 草稿不显示：用户 SQL 固定 `publish_status = PUBLISHED`。
7. 停用不显示：停用写入 `DISABLED`，用户 SQL只取 `PUBLISHED`。
8. 未到发布时间不显示：SQL 校验 `publish_time <= now()`。
9. 已过失效时间不显示：SQL 校验 `expire_time > now()`。
10. 删除不显示：逻辑删除且用户 SQL 固定 `del_flag = 0`。
11. 无通知不显示面板：两端使用 `hasCurrentNotices` 条件渲染。
12. 通知失败不影响证件：两端使用 `Promise.allSettled` 分别处理。
13. 富文本 XSS：自动化单元测试已通过。
14. 越权参数：`myList` 无目标用户参数，只从登录态取 `userId`。
15. 赛场归属：保存前比较所选去重赛场数与同赛事有效赛场数，不一致即拒绝。
16. PC/小程序一致：使用相同接口、同一赛事键、相同分类顺序和服务端排序。

## 12. 部署步骤

1. 执行 `20260710_competition_scene_notice.sql`；
2. 执行 `20260710_competition_scene_notice_menu.sql`；
3. 发布 competition 后端；
4. 发布 Admin、PC 和小程序；
5. 可选：在 Nacos 网关 `security.xss.excludeUrls` 增加 `/competition/sceneNotice`；Admin 已使用 Base64 传输，不再依赖该配置，但其他直接提交原始 `content` 富文本的调用方仍建议配置；
6. 使用管理员账号完成公告/个人通知发布，用不同赛场和不同人员账号执行 16 项 UAT。

## 13. 未完成事项与风险

- 当前工作区未连接目标 MySQL、Nacos 和真实登录环境，因此未执行数据库迁移，也未完成真实账号端到端 UAT；上述 16 项已完成代码路径和静态/单元覆盖，仍需部署环境验收。
- 小程序目录没有独立 `package.json` 和可直接执行的 uni-app CLI 依赖，已完成 SFC 编译校验，仍需在项目实际 HBuilderX/uni-app 构建链路中执行微信小程序打包。
- 网关 XSS 排除路径由 Nacos 管理，仓库内没有对应环境配置文件。Admin 已通过 Base64 传输规避网关破坏 JSON；其他第三方调用方若直接发送原始 `content` 富文本，仍需配置排除路径或改用 `contentBase64`。
- SQL 可见性依赖现有 `competition_apply_info`、现场证件和赛场对象数据质量。历史纯手工对象如果没有 `user_id/member_id`，按安全策略不能接收个人通知。
