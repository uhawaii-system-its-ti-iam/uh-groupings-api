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
import edu.hawaii.its.api.util.Strings;

/**
 * Maps the "roles" claim of a JWT onto Spring Security authorities.
 */
@Component
public class JwtRoleConverter {

    private static final Log log = LogFactory.getLog(JwtRoleConverter.class);

    private static final int MAX_LOGGED_CLAIM_LENGTH = 80;

    /** Control (Cc), format (Cf, e.g. bidi overrides) and line/paragraph separator characters. */
    private static final String UNLOGGABLE_CHARACTERS = "[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]";

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
            if (Strings.isEmpty(claim)) {
                continue;
            }
            Optional<String> authorityName = Role.authorityNameFromClaim(claim);
            if (authorityName.isEmpty()) {
                log.warn("Discarding unrecognized role in JWT roles claim: " + loggable(claim));
                continue;
            }
            authorities.add(new SimpleGrantedAuthority(authorityName.get()));
        }
        return List.copyOf(authorities);
    }

    /**
     * Truncate a claim and replace control and line-separator characters so it cannot forge log lines.
     */
    static String loggable(String claim) {
        return Strings.truncate(claim, MAX_LOGGED_CLAIM_LENGTH).replaceAll(UNLOGGABLE_CHARACTERS, "_");
    }
}
