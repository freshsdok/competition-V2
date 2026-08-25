package com.teaching.common.swagger.config;

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
 * 默认拒绝接口文档和 Swagger UI 路径。
 */
public class SpringDocExposureFilter extends OncePerRequestFilter
{
    private static final Pattern MULTIPLE_SLASHES = Pattern.compile("/{2,}");
    private static final Pattern MATRIX_PARAMETERS = Pattern.compile(";[^/]*");
    private static final Pattern TRAILING_SEGMENT_DOTS = Pattern.compile("\\.+(?=/|$)");
    private static final Pattern DOCUMENT_PATH = Pattern.compile(
        "^/(?:prod-api/)?(?:[^/]+/)?(?:"
            + "v3/api-docs(?:\\.yaml)?(?:/.*)?"
            + "|swagger-ui(?:\\.html|/.*)?"
            + ")$",
        Pattern.CASE_INSENSITIVE
    );

    private final boolean exposureEnabled;

    public SpringDocExposureFilter()
    {
        this(false);
    }

    SpringDocExposureFilter(boolean exposureEnabled)
    {
        this.exposureEnabled = exposureEnabled;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException
    {
        if (!exposureEnabled && isDocumentPath(requestPath(request)))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        filterChain.doFilter(request, response);
    }

    static boolean isDocumentPath(String rawPath)
    {
        if (!StringUtils.hasText(rawPath))
        {
            return false;
        }

        String normalizedPath = decodePath(rawPath).replace('\\', '/');
        normalizedPath = MULTIPLE_SLASHES.matcher(normalizedPath).replaceAll("/");
        normalizedPath = MATRIX_PARAMETERS.matcher(normalizedPath).replaceAll("");
        normalizedPath = StringUtils.cleanPath(normalizedPath);
        normalizedPath = TRAILING_SEGMENT_DOTS.matcher(normalizedPath).replaceAll("");
        normalizedPath = MULTIPLE_SLASHES.matcher(normalizedPath).replaceAll("/");
        return DOCUMENT_PATH.matcher(normalizedPath).matches();
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
