# 代码库清单

审计口径：只读静态扫描，排除 `.git`、`node_modules`、`target`、`dist`、`build`、`unpackage`、`logs`、`.idea`、`bin`、`tmp`、`artifacts`、`uni_modules`，以及 `old-code-admin/lib`、`old-code-pc/lib`、`old-code-pc/pc` 等 vendor/打包产物；这些目录另作为工程治理问题记录。

## 顶层范围

| 目录 | 文件数 | 代码/配置行数 |
| --- | --- | --- |
| old-code | 1817 | 240854 |
| old-code-admin | 476 | 124287 |
| old-code-pc | 217 | 71057 |
| old-code-mini | 47 | 7459 |
| db | 23 | 3216 |
| scripts | 3 | 492 |

## 文件类型分布

| 类型 | 文件数 | 行数 |
| --- | --- | --- |
| .java | 1545 | 201353 |
| .vue | 448 | 139758 |
| .js | 274 | 22707 |
| .xml | 176 | 34587 |
| .yml | 62 | 1706 |
| pom.xml | 31 | 3003 |
| .sql | 23 | 3216 |
| .json | 11 | 40052 |
| .md | 4 | 274 |
| .sh | 4 | 421 |
| package.json | 2 | 156 |
| .ts | 2 | 100 |
| .properties | 1 | 32 |

## 模块清单

| 模块 | 文件数 | 行数 |
| --- | --- | --- |
| db | 23 | 3216 |
| old-code | 2 | 392 |
| old-code-admin | 476 | 124287 |
| old-code-mini | 47 | 7459 |
| old-code-pc | 217 | 71057 |
| old-code/docker | 4 | 280 |
| old-code/teaching-api/pom.xml | 1 | 22 |
| old-code/teaching-api/teaching-api-system | 89 | 20335 |
| old-code/teaching-auth | 14 | 1054 |
| old-code/teaching-common/pom.xml | 1 | 32 |
| old-code/teaching-common/teaching-common-client | 11 | 568 |
| old-code/teaching-common/teaching-common-core | 91 | 12611 |
| old-code/teaching-common/teaching-common-datascope | 3 | 225 |
| old-code/teaching-common/teaching-common-datasource | 3 | 82 |
| old-code/teaching-common/teaching-common-im | 26 | 1119 |
| old-code/teaching-common/teaching-common-log | 9 | 506 |
| old-code/teaching-common/teaching-common-redis | 5 | 659 |
| old-code/teaching-common/teaching-common-seata | 1 | 27 |
| old-code/teaching-common/teaching-common-security | 21 | 1866 |
| old-code/teaching-common/teaching-common-sensitive | 5 | 228 |
| old-code/teaching-common/teaching-common-swagger | 3 | 234 |
| old-code/teaching-gateway | 25 | 1536 |
| old-code/teaching-modules/pom.xml | 1 | 32 |
| old-code/teaching-modules/teaching-competition | 629 | 85666 |
| old-code/teaching-modules/teaching-content | 69 | 9213 |
| old-code/teaching-modules/teaching-course | 31 | 4215 |
| old-code/teaching-modules/teaching-file | 23 | 4895 |
| old-code/teaching-modules/teaching-flowable | 116 | 14495 |
| old-code/teaching-modules/teaching-gen | 21 | 3260 |
| old-code/teaching-modules/teaching-imPlatform | 133 | 7903 |
| old-code/teaching-modules/teaching-imServer | 29 | 1302 |
| old-code/teaching-modules/teaching-job | 37 | 2816 |
| old-code/teaching-modules/teaching-system | 368 | 60169 |
| old-code/teaching-modules/teaching-wxApp | 38 | 4772 |
| old-code/teaching-visual/pom.xml | 1 | 22 |
| old-code/teaching-visual/teaching-monitor | 7 | 318 |
| scripts | 3 | 492 |

## Java 分层统计

| 类别 | 数量 |
| --- | --- |
| OtherJava | 392 |
| Domain/Entity | 326 |
| Service | 212 |
| ServiceImpl | 188 |
| Mapper | 171 |
| Controller | 162 |
| DTO/VO | 75 |
| Test | 19 |

