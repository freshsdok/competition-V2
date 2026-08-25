package com.teaching.system.api.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 赛事系列信息对象 competition_series_info
 *
 * @author teaching
 * @date 2025-10-13
 */
public class CompetitionSeriesInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛事id */
    @Excel(name = "赛事id")
    private Long competitionId;

    /** 赛事系列名称 */
    @Excel(name = "赛事系列名称")
    private String competitionSeriesName;

    /** 赛事系列 */
    @Excel(name = "赛事系列")
    private String competitionSeries;

    /** 赛事状态(启动/终止) */
    @Excel(name = "赛事状态(启动/终止)")
    private String competitionStatus;

    /** 赛事类型 */
    @Excel(name = "赛事类型")
    private String competitionType;

    /** 赛事描述 */
    @Excel(name = "赛事描述")
    private String competitionDesc;

    /** 赛事封面图名称 */
    private String competitionImageName;

    /** 赛事封面图 */
    @Excel(name = "赛事封面图")
    private String competitionImage;

    /** 赛事开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "赛事开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date competitionStartTime;

    /** 赛事结束日志 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "赛事结束日志", width = 30, dateFormat = "yyyy-MM-dd")
    private Date competitionEndTime;

    /** 审核状态 */
    @Excel(name = "审核状态")
    private String checkStatus;

    /** 最大参赛人数 */
    @Excel(name = "最大参赛人数")
    private String competitionNumber;

    /** 赞助方id */
    @Excel(name = "赞助方id")
    private Long enterpriseId;

    /** 公告标题 */
    private String noticeTitle;

    /** 赛事公告时间 */
    private String noticeTime;

    /** 赛事公告内容 */
    private String noticeContent;

    /** 赛事扩展字段 */
    private String competitionExtension;

    private Long publishPerson;

    private String publishPersonName;

    private Date publishTime;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public CompetitionSeriesInfo() {
    }

    public CompetitionSeriesInfo(Long competitionId, String checkStatus) {
        this.competitionId = competitionId;
        this.checkStatus = checkStatus;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionId(Long competitionId)
    {
        this.competitionId = competitionId;
    }

    public Long getCompetitionId()
    {
        return competitionId;
    }

    public void setCompetitionSeriesName(String competitionSeriesName)
    {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCompetitionSeriesName()
    {
        return competitionSeriesName;
    }

    public void setCompetitionSeries(String competitionSeries)
    {
        this.competitionSeries = competitionSeries;
    }

    public String getCompetitionSeries()
    {
        return competitionSeries;
    }

    public void setCompetitionStatus(String competitionStatus)
    {
        this.competitionStatus = competitionStatus;
    }

    public String getCompetitionStatus()
    {
        return competitionStatus;
    }

    public void setCompetitionType(String competitionType)
    {
        this.competitionType = competitionType;
    }

    public String getCompetitionType()
    {
        return competitionType;
    }

    public void setCompetitionDesc(String competitionDesc)
    {
        this.competitionDesc = competitionDesc;
    }

    public String getCompetitionDesc()
    {
        return competitionDesc;
    }

    public void setCompetitionImage(String competitionImage)
    {
        this.competitionImage = competitionImage;
    }

    public String getCompetitionImage()
    {
        return competitionImage;
    }

    public void setCompetitionStartTime(Date competitionStartTime)
    {
        this.competitionStartTime = competitionStartTime;
    }

    public Date getCompetitionStartTime()
    {
        return competitionStartTime;
    }

    public void setCompetitionEndTime(Date competitionEndTime)
    {
        this.competitionEndTime = competitionEndTime;
    }

    public Date getCompetitionEndTime()
    {
        return competitionEndTime;
    }

    public void setCheckStatus(String checkStatus)
    {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus()
    {
        return checkStatus;
    }

    public void setCompetitionNumber(String competitionNumber)
    {
        this.competitionNumber = competitionNumber;
    }

    public String getCompetitionNumber()
    {
        return competitionNumber;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setOrgId(Long orgId)
    {
        this.orgId = orgId;
    }

    public Long getOrgId()
    {
        return orgId;
    }

    public String getCompetitionImageName() {
        return competitionImageName;
    }

    public void setCompetitionImageName(String competitionImageName) {
        this.competitionImageName = competitionImageName;
    }

    public String getCompetitionExtension() {
        return competitionExtension;
    }

    public void setCompetitionExtension(String competitionExtension) {
        this.competitionExtension = competitionExtension;
    }

    public Long getPublishPerson() {
        return publishPerson;
    }

    public void setPublishPerson(Long publishPerson) {
        this.publishPerson = publishPerson;
    }

    public String getPublishPersonName() {
        return publishPersonName;
    }

    public void setPublishPersonName(String publishPersonName) {
        this.publishPersonName = publishPersonName;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(String noticeTime) {
        this.noticeTime = noticeTime;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionId", getCompetitionId())
            .append("competitionSeriesName", getCompetitionSeriesName())
            .append("competitionSeries", getCompetitionSeries())
            .append("competitionStatus", getCompetitionStatus())
            .append("competitionType", getCompetitionType())
            .append("competitionDesc", getCompetitionDesc())
            .append("competitionImage", getCompetitionImage())
            .append("competitionStartTime", getCompetitionStartTime())
            .append("competitionEndTime", getCompetitionEndTime())
            .append("checkStatus", getCheckStatus())
            .append("competitionNumber", getCompetitionNumber())
            .append("enterpriseId", getEnterpriseId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userD", getUserId())
            .append("orgId", getOrgId())
            .append("competitionImageName", getCompetitionImageName())
            .append("competitionExtension", getCompetitionExtension())
                .append("noticeTitle", getNoticeTitle())
                .append("noticeTime", getNoticeTime())
                .append("noticeContent", getNoticeContent())
            .toString();
    }
}
