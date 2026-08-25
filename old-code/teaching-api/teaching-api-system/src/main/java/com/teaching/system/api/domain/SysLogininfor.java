package com.teaching.system.api.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 系统访问记录表 sys_logininfor
 *
 * @author teaching
 */
public class SysLogininfor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    @Excel(name = "访问编号", cellType = ColumnType.NUMERIC)
    private Long infoId;

    /** 用户账号 */
    @Excel(name = "用户账号")
    private String userName;

    /** 状态 0成功 1失败 */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /** 地址 */
    @Excel(name = "地址")
    private String ipaddr;

    /** 描述 */
    @Excel(name = "描述")
    private String msg;

    /** 操作系统 */
    @Excel(name = "操作系统")
    private String os;

    /** 浏览器 */
    @Excel(name = "浏览器")
    private String browser;

    /** 访问时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date accessTime;

    public Long getInfoId()
    {
        return infoId;
    }

    public void setInfoId(Long infoId)
    {
        this.infoId = infoId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public Date getAccessTime()
    {
        return accessTime;
    }

    public void setAccessTime(Date accessTime)
    {
        this.accessTime = accessTime;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }
}
