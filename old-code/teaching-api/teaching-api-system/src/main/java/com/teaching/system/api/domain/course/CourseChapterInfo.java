package com.teaching.system.api.domain.course;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 章节信息对象 course_chapter_info
 *
 * @author teaching
 * @date 2025-10-22
 */
public class CourseChapterInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 章节id
     */
    private Long chapterId;

    /**
     * 章节code
     */
    @Excel(name = "章节code")
    private String chapterCode;

    /**
     * 章节名称
     */
    @Excel(name = "章节名称")
    private String chapterName;

    /**
     * 章节序号
     */
    @Excel(name = "章节序号")
    private String chapterNum;

    /**
     * 课程id
     */
    @Excel(name = "课程id")
    private Long courseId;
    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 章节描述
     */
    @Excel(name = "章节描述")
    private String description;

    /**
     * 学时
     */
    @Excel(name = "学时")
    private String creditHour;

    /**
     * 是否免费(字典sys_yes_no
     */
    @Excel(name = "是否免费(字典sys_yes_no")
    private String isFree;

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
     * 章节视频列表
     */
    private List<CourseChapterVideo> chapterVideoList;

    public List<CourseChapterVideo> getChapterVideoList() {
        return chapterVideoList;
    }

    public void setChapterVideoList(List<CourseChapterVideo> chapterVideoList) {
        this.chapterVideoList = chapterVideoList;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setChapterCode(String chapterCode) {
        this.chapterCode = chapterCode;
    }

    public String getChapterCode() {
        return chapterCode;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterNum(String chapterNum) {
        this.chapterNum = chapterNum;
    }

    public String getChapterNum() {
        return chapterNum;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setCreditHour(String creditHour) {
        this.creditHour = creditHour;
    }

    public String getCreditHour() {
        return creditHour;
    }

    public void setIsFree(String isFree) {
        this.isFree = isFree;
    }

    public String getIsFree() {
        return isFree;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("chapterId", getChapterId())
                .append("chapterCode", getChapterCode())
                .append("chapterName", getChapterName())
                .append("chapterNum", getChapterNum())
                .append("courseId", getCourseId())
                .append("description", getDescription())
                .append("creditHour", getCreditHour())
                .append("isFree", getIsFree())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
