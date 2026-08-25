package com.teaching.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oss")
public class OSSConfig {
    /**
     * OSS访问域名
     */
    private String endpoint;
    /**
     * OSS访问密钥ID
     */
    private String accessKeyId;
    /**
     * OSS访问密钥Secret
     */
    private String accessKeySecret;
    /**
     * OSS存储空间名（BucketName）
     */
    private String bucketName;

    /**
     * 连接池最大连接数
     */
    private int maxConnections = 20;

    /**
     * 自定义域名
     */
    private String domain;

}
