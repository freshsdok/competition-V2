package com.teaching.common.core.sms;

import com.aliyun.dysmsapi20170525.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@RefreshScope
public class SmsConfigInfo {
    @Value("${sms.ali.access-key-id}")
    private String accessKeyId;
    @Value("${sms.ali.access-key-secret}")
    private String accessKeySecret;
    @Value("${sms.ali.endpoint}")
    private String endpoint;
    @Value("${sms.ali.sign-name}")
    private String signName;
    @Value("${sms.ali.template-code}")
    private String templateCode;

    @Bean
    public Client smsClient() throws Exception {
        // 初始化Client
        com.aliyun.teaopenapi.models.Config config =
                new com.aliyun.teaopenapi.models.Config()
                        .setAccessKeyId(accessKeyId)
                        .setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;

        return new Client(config);
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }
}
