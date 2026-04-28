package edu.hawaii.its.api.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for checking user roles from JWT token via SecurityContext.
 * This service is used for general authorization checks (is admin? is owner?)
 * without querying Grouper, as these roles are already embedded in the JWT token.
 *
 * For specific grouping ownership checks, use MemberService.isOwner(groupingPath, uhIdentifier)
 * which still queries Grouper.
 */
@Service
public class SecurityContextRoleService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_OWNER = "ROLE_OWNER";

    /**
     * Check if the current authenticated user has the ADMIN role.
     * This checks the JWT token roles stored in SecurityContext.
     * 
     * @return true if the current user has ROLE_ADMIN, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
    }

    /**
     * Check if the current authenticated user has the OWNER role.
     * This checks the JWT token roles stored in SecurityContext.
     * 
     * @return true if the current user has ROLE_OWNER, false otherwise
     */
    public boolean isCurrentUserOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_OWNER));
    }

    /**
     * Check if the current authenticated user has either ADMIN or OWNER role.
     * This checks the JWT token roles stored in SecurityContext.
     * 
     * @return true if the current user has ROLE_ADMIN or ROLE_OWNER, false otherwise
     */
    public boolean isCurrentUserAdminOrOwner() {
        return isCurrentUserAdmin() || isCurrentUserOwner();
    }
}


