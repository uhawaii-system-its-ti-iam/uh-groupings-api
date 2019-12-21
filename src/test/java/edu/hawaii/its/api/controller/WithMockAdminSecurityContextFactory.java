package edu.hawaii.its.api.controller;

import edu.hawaii.its.api.access.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class WithMockAdminSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAdminUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAdminUser adminUser) {

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : adminUser.roles()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        User user = new User(adminUser.username(), authorities);
        user.setUhUuid(adminUser.uhUuid());

        final Authentication auth =
                new UsernamePasswordAuthenticationToken(user, "pw", user.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        return context;
    }

}