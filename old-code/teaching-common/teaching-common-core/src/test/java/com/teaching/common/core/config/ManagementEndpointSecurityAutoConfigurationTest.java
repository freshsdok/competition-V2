package com.teaching.common.core.config;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.core.env.StandardEnvironment;

public class ManagementEndpointSecurityAutoConfigurationTest
{
    @Test
    public void shouldExposeOnlyHealthEvenWhenRemoteExposureIsBroad()
    {
        EndpointFilter<ExposableWebEndpoint> filter =
            new ManagementEndpointSecurityAutoConfiguration().managementEndpointAllowListFilter();

        Assert.assertFalse(filter.match(endpoint("env")));
        Assert.assertFalse(filter.match(endpoint("configprops")));
        Assert.assertFalse(filter.match(endpoint("heapdump")));
        Assert.assertFalse(filter.match(endpoint("metrics")));
        Assert.assertFalse(filter.match(endpoint("prometheus")));
        Assert.assertFalse(filter.match(endpoint("scheduledtasks")));
        Assert.assertTrue(filter.match(endpoint("health")));
    }

    @Test
    public void shouldProvideMinimalManagementDefaults()
    {
        StandardEnvironment environment = new StandardEnvironment();

        new ManagementSecurityDefaultsEnvironmentPostProcessor().postProcessEnvironment(
            environment,
            new SpringApplication()
        );

        Assert.assertEquals("false", environment.getProperty("management.endpoints.enabled-by-default"));
        Assert.assertEquals("health", environment.getProperty("management.endpoints.web.exposure.include"));
        Assert.assertEquals("never", environment.getProperty("management.endpoint.health.show-details"));
        Assert.assertEquals("false", environment.getProperty("management.endpoint.heapdump.enabled"));
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
