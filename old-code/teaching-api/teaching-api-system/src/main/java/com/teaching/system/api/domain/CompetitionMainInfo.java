package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 赛事主数据对象 competition_main_info
 *
 * @author teaching
 * @date 2025-10-10
 */
public class CompetitionMainInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 赛事id
     */
    private Long competitionId;

    /**
     * 赛事编码
     */
    @Excel(name = "赛事编码")
    private String competitionCode;

    /**
     * 赛事名称
     */
    @Excel(name = "赛事名称")
    private String competitionName;

    /**
     * 主办方信息
     */
    @Excel(name = "主办方信息")
    private String organizer;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛事系列名称
     */
    private String competitionSeriesName;

    /**
     * 赛事类型
     */
    private String competitionType;

    /**
     * 赛事描述
     */
    private String competitionDesc;

    /**
     * 赞助方企业名称
     */
    private String enterpriseName;

    /**
     * 赛事状态
     */
    private String checkStatus;

    /**
     * 赛事开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionStartTime;

    /**
     * 赛事结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionEndTime;

    /**
     * 赛事最大参赛人数
     */
    private String competitionNumber;

    /**
     * 赞助方企业id
     */
    private Long enterpriseId;

    /**
     * 赛事启动状态
     */
    private String competitionStatus;

    /**
     * 赛事奖金金额
     */
    private String bonusNum;

    /**
     * 删除标识
     */
    private String delFlag;

    private Long userId;

    private Long orgId;

    private String applyReason;

    // 用户端使用
    // 赛事收藏数量
    private Integer competitionCollectNum;

    // 用户端使用
    // 赛事分享数量
    private Integer competitionShareNum;

    private String competitionExtension;

    private String competitionImageName;

    private String competitionImage;

    private String publishPerson;

    private String publishTime;

    private String publishPersonName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionCode(String competitionCode) {
        this.competitionCode = competitionCode;
    }

    public String getCompetitionCode() {
        return competitionCode;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getOrganizer() {
        return organizer;
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

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCompetitionDesc() {
        return competitionDesc;
    }

    public void setCompetitionDesc(String competitionDesc) {
        this.competitionDesc = competitionDesc;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Date getCompetitionStartTime() {
        return competitionStartTime;
    }

    public void setCompetitionStartTime(Date competitionStartTime) {
        this.competitionStartTime = competitionStartTime;
    }

    public Date getCompetitionEndTime() {
        return competitionEndTime;
    }

    public void setCompetitionEndTime(Date competitionEndTime) {
        this.competitionEndTime = competitionEndTime;
    }

    public String getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(String competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public String getBonusNum() {
        return bonusNum;
    }

    public void setBonusNum(String bonusNum) {
        this.bonusNum = bonusNum;
    }

    public String getCompetitionNumber() {
        return competitionNumber;
    }

    public void setCompetitionNumber(String competitionNumber) {
        this.competitionNumber = competitionNumber;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public Integer getCompetitionCollectNum() {
        return competitionCollectNum;
    }

    public void setCompetitionCollectNum(Integer competitionCollectNum) {
        this.competitionCollectNum = competitionCollectNum;
    }

    public Integer getCompetitionShareNum() {
        return competitionShareNum;
    }

    public void setCompetitionShareNum(Integer competitionShareNum) {
        this.competitionShareNum = competitionShareNum;
    }

    public String getCompetitionExtension() {
        return competitionExtension;
    }

    public void setCompetitionExtension(String competitionExtension) {
        this.competitionExtension = competitionExtension;
    }

    public String getCompetitionImageName() {
        return competitionImageName;
    }

    public void setCompetitionImageName(String competitionImageName) {
        this.competitionImageName = competitionImageName;
    }

    public String getCompetitionImage() {
        return competitionImage;
    }

    public void setCompetitionImage(String competitionImage) {
        this.competitionImage = competitionImage;
    }

    public String getPublishPerson() {
        return publishPerson;
    }

    public void setPublishPerson(String publishPerson) {
        this.publishPerson = publishPerson;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getPublishPersonName() {
        return publishPersonName;
    }

    public void setPublishPersonName(String publishPersonName) {
        this.publishPersonName = publishPersonName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("competitionId", getCompetitionId())
                .append("competitionCode", getCompetitionCode())
                .append("competitionName", getCompetitionName())
                .append("organizer", getOrganizer())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("competitionSeriesName", getCompetitionSeriesName())
                .append("competitionType", getCompetitionType())
                .append("competitionDesc", getCompetitionDesc())
                .append("enterpriseName", getEnterpriseName())
                .append("checkStatus", getCheckStatus())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("competitionStartTime", getCompetitionStartTime())
                .append("competitionEndTime", getCompetitionEndTime())
                .append("competitionStatus", getCompetitionStatus())
                .append("bonusNum", getBonusNum())
                .append("competitionNumber", getCompetitionNumber())
                .append("enterpriseId", getEnterpriseId())
                .append("applyReason", getApplyReason())
                .append("competitionExtension", getCompetitionExtension())
                .append("competitionImageName", getCompetitionImageName())
                .append("competitionImage", getCompetitionImage())
                .append("publishPerson", getPublishPerson())
                .append("publishTime", getPublishTime())
                .append("publishPersonName", getPublishPersonName())
                .toString();
    }
}
