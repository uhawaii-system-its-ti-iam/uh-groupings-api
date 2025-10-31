package edu.hawaii.its.api.configuration;

import edu.hawaii.its.api.controller.WithMockUhAdminSecurityContextFactory;
import edu.hawaii.its.api.controller.WithMockUhOwnerSecurityContextFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    public WithMockUhAdminSecurityContextFactory withMockUhAdminSecurityContextFactory() {
        return new WithMockUhAdminSecurityContextFactory();
    }

    @Bean
    public WithMockUhOwnerSecurityContextFactory withMockUhOwnerSecurityContextFactory() {
        return new WithMockUhOwnerSecurityContextFactory();
    }
}
