package com.teaching.course.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 课程推荐关联关系对象 course_recommend_rela
 *
 * @author teaching
 * @date 2025-10-23
 */
public class CourseRecommendRela extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 关联关系id
     */
    private Long id;

    /**
     * 课程推荐id
     */
    @Excel(name = "课程推荐id")
    private Long remdId;

    /**
     * 课程id
     */
    @Excel(name = "课程id")
    private Long courseId;

    /**
     * 排序
     */
    @Excel(name = "排序")
    private Long sort;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setRemdId(Long remdId) {
        this.remdId = remdId;
    }

    public Long getRemdId() {
        return remdId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public Long getSort() {
        return sort;
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
                .append("id", getId())
                .append("remdId", getRemdId())
                .append("courseId", getCourseId())
                .append("sort", getSort())
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
