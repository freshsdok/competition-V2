package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;

import java.io.Serializable;
import java.util.List;

public class FileUploadReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务主id */
    private Long id;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String taskName;

    private Long userId;

    private Boolean submitStatus;

    /** 文件上传任务列表 */
    private List<FileUploadRecord> fileUploadManagerList;

    // 用户组所属赛事信息
    private List<List<SysUserGroupCompetitionRelation>> sysUserGroupCompetitionRelationList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<FileUploadRecord> getFileUploadManagerList() {
        return fileUploadManagerList;
    }

    public void setFileUploadManagerList(List<FileUploadRecord> fileUploadManagerList) {
        this.fileUploadManagerList = fileUploadManagerList;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<List<SysUserGroupCompetitionRelation>> getSysUserGroupCompetitionRelationList() {
        return sysUserGroupCompetitionRelationList;
    }

    public void setSysUserGroupCompetitionRelationList(List<List<SysUserGroupCompetitionRelation>> sysUserGroupCompetitionRelationList) {
        this.sysUserGroupCompetitionRelationList = sysUserGroupCompetitionRelationList;
    }

    public Boolean getSubmitStatus() {
        return submitStatus;
    }

    public void setSubmitStatus(Boolean submitStatus) {
        this.submitStatus = submitStatus;
    }
}
