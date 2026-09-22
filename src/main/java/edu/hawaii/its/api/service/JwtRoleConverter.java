package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import edu.hawaii.its.api.type.Role;

/**
 * Maps the "roles" claim of a JWT onto Spring Security authorities.
 *
 * This is the one place where the ROLE_ prefix is applied, mirroring Spring's own
 * JwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"). Clients send plain role
 * names such as ADMIN; adding the prefix Spring expects on an authority is this
 * application's concern, not the client's.
 */
@Component
public class JwtRoleConverter {

    private static final Log log = LogFactory.getLog(JwtRoleConverter.class);

    /**
     * Convert the entries of a roles claim into authorities, discarding any entry that
     * does not name a known Role. A null or absent claim yields no authorities, leaving
     * the user authenticated but holding no role, which every role check then denies.
     */
    public List<GrantedAuthority> convert(Collection<String> roleClaims) {
        if (roleClaims == null) {
            return List.of();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String claim : roleClaims) {
            Optional<Role> role = Role.fromClaim(claim);
            if (role.isEmpty()) {
                log.warn("Discarding unrecognized role in JWT roles claim: " + claim);
                continue;
            }
            authorities.add(new SimpleGrantedAuthority(role.get().authorityName()));
        }
        return List.copyOf(authorities);
    }
}
