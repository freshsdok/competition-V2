package com.teaching.common.swagger.config;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class SecurityDefaultsEnvironmentPostProcessorTest
{
    @Test
    public void shouldDisableDocsAndExposeOnlyHealthByDefault()
    {
        StandardEnvironment environment = new StandardEnvironment();

        new SecurityDefaultsEnvironmentPostProcessor().postProcessEnvironment(
            environment,
            new SpringApplication()
        );

        Assert.assertEquals("false", environment.getProperty("springdoc.api-docs.enabled"));
        Assert.assertEquals("false", environment.getProperty("springdoc.swagger-ui.enabled"));
    }

    @Test
    public void shouldAllowAnExplicitDevelopmentOverride()
    {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(
            "developmentOverrides",
            Collections.singletonMap("springdoc.api-docs.enabled", "true")
        ));

        new SecurityDefaultsEnvironmentPostProcessor().postProcessEnvironment(
            environment,
            new SpringApplication()
        );

        Assert.assertEquals("true", environment.getProperty("springdoc.api-docs.enabled"));
    }
}
