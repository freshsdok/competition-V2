package com.teaching.common.core.utils;

import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import com.teaching.common.core.utils.ip.IpUtils;

/**
 * 安全审计工具类
 * 用于记录系统安全、数据权限、重要配置相关的关键操作
 * 
 * @author teaching
 */
public class SecurityAuditUtils
{
    /**
     * 创建权限审计日志对象
     * 
     * @param eventName 事件名称
     * @param eventDesc 事件描述
     * @param operationType 操作类型
     * @param request HTTP请求对象
     * @return 审计日志对象（需要在调用方设置具体字段后保存）
     */
    public static Object createPermissionAuditLog(String eventName, String eventDesc, String operationType, HttpServletRequest request)
    {
        return createAuditLog("权限审计", "权限变更", eventName, eventDesc, operationType, "MEDIUM", request);
    }

    /**
     * 创建配置审计日志对象
     * 
     * @param eventName 事件名称
     * @param eventDesc 事件描述
     * @param operationType 操作类型
     * @param request HTTP请求对象
     * @return 审计日志对象
     */
    public static Object createConfigAuditLog(String eventName, String eventDesc, String operationType, HttpServletRequest request)
    {
        return createAuditLog("配置审计", "配置变更", eventName, eventDesc, operationType, "MEDIUM", request);
    }

    /**
     * 创建数据审计日志对象
     * 
     * @param eventName 事件名称
     * @param eventDesc 事件描述
     * @param operationType 操作类型
     * @param request HTTP请求对象
     * @return 审计日志对象
     */
    public static Object createDataAuditLog(String eventName, String eventDesc, String operationType, HttpServletRequest request)
    {
        return createAuditLog("数据审计", "敏感操作", eventName, eventDesc, operationType, "MEDIUM", request);
    }

    /**
     * 创建操作审计日志对象（高风险）
     * 
     * @param eventName 事件名称
     * @param eventDesc 事件描述
     * @param operationType 操作类型
     * @param request HTTP请求对象
     * @return 审计日志对象
     */
    public static Object createHighRiskAuditLog(String eventName, String eventDesc, String operationType, HttpServletRequest request)
    {
        return createAuditLog("操作审计", "敏感操作", eventName, eventDesc, operationType, "HIGH", request);
    }

    /**
     * 创建审计日志基础对象
     * 
     * @param auditType 审计类型
     * @param auditCategory 审计分类
     * @param eventName 事件名称
     * @param eventDesc 事件描述
     * @param operationType 操作类型
     * @param riskLevel 风险级别
     * @param request HTTP请求对象
     * @return 审计日志对象（返回Object类型，避免循环依赖）
     */
    private static Object createAuditLog(String auditType, String auditCategory, String eventName, 
                                        String eventDesc, String operationType, String riskLevel, 
                                        HttpServletRequest request)
    {
        // 注意：这里返回Object类型是为了避免common模块依赖system模块
        // 实际使用时需要在service层转换为SysAuditLog对象
        return new AuditLogBuilder()
            .auditType(auditType)
            .auditCategory(auditCategory)
            .eventName(eventName)
            .eventDesc(eventDesc)
            .operationType(operationType)
            .riskLevel(riskLevel)
            .ipAddress(IpUtils.getIpAddr())
            .operationTime(new Date())
            .auditStatus("0")
            .isAbnormal("0")
            .request(request)
            .build();
    }

    /**
     * 审计日志构建器（内部类）
     * 用于构建审计日志的基础信息
     */
    public static class AuditLogBuilder
    {
        private String auditType;
        private String auditCategory;
        private String eventName;
        private String eventDesc;
        private String operationType;
        private String operationModule;
        private String riskLevel;
        private String ipAddress;
        private String requestUrl;
        private String requestMethod;
        private String userAgent;
        private String browser;
        private String os;
        private String deviceType;
        private Date operationTime;
        private String auditStatus;
        private String isAbnormal;

        public AuditLogBuilder auditType(String auditType)
        {
            this.auditType = auditType;
            return this;
        }

        public AuditLogBuilder auditCategory(String auditCategory)
        {
            this.auditCategory = auditCategory;
            return this;
        }

        public AuditLogBuilder eventName(String eventName)
        {
            this.eventName = eventName;
            return this;
        }

        public AuditLogBuilder eventDesc(String eventDesc)
        {
            this.eventDesc = eventDesc;
            return this;
        }

        public AuditLogBuilder operationType(String operationType)
        {
            this.operationType = operationType;
            return this;
        }

        public AuditLogBuilder operationModule(String operationModule)
        {
            this.operationModule = operationModule;
            return this;
        }

        public AuditLogBuilder riskLevel(String riskLevel)
        {
            this.riskLevel = riskLevel;
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress)
        {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuditLogBuilder operationTime(Date operationTime)
        {
            this.operationTime = operationTime;
            return this;
        }

        public AuditLogBuilder auditStatus(String auditStatus)
        {
            this.auditStatus = auditStatus;
            return this;
        }

        public AuditLogBuilder isAbnormal(String isAbnormal)
        {
            this.isAbnormal = isAbnormal;
            return this;
        }

        public AuditLogBuilder request(HttpServletRequest request)
        {
            if (request != null)
            {
                this.requestUrl = request.getRequestURI();
                this.requestMethod = request.getMethod();
                this.userAgent = ClientInfoUtils.getUserAgent(request);
                this.browser = ClientInfoUtils.parseBrowser(this.userAgent);
                this.os = ClientInfoUtils.parseOS(this.userAgent);
                this.deviceType = AuditLogUtils.getDeviceType(this.userAgent);
            }
            return this;
        }

        public Object build()
        {
            // 返回包含所有字段的Map，在service层转换为SysAuditLog
            java.util.Map<String, Object> auditData = new java.util.HashMap<>();
            auditData.put("auditType", auditType);
            auditData.put("auditCategory", auditCategory);
            auditData.put("eventName", eventName);
            auditData.put("eventDesc", eventDesc);
            auditData.put("operationType", operationType);
            auditData.put("operationModule", operationModule);
            auditData.put("riskLevel", riskLevel);
            auditData.put("ipAddress", ipAddress);
            auditData.put("requestUrl", requestUrl);
            auditData.put("requestMethod", requestMethod);
            auditData.put("userAgent", userAgent);
            auditData.put("browser", browser);
            auditData.put("os", os);
            auditData.put("deviceType", deviceType);
            auditData.put("operationTime", operationTime);
            auditData.put("auditStatus", auditStatus);
            auditData.put("isAbnormal", isAbnormal);
            return auditData;
        }

        // Getter方法供外部访问
        public String getAuditType() { return auditType; }
        public String getAuditCategory() { return auditCategory; }
        public String getEventName() { return eventName; }
        public String getEventDesc() { return eventDesc; }
        public String getOperationType() { return operationType; }
        public String getOperationModule() { return operationModule; }
        public String getRiskLevel() { return riskLevel; }
        public String getIpAddress() { return ipAddress; }
        public String getRequestUrl() { return requestUrl; }
        public String getRequestMethod() { return requestMethod; }
        public String getUserAgent() { return userAgent; }
        public String getBrowser() { return browser; }
        public String getOs() { return os; }
        public String getDeviceType() { return deviceType; }
        public Date getOperationTime() { return operationTime; }
        public String getAuditStatus() { return auditStatus; }
        public String getIsAbnormal() { return isAbnormal; }
    }
}