## Maven / NPM 清单

### Maven pom.xml
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

### package.json
- `old-code-admin/package.json`
- `old-code-pc/package.json`

## 高风险/高复杂度文件 Top 40

| 文件 | 行数 | 静态风险分 |
| --- | --- | --- |
| old-code-admin/src/utils/generator/html.js | 359 | 2115.4 |
| scripts/review/start_review_uat.sh | 249 | 1439.0 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionApplyInfoMapper.xml | 2057 | 1315.8 |
| old-code-admin/src/views/course/courseInfo/index.vue | 3232 | 1146.6 |
| old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/service/OSSFileServiceImpl.java | 2644 | 1115.2 |
| old-code-admin/src/views/tournament/sceneSchedule/index.vue | 3150 | 1017.0 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewObjectServiceImpl.java | 2331 | 841.0 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneCredentialMapper.xml | 758 | 765.4 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneVerifyServiceImpl.java | 1742 | 700.6 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionSceneScheduleTargetMapper.xml | 434 | 671.2 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionServiceImpl.java | 2267 | 659.4 |
| old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/MerchantParamConfigMapper.xml | 227 | 616.4 |
| old-code-pc/src/views/personal/components/TeamDetails.vue | 2467 | 615.9 |
| old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/OrderInfoServiceImpl.java | 1971 | 610.5 |
| old-code-admin/src/views/content/page/editContent/utils/componentRenderer.js | 1274 | 591.2 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneScheduleServiceImpl.java | 1463 | 583.1 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionPromotedApplyInfoMapper.xml | 581 | 575.0 |
| old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/PayServiceImpl.java | 835 | 556.8 |
| old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/SysUserMapper.xml | 970 | 539.0 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/TeamManagerInfoMapper.xml | 839 | 511.9 |
| old-code-admin/src/views/content/page/editContent/components/PropertyPanel.vue | 1959 | 504.9 |
| old-code-admin/src/views/review/my-review/index.vue | 1718 | 504.9 |
| old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/InvoiceInfoMapper.xml | 274 | 490.7 |
| old-code-admin/src/utils/generator/js.js | 370 | 476.0 |
| old-code/teaching-modules/teaching-course/src/main/resources/mapper/course/CourseInfoMapper.xml | 498 | 456.9 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/UserCertificateMapper.xml | 521 | 452.1 |
| old-code-pc/src/views/personal/personaltabs/Competition.vue | 2073 | 445.6 |
| old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/poi/ExcelUtil.java | 1941 | 443.6 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSecretaryServiceImpl.java | 537 | 439.9 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImpl.java | 1256 | 439.3 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewMyReviewServiceImpl.java | 1105 | 422.8 |
| old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/FileUploadManagerMapper.xml | 287 | 419.4 |
| old-code/teaching-modules/teaching-system/src/main/resources/mapper/system/OrderInfoMapper.xml | 352 | 412.1 |
| old-code-mini/pages/my-credential/index.vue | 1448 | 404.9 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/CompetitionGradeInfoMapper.xml | 621 | 403.1 |
| old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/service/impl/WfProcessServiceImpl.java | 1937 | 396.9 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionApplyInfoServiceImpl.java | 1501 | 391.6 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/competition/UserCertificateOriginMapper.xml | 276 | 388.8 |
| old-code/teaching-modules/teaching-competition/src/main/resources/mapper/review/ReviewObjectMapper.xml | 265 | 385.2 |
| old-code-mini/pages/review-secretary/index.vue | 1028 | 383.4 |

## 特别说明
- 需求中提到的 `tianda-miniprogram` 目录本仓库未发现；本轮按实际存在的 `old-code-mini` 完成小程序端审计。
- `old-code-admin/dist`、`old-code-pc/node_modules`、`old-code-mini/unpackage`、`old-code/logs`、`old-code-admin/lib`、`old-code-pc/lib`、`old-code-pc/pc` 等不纳入核心代码质量统计，但应纳入仓库卫生治理。
