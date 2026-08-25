package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 评审任务分配信息对象 review_task_info
 *
 * @author teaching
 * @date 2026-04-09
 */
public class ReviewTaskInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 评审任务id
     */
    private Long reviewId;

    /**
     * 文件上传管理id
     */
    private Long fileUploadManagerId;

    /** 赛事 */
    private String competitionSeriesId;

    /** 赛事名称 */
    private String competitionName;

    private String competitionStageId;

    private String competitionStageName;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 赛道编码 */
    private String competitionTrackCode;

    /** 赛道查询 */
    private String competitionTrackQuery;

    /** 组别code */
    private String secondLevelCode;

    /** 组别名称 */
    private String secondLevelName;

    /**
     * 队长名称
     */
    private String teamLeader;

    /** 上传用户姓名 */
    private String userName;

    /**
     * 文件信息
     */
    private String fileInfo;

    /**
     * 带队老师id
     */
    private String leaderTeacherId;

    /**
     * 带队老师姓名
     */
    private String leaderTeacherName;

    /** 学校名称 */
    private String schoolName;

    /** 学校id */
    private String schoolId;

    /** 省份名称 */
    private String province;

    /** 省份编码 */
    private String provinceCode;

    /**
     * 团队编码
     */
    private String teamCode;

    /** 团队名称 */
    private String teamName;
    /**
     * 评审任务名称
     */
    @Excel(name = "评审任务名称")
    private String reviewName;

    /**
     * 审阅开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审阅开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewStartTime;

    /**
     * 审阅结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审阅结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewEndTime;

    /**
     * 评审备注
     */
    @Excel(name = "评审备注")
    private String reviewDesc;

    /**
     * 所在组ID集合
     */
    @Excel(name = "所在组ID集合")
    private String reviewGroupId;

    /**
     * 所在组ID
     */
    private String taskGroupId;

    /**
     * 所在组名称
     */
    private String allotGroupName;

    /**
     * 参考文档
     */
    @Excel(name = "参考文档")
    private String referenceDocument;

    /**
     * 分配状态
     */
    @Excel(name = "分配状态")
    private String distributeStatus;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 学校队长带队老师关键词
     */
    private String keyWords;

    /**
     * 评审任务分配组关联关系列表
     */
    private List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList;

    /**
     * 任务分配专家关联关系列表
     */
    private List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList;

    /**
     * 当前时间
     */
    private Date nowDate;
    /**
     * 是否继续
     */
    private Boolean continueFlag;

    public ReviewTaskInfo() {
    }

    public ReviewTaskInfo(Boolean continueFlag) {
        this.continueFlag = continueFlag;
    }

    public Boolean getContinueFlag() {
        return continueFlag;
    }

    public void setContinueFlag(Boolean continueFlag) {
        this.continueFlag = continueFlag;
    }

    public Date getNowDate() {
        return nowDate;
    }

    public void setNowDate(Date nowDate) {
        this.nowDate = nowDate;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getFileUploadManagerId() {
        return fileUploadManagerId;
    }

    public void setFileUploadManagerId(Long fileUploadManagerId) {
        this.fileUploadManagerId = fileUploadManagerId;
    }

    public String getReviewName() {
        return reviewName;
    }

    public void setReviewName(String reviewName) {
        this.reviewName = reviewName;
    }

    public Date getReviewStartTime() {
        return reviewStartTime;
    }

    public void setReviewStartTime(Date reviewStartTime) {
        this.reviewStartTime = reviewStartTime;
    }

    public Date getReviewEndTime() {
        return reviewEndTime;
    }

    public void setReviewEndTime(Date reviewEndTime) {
        this.reviewEndTime = reviewEndTime;
    }

    public String getReviewDesc() {
        return reviewDesc;
    }

    public void setReviewDesc(String reviewDesc) {
        this.reviewDesc = reviewDesc;
    }

    public String getReviewGroupId() {
        return reviewGroupId;
    }

    public void setReviewGroupId(String reviewGroupId) {
        this.reviewGroupId = reviewGroupId;
    }

    public String getReferenceDocument() {
        return referenceDocument;
    }

    public void setReferenceDocument(String referenceDocument) {
        this.referenceDocument = referenceDocument;
    }

    public String getDistributeStatus() {
        return distributeStatus;
    }

    public void setDistributeStatus(String distributeStatus) {
        this.distributeStatus = distributeStatus;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public List<ReviewTaskAllotGroupRelation> getReviewTaskAllotGroupRelationList() {
        return reviewTaskAllotGroupRelationList;
    }

    public void setReviewTaskAllotGroupRelationList(List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList) {
        this.reviewTaskAllotGroupRelationList = reviewTaskAllotGroupRelationList;
    }

    public List<ReviewTaskSpecialistRelation> getReviewTaskSpecialistRelationList() {
        return reviewTaskSpecialistRelationList;
    }

    public void setReviewTaskSpecialistRelationList(List<ReviewTaskSpecialistRelation> reviewTaskSpecialistRelationList) {
        this.reviewTaskSpecialistRelationList = reviewTaskSpecialistRelationList;
    }

    public String getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(String competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionStageId() {
        return competitionStageId;
    }

    public void setCompetitionStageId(String competitionStageId) {
        this.competitionStageId = competitionStageId;
    }

    public String getCompetitionStageName() {
        return competitionStageName;
    }

    public void setCompetitionStageName(String competitionStageName) {
        this.competitionStageName = competitionStageName;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionTrackCode() {
        return competitionTrackCode;
    }

    public void setCompetitionTrackCode(String competitionTrackCode) {
        this.competitionTrackCode = competitionTrackCode;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
    }

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(String teamLeader) {
        this.teamLeader = teamLeader;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLeaderTeacherId() {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(String leaderTeacherId) {
        this.leaderTeacherId = leaderTeacherId;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
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

    public String getAllotGroupName() {
        return allotGroupName;
    }

    public void setAllotGroupName(String allotGroupName) {
        this.allotGroupName = allotGroupName;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getCompetitionTrackQuery() {
        return competitionTrackQuery;
    }

    public void setCompetitionTrackQuery(String competitionTrackQuery) {
        this.competitionTrackQuery = competitionTrackQuery;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(String fileInfo) {
        this.fileInfo = fileInfo;
    }

    public String getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(String taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("reviewId", getReviewId())
                .append("fileUploadManagerId", getFileUploadManagerId())
                .append("reviewName", getReviewName())
                .append("reviewStartTime", getReviewStartTime())
                .append("reviewEndTime", getReviewEndTime())
                .append("reviewDesc", getReviewDesc())
                .append("reviewGroupId", getReviewGroupId())
                .append("referenceDocument", getReferenceDocument())
                .append("distributeStatus", getDistributeStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
