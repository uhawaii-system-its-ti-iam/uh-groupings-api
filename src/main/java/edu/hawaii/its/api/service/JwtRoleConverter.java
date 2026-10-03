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
 */
@Component
public class JwtRoleConverter {

    private static final Log log = LogFactory.getLog(JwtRoleConverter.class);

    /**
     * Convert a roles claim into authorities, discarding entries that name no known Role.
     * A null or absent claim yields none, leaving the user with no role and every check denying.
     */
    public List<GrantedAuthority> convert(Collection<String> roleClaims) {
        if (roleClaims == null) {
            return List.of();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String claim : roleClaims) {
            Optional<String> authorityName = Role.authorityNameFromClaim(claim);
            if (authorityName.isEmpty()) {
                log.warn("Discarding unrecognized role in JWT roles claim: " + claim);
                continue;
            }
            authorities.add(new SimpleGrantedAuthority(authorityName.get()));
        }
        return List.copyOf(authorities);
    }
}
