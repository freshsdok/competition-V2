package com.teaching.system.api.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;

import java.io.Serializable;
import java.util.Date;


public class CompetitionMainInfoReq extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赛事id */
    private Long competitionId;

    /** 赛事ids,id以逗号隔开 */
    private String competitionIds;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛事系列id,id以逗号隔开 */
    private String competitionSeriesIds;

    /** 赛事类型 */
    private String competitionType;

    /** 赛事名称 */
    private String competitionName;

    /** 赛事界名称 */
    private String competitionSeriesName;

    /** 赛事主办方 */
    private String organizer;

    /** 赛事赞助企业 */
    private String enterpriseName;

    /** 赛事状态 */
    private String checkStatus;

    /** 赞助金额起止 */
    private String bonusNumStart;

    /** 赞助金额终止 */
    private String bonusNumEnd;

    /** 报名开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyStartTime;

    /** 报名结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyEndTime;

    /** 赛事开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionStartTime;

    /** 赛事结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionEndTime;

    /** 赛事费用 */
    private Integer feeStart;

    /** 赛事费用 */
    private Integer feeEnd;

    /** 赛事编号集合,逗号隔开 */
    private String competitionCodes;

    /** 赛事所需条数 */
    private  Integer competitionNum;

    /** 参赛方式 */
    private String joinType;

    public CompetitionMainInfoReq() {
    }

    public CompetitionMainInfoReq(Long competitionId) {
        this.competitionId = competitionId;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getBonusNumStart() {
        return bonusNumStart;
    }

    public void setBonusNumStart(String bonusNumStart) {
        this.bonusNumStart = bonusNumStart;
    }

    public String getBonusNumEnd() {
        return bonusNumEnd;
    }

    public void setBonusNumEnd(String bonusNumEnd) {
        this.bonusNumEnd = bonusNumEnd;
    }

    public Integer getFeeStart() {
        return feeStart;
    }

    public void setFeeStart(Integer feeStart) {
        this.feeStart = feeStart;
    }

    public Integer getFeeEnd() {
        return feeEnd;
    }

    public void setFeeEnd(Integer feeEnd) {
        this.feeEnd = feeEnd;
    }

    public Date getApplyStartTime() {
        return applyStartTime;
    }

    public void setApplyStartTime(Date applyStartTime) {
        this.applyStartTime = applyStartTime;
    }

    public Date getApplyEndTime() {
        return applyEndTime;
    }

    public void setApplyEndTime(Date applyEndTime) {
        this.applyEndTime = applyEndTime;
    }

    public Date getCompetitionStartTime() {
        return competitionStartTime;
    }

    public void setCompetitionStartTime(Date competitionStartTime) {
        this.competitionStartTime = competitionStartTime;
    }

    public Date getCompetitionEndTime() {
        return competitionEndTime;
    }

    public void setCompetitionEndTime(Date competitionEndTime) {
        this.competitionEndTime = competitionEndTime;
    }

    public String getCompetitionIds() {
        return competitionIds;
    }

    public void setCompetitionIds(String competitionIds) {
        this.competitionIds = competitionIds;
    }

    public String getCompetitionSeriesIds() {
        return competitionSeriesIds;
    }

    public void setCompetitionSeriesIds(String competitionSeriesIds) {
        this.competitionSeriesIds = competitionSeriesIds;
    }

    public String getCompetitionCodes() {
        return competitionCodes;
    }

    public void setCompetitionCodes(String competitionCodes) {
        this.competitionCodes = competitionCodes;
    }

    public Integer getCompetitionNum() {
        return competitionNum;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public void setCompetitionNum(Integer competitionNum) {
        this.competitionNum = competitionNum;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }
}
