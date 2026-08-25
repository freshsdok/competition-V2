package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

public class UserCompetitionApplyInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赛事id */
    private Long competitionId;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 用户id */
    private Long userId;

    /** 赛事名称 */
    private String competitionName;

    /** 赛事类型 */
    private String competitionType;

    /** 赛事描述 */
    private String competitionDesc;

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationTime;

    /** 参赛方式 */
    private String joinType;

    /** 是否作品标识 */
    private String worksFlag;

    /** 作品状态 */
    private String worksStatus;

    /** 赛事图片名称 */
    private String competitionImageName;

    /** 赛事图片地址 */
    private String competitionImage;

    /** 赛事状态 */
    private String checkStatus;

    /** 赛事收藏数 */
    private Integer competitionCollectNum;

    /** 赛事分享数 */
    private Integer competitionShareNum;

    /** 作品格式 */
    private String worksFormat;

    /** 作品格式大小 */
    private String worksFormatSize;

    /** 团队代码 */
    private String teamCode;

    /** 团队名称 */
    private String teamName;

    /** 是否上传标识 */
    private Boolean uploadFlag;

    /** 赛事阶段id */
    private String stageId;

    /** 阶段名称 */
    private String stageName;

    // 报名费用
    private String amount;

    // 个人 队长  团员  标识
    private String flag;

    //报名状态
    private String applyStatus;

    /** 审核原因 */
    private String applyReason;

    /** 作品提交截至日 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date worksSubmitDate;

    /** 是否提交标识 */
    private Boolean worksSubmitFlag;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 组别 */
    private String groupClassify;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getCompetitionDesc() {
        return competitionDesc;
    }

    public void setCompetitionDesc(String competitionDesc) {
        this.competitionDesc = competitionDesc;
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
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

    public String getCompetitionImageName() {
        return competitionImageName;
    }

    public void setCompetitionImageName(String competitionImageName) {
        this.competitionImageName = competitionImageName;
    }

    public String getCompetitionImage() {
        return competitionImage;
    }

    public void setCompetitionImage(String competitionImage) {
        this.competitionImage = competitionImage;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Integer getCompetitionShareNum() {
        return competitionShareNum;
    }

    public void setCompetitionShareNum(Integer competitionShareNum) {
        this.competitionShareNum = competitionShareNum;
    }

    public Integer getCompetitionCollectNum() {
        return competitionCollectNum;
    }

    public void setCompetitionCollectNum(Integer competitionCollectNum) {
        this.competitionCollectNum = competitionCollectNum;
    }

    public String getWorksFormat() {
        return worksFormat;
    }

    public void setWorksFormat(String worksFormat) {
        this.worksFormat = worksFormat;
    }

    public String getWorksFormatSize() {
        return worksFormatSize;
    }

    public void setWorksFormatSize(String worksFormatSize) {
        this.worksFormatSize = worksFormatSize;
    }

    public Date getWorksSubmitDate() {
        return worksSubmitDate;
    }

    public void setWorksSubmitDate(Date worksSubmitDate) {
        this.worksSubmitDate = worksSubmitDate;
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

    public Boolean getUploadFlag() {
        return uploadFlag;
    }

    public void setUploadFlag(Boolean uploadFlag) {
        this.uploadFlag = uploadFlag;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
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

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getGroupClassify() {
        return groupClassify;
    }

    public void setGroupClassify(String groupClassify) {
        this.groupClassify = groupClassify;
    }

    public Boolean getWorksSubmitFlag() {
        return worksSubmitFlag;
    }

    public void setWorksSubmitFlag(Boolean worksSubmitFlag) {
        this.worksSubmitFlag = worksSubmitFlag;
    }
}
