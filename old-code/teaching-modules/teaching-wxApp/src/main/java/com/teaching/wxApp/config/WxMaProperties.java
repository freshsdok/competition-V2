package com.teaching.wxApp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 微信小程序配置属性类
 * 从Nacos配置中心读取 wx.configs 配置
 * 支持配置热更新（@RefreshScope）
 */
@RefreshScope
@ConfigurationProperties(prefix = "wx")
public class WxMaProperties {

    private List<Config> configs;

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }

    public static class Config {
        /**
         * 设置微信小程序的appid
         */
        private String appid;

        /**
         * 设置微信小程序的Secret
         */
        private String secret;

        /**
         * 消息格式，XML或者JSON
         */
        private String msgDataFormat;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getMsgDataFormat() {
            return msgDataFormat;
        }

        public void setMsgDataFormat(String msgDataFormat) {
            this.msgDataFormat = msgDataFormat;
        }

//        /**
//         * 消息服务器配置的token（可选，用于消息加解密）
//         */
//        private String token;
//
//        /**
//         * 消息服务器配置的EncodingAESKey（可选，用于消息加解密）
//         */
//        private String aesKey;

    }

}
