package edu.hawaii.its.api.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile(value = { "localhost", "test", "integrationTest", "qa", "default", "dev", "localTest" })
@Configuration
@ComponentScan(basePackages = "edu.hawaii.its")
@PropertySource("classpath:custom.properties")
public class AppConfig {
    // Empty.
}
