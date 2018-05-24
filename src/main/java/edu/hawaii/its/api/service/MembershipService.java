package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import java.util.List;

public interface MembershipService {

    public List<GroupingsServiceResult> addGroupingMember(String ownerUsername, String groupingPath, String userToAdd);

    public List<GroupingsServiceResult> addGroupingMemberByUsername(String ownerUsername, String groupingPath,
            String userToAddUsername);

    public List<GroupingsServiceResult> addGroupingMemberByUuid(String ownerUsername, String groupingPath,
            String userToAddUuid);

    public List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupingPath, String userToAdd);

    public List<GroupingsServiceResult> addGroupMemberByUsername(String ownerUsername, String groupPath,
            String userToAddUsername);

    public List<GroupingsServiceResult> addGroupMemberByUuid(String ownerUsername, String group, String userToAddUuid);

    public List<GroupingsServiceResult> addGroupMembers(String ownerUsername, String groupingPath, List<String> usersToAdd);

    public List<GroupingsServiceResult> addGroupMembersByUsername(String ownerUsername, String group,
            List<String> usersToAddUsername);

    public List<GroupingsServiceResult> addGroupMembersByUuid(String ownerUsername, String group,
            List<String> usersToAddUuid);

    public List<GroupingsServiceResult> deleteGroupingMemberByUsername(String ownerUsername, String groupingPath,
            String userToDeleteUsername);

    public List<GroupingsServiceResult> deleteGroupingMemberByUuid(String ownerUsername, String groupingPath,
            String userToDeleteUuid);

    public GroupingsServiceResult deleteGroupMemberByUsername(String ownerUsername, String groupPath,
            String userToDeleteUsername);

    public GroupingsServiceResult deleteGroupMemberByUuid(String ownerUsername, String groupPath,
            String userToDeleteUuid);

    //todo deleteGroupMembersByUuid
    //todo deleteGroupMembersByUsername

    public List<String> listOwned(String adminUsername, String username);

    public GroupingsServiceResult addAdmin(String adminUsername, String adminToAddUsername);

    public GroupingsServiceResult deleteAdmin(String adminUsername, String adminToDeleteUsername);

    public List<GroupingsServiceResult> optIn(String username, String groupingPath);

    public List<GroupingsServiceResult> optOut(String username, String groupingPath);

    public boolean groupOptInPermission(String username, String groupPath);

    public boolean groupOptOutPermission(String username, String groupPath);

    //do not include in REST controller
    public GroupingsServiceResult updateLastModified(String groupPath);

    public GroupingsServiceResult addSelfOpted(String groupPath, String username);

    public GroupingsServiceResult removeSelfOpted(String groupPath, String username);
}
