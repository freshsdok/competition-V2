package com.teaching.file.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.teaching.file.config.OSSConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
@Configuration
@RequiredArgsConstructor
public class OSSClientFactory {
    private final OSSConfig ossConfig;
    private final Map<String, OSS> clientCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化默认客户端
        clientCache.put("default", createOSSClient(ossConfig.getEndpoint()));
    }

    public OSS getOSSClient(String clientName) {
        return clientCache.computeIfAbsent(clientName, k -> createOSSClient(ossConfig.getEndpoint()));
    }

    /**
     * 获取用于生成预签名URL的客户端（优先使用自定义域名）
     */
    public OSS getPreviewOSSClient() {
        String domain = ossConfig.getDomain();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(domain)) {
            return clientCache.computeIfAbsent("preview", k -> createOSSClient(domain));
        }
        return getOSSClient("default");
    }

    private OSS createOSSClient(String endpoint) {
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setMaxConnections(ossConfig.getMaxConnections());
        conf.setConnectionTimeout(5000);
        conf.setSocketTimeout(5000);

        return new OSSClientBuilder()
                .build(endpoint,
                        ossConfig.getAccessKeyId(),
                        ossConfig.getAccessKeySecret(),
                        conf);
    }

    @PreDestroy
    public void shutdownAll() {
        clientCache.values().forEach(OSS::shutdown);
        clientCache.clear();
    }
}
