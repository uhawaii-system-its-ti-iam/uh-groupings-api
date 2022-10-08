package edu.hawaii.its.api.service;

import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.UIAddMemberResults;
import edu.hawaii.its.api.type.UIRemoveMemberResults;
import edu.hawaii.its.api.type.UpdateTimestampResult;

import java.util.List;

public interface MembershipService {

    List<UIAddMemberResults> addOwnerships(String groupingPath, String ownerUsername, List<String> newOwnerUsername);

    List<UIRemoveMemberResults> removeOwnerships(String groupingPath, String actor, List<String> ownersToRemove);

    List<Membership> membershipResults(String currentUser, String uid);

    List<UIAddMemberResults> addGroupMembers(String currentUser, String groupPath, List<String> usersToAdd);

    GroupingsMoveMembersResult addGroupMembersNewImplementation(String currentUser, String groupPath, List<String> usersToAdd);

    List<UIAddMemberResults> addIncludeMembers(String currentUser, String groupingPath, List<String> usersToAdd);

    List<UIAddMemberResults> addExcludeMembers(String currentUser, String groupingPath, List<String> usersToAdd);

    List<UIRemoveMemberResults> removeGroupMembers(String currentUser, String groupPath, List<String> usersToRemove);

    List<UIRemoveMemberResults> removeIncludeMembers(String currentUser, String groupingPath, List<String> usersToRemove);

    List<UIRemoveMemberResults> removeExcludeMembers(String currentUser, String groupingPath, List<String> usersToRemove);

    UIAddMemberResults optIn(String currentUser, String groupingPath, String uid);

    UIAddMemberResults optOut(String currentUser, String groupingPath, String uid);

    GroupingsAddResult addAdmin(String currentUser, String adminToAdd);

    GroupingsRemoveResult removeAdmin(String currentUser, String adminToRemove);

    List<UIRemoveMemberResults> removeFromGroups(String adminUsername, String userToRemove, List<String> groupPaths);

    List<UIRemoveMemberResults> resetGroup(String currentUser, String path, List<String> uhNumbersInclude,
            List<String> uhNumbersExclude);

    UpdateTimestampResult updateLastModified(String groupPath);

    UpdateTimestampResult updateLastModifiedTimestamp(String dateTime, String groupPath);

    Integer getNumberOfMemberships(String currentUser, String uid);
    
    UIAddMemberResults addMember(String currentUser, String userToAdd, String removalPath, String additionPath);
}