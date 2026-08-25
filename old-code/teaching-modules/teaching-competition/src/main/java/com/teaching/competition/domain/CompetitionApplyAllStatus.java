package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;

public class CompetitionApplyAllStatus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赛事系列id(个人参赛) */
    private Long competitionSeriesId;

    /** 用户id */
    private Long userId;

    /** 团队负责人id */
    private Long teamLeaderId;

    /** 团队负责人姓名 */
    private String teamLeaderName;

    //报名状态
    private String applyStatus;

    /** 报名理由 */
    private String applyReason;

    /** 团队编号 */
    private String teamCode;

    /** 团队名称 */
    private String teamName;

    /** 订单号 */
    private String orderId;

    // 报名费用
    private String amount;

    /** 是否作品标识 */
    private String worksFlag;

    /** 作品状态 */
    private String worksStatus;

    /** 组别 */
    protected String groupClassify;


    /** 赛事赛道name */
    private String competitionTrackName;

    /** 作品提交标识  */
    private Boolean worksSubmitFlag;

    /** 阶段标识 */
    private Boolean stageFlag;

    /** 报名时间标识 */
    private Boolean applyTimeFlag;

    // 个人 队长  团员  标识
    private String flag;

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamLeaderId() {
        return teamLeaderId;
    }

    public void setTeamLeaderId(Long teamLeaderId) {
        this.teamLeaderId = teamLeaderId;
    }

    public String getTeamLeaderName() {
        return teamLeaderName;
    }

    public void setTeamLeaderName(String teamLeaderName) {
        this.teamLeaderName = teamLeaderName;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getWorksFlag() {
        return worksFlag;
    }

    public void setWorksFlag(String worksFlag) {
        this.worksFlag = worksFlag;
    }

    public String getWorksStatus() {
        return worksStatus;
    }

    public void setWorksStatus(String worksStatus) {
        this.worksStatus = worksStatus;
    }

    public String getGroupClassify() {
        return groupClassify;
    }

    public void setGroupClassify(String groupClassify) {
        this.groupClassify = groupClassify;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public Boolean getWorksSubmitFlag() {
        return worksSubmitFlag;
    }

    public void setWorksSubmitFlag(Boolean worksSubmitFlag) {
        this.worksSubmitFlag = worksSubmitFlag;
    }

    public Boolean getStageFlag() {
        return stageFlag;
    }

    public void setStageFlag(Boolean stageFlag) {
        this.stageFlag = stageFlag;
    }

    public Boolean getApplyTimeFlag() {
        return applyTimeFlag;
    }

    public void setApplyTimeFlag(Boolean applyTimeFlag) {
        this.applyTimeFlag = applyTimeFlag;
    }
}
