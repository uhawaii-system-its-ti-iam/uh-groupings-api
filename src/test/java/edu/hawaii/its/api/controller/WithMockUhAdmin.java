package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mocks an authorized user with ADMIN privileges.
 * Used to populate security context authentication.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUhAdminSecurityContextFactory.class)
public @interface WithMockUhAdmin {}
