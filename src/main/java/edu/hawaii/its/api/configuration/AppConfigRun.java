package edu.hawaii.its.api.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableAsync;

@Profile(value = { "localhost", "test", "integrationTest", "qa" })
@Configuration
@EnableAsync
@ComponentScan(basePackages = "edu.hawaii.its")
@PropertySources({
        @PropertySource("classpath:custom.properties"),
        @PropertySource(value = "file:${user.home}/.${user.name}-conf/uh-groupings-api-overrides.properties",
                ignoreResourceNotFound = true)
})
public class AppConfigRun {
    // Empty.
}
