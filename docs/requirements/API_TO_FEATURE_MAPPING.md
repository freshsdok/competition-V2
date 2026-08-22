# 接口到功能映射

## 后端接口样本

| 业务域 | 功能类型 | 方法 | 路径 | Controller | 权限 | 位置 |
| --- | --- | --- | --- | --- | --- | --- |
| 系统用户与权限 | 业务操作 | POST | /login | TokenController |  | `old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:34` |
| 系统用户与权限 | 删除/作废 | DELETE | /logout | TokenController |  | `old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:46` |
| 系统用户与权限 | 业务操作 | POST | /refresh | TokenController |  | `old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:68` |
| 系统用户与权限 | 业务操作 | POST | /register | TokenController |  | `old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:80` |
| 系统用户与权限 | 查询/查看 | GET | /awardDetailsUser/awardPublicityList | AwardDetailsUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/AwardDetailsUserController.java:78` |
| 系统用户与权限 | 修改/状态变更、查询/查看 | POST | /awardDetailsUser/updateAwardDetailsList | AwardDetailsUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/AwardDetailsUserController.java:87` |
| 系统用户与权限 | 导出、查询/查看 | POST | /awardDetailsUser/export | AwardDetailsUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/AwardDetailsUserController.java:96` |
| 导入中间表 | 导入 | POST | /publicity/importData | AwardPublicityController | competition:publicity:import | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/AwardPublicityController.java:63` |
| 现场证件 | 查询/查看 | GET | /competition/candidateCertInfo/list | CandidateCertInfoController | competition:candidateCertInfo:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:60` |
| 现场证件 | 导出、查询/查看 | POST | /competition/candidateCertInfo/export | CandidateCertInfoController | competition:candidateCertInfo:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:71` |
| 现场证件 | 查询/查看 | GET | /competition/candidateCertInfo/{candidateId} | CandidateCertInfoController | competition:candidateCertInfo:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:136` |
| 现场证件 | 新增/创建、查询/查看 | POST | /competition/candidateCertInfo/saveCandidateCertInfo | CandidateCertInfoController | competition:candidateCertInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:146` |
| 现场证件 | 新增/创建、查询/查看 | POST | /competition/candidateCertInfo/batchInsertCandidateCertInfo/{certConfigId} | CandidateCertInfoController | competition:candidateCertInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:156` |
| 现场证件 | 查询/查看 | POST | /competition/candidateCertInfo/ | CandidateCertInfoController | competition:candidateCertInfo:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:166` |
| 导入中间表 | 导入、查询/查看 | POST | /competition/candidateCertInfo/importCandidateCertInfo | CandidateCertInfoController | competition:candidateCertInfo:import | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:176` |
| 现场证件 | 新增/创建、查询/查看 | POST | /competition/candidateCertInfo/insertCandidateCertInfoFromAwards | CandidateCertInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:189` |
| 现场证件 | 查询/查看 | GET | /competition/candidateCertInfo/{candidateIds} | CandidateCertInfoController | competition:candidateCertInfo:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CandidateCertInfoController.java:199` |
| 现场证件 | 查询/查看 | GET | /competition/certConfigInfo/list | CertConfigInfoController | competition:certConfigInfo:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:45` |
| 现场证件 | 导出、查询/查看 | POST | /competition/certConfigInfo/export | CertConfigInfoController | competition:certConfigInfo:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:57` |
| 现场证件 | 查询/查看 | GET | /competition/certConfigInfo/getCertConfigInfo/{certConfigId} | CertConfigInfoController | competition:certConfigInfo:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:68` |
| 现场证件 | 新增/创建、查询/查看 | POST | /competition/certConfigInfo/addCertConfigInfo | CertConfigInfoController | competition:certConfigInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:78` |
| 现场证件 | 修改/状态变更、查询/查看 | POST | /competition/certConfigInfo/updateCertConfigInfo | CertConfigInfoController | competition:certConfigInfo:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:88` |
| 现场证件 | 删除/作废、查询/查看 | DELETE | /competition/certConfigInfo/{certConfigIds} | CertConfigInfoController | competition:certConfigInfo:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:98` |
| 报名与团队 | 查询/查看 | POST | /competition/certConfigInfo/cert/getCompetitionApplyInfo | CertConfigInfoController | competition:certConfigInfo:issue | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertConfigInfoController.java:108` |
| 现场证件 | 修改/状态变更、查询/查看 | GET | /competition/certExchangeRuleDetail/list | CertExchangeRuleDetailController | competition:certExchangeRuleDetail:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:40` |
| 现场证件 | 修改/状态变更、导出、查询/查看 | POST | /competition/certExchangeRuleDetail/export | CertExchangeRuleDetailController | competition:certExchangeRuleDetail:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:52` |
| 现场证件 | 修改/状态变更、查询/查看 | GET | /competition/certExchangeRuleDetail/{detailId} | CertExchangeRuleDetailController | competition:certExchangeRuleDetail:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:63` |
| 现场证件 | 修改/状态变更、查询/查看 | POST | /competition/certExchangeRuleDetail/ | CertExchangeRuleDetailController | competition:certExchangeRuleDetail:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:73` |
| 现场证件 | 修改/状态变更、查询/查看 | PUT | /competition/certExchangeRuleDetail/ | CertExchangeRuleDetailController | competition:certExchangeRuleDetail:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:83` |
| 现场证件 | 修改/状态变更、删除/作废、查询/查看 | DELETE | /competition/certExchangeRuleDetail/{detailIds} | CertExchangeRuleDetailController | competition:certExchangeRuleDetail:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertExchangeRuleDetailController.java:93` |
| 现场证件 | 查询/查看 | GET | /competition/certOrgInfo/list | CertOrgInfoController | competition:certOrgInfo:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:39` |
| 现场证件 | 导出、查询/查看 | POST | /competition/certOrgInfo/export | CertOrgInfoController | competition:certOrgInfo:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:51` |
| 现场证件 | 查询/查看 | GET | /competition/certOrgInfo/{orgId} | CertOrgInfoController | competition:certOrgInfo:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:62` |
| 现场证件 | 查询/查看 | POST | /competition/certOrgInfo/ | CertOrgInfoController | competition:certOrgInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:72` |
| 现场证件 | 查询/查看 | PUT | /competition/certOrgInfo/ | CertOrgInfoController | competition:certOrgInfo:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:82` |
| 现场证件 | 删除/作废、查询/查看 | DELETE | /competition/certOrgInfo/{orgIds} | CertOrgInfoController | competition:certOrgInfo:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertOrgInfoController.java:92` |
| 现场证件 | 查询/查看 | GET | /competition/certPlayerInfo/list | CertPlayerInfoController | competition:certPlayerInfo:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:39` |
| 现场证件 | 导出、查询/查看 | POST | /competition/certPlayerInfo/export | CertPlayerInfoController | competition:certPlayerInfo:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:51` |
| 现场证件 | 查询/查看 | GET | /competition/certPlayerInfo/{relaId} | CertPlayerInfoController | competition:certPlayerInfo:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:62` |
| 现场证件 | 查询/查看 | POST | /competition/certPlayerInfo/ | CertPlayerInfoController | competition:certPlayerInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:72` |
| 现场证件 | 查询/查看 | POST | /competition/certPlayerInfo/batch | CertPlayerInfoController | competition:certPlayerInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:82` |
| 现场证件 | 查询/查看 | PUT | /competition/certPlayerInfo/ | CertPlayerInfoController | competition:certPlayerInfo:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:92` |
| 现场证件 | 删除/作废、查询/查看 | DELETE | /competition/certPlayerInfo/{relaIds} | CertPlayerInfoController | competition:certPlayerInfo:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CertPlayerInfoController.java:102` |
| 现场 operation_log | 修改/状态变更、查询/查看 | GET | /log/list | ChangeLogController | competition:log:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/ChangeLogController.java:37` |
| 报名与团队 | 查询/查看 | POST | /competitionApply/list | CompetitionApplyInfoController | competition:competitionApply:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:63` |
| 报名与团队 | 修改/状态变更、支付/退款、查询/查看 | GET | /competitionApply/selectCompetitionApplyInfoByPayStatus | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:71` |
| 报名与团队 | 导出、查询/查看 | POST | /competitionApply/export | CompetitionApplyInfoController | competition:competitionApply:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:82` |
| 报名与团队 | 查询/查看 | GET | /competitionApply/getApplyDetailInfo/{memberId} | CompetitionApplyInfoController | competition:competitionApply:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:181` |
| 报名与团队 | 查询/查看 | GET | /competitionApply/getInnerApplyDetailInfo/{memberId} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:190` |
| 报名与团队 | 查询/查看 | POST | /competitionApply/getInnerApplyDetailInfo | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:197` |
| 报名与团队 | 查询/查看 | POST | /competitionApply/getInnerApplyUserInfo | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:204` |
| 报名与团队 | 新增/创建、查询/查看 | POST | /competitionApply/saveCompetitionApplyInfo | CompetitionApplyInfoController | system:info:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:214` |
| 报名与团队 | 修改/状态变更、查询/查看 | POST | /competitionApply/updateCompetitionApplyInfo | CompetitionApplyInfoController | competition:competitionApply:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:224` |
| 报名与团队 | 修改/状态变更、查询/查看 | POST | /competitionApply/queryTeamMemberInvoiceStatus | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:231` |
| 报名与团队 | 修改/状态变更、支付/退款、查询/查看 | POST | /competitionApply/updatePayStatus | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:239` |
| 报名与团队 | 修改/状态变更、查询/查看 | POST | /competitionApply/updateCompetitionApplyInfoStatus | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:248` |
| 报名与团队 | 删除/作废、查询/查看 | DELETE | /competitionApply/{memberIds} | CompetitionApplyInfoController | competition:competitionApply:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:258` |
| 报名与团队 | 删除/作废、查询/查看 | GET | /competitionApply/removeTeam/{teamCode} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:265` |
| 报名与团队 | 新增/创建、查询/查看 | POST | /competitionApply/saveBatchCompetitionApplyInfo | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:271` |
| 报名与团队 | 删除/作废、查询/查看 | GET | /competitionApply/removeApplyInfo/{memberIds} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:278` |
| 报名与团队 | 查询/查看 | GET | /competitionApply/selectCompetitionApplyTeamCode/{teamCode} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:285` |
| 报名与团队 | 修改/状态变更、支付/退款、查询/查看 | GET | /competitionApply/getCompetitionApplyInfoByPayStatusForUserGroup/{userId} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:297` |
| 报名与团队 | 查询/查看 | POST | /competitionApply/getUserInfoByCompetitions | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:310` |
| 报名与团队 | 查询/查看 | POST | /competitionApply/selectAllUserInfoByCompetitions | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:317` |
| 报名与团队 | 查询/查看 | GET | /competitionApply/getApplyInfoByUsrIdAndCompetitionId/{userId}/{competitionId} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:330` |
| 报名与团队 | 查询/查看 | GET | /competitionApply/getApplyInfoByUsrIdAndCompetitionId/{competitionId} | CompetitionApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionApplyInfoController.java:342` |
| 报名与团队 | 修改/状态变更、查询/查看 | GET | /competition/competitionCertExchangeApply/list | CompetitionCertExchangeApplyController | competition:competitionCertExchangeApply:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:60` |
| 报名与团队 | 修改/状态变更、导出 | POST | /competition/competitionCertExchangeApply/export | CompetitionCertExchangeApplyController | competition:competitionCertExchangeApply:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:72` |
| 报名与团队 | 修改/状态变更、查询/查看 | GET | /competition/competitionCertExchangeApply/{applyId} | CompetitionCertExchangeApplyController | competition:competitionCertExchangeApply:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:137` |
| 报名与团队 | 修改/状态变更 | POST | /competition/competitionCertExchangeApply/ | CompetitionCertExchangeApplyController | competition:competitionCertExchangeApply:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:147` |
| 报名与团队 | 修改/状态变更 | PUT | /competition/competitionCertExchangeApply/ | CompetitionCertExchangeApplyController | competition:competitionCertExchangeApply:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:157` |
| 报名与团队 | 修改/状态变更、删除/作废 | DELETE | /competition/competitionCertExchangeApply/{applyIds} | CompetitionCertExchangeApplyController | competition:competitionCertExchangeApply:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeApplyController.java:167` |
| 现场证件 | 修改/状态变更、查询/查看 | GET | /competition/competitionCertExchangeRule/list | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:42` |
| 现场证件 | 修改/状态变更、查询/查看 | GET | /competition/competitionCertExchangeRule/getList | CompetitionCertExchangeRuleController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:52` |
| 现场证件 | 修改/状态变更、查询/查看 | GET | /competition/competitionCertExchangeRule/getHomeList | CompetitionCertExchangeRuleController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:63` |
| 现场证件 | 修改/状态变更、查询/查看 | POST | /competition/competitionCertExchangeRule/inner/list | CompetitionCertExchangeRuleController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:76` |
| 现场证件 | 修改/状态变更、导出 | POST | /competition/competitionCertExchangeRule/export | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:133` |
| 现场证件 | 修改/状态变更、查询/查看 | GET | /competition/competitionCertExchangeRule/{ruleId} | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:144` |
| 现场证件 | 修改/状态变更、新增/创建 | POST | /competition/competitionCertExchangeRule/saveCompetitionCertExchangeRule | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:154` |
| 现场证件 | 修改/状态变更 | POST | /competition/competitionCertExchangeRule/updateCompetitionCertExchangeRule | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:164` |
| 现场证件 | 修改/状态变更 | POST | /competition/competitionCertExchangeRule/updateCompetitionCertExchangeRuleMain | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:174` |
| 现场证件 | 修改/状态变更、删除/作废、查询/查看 | GET | /competition/competitionCertExchangeRule/remove/{ruleId} | CompetitionCertExchangeRuleController | competition:competitionCertExchangeRule:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeRuleController.java:187` |
| 系统用户与权限 | 修改/状态变更、查询/查看 | GET | /user/competitionCertExchangeRule/queryUserCertExchangeApplyDetail/{rulerId} | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:38` |
| 系统用户与权限 | 修改/状态变更、查询/查看 | GET | /user/competitionCertExchangeRule/queryUserCertExchangeApplyDetailNoAuth/{rulerId} | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:44` |
| 系统用户与权限 | 修改/状态变更、查询/查看 | POST | /user/competitionCertExchangeRule/queryUserCertExchangeApplyDetail | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:53` |
| 系统用户与权限 | 修改/状态变更、新增/创建 | POST | /user/competitionCertExchangeRule/saveUserCertExchangeApply | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:62` |
| 系统用户与权限 | 修改/状态变更、新增/创建 | POST | /user/competitionCertExchangeRule/saveUserCertExchangeApplyCheck | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:72` |
| 系统用户与权限 | 修改/状态变更 | POST | /user/competitionCertExchangeRule/updateUserCertExchangeApply | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:79` |
| 系统用户与权限 | 修改/状态变更 | POST | /user/competitionCertExchangeRule/updateUserCertExchangeApplyInvoiceStatus | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:86` |
| 系统用户与权限 | 修改/状态变更、查询/查看 | POST | /user/competitionCertExchangeRule/list | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:94` |
| 现场证件 | 修改/状态变更、查询/查看 | POST | /user/competitionCertExchangeRule/getCompetitionCertificateList | CompetitionCertExchangeUserController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionCertExchangeUserController.java:104` |
| 导入中间表 | 导入、查询/查看 | POST | /competition/competitionGradeInfo/importGradeInfo | CompetitionGradeInfoController | competition:competitionGradeInfo:import | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionGradeInfoController.java:71` |
| 导入中间表 | 导入、查询/查看 | POST | /competition/competitionGradeInfo/importTemplate | CompetitionGradeInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionGradeInfoController.java:91` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/list | CompetitionMainInfoController | competition:competitionManager:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:56` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/queryCompetitionInfo | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:66` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/pullDownList | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:73` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/queryCompetitionInfoByCompetitionName | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:79` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/getCompetitionDetailInfoById | CompetitionMainInfoController | competition:competitionManager:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:89` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/queryNowCompetitionStageConfig | CompetitionMainInfoController | competition:competitionManager:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:102` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/getNoStartCompetitionInfo | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:112` |
| 赛事主数据 | 新增/创建、查询/查看 | POST | /competitionManager/saveCompetitionInfo | CompetitionMainInfoController | competition:competitionManager:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:123` |
| 赛事主数据 | 修改/状态变更、查询/查看 | POST | /competitionManager/updateCompetitionInfo | CompetitionMainInfoController | competition:competitionManager:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:134` |
| 赛事主数据 | 删除/作废、查询/查看 | POST | /competitionManager/removeCompetitionMainInfo | CompetitionMainInfoController | competition:competitionManager:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:145` |
| 赛事主数据 | 修改/状态变更、查询/查看 | POST | /competitionManager/updateCompetitionInfoStatus | CompetitionMainInfoController | competition:competitionManager:editStatus | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:156` |
| 赛事主数据 | 修改/状态变更、查询/查看 | POST | /competitionManager/updateTaskCompetitionInfoStatus | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:163` |
| 赛事主数据 | 新增/创建、查询/查看 | POST | /competitionManager/insertCompetitionStageConfig | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:169` |
| 赛事主数据 | 修改/状态变更、查询/查看 | POST | /competitionManager/updateInnerCompetitionInfoStatus | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:177` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/getInnerCompetitionDetailInfo | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:185` |
| 赛事主数据 | 查询/查看 | GET | /competitionManager/selectAllCompetitionDetailInfo | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:196` |
| 系统用户与权限 | 查询/查看 | GET | /competitionManager/selectAllCompetitionDetailInfoForUserGroup | CompetitionMainInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionMainInfoController.java:245` |
| 报名与团队 | 查询/查看 | GET | /promotedApplyInfo/list | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:57` |
| 报名与团队 | 查询/查看 | GET | /promotedApplyInfo/pcList | CompetitionPromotedApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:74` |
| 报名与团队 | 导出、查询/查看 | POST | /promotedApplyInfo/export | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:export | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:91` |
| 导入中间表 | 导入、查询/查看 | POST | /promotedApplyInfo/import | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:import | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:103` |
| 报名与团队 | 导出、查询/查看 | POST | /promotedApplyInfo/export | CompetitionPromotedApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:120` |
| 报名与团队 | 导出、查询/查看 | POST | /promotedApplyInfo/pcExport | CompetitionPromotedApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:195` |
| 报名与团队 | 查询/查看 | GET | /promotedApplyInfo/{applyId} | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:223` |
| 报名与团队 | 新增/创建、查询/查看 | POST | /promotedApplyInfo/addCompetitionPromotedApplyInfo | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:233` |
| 报名与团队 | 查询/查看 | PUT | /promotedApplyInfo/ | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:243` |
| 报名与团队 | 修改/状态变更、查询/查看 | PUT | /promotedApplyInfo/pcEdit | CompetitionPromotedApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:256` |
| 报名与团队 | 删除/作废、查询/查看 | DELETE | /promotedApplyInfo/{competitionSeriesId}/{teamCode} | CompetitionPromotedApplyInfoController | competition:promotedApplyInfo:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:267` |
| 报名与团队 | 查询/查看 | POST | /promotedApplyInfo/pcApply | CompetitionPromotedApplyInfoController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionPromotedApplyInfoController.java:279` |
| 现场证件 | 查询/查看 | GET | /sceneCredential/list | CompetitionSceneCredentialController | competition:sceneCredential:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:38` |
| 现场证件 | 查询/查看 | GET | /sceneCredential/competitionList | CompetitionSceneCredentialController | competition:sceneCredential:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:46` |
| 现场证件 | 查询/查看 | GET | /sceneCredential/{credentialId} | CompetitionSceneCredentialController | competition:sceneCredential:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:55` |
| 现场证件 | 业务操作 | POST | /sceneCredential/generate | CompetitionSceneCredentialController | competition:sceneCredential:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:62` |
| 现场证件 | 业务操作 | POST | /sceneCredential/competitionDirectIssue | CompetitionSceneCredentialController | competition:sceneCredential:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:69` |
| 现场证件 | 业务操作 | PUT | /sceneCredential/ | CompetitionSceneCredentialController | competition:sceneCredential:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:76` |
| 现场证件 | 删除/作废 | DELETE | /sceneCredential/{credentialIds} | CompetitionSceneCredentialController | competition:sceneCredential:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:83` |
| 现场证件 | 查询/查看 | GET | /sceneCredential/myList | CompetitionSceneCredentialController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneCredentialController.java:88` |
| 内容管理 | 查询/查看 | GET | /sceneNotice/list | CompetitionSceneNoticeController | competition:sceneNotice:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:37` |
| 内容管理 | 查询/查看 | GET | /sceneNotice/{noticeId} | CompetitionSceneNoticeController | competition:sceneNotice:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:45` |
| 内容管理 | 业务操作 | POST | /sceneNotice/ | CompetitionSceneNoticeController | competition:sceneNotice:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:52` |
| 内容管理 | 业务操作 | PUT | /sceneNotice/ | CompetitionSceneNoticeController | competition:sceneNotice:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:59` |
| 内容管理 | 删除/作废 | DELETE | /sceneNotice/{noticeIds} | CompetitionSceneNoticeController | competition:sceneNotice:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:66` |
| 内容管理 | 修改/状态变更 | PUT | /sceneNotice/changeStatus | CompetitionSceneNoticeController | competition:sceneNotice:publish | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:73` |
| 内容管理 | 业务操作 | POST | /sceneNotice/publish/{noticeId} | CompetitionSceneNoticeController | competition:sceneNotice:publish | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:80` |
| 内容管理 | 查询/查看 | GET | /sceneNotice/myList | CompetitionSceneNoticeController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneNoticeController.java:85` |
| 赛场安排 | 查询/查看 | POST | /sceneOneCardIssue/issueByTarget | CompetitionSceneOneCardIssueController | competition:sceneCredential:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardIssueController.java:28` |
| 现场证件 | 扫码/确认 | POST | /sceneOneCardVerify/pilot/scan | CompetitionSceneOneCardVerifyController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java:25` |
| 现场证件 | 扫码/确认 | POST | /sceneOneCardVerify/pilot/confirm | CompetitionSceneOneCardVerifyController |  | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneOneCardVerifyController.java:31` |
| 资源台账 | 查询/查看 | GET | /sceneResource/list | CompetitionSceneResourceController | competition:sceneResource:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:37` |
| 资源台账 | 查询/查看 | GET | /sceneResource/{resourceId} | CompetitionSceneResourceController | competition:sceneResource:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:45` |
| 资源台账 | 业务操作 | POST | /sceneResource/ | CompetitionSceneResourceController | competition:sceneResource:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:52` |
| 资源台账 | 业务操作 | PUT | /sceneResource/ | CompetitionSceneResourceController | competition:sceneResource:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:59` |
| 资源台账 | 删除/作废 | DELETE | /sceneResource/{resourceIds} | CompetitionSceneResourceController | competition:sceneResource:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:66` |
| 资源台账 | 修改/状态变更 | POST | /sceneResource/changeStatus | CompetitionSceneResourceController | competition:sceneResource:changeStatus | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceController.java:73` |
| 资源预约 | 查询/查看、预约 | GET | /sceneResourceReservation/list | CompetitionSceneResourceReservationController | competition:sceneScheduleResource:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceReservationController.java:29` |
| 资源预约 | 查询/查看、预约 | GET | /sceneResourceReservation/{reservationId} | CompetitionSceneResourceReservationController | competition:sceneScheduleResource:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceReservationController.java:38` |
| 资源时段 | 查询/查看 | GET | /sceneResourceSlot/list | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:38` |
| 资源时段 | 查询/查看 | GET | /sceneResourceSlot/{slotId} | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:47` |
| 资源时段 | 业务操作 | POST | /sceneResourceSlot/ | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:54` |
| 资源时段 | 业务操作 | POST | /sceneResourceSlot/batch | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:61` |
| 资源时段 | 业务操作 | PUT | /sceneResourceSlot/ | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:68` |
| 资源时段 | 删除/作废 | DELETE | /sceneResourceSlot/{slotIds} | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:75` |
| 资源时段 | 修改/状态变更 | POST | /sceneResourceSlot/changeStatus | CompetitionSceneResourceSlotController | competition:sceneResourceSlot:changeStatus | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotController.java:82` |
| 资源时段 | 查询/查看 | GET | /sceneResourceSlotGroupScope/listBySlot | CompetitionSceneResourceSlotGroupScopeController | competition:sceneResourceSlot:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotGroupScopeController.java:35` |
| 资源时段 | 查询/查看 | GET | /sceneResourceSlotGroupScope/groupOptions | CompetitionSceneResourceSlotGroupScopeController | competition:sceneResourceSlot:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotGroupScopeController.java:41` |
| 资源时段 | 业务操作 | POST | /sceneResourceSlotGroupScope/replace | CompetitionSceneResourceSlotGroupScopeController | competition:sceneResourceSlot:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotGroupScopeController.java:48` |
| 资源时段 | 业务操作 | POST | /sceneResourceSlotGroupScope/batchReplace | CompetitionSceneResourceSlotGroupScopeController | competition:sceneResourceSlot:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneResourceSlotGroupScopeController.java:56` |
| 赛场安排 | 查询/查看 | GET | /sceneSchedule/list | CompetitionSceneScheduleController | competition:sceneSchedule:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:43` |
| 赛场安排 | 查询/查看 | GET | /sceneSchedule/{scheduleId} | CompetitionSceneScheduleController | competition:sceneSchedule:query | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:51` |
| 赛场安排 | 业务操作 | POST | /sceneSchedule/ | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:58` |
| 赛场安排 | 业务操作 | PUT | /sceneSchedule/ | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:65` |
| 赛场安排 | 删除/作废 | DELETE | /sceneSchedule/{scheduleIds} | CompetitionSceneScheduleController | competition:sceneSchedule:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:72` |
| 赛场安排 | 业务操作 | POST | /sceneSchedule/match/{scheduleId} | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:79` |
| 赛场安排 | 查询/查看 | GET | /sceneSchedule/target/list | CompetitionSceneScheduleController | competition:sceneSchedule:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:85` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/target | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:94` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/target/batch | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:101` |
| 赛场安排 | 查询/查看 | PUT | /sceneSchedule/target | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:108` |
| 赛场安排 | 删除/作废、查询/查看 | DELETE | /sceneSchedule/target/{targetIds} | CompetitionSceneScheduleController | competition:sceneSchedule:remove | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:115` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/review-objects | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:122` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/teams | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:130` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/persons | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:138` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/manual | CompetitionSceneScheduleController | competition:sceneSchedule:add | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:146` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/sequence | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:154` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/sequence/auto-generate | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:162` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/sequence/by-name | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:170` |
| 赛场安排 | 查询/查看 | POST | /sceneSchedule/{scheduleId}/targets/sync-review-session | CompetitionSceneScheduleController | competition:sceneSchedule:edit | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleController.java:178` |
| 赛场安排 | 查询/查看 | GET | /sceneScheduleResource/list | CompetitionSceneScheduleResourceController | competition:sceneScheduleResource:list | `old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/controller/CompetitionSceneScheduleResourceController.java:37` |

## 前端调用样本

| 业务域 | 功能类型 | URL | 调用位置 |
| --- | --- | --- | --- |
| 系统用户与权限 | 业务操作 | /system/task | `old-code-admin/src/api/business/index.js:11` |
| 现场证件 | 查询/查看 | /competition/competition/certConfigInfo/list | `old-code-admin/src/api/certInterconnect/certConfig.js:6` |
| 现场证件 | 查询/查看 | /competition/competition/certConfigInfo/getCertConfigInfo/${id} | `old-code-admin/src/api/certInterconnect/certConfig.js:15` |
| 现场证件 | 新增/创建、查询/查看 | /competition/competition/certConfigInfo/addCertConfigInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:23` |
| 现场证件 | 修改/状态变更、查询/查看 | /competition/competition/certConfigInfo/updateCertConfigInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:32` |
| 现场证件 | 查询/查看 | /competition/competition/certConfigInfo/${ids} | `old-code-admin/src/api/certInterconnect/certConfig.js:41` |
| 现场证件 | 导出、查询/查看 | /competition/competition/certConfigInfo/export | `old-code-admin/src/api/certInterconnect/certConfig.js:49` |
| 赛事主数据 | 查询/查看 | /competition/competitionManager/selectAllCompetitionDetailInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:58` |
| 系统用户与权限 | 查询/查看 | /system/user/group/list | `old-code-admin/src/api/certInterconnect/certConfig.js:67` |
| 现场证件 | 查询/查看 | /competition/competition/certOrgInfo/list | `old-code-admin/src/api/certInterconnect/certConfig.js:78` |
| 报名与团队 | 查询/查看 | /competition/competition/certConfigInfo/cert/getCompetitionApplyInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:89` |
| 现场证件 | 查询/查看 | /competition/competition/candidateCertInfo/list | `old-code-admin/src/api/certInterconnect/certConfig.js:99` |
| 现场证件 | 新增/创建、查询/查看 | /competition/competition/candidateCertInfo/saveCandidateCertInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:108` |
| 现场证件 | 新增/创建、查询/查看 | /competition/competition/candidateCertInfo/batchInsertCandidateCertInfo/${certConfigId} | `old-code-admin/src/api/certInterconnect/certConfig.js:116` |
| 导入中间表 | 导入、查询/查看 | /competition/competition/candidateCertInfo/importCandidateCertInfo | `old-code-admin/src/api/certInterconnect/certConfig.js:125` |
| 现场证件 | 导出、查询/查看 | /competition/competition/candidateCertInfo/export | `old-code-admin/src/api/certInterconnect/certConfig.js:140` |
| 现场证件 | 新增/创建、查询/查看 | /competition/competition/candidateCertInfo/insertCandidateCertInfoFromAwards | `old-code-admin/src/api/certInterconnect/certConfig.js:149` |
| 现场证件 | 查询/查看 | /competition/competition/competitionGradeInfo/list | `old-code-admin/src/api/certInterconnect/gradesManagement.js:7` |
| 现场证件 | 查询/查看 | /competition/competition/competitionGradeInfo/${gradeId} | `old-code-admin/src/api/certInterconnect/gradesManagement.js:17` |
| 现场证件 | 修改/状态变更、查询/查看 | /competition/competition/competitionGradeInfo/updateCompetitionGradeInfo | `old-code-admin/src/api/certInterconnect/gradesManagement.js:25` |
| 现场证件 | 删除/作废、查询/查看 | /competition/competition/competitionGradeInfo/removeCompetitionGradeInfo/${gradeId} | `old-code-admin/src/api/certInterconnect/gradesManagement.js:34` |
| 导入中间表 | 导入、查询/查看 | /competition/competition/competitionGradeInfo/importGradeInfo | `old-code-admin/src/api/certInterconnect/gradesManagement.js:42` |
| 现场证件 | 修改/状态变更、查询/查看 | /competition/competition/competitionGradeInfo/updateGradeInfo | `old-code-admin/src/api/certInterconnect/gradesManagement.js:57` |
| 现场证件 | 修改/状态变更、查询/查看 | /competition/competition/competitionCertExchangeRule/list | `old-code-admin/src/api/certInterconnect/interconnectConfig.js:6` |
| 现场证件 | 修改/状态变更 | /competition/competition/competitionCertExchangeRule/${ruleId} | `old-code-admin/src/api/certInterconnect/interconnectConfig.js:15` |
| 现场证件 | 修改/状态变更、新增/创建 | /competition/competition/competitionCertExchangeRule/saveCompetitionCertExchangeRule | `old-code-admin/src/api/certInterconnect/interconnectConfig.js:23` |
| 现场证件 | 修改/状态变更 | /competition/competition/competitionCertExchangeRule/updateCompetitionCertExchangeRule | `old-code-admin/src/api/certInterconnect/interconnectConfig.js:32` |
| 现场证件 | 修改/状态变更 | /competition/competition/competitionCertExchangeRule/updateCompetitionCertExchangeRuleMain | `old-code-admin/src/api/certInterconnect/interconnectConfig.js:41` |
| 现场证件 | 修改/状态变更、删除/作废 | /competition/competition/competitionCertExchangeRule/remove/${ruleId} | `old-code-admin/src/api/certInterconnect/interconnectConfig.js:50` |
| 现场证件 | 查询/查看 | /competition/competition/userCertificate/list | `old-code-admin/src/api/certInterconnect/userCert.js:6` |
| 现场证件 | 查询/查看 | /competition/competition/userCertificate/getCertInfo | `old-code-admin/src/api/certInterconnect/userCert.js:15` |
| 现场证件 | 新增/创建 | /competition/competition/userCertificate/batchSaveUserCertificate | `old-code-admin/src/api/certInterconnect/userCert.js:24` |
| 现场证件 | 业务操作 | /competition/competition/userCertificate | `old-code-admin/src/api/certInterconnect/userCert.js:33` |
| 现场证件 | 查询/查看 | /competition/competition/userCertificateOrigin/list | `old-code-admin/src/api/certInterconnect/userCert.js:43` |
| 现场证件 | 导出 | /competition/competition/userCertificate/export | `old-code-admin/src/api/certInterconnect/userCert.js:52` |
| 现场证件 | 删除/作废 | /competition/competition/userCertificate/remove | `old-code-admin/src/api/certInterconnect/userCert.js:61` |
| 系统用户与权限 | 修改/状态变更、查询/查看 | /competition/competition/competitionCertExchangeApply/list | `old-code-admin/src/api/certInterconnect/userCertInterconnect.js:6` |
| 系统用户与权限 | 修改/状态变更、导出 | /competition/competition/competitionCertExchangeApply/export | `old-code-admin/src/api/certInterconnect/userCertInterconnect.js:15` |
| 内容管理 | 查询/查看 | /content/bannerInfo/list | `old-code-admin/src/api/content/bannerInfo.js:6` |
| 内容管理 | 查询/查看 | /content/bannerInfo/ | `old-code-admin/src/api/content/bannerInfo.js:15` |
| 内容管理 | 查询/查看 | /content/bannerInfo | `old-code-admin/src/api/content/bannerInfo.js:23` |
| 内容管理 | 查询/查看 | /content/bannerInfo | `old-code-admin/src/api/content/bannerInfo.js:32` |
| 内容管理 | 查询/查看 | /content/bannerInfo/ | `old-code-admin/src/api/content/bannerInfo.js:41` |
| 内容管理 | 查询/查看 | /content/contentColumn/list | `old-code-admin/src/api/content/column.js:6` |
| 内容管理 | 业务操作 | /content/contentColumn/ | `old-code-admin/src/api/content/column.js:15` |
| 系统用户与权限 | 查询/查看 | /content/contentColumn/getByMenuId/ | `old-code-admin/src/api/content/column.js:23` |
| 内容管理 | 业务操作 | /content/contentColumn/tree | `old-code-admin/src/api/content/column.js:31` |
| 内容管理 | 业务操作 | /content/contentColumn | `old-code-admin/src/api/content/column.js:40` |
| 内容管理 | 业务操作 | /content/contentColumn | `old-code-admin/src/api/content/column.js:49` |
| 内容管理 | 业务操作 | /content/contentColumn/ | `old-code-admin/src/api/content/column.js:58` |
| 内容管理 | 业务操作 | /content/contentColumn/ | `old-code-admin/src/api/content/column.js:66` |
| 内容管理 | 查询/查看 | /content/subassembly/list | `old-code-admin/src/api/content/content.js:6` |
| 内容管理 | 查询/查看 | /content/subassembly/getList | `old-code-admin/src/api/content/content.js:14` |
| 内容管理 | 业务操作 | /content/subassembly/ | `old-code-admin/src/api/content/content.js:22` |
| 内容管理 | 业务操作 | /content/subassembly | `old-code-admin/src/api/content/content.js:30` |
| 内容管理 | 业务操作 | /content/subassembly | `old-code-admin/src/api/content/content.js:39` |
| 内容管理 | 业务操作 | /content/subassembly/ | `old-code-admin/src/api/content/content.js:48` |
| 内容管理 | 查询/查看 | /content/contentDetail/list | `old-code-admin/src/api/content/detail.js:6` |
| 内容管理 | 查询/查看 | /content/contentDetail/ | `old-code-admin/src/api/content/detail.js:15` |
| 内容管理 | 查询/查看 | /content/contentDetail/getByColumnId/ | `old-code-admin/src/api/content/detail.js:23` |
| 内容管理 | 查询/查看 | /content/contentDetail | `old-code-admin/src/api/content/detail.js:31` |
| 内容管理 | 查询/查看 | /content/contentDetail | `old-code-admin/src/api/content/detail.js:40` |
| 内容管理 | 查询/查看 | /content/contentDetail/ | `old-code-admin/src/api/content/detail.js:49` |
| 内容管理 | 查询/查看 | /content/contentDetail/ | `old-code-admin/src/api/content/detail.js:57` |
| 内容管理 | 查询/查看 | /content/contentFile/list | `old-code-admin/src/api/content/file.js:6` |
| 内容管理 | 业务操作 | /content/contentFile/ | `old-code-admin/src/api/content/file.js:15` |
| 内容管理 | 查询/查看 | /content/contentFile/getByColumnId/ | `old-code-admin/src/api/content/file.js:23` |
| 内容管理 | 新增/创建 | /content/contentFile/add | `old-code-admin/src/api/content/file.js:31` |
| 内容管理 | 业务操作 | /content/contentFile | `old-code-admin/src/api/content/file.js:40` |
| 内容管理 | 业务操作 | /content/contentFile/ | `old-code-admin/src/api/content/file.js:49` |
| 内容管理 | 业务操作 | /content/contentFile/ | `old-code-admin/src/api/content/file.js:57` |
| 文件 | 业务操作 | /file/upload | `old-code-admin/src/api/content/file.js:65` |
| 内容管理 | 查询/查看 | /content/contentFile/getFileByColumnId/ | `old-code-admin/src/api/content/file.js:74` |
| 内容管理 | 业务操作 | /content/contentFile | `old-code-admin/src/api/content/file.js:82` |
| 内容管理 | 查询/查看 | /content/newsInfo/list | `old-code-admin/src/api/content/newsInfo.js:6` |
| 内容管理 | 查询/查看 | /content/newsInfo/ | `old-code-admin/src/api/content/newsInfo.js:15` |
| 内容管理 | 查询/查看 | /content/newsInfo | `old-code-admin/src/api/content/newsInfo.js:23` |
| 内容管理 | 查询/查看 | /content/newsInfo | `old-code-admin/src/api/content/newsInfo.js:32` |
| 内容管理 | 查询/查看 | /content/newsInfo/ | `old-code-admin/src/api/content/newsInfo.js:41` |
| 内容管理 | 查询/查看 | /content/newsInfo/publish/ | `old-code-admin/src/api/content/newsInfo.js:49` |
| 内容管理 | 查询/查看 | /content/newsInfo/offline/ | `old-code-admin/src/api/content/newsInfo.js:57` |
| 内容管理 | 查询/查看 | /content/newsInfo/submitAudit/ | `old-code-admin/src/api/content/newsInfo.js:65` |
| 内容管理 | 查询/查看 | /content/newsInfo/increaseReading/ | `old-code-admin/src/api/content/newsInfo.js:73` |
| 内容管理 | 查询/查看 | /content/newsInfo/increaseLikes/ | `old-code-admin/src/api/content/newsInfo.js:81` |
| 内容管理 | 查询/查看 | /content/noticeInfo/list | `old-code-admin/src/api/content/noticeInfo.js:6` |
| 内容管理 | 查询/查看 | /content/noticeInfo/ | `old-code-admin/src/api/content/noticeInfo.js:15` |
| 内容管理 | 查询/查看 | /content/noticeInfo | `old-code-admin/src/api/content/noticeInfo.js:23` |
| 内容管理 | 查询/查看 | /content/noticeInfo | `old-code-admin/src/api/content/noticeInfo.js:32` |
| 内容管理 | 查询/查看 | /content/noticeInfo/ | `old-code-admin/src/api/content/noticeInfo.js:41` |
| 内容管理 | 查询/查看 | /content/noticeInfo/publish/ | `old-code-admin/src/api/content/noticeInfo.js:49` |
| 内容管理 | 查询/查看 | /content/noticeInfo/offline/ | `old-code-admin/src/api/content/noticeInfo.js:57` |
| 内容管理 | 查询/查看 | /content/noticeInfo/submitAudit/ | `old-code-admin/src/api/content/noticeInfo.js:65` |
| 内容管理 | 查询/查看 | /content/page/list | `old-code-admin/src/api/content/page.js:6` |
| 内容管理 | 查询/查看 | /content/page/ | `old-code-admin/src/api/content/page.js:15` |
| 内容管理 | 查询/查看 | /content/page | `old-code-admin/src/api/content/page.js:23` |
| 内容管理 | 查询/查看 | /content/page | `old-code-admin/src/api/content/page.js:32` |
| 内容管理 | 查询/查看 | /content/page | `old-code-admin/src/api/content/page.js:41` |
| 内容管理 | 修改/状态变更、查询/查看 | /content/page/editContent | `old-code-admin/src/api/content/page.js:58` |
| 内容管理 | 查询/查看 | /content/page/ | `old-code-admin/src/api/content/page.js:71` |
| 内容管理 | 查询/查看 | /content/page/copy/ | `old-code-admin/src/api/content/page.js:84` |
| 内容管理 | 查询/查看 | /content/questions/list | `old-code-admin/src/api/content/questions.js:6` |
| 内容管理 | 业务操作 | /content/questions/ | `old-code-admin/src/api/content/questions.js:15` |
| 内容管理 | 业务操作 | /content/questions | `old-code-admin/src/api/content/questions.js:23` |
| 内容管理 | 业务操作 | /content/questions | `old-code-admin/src/api/content/questions.js:32` |
| 内容管理 | 业务操作 | /content/questions/ | `old-code-admin/src/api/content/questions.js:41` |
| 内容管理 | 查询/查看 | /content/source/list | `old-code-admin/src/api/content/source.js:6` |
| 内容管理 | 业务操作 | /content/source/ | `old-code-admin/src/api/content/source.js:15` |
| 内容管理 | 业务操作 | /content/source | `old-code-admin/src/api/content/source.js:23` |
| 内容管理 | 业务操作 | /content/source | `old-code-admin/src/api/content/source.js:32` |
| 内容管理 | 业务操作 | /content/source/ | `old-code-admin/src/api/content/source.js:41` |
| 其他 | 查询/查看 | /course/courseInfo/list | `old-code-admin/src/api/course/chapterVideo.js:6` |
| 其他 | 查询/查看 | /course/courseInfo/ | `old-code-admin/src/api/course/chapterVideo.js:15` |
| 其他 | 查询/查看 | /course/courseInfo | `old-code-admin/src/api/course/chapterVideo.js:23` |
| 其他 | 查询/查看 | /course/courseInfo | `old-code-admin/src/api/course/chapterVideo.js:32` |
| 其他 | 业务操作 | /course/chapterVideo/ | `old-code-admin/src/api/course/chapterVideo.js:41` |
| 其他 | 查询/查看 | /course/chapterVideo/getInfoByChapterId/ | `old-code-admin/src/api/course/chapterVideo.js:49` |
| 其他 | 业务操作 | /course/chapterVideo | `old-code-admin/src/api/course/chapterVideo.js:57` |
| 其他 | 业务操作 | /course/chapterVideo | `old-code-admin/src/api/course/chapterVideo.js:66` |
| 其他 | 业务操作 | /course/chapterVideo/ | `old-code-admin/src/api/course/chapterVideo.js:79` |
| 其他 | 导出 | /course/chapterVideo/export | `old-code-admin/src/api/course/chapterVideo.js:87` |
| 文件 | 业务操作 | /file/uploadVideo | `old-code-admin/src/api/course/chapterVideo.js:96` |
| 其他 | 查询/查看 | /course/courseInfo/list | `old-code-admin/src/api/course/courseInfo.js:6` |
| 其他 | 查询/查看 | /course/courseInfo/ | `old-code-admin/src/api/course/courseInfo.js:15` |
| 其他 | 查询/查看 | /course/courseInfo | `old-code-admin/src/api/course/courseInfo.js:23` |
| 其他 | 查询/查看 | /course/courseInfo | `old-code-admin/src/api/course/courseInfo.js:32` |
| 其他 | 查询/查看 | /course/courseInfo/ | `old-code-admin/src/api/course/courseInfo.js:41` |
| 其他 | 导出、查询/查看 | /course/courseInfo/export | `old-code-admin/src/api/course/courseInfo.js:49` |
| 其他 | 修改/状态变更、查询/查看 | /course/courseInfo/updateStatus | `old-code-admin/src/api/course/courseInfo.js:58` |
| 其他 | 查询/查看 | /course/classify/list | `old-code-admin/src/api/course/coursetype.js:6` |
| 其他 | 查询/查看 | /course/classify/getList | `old-code-admin/src/api/course/coursetype.js:14` |
| 其他 | 业务操作 | /course/classify/ | `old-code-admin/src/api/course/coursetype.js:24` |
| 其他 | 业务操作 | /course/classify | `old-code-admin/src/api/course/coursetype.js:32` |
| 其他 | 业务操作 | /course/classify | `old-code-admin/src/api/course/coursetype.js:41` |
| 其他 | 业务操作 | /course/classify/ | `old-code-admin/src/api/course/coursetype.js:50` |
| 其他 | 查询/查看 | /course/recommendInfo/list | `old-code-admin/src/api/course/recommendInfo.js:6` |
| 其他 | 查询/查看 | /course/recommendInfo/ | `old-code-admin/src/api/course/recommendInfo.js:15` |
| 其他 | 查询/查看 | /course/recommendInfo | `old-code-admin/src/api/course/recommendInfo.js:23` |
| 其他 | 查询/查看 | /course/recommendInfo | `old-code-admin/src/api/course/recommendInfo.js:32` |
| 其他 | 查询/查看 | /course/recommendInfo/ | `old-code-admin/src/api/course/recommendInfo.js:41` |
| 其他 | 导出、查询/查看 | /course/recommendInfo/export | `old-code-admin/src/api/course/recommendInfo.js:49` |
| 系统用户与权限 | 查询/查看 | /system/user/group/list | `old-code-admin/src/api/fileTask/index.js:6` |
| 系统用户与权限 | 查询/查看 | /competition/competitionManager/selectAllCompetitionDetailInfoForUserGroup | `old-code-admin/src/api/fileTask/index.js:15` |
| 系统用户与权限 | 业务操作 | /system/userGroup | `old-code-admin/src/api/fileTask/index.js:24` |
| 系统用户与权限 | 业务操作 | /system/userGroup | `old-code-admin/src/api/fileTask/index.js:33` |
| 系统用户与权限 | 查询/查看 | /system/userGroup/list | `old-code-admin/src/api/fileTask/index.js:42` |
| 系统用户与权限 | 业务操作 | /system/userGroup/ | `old-code-admin/src/api/fileTask/index.js:51` |
| 系统用户与权限 | 业务操作 | /system/userGroup/ | `old-code-admin/src/api/fileTask/index.js:59` |
| 文件 | 查询/查看 | /system/fileUploadRecord/list | `old-code-admin/src/api/fileTask/index.js:67` |
| 文件 | 查询/查看 | /system/fileUploadManager/list | `old-code-admin/src/api/fileTask/index.js:76` |
| 文件 | 导出 | /system/fileUploadRecord/exportFiles | `old-code-admin/src/api/fileTask/index.js:85` |
| 文件 | 导出 | /system/fileUploadRecord/export | `old-code-admin/src/api/fileTask/index.js:93` |
| 文件 | 导出 | /system/fileUploadManager/export | `old-code-admin/src/api/fileTask/index.js:102` |
| 文件 | 导出 | /system/fileUploadManager/exportFiles | `old-code-admin/src/api/fileTask/index.js:111` |
| 文件 | 导出、查询/查看 | /system/fileUploadManager/selectExportFiles | `old-code-admin/src/api/fileTask/index.js:120` |
| 文件 | 业务操作 | /file/oss/presignedUrl | `old-code-admin/src/api/fileTask/index.js:128` |
| 文件 | 业务操作 | /file/oss/temporaryVoucher | `old-code-admin/src/api/fileTask/index.js:136` |
| 系统用户与权限 | 查询/查看 | /system/fileDistributeTask/list | `old-code-admin/src/api/fileTask/task.js:6` |
| 系统用户与权限 | 新增/创建 | /system/fileDistributeTask/saveFileTask | `old-code-admin/src/api/fileTask/task.js:16` |
| 系统用户与权限 | 修改/状态变更 | /system/fileDistributeTask/editFileTask | `old-code-admin/src/api/fileTask/task.js:25` |
| 系统用户与权限 | 删除/作废 | /system/fileDistributeTask/remove/${id} | `old-code-admin/src/api/fileTask/task.js:33` |
| 系统用户与权限 | 修改/状态变更 | /system/fileDistributeTask/updateTaskStatus | `old-code-admin/src/api/fileTask/task.js:41` |
| 系统用户与权限 | 导出、查询/查看 | /system/exportManage/list | `old-code-admin/src/api/fileTask/task.js:50` |
| 文件 | 查询/查看 | /system/downLoadRecord/list | `old-code-admin/src/api/fileTask/task.js:59` |
| 支付 | 支付/退款、查询/查看 | /system/order/list | `old-code-admin/src/api/iPayment/index.js:5` |
| 支付 | 支付/退款 | /system/order/${id} | `old-code-admin/src/api/iPayment/index.js:12` |
| 支付 | 支付/退款 | /system/order/cancelOrder/${id} | `old-code-admin/src/api/iPayment/index.js:19` |
| 支付 | 支付/退款 | /system/order/proofAudit | `old-code-admin/src/api/iPayment/index.js:26` |
| 支付 | 支付/退款、查询/查看 | /system/invoice/list?pageNum=${pam}&pageSize=${psise} | `old-code-admin/src/api/iPayment/index.js:34` |
| 支付 | 支付/退款 | /system/invoice/apply | `old-code-admin/src/api/iPayment/index.js:42` |
| 支付 | 支付/退款 | /system/invoice/reInvoice | `old-code-admin/src/api/iPayment/index.js:50` |
| 支付 | 支付/退款、查询/查看 | /system/invoice/queryInvoiceResult | `old-code-admin/src/api/iPayment/index.js:58` |
| 支付 | 支付/退款、查询/查看 | /system/merchantParamConfig/merSelect | `old-code-admin/src/api/iPayment/index.js:68` |
| 支付 | 支付/退款、查询/查看 | /system/order/commodityNameList | `old-code-admin/src/api/iPayment/index.js:75` |
| 支付 | 支付/退款、查询/查看 | /system/merchantParamConfig/list | `old-code-admin/src/api/iPayment/merchant.js:5` |
| 支付 | 支付/退款 | /system/merchantParamConfig/${id} | `old-code-admin/src/api/iPayment/merchant.js:13` |
| 支付 | 支付/退款 | /system/merchantParamConfig/${id} | `old-code-admin/src/api/iPayment/merchant.js:20` |
| 支付 | 支付/退款 | /system/merchantParamConfig | `old-code-admin/src/api/iPayment/merchant.js:27` |
| 支付 | 支付/退款 | /system/merchantParamConfig | `old-code-admin/src/api/iPayment/merchant.js:36` |
| 支付 | 修改/状态变更、支付/退款 | /system/merchantParamConfig/changeStatus/${id} | `old-code-admin/src/api/iPayment/merchant.js:44` |
| 支付 | 支付/退款、查询/查看 | /system/invoice/getSecondList | `old-code-admin/src/api/iPayment/merchant.js:53` |

