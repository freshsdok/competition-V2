# V1 获奖学生队伍办理与人工完成确认

## 范围与证据边界

仅修改 V1（`old-code/teaching-modules/teaching-system`、`old-code-pc`、V1 SQL）。V2 页面、接口、数据库及用户身份/短信验证流程保持原状。

学生来自 Excel 三张名单页的“姓名N”列。教师列不导入。当前文件 SHA-256 为 `967559dfddf2be3b70ac513b9e0074835ddbccec441fe29e626bf98a2740e9ec`，共 520 队、1216 个学生姓名单元格；姓名单元格数不等于已匹配账号人数。名单最终资格必须以当前数据库的匹配审核结果为准。

本功能保证 V1 同队只有一个有效办理持有人；不保证 V2 数据库只发生一次保存。`USER_CONFIRMED_COMPLETE` 是办理人的人工声明，不是 V2 保存回执或银行核验结果。释放或确认只能关闭本轮 V1 外壳，不能吊销旧 V2 会话。V2 中已打开的旧标签页仍可能保存/修改资料，这是用户接受的原页面复用边界。

## 业务行为

1. 仅经审核、已启用的学生账号可以在个人中心看到“我的收款信息”，并查询自己名单内队伍。
2. 打开队伍列表不会抢占。点击申请后，V1 数据库锁定队伍行，再以状态/版本条件更新取得办理名额。
3. 当前办理人可以继续进入原 V2 iframe；同队学生可查看办理人姓名、开始办理时间，不能取得新入口。
4. 办理人选择“我已完成填报”并再次确认后，V1 记录姓名及确认时间，同队成员可查看人工确认结果。
5. 办理人明确选择“放弃本次办理，释放名额”后，其他学生可以申请。V1 不删除 V2 已保存资料。
6. 刷新、关闭页面、网络异常、桥接异常均不自动释放。原办理人重新打开列表可继续。
7. 每次状态变更递增版本。旧页面的确认/释放/新入口请求不能影响后续办理。紧邻同一命令的重试幂等。
8. 查询、申请、完成、释放、原桥接入口均在后端校验；身份使用 Token 解析后的 LoginUser，不信任单独的用户编号请求头。
9. 时间以 UTC 保存、ISO UTC 返回，前端显示北京时间。状态列表只返回队伍/办理元数据，没有银行、证件、联系方式字段。

## 文件与数据模型

- `db/migration/20260913_team_collection.sql`：两张核心业务表 `v1_team_collection`、`v1_team_collection_member`，以及操作事件表、源名单暂存表。
- `db/team_collection/01_source.sql`：生成的 Excel 学生名单暂存 SQL，不直接赋权。
- `db/team_collection/02_import.sql`：统一匹配、三人确认映射、预览及原子发布，每次重建预览；重复导入不重置已有办理状态。
- `db/team_collection/README.md`：唯一完整迁移操作手册，包含初始化、预览、正式导入、验证及异常处理。
- `db/maintenance/team_collection_recovery.sql`：管理员核实长期占用/误确认后手工恢复，校验版本并记录事件；不属于正常导入。
- `scripts/team_collection/generate_roster.py`：标准库 XLSX 只读提取，生成暂存 SQL 和摘要；不连接数据库、不改工作簿。

队伍唯一键为 `(collection_code,team_code)`。成员唯一键为 `(team_id,user_id)`。名单来源摘要与采集活动分开，重复导入不能创造新轮次。

## 账号映射口径

预览以队伍编号+名单学生姓名匹配，不在全库仅按姓名/手机号查找用户。候选同时要求：

- 唯一有效 `team_manager_info` 队伍、`check_status='4'`；赛事届次与报名记录一致。
- `competition_apply_info.del_flag='0'`、`check_status='4'`；仅当前固定来源名单、81届次允许历史 `NULL`。
- 报名角色正向白名单：`队长/队员/学生/选手/参赛选手/成员`。
- 姓名与当前队伍内报名记录精确匹配（仅去除首尾空白），且有 `user_id`。
- 同队 `team_member_rela.del_flag='0'`、`check_status='2'`；仅当前固定来源名单、81届次允许历史 `NULL`。
- `sys_user.del_flag='0'`、`status='0'`。

空角色、缺账号、同名多账号、重复队伍、缺少有效成员关联都会留作未解决项。**这些条件需要现场核实；不能为凑齐1216条记录而放宽角色或猜测账号。** 若原数据不满足，应先输出差异并审核修订映射；本实现不自动修复业务源数据。

2026-09-13 在用户授权的本地 `jiaoxue_test` 只读核实：520队均审核通过；1216条学生报名状态均NULL；1213条有账号的同队成员状态也均NULL，成员记录及账号均有效。当前代码 `saveApplyCompetitionData` 仅为教师赛设置成员审核状态，普通赛存在不赋值的路径，因此不能将成员NULL一概视作未正式参赛。当前导入代码会设置报名状态4，无法仅凭现有代码证明历史报名NULL的生成过程；本次仅依据获奖源名单、审核通过的队伍及已关联账号接受该批历史NULL，不推广到其他届次，也不接受明确拒绝状态。

