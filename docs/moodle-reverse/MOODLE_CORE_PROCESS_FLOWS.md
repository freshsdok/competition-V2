# Moodle 核心业务流程反向文档

## 1. 用户访问课程活动

```mermaid
sequenceDiagram
    participant U as 用户
    participant P as 页面入口
    participant S as Session/Login
    participant A as Access Check
    participant C as Course/Module
    participant M as Activity Plugin
    U->>P: 打开课程或活动页面
    P->>S: require_login
    S-->>P: 当前用户与会话
    P->>A: has_capability / require_capability
    A->>C: 按 context 判断权限
    C->>M: 根据 course_modules 找到插件实例
    M-->>U: 输出活动页面
```

## 2. 用户进入课程

```mermaid
flowchart LR
    User["用户"] --> Method["选课方式 enrol plugin"]
    Course["课程"] --> Enrol["enrol 选课实例"]
    Method --> Enrol
    Enrol --> UE["user_enrolments"]
    UE --> Role["课程上下文角色"]
    Role --> Access["可访问课程资源"]
```

反向需求：课程参与资格不是直接写在课程成员字段中，而是由选课实例和用户选课事实表达。角色分配再补充教师、学生、助教、管理者等权限差异。

## 3. 课程活动创建

```mermaid
flowchart TB
    Teacher["教师/管理员"] --> Permission["检查 course:manageactivities"]
    Permission --> Plugin["选择 mod 插件"]
    Plugin --> Instance["写入插件实例表，如 assign/quiz/forum"]
    Instance --> CM["写入 course_modules"]
    CM --> Context["生成模块 context"]
    CM --> Grade["必要时生成 grade_items"]
```

## 4. 作业/测验评分

```mermaid
flowchart LR
    Activity["活动插件"] --> Submission["提交/尝试事实"]
    Submission --> Grading["评分或自动判分"]
    Grading --> GradeItem["grade_items"]
    GradeItem --> GradeGrades["grade_grades"]
    GradeGrades --> Report["成绩报表"]
```

## 5. 文件处理流程

```mermaid
flowchart LR
    Upload["上传/仓库选择"] --> Draft["草稿区"]
    Draft --> FilePool["files 文件池"]
    FilePool --> Context["contextid"]
    FilePool --> Component["component"]
    FilePool --> Area["filearea"]
    FilePool --> Item["itemid"]
    Component --> Business["业务插件读取文件"]
```

## 6. 外部服务调用

```mermaid
flowchart LR
    Client["移动端/第三方"] --> Token["token/session"]
    Token --> Service["external_services"]
    Service --> Function["external_functions"]
    Function --> ExternalLib["externallib/classes external"]
    ExternalLib --> Capability["权限检查"]
    Capability --> Domain["业务处理"]
```

## 7. 关键代码证据

| 机制 | 证据示例 |
|---|---|
| 登录要求 | `require_login` 在入口、文件、课程、仓库等脚本中出现 |
| 权限判断 | `has_capability`、`require_capability` 结合 `context_*::instance()` 使用 |
| 课程模块缓存 | `get_fast_modinfo` 在课程页面和模块处理中使用 |
| 文件池 | `get_file_storage` 在文件、仓库、能力等模块中使用 |
| 服务注册 | `db/services.php` 与 `external_services`、`external_functions` 表 |
