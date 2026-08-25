package com.teaching.gateway.filter;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

public class SecurityResponseHeadersFilterTest
{
    private final SecurityResponseHeadersFilter filter = new SecurityResponseHeadersFilter();

    @Test
    public void shouldSetOneCanonicalValueForEverySecurityHeader()
    {
        MockServerWebExchange exchange = exchange();

        filter.filter(exchange, current -> {
            current.getResponse().getHeaders().add("X-Frame-Options", "DENY");
            current.getResponse().setStatusCode(HttpStatus.OK);
            return current.getResponse().setComplete();
        }).block();

        assertSecurityHeaders(exchange);
        Assert.assertEquals(
            List.of("SAMEORIGIN"),
            exchange.getResponse().getHeaders().get("X-Frame-Options")
        );
    }

    @Test
    public void shouldKeepSecurityHeadersOnErrorResponses()
    {
        MockServerWebExchange exchange = exchange();

        filter.filter(exchange, ignored -> Mono.error(new IllegalStateException("test")))
            .onErrorResume(ignored -> {
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            })
            .block();

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertSecurityHeaders(exchange);
    }

    private MockServerWebExchange exchange()
    {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/example").build());
    }

    private void assertSecurityHeaders(MockServerWebExchange exchange)
    {
        Assert.assertEquals(
            SecurityResponseHeadersFilter.CONTENT_SECURITY_POLICY,
            exchange.getResponse().getHeaders().getFirst("Content-Security-Policy")
        );
        Assert.assertEquals(
            "SAMEORIGIN",
            exchange.getResponse().getHeaders().getFirst("X-Frame-Options")
        );
        Assert.assertEquals(
            "nosniff",
            exchange.getResponse().getHeaders().getFirst("X-Content-Type-Options")
        );
        Assert.assertEquals(
            SecurityResponseHeadersFilter.REFERRER_POLICY,
            exchange.getResponse().getHeaders().getFirst("Referrer-Policy")
        );
    }
}
