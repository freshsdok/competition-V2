# 教师查看自己指导学生参赛证开发结果

## 1. 修改文件清单

后端：

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionSceneCredentialController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionTeacherStudentCredentialQuery.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionTeacherStudentCredentialVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/mapper/CompetitionSceneCredentialMapper.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/ICompetitionSceneCredentialService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneCredentialServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialTeacherStudentServiceImplTest.java`

PC 前端：

- `old-code-pc/src/api/personal/index.js`
- `old-code-pc/src/views/personal/personaltabs/Competition.vue`

文档：

- `docs/competition-scene/SCENE_TEACHER_VIEW_STUDENT_CREDENTIAL_DEV_RESULT.md`

## 2. 新增接口

新增 PC 用户端教师专用只读接口：

- `GET /competition/userCompetition/teacher/studentCredentials`
- `GET /competition/userCompetition/teacher/studentCredential/{credentialId}`

兼容保留原有用户自身证件接口路径：

- `GET /competition/userCompetition/sceneCredential/myList`
- `GET /competition/userCompetition/sceneCredential/{credentialId}`

本次未新增任何状态修改、报道、资料领取、候场操作接口。

## 3. 教师指导关系判断逻辑

教师身份从登录态获取当前 `userId`，前端不能传教师 ID。

教师可见范围基于 `competition_apply_info` 中同一 `competition_series_id + team_code` 的授权团队判断：

- `auth.leader_teacher_id = 当前登录教师 userId`
- 或存在团队内指导教师报名行：`auth.user_id = 当前登录教师 userId` 且 `auth.competition_role_name = '指导教师'`

学生/队员记录查询条件：

- `m.del_flag = '0'`
- `m.pay_status = 'paid'`
- `m.check_status = '4'`，兼容为空的历史数据
- 排除 `competition_role_name in ('指导教师', '指导老师')`

未使用教师姓名作为主权限判断，也未使用 `guide_teacher` 姓名模糊匹配作为授权依据。

## 4. 权限校验逻辑

列表接口：

- 后端先以当前登录教师 `userId` 限定授权团队；
- `competitionId`、`competitionSeriesId`、`teamCode`、`keyword` 只作为已授权范围内的过滤条件；
- 即使前端传入其他团队 `teamCode`，也必须满足授权团队 `exists` 条件，否则不会返回。

详情接口：

- 先确认 `credentialId` 对应证件存在且未删除；
- 再通过 `credentialId + 当前教师 userId` 走授权查询；
- 证件必须能关联到该教师指导范围内的学生或团队；
- 校验失败返回“无权限查看该学生参赛证”；
- 未复用后台管理端无过滤 `credentialId` 查询接口。

普通学生调用教师接口时，如果不是对应团队的 `leader_teacher_id` 或指导教师报名行用户，授权查询为空，不能查看其他学生证件。

## 5. 返回字段

返回教师 PC 只读展示所需字段：

- 团队/赛事：`competitionId`、`competitionSeriesId`、`competitionName`、`teamCode`、`teamName`、`schoolName`、`groupCode`、`groupName`
- 学生：`userId`、`memberId`、`studentName`、`roleCode`、`roleName`、`studentGroupCode`、`studentGroupName`
- 证件：`credentialId`、`credentialName`、`credentialNo`、`credentialType`、`credentialStatus`、`qrContent`、`qrCodeUrl`、`credentialFileUrl`、`credentialImageUrl`
- 现场状态：`reportStatus`、`materialStatus`、`waitingStatus`、`reportTime`、`materialTime`、`waitingTime`、`delegateInfo`
- 赛场：`scheduleId`、`scheduleName`、`scheduleLocation`、`scheduleStartTime`、`scheduleEndTime`

学生没有生成参赛证时仍返回学生记录，并设置：

- `credentialStatus = NOT_GENERATED`
- `credentialId = null`

安全字段处理：

- 不返回身份证号；
- 不返回 `openId` / `unionId`；
- 不返回 `credentialToken`；
- 二维码展示使用 `qrContent` 或已有二维码 URL，并由教师授权接口限制访问范围；
- 本期未返回手机号。

## 6. PC 页面改造

`我的赛事` 页面新增“指导学生参赛证”区域：

- 自动调用教师学生参赛证列表接口；
- 按赛事/团队分组；
- 展示学生姓名、角色、组别、证件状态、报道/资料/候场状态；
- 有证件时展示“查看参赛证”；
- 无证件时展示“未生成参赛证”；
- 详情弹窗展示二维码、证件编号、团队、赛场、地点、现场状态、代领信息；
- 页面没有修改学生信息、修改证件状态、代报道、代领资料、代候场按钮。

非教师或无指导团队用户会看到空状态。

## 7. 安全边界

本期只读：

- 不修改 `competition_apply_info`；
- 不修改 `competition_scene_credential`；
- 不写入报道状态；
- 不写入资料领取状态；
- 不写入候场状态；
- 不提供任何教师代操作能力。

越权边界：

- 教师不能通过 `teamCode` 查看非自己指导团队；
- 教师不能通过 `credentialId` 查看非自己指导学生/团队证件；
- 学生不能通过教师接口查看其他学生证件；
- 普通用户不能绕过个人证件列表直接查看他人证件；
- 后台管理端详情接口未用于 PC 教师个人中心。

## 8. 测试结果

新增单元测试：

- 教师列表中无证学生标记 `NOT_GENERATED`；
- 教师详情接口对未授权 `credentialId` 抛出无权限异常；
- 教师详情接口只读填充报道、资料、候场状态；
- `checkTeacherCanViewCredential` 使用授权详情查询判断权限；
- 测试中校验未调用证件状态更新方法。

执行结果：

```bash
mvn -pl teaching-modules/teaching-competition -am test -Dtest=CompetitionSceneCredentialTeacherStudentServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
```

结果：`Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`。

说明：第一次未加 `-Dsurefire.failIfNoSpecifiedTests=false` 时，多模块上游模块没有匹配到该指定测试类，Surefire 按默认策略失败；重新执行目标测试后通过。

## 9. 构建结果

后端：

```bash
mvn -pl teaching-modules/teaching-competition -am compile -DskipTests
```

结果：`BUILD SUCCESS`。

PC：

```bash
npm run build
```

结果：构建成功，产物输出到 PC 构建目录。

构建中存在项目既有警告，包括 Sass legacy JS API、Browserslist 数据较旧、部分 eval 警告、CSS 拼写警告、chunk size 警告；本次功能未引入阻断性构建错误。

## 10. 已知风险

- 未连接生产数据库，未做生产数据验证，符合本轮禁止事项。
- 未连接测试库做真实联调，Mapper SQL 需要用测试库报名/证件数据确认返回形态。
- 当前稳定授权字段采用现有报名表可确认的 `leader_teacher_id` 以及指导教师报名行 `user_id + competition_role_name`；如果后续确认表中新增或启用 `teacher_user_id`、`guide_teacher_user_id` 等稳定字段，应在同一授权 SQL 中补充，但仍不能降级为姓名匹配。
- 团队级证件会按团队授权返回到团队成员视图中，测试库需确认前端展示是否符合业务预期。
- PC 入口对所有用户展示；无指导团队或非教师账号返回空状态。若后续产品要求隐藏入口，可增加前端角色/接口空态策略，但不能替代后端权限校验。

## 11. 是否可以进入测试库联调

可以进入测试库联调。

建议准备测试数据：

- 教师 A 指导团队 1，团队内学生有证/无证各一名；
- 教师 B 指导团队 2；
- 学生账号尝试调用教师接口；
- 教师 A 使用团队 2 的 `teamCode` 和学生证件 `credentialId` 尝试越权；
- 团队级证件与学生级证件各一组；
- 校验响应中不包含身份证号、`openId`、`unionId`、`credentialToken`。
