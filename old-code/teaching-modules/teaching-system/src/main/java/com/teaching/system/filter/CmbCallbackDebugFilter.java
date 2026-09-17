package com.teaching.system.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 临时诊断招行回调是否真正进入system服务；不读取原始body，避免影响表单参数解析。
 */
@Slf4j
@Component
public class CmbCallbackDebugFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }
        String lowerUri = uri.toLowerCase(Locale.ROOT);
        return !lowerUri.contains("paymentcallback")
                && !lowerUri.contains("refundcallback")
                && !lowerUri.contains("returncallback");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long began = System.currentTimeMillis();
        log.info("V1_CMB_CALLBACK_FILTER_IN method={} uri={} query={} remoteAddr={} forwardedFor={} contentType={} contentLength={} paramKeys={} safeParams={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr(),
                request.getHeader("X-Forwarded-For"),
                request.getContentType(),
                request.getContentLengthLong(),
                request.getParameterMap().keySet(),
                safeParams(request.getParameterMap()));
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("V1_CMB_CALLBACK_FILTER_OUT method={} uri={} status={} elapsedMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - began);
        }
    }

    private Map<String, String> safeParams(Map<String, String[]> params) {
        return params.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> safeValue(entry.getKey(), entry.getValue())));
    }

    private String safeValue(String key, String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        String lowerKey = key == null ? "" : key.toLowerCase(Locale.ROOT);
        if (lowerKey.contains("sign") || lowerKey.contains("key") || lowerKey.contains("secret")) {
            return "***";
        }
        String joined = Arrays.stream(values).filter(value -> value != null).collect(Collectors.joining(","));
        if ("biz_content".equals(key)) {
            return joined.length() > 256 ? joined.substring(0, 256) + "...(len=" + joined.length() + ")" : joined;
        }
        return joined.length() > 128 ? joined.substring(0, 128) + "...(len=" + joined.length() + ")" : joined;
    }
}
