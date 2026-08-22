# 后端代码质量审计

## 统计

| Java 类别 | 数量 |
| --- | --- |
| OtherJava | 392 |
| Domain/Entity | 326 |
| Service | 212 |
| ServiceImpl | 201 |
| Mapper | 171 |
| Controller | 162 |
| DTO/VO | 75 |
| Test | 6 |

## 高复杂度后端文件

| 文件 | 行数 | 风险分 |
| --- | --- | --- |
| old-code/teaching-modules/teaching-file/src/main/java/com/teaching/file/service/OSSFileServiceImpl.java | 2644 | 1115.2 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewObjectServiceImpl.java | 2331 | 841.0 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneVerifyServiceImpl.java | 1742 | 700.6 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionServiceImpl.java | 2267 | 659.4 |
| old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/OrderInfoServiceImpl.java | 1971 | 610.5 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneScheduleServiceImpl.java | 1463 | 583.1 |
| old-code/teaching-modules/teaching-system/src/main/java/com/teaching/system/service/impl/PayServiceImpl.java | 835 | 556.8 |
| old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/poi/ExcelUtil.java | 1941 | 443.6 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSecretaryServiceImpl.java | 537 | 439.9 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionSceneOneCardVerifyServiceImpl.java | 1256 | 439.3 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewMyReviewServiceImpl.java | 1105 | 422.8 |
| old-code/teaching-modules/teaching-flowable/src/main/java/com/teaching/flowable/service/impl/WfProcessServiceImpl.java | 1937 | 396.9 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionApplyInfoServiceImpl.java | 1501 | 391.6 |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/UserCompetitionSceneResourceServiceImpl.java | 1126 | 383.3 |

## 异常与日志

| 类型 | 位置 | 代码 |
| --- | --- | --- |
| catch(Exception) | old-code/teaching-auth/src/main/java/com/teaching/auth/controller/TokenController.java:60 | catch (Exception ignored) |
| catch(Exception) | old-code/teaching-auth/src/main/java/com/teaching/auth/service/SysLoginService.java:252 | catch (Exception e) |
| catch(Exception) | old-code/teaching-auth/src/main/java/com/teaching/auth/service/SysLoginService.java:300 | catch (Exception e) |
| catch(Exception) | old-code/teaching-auth/src/main/java/com/teaching/auth/service/SysLoginService.java:358 | catch (Exception e) |
| catch(Exception) | old-code/teaching-auth/src/main/java/com/teaching/auth/service/SysPasswordService.java:152 | catch (Exception e) |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/sms/SmsUtil.java:49 | } catch (Exception e) { |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/sms/SmsUtil.java:78 | } catch (Exception e) { |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/text/Convert.java:123 | catch (Exception e) |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/text/Convert.java:174 | catch (Exception e) |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/text/Convert.java:221 | catch (Exception e) |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/text/Convert.java:272 | catch (Exception e) |
| catch(Exception) | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/text/Convert.java:419 | catch (Exception e) |
| System.out | old-code/teaching-auth/src/main/java/com/teaching/auth/TeachingAuthApplication.java:24 | System.out.println("(♥◠‿◠)ﾉﾞ  天津大学教学认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ  \n" + |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/html/EscapeUtil.java:163 | System.out.println("clean: " + EscapeUtil.clean(html)); |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/html/EscapeUtil.java:164 | System.out.println("escape: " + escape); |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/html/EscapeUtil.java:165 | System.out.println("unescape: " + EscapeUtil.unescape(escape)); |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/sign/RsaUtils.java:96 | System.out.println(s); |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/sign/RsaUtils.java:98 | System.out.println(s2); |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/sign/RsaUtils.java:101 | System.out.println(s3); |
| System.out | old-code/teaching-common/teaching-common-core/src/main/java/com/teaching/common/core/utils/sign/RsaUtils.java:103 | System.out.println(s4); |

## 主要结论
- ServiceImpl 承载业务编排过重，`teaching-competition` 是首要治理对象。
- 异常处理需要从粗粒度 catch 迁移到业务异常和系统异常分层。
- DTO/VO/Entity 边界建议从高风险接口先收敛，避免数据库字段直接穿透前端。
- common-core 中业务常量应逐步拆出，降低公共模块耦合。
