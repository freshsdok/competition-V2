# Review Engine V1.0 业务流程

## 1. 标准评审流程

```mermaid
flowchart TD
    A["管理员创建评审活动"] --> B["创建评审轮次"]
    B --> C["配置、校验并启用评分规则"]
    C --> D["创建或从外部业务导入 Review Object"]
    D --> E["向填报人授予 Submission Permission"]
    E --> F["填报人保存草稿、上传材料"]
    F --> G["填报人提交"]
    G --> H["管理员关闭填报"]
    H --> I{"对象提交状态"}
    I -->|"SUBMITTED"| J["锁定为 LOCKED"]
    I -->|"未完成"| K["作废为 INVALID"]
    J --> L["配置专家/专家组并分配 Assignment"]
    L --> M["专家保存评分草稿"]
    M --> N["专家提交评分"]
    N --> O["按已提交记录计算平均分和排名"]
    O --> P["管理员填写发布性结论"]
    P --> Q["发布结果"]
    Q --> R["填报人查看已发布结果"]
```

关键约束：

- 评分必须从本人 Assignment 进入；任务、活动、轮次、对象和评分规则必须一致。
- 提交时校验必填项、评分类型、选项值、上下限以及规则总分。
- 结果按 `SUBMITTED` 评分记录汇总；评分未全部完成时仍可生成当前平均分，但返回警告。
- 已发布结果必须先撤回才能重新生成。
- 管理员只能填写 `evaluation_conclusion`，不能改专家分或 `calculated_score`。

## 2. 填报与撤回流程

```mermaid
stateDiagram-v2
    [*] --> DRAFT
    DRAFT --> SUBMITTED: submit
    WITHDRAW_APPROVED --> SUBMITTED: resubmit
    SUBMITTED --> WITHDRAW_REQUESTED: withdraw-request
    WITHDRAW_REQUESTED --> WITHDRAW_APPROVED: withdraw-approve
    WITHDRAW_REQUESTED --> SUBMITTED: withdraw-reject
    SUBMITTED --> LOCKED: close-submission
    DRAFT --> INVALID: close-submission
    WITHDRAW_APPROVED --> INVALID: close-submission
```

V1.0 的撤回驳回实现会把状态写回 `SUBMITTED`；枚举中的 `WITHDRAW_REJECTED` 没有在该流程中落库。

## 3. 现场评审流程

```mermaid
sequenceDiagram
    participant A as 管理员
    participant S as 评审秘书
    participant E as Review Engine
    participant R as 专家页面

    A->>E: 创建 Session、专家组和对象顺序
    S->>E: 打开本人负责的现场场次
    S->>E: 输入/扫码参赛证编号
    E-->>S: 返回候选 Review Object
    S->>E: 二次确认并设置 current object
    E->>E: 更新 Session/SessionObject，写 Event Log
    loop 每 3 秒
        R->>E: 查询专家当前场次对象
        E-->>R: 返回 current object
    end
    R->>R: 当前对象自动置顶
    R->>E: 保存或提交评分
    S->>E: 切换下一位或更新到场/跳过/延后状态
```

秘书必须是 `review_session.secretary_user_id`、专家组的 `secretary_user_id`，或活动内启用的 `SECRETARY/ADMIN/OPERATOR`。专家的轮询接口还会验证其在当前活动、轮次和专家组中的任务分配。

## 4. Competition 外部业务导入

```mermaid
flowchart LR
    C["Competition 报名/团队/答辩安排"] --> P["Import Preview"]
    P --> V{"校验来源、活动和对象重复"}
    V -->|"通过"| O["Review Object"]
    O --> M["Object Members"]
    O --> SP["Submission Permissions"]
    O --> CR["Certificate Refs"]
    O --> ER["External Refs"]
    O --> SO["可选：Session Object 顺序"]
    V -->|"跳过/失败"| REP["导入结果与告警"]
```

当前实现识别 `TEAM`、`REGISTRATION`、`REGISTRATION_TEAM_CODE`、`DEFENSE_SCHEDULE` 等竞赛来源，支持预览、覆盖控制、权限用户策略、证件同步和初始状态控制。

## 5. 文件任务导入

```mermaid
flowchart LR
    FT["FileTask / 文件上传管理"] --> RS["远程查询 FileReviewImportSource"]
    RS --> PRE["导入预览"]
    PRE --> OBJ["Review Object"]
    OBJ --> MAT["同步已提交文件为 Object Material"]
    MAT --> TRACE["记录 material source_* 来源"]
```

V1.0 已有定向接口：

- `POST /competition/review/activity/{activityId}/file-task/{fileTaskId}/import-preview`
- `POST /competition/review/activity/{activityId}/file-task/{fileTaskId}/import`
- `POST /competition/review/object/sync-file-task-materials`

这不是通用适配框架：实现仍直接依赖文件任务远程服务和专用 DTO。将 FileTask、Competition 和其他业务统一为可插拔 Business Adapter Framework 属于规划能力。
