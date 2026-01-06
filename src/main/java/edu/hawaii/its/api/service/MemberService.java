package edu.hawaii.its.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;

@Service
public class MemberService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    private final GrouperService grouperService;
    private final SecurityContextRoleService securityContextRoleService;

    public MemberService(GrouperService grouperService, SecurityContextRoleService securityContextRoleService) {
        this.grouperService = grouperService;
        this.securityContextRoleService = securityContextRoleService;
    }

    /**
     * Check if a user is an admin by querying Grouper.
     * This method is used by role population endpoints (/is-admin) which need to query Grouper
     * to populate JWT tokens during login.
     * 
     * For general authorization checks of the current user, use isCurrentUserAdmin() instead.
     * 
     * @param uhIdentifier the user identifier to check
     * @return true if the user is an admin according to Grouper
     */
    public boolean isAdmin(String uhIdentifier) {
        return isMember(GROUPING_ADMINS, uhIdentifier);
    }

    /**
     * Check if a user is an owner by querying Grouper.
     * This method is used by role population endpoints (/is-owner) which need to query Grouper
     * to populate JWT tokens during login.
     * 
     * For general authorization checks of the current user, use isCurrentUserOwner() instead.
     * 
     * @param uhIdentifier the user identifier to check
     * @return true if the user is an owner according to Grouper
     */
    public boolean isOwner(String uhIdentifier) {
        return isMember(OWNERS_GROUP, uhIdentifier);
    }

    /**
     * Check if the current authenticated user has the ADMIN role from JWT token.
     * This method uses SecurityContext (populated by JWT) instead of querying Grouper.
     * Use this for general authorization checks in service methods.
     * 
     * @return true if the current user has ROLE_ADMIN in their JWT token
     */
    public boolean isCurrentUserAdmin() {
        return securityContextRoleService.isCurrentUserAdmin();
    }

    /**
     * Check if the current authenticated user has the OWNER role from JWT token.
     * This method uses SecurityContext (populated by JWT) instead of querying Grouper.
     * Use this for general authorization checks in service methods.
     * 
     * @return true if the current user has ROLE_OWNER in their JWT token
     */
    public boolean isCurrentUserOwner() {
        return securityContextRoleService.isCurrentUserOwner();
    }

    /**
     * Check if the current authenticated user has either ADMIN or OWNER role from JWT token.
     * This method uses SecurityContext (populated by JWT) instead of querying Grouper.
     * Use this for general authorization checks in service methods.
     * 
     * @return true if the current user has ROLE_ADMIN or ROLE_OWNER in their JWT token
     */
    public boolean isCurrentUserAdminOrOwner() {
        return securityContextRoleService.isCurrentUserAdminOrOwner();
    }

    public boolean isOwner(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.OWNERS.value(), uhIdentifier);
    }

    public boolean isBasis(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.BASIS.value(), uhIdentifier);
    }

    public boolean isInclude(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.INCLUDE.value(), uhIdentifier);
    }

    public boolean isExclude(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.EXCLUDE.value(), uhIdentifier);
    }

    public boolean isMember(String groupPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = grouperService.hasMemberResults(groupPath, uhIdentifier);

        List<HasMemberResult> results = hasMembersResults.getResults();

        if (results.isEmpty()) {
            return false;
        }
        return results.get(0).getResultCode().equals("IS_MEMBER");
    }
}
