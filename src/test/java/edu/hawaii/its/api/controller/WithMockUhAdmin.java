package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithMockUser;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mocks a logged-in user with ADMIN privileges.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(
        username = "admin",
        authorities = {"ROLE_ADMINISTRATOR", "ROLE_UH"}
)
public @interface WithMockUhAdmin {}
