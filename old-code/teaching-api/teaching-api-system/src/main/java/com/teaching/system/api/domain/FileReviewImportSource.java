package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件上传管理导入评审模块的数据快照。
 */
public class FileReviewImportSource implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fileTaskId;

    private String fileTaskName;

    private Long userId;

    private String userName;

    private String userGroupIds;

    private String competitionSeriesId;

    private String competitionName;

    private String competitionStageId;

    private String competitionStageName;

    private String competitionTrackName;

    private String competitionTrackCode;

    private String secondLevelCode;

    private String secondLevelName;

    private String leaderTeacherId;

    private String leaderTeacherName;

    private String teamCode;

    private String teamName;

    private String guideTeacher;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    private String totalSize;

    private String fileInfo;

    private Long orgId;

    private Boolean submitStatus;

    private List<FileReviewImportMaterial> materials = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileTaskId() {
        return fileTaskId;
    }

    public void setFileTaskId(Long fileTaskId) {
        this.fileTaskId = fileTaskId;
    }

    public String getFileTaskName() {
        return fileTaskName;
    }

    public void setFileTaskName(String fileTaskName) {
        this.fileTaskName = fileTaskName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserGroupIds() {
        return userGroupIds;
    }

    public void setUserGroupIds(String userGroupIds) {
        this.userGroupIds = userGroupIds;
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

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(String fileInfo) {
        this.fileInfo = fileInfo;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Boolean getSubmitStatus() {
        return submitStatus;
    }

    public void setSubmitStatus(Boolean submitStatus) {
        this.submitStatus = submitStatus;
    }

    public List<FileReviewImportMaterial> getMaterials() {
        return materials;
    }

    public void setMaterials(List<FileReviewImportMaterial> materials) {
        this.materials = materials;
    }
}
