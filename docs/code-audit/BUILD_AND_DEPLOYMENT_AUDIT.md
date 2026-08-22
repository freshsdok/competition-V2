# 构建与部署审计

## 构建文件

### Maven
- `old-code/pom.xml`
- `old-code/teaching-api/pom.xml`
- `old-code/teaching-api/teaching-api-system/pom.xml`
- `old-code/teaching-auth/pom.xml`
- `old-code/teaching-common/pom.xml`
- `old-code/teaching-common/teaching-common-client/pom.xml`
- `old-code/teaching-common/teaching-common-core/pom.xml`
- `old-code/teaching-common/teaching-common-datascope/pom.xml`
- `old-code/teaching-common/teaching-common-datasource/pom.xml`
- `old-code/teaching-common/teaching-common-im/pom.xml`
- `old-code/teaching-common/teaching-common-log/pom.xml`
- `old-code/teaching-common/teaching-common-redis/pom.xml`
- `old-code/teaching-common/teaching-common-seata/pom.xml`
- `old-code/teaching-common/teaching-common-security/pom.xml`
- `old-code/teaching-common/teaching-common-sensitive/pom.xml`
- `old-code/teaching-common/teaching-common-swagger/pom.xml`
- `old-code/teaching-gateway/pom.xml`
- `old-code/teaching-modules/pom.xml`
- `old-code/teaching-modules/teaching-competition/pom.xml`
- `old-code/teaching-modules/teaching-content/pom.xml`
- `old-code/teaching-modules/teaching-course/pom.xml`
- `old-code/teaching-modules/teaching-file/pom.xml`
- `old-code/teaching-modules/teaching-flowable/pom.xml`
- `old-code/teaching-modules/teaching-gen/pom.xml`
- `old-code/teaching-modules/teaching-imPlatform/pom.xml`
- `old-code/teaching-modules/teaching-imServer/pom.xml`
- `old-code/teaching-modules/teaching-job/pom.xml`
- `old-code/teaching-modules/teaching-system/pom.xml`
- `old-code/teaching-modules/teaching-wxApp/pom.xml`
- `old-code/teaching-visual/pom.xml`
- `old-code/teaching-visual/teaching-monitor/pom.xml`

### NPM
- `old-code-admin/package.json`
- `old-code-pc/package.json`

## 前端依赖摘要

| package.json | 依赖样本 |
| --- | --- |
| old-code-admin/package.json | @element-plus/icons-vue, @highlightjs/vue-plugin, @vant/touch-emulator, @vue-office/docx, @vue-office/excel, @vue-office/pdf, @vue-office/pptx, @vueup/vue-quill, @vueuse/core, @wangeditor/editor, @wangeditor/editor-for-vue, ali-oss, axios, bpmn-js-token-simulation, clipboard, decimal.js, echarts, element-plus, file-saver, fuse.js |
| old-code-pc/package.json | @element-plus/icons, @vueup/vue-quill, @vueuse/core, ali-oss, amfe-flexible, animate.css, autoprefixer, axios, bpmn-js, bpmn-js-task-resize, bpmn-js-token-simulation, codemirror-editor-vue3, decimal.js, diagram-js, diagram-js-minimap, element-plus, file-saver, gsap, highlight.js, hover.css |

## 配置/脚本风险

| 风险 | 证据 | 建议 |
| --- | --- | --- |
| 环境配置分散 | bootstrap/application、docker、scripts 同时存在。 | 建立环境矩阵和配置模板。 |
| 敏感配置占位/明文风险 | old-code/docker/docker-compose.yml:40 | 密钥迁移出仓库。 |
| 过程/打包/vendor 目录污染 | dist/node_modules/target/logs/.idea/bin/lib/pc/uni_modules 在工作区存在。 | 源代码包和 CI 排除。 |
| 回滚剧本不足 | 未静态确认统一 rollback 流程。 | 补充发布、验证、回滚三段式脚本说明。 |

## 本轮未执行
为保持只读审计，本轮未执行 Maven/NPM 构建、未跑测试、未启动服务。构建风险基于文件结构和配置静态分析。
