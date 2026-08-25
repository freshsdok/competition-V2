package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.ChapterAuditResult;
import com.teaching.system.api.domain.course.CourseChapterVideo;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 审核任务对象 sys_audit_task
 *
 * @author teaching
 * @date 2025-10-16
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysAuditTask extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 业务表名
     */
    @Excel(name = "业务表名")
    private String businessTable;

    /**
     * 业务id
     */
    @Excel(name = "业务id")
    private Long businessId;

    /**
     * 业务详情信息
     */
    @JsonIgnore
    private String businessDetails;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 教师所在学校
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String teacherSchool;
    /**
     * 教师所在学校名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String teacherSchoolName;

    /**
     * 学校老师认证数量
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String schoolTeacherCount;

    /**
     * 学校已认证老师信息列表
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Map<String, Object>> schoolTeacherNames;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date subTime;

    /**
     * 提交人员
     */
    @Excel(name = "提交人员")
    private String subPer;

    /**
     * 审核流程id
     */
    @Excel(name = "审核流程id")
    private Long auditId;

    /**
     * 当前审核环节（节点id）
     */
    @Excel(name = "当前审核环节", readConverterExp = "节点id")
    private Long nowCheckStep;

    /**
     * 整个流程审核完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "整个流程审核完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /**
     * 整个流程审核状态
     */
    @Excel(name = "整个流程审核状态")
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
     * 审核标题
     */
    private String auditTitle;
    /**
     * 审核类型
     */
    private String auditType;
    /**
     * 当前环节名称
     */
    private String levelName;
    /**
     * 审核人
     */
    private String checkPer;
    /**
     * 审核意见
     */
    private String checkOpinion;

    /**
     * 业务详情 走审核流程的业务
     */
    private Map<String, Object> businessDetail;

    /**
     * 审核详情信息
     */
    private List<SysAuditTaskSubinfo> subInfos;

    /**
     * 章节视频审核结果
     */
    private ChapterAuditResult chapterAuditResult;

    /**
     * 章节视频详情信息
     */
    private List<CourseChapterVideo> courseChapterVideos;

    /**
     * 审核流程图信息
     */
    private List<Map<String, Object>> reviewProcess;

    /**
     * 视频id集合，逗号分隔
     */
    private String videoIds;

    /**
     * 管理员是否在介入审核中 Y/N
     */
    private String adminAccessing;

    public SysAuditTask() {
    }

    public SysAuditTask(Long businessId, String auditType) {
        this.businessId = businessId;
        this.auditType = auditType;
    }

    public String getSchoolTeacherCount() {
        return schoolTeacherCount;
    }

    public void setSchoolTeacherCount(String schoolTeacherCount) {
        this.schoolTeacherCount = schoolTeacherCount;
    }

    public List<Map<String, Object>> getSchoolTeacherNames() {
        return schoolTeacherNames;
    }

    public void setSchoolTeacherNames(List<Map<String, Object>> schoolTeacherNames) {
        this.schoolTeacherNames = schoolTeacherNames;
    }

    public String getTeacherSchoolName() {
        return teacherSchoolName;
    }

    public void setTeacherSchoolName(String teacherSchoolName) {
        this.teacherSchoolName = teacherSchoolName;
    }

    public String getAdminAccessing() {
        return adminAccessing;
    }

    public void setAdminAccessing(String adminAccessing) {
        this.adminAccessing = adminAccessing;
    }

    public ChapterAuditResult getChapterAuditResult() {
        return chapterAuditResult;
    }

    public List<CourseChapterVideo> getCourseChapterVideos() {
        return courseChapterVideos;
    }

    public void setCourseChapterVideos(List<CourseChapterVideo> courseChapterVideos) {
        this.courseChapterVideos = courseChapterVideos;
    }

    public void setChapterAuditResult(ChapterAuditResult chapterAuditResult) {
        this.chapterAuditResult = chapterAuditResult;
    }

    public String getCheckOpinion() {
        return checkOpinion;
    }

    public void setCheckOpinion(String checkOpinion) {
        this.checkOpinion = checkOpinion;
    }

    /**
     * 构造方法
     *
     * @param auditId 审核流程id
     */
    public SysAuditTask(Long auditId) {
        this.auditId = auditId;
    }

    public String getVideoIds() {
        return videoIds;
    }

    public void setVideoIds(String videoIds) {
        this.videoIds = videoIds;
    }

    public String getTeacherSchool() {
        return teacherSchool;
    }

    public void setTeacherSchool(String teacherSchool) {
        this.teacherSchool = teacherSchool;
    }

    public String getCheckPer() {
        return checkPer;
    }

    public void setCheckPer(String checkPer) {
        this.checkPer = checkPer;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAuditTitle() {
        return auditTitle;
    }

    public void setAuditTitle(String auditTitle) {
        this.auditTitle = auditTitle;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public List<SysAuditTaskSubinfo> getSubInfos() {
        return subInfos;
    }

    public void setSubInfos(List<SysAuditTaskSubinfo> subInfos) {
        this.subInfos = subInfos;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setBusinessTable(String businessTable) {
        this.businessTable = businessTable;
    }

    public String getBusinessTable() {
        return businessTable;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setSubTime(Date subTime) {
        this.subTime = subTime;
    }

    public Date getSubTime() {
        return subTime;
    }

    public void setSubPer(String subPer) {
        this.subPer = subPer;
    }

    public String getSubPer() {
        return subPer;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setNowCheckStep(Long nowCheckStep) {
        this.nowCheckStep = nowCheckStep;
    }

    public Long getNowCheckStep() {
        return nowCheckStep;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public Date getCheckTime() {
        return checkTime;
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

    public List<Map<String, Object>> getReviewProcess() {
        return reviewProcess;
    }

    public void setReviewProcess(List<Map<String, Object>> reviewProcess) {
        this.reviewProcess = reviewProcess;
    }

    public String getBusinessDetails() {
        return businessDetails;
    }

    public void setBusinessDetails(String businessDetails) {
        this.businessDetails = businessDetails;
    }

    public Map<String, Object> getBusinessDetail() {
        return businessDetail;
    }

    public void setBusinessDetail(Map<String, Object> businessDetail) {
        this.businessDetail = businessDetail;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("taskId", getTaskId())
                .append("businessTable", getBusinessTable())
                .append("businessId", getBusinessId())
                .append("subTime", getSubTime())
                .append("subPer", getSubPer())
                .append("auditId", getAuditId())
                .append("nowCheckStep", getNowCheckStep())
                .append("checkTime", getCheckTime())
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
