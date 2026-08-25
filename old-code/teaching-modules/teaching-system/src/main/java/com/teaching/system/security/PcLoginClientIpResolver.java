package com.teaching.system.security;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves a client IP without trusting forwarding headers from arbitrary peers.
 *
 * <p>The immediate peer must match {@code security.pc-login.trusted-proxies}
 * before X-Forwarded-For or X-Real-IP is considered. Configure trusted reverse
 * proxy addresses with the same semicolon-separated exact/wildcard/range syntax
 * supported by {@link IpUtils#isMatchedIp(String, String)}.</p>
 */
@Component
public class PcLoginClientIpResolver {

    @Value("${security.pc-login.trusted-proxies:127.0.0.1;::1;teaching-gateway;teaching-nginx}")
    private String trustedProxies;

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String remoteAddress = normalize(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwardedFor)) {
            String[] chain = forwardedFor.split(",");
            for (int i = chain.length - 1; i >= 0; i--) {
                String candidate = normalize(chain[i]);
                if (isValidAddress(candidate) && !isTrustedProxy(candidate)) {
                    return candidate;
                }
            }
        }

        String realIp = normalize(request.getHeader("X-Real-IP"));
        return isValidAddress(realIp) ? realIp : remoteAddress;
    }

    private boolean isTrustedProxy(String address) {
        if (StringUtils.isBlank(address) || StringUtils.isBlank(trustedProxies)) {
            return false;
        }
        for (String configured : trustedProxies.split(";")) {
            String candidate = configured.trim();
            if (address.equalsIgnoreCase(candidate) || resolvesTo(candidate, address)) {
                return true;
            }
        }
        return IpUtils.isMatchedIp(trustedProxies, address);
    }

    private static boolean resolvesTo(String configuredHost, String address) {
        if (StringUtils.isBlank(configuredHost)
                || IpUtils.isIP(configuredHost)
                || configuredHost.contains("*")
                || configuredHost.contains("-")
                || configuredHost.contains(":")) {
            return false;
        }
        try {
            for (InetAddress resolved : InetAddress.getAllByName(configuredHost)) {
                if (address.equalsIgnoreCase(normalize(resolved.getHostAddress()))) {
                    return true;
                }
            }
        } catch (UnknownHostException ignored) {
            // 未部署的可选代理主机不应影响本地启动。
        }
        return false;
    }

    private static boolean isValidAddress(String address) {
        if (IpUtils.isIP(address)) {
            return true;
        }
        if (StringUtils.isBlank(address)
                || address.indexOf(':') < 0
                || !address.matches("[0-9a-fA-F:]{2,45}")) {
            return false;
        }
        try {
            // The character allowlist above prevents hostname/DNS resolution.
            return InetAddress.getByName(address).getAddress().length == 16;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private static String normalize(String address) {
        if (StringUtils.isBlank(address)) {
            return "unknown";
        }
        String value = address.trim();
        return "0:0:0:0:0:0:0:1".equals(value) ? "127.0.0.1"
                : StringUtils.substring(value, 0, 255);
    }
}
