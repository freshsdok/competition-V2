package com.teaching.wxApp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 二维码生成记录对象 wx_qc_code_record
 *
 * @author teaching
 * @date 2026-04-08
 */
public class WxQcCodeRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 记录id
     */
    private Long recordId;

    /**
     * 二维码管理表ID
     */
    private Long codeConfigId;

    /**
     * 学校
     */
    private Long school;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 考场
     */
    private String examinationHall;

    /**
     * 考试时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date examTime;

    /**
     * 二维码状态
     */
    private String codeStatus;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 二维码base64
     */
    private String codeBase64;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getCodeConfigId() {
        return codeConfigId;
    }

    public void setCodeConfigId(Long codeConfigId) {
        this.codeConfigId = codeConfigId;
    }

    public Long getSchool() {
        return school;
    }

    public void setSchool(Long school) {
        this.school = school;
    }

    public String getExaminationHall() {
        return examinationHall;
    }

    public void setExaminationHall(String examinationHall) {
        this.examinationHall = examinationHall;
    }

    public Date getExamTime() {
        return examTime;
    }

    public void setExamTime(Date examTime) {
        this.examTime = examTime;
    }

    public String getCodeStatus() {
        return codeStatus;
    }

    public void setCodeStatus(String codeStatus) {
        this.codeStatus = codeStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCodeBase64() {
        return codeBase64;
    }

    public void setCodeBase64(String codeBase64) {
        this.codeBase64 = codeBase64;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("recordId", getRecordId())
                .append("codeConfigId", getCodeConfigId())
                .append("school", getSchool())
                .append("schoolName", getSchoolName())
                .append("examinationHall", getExaminationHall())
                .append("examTime", getExamTime())
                .append("codeStatus", getCodeStatus())
                .append("userId", getUserId())
                .append("codeBase64", getCodeBase64())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
