package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;

import java.util.List;

public interface MembershipService {

    boolean isUhUuid(String username);
   
    List<Membership> getMembershipResults(String owner, String uid);

    List<AddMemberResult> addGroupMembers(String currentUser, String groupPath, List<String> usersToAdd);

    List<AddMemberResult> addIncludeMembers(String currentUser, String groupingPath, List<String> usersToAdd);

    List<AddMemberResult> addExcludeMembers(String currentUser, String groupingPath, List<String> usersToAdd);

    List<RemoveMemberResult> removeGroupMembers(String currentUser, String groupPath, List<String> usersToRemove);

    List<RemoveMemberResult> removeIncludeMembers(String currentUser, String groupingPath, List<String> usersToRemove);

    List<RemoveMemberResult> removeExcludeMembers(String currentUser, String groupingPath, List<String> usersToRemove);

    List<AddMemberResult> optIn(String currentUser, String groupingPath, String uid);

    List<AddMemberResult> optOut(String currentUser, String groupingPath, String uid);

    GroupingsServiceResult addAdmin(String adminUsername, String adminToAddUsername);

    GroupingsServiceResult removeAdmin(String adminUsername, String adminToRemoveUsername);

    List<GroupingsServiceResult> removeFromGroups(String adminUsername, String userToRemove, List<String> GroupPaths);

    List<GroupingsServiceResult> resetGroup(String currentUser, String path, List<String> includeIdentifier,
            List<String> excludeIdentifier);

    boolean isGroupCanOptIn(String username, String groupPath);

    boolean isGroupCanOptOut(String username, String groupPath);

    //do not include in REST controller
    GroupingsServiceResult updateLastModified(String groupPath);

    GroupingsServiceResult addSelfOpted(String groupPath, String username);

    GroupingsServiceResult removeSelfOpted(String groupPath, String username);

    Integer getNumberOfMemberships(String currentUser, String uid);
}