剩余3条报名及队员关系均缺少user_id。用户随后明确核对并确认方柏辉44349、徐启航50719、张清44874与参赛者一致（包括前述手机号及身份申请差异）。统一导入脚本只对固定Excel来源、队伍、单元格、姓名和这3个账号应用已确认映射，并重新核验审核通过队伍、学生角色的有效报名/成员记录，以及姓名/非空证件号/证件类型一致、实名认证通过且正常的唯一账号。任一条件变化则三条均不补充，需重新核对。该确认仅用于本次收款名单，不修复全局账号绑定或改变身份认证状态。

统一导入脚本在预览模式下只构造会话级匹配结果，正式模式重新匹配后将三人确认账号与另外1213个位置一并发布。本次仍要求1216个位置/520队、零歧义及零同队重复账号。无需重导01或修改原报名状态。

发布后的成员表是本次名单资格快照。普通加队或改名不会自动扩张范围。运行时继续核验账号有效性、队伍/成员启用开关；不实时重算报名关系。需要撤销资格时，应关闭对应成员记录；如持有人被撤销，管理员核实后处理其占用。

## API

所有用户ID由服务端取值，客户端ID用字符串避免整数精度损失。以下路径为浏览器经 V1 网关使用的路径：

| 方法 | 路径 | 参数/用途 |
|---|---|---|
| GET | `/system/teamCollection/mine` | 本人已启用名单内所有队伍及非敏感状态 |
| POST | `/system/teamCollection/{id}/start` | `{version}`，取得办理名额 |
| POST | `/system/teamCollection/{id}/confirm` | `{version}`，办理人确认完成 |
| POST | `/system/teamCollection/{id}/release` | `{version}`，主动放弃 |
| POST | `/system/v2EmbeddedBridge/settlement` | `{teamId,version}`，仅当前持有人可签发入口 |

旧的无参数收款桥接调用不再可用；前后端须一起部署。赛证互通桥接不变。关闭新功能开关不会恢复无名单校验的旧入口。

入口签发期间持有本队数据库行锁，防止释放后又给旧办理人签发；现有桥接连接/读取超时分别为3秒/5秒。远端异常回滚本次签发事务，保留先前已取得的名额。对已经成功签发的引用，V1不能取消V2会话。

## 手工部署顺序

以 [完整迁移操作步骤](../db/team_collection/README.md) 为唯一操作手册。首次初始化执行建表与01，已有源数据直接执行统一02：`@publish_team_collection=0`先预览，审阅通过后改为1再完整执行同一文件。每次明确设置审核数据库和81届次，无需跨文件保留临时匹配结果。

旧版02预览、03发布、05/06诊断和07补充已删除；恢复脚本移入维护目录。不得再按历史对话执行这些旧文件。业务数据库写入由操作人员执行，本次整理未执行实际导入。

数据库导入后部署配套V1前后端并启用配置，真实账号及原V2短信/保存流程仍需独立验收。V1测试通过不代表V2保存成功。

## 回退与异常处理

- 首选关闭 `V1_TEAM_COLLECTION_ENABLED`，保留名单、占用与事件。关闭功能不清空数据、不放开旧入口。
- 暂停一个队伍：维护人员将 `v1_team_collection.enabled` 设为0；名单资格撤销使用成员 `enabled=0`。变更应有外部工单记录。
- 长期占用或误确认：先读取状态、版本及事件，联系原办理人停止所有旧窗口，在 `db/maintenance/team_collection_recovery.sql` 设置确切目标库、队伍ID、当前版本、管理员账号与对应动作后执行。没有参数/状态不符会拒绝。
- 恢复不会删除V2资料，也不是完成状态自动核验。管理员恢复原因保存在外部工单，并以队伍ID/版本关联V1事件。
- 不提供“浏览器关闭自动释放”或“若干分钟自动释放”。

## 验证命令

```sh
# V1后端：使用隔离MySQL容器，既有业务数据库不参与。
cd old-code
DOCKER_HOST=unix:///Users/wwang/.colima/default/docker.sock \
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock \
mvn -Dapi.version=1.44 -pl teaching-modules/teaching-system -am test \
  -Dtest=TeamCollectionMySqlTest,TeamCollectionControllerTest,V2InternalEmbeddedBridgeClientTest,V2CredentialBridgeControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false

# PC组件/入口测试
cd ../old-code-pc
./node_modules/.bin/vitest run --config vitest.team-collection.config.js
./node_modules/.bin/vite build --outDir /tmp/deshi-team-collection-pc-build

# 导入工具测试（仓库根目录）
python3 -m unittest discover -s scripts/team_collection -p 'test_*.py' -v
```

Docker地址须根据当前 `docker context inspect` 结果调整；本机Colima和Docker API 1.44仅为本次测试环境，不是生产配置。

验证结果见同目录 `team-collection-validation-20260913.md`。组件测试不是浏览器完整E2E，合成账号映射不是实际名单匹配证据。
