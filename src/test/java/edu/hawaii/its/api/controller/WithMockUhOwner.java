package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithMockUser;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mocks a logged-in user with standard TEST_USER privileges.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(
        username = "test_user",
        authorities = {"ROLE_OWNER", "ROLE_UH"}
)
public @interface WithMockUhOwner {}