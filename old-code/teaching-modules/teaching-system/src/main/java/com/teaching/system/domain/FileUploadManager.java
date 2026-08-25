package com.teaching.system.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 文件上传管理对象 file_upload_manager
 *
 * @author teaching
 * @date 2026-01-15
 */
public class FileUploadManager extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    private List<String> ids;

    /** 文件任务id(file_task表中id) */
//    @Excel(name = "文件任务id(file_task表中id)")
    private Long fileTaskId;

    /** 文件任务名称 */
    @Excel(name = "任务名称")
    private String fileTaskName;

    /** 用户id */
//    @Excel(name = "用户id")
    private Long userId;

    /** 用户组id */
//    @Excel(name = "用户组id")
    private String userGroupIds;

    /** 赛事 */
//    @Excel(name = "赛事")
    private String competitionSeriesId;

    /** 赛事名称 */
    @Excel(name = "赛事名称")
    private String competitionName;

    private String competitionStageId;

    private String competitionStageName;

    /** 赛道名称 */
    @Excel(name = "赛道名称")
    private String competitionTrackName;

    /** 赛道编码 */
//    @Excel(name = "赛道编码")
    private String competitionTrackCode;

    /** 组别code */
//    @Excel(name = "组别code")
    private String secondLevelCode;

    /** 组别名称 */
    @Excel(name = "组别/赛项")
    private String secondLevelName;


    @Excel(name = "用户姓名")
    private String userName;

    /** 带队老师id */
//    @Excel(name = "带队老师id")
    private String leaderTeacherId;


    /** 团队code */
    @Excel(name = "队伍ID")
    private String teamCode;

    /** 团队名称 */
    @Excel(name = "队伍名称")
    private String teamName;

    @Excel(name = "带队老师")
    private String leaderTeacherName;

    /** 指导老师 */
    @Excel(name = "指导老师")
    private String guideTeacher;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上传时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    @Excel(name = "上传文件")
    private String fileName;

    /** 任务上传文件总大小 */
    @Excel(name = "总大小(M)")
    private String totalSize;

    /** 文件json */
//    @Excel(name = "文件json")
    private String fileInfo;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTimeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTimeEnd;

    private Long orgId;

    private Boolean submitStatus;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setFileTaskId(Long fileTaskId)
    {
        this.fileTaskId = fileTaskId;
    }

    public Long getFileTaskId()
    {
        return fileTaskId;
    }

    public void setFileTaskName(String fileTaskName)
    {
        this.fileTaskName = fileTaskName;
    }

    public String getFileTaskName()
    {
        return fileTaskName;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserGroupIds(String userGroupIds)
    {
        this.userGroupIds = userGroupIds;
    }

    public String getUserGroupIds()
    {
        return userGroupIds;
    }

    public void setCompetitionSeriesId(String competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionName(String competitionName)
    {
        this.competitionName = competitionName;
    }

    public String getCompetitionName()
    {
        return competitionName;
    }

    public void setCompetitionTrackName(String competitionTrackName)
    {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionTrackName()
    {
        return competitionTrackName;
    }

    public void setCompetitionTrackCode(String competitionTrackCode)
    {
        this.competitionTrackCode = competitionTrackCode;
    }

    public String getCompetitionTrackCode()
    {
        return competitionTrackCode;
    }

    public void setSecondLevelCode(String secondLevelCode)
    {
        this.secondLevelCode = secondLevelCode;
    }

    public String getSecondLevelCode()
    {
        return secondLevelCode;
    }

    public void setSecondLevelName(String secondLevelName)
    {
        this.secondLevelName = secondLevelName;
    }

    public String getSecondLevelName()
    {
        return secondLevelName;
    }

    public String getLeaderTeacherId() {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(String leaderTeacherId) {
        this.leaderTeacherId = leaderTeacherId;
    }

    public void setTeamCode(String teamCode)
    {
        this.teamCode = teamCode;
    }

    public String getTeamCode()
    {
        return teamCode;
    }

    public void setTeamName(String teamName)
    {
        this.teamName = teamName;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public void setGuideTeacher(String guideTeacher)
    {
        this.guideTeacher = guideTeacher;
    }

    public String getGuideTeacher()
    {
        return guideTeacher;
    }

    public void setUploadTime(Date uploadTime)
    {
        this.uploadTime = uploadTime;
    }

    public Date getUploadTime()
    {
        return uploadTime;
    }

    public void setTotalSize(String totalSize)
    {
        this.totalSize = totalSize;
    }

    public String getTotalSize()
    {
        return totalSize;
    }

    public void setFileInfo(String fileInfo)
    {
        this.fileInfo = fileInfo;
    }

    public String getFileInfo()
    {
        return fileInfo;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
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

    public Long getOrgId() {
        return orgId;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadTimeStart() {
        return uploadTimeStart;
    }

    public void setUploadTimeStart(Date uploadTimeStart) {
        this.uploadTimeStart = uploadTimeStart;
    }

    public Date getUploadTimeEnd() {
        return uploadTimeEnd;
    }

    public void setUploadTimeEnd(Date uploadTimeEnd) {
        this.uploadTimeEnd = uploadTimeEnd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getSubmitStatus() {
        return submitStatus;
    }

    public void setSubmitStatus(Boolean submitStatus) {
        this.submitStatus = submitStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("fileTaskId", getFileTaskId())
            .append("fileTaskName", getFileTaskName())
            .append("userId", getUserId())
            .append("userGroupIds", getUserGroupIds())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionName", getCompetitionName())
                .append("competitionStageId", getCompetitionStageId())
                .append("competitionStageName", getCompetitionStageName())
            .append("competitionTrackName", getCompetitionTrackName())
            .append("competitionTrackCode", getCompetitionTrackCode())
            .append("secondLevelCode", getSecondLevelCode())
            .append("secondLevelName", getSecondLevelName())
            .append("leaderTeacherId", getLeaderTeacherId())
            .append("teamCode", getTeamCode())
            .append("teamName", getTeamName())
            .append("guideTeacher", getGuideTeacher())
            .append("uploadTime", getUploadTime())
            .append("totalSize", getTotalSize())
            .append("fileInfo", getFileInfo())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
                .append("orgId", getOrgId())
            .toString();
    }
}
