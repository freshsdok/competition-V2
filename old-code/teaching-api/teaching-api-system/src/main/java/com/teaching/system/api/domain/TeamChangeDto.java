package com.teaching.system.api.domain;

import java.math.BigDecimal;
import java.util.List;

public class TeamChangeDto {

    /**
     * 团队编码
     */
    private String teamCode;

    /**
     * 变更用户ID，多个id逗号分割
     */
    private String userIds;

    private Integer userNum;

    /**
     * 变更类型（ADD-添加成员，REMOVE-移除成员，NONE-人数不变）
     */
//    private String changeType;

    /**
     * 作用范围类别
     */
    private String commodityType;

    /**
     * 业务事项id
     */
    private Long eventId;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 流程类型
     * @return
     */
    private String changeType;

    /**
     * 赛道编码
     */
    private String secondLevelCode;


    /**
     * 流程实例id
     */
    private String processInstanceId;
    /**
     * 流程taskId
     */
    private String taskId;

    /**
     * 原订单id
     */
    private String payOrderId;

    /**
     * 赛事id
     */
    private String competitionSeriesId;

    private String teamNewInfo;

    public String getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(String competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getCommodityType() {
        return commodityType;
    }

    public void setCommodityType(String commodityType) {
        this.commodityType = commodityType;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
    }

    public Integer getUserNum() {
        return userNum;
    }

    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getTeamNewInfo() {
        return teamNewInfo;
    }

    public void setTeamNewInfo(String teamNewInfo) {
        this.teamNewInfo = teamNewInfo;
    }
}
