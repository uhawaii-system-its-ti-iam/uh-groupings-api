package edu.hawaii.its.api.configuration;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.hawaii.its.api.service.ExecutorService;
import edu.hawaii.its.api.service.RetryExecutorService;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.GrouperService;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@Configuration
public class GrouperPropertyConfigurer {

    public static final Log log = LogFactory.getLog(GrouperPropertyConfigurer.class);

    @Value("${grouperClient.webService.url:}")
    private String webServiceUrl;

    @Value("${grouperClient.webService.login:}")
    private String webServiceLogin;

    @Value("${grouperClient.webService.password:}")
    private String webServicePassword;

    @PostConstruct
    public void init() {
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        setOverride(config, "grouperClient.webService.url", webServiceUrl);
        setOverride(config, "grouperClient.webService.login", webServiceLogin);
        setOverride(config, "grouperClient.webService.password", webServicePassword);
    }

    private void setOverride(GrouperClientConfig config, String key, String value) {
        if (value != null) {
            config.propertiesOverrideMap().put(key, value);
        }
    }

    @Bean(name = "grouperService")
    @ConditionalOnProperty(name = "grouping.api.server.type", havingValue = "GROUPER", matchIfMissing = true)
    public GrouperService grouperApiService(ExecutorService executorService, RetryExecutorService retryExecutorService) {
        log.debug("REAL Grouper Api Service Started");
        return new GrouperApiService(executorService, retryExecutorService);
    }
}