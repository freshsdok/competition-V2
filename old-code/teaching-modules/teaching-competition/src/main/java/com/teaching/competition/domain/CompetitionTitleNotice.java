package com.teaching.competition.domain;

import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 提示信息表 competition_title_notice
 * 
 * @author teaching
 */
public class CompetitionTitleNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公告id */
    @Excel(name = "公告id", cellType = Excel.ColumnType.NUMERIC)
    private Long noticeId;

    /** 说明 */
    @Excel(name = "说明")
    private String explainContent;

    /** 提示信息code */
    @Excel(name = "提示信息code")
    private Long code;

    /** 申请提示信息 */
    @Excel(name = "申请提示信息")
    private String hint;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    private String delFlag;

    public Long getNoticeId()
    {
        return noticeId;
    }

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }

    public String getExplainContent()
    {
        return explainContent;
    }

    public void setExplainContent(String explainContent)
    {
        this.explainContent = explainContent;
    }

    public Long getCode()
    {
        return code;
    }

    public void setCode(Long code)
    {
        this.code = code;
    }

    public String getHint()
    {
        return hint;
    }

    public void setHint(String hint)
    {
        this.hint = hint;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("noticeId", getNoticeId())
            .append("explainContent", getExplainContent())
            .append("code", getCode())
            .append("hint", getHint())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
