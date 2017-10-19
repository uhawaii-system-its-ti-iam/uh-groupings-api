package edu.hawaii.its.api.access;

public interface AuthorizationService {
   public RoleHolder fetchRoles(String uhuuid, String username);
}