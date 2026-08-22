# 测试质量审计

## 测试文件分布

| 模块 | 测试文件数 |
| --- | --- |
| old-code/teaching-modules/teaching-competition | 19 |

## 已发现测试文件

- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewModulePhase1SmokeTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewMyReviewServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewResultServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewRuleServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewSecretaryServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/review/ReviewSubmissionServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialScopeGrantServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneCredentialTeacherStudentServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneNoticeContentCodecTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneNoticeHtmlSanitizerTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneNoticeServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneOneCardIssueServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneResourceScheduleScopeServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneResourceSlotGroupScopeServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneScheduleServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/CompetitionSceneVerifyServiceImplTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceReservationBaseMethodTest.java`
- `old-code/teaching-modules/teaching-competition/src/test/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImplTest.java`

## 缺口
- 认证、网关、文件、支付、内容、系统管理模块缺少可见测试。
- 前端工程未发现测试目录或冒烟测试。
- 资源预约、扫码核验、支付回调缺少并发/幂等重放测试。
- Mapper SQL 缺少约束级验证测试。

## 建议测试矩阵

| 优先级 | 测试类型 | 覆盖流程 |
| --- | --- | --- |
| P1 | 并发集成测试 | 资源预约创建/取消、slot 容量扣减与释放 |
| P1 | 幂等重放测试 | 预约 idempotency_key、支付回调、扫码 confirm |
| P1 | 安全测试 | 文件上传类型、v-html 净化、权限注解/菜单对账 |
| P2 | Mapper 测试 | deleted/del_flag、状态过滤、唯一键冲突 |
| P2 | 前端冒烟 | 登录、报名、证件查看、扫码、预约、评审秘书 |
