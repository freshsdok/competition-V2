package com.teaching.common.swagger.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 为接口文档提供安全默认值。
 *
 * <p>这些属性使用最低优先级，开发、测试环境仍可通过显式配置开启接口文档。
 * 对外访问还需要显式开启专用的文档暴露开关。</p>
 */
public class SecurityDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered
{
    static final String PROPERTY_SOURCE_NAME = "teachingSecurityDefaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application)
    {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME))
        {
            return;
        }

        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("springdoc.api-docs.enabled", "false");
        defaults.put("springdoc.swagger-ui.enabled", "false");

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
    }

    @Override
    public int getOrder()
    {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
