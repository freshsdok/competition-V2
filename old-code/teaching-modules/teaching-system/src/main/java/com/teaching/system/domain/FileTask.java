package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件分发任务对象 file_task
 * 
 * @author teaching
 * @date 2026-01-07
 */
public class FileTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String taskName;

    private String taskType;

    /** 用户组ids */
    @Excel(name = "用户组ids")
    private String userGroupIds;

    /** 任务状态 */
    private String taskStatus;

    /** 用户组名称搜索 */
    private String userGroupName;

    /** 用户组名称 */
    private String userGroupNames;

    private List<FileTaskConfig> fileTaskConfigList;

    // 用户组所属赛事信息
    private List<List<SysUserGroupCompetitionRelation>> sysUserGroupCompetitionRelationList;

    /** 文件分发任务阅读统计 */
    private Map<String,Object> fileTaskStatisticsMap;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeEnd;

    // 已上传量
    private Integer uploadedCount;
    // 已读量
    private Integer readCount;
    // 用户组人数
    private Integer peopleCount;
    // 已下载量
    private Integer downCount;
    // 用户是否已读该任务
    private Boolean readCountFlag;

    // 当前用户收到的有效任务通知总数
    private Integer notificationCount;

    private Boolean submitStatus;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTaskName(String taskName) 
    {
        this.taskName = (taskName != null) ? taskName.trim() : null;
    }

    public String getTaskName() 
    {
        return taskName;
    }

    public void setUserGroupIds(String userGroupIds) 
    {
        this.userGroupIds = userGroupIds;
    }

    public String getUserGroupIds() 
    {
        return userGroupIds;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public List<FileTaskConfig> getFileTaskConfigList() {
        return fileTaskConfigList;
    }

    public void setFileTaskConfigList(List<FileTaskConfig> fileTaskConfigList) {
        this.fileTaskConfigList = fileTaskConfigList;
    }

    public String getUserGroupNames() {
        return userGroupNames;
    }

    public void setUserGroupNames(String userGroupNames) {
        this.userGroupNames = userGroupNames;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getUserGroupName() {
        return userGroupName;
    }

    public void setUserGroupName(String userGroupName) {
        this.userGroupName = (userGroupName != null) ? userGroupName.trim() : null;
    }

    public Map<String, Object> getFileTaskStatisticsMap() {
        return fileTaskStatisticsMap;
    }

    public void setFileTaskStatisticsMap(Map<String, Object> fileTaskStatisticsMap) {
        this.fileTaskStatisticsMap = fileTaskStatisticsMap;
    }

    public List<List<SysUserGroupCompetitionRelation>> getSysUserGroupCompetitionRelationList() {
        return sysUserGroupCompetitionRelationList;
    }

    public void setSysUserGroupCompetitionRelationList(List<List<SysUserGroupCompetitionRelation>> sysUserGroupCompetitionRelationList) {
        this.sysUserGroupCompetitionRelationList = sysUserGroupCompetitionRelationList;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Date getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public Integer getUploadedCount() {
        return uploadedCount;
    }

    public void setUploadedCount(Integer uploadedCount) {
        this.uploadedCount = uploadedCount;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Integer getDownCount() {
        return downCount;
    }

    public void setDownCount(Integer downCount) {
        this.downCount = downCount;
    }

    public Boolean getReadCountFlag() {
        return readCountFlag;
    }

    public void setReadCountFlag(Boolean readCountFlag) {
        this.readCountFlag = readCountFlag;
    }

    public Integer getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(Integer notificationCount) {
        this.notificationCount = notificationCount;
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
            .append("taskName", getTaskName())
            .append("userGroupIds", getUserGroupIds())
            .append("taskStatus", getTaskStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
