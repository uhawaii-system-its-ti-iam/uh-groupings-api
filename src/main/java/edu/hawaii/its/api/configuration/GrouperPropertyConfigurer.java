package edu.hawaii.its.api.configuration;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@Configuration
public class GrouperPropertyConfigurer {

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
}