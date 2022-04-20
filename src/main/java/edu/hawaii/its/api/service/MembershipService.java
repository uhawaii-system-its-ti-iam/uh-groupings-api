package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.UpdateTimestampResult;

import java.util.List;

public interface MembershipService {

    List<AddMemberResult> addOwnerships(String groupingPath, String ownerUsername, List<String> newOwnerUsername);

    List<RemoveMemberResult> removeOwnerships(String groupingPath, String actor, List<String> ownersToRemove);

    List<Membership> membershipResults(String currentUser, String uid);

    List<AddMemberResult> addGroupMembers(String currentUser, String groupPath, List<String> usersToAdd);

    List<AddMemberResult> addIncludeMembers(String currentUser, String groupingPath, List<String> usersToAdd);

    List<AddMemberResult> addExcludeMembers(String currentUser, String groupingPath, List<String> usersToAdd);

    List<RemoveMemberResult> removeGroupMembers(String currentUser, String groupPath, List<String> usersToRemove);

    List<RemoveMemberResult> removeIncludeMembers(String currentUser, String groupingPath, List<String> usersToRemove);

    List<RemoveMemberResult> removeExcludeMembers(String currentUser, String groupingPath, List<String> usersToRemove);

    List<AddMemberResult> optIn(String currentUser, String groupingPath, String uid);

    List<AddMemberResult> optOut(String currentUser, String groupingPath, String uid);

    AddMemberResult addAdmin(String currentUser, String adminToAdd);

    RemoveMemberResult removeAdmin(String currentUser, String adminToRemove);

    List<RemoveMemberResult> removeFromGroups(String adminUsername, String userToRemove, List<String> groupPaths);

    List<RemoveMemberResult> resetGroup(String currentUser, String path, List<String> uhNumbersInclude,
            List<String> uhNumbersExclude);

    UpdateTimestampResult updateLastModified(String groupPath);

    UpdateTimestampResult updateLastModifiedTimestamp(String dateTime, String groupPath);

    Integer getNumberOfMemberships(String currentUser, String uid);
}