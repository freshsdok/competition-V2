package com.teaching.common.core.config;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ManagementEndpointExposureFilterTest
{
    private final ManagementEndpointExposureFilter filter = new ManagementEndpointExposureFilter();

    @Test
    public void shouldRejectEveryNonHealthActuatorPath() throws Exception
    {
        assertRejected("/actuator/env");
        assertRejected("/actuator/configprops");
        assertRejected("/actuator/heapdump");
        assertRejected("/actuator/metrics");
        assertRejected("/teaching-auth/actuator/loggers");
        assertRejected("/prod-api/teaching-system/actuator/mappings");
        assertRejected("/%2561ctuator/heapdump");
        assertRejected("/actuator./env");
    }

    @Test
    public void shouldAllowOnlyDiscoveryHealthAndBusinessPaths() throws Exception
    {
        assertAllowed("/actuator");
        assertAllowed("/actuator/");
        assertAllowed("/actuator/health");
        assertAllowed("/actuator/health/liveness");
        assertAllowed("/system/user/1");
    }

    private void assertRejected(String path) throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean routed = new AtomicBoolean(false);

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> routed.set(true));

        Assert.assertFalse("非 health 管理路径不应进入后续路由: " + path, routed.get());
        Assert.assertEquals(404, response.getStatus());
    }

    private void assertAllowed(String path) throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean routed = new AtomicBoolean(false);

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> routed.set(true));

        Assert.assertTrue("允许的路径应进入后续路由: " + path, routed.get());
    }
}
