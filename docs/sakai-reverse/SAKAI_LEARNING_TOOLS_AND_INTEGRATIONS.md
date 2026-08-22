# Sakai 教学工具与集成特点

## 1. 主要教学工具

Sakai 工具模块覆盖教学协作的主要场景：

- `assignment`：作业。
- `samigo`：测验和题库。
- `gradebookng`：成绩册。
- `lessonbuilder`：课程内容页面。
- `content`：Resources 和 Drop Box。
- `announcement`、`calendar`、`chat`、`msgcntr`、`conversations`：沟通协作。
- `lti`、`basiclti`、`plus`：外部工具集成。
- `rubrics`：评分量规。
- `roster2`、`sections`、`site-manage`：成员、分组和站点管理。

## 2. 集成方式

Sakai 的集成能力包括：

- EntityBroker / WebServices / WebAPI。
- LTI/BasicLTI/Plus。
- WebDAV/DAV。
- Microsoft/Cloud Storage。
- Content Review。
- Search。

## 3. 设计特点

Sakai 不把所有能力塞进课程核心，而是让工具依赖 Kernel 服务并挂到 Site 页面上。这种工具化结构比单体业务页面更利于治理。
