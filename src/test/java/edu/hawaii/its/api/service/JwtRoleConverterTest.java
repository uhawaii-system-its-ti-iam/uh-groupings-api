package edu.hawaii.its.api.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtRoleConverterTest {

    private JwtRoleConverter jwtRoleConverter;

    @BeforeEach
    public void setUp() {
        jwtRoleConverter = new JwtRoleConverter();
    }

    @Test
    public void convertAppliesTheAuthorityPrefixToPlainRoleNames() {
        List<GrantedAuthority> authorities = jwtRoleConverter.convert(List.of("ADMIN", "UH"));

        assertThat(authorities, equalTo(List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_UH"))));
    }

    @Test
    public void convertDoesNotDoublePrefixRolesFromAnOlderUi() {
        List<GrantedAuthority> authorities = jwtRoleConverter.convert(List.of("ROLE_ADMIN", "ROLE_UH"));

        assertThat(authorities, equalTo(List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_UH"))));
    }

    @Test
    public void convertDiscardsUnknownRolesButKeepsTheRest() {
        List<GrantedAuthority> authorities = jwtRoleConverter.convert(Arrays.asList("ADMIN", "SUPERUSER", "UH"));

        assertThat(authorities, equalTo(List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_UH"))));
    }

    @Test
    public void convertNeverMintsAnAuthorityFromAnArbitraryClaim() {
        List<GrantedAuthority> authorities =
                jwtRoleConverter.convert(Arrays.asList("ROLE_SUPERUSER", "', OR 1=1 --", null, ""));

        assertTrue(authorities.isEmpty());
    }

    @Test
    public void convertReturnsNoAuthoritiesForAnAbsentOrEmptyClaim() {
        assertTrue(jwtRoleConverter.convert(null).isEmpty());
        assertTrue(jwtRoleConverter.convert(List.of()).isEmpty());
    }

    @Test
    public void convertReturnsAnImmutableList() {
        List<GrantedAuthority> authorities = jwtRoleConverter.convert(List.of("ADMIN"));

        assertThrows(UnsupportedOperationException.class,
                () -> authorities.add(new SimpleGrantedAuthority("ROLE_OWNER")));
    }

    @Test
    public void convertPreservesTheOrderOfTheClaim() {
        List<String> claim = new ArrayList<>(List.of("UH", "OWNER", "ADMIN"));

        List<GrantedAuthority> authorities = jwtRoleConverter.convert(claim);

        assertThat(authorities, equalTo(List.of(
                new SimpleGrantedAuthority("ROLE_UH"),
                new SimpleGrantedAuthority("ROLE_OWNER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
}