## 前端路由/页面样本

| 业务域 | 路径 | 位置 |
| --- | --- | --- |
| 其他 | /redirect | `old-code-admin/src/router/index.js:30` |
| 其他 | /redirect/:path(.*) | `old-code-admin/src/router/index.js:35` |
| 系统用户与权限 | /login | `old-code-admin/src/router/index.js:41` |
| 系统用户与权限 | /register | `old-code-admin/src/router/index.js:46` |
| 其他 | /:pathMatch(.*)* | `old-code-admin/src/router/index.js:51` |
| 其他 | /401 | `old-code-admin/src/router/index.js:56` |
| 其他 | /evaluation | `old-code-admin/src/router/index.js:62` |
| 其他 | /index | `old-code-admin/src/router/index.js:72` |
| 系统用户与权限 | /user | `old-code-admin/src/router/index.js:80` |
| 文件 | profile/:activeTab? | `old-code-admin/src/router/index.js:86` |
| 流程审批 | /workflow/process | `old-code-admin/src/router/index.js:94` |
| 其他 | start/:deployId([\\w\|\\-]+) | `old-code-admin/src/router/index.js:99` |
| 其他 | detail/:procInsId([\\w\|\\-]+) | `old-code-admin/src/router/index.js:105` |
| 流程审批 | /wentiflow/process | `old-code-admin/src/router/index.js:113` |
| 其他 | start/:deployId([\\w\|\\-]+) | `old-code-admin/src/router/index.js:118` |
| 其他 | detail/:procInsId([\\w\|\\-]+) | `old-code-admin/src/router/index.js:124` |
| 系统用户与权限 | /system/user-auth | `old-code-admin/src/router/index.js:136` |
| 系统用户与权限 | role/:userId(\\d+) | `old-code-admin/src/router/index.js:142` |
| 系统用户与权限 | /system/role-auth | `old-code-admin/src/router/index.js:150` |
| 系统用户与权限 | user/:roleId(\\d+) | `old-code-admin/src/router/index.js:156` |
| 系统用户与权限 | /system/dict-data | `old-code-admin/src/router/index.js:164` |
| 系统用户与权限 | index/:dictId(\\d+) | `old-code-admin/src/router/index.js:170` |
| 评审评分 | /tournament/score-data | `old-code-admin/src/router/index.js:178` |
| 其他 | Selectevent | `old-code-admin/src/router/index.js:184` |
| 其他 | /review/object-detail | `old-code-admin/src/router/index.js:192` |
| 其他 | index/:id(\\d+) | `old-code-admin/src/router/index.js:198` |
| 其他 | /review/my-submission-detail | `old-code-admin/src/router/index.js:206` |
| 其他 | index/:id(\\d+) | `old-code-admin/src/router/index.js:212` |
| 评审任务 | /review/secretary/session | `old-code-admin/src/router/index.js:220` |
| 其他 | index/:sessionId(\\d+) | `old-code-admin/src/router/index.js:226` |
| 其他 | :sessionId(\\d+) | `old-code-admin/src/router/index.js:232` |
| 评审评分 | /tournament/score-prize | `old-code-admin/src/router/index.js:240` |
| 其他 | prizes | `old-code-admin/src/router/index.js:246` |
| 其他 | /monitor/job-log | `old-code-admin/src/router/index.js:254` |
| 其他 | index/:jobId(\\d+) | `old-code-admin/src/router/index.js:260` |
| 其他 | /tool/gen-edit | `old-code-admin/src/router/index.js:269` |
| 其他 | index/:tableId(\\d+) | `old-code-admin/src/router/index.js:275` |
| 现场证件 | /certManage/certIssue | `old-code-admin/src/router/index.js:283` |
| 现场证件 | index/:certConfigId(\\d+) | `old-code-admin/src/router/index.js:289` |
| 现场证件 | select/:certConfigId(\\d+) | `old-code-admin/src/router/index.js:296` |
| 现场证件 | /certManage/actionInterconnectConfig | `old-code-admin/src/router/index.js:305` |
| 其他 | index | `old-code-admin/src/router/index.js:311` |
| 内容管理 | pages/index/index | `old-code-mini/pages.json:3` |
| 系统用户与权限 | pages/login | `old-code-mini/pages.json:8` |
| 内容管理 | pages/match/index | `old-code-mini/pages.json:14` |
| 内容管理 | pages/scan/index | `old-code-mini/pages.json:19` |
| 内容管理 | pages/scan/result | `old-code-mini/pages.json:25` |
| 内容管理 | pages/scan/rule | `old-code-mini/pages.json:30` |
| 内容管理 | pages/scan/query | `old-code-mini/pages.json:35` |
| 内容管理 | pages/news/index | `old-code-mini/pages.json:40` |
| 内容管理 | pages/mine/index | `old-code-mini/pages.json:45` |
| 内容管理 | pages/scene-resource/index | `old-code-mini/pages.json:50` |
| 内容管理 | pages/my-credential/index | `old-code-mini/pages.json:55` |
| 内容管理 | pages/review-secretary/index | `old-code-mini/pages.json:60` |
| 内容管理 | pages/agreement/index | `old-code-mini/pages.json:66` |
| 内容管理 | pages/notice/detail | `old-code-mini/pages.json:71` |
| 系统用户与权限 | /login | `old-code-pc/src/router/index.js:7` |
| 系统用户与权限 | /register | `old-code-pc/src/router/index.js:23` |
| 其他 | /password | `old-code-pc/src/router/index.js:37` |
| 流程审批 | /workflow/process | `old-code-pc/src/router/index.js:49` |
| 其他 | start/:deployId([\\w\|\\-]+) | `old-code-pc/src/router/index.js:54` |
| 其他 | detail/:procInsId([\\w\|\\-]+) | `old-code-pc/src/router/index.js:64` |
| 其他 | category | `old-code-pc/src/router/index.js:70` |
| 其他 | /404 | `old-code-pc/src/router/index.js:80` |
| 其他 | /:pathMatch(.*)* | `old-code-pc/src/router/index.js:105` |
| 其他 | / | `old-code-pc/src/router/webGroups.js:4` |
| 其他 | home | `old-code-pc/src/router/webGroups.js:12` |
| 其他 | event | `old-code-pc/src/router/webGroups.js:18` |
| 其他 | browse | `old-code-pc/src/router/webGroups.js:24` |
| 其他 | detail | `old-code-pc/src/router/webGroups.js:30` |
| 报名与团队 | team | `old-code-pc/src/router/webGroups.js:40` |
| 报名与团队 | apply | `old-code-pc/src/router/webGroups.js:46` |
| 报名与团队 | teacherApply | `old-code-pc/src/router/webGroups.js:52` |
| 其他 | shopping | `old-code-pc/src/router/webGroups.js:64` |
| 支付 | order | `old-code-pc/src/router/webGroups.js:70` |
| 其他 | learn | `old-code-pc/src/router/webGroups.js:82` |
| 其他 | information | `old-code-pc/src/router/webGroups.js:88` |
| 其他 | detail | `old-code-pc/src/router/webGroups.js:99` |
| 其他 | personal | `old-code-pc/src/router/webGroups.js:107` |
| 其他 | list | `old-code-pc/src/router/webGroups.js:113` |
| 报名与团队 | TeamDetails | `old-code-pc/src/router/webGroups.js:119` |
| 其他 | accountmanagement | `old-code-pc/src/router/webGroups.js:126` |
| 支付 | paymentrecords | `old-code-pc/src/router/webGroups.js:132` |
| 支付 | payment | `old-code-pc/src/router/webGroups.js:143` |
| 支付 | OrderDetails | `old-code-pc/src/router/webGroups.js:150` |
| 支付 | invoiceIssuance | `old-code-pc/src/router/webGroups.js:157` |
| 支付 | invoice-preparation | `old-code-pc/src/router/webGroups.js:164` |
| 其他 | feedback | `old-code-pc/src/router/webGroups.js:175` |
| 其他 | qa | `old-code-pc/src/router/webGroups.js:181` |
| 其他 | customize | `old-code-pc/src/router/webGroups.js:187` |
| 其他 | site | `old-code-pc/src/router/webGroups.js:193` |
| 其他 | list | `old-code-pc/src/router/webGroups.js:198` |
| 其他 | detail | `old-code-pc/src/router/webGroups.js:203` |
| 现场证件 | certInterconnect | `old-code-pc/src/router/webGroups.js:210` |
| 其他 | description | `old-code-pc/src/router/webGroups.js:222` |
| 其他 | details/:ruleId(\\d+) | `old-code-pc/src/router/webGroups.js:228` |
| 现场证件 | myCert | `old-code-pc/src/router/webGroups.js:234` |
| 其他 | inquiry | `old-code-pc/src/router/webGroups.js:240` |
| 其他 | exam | `old-code-pc/src/router/webGroups.js:248` |
| 其他 | awardPublicity | `old-code-pc/src/router/webGroups.js:254` |
| 其他 | promotion | `old-code-pc/src/router/webGroups.js:260` |
