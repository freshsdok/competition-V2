# 事务与并发审计

## @Transactional 扫描

| 位置 | 代码 |
| --- | --- |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewAssignmentServiceImpl.java:112 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewMyReviewServiceImpl.java:322 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewMyReviewServiceImpl.java:353 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewMyReviewServiceImpl.java:359 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewObjectServiceImpl.java:242 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewObjectServiceImpl.java:334 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewRecordServiceImpl.java:45 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewRecordServiceImpl.java:51 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewResultServiceImpl.java:63 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewResultServiceImpl.java:149 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewResultServiceImpl.java:169 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewResultServiceImpl.java:200 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewRuleServiceImpl.java:183 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSecretaryServiceImpl.java:129 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSecretaryServiceImpl.java:139 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSecretaryServiceImpl.java:167 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSessionServiceImpl.java:104 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:155 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:186 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:228 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:251 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:273 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:294 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:314 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/review/service/impl/ReviewSubmissionServiceImpl.java:332 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/AwardDetailsServiceImpl.java:191 | @Transactional |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/AwardPublicityServiceImpl.java:59 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/AwardPublicityServiceImpl.java:195 | @Transactional(rollbackFor = Exception.class) |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionApplyInfoServiceImpl.java:659 | @Transactional |
| old-code/teaching-modules/teaching-competition/src/main/java/com/teaching/competition/service/impl/CompetitionCertExchangeRuleServiceImpl.java:169 | @Transactional |

## 重点风险

| 场景 | 风险 | 建议 |
| --- | --- | --- |
| 资源预约创建 | 查重、容量扣减、预约插入不是原子操作时会超卖。 | 同事务内使用条件更新；容量不足通过 affected rows 判断。 |
| 资源预约取消 | 取消预约与释放容量若任一步失败会造成占用不准。 | 取消状态更新与容量释放同事务；重复取消返回幂等结果。 |
| idempotency_key | 数据库唯一键能兜底，但重复请求可能抛异常。 | 捕获唯一冲突后查询既有记录并返回业务成功/已处理。 |
| 扫码核验 DONE | 同一主体/赛程/动作可能重复完成。 | 建立 operation active/DONE 唯一约束或 active_key。 |
| grant 生效/撤销 | 并发授权/撤销可能出现多条 active grant。 | 用唯一键和状态条件更新控制 active grant。 |
| 支付回调 | 支付平台重试回调天然重复。 | 所有回调以订单号/流水号幂等，状态单向流转。 |

## 结论
当前并发风险主要集中在资源预约、扫码状态、grant、生效/撤销、支付回调。建议优先用数据库约束 + 条件更新 + 应用层幂等响应三件套治理，不建议只靠 synchronized 或前端防重。
