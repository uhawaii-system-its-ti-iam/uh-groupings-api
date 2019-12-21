package edu.hawaii.its.api.access;

public interface AuthorizationService {
    RoleHolder fetchRoles(String uhUuid, String username);
}
