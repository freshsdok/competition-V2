package com.teaching.system.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 微信集成表 wechat_integration
 * 
 * @author teaching
 */
public class WechatIntegration extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @Excel(name = "主键ID", cellType = ColumnType.NUMERIC)
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = ColumnType.NUMERIC)
    private Long userId;

    /** 微信OpenID */
    @Excel(name = "微信OpenID")
    private String wxOpenId;

    /** 微信昵称 */
    @Excel(name = "微信昵称")
    private String nickName;

    /** 绑定时间 */
    @Excel(name = "绑定时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    /** 绑定状态（0未绑定 1已绑定 2已解绑） */
    @Excel(name = "绑定状态", readConverterExp = "0=未绑定,1=已绑定,2=已解绑")
    private String status;

    /** 最后活跃时间 */
    @Excel(name = "最后活跃时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastActivTime;

    /** 版本号 */
    private Integer version;

    /** 删除标志 */
    private String delFlag;

    /** 绑定状态列表（用于查询） */
    private String statusList;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotNull(message = "用户ID不能为空")
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @NotBlank(message = "微信OpenID不能为空")
    @Size(min = 0, max = 100, message = "微信OpenID长度不能超过100个字符")
    public String getWxOpenId()
    {
        return wxOpenId;
    }

    public void setWxOpenId(String wxOpenId)
    {
        this.wxOpenId = wxOpenId;
    }

    @Size(min = 0, max = 100, message = "微信昵称长度不能超过100个字符")
    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public Date getBindTime()
    {
        return bindTime;
    }

    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getLastActivTime()
    {
        return lastActivTime;
    }

    public void setLastActivTime(Date lastActivTime)
    {
        this.lastActivTime = lastActivTime;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getStatusList()
    {
        return statusList;
    }

    public void setStatusList(String statusList)
    {
        this.statusList = statusList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("wxOpenId", getWxOpenId())
            .append("nickName", getNickName())
            .append("bindTime", getBindTime())
            .append("status", getStatus())
            .append("lastActivTime", getLastActivTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
