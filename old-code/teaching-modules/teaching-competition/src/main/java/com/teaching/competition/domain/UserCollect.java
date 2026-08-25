package com.teaching.competition.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 用户收藏信息对象 user_collect
 * 
 * @author teaching
 * @date 2025-10-22
 */
public class UserCollect extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收藏id */
    private Long collectId;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 课程id */
    @Excel(name = "课程id")
    private Long courseId;

    /** 赛事id */
    @Excel(name = "赛事id")
    private Long competitionId;

    /** 赛事界id */
    @Excel(name = "赛事界id")
    private Long competitionSeriesId;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public void setCollectId(Long collectId) 
    {
        this.collectId = collectId;
    }

    public Long getCollectId() 
    {
        return collectId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setCompetitionId(Long competitionId) 
    {
        this.competitionId = competitionId;
    }

    public Long getCompetitionId() 
    {
        return competitionId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) 
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() 
    {
        return competitionSeriesId;
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

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("collectId", getCollectId())
            .append("userId", getUserId())
            .append("courseId", getCourseId())
            .append("competitionId", getCompetitionId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("orgId", getOrgId())
            .toString();
    }
}
