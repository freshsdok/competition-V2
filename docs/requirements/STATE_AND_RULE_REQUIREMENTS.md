# 状态与规则需求反推

## 状态字段候选

| 业务域 | 位置 | 代码 |
| --- | --- | --- |
| 支付 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/CompetitionCertExchangeApply.java:93` | @Excel(name = "订单状态", readConverterExp = "pending=待支付,paid=已支付,refunded=已退款,cancelled=已取消,failed=支付失败") |
| 支付 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/OrderInfo.java:68` | @Excel(name = "状态", readConverterExp = "pending=待支付,paid=已支付,refunded=已退款,cancelled=已取消,approving=待审核,approve_rejected=审核不通过") |
| 支付 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/OrderInfo.java:242` | * 退款状态（refunding-退款中，refunded-已退款） |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictData.java:53` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictData.java:149` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictData.java:152` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictData.java:154` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictData.java:168` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictType.java:35` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictType.java:74` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictType.java:77` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictType.java:79` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysDictType.java:88` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysErrorLog.java:83` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysErrorLog.java:271` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysErrorLog.java:274` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysErrorLog.java:276` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysLogininfor.java:28` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysLogininfor.java:73` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysLogininfor.java:76` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysLogininfor.java:78` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOperLog.java:71` | private Integer status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOperLog.java:218` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOperLog.java:221` | public void setStatus(Integer status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOperLog.java:223` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOrg.java:65` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOrg.java:192` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOrg.java:194` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOrg.java:199` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysOrg.java:264` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysRole.java:55` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysRole.java:201` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysRole.java:204` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysRole.java:206` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysRole.java:364` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysUser.java:107` | private String status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysUser.java:377` | return status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysUser.java:380` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysUser.java:382` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-api/teaching-api-system/src/main/java/com/teaching/system/api/domain/SysUser.java:602` | .append("status", getStatus()) |
| 支付 | `old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:6` | public static final String PAY_STATUS = "pay_status";  //支付成功 |
| 系统用户与权限 | `old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:8` | public static final String PENDING = "pending";  //待支付 |
| 系统用户与权限 | `old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:12` | public static final String PAID = "paid";  //已支付 |
| 系统用户与权限 | `old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:13` | public static final String CANCELLED = "cancelled";  //已取消 |
| 支付 | `old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/constant/DictConstant.java:18` | public static final String REFUNDED = "refunded";  //已退款 |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/AwardPublicity.java:61` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/AwardPublicity.java:138` | return status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/AwardPublicity.java:141` | public void setStatus(String status) { |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/AwardPublicity.java:142` | this.status = status; |
| 内容管理 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNotice.java:40` | private String status; |
| 内容管理 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneNoticeForm.java:45` | private String status; |
| 现场证件 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneOneCardAction.java:18` | private String status; |
| 扫码/小程序 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneScanAction.java:14` | private String status; |
| 赛场安排 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneSchedule.java:54` | private String status; |
| 赛场安排 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/domain/CompetitionSceneScheduleTarget.java:58` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewActivity.java:46` | private String status; |
| 评审任务 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewAssignment.java:30` | private String status; |
| 现场对象 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewObjectMaterial.java:43` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewPanel.java:26` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewPanelMember.java:26` | private String status; |
| 评审评分 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewResultPublishLog.java:31` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewRound.java:32` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewSession.java:41` | private String status; |
| 评审材料 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewSubmissionPermission.java:26` | private String status; |
| 文件 | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/domain/ReviewerProfile.java:36` | private String status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ComponentLibraryInfo.java:78` | private String status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ComponentLibraryInfo.java:175` | public void setStatus(String status) { |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ComponentLibraryInfo.java:176` | this.status = status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ComponentLibraryInfo.java:180` | return status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ComponentLibraryInfo.java:226` | .append("status", getStatus()) |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentColumn.java:89` | private String status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentColumn.java:188` | return status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentColumn.java:191` | public void setStatus(String status) { |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentColumn.java:192` | this.status = status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentColumn.java:232` | .append("status", getStatus()) |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentFile.java:70` | private String status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentFile.java:134` | return status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentFile.java:137` | public void setStatus(String status) { |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentFile.java:138` | this.status = status; |
| 内容管理 | `old-code/teaching-modules/teaching-content/src/main/java/com/teaching/content/domain/ContentFile.java:159` | .append("status", getStatus()) |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseClassifyInfo.java:69` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseClassifyInfo.java:158` | public void setStatus(String status) { |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseClassifyInfo.java:159` | this.status = status; |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseClassifyInfo.java:163` | return status; |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseClassifyInfo.java:208` | .append("status", getStatus()) |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseRecommendInfo.java:76` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseRecommendInfo.java:178` | public void setStatus(String status) { |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseRecommendInfo.java:179` | this.status = status; |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseRecommendInfo.java:183` | return status; |
| 其他 | `old-code/teaching-modules/teaching-course/src/main/java/com/teaching/course/domain/CourseRecommendInfo.java:236` | .append("status", getStatus()) |
| 流程审批 | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/domain/vo/WfDeployVo.java:87` | private String status; |
| 流程审批 | `old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/domain/vo/WfOwnTaskExportVo.java:56` | private String status; |
| 即时通讯 | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/entity/GroupMessage.java:81` | private Integer status; |
| 即时通讯 | `old-code/teaching-modules/teaching-imPlatform/src/main/java/com/teaching/implatform/entity/PrivateMessage.java:54` | private Integer status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJob.java:56` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJob.java:146` | return status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJob.java:149` | public void setStatus(String status) |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJob.java:151` | this.status = status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJob.java:164` | .append("status", getStatus()) |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJobLog.java:41` | private String status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJobLog.java:105` | return status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJobLog.java:108` | public void setStatus(String status) |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJobLog.java:110` | this.status = status; |
| 其他 | `old-code/teaching-modules/teaching-job/src/main/java/com/teaching/job/domain/SysJobLog.java:150` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/ExportManage.java:49` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/ExportManage.java:130` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/ExportManage.java:132` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/ExportManage.java:137` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/ExportManage.java:195` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/MerchantParamConfig.java:115` | private Long status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/MerchantParamConfig.java:379` | public void setStatus(Long status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/MerchantParamConfig.java:381` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/MerchantParamConfig.java:386` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/MerchantParamConfig.java:478` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/OrderStatementRecord.java:47` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/OrderStatementRecord.java:120` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/OrderStatementRecord.java:122` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/OrderStatementRecord.java:127` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/OrderStatementRecord.java:169` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysMenu.java:91` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysMenu.java:307` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysMenu.java:310` | public void setStatus(String status) { |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysMenu.java:311` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysMenu.java:378` | .append("status ", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysNotice.java:32` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysNotice.java:77` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysNotice.java:79` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysNotice.java:84` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysNotice.java:94` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysPost.java:39` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysPost.java:91` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysPost.java:94` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysPost.java:96` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/SysPost.java:116` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/TeacherTmpInfo.java:86` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/TeacherTmpInfo.java:248` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/TeacherTmpInfo.java:250` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/TeacherTmpInfo.java:255` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/TeacherTmpInfo.java:277` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/WechatIntegration.java:46` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/WechatIntegration.java:118` | return status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/WechatIntegration.java:121` | public void setStatus(String status) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/WechatIntegration.java:123` | this.status = status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/WechatIntegration.java:174` | .append("status", getStatus()) |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/domain/vo/invoice/InvoiceQueryResult.java:17` | private String status; |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/IdentityInfoServiceImpl.java:229` | String status = insertSysAuditTask(identityInfo); |
| 系统用户与权限 | `old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/IdentityInfoServiceImpl.java:230` | identityInfo.setCheckStatus(status); |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:54` | `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态: 0启用/1停用', |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:66` | KEY `idx_scene_schedule_status` (`status`, `del_flag`) |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:105` | `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态: 0有效/1停用', |
| 赛场安排 | `db/competition_scene_credential_resource_merged_20260705.sql:115` | KEY `idx_scene_target_schedule` (`schedule_id`, `status`, `del_flag`), |
| 资源预约 | `db/competition_scene_credential_resource_merged_20260705.sql:398` | `reservation_status` varchar(32) NOT NULL DEFAULT 'RESERVED' COMMENT '预约状态: RESERVED/CANCELLED/CHECKED', |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:401` | `check_status` varchar(32) NOT NULL DEFAULT 'UNCHECKED' COMMENT '核销状态: UNCHECKED/CHECKED', |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:412` | KEY `idx_scene_reservation_slot` (`slot_id`, `reservation_status`, `deleted`), |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:413` | KEY `idx_scene_reservation_subject` (`schedule_id`, `subject_type`, `subject_code`, `reservation_status`, `deleted`), |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:503` | `operation_status` varchar(32) NOT NULL COMMENT '操作状态: DONE/CANCELLED/INVALID', |
| 赛事主数据 | `db/competition_scene_credential_resource_merged_20260705.sql:520` | KEY `idx_scene_subject_operation_lookup` (`competition_series_id`, `scope_type`, `scope_ref_id`, `subject_type`, `subject_code`, `operation_type`, `operation_status`, `deleted`), |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:644` | visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:662` | visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) |
| 现场证件 | `db/competition_scene_credential_resource_merged_20260705.sql:669` | visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) |

## 状态模型反推

| 状态域 | 反向规则 | 证据等级 | 重构要求 |
| --- | --- | --- | --- |
| 报名/团队 check_status | 报名、团队、成员均存在审核/校验状态，且和支付、实名认证联动。 | CONFIRMED_BY_CODE | 明确 check_status 字典和状态流转，避免跨表同名不同义。 |
| 支付 pay_status/refund_status | 订单存在 pending/paid/refunding/refunded/cancelled 等状态。 | CONFIRMED_BY_CODE | 统一 CANCELED/CANCELLED，回调必须幂等。 |
| 现场 credential status | 证件存在发放/撤销/删除/类型等状态或快照字段。 | STATIC_INFERENCE | 证件状态作为主体事实，不能由日志反推。 |
| grant active status | 一证多权授权需要 active/撤销/失效状态。 | STATIC_INFERENCE | 建立 active grant 唯一性和能力码枚举。 |
| operation_state operation_status | 现场操作存在 DONE/CANCELLED/INVALID 等状态。 | CONFIRMED_BY_CODE | operation_state 作为当前事实源，operation_log 作为流水。 |
| reservation_status/check_status | 资源预约存在 RESERVED/CANCELLED/CHECKED 等状态。 | CONFIRMED_BY_CODE | 预约创建/取消需幂等并维护容量一致性。 |
| slot_status/booking_status | 资源部署和时段控制是否可预约。 | STATIC_INFERENCE | 明确谁控制开放、谁控制容量。 |
| review status | 评审活动、对象、提交、记录、结果均有状态。 | CONFIRMED_BY_CODE | 建立评审状态机，避免秘书状态和专家评分互相覆盖。 |
| deleted/del_flag | 多表软删除并存 deleted/del_flag。 | CONFIRMED_BY_CODE | 新模型统一软删除字段和查询过滤规范。 |

## 规则需求
- 所有写入流程必须定义幂等键或自然唯一键。
- 快照字段要显式标注，不应被当作主数据事实。
- 导入数据要保留原始文本、校验状态和错误原因。
- 状态枚举需要统一注册表，旧值做兼容映射。
