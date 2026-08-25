package com.teaching.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 推送信息日志对象 sys_sender_message_log
 * 
 * @author teaching
 * @date 2026-01-30
 */
public class SysSenderMessageLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long sendId;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operator;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    /** 操作ip */
    @Excel(name = "操作ip")
    private String operationIp;

    /** 验证类型 */
    @Excel(name = "验证类型")
    private String verifyType;

    /** 验证结果 */
    @Excel(name = "验证结果")
    private String verifyResult;

    /** 验证码 */
    @Excel(name = "验证码")
    private String verifyCode;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 机构 */
    @Excel(name = "机构")
    private Long orgId;

    /** 原因 */
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTimeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTimeEnd;

    public void setSendId(Long sendId) 
    {
        this.sendId = sendId;
    }

    public Long getSendId() 
    {
        return sendId;
    }

    public void setOperator(String operator) 
    {
        this.operator = operator;
    }

    public String getOperator() 
    {
        return operator;
    }

    public void setOperationTime(Date operationTime) 
    {
        this.operationTime = operationTime;
    }

    public Date getOperationTime() 
    {
        return operationTime;
    }

    public void setOperationIp(String operationIp) 
    {
        this.operationIp = operationIp;
    }

    public String getOperationIp() 
    {
        return operationIp;
    }

    public void setVerifyType(String verifyType) 
    {
        this.verifyType = verifyType;
    }

    public String getVerifyType() 
    {
        return verifyType;
    }

    public void setVerifyResult(String verifyResult) 
    {
        this.verifyResult = verifyResult;
    }

    public String getVerifyResult() 
    {
        return verifyResult;
    }

    public void setVerifyCode(String verifyCode) 
    {
        this.verifyCode = verifyCode;
    }

    public String getVerifyCode() 
    {
        return verifyCode;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getOperationTimeStart() {
        return operationTimeStart;
    }

    public void setOperationTimeStart(Date operationTimeStart) {
        this.operationTimeStart = operationTimeStart;
    }

    public Date getOperationTimeEnd() {
        return operationTimeEnd;
    }

    public void setOperationTimeEnd(Date operationTimeEnd) {
        this.operationTimeEnd = operationTimeEnd;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("sendId", getSendId())
            .append("operator", getOperator())
            .append("operationTime", getOperationTime())
            .append("operationIp", getOperationIp())
            .append("verifyType", getVerifyType())
            .append("verifyResult", getVerifyResult())
            .append("verifyCode", getVerifyCode())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("orgId", getOrgId())
                .append("reason", getReason())
            .toString();
    }
}
