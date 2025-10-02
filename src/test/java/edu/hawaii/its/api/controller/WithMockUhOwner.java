package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mocks a logged-in user with standard OWNER privileges.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUhOwnerSecurityContextFactory.class)
public @interface WithMockUhOwner {}