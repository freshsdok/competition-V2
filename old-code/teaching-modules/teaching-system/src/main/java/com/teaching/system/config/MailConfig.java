package com.teaching.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;
    
    @Value("${spring.mail.port}")
    private int port;
    
    @Value("${spring.mail.username}")
    private String username;
    
    @Value("${spring.mail.password}")
    private String password;
    
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String smtpAuth;
    
    @Value("${spring.mail.properties.mail.smtp.ssl.enable:true}")
    private String smtpSslEnable;
    
    @Bean
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.ssl.enable", smtpSslEnable);
        props.put("mail.smtp.ssl.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.socketFactory.port", String.valueOf(port));
        
        return mailSender;
    }
}