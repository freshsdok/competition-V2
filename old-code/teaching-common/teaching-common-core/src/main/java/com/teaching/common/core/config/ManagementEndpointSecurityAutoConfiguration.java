package com.teaching.common.core.config;

import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Actuator Web 端点的代码级安全边界。
 *
 * <p>该过滤器独立于 Nacos 的 exposure 配置。即使远端配置仍为
 * {@code management.endpoints.web.exposure.include=*}，HTTP 也只注册 health
 * 端点。</p>
 */
@AutoConfiguration(beforeName = "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration")
@ConditionalOnClass({ EndpointFilter.class, ExposableWebEndpoint.class })
public class ManagementEndpointSecurityAutoConfiguration
{
    @Bean
    public EndpointFilter<ExposableWebEndpoint> managementEndpointAllowListFilter()
    {
        return endpoint -> "health".equals(endpoint.getEndpointId().toString());
    }
}
