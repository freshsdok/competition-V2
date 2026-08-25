
package com.teaching.common.log.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
 *
 * @author teaching
 */
@Configuration
@EnableFeignClients(basePackages = "com.teaching.system.api")
public class FeignConfig
{
}
