package com.teaching.competition.domain;

import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 证书人员表 cert_player_info
 * 
 * @author teaching
 */
public class CertPlayerInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联id */
    @Excel(name = "关联id", cellType = Excel.ColumnType.NUMERIC)
    private Long relaId;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 报名人员id */
    @Excel(name = "报名人员id")
    private Long memberId;

    /** 团队code */
    @Excel(name = "团队code")
    private String teamCode;

    /** 用户证书id */
    @Excel(name = "用户证书id")
    private Long certId;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    private String delFlag;

    public Long getRelaId()
    {
        return relaId;
    }

    public void setRelaId(Long relaId)
    {
        this.relaId = relaId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    @Size(min = 0, max = 255, message = "团队code不能超过255个字符")
    public String getTeamCode()
    {
        return teamCode;
    }

    public void setTeamCode(String teamCode)
    {
        this.teamCode = teamCode;
    }

    public Long getCertId()
    {
        return certId;
    }

    public void setCertId(Long certId)
    {
        this.certId = certId;
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
            .append("relaId", getRelaId())
            .append("userId", getUserId())
            .append("memberId", getMemberId())
            .append("teamCode", getTeamCode())
            .append("certId", getCertId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
