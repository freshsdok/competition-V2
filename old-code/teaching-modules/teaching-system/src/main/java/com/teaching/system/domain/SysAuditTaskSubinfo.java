package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.ChapterAuditResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 审核任务审核信息对象 sys_audit_task_subinfo
 *
 * @author teaching
 * @date 2025-10-16
 */
public class SysAuditTaskSubinfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long subId;

    /**
     * 主任务id
     */
    @NotNull(message = "主任务id不能为空")
    @Excel(name = "主任务id")
    private Long taskId;

    /**
     * 流程节点id
     */
    @Excel(name = "流程节点id")
    @NotNull(message = "流程节点id不能为空")
    private Long auditConfigId;

    /**
     * 审核人员id
     */
    @Excel(name = "审核人员id")
    private Long checkPer;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkTime;

    /**
     * 审核意见
     */
    @Excel(name = "审核意见")
    private String checkOpinion;

    /**
     * 审核状态
     */
    @NotBlank(message = "审核状态不能为空")
    @Excel(name = "审核状态")
    private String checkStatus;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 数据权限用户id
     */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /**
     * 数据权限机构id
     */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /**
     * 管理员是否介入审核 Y/N
     */
    private String adminIntervention;

    /**
     * 审核的视频id
     */
    private Long videoId;

    /**
     * 视频审核信息
     */
    private ChapterAuditResult chapterAuditResult;

    public ChapterAuditResult getChapterAuditResult() {
        return chapterAuditResult;
    }

    public void setChapterAuditResult(ChapterAuditResult chapterAuditResult) {
        this.chapterAuditResult = chapterAuditResult;
    }

    public void setSubId(Long subId) {
        this.subId = subId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getSubId() {
        return subId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setAuditConfigId(Long auditConfigId) {
        this.auditConfigId = auditConfigId;
    }

    public Long getAuditConfigId() {
        return auditConfigId;
    }

    public void setCheckPer(Long checkPer) {
        this.checkPer = checkPer;
    }

    public Long getCheckPer() {
        return checkPer;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckOpinion(String checkOpinion) {
        this.checkOpinion = checkOpinion;
    }

    public String getCheckOpinion() {
        return checkOpinion;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public String getAdminIntervention() {
        return adminIntervention;
    }

    public void setAdminIntervention(String adminIntervention) {
        this.adminIntervention = adminIntervention;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("subId", getSubId())
                .append("taskId", getTaskId())
                .append("auditConfigId", getAuditConfigId())
                .append("checkPer", getCheckPer())
                .append("checkTime", getCheckTime())
                .append("checkOpinion", getCheckOpinion())
                .append("checkStatus", getCheckStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
