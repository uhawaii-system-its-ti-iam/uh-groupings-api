package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextRoleServiceTest {

    private static final String TEST_USER = "testiwta";

    private SecurityContextRoleService securityContextRoleService;

    @BeforeEach
    public void setUp() {
        securityContextRoleService = new SecurityContextRoleService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWith(String... authorities) {
        List<GrantedAuthority> granted = List.of(authorities).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_USER, null, granted));
    }

    @Test
    public void adminAuthorityGrantsTheAdminRole() {
        authenticateWith("ROLE_ADMIN", "ROLE_UH");

        assertTrue(securityContextRoleService.isCurrentUserAdmin());
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }

    @Test
    public void ownerAuthorityGrantsTheOwnerRole() {
        authenticateWith("ROLE_OWNER", "ROLE_UH");

        assertTrue(securityContextRoleService.isCurrentUserOwner());
        assertFalse(securityContextRoleService.isCurrentUserAdmin());
    }

    @Test
    public void aPlainUhUserHoldsNeitherRole() {
        authenticateWith("ROLE_UH");

        assertFalse(securityContextRoleService.isCurrentUserAdmin());
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }

    @Test
    public void anUnauthenticatedContextHoldsNeitherRole() {
        assertFalse(securityContextRoleService.isCurrentUserAdmin());
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }

    @Test
    public void noAuthoritiesHoldsNeitherRole() {
        authenticateWith();

        assertFalse(securityContextRoleService.isCurrentUserAdmin());
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }

    @Test
    public void anUnprefixedAuthorityDoesNotGrantTheAdminRole() {
        // An authority is always the ROLE_-prefixed form; JwtRoleConverter is what
        // applies the prefix. A bare ADMIN authority means something bypassed that
        // mapping, and must not be honoured here.
        authenticateWith("ADMIN");

        assertFalse(securityContextRoleService.isCurrentUserAdmin());
    }

    @Test
    public void aRoleIsNotGrantedByAPrefixMatch() {
        authenticateWith("ROLE_ADMINISTRATOR", "ROLE_OWNERS");

        assertFalse(securityContextRoleService.isCurrentUserAdmin());
        assertFalse(securityContextRoleService.isCurrentUserOwner());
    }
}
