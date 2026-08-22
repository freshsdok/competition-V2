# 教师查看学生参赛证功能审计

审计日期：2026-07-07

## 结论

当前系统不支持“教师登录后在 PC 端我的赛事/参赛证页面查看自己指导学生的参赛证”。

现有个人中心证件接口只返回当前登录用户自己的现场证件：按 `competition_scene_credential.user_id = 当前 userId`，以及 `subject_type = 'USER' and subject_code = 当前 userId` 两条拥有关系查询。代码中没有发现“教师 userId -> 指导团队/学生 -> 学生参赛证”的查询链路。

用户端 `credentialId` 详情接口也不是直接按 ID 查库，而是先取当前用户证件列表再过滤，普通用户通过该接口直接访问他人证件会被拒绝。后台管理端存在按 `credential_id` 直接查询接口，但有 `competition:sceneCredential:query` 权限要求，属于管理权限面。

## 1. 当前 PC 页面入口

入口位于个人中心“我的赛事”标签：

- `old-code-pc/src/views/personal/index.vue:122-132`：左侧菜单展示“我的赛事”。
- `old-code-pc/src/views/personal/index.vue:199-202`：当 `lefttabs == '我的赛事'` 时挂载 `personaltabs/Competition.vue`。
- `old-code-pc/src/views/personal/personaltabs/Competition.vue:54-62`：每条赛事卡片右侧提供“现场证件”按钮。
- `old-code-pc/src/views/personal/personaltabs/Competition.vue:433-449`：页面初始化同时调用“我的赛事列表”和“我的现场证件列表”。
- `old-code-pc/src/views/personal/personaltabs/Competition.vue:230-241`、`:866-867`：二维码由当前证件的 `qrContent` 或 `credentialToken` 前端生成。
- `old-code-pc/src/views/personal/personaltabs/Competition.vue:817-824`：页面只展示当前证件的报到、资料领取状态。

页面没有教师查看学生参赛证的独立入口、学生列表选择器、团队证件切换器，前端也没有传 `teacherId`、`teamCode`、`studentUserId` 给参赛证接口。

## 2. 当前前端 API

文件：`old-code-pc/src/api/personal/index.js`

| 前端方法 | 请求 | 用途 |
| --- | --- | --- |
| `userCompetition(data)` | `POST /competition/userCompetition/list` | 查询当前登录用户的“我的赛事”列表 |
| `mySceneCredentialList()` | `GET /competition/userCompetition/sceneCredential/myList` | 查询当前登录用户的现场证件/参赛证列表 |

当前 PC 个人中心未封装 `GET /competition/userCompetition/sceneCredential/{credentialId}` 详情方法，也未封装单独的二维码、证件文件下载接口。二维码和证件文件字段来自列表/详情返回的同一个 `CompetitionSceneCredential` 对象，包括 `credentialToken`、`qrContent`、`qrCodeUrl`、`credentialFileUrl`、`credentialImageUrl`。

## 3. 当前后端接口

### 当前登录用户赛事

文件：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionInfoController.java`

- `POST /userCompetition/list`
- Controller 从登录态获取 `SecurityUtils.getLoginUser().getSysUser().getUserId()`。
- Service 调用 `competitionApplyInfoService.selectCompetitionApplyInfoByUserId(userId)`。

对应 PC 网关路径为 `/competition/userCompetition/list`。

### 当前登录用户证件

文件：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionSceneCredentialController.java`

- `GET /userCompetition/sceneCredential/myList`
- 从登录态取当前 `userId`。
- 调用 `selectMyCompetitionSceneCredentialList(userId)`。

对应 PC 网关路径为 `/competition/userCompetition/sceneCredential/myList`。

### 用户端参赛证详情

同文件：

- `GET /userCompetition/sceneCredential/{credentialId}`
- 先调用 `selectMyCompetitionSceneCredentialList(userId)`，再在结果中按 `credentialId` 过滤。
- 未命中则返回“无权限查看该证件”。

该详情接口没有被当前 PC 个人中心页面调用。

### 后台管理端证件接口

