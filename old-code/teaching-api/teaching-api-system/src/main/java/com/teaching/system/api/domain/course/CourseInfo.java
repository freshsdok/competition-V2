package com.teaching.system.api.domain.course;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 课程信息对象 course_info
 *
 * @author teaching
 * @date 2025-10-22
 */
public class CourseInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 课程编号
     */
    @Excel(name = "课程编号")
    private String courseCode;

    /**
     * 学校编码
     */
    @Excel(name = "学校编码")
    private String schoolCode;

    /**
     * 学院编码
     */
    @Excel(name = "学院编码")
    private String collegeCode;

    /**
     * 课程名字
     */
    @Excel(name = "课程名字")
    @NotBlank(message = "课程名字不能为空")
    private String name;

    /**
     * 课程分类
     */
    @Excel(name = "课程分类")
    @NotNull(message = "课程分类不能为空")
    private Long classifyId;

    /**
     * 课程分类名称
     */
    @Excel(name = "课程分类名称")
    private String classifyName;

    /**
     * 授课老师
     */
    @Excel(name = "授课老师")
    private String teacher;

    /**
     * 学时
     */
    @Excel(name = "学时")
    private String creditHour;

    /**
     * 学分
     */
    @Excel(name = "学分")
    private String studyScore;

    /**
     * 价格
     */
    @Excel(name = "价格")
    private String price;

    /**
     * 难度(字典course_difficulty
     */
    @Excel(name = "难度(字典course_difficulty")
    @NotBlank(message = "难度不能为空")
    private String difficultyLevel;

    /**
     * 审核状态(字典check_status
     */
    @Excel(name = "审核状态(字典check_status")
    private String checkStatus;

    /**
     * 审核意见
     */
    private String applyReason;

    /**
     * 发布状态
     */
    @Excel(name = "发布状态")
    private String publishStatus;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishTime;

    /**
     * 课程介绍
     */
    @Excel(name = "课程介绍")
    @NotBlank(message = "课程介绍不能为空")
    private String details;

    /**
     * 封面图
     */
    @Excel(name = "封面图")
    private String coverImage;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

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
     * 章节信息信息
     */
    private List<CourseChapterInfo> courseChapterInfoList;

    /**
     * 课程总时长（单位：秒）
     */
    private Long videoTotalDuration;

    public CourseInfo() {
    }

    public CourseInfo(Long courseId, String checkStatus) {
        this.checkStatus = checkStatus;
        this.courseId = courseId;
    }

    public CourseInfo(Long courseId, String checkStatus, String applyReason) {
        this.courseId = courseId;
        this.checkStatus = checkStatus;
        this.applyReason = applyReason;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public Long getVideoTotalDuration() {
        return videoTotalDuration;
    }

    public void setVideoTotalDuration(Long videoTotalDuration) {
        this.videoTotalDuration = videoTotalDuration;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = collegeCode;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public @NotNull(message = "课程分类不能为空") Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(@NotNull(message = "课程分类不能为空") Long classifyId) {
        this.classifyId = classifyId;
    }

    public String getClassifyName() {
        return classifyName;
    }

    public void setClassifyName(String classifyName) {
        this.classifyName = classifyName;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setCreditHour(String creditHour) {
        this.creditHour = creditHour;
    }

    public String getCreditHour() {
        return creditHour;
    }

    public void setStudyScore(String studyScore) {
        this.studyScore = studyScore;
    }

    public String getStudyScore() {
        return studyScore;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
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

    public List<CourseChapterInfo> getCourseChapterInfoList() {
        return courseChapterInfoList;
    }

    public void setCourseChapterInfoList(List<CourseChapterInfo> courseChapterInfoList) {
        this.courseChapterInfoList = courseChapterInfoList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("courseId", getCourseId())
                .append("courseCode", getCourseCode())
                .append("schoolCode", getSchoolCode())
                .append("collegeCode", getCollegeCode())
                .append("name", getName())
                .append("classifyId", getClassifyId())
                .append("teacher", getTeacher())
                .append("creditHour", getCreditHour())
                .append("studyScore", getStudyScore())
                .append("price", getPrice())
                .append("difficultyLevel", getDifficultyLevel())
                .append("checkStatus", getCheckStatus())
                .append("publishStatus", getPublishStatus())
                .append("publishTime", getPublishTime())
                .append("details", getDetails())
                .append("coverImage", getCoverImage())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("courseChapterInfoList", getCourseChapterInfoList())
                .toString();
    }
}
