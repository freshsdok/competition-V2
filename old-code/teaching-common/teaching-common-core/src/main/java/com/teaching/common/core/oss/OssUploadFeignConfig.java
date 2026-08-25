package com.teaching.common.core.oss;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssUploadFeignConfig {

    @Bean
    public Request.Options options() {
        // 大文件上传需要更长超时时间
        return new Request.Options(6000000, 60000000); // 连接6000秒，读取60000秒
    }

    @Bean
    public Retryer retryer() {
        // 设置重试机制
        return new Retryer.Default(2000, 8000, 2);
    }

    // 如果需要熔断，可以配置FallbackFactory
//    @Bean
//    public RemoteOssUploadFallbackFactory ossUploadFallbackFactory() {
//        return new RemoteOssUploadFallbackFactory();
//    }
}
