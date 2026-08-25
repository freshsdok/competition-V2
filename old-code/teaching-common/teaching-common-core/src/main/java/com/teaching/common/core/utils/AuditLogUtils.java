package com.teaching.common.core.utils;

import java.util.Calendar;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 审计日志工具类
 * 
 * @author teaching
 */
public class AuditLogUtils
{
    /**
     * 构建基础审计日志信息
     * 
     * @param request HTTP请求
     * @param userName 用户名
     * @return 审计日志对象（需要设置具体的审计类型、事件等信息）
     */
    public static Object buildBaseAuditLog(HttpServletRequest request, String userName)
    {
        // 这里返回Object是为了避免循环依赖
        // 实际使用时需要在具体模块中转换为SysAuditLog
        return null;
    }

    /**
     * 检测是否为异地登录
     * 
     * @param currentIp 当前登录IP
     * @param lastIp 上次登录IP
     * @return 是否异地登录
     */
    public static boolean isRemoteLogin(String currentIp, String lastIp)
    {
        if (StringUtils.isEmpty(lastIp) || StringUtils.isEmpty(currentIp))
        {
            return false;
        }
        
        // 简单判断：IP前两段不同则认为是异地登录
        // 实际项目中可以使用IP地址库进行更精确的地理位置判断
        String[] currentParts = currentIp.split("\\.");
        String[] lastParts = lastIp.split("\\.");
        
        if (currentParts.length >= 2 && lastParts.length >= 2)
        {
            return !currentParts[0].equals(lastParts[0]) || !currentParts[1].equals(lastParts[1]);
        }
        
        return false;
    }

    /**
     * 检测是否为非工作时间登录
     * 
     * @return 是否非工作时间
     */
    public static boolean isNonWorkingHours()
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // 工作时间：8:00 - 18:00
        return hour < 8 || hour >= 18;
    }

    /**
     * 计算风险级别
     * 
     * @param isAbnormal 是否异常
     * @param operationType 操作类型
     * @return 风险级别
     */
    public static String calculateRiskLevel(boolean isAbnormal, String operationType)
    {
        if (isAbnormal)
        {
            return "HIGH";
        }
        
        // 根据操作类型判断风险级别
        if (StringUtils.equalsAny(operationType, "删除", "授权", "配置修改"))
        {
            return "HIGH";
        }
        else if (StringUtils.equalsAny(operationType, "修改", "导出"))
        {
            return "MEDIUM";
        }
        else
        {
            return "LOW";
        }
    }

    /**
     * 获取设备类型
     * 
     * @param userAgent User-Agent字符串
     * @return 设备类型
     */
    public static String getDeviceType(String userAgent)
    {
        if (StringUtils.isEmpty(userAgent))
        {
            return "Unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone"))
        {
            return "Mobile";
        }
        else if (userAgent.contains("tablet") || userAgent.contains("ipad"))
        {
            return "Tablet";
        }
        else
        {
            return "PC";
        }
    }
}
