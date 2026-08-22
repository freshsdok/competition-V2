# Moodle 接口、前端与测试模型反向文档

## 1. 外部接口模型

Moodle 的外部接口由 `db/services.php` 注册，数据库中有 `external_services`、`external_functions` 等核心表，源码中扫描到 521 个服务注册项。服务函数通常通过 external 类定义参数、返回值和权限检查。

### 典型设计

- 服务注册：`*/db/services.php`
- 服务函数实现：`externallib.php` 或 `classes/external/*`
- 服务元数据表：`external_services`、`external_functions`
- 访问控制：token、service、capability、context 共同约束

## 2. 前端模型

源码中存在 1272 个 Mustache 模板。Moodle 前端不是完全前后端分离，而是服务端渲染为主，并结合 Mustache 模板、AMD JS 模块、主题插件和局部 Ajax。

| 前端组成 | 反推作用 |
|---|---|
| `theme/*` | 主题插件，控制整体视觉与布局 |
| `templates/*.mustache` | 服务端/客户端复用模板 |
| `amd/src` | 前端 JS 模块源码 |
| `pix` | 图标和图片资源 |
| `scss/css` | 样式资源 |

## 3. 测试模型

插件目录中大量存在 `tests`、Behat feature 和 PHPUnit 测试文件。反向看，Moodle 的质量策略是：核心平台和插件分别承担测试，插件需要在自己的目录内验证权限、服务、业务行为和界面流程。

| 测试类型 | 代码位置特征 | 说明 |
|---|---|---|
| PHPUnit | `tests/*_test.php` | 单元和集成测试 |
| Behat | `tests/behat/*.feature` | 端到端行为测试 |
| Fixtures | `tests/fixtures` | 测试插件/测试数据 |
| 生成器 | `tests/generator` 或 data generator | 构造课程、用户、活动等测试对象 |

## 4. 对本项目的启发

1. API 不宜直接散落在控制器中，建议建立接口注册或接口清单机制。
2. 前端如果仍以服务端页面为主，可先引入模板组件化，而不是一次性改造成大型前端单页应用。
3. 插件化或模块化功能必须配套模块级测试，否则扩展点会很快失控。
4. 对移动端/第三方调用，应把权限能力、接口能力、文件能力统一纳入审计清单。
