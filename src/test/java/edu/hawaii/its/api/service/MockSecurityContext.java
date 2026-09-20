package edu.hawaii.its.api.service;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityContextTestHelper {

    public static void setAdminContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_UH")
        );
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        authorities
                )
        );

        SecurityContextHolder.setContext(context);
    }

    public static void setOwnerContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_OWNER"),
                new SimpleGrantedAuthority("ROLE_UH")
        );
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "owner",
                        null,
                        authorities
                )
        );

        SecurityContextHolder.setContext(context);
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
