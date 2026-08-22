# 核心业务流程反向重建

说明：流程来自接口、页面、Service/Mapper 命名和 migration 反推；写链路中的事务细节需后续代码走读确认。

## 用户注册登录与身份建立

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 用户进入 PC/小程序登录页
2. 调用登录/微信登录接口获取 token
3. 系统加载用户资料、权限和认证信息
4. 未认证或资料缺失时进入个人中心补全

**代表证据**：`old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:80`, `old-code-mini/api/login.js:6`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 赛事发布与报名

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 管理员维护赛事、赛道、阶段和报名配置
2. 用户在 PC 端浏览赛事详情
3. 个人或团队提交报名信息
4. 系统生成报名/团队/成员记录
5. 报名状态进入审核或待支付

**代表证据**：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:80`, `old-code-admin/src/api/certInterconnect/certConfig.js:58`, `old-code-admin/src/api/fileTask/index.js:15`, `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionApplyInfo.java:80`, `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionApplyInfoExcel.java:54`, `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionApplyInfoVO.java:80`, `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/TeamManagerInfo.java:80`, `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/TeamManagerInfoAwardsInfo.java:80`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 团队创建与成员维护

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 队长创建团队并填写团队信息
2. 邀请或添加队员/指导教师
3. 成员提交个人信息和认证材料
4. 管理员审核团队或成员
5. 团队信息用于报名、支付、赛场和证件

**代表证据**：`old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/TeamMemberRela.java:80`, `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/TeamMemberRelaController.java:80`, `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionInfoController.java:80`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 订单支付与补缴退款

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 用户确认报名或团队变更订单
2. 系统创建订单并返回支付信息
3. 支付结果同步或回调更新 pay_status
4. 退款/补缴生成关联订单
5. 发票状态与订单状态关联

**代表证据**：`old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/factory/OrderServiceFactory.java:80`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 赛场安排与 target 生成

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 管理员创建现场赛程
2. 从报名队伍/个人/评审对象生成 target
3. 可手工新增或批量导入 target
4. 对 target 排序、自动编号
5. 同步到评审 session 或现场证件

**代表证据**：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:80`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 现场证件生成与查看

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 系统根据赛程 target/报名对象生成证件
2. 证件保存姓名、学校、组别、角色等快照
3. 管理员可直接发证或作废证件
4. 教师/用户按关系查看证件

**代表证据**：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:80`, `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionSceneCredentialController.java:53`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 一证多权授权与扫码核验

**证据等级**：`STATIC_INFERENCE`

**流程步骤**：
1. 管理员或系统为证件授予日程/能力 grant
2. 扫码 scan 解析证件与场景
3. confirm 校验 grant、credential、operation_state
4. 写入 operation_state 和 operation_log
5. 前端显示成功/失败/已完成

**代表证据**：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneVerifyController.java:56`, `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java:42`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 资源预约

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 管理员维护资源台账、部署到赛程并配置时段
2. 用户查看可预约资源和 slot
3. 用户提交预约及 idempotency_key
4. 系统校验主体、范围、容量并扣减
5. 用户可查看我的预约并取消，取消释放容量

**代表证据**：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/UserCompetitionSceneResourceController.java:80`, `old-code-admin/src/api/tournament/sceneResource.js:5`, `old-code-admin/src/api/tournament/sceneResource.js:13`, `old-code-admin/src/api/tournament/sceneResource.js:20`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 评审任务与评分

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 管理员创建评审活动/场次/对象
2. 参赛对象提交材料
3. 专家查看我的评审任务并预览材料
4. 秘书控制当前对象和状态
5. 专家提交评分，系统汇总结果

**代表证据**：`old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewSecretaryController.java:69`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。

## 文件交互提交

**证据等级**：`CONFIRMED_BY_CODE`

**流程步骤**：
1. 管理员配置文件任务或业务表单文件项
2. 用户上传文件到 OSS/文件服务
3. 系统保存文件记录与业务关联
4. 管理员/专家/用户下载或预览
5. 导出时可打包生成文件

**代表证据**：`old-code-admin/src/api/fileTask/index.js:6`, `old-code-admin/src/api/fileTask/index.js:15`, `old-code-admin/src/api/fileTask/index.js:24`

**重构建议**：先做只读流程还原和对账，再重构写入；支付、预约、扫码、评分属于高风险写链路。
