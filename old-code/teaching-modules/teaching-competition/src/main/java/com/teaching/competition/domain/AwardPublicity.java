package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 获奖公示管理对象 award_publicity
 *
 * @author teaching
 * @date 2026-05-12
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwardPublicity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 获奖公示管理主表表id
     */
    private Long awardPublicityId;

    /**
     * 赛事系列id(个人参赛)
     */
    private Long competitionSeriesId;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

    /**
     * 提示信息
     */
    private String tipInfo;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 状态
     */
    private String status;

    /**
     * 导入类型
     */
    private String importType;

    /**
     * 是否已截止
     */
    private Boolean isExpired;

    public Long getAwardPublicityId() {
        return awardPublicityId;
    }

    public void setAwardPublicityId(Long awardPublicityId) {
        this.awardPublicityId = awardPublicityId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setTipInfo(String tipInfo) {
        this.tipInfo = tipInfo;
    }

    public String getTipInfo() {
        return tipInfo;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("competitionName", getCompetitionName())
                .append("expirationTime", getExpirationTime())
                .append("tipInfo", getTipInfo())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
