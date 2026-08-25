package com.teaching.common.swagger.config;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SpringDocExposureFilterTest
{
    private final SpringDocExposureFilter filter = new SpringDocExposureFilter();

    @Test
    public void shouldRejectDocumentationPathsBeforeServletRouting() throws Exception
    {
        assertRejected("/v3/api-docs");
        assertRejected("/v3/api-docs.yaml");
        assertRejected("/swagger-ui.html");
        assertRejected("/swagger-ui/index.html");
        assertRejected("/teaching-system/v3/api-docs");
        assertRejected("/prod-api/teaching-content/v3/api-docs.yaml");
        assertRejected("/%2576%2533/api-docs");
        assertRejected("/v3/api-docs.");
    }

    @Test
    public void shouldAllowOrdinaryBusinessPaths() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/user/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean routed = new AtomicBoolean(false);

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> routed.set(true));

        Assert.assertTrue(routed.get());
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldAllowDocsOnlyWithDedicatedExposureSwitch() throws Exception
    {
        SpringDocExposureFilter explicitlyEnabledFilter = new SpringDocExposureFilter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean routed = new AtomicBoolean(false);

        explicitlyEnabledFilter.doFilter(
            request,
            response,
            (ignoredRequest, ignoredResponse) -> routed.set(true)
        );

        Assert.assertTrue(routed.get());
    }

    private void assertRejected(String path) throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean routed = new AtomicBoolean(false);

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> routed.set(true));

        Assert.assertFalse("文档路径不应进入后续 Servlet 路由: " + path, routed.get());
        Assert.assertEquals(404, response.getStatus());
    }
}
