package com.teaching.common.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 在 Servlet 路由和全局异常处理器之前拒绝非 health 的 Actuator 路径。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OncePerRequestFilter.class)
public class ManagementEndpointExposureSecurityAutoConfiguration
{
    @Bean
    public FilterRegistrationBean<ManagementEndpointExposureFilter> managementEndpointExposureFilter()
    {
        FilterRegistrationBean<ManagementEndpointExposureFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ManagementEndpointExposureFilter());
        registration.setName("managementEndpointExposureFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
