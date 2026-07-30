package edu.hawaii.its.api.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Role;

/**
 * Service for checking user roles from JWT token via SecurityContext.
 * This service is used for general authorization checks (is admin? is owner?)
 * without querying Grouper, as these roles are already embedded in the JWT token.
 *
 * The authorities compared here are produced by JwtRoleConverter, which is what applies
 * the ROLE_ prefix; Role.authorityName() is the single definition of that authority name.
 *
 * For specific grouping ownership checks, use MemberService.isOwner(groupingPath, uhIdentifier)
 * which still queries Grouper.
 */
@Service
public class SecurityContextRoleService {

    /**
     * Check if the current authenticated user has the ADMIN role.
     * This checks the JWT token roles stored in SecurityContext.
     *
     * @return true if the current user has the ADMIN role, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Check if the current authenticated user has the OWNER role.
     * This checks the JWT token roles stored in SecurityContext.
     *
     * @return true if the current user has the OWNER role, false otherwise
     */
    public boolean isCurrentUserOwner() {
        return hasRole(Role.OWNER);
    }

    private boolean hasRole(Role role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(authority -> role.authorityName().equals(authority.getAuthority()));
    }
}
