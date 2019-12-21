package edu.hawaii.its.api.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAdminSecurityContextFactory.class)
public @interface WithMockAdminUser {
//    String username() default "Admin";
    String username() default "_grouping_api_2";

    //todo Hopefully there is no functional difference where we need ROLE_APP or something similar
    String[] roles() default {"ROLE_ADMIN"};

    //todo Change long to String for consistency w/ Grouper
    String uhUuid() default "12345678L";

    String name() default "UH Groupings API 2";
}
