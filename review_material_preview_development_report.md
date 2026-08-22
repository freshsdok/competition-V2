# 评审附件材料在线预览开发报告

## 1. 后端修改文件

- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/controller/ReviewMaterialController.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/IReviewFilePreviewService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewFilePreviewServiceImpl.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/OfficeToPdfConvertService.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/config/ReviewPreviewProperties.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/vo/ReviewMaterialPreviewVO.java`
- `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/vo/ReviewPreviewResource.java`
- `old-code/teaching-modules/teaching-competition/src/main/resources/bootstrap.yml`

## 2. 前端修改文件

- `old-code-admin/src/views/review/my-review/index.vue`
- `old-code-admin/src/views/review/my-review/components/ReviewMaterialPreview.vue`
- `old-code-admin/src/api/review/materialPreview.js`
- `old-code-admin/package.json`

其中 `index.vue` 额外增加了“评审对象资料”和“人员信息”的展开/收起按钮，人员信息默认收起；并在左侧材料区与右侧评分区中间增加拖拽分隔条，可调整两侧宽度，以释放附件预览空间。图片材料预览直接使用材料原始 `fileUrl` 展示，PDF、DOCX、Excel 改为使用 `vue-office` 前端组件直链预览，附件下载直接打开材料原始链接。

## 3. 新增接口

- `GET /review/material/preview/{fileId}`：返回材料预览元信息。
- `GET /review/material/preview-stream/{fileId}`：返回 PDF、图片、文本或转换后的 PDF 流。
- `GET /review/material/download/{fileId}`：受权限校验保护的材料下载接口。当前教师评审页下载按钮按最新交互要求直接打开材料原始链接，未调用该代理下载接口。

前端经现有网关前缀访问：`/competition/review/material/...`。

## 4. 新增配置项

```yaml
review:
  preview:
    enabled: true
    cache-dir: ${file.path:/tmp}/review-preview
    temp-dir: ${file.path:/tmp}/review-preview-temp
    libreoffice-path: libreoffice
    max-text-preview-size: 1048576
    convert-timeout-seconds: 60
```

## 5. 支持文件类型

- PDF：`pdf`
- 图片：`jpg`、`jpeg`、`png`、`gif`、`bmp`、`webp`
- 文本：`txt`、`md`、`json`、`csv`、`log`
- 前端 Office 组件预览：`docx`、`xls`、`xlsx`
- 后端转 PDF 兜底：`doc`、`ppt`、`pptx`

## 6. 预览策略

- 前端组件预览：PDF、DOCX、Excel 使用 `@vue-office/pdf`、`@vue-office/docx`、`@vue-office/excel`。
- 原生预览：文本。
- 图片直链预览：`jpg`、`jpeg`、`png`、`gif`、`bmp`、`webp` 直接使用材料原始链接展示。
- PDF 转换预览兜底：`doc`、`ppt`、`pptx`。
- 不支持类型：返回 `unsupported`，前端提示“当前文件暂不支持在线预览，请下载查看”并保留下载。

## 7. 权限与安全

- 预览和下载均通过当前登录用户校验材料归属。
- 校验链路为：材料存在且未删除、材料对评审教师可见、材料所属评审对象存在、当前用户被分配到该评审对象。
- 不向前端暴露服务器真实文件路径。
- 本地文件访问按 `file.path` 和 `file.prefix` 解析，并做 `..` 路径穿越拦截。
- 教师评审页的图片、PDF、DOCX、Excel 和下载按最新交互要求直接使用材料原始链接；文本、DOC、PPT/PPTX 等兜底类型仍通过受权限校验的后端预览流读取。

## 8. LibreOffice 要求

仅后端转 PDF 兜底类型需要服务器安装 LibreOffice 或 OpenOffice，并保证配置项 `review.preview.libreoffice-path` 可执行。默认命令为 `libreoffice`。

## 9. 本地验证结果

- 后端编译：`mvn -pl teaching-modules/teaching-competition -am -DskipTests compile` 通过。
- 前端构建：`npm run build:prod` 通过。
- 构建中仅出现项目既有依赖的 eval、sourcemap 和体积类 warning。

## 10. 尚未完成或后续优化

- 本地环境未连接真实评审附件数据，未逐项进行真实 PDF/JPG/TXT/DOCX/PPTX/XLSX/ZIP 浏览器验收。
- 生产环境需确认 `file.path`、`file.prefix`、预览缓存目录、临时目录权限正确。
- 若 `review.preview.cache-dir` 或 `review.preview.temp-dir` 未配置或不可写，后端会优先使用 Nacos 下发的 `file.path` 创建预览缓存/临时目录，再兜底到系统临时目录。
- 大型 Office 文件的转换耗时和版式依赖 LibreOffice 版本，建议上线前用真实评审材料抽样压测。
- Excel 当前按本期要求转 PDF 预览，未实现表格化筛选或编辑。
