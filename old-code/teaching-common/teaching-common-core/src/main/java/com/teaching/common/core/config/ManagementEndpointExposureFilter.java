package com.teaching.common.core.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Actuator 路径过滤器，仅允许根发现页和 health。
 */
public class ManagementEndpointExposureFilter extends OncePerRequestFilter
{
    private static final Pattern MULTIPLE_SLASHES = Pattern.compile("/{2,}");
    private static final Pattern MATRIX_PARAMETERS = Pattern.compile(";[^/]*");
    private static final Pattern TRAILING_SEGMENT_DOTS = Pattern.compile("\\.+(?=/|$)");
    private static final Pattern MANAGEMENT_PATH = Pattern.compile(
        "^/(?:prod-api/)?(?:[^/]+/)?actuator(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ALLOWED_MANAGEMENT_PATH = Pattern.compile(
        "^/(?:prod-api/)?(?:[^/]+/)?actuator(?:/?|/health(?:/.*)?)$",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException
    {
        String path = normalizePath(requestPath(request));
        if (MANAGEMENT_PATH.matcher(path).matches()
            && !ALLOWED_MANAGEMENT_PATH.matcher(path).matches())
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        filterChain.doFilter(request, response);
    }

    static String normalizePath(String rawPath)
    {
        if (!StringUtils.hasText(rawPath))
        {
            return "";
        }

        String normalizedPath = decodePath(rawPath).replace('\\', '/');
        normalizedPath = MULTIPLE_SLASHES.matcher(normalizedPath).replaceAll("/");
        normalizedPath = MATRIX_PARAMETERS.matcher(normalizedPath).replaceAll("");
        normalizedPath = StringUtils.cleanPath(normalizedPath);
        normalizedPath = TRAILING_SEGMENT_DOTS.matcher(normalizedPath).replaceAll("");
        return MULTIPLE_SLASHES.matcher(normalizedPath).replaceAll("/");
    }

    private String requestPath(HttpServletRequest request)
    {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath))
        {
            return path.substring(contextPath.length());
        }
        return path;
    }

    private static String decodePath(String rawPath)
    {
        String decodedPath = rawPath;
        for (int i = 0; i < 3; i++)
        {
            try
            {
                String next = UriUtils.decode(decodedPath, StandardCharsets.UTF_8);
                if (next.equals(decodedPath))
                {
                    break;
                }
                decodedPath = next;
            }
            catch (IllegalArgumentException ex)
            {
                break;
            }
        }
        return decodedPath;
    }
}
