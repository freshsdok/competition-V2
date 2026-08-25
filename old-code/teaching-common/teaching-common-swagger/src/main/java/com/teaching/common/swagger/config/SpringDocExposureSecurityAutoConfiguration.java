package com.teaching.common.swagger.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 接口文档的 Servlet 访问边界。
 *
 * <p>旧配置中的 {@code springdoc.*.enabled=true} 只能生成文档，不能对外访问。
 * 开发或测试环境确需访问时，还必须显式设置
 * {@code teaching.security.api-docs.exposure-enabled=true}。</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OncePerRequestFilter.class)
public class SpringDocExposureSecurityAutoConfiguration
{
    @Bean
    public FilterRegistrationBean<SpringDocExposureFilter> springDocExposureFilter(
        @Value("${teaching.security.api-docs.exposure-enabled:false}") boolean exposureEnabled
    )
    {
        FilterRegistrationBean<SpringDocExposureFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SpringDocExposureFilter(exposureEnabled));
        registration.setName("springDocExposureFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
