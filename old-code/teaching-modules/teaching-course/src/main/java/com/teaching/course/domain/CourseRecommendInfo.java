package com.teaching.course.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 课程推荐信息对象 course_recommend_info
 *
 * @author teaching
 * @date 2025-10-23
 */
public class CourseRecommendInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 课程推荐id
     */
    private Long remdId;

    /**
     * 推荐名称
     */
    @Excel(name = "推荐位名称")
    @NotBlank(message = "推荐位名称不能为空")
    private String remdName;

    /**
     * 类型(字典recommended_type
     */
    @Excel(name = "类型")
    @NotBlank(message = "类型不能为空")
    private String type;

    /**
     * 权重
     */
    @Excel(name = "权重")
    private Long weight;

    /**
     * 展示开始时间
     */
    @Excel(name = "展示开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date displayStartTime;

    /**
     * 展示结束时间
     */
    @Excel(name = "展示结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date displayEndTime;

    /**
     * 目标用户群体
     */
    @Excel(name = "目标用户群体")
    private String targetUserGroup;

    /**
     * 课程数量
     */
    @Excel(name = "课程数量")
    private Long courseNum;
    /**
     * 推荐状态
     */
    @Excel(name = "推荐状态")
    private String status;

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
     * 课程推荐关联关系信息
     */
    private List<CourseRecommendRela> courseRecommendRelaList;

    public CourseRecommendInfo() {
    }

    public CourseRecommendInfo(Long remdId, Long courseNum) {
        this.remdId = remdId;
        this.courseNum = courseNum;
    }

    public Long getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(Long courseNum) {
        this.courseNum = courseNum;
    }

    public void setRemdId(Long remdId) {
        this.remdId = remdId;
    }

    public Long getRemdId() {
        return remdId;
    }

    public void setRemdName(String remdName) {
        this.remdName = remdName;
    }

    public String getRemdName() {
        return remdName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getWeight() {
        return weight;
    }

    public void setDisplayStartTime(Date displayStartTime) {
        this.displayStartTime = displayStartTime;
    }

    public Date getDisplayStartTime() {
        return displayStartTime;
    }

    public void setDisplayEndTime(Date displayEndTime) {
        this.displayEndTime = displayEndTime;
    }

    public Date getDisplayEndTime() {
        return displayEndTime;
    }

    public void setTargetUserGroup(String targetUserGroup) {
        this.targetUserGroup = targetUserGroup;
    }

    public String getTargetUserGroup() {
        return targetUserGroup;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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

    public List<CourseRecommendRela> getCourseRecommendRelaList() {
        return courseRecommendRelaList;
    }

    public void setCourseRecommendRelaList(List<CourseRecommendRela> courseRecommendRelaList) {
        this.courseRecommendRelaList = courseRecommendRelaList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("remdId", getRemdId())
                .append("remdName", getRemdName())
                .append("type", getType())
                .append("weight", getWeight())
                .append("displayStartTime", getDisplayStartTime())
                .append("displayEndTime", getDisplayEndTime())
                .append("targetUserGroup", getTargetUserGroup())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("courseRecommendRelaList", getCourseRecommendRelaList())
                .toString();
    }
}
