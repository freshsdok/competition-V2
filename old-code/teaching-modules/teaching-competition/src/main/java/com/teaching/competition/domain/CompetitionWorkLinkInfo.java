package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

/**
 * 作品打分链接信息对象 competition_work_link_info
 * 
 * @author teaching
 * @date 2025-11-19
 */
public class CompetitionWorkLinkInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long linkId;

    /** 链接url */
    @Excel(name = "链接url")
    private String linkUrl;

    /** 链接名称 */
    @Excel(name = "链接名称")
    private String linkName;

    /** 作品id集合 */
    @Excel(name = "作品id集合")
    private String worksId;

    /** 赛事id */
    @Excel(name = "赛事id")
    private Long competitionSeriesId;

    /** 赛事名称 */
    private String competitionName;

    /** 赛事阶段id */
    @Excel(name = "赛事阶段id")
    private String stageId;

    /** 赛事阶段名称 */
    private String stageName;

    /** 抽取码 */
    private String extractionCode;

    /** 抽取码时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date extractionCodeTime;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    private List<CompetitionWorks> competitionWorksList;

    public void setLinkId(Long linkId) 
    {
        this.linkId = linkId;
    }

    public Long getLinkId() 
    {
        return linkId;
    }

    public void setLinkUrl(String linkUrl) 
    {
        this.linkUrl = linkUrl;
    }

    public String getLinkUrl() 
    {
        return linkUrl;
    }

    public void setLinkName(String linkName) 
    {
        this.linkName = linkName;
    }

    public String getLinkName() 
    {
        return linkName;
    }

    public void setWorksId(String worksId) 
    {
        this.worksId = worksId;
    }

    public String getWorksId() 
    {
        return worksId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) 
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() 
    {
        return competitionSeriesId;
    }

    public void setStageId(String stageId) 
    {
        this.stageId = stageId;
    }

    public String getStageId() 
    {
        return stageId;
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

    public List<CompetitionWorks> getCompetitionWorksList() {
        return competitionWorksList;
    }

    public void setCompetitionWorksList(List<CompetitionWorks> competitionWorksList) {
        this.competitionWorksList = competitionWorksList;
    }

    public String getExtractionCode() {
        return extractionCode;
    }

    public void setExtractionCode(String extractionCode) {
        this.extractionCode = extractionCode;
    }

    public Date getExtractionCodeTime() {
        return extractionCodeTime;
    }

    public void setExtractionCodeTime(Date extractionCodeTime) {
        this.extractionCodeTime = extractionCodeTime;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("linkId", getLinkId())
            .append("linkUrl", getLinkUrl())
            .append("linkName", getLinkName())
            .append("worksId", getWorksId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("stageId", getStageId())
                .append("extractionCode", getExtractionCode())
                .append("extractionCodeTime", getExtractionCodeTime())
                .append("competitionName", getCompetitionName())
                .append("stageName", getStageName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
