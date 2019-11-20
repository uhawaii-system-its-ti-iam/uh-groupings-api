package edu.hawaii.its.api.access;

public interface AuthorizationService {
    RoleHolder fetchRoles(String uhuuid, String username);
}
