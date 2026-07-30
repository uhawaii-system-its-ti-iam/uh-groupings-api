package edu.hawaii.its.api.type;

import java.util.Locale;
import java.util.Optional;

/**
 * The roles carried in the "roles" claim of a JWT.
 *
 * These names mirror the UI's Role enum (ui/src/lib/access/role.ts) and are the wire
 * contract between the UI and this API. Spring Security's ROLE_ authority prefix is not
 * part of that contract: it belongs to this application and is applied by authorityName(),
 * following the same convention as edu.hawaii.its.groupings.access.Role in the UI project.
 */
public enum Role {

    ADMIN,
    ANONYMOUS,
    DEPARTMENTAL,
    OWNER,
    UH;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    /**
     * The Spring Security authority name for this role: the plain role name with the
     * ROLE_ prefix applied (e.g. ADMIN becomes ROLE_ADMIN). This is the exact string
     * stored as a GrantedAuthority and compared against during role checks, so it is the
     * single place the ROLE_ prefix is defined.
     */
    public String authorityName() {
        return AUTHORITY_PREFIX + name();
    }

    /**
     * Resolve a single entry of a JWT roles claim to a Role.
     *
     * Both the plain contract form ("ADMIN") and the prefixed form ("ROLE_ADMIN") are
     * accepted so that a UI still sending the prefixed form keeps working; this lets the
     * UI and API deploy independently. Anything unrecognized yields an empty Optional
     * rather than an authority minted from an unknown name.
     */
    public static Optional<Role> fromClaim(String claim) {
        if (claim == null) {
            return Optional.empty();
        }
        String name = claim.trim().toUpperCase(Locale.ROOT);
        if (name.startsWith(AUTHORITY_PREFIX)) {
            name = name.substring(AUTHORITY_PREFIX.length());
        }
        for (Role role : values()) {
            if (role.name().equals(name)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }
}
