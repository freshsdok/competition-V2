package com.teaching.imclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@ComponentScan(basePackages = {"com.teaching.imclient", "com.teaching.imcommon"})
public class IMAutoConfiguration {

}
