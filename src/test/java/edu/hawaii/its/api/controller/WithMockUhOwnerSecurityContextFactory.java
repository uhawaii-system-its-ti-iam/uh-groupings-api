package edu.hawaii.its.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Stream;

public class WithMockUhOwnerSecurityContextFactory implements WithSecurityContextFactory<WithMockUhOwner> {

    @Value("${groupings.api.localhost.user}")
    private String TEST_USER;

    @Override
    public SecurityContext createSecurityContext(WithMockUhOwner annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        List<SimpleGrantedAuthority> authorities = Stream.of(
                new SimpleGrantedAuthority("ROLE_OWNER"),
                new SimpleGrantedAuthority("ROLE_UH")
        ).toList();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(TEST_USER, null, authorities);

        context.setAuthentication(authToken);
        return context;
    }
}