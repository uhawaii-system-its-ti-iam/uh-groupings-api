package edu.hawaii.its.api.type;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The roles carried in the "roles" claim of a JWT. These names mirror the UI's Role enum
 * (ui/src/lib/access/role.ts); the ROLE_ authority prefix is this application's, not the UI's.
 */
public enum Role {

    ADMIN,
    ANONYMOUS,
    DEPARTMENTAL,
    OWNER,
    UH;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    /** Built once per constant at class initialization; the canonical form claims are matched against. */
    private final String authorityName = AUTHORITY_PREFIX + name();

    /** The same authority names, indexed once at class initialization so a match is one hash lookup. */
    private static final Map<String, String> AUTHORITY_NAMES = authorityNames();

    private static Map<String, String> authorityNames() {
        Map<String, String> names = new HashMap<>();
        for (Role role : values()) {
            names.put(role.authorityName, role.authorityName);
        }
        return Map.copyOf(names);
    }

    public String authorityName() {
        return authorityName;
    }

    /**
     * Resolve a roles claim entry to its authority name, or empty if it names no known Role.
     */
    public static Optional<String> authorityNameFromClaim(String claim) {
        if (claim == null) {
            return Optional.empty();
        }
        String name = claim.trim().toUpperCase(Locale.ROOT);

        String finalName = name.startsWith(AUTHORITY_PREFIX) ? name : AUTHORITY_PREFIX + name;

        // Match the (possibly prefixed) claim against the standard authority names of all roles.
        return Optional.ofNullable(AUTHORITY_NAMES.get(finalName));
    }
}