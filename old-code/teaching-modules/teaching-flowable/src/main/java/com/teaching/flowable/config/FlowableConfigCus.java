package com.teaching.flowable.config;

import org.flowable.engine.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Administrator
 */
@Configuration
@RefreshScope
public class FlowableConfigCus {

    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
    private String driver;
    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String userName;
    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String password;
    @Bean
    @Primary
    public ProcessEngine processEngine() {
        ProcessEngineConfiguration processEngineConfiguration =
                ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngineConfiguration.setJdbcUrl(url);
        processEngineConfiguration.setJdbcDriver(driver);
        processEngineConfiguration.setJdbcUsername(userName);
        processEngineConfiguration.setJdbcPassword(password);
        return processEngineConfiguration.buildProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService() {
        return processEngine().getRepositoryService();
    }
    @Bean
    public IdentityService identityService() {
        return processEngine().getIdentityService();
    }

    @Bean
    public RuntimeService runtimeService() {
        return processEngine().getRuntimeService();
    }

    @Bean
    public TaskService taskService() {
        return processEngine().getTaskService();
    }

    @Bean
    public HistoryService historyService() {
        return processEngine().getHistoryService();
    }

    @Bean
    public ManagementService managementService() {
        return processEngine().getManagementService();
    }
    @Bean
    public FormService formService() {
        return processEngine().getFormService();
    }
}
