package edu.hawaii.its.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;
import java.util.stream.Stream;

public class WithMockUhAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockUhAdmin> {

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Override
    public SecurityContext createSecurityContext(WithMockUhAdmin annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        List<SimpleGrantedAuthority> authorities = Stream.of(
                new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"),
                new SimpleGrantedAuthority("ROLE_UH")
        ).toList();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(ADMIN, null, authorities);

        context.setAuthentication(authToken);
        return context;
    }

}
