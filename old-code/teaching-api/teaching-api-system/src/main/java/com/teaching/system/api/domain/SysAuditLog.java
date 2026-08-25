package com.teaching.system.api.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 审计日志记录表 sys_audit_log
 * 
 * @author teaching
 */
public class SysAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审计日志主键 */
    @Excel(name = "审计序号", cellType = ColumnType.NUMERIC)
    private Long auditId;

    /** 审计类型 */
    @Excel(name = "审计类型", readConverterExp = "登录审计=登录审计,权限审计=权限审计,数据审计=数据审计,配置审计=配置审计,操作审计=操作审计")
    private String auditType;

    /** 审计分类 */
    @Excel(name = "审计分类", readConverterExp = "安全事件=安全事件,异常行为=异常行为,敏感操作=敏感操作,配置变更=配置变更")
    private String auditCategory;

    /** 风险级别 */
    @Excel(name = "风险级别", readConverterExp = "LOW=低,MEDIUM=中,HIGH=高,CRITICAL=严重")
    private String riskLevel;

    /** 事件名称 */
    @Excel(name = "事件名称")
    private String eventName;

    /** 事件描述 */
    @Excel(name = "事件描述")
    private String eventDesc;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 用户类型 */
    @Excel(name = "用户类型")
    private String userType;

    /** 部门名称 */
    @Excel(name = "部门名称")
    private String deptName;

    /** 操作类型 */
    @Excel(name = "操作类型")
    private String operationType;

    /** 操作模块 */
    @Excel(name = "操作模块")
    private String operationModule;

    /** 操作方法 */
    private String operationMethod;

    /** 请求URL */
    @Excel(name = "请求URL")
    private String requestUrl;

    /** 请求方式 */
    @Excel(name = "请求方式")
    private String requestMethod;

    /** 请求参数 */
    private String requestParam;

    /** 响应结果 */
    private String responseResult;

    /** IP地址 */
    @Excel(name = "IP地址")
    private String ipAddress;

    /** IP归属地 */
    @Excel(name = "IP归属地")
    private String ipLocation;

    /** 浏览器 */
    @Excel(name = "浏览器")
    private String browser;

    /** 操作系统 */
    @Excel(name = "操作系统")
    private String os;

    /** 设备类型 */
    @Excel(name = "设备类型")
    private String deviceType;

    /** 用户代理 */
    private String userAgent;

    /** 是否异常 */
    @Excel(name = "是否异常", readConverterExp = "0=正常,1=异常")
    private String isAbnormal;

    /** 异常原因 */
    @Excel(name = "异常原因")
    private String abnormalReason;

    /** 数据ID */
    private String dataId;

    /** 数据类型 */
    private String dataType;

    /** 变更前数据 */
    private String oldValue;

    /** 变更后数据 */
    private String newValue;

    /** 审计状态 */
    @Excel(name = "审计状态", readConverterExp = "0=待审计,1=已审计,2=已忽略")
    private String auditStatus;

    /** 审计人 */
    @Excel(name = "审计人")
    private String auditBy;

    /** 审计时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "审计时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审计备注 */
    @Excel(name = "审计备注")
    private String auditRemark;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    /** 耗时 */
    @Excel(name = "耗时", suffix = "毫秒")
    private Long costTime;

    /** 审计类型列表（用于查询） */
    private String auditTypeList;

    /** 操作类型列表（用于查询） */
    private String operationTypeList;

    /** 操作模块列表（用于查询） */
    private String operationModuleList;

    /** 是否异常列表（用于查询） */
    private String isAbnormalList;

    /** 审计状态列表（用于查询） */
    private String auditStatusList;

    public Long getAuditId()
    {
        return auditId;
    }

    public void setAuditId(Long auditId)
    {
        this.auditId = auditId;
    }

    public String getAuditType()
    {
        return auditType;
    }

    public void setAuditType(String auditType)
    {
        this.auditType = auditType;
    }

    public String getAuditCategory()
    {
        return auditCategory;
    }

    public void setAuditCategory(String auditCategory)
    {
        this.auditCategory = auditCategory;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getEventName()
    {
        return eventName;
    }

    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }

    public String getEventDesc()
    {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc)
    {
        this.eventDesc = eventDesc;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getOperationType()
    {
        return operationType;
    }

    public void setOperationType(String operationType)
    {
        this.operationType = operationType;
    }

    public String getOperationModule()
    {
        return operationModule;
    }

    public void setOperationModule(String operationModule)
    {
        this.operationModule = operationModule;
    }

    public String getOperationMethod()
    {
        return operationMethod;
    }

    public void setOperationMethod(String operationMethod)
    {
        this.operationMethod = operationMethod;
    }

    public String getRequestUrl()
    {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl)
    {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod()
    {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod)
    {
        this.requestMethod = requestMethod;
    }

    public String getRequestParam()
    {
        return requestParam;
    }

    public void setRequestParam(String requestParam)
    {
        this.requestParam = requestParam;
    }

    public String getResponseResult()
    {
        return responseResult;
    }

    public void setResponseResult(String responseResult)
    {
        this.responseResult = responseResult;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getIpLocation()
    {
        return ipLocation;
    }

    public void setIpLocation(String ipLocation)
    {
        this.ipLocation = ipLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public String getIsAbnormal()
    {
        return isAbnormal;
    }

    public void setIsAbnormal(String isAbnormal)
    {
        this.isAbnormal = isAbnormal;
    }

    public String getAbnormalReason()
    {
        return abnormalReason;
    }

    public void setAbnormalReason(String abnormalReason)
    {
        this.abnormalReason = abnormalReason;
    }

    public String getDataId()
    {
        return dataId;
    }

    public void setDataId(String dataId)
    {
        this.dataId = dataId;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public String getAuditStatus()
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus)
    {
        this.auditStatus = auditStatus;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public Date getOperationTime()
    {
        return operationTime;
    }

    public void setOperationTime(Date operationTime)
    {
        this.operationTime = operationTime;
    }

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }

    public String getAuditTypeList()
    {
        return auditTypeList;
    }

    public void setAuditTypeList(String auditTypeList)
    {
        this.auditTypeList = auditTypeList;
    }

    public String getOperationTypeList()
    {
        return operationTypeList;
    }

    public void setOperationTypeList(String operationTypeList)
    {
        this.operationTypeList = operationTypeList;
    }

    public String getOperationModuleList()
    {
        return operationModuleList;
    }

    public void setOperationModuleList(String operationModuleList)
    {
        this.operationModuleList = operationModuleList;
    }

    public String getIsAbnormalList()
    {
        return isAbnormalList;
    }

    public void setIsAbnormalList(String isAbnormalList)
    {
        this.isAbnormalList = isAbnormalList;
    }

    public String getAuditStatusList()
    {
        return auditStatusList;
    }

    public void setAuditStatusList(String auditStatusList)
    {
        this.auditStatusList = auditStatusList;
    }
}
