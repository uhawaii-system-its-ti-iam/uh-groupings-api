package edu.hawaii.its.api.configuration;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@Configuration
@Profile("dockerhost")
public class VaultConfig {

    public static final Log log = LogFactory.getLog(VaultConfig.class);

    @Value("${grouperClient.webService.password:}")
    private String password;

    @PostConstruct
    public void init() {
        if (password.equals("")) {
            log.warn("Grouper client web service password from Vault is empty");
            return;
        }
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();
        setOverride(config, "grouperClient.webService.password", password);
    }

    private void setOverride(GrouperClientConfig config, String key, String value) {
        if (value != null) {
            config.propertiesOverrideMap().put(key, value);
        }
    }
}
