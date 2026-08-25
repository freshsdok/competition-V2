package com.teaching.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 为网关的成功和错误响应统一设置浏览器安全响应头。
 */
@Component
public class SecurityResponseHeadersFilter implements WebFilter, Ordered
{
    static final String CONTENT_SECURITY_POLICY = "frame-ancestors 'self'";
    static final String REFERRER_POLICY = "strict-origin-when-cross-origin";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain)
    {
        applySecurityHeaders(exchange.getResponse());
        exchange.getResponse().beforeCommit(() -> {
            applySecurityHeaders(exchange.getResponse());
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    static void applySecurityHeaders(ServerHttpResponse response)
    {
        HttpHeaders headers = response.getHeaders();
        headers.set("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        headers.set("X-Frame-Options", "SAMEORIGIN");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", REFERRER_POLICY);
    }

    @Override
    public int getOrder()
    {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
