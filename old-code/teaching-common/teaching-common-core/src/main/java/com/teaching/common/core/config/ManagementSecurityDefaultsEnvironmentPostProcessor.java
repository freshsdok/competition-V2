package com.teaching.common.core.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 为所有依赖 common-core 的服务提供最小化 Actuator 默认配置。
 */
public class ManagementSecurityDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered
{
    static final String PROPERTY_SOURCE_NAME = "teachingManagementSecurityDefaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application)
    {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME))
        {
            return;
        }

        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("management.endpoints.enabled-by-default", "false");
        defaults.put("management.endpoints.web.exposure.include", "health");
        defaults.put("management.endpoint.health.enabled", "true");
        defaults.put("management.endpoint.health.show-details", "never");
        defaults.put("management.endpoint.env.enabled", "false");
        defaults.put("management.endpoint.configprops.enabled", "false");
        defaults.put("management.endpoint.heapdump.enabled", "false");
        defaults.put("management.endpoint.threaddump.enabled", "false");

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
    }

    @Override
    public int getOrder()
    {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
