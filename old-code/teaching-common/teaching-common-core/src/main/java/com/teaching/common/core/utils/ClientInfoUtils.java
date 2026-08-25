package com.teaching.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import com.teaching.common.core.utils.ip.IpUtils;

/**
 * 客户端信息解析工具：IP、浏览器、操作系统
 */
public class ClientInfoUtils
{
    /**
     * 获取客户端 IP（基于多级反向代理头）
     */
    public static String getClientIp()
    {
        return IpUtils.getIpAddr();
    }

    /**
     * 从请求中获取 User-Agent 字符串
     */
    public static String getUserAgent(HttpServletRequest request)
    {
        String ua = request != null ? request.getHeader("User-Agent") : null;
        return StringUtils.isEmpty(ua) ? "" : StringUtils.substring(ua, 0, 500);
    }

    /**
     * 解析浏览器
     */
    public static String parseBrowser(String userAgent)
    {
        if (StringUtils.isEmpty(userAgent))
        {
            return "";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/") && !ua.contains("chrome/")) return "Safari";
        if (ua.contains("msie") || ua.contains("trident")) return "IE";
        return "Other";
    }

    /**
     * 解析操作系统
     */
    public static String parseOS(String userAgent)
    {
        if (StringUtils.isEmpty(userAgent))
        {
            return "";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 11")) return "Windows 11";
        if (ua.contains("windows nt 10")) return "Windows 10";
        if (ua.contains("windows nt")) return "Windows";
        if (ua.contains("mac os x")) return "macOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("linux")) return "Linux";
        return "Other";
    }

    /**
     * 直接从当前请求解析浏览器
     */
    public static String getBrowserFromRequest()
    {
        String ua = getUserAgent(ServletUtils.getRequest());
        return parseBrowser(ua);
    }

    /**
     * 直接从当前请求解析操作系统
     */
    public static String getOSFromRequest()
    {
        String ua = getUserAgent(ServletUtils.getRequest());
        return parseOS(ua);
    }

    /**
     * 聚合获取客户端信息（IP、浏览器、操作系统）
     */
    public static ClientInfo getClientInfo()
    {
        String ip = getClientIp();
        String ua = getUserAgent(ServletUtils.getRequest());
        String browser = parseBrowser(ua);
        String os = parseOS(ua);
        return new ClientInfo(ip, browser, os);
    }

    /**
     * 客户端信息载体
     */
    public static class ClientInfo
    {
        private final String ip;
        private final String browser;
        private final String os;

        public ClientInfo(String ip, String browser, String os)
        {
            this.ip = ip;
            this.browser = browser;
            this.os = os;
        }

        public String getIp()
        {
            return ip;
        }

        public String getBrowser()
        {
            return browser;
        }

        public String getOs()
        {
            return os;
        }
    }
}


