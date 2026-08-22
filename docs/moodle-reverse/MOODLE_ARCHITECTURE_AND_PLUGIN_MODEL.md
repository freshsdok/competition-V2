# Moodle 架构与插件模型反向文档

## 1. 架构定位

Moodle 是典型模块化单体架构。核心代码提供配置、数据库、缓存、会话、权限、上下文、页面输出、文件、日志、任务、服务注册等基础能力；业务功能通过插件目录扩展。

它不是微服务架构，也不是简单 MVC 目录。更准确的描述是：一个强约定插件平台，所有插件共享同一个运行时、数据库连接、权限模型和页面输出体系。

## 2. 插件类型分布

| 插件类型 | 数量 | 设计含义 |
|---|---:|---|
| block | 40 | 页面区块扩展点 |
| tool | 40 | 管理后台工具扩展点 |
| mod | 23 | 课程活动/学习资源扩展点 |
| repository | 21 | 文件来源与内容仓库扩展点 |
| report | 20 | 报表扩展点 |
| qbank | 19 | 题库管理能力扩展点 |
| qtype | 18 | 题型扩展点 |
| enrol | 13 | 选课/报名方式扩展点 |
| filter | 13 | 内容过滤器扩展点 |
| qbehaviour | 11 | 专项插件扩展点 |
| tinyplugin | 11 | 专项插件扩展点 |
| auth | 9 | 认证方式扩展点 |
| quizaccess | 9 | 专项插件扩展点 |
| gradereport | 7 | 专项插件扩展点 |
| qformat | 7 | 专项插件扩展点 |
| availability | 6 | 可用性条件扩展点 |
| dataformat | 6 | 专项插件扩展点 |
| profilefield | 6 | 专项插件扩展点 |
| customfield | 6 | 专项插件扩展点 |
| aiprovider | 6 | 专项插件扩展点 |
| ltiservice | 6 | 专项插件扩展点 |
| mediaplayer | 5 | 专项插件扩展点 |
| cachestore | 5 | 专项插件扩展点 |
| courseformat | 4 | 专项插件扩展点 |
| gradeexport | 4 | 专项插件扩展点 |
| messageoutput | 4 | 专项插件扩展点 |
| assignfeedback | 4 | 专项插件扩展点 |
| quizreport | 4 | 专项插件扩展点 |
| workshopform | 4 | 专项插件扩展点 |
| gradeimport | 3 | 专项插件扩展点 |
| assignsubmission | 3 | 专项插件扩展点 |
| workshopallocation | 3 | 专项插件扩展点 |
| booktool | 3 | 专项插件扩展点 |
| gradingform | 2 | 专项插件扩展点 |
| theme | 2 | 主题与界面扩展点 |
| communication | 2 | 专项插件扩展点 |
| aiplacement | 2 | 专项插件扩展点 |
| editor | 2 | 专项插件扩展点 |
| gradepenalty | 1 | 专项插件扩展点 |
| contenttype | 1 | 专项插件扩展点 |
| calendartype | 1 | 专项插件扩展点 |
| cachelock | 1 | 专项插件扩展点 |
| workshopeval | 1 | 专项插件扩展点 |

## 3. 插件约定

| 文件/目录 | 反推含义 |
|---|---|
| `version.php` | 插件版本、依赖和升级识别入口 |
| `db/install.xml` | 插件私有表定义，使用 XMLDB 格式 |
| `db/access.php` | 插件权限点注册 |
| `db/services.php` | 插件外部服务/API 注册 |
| `classes/` | PHP 类、任务、事件、服务、输出对象等 |
| `templates/` | Mustache 模板 |
| `amd/src` | 前端模块源码 |
| `tests/` | PHPUnit/Behat 等测试 |
| `lang/en` | 语言包字符串 |

## 4. 核心插件族

| 插件族 | 代表插件 | 设计作用 |
|---|---|---|
| `mod` | assign、quiz、forum、resource、page、lti、scorm、workshop | 承载课程内教学活动和资源 |
| `enrol` | manual、self、guest、cohort、ldap、paypal、lti | 定义用户如何进入课程 |
| `auth` | manual、email、ldap、oauth2、lti、shibboleth | 定义用户身份来源 |
| `repository` | upload、user、filesystem、googledocs、onedrive、s3、webdav | 定义文件来源和外部内容接入 |
| `qtype` | multichoice、essay、shortanswer、numerical、truefalse | 定义题目表达和评分逻辑 |
| `qbank` | managecategories、history、statistics、tagquestion | 定义题库管理能力 |
| `block` | navigation、timeline、myoverview、calendar_month | 定义页面区块 |
| `theme` | boost、classic | 定义界面主题 |
| `tool` | dataprivacy、policy、mfa、recyclebin、uploadcourse | 定义管理后台工具 |

## 5. 可借鉴的架构原则

1. 扩展点先稳定，再让业务实现插件化。
2. 每个插件同时声明数据、权限、服务和界面，而不是只写业务代码。
3. 课程活动用统一 `course_modules` 挂接，避免每种活动直接污染课程表。
4. 对外 API 通过注册表治理，便于移动端、第三方系统和权限审计。
5. 文件、权限、日志都使用统一核心能力，插件只声明归属和语义。

## 6. 风险与成本

- 插件化会带来学习成本，目录多、约定多、跨组件调用多。
- 数据表数量会自然膨胀，需要强命名规范和插件生命周期治理。
- 如果业务规模不大，直接照搬 Moodle 式插件体系会偏重；更适合借鉴其扩展点思想。
