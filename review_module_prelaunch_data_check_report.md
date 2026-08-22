# 评审模块上线前数据检查报告

- 生成时间：2026-07-06 23:41:07
- 数据库：localhost:3306/jiaoxue_test
- 汇总：PASS 15，WARN 0，FAIL 0

## [PASS] TABLES - 评审模块核心表完整性

未发现异常。

## [PASS] RECORD_COLUMNS - review_record 评审模块字段完整性

未发现异常。

## [PASS] MENU_PERMISSIONS - 评审模块菜单和按钮权限完整性

未发现异常。

## [PASS] CERT_DUP - 同一活动下同一有效参赛证映射多个评审对象

未发现异常。

## [PASS] PERMISSION_USER - 有效填报权限缺少 user_id

未发现异常。

## [PASS] ASSIGN_OBJECT_STATUS - 未锁定或已作废对象仍存在有效评审任务

未发现异常。

## [PASS] SUBMITTED_ASSIGNMENT_RECORD - 已提交任务缺少已提交评分记录

未发现异常。

## [PASS] SUBMITTED_RECORD_ASSIGNMENT - 已提交评分记录对应任务未提交

未发现异常。

## [PASS] SCORE_SNAPSHOT - 已提交评分明细缺少指标快照字段

未发现异常。

## [PASS] PUBLISHED_LOG - 已发布结果缺少发布日志

未发现异常。

## [PASS] RESULT_SCORE - 已生成或已发布结果缺少系统计算分

未发现异常。

## [PASS] SESSION_CURRENT_IN_LIST - 场次当前对象不在场次对象列表中

未发现异常。

## [PASS] SESSION_CURRENT_OBJECT_STATUS - 场次当前对象不是 LOCKED 状态

未发现异常。

## [PASS] REVIEWER_MENU_ROLE - 已分配专家账号缺少专家端菜单权限

未发现异常。

## [PASS] SECRETARY_MENU_ROLE - 场次秘书账号缺少秘书控制台权限

未发现异常。
