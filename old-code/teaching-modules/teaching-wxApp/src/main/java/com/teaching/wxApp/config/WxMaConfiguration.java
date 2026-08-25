package com.teaching.wxApp.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信小程序配置
 * 支持Nacos配置中心动态刷新
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WxMaProperties.class)
public class WxMaConfiguration {

    @Autowired
    private WxMaProperties properties;

    /**
     * 微信小程序服务
     * 使用 @RefreshScope 支持Nacos配置热更新
     */
    @Bean
    @RefreshScope
    public WxMaService wxMaService() {
        List<WxMaProperties.Config> configs = this.properties.getConfigs();
        if (configs == null || configs.isEmpty()) {
            log.error("微信小程序配置未找到！请检查Nacos配置中心 wxApp.configs 配置");
            throw new WxRuntimeException("微信小程序配置未找到！请检查Nacos配置中心 wxApp.configs 配置");
        }

        WxMaService maService = new WxMaServiceImpl();
        maService.setMultiConfigs(
                configs.stream()
                        .map(this::buildWxMaConfig)
                        .collect(Collectors.toMap(
                                WxMaDefaultConfigImpl::getAppid,
                                config -> config,
                                (o, n) -> o
                        ))
        );

        log.info("微信小程序服务初始化成功，共配置 {} 个小程序", configs.size());
        return maService;
    }

    /**
     * 构建微信小程配置
     */
    private WxMaDefaultConfigImpl buildWxMaConfig(WxMaProperties.Config config) {
        if (config.getAppid() == null || config.getAppid().trim().isEmpty()) {
            throw new WxRuntimeException("微信小程序appid不能为空");
        }
        if (config.getSecret() == null || config.getSecret().trim().isEmpty()) {
            throw new WxRuntimeException("微信小程序secret不能为空，appid: " + config.getAppid());
        }

        WxMaDefaultConfigImpl maConfig = new WxMaDefaultConfigImpl();
        maConfig.setAppid(config.getAppid().trim());
        maConfig.setSecret(config.getSecret().trim());
        if (config.getMsgDataFormat() != null) {
            maConfig.setMsgDataFormat(config.getMsgDataFormat().trim());
        }

        log.debug("加载微信小程序配置: appid={}", config.getAppid());
        return maConfig;
    }
}
