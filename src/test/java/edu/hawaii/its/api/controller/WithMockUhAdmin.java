package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mocks a logged-in user with ADMIN privileges.
 */
@Retention(RetentionPolicy.RUNTIME)
<<<<<<< HEAD
@WithMockUser(
        username = "test_admin_user",
        authorities = {"ROLE_ADMINISTRATOR", "ROLE_UH"}
)
=======
@WithSecurityContext(factory = WithMockUhAdminSecurityContextFactory.class)
>>>>>>> 98484f84a (Add SecurityConfigTest)
public @interface WithMockUhAdmin {}
