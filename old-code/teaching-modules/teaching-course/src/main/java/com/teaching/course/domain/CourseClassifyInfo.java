package com.teaching.course.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 课程分类对象 course_classify_info
 *
 * @author teaching
 * @date 2025-10-22
 */
public class CourseClassifyInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 课程分类id
     */
    private Long classifyId;

    /**
     * 上级分类id
     */
    @Excel(name = "上级分类id")
    private Long parentClassify;
    /**
     * 上级分类名称
     */
    @Excel(name = "上级分类名称")
    private String parentName;

    /**
     * 分类名称
     */
    @Excel(name = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String classifyName;

    /**
     * 分类编码
     */
    @Excel(name = "分类编码")
    private String classifyCode;

    /**
     * 描述
     */
    @Excel(name = "描述")
    private String classifyDesc;

    /**
     * 图标
     */
    @Excel(name = "图标")
    private String classifyImage;

    /**
     * 排序权重
     */
    @Excel(name = "排序权重")
    private Long weight;

    /**
     * 状态字典subassembly_status
     */
    @Excel(name = "状态字典subassembly_status")
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

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setParentClassify(Long parentClassify) {
        this.parentClassify = parentClassify;
    }

    public Long getParentClassify() {
        return parentClassify;
    }

    public void setClassifyName(String classifyName) {
        this.classifyName = classifyName;
    }

    public String getClassifyName() {
        return classifyName;
    }

    public void setClassifyCode(String classifyCode) {
        this.classifyCode = classifyCode;
    }

    public String getClassifyCode() {
        return classifyCode;
    }

    public void setClassifyDesc(String classifyDesc) {
        this.classifyDesc = classifyDesc;
    }

    public String getClassifyDesc() {
        return classifyDesc;
    }

    public void setClassifyImage(String classifyImage) {
        this.classifyImage = classifyImage;
    }

    public String getClassifyImage() {
        return classifyImage;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getWeight() {
        return weight;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("classifyId", getClassifyId())
                .append("parentClassify", getParentClassify())
                .append("classifyName", getClassifyName())
                .append("classifyCode", getClassifyCode())
                .append("classifyDesc", getClassifyDesc())
                .append("classifyImage", getClassifyImage())
                .append("weight", getWeight())
                .append("status", getStatus())
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
