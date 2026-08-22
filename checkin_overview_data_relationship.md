# 签到概览数据关系说明

## 1. 实体关系图

```mermaid
erDiagram
    COMPETITION_MAIN_INFO ||--o{ COMPETITION_SERIES_INFO : "competition_id"
    COMPETITION_SERIES_INFO ||--o{ COMPETITION_TRACK_INFO : "competition_series_id"
    COMPETITION_SERIES_INFO ||--o{ COMPETITION_APPLY_INFO : "competition_series_id"
    COMPETITION_SERIES_INFO ||--o{ COMPETITION_SCENE_SCHEDULE : "competition_series_id"
    COMPETITION_SCENE_SCHEDULE ||--o{ COMPETITION_SCENE_SCHEDULE_TARGET : "schedule_id"
    COMPETITION_SCENE_SCHEDULE_TARGET ||--o{ COMPETITION_SCENE_CREDENTIAL : "target_id"
    COMPETITION_SCENE_SCHEDULE ||--o{ COMPETITION_SCENE_CREDENTIAL : "schedule_id"
    COMPETITION_SCENE_CREDENTIAL ||--o{ COMPETITION_SCENE_SUBJECT_OPERATION_STATE : "credential_id"
    COMPETITION_SCENE_CREDENTIAL ||--o{ COMPETITION_SCENE_OPERATION_LOG : "credential_id"
    COMPETITION_SCENE_SCHEDULE ||--o{ COMPETITION_SCENE_OPERATION_LOG : "schedule_id"

    COMPETITION_MAIN_INFO {
        bigint competition_id PK
        varchar competition_name
    }

    COMPETITION_SERIES_INFO {
        bigint competition_series_id PK
        bigint competition_id FK
        varchar competition_series_name
    }

    COMPETITION_TRACK_INFO {
        bigint track_id PK
        varchar competition_track_id
        bigint competition_series_id FK
        varchar competition_track_name
    }

    COMPETITION_APPLY_INFO {
        bigint member_id PK
        bigint competition_series_id FK
        bigint user_id
        varchar team_code
        varchar team_name
        varchar competition_role_name
        varchar competition_track_id
        varchar second_level_code
        varchar check_status
        varchar pay_status
        varchar del_flag
    }

    COMPETITION_SCENE_SCHEDULE {
        bigint schedule_id PK
        bigint competition_series_id FK
        varchar competition_track_id
        varchar second_level_code
        datetime report_start_time
        datetime report_end_time
        varchar report_location
        datetime contest_start_time
        datetime contest_end_time
        varchar contest_location
        varchar contest_room
        char status
        char del_flag
    }

    COMPETITION_SCENE_SCHEDULE_TARGET {
        bigint target_id PK
        bigint schedule_id FK
        bigint competition_series_id FK
        varchar target_key
        varchar target_type
        varchar team_code
        bigint member_id
        bigint user_id
        varchar user_name
        varchar competition_role_name
        varchar match_status
        char status
        char del_flag
    }

    COMPETITION_SCENE_CREDENTIAL {
        bigint credential_id PK
        bigint schedule_id FK "nullable for competition-scope"
        bigint target_id FK "nullable for direct issue"
        varchar scope_type
        bigint scope_ref_id
        varchar subject_type
        varchar subject_code
        bigint member_id
        bigint user_id
        varchar team_code
        varchar competition_role_name
        char report_status
        datetime report_time
        varchar credential_status
        char del_flag
    }

    COMPETITION_SCENE_SUBJECT_OPERATION_STATE {
        bigint state_id PK
        bigint competition_series_id
        varchar scope_type
        bigint scope_ref_id
        varchar subject_type
        varchar subject_code
        varchar operation_type
        varchar operation_status
        datetime operation_time
        bigint credential_id FK "nullable by schema"
        tinyint deleted
    }

    COMPETITION_SCENE_OPERATION_LOG {
        bigint log_id PK
        bigint credential_id FK
        bigint schedule_id FK
        bigint target_id
        varchar operation_type
        varchar operation_stage
        varchar operation_result
        varchar result_status
        datetime operation_time
        char del_flag
    }
```

## 2. 签到统计采用的关联路径

首选路径：

```mermaid
flowchart LR
    A["competition_scene_schedule<br/>schedule_id"] --> B["competition_scene_schedule_target<br/>schedule_id + target_id"]
    B --> C["competition_scene_credential<br/>target_id + schedule_id"]
    C --> D["competition_scene_subject_operation_state<br/>credential_id"]
    D --> E["SIGNED when<br/>scope_type=SCHEDULE<br/>scope_ref_id=schedule_id<br/>operation_type=REPORT<br/>operation_status=DONE<br/>deleted=0"]
```

辅助审计路径：

```mermaid
flowchart LR
    C["competition_scene_credential"] --> L["competition_scene_operation_log"]
    L --> R["REPORT_SIGN only for audit<br/>and last checkin time helper"]
```

## 3. 主键与业务键

- 赛场卡片主键：`competition_scene_schedule.schedule_id`
- 应签到人员主键：优先 `schedule_id + target_id`
- 兼容键：`schedule_id + credential_id`
- 兜底业务键：`schedule_id + member_id` 或 `schedule_id + user_id`
- 禁止键：姓名、手机号、团队名。

## 4. 可能为空的关键字段

- `competition_scene_schedule.competition_track_id`、`second_level_code` 可为空。
- `competition_scene_credential.schedule_id` 对大赛级证件可为空。
- `competition_scene_credential.target_id` 对大赛级直发证件可为空。
- `competition_scene_subject_operation_state.credential_id` 表结构允许为空，但当前代码确认操作会写入。
- `competition_scene_schedule_target.target_type` 真实库大量为空。
- `competition_scene_schedule_target.member_id/user_id` 对团队、工作人员、手工对象可能为空，但真实库当前有效目标未发现三者都空且无团队编码的记录。

## 5. 可能产生重复统计的位置

- 同一 `target_key` 出现在多个 `schedule_id`：真实库已存在，这是多赛场参与的正常情况，必须带 `schedule_id` 统计。
- `competition_scene_operation_log` 同一操作有 `SCAN` 和 `CONFIRM` 两条流水，不能按流水条数计人数。
- `operation_result = DUPLICATE` 或 `operation_status = CANCELLED` 不能计为已签到。
- 大赛级 `scope_type = COMPETITION` 的 `REPORT` 是大赛范围状态，不能复用到各赛场。
- 同一团队如果按 `TEAM` 维度绑定，一条目标可能代表团队主体；首期人员统计应优先使用 PERSON 维度目标，或在团队维度下明确拆成员规则。
- 首期仅统计有效、已匹配的队员目标；`选手/PLAYER` 按队员处理，队长、教师和工作人员角色排除。

## 6. 真实库验证摘要

- `competition_scene_schedule`：64 行。
- `competition_scene_schedule_target`：2036 行。
- `competition_scene_credential`：3159 行。
- `competition_scene_subject_operation_state`：6 行。
- `competition_scene_operation_log`：40 行。
- 当前状态表同时存在 `COMPETITION/REPORT`、`SCHEDULE/REPORT`、`CANCELLED`，证明统计必须限定 scope 和状态。
- 当前赛场目标角色值包括 `队员`、`选手`、`MEMBER`、`CHECKIN_STAFF`、`MATERIAL_STAFF`、`VOLUNTEER`，未发现 `队长/CAPTAIN`。
- 已确认 `队员/MEMBER/选手/PLAYER` 计入首期应签到人数；当前无队长记录，首期去除队长相关统计。
