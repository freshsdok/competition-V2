package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 专业信息对象 school_specialty_info
 *
 * @author teaching
 * @date 2025-12-03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchoolSpecialtyInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 专业id
     */
    private Long id;

    /**
     * 学科门类
     */
    @Excel(name = "学科门类")
    private String disciplineCategory;

    /**
     * 学科门类编码（如01哲学）
     */
    @Excel(name = "学科门类编码", readConverterExp = "如=01哲学")
    private String disciplineCategoryCode;

    /**
     * 专业类名称
     */
    @Excel(name = "专业类名称")
    private String majorClass;

    /**
     * 专业类代码（如0101哲学类）
     */
    @Excel(name = "专业类代码", readConverterExp = "如=0101哲学类")
    private String majorClassCode;

    /**
     * 专业名称
     */
    @Excel(name = "专业名称")
    private String minorClass;

    /**
     * 专业代码
     */
    @Excel(name = "专业代码")
    private String minorClassCode;

    /**
     * 删除标识
     */
    private String delFlag;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDisciplineCategory(String disciplineCategory) {
        this.disciplineCategory = disciplineCategory;
    }

    public String getDisciplineCategory() {
        return disciplineCategory;
    }

    public void setDisciplineCategoryCode(String disciplineCategoryCode) {
        this.disciplineCategoryCode = disciplineCategoryCode;
    }

    public String getDisciplineCategoryCode() {
        return disciplineCategoryCode;
    }

    public void setMajorClass(String majorClass) {
        this.majorClass = majorClass;
    }

    public String getMajorClass() {
        return majorClass;
    }

    public void setMajorClassCode(String majorClassCode) {
        this.majorClassCode = majorClassCode;
    }

    public String getMajorClassCode() {
        return majorClassCode;
    }

    public void setMinorClass(String minorClass) {
        this.minorClass = minorClass;
    }

    public String getMinorClass() {
        return minorClass;
    }

    public void setMinorClassCode(String minorClassCode) {
        this.minorClassCode = minorClassCode;
    }

    public String getMinorClassCode() {
        return minorClassCode;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("disciplineCategory", getDisciplineCategory())
                .append("disciplineCategoryCode", getDisciplineCategoryCode())
                .append("majorClass", getMajorClass())
                .append("majorClassCode", getMajorClassCode())
                .append("minorClass", getMinorClass())
                .append("minorClassCode", getMinorClassCode())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
