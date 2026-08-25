package com.teaching.modules.monitor.config;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebOperation;

public class WebSecurityConfigurerTest
{
    @Test
    public void shouldExposeOnlyHealthFromMonitorActuator()
    {
        EndpointFilter<ExposableWebEndpoint> filter =
            WebSecurityConfigurer.monitorManagementEndpointAllowListFilter();

        Assert.assertTrue(filter.match(endpoint("health")));
        Assert.assertFalse(filter.match(endpoint("env")));
        Assert.assertFalse(filter.match(endpoint("heapdump")));
        Assert.assertFalse(filter.match(endpoint("metrics")));
    }

    private ExposableWebEndpoint endpoint(String id)
    {
        return new ExposableWebEndpoint()
        {
            @Override
            public EndpointId getEndpointId()
            {
                return EndpointId.of(id);
            }

            @Override
            public boolean isEnableByDefault()
            {
                return true;
            }

            @Override
            public java.util.Collection<WebOperation> getOperations()
            {
                return Collections.emptyList();
            }

            @Override
            public String getRootPath()
            {
                return id;
            }
        };
    }
}
