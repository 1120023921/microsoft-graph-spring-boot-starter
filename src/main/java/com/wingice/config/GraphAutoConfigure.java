package com.wingice.config;

import com.wingice.service.IAuthenticatedClientService;
import com.wingice.service.IGraphEventService;
import com.wingice.service.impl.AuthenticatedClientServiceImpl;
import com.wingice.service.impl.GraphEventServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 胡昊
 * Description: graph自动装配
 * Date: 2019/5/23
 * Time: 21:18
 * Create: DoubleH
 */
@Configuration
@EnableConfigurationProperties(GraphProperties.class)
public class GraphAutoConfigure {

    private final GraphProperties properties;

    @Autowired
    public GraphAutoConfigure(GraphProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "com.wingice.graph", name = {"tokenEndpoint", "clientId", "secret", "scope", "grantType"})
    public IAuthenticatedClientService authenticatedClientService() {
        return new AuthenticatedClientServiceImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(IAuthenticatedClientService.class)
    public IGraphEventService graphEventService(@Autowired IAuthenticatedClientService authenticatedClientService) {
        return new GraphEventServiceImpl(authenticatedClientService);
    }
}
