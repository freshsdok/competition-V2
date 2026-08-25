package com.teaching.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

/**
 * 在鉴权白名单和动态路由之前拒绝管理端点及接口文档路径。
 */
@Component
public class SensitivePathFilter implements WebFilter, Ordered
{
    private static final Pattern MULTIPLE_SLASHES = Pattern.compile("/{2,}");
    private static final Pattern MATRIX_PARAMETERS = Pattern.compile(";[^/]*");
    private static final Pattern TRAILING_SEGMENT_DOTS = Pattern.compile("\\.+(?=/|$)");
    private static final Pattern SENSITIVE_PATH = Pattern.compile(
        "^/(?:prod-api/)?(?:[^/]+/)?(?:"
            + "actuator(?:/.*)?"
            + "|v3/api-docs(?:\\.yaml)?(?:/.*)?"
            + "|swagger-ui(?:\\.html|/.*)?"
            + ")$",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain)
    {
        if (isSensitivePath(exchange.getRequest().getURI().getRawPath()))
        {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    static boolean isSensitivePath(String rawPath)
    {
        if (rawPath == null || rawPath.isEmpty())
        {
            return false;
        }

        String decodedPath = decodePath(rawPath);

        String normalizedPath = decodedPath.replace('\\', '/');
        normalizedPath = MULTIPLE_SLASHES.matcher(normalizedPath).replaceAll("/");
        normalizedPath = MATRIX_PARAMETERS.matcher(normalizedPath).replaceAll("");
        normalizedPath = StringUtils.cleanPath(normalizedPath);
        normalizedPath = TRAILING_SEGMENT_DOTS.matcher(normalizedPath).replaceAll("");
        normalizedPath = MULTIPLE_SLASHES.matcher(normalizedPath).replaceAll("/");
        return SENSITIVE_PATH.matcher(normalizedPath).matches();
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

    @Override
    public int getOrder()
    {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
