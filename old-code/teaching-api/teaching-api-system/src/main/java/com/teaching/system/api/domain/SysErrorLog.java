package com.teaching.system.api.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 错误日志记录表 sys_error_log
 * 
 * @author teaching
 */
public class SysErrorLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 错误日志主键 */
    @Excel(name = "错误序号", cellType = ColumnType.NUMERIC)
    private Long errorId;

    /** 错误编码 */
    @Excel(name = "错误编码")
    private String errorCode;

    /** 错误类型 */
    @Excel(name = "错误类型", readConverterExp = "系统错误=系统错误,业务错误=业务错误,SQL错误=SQL错误,网络错误=网络错误")
    private String errorType;

    /** 错误级别 */
    @Excel(name = "错误级别", readConverterExp = "ERROR=错误,WARN=警告,FATAL=致命")
    private String errorLevel;

    /** 错误消息 */
    @Excel(name = "错误消息")
    private String errorMessage;

    /** 异常类名 */
    @Excel(name = "异常类名")
    private String exceptionClass;

    /** 异常方法 */
    @Excel(name = "异常方法")
    private String exceptionMethod;

    /** 堆栈信息 */
    private String stackTrace;

    /** 请求URL */
    @Excel(name = "请求URL")
    private String requestUrl;

    /** 请求方式 */
    @Excel(name = "请求方式")
    private String requestMethod;

    /** 请求参数 */
    @Excel(name = "请求参数")
    private String requestParam;

    /** 用户代理 */
    private String userAgent;

    /** 操作人员 */
    @Excel(name = "操作人员")
    private String operName;

    /** 操作IP */
    @Excel(name = "操作IP")
    private String operIp;

    /** 浏览器 */
    @Excel(name = "浏览器")
    private String browser;

    /** 操作系统 */
    @Excel(name = "操作系统")
    private String os;

    /** 处理状态 */
    @Excel(name = "处理状态", readConverterExp = "0=未处理,1=已处理,2=已忽略")
    private String status;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handleBy;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理备注 */
    @Excel(name = "处理备注")
    private String handleRemark;

    /** 错误发生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "错误时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date errorTime;

    /** 错误类型列表（用于查询） */
    private List<String> errorTypeList;

    /** 处理状态列表（用于查询） */
    private List<String> statusList;

    public Long getErrorId()
    {
        return errorId;
    }

    public void setErrorId(Long errorId)
    {
        this.errorId = errorId;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorType()
    {
        return errorType;
    }

    public void setErrorType(String errorType)
    {
        this.errorType = errorType;
    }

    public String getErrorLevel()
    {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel)
    {
        this.errorLevel = errorLevel;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getExceptionClass()
    {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass)
    {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMethod()
    {
        return exceptionMethod;
    }

    public void setExceptionMethod(String exceptionMethod)
    {
        this.exceptionMethod = exceptionMethod;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace)
    {
        this.stackTrace = stackTrace;
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

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }

    public String getOperIp()
    {
        return operIp;
    }

    public void setOperIp(String operIp)
    {
        this.operIp = operIp;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getHandleBy()
    {
        return handleBy;
    }

    public void setHandleBy(String handleBy)
    {
        this.handleBy = handleBy;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public String getHandleRemark()
    {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark)
    {
        this.handleRemark = handleRemark;
    }

    public Date getErrorTime()
    {
        return errorTime;
    }

    public void setErrorTime(Date errorTime)
    {
        this.errorTime = errorTime;
    }

    public List<String> getErrorTypeList()
    {
        return errorTypeList;
    }

    public void setErrorTypeList(List<String> errorTypeList)
    {
        this.errorTypeList = errorTypeList;
    }

    public List<String> getStatusList()
    {
        return statusList;
    }

    public void setStatusList(List<String> statusList)
    {
        this.statusList = statusList;
    }
}
