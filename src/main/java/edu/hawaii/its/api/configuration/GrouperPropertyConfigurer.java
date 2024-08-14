package edu.hawaii.its.api.configuration;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@Configuration
public class GrouperPropertyConfigurer {

    private final Environment env;

    @Autowired
    public GrouperPropertyConfigurer(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        setOverride(config, "grouperClient.webService.url");
        setOverride(config, "grouperClient.webService.login");
        setOverride(config, "grouperClient.webService.password");
    }

    private void setOverride(GrouperClientConfig config, String key) {
        String envKey = convertToEnvKey(key);

        // Check for the environment variable first. If using the project's
        // Docker container, GROUPERCLIENT_WEBSERVICE_PASSWORD may be set.
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            config.propertiesOverrideMap().put(key, envValue);
        } else if (isOverride(key)) {
            config.propertiesOverrideMap().put(key, env.getProperty(key));
        }
    }

    // Helper method to convert property key to environment variable name.
    private String convertToEnvKey(String key) {
        return key.replace('.', '_').toUpperCase();
    }

    // Check to see an override exists.
    private boolean isOverride(String key) {
        return env.containsProperty(key);
    }
}
