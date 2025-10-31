package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mocks an authorized user with standard OWNER privileges.
 * Used to populate security context authentication.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUhOwnerSecurityContextFactory.class)
public @interface WithMockUhOwner {}