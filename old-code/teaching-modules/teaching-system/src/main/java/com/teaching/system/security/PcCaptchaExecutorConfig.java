package com.teaching.system.security;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Dedicated bounded executor for verification-code delivery. Delivery is kept
 * outside the HTTP request so account existence cannot be inferred from
 * mail/SMS provider latency.
 */
@Configuration
public class PcCaptchaExecutorConfig {

    @Bean(name = "pcCaptchaTaskExecutor")
    public ThreadPoolTaskExecutor pcCaptchaTaskExecutor(
            @Value("${security.pc-captcha.executor.core-size:2}") int coreSize,
            @Value("${security.pc-captcha.executor.max-size:4}") int maxSize,
            @Value("${security.pc-captcha.executor.queue-capacity:200}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int safeCoreSize = Math.max(1, coreSize);
        executor.setCorePoolSize(safeCoreSize);
        executor.setMaxPoolSize(Math.max(safeCoreSize, maxSize));
        executor.setQueueCapacity(Math.max(1, queueCapacity));
        executor.setThreadNamePrefix("pc-captcha-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}
