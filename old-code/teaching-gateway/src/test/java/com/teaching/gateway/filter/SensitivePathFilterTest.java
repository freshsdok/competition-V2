package com.teaching.gateway.filter;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

public class SensitivePathFilterTest
{
    private final SensitivePathFilter filter = new SensitivePathFilter();

    @Test
    public void shouldRejectActuatorAndApiDocPathsBeforeRouting()
    {
        assertRejected("/actuator/env");
        assertRejected("/teaching-system/actuator/heapdump");
        assertRejected("/v3/api-docs");
        assertRejected("/teaching-content/v3/api-docs/swagger-config");
        assertRejected("/v3/api-docs.yaml");
        assertRejected("/teaching-content/v3/api-docs.yaml");
        assertRejected("/prod-api/teaching-system/actuator/configprops");
        assertRejected("/prod-api/teaching-content/v3/api-docs");
        assertRejected("/prod-api/teaching-content/v3/api-docs.yaml");
        assertRejected("/swagger-ui.html");
        assertRejected("/teaching-content/swagger-ui.html");
        assertRejected("/%61ctuator/heapdump");
        assertRejected("/%2561ctuator/heapdump");
        assertRejected("/actuator./heapdump");
        assertRejected("/service/../actuator/env");
    }

    @Test
    public void shouldNotBlockOrdinaryBusinessPaths()
    {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/teaching-system/system/user/1").build()
        );
        AtomicBoolean routed = new AtomicBoolean(false);

        filter.filter(exchange, ignored -> {
            routed.set(true);
            ignored.getResponse().setStatusCode(HttpStatus.OK);
            return ignored.getResponse().setComplete();
        }).block();

        Assert.assertTrue(routed.get());
        Assert.assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

    private void assertRejected(String path)
    {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get(path).build()
        );
        AtomicBoolean routed = new AtomicBoolean(false);

        filter.filter(exchange, ignored -> {
            routed.set(true);
            return Mono.empty();
        }).block();

        Assert.assertFalse("敏感路径不应进入后续路由: " + path, routed.get());
        Assert.assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }
}
