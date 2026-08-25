package com.teaching.modules.monitor.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * 监控权限配置
 *
 * @author teaching
 */
@EnableWebSecurity
@Configuration
public class WebSecurityConfigurer
{
    private final String adminContextPath;

    public WebSecurityConfigurer(AdminServerProperties adminServerProperties)
    {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
    {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");

        return httpSecurity.headers((header) -> header
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .contentSecurityPolicy(
                    (policy) -> policy.policyDirectives("frame-ancestors 'self'"))
                .contentTypeOptions(Customizer.withDefaults())
                .referrerPolicy((policy) -> policy.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
            .authorizeHttpRequests(
                (authorize) -> authorize
                    .requestMatchers(adminContextPath + "/assets/**",
                        adminContextPath + "/login",
                        adminContextPath + "/actuator/health",
                        adminContextPath + "/instances/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
            .formLogin((formLogin) -> formLogin.loginPage(adminContextPath + "/login").successHandler(successHandler))
            .logout((logout) -> logout.logoutUrl(adminContextPath + "/logout"))
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    public static EndpointFilter<ExposableWebEndpoint> monitorManagementEndpointAllowListFilter()
    {
        return endpoint -> "health".equals(endpoint.getEndpointId().toString());
    }
}
