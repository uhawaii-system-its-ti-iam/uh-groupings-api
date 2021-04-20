package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;

import java.util.List;

public interface MembershipService {

    List<Membership> getMembershipResults(String owner, String uid);

    List<AddMemberResult> addGroupingMembers(String ownerUsername, String groupingPath, List<String> usersToAdd);

    List<AddMemberResult> addIncludeMembers(String ownerUsername, String groupingPath, List<String> usersToAdd);

    List<AddMemberResult> addExcludeMembers(String ownerUsername, String groupingPath, List<String> usersToAdd);

    List<RemoveMemberResult> removeGroupingMembers(String ownerUsername, String groupPath, List<String> usersToRemove);

    List<GroupingsServiceResult> addGroupMember(String ownerUsername, String groupingPath, String userToAdd);

    List<GroupingsServiceResult> deleteGroupingMember(String ownerUsername, String groupingPath, String userIdentifier);

    GroupingsServiceResult deleteGroupMember(String ownerUsername, String groupPath, String userToDelete);

    List<String> listOwned(String adminUsername, String username);

    GroupingsServiceResult addAdmin(String adminUsername, String adminToAddUsername);

    List<GroupingsServiceResult> removeFromGroups(String adminUsername, String userToRemove, List<String> GroupPaths);

    List<GroupingsServiceResult> resetGroup(String ownerUsername, String path, List<String> includeIdentifier,
            List<String> excludeIdentifier);

    GroupingsServiceResult deleteAdmin(String adminUsername, String adminToDeleteUsername);

    List<AddMemberResult> optIn(String currentUser, String groupingPath, String uid);

    List<AddMemberResult> optOut(String currentUser, String groupingPath, String uid);

    boolean isGroupCanOptIn(String username, String groupPath);

    boolean isGroupCanOptOut(String username, String groupPath);

    //do not include in REST controller
    GroupingsServiceResult updateLastModified(String groupPath);

    GroupingsServiceResult addSelfOpted(String groupPath, String username);

    GroupingsServiceResult removeSelfOpted(String groupPath, String username);

    boolean isUhUuid(String username);

    boolean canOpt(String path);
}