文件：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java`

- `GET /sceneCredential/list`、`GET /competition/sceneCredential/list`：需要 `competition:sceneCredential:list`。
- `GET /sceneCredential/{credentialId}`、`GET /competition/sceneCredential/{credentialId}`：需要 `competition:sceneCredential:query`。
- `GET /sceneCredential/myList`、`GET /competition/sceneCredential/myList`：同样按当前登录用户查询自己的证件。

后台管理端详情接口按 `credential_id` 直接查证件，但受权限注解控制，不是普通 PC 个人中心入口。

### 二维码/证件文件

未发现个人中心专用的二维码或文件查询接口。当前字段在 `CompetitionSceneCredentialMapper.xml` 的通用查询列中返回：

- `credential_token`
- `qr_content`
- `qr_code_url`
- `credential_file_url`
- `credential_image_url`

现场扫码接口 `POST /competition/sceneVerify/scan`、`POST /competition/sceneOneCardVerify/pilot/scan` 可按二维码 token 解析证件，但这是现场核验链路，不是教师在个人中心查看学生参赛证的链路。

## 4. 当前 SQL / Mapper 查询条件

### 我的赛事列表

`CompetitionApplyInfoMapper.xml:1112-1125`

```sql
where (a.user_id = #{userId} or a.leader_teacher_id = #{userId})
  and a.del_flag = '0'
  and a.check_status not in ('5')
```

含义：当前“我的赛事”入口会展示当前用户自己报名的赛事，或当前用户作为带队老师 `leader_teacher_id` 的赛事。

注意：这里能展示教师相关赛事，不代表证件接口会按这些赛事/团队继续查询学生参赛证。

### 我的证件列表

`CompetitionSceneCredentialServiceImpl:94-109`

当前用户证件列表由两次查询合并：

1. `query.setUserId(userId)`。
2. `subject_type = 'USER'` 且 `subject_code = String.valueOf(userId)`。

`CompetitionSceneCredentialMapper.xml:195-280` 的动态条件中，对应核心条件为：

```sql
and user_id = #{userId}
```

以及：

```sql
and subject_type = #{subjectType}
and subject_code = #{subjectCode}
```

该 Mapper 虽然返回 `leader_teacher_id`、`guide_teacher` 等字段，但当前 `myList` 查询没有使用：

- `leader_teacher_id = 当前教师 userId`
- `guide_teacher` 或指导教师姓名
- 当前教师关联的 `team_code in (...)`
- 学生 `user_id in (...)`

### 用户端详情

`UserCompetitionSceneCredentialController.java:30-39`

用户端详情不是调用 `selectCompetitionSceneCredentialById(credentialId)`，而是：

```java
selectMyCompetitionSceneCredentialList(userId)
  .stream()
  .filter(item -> credentialId.equals(item.getCredentialId()))
```

因此普通用户不能仅凭 `credentialId` 通过用户端详情接口读取他人证件。

### 后台管理详情

`CompetitionSceneCredentialController.java:54-58` 调用 `selectCompetitionSceneCredentialById(credentialId)`；Mapper 条件为：

```sql
where credential_id = #{credentialId} and del_flag = '0'
```

该接口有 `@RequiresPermissions("competition:sceneCredential:query")`。

### 报到/资料/候场状态

`CompetitionSceneSubjectOperationStateServiceImpl:67-108` 会给返回的证件补充：

- `reportStatus/reportTime`
- `materialStatus/materialTime`
- `waitingStatus/waitingTime`

但补充对象仍然只限于当前证件查询结果。教师当前拿不到学生证件，自然也拿不到学生证件的这些状态。

## 5. 当前教师能看到哪些证件

教师当前能看到：

1. 证件行 `user_id = 教师 userId` 的证件。
2. 证件行 `subject_type = 'USER' and subject_code = 教师 userId` 的证件。
3. 如果教师本人被发放教师证，且证件按上述用户拥有关系落库，则能看到自己的教师证。
4. 如果某条团队维度证件本身把 `user_id` 写成了该教师 userId，则教师会看到该证件；但这是证件行拥有者命中，不是按“教师指导团队”授权查询。

`selectMyCompetitionSceneCredentialList` 还会按当前证件的 `teamCode` 补充 `teamMembers` 摘要，但这些摘要只包含成员基础信息，不包含成员证件 ID、二维码、证件文件、报到/资料/候场状态；当前 PC 页面也未渲染 `teamMembers`。

## 6. 当前教师不能看到哪些证件

教师当前不能因为“带队/指导关系”看到：

1. 自己指导学生的个人参赛证。
2. 自己指导团队的团队参赛证，除非该证件行本身 `user_id` 恰好等于教师 userId。
3. 学生参赛证二维码，包括 `qrContent`、`credentialToken`、`qrCodeUrl`。
4. 学生参赛证文件，包括 `credentialFileUrl`、`credentialImageUrl`。
5. 学生报到、资料领取、候场状态。
6. 非自己指导学生的参赛证。

## 7. 是否支持教师查看学生参赛证

不支持。

当前只支持“当前登录用户查看自己的现场证件”。虽然“我的赛事”列表可按 `leader_teacher_id = 当前 userId` 展示教师带队赛事，但参赛证接口没有继续沿用这个团队/学生关系，也没有任何教师专用的学生证件列表或详情接口。

## 8. 如果不支持，需要新增哪些后端接口

建议新增教师专用、只读、强权限边界的个人中心接口，不要扩大现有 `myList` 的语义：

1. `GET /competition/userCompetition/teacherSceneCredential/list`
   - 从登录态取当前教师 `userId`，禁止前端传入 `teacherId` 决定权限。
   - 后端解析该教师可管理的团队/学生范围。
   - 返回该范围内学生/团队证件列表，可支持 `competitionSeriesId`、`teamCode` 过滤，但过滤值必须再次校验属于当前教师范围。

2. `GET /competition/userCompetition/teacherSceneCredential/{credentialId}`
   - 按 `credentialId` 查询前必须用 `EXISTS` 或服务层校验确认该证件属于当前教师授权团队/学生。
   - 未授权返回无权限，不返回证件存在性细节。

3. `GET /competition/userCompetition/teacherSceneCredential/{credentialId}/qr`
   - 如需单独获取二维码，必须复用同一授权校验。
   - 也可以不单设接口，只在详情接口授权通过后返回二维码字段。

4. `GET /competition/userCompetition/teacherSceneCredential/status`
   - 如需批量查看报到、资料、候场状态，应按授权团队/学生范围返回只读状态。
   - 不应允许教师通过该接口执行报到、资料领取或候场确认。

授权范围建议基于结构化字段：

- 带队老师：`competition_apply_info.leader_teacher_id = 当前教师 userId`。
- 指导教师：优先使用报名明细中 `competition_role_name = '指导教师' and user_id = 当前教师 userId` 的结构化关系。
- 不建议使用 `guide_teacher like '%教师姓名%'` 作为权限依据，因为姓名匹配容易误伤同名教师，也容易扩大授权范围。

## 9. 如果支持，权限边界是否安全

当前不支持教师查看学生参赛证，因此不存在“教师通过该功能看到非自己指导学生参赛证”的现成功能风险。

现有边界判断：

| 场景 | 当前结论 |
| --- | --- |
| 教师看到非自己指导学生参赛证 | 当前用户端链路不支持教师看学生证，未发现该越权路径 |
| 教师看到自己指导学生参赛证 | 不支持 |
| 学生看到其他学生证件 | 用户端 `myList` 只按当前用户拥有关系查；用户端详情先取我的列表再过滤，未发现按 ID 越权 |
| 普通用户通过 `credentialId` 直接访问他人证件 | 通过 `/userCompetition/sceneCredential/{credentialId}` 不可行；后台 `/sceneCredential/{credentialId}` 需要管理权限 |
| 有管理权限用户按 ID 查看证件 | 可行，属于后台管理权限面，依赖角色权限配置 |
| 通过二维码 token 扫码解析证件 | 现场扫码接口可按 token 解析证件；这不是 `credentialId` 越权，但建议单独审计现场扫码接口的使用角色和入口控制 |

## 10. 建议的最小改造方案

1. 保持现有 `GET /competition/userCompetition/sceneCredential/myList` 不变，继续只表示“我的证件”。
2. 新增教师专用查询服务，第一步只解析当前登录教师可访问的 `team_code` / 学生 `user_id` 集合。
3. 授权解析只使用结构化关系：`leader_teacher_id = 当前教师 userId`，以及指导教师报名行 `competition_role_name = '指导教师' and user_id = 当前教师 userId`。
4. 新增证件列表 SQL 时使用 `EXISTS` 绑定授权团队，例如证件 `team_code`、`competition_series_id` 必须落在当前教师授权报名记录范围内；学生个人证件还应排除指导教师本人角色。
5. 详情和二维码接口必须复用同一授权校验，不能只在列表接口做限制。
6. 默认只返回必要字段；身份证、手机号继续保持当前脱敏/后缀策略，二维码 token 仅在明确授权的详情或二维码接口返回。
7. 增加最小回归用例：
   - 教师 A 能看到自己带队/指导团队的学生证件。
   - 教师 A 不能看到教师 B 团队的学生证件。
   - 学生只能看到自己的证件。
   - 普通用户用他人 `credentialId` 调用户端详情返回无权限。
   - 后台管理端原有权限行为不变。
